import { Location } from '@angular/common';
import { Component, inject, OnDestroy, OnInit, QueryList, ViewChildren } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import { NbTooltipDirective, NbToastrService } from '@nebular/theme';
import { Subject, takeUntil } from 'rxjs';

import { BotConfigurationService } from '../../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { CompressorSettings } from '../../configuration/compressor-settings/models/compressor-settings';
import { DocumentSearchType } from '../../rag/rag-settings/models/engines-configurations';
import {
  CompressionStage,
  RunComparison,
  SearchFunnel,
  SearchRequest,
  SearchResponse,
  SearchResultChunk,
  SearchRun,
  VectorStoreCapabilities,
  VectorStoreIndex
} from '../models/vector-store-inspection.models';
import { VectorStoreInspectionService } from '../services/vector-store-inspection.service';
import { VectorStoreInspectionStateService } from '../services/vector-store-inspection-state.service';
import { buildRunComparison } from '../utils/run-comparison';

/** Default retrieval width. Deliberately larger than k so the funnel has something to show. */
const DEFAULT_FETCH_K = 50;

/** Matches the runtime default of maxDocumentsRetrieved. */
const DEFAULT_K = 4;

/** Compressor thresholds, overridden as soon as the bot configuration arrives. */
const DEFAULT_MIN_SCORE = 0.4;
const DEFAULT_MAX_DOCUMENTS = 4;

/** Navigation state handed over by the dialog logger. */
interface DiagnosticNavigationState {
  question?: string;
  condensed_question?: string;
  key_words?: string[];
  indexName?: string;
  k?: number;
}

/** Shape of the compressor setting fields this view tweaks. */
interface CompressorThresholds {
  minScore?: number;
  maxDocuments?: number;
  fillToMaxDocuments?: boolean;
}

/**
 * Retrieval diagnostic: runs a search and shows the whole funnel, from the
 * candidates pulled out of the store down to the chunks actually handed to the
 * answering model.
 *
 * The screen is laid out along the intended pipeline, not the current one:
 * search, then compression, then the final cut. That ordering is what makes
 * the compressor useful — it gets to arbitrate the whole fetched set, and k
 * only protects the prompt afterwards.
 *
 * The runtime does it the other way round: k truncates before the compressor
 * ever sees anything, which leaves it four already-selected documents to work
 * with. That behaviour is reachable through an explicit opt-in, so the gap can
 * be demonstrated side by side, but it is not what this tool presents as
 * normal.
 *
 * Condensation is optional and explicit. What is sent to the search is always
 * what the user sees in the form: the condensed question and keywords are
 * editable, so a run can be replayed identically even though the condensation
 * model is not deterministic.
 */
@Component({
  selector: 'tock-vector-store-diagnostic',
  templateUrl: './diagnostic.component.html',
  styleUrl: './diagnostic.component.scss',
  standalone: false
})
export class DiagnosticComponent implements OnInit, OnDestroy {
  @ViewChildren(NbTooltipDirective) tooltips: QueryList<NbTooltipDirective>;

  destroy$: Subject<unknown> = new Subject();

  loading: boolean = false;
  condensing: boolean = false;

  configurations: BotApplicationConfiguration[];

  indexes: VectorStoreIndex[] = [];
  currentIndex: VectorStoreIndex | null = null;
  capabilities: VectorStoreCapabilities | null = null;

  searchTypes = DocumentSearchType;
  searchType: DocumentSearchType = DocumentSearchType.HYBRID_SEARCH;

  question: string = '';

  condensationEnabled: boolean = true;
  /** Filled by /condense, then editable: the search sends this, not the raw question. */
  condensedQuestion: string = '';
  keyWordsInput: string = '';

  fetchK: number = DEFAULT_FETCH_K;
  k: number = DEFAULT_K;

  compressorSettings: CompressorSettings | null = null;
  compressionEnabled: boolean = false;
  minScore: number = DEFAULT_MIN_SCORE;
  maxDocuments: number = DEFAULT_MAX_DOCUMENTS;
  fillToMaxDocuments: boolean = false;

  /**
   * Opt-in reproduction of the current runtime ordering, where k truncates
   * before compression. Off by default: the tool presents the intended
   * pipeline, and this exists to demonstrate the gap.
   *
   * Once the runtime dissociates fetchK from k, this flag and the
   * compressionStage parameter it drives can both be dropped.
   */
  reproduceRuntimeOrder: boolean = false;

  funnel: SearchFunnel | null = null;
  results: SearchResultChunk[] = [];
  duration: number | null = null;

  currentRun: SearchRun | null = null;
  referenceRun: SearchRun | null = null;
  comparison: RunComparison | null = null;

  pinnedChunkIds: string[] = [];

  private readonly botConfiguration = inject(BotConfigurationService);
  private readonly inspection = inject(VectorStoreInspectionService);
  private readonly location = inject(Location);
  private readonly toastrService = inject(NbToastrService);
  private readonly translocoService = inject(TranslocoService);
  private navigationIndexName: string | null = null;
  public readonly state = inject(VectorStoreInspectionStateService);

  ngOnInit(): void {
    this.applyNavigationState();

    // The list must be fed before the selection, otherwise nb-select cannot
    // match the preselected index against options it does not hold yet.
    this.state.indexes$.pipe(takeUntil(this.destroy$)).subscribe((indexes) => {
      this.indexes = indexes;
      if (this.navigationIndexName && indexes.length) {
        const indexName = this.navigationIndexName;
        this.navigationIndexName = null;
        const index = indexes.find((candidate) => candidate.indexName === indexName);
        if (index) {
          this.state.selectIndex(index);
        } else {
          this.toastrService.warning('', this.translocoService.translate('vsi.diagnostic.index_not_found', { indexName }));
        }
      }
    });

    this.state.currentIndex$.pipe(takeUntil(this.destroy$)).subscribe((index) => {
      const changed = index?.indexName !== this.currentIndex?.indexName;
      this.currentIndex = index;

      // Results belong to the index they came from. Pins are kept: following a
      // chunk across two ingestions is exactly what they are for.
      if (changed) this.clearResults();
    });

    this.state.capabilities$.pipe(takeUntil(this.destroy$)).subscribe((capabilities) => {
      this.capabilities = capabilities;

      // Never assume a mode is available: a provider may support none of them
      // beyond plain similarity search.
      if (capabilities?.searchTypes.length && !capabilities.searchTypes.includes(this.searchType)) {
        this.searchType = capabilities.searchTypes[0];
      }
    });

    this.state.pinnedChunkIds$.pipe(takeUntil(this.destroy$)).subscribe((ids) => {
      this.pinnedChunkIds = ids;
    });

    this.state.referenceRun$.pipe(takeUntil(this.destroy$)).subscribe((run) => {
      this.referenceRun = run;
      this.refreshComparison();
    });

    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((confs) => {
      this.configurations = confs;

      const botChanged = this.state.applyBotContext(this.botKey(confs));

      if (!confs.length) return;

      if (botChanged) {
        this.clearResults();
        this.compressorSettings = null;
      }

      // loadIndexes uses its cache unless forced, so re-entering the view with
      // the same bot costs nothing.
      this.state.loadIndexes(botChanged).pipe(takeUntil(this.destroy$)).subscribe();

      if (!this.capabilities) this.state.loadCapabilities().pipe(takeUntil(this.destroy$)).subscribe();
      if (!this.compressorSettings) this.loadCompressorSettings();
    });
  }

  /**
   * Pre-fills the form from the dialog logger hand-off. The recorded condensed
   * question and keywords are reused as-is: replaying with a fresh, non
   * deterministic condensation would change the inputs being investigated.
   *
   * Not carried in the URL, so it does not survive a reload — acceptable for a
   * one-shot hand-off.
   */
  private applyNavigationState(): void {
    const navigationState = this.location.getState() as DiagnosticNavigationState | null;
    if (!navigationState?.question) return;

    this.question = navigationState.question;
    this.navigationIndexName = navigationState.indexName ?? null;

    if (navigationState.k && navigationState.k > 0) {
      this.k = navigationState.k;
      this.fetchK = navigationState.k;
    }

    if (navigationState.condensed_question) {
      this.condensedQuestion = navigationState.condensed_question;
      this.condensationEnabled = true;
    }

    if (navigationState.key_words?.length) {
      this.keyWordsInput = navigationState.key_words.join(', ');
    }
  }

  private botKey(confs: BotApplicationConfiguration[]): string | null {
    if (!confs.length) return null;
    return `${confs[0].namespace}/${confs[0].botId}`;
  }

  loadCompressorSettings(): void {
    this.inspection
      .getCompressorSettings()
      .pipe(takeUntil(this.destroy$))
      .subscribe((settings) => {
        this.compressorSettings = settings;
        // Start from what the running chain actually does, so any divergence
        // the user introduces is a deliberate one.
        this.compressionEnabled = !!settings?.enabled;

        const thresholds = settings?.setting as CompressorThresholds | undefined;
        if (thresholds) {
          this.minScore = thresholds.minScore ?? DEFAULT_MIN_SCORE;
          this.maxDocuments = thresholds.maxDocuments ?? DEFAULT_MAX_DOCUMENTS;
          this.fillToMaxDocuments = !!thresholds.fillToMaxDocuments;
        }
      });
  }

  // -- Condensation ------------------------------------------------------

  condense(): void {
    if (!this.question.trim()) return;

    this.condensing = true;

    this.inspection
      .condense({ question: this.question })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.condensedQuestion = response.condensedQuestion;
          this.keyWordsInput = response.keyWords.join(', ');
          this.condensing = false;
        },
        error: () => (this.condensing = false)
      });
  }

  /** What the retrieval actually receives, condensation on or off. */
  get effectiveQuery(): string {
    return this.condensationEnabled && this.condensedQuestion.trim() ? this.condensedQuestion.trim() : this.question.trim();
  }

  get keyWords(): string[] {
    return this.keyWordsInput
      .split(',')
      .map((word) => word.trim())
      .filter((word) => !!word);
  }

  get needsKeyWords(): boolean {
    return this.searchType === DocumentSearchType.FULL_TEXT_SEARCH || this.searchType === DocumentSearchType.HYBRID_SEARCH;
  }

  get usesVector(): boolean {
    return this.searchType === DocumentSearchType.SIMILARITY_SEARCH || this.searchType === DocumentSearchType.HYBRID_SEARCH;
  }

  get usesFts(): boolean {
    return this.needsKeyWords;
  }

  get isHybrid(): boolean {
    return this.searchType === DocumentSearchType.HYBRID_SEARCH;
  }

  // -- Pipeline ordering -------------------------------------------------

  /**
   * beforeCut is the intended pipeline: the compressor arbitrates everything
   * that was fetched, then k protects the prompt. afterCut reproduces what the
   * runtime does today.
   */
  get compressionStage(): CompressionStage {
    return this.reproduceRuntimeOrder ? 'afterCut' : 'beforeCut';
  }

  // -- Divergences from the running chain --------------------------------

  /**
   * The runtime uses a single k for both the retrieval width and the final
   * cut, so any fetchK different from k describes something production does
   * not do.
   */
  get fetchKDiverges(): boolean {
    return this.fetchK !== this.k;
  }

  get compressionDiverges(): boolean {
    return this.compressionEnabled !== !!this.compressorSettings?.enabled;
  }

  get thresholdsDiverge(): boolean {
    const thresholds = this.compressorSettings?.setting as CompressorThresholds | undefined;
    if (!thresholds || !this.compressionEnabled) return false;

    return (
      this.minScore !== thresholds.minScore ||
      this.maxDocuments !== thresholds.maxDocuments ||
      this.fillToMaxDocuments !== !!thresholds.fillToMaxDocuments
    );
  }

  get hasDivergence(): boolean {
    return this.fetchKDiverges || this.compressionDiverges || this.thresholdsDiverge;
  }

  /**
   * The compressor would keep more documents than the final cut allows, so k
   * would truncate work the reranker just did — with no criterion beyond rank.
   * A k above maxDocuments is healthy, though: it acts as the safety margin
   * that catches a compressor failure, where the untouched candidates fall
   * through and only k stands between them and the prompt.
   */
  get maxDocumentsExceedsK(): boolean {
    return this.compressionEnabled && this.maxDocuments > this.k;
  }

  // -- Search ------------------------------------------------------------

  get canSearch(): boolean {
    if (!this.currentIndex || this.loading) return false;
    if (!this.effectiveQuery) return false;
    if (this.needsKeyWords && !this.keyWords.length) return false;
    return true;
  }

  search(): void {
    if (!this.currentIndex || this.loading || !this.effectiveQuery) return;

    // The backend falls back to vector search when hybrid runs without
    // keywords, silently. Acceptable at runtime, misleading in a diagnostic
    // tool, so it is refused here instead.
    if (this.needsKeyWords && !this.keyWords.length) {
      this.toastrService.warning('', this.translocoService.translate('vsi.diagnostic.keywords_required'));
      return;
    }

    const request: SearchRequest = {
      indexName: this.currentIndex.indexName,
      searchType: this.searchType,
      query: this.effectiveQuery,
      keyWords: this.needsKeyWords ? this.keyWords : undefined,
      fetchK: this.fetchK,
      k: this.k,
      compressionEnabled: this.compressionEnabled,
      compressionStage: this.compressionStage,
      compressionOverride: {
        minScore: this.minScore,
        maxDocuments: this.maxDocuments,
        fillToMaxDocuments: this.fillToMaxDocuments
      },
      pinnedChunkIds: this.pinnedChunkIds,
      pinnedRankStrategy: 'exact_rank'
    };

    this.loading = true;

    this.inspection
      .search(request)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: SearchResponse) => {
          this.funnel = response.funnel;
          this.results = response.results;
          this.duration = response.duration;

          this.currentRun = {
            request,
            response,
            label: this.currentIndex!.indexName,
            ranAt: new Date()
          };
          this.refreshComparison();

          this.loading = false;
        },
        error: () => {
          this.loading = false;
          this.clearResults();
        }
      });
  }

  clearResults(): void {
    this.funnel = null;
    this.results = [];
    this.duration = null;
    this.currentRun = null;
    this.comparison = null;
  }

  // -- Run comparison ----------------------------------------------------

  /**
   * Comparing a run against itself would show nothing but stable rows, so the
   * delta only exists once the current run differs from the reference.
   */
  private refreshComparison(): void {
    this.comparison =
      this.referenceRun && this.currentRun && this.referenceRun !== this.currentRun
        ? buildRunComparison(this.referenceRun, this.currentRun)
        : null;
  }

  pinAsReference(): void {
    this.state.setReferenceRun(this.currentRun);
    this.comparison = null;
  }

  clearReference(): void {
    this.state.setReferenceRun(null);
  }

  get canPinAsReference(): boolean {
    return !!this.currentRun && this.currentRun !== this.referenceRun;
  }

  // -- Funnel display ----------------------------------------------------

  /** Ordered funnel stages, skipping those the current mode does not use. */
  get funnelStages(): { key: string; stage: SearchFunnel[keyof SearchFunnel] }[] {
    if (!this.funnel) return [];

    // Compression sits before the cut unless the runtime ordering is being
    // reproduced, and the funnel must read in the order things happened.
    const compressionFirst = this.compressionEnabled && !this.reproduceRuntimeOrder;

    const tail = compressionFirst
      ? [
          { key: 'compression', stage: this.funnel.compression },
          { key: 'topKCut', stage: this.funnel.topKCut }
        ]
      : [
          { key: 'topKCut', stage: this.funnel.topKCut },
          { key: 'compression', stage: this.funnel.compression }
        ];

    return (
      [
        { key: 'vector', stage: this.funnel.vector },
        { key: 'fts', stage: this.funnel.fts },
        { key: 'rrf', stage: this.funnel.rrf },
        ...tail
      ] as { key: string; stage: SearchFunnel[keyof SearchFunnel] }[]
    ).filter((entry) => entry.stage.status !== 'skipped');
  }

  /**
   * Vector and full text are parallel channels feeding the fusion, not
   * successive stages. An arrow between them would suggest the second consumes
   * the output of the first, which is false.
   */
  isParallelSeparator(index: number): boolean {
    const stages = this.funnelStages;
    return stages[index]?.key === 'vector' && stages[index + 1]?.key === 'fts';
  }

  /** How many chunks are lost between two successive stages. */
  stageDrop(index: number): number | null {
    if (this.isParallelSeparator(index)) return null;

    const stages = this.funnelStages;
    const from = stages[index]?.stage.count;
    const to = stages[index + 1]?.stage.count;

    if (from === null || from === undefined || to === null || to === undefined) return null;
    return to < from ? from - to : null;
  }

  get keptCount(): number {
    return this.results.filter((result) => result.outcome === 'kept').length;
  }

  // -- Misc --------------------------------------------------------------

  /**
   * Nebular mounts tooltips in a detached overlay. When a select closes its
   * option list the option is destroyed without the directive ever receiving a
   * mouseleave, so the overlay stays on screen. Closing them by hand is the
   * only reliable way out.
   */
  hideTooltips(): void {
    this.tooltips?.forEach((tooltip) => tooltip.hide());
  }

  selectIndex(index: VectorStoreIndex): void {
    this.hideTooltips();
    this.state.selectIndex(index);
  }

  selectSearchType(searchType: DocumentSearchType): void {
    this.hideTooltips();
    this.searchType = searchType;
    this.state.setSearchType(searchType);
  }

  ngOnDestroy(): void {
    this.destroy$.next(null);
    this.destroy$.complete();
  }
}

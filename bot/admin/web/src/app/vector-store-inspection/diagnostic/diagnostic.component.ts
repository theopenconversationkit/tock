import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { Component, inject, OnDestroy, OnInit, QueryList, ViewChildren } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import { NbTooltipDirective, NbToastrService } from '@nebular/theme';
import { Subject, takeUntil } from 'rxjs';

import { BotConfigurationService } from '../../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { DocumentSearchType } from '../../rag/rag-settings/models/engines-configurations';
import {
  RetrievalSettings,
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
  private readonly route = inject(ActivatedRoute);
  private kbPinApplied = false;
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

  k: number = DEFAULT_K;

  /** The bot's effective retrieval settings, prefilled and used for divergences. */
  botSettings: RetrievalSettings | null = null;
  compressionEnabled: boolean = false;
  minScore: number = DEFAULT_MIN_SCORE;
  maxDocuments: number = DEFAULT_MAX_DOCUMENTS;
  fillToMaxDocuments: boolean = false;

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
  private navigationIndexRefreshed = false;
  /** True when the dialog logger hand-off supplied k, so bot settings must not override it. */
  private navigationKProvided = false;
  public readonly state = inject(VectorStoreInspectionStateService);

  ngOnInit(): void {
    this.applyNavigationState();

    // The list must be fed before the selection, otherwise nb-select cannot
    // match the preselected index against options it does not hold yet. The requested index may arrive before the
    // list (this subscription re-applies on every emission) or after a cached list already replayed (the KB hand-off
    // sets it in the later configurations callback, which calls applyRequestedIndex itself).
    this.state.indexes$.pipe(takeUntil(this.destroy$)).subscribe((indexes) => {
      this.indexes = indexes;
      this.applyRequestedIndex();
    });

    this.state.currentIndex$.pipe(takeUntil(this.destroy$)).subscribe((index) => {
      const changed = index?.indexName !== this.currentIndex?.indexName;
      this.currentIndex = index;

      // Results belong to the index they came from. Pins are kept: following a
      // chunk across two ingestions is exactly what they are for.
      if (changed) this.clearResults();
    });

    this.state.capabilities$.pipe(takeUntil(this.destroy$)).subscribe((capabilities) => {
      // Kept for the capability notes; the search type is locked to the bot's
      // configuration (see loadRetrievalSettings), not chosen here.
      this.capabilities = capabilities;
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
      if (!this.kbPinApplied) {
        const params = this.route.snapshot.queryParamMap;
        const chunkId = params.get('chunkId');
        if (chunkId) {
          this.kbPinApplied = true;
          if (!this.state.isPinned(chunkId)) this.state.togglePin(chunkId);
          this.question = params.get('question') || '';
          this.navigationIndexName = params.get('indexName');
          // The index list was already replayed from cache by the indexes$ subscription before this callback ran,
          // so apply the requested selection here instead of waiting for an emission that a cached loadIndexes(false)
          // will not produce.
          this.applyRequestedIndex();
        }
      }

      if (botChanged) {
        this.clearResults();
        this.botSettings = null;
      }

      // loadIndexes uses its cache unless forced, so re-entering the view with
      // the same bot costs nothing.
      this.state.loadIndexes(botChanged).pipe(takeUntil(this.destroy$)).subscribe();

      if (!this.capabilities) this.state.loadCapabilities().pipe(takeUntil(this.destroy$)).subscribe();
      if (!this.botSettings) this.loadRetrievalSettings();
    });
  }

  /**
   * Applies the index requested by navigation (KB hand-off or dialog logger) to the current list. Idempotent and
   * safe to call both when the list changes and when the requested name is set late. An index absent from the list
   * may have just been created (e.g. from the knowledge base), so refresh the list once and let the resulting
   * emission retry through this same method; if it is still missing after that, warn the user.
   */
  private applyRequestedIndex(): void {
    const indexName = this.navigationIndexName;
    if (!indexName || !this.indexes.length) return;

    const index = this.indexes.find((candidate) => candidate.indexName === indexName);
    if (index) {
      this.navigationIndexName = null;
      this.state.selectIndex(index);
      return;
    }

    if (this.navigationIndexRefreshed) {
      this.navigationIndexName = null;
      this.toastrService.warning('', this.translocoService.translate('vsi.diagnostic.index_not_found', { indexName }));
      return;
    }

    this.navigationIndexRefreshed = true;
    this.state.refreshIndexes().pipe(takeUntil(this.destroy$)).subscribe();
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
      this.navigationKProvided = true;
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

  loadRetrievalSettings(): void {
    this.inspection
      .getRetrievalSettings()
      .pipe(takeUntil(this.destroy$))
      .subscribe((settings) => {
        this.botSettings = settings;
        if (!settings) return;

        // Start from what the running chain actually does, so any divergence the
        // user introduces is a deliberate one.
        this.searchType = settings.searchType;
        this.state.setSearchType(settings.searchType);
        // The dialog logger hand-off, when present, owns k; otherwise take the bot's.
        if (!this.navigationKProvided) this.k = settings.maxDocumentsRetrieved;

        this.compressionEnabled = settings.compressionEnabled;
        if (settings.minScore != null) this.minScore = settings.minScore;
        if (settings.maxDocuments != null) this.maxDocuments = settings.maxDocuments;
        if (settings.fillToMaxDocuments != null) this.fillToMaxDocuments = settings.fillToMaxDocuments;
      });
  }

  /** A compressor is configured for the bot (regardless of whether it is enabled). */
  get compressorConfigured(): boolean {
    return !!this.botSettings?.provider;
  }

  resetToBotSettings(): void {
    const settings = this.botSettings;
    if (!settings) return;
    this.k = settings.maxDocumentsRetrieved;
    this.compressionEnabled = settings.compressionEnabled;
    if (settings.minScore != null) this.minScore = settings.minScore;
    if (settings.maxDocuments != null) this.maxDocuments = settings.maxDocuments;
    if (settings.fillToMaxDocuments != null) this.fillToMaxDocuments = settings.fillToMaxDocuments;
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

  // -- Divergences from the running chain --------------------------------

  get kDiverges(): boolean {
    return !!this.botSettings && this.k !== this.botSettings.maxDocumentsRetrieved;
  }

  get compressionDiverges(): boolean {
    return !!this.botSettings && this.compressionEnabled !== this.botSettings.compressionEnabled;
  }

  get maxDocumentsDiverges(): boolean {
    const bot = this.botSettings;
    if (!bot || !this.compressionEnabled || bot.maxDocuments == null) return false;
    return this.maxDocuments !== bot.maxDocuments;
  }

  get minScoreDiverges(): boolean {
    const bot = this.botSettings;
    if (!bot || !this.compressionEnabled || bot.minScore == null) return false;
    return this.minScore !== bot.minScore;
  }

  get hasDivergence(): boolean {
    return this.kDiverges || this.compressionDiverges || this.maxDocumentsDiverges || this.minScoreDiverges;
  }

  /**
   * The compressor runs after the top-k cut, on the k retrieved documents, so a
   * Max documents above k can never widen the result — it is ineffective and the
   * form blocks the search until it is brought back within k.
   */
  get maxDocumentsExceedsK(): boolean {
    return this.compressionEnabled && this.maxDocuments > this.k;
  }

  // -- Search ------------------------------------------------------------

  get canSearch(): boolean {
    if (!this.currentIndex || this.loading) return false;
    if (!this.effectiveQuery) return false;
    if (this.needsKeyWords && !this.keyWords.length) return false;
    if (this.maxDocumentsExceedsK) return false;
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
      k: this.k,
      compressionEnabled: this.compressionEnabled,
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

    // The runtime order: retrieve, fuse, cut to k, then compress.
    return (
      [
        { key: 'vector', stage: this.funnel.vector },
        { key: 'fts', stage: this.funnel.fts },
        { key: 'rrf', stage: this.funnel.rrf },
        { key: 'topKCut', stage: this.funnel.topKCut },
        { key: 'compression', stage: this.funnel.compression }
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

  ngOnDestroy(): void {
    this.destroy$.next(null);
    this.destroy$.complete();
  }
}

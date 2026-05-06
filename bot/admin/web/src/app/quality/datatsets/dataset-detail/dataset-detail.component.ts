import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, OnDestroy, OnInit, QueryList, ViewChildren } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, of, Subject, takeUntil } from 'rxjs';
import { filter, skip, switchMap, take } from 'rxjs/operators';
import { BotConfigurationService } from '../../../core/bot-configuration.service';

import { Dataset, DatasetQuestion, DatasetRun, DatasetRunAction, DatasetRunState } from '../models';

import { SettingsService } from '../../../core-nlp/settings.service';
import { DialogService } from '../../../core-nlp/dialog.service';
import { DatasetDetailSettingsDiffComponent, SettingsDiffCurrentTabs } from './settings-diff/settings-diff.component';
import { hasDiffExcluding } from '../../../shared/utils';
import { DatasetsService } from '../services/datasets.service';
import { DatePipe } from '@angular/common';

interface RunDiffFlags {
  statusDiff: boolean;
  sourceDiff: boolean;
  typeDiff: boolean;
  questionDiff: boolean;
}
interface ActiveFilters {
  statusDiff: boolean;
  sourceDiff: boolean;
  typeDiff: boolean;
  questionDiff: boolean;
}

@Component({
  selector: 'tock-dataset-detail',
  templateUrl: './dataset-detail.component.html',
  styleUrl: './dataset-detail.component.scss'
})
export class DatasetDetailComponent implements OnInit, AfterViewInit, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  loading: boolean = true;

  dataset: Dataset;

  currentRun: DatasetRun;
  comparisonRun: DatasetRun;

  showComparison: boolean = true;

  toggleShowComparison(): void {
    this.showComparison = !this.showComparison;
  }

  currentRunActions: DatasetRunAction[];
  comparisonRunActions: DatasetRunAction[];

  // Pre-computed once per dataset load, used by the template's *ngFor — avoids
  // recomputing on every change detection cycle
  comparableRuns: DatasetRun[] = [];

  settingsDiffCurrentTabs = SettingsDiffCurrentTabs;

  filters: ActiveFilters = {
    statusDiff: false,
    sourceDiff: false,
    typeDiff: false,
    questionDiff: false
  };

  @ViewChildren('runsScrollA, runsScrollB') runsScrollContainers: QueryList<ElementRef<HTMLDivElement>>;

  // Scroll state per side — updated on scroll events and after view init
  private _scrollState: Record<'current' | 'compared', { left: boolean; right: boolean }> = {
    current: { left: false, right: false },
    compared: { left: false, right: false }
  };

  constructor(
    private botConfiguration: BotConfigurationService,
    private route: ActivatedRoute,
    private router: Router,
    public settings: SettingsService,
    private dialogService: DialogService,
    private datasetsService: DatasetsService,
    private datePipe: DatePipe,
    private cdr: ChangeDetectorRef
  ) {}

  ngAfterViewInit(): void {
    // Initialize scroll state once containers are in the DOM
    this.runsScrollContainers.changes.subscribe(() => this._refreshScrollState());
    this._refreshScrollState();
  }

  /** Pre-computed diff flags per question id — rebuilt after each fetchRunEntries(). */
  private _diffFlagsByQuestionId = new Map<string, RunDiffFlags>();

  get anyFilterActive(): boolean {
    return Object.values(this.filters).some(Boolean);
  }

  get allFiltersActive(): boolean {
    return Object.values(this.filters).every(Boolean);
  }

  toggleAllFilters(): void {
    const next = !this.allFiltersActive;
    this.filters = { statusDiff: next, sourceDiff: next, typeDiff: next, questionDiff: next };
  }

  get filteredQuestions(): DatasetQuestion[] {
    if (!this.dataset?.questions) return [];
    if (!this.anyFilterActive || !this.showComparison) return this.dataset.questions;

    return this.dataset.questions.filter((q) => {
      const flags = this._diffFlagsByQuestionId.get(q.id);
      if (!flags) return false;
      return (
        (this.filters.statusDiff && flags.statusDiff) ||
        (this.filters.sourceDiff && flags.sourceDiff) ||
        (this.filters.typeDiff && flags.typeDiff) ||
        (this.filters.questionDiff && flags.questionDiff)
      );
    });
  }

  // ── Scroll arrow helpers ──────────────────────────────────────────────────

  private _getScrollEl(side: 'current' | 'compared'): HTMLDivElement | null {
    const els = this.runsScrollContainers?.toArray();
    if (!els?.length) return null;
    // runsScrollA is index 0, runsScrollB is index 1 (QueryList order matches template order)
    return side === 'current' ? els[0]?.nativeElement : els[1]?.nativeElement;
  }

  private _refreshScrollState(): void {
    (['current', 'compared'] as const).forEach((side) => {
      const el = this._getScrollEl(side);
      if (!el) return;
      this._scrollState[side] = {
        left: el.scrollLeft > 0,
        right: el.scrollLeft + el.clientWidth < el.scrollWidth - this._scrollRightMargin
      };
    });
    this.cdr.detectChanges();
  }

  onRunsScroll(side: 'current' | 'compared'): void {
    const el = this._getScrollEl(side);
    if (!el) return;
    this._scrollState[side] = {
      left: el.scrollLeft > 0,
      right: el.scrollLeft + el.clientWidth < el.scrollWidth - this._scrollRightMargin
    };
  }

  canScrollLeft(side: 'current' | 'compared'): boolean {
    return this._scrollState[side].left;
  }

  canScrollRight(side: 'current' | 'compared'): boolean {
    return this._scrollState[side].right;
  }

  private _scrollInterval: ReturnType<typeof setInterval> | null = null;
  private readonly _scrollRightMargin = 60; // px — stop before the real right edge

  startScroll(side: 'current' | 'compared', direction: 'left' | 'right'): void {
    this.stopScroll();
    const el = this._getScrollEl(side);
    if (!el) return;
    const step = direction === 'right' ? 4 : -4;
    this._scrollInterval = setInterval(() => {
      const maxScroll = el.scrollWidth - el.clientWidth - this._scrollRightMargin;
      if (direction === 'right' && el.scrollLeft >= maxScroll) {
        this.stopScroll();
        return;
      }
      el.scrollLeft += step;
    }, 16); // ~60fps
  }

  stopScroll(): void {
    if (this._scrollInterval !== null) {
      clearInterval(this._scrollInterval);
      this._scrollInterval = null;
    }
  }

  // Emits once when the dataset has been successfully loaded — used to gate
  // the bot-switch detection so it only activates after the initial load is done.
  private readonly datasetLoaded$ = new Subject<void>();

  ngOnInit(): void {
    // Step 1 — initial load: read the route param once, wait for first valid configuration.
    this.route.params
      .pipe(
        take(1),
        switchMap((params) =>
          this.botConfiguration.configurations.pipe(
            filter((confs) => !!confs.length),
            take(1),
            switchMap(() => [params])
          )
        )
      )
      .subscribe((params) => this.fetchDataset(params['id']));

    // Step 2 — bot switch detection: only starts listening after the dataset is loaded.
    // This prevents the initial burst of configurations emissions from triggering
    // a premature navigation back to the board.
    this.datasetLoaded$
      .pipe(
        take(1),
        switchMap(() =>
          this.botConfiguration.configurations.pipe(
            filter((confs) => !!confs.length),
            skip(1)
          )
        ),
        takeUntil(this.destroy$)
      )
      .subscribe(() => this.router.navigate(['/quality/datasets']));
  }

  fetchDataset(datasetId: string): void {
    this.loading = true;

    // Always fetch the full dataset individually — GET /datasets returns DatasetLight
    // entries without settingsSnapshot, which are required in this view.
    this.datasetsService
      .getDataset(datasetId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (dataset) => this._initFromDataset(dataset),
        error: () => this.router.navigate(['/quality/datasets'])
      });
  }

  private _initFromDataset(dataset: Dataset): void {
    this.dataset = dataset;
    this.datasetLoaded$.next();
    this.comparableRuns = this._buildComparableRuns(dataset);
    this.currentRun = this.comparableRuns[0] ?? null;

    // If a compareRunId query param is present, use it to pre-select Run B —
    // allows the board entry "compare with latest run" button to deep-link directly
    // into a specific comparison. Falls back to run -1 if the id is not found
    // among comparable runs (e.g. run is not COMPLETED).
    const compareRunId = this.route.snapshot.queryParams['compareRunId'];
    if (compareRunId) {
      this.comparisonRun = this.comparableRuns.find((r) => r.id === compareRunId) ?? this.comparableRuns[1] ?? null;
    } else {
      this.comparisonRun = this.comparableRuns[1] ?? null;
    }

    if (this.currentRun) this.fetchRunEntries();
    this.loading = false;
  }

  fetchRunEntries(): void {
    if (!this.currentRun) return;

    forkJoin({
      current: this.datasetsService.getRunActions(this.dataset.id, this.currentRun.id),
      comparison: this.comparisonRun ? this.datasetsService.getRunActions(this.dataset.id, this.comparisonRun.id) : of([])
    })
      .pipe(takeUntil(this.destroy$))
      .subscribe(({ current, comparison }) => {
        this.currentRunActions = current as DatasetRunAction[];
        this.comparisonRunActions = comparison as DatasetRunAction[];
        this._diffFlagsByQuestionId = this._buildDiffFlags(this.currentRunActions, this.comparisonRunActions);
      });
  }

  selectCurrentRun(run: DatasetRun): void {
    this.currentRun = run;
    this.fetchRunEntries();
  }

  selectComparableRun(run: DatasetRun): void {
    this.comparisonRun = run;
    this.fetchRunEntries();
  }

  isRunSelectable(which: 'current' | 'compared', entry: DatasetRun): boolean {
    if (entry.id === this.currentRun?.id || entry.id === this.comparisonRun?.id) return false;

    if (which === 'current' && this.comparisonRun && entry.startTime < this.comparisonRun.startTime) {
      return false;
    }

    if (which === 'compared' && this.currentRun && entry.startTime > this.currentRun.startTime) {
      return false;
    }

    return true;
  }

  getPrevEntryTooltip(run: DatasetRun) {
    return `Run started by ${run.startedBy} on ${this.datePipe.transform(run.startTime, 'y/MM/dd - HH:mm')}`;
  }

  getRunDuration(run: DatasetRun): string {
    if (!run.endTime) return '-';
    const durationMs = new Date(run.endTime).getTime() - new Date(run.startTime).getTime();
    return this.datasetsService.formatDuration(durationMs);
  }

  // ── Diff flag computation ─────────────────────────────────────────────────

  /**
   * Pre-computes diff flags for every question that appears in either run.
   * Called once after fetchRunEntries() resolves — never recomputed during
   * change detection cycles.
   */
  private _buildDiffFlags(currentActions: DatasetRunAction[], comparisonActions: DatasetRunAction[]): Map<string, RunDiffFlags> {
    const map = new Map<string, RunDiffFlags>();
    if (!currentActions?.length || !comparisonActions?.length) return map;

    const byId = (actions: DatasetRunAction[]) => new Map(actions.map((a) => [a.questionId, a]));
    const currentById = byId(currentActions);
    const comparisonById = byId(comparisonActions);

    const allIds = new Set([...currentById.keys(), ...comparisonById.keys()]);

    for (const qId of allIds) {
      const a = currentById.get(qId) ?? null;
      const b = comparisonById.get(qId) ?? null;

      map.set(qId, {
        statusDiff: this._hasStatusDiff(a, b),
        sourceDiff: this._hasSourceDiff(a, b),
        typeDiff: this._hasTypeDiff(a, b),
        questionDiff: this._hasQuestionDiff(a, b)
      });
    }

    return map;
  }

  /** Compares the RAG answer status (found_in_context, out_of_scope, …) between runs. */
  private _hasStatusDiff(a: DatasetRunAction | null, b: DatasetRunAction | null): boolean {
    if (!a?.action?.ragDebug?.answer?.status || !b?.action?.ragDebug?.answer?.status) return false;

    const statusA = a.action.ragDebug.answer.status ?? null;
    const statusB = b.action.ragDebug.answer.status ?? null;
    if (statusA == null && statusB == null) return false;
    return statusA !== statusB;
  }

  /** True when the source sets differ between two RAG runs (by url+title identity). */
  private _hasSourceDiff(a: DatasetRunAction | null, b: DatasetRunAction | null): boolean {
    if (!a?.action?.metadata?.isGenAiRagAnswer || !b?.action?.metadata?.isGenAiRagAnswer) return false;

    const footnotesA = a.action.message?.footnotes ?? [];
    const footnotesB = b.action.message?.footnotes ?? [];

    if (!footnotesA.length && !footnotesB.length) return false;
    if (footnotesA.length !== footnotesB.length) return true;

    const keyOf = (s: { url?: string; title?: string }) => `${s.url ?? ''}::${s.title ?? ''}`;
    const setA = new Set(footnotesA.map(keyOf));

    for (const s of footnotesB) {
      if (!setA.has(keyOf(s))) return true;
    }
    return false;
  }

  /** True when the answer type (RAG vs non-RAG) changed between the two runs. */
  private _hasTypeDiff(a: DatasetRunAction | null, b: DatasetRunAction | null): boolean {
    if (!a?.action || !b?.action) return false;
    return !!a.action.metadata?.isGenAiRagAnswer !== !!b.action.metadata?.isGenAiRagAnswer;
  }

  /** True when ragDebug.user_question differs between two RAG runs. */
  private _hasQuestionDiff(a: DatasetRunAction | null, b: DatasetRunAction | null): boolean {
    if (!a?.action?.metadata?.isGenAiRagAnswer || !b?.action?.metadata?.isGenAiRagAnswer) return false;
    const qA = a.action.ragDebug?.user_question;
    const qB = b.action.ragDebug?.user_question;
    if (qA == null || qB == null) return false;
    return qA.trim() !== qB.trim();
  }
  // ── Private helpers ───────────────────────────────────────────────────────

  private _buildComparableRuns(dataset: Dataset): DatasetRun[] {
    return (dataset.runs ?? [])
      .filter((r) => r.state === DatasetRunState.COMPLETED)
      .sort((a, b) => new Date(b.startTime).getTime() - new Date(a.startTime).getTime());
  }

  // ── Settings diff helpers ─────────────────────────────────────────────────

  compareSettings(tab: SettingsDiffCurrentTabs): void {
    this.dialogService.openDialog(DatasetDetailSettingsDiffComponent, {
      context: {
        currentRun: this.currentRun,
        comparisonRun: this.comparisonRun,
        currentTab: tab
      }
    });
  }

  hasPromptsDiff(): boolean {
    if (!this.comparisonRun) return false;
    return (
      this.currentRun.settingsSnapshot.questionAnsweringPrompt.template !==
        this.comparisonRun.settingsSnapshot.questionAnsweringPrompt.template ||
      this.currentRun.settingsSnapshot.questionCondensingPrompt.template !==
        this.comparisonRun.settingsSnapshot.questionCondensingPrompt.template
    );
  }

  hasSettingsDiff(): boolean {
    if (!this.comparisonRun) return false;
    return hasDiffExcluding(this.currentRun.settingsSnapshot, this.comparisonRun.settingsSnapshot, [
      'questionAnsweringPrompt',
      'questionCondensingPrompt',
      'indexSessionId'
    ]);
  }

  hasIndexingSessionDiff(): boolean {
    if (!this.comparisonRun) return false;
    return this.currentRun.settingsSnapshot.indexSessionId !== this.comparisonRun.settingsSnapshot.indexSessionId;
  }

  ngOnDestroy(): void {
    this.stopScroll();
    this.datasetLoaded$.complete();
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}

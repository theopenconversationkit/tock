import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, OnDestroy, OnInit, QueryList, ViewChildren } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, of, Subject, takeUntil } from 'rxjs';
import { filter, skip, switchMap, take } from 'rxjs/operators';
import { BotConfigurationService } from '../../../core/bot-configuration.service';

import { Dataset, DatasetRun, DatasetRunAction, DatasetRunState } from '../models';

import { SettingsService } from '../../../core-nlp/settings.service';
import { DialogService } from '../../../core-nlp/dialog.service';
import { DatasetDetailSettingsDiffComponent, SettingsDiffCurrentTabs } from './settings-diff/settings-diff.component';
import { hasDiffExcluding } from '../../../shared/utils';
import { DatasetsService } from '../services/datasets.service';
import { DatePipe } from '@angular/common';

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

import { Component, Input, OnDestroy } from '@angular/core';
import { Observable, Subject, interval } from 'rxjs';
import { startWith, switchMap, takeUntil, takeWhile } from 'rxjs/operators';
import { Dataset, DatasetRun, DatasetRunState } from '../../models';
import { DatasetsService } from '../../services/datasets.service';
import { DialogService } from '../../../../core-nlp/dialog.service';
import { ChoiceDialogComponent } from '../../../../shared/components';
import { DatasetCreateComponent } from '../../dataset-create/dataset-create.component';
import { StateService } from '../../../../core-nlp/state.service';

@Component({
  selector: 'tock-datasets-board-entry',
  templateUrl: './datasets-board-entry.component.html',
  styleUrl: './datasets-board-entry.component.scss'
})
export class DatasetsBoardEntryComponent implements OnDestroy {
  destroy$: Subject<void> = new Subject<void>();

  displayQuestionsDetail: boolean = false;
  displayRunsDetail: boolean = false;

  datasetRunState = DatasetRunState;

  runDurations$: Record<string, Observable<string>> = {};

  private _pollingRunIds = new Set<string>();

  @Input() set dataset(dataset: Dataset) {
    this._dataset = dataset;

    dataset.runs.forEach((run) => {
      const isActive = run.state === DatasetRunState.QUEUED || run.state === DatasetRunState.RUNNING;
      const existing$ = this.runDurations$[run.id];

      if (!existing$ || (!isActive && run.endTime)) {
        this.runDurations$ = {
          ...this.runDurations$,
          [run.id]: this.datasetsService.getRunDuration(run).pipe(takeUntil(this.destroy$))
        };
      }

      if (isActive && !this._pollingRunIds.has(run.id)) {
        this._startPolling(run);
      }
    });
  }

  get dataset(): Dataset {
    return this._dataset;
  }
  private _dataset: Dataset;

  constructor(private datasetsService: DatasetsService, private dialogService: DialogService, private stateService: StateService) {}

  getLatestRun(): DatasetRun | null {
    return this.datasetsService.getLatestRun(this.dataset);
  }

  // ── Action availability helpers ───────────────────────────────────────────

  get latestRunState(): DatasetRunState | null {
    return this.getLatestRun()?.state ?? null;
  }

  get hasActiveRun(): boolean {
    return this.latestRunState === DatasetRunState.QUEUED || this.latestRunState === DatasetRunState.RUNNING;
  }

  get canPlay(): boolean {
    return !this.hasActiveRun;
  }

  get canCancel(): boolean {
    return this.hasActiveRun;
  }

  get canViewHistory(): boolean {
    return !!this.dataset.runs.length;
  }

  get canEdit(): boolean {
    return !this.hasActiveRun;
  }

  get canDelete(): boolean {
    return !this.hasActiveRun;
  }

  runDataset(): void {
    this.datasetsService
      .createRun(this.dataset.id, { language: this.stateService.currentLocale })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        error: (err) => console.error('Failed to create run', err)
      });
  }

  private _startPolling(run: DatasetRun): void {
    this._pollingRunIds.add(run.id);

    interval(5_000)
      .pipe(
        startWith(0),
        switchMap(() => this.datasetsService.getRun(this.dataset.id, run.id)),
        takeWhile((r) => r.state === DatasetRunState.QUEUED || r.state === DatasetRunState.RUNNING, true), // stops on COMPLETED or CANCELLED
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (updated) => {
          this.datasetsService.updateRunState(this.dataset.id, run.id, {
            state: updated.state,
            stats: updated.stats,
            ...(updated.endTime ? { endTime: updated.endTime } : {})
          });
        },
        complete: () => {
          this._pollingRunIds.delete(run.id);
        }
      });
  }

  cancelRun(run: DatasetRun): void {
    this.datasetsService
      .cancelRun(this.dataset.id, run.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        error: (err) => console.error('Failed to cancel run', err)
      });
  }

  confirmDeleteDataset(): void {
    const action = 'permanently delete';
    const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
      context: {
        title: 'Delete a dataset',
        subtitle: `Are you sure you want to delete the "${this.dataset.name}" dataset and all its execution history?`,
        modalStatus: 'danger',
        actions: [
          { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
          { actionName: action, buttonStatus: 'danger' }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result === action) this.deleteDataset();
    });
  }

  deleteDataset(): void {
    this.datasetsService
      .deleteDataset(this.dataset.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        error: (err) => console.error('Failed to delete dataset', err)
      });
  }

  editDataset(): void {
    this.dialogService.openDialog(DatasetCreateComponent, {
      context: { dataset: this.dataset }
    });
  }

  getStateColor(state: DatasetRunState): string {
    switch (state) {
      case DatasetRunState.QUEUED:
        return 'text-warning';
      case DatasetRunState.RUNNING:
        return 'text-info';
      case DatasetRunState.COMPLETED:
        return 'text-success';
      case DatasetRunState.CANCELLED:
        return 'text-danger';
      default:
        return 'text-basic';
    }
  }

  switchQuestionsDetail(): void {
    this.displayQuestionsDetail = !this.displayQuestionsDetail;
    if (this.displayQuestionsDetail) this.displayRunsDetail = false;
  }

  switchRunsDetail(): void {
    this.displayRunsDetail = !this.displayRunsDetail;
    if (this.displayRunsDetail) this.displayQuestionsDetail = false;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

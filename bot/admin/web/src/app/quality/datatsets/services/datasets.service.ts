import { Injectable } from '@angular/core';
import { BehaviorSubject, interval, Observable, of } from 'rxjs';
import { map, startWith, tap } from 'rxjs/operators';
import { Dataset, DatasetQuestion, DatasetRun, DatasetRunAction, DatasetRunState } from '../models';
import { deepCopy } from '../../../shared/utils';
import { StateService } from '../../../core-nlp/state.service';
import { RestService } from '../../../core-nlp/rest/rest.service';

@Injectable({ providedIn: 'root' })
export class DatasetsService {
  private datasetsSubject = new BehaviorSubject<Dataset[]>([]);

  datasets$ = this.datasetsSubject.asObservable().pipe(
    map((datasets) =>
      [...datasets].sort((a, b) => {
        const aMostRecent = a.runs.length > 0 ? Math.max(...a.runs.map((r) => new Date(r.startTime).getTime())) : 0;
        const bMostRecent = b.runs.length > 0 ? Math.max(...b.runs.map((r) => new Date(r.startTime).getTime())) : 0;
        return bMostRecent - aMostRecent;
      })
    )
  );

  get datasets(): Dataset[] {
    return this.datasetsSubject.getValue();
  }
  private set datasets(value: Dataset[]) {
    this.datasetsSubject.next(value);
  }

  constructor(private stateService: StateService, private rest: RestService) {}

  // ---------------------------------------------------------------------------
  // GET /bots/:botId/datasets
  // ---------------------------------------------------------------------------
  getDatasets(): Observable<Dataset[]> {
    return this.rest.getArray<Dataset>(this.apiBase(), (res) => res as Dataset[]).pipe(tap((fetched) => this._mergeIntoStore(fetched)));
  }

  // ---------------------------------------------------------------------------
  // GET /bots/:botId/datasets/:datasetId
  // ---------------------------------------------------------------------------
  getDataset(datasetId: string): Observable<Dataset> {
    const cached = this.datasets.find((d) => d.id === datasetId);
    if (cached?._settingsLoaded) {
      return of(deepCopy(cached));
    }

    return this.rest
      .get<Dataset>(`${this.apiBase()}/${datasetId}`, (res) => res as Dataset)
      .pipe(tap((dataset) => this._upsertIntoStore({ ...dataset, _settingsLoaded: true })));
  }

  // ---------------------------------------------------------------------------
  // POST /bots/:botId/datasets
  // ---------------------------------------------------------------------------
  createDataset(payload: { name: string; description: string; questions: Omit<DatasetQuestion, 'id'>[] }): Observable<Dataset> {
    return this.rest
      .post<typeof payload, Dataset>(this.apiBase(), payload, (res) => res as Dataset)
      .pipe(tap((created) => (this.datasets = [{ ...created, _settingsLoaded: false }, ...this.datasets])));
  }

  // ---------------------------------------------------------------------------
  // PUT /bots/:botId/datasets/:datasetId
  // ---------------------------------------------------------------------------
  updateDataset(
    datasetId: string,
    payload: { name: string; description: string; questions: (Omit<DatasetQuestion, 'id'> & { id?: string })[] }
  ): Observable<Dataset> {
    return this.rest
      .put<typeof payload, Dataset>(`${this.apiBase()}/${datasetId}`, payload, (res) => res as Dataset)
      .pipe(
        tap((updated) => {
          const index = this.datasets.findIndex((d) => d.id === datasetId);
          if (index !== -1) {
            const merged = { ...updated, runs: this.datasets[index].runs, _settingsLoaded: this.datasets[index]._settingsLoaded };
            this.datasets = [...this.datasets.slice(0, index), merged, ...this.datasets.slice(index + 1)];
          }
        })
      );
  }

  // ---------------------------------------------------------------------------
  // DELETE /bots/:botId/datasets/:datasetId
  // ---------------------------------------------------------------------------
  deleteDataset(datasetId: string): Observable<boolean> {
    return this.rest.delete<void>(`${this.apiBase()}/${datasetId}`).pipe(
      tap(() => {
        this.datasets = this.datasets.filter((d) => d.id !== datasetId);
      })
    );
  }

  // ---------------------------------------------------------------------------
  // POST /bots/:botId/datasets/:datasetId/runs
  // ---------------------------------------------------------------------------
  createRun(datasetId: string, payload: { language: string }): Observable<DatasetRun> {
    return this.rest
      .post<typeof payload, DatasetRun>(`${this.apiBase()}/${datasetId}/runs`, payload, (res) => res as DatasetRun)
      .pipe(
        tap((run) => {
          const dataset = this.datasets.find((d) => d.id === datasetId);
          if (dataset) {
            this._updateDatasetRuns(datasetId, [run, ...dataset.runs]);
          }
        })
      );
  }

  // ---------------------------------------------------------------------------
  // GET /bots/:botId/datasets/:datasetId/runs/:runId
  // ---------------------------------------------------------------------------
  getRun(datasetId: string, runId: string): Observable<DatasetRun> {
    return this.rest.get<DatasetRun>(`${this.apiBase()}/${datasetId}/runs/${runId}`, (res) => res as DatasetRun);
  }

  // ---------------------------------------------------------------------------
  // GET /bots/:botId/datasets/:datasetId/runs/:runId/actions
  // ---------------------------------------------------------------------------
  getRunActions(datasetId: string, runId: string): Observable<DatasetRunAction[]> {
    return this.rest.getArray<DatasetRunAction>(`${this.apiBase()}/${datasetId}/runs/${runId}/actions`, (res) => res as DatasetRunAction[]);
  }

  // ---------------------------------------------------------------------------
  // POST /bots/:botId/datasets/:datasetId/runs/:runId/cancel
  // ---------------------------------------------------------------------------
  cancelRun(datasetId: string, runId: string): Observable<DatasetRun> {
    return this.rest
      .post<object, DatasetRun>(`${this.apiBase()}/${datasetId}/runs/${runId}/cancel`, {}, (res) => res as DatasetRun)
      .pipe(
        tap((cancelled) =>
          this.updateRunState(datasetId, runId, { state: cancelled.state, endTime: cancelled.endTime, stats: cancelled.stats })
        )
      );
  }

  // ---------------------------------------------------------------------------
  // DELETE /bots/:botId/datasets/:datasetId/runs/:runId
  // ---------------------------------------------------------------------------
  deleteRun(datasetId: string, runId: string): Observable<boolean | void> {
    return this.rest.delete<void>(`${this.apiBase()}/${datasetId}/runs/${runId}`).pipe(
      tap(() => {
        const dataset = this.datasets.find((d) => d.id === datasetId);
        if (dataset) {
          this._updateDatasetRuns(
            datasetId,
            dataset.runs.filter((r) => r.id !== runId)
          );
        }
      })
    );
  }

  // ---------------------------------------------------------------------------
  // PATCH run state
  // ---------------------------------------------------------------------------
  updateRunState(datasetId: string, runId: string, patch: Partial<Pick<DatasetRun, 'state' | 'endTime' | 'stats'>>): void {
    const datasets = this.datasets;
    const datasetIndex = datasets.findIndex((d) => d.id === datasetId);
    if (datasetIndex === -1) return;

    const dataset = datasets[datasetIndex];
    const runIndex = dataset.runs.findIndex((r) => r.id === runId);
    if (runIndex === -1) return;

    const updatedRun: DatasetRun = { ...dataset.runs[runIndex], ...patch };
    const updatedRuns = [...dataset.runs.slice(0, runIndex), updatedRun, ...dataset.runs.slice(runIndex + 1)];

    // A run with COMPLETED status now has a settingsSnapshot on the server side
    // that the store does not have — we invalidate to force a reload
    // on the next opening of the detail view.
    const settingsLoaded = patch.state === DatasetRunState.COMPLETED ? false : dataset._settingsLoaded;

    const updatedDataset: Dataset = { ...dataset, runs: updatedRuns, _settingsLoaded: settingsLoaded };
    this.datasets = [...datasets.slice(0, datasetIndex), updatedDataset, ...datasets.slice(datasetIndex + 1)];
  }

  // ---------------------------------------------------------------------------
  // Domain helpers
  // ---------------------------------------------------------------------------
  getLatestRun(dataset: Dataset): DatasetRun | null {
    if (!dataset.runs?.length) return null;
    return dataset.runs.reduce((latest, current) => (new Date(current.startTime) > new Date(latest.startTime) ? current : latest));
  }

  hasActiveRun(dataset: Dataset): boolean {
    return dataset.runs.some((r) => r.state === DatasetRunState.QUEUED || r.state === DatasetRunState.RUNNING);
  }

  canProposeNewRun(dataset: Dataset): boolean {
    return !this.hasActiveRun(dataset);
  }

  getRunDuration(run: DatasetRun): Observable<string> {
    const start = new Date(run.startTime);
    const end = run.endTime ? new Date(run.endTime) : null;

    if (end) {
      const durationMs = end.getTime() - start.getTime();
      return durationMs <= 0 ? of('-') : of(this.formatDuration(durationMs));
    }

    if ([DatasetRunState.QUEUED, DatasetRunState.RUNNING].includes(run.state)) {
      return interval(1000).pipe(
        startWith(0),
        map(() => this.formatDuration(new Date().getTime() - start.getTime()))
      );
    }

    return of('-');
  }

  formatDuration(durationMs: number): string {
    const days = Math.floor(durationMs / (1000 * 60 * 60 * 24));
    const hours = Math.floor((durationMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((durationMs % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((durationMs % (1000 * 60)) / 1000);
    const time = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    return days > 0 ? `${days}d ${time}` : time;
  }

  // ---------------------------------------------------------------------------
  // Private helpers
  // ---------------------------------------------------------------------------
  private apiBase(): string {
    return `/bots/${this.stateService.currentApplication.name}/datasets`;
  }

  private _mergeIntoStore(fetched: Dataset[]): void {
    const existing = new Map(this.datasets.map((d) => [d.id, d]));
    this.datasets = fetched.map((d) => {
      const cached = existing.get(d.id);
      return cached?._settingsLoaded ? cached : d;
    });
  }

  private _upsertIntoStore(dataset: Dataset): void {
    const index = this.datasets.findIndex((d) => d.id === dataset.id);
    if (index === -1) {
      this.datasets = [dataset, ...this.datasets];
    } else {
      this.datasets = [...this.datasets.slice(0, index), dataset, ...this.datasets.slice(index + 1)];
    }
  }

  private _updateDatasetRuns(datasetId: string, runs: DatasetRun[]): void {
    const datasets = this.datasets;
    const index = datasets.findIndex((d) => d.id === datasetId);
    if (index === -1) return;

    const updatedDataset: Dataset = { ...datasets[index], runs, _settingsLoaded: datasets[index]._settingsLoaded };
    this.datasets = [...datasets.slice(0, index), updatedDataset, ...datasets.slice(index + 1)];
  }
}

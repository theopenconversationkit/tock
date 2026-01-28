import { Injectable } from '@angular/core';
import { interval, Observable, of, throwError, BehaviorSubject } from 'rxjs';
import { delay, map, startWith, switchMap, tap } from 'rxjs/operators';
// import { HttpClient } from '@angular/common/http';                          // [REAL] uncomment
import { Dataset, DatasetQuestion, DatasetRun, DatasetRunActionState, DatasetRunState } from '../models';
import { deepCopy } from '../../../shared/utils';
import { mock_datasets } from '../model-dataset'; // [MOCK] delete
import { getMockedDatasetEntries } from '../model-dataset-run-actions'; // [MOCK] delete
import { StateService } from '../../../core-nlp/state.service';

// ─── [MOCK] delete this block ────────────────────────────────────────────────
const SIMULATED_DELAY_MS = 400;

function generateId(): string {
  return [...Array(24)].map(() => Math.floor(Math.random() * 16).toString(16)).join('');
}
// ─────────────────────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class DatasetsService {
  private datasetsSubject = new BehaviorSubject<Dataset[]>([]);

  // Datasets sorted by most recent run, ready to consume in templates
  datasets$ = this.datasetsSubject.asObservable().pipe(
    map((datasets) =>
      [...datasets].sort((a, b) => {
        const aMostRecent = a.runs.length > 0 ? Math.max(...a.runs.map((r) => new Date(r.startTime).getTime())) : 0;
        const bMostRecent = b.runs.length > 0 ? Math.max(...b.runs.map((r) => new Date(r.startTime).getTime())) : 0;
        return bMostRecent - aMostRecent;
      })
    )
  );

  // Public read-only snapshot of the current store — use datasets$ for reactive bindings
  get datasets(): Dataset[] {
    return this.datasetsSubject.getValue();
  }
  private set datasets(value: Dataset[]) {
    this.datasetsSubject.next(value);
  }

  // ─── [MOCK] delete this block ───────────────────────────────────────────────
  private runProgressTimers: Map<string, ReturnType<typeof setTimeout>> = new Map();
  // ───────────────────────────────────────────────────────────────────────────

  constructor(private stateService: StateService) {}

  // ---------------------------------------------------------------------------
  // Builds the base API URL for a given bot.                                  // [REAL] keep
  private apiBase(): string {
    return `/bots/${this.stateService.currentApplication.name}/datasets`;
  }

  // ---------------------------------------------------------------------------
  // GET /bots/:botId/datasets — returns Dataset[] without settingsSnapshot
  // Merges with existing store entries to preserve already-loaded settingsSnapshots.
  // ---------------------------------------------------------------------------
  getDatasets(): Observable<Dataset[]> {
    // ── [REAL] uncomment and delete mock block below ──────────────────────────
    // return this.http.get<Dataset[]>(this.apiBase()).pipe(
    //   tap((fetched) => this._mergeIntoStore(fetched))
    // );
    // ─────────────────────────────────────────────────────────────────────────

    // ─── [MOCK] delete this block ─────────────────────────────────────────────
    const fetched: Dataset[] = deepCopy(mock_datasets).map((d) => ({
      ...d,
      runs: d.runs.map(({ settingsSnapshot, ...run }) => run),
      _settingsLoaded: false
    }));
    this._mergeIntoStore(fetched);
    return of(this.datasets).pipe(delay(SIMULATED_DELAY_MS));
    // ─────────────────────────────────────────────────────────────────────────
  }

  // ---------------------------------------------------------------------------
  // GET /bots/:botId/datasets/:datasetId — returns full Dataset with settingsSnapshot.
  // Serves from cache if _settingsLoaded is already true, otherwise fetches and
  // upserts the full entry into the store.
  // ---------------------------------------------------------------------------
  getDataset(datasetId: string): Observable<Dataset> {
    const cached = this.datasets.find((d) => d.id === datasetId);

    if (cached?._settingsLoaded) {
      return of(deepCopy(cached));
    }

    // ── [REAL] uncomment and delete mock block below ──────────────────────────
    // return this.http.get<Dataset>(`${this.apiBase()}/${datasetId}`).pipe(
    //   tap((dataset) => this._upsertIntoStore({ ...dataset, _settingsLoaded: true }))
    // );
    // ─────────────────────────────────────────────────────────────────────────

    // ─── [MOCK] delete this block ─────────────────────────────────────────────
    const mockDataset = deepCopy(mock_datasets).find((d) => d.id === datasetId);
    if (!mockDataset) {
      return throwError(() => ({ status: 404, error: `Dataset ${datasetId} not found` }));
    }
    // Merge live runs from the store (up-to-date state/endTime) with settingsSnapshots
    // from the mock source, then mark as fully loaded.
    const liveRuns = cached?.runs ?? mockDataset.runs;
    const mockRunsById = new Map(mockDataset.runs.map((r) => [r.id, r]));
    const fullDataset: Dataset = {
      ...mockDataset,
      runs: liveRuns.map((r) => ({
        ...r,
        settingsSnapshot: mockRunsById.get(r.id)?.settingsSnapshot
      })),
      _settingsLoaded: true
    };
    this._upsertIntoStore(fullDataset);
    return of(deepCopy(fullDataset)).pipe(delay(SIMULATED_DELAY_MS));
    // ─────────────────────────────────────────────────────────────────────────
  }

  // ---------------------------------------------------------------------------
  // POST /bots/:botId/datasets
  // ---------------------------------------------------------------------------
  createDataset(payload: { name: string; description: string; questions: Omit<DatasetQuestion, 'id'>[] }): Observable<Dataset> {
    // ── [REAL] uncomment and delete mock block below ──────────────────────────
    // return this.http.post<Dataset>(this.apiBase(), payload).pipe(
    //   tap((created) => this.datasets = [{ ...created, _settingsLoaded: false }, ...this.datasets])
    // );
    // ─────────────────────────────────────────────────────────────────────────

    // ─── [MOCK] delete this block ─────────────────────────────────────────────
    const newDataset: Dataset = {
      id: generateId(),
      name: payload.name,
      description: payload.description,
      questions: payload.questions.map((q) => ({ ...q, id: generateId() })),
      runs: [],
      createdAt: new Date().toISOString(),
      createdBy: 'mock-user',
      updatedAt: null,
      updatedBy: null,
      _settingsLoaded: false
    };
    this.datasets = [newDataset, ...this.datasets];
    return of(deepCopy(newDataset)).pipe(delay(SIMULATED_DELAY_MS));
    // ─────────────────────────────────────────────────────────────────────────
  }

  // ---------------------------------------------------------------------------
  // PUT /bots/:botId/datasets/:datasetId
  // ---------------------------------------------------------------------------
  updateDataset(
    datasetId: string,
    payload: { name: string; description: string; questions: (Omit<DatasetQuestion, 'id'> & { id?: string })[] }
  ): Observable<Dataset> {
    // ── [REAL] uncomment and delete mock block below ──────────────────────────
    // return this.http.put<Dataset>(`${this.apiBase()}/${datasetId}`, payload).pipe(
    //   tap((updated) => {
    //     const index = this.datasets.findIndex((d) => d.id === datasetId);
    //     if (index !== -1) {
    //       // Preserve _settingsLoaded and runs — the update endpoint only touches metadata
    //       const merged = { ...updated, runs: this.datasets[index].runs, _settingsLoaded: this.datasets[index]._settingsLoaded };
    //       this.datasets = [...this.datasets.slice(0, index), merged, ...this.datasets.slice(index + 1)];
    //     }
    //   })
    // );
    // ─────────────────────────────────────────────────────────────────────────

    // ─── [MOCK] delete this block ─────────────────────────────────────────────
    const index = this.datasets.findIndex((d) => d.id === datasetId);
    if (index === -1) {
      return throwError(() => ({ status: 404, error: `Dataset ${datasetId} not found` }));
    }
    const updatedDataset: Dataset = {
      ...this.datasets[index],
      name: payload.name,
      description: payload.description,
      questions: payload.questions.map((q) => ({
        id: q.id ?? generateId(),
        question: q.question,
        groundTruth: q.groundTruth
      }))
      // _settingsLoaded and runs preserved via spread
    };
    this.datasets = [...this.datasets.slice(0, index), updatedDataset, ...this.datasets.slice(index + 1)];
    return of(deepCopy(updatedDataset)).pipe(delay(SIMULATED_DELAY_MS));
    // ─────────────────────────────────────────────────────────────────────────
  }

  // ---------------------------------------------------------------------------
  // DELETE /bots/:botId/datasets/:datasetId
  // ---------------------------------------------------------------------------
  deleteDataset(datasetId: string): Observable<void> {
    // ── [REAL] uncomment and delete mock block below ──────────────────────────
    // return this.http.delete<void>(`${this.apiBase()}/${datasetId}`).pipe(
    //   tap(() => this.datasets = this.datasets.filter((d) => d.id !== datasetId))
    // );
    // ─────────────────────────────────────────────────────────────────────────

    // ─── [MOCK] delete this block ─────────────────────────────────────────────
    const index = this.datasets.findIndex((d) => d.id === datasetId);
    if (index === -1) {
      return throwError(() => ({ status: 404, error: `Dataset ${datasetId} not found` }));
    }
    this.datasets[index].runs.forEach((run) => {
      const timer = this.runProgressTimers.get(run.id);
      if (timer) {
        clearTimeout(timer);
        this.runProgressTimers.delete(run.id);
      }
    });
    this.datasets = this.datasets.filter((d) => d.id !== datasetId);
    return of(undefined).pipe(delay(SIMULATED_DELAY_MS));
    // ─────────────────────────────────────────────────────────────────────────
  }

  // ---------------------------------------------------------------------------
  // POST /bots/:botId/datasets/:datasetId/runs
  // ---------------------------------------------------------------------------
  createRun(datasetId: string): Observable<DatasetRun> {
    // ── [REAL] uncomment and delete mock block below ──────────────────────────
    // return this.http.post<DatasetRun>(`${this.apiBase()}/${datasetId}/runs`, {}).pipe(
    //   tap((newRun) => {
    //     const dataset = this.datasets.find((d) => d.id === datasetId);
    //     if (dataset) this._updateDatasetRuns(datasetId, [newRun, ...dataset.runs]);
    //   })
    // );
    // ─────────────────────────────────────────────────────────────────────────

    // ─── [MOCK] delete this block ─────────────────────────────────────────────
    const dataset = this.datasets.find((d) => d.id === datasetId);
    if (!dataset) {
      return throwError(() => ({ status: 404, error: `Dataset ${datasetId} not found` }));
    }
    if (this.hasActiveRun(dataset)) {
      return throwError(() => ({ status: 409, error: 'A run is already active on this dataset' }));
    }
    const newRun: DatasetRun = {
      id: generateId(),
      state: DatasetRunState.QUEUED,
      startTime: new Date().toISOString(),
      endTime: null,
      startedBy: 'mock-user'
    };
    this._updateDatasetRuns(datasetId, [newRun, ...dataset.runs]);
    this._simulateRunProgression(datasetId, newRun);
    return of(deepCopy(newRun)).pipe(delay(SIMULATED_DELAY_MS));
    // ─────────────────────────────────────────────────────────────────────────
  }

  // ---------------------------------------------------------------------------
  // GET /bots/:botId/datasets/:datasetId/runs/:runId
  // Polling stops when state reaches COMPLETED or CANCELLED.
  // ---------------------------------------------------------------------------
  getRun(datasetId: string, runId: string): Observable<Pick<DatasetRun, 'id' | 'state' | 'startTime' | 'endTime'>> {
    // ── [REAL] uncomment and delete mock block below ──────────────────────────
    // return this.http.get<Pick<DatasetRun, 'id' | 'state' | 'startTime' | 'endTime'>>(
    //   `${this.apiBase()}/${datasetId}/runs/${runId}`
    // ).pipe(
    //   tap((updated) => this.updateRunState(datasetId, runId, { state: updated.state, endTime: updated.endTime }))
    // );
    // ─────────────────────────────────────────────────────────────────────────

    // ─── [MOCK] delete this block ─────────────────────────────────────────────
    const dataset = this.datasets.find((d) => d.id === datasetId);
    if (!dataset) {
      return throwError(() => ({ status: 404, error: `Dataset ${datasetId} not found` }));
    }
    const run = dataset.runs.find((r) => r.id === runId);
    if (!run) {
      return throwError(() => ({ status: 404, error: `Run ${runId} not found` }));
    }
    if ([DatasetRunState.QUEUED, DatasetRunState.RUNNING].includes(run.state)) {
      this._simulateRunProgression(datasetId, run);
    }
    return of({ id: run.id, state: run.state, startTime: run.startTime, endTime: run.endTime ?? null }).pipe(delay(SIMULATED_DELAY_MS));
    // ─────────────────────────────────────────────────────────────────────────
  }

  // ---------------------------------------------------------------------------
  // GET /bots/:botId/datasets/:datasetId/runs/:runId/actions
  // ---------------------------------------------------------------------------
  getRunActions(datasetId: string, runId: string): Observable<any[]> {
    // ── [REAL] uncomment and delete mock block below ──────────────────────────
    // return this.http.get<any[]>(
    //   `${this.apiBase()}/${datasetId}/runs/${runId}/actions`
    // );
    // ─────────────────────────────────────────────────────────────────────────

    // ─── [MOCK] delete this block ─────────────────────────────────────────────
    const dataset = this.datasets.find((d) => d.id === datasetId);
    if (!dataset) {
      return throwError(() => ({ status: 404, error: `Dataset ${datasetId} not found` }));
    }
    const run = dataset.runs.find((r) => r.id === runId);
    if (!run) {
      return throwError(() => ({ status: 404, error: `Run ${runId} not found` }));
    }
    if ([DatasetRunState.QUEUED, DatasetRunState.RUNNING].includes(run.state)) {
      return throwError(() => ({ status: 409, error: `Run ${runId} is not yet COMPLETED or CANCELLED` }));
    }
    const actions = getMockedDatasetEntries(datasetId, runId);
    return of(deepCopy(actions)).pipe(delay(SIMULATED_DELAY_MS));
    // ─────────────────────────────────────────────────────────────────────────
  }

  // ---------------------------------------------------------------------------
  // POST /bots/:botId/datasets/:datasetId/runs/:runId/cancel
  // ---------------------------------------------------------------------------
  cancelRun(datasetId: string, runId: string): Observable<DatasetRun> {
    // ── [REAL] uncomment and delete mock block below ──────────────────────────
    // return this.http.post<DatasetRun>(`${this.apiBase()}/${datasetId}/runs/${runId}/cancel`, {}).pipe(
    //   tap((cancelled) => this.updateRunState(datasetId, runId, { state: cancelled.state, endTime: cancelled.endTime }))
    // );
    // ─────────────────────────────────────────────────────────────────────────

    // ─── [MOCK] delete this block ─────────────────────────────────────────────
    const dataset = this.datasets.find((d) => d.id === datasetId);
    if (!dataset) {
      return throwError(() => ({ status: 404, error: `Dataset ${datasetId} not found` }));
    }
    const run = dataset.runs.find((r) => r.id === runId);
    if (!run) {
      return throwError(() => ({ status: 404, error: `Run ${runId} not found` }));
    }
    if (![DatasetRunState.QUEUED, DatasetRunState.RUNNING].includes(run.state)) {
      return throwError(() => ({ status: 409, error: `Run ${runId} is already ${run.state}` }));
    }
    const endTime = new Date().toISOString();
    this.updateRunState(datasetId, runId, { state: DatasetRunState.CANCELLED, endTime });
    const cancelledRun = deepCopy({ ...run, state: DatasetRunState.CANCELLED, endTime });
    // Cancel any pending mock progression timers
    const timer = this.runProgressTimers.get(runId);
    if (timer) {
      clearTimeout(timer);
      this.runProgressTimers.delete(runId);
    }
    return of(cancelledRun).pipe(delay(SIMULATED_DELAY_MS));
    // ─────────────────────────────────────────────────────────────────────────
  }

  // ---------------------------------------------------------------------------
  // PATCH run state — updates the run inside the BehaviorSubject immutably    // [REAL] keep
  // Used by the polling loop in the component to reflect server-side state.
  // ---------------------------------------------------------------------------
  updateRunState(datasetId: string, runId: string, patch: Partial<Pick<DatasetRun, 'state' | 'endTime'>>): void {
    const datasets = this.datasets;
    const datasetIndex = datasets.findIndex((d) => d.id === datasetId);
    if (datasetIndex === -1) return;

    const dataset = datasets[datasetIndex];
    const runIndex = dataset.runs.findIndex((r) => r.id === runId);
    if (runIndex === -1) return;

    const updatedRun: DatasetRun = { ...dataset.runs[runIndex], ...patch };
    const updatedRuns = [...dataset.runs.slice(0, runIndex), updatedRun, ...dataset.runs.slice(runIndex + 1)];
    const updatedDataset: Dataset = { ...dataset, runs: updatedRuns };

    this.datasets = [...datasets.slice(0, datasetIndex), updatedDataset, ...datasets.slice(datasetIndex + 1)];
  }

  // ---------------------------------------------------------------------------
  // Domain helpers                                                             // [REAL] keep
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

  /**
   * Returns an observable that emits the formatted elapsed/total duration of a run.
   * For active runs the observable ticks every second — the caller is responsible
   * for unsubscribing (e.g. via takeUntil(destroy$)) to avoid leaks.
   */
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
  // Private helpers                                                            // [REAL] keep
  // ---------------------------------------------------------------------------

  // Merges a freshly-fetched list into the store: preserves _settingsLoaded and
  // run-level data for entries already fully loaded, avoids downgrading the cache.
  private _mergeIntoStore(fetched: Dataset[]): void {
    const existing = new Map(this.datasets.map((d) => [d.id, d]));
    this.datasets = fetched.map((d) => {
      const cached = existing.get(d.id);
      return cached?._settingsLoaded ? cached : d;
    });
  }

  // Inserts or replaces a single dataset entry in the store.
  private _upsertIntoStore(dataset: Dataset): void {
    const index = this.datasets.findIndex((d) => d.id === dataset.id);
    if (index === -1) {
      this.datasets = [dataset, ...this.datasets];
    } else {
      this.datasets = [...this.datasets.slice(0, index), dataset, ...this.datasets.slice(index + 1)];
    }
  }

  // Replaces the runs array of a dataset immutably.
  private _updateDatasetRuns(datasetId: string, runs: DatasetRun[]): void {
    const datasets = this.datasets;
    const index = datasets.findIndex((d) => d.id === datasetId);
    if (index === -1) return;

    const updatedDataset: Dataset = { ...datasets[index], runs };
    this.datasets = [...datasets.slice(0, index), updatedDataset, ...datasets.slice(index + 1)];
  }

  // ─── [MOCK] delete this method entirely ──────────────────────────────────
  private _simulateRunProgression(datasetId: string, run: DatasetRun): void {
    const toRunning = setTimeout(() => {
      const dataset = this.datasets.find((d) => d.id === datasetId);
      const liveRun = dataset?.runs.find((r) => r.id === run.id);
      if (!liveRun || ![DatasetRunState.QUEUED, DatasetRunState.RUNNING].includes(liveRun.state)) return;

      this.updateRunState(datasetId, run.id, { state: DatasetRunState.RUNNING });

      const questionCount = dataset!.questions.length;
      const durationMs = Math.min(5_000 + questionCount * 3_000, 30_000);

      const toCompleted = setTimeout(() => {
        const ds = this.datasets.find((d) => d.id === datasetId);
        const r = ds?.runs.find((r) => r.id === run.id);
        if (!r || r.state !== DatasetRunState.RUNNING) return;

        this.updateRunState(datasetId, run.id, {
          state: DatasetRunState.COMPLETED,
          endTime: new Date().toISOString()
        });
        this.runProgressTimers.delete(run.id);
      }, durationMs);

      this.runProgressTimers.set(run.id, toCompleted);
    }, 2_000);

    this.runProgressTimers.set(run.id, toRunning);
  }
  // ─────────────────────────────────────────────────────────────────────────
}

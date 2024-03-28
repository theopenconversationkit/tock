import { Injectable } from '@angular/core';
import { BehaviorSubject, filter, map, merge, Observable, switchMap, tap, throwError } from 'rxjs';
import { RestService } from '../../core-nlp/rest/rest.service';
import { SourceManagementApiService } from './source-management.api.service';
import { IndexingSession, ProcessAdvancement, Source, SourceImportParams } from './models';

export interface SourcesManagementState {
  loaded: boolean;
  loading: boolean;
  sources: Source[];
}

const SourcesManagementInitialState: SourcesManagementState = {
  loaded: false,
  loading: false,
  sources: []
};

@Injectable({
  providedIn: 'root'
})
export class SourceManagementService {
  private _state: BehaviorSubject<SourcesManagementState>;
  state$: Observable<SourcesManagementState>;

  constructor(private rest: RestService, private sourceApiService: SourceManagementApiService) {
    this._state = new BehaviorSubject(SourcesManagementInitialState);
    this.state$ = this._state.asObservable();
  }

  private getState(): SourcesManagementState {
    return this._state.getValue();
  }

  private setState(state: SourcesManagementState): void {
    return this._state.next(state);
  }

  setSourcesLoading(): void {
    this.setState({
      ...this.getState(),
      loading: true,
      loaded: false
    });
  }

  setSourcesUnloading(): void {
    this.setState({
      ...this.getState(),
      loading: false,
      loaded: false
    });
  }

  setSourcesData(sources: Source[]): void {
    this.setState({
      ...this.getState(),
      loading: false,
      loaded: true,
      sources: sources
    });
  }

  getSources(forceReload: boolean = false): Observable<Array<Source>> {
    if (forceReload) {
      this.setSourcesUnloading();
    }

    const sourcesState = this.state$;
    const notLoaded = sourcesState.pipe(
      filter((state) => !state.loaded && !state.loading),
      tap(() => this.setSourcesLoading()),
      switchMap(() => this.sourceApiService.getSources()),
      tap((sources) => this.setSourcesData(sources)),
      switchMap(() => sourcesState),
      map((state) => state.sources)
    );
    const loaded = sourcesState.pipe(
      filter((state) => state.loaded === true),
      map((state) => state.sources)
    );

    return merge(notLoaded, loaded);
  }

  postSource(source: Source): Observable<Source> {
    return this.sourceApiService.postSource(source).pipe(
      tap((newScenarioGroup) => {
        const state = this.getState();
        state.sources = [...state.sources, newScenarioGroup];
        this.setState(state);
      })
    );
  }

  updateSource(sourcePartial: Partial<Source>): Observable<Source> {
    if (!sourcePartial.id?.trim().length) {
      return throwError(() => new Error('Source Id required for modification'));
    }

    const state = this.getState();
    const existingSource = state.sources.find((s) => s.id === sourcePartial.id);
    const existingSourceIndex = state.sources.indexOf(existingSource);
    if (existingSourceIndex > -1) {
      return this.sourceApiService.updateSource(sourcePartial).pipe(
        tap((modifiedSource) => {
          state.sources[existingSourceIndex] = { ...existingSource, ...modifiedSource };
          this.setState(state);
        })
      );
    }
  }

  deleteSource(sourceId: string): Observable<boolean> {
    return this.sourceApiService.deleteSource(sourceId).pipe(
      tap(() => {
        const state = this.getState();
        state.sources = state.sources.filter((s) => s.id !== sourceId);
        this.setState(state);
      })
    );
  }

  postIndexingSession(source: Source, data?: SourceImportParams): Observable<IndexingSession> {
    const state = this.getState();
    const existingSource = state.sources.find((s) => s.id === source.id);
    const existingSourceIndex = state.sources.indexOf(existingSource);
    if (existingSourceIndex > -1) {
      return this.sourceApiService.postIndexingSession(source, data).pipe(
        tap((indexingSession) => {
          state.sources[existingSourceIndex].indexing_sessions = [...state.sources[existingSourceIndex].indexing_sessions, indexingSession];
          state.sources[existingSourceIndex].status = ProcessAdvancement.running;
          this.setState(state);
        })
      );
    }
  }

  getIndexingSession(source: Source, session: IndexingSession): Observable<IndexingSession> {
    const state = this.getState();
    const existingSource = state.sources.find((s) => s.id === source.id);
    const existingSourceIndex = state.sources.indexOf(existingSource);
    const existingSession = existingSource.indexing_sessions.find((is) => is.id === session.id);
    const existingSessionIndex = existingSource.indexing_sessions.indexOf(existingSession);
    if (existingSessionIndex > -1) {
      return this.sourceApiService.getIndexingSession(source, session).pipe(
        tap((updatedSession) => {
          state.sources[existingSourceIndex].indexing_sessions[existingSessionIndex] = { ...updatedSession };

          if (updatedSession.tasks.every((task) => task.status === ProcessAdvancement.complete)) {
            state.sources[existingSourceIndex].status = ProcessAdvancement.complete;
            state.sources[existingSourceIndex].current_indexing_session_id = updatedSession.id;
          }

          this.setState(state);
        })
      );
    }
  }

  deleteIndexingSession(source: Source, session: IndexingSession): Observable<boolean> {
    return this.sourceApiService.deleteIndexingSession(source, session).pipe(
      tap(() => {
        const state = this.getState();
        const existingSource = state.sources.find((s) => s.id === source.id);
        const existingSourceIndex = state.sources.indexOf(existingSource);
        state.sources[existingSourceIndex].indexing_sessions = state.sources[existingSourceIndex].indexing_sessions.filter(
          (is) => is.id != session.id
        );
        this.setState(state);
      })
    );
  }
}

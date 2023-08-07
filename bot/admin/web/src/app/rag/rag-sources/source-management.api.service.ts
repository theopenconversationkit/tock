import { Injectable } from '@angular/core';
import { Observable, of, switchMap } from 'rxjs';
import { ApplicationService } from '../../core-nlp/applications.service';
import { RestService } from '../../core-nlp/rest/rest.service';
import { Application } from '../../model/application';
import { IndexingSession, IndexingSessionTaskTypes, ProcessAdvancement, Source, SourceImportParams } from './models';
import { TMPsources } from './mock-sources';
import { deepCopy } from '../../shared/utils';

@Injectable()
export class SourceManagementApiService {
  constructor(private rest: RestService, private applicationService: ApplicationService) {}

  getSources(): Observable<Array<Source>> {
    return this.applicationService.retrieveCurrentApplication().pipe(
      switchMap(
        (currentApplication: Application) => of(TMPsources)
        //   this.rest.get<Array<Source>>(
        //     `/bot/${currentApplication.name}/sources`,
        //     (sources: Source[]) => sources
        //   )
      )
    );
  }

  postSource(source: Source): Observable<Source> {
    return this.applicationService.retrieveCurrentApplication().pipe(
      switchMap((currentApplication: Application) => {
        source.id = Math.random().toString().replace('.', '');
        return of(source);
        // return this.rest.post<Source, Source>(`/bot/${currentApplication.name}/sources`, source)
      })
    );
  }

  postIndexingSession(source: Source, data?: SourceImportParams): Observable<IndexingSession> {
    // MOCKING
    this.TEMP_counter = -1;
    // //MOCKING

    return this.applicationService.retrieveCurrentApplication().pipe(
      switchMap((currentApplication: Application) => {
        return of({
          id: Math.random().toString().replace('.', ''),
          start_date: new Date(),
          end_date: null,
          embeding_engine: 'text-embedding-ada-002',
          status: ProcessAdvancement.pristine,
          tasks: deepCopy(this.TEMP_tasks)
        });
        // return this.rest.post<Source, Source>(`/bot/${currentApplication.name}/sources/${source.id}/indexing_session`, data)
      })
    );
  }

  // MOCKING
  TEMP_tasks = [
    { id: Math.random().toString().replace('.', ''), type: IndexingSessionTaskTypes.crawling, status: ProcessAdvancement.pristine },
    { id: Math.random().toString().replace('.', ''), type: IndexingSessionTaskTypes.fetching, status: ProcessAdvancement.pristine },
    { id: Math.random().toString().replace('.', ''), type: IndexingSessionTaskTypes.chunking, status: ProcessAdvancement.pristine },
    { id: Math.random().toString().replace('.', ''), type: IndexingSessionTaskTypes.embeddings, status: ProcessAdvancement.pristine }
  ];

  TEMP_counter = -1;
  // //MOCKING

  getIndexingSession(source: Source, session: IndexingSession): Observable<IndexingSession> {
    // MOCKING
    this.TEMP_counter++;

    let status = ProcessAdvancement.pristine;
    let end_date;

    let tasks = deepCopy(this.TEMP_tasks);

    if (this.TEMP_counter > 2) {
      status = ProcessAdvancement.running;
    }

    if (this.TEMP_counter > 5) {
      tasks[0].status = ProcessAdvancement.running;
    }
    if (this.TEMP_counter > 10) {
      tasks[0].status = ProcessAdvancement.complete;
      tasks[1].status = ProcessAdvancement.running;
    }
    if (this.TEMP_counter > 15) {
      tasks[0].status = ProcessAdvancement.complete;
      tasks[1].status = ProcessAdvancement.complete;
      tasks[2].status = ProcessAdvancement.running;
    }
    if (this.TEMP_counter > 20) {
      tasks[0].status = ProcessAdvancement.complete;
      tasks[1].status = ProcessAdvancement.complete;
      tasks[2].status = ProcessAdvancement.complete;
      tasks[3].status = ProcessAdvancement.running;
    }
    if (this.TEMP_counter > 25) {
      tasks[0].status = ProcessAdvancement.complete;
      tasks[1].status = ProcessAdvancement.complete;
      tasks[2].status = ProcessAdvancement.complete;
      tasks[3].status = ProcessAdvancement.complete;
      status = ProcessAdvancement.complete;
      end_date = new Date();
    }
    // //MOCKING

    return this.applicationService.retrieveCurrentApplication().pipe(
      switchMap((currentApplication: Application) => {
        return of({
          ...session,
          status: status,
          end_date: end_date,
          tasks: tasks
        });
        // return this.rest.get<IndexingSession>(`/bot/${currentApplication.name}/sources/${source.id}/indexing_session/${session.id}`,(indexingSession: IndexingSession) => indexingSession)
      })
    );
  }

  updateSource(source: Partial<Source>): Observable<Source> {
    return this.applicationService.retrieveCurrentApplication().pipe(
      switchMap((currentApplication: Application) => {
        return of(source) as Observable<Source>;
        // return this.rest.put<Source, Source>(
        //   `/bot/${currentApplication.name}/sources/${source.id}`,
        //   source as Source
        // )
      })
    );
  }

  deleteSource(sourceId: string): Observable<boolean> {
    return this.applicationService.retrieveCurrentApplication().pipe(
      switchMap(
        (currentApplication: Application) => of(true)
        // this.rest.delete<boolean>(`/bot/${currentApplication.name}/sources/${sourceId}`)
      )
    );
  }

  deleteIndexingSession(source: Source, session: IndexingSession): Observable<boolean> {
    return this.applicationService.retrieveCurrentApplication().pipe(
      switchMap(
        (currentApplication: Application) => of(true)
        // this.rest.delete<boolean>(`/bot/${currentApplication.name}/sources/${sourceId}/indexing_sessions/${session.id}`)
      )
    );
  }
}

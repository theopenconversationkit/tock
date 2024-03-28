import { TestBed } from '@angular/core/testing';
import { RestService } from '../../core-nlp/rest/rest.service';
import { SourceManagementApiService } from './source-management.api.service';

import { SourceManagementService, SourcesManagementState } from './source-management.service';
import { IndexingSessionTaskTypes, ProcessAdvancement, Source, SourceTypes } from './models';
import { first, of } from 'rxjs';
import { deepCopy } from '../../shared/utils';

const SourcesManagementInitialState: SourcesManagementState = {
  loaded: false,
  loading: false,
  sources: []
};

const sourcesMock = [
  {
    id: '987654321',
    enabled: true,
    name: 'Data source',
    description: '',
    source_type: SourceTypes.file,
    status: ProcessAdvancement.pristine,
    source_parameters: {
      file_format: 'json'
    },
    indexing_sessions: []
  } as Source
];

const newSourceMock = {
  id: '123456789',
  name: 'new test source',
  description: null,
  source_type: 'file',
  source_parameters: {
    source_url: null,
    exclusion_urls: [],
    addExclusionUrlInputControl: null,
    xpaths: [],
    addXPathInputControl: null,
    periodic_update: null,
    periodic_update_frequency: null
  }
} as unknown as Source;

const indexingSessionMock = {
  id: Math.random().toString().replace('.', ''),
  start_date: new Date('2023-09-01T12:25:21.267Z'),
  end_date: null,
  embeding_engine: 'text-embedding-ada-002',
  status: ProcessAdvancement.pristine,
  tasks: [
    { id: Math.random().toString().replace('.', ''), type: IndexingSessionTaskTypes.crawling, status: ProcessAdvancement.pristine },
    { id: Math.random().toString().replace('.', ''), type: IndexingSessionTaskTypes.fetching, status: ProcessAdvancement.pristine },
    { id: Math.random().toString().replace('.', ''), type: IndexingSessionTaskTypes.chunking, status: ProcessAdvancement.pristine },
    { id: Math.random().toString().replace('.', ''), type: IndexingSessionTaskTypes.embeddings, status: ProcessAdvancement.pristine }
  ]
};

describe('SourceManagementService', () => {
  let service: SourceManagementService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        { provide: RestService, useValue: {} },
        {
          provide: SourceManagementApiService,
          useValue: {
            getSources: () => of(sourcesMock),
            postSource: (source) => of(newSourceMock),
            updateSource: (sourcePartial) => of({ ...sourcesMock[0], ...sourcePartial }),
            deleteSource: (sourceId) => of(true),
            postIndexingSession: (source, data) => of(deepCopy(indexingSessionMock)),
            getIndexingSession: (source, session) => {
              const updatedSession = deepCopy(indexingSessionMock);
              updatedSession.status = ProcessAdvancement.complete;
              return of(updatedSession);
            },
            deleteIndexingSession: (source, session) => {}
          }
        }
      ]
    });
    service = TestBed.inject(SourceManagementService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should populate the state with result when loading sources', (done) => {
    expect(service['getState']().loaded).toBeFalse();
    expect(service['getState']()).toEqual(SourcesManagementInitialState);

    service
      .getSources()
      .pipe(first())
      .subscribe(() => {
        const state = service['getState']();
        expect(state.loaded).toBeTrue();
        expect(state.sources).toEqual(sourcesMock);
        done();
      });
  });

  it('should populate the state with result when posting new source', (done) => {
    service
      .getSources()
      .pipe(first())
      .subscribe(() => {
        service
          .postSource(newSourceMock)
          .pipe(first())
          .subscribe(() => {
            const state = service['getState']();
            expect(state.loaded).toBeTrue();
            expect(state.sources).toEqual([...sourcesMock, newSourceMock]);
            done();
          });
      });
  });

  it('should reject a source update if the source identifier is not provided', (done) => {
    service
      .getSources()
      .pipe(first())
      .subscribe(() => {
        service
          .updateSource({ name: 'test renaming' })
          .pipe(first())
          .subscribe({
            error: (error) => {
              expect(error).toEqual(new Error('Source Id required for modification'));
              done();
            }
          });
      });
  });

  it('should populate the state with result when updating a source', (done) => {
    const expectedResult = deepCopy(sourcesMock);
    expectedResult[0].name = 'test renaming';
    service
      .getSources()
      .pipe(first())
      .subscribe(() => {
        service
          .updateSource({ id: '987654321', name: 'test renaming' })
          .pipe(first())
          .subscribe({
            next: (res) => {
              const state = service['getState']();
              expect(state.sources).toEqual(expectedResult);
              done();
            }
          });
      });
  });

  it('should populate the state with result when deleting a source', (done) => {
    service
      .getSources()
      .pipe(first())
      .subscribe(() => {
        service
          .deleteSource('987654321')
          .pipe(first())
          .subscribe({
            next: (res) => {
              const state = service['getState']();
              expect(state.sources).toEqual([]);
              done();
            }
          });
      });
  });

  it('should populate the state with result when posting an indexing session', (done) => {
    const expectedResult = deepCopy(sourcesMock);
    expectedResult[0].status = ProcessAdvancement.running;
    expectedResult[0].indexing_sessions = [deepCopy(indexingSessionMock)];

    service
      .getSources()
      .pipe(first())
      .subscribe(() => {
        service
          .postIndexingSession(deepCopy(sourcesMock[0]))
          .pipe(first())
          .subscribe({
            next: (res) => {
              const state = service['getState']();
              expect(state.sources).toEqual(expectedResult);
              done();
            }
          });
      });
  });
});

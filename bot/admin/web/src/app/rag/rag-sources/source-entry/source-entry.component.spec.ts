import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbDialogService } from '@nebular/theme';
import { IndexingSessionTaskTypes, ProcessAdvancement, Source, SourceTypes } from '../models';

import { SourceEntryComponent, TaskDefinition } from './source-entry.component';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { getSourceMostRecentRunningIndexingSession } from '../commons/utils';

const sourceMock = {
  id: '123456789',
  name: 'Data source one',
  enabled: true,
  description: 'Data source one description',
  source_type: SourceTypes.remote,
  status: ProcessAdvancement.complete,
  source_parameters: {
    source_url: new URL('https://www.sourceone.test'),
    exclusion_urls: [new URL('https://www.sourceone.test/home'), new URL('https://www.sourceone.test/cgu')],
    xpaths: ['//*[@id="st-faq-root"]/section/div/div[2]'],
    periodic_update: true,
    periodic_update_frequency: 30
  },
  current_indexing_session_id: '111111111',
  indexing_sessions: [
    {
      id: '111111111',
      start_date: new Date('2023-07-24T12:06:11.106Z'),
      end_date: new Date('2023-07-24T14:22:07.106Z'),
      embeding_engine: 'text-embedding-ada-002',
      status: ProcessAdvancement.complete
    },
    {
      id: '222222222',
      start_date: new Date('2023-07-25T12:06:11.106Z'),
      end_date: new Date('2023-07-25T14:22:07.106Z'),
      embeding_engine: 'text-embedding-ada-002',
      status: ProcessAdvancement.running,
      tasks: [
        { id: Math.random().toString().replace('.', ''), type: IndexingSessionTaskTypes.crawling, status: ProcessAdvancement.complete },
        { id: Math.random().toString().replace('.', ''), type: IndexingSessionTaskTypes.fetching, status: ProcessAdvancement.error },
        { id: Math.random().toString().replace('.', ''), type: IndexingSessionTaskTypes.chunking, status: ProcessAdvancement.running },
        { id: Math.random().toString().replace('.', ''), type: IndexingSessionTaskTypes.embeddings, status: ProcessAdvancement.pristine }
      ]
    }
  ]
};

describe('SourceEntryComponent', () => {
  let component: SourceEntryComponent;
  let fixture: ComponentFixture<SourceEntryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SourceEntryComponent],
      providers: [{ provide: NbDialogService, useValue: { open: () => {} } }],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(SourceEntryComponent);
    component = fixture.componentInstance;
    component.source = sourceMock as Source;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should list running indexing sessions', () => {
    const expectedResult = [
      {
        type: 'initialization',
        label: 'Initialization',
        icon: 'clock-outline'
      },
      {
        type: 'crawling',
        label: 'Source exploration',
        icon: 'compass'
      },
      {
        type: 'fetching',
        label: 'Data extraction',
        icon: 'code-download-outline'
      },
      {
        type: 'chunking',
        label: 'Text processing',
        icon: 'scissors-outline'
      },
      {
        type: 'embeddings',
        label: 'Word embedding',
        icon: 'menu-arrow-outline'
      }
    ] as TaskDefinition[];

    expect(component.getRunningIndexingSessionTasks()).toEqual(expectedResult);
  });

  it('should detect if an indexing session is running', () => {
    expect(component.hasIndexingSessionRunning()).toBeTrue();
  });

  it('should detect if an indexing session is the current one', () => {
    expect(component.isCurrentIndexingSession(sourceMock.indexing_sessions[0])).toBeTrue();
    expect(component.isCurrentIndexingSession(sourceMock.indexing_sessions[1])).toBeFalse();
  });

  it('should detect if an indexing session is complete', () => {
    expect(component.isSessionComplete(sourceMock.indexing_sessions[0])).toBeTrue();
    expect(component.isSessionComplete(sourceMock.indexing_sessions[1])).toBeFalse();
  });

  it('should return the current indexing session', () => {
    expect(component.getCurrentIndexingSession()).toEqual(sourceMock.indexing_sessions[0]);
  });

  it('should detect if a step is complete', () => {
    expect(component.isStepComplete(IndexingSessionTaskTypes.chunking)).toBeFalse();
    expect(component.isStepComplete(IndexingSessionTaskTypes.crawling)).toBeTrue();
    expect(component.isStepComplete(IndexingSessionTaskTypes.embeddings)).toBeFalse();
    expect(component.isStepComplete(IndexingSessionTaskTypes.fetching)).toBeFalse();
    expect(component.isStepComplete(IndexingSessionTaskTypes.initialization)).toBeTrue();
  });

  it('should detect if a step is running', () => {
    expect(component.isStepRunning(IndexingSessionTaskTypes.chunking)).toBeTrue();
    expect(component.isStepRunning(IndexingSessionTaskTypes.crawling)).toBeFalse();
    expect(component.isStepRunning(IndexingSessionTaskTypes.embeddings)).toBeFalse();
    expect(component.isStepRunning(IndexingSessionTaskTypes.fetching)).toBeFalse();
    expect(component.isStepRunning(IndexingSessionTaskTypes.initialization)).toBeFalse();
  });
});

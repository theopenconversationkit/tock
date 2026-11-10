import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import { TranslocoService } from '@jsverse/transloco';
import { BehaviorSubject, of, Subject, throwError } from 'rxjs';

import { BotConfigurationService } from '../../core/bot-configuration.service';
import { DialogService } from '../../core-nlp/dialog.service';
import { VectorDbProvider } from '../../configuration/vector-db-settings/models/providers-configuration';
import { KnowledgeBaseJobState } from '../../knowledge-base/models';
import { IndexMetadata, InspectedChunk, InspectedDocument, VectorStoreIndex } from '../models/vector-store-inspection.models';
import { VectorStoreInspectionService } from '../services/vector-store-inspection.service';
import { VectorStoreInspectionStateService } from '../services/vector-store-inspection-state.service';
import { ExplorationComponent } from './exploration.component';

describe('ExplorationComponent management', () => {
  let component: ExplorationComponent;
  let inspection: jasmine.SpyObj<VectorStoreInspectionService>;
  let dialogs: jasmine.SpyObj<DialogService>;
  let confirmation: Subject<boolean>;
  const index: VectorStoreIndex = {
    indexName: 'index',
    indexSessionId: 'session',
    indexDatetime: '',
    chunkCount: 1,
    documentCount: 1,
    isCurrent: true
  };
  const document: InspectedDocument = { documentId: 'doc', title: 'Title', source: null, chunkCount: 1, indexSessionId: 'session' };
  const chunk: InspectedChunk = {
    rowId: 'physical-row',
    chunkId: 'doc:1/1',
    chunk: '1/1',
    content: 'Content',
    contentLength: 7,
    metadata: {}
  };

  beforeEach(() => {
    confirmation = new Subject();
    inspection = jasmine.createSpyObj('inspection', [
      'getCapabilities',
      'getIndexes',
      'getDocuments',
      'getIndexMetadata',
      'setEmbeddingModel',
      'deleteChunk',
      'deleteIndex',
      'getMutationJob'
    ]);
    inspection.getCapabilities.and.returnValue(of({ provider: VectorDbProvider.PGVector } as any));
    inspection.getIndexes.and.returnValue(of({ indexes: [index] }));
    inspection.getDocuments.and.returnValue(of({ rows: [document], total: 1, start: 0, end: 1, stats: null, anomalies: [] }));
    inspection.getIndexMetadata.and.returnValue(
      of({ metadata: {}, isCurrent: true, currentEmbeddingModel: 'model', embeddingIncompatible: false })
    );
    inspection.deleteChunk.and.returnValue(of({ deletedCount: 1 }));
    inspection.deleteIndex.and.returnValue(of({ deletedCount: 1 }));
    dialogs = jasmine.createSpyObj('dialogs', ['openDialog']);
    dialogs.openDialog.and.returnValue({ onClose: confirmation } as any);
    TestBed.configureTestingModule({
      declarations: [ExplorationComponent],
      providers: [
        { provide: VectorStoreInspectionService, useValue: inspection },
        VectorStoreInspectionStateService,
        { provide: BotConfigurationService, useValue: { configurations: new BehaviorSubject([{ namespace: 'ns', botId: 'bot' }]) } },
        { provide: DialogService, useValue: dialogs },
        { provide: TranslocoService, useValue: { translate: (key: string) => key } }
      ]
    }).overrideTemplate(ExplorationComponent, '');
    component = TestBed.createComponent(ExplorationComponent).componentInstance;
    component.currentIndex = index;
    component.managementEnabled = true;
    component.configurations = [{ namespace: 'ns', botId: 'bot' }] as any;
  });

  afterEach(() => component.ngOnDestroy());

  it('requires confirmation and explicitly confirms an empty index for the last chunk', () => {
    spyOn(component, 'refresh');
    component.deleteChunk(chunk, document);
    expect(inspection.deleteChunk).not.toHaveBeenCalled();
    expect(dialogs.openDialog.calls.mostRecent().args[1].context['subtitle']).toContain('last_chunk_warning');
    confirmation.next(true);
    expect(inspection.deleteChunk).toHaveBeenCalledWith({
      indexName: 'index',
      rowId: 'physical-row',
      expectedCurrent: true,
      allowEmptyIndex: true,
      unpublishKnowledgeBase: false
    });
  });

  it('does nothing when the user cancels', () => {
    component.deleteChunk(chunk, document);
    confirmation.next(false);
    expect(inspection.deleteChunk).not.toHaveBeenCalled();
  });

  it('does not apply a confirmation to another index', () => {
    component.deleteChunk(chunk, document);
    component.currentIndex = { ...index, indexName: 'other' };
    confirmation.next(true);
    expect(inspection.deleteChunk).not.toHaveBeenCalled();
  });

  it('explains and requests unpublication for a current knowledge base chunk', () => {
    spyOn(component, 'refresh');
    component.deleteChunk({ ...chunk, metadata: { source_type: 'internal_kb', kb_entry_id: 'entry' } }, document);
    expect(dialogs.openDialog.calls.mostRecent().args[1].context['subtitle']).toContain('confirm_unpublish');
    confirmation.next(true);
    expect(inspection.deleteChunk.calls.mostRecent().args[0].unpublishKnowledgeBase).toBeTrue();
  });

  it('only deletes the old-index copy of a knowledge base chunk', () => {
    spyOn(component, 'refresh');
    component.currentIndex = { ...index, isCurrent: false };
    component.deleteChunk({ ...chunk, metadata: { source_type: 'internal_kb' } }, document);
    confirmation.next(true);
    expect(inspection.deleteChunk.calls.mostRecent().args[0].unpublishKnowledgeBase).toBeFalse();
  });

  it('does not offer deletion of the configured index', () => {
    component.deleteIndex();
    expect(dialogs.openDialog).not.toHaveBeenCalled();
    expect(inspection.deleteIndex).not.toHaveBeenCalled();
  });

  it('reports a failed synchronization instead of claiming the chunk was removed', fakeAsync(() => {
    spyOn(component, 'refresh');
    inspection.deleteChunk.and.returnValue(of({ deletedCount: 0, jobId: 'job' }));
    inspection.getMutationJob.and.returnValues(
      of({ state: KnowledgeBaseJobState.QUEUED } as any),
      of({ state: KnowledgeBaseJobState.COMPLETED, failures: [{ error: 'failed' }] } as any)
    );
    component.deleteChunk({ ...chunk, metadata: { source_type: 'internal_kb' } }, document);
    confirmation.next(true);
    tick(0);
    expect(component.managementMessage).toBe('vsi.management.unpublish_pending');
    expect(component.managementBusy).toBeTrue();
    tick(2000);
    expect(component.managementMessage).toBeNull();
    expect(component.managementError).toBe('vsi.management.errors.unpublish_failed');
    expect(component.managementBusy).toBeFalse();
  }));

  it('keeps a pending job available when polling fails', fakeAsync(() => {
    inspection.getMutationJob.and.returnValue(throwError(() => new Error('offline')));
    component.pendingJobId = 'job';
    component.followUnpublication();
    tick(0);
    expect(component.pendingJobId).toBe('job');
    expect(component.managementBusy).toBeFalse();
    expect(component.managementError).toBe('vsi.management.errors.job_status_unavailable');
  }));

  it('ignores late metadata from the previously selected index', () => {
    const first = new Subject<IndexMetadata>();
    const second = new Subject<IndexMetadata>();
    inspection.getIndexMetadata.and.returnValues(first, second);
    component.fetchMetadata();
    component.currentIndex = { ...index, indexName: 'other' };
    component.fetchMetadata();
    second.next({ metadata: { embedding_model: 'new' }, isCurrent: false, currentEmbeddingModel: null, embeddingIncompatible: false });
    first.next({ metadata: {}, isCurrent: true, currentEmbeddingModel: null, embeddingIncompatible: false });
    expect(component.indexMetadata.metadata['embedding_model']).toBe('new');
  });

  it('clears documents and metadata when the last available index disappears', () => {
    component.currentIndex = null;
    component.ngOnInit();
    expect(component.documents.length).toBe(1);
    inspection.getIndexes.and.returnValue(of({ indexes: [] }));
    component.refresh();
    expect(component.currentIndex).toBeNull();
    expect(component.documents).toEqual([]);
    expect(component.indexMetadata).toBeNull();
  });

  it('loads capabilities and metadata once on entry, and reuses capabilities on configuration emissions', () => {
    component.currentIndex = null;
    component.managementEnabled = false;
    component.ngOnInit();
    expect(inspection.getCapabilities).toHaveBeenCalledTimes(1);
    expect(inspection.getIndexMetadata).toHaveBeenCalledTimes(1);
    (TestBed.inject(BotConfigurationService).configurations as BehaviorSubject<any>).next([{ namespace: 'ns', botId: 'bot' }]);
    expect(inspection.getCapabilities).toHaveBeenCalledTimes(1);
    expect(inspection.getIndexMetadata).toHaveBeenCalledTimes(2);
  });

  it('waits for capabilities before fetching index metadata', () => {
    const capabilities = new Subject<any>();
    inspection.getCapabilities.and.returnValue(capabilities);
    component.currentIndex = null;
    component.managementEnabled = false;
    component.ngOnInit();
    expect(inspection.getIndexMetadata).not.toHaveBeenCalled();
    (TestBed.inject(BotConfigurationService).configurations as BehaviorSubject<any>).next([{ namespace: 'ns', botId: 'bot' }]);
    capabilities.next({ provider: VectorDbProvider.PGVector });
    capabilities.complete();
    expect(inspection.getCapabilities).toHaveBeenCalledTimes(1);
    expect(inspection.getIndexes).toHaveBeenCalledTimes(1);
    expect(inspection.getIndexMetadata).toHaveBeenCalledTimes(1);
  });

  it('restores management from cached capabilities when returning to exploration', () => {
    component.currentIndex = null;
    component.ngOnInit();
    component.ngOnDestroy();
    inspection.getIndexMetadata.calls.reset();
    component = TestBed.createComponent(ExplorationComponent).componentInstance;
    component.ngOnInit();
    expect(component.managementEnabled).toBeTrue();
    expect(inspection.getCapabilities).toHaveBeenCalledTimes(1);
    expect(inspection.getIndexMetadata).toHaveBeenCalledTimes(1);
  });

  it('does not load a previous bot after a late capabilities response', () => {
    const previous = new Subject<any>();
    inspection.getCapabilities.and.returnValues(previous, of({ provider: VectorDbProvider.OpenSearch } as any));
    component.currentIndex = null;
    component.managementEnabled = false;
    component.ngOnInit();
    (TestBed.inject(BotConfigurationService).configurations as BehaviorSubject<any>).next([{ namespace: 'ns', botId: 'other' }]);
    previous.next({ provider: VectorDbProvider.PGVector });
    previous.complete();
    expect(component.managementEnabled).toBeFalse();
    expect(inspection.getIndexes).toHaveBeenCalledTimes(1);
    expect(inspection.getIndexMetadata).not.toHaveBeenCalled();
  });

  it('never offers overwriting a nonempty model, including malformed stored values', () => {
    component.indexMetadata = {
      metadata: { embedding_model: 'existing' },
      isCurrent: true,
      currentEmbeddingModel: null,
      embeddingIncompatible: false
    };
    expect(component.canSetEmbeddingModel).toBeFalse();
    component.indexMetadata.metadata['embedding_model'] = 42;
    expect(component.canSetEmbeddingModel).toBeFalse();
    component.indexMetadata.metadata['embedding_model'] = '  ';
    expect(component.canSetEmbeddingModel).toBeTrue();
  });
});

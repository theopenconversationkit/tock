import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BehaviorSubject, of } from 'rxjs';

import { BotConfigurationService } from '../../core/bot-configuration.service';
import { DialogService } from '../../core-nlp/dialog.service';
import { VectorDbProvider } from '../../configuration/vector-db-settings/models/providers-configuration';
import { TestSharedModule } from '../../shared/test-shared.module';
import { IndexMetadata, VectorStoreIndex } from '../models/vector-store-inspection.models';
import { VectorStoreInspectionService } from '../services/vector-store-inspection.service';
import { VectorStoreInspectionStateService } from '../services/vector-store-inspection-state.service';
import { MetadataViewComponent } from '../utils/metadata-view/metadata-view.component';
import { ExplorationComponent } from './exploration.component';

describe('Exploration embedding model cell', () => {
  let fixture: ComponentFixture<ExplorationComponent>;
  let inspection: jasmine.SpyObj<VectorStoreInspectionService>;
  const index: VectorStoreIndex = {
    indexName: 'index',
    indexSessionId: 'session',
    indexDatetime: '',
    chunkCount: 0,
    documentCount: 0,
    isCurrent: true
  };
  const metadata: IndexMetadata = {
    metadata: { embedding_model: 'typo', embedding_model_source: 'manual', nested: { embedding_model: 'nested-model' } },
    isCurrent: true,
    currentEmbeddingModel: 'corrected',
    embeddingIncompatible: true
  };

  beforeEach(async () => {
    inspection = jasmine.createSpyObj('inspection', [
      'getCapabilities',
      'getIndexes',
      'getDocuments',
      'getIndexMetadata',
      'setEmbeddingModel'
    ]);
    inspection.getCapabilities.and.returnValue(of({ provider: VectorDbProvider.PGVector } as any));
    inspection.getIndexes.and.returnValue(of({ indexes: [index] }));
    inspection.getDocuments.and.returnValue(of({ rows: [], total: 0, start: 0, end: 0, stats: null, anomalies: [] }));
    inspection.getIndexMetadata.and.returnValue(of(metadata));
    inspection.setEmbeddingModel.and.callFake((_index, model) =>
      of({ ...metadata, metadata: { ...metadata.metadata, embedding_model: model } })
    );
    await TestBed.configureTestingModule({
      imports: [TestSharedModule],
      declarations: [ExplorationComponent, MetadataViewComponent],
      providers: [
        { provide: VectorStoreInspectionService, useValue: inspection },
        VectorStoreInspectionStateService,
        { provide: BotConfigurationService, useValue: { configurations: new BehaviorSubject([{ namespace: 'ns', botId: 'bot' }]) } },
        { provide: DialogService, useValue: { openDialog: jasmine.createSpy('openDialog') } }
      ],
      // Unrelated exploration widgets are outside this integration test.
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
    fixture = TestBed.createComponent(ExplorationComponent);
    fixture.detectChanges();
  });

  function edit(value: string): HTMLInputElement {
    fixture.nativeElement.querySelector('.model-display').click();
    fixture.detectChanges();
    const input: HTMLInputElement = fixture.nativeElement.querySelector('.model-editor input');
    expect(input.value).toBe('typo');
    expect(document.activeElement).toBe(input);
    input.value = value;
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    return input;
  }

  it('edits only the top-level manual model and saves with Enter', () => {
    expect(fixture.nativeElement.querySelectorAll('.model-display').length).toBe(1);
    const input = edit(' corrected ');
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    fixture.detectChanges();
    expect(inspection.setEmbeddingModel).toHaveBeenCalledOnceWith('index', 'corrected', 'typo');
    expect(fixture.nativeElement.querySelector('.model-editor')).toBeNull();
    expect(fixture.nativeElement.querySelector('.model-display').textContent).toContain('corrected');
    expect(fixture.nativeElement.textContent).toContain('nested-model');
  });

  it('saves with the checkmark and cancels with Escape or the cross', () => {
    edit('discarded').dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.model-editor')).toBeNull();
    edit('discarded');
    fixture.nativeElement.querySelectorAll('.model-editor button')[1].click();
    fixture.detectChanges();
    expect(inspection.setEmbeddingModel).not.toHaveBeenCalled();
    edit('corrected');
    fixture.nativeElement.querySelector('.model-editor button').click();
    fixture.detectChanges();
    expect(inspection.setEmbeddingModel).toHaveBeenCalledOnceWith('index', 'corrected', 'typo');
  });

  it('refuses an empty value and ignores Enter during composition', () => {
    const input = edit('   ');
    expect(fixture.nativeElement.querySelector('.model-editor button').disabled).toBeTrue();
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    input.value = 'corrected';
    input.dispatchEvent(new Event('input'));
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', isComposing: true }));
    expect(inspection.setEmbeddingModel).not.toHaveBeenCalled();
  });

  it('renders automatic and unknown-origin models as plain text', () => {
    for (const source of ['automatic', undefined]) {
      fixture.componentInstance.indexMetadata = { ...metadata, metadata: { embedding_model: 'existing', embedding_model_source: source } };
      fixture.detectChanges();
      expect(fixture.nativeElement.querySelector('.model-display')).toBeNull();
      expect(fixture.nativeElement.querySelector('.model-editor')).toBeNull();
      expect(fixture.nativeElement.querySelector('tock-vector-store-metadata').textContent).toContain('existing');
    }
  });
});

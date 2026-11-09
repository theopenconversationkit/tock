/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { HttpErrorResponse } from '@angular/common/http';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, fakeAsync, flush, tick } from '@angular/core/testing';
import { FormControl } from '@angular/forms';
import { NbToastrService, NbWindowService } from '@nebular/theme';
import { of, throwError } from 'rxjs';
import { RestService } from '../../core-nlp/rest/rest.service';
import { StateService } from '../../core-nlp/state.service';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { deepCopy } from '../../shared/utils';
import { RagEmbeddingCoherence, RagIndexState, RagIndexStatus, RagSettings } from './models';
import { VectorStoreIndex } from '../../vector-store-inspection/models/vector-store-inspection.models';

import { RagSettingsComponent } from './rag-settings.component';
import { TestSharedModule } from '../../shared/test-shared.module';

function index(indexSessionId: string, indexName = `ns_app_bot_new_assistant_session_${indexSessionId}`): VectorStoreIndex {
  return { indexName, indexSessionId, indexDatetime: '2026-11-01T10:00:00', documentCount: 1, chunkCount: 1, isCurrent: false };
}

const settings = {
  id: 'abcdefghijkl123456789',
  namespace: 'app',
  botId: 'new_assistant',
  enabled: true,
  explainabilityEnabled: false,
  engine: 'azureOpenAi',
  embeddingEngine: 'text-embedding-ada-002',
  temperature: '0.15',
  prompt:
    'Use the following context to answer the question at the end.\nIf you dont know the answer, just say {no_answer}.\n\nContext:\n{context}\n\nQuestion:\n{question}\n\nAnswer in {locale}:',
  params: {
    modelName: 'gpt-4-32k',
    deploymentName: 'azure deployment name',
    model: 'model name',
    privateEndpointBaseUrl: 'azure endpoint url',
    apiVersion: '2023-03-15-preview',
    embeddingDeploymentName: 'Embedding deployment name',
    embeddingModelName: 'text-embedding-ada-002',
    embeddingApiKey: 'Embedding OpenAI API Key',
    embeddingApiVersion: '2023-03-15-preview'
  }
} as unknown as RagSettings;

describe('RagSettingsComponent', () => {
  let component: RagSettingsComponent;
  let fixture: ComponentFixture<RagSettingsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RagSettingsComponent],
      imports: [TestSharedModule],
      providers: [
        {
          provide: StateService,
          useValue: {
            currentLocale: 'fr',
            currentApplication: {
              namespace: 'testNamespace',
              name: 'testName'
            }
          }
        },
        {
          provide: RestService,
          useValue: { get: () => of(settings) }
        },
        {
          provide: NbToastrService,
          useValue: { success: () => {} }
        },
        {
          provide: BotConfigurationService,
          useValue: { configurations: of([]) }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(RagSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  xit('should load settings', () => {
    expect(component.settingsBackup).toEqual(settings);

    const cleanedSettings = deepCopy(settings);
    delete cleanedSettings['namespace'];
    delete cleanedSettings['botId'];

    const cleanedFormValue = deepCopy(component.form.getRawValue());
    delete cleanedFormValue.questionAnsweringLlmSetting.apiKey;

    expect(cleanedFormValue as unknown).toEqual(cleanedSettings as unknown);
  });

  describe('index status hints', () => {
    it('flags an unknown model only on a ready index', () => {
      component.indexStatus = {
        indexState: RagIndexState.READY,
        collectionEmbeddingModel: null,
        coherence: RagEmbeddingCoherence.UNKNOWN
      };

      expect(component.indexStatusUnknownModel).toBeTrue();
      expect(component.indexStatusMismatch).toBeFalse();
    });

    it('does not flag an unknown model for a blank/none session', () => {
      component.indexStatus = {
        indexState: RagIndexState.NONE,
        collectionEmbeddingModel: null,
        coherence: RagEmbeddingCoherence.UNKNOWN
      };

      expect(component.indexStatusUnknownModel).toBeFalse();
      expect(component.indexStatusMismatch).toBeFalse();
    });

    it('flags a model mismatch and exposes both models', () => {
      component.indexStatus = {
        indexState: RagIndexState.READY,
        collectionEmbeddingModel: 'text-embedding-ada-002',
        coherence: RagEmbeddingCoherence.MISMATCH
      };
      component.form.controls.emSetting.addControl('model', new FormControl('text-embedding-3-large'), { emitEvent: false });

      expect(component.indexStatusMismatch).toBeTrue();
      expect(component.indexStatusIndexModel).toBe('text-embedding-ada-002');
      expect(component.indexStatusBotModel).toBe('text-embedding-3-large');
    });

    it('shows no hint without a status', () => {
      component.indexStatus = null;

      expect(component.indexStatusUnknownModel).toBeFalse();
      expect(component.indexStatusMismatch).toBeFalse();
      expect(component.indexStatusIndexModel).toBeNull();
      expect(component.indexStatusBotModel).toBe('');
    });
  });

  describe('index status loading', () => {
    // The debounced trigger is plain plumbing; the decision logic lives in loadIndexStatus, which is
    // exercised directly here (of()/throwError() resolve synchronously, so the result is set at once).
    function loadResult(): { session: string | null; status: RagIndexStatus | null } | undefined {
      let result: { session: string | null; status: RagIndexStatus | null } | undefined;
      component['loadIndexStatus']().subscribe((value) => (result = value));
      return result;
    }

    function loadStatus(): RagIndexStatus | null | undefined {
      return loadResult()?.status;
    }

    it('reports NONE for a blank session without calling the endpoint', () => {
      const rest = TestBed.inject(RestService) as unknown as { post: jasmine.Spy };
      rest.post = jasmine.createSpy('post');
      component.indexSessionId.setValue('   ');

      expect(loadResult()).toEqual({
        session: null,
        status: {
          indexState: RagIndexState.NONE,
          collectionEmbeddingModel: null,
          coherence: RagEmbeddingCoherence.UNKNOWN
        }
      });
      expect(rest.post).not.toHaveBeenCalled();
    });

    it('keeps an outage flagged when the session is blank (no remote check)', () => {
      component.indexSessionId.setValue('');
      component.indexStatusUnavailable = true;

      loadStatus();

      expect(component.indexStatusUnavailable).toBeTrue();
    });

    it('does not call the endpoint while the form is incomplete, and keeps an outage flagged', () => {
      const rest = TestBed.inject(RestService) as unknown as { post: jasmine.Spy };
      rest.post = jasmine.createSpy('post');
      component.indexSessionId.setValue('session-1');
      component.indexStatusUnavailable = true;

      expect(loadResult()).toEqual({ session: 'session-1', status: null });
      expect(rest.post).not.toHaveBeenCalled();
      expect(component.indexStatusUnavailable).toBeTrue();
    });

    describe('with a complete form', () => {
      let rest: { post: jasmine.Spy };

      beforeEach(() => {
        rest = TestBed.inject(RestService) as unknown as { post: jasmine.Spy };
        spyOnProperty(component.form, 'valid').and.returnValue(true);
        component.indexSessionId.setValue('session-1');
      });

      it('reads the status from the index-status endpoint', () => {
        const status: RagIndexStatus = {
          indexState: RagIndexState.READY,
          collectionEmbeddingModel: null,
          coherence: RagEmbeddingCoherence.UNKNOWN
        };
        rest.post = jasmine.createSpy('post').and.returnValue(of(status));
        component.indexStatusUnavailable = true;

        expect(loadResult()).toEqual({ session: 'session-1', status });
        expect(rest.post).toHaveBeenCalledWith(
          '/gen-ai/bots/testName/configuration/rag/index-status',
          jasmine.any(Object),
          null,
          null,
          true
        );
        expect(component.indexStatusUnavailable).toBeFalse();
      });

      it('clears the status without locking the form on a refused candidate (400)', () => {
        rest.post = jasmine.createSpy('post').and.returnValue(throwError(() => new HttpErrorResponse({ status: 400 })));

        expect(loadStatus()).toBeNull();
        expect(component.indexStatusUnavailable).toBeFalse();
        expect(component.vectorStoreUnavailable).toBeFalse();
      });

      it('does not lift an outage on a refused candidate (400): it proves no recovery', () => {
        rest.post = jasmine.createSpy('post').and.returnValue(throwError(() => new HttpErrorResponse({ status: 422 })));
        component.indexStatusUnavailable = true;

        expect(loadStatus()).toBeNull();
        expect(component.indexStatusUnavailable).toBeTrue();
      });

      it('locks the form on a server failure', () => {
        rest.post = jasmine.createSpy('post').and.returnValue(throwError(() => 'Server error : 500 - Internal Server Error'));

        expect(loadStatus()).toBeNull();
        expect(component.indexStatusUnavailable).toBeTrue();
        expect(component.vectorStoreUnavailable).toBeTrue();
      });

      it('locks the form on an empty answer', () => {
        rest.post = jasmine.createSpy('post').and.returnValue(of({}));

        expect(loadStatus()).toBeNull();
        expect(component.vectorStoreUnavailable).toBeTrue();
      });
    });
  });

  describe('index status transitions (through the debounced trigger)', () => {
    const ready: RagIndexStatus = {
      indexState: RagIndexState.READY,
      collectionEmbeddingModel: null,
      coherence: RagEmbeddingCoherence.UNKNOWN
    };
    const missing: RagIndexStatus = {
      indexState: RagIndexState.MISSING,
      collectionEmbeddingModel: null,
      coherence: RagEmbeddingCoherence.UNKNOWN
    };
    let rest: { post: jasmine.Spy; delete: jasmine.Spy };
    let formValid: boolean;

    // The debounced trigger keeps a single pending timer: the component must be created inside the fake zone,
    // otherwise a check scheduled during creation runs on a real timer that tick() cannot reach.
    function transitionTest(body: () => void): () => void {
      return fakeAsync(() => {
        rest = TestBed.inject(RestService) as unknown as { post: jasmine.Spy; delete: jasmine.Spy };
        rest.post = jasmine.createSpy('post').and.returnValue(of(null));
        rest.delete = jasmine.createSpy('delete').and.returnValue(of(true));
        fixture = TestBed.createComponent(RagSettingsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        formValid = true;
        spyOnProperty(component.form, 'valid').and.callFake(() => formValid);
        component['applyIndexes']([index('a'), index('b')]);
        flush();

        body();

        flush();
      });
    }

    function failNextChecks(): void {
      rest.post = jasmine.createSpy('post').and.returnValue(throwError(() => new HttpErrorResponse({ status: 503 })));
    }

    function answerNextChecks(status: RagIndexStatus): void {
      rest.post = jasmine.createSpy('post').and.returnValue(of(status));
    }

    function select(session: string | null): void {
      component.indexSessionId.setValue(session);
      tick(400);
    }

    it(
      'keeps the outage when "No index" is then selected',
      transitionTest(() => {
        failNextChecks();
        select('a');
        expect(component.vectorStoreUnavailable).toBeTrue();

        select(null);

        expect(component.vectorStoreUnavailable).toBeTrue();
        component.deleteSettings();
        expect(rest.delete).not.toHaveBeenCalled();
      })
    );

    it(
      'keeps the outage when the form then becomes incomplete',
      transitionTest(() => {
        failNextChecks();
        select('a');
        expect(component.vectorStoreUnavailable).toBeTrue();

        formValid = false;
        select('b');

        expect(component.vectorStoreUnavailable).toBeTrue();
        component.deleteSettings();
        expect(rest.delete).not.toHaveBeenCalled();
      })
    );

    it(
      'lifts the outage once a remote check succeeds',
      transitionTest(() => {
        failNextChecks();
        select('a');
        expect(component.vectorStoreUnavailable).toBeTrue();

        answerNextChecks(ready);
        select('b');

        expect(component.vectorStoreUnavailable).toBeFalse();
        component.deleteSettings();
        expect(rest.delete).toHaveBeenCalled();
      })
    );

    it(
      'treats a listed index as missing when the server answers MISSING for it',
      transitionTest(() => {
        component.enabled.enable({ emitEvent: false });
        component.enabled.setValue(true, { emitEvent: false });
        answerNextChecks(missing);
        select('b');

        expect(component.configuredIndexMissing).toBeTrue();
        expect(component.saveBlockReason).toBe('rag.rag-settings.save_blocked_index_missing');

        component.form.markAsDirty();
        component.submit();
        expect(rest.post).toHaveBeenCalledTimes(1); // the status check only, no save
      })
    );

    it(
      'does not let a MISSING answer for one session block another one',
      transitionTest(() => {
        answerNextChecks(missing);
        select('b');
        expect(component.configuredIndexMissing).toBeTrue();

        // Selecting A: until its own check answers, the MISSING status still held is B's and must not apply to A.
        answerNextChecks(ready);
        component.indexSessionId.setValue('a');
        expect(component.configuredIndexMissing).toBeFalse();

        tick(400);
        expect(component.configuredIndexMissing).toBeFalse();
      })
    );
  });

  describe('index list', () => {
    it('loads the indexes of the current bot', () => {
      const rest = TestBed.inject(RestService) as unknown as { get: jasmine.Spy };
      rest.get = jasmine.createSpy('get').and.callFake((_url: string, parse: (value: any) => any) => of(parse({ indexes: [index('a')] })));

      let result: VectorStoreIndex[] | null | undefined;
      component['getIndexesLoader']().subscribe((indexes) => (result = indexes));

      expect(rest.get).toHaveBeenCalledWith('/gen-ai/bots/testName/vector-store/indexes', jasmine.any(Function), true);
      expect(result).toEqual([index('a')]);
    });

    it('treats an answer without an indexes array as a failure', () => {
      const rest = TestBed.inject(RestService) as unknown as { get: jasmine.Spy };
      rest.get = jasmine.createSpy('get').and.callFake((_url: string, parse: (value: any) => any) => of(parse({})));

      let result: VectorStoreIndex[] | null | undefined;
      component['getIndexesLoader']().subscribe((indexes) => (result = indexes));

      expect(result).toBeNull();
    });

    it('treats a request error as a failure', () => {
      const rest = TestBed.inject(RestService) as unknown as { get: jasmine.Spy };
      rest.get = jasmine.createSpy('get').and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));

      let result: VectorStoreIndex[] | null | undefined;
      component['getIndexesLoader']().subscribe((indexes) => (result = indexes));

      expect(result).toBeNull();
    });

    it('keeps only collections carrying a session id as selectable, reports the others', () => {
      const invalid = index('', 'ns_app_bot_new_assistant_session_ghost');
      component['applyIndexes']([index('recent'), invalid, index('old')]);

      expect(component.indexes.map((i) => i.indexSessionId)).toEqual(['recent', 'old']);
      expect(component.invalidIndexes).toEqual([invalid]);
      expect(component.indexesLoaded).toBeTrue();
      expect(component.vectorStoreUnavailable).toBeFalse();
    });

    it('flags the vector store as unavailable when the list failed', () => {
      component['applyIndexes'](null);

      expect(component.indexesLoaded).toBeFalse();
      expect(component.indexesLoadFailed).toBeTrue();
      expect(component.vectorStoreUnavailable).toBeTrue();
    });

    it('marks as current the index of the saved configuration only', () => {
      component.settingsBackup = { ...settings, indexSessionId: 'old' };

      expect(component.isCurrentIndex(index('old'))).toBeTrue();
      expect(component.isCurrentIndex(index('recent'))).toBeFalse();
    });
  });

  describe('missing configured index', () => {
    beforeEach(() => component['applyIndexes']([index('recent'), index('', 'ns_app_bot_new_assistant_session_ghost')]));

    it('is missing when no valid index matches the configured session', () => {
      component.indexSessionId.setValue('gone');
      expect(component.configuredIndexMissing).toBeTrue();
    });

    it('is not missing when a valid index matches', () => {
      component.indexSessionId.setValue('recent');
      expect(component.configuredIndexMissing).toBeFalse();
    });

    it('is not missing without a configured session', () => {
      component.indexSessionId.setValue('  ');
      expect(component.configuredIndexMissing).toBeFalse();
    });

    it('is never asserted while the list is unknown', () => {
      component['applyIndexes'](null);
      component.indexSessionId.setValue('gone');
      expect(component.configuredIndexMissing).toBeFalse();
    });
  });

  describe('save blocking', () => {
    function setEnabled(value: boolean): void {
      component.enabled.enable({ emitEvent: false });
      component.enabled.setValue(value, { emitEvent: false });
    }

    it('allows saving in the nominal case', () => {
      component['applyIndexes']([index('recent')]);
      component.indexSessionId.setValue('recent');
      setEnabled(true);

      expect(component.saveBlockReason).toBeNull();
    });

    it('blocks saving a missing index while RAG is enabled', () => {
      component['applyIndexes']([index('recent')]);
      component.indexSessionId.setValue('gone');
      setEnabled(true);

      expect(component.saveBlockReason).toBe('rag.rag-settings.save_blocked_index_missing');
      expect(component.canSave).toBeFalse();
    });

    it('allows saving a missing index while RAG is disabled', () => {
      component['applyIndexes']([index('recent')]);
      component.indexSessionId.setValue('gone');
      setEnabled(false);

      expect(component.saveBlockReason).toBeNull();
    });

    describe('when the vector store is unavailable', () => {
      beforeEach(() => {
        component['applyIndexes'](null);
        component.settingsBackup = { ...settings, enabled: true, indexSessionId: 'recent' };
        component.indexSessionId.setValue('recent');
        component.form.markAsPristine();
      });

      it('blocks any save', () => {
        setEnabled(true);
        component.maxDocumentsRetrieved.setValue(8);
        component.maxDocumentsRetrieved.markAsDirty();

        expect(component.saveBlockReason).toBe('rag.rag-settings.save_blocked_unavailable');
      });

      it('allows disabling RAG when nothing else changed', () => {
        setEnabled(false);
        component.enabled.markAsDirty();

        expect(component.saveBlockReason).toBeNull();
      });

      it('blocks disabling RAG along with another change', () => {
        setEnabled(false);
        component.enabled.markAsDirty();
        component.maxDocumentsRetrieved.markAsDirty();

        expect(component.saveBlockReason).toBe('rag.rag-settings.save_blocked_unavailable');
      });

      it('blocks disabling RAG on imported settings', () => {
        setEnabled(false);
        component['importedSettings'] = true;

        expect(component.saveBlockReason).toBe('rag.rag-settings.save_blocked_unavailable');
      });

      it('blocks when RAG was not saved as enabled', () => {
        component.settingsBackup = { ...settings, enabled: false, indexSessionId: 'recent' };
        setEnabled(false);

        expect(component.saveBlockReason).toBe('rag.rag-settings.save_blocked_unavailable');
      });
    });
  });

  describe('submit', () => {
    let rest: { post: jasmine.Spy };

    beforeEach(() => {
      rest = TestBed.inject(RestService) as unknown as { post: jasmine.Spy };
      spyOn(component['translocoService'], 'translate').and.callFake(((key: string) => key) as any);
      spyOnProperty(component.form, 'valid').and.returnValue(true);
    });

    it('clears a missing index when RAG is disabled', () => {
      component['applyIndexes']([index('recent')]);
      component.indexSessionId.setValue('gone');
      component.form.markAsDirty();
      rest.post = jasmine.createSpy('post').and.callFake((_url: string, body: RagSettings) => of({ ...settings, ...body, enabled: false }));

      component.submit();

      expect(rest.post.calls.mostRecent().args[1].indexSessionId).toBeNull();
    });

    it('keeps the configured index when the list could not be loaded', () => {
      component['applyIndexes'](null);
      component.settingsBackup = { ...settings, enabled: true, indexSessionId: 'recent' };
      component.indexSessionId.setValue('recent');
      component.form.markAsPristine();
      component.enabled.enable({ emitEvent: false });
      component.enabled.setValue(false, { emitEvent: false });
      component.enabled.markAsDirty();
      rest.post = jasmine.createSpy('post').and.callFake((_url: string, body: RagSettings) => of({ ...settings, ...body }));

      component.submit();

      const body = rest.post.calls.mostRecent().args[1];
      expect(body.indexSessionId).toBe('recent');
      expect(body.enabled).toBeFalse();
    });

    it('does not post while saving is blocked', () => {
      component['applyIndexes'](null);
      component.form.markAsDirty();
      rest.post = jasmine.createSpy('post');

      component.submit();

      expect(rest.post).not.toHaveBeenCalled();
    });

    it('moves the current index mark once the server confirmed the save', () => {
      component['applyIndexes']([index('recent'), index('old')]);
      component.settingsBackup = { ...settings, indexSessionId: 'old' };
      component.indexSessionId.setValue('recent');
      component.form.markAsDirty();
      rest.post = jasmine.createSpy('post').and.callFake((_url: string, body: RagSettings) => of({ ...settings, ...body }));

      expect(component.isCurrentIndex(index('old'))).toBeTrue();

      component.submit();

      expect(component.isCurrentIndex(index('recent'))).toBeTrue();
      expect(component.isCurrentIndex(index('old'))).toBeFalse();
    });
  });

  describe('save error handling', () => {
    let toastr: { danger: jasmine.Spy };
    let windowOpen: jasmine.Spy;

    beforeEach(() => {
      toastr = TestBed.inject(NbToastrService) as unknown as { danger: jasmine.Spy };
      toastr.danger = jasmine.createSpy('danger');
      windowOpen = spyOn(TestBed.inject(NbWindowService), 'open');
      spyOn(component['translocoService'], 'translate').and.callFake(((key: string) => key) as any);
    });

    it('shows the dedicated message when the server refuses an incompatible embedding model', () => {
      component['onSaveError'](
        new HttpErrorResponse({
          status: 400,
          error: { errors: [{ code: null, message: 'rag.embedding.incompatible_index', params: null }] }
        })
      );

      expect(toastr.danger).toHaveBeenCalledWith('rag.rag-settings.embedding_incompatible_error', 'rag.rag-settings.error_title', {
        duration: 5000,
        status: 'danger'
      });
      expect(windowOpen).not.toHaveBeenCalled();
      expect(component.loading).toBeFalse();
    });

    it('keeps the generic message and the debug window for any other error', () => {
      component['onSaveError'](
        new HttpErrorResponse({
          status: 400,
          error: { errors: [{ code: null, message: 'rag.some.other_error', params: null }] }
        })
      );

      expect(toastr.danger).toHaveBeenCalledWith('rag.rag-settings.an_error_occurred', 'rag.rag-settings.error_title', {
        duration: 5000,
        status: 'danger'
      });
      expect(windowOpen).toHaveBeenCalled();
      expect(component.loading).toBeFalse();
    });

    it('routes a failed submit through the save error handling', () => {
      const rest = TestBed.inject(RestService) as unknown as { post: jasmine.Spy };
      rest.post = jasmine.createSpy('post').and.returnValue(
        throwError(
          () =>
            new HttpErrorResponse({
              status: 400,
              error: { errors: [{ code: null, message: 'rag.embedding.incompatible_index', params: null }] }
            })
        )
      );
      spyOnProperty(component, 'canSave').and.returnValue(true);
      component.form.markAsDirty();

      component.submit();

      expect(rest.post).toHaveBeenCalled();
      expect(toastr.danger).toHaveBeenCalledWith('rag.rag-settings.embedding_incompatible_error', 'rag.rag-settings.error_title', {
        duration: 5000,
        status: 'danger'
      });
    });
  });
});

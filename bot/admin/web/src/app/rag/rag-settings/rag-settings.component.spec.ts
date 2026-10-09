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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl } from '@angular/forms';
import { NbToastrService, NbWindowService } from '@nebular/theme';
import { of, throwError } from 'rxjs';
import { RestService } from '../../core-nlp/rest/rest.service';
import { StateService } from '../../core-nlp/state.service';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { deepCopy } from '../../shared/utils';
import { RagEmbeddingCoherence, RagIndexState, RagIndexStatus, RagSettings } from './models';

import { RagSettingsComponent } from './rag-settings.component';
import { TestSharedModule } from '../../shared/test-shared.module';

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
    it('flags a missing index', () => {
      component.indexStatus = {
        indexState: RagIndexState.MISSING,
        collectionEmbeddingModel: null,
        coherence: RagEmbeddingCoherence.UNKNOWN
      };

      expect(component.indexStatusMissing).toBeTrue();
      expect(component.indexStatusUnknownModel).toBeFalse();
      expect(component.indexStatusMismatch).toBeFalse();
    });

    it('flags an unknown model only on a ready index', () => {
      component.indexStatus = {
        indexState: RagIndexState.READY,
        collectionEmbeddingModel: null,
        coherence: RagEmbeddingCoherence.UNKNOWN
      };

      expect(component.indexStatusUnknownModel).toBeTrue();
      expect(component.indexStatusMissing).toBeFalse();
      expect(component.indexStatusMismatch).toBeFalse();
    });

    it('does not flag an unknown model for a blank/none session', () => {
      component.indexStatus = {
        indexState: RagIndexState.NONE,
        collectionEmbeddingModel: null,
        coherence: RagEmbeddingCoherence.UNKNOWN
      };

      expect(component.indexStatusUnknownModel).toBeFalse();
      expect(component.indexStatusMissing).toBeFalse();
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

      expect(component.indexStatusMissing).toBeFalse();
      expect(component.indexStatusUnknownModel).toBeFalse();
      expect(component.indexStatusMismatch).toBeFalse();
      expect(component.indexStatusIndexModel).toBeNull();
      expect(component.indexStatusBotModel).toBe('');
    });
  });

  describe('index status loading', () => {
    // The debounced trigger is plain plumbing; the decision logic lives in loadIndexStatus, which is
    // exercised directly here (of()/throwError() resolve synchronously, so the result is set at once).
    function loadStatus(): RagIndexStatus | null | undefined {
      let result: RagIndexStatus | null | undefined;
      component['loadIndexStatus']().subscribe((status) => (result = status));
      return result;
    }

    it('reports NONE for a blank session without calling the endpoint', () => {
      const rest = TestBed.inject(RestService) as unknown as { post: jasmine.Spy };
      rest.post = jasmine.createSpy('post');
      component.indexSessionId.setValue('   ');

      expect(loadStatus()).toEqual({
        indexState: RagIndexState.NONE,
        collectionEmbeddingModel: null,
        coherence: RagEmbeddingCoherence.UNKNOWN
      });
      expect(rest.post).not.toHaveBeenCalled();
    });

    it('reads the status from the index-status endpoint when a session is set', () => {
      const status: RagIndexStatus = {
        indexState: RagIndexState.MISSING,
        collectionEmbeddingModel: null,
        coherence: RagEmbeddingCoherence.UNKNOWN
      };
      const rest = TestBed.inject(RestService) as unknown as { post: jasmine.Spy };
      rest.post = jasmine.createSpy('post').and.returnValue(of(status));
      component.indexSessionId.setValue('session-1');

      expect(loadStatus()).toEqual(status);
      expect(rest.post).toHaveBeenCalledWith('/gen-ai/bots/testName/configuration/rag/index-status', jasmine.any(Object));
    });

    it('swallows an endpoint failure and clears the status', () => {
      const rest = TestBed.inject(RestService) as unknown as { post: jasmine.Spy };
      rest.post = jasmine.createSpy('post').and.returnValue(throwError(() => new Error('boom')));
      component.indexSessionId.setValue('session-1');

      expect(loadStatus()).toBeNull();
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

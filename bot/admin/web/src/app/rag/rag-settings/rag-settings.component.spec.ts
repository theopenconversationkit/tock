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

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbToastrService } from '@nebular/theme';
import { of } from 'rxjs';
import { RestService } from '../../core-nlp/rest/rest.service';
import { StateService } from '../../core-nlp/state.service';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { deepCopy } from '../../shared/utils';
import { RagSettings } from './models';

import { RagSettingsComponent } from './rag-settings.component';

const settings = {
  id: 'abcdefghijkl123456789',
  namespace: 'app',
  botId: 'new_assistant',
  enabled: true,
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

  it('should load settings', () => {
    expect(component.settingsBackup).toEqual(settings);

    const cleanedSettings = deepCopy(settings);
    delete cleanedSettings['namespace'];
    delete cleanedSettings['botId'];

    const cleanedFormValue = deepCopy(component.form.getRawValue());
    delete cleanedFormValue.questionAnsweringLlmSetting.apiKey;

    expect(cleanedFormValue as unknown).toEqual(cleanedSettings as unknown);
  });
});

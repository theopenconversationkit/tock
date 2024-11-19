import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbToastrService } from '@nebular/theme';
import { of } from 'rxjs';
import { BotService } from '../../bot/bot-service';
import { StoryDefinitionConfigurationSummary } from '../../bot/model/story';
import { RestService } from '../../core-nlp/rest/rest.service';
import { StateService } from '../../core-nlp/state.service';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { deepCopy } from '../../shared/utils';
import { RagSettings } from './models';

import { RagSettingsComponent } from './rag-settings.component';

const stories = [
  {
    _id: '123456789abcdefghijkl',
    storyId: 'teststory',
    botId: 'new_assistant',
    intent: {
      name: 'testintent'
    },
    currentType: 'simple',
    name: 'Test story',
    category: 'faq',
    description: '',
    lastEdited: '2023-07-31T14:48:21.291Z'
  } as unknown as StoryDefinitionConfigurationSummary
];

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
  },
  noAnswerSentence: 'No answer sentence',
  noAnswerStoryId: 'null'
} as unknown as RagSettings;

describe('RagSettingsComponent', () => {
  let component: RagSettingsComponent;
  let fixture: ComponentFixture<RagSettingsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RagSettingsComponent],
      providers: [
        {
          provide: BotService,
          useValue: {
            searchStories: () => of(stories)
          }
        },
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

  it('should load stories', () => {
    expect(component.availableStories).toEqual(stories);
  });

  it('should load settings', () => {
    expect(component.settingsBackup).toEqual(settings);

    const cleanedSettings = deepCopy(settings);
    delete cleanedSettings['namespace'];
    delete cleanedSettings['botId'];

    const cleanedFormValue = deepCopy(component.form.getRawValue());
    delete cleanedFormValue.params.apiKey;

    expect(cleanedFormValue as unknown).toEqual(cleanedSettings as unknown);
  });
});

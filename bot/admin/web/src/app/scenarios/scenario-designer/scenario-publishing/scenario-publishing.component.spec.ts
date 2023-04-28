import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { NbDialogService, NbToastrService } from '@nebular/theme';

import { BotService } from '../../../bot/bot-service';
import { StateService } from '../../../core-nlp/state.service';
import { NlpService } from '../../../nlp-tabs/nlp.service';
import {
  MachineState,
  ScenarioVersionExtended,
  SCENARIO_ITEM_FROM_BOT,
  SCENARIO_ITEM_FROM_CLIENT,
  SCENARIO_MODE,
  SCENARIO_STATE,
  TickStory
} from '../../models';
import { ScenarioService } from '../../services';
import { ScenarioDesignerService } from '../scenario-designer.service';
import { ScenarioPublishingComponent } from './scenario-publishing.component';
import { DependencyUpdateJob } from '../../models/designer.model';
import { deepCopy } from '../../../shared/utils';
import { TestingModule } from '../../../../testing';

const testScenario: ScenarioVersionExtended = {
  id: '63eb951b0bf5327dd6862bf7',
  data: {
    mode: SCENARIO_MODE.publishing,
    scenarioItems: [
      {
        id: 0,
        from: SCENARIO_ITEM_FROM_CLIENT,
        text: "Quelle est la couleur du cheval blanc d'Henri IV",
        main: true,
        intentDefinition: {
          label: "Quelle est la couleur du cheval blanc d'Henri IV",
          name: 'quelleEstLaCouleurDuChevalBlancDHenriIv',
          category: 'scenarios',
          primary: true,
          sentences: [
            {
              namespace: 'app',
              applicationName: 'new_assistant',
              language: 'fr',
              query: "quelle est la couleur du cheval blanc d'henry iV ?",
              checkExistingQuery: false,
              state: '',
              classification: { entities: [] }
            }
          ],
          outputContextNames: [],
          intentId: '63e3774a0bf5327dd6862b57'
        }
      },
      {
        id: 1,
        parentIds: [0],
        from: SCENARIO_ITEM_FROM_BOT,
        text: "Parlez vous d'Henri IV de France ou d'Henry IV d'Angleterre ?",
        actionDefinition: {
          name: 'HENRI_IV_DE_FRANCE_OU_DANGLETERRE',
          description: "Parlez vous d'Henri IV de France ou d'Henry IV d'Angleterre ???",
          answers: [
            {
              answer: 'Are you talking about Henri IV of France or Henry IV of England?',
              locale: 'en',
              interfaceType: 0
            },
            {
              locale: 'fr',
              interfaceType: 0,
              answer: "Parlez vous d'Henri IV de France ou d'Henry IV d'Angleterre ???"
            }
          ],
          answerId: 'app_scenario_Are you talking about Henri IV of France or Henry IV of England?',
          inputContextNames: [],
          outputContextNames: ['FRANCE'],
          unknownAnswers: [
            {
              locale: 'fr',
              interfaceType: 0,
              answer: "Merci de préciser si vous parlez d'Henri IV de France ou d'Henry IV d'Angleterre"
            }
          ],
          final: false
        }
      },
      {
        id: 2,
        parentIds: [1],
        from: SCENARIO_ITEM_FROM_CLIENT,
        text: 'Henri IV de France',
        intentDefinition: {
          label: 'Henri IV de France',
          name: 'henriIvDeFrance',
          category: 'scenarios',
          primary: false,
          sentences: [
            {
              namespace: 'app',
              applicationName: 'new_assistant',
              language: 'fr',
              query: 'france',
              checkExistingQuery: false,
              state: '',
              classification: { entities: [] }
            }
          ],
          outputContextNames: ['FRANCE'],
          intentId: '63e397480bf5327dd6862b5b'
        }
      },
      {
        id: 3,
        parentIds: [2],
        from: SCENARIO_ITEM_FROM_BOT,
        text: "Le cheval d'Henri IV était de robe grise",
        final: true,
        actionDefinition: {
          name: 'GRISE',
          description: "Le cheval d'Henri IV était de robe grise",
          answers: [
            {
              answer: 'The horse of Henri IV was of gray dress',
              locale: 'en',
              interfaceType: 0
            },
            {
              locale: 'fr',
              answer: "Le cheval d'Henri IV était de robe grise",
              interfaceType: 0
            }
          ],
          answerId: 'app_scenario_The horse of Henri IV was of gray dress',
          inputContextNames: ['FRANCE'],
          outputContextNames: [],
          unknownAnswers: [],
          final: true
        }
      },
      {
        id: 4,
        parentIds: [1],
        from: SCENARIO_ITEM_FROM_CLIENT,
        text: "Henry IV d'Angleterre",
        intentDefinition: {
          label: "Henry IV d'Angleterre",
          name: 'henryIvDAngleterre',
          category: 'scenarios',
          primary: false,
          sentences: [
            {
              namespace: 'app',
              applicationName: 'new_assistant',
              language: 'fr',
              query: 'angleterre',
              checkExistingQuery: false,
              state: '',
              classification: { entities: [] }
            }
          ],
          outputContextNames: ['ANGLETERRE'],
          intentId: '63e397480bf5327dd6862b5d'
        }
      },
      {
        id: 5,
        parentIds: [4],
        from: SCENARIO_ITEM_FROM_BOT,
        text: "Le cheval d'Henry IV était de robe rouanne",
        actionDefinition: {
          name: 'ROUANNE',
          description: "Le cheval d'Henry IV était de robe rouanne",
          answers: [
            {
              answer: 'The horse of Henry IV was of red dress',
              locale: 'en',
              interfaceType: 0
            },
            {
              locale: 'fr',
              answer: "Le cheval d'Henry IV était de robe rouanne",
              interfaceType: 0
            }
          ],
          answerId: 'app_scenario_The horse of Henry IV was of red dress',
          inputContextNames: ['ANGLETERRE'],
          outputContextNames: [],
          unknownAnswers: [],
          final: false
        }
      }
    ],
    contexts: [
      {
        name: 'FRANCE',
        type: 'string',
        entityType: 'app:henriiv',
        entityRole: 'henriiv'
      },
      {
        name: 'ANGLETERRE',
        type: 'string',
        entityType: 'app:henryiv',
        entityRole: 'henryiv'
      }
    ],
    stateMachine: {
      id: 'root',
      type: 'parallel',
      states: {
        Global: {
          id: 'Global',
          states: {
            WHICH: {
              id: 'WHICH',
              states: {
                HENRI_IV_DE_FRANCE_OU_DANGLETERRE: {
                  id: 'HENRI_IV_DE_FRANCE_OU_DANGLETERRE'
                },
                GRISE: { id: 'GRISE' },
                ROUANNE: { id: 'ROUANNE' }
              },
              on: {
                henriIvDeFrance: '#GRISE',
                henryIvDAngleterre: '#ROUANNE'
              },
              initial: 'HENRI_IV_DE_FRANCE_OU_DANGLETERRE'
            }
          },
          on: { quelleEstLaCouleurDuChevalBlancDHenriIv: '#WHICH' },
          initial: 'WHICH'
        }
      },
      initial: 'Global',
      on: {}
    },
    triggers: []
  },
  state: SCENARIO_STATE.draft,
  comment: 'v23'
};

describe('ScenarioPublishingComponent', () => {
  let component: ScenarioPublishingComponent;
  let fixture: ComponentFixture<ScenarioPublishingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScenarioPublishingComponent],
      imports: [TestingModule],
      providers: [
        {
          provide: ScenarioService,
          useValue: {}
        },
        { provide: NbToastrService, useValue: {} },
        {
          provide: StateService,
          useValue: {
            currentApplication: { name: 'testApplication' }
          }
        },
        {
          provide: ScenarioDesignerService,
          useValue: {}
        },
        { provide: NbDialogService, useValue: {} },
        {
          provide: BotService,
          useValue: {}
        },
        {
          provide: NlpService,
          useValue: {}
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScenarioPublishingComponent);
    component = fixture.componentInstance;
    component.scenario = testScenario;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('Should destroy', () => {
    expect(component.ngOnDestroy).toBeDefined();
    expect(component.destroy.isStopped).toBeFalsy();
    component.ngOnDestroy();
    expect(component.destroy.isStopped).toBeTruthy();
  });

  it('Should allow publication if the scenario is valid', () => {
    expect(component.isScenarioUnPublishable()).not.toBeDefined();
    expect(fixture.debugElement.query(By.css('[data-testid="publish-button"]'))).not.toBeNull();
  });

  describe('Publication restrictions', () => {
    [
      {
        description: 'Should not allow publication if Casting step is not valid',
        mutations: (scenarioCopy) => {
          scenarioCopy.data.scenarioItems[0].text = '';
          return scenarioCopy;
        },
        result: 'At least one of the previous steps requires your attention'
      },
      {
        description: 'Should not allow publication if Production step is not valid',
        mutations: (scenarioCopy) => {
          delete scenarioCopy.data.scenarioItems[1].actionDefinition;
          return scenarioCopy;
        },
        result: 'At least one of the previous steps requires your attention'
      },
      {
        description: 'Should not allow publication if Publishing step is not valid',
        mutations: (scenarioCopy) => {
          scenarioCopy.data.stateMachine = {} as MachineState;
          return scenarioCopy;
        },
        result: 'At least one of the previous steps requires your attention'
      },
      {
        description: 'Should not allow publication if referenced handlers are not avalaible',
        mutations: (scenarioCopy) => {
          Object.defineProperty(component, 'avalaibleHandlers', { value: [] });
          scenarioCopy.data.scenarioItems[1].actionDefinition.handler = 'testMissingHandler';
          return scenarioCopy;
        },
        result: 'The following handlers are not yet implemented on the server side : testMissingHandler'
      },
      {
        description: 'Should not allow publication if referenced targetStory is not avalaible',
        mutations: (scenarioCopy) => {
          Object.defineProperty(component, 'availableStories', { value: [] });
          scenarioCopy.data.scenarioItems[1].actionDefinition.targetStory = 'missingTestStory';
          return scenarioCopy;
        },
        result: 'The following target stories have been removed : missingTestStory'
      }
    ].forEach((test) => {
      it(test.description, () => {
        component.scenario = test.mutations(deepCopy(testScenario));
        fixture.detectChanges();
        expect(component.isScenarioUnPublishable()).toEqual(test.result);
        expect(fixture.debugElement.query(By.css('[data-testid="publish-button"]'))).toBeNull();
      });
    });
  });

  it('Should list dependencies to create or update', () => {
    const dependencies = {
      intentsToCreate: [],
      intentsToUpdate: [
        {
          type: 'update' as DependencyUpdateJob['type'],
          done: false,
          item: testScenario.data.scenarioItems[0]
        },
        {
          type: 'update' as DependencyUpdateJob['type'],
          done: false,
          item: testScenario.data.scenarioItems[2]
        },
        {
          type: 'update' as DependencyUpdateJob['type'],
          done: false,
          item: testScenario.data.scenarioItems[4]
        }
      ],
      answersToCreate: [],
      answersToUpdate: [],
      unknownAnswersToCreate: [
        {
          type: 'creation' as DependencyUpdateJob['type'],
          done: false,
          item: testScenario.data.scenarioItems[1],
          answer: testScenario.data.scenarioItems[1].actionDefinition.unknownAnswers[0]
        }
      ],
      unknownAnswersToUpdate: []
    };

    expect(component.dependencies).toEqual(dependencies);
  });

  it('Should compile tick story correctly', () => {
    const expectedTickStory = {
      storyId: 'testScenarioGroupId',
      botId: 'testApplication',
      name: 'TickStory from the scenarioGroup "testScenarioGroupName"',
      description: 'TickStory from the scenarioGroup "testScenarioGroupName" with id testScenarioGroupId',
      mainIntent: 'quelleEstLaCouleurDuChevalBlancDHenriIv',
      primaryIntents: [],
      secondaryIntents: ['henriIvDeFrance', 'henryIvDAngleterre'],
      contexts: [
        { name: 'FRANCE', type: 'string', entityType: 'app:henriiv', entityRole: 'henriiv' },
        { name: 'ANGLETERRE', type: 'string', entityType: 'app:henryiv', entityRole: 'henryiv' }
      ],
      triggers: [],
      actions: [
        {
          name: 'HENRI_IV_DE_FRANCE_OU_DANGLETERRE',
          description: "Parlez vous d'Henri IV de France ou d'Henry IV d'Angleterre ???",
          answers: [
            { answer: 'Are you talking about Henri IV of France or Henry IV of England?', locale: 'en', interfaceType: 0 },
            { locale: 'fr', interfaceType: 0, answer: "Parlez vous d'Henri IV de France ou d'Henry IV d'Angleterre ???" }
          ],
          answerId: 'app_scenario_Are you talking about Henri IV of France or Henry IV of England?',
          inputContextNames: [],
          outputContextNames: ['FRANCE'],
          unknownAnswers: [
            { locale: 'fr', interfaceType: 0, answer: "Merci de préciser si vous parlez d'Henri IV de France ou d'Henry IV d'Angleterre" }
          ],
          final: false
        },
        {
          name: 'GRISE',
          description: "Le cheval d'Henri IV était de robe grise",
          answers: [
            { answer: 'The horse of Henri IV was of gray dress', locale: 'en', interfaceType: 0 },
            { locale: 'fr', answer: "Le cheval d'Henri IV était de robe grise", interfaceType: 0 }
          ],
          answerId: 'app_scenario_The horse of Henri IV was of gray dress',
          inputContextNames: ['FRANCE'],
          outputContextNames: [],
          unknownAnswers: [],
          final: true
        },
        {
          name: 'ROUANNE',
          description: "Le cheval d'Henry IV était de robe rouanne",
          answers: [
            { answer: 'The horse of Henry IV was of red dress', locale: 'en', interfaceType: 0 },
            { locale: 'fr', answer: "Le cheval d'Henry IV était de robe rouanne", interfaceType: 0 }
          ],
          answerId: 'app_scenario_The horse of Henry IV was of red dress',
          inputContextNames: ['ANGLETERRE'],
          outputContextNames: [],
          unknownAnswers: [],
          final: false
        }
      ],
      stateMachine: {
        id: 'root',
        type: 'parallel',
        states: {
          Global: {
            id: 'Global',
            states: {
              WHICH: {
                id: 'WHICH',
                states: {
                  HENRI_IV_DE_FRANCE_OU_DANGLETERRE: { id: 'HENRI_IV_DE_FRANCE_OU_DANGLETERRE' },
                  GRISE: { id: 'GRISE' },
                  ROUANNE: { id: 'ROUANNE' }
                },
                on: { henriIvDeFrance: '#GRISE', henryIvDAngleterre: '#ROUANNE' },
                initial: 'HENRI_IV_DE_FRANCE_OU_DANGLETERRE'
              }
            },
            on: { quelleEstLaCouleurDuChevalBlancDHenriIv: '#WHICH' },
            initial: 'WHICH'
          }
        },
        initial: 'Global',
        on: {}
      },
      intentsContexts: [
        { intentName: 'henriIvDeFrance', associations: [{ actionName: 'HENRI_IV_DE_FRANCE_OU_DANGLETERRE', contextNames: ['FRANCE'] }] },
        {
          intentName: 'henryIvDAngleterre',
          associations: [{ actionName: 'HENRI_IV_DE_FRANCE_OU_DANGLETERRE', contextNames: ['ANGLETERRE'] }]
        }
      ],
      unknownAnswerConfigs: []
    } as unknown as TickStory;

    component.scenario._scenarioGroupId = 'testScenarioGroupId';
    component.scenario._name = 'testScenarioGroupName';
    const tickStory = component['compileTickStory']();
    expect(tickStory).toEqual(expectedTickStory);
  });
});

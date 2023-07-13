import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbDialogService } from '@nebular/theme';

import { ScenarioProductionComponent } from './scenario-production.component';
import { ScenarioProductionService } from './scenario-production.service';
import { ScenarioVersionExtended, SCENARIO_ITEM_FROM_BOT, SCENARIO_ITEM_FROM_CLIENT, SCENARIO_MODE, SCENARIO_STATE } from '../../models';
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

describe('ScenarioProductionComponent', () => {
  let component: ScenarioProductionComponent;
  let fixture: ComponentFixture<ScenarioProductionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScenarioProductionComponent],
      imports: [TestingModule],
      providers: [
        {
          provide: ScenarioProductionService,
          useValue: {}
        },
        { provide: NbDialogService, useValue: {} }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScenarioProductionComponent);
    component = fixture.componentInstance;
    component.scenario = deepCopy(testScenario);
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

  it('Should correctly update state machine when droping an intent on a state', () => {
    const dropEvent = {
      type: 'itemDropped',
      stateId: 'HENRI_IV_DE_FRANCE_OU_DANGLETERRE',
      dropped: {
        type: 'intent',
        name: 'henriIvDeFrance'
      }
    };

    component.itemDropped(dropEvent);

    expect(component.scenario.data.stateMachine).toEqual({
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
              on: { henriIvDeFrance: '#HENRI_IV_DE_FRANCE_OU_DANGLETERRE', henryIvDAngleterre: '#ROUANNE' },
              initial: 'HENRI_IV_DE_FRANCE_OU_DANGLETERRE'
            }
          },
          on: { quelleEstLaCouleurDuChevalBlancDHenriIv: '#WHICH' },
          initial: 'WHICH'
        }
      },
      initial: 'Global',
      on: {}
    });
  });

  it('Should correctly update state machine when droping an action on a state', () => {
    const scenarioCopy = deepCopy(testScenario);
    scenarioCopy.data.scenarioItems.push({
      id: 6,
      parentIds: [4],
      from: SCENARIO_ITEM_FROM_BOT,
      text: 'test action',
      actionDefinition: {
        name: 'TESTACTION'
      }
    });

    component.scenario = scenarioCopy;

    const dropEvent = {
      type: 'itemDropped',
      stateId: 'WHICH',
      dropped: {
        type: 'action',
        name: 'TESTACTION'
      }
    };

    component.itemDropped(dropEvent);

    expect(component.scenario.data.stateMachine).toEqual({
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
                GRISE: {
                  id: 'GRISE'
                },
                ROUANNE: {
                  id: 'ROUANNE'
                },
                TESTACTION: {
                  id: 'TESTACTION'
                }
              },
              on: {
                henriIvDeFrance: '#GRISE',
                henryIvDAngleterre: '#ROUANNE'
              },
              initial: 'HENRI_IV_DE_FRANCE_OU_DANGLETERRE'
            }
          },
          on: {
            quelleEstLaCouleurDuChevalBlancDHenriIv: '#WHICH'
          },
          initial: 'WHICH'
        }
      },
      initial: 'Global',
      on: {}
    });
  });

  it('Should correctly update state machine when droping a transition target on a state', () => {
    const dropEvent = {
      type: 'itemDropped',
      stateId: 'HENRI_IV_DE_FRANCE_OU_DANGLETERRE',
      dropped: {
        type: 'transitionTarget',
        source: 'WHICH',
        name: 'henriIvDeFrance'
      }
    };

    component.itemDropped(dropEvent);

    expect(component.scenario.data.stateMachine).toEqual({
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
                GRISE: {
                  id: 'GRISE'
                },
                ROUANNE: {
                  id: 'ROUANNE'
                }
              },
              on: {
                henriIvDeFrance: '#HENRI_IV_DE_FRANCE_OU_DANGLETERRE',
                henryIvDAngleterre: '#ROUANNE'
              },
              initial: 'HENRI_IV_DE_FRANCE_OU_DANGLETERRE'
            }
          },
          on: {
            quelleEstLaCouleurDuChevalBlancDHenriIv: '#WHICH'
          },
          initial: 'WHICH'
        }
      },
      initial: 'Global',
      on: {}
    });
  });

  it('Should correctly update state machine when droping a transition source on a state', () => {
    const dropEvent = {
      type: 'itemDropped',
      stateId: 'Global',
      dropped: {
        type: 'transitionSource',
        source: 'WHICH',
        name: 'henryIvDAngleterre'
      }
    };

    component.itemDropped(dropEvent);

    expect(component.scenario.data.stateMachine).toEqual({
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
                GRISE: {
                  id: 'GRISE'
                },
                ROUANNE: {
                  id: 'ROUANNE'
                }
              },
              on: {
                henriIvDeFrance: '#GRISE'
              },
              initial: 'HENRI_IV_DE_FRANCE_OU_DANGLETERRE'
            }
          },
          on: {
            quelleEstLaCouleurDuChevalBlancDHenriIv: '#WHICH',
            henryIvDAngleterre: '#ROUANNE'
          },
          initial: 'WHICH'
        }
      },
      initial: 'Global',
      on: {}
    });
  });

  it('Should not add a group with an already existing name', () => {
    const initialState = deepCopy(component.scenario.data.stateMachine);
    const addGroupEvent = {
      type: 'addStateGroup',
      stateId: 'WHICH',
      groupName: 'ROUANNE'
    };

    component['addStateGroup'](addGroupEvent);

    expect(component.scenario.data.stateMachine).toEqual(initialState);
  });

  it('Should correctly update state machine when adding a state group', () => {
    const addGroupEvent = {
      type: 'addStateGroup',
      stateId: 'WHICH',
      groupName: 'TESTGROUP'
    };

    component['addStateGroup'](addGroupEvent);

    expect(component.scenario.data.stateMachine).toEqual({
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
                GRISE: {
                  id: 'GRISE'
                },
                ROUANNE: {
                  id: 'ROUANNE'
                },
                TESTGROUP: {
                  id: 'TESTGROUP',
                  states: {},
                  on: {}
                }
              },
              on: {
                henriIvDeFrance: '#GRISE',
                henryIvDAngleterre: '#ROUANNE'
              },
              initial: 'HENRI_IV_DE_FRANCE_OU_DANGLETERRE'
            }
          },
          on: {
            quelleEstLaCouleurDuChevalBlancDHenriIv: '#WHICH'
          },
          initial: 'WHICH'
        }
      },
      initial: 'Global',
      on: {}
    });
  });
});

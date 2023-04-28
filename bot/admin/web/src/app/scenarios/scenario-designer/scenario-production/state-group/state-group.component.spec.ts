import { ChangeDetectorRef, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbDialogService } from '@nebular/theme';
import { Subject } from 'rxjs';

import { ScenarioStateGroupComponent } from './state-group.component';
import { ScenarioProductionService } from '../scenario-production.service';
import { MachineState } from '../../../models';
import { StateService } from '../../../../core-nlp/state.service';
import { TestingModule } from '../../../../../testing';

const machineState: MachineState = {
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
              id: 'ROUANNE',
              states: { TEST: { id: 'TEST' } }
            },
            EMPTYSTATE: {
              id: 'ROUANNE',
              states: {}
            }
          },
          on: {
            henriIvDeFrance: '#GRISE',
            henryIvDAngleterre: '#ROUANNE'
          },
          initial: 'HENRI_IV_DE_FRANCE_OU_DANGLETERRE'
        },
        TEST: { id: 'TEST' }
      },
      on: {
        quelleEstLaCouleurDuChevalBlancDHenriIv: '#WHICH'
      },
      initial: 'WHICH'
    }
  },
  initial: 'Global',
  on: {}
};

const actions = [
  {
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
  },
  {
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
  },
  {
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
];

describe('ScenarioStateGroupComponent', () => {
  let component: ScenarioStateGroupComponent;
  let fixture: ComponentFixture<ScenarioStateGroupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScenarioStateGroupComponent],
      imports: [TestingModule],
      providers: [
        {
          provide: ScenarioProductionService,
          useValue: {
            scenarioProductionItemsCommunication: new Subject<any>(),
            registerStateComponent: () => {},
            unRegisterStateComponent: () => {}
          }
        },
        { provide: NbDialogService, useValue: {} },
        { provide: ChangeDetectorRef, useValue: {} },
        { provide: StateService, useValue: {} }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScenarioStateGroupComponent);
    component = fixture.componentInstance;
    component.stateMachine = machineState;
    component.state = machineState.states.Global;
    component.actions = actions;
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

  describe('Should accept draggables types depending on state nature', () => {
    it('If state is Global', () => {
      const draggableTypes = component.getDraggableTypes();
      expect(draggableTypes).toEqual(['action']);
    });
    it('If parent state is Global and state is a group', () => {
      component.state = machineState.states.Global.states.WHICH;
      const draggableTypes = component.getDraggableTypes();
      expect(draggableTypes).toEqual(['primaryIntent', 'action']);
    });
    it('If parent state is Global and state is not a group', () => {
      component.state = machineState.states.Global.states.TEST;
      const draggableTypes = component.getDraggableTypes();
      expect(draggableTypes).toEqual(['primaryIntent']);
    });
    it('If parent state is not Global and state is a group', () => {
      component.state = machineState.states.Global.states.WHICH.states.ROUANNE;
      const draggableTypes = component.getDraggableTypes();
      expect(draggableTypes).toEqual(['intent', 'action']);
    });
    it('If parent state is not Global and state is not a group', () => {
      component.state = machineState.states.Global.states.WHICH.states.GRISE;
      const draggableTypes = component.getDraggableTypes();
      expect(draggableTypes).toEqual(['intent']);
    });
  });

  describe('Should alert if state is not correctly structured', () => {
    it('If state is a group but have no child state', () => {
      component.state = machineState.states.Global.states.WHICH.states.EMPTYSTATE;
      const error = component.getNextActionError();
      expect(error).toEqual('An action group cannot be empty');
    });
    it("If state is a group but doesn't have an initial child state", () => {
      component.state = machineState.states.Global.states.WHICH.states.ROUANNE;
      const error = component.getNextActionError();
      expect(error).toEqual('This action group lacks an initial state');
    });
  });
});

import { NO_ERRORS_SCHEMA } from '@angular/core';
import {
  async,
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
  waitForAsync
} from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { NbDialogRef } from '@nebular/theme';
import { StateService } from '../../../../core-nlp/state.service';
import { Scenario, ScenarioItemFrom, SCENARIO_MODE, SCENARIO_STATE } from '../../../models';
import { ActionEditComponent } from './action-edit.component';

const scenarioMock = {
  id: '62fcbb7ae4d25c16a44071a1',
  name: 'testing scenario',
  category: 'scenarios',
  tags: ['testing'],
  applicationId: '62558f21b318632c9200b567',
  createDate: '2022-08-17T09:57:14.428Z',
  updateDate: '2022-08-17T09:57:33.053Z',
  description: '',
  data: {
    mode: 'writing' as SCENARIO_MODE,
    scenarioItems: [
      {
        id: 0,
        from: 'client' as ScenarioItemFrom,
        text: 'Main intent',
        main: true,
        intentDefinition: { name: 'intent1', label: 'intent1', primary: true }
      },
      {
        id: 1,
        text: 'action1',
        from: 'bot' as ScenarioItemFrom,
        parentIds: [0],
        tickActionDefinition: {
          name: 'action1',
          inputContextNames: [],
          outputContextNames: ['test']
        }
      },
      {
        id: 2,
        text: 'action2',
        from: 'bot' as ScenarioItemFrom,
        parentIds: [0, 1],
        tickActionDefinition: {
          name: 'action2',
          inputContextNames: ['test'],
          outputContextNames: [],
          answerId: '456',
          answer: 'action2 answer'
        }
      },
      {
        id: 3,
        from: 'client' as ScenarioItemFrom,
        text: 'Second intent',
        intentDefinition: { name: 'intent2', label: 'intent2', intentId: '123' }
      },
      {
        id: 4,
        text: 'action3',
        from: 'bot' as ScenarioItemFrom,
        parentIds: [3]
      },
      {
        id: 5,
        from: 'client' as ScenarioItemFrom,
        text: 'Third intent'
      }
    ],
    contexts: [{ name: 'test', type: 'string' }],
    stateMachine: {
      id: 'root',
      type: 'parallel',
      states: {
        Global: {
          id: 'Global',
          states: {
            action1: { id: 'action1' },
            action2: { id: 'action2' }
          },
          on: { intent1: '#action1' }
        }
      },
      initial: 'Global',
      on: {}
    }
  },
  state: 'draft' as SCENARIO_STATE
};

function getScenarioMock() {
  return JSON.parse(JSON.stringify(scenarioMock)) as Scenario;
}

describe('ActionEditComponent', () => {
  let component: ActionEditComponent;
  let fixture: ComponentFixture<ActionEditComponent>;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ActionEditComponent],
      providers: [
        {
          provide: NbDialogRef,
          useValue: {}
        },
        { provide: StateService, useValue: {} }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ActionEditComponent);
    component = fixture.componentInstance;
    component.scenario = getScenarioMock();
    component.item = component.scenario.data.scenarioItems[2];
    component.contexts = component.scenario.data.contexts;
    fixture.detectChanges();
  });

  it('Should create', () => {
    expect(component).toBeTruthy();
    expect(component.form.value).toEqual({
      name: 'action2',
      description: null,
      handler: null,
      answer: 'action2 answer',
      answerId: '456',
      inputContextNames: ['test'],
      outputContextNames: [],
      final: false
    });
  });

  it('Should destroy', () => {
    expect(component.ngOnDestroy).toBeDefined();
    expect(component.destroy.isStopped).toBeFalsy();
    component.ngOnDestroy();
    expect(component.destroy.isStopped).toBeTruthy();
  });

  it('Should not accept existing action name as name', () => {
    let modifItem = getScenarioMock().data.scenarioItems[2];
    modifItem.tickActionDefinition.name = 'action1';
    component.item = modifItem;
    component.ngOnInit();
    expect(component.name.valid).not.toBeTruthy();
  });

  it('Should format name in snake case', () => {
    let nameinput = fixture.debugElement.query(By.css('[data-testid="name"]'));
    nameinput.nativeElement.dispatchEvent(new Event('blur'));
    fixture.detectChanges();
    expect(component.name.value).toEqual('ACTION2');
  });
});

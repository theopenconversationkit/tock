import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { NbDialogRef } from '@nebular/theme';
import { UserInterfaceType } from '../../../../core/model/configuration';
import { StateService } from '../../../../core-nlp/state.service';
import { ScenarioVersion, SCENARIO_MODE, SCENARIO_STATE, ScenarioContext } from '../../../models';
import { ActionEditComponent } from './action-edit.component';

const scenarioMock: ScenarioVersion = {
  id: '62fcbb7ae4d25c16a44071a1',
  creationDate: '2022-08-17T09:57:14.428Z',
  updateDate: '2022-08-17T09:57:33.053Z',
  data: {
    mode: 'writing' as SCENARIO_MODE,
    scenarioItems: [
      {
        id: 0,
        from: 'client',
        text: 'Main intent',
        main: true,
        intentDefinition: { name: 'intent1', label: 'intent1', primary: true }
      },
      {
        id: 1,
        text: 'action1',
        from: 'bot',
        parentIds: [0],
        actionDefinition: {
          name: 'ACTION1',
          inputContextNames: [],
          outputContextNames: ['TEST'],
          answers: []
        }
      },
      {
        id: 2,
        text: 'action2',
        from: 'bot',
        parentIds: [0, 1],
        actionDefinition: {
          name: 'ACTION2',
          inputContextNames: ['TEST'],
          outputContextNames: ['CONTEXT2'],
          answerId: '456',
          answers: [{ locale: 'fr', interfaceType: UserInterfaceType.textChat, answer: 'action2 answer' }]
        }
      },
      {
        id: 3,
        from: 'client',
        text: 'Second intent',
        intentDefinition: { name: 'intent2', label: 'intent2', intentId: '123' }
      },
      {
        id: 4,
        text: 'action3',
        from: 'bot',
        parentIds: [3]
      },
      {
        id: 5,
        from: 'client',
        text: 'Third intent'
      }
    ],
    contexts: [
      { name: 'TEST', type: 'string', entityType: 'hello', entityRole: 'world' },
      { name: 'CONTEXT2', type: 'string' as const },
      { name: 'CONTEXT3', type: 'string' as const },
      { name: 'CONTEXT4', type: 'string' as const }
    ],
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
  return JSON.parse(JSON.stringify(scenarioMock)) as ScenarioVersion;
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
        {
          provide: StateService,
          useValue: {
            user: {
              organization: 'org'
            },
            currentApplication: {
              supportedLocales: ['fr']
            },
            localeName: () => {
              return 'french';
            },
            currentLocale: 'fr',
            supportedLocales: ['fr']
          }
        }
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
      name: 'ACTION2',
      description: null,
      handler: null,
      trigger: null,
      answers: [{ locale: 'fr', interfaceType: UserInterfaceType.textChat, answer: 'action2 answer' }],
      answerId: '456',
      inputContextNames: ['TEST'],
      outputContextNames: ['CONTEXT2'],
      final: false,
      targetStory: null,
      unknownAnswers: [{ locale: 'fr', interfaceType: UserInterfaceType.textChat }]
    });
  });

  it('Should not accept existing action name as name', () => {
    let modifItem = getScenarioMock().data.scenarioItems[2];
    modifItem.actionDefinition.name = 'action1';
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

  it('Should list contexts for autocomplete', () => {
    component.updateContextsAutocompleteValues();
    component.contextsAutocompleteValues.subscribe((result) => expect(result).toEqual(['CONTEXT3', 'CONTEXT4']));
  });

  it('Should list contexts for autocomplete matching typed string', () => {
    component.updateContextsAutocompleteValues({
      target: { value: '3' }
    } as unknown as KeyboardEvent);
    component.contextsAutocompleteValues.subscribe((result) => expect(result).toEqual(['CONTEXT3']));
  });

  it('Should add context', () => {
    component.inputContextsInput.nativeElement.value = 'test add context';
    component.addContext('input');
    expect(component.inputContextNames.value).toEqual(['TEST', 'TEST_ADD_CONTEXT'], 'Should add context');
    expect(component.form.dirty).toBeTruthy('Should make form dirty');
  });

  it('Should not add context if string is too short', () => {
    component.inputContextsInput.nativeElement.value = 'abc';
    component.addContext('input');
    expect(component.inputContextNames.value).toEqual(['TEST']);
  });

  it('Should not add context if context already associated with action input contexts', () => {
    component.inputContextsInput.nativeElement.value = 'test';
    component.addContext('input');
    expect(component.inputContextNames.value).toEqual(['TEST']);
  });

  it('Should not add context if context already associated with action output contexts', () => {
    component.inputContextsInput.nativeElement.value = 'context2';
    component.addContext('input');
    expect(component.inputContextNames.value).toEqual(['TEST']);
  });

  it('Should remove context', () => {
    component.removeContext('input', 'TEST');
    expect(component.inputContextNames.value).toEqual([]);
    expect(component.form.dirty).toBeTruthy('Should make form dirty');
  });

  it('Should return context color', () => {
    let color = component.getContextEntityColor(scenarioMock.data.contexts[0] as ScenarioContext);
    expect(color).toEqual('#ff87ef');
  });

  it('Should return context contrast', () => {
    let color = component.getContextEntityContrast(scenarioMock.data.contexts[0] as ScenarioContext);
    expect(color).toEqual('black');
  });

  it('Should hide output contexts and unknown answers if target story is set', () => {
    Object.defineProperty(component, 'availableStories', { value: [] });
    let modifItem = getScenarioMock().data.scenarioItems[2];
    modifItem.actionDefinition.targetStory = 'targetStoryTestId';
    component.item = modifItem;
    component.ngOnInit();
    fixture.detectChanges();

    expect(fixture.debugElement.query(By.css('[data-testid="output-context-block"]'))).toBeNull();
    expect(fixture.debugElement.query(By.css('[data-testid="unknown-answers-block"]'))).toBeNull();

    modifItem.actionDefinition.targetStory = null;
    component.item = modifItem;
    component.ngOnInit();
    fixture.detectChanges();

    expect(fixture.debugElement.query(By.css('[data-testid="output-context-block"]'))).not.toBeNull();
    expect(fixture.debugElement.query(By.css('[data-testid="unknown-answers-block"]'))).not.toBeNull();
  });
});

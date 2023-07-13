import { ScenarioConceptionComponent } from './scenario-conception.component';
import { ScenarioConceptionService } from './scenario-conception-service.service';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import {
  ScenarioVersion,
  ScenarioItem,
  ScenarioItemFrom,
  SCENARIO_ITEM_FROM_BOT,
  SCENARIO_ITEM_FROM_CLIENT,
  SCENARIO_MODE,
  SCENARIO_STATE
} from '../../models';
import { NbDialogService } from '@nebular/theme';

import { StateService } from '../../../core-nlp/state.service';

const scenarioMock = {
  id: '62fcbb7ae4d25c16a44071a1',
  name: 'testing scenario',
  category: 'scenarios',
  tags: ['testing'],
  applicationId: '62558f21b318632c9200b567',
  creationDate: '2022-08-17T09:57:14.428Z',
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
        intentDefinition: { name: 'intent1', label: 'intent1' }
      },
      {
        id: 1,
        text: 'action1',
        from: 'bot',
        parentIds: [0],
        actionDefinition: {
          name: 'action1',
          inputContextNames: [],
          outputContextNames: ['test']
        }
      },
      {
        id: 2,
        text: 'action2',
        from: 'bot',
        parentIds: [0, 1],
        actionDefinition: {
          name: 'action2',
          inputContextNames: ['test'],
          outputContextNames: []
        }
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
          }
        }
      },
      initial: 'Global',
      on: { intent1: '#Global' }
    }
  },
  state: 'draft' as SCENARIO_STATE
};

function getScenarioMock() {
  return JSON.parse(JSON.stringify(scenarioMock)) as ScenarioVersion;
}

describe('ScenarioConceptionComponent', () => {
  let component: ScenarioConceptionComponent;
  let fixture: ComponentFixture<ScenarioConceptionComponent>;
  let scenarioConceptionService: ScenarioConceptionService;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScenarioConceptionComponent],
      providers: [ScenarioConceptionService, { provide: NbDialogService, useValue: {} }, { provide: StateService, useValue: {} }],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    scenarioConceptionService = TestBed.inject(ScenarioConceptionService);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScenarioConceptionComponent);
    component = fixture.componentInstance;
    component.scenario = getScenarioMock();
    fixture.detectChanges();
  });

  it('Should create', () => {
    expect(component).toBeTruthy();
  });

  it('Should destroy', () => {
    expect(component.ngOnDestroy).toBeDefined();
    expect(component.destroy.isStopped).toBeFalsy();
    component.ngOnDestroy();
    expect(component.destroy.isStopped).toBeTruthy();
  });

  describe('Should respond to scenarioDesignerItemsCommunication events', () => {
    const item = { id: 1, from: SCENARIO_ITEM_FROM_BOT, text: 'Bot response1' } as ScenarioItem;
    const position = { offsetLeft: 1, offsetTop: 1, offsetWidth: 10, offsetHeight: 10 };

    [
      {
        description: 'addItem',
        method: 'addItem',
        servicemethod: 'addAnswer',
        parameters: [item]
      },
      {
        description: 'deleteAnswer',
        method: 'deleteItem',
        servicemethod: 'deleteAnswer',
        parameters: [item, 0]
      },
      {
        description: 'itemDropped',
        method: 'itemDropped',
        servicemethod: 'itemDropped',
        parameters: [0, 1]
      },
      {
        description: 'selectItem',
        method: 'selectItem',
        servicemethod: 'selectItem',
        parameters: [item]
      },
      {
        description: 'testStory',
        method: 'testStory',
        servicemethod: 'testItem',
        parameters: [item]
      },
      {
        description: 'centerOnItem',
        method: 'centerOnItem',
        servicemethod: 'exposeItemPosition',
        parameters: [item, position]
      },
      {
        description: 'changeItemType',
        method: 'changeItemType',
        servicemethod: 'changeItemType',
        parameters: [item, SCENARIO_ITEM_FROM_BOT]
      },
      {
        description: 'removeItemDefinition',
        method: 'removeItemDefinition',
        servicemethod: 'removeItemDefinition',
        parameters: [item]
      }
    ].forEach((test) => {
      it(test.description, () => {
        spyOn(component, test.method as any);
        component['scenarioConceptionService'][test.servicemethod](...test.parameters);
        expect(component[test.method]).toHaveBeenCalledWith(...test.parameters);
      });
    });
  });

  it('Should clean actions when deleting context', () => {
    component['deleteContext'](component.scenario.data.contexts[0]);
    const expected = getScenarioMock().data;
    expected.contexts = [];
    expected.scenarioItems[1].actionDefinition.inputContextNames = [];
    expected.scenarioItems[1].actionDefinition.outputContextNames = [];
    expected.scenarioItems[2].actionDefinition.inputContextNames = [];
    expected.scenarioItems[2].actionDefinition.outputContextNames = [];
    expect(component.scenario.data).toEqual(expected);
  });

  it('Should detect if a context is fully used', () => {
    expect(component.isContextUsed(component.scenario.data.contexts[0])).toBeTruthy();

    component.scenario.data.scenarioItems[1].actionDefinition.inputContextNames = [];
    component.scenario.data.scenarioItems[1].actionDefinition.outputContextNames = [];
    expect(component.isContextUsed(component.scenario.data.contexts[0])).toBeFalsy();
  });

  it('Should add an item to a scenario, select it and require its position', fakeAsync(() => {
    spyOn(component['scenarioConceptionService'], 'requireItemPosition');

    component['addItem'](component.scenario.data.scenarioItems[2]);
    fixture.detectChanges();

    const expected = getScenarioMock();
    const newItem = {
      id: 3,
      parentIds: [2],
      from: 'client',
      text: ''
    } as ScenarioItem;

    expected.data.scenarioItems.push(newItem);
    expect(component.scenario).toEqual(expected);

    tick(100);

    expect(component.selectedItem.id).toEqual(3);

    expect(component['scenarioConceptionService'].requireItemPosition).toHaveBeenCalledWith(newItem);
  }));

  it('Should delete an item and its references', () => {
    component['deleteItem'](component.scenario.data.scenarioItems[2], 0);
    expect(component.scenario.data.scenarioItems[2].parentIds).toEqual([1]);

    component['deleteItem'](component.scenario.data.scenarioItems[2], 1);
    expect(component.scenario.data.scenarioItems[2]).toBeUndefined();
    expect(component.scenario.data.stateMachine.states.Global.states['action2']).toBeUndefined();
  });

  it('Should change the type of an item and remove its previous definition', () => {
    component['changeItemType'](component.scenario.data.scenarioItems[0], SCENARIO_ITEM_FROM_BOT);
    expect(component.scenario.data.scenarioItems[0].from).toEqual('bot');
    expect(component.scenario.data.scenarioItems[0].intentDefinition).toBeUndefined();

    component['changeItemType'](component.scenario.data.scenarioItems[2], SCENARIO_ITEM_FROM_CLIENT);
    expect(component.scenario.data.scenarioItems[2].from).toEqual('client');
    expect(component.scenario.data.scenarioItems[2].actionDefinition).toBeUndefined();
    expect(component.scenario.data.stateMachine.states.Global.states['action2']).toBeUndefined();
  });

  it('Should find item first child', () => {
    expect(component['findItemChild'](component.scenario.data.scenarioItems[0])).toEqual(component.scenario.data.scenarioItems[1]);
  });

  it('Should find an item by id', () => {
    expect(component['findItemById'](2)).toEqual(component.scenario.data.scenarioItems[2]);
  });

  it('Should find item children', () => {
    expect(component['getChildren'](component.scenario.data.scenarioItems[0])).toEqual([
      component.scenario.data.scenarioItems[1],
      component.scenario.data.scenarioItems[2]
    ]);
  });

  it('Should find item brotherhood', () => {
    expect(component['getBrotherhood'](component.scenario.data.scenarioItems[1])).toEqual([
      component.scenario.data.scenarioItems[1],
      component.scenario.data.scenarioItems[2]
    ]);
  });
});

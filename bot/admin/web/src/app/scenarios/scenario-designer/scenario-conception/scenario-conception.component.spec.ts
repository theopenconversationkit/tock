import { ScenarioConceptionComponent } from './scenario-conception.component';
import { ScenarioConceptionService } from './scenario-conception-service.service';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { DialogService } from '../../../core-nlp/dialog.service';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { StateService } from '../../../core-nlp/state.service';
import {
  Scenario,
  ScenarioItem,
  ScenarioItemFrom,
  SCENARIO_ITEM_FROM_BOT,
  SCENARIO_ITEM_FROM_CLIENT,
  SCENARIO_MODE,
  SCENARIO_STATE
} from '../../models';

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
        intentDefinition: { name: 'intent1', label: 'intent1' }
      },
      {
        id: 1,
        text: 'action1',
        from: 'bot',
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
        from: 'bot',
        parentIds: [0, 1],
        tickActionDefinition: {
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
  return JSON.parse(JSON.stringify(scenarioMock)) as Scenario;
}

describe('ScenarioConceptionComponent', () => {
  let component: ScenarioConceptionComponent;
  let fixture: ComponentFixture<ScenarioConceptionComponent>;
  let scenarioConceptionService: ScenarioConceptionService;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScenarioConceptionComponent],
      providers: [
        ScenarioConceptionService,
        { provide: DialogService, useValue: {} },
        { provide: StateService, useValue: {} }
      ],
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

  it('Should respond to scenarioDesignerItemsCommunication events', () => {
    const item = { id: 1, from: SCENARIO_ITEM_FROM_BOT, text: 'Bot response1' } as ScenarioItem;

    spyOn(component, 'addItem');
    component['scenarioConceptionService'].addAnswer(item);
    expect(component.addItem).toHaveBeenCalledWith(item);

    spyOn(component, 'deleteItem');
    component['scenarioConceptionService'].deleteAnswer(item, 0);
    expect(component.deleteItem).toHaveBeenCalledWith(item, 0);

    spyOn(component, 'itemDropped');
    component['scenarioConceptionService'].itemDropped(0, 1);
    expect(component.itemDropped).toHaveBeenCalledWith(0, 1);

    spyOn(component, 'selectItem');
    component['scenarioConceptionService'].selectItem(item);
    expect(component.selectItem).toHaveBeenCalledWith(item);

    spyOn(component, 'testStory');
    component['scenarioConceptionService'].testItem(item);
    expect(component.testStory).toHaveBeenCalledWith(item);

    const position = { offsetLeft: 1, offsetTop: 1, offsetWidth: 10, offsetHeight: 10 };
    spyOn(component, 'centerOnItem');
    component['scenarioConceptionService'].exposeItemPosition(item, position);
    expect(component.centerOnItem).toHaveBeenCalledWith(item, position);

    spyOn(component, 'changeItemType');
    component['scenarioConceptionService'].changeItemType(item, SCENARIO_ITEM_FROM_BOT);
    expect(component.changeItemType).toHaveBeenCalledWith(item, SCENARIO_ITEM_FROM_BOT);

    spyOn(component, 'removeItemDefinition');
    component['scenarioConceptionService'].removeItemDefinition(item);
    expect(component.removeItemDefinition).toHaveBeenCalledWith(item);
  });

  it('Should clean actions when deleting context', () => {
    component.deleteContext(component.scenario.data.contexts[0]);
    const expected = getScenarioMock().data;
    expected.contexts = [];
    expected.scenarioItems[1].tickActionDefinition.inputContextNames = [];
    expected.scenarioItems[1].tickActionDefinition.outputContextNames = [];
    expected.scenarioItems[2].tickActionDefinition.inputContextNames = [];
    expected.scenarioItems[2].tickActionDefinition.outputContextNames = [];
    expect(component.scenario.data).toEqual(expected);
  });

  it('Should detect if a context is fully used', () => {
    expect(component.isContextUsed(component.scenario.data.contexts[0])).toBeTruthy();

    component.scenario.data.scenarioItems[1].tickActionDefinition.inputContextNames = [];
    component.scenario.data.scenarioItems[1].tickActionDefinition.outputContextNames = [];
    expect(component.isContextUsed(component.scenario.data.contexts[0])).toBeFalsy();
  });

  it('Should add an item to a scenario, select it and require its position', fakeAsync(() => {
    spyOn(component['scenarioConceptionService'], 'requireItemPosition');

    component.addItem(component.scenario.data.scenarioItems[2]);
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

    expect(component['scenarioConceptionService'].requireItemPosition).toHaveBeenCalledWith(
      newItem
    );
  }));

  it('Should delete an item and its references', () => {
    component.deleteItem(component.scenario.data.scenarioItems[2], 0);
    expect(component.scenario.data.scenarioItems[2].parentIds).toEqual([1]);

    component.deleteItem(component.scenario.data.scenarioItems[2], 1);
    expect(component.scenario.data.scenarioItems[2]).toBeUndefined();
    expect(component.scenario.data.stateMachine.states.Global.states['action2']).toBeUndefined();
  });

  it('Should change the type of an item and remove its previous definition', () => {
    component.changeItemType(component.scenario.data.scenarioItems[0], SCENARIO_ITEM_FROM_BOT);
    expect(component.scenario.data.scenarioItems[0].from).toEqual('bot');
    expect(component.scenario.data.scenarioItems[0].intentDefinition).toBeUndefined();

    component.changeItemType(component.scenario.data.scenarioItems[2], SCENARIO_ITEM_FROM_CLIENT);
    expect(component.scenario.data.scenarioItems[2].from).toEqual('client');
    expect(component.scenario.data.scenarioItems[2].tickActionDefinition).toBeUndefined();
    expect(component.scenario.data.stateMachine.states.Global.states['action2']).toBeUndefined();
  });

  it('Should find item first child', () => {
    expect(component.findItemChild(component.scenario.data.scenarioItems[0])).toEqual(
      component.scenario.data.scenarioItems[1]
    );
  });

  it('Should find an item by id', () => {
    expect(component.findItemById(2)).toEqual(component.scenario.data.scenarioItems[2]);
  });

  it('Should find item children', () => {
    expect(component.getChildren(component.scenario.data.scenarioItems[0])).toEqual([
      component.scenario.data.scenarioItems[1],
      component.scenario.data.scenarioItems[2]
    ]);
  });

  it('Should find item brotherhood', () => {
    expect(component.getBrotherhood(component.scenario.data.scenarioItems[1])).toEqual([
      component.scenario.data.scenarioItems[1],
      component.scenario.data.scenarioItems[2]
    ]);
  });

  it('Should find item brothers', () => {
    expect(component.getItemBrothers(component.scenario.data.scenarioItems[1])).toEqual([
      component.scenario.data.scenarioItems[2]
    ]);
  });

  it('Should detect if an item has no brothers', () => {
    expect(component.isItemOnlyChild(component.scenario.data.scenarioItems[0])).toBeTruthy();
    expect(component.isItemOnlyChild(component.scenario.data.scenarioItems[1])).toBeFalsy();
  });
});

import { By } from '@angular/platform-browser';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { EMPTY, of } from 'rxjs';
import { NbDialogRef, NbDialogService } from '@nebular/theme';

import {
  ScenarioIntentDefinition,
  ScenarioVersion,
  ScenarioItemFrom,
  SCENARIO_ITEM_FROM_CLIENT,
  SCENARIO_MODE,
  SCENARIO_STATE,
  TempSentence,
  ScenarioContext
} from '../../models';
import { ScenarioConceptionItemComponent } from './scenario-conception-item.component';
import { ScenarioConceptionService } from './scenario-conception-service.service';
import { ScenarioDesignerService } from '../scenario-designer.service';
import { StateService } from '../../../core-nlp/state.service';
import { NlpService } from '../../../nlp-tabs/nlp.service';
import { UserInterfaceType } from '../../../core/model/configuration';
import { Intent } from '../../../model/nlp';

const scenarioMock: ScenarioVersion = {
  id: '62fcbb7ae4d25c16a44071a1',
  creationDate: '2022-08-17T09:57:14.428Z',
  updateDate: '2022-08-17T09:57:33.053Z',
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
          outputContextNames: [],
          answerId: '456',
          answers: [{ locale: 'fr', interfaceType: UserInterfaceType.textChat, answer: 'Action2 answer' }]
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
  return JSON.parse(JSON.stringify(scenarioMock)) as ScenarioVersion;
}

describe('ScenarioConceptionItemComponent', () => {
  let component: ScenarioConceptionItemComponent;
  let fixture: ComponentFixture<ScenarioConceptionItemComponent>;
  let scenarioConceptionService: ScenarioConceptionService;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScenarioConceptionItemComponent],
      providers: [
        ScenarioConceptionService,
        {
          provide: NbDialogService,
          useValue: { open: () => ({ onClose: (val: any) => of(val) }) }
        },
        { provide: StateService, useValue: {} },
        { provide: NlpService, useValue: {} },
        { provide: ScenarioDesignerService, useValue: {} }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    scenarioConceptionService = TestBed.inject(ScenarioConceptionService);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScenarioConceptionItemComponent);
    component = fixture.componentInstance;
    component.scenario = getScenarioMock();
    component.mode = component.scenario.data.mode;
    component.item = getScenarioMock().data.scenarioItems[2];
    component.parentId = 0;
    component.contexts = [];
    component.selectedItem = getScenarioMock().data.scenarioItems[2];

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
    const item = scenarioMock.data.scenarioItems[2];

    spyOn(component, 'focusItem');
    component['scenarioConceptionService'].focusItem(item);
    expect(component.focusItem).toHaveBeenCalledWith(item);

    spyOn<any>(component, 'requireItemPosition');
    component['scenarioConceptionService'].requireItemPosition(item);
    expect(component['requireItemPosition']).toHaveBeenCalledWith(item);
  });

  it('Should set the current item on init', () => {
    const item = scenarioMock.data.scenarioItems[2];
    expect(component.item).toEqual(item);

    expect(component.draggable.data).toEqual(2);
  });

  it('Should handle correctly ActionEditComponent.saveModifications return', () => {
    const modifications = {
      name: 'RenamedAction',
      answerId: '456',
      answers: [{ locale: 'fr', interfaceType: UserInterfaceType.textChat, answer: 'Modified answer' }],
      inputContextNames: ['context1'],
      outputContextNames: ['context1', 'context2'],
      unknownAnswers: []
    };
    spyOn(component['nbDialogService'], 'open').and.returnValue({
      componentRef: { instance: { saveModifications: of(modifications), deleteDefinition: of() } },
      close: () => {}
    } as NbDialogRef<any>);

    component.manageAction();

    expect(component['nbDialogService'].open).toHaveBeenCalled();
    expect(component.contexts).toEqual([
      {
        name: 'context1',
        type: 'string'
      },
      {
        name: 'context2',
        type: 'string'
      }
    ]);

    expect(component.scenario.data.stateMachine.states.Global.states.RenamedAction).toBeTruthy();
    expect(component.item.actionDefinition.answers[0].answerUpdate).toBeTruthy();
  });

  it('Should handle correctly ActionEditComponent.deleteDefinition return', () => {
    spyOn(scenarioConceptionService, 'removeItemDefinition');
    spyOn(component['nbDialogService'], 'open').and.returnValue({
      close: () => {},
      componentRef: { instance: { saveModifications: EMPTY, deleteDefinition: of(true) } }
    } as NbDialogRef<any>);

    component.manageAction();
    fixture.detectChanges();
    expect(component['nbDialogService'].open).toHaveBeenCalled();
    expect(scenarioConceptionService.removeItemDefinition).toHaveBeenCalled();
  });

  it('Should handle correctly manageIntent call', () => {
    let editSpy = spyOn<any>(component, 'editIntent');
    let searchSpy = spyOn<any>(component, 'searchIntent');

    component.item = getScenarioMock().data.scenarioItems[0];
    component.ngOnInit();
    component.manageIntent();
    expect(component['editIntent']).toHaveBeenCalled();
    expect(component['searchIntent']).not.toHaveBeenCalled();

    editSpy.calls.reset();
    searchSpy.calls.reset();

    delete component.item.intentDefinition;

    component.manageIntent();
    expect(component['editIntent']).not.toHaveBeenCalled();
    expect(component['searchIntent']).toHaveBeenCalled();
  });

  it('Should handle correctly searchIntent => createNewIntentEvent call', () => {
    spyOn<any>(component, 'createIntent');
    spyOn(component['nbDialogService'], 'open').and.returnValue({
      close: () => {},
      componentRef: { instance: { useIntentEvent: EMPTY, createNewIntentEvent: of(true) } }
    } as NbDialogRef<any>);

    component['searchIntent']();

    expect(component['nbDialogService'].open).toHaveBeenCalled();
    expect(component['createIntent']).toHaveBeenCalled();
  });

  it('Should handle correctly searchIntent => useIntentEvent call', () => {
    const intentDef = {
      label: 'test',
      name: 'name',
      category: 'category',
      description: 'category',
      _id: 'id'
    } as Intent;
    spyOn<any>(component, 'setItemIntentDefinition');
    spyOn(component['nbDialogService'], 'open').and.returnValue({
      close: () => {},
      componentRef: { instance: { useIntentEvent: of(intentDef), createNewIntentEvent: EMPTY } }
    } as NbDialogRef<any>);

    component['searchIntent']();

    expect(component['nbDialogService'].open).toHaveBeenCalled();
    expect(component['setItemIntentDefinition']).toHaveBeenCalledWith(intentDef);
  });

  it('Should handle correctly createIntent => createIntentEvent call', () => {
    const intentDef = {
      label: 'test',
      name: 'name',
      category: 'category',
      description: 'category',
      primary: false
    } as ScenarioIntentDefinition;

    spyOn<any>(component, 'editIntent');
    spyOn(component['nbDialogService'], 'open').and.returnValue({
      close: () => {},
      componentRef: { instance: { createIntentEvent: of(intentDef) } }
    } as NbDialogRef<any>);

    component['createIntent']();

    expect(component['nbDialogService'].open).toHaveBeenCalled();
    expect(component.item.intentDefinition).toEqual(intentDef);
    expect(component['editIntent']).toHaveBeenCalled();
  });

  xit('Should handle correctly editIntent => saveModifications call', () => {
    const intentDef = {
      sentences: [
        {
          namespace: 'abc',
          applicationName: 'def',
          language: 'fr-fr',
          query: 'Test new sentence',
          checkExistingQuery: false,
          state: ''
        } as TempSentence
      ],
      contextsEntities: [
        {
          name: 'testContext',
          type: 'string'
        } as ScenarioContext
      ],
      primary: false
    };

    spyOn(component['nbDialogService'], 'open').and.returnValue({
      close: () => {},
      componentRef: { instance: { saveModifications: of(intentDef) } }
    } as NbDialogRef<any>);

    component.item = getScenarioMock().data.scenarioItems[0];
    component.ngOnInit();

    component['editIntent']();

    expect(component['nbDialogService'].open).toHaveBeenCalled();
    expect(component.item.intentDefinition.sentences).toEqual(intentDef.sentences);
    expect(component.item.intentDefinition.primary).toEqual(intentDef.primary);
    expect(component.scenario.data.stateMachine.states.Global.on).toEqual({});
    expect(component.contexts).toEqual(intentDef.contextsEntities);
  });

  describe('getChildItems | itemHasNoChildren', () => {
    it('Should return child items', () => {
      component.item = getScenarioMock().data.scenarioItems[0];
      component.ngOnInit();
      expect(component.getChildItems()).toEqual([component.scenario.data.scenarioItems[1], component.scenario.data.scenarioItems[2]]);

      expect(component.itemHasNoChildren()).toEqual(false);
    });
  });

  describe('delete', () => {
    it('Should confirm before deleting if item has definition', () => {
      spyOn(component['nbDialogService'], 'open').and.returnValue({
        close: () => {},
        onClose: of('delete')
      } as NbDialogRef<any>);

      component.delete();

      expect(component['nbDialogService'].open).toHaveBeenCalled();
    });

    it('Should not confirm before deleting if item has no definition', () => {
      spyOn(component['nbDialogService'], 'open').and.returnValue({
        close: () => {},
        onClose: of('delete')
      } as NbDialogRef<any>);
      delete component.item.actionDefinition;
      component.delete();
      expect(component['nbDialogService'].open).not.toHaveBeenCalled();
    });
  });

  it('Should set correct classes on item', () => {
    const classes = component.getItemCardCssClass();
    expect(classes).toEqual('cursor-default bot duplicate selected');
  });

  describe('switchItemType', () => {
    it('Should confirm before changing type of an item if it has definition', () => {
      spyOn(component['nbDialogService'], 'open').and.returnValue({
        close: () => {},
        onClose: of('delete')
      } as NbDialogRef<any>);

      component.switchItemType(SCENARIO_ITEM_FROM_CLIENT);

      expect(component['nbDialogService'].open).toHaveBeenCalled();
    });

    it('Should not confirm before changing type of an item if it has no definition', () => {
      spyOn(component['nbDialogService'], 'open').and.returnValue({
        close: () => {},
        onClose: of('delete')
      } as NbDialogRef<any>);
      delete component.item.actionDefinition;
      component.switchItemType(SCENARIO_ITEM_FROM_CLIENT);
      expect(component['nbDialogService'].open).not.toHaveBeenCalled();
    });
  });

  describe('getItemBrothers', () => {
    it('Should return the items brothers', () => {
      const brothers = component.getItemBrothers();
      expect(brothers).toEqual([component.scenario.data.scenarioItems[1]]);
    });
  });

  it('Should show item.text textarea if has no definition', () => {
    let textarea = fixture.debugElement.query(By.css('[data-testid="item-text-textarea"]'));
    expect(textarea).toBeNull();

    component.item = getScenarioMock().data.scenarioItems[4];
    component.ngOnInit();
    fixture.detectChanges();

    textarea = fixture.debugElement.query(By.css('[data-testid="item-text-textarea"]'));
    expect(textarea).not.toBeNull();
  });

  it('Should show intent item definition details if it has definition', () => {
    component.item = getScenarioMock().data.scenarioItems[0];
    component.ngOnInit();
    fixture.detectChanges();

    let details = fixture.debugElement.query(By.css('[data-testid="item-intent-definition-details"]'));
    expect(details).not.toBeNull();
    expect(details.nativeElement.textContent.trim()).toEqual(scenarioMock.data.scenarioItems[0].intentDefinition.name);
    let primary = fixture.debugElement.query(By.css('[data-testid="item-intent-definition-primary"]'));
    expect(primary).not.toBeNull();

    component.item = getScenarioMock().data.scenarioItems[5];
    component.ngOnInit();
    fixture.detectChanges();

    details = fixture.debugElement.query(By.css('[data-testid="item-intent-definition-details"]'));
    expect(details).toBeNull();

    primary = fixture.debugElement.query(By.css('[data-testid="item-intent-definition-primary"]'));
    expect(primary).toBeNull();
  });

  it('Should show action item definition details if it has definition', () => {
    component.item = getScenarioMock().data.scenarioItems[1];
    component.ngOnInit();
    fixture.detectChanges();

    let details = fixture.debugElement.query(By.css('[data-testid="item-action-definition-details"]'));
    expect(details).not.toBeNull();
    expect(details.nativeElement.textContent.trim()).toEqual(scenarioMock.data.scenarioItems[1].actionDefinition.name);

    component.item = getScenarioMock().data.scenarioItems[4];
    component.ngOnInit();
    fixture.detectChanges();

    details = fixture.debugElement.query(By.css('[data-testid="item-action-definition-details"]'));
    expect(details).toBeNull();
  });

  it('Should show add intervention buttons', () => {
    let addBtn = fixture.debugElement.query(By.css('[data-testid="item-answer"]'));
    expect(addBtn).not.toBeNull();
  });

  it('Should show define definitions buttons', () => {
    component.mode = SCENARIO_MODE.casting;
    fixture.detectChanges();

    let actionBtn = fixture.debugElement.query(By.css('[data-testid="item-define-action"]'));
    expect(actionBtn).not.toBeNull();

    component.item = getScenarioMock().data.scenarioItems[0];
    component.ngOnInit();
    fixture.detectChanges();

    let intentBtn = fixture.debugElement.query(By.css('[data-testid="item-define-intent"]'));
    expect(intentBtn).not.toBeNull();
  });

  it('Should show delete button', () => {
    let deleteBtn = fixture.debugElement.query(By.css('[data-testid="item-delete"]'));
    expect(deleteBtn).not.toBeNull();
  });

  it('Should not show delete button', () => {
    component.mode = SCENARIO_MODE.casting;
    fixture.detectChanges();

    let deleteBtn = fixture.debugElement.query(By.css('[data-testid="item-delete"]'));
    expect(deleteBtn).toBeNull();
  });
});

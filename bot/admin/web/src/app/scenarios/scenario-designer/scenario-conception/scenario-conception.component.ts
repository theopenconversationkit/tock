import { Component, ElementRef, EventEmitter, HostListener, Input, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import { NbDialogService } from '@nebular/theme';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { ScenarioConceptionService } from './scenario-conception-service.service';
import {
  ScenarioItem,
  ScenarioItemFrom,
  SCENARIO_ITEM_FROM_BOT,
  SCENARIO_ITEM_FROM_CLIENT,
  SCENARIO_MODE,
  ScenarioContext,
  ScenarioVersionExtended,
  Handler
} from '../../models';
import { StateService } from '../../../core-nlp/state.service';
import { entityColor, qualifiedName, qualifiedRole } from '../../../model/nlp';
import {
  getContrastYIQ,
  getScenarioActionDefinitions,
  getScenarioIntentDefinitions,
  getSmTransitionParentsByname,
  removeSmStateById
} from '../../commons/utils';
import { ContextCreateComponent } from './context-create/context-create.component';
import { ContextsGraphComponent } from '../contexts-graph/contexts-graph.component';
import { TriggerCreateComponent } from './trigger-create/trigger-create.component';
import { ChoiceDialogComponent } from '../../../shared/components';
import { OffsetPosition } from '../../../shared/canvas/models';
import { StoryDefinitionConfigurationSummary } from '../../../bot/model/story';

const CANVAS_TRANSITION_TIMING: number = 300;

@Component({
  selector: 'tock-scenario-conception',
  templateUrl: './scenario-conception.component.html',
  styleUrls: ['./scenario-conception.component.scss'],
  providers: [ScenarioConceptionService]
})
export class ScenarioConceptionComponent implements OnInit, OnDestroy {
  destroy = new Subject();
  @Input() scenario: ScenarioVersionExtended;
  @Input() isReadonly: boolean;
  @Input() isFullscreen: boolean = false;
  @Input() readonly avalaibleHandlers: Handler[];
  @Input() readonly availableStories: StoryDefinitionConfigurationSummary[];

  @Output() requestFullscreen = new EventEmitter();

  @ViewChild('canvasWrapperElem') canvasWrapperElem: ElementRef;
  @ViewChild('canvasElem') canvasElem: ElementRef;

  readonly SCENARIO_MODE = SCENARIO_MODE;
  readonly SCENARIO_ITEM_FROM_CLIENT = SCENARIO_ITEM_FROM_CLIENT;
  readonly SCENARIO_ITEM_FROM_BOT = SCENARIO_ITEM_FROM_BOT;

  private qualifiedName = qualifiedName;

  constructor(
    private scenarioConceptionService: ScenarioConceptionService,
    private stateService: StateService,
    private nbDialogService: NbDialogService
  ) {}

  ngOnInit(): void {
    this.scenarioConceptionService.scenarioDesignerItemsCommunication.pipe(takeUntil(this.destroy)).subscribe((evt) => {
      if (evt.type === 'addAnswer') this.addItem(evt.item);
      if (evt.type === 'deleteAnswer') this.deleteItem(evt.item, evt.parentItemId);
      if (evt.type === 'itemDropped') this.itemDropped(evt.targetId, evt.droppedId);
      if (evt.type === 'itemSelected') this.selectItem(evt.item);
      if (evt.type === 'testItem') this.testStory(evt.item);
      if (evt.type === 'exposeItemPosition') this.centerOnItem(evt.item, evt.position);
      if (evt.type === 'changeItemType') this.changeItemType(evt.item, evt.targetType);
      if (evt.type === 'removeItemDefinition') this.removeItemDefinition(evt.item);
    });
  }

  isSidePanelOpen: boolean = true;

  getContextEntityColor(context: ScenarioContext): string {
    if (context.entityType) return entityColor(qualifiedRole(context.entityType, context.entityRole));
  }

  getContextEntityContrast(context: ScenarioContext): string {
    if (context.entityType) return getContrastYIQ(entityColor(qualifiedRole(context.entityType, context.entityRole)));
  }

  addTrigger(): void {
    const modal = this.nbDialogService.open(TriggerCreateComponent, {
      context: {
        scenarioVersion: this.scenario
      }
    });

    const validate = modal.componentRef.instance.validate.pipe(takeUntil(this.destroy)).subscribe((trigger: string) => {
      this.scenario.data.triggers.push(trigger);

      validate.unsubscribe();
      modal.close();
    });
  }

  confirmDeleteTrigger(trigger: string): void {
    const deleteAction = 'delete';
    const modal = this.nbDialogService.open(ChoiceDialogComponent, {
      context: {
        title: `Delete event`,
        subtitle: 'Are you sure you want to delete this event?',
        modalStatus: 'danger',
        actions: [
          { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
          { actionName: deleteAction, buttonStatus: 'danger' }
        ]
      }
    });
    modal.onClose.subscribe((res) => {
      if (res == deleteAction) {
        this.deleteTrigger(trigger);
      }
    });
  }

  deleteTrigger(trigger: string): void {
    this.scenario.data.scenarioItems.forEach((item) => {
      if (item.from == SCENARIO_ITEM_FROM_BOT && item.actionDefinition && item.actionDefinition.trigger === trigger) {
        item.actionDefinition.trigger = null;
      }
    });

    this.removeTriggerTransitionInStateMachine(trigger);
    this.scenario.data.triggers = this.scenario.data.triggers.filter((tgg) => tgg !== trigger);
  }

  private removeTriggerTransitionInStateMachine(trigger: string): void {
    if (this.scenario.data.stateMachine) {
      const transitionsParents = getSmTransitionParentsByname(trigger, this.scenario.data.stateMachine);
      transitionsParents.forEach((parent) => {
        delete parent.on[trigger];
      });
    }
  }

  addContext(): void {
    const modal = this.nbDialogService.open(ContextCreateComponent, {
      context: {
        scenario: this.scenario
      }
    });
    const validate = modal.componentRef.instance.validate.pipe(takeUntil(this.destroy)).subscribe((contextDef) => {
      this.scenario.data.contexts.push({
        name: contextDef.name,
        type: 'string'
      });

      validate.unsubscribe();
      modal.close();
    });
  }

  confirmDeleteContext(context: ScenarioContext): void {
    const deleteAction = 'delete';
    const modal = this.nbDialogService.open(ChoiceDialogComponent, {
      context: {
        title: `Delete context`,
        subtitle: 'Are you sure you want to delete this context?',
        modalStatus: 'danger',
        actions: [
          { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
          { actionName: deleteAction, buttonStatus: 'danger' }
        ]
      }
    });
    modal.onClose.subscribe((res) => {
      if (res == deleteAction) {
        this.deleteContext(context);
      }
    });
  }

  private deleteContext(context: ScenarioContext): void {
    this.scenario.data.scenarioItems.forEach((item) => {
      if (item.from == SCENARIO_ITEM_FROM_BOT && item.actionDefinition) {
        item.actionDefinition.inputContextNames = item.actionDefinition.inputContextNames.filter(
          (inputContextName) => inputContextName != context.name
        );
        item.actionDefinition.outputContextNames = item.actionDefinition.outputContextNames.filter(
          (outputContextName) => outputContextName != context.name
        );
      }
      if (item.from == SCENARIO_ITEM_FROM_CLIENT && item.intentDefinition && item.intentDefinition.outputContextNames) {
        item.intentDefinition.outputContextNames = item.intentDefinition.outputContextNames.filter(
          (outputContextName) => outputContextName != context.name
        );
      }
    });
    this.scenario.data.contexts = this.scenario.data.contexts.filter((ctx) => ctx !== context);
  }

  isContextUsed(context: ScenarioContext): boolean {
    let isInput: boolean;
    let isOutput: boolean;
    const actionsDefinitions = getScenarioActionDefinitions(this.scenario);
    actionsDefinitions.forEach((actionDef) => {
      if (actionDef.inputContextNames.find((inputContextName) => inputContextName === context.name)) {
        isInput = true;
      }
      if (actionDef.outputContextNames.find((outputContextName) => outputContextName === context.name)) {
        isOutput = true;
      }
    });

    const intentDefinitions = getScenarioIntentDefinitions(this.scenario);
    intentDefinitions.forEach((intentDef) => {
      if (intentDef.outputContextNames?.find((outputContextName) => outputContextName === context.name)) {
        isOutput = true;
      }
    });

    return isInput && isOutput;
  }

  isTriggerUsed(trigger: string): boolean {
    const actionsDefinitions = getScenarioActionDefinitions(this.scenario);

    for (let action of actionsDefinitions) {
      if (action.trigger === trigger) return true;
    }

    return false;
  }

  private getNextItemId(): number {
    return Math.max(...this.scenario.data.scenarioItems.map((i) => i.id)) + 1;
  }

  private addItem(itemRef: ScenarioItem, from?: ScenarioItemFrom): void {
    let fromType = from || SCENARIO_ITEM_FROM_CLIENT;
    if (from == undefined && itemRef.from == SCENARIO_ITEM_FROM_CLIENT) {
      fromType = SCENARIO_ITEM_FROM_BOT;
    }

    const newEntry: ScenarioItem = {
      id: this.getNextItemId(),
      parentIds: [itemRef.id],
      from: fromType,
      text: ''
    };

    this.scenario.data.scenarioItems.push(newEntry);

    setTimeout(() => {
      this.selectItem(newEntry);
      this.scenarioConceptionService.requireItemPosition(newEntry);
    });
  }

  private deleteItem(itemRef: ScenarioItem, parentItemId: number): void {
    if (itemRef.parentIds.length > 1) {
      itemRef.parentIds = itemRef.parentIds.filter((pi) => pi != parentItemId);
    } else {
      this.removeItemDefinition(itemRef);
      this.scenario.data.scenarioItems = this.scenario.data.scenarioItems.filter((item) => item.id != itemRef.id);
    }
  }

  private changeItemType(item: ScenarioItem, targetType: ScenarioItemFrom): void {
    if (targetType === SCENARIO_ITEM_FROM_BOT && item.intentDefinition) {
      if (this.scenario.data.stateMachine) {
        this.removeItemDefinition(item);
      }
    }

    if (targetType === SCENARIO_ITEM_FROM_CLIENT && item.actionDefinition) {
      if (this.scenario.data.stateMachine) {
        this.removeItemDefinition(item);
      }
    }

    item.from = targetType;
  }

  private removeItemDefinition(item: ScenarioItem): void {
    if (item.intentDefinition) {
      if (this.scenario.data.stateMachine) {
        const intentTransitionsParents = getSmTransitionParentsByname(item.intentDefinition.name, this.scenario.data.stateMachine);
        intentTransitionsParents.forEach((parent) => {
          delete parent.on[item.intentDefinition.name];
        });
      }

      delete item.intentDefinition;
    }

    if (item.actionDefinition) {
      removeSmStateById(item.actionDefinition.name, this.scenario.data.stateMachine);

      delete item.actionDefinition;
    }
  }

  selectedItem: ScenarioItem;

  selectItem(item?: ScenarioItem): void {
    this.selectedItem = item ? item : undefined;
  }

  elementPosition: OffsetPosition;
  private centerOnItem(item: ScenarioItem, position: OffsetPosition, setFocus: boolean = true): void {
    this.elementPosition = position;

    if (setFocus) {
      setTimeout(() => {
        this.scenarioConceptionService.focusItem(item);
      }, CANVAS_TRANSITION_TIMING);
    }
  }

  @HostListener('window:keydown', ['$event'])
  onKeyPress(event: KeyboardEvent): void {
    if (this.isReadonly) return;

    if (this.scenario.data.mode === this.SCENARIO_MODE.writing && this.selectedItem) {
      if (event.altKey) {
        if (event.key === 'c') {
          this.addItem(this.selectedItem, SCENARIO_ITEM_FROM_CLIENT);
        }
        if (event.key === 'b') {
          this.addItem(this.selectedItem, SCENARIO_ITEM_FROM_BOT);
        }
        if (event.key === 'n') {
          this.addItem(this.selectedItem);
        }
      }
    }
  }

  itemDropped(targetId: number, droppedId: number): void {
    let targeted = this.findItemById(targetId);
    let dropped = this.findItemById(droppedId);

    if (dropped.parentIds === undefined) return;
    if (dropped.parentIds.includes(targetId)) return;
    if (this.isInFiliation(targeted, dropped)) return;

    // duplication désactivée
    // if (this.findItemChild(dropped)) dropped.parentIds = [targetId];
    // else dropped.parentIds.push(targetId);
    dropped.parentIds = [targetId];
  }

  private isInFiliation(parent: ScenarioItem, child: ScenarioItem): boolean {
    let current = parent;
    while (true) {
      if (!current.parentIds) return false;
      current = this.findItemById(current.parentIds[0]);
      if (!current) return false;
      if (current.id === child.id) return true;
    }
  }

  stopPropagation(event: MouseEvent, preventDefault = true): void {
    if (preventDefault) event.preventDefault();
    event.stopPropagation();
  }

  chatDisplayed: boolean = false;
  chatControlsDisplay: boolean = false;
  chatControlsFrom: ScenarioItemFrom;
  chatPropositions: ScenarioItem[];
  messages: any[] = [];

  closeChat(): void {
    this.chatDisplayed = false;
    this.chatPropositions = undefined;
    this.chatControlsFrom = undefined;
    this.chatControlsDisplay = false;
  }

  testStory(item?: ScenarioItem): void {
    this.messages = [];

    if (!item) {
      item = this.findItemById(0);
    }

    this.chatPropositions = undefined;
    this.chatControlsFrom = undefined;
    this.chatControlsDisplay = false;
    this.processChatEntry(item);
    this.chatDisplayed = true;
  }

  chatResponsesTimeout: number = 1000;

  processChatEntry(item: ScenarioItem): void {
    if (item) {
      this.addChatMessage(item.from, item.text);

      let children = this.getChildren(item);
      if (children.length < 1) return;

      if (children.length == 1) {
        setTimeout(() => {
          this.processChatEntry(children[0]);
        }, this.chatResponsesTimeout);
      } else {
        this.makePropositions(children[0]);
      }
    }
  }

  makePropositions(item: ScenarioItem): void {
    this.chatPropositions = this.getBrotherhood(item);
    let from = item.from;
    this.chatControlsFrom = from;
    this.chatControlsDisplay = true;
  }

  chooseProposition(item: ScenarioItem): void {
    this.chatPropositions = undefined;
    this.chatControlsFrom = undefined;
    this.chatControlsDisplay = false;
    this.addChatMessage(item.from, item.text);
    let child = this.findItemChild(item);
    if (child) {
      setTimeout(() => {
        this.processChatEntry(child);
      }, this.chatResponsesTimeout);
    }
  }

  userIdentities = {
    client: { name: 'Pierre Martin', avatar: 'assets/images/scenario-client.svg' },
    bot: { name: 'Bot', avatar: 'assets/images/scenario-bot.svg' },
    api: { name: 'Vérification', avatar: 'assets/images/scenario-verification.svg' }
  };

  private addChatMessage(from: string, text: string, type: string = 'text'): void {
    let user;
    switch (from) {
      case SCENARIO_ITEM_FROM_CLIENT:
        user = this.userIdentities[SCENARIO_ITEM_FROM_CLIENT];
        break;
      case SCENARIO_ITEM_FROM_BOT:
        user = this.userIdentities[SCENARIO_ITEM_FROM_BOT];
        break;
    }

    this.messages.push({
      text: text,
      date: new Date(),
      reply: from == SCENARIO_ITEM_FROM_CLIENT ? true : false,
      type: type,
      user: user
    });
  }

  private findItemChild(item: ScenarioItem): ScenarioItem {
    return this.scenario.data.scenarioItems.find((oitem) => oitem.parentIds?.includes(item.id));
  }

  private findItemById(id: number): ScenarioItem {
    return this.scenario.data.scenarioItems.find((oitem) => oitem.id == id);
  }

  private getChildren(item: ScenarioItem): ScenarioItem[] {
    return this.scenario.data.scenarioItems.filter((oitem) => oitem.parentIds?.includes(item.id));
  }

  private getBrotherhood(item: ScenarioItem): ScenarioItem[] {
    return this.scenario.data.scenarioItems.filter((oitem) => oitem.parentIds?.some((oip) => item.parentIds?.includes(oip)));
  }

  displayGraph(): void {
    this.nbDialogService.open(ContextsGraphComponent, {
      context: {
        scenario: this.scenario
      },
      dialogClass: 'full-width-dialog'
    });
  }

  toggleFullscreen(_toggle: boolean): void {
    this.requestFullscreen.emit();
  }

  sidePanelChange(status: 'open' | 'close'): void {
    this.isSidePanelOpen = status === 'open';
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}

import { Component, ElementRef, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, take } from 'rxjs/operators';
import { DialogService } from 'src/app/core-nlp/dialog.service';
import { StateService } from 'src/app/core-nlp/state.service';
import { ChoiceDialogComponent } from '../../../shared/components';
import { getSmTransitionParentsByname, renameSmStateById } from '../../commons/utils';
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
import { ScenarioDesignerService } from '../scenario-designer.service';
import { ActionEditComponent } from './action-edit/action-edit.component';
import { IntentCreateComponent } from './intent-create/intent-create.component';
import { IntentEditComponent } from './intent-edit/intent-edit.component';
import { IntentsSearchComponent } from './intents-search/intents-search.component';
import { ScenarioConceptionService } from './scenario-conception-service.service';

@Component({
  selector: 'scenario-conception-item',
  templateUrl: './scenario-conception-item.component.html',
  styleUrls: ['./scenario-conception-item.component.scss']
})
export class ScenarioConceptionItemComponent implements OnInit, OnDestroy {
  readonly SCENARIO_MODE = SCENARIO_MODE;
  readonly SCENARIO_ITEM_FROM_CLIENT = SCENARIO_ITEM_FROM_CLIENT;
  readonly SCENARIO_ITEM_FROM_BOT = SCENARIO_ITEM_FROM_BOT;

  destroy = new Subject();

  @Input() item: ScenarioItem;
  @Input() parentId: number;
  @Input() contexts: ScenarioContext[];
  @Input() selectedItem: ScenarioItem;
  @Input() mode: string;
  @Input() scenario: ScenarioVersionExtended;
  @Input() isReadonly: boolean = false;
  @Input() readonly avalaibleHandlers: Handler[];

  @ViewChild('itemCard', { read: ElementRef }) itemCard: ElementRef<HTMLInputElement>;
  @ViewChild('itemTextarea', { read: ElementRef }) itemTextarea: ElementRef<HTMLInputElement>;

  constructor(
    private scenarioConceptionService: ScenarioConceptionService,
    private dialogService: DialogService,
    protected state: StateService,
    private scenarioDesignerService: ScenarioDesignerService
  ) {
    this.scenarioConceptionService.scenarioDesignerItemsCommunication.pipe(takeUntil(this.destroy)).subscribe((evt) => {
      if (evt.type == 'focusItem') this.focusItem(evt.item);
      if (evt.type == 'requireItemPosition') this.requireItemPosition(evt.item);
    });
  }

  ngOnInit(): void {
    this.draggable = {
      data: this.item.id
    };
  }

  ngAfterViewInit(): void {
    this.itemCard.nativeElement.addEventListener('mousedown', function (event) {
      event.stopPropagation();
    });
  }

  manageAction() {
    const modal = this.dialogService.openDialog(ActionEditComponent, {
      context: {
        item: this.item,
        contexts: this.contexts,
        scenario: this.scenario,
        avalaibleHandlers: this.avalaibleHandlers,
        isReadonly: this.isReadonly
      }
    });
    modal.componentRef.instance.saveModifications.pipe(take(1)).subscribe((actionDef) => {
      this.checkAndAddNewTrigger(actionDef.trigger);
      this.checkAndAddNewContexts(actionDef.inputContextNames);
      this.checkAndAddNewContexts(actionDef.outputContextNames);
      if (this.scenario.data.stateMachine && this.item.actionDefinition && this.item.actionDefinition.name !== actionDef.name) {
        renameSmStateById(this.item.actionDefinition.name, actionDef.name, this.scenario.data.stateMachine);
      }

      if (this.item.actionDefinition?.answerId) {
        if (this.item.actionDefinition.answer !== actionDef.answer) {
          actionDef.answerUpdate = true;
        }
      }

      this.item.actionDefinition = actionDef;

      modal.close();
    });

    modal.componentRef.instance.deleteDefinition.pipe(take(1)).subscribe(() => {
      this.scenarioConceptionService.removeItemDefinition(this.item);

      modal.close();
    });
  }

  private checkAndAddNewTrigger(trigger: string): void {
    if (trigger && !this.scenario.data.triggers.includes(trigger)) this.scenario.data.triggers = [...this.scenario.data.triggers, trigger];
  }

  checkAndAddNewContexts(contextNames) {
    contextNames.forEach((context) => {
      if (!this.contexts?.find((c) => c.name == context)) {
        this.contexts.push({
          name: context,
          type: 'string'
        });
      }
    });
  }

  manageIntent(): void {
    if (this.item.intentDefinition) {
      this.editIntent();
    } else {
      this.searchIntent();
    }
  }

  searchIntent(): void {
    const modal = this.dialogService.openDialog(IntentsSearchComponent, {
      context: {
        intentSentence: this.item.text,
        scenarioDesignerService: this.scenarioDesignerService
      }
    });

    modal.componentRef.instance.createNewIntentEvent.pipe(take(1)).subscribe(() => {
      this.createIntent();
      modal.close();
    });

    modal.componentRef.instance.useIntentEvent.pipe(take(1)).subscribe((intent) => {
      this.setItemIntentDefinition(intent);
      modal.close();
    });
  }

  setItemIntentDefinition(intentDef): void {
    this.item.intentDefinition = {
      label: intentDef.label,
      name: intentDef.name,
      category: intentDef.category,
      description: intentDef.description,
      intentId: intentDef._id,
      primary: this.item.main
      // isEvent: false
    };

    this.scenarioDesignerService.grabIntentSentences(this.item).subscribe(() => {
      this.editIntent();
    });
  }

  createIntent(): void {
    const modal = this.dialogService.openDialog(IntentCreateComponent, {
      context: {
        item: this.item,
        scenario: this.scenario
      }
    });
    const createIntentEvent = modal.componentRef.instance.createIntentEvent.pipe(take(1)).subscribe((res) => {
      if (this.item.main) res.primary = true;
      this.item.intentDefinition = res;
      this.editIntent();
      // createIntentEvent.unsubscribe();
      modal.close();
    });
  }

  editIntent(): void {
    const modal = this.dialogService.openDialog(IntentEditComponent, {
      context: {
        item: this.item,
        contexts: this.contexts,
        scenario: this.scenario,
        isReadonly: this.isReadonly
      }
    });

    modal.componentRef.instance.saveModifications.pipe(take(1)).subscribe((intentDef) => {
      // If an intent is not primary anymore, it should not be a transition of the global state
      if (this.scenario.data.stateMachine && this.item.intentDefinition.primary && !intentDef.primary) {
        const parents = getSmTransitionParentsByname(this.item.intentDefinition.name, this.scenario.data.stateMachine);
        parents.forEach((parent) => {
          if (parent?.id.toLowerCase() === 'global') {
            delete parent.on[this.item.intentDefinition.name];
          }
        });
      }

      this.item.intentDefinition.primary = intentDef.primary;
      this.item.intentDefinition.sentences = intentDef.sentences;

      intentDef.contextsEntities.forEach((ctxEntity) => {
        const ctxIndex = this.contexts.findIndex((ctx) => ctx.name == ctxEntity.name);
        if (ctxIndex >= 0) this.contexts.splice(ctxIndex, 1, ctxEntity);
        else this.contexts.push(ctxEntity);
      });

      this.checkAndAddNewContexts(intentDef.outputContextNames);
      this.item.intentDefinition.outputContextNames = intentDef.outputContextNames;

      modal.close();
    });

    modal.componentRef.instance.onRemoveDefinition.pipe(take(1)).subscribe((intentDef) => {
      this.scenarioConceptionService.removeItemDefinition(this.item);
      modal.close();
    });
  }

  itemHasDefinition() {
    return this.item.intentDefinition || this.item.actionDefinition;
  }

  selectItem(): void {
    this.scenarioConceptionService.selectItem(this.item);
  }

  focusItem(item: ScenarioItem): void {
    if (item == this.item) {
      this.itemTextarea.nativeElement.focus();
    }
  }

  requireItemPosition(item: ScenarioItem): void {
    if (item == this.item) {
      this.scenarioConceptionService.exposeItemPosition(this.item, {
        offsetLeft: this.itemCard.nativeElement.offsetLeft,
        offsetTop: this.itemCard.nativeElement.offsetTop,
        offsetWidth: this.itemCard.nativeElement.offsetWidth,
        offsetHeight: this.itemCard.nativeElement.offsetHeight
      });
    }
  }

  test(): void {
    this.scenarioConceptionService.testItem(this.item);
  }

  getParentItem(): ScenarioItem {
    return this.scenario.data.scenarioItems.find((item) => item.id == this.parentId);
  }

  getChildItems(): ScenarioItem[] {
    return this.scenario.data.scenarioItems.filter((item) => item.parentIds?.includes(this.item.id));
  }

  itemHasNoChildren(): boolean {
    let childs = this.getChildItems();
    return !childs.length;
  }

  itemHasSeveralChildren(): boolean {
    return this.getChildItems().length > 1;
  }

  answering(): void {
    this.scenarioConceptionService.addAnswer(this.item);
  }

  delete(): void {
    let alertMessage;
    if (this.item.intentDefinition) {
      if (this.item.from === SCENARIO_ITEM_FROM_CLIENT) {
        alertMessage =
          'This client intervention already has an intent definition. By deleting the intervention, this definition will be lost. Are you sure you want to continue?';
      } else {
        // to ensure backward compatibility with the early stages of the project
        delete this.item.intentDefinition;
      }
    } else if (this.item.actionDefinition) {
      if (this.item.from === SCENARIO_ITEM_FROM_BOT) {
        alertMessage =
          'This bot intervention already has an action definition. By deleting the intervention, this definition will be lost. Are you sure you want to continue?';
      } else {
        // to ensure backward compatibility with the early stages of the project
        delete this.item.actionDefinition;
      }
    }

    if (alertMessage) {
      const cancelAction = 'cancel';
      const confirmAction = 'delete';
      const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
        context: {
          title: `Deletion of an intervention`,
          subtitle: alertMessage,
          modalStatus: 'danger',
          actions: [{ actionName: cancelAction, buttonStatus: 'basic' }, { actionName: confirmAction }]
        }
      });
      dialogRef.onClose.subscribe((result) => {
        if (result) {
          if (result == confirmAction) {
            this.scenarioConceptionService.deleteAnswer(this.item, this.parentId);
          }
        }
      });
    } else {
      this.scenarioConceptionService.deleteAnswer(this.item, this.parentId);
    }
  }

  getItemCardCssClass(): string {
    let classes = 'cursor-default ' + this.item.from;
    if (this.item.from == SCENARIO_ITEM_FROM_BOT) {
      if (this.item.final) classes += ' final';
    }
    if (this.item.parentIds?.length > 1) classes += ' duplicate';
    if (this.selectedItem?.id == this.item.id) classes += ' selected';
    return classes;
  }

  switchItemType(which: ScenarioItemFrom): void {
    if (which === this.item.from) return;

    let alertMessage;
    if (this.item.intentDefinition) {
      if (this.item.from === SCENARIO_ITEM_FROM_CLIENT) {
        alertMessage =
          'This client intervention already has an intent definition. By changing the type of the intervention, this definition will be removed. Are you sure you want to continue?';
      } else {
        // to ensure backward compatibility with the early stages of the project
        delete this.item.intentDefinition;
      }
    } else if (this.item.actionDefinition) {
      if (this.item.from === SCENARIO_ITEM_FROM_BOT) {
        alertMessage =
          'This bot intervention already has an action definition. By changing the type of the intervention, this definition will be removed. Are you sure you want to continue?';
      } else {
        // to ensure backward compatibility with the early stages of the project
        delete this.item.actionDefinition;
      }
    }

    if (alertMessage) {
      const cancelAction = 'cancel';
      const confirmAction = 'change';
      const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
        context: {
          title: `Change of intervention type`,
          subtitle: alertMessage,
          modalStatus: 'danger',
          actions: [
            { actionName: cancelAction, buttonStatus: 'basic', ghost: true },
            { actionName: confirmAction, buttonStatus: 'danger' }
          ]
        }
      });
      dialogRef.onClose.subscribe((result) => {
        if (result) {
          if (result == confirmAction) {
            this.scenarioConceptionService.changeItemType(this.item, which);
          }
        }
      });
    } else {
      this.scenarioConceptionService.changeItemType(this.item, which);
    }
  }

  toggleFinal($event): void {
    if ($event) this.item.final = true;
    else delete this.item.final;
  }

  itemCanHaveAnswer(): boolean {
    return !this.item.final;
  }

  getItemBrothers(): ScenarioItem[] {
    return this.scenario.data.scenarioItems.filter((item) => {
      return item.id != this.item.id && item.parentIds?.some((id) => this.item.parentIds?.includes(id));
    });
  }

  shouldShowArrowTop(which): boolean {
    let brothers = this.getItemBrothers();

    if (!brothers.length) return false;

    let bIds = brothers.map((b) => b.id);

    if (which == 'left') {
      const min = Math.min(...bIds);
      if (this.item.id < min) return false;
    }
    if (which == 'right') {
      const max = Math.max(...bIds);
      if (this.item.id > max) return false;
    }
    return true;
  }

  draggable;

  onDrop($event): void {
    if (this.item.id == $event.data) return;
    this.scenarioConceptionService.itemDropped(this.item.id, $event.data);
  }

  mouseWheel(event) {
    event.stopPropagation();
  }

  ngOnDestroy() {
    this.destroy.next();
    this.destroy.complete();
  }
}

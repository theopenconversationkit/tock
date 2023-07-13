import { Component, ElementRef, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { NbDialogService } from '@nebular/theme';
import { Subject } from 'rxjs';
import { takeUntil, take } from 'rxjs/operators';
import { StoryDefinitionConfigurationSummary } from '../../../bot/model/story';

import { StateService } from '../../../core-nlp/state.service';
import { Intent } from '../../../model/nlp';
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
  Handler,
  ScenarioActionDefinition,
  ScenarioIntentDefinitionForm,
  ScenarioIntentDefinition
} from '../../models';
import { ScenarioDesignerService } from '../scenario-designer.service';
import { ActionEditComponent } from './action-edit/action-edit.component';
import { IntentCreateComponent } from './intent-create/intent-create.component';
import { IntentEditComponent } from './intent-edit/intent-edit.component';
import { IntentsSearchComponent } from './intents-search/intents-search.component';
import { ScenarioConceptionService } from './scenario-conception-service.service';

type Draggable = { data: number };

@Component({
  selector: 'tock-scenario-conception-item',
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
  @Input() readonly availableStories: StoryDefinitionConfigurationSummary[];

  @ViewChild('itemCard', { read: ElementRef }) itemCard: ElementRef<HTMLInputElement>;
  @ViewChild('itemTextarea', { read: ElementRef }) itemTextarea: ElementRef<HTMLInputElement>;

  constructor(
    private scenarioConceptionService: ScenarioConceptionService,
    private stateService: StateService,
    private scenarioDesignerService: ScenarioDesignerService,
    private nbDialogService: NbDialogService
  ) {
    this.scenarioConceptionService.scenarioDesignerItemsCommunication.pipe(takeUntil(this.destroy)).subscribe((evt) => {
      if (evt.type == 'focusItem') this.focusItem(evt.item);
      if (evt.type == 'requireItemPosition') this.requireItemPosition(evt.item);
    });
  }

  draggable: Draggable;

  ngOnInit(): void {
    this.draggable = {
      data: this.item.id
    };
  }

  stopPropagation(event: MouseEvent): void {
    event.stopPropagation();
  }

  manageAction(): void {
    const modal = this.nbDialogService.open(ActionEditComponent, {
      context: {
        item: this.item,
        contexts: this.contexts,
        scenario: this.scenario,
        avalaibleHandlers: this.avalaibleHandlers,
        isReadonly: this.isReadonly,
        availableStories: this.availableStories
      }
    });
    modal.componentRef.instance.saveModifications.pipe(take(1)).subscribe((actionDef: ScenarioActionDefinition) => {
      this.checkAndAddNewTrigger(actionDef.trigger);
      this.checkAndAddNewContexts(actionDef.inputContextNames);
      this.checkAndAddNewContexts(actionDef.outputContextNames);

      if (this.scenario.data.stateMachine && this.item.actionDefinition && this.item.actionDefinition.name !== actionDef.name) {
        renameSmStateById(this.item.actionDefinition.name, actionDef.name, this.scenario.data.stateMachine);
      }

      // we update the answers according to incoming modifications
      this.updateAnswersModifications(actionDef, false);

      // we update the unknown answers according to incoming modifications
      this.updateAnswersModifications(actionDef, true);

      this.item.actionDefinition = actionDef;

      modal.close();
    });

    modal.componentRef.instance.deleteDefinition.pipe(take(1)).subscribe(() => {
      this.scenarioConceptionService.removeItemDefinition(this.item);

      modal.close();
    });
  }

  private updateAnswersModifications(actionDef: ScenarioActionDefinition, unknownAnswers: boolean): void {
    let answersArray;
    let storedAnswerId;

    if (!unknownAnswers) {
      // We get rid of the empty locale answers
      actionDef.answers = actionDef.answers.filter((scenarioAnswer) => scenarioAnswer.answer?.trim().length > 0);

      answersArray = actionDef.answers;
      storedAnswerId = actionDef.answerId;
    } else {
      // We get rid of the empty locale unknown answers
      actionDef.unknownAnswers = actionDef.unknownAnswers.filter((scenarioAnswer) => scenarioAnswer.answer?.trim().length > 0);

      answersArray = actionDef.unknownAnswers;
      storedAnswerId = actionDef.unknownAnswerId;
    }

    // We check the necessary updates if answers already existed
    if (storedAnswerId) {
      let stillHasAnswers = false;
      answersArray.forEach((scenarioAnswer) => {
        if (scenarioAnswer.answer.trim().length) stillHasAnswers = true;
      });

      if (!stillHasAnswers) {
        // No more answer is defined, we delete the stored answer id
        if (!unknownAnswers) {
          delete actionDef.answerId;
        } else {
          delete actionDef.unknownAnswerId;
        }
      } else {
        // We check if answers have changed to flag them in update
        answersArray.forEach((scenarioAnswer) => {
          let existingAnswersArray = this.item.actionDefinition.answers;
          if (unknownAnswers) {
            existingAnswersArray = this.item.actionDefinition.unknownAnswers;
          }

          const existingAnswer = existingAnswersArray?.find(
            (storedScenarioAnswer) => storedScenarioAnswer.locale === scenarioAnswer.locale
          );
          if (existingAnswer && existingAnswer.answer !== scenarioAnswer.answer) {
            scenarioAnswer.answerUpdate = true;
          }
        });
      }
    }
  }

  private checkAndAddNewTrigger(trigger: string): void {
    if (trigger && !this.scenario.data.triggers.includes(trigger)) this.scenario.data.triggers = [...this.scenario.data.triggers, trigger];
  }

  private checkAndAddNewContexts(contextNames: string[]): void {
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

  private searchIntent(): void {
    const modal = this.nbDialogService.open(IntentsSearchComponent, {
      context: {
        intentSentence: this.item.text
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

  private setItemIntentDefinition(intent: Intent): void {
    this.item.intentDefinition = {
      label: intent.label,
      name: intent.name,
      category: intent.category,
      description: intent.description,
      intentId: intent._id,
      sentences: [],
      primary: this.item.main
    };

    this.scenarioDesignerService.grabIntentSentences(this.item).subscribe(() => {
      this.editIntent();
    });
  }

  private createIntent(): void {
    const modal = this.nbDialogService.open(IntentCreateComponent, {
      context: {
        item: this.item,
        scenario: this.scenario
      }
    });
    modal.componentRef.instance.createIntentEvent.pipe(take(1)).subscribe((res) => {
      if (this.item.main) res.primary = true;
      this.item.intentDefinition = res;
      this.editIntent();
      modal.close();
    });
  }

  private editIntent(): void {
    const modal = this.nbDialogService.open(IntentEditComponent, {
      context: {
        item: this.item,
        contexts: this.contexts,
        scenario: this.scenario,
        isReadonly: this.isReadonly
      }
    });

    modal.componentRef.instance.saveModifications.pipe(take(1)).subscribe((intentDef: ScenarioIntentDefinitionForm) => {
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

    modal.componentRef.instance.onRemoveDefinition.pipe(take(1)).subscribe((_intentDef) => {
      this.scenarioConceptionService.removeItemDefinition(this.item);
      modal.close();
    });
  }

  itemHasDefinition(): ScenarioActionDefinition | ScenarioIntentDefinition {
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

  private requireItemPosition(item: ScenarioItem): void {
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

  getChildItems(): ScenarioItem[] {
    return this.scenario.data.scenarioItems.filter((item) => item.parentIds?.includes(this.item.id));
  }

  itemHasNoChildren(): boolean {
    let childs = this.getChildItems();
    return !childs.length;
  }

  answering(): void {
    this.scenarioConceptionService.addAnswer(this.item);
  }

  getCurrentLocaleAnswer(): string {
    const scenarioAnswer = this.item.actionDefinition.answers.find((sa) => {
      return sa.locale === this.stateService.currentLocale;
    });
    return scenarioAnswer?.answer || '';
  }

  delete(): void {
    let alertMessage: string;
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
      const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
        context: {
          title: `Deletion of an intervention`,
          subtitle: alertMessage,
          modalStatus: 'danger',
          actions: [{ actionName: cancelAction, buttonStatus: 'basic' }, { actionName: confirmAction }]
        }
      });
      dialogRef.onClose.subscribe((result) => {
        if (result === confirmAction) {
          this.scenarioConceptionService.deleteAnswer(this.item, this.parentId);
        }
      });
    } else {
      this.scenarioConceptionService.deleteAnswer(this.item, this.parentId);
    }
  }

  getItemCardCssClass(): string {
    const classes = ['cursor-default', this.item.from];

    if (this.item.from === SCENARIO_ITEM_FROM_BOT && this.item.final) classes.push('final');
    if (this.item.parentIds?.length > 1) classes.push('duplicate');
    if (this.selectedItem?.id == this.item.id) classes.push('selected');

    return classes.join(' ');
  }

  switchItemType(wich: ScenarioItemFrom): void {
    if (wich === this.item.from) return;

    let alertMessage: string;
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
      const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
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
            this.scenarioConceptionService.changeItemType(this.item, wich);
          }
        }
      });
    } else {
      this.scenarioConceptionService.changeItemType(this.item, wich);
    }
  }

  toggleFinal($event: boolean): void {
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

  shouldShowArrowTop(wich: 'left' | 'right' | 'leftAndRight'): boolean {
    let brothers = this.getItemBrothers();

    if (!brothers.length) return false;

    let bIds = brothers.map((b) => b.id);

    if (wich === 'left') {
      const min = Math.min(...bIds);
      if (this.item.id < min) return false;
    }
    if (wich === 'right') {
      const max = Math.max(...bIds);
      if (this.item.id > max) return false;
    }
    if (wich === 'leftAndRight') {
      const max = Math.max(...bIds);
      const min = Math.min(...bIds);
      if (this.item.id < min || this.item.id > max) return true;
    }
    return true;
  }

  onDrop($event: Draggable): void {
    if (this.item.id == $event.data) return;
    this.scenarioConceptionService.itemDropped(this.item.id, $event.data);
  }

  ngOnDestroy() {
    this.destroy.next(true);
    this.destroy.complete();
  }
}

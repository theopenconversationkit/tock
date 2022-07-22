import { Component, ElementRef, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { DialogService } from 'src/app/core-nlp/dialog.service';
import { StateService } from 'src/app/core-nlp/state.service';
import { SearchQuery } from 'src/app/model/nlp';
import { NlpService } from 'src/app/nlp-tabs/nlp.service';
import {
  scenarioItem,
  SCENARIO_ITEM_FROM_BOT,
  SCENARIO_ITEM_FROM_CLIENT,
  SCENARIO_MODE,
  TickContext
} from '../../models/scenario.model';
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
  @Input() itemId: number;
  @Input() parentId: number;
  @Input() scenarioItems: scenarioItem[];
  @Input() contexts: TickContext[];
  @Input() selectedItem: scenarioItem;
  @Input() mode: string;
  @ViewChild('itemCard', { read: ElementRef }) itemCard: ElementRef<HTMLInputElement>;
  @ViewChild('itemTextarea', { read: ElementRef }) itemTextarea: ElementRef<HTMLInputElement>;

  item: scenarioItem;
  utterancesLoading = true;

  constructor(
    private scenarioConceptionService: ScenarioConceptionService,
    private dialogService: DialogService,
    protected state: StateService,
    private nlp: NlpService
  ) {
    this.scenarioConceptionService.scenarioDesignerItemsCommunication
      .pipe(takeUntil(this.destroy))
      .subscribe((evt) => {
        if (evt.type == 'focusItem') this.focusItem(evt.item);
        if (evt.type == 'requireItemPosition') this.requireItemPosition(evt.item);
      });
  }

  ngOnInit(): void {
    this.item = this.scenarioItems.find((item) => item.id === this.itemId);
    this.draggable = {
      data: this.item.id
    };

    if (this.item.intentDefinition?.intentId) this.collectIntentUtterances();
    else {
      this.utterancesLoading = false;
    }
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
        contexts: this.contexts
      }
    });
    const saveModifications = modal.componentRef.instance.saveModifications
      .pipe(takeUntil(this.destroy))
      .subscribe((actionDef) => {
        this.checkAndAddNewContexts(actionDef.inputContextNames);
        this.checkAndAddNewContexts(actionDef.outputContextNames);
        this.item.tickActionDefinition = actionDef;

        saveModifications.unsubscribe();
        modal.close();
      });

    const deleteDefinition = modal.componentRef.instance.deleteDefinition
      .pipe(takeUntil(this.destroy))
      .subscribe(() => {
        delete this.item.tickActionDefinition;

        deleteDefinition.unsubscribe();
        modal.close();
      });
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
        intentSentence: this.item.text
      }
    });
    const createNewIntentEvent = modal.componentRef.instance.createNewIntentEvent
      .pipe(takeUntil(this.destroy))
      .subscribe((res) => {
        this.createIntent();
        createNewIntentEvent.unsubscribe();
        modal.close();
      });
    const useIntentEvent = modal.componentRef.instance.useIntentEvent
      .pipe(takeUntil(this.destroy))
      .subscribe((intent) => {
        this.setItemIntentDefinition(intent);
        useIntentEvent.unsubscribe();
        modal.close();
      });
  }

  setItemIntentDefinition(intentDef) {
    this.item.intentDefinition = {
      label: intentDef.label,
      name: intentDef.name,
      category: intentDef.category,
      description: intentDef.description,
      intentId: intentDef._id
    };
    this.utterancesLoading = true;
    this.collectIntentUtterances();
  }

  collectIntentUtterances() {
    const searchQuery: SearchQuery = this.scenarioConceptionService.createSearchIntentsQuery({
      intentId: this.item.intentDefinition.intentId
    });

    const nlpSubscription = this.nlp.searchSentences(searchQuery).subscribe((sentencesResearch) => {
      this.item.intentDefinition._sentences = sentencesResearch.rows;
      this.utterancesLoading = false;
      nlpSubscription.unsubscribe();

      // if (this.item.id === 0) this.manageIntent();
    });
  }

  createIntent(): void {
    const modal = this.dialogService.openDialog(IntentCreateComponent, {
      context: {
        item: this.item
      }
    });
    const createIntentEvent = modal.componentRef.instance.createIntentEvent
      .pipe(takeUntil(this.destroy))
      .subscribe((res) => {
        this.item.intentDefinition = res;
        this.editIntent();
        createIntentEvent.unsubscribe();
        modal.close();
      });
  }

  editIntent(): void {
    const modal = this.dialogService.openDialog(IntentEditComponent, {
      context: {
        item: this.item,
        contexts: this.contexts
      }
    });
    const saveModificationsSubscription = modal.componentRef.instance.saveModifications
      .pipe(takeUntil(this.destroy))
      .subscribe((intentDef) => {
        this.item.intentDefinition.primary = intentDef.primary;
        this.item.intentDefinition.sentences = intentDef.sentences;

        intentDef.contextsEntities.forEach((ctxEntity) => {
          const ctxIndex = this.contexts.findIndex((ctx) => ctx.name == ctxEntity.name);
          if (ctxIndex >= 0) this.contexts.splice(ctxIndex, 1, ctxEntity);
          else this.contexts.push(ctxEntity);
        });
        saveModificationsSubscription.unsubscribe();
        modal.close();
      });
  }

  itemHasDefinition() {
    return this.item.intentDefinition || this.item.tickActionDefinition;
  }

  selectItem(): void {
    this.scenarioConceptionService.selectItem(this.item);
  }

  focusItem(item: scenarioItem): void {
    if (item == this.item) {
      this.itemTextarea.nativeElement.focus();
    }
  }

  requireItemPosition(item: scenarioItem): void {
    if (item == this.item) {
      this.scenarioConceptionService.exposeItemPosition(this.item, {
        left: this.itemCard.nativeElement.offsetLeft,
        top: this.itemCard.nativeElement.offsetTop,
        width: this.itemCard.nativeElement.offsetWidth,
        height: this.itemCard.nativeElement.offsetHeight
      });
    }
  }

  test(): void {
    this.scenarioConceptionService.testItem(this.item);
  }

  getParentItem(): scenarioItem {
    return this.scenarioItems.find((item) => item.id == this.parentId);
  }

  getChildItems(): scenarioItem[] {
    return this.scenarioItems.filter((item) => item.parentIds?.includes(this.item.id));
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
    this.scenarioConceptionService.deleteAnswer(this.item, this.parentId);
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

  switchItemType(which): void {
    if (which == SCENARIO_ITEM_FROM_CLIENT) {
      this.item.from = SCENARIO_ITEM_FROM_CLIENT;
    }
    if (which == SCENARIO_ITEM_FROM_BOT) {
      this.item.from = SCENARIO_ITEM_FROM_BOT;
    }
  }

  toggleFinal($event): void {
    if ($event) this.item.final = true;
    else delete this.item.final;
  }

  getItemBrothers(): scenarioItem[] {
    return this.scenarioItems.filter((item) => {
      return (
        item.id != this.item.id &&
        item.parentIds?.some((id) => this.item.parentIds && this.item.parentIds.includes(id))
      );
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

  itemCanHaveAnswer(): boolean {
    return !this.item.final;
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

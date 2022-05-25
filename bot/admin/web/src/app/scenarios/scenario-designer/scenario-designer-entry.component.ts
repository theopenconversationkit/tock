import { Component, ElementRef, HostListener, Input, OnInit, ViewChild } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { DialogService } from 'src/app/core-nlp/dialog.service';
import { StateService } from 'src/app/core-nlp/state.service';
import { SearchQuery } from 'src/app/model/nlp';
import { NlpService } from 'src/app/nlp-tabs/nlp.service';
import {
  scenarioItem,
  SCENARIO_ITEM_FROM_API,
  SCENARIO_ITEM_FROM_BOT,
  SCENARIO_ITEM_FROM_CLIENT,
  SCENARIO_MODE_PRODUCTION,
  SCENARIO_MODE_WRITING
} from '../models/scenario.model';
import { ApiEditComponent } from './api-edit/api-edit.component';
import { IntentCreateComponent } from './intent-create/intent-create.component';
import { IntentEditComponent } from './intent-edit/intent-edit.component';
import { IntentsSearchComponent } from './intents-search/intents-search.component';
import { ScenarioDesignerService } from './scenario-designer-service.service';

@Component({
  selector: 'scenario-designer-entry',
  templateUrl: './scenario-designer-entry.component.html',
  styleUrls: ['./scenario-designer-entry.component.scss']
})
export class ScenarioDesignerEntryComponent implements OnInit {
  readonly SCENARIO_MODE_PRODUCTION = SCENARIO_MODE_PRODUCTION;
  readonly SCENARIO_MODE_WRITING = SCENARIO_MODE_WRITING;
  readonly SCENARIO_ITEM_FROM_CLIENT = SCENARIO_ITEM_FROM_CLIENT;
  readonly SCENARIO_ITEM_FROM_BOT = SCENARIO_ITEM_FROM_BOT;
  readonly SCENARIO_ITEM_FROM_API = SCENARIO_ITEM_FROM_API;

  destroy = new Subject();
  @Input() itemId: number;
  @Input() parentId: number;
  @Input() dataList: scenarioItem[];
  @Input() selectedItem: scenarioItem;
  @Input() mode: string;
  @ViewChild('itemCard', { read: ElementRef }) itemCard: ElementRef<HTMLInputElement>;
  @ViewChild('itemTextarea', { read: ElementRef }) itemTextarea: ElementRef<HTMLInputElement>;

  item: scenarioItem;
  utterancesLoading = true;

  constructor(
    private scenarioDesignerService: ScenarioDesignerService,
    private dialogService: DialogService,
    protected state: StateService,
    private nlp: NlpService
  ) {
    this.scenarioDesignerService.scenarioDesignerItemsCommunication
      .pipe(takeUntil(this.destroy))
      .subscribe((evt) => {
        if (evt.type == 'focusItem') this.focusItem(evt.item);
        if (evt.type == 'requireItemPosition') this.requireItemPosition(evt.item);
      });
  }

  ngOnInit(): void {
    this.item = this.dataList.find((item) => item.id === this.itemId);
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

  manageApi() {
    const modal = this.dialogService.openDialog(ApiEditComponent, {
      context: {
        item: this.item
      }
    });
    const saveModifications = modal.componentRef.instance.saveModifications
      .pipe(takeUntil(this.destroy))
      .subscribe((res) => {
        saveModifications.unsubscribe();
        modal.close();
      });
  }
  setItemApiDefinition(apiDef) {}

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
    const searchQuery: SearchQuery = this.scenarioDesignerService.createSearchIntentsQuery({
      intentId: this.item.intentDefinition.intentId
    });

    const nlpSubscription = this.nlp.searchSentences(searchQuery).subscribe((sentencesResearch) => {
      this.item.intentDefinition._sentences = sentencesResearch.rows.map(
        (sentence) => sentence.text
      );
      this.utterancesLoading = false;
      nlpSubscription.unsubscribe();
    });
  }

  createIntent(): void {
    const modal = this.dialogService.openDialog(IntentCreateComponent, {
      context: {
        intentSentence: this.item.text
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
        item: this.item
      }
    });
    const saveModificationsSubsciption = modal.componentRef.instance.saveModifications
      .pipe(takeUntil(this.destroy))
      .subscribe((modifiedItem) => {
        this.item.intentDefinition = modifiedItem.intentDefinition;
        saveModificationsSubsciption.unsubscribe();
        modal.close();
      });
  }

  itemHasIntent() {
    return this.item.intentDefinition;
  }

  selectItem(): void {
    this.scenarioDesignerService.selectItem(this.item);
  }

  focusItem(item: scenarioItem): void {
    if (item == this.item) {
      this.itemTextarea.nativeElement.focus();
    }
  }

  requireItemPosition(item: scenarioItem): void {
    if (item == this.item) {
      this.scenarioDesignerService.exposeItemPosition(this.item, {
        left: this.itemCard.nativeElement.offsetLeft,
        top: this.itemCard.nativeElement.offsetTop,
        width: this.itemCard.nativeElement.offsetWidth,
        height: this.itemCard.nativeElement.offsetHeight
      });
    }
  }

  test(): void {
    this.scenarioDesignerService.testItem(this.item);
  }

  getChildItems(): scenarioItem[] {
    return this.dataList.filter((item) => item.parentIds?.includes(this.item.id));
  }

  itemHasSeveralChildren(): boolean {
    return this.getChildItems().length > 1;
  }

  answering(): void {
    this.scenarioDesignerService.addAnswer(this.item);
  }

  delete(): void {
    this.scenarioDesignerService.deleteAnswer(this.item, this.parentId);
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
    if (which == SCENARIO_ITEM_FROM_API) {
      this.item.from = SCENARIO_ITEM_FROM_API;
    }
  }

  toggleFinal($event): void {
    if ($event) this.item.final = true;
    else delete this.item.final;
  }

  shouldShowArrowTop(which): boolean {
    let brothers = this.dataList.filter((item) => {
      return (
        item.id != this.item.id &&
        item.parentIds?.some((ip) => this.item.parentIds && this.item.parentIds.includes(ip))
      );
    });

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

  itemHasNoChildren(): boolean {
    let childs = this.dataList.filter(
      (item) => item.parentIds && item.parentIds.includes(this.item.id)
    );
    return !childs.length;
  }

  itemCanHaveAnswer(): boolean {
    return !this.item.final;
  }

  draggable;

  onDrop($event): void {
    if (this.item.id == $event.data) return;
    this.scenarioDesignerService.itemDropped(this.item.id, $event.data);
  }

  mouseWheel(event) {
    event.stopPropagation();
  }

  ngOnDestroy() {
    this.destroy.next();
    this.destroy.complete();
  }
}

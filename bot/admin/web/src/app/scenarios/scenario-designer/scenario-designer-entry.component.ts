import { Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { DialogService } from 'src/app/core-nlp/dialog.service';
import { scenarioItem } from '../models/scenario.model';
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
  destroy = new Subject();
  @Input() itemId: number;
  @Input() parentId: number;
  @Input() dataList: scenarioItem[];
  @Input() selectedItem: scenarioItem;
  @Input() mode: string;
  @ViewChild('itemCard', { read: ElementRef }) itemCard: ElementRef<HTMLInputElement>;
  @ViewChild('itemTextarea', { read: ElementRef }) itemTextarea: ElementRef<HTMLInputElement>;

  item;
  constructor(
    private scenarioDesignerService: ScenarioDesignerService,
    private dialogService: DialogService
  ) {}

  ngOnInit(): void {
    this.item = this.dataList.find((item) => item.id === this.itemId);
    this.draggable = {
      data: this.item.id
    };

    this.scenarioDesignerService.scenarioDesignerItemsCommunication
      .pipe(takeUntil(this.destroy))
      .subscribe((evt) => {
        if (evt.type == 'focusItem') this.focusItem(evt.item);
        if (evt.type == 'requireItemPosition') this.requireItemPosition(evt.item);
      });
  }

  ngOnChanges(): void {
    this.ngOnInit();
  }

  searchIntent(): void {
    const modal = this.dialogService.openDialog(IntentsSearchComponent, {
      context: {
        intentSentence: this.item.text
      }
    });
    const modalSubscription = modal.componentRef.instance.createNewIntentEvent
      .pipe(takeUntil(this.destroy))
      .subscribe((res) => {
        modal.close();
        this.createIntent();
      });
  }

  createIntent(): void {
    const modal = this.dialogService.openDialog(IntentCreateComponent, {
      context: {
        intentSentence: this.item.text
      }
    });
    const modalSubscription = modal.componentRef.instance.createIntentEvent
      .pipe(takeUntil(this.destroy))
      .subscribe((res) => {
        modal.close();
        this.editIntent();
      });
  }

  editIntent(): void {
    const modal = this.dialogService.openDialog(IntentEditComponent, {
      context: {}
    });
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
    let classes = this.item.from;
    if (this.item.from == 'bot') {
      if (this.item.final) classes += ' final';
    }
    if (this.item.parentIds?.length > 1) classes += ' duplicate';
    if (this.selectedItem?.id == this.item.id) classes += ' selected';
    return classes;
  }

  switchItemType(which): void {
    if (which == 'client') {
      this.item.from = 'client';
    }
    if (which == 'bot') {
      this.item.from = 'bot';
    }
    if (which == 'verification') {
      this.item.from = 'verification';
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

  ngOnDestroy() {
    this.destroy.next();
    this.destroy.complete();
  }
}

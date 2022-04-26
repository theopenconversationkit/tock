import { Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { EditorServiceService } from './editor-service.service';
import { storyCollectorItem } from './story-collector.types';

@Component({
  selector: 'app-editor-entry',
  templateUrl: './editor-entry.component.html',
  styleUrls: ['./editor-entry.component.scss']
})
export class EditorEntryComponent implements OnInit {
  destroy = new Subject();
  @Input() itemId: number;
  @Input() parentId: number;
  @Input() dataList: storyCollectorItem[];
  @Input() selectedItem: storyCollectorItem;
  @ViewChild('itemCard', { read: ElementRef }) itemCard: ElementRef<HTMLInputElement>;
  @ViewChild('itemTextarea', { read: ElementRef }) itemTextarea: ElementRef<HTMLInputElement>;

  item;
  constructor(private editorService: EditorServiceService) {}

  ngOnInit(): void {
    this.item = this.dataList.find((item) => item.id === this.itemId);
    this.draggable = {
      data: this.item.id
    };

    this.editorService.editorItemsCommunication.pipe(takeUntil(this.destroy)).subscribe((evt) => {
      if (evt.type == 'focusItem') this.focusItem(evt.item);
      if (evt.type == 'requireItemPosition') this.requireItemPosition(evt.item);
    });
  }

  ngOnChanges(): void {
    this.ngOnInit();
  }

  selectItem(): void {
    this.editorService.selectItem(this.item);
  }

  focusItem(item: storyCollectorItem): void {
    if (item.id == this.item.id) {
      this.itemTextarea.nativeElement.focus();
    }
  }

  requireItemPosition(item: storyCollectorItem): void {
    if (item.id == this.item.id) {
      this.editorService.exposeItemPosition(this.item, {
        left: this.itemCard.nativeElement.offsetLeft,
        top: this.itemCard.nativeElement.offsetTop,
        width: this.itemCard.nativeElement.offsetWidth,
        height: this.itemCard.nativeElement.offsetHeight
      });
    }
  }

  test(): void {
    this.editorService.testItem(this.item);
  }

  getChildItems(): storyCollectorItem[] {
    return this.dataList.filter((item) => item.parentIds?.includes(this.item.id));
  }

  itemHasSeveralChildren(): boolean {
    return this.getChildItems().length > 1;
  }

  answering(): void {
    this.editorService.addAnswer(this.item);
  }

  delete(): void {
    this.editorService.deleteAnswer(this.item, this.parentId);
  }

  getItemCardCssClass(): string {
    let classes = this.item.from;
    if (this.item.from == 'bot') {
      if (this.item.botAnswerType == 'final') classes += ' final';
    }
    if (this.item.parentIds?.length > 1) classes += ' duplicate';
    if (this.selectedItem?.id == this.item.id) classes += ' selected';
    return classes;
  }

  switchItemType(which): void {
    if (which == 'client') {
      this.item.from = 'client';
      delete this.item.botAnswerType;
    }
    if (which == 'bot') {
      this.item.from = 'bot';
      this.item.botAnswerType = 'question';
    }
    if (which == 'verification') {
      this.item.from = 'verification';
      delete this.item.botAnswerType;
    }
  }

  toggleFinal($event): void {
    console.log($event);
    if ($event) this.item.botAnswerType = 'final';
    else this.item.botAnswerType = 'question';
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
    return !this.item.botAnswerType || this.item.botAnswerType != 'final';
  }

  draggable;

  onDrop($event): void {
    if (this.item.id == $event.data) return;
    this.editorService.itemDropped(this.item.id, $event.data);
  }

  ngOnDestroy() {
    this.destroy.next();
    this.destroy.complete();
  }
}

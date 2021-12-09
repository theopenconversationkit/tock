import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

export type BatchActionName = 'validate' | 'unknown' | 'delete';

@Component({
  selector: 'tock-train-toolbar',
  templateUrl: './train-toolbar.component.html',
  styleUrls: ['./train-toolbar.component.scss']
})
export class TrainToolbarComponent implements OnInit {

  @Input()
  allChecked: boolean;

  @Input()
  disabled: boolean;

  @Output()
  onToggleSelectAll = new EventEmitter<boolean>();

  @Output()
  onBatchAction = new EventEmitter<BatchActionName>();

  constructor() { }

  ngOnInit(): void {
  }

  onSelectAll(value: boolean): void {
    this.onToggleSelectAll.emit(value);
  }

  validateAll(): void {
    this.onBatchAction.emit('validate');
  }

  unknownAll(): void {
    this.onBatchAction.emit('unknown');
  }

  deleteAll(): void {
    this.onBatchAction.emit('delete');
  }

}

import { Component, EventEmitter, Input, Output } from '@angular/core';

import { CanvaAction, ZoomValue } from '../models';

@Component({
  selector: 'tock-bottom-actions',
  templateUrl: './bottom-actions.component.html',
  styleUrls: ['./bottom-actions.component.scss']
})
export class BottomActionsComponent {
  @Input() isFullscreen!: boolean;
  @Input() hideActions: Array<CanvaAction> = [];

  @Output() onFullscreen = new EventEmitter<boolean>();
  @Output() onResetPosition = new EventEmitter<boolean>();
  @Output() onZoom = new EventEmitter<ZoomValue>();

  readonly zoomValue: typeof ZoomValue = ZoomValue;
  readonly action: typeof CanvaAction = CanvaAction;

  fullscreen(value: boolean): void {
    this.onFullscreen.emit(value);
  }

  resetPosition(): void {
    this.onResetPosition.emit(true);
  }

  zoom(value: ZoomValue): void {
    this.onZoom.emit(value);
  }
}

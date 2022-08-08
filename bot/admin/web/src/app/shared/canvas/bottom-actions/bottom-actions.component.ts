import { Component, EventEmitter, Input, Output } from '@angular/core';

import { ZoomValue } from '../models/zoomInOut.model';

@Component({
  selector: 'tock-bottom-actions',
  templateUrl: './bottom-actions.component.html',
  styleUrls: ['./bottom-actions.component.scss']
})
export class BottomActionsComponent {
  @Input() isFullscreen!: boolean;
  @Output() onFullscreen = new EventEmitter<boolean>();
  @Output() onZoom = new EventEmitter<ZoomValue>();

  readonly zoomValue: typeof ZoomValue = ZoomValue;

  fullscreen(value: boolean): void {
    this.onFullscreen.emit(value);
  }

  zoom(value: ZoomValue): void {
    this.onZoom.emit(value);
  }
}

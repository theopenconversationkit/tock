import { Component, EventEmitter, Output } from '@angular/core';

import { ZoomValue } from '../models/zoomInOut.model';

@Component({
  selector: 'tock-zoom-in-out',
  templateUrl: './zoom-in-out.component.html',
  styleUrls: ['./zoom-in-out.component.scss']
})
export class ZoomInOutComponent {
  @Output() onZoom = new EventEmitter<ZoomValue>();

  readonly zoomValue: typeof ZoomValue = ZoomValue;

  zoom(value: ZoomValue): void {
    this.onZoom.emit(value);
  }
}

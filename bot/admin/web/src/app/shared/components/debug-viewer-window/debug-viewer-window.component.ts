import { Component, Input } from '@angular/core';

@Component({
  selector: 'tock-debug-viewer-window',
  templateUrl: './debug-viewer-window.component.html',
  styleUrls: ['./debug-viewer-window.component.scss']
})
export class DebugViewerWindowComponent {
  @Input() debug?: any;
}

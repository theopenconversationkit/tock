import { Component, Input, OnInit } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';

@Component({
  selector: 'tock-debug-viewer',
  templateUrl: './debug-viewer.component.html',
  styleUrls: ['./debug-viewer.component.scss']
})
export class DebugViewerComponent {
  @Input() debug?: any;

  constructor(public dialogRef: NbDialogRef<DebugViewerComponent>) {}

  cancel() {
    this.dialogRef.close();
  }
}

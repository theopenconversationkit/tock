import { Component, Input, OnInit } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';

@Component({
  selector: 'tock-debug-viewer-dialog',
  templateUrl: './debug-viewer-dialog.component.html',
  styleUrls: ['./debug-viewer-dialog.component.scss']
})
export class DebugViewerDialogComponent {
  @Input() debug?: any;
  @Input() title?: string = 'Debug infos';

  constructor(public dialogRef: NbDialogRef<DebugViewerDialogComponent>) {}

  cancel() {
    this.dialogRef.close();
  }
}

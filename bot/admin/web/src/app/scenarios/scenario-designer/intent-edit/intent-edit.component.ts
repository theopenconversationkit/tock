import { Component, EventEmitter, Inject, Input, OnInit, Output } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';

@Component({
  selector: 'scenario-intent-edit',
  templateUrl: './intent-edit.component.html',
  styleUrls: ['./intent-edit.component.scss']
})
export class IntentEditComponent implements OnInit {
  constructor(public dialogRef: NbDialogRef<IntentEditComponent>) {}

  ngOnInit(): void {}

  cancel(): void {
    this.dialogRef.close();
  }
}

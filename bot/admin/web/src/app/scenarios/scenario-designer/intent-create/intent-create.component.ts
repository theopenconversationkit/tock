import { Component, Inject, Input, OnInit } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';

@Component({
  selector: 'scenario-intent-create',
  templateUrl: './intent-create.component.html',
  styleUrls: ['./intent-create.component.scss']
})
export class IntentCreateComponent implements OnInit {
  @Input() intentSentence: string;
  constructor(public dialogRef: NbDialogRef<IntentCreateComponent>) {}

  ngOnInit(): void {}

  cancel() {
    this.dialogRef.close();
  }
}

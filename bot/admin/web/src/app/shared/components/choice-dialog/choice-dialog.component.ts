import { Component, Input, OnInit } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';

@Component({
  selector: 'tock-choice-dialog',
  templateUrl: './choice-dialog.component.html',
  styleUrls: ['./choice-dialog.component.scss']
})
export class ChoiceDialogComponent implements OnInit {
  @Input() modalStatus: string = 'primary';
  @Input() title: string;
  @Input() subtitle: string;
  @Input() list?: string[];
  @Input() cancellable: boolean = true;
  @Input() actions: { actionName: string; buttonStatus?: string; ghost?: boolean }[];

  constructor(public dialogRef: NbDialogRef<ChoiceDialogComponent>) {}

  ngOnInit() {
    this.actions?.forEach((actionDef) => {
      if (!actionDef.buttonStatus) actionDef.buttonStatus = 'primary';
      if (actionDef.ghost == null) actionDef.ghost = false;
    });
  }
}

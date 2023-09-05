import { Component, Input, OnInit } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';

@Component({
  selector: 'tock-nlp-stats-display',
  templateUrl: './nlp-stats-display.component.html',
  styleUrls: ['./nlp-stats-display.component.scss']
})
export class NlpStatsDisplayComponent {
  @Input() data;

  constructor(public dialogRef: NbDialogRef<NlpStatsDisplayComponent>) {}

  cancel() {
    this.dialogRef.close();
  }
}

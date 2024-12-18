import { Component, Input, OnInit } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { StateService } from '../../../../core-nlp/state.service';
import { flatMap } from '../../../../model/commons';

@Component({
  selector: 'tock-sentence-review-request',
  templateUrl: './sentence-review-request.component.html',
  styleUrls: ['./sentence-review-request.component.scss']
})
export class SentenceReviewRequestComponent implements OnInit {
  @Input() beforeClassification;
  @Input() reviewComment;

  description: string;

  constructor(public dialogRef: NbDialogRef<SentenceReviewRequestComponent>, private state: StateService) {}

  ngOnInit(): void {
    if (this.reviewComment) {
      this.description = this.reviewComment;
    } else {
      this.state.currentIntentsCategories.subscribe((c) => {
        let intent = flatMap(c, (cat) => cat.intents).find((intent) => intent._id === this.beforeClassification);
        this.description = 'Initial intent: ' + (intent ? intent.name : '') + '\n\n';
      });
    }
  }

  save() {
    this.dialogRef.close({ status: 'confirm', description: this.description });
  }

  delete() {
    this.dialogRef.close({ status: 'delete' });
  }
}

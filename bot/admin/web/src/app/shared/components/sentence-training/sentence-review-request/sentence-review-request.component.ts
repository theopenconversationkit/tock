/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

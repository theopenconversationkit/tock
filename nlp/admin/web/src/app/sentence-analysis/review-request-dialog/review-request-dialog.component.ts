/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import { Component, Inject, Input, OnInit } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { StateService } from '../../core-nlp/state.service';
import { flatMap } from '../../model/commons';

@Component({
  selector: 'tock-create-entity-dialog',
  templateUrl: 'review-request-dialog.component.html',
  styleUrls: ['./review-request-dialog.component.css']
})
export class ReviewRequestDialogComponent implements OnInit {
  description: string;
  @Input() beforeClassification: string;
  @Input() reviewComment: string;

  constructor(
    public dialogRef: NbDialogRef<ReviewRequestDialogComponent>,
    private state: StateService
  ) {}

  ngOnInit() {
    if (this.reviewComment) {
      this.description = this.reviewComment;
    } else {
      this.state.currentIntentsCategories.subscribe((c) => {
        let intent = flatMap(c, (cat) => cat.intents).find(
          (intent) => intent._id === this.beforeClassification
        );
        this.description = 'Initial intent: ' + (intent ? intent.name : '') + '\n\n';
      });
    }
  }

  save() {
    this.dialogRef.close({ status: 'confirm', description: this.description });
  }
}

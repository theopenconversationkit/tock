/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Component, Inject, OnInit} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {StateService} from "../../core-nlp/state.service";
import {flatMap} from "../../model/commons";

@Component({
  selector: 'tock-create-entity-dialog',
  templateUrl: 'review-request-dialog.component.html'
})
export class ReviewRequestDialogComponent implements OnInit {
  description: string;
  beforeClassification: string;
  reviewComment: string;

  constructor(public dialogRef: MatDialogRef<ReviewRequestDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private state: StateService) {
    this.beforeClassification = data.beforeClassification;
    this.reviewComment = data.reviewComment
  }

  ngOnInit() {
    if (this.reviewComment) {
      this.description = this.reviewComment
    } else {
      this.state.currentIntentsCategories.subscribe(c => {
        let intent = flatMap(c, cat => cat.intents).find(intent => intent._id === this.beforeClassification);
        this.description = "Before classification : " + intent.name + "\n\n";
      });
    }
  }

  save() {
    this.dialogRef.close({status: 'confirm',  description: this.description});
  }
}

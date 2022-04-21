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

import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { StateService } from '../../core-nlp/state.service';
import { StoryStep } from '../model/story';

@Component({
  selector: 'tock-step-dialog',
  templateUrl: './step-dialog.component.html',
  styleUrls: ['./step-dialog.component.css']
})
export class StepDialogComponent {
  steps: StoryStep[];
  defaultCategory: string;

  constructor(
    public dialogRef: MatDialogRef<StepDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public state: StateService
  ) {
    this.defaultCategory = this.data.category;
    this.steps = this.data.steps
      ? this.data.steps.slice(0).map((a) => {
          let newA = a.clone();
          newA.intentDefinition = this.state.findIntentByName(a.intent.name);
          return newA;
        })
      : [];
  }

  cancel() {
    this.dialogRef.close({});
  }

  save() {
    this.dialogRef.close({
      steps: StoryStep.filterNew(this.steps)
    });
  }
}

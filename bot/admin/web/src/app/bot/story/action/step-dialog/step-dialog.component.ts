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

import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';

import { StateService } from '../../../../core-nlp/state.service';
import { StoryStep } from '../../../model/story';

@Component({
  selector: 'tock-step-dialog',
  templateUrl: './step-dialog.component.html',
  styleUrls: ['./step-dialog.component.scss']
})
export class StepDialogComponent implements OnChanges {
  @Input() steps: StoryStep[];
  @Input() defaultCategory: string;

  constructor(private nbDialogRef: NbDialogRef<StepDialogComponent>, public stateService: StateService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.steps.currentValue) {
      const steps = changes.steps.currentValue;
      this.steps = steps
        ? steps.slice(0).map((a) => {
            let newA = a.clone();
            newA.intentDefinition = this.stateService.findIntentByName(a.intent.name);
            return newA;
          })
        : [];
    }
  }

  cancel() {
    this.nbDialogRef.close({});
  }

  save() {
    this.nbDialogRef.close({
      steps: StoryStep.filterNew(this.steps)
    });
  }
}

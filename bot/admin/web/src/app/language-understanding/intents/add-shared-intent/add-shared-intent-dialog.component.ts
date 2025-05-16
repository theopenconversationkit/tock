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

import { Component, Input } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { StateService } from 'src/app/core-nlp/state.service';
import { Intent } from 'src/app/model/nlp';

@Component({
  selector: 'tock-add-shared-intent-dialog',
  templateUrl: './add-shared-intent-dialog.component.html',
  styleUrls: ['./add-shared-intent-dialog.component.css']
})
export class AddSharedIntentDialogComponent {
  name: string;
  @Input() title: string;

  selectedIntent: Intent;

  constructor(public state: StateService, public dialogRef: NbDialogRef<AddSharedIntentDialogComponent>) {}

  save() {
    this.dialogRef.close({ intent: this.selectedIntent });
  }
}

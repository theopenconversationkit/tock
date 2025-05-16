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

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
import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';

import { BotIdentity } from '../../models/dashboard.model';

@Component({
  selector: 'tock-bot-identity-edit',
  templateUrl: './bot-identity-edit.component.html',
  styleUrls: ['./bot-identity-edit.component.scss'],
  standalone: false
})
export class BotIdentityEditComponent implements OnInit {
  @Input() identity: BotIdentity;
  @Input() technicalName: string;

  @Output() onSave = new EventEmitter<BotIdentity>();

  readonly dialogRef = inject(NbDialogRef<BotIdentityEditComponent>);

  form = new FormGroup({
    displayName: new FormControl('', { nonNullable: true }),
    notes: new FormControl('', { nonNullable: true })
  });

  ngOnInit(): void {
    this.form.patchValue({
      displayName: this.identity?.displayName ?? '',
      notes: this.identity?.notes ?? ''
    });
  }

  save(): void {
    this.onSave.emit({
      ...this.identity,
      ...this.form.getRawValue()
    });
    this.dialogRef.close();
  }

  cancel(): void {
    this.dialogRef.close();
  }
}

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

import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { Subject, takeUntil } from 'rxjs';
import { normalizedSnakeCase } from '../../../shared/utils/strings.utils';

export interface CreateNamespaceData {
  label: string;
  name: string;
}

@Component({
  selector: 'tock-create-namespace',
  templateUrl: './create-namespace.component.html',
  styleUrls: ['./create-namespace.component.scss']
})
export class CreateNamespaceComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  private technicalNameManuallyEdited = false;

  @Output() validate = new EventEmitter<CreateNamespaceData>();

  constructor(private dialogRef: NbDialogRef<CreateNamespaceComponent>) {}

  form: FormGroup = new FormGroup({
    label: new FormControl<string>('', [Validators.required, this.noWhitespaceValidator]),
    name: new FormControl<string>('', [Validators.required, this.noWhitespaceValidator])
  });

  ngOnInit(): void {
    this.label.valueChanges.pipe(takeUntil(this.destroy$)).subscribe((value) => {
      if (!this.technicalNameManuallyEdited) {
        this.name.setValue(this.buildTechnicalName(value || ''), { emitEvent: false });
      }
    });

    this.name.valueChanges.pipe(takeUntil(this.destroy$)).subscribe((value) => {
      this.technicalNameManuallyEdited = value !== this.buildTechnicalName(this.label.value || '');
    });
  }

  public noWhitespaceValidator(control: FormControl) {
    return (control.value || '').trim().length ? null : { custom: 'Only whitespace characters are not allowed' };
  }

  private buildTechnicalName(value: string): string {
    return normalizedSnakeCase(value).toLowerCase();
  }

  isSubmitted: boolean = false;

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  get label(): FormControl {
    return this.form.get('label') as FormControl;
  }

  get name(): FormControl {
    return this.form.get('name') as FormControl;
  }

  save(): void {
    this.isSubmitted = true;
    if (this.canSave) {
      this.validate.emit({
        label: (this.label.value || '').trim(),
        name: (this.name.value || '').trim()
      });
    }
  }

  cancel(): void {
    this.dialogRef.close();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

import { Component, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { Subscription } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

import { EditUtteranceResult } from './edit-utterance-result';
import { Utterance } from '../../model/utterance';

/**
 * Edit Utterance DIALOG
 */
@Component({
  selector: 'tock-edit-utterance',
  templateUrl: './edit-utterance.component.html',
  styleUrls: ['./edit-utterance.component.scss']
})
export class EditUtteranceComponent implements OnInit, OnDestroy {

  @Input()
  public title: string;

  @Input()
  public value: string;

  @Input()
  public lookup?: (v: string) => (Utterance | null);

  @Input()
  public mode?: string;

  @Output()
  public saveAction?: (string) => void;

  public existingQuestion?: string;

  public form = new FormGroup({
    utterance: new FormControl('', [Validators.required, Validators.maxLength(260)])
  });

  public isSubmitted = false;

  private subscriptions = new Subscription();

  get utterance(): FormControl {
    return this.form.get('utterance') as FormControl;
  }

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  constructor(
    private readonly dialogRef: NbDialogRef<EditUtteranceComponent>
  ) {}

  ngOnInit() {
    this.utterance.patchValue(this.value);

    this.subscriptions.add(
      this.utterance.valueChanges
        .pipe(debounceTime(500))
        .subscribe(v => {
          this.ensureUniq(v);
        })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  cancel(): void {
    const result: EditUtteranceResult = {
      cancelled: true
    };
    this.dialogRef.close(result);
  }

  saveAndClose(): void {
    this.isSubmitted = true;

    if (this.canSave) {
      const result: EditUtteranceResult = {
        cancelled: false,
        value: this.utterance.value
      };
      this.dialogRef.close(result);
    }
  }

  save(): void {
    this.isSubmitted = true;

    if (this.canSave) {
      this.saveAction(this.utterance.value);
      this.utterance.patchValue('');
      this.isSubmitted = false;
    }
  }

  ensureUniq(evt: string): void {
    const res = this.lookup ? this.lookup(evt) : undefined; // look for similar question
    if (res) {
      this.existingQuestion = res;
    } else {
      this.existingQuestion = undefined;
    }
  }
}

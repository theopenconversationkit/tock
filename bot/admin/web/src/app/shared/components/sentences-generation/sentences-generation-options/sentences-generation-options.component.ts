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

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { SentencesGenerationOptions } from './../models';

@Component({
  selector: 'tock-sentences-generation-options',
  templateUrl: './sentences-generation-options.component.html',
  styleUrls: ['./sentences-generation-options.component.scss']
})
export class SentencesGenerationOptionsComponent implements OnInit {
  @Input() sentences: string[] = [];
  @Input() options!: Partial<SentencesGenerationOptions>;

  @Output() onOptionsUpdate = new EventEmitter<Partial<SentencesGenerationOptions>>();

  showAlert: boolean = true;

  maxTemperature: number = 1;

  ngOnInit(): void {
    this.form.patchValue({ ...this.options });

    this.form.valueChanges.subscribe((value) => {
      this.onOptionsUpdate.emit(this.form.value);
    });
  }

  sentencesExampleMax: number = 5;

  form = new FormGroup({
    spellingMistakes: new FormControl(false),
    smsLanguage: new FormControl(false),
    abbreviatedLanguage: new FormControl(false),
    llmTemperature: new FormControl(0.5),
    sentencesExample: new FormControl([], [Validators.maxLength(this.sentencesExampleMax)])
  });

  get spellingMistakes(): FormControl {
    return this.form.get('spellingMistakes') as FormControl;
  }

  get smsLanguage(): FormControl {
    return this.form.get('smsLanguage') as FormControl;
  }

  get abbreviatedLanguage(): FormControl {
    return this.form.get('abbreviatedLanguage') as FormControl;
  }

  get llmTemperature(): FormControl {
    return this.form.get('llmTemperature') as FormControl;
  }

  get sentencesExample(): FormControl {
    return this.form.get('sentencesExample') as FormControl;
  }

  get canGenerate(): boolean {
    return this.form.valid && this.sentencesExample.value.length;
  }

  closeAlert(): void {
    this.showAlert = false;
  }
}

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
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';

interface CreateFeatureForm {
  name: FormControl<string>;
  category: FormControl<string>;
  enabled: FormControl<boolean>;
  startDate: FormControl<Date>;
  endDate: FormControl<Date>;
  graduation: FormControl<number>;
}

@Component({
  selector: 'tock-create-feature',
  templateUrl: './create-feature.component.html',
  styleUrls: ['./create-feature.component.scss']
})
export class CreateFeatureComponent implements OnInit {
  isSubmitted = false;

  botApplicationConfigurationId: string;

  @Input() type: 'tock' | 'application';

  @Output() onSave = new EventEmitter();

  constructor(private nbDialogRef: NbDialogRef<CreateFeatureComponent>) {}

  ngOnInit(): void {
    if (this.type === 'tock') {
      this.form.patchValue({ category: 'tock' });
      this.category.disable();
    }

    if (this.type === 'application') {
      this.form.patchValue({ category: 'myCategory' });
    }
  }

  form = new FormGroup<CreateFeatureForm>({
    name: new FormControl('', [Validators.required, Validators.maxLength(50)]),
    category: new FormControl(''),
    enabled: new FormControl(true),
    startDate: new FormControl(undefined),
    endDate: new FormControl(undefined),
    graduation: new FormControl(undefined)
  });

  get name(): FormControl {
    return this.form.get('name') as FormControl;
  }

  get category(): FormControl {
    return this.form.get('category') as FormControl;
  }

  get enabled(): FormControl {
    return this.form.get('enabled') as FormControl;
  }

  get startDate(): FormControl {
    return this.form.get('startDate') as FormControl;
  }

  get endDate(): FormControl {
    return this.form.get('endDate') as FormControl;
  }

  get graduation(): FormControl {
    return this.form.get('graduation') as FormControl;
  }

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  cancel(): void {
    this.nbDialogRef.close({});
  }

  save(): void {
    this.isSubmitted = true;

    if (this.canSave) {
      let form = this.form.getRawValue();
      this.onSave.emit({ ...form, botApplicationConfigurationId: this.botApplicationConfigurationId });
      this.cancel();
    }
  }
}

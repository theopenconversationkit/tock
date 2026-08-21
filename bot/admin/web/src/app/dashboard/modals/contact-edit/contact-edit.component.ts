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
import { NbDialogRef } from '@nebular/theme';

import { BotContact, CONTACT_ROLE_SUGGESTIONS } from '../../models/dashboard.model';

interface ContactForm {
  role: FormControl<string>;
  name: FormControl<string>;
  email: FormControl<string>;
  link: FormControl<string>;
  note: FormControl<string>;
  comment: FormControl<string>;
}

@Component({
  selector: 'tock-contact-edit',
  templateUrl: './contact-edit.component.html',
  styleUrls: ['./contact-edit.component.scss'],
  standalone: false
})
export class ContactEditComponent implements OnInit {
  /** Undefined when adding. */
  @Input() contact?: BotContact;

  @Output() onSave = new EventEmitter<BotContact>();
  @Output() onDelete = new EventEmitter<BotContact>();

  readonly roleSuggestions = CONTACT_ROLE_SUGGESTIONS;

  confirmDelete: boolean = false;

  form = new FormGroup<ContactForm>({
    role: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    name: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    email: new FormControl('', { nonNullable: true, validators: [Validators.email] }),
    link: new FormControl('', { nonNullable: true }),
    note: new FormControl('', { nonNullable: true }),
    comment: new FormControl('', { nonNullable: true })
  });

  constructor(public dialogRef: NbDialogRef<ContactEditComponent>) {}

  ngOnInit(): void {
    if (this.contact) {
      this.form.patchValue({
        role: this.contact.role ?? '',
        name: this.contact.name ?? '',
        email: this.contact.email ?? '',
        link: this.contact.link ?? '',
        note: this.contact.note ?? '',
        comment: this.contact.comment ?? ''
      });
    }
  }

  get isEdit(): boolean {
    return !!this.contact;
  }

  get canSave(): boolean {
    return this.form.valid;
  }

  save(): void {
    if (!this.canSave) {
      this.form.markAllAsTouched();
      return;
    }

    this.onSave.emit({
      id: this.contact?.id,
      ...this.form.getRawValue()
    } as BotContact);

    this.dialogRef.close();
  }

  askDelete(): void {
    this.confirmDelete = true;
  }

  cancelDelete(): void {
    this.confirmDelete = false;
  }

  remove(): void {
    this.onDelete.emit(this.contact);
    this.dialogRef.close();
  }

  cancel(): void {
    this.dialogRef.close();
  }
}

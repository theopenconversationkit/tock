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
import { FormControl, FormGroup } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';

import { IngestionNotes } from '../../models/dashboard.model';

@Component({
  selector: 'tock-ingestion-notes',
  templateUrl: './ingestion-notes.component.html',
  styleUrls: ['./ingestion-notes.component.scss'],
  standalone: false
})
export class IngestionNotesComponent implements OnInit {
  @Input() notes: IngestionNotes;

  @Output() onSave = new EventEmitter<IngestionNotes>();

  form = new FormGroup({
    text: new FormControl('', { nonNullable: true })
  });

  constructor(public dialogRef: NbDialogRef<IngestionNotesComponent>) {}

  ngOnInit(): void {
    this.form.patchValue({ text: this.notes?.text ?? '' });
  }

  save(): void {
    this.onSave.emit({
      ...this.notes,
      text: this.form.controls.text.value
    });
    this.dialogRef.close();
  }

  cancel(): void {
    this.dialogRef.close();
  }
}

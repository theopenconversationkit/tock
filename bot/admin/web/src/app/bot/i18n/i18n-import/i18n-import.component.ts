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

import { Component, EventEmitter, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { FileValidators } from '../../../shared/validators';
import { NbDialogRef, NbToastrService } from '@nebular/theme';
import { RestService } from '../../../core-nlp/rest/rest.service';

@Component({
  selector: 'tock-i18n-import-action',
  templateUrl: './i18n-import.component.html',
  styleUrls: ['./i18n-import.component.scss']
})
export class I18nImportComponent {
  fileFormatErrorMessage: string;

  isImportSubmitted: boolean = false;

  uploading: boolean = false;

  @Output() onUploadComplete = new EventEmitter();

  constructor(public dialogRef: NbDialogRef<I18nImportComponent>, private rest: RestService, private toastrService: NbToastrService) {}

  form: FormGroup = new FormGroup({
    file: new FormControl<File[]>([], [Validators.required, FileValidators.mimeTypeSupported(['application/json', 'text/csv'])])
  });

  get file(): FormControl {
    return this.form.get('file') as FormControl;
  }

  get canSaveImport(): boolean {
    return this.isImportSubmitted ? this.form.valid : this.form.dirty;
  }

  import(): void {
    this.isImportSubmitted = true;
    this.fileFormatErrorMessage = undefined;

    if (this.canSaveImport) {
      this.uploading = true;

      const file = this.file.value[0];

      let url;
      if (file.type === 'application/json') {
        url = '/i18n/import/json';
      }

      if (file.type === 'text/csv') {
        url = '/i18n/import/csv';
      }

      const formData = new FormData();

      formData.append('file', file);

      this.rest.postFormData(url, formData, null, true).subscribe({
        next: (nbLabelsImported: number) => {
          if (nbLabelsImported > 0) {
            this.toastrService.success(nbLabelsImported + ' labels have been created or updated.', 'Labels Imported', {
              duration: 5000
            });

            this.onUploadComplete.emit();
          } else {
            this.toastrService.danger('No label created or updated: file might be empty or no label is validated.', 'No Label Imported', {
              duration: 5000
            });
          }
          this.uploading = false;
          this.dialogRef.close();
        },
        error: (error) => {
          this.toastrService.danger(
            'The imported file has caused an error. Be sure to provide a valid dump file of answers labels.',
            'An error occured',
            {
              duration: 5000
            }
          );

          this.uploading = false;
        }
      });
    }
  }

  cancel(): void {
    this.dialogRef.close();
  }
}

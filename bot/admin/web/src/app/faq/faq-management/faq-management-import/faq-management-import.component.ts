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

import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef, NbToastrService } from '@nebular/theme';
import { TranslocoService } from '@jsverse/transloco';

import { RestService } from '../../../core-nlp/rest/rest.service';
import { StateService } from '../../../core-nlp/state.service';
import { readFileAsText } from '../../../shared/utils';
import { FileValidators } from '../../../shared/validators';
import { FaqDefinition } from '../../models';

@Component({
  selector: 'tock-faq-management-import',
  templateUrl: './faq-management-import.component.html',
  standalone: false
})
export class FaqManagementImportComponent {
  readonly dialogRef = inject<NbDialogRef<FaqManagementImportComponent>>(NbDialogRef);
  private readonly rest = inject(RestService);
  private readonly stateService = inject(StateService);
  private readonly toastrService = inject(NbToastrService);
  private readonly transloco = inject(TranslocoService);

  loading = false;
  isSubmitted = false;
  fileFormatError = false;

  form = new FormGroup({
    file: new FormControl<File[]>([], {
      nonNullable: true,
      validators: [Validators.required, FileValidators.extensionSupported(['json'])]
    })
  });

  get file(): FormControl<File[]> {
    return this.form.controls.file;
  }

  import(): void {
    this.isSubmitted = true;
    this.fileFormatError = false;

    if (this.form.invalid) return;

    readFileAsText(this.file.value[0]).then((fileContent) => {
      try {
        const faqs = JSON.parse(fileContent.data);

        if (!this.isFaqExport(faqs)) {
          this.fileFormatError = true;
          return;
        }

        this.postData(faqs);
      } catch (_) {
        this.fileFormatError = true;
      }
    });
  }

  private isFaqExport(data: unknown): data is FaqDefinition[] {
    return (
      Array.isArray(data) &&
      data.length > 0 &&
      data.every(
        (faq) =>
          typeof faq?.title === 'string' &&
          typeof faq?.intentName === 'string' &&
          Array.isArray(faq?.utterances) &&
          Array.isArray(faq?.answer?.i18n)
      )
    );
  }

  private postData(faqs: FaqDefinition[]): void {
    this.loading = true;
    this.rest.post<FaqDefinition[], number>(`/faq/import/${this.stateService.currentApplication._id}`, faqs).subscribe({
      next: (count) => {
        this.toastrService.success(
          this.transloco.translate('faq.faq-management-import.successMessage', { count }),
          this.transloco.translate('common.messages.success'),
          { duration: 5000, status: 'success' }
        );
        this.loading = false;
        this.dialogRef.close(count);
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}

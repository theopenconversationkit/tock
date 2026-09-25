import { Component, EventEmitter, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { FileValidators } from '../../../shared/validators';
import { NbDialogRef, NbToastrService } from '@nebular/theme';
import { RestService } from '../../../core-nlp/rest/rest.service';
import { TranslocoService } from '@jsverse/transloco';

@Component({
    selector: 'tock-i18n-import-action',
    templateUrl: './i18n-import.component.html',
    styleUrls: ['./i18n-import.component.scss'],
    standalone: false
})
export class I18nImportComponent {
  fileFormatErrorMessage: string;

  isImportSubmitted: boolean = false;

  uploading: boolean = false;

  @Output() onUploadComplete = new EventEmitter();

  constructor(
    public dialogRef: NbDialogRef<I18nImportComponent>,
    private rest: RestService,
    private toastrService: NbToastrService,
    private transloco: TranslocoService
  ) {}

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
            this.toastrService.success(
              this.transloco.translate('bot.i18n-import.successMessage', { count: nbLabelsImported }),
              this.transloco.translate('bot.i18n-import.successTitle'),
              { duration: 5000 }
            );
            this.onUploadComplete.emit();
          } else {
            this.toastrService.danger(
              this.transloco.translate('bot.i18n-import.noLabelImportedMessage'),
              this.transloco.translate('bot.i18n-import.noLabelImportedTitle'),
              { duration: 5000 }
            );
          }
          this.uploading = false;
          this.dialogRef.close();
        },
        error: (error) => {
          this.toastrService.danger(
            this.transloco.translate('bot.i18n-import.errorMessage'),
            this.transloco.translate('bot.i18n-import.errorTitle'),
            { duration: 5000 }
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

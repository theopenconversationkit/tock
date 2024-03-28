import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { AbstractControl, AsyncValidatorFn, FormControl, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { readFileAsText } from '../../../shared/utils';
import { FileValidators } from '../../../shared/validators';
import { Source, SourceTypes } from '../models';
import Papa from 'papaparse';

@Component({
  selector: 'tock-source-import',
  templateUrl: './source-import.component.html',
  styleUrls: ['./source-import.component.scss']
})
export class SourceImportComponent {
  @Input() source?: Source;

  @Output() onImport = new EventEmitter();

  sourceTypes = SourceTypes;
  isImportSubmitted: boolean = false;
  uploading: boolean = false;

  constructor(public dialogRef: NbDialogRef<SourceImportComponent>) {}

  importForm: FormGroup = new FormGroup({
    filesSources: new FormControl<File[]>([], [Validators.required, FileValidators.mimeTypeSupported(['text/csv', 'application/json'])])
  });

  get filesSources(): FormControl {
    return this.importForm.get('filesSources') as FormControl;
  }

  get canSaveImport(): boolean {
    return this.isImportSubmitted ? this.importForm.valid : this.importForm.dirty;
  }

  uploadProgress: number = 0;

  import(): void {
    this.isImportSubmitted = true;
    if (this.canSaveImport) {
      const file = this.filesSources.value[0];

      if (file.type === 'text/csv') {
        this.uploading = true;
        const data = [];
        Papa.parse(file, {
          header: false,
          worker: true,
          skipEmptyLines: 'greedy',
          step: (row) => {
            this.uploadProgress++;
            data.push(row.data);
          },
          complete: () => {
            this.onImport.emit({ fileFormat: 'csv', data });
            this.cancel();
          }
        });
      }

      if (file.type === 'application/json') {
        readFileAsText(file).then((fileContent) => {
          const data = JSON.parse(fileContent.data);
          this.onImport.emit({ fileFormat: 'json', data });
          this.cancel();
        });
      }
    }
  }

  cancel(): void {
    this.dialogRef.close();
  }
}

import { Component, EventEmitter, OnDestroy, Output } from '@angular/core';
import { AbstractControl, AsyncValidatorFn, FormControl, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { Subject } from 'rxjs';

import { readFileAsText } from '../../commons/utils';
import { FileValidators } from '../../../shared/validators';

@Component({
  selector: 'tock-scenario-import',
  templateUrl: './scenario-import.component.html',
  styleUrls: ['./scenario-import.component.scss']
})
export class ScenarioImportComponent implements OnDestroy {
  destroy = new Subject();

  @Output() validate = new EventEmitter<JSON[]>();

  constructor(public dialogRef: NbDialogRef<ScenarioImportComponent>) {}

  private filesToImport = [];

  filesInError = [];
  isImportSubmitted: boolean = false;
  importForm: FormGroup = new FormGroup({
    filesSources: new FormControl([], [Validators.required, FileValidators.typeSupported(['application/json'])], [this.checkFilesFormat()])
  });

  get filesSources(): FormControl {
    return this.importForm.get('filesSources') as FormControl;
  }

  get canSaveImport(): boolean {
    return this.isImportSubmitted ? this.importForm.valid : this.importForm.dirty;
  }

  importCancel() {
    this.filesInError = [];
    this.isImportSubmitted = false;
    this.importForm.reset();
    this.dialogRef.close();
  }

  checkFilesFormat(): AsyncValidatorFn {
    return async (control: AbstractControl): Promise<ValidationErrors> | null => {
      this.filesInError = [];
      const filesNameWithWrongFormat = [];
      const readers = [];

      control.value.forEach((file) => {
        readers.push(readFileAsText(file));
      });

      let jsons = [];
      await Promise.all(readers).then((values) => {
        values.forEach((result) => {
          if (typeof result.data === 'string') {
            let importJson;
            try {
              importJson = JSON.parse(result.data);
            } catch (error) {
              filesNameWithWrongFormat.push(result.fileName);
              return;
            }
            if (!importJson.data?.scenarioItems) {
              filesNameWithWrongFormat.push(result.fileName);
            } else {
              jsons.push(importJson);
            }
          } else {
            filesNameWithWrongFormat.push(result.fileName);
          }
        });
      });

      return new Promise((resolve) => {
        if (filesNameWithWrongFormat.length) {
          this.filesInError = [...filesNameWithWrongFormat];
          resolve({
            filesNameWithWrongFormat,
            message: jsons.length
              ? "A least one of the selected file has the wrong format and won't be imported. Please provide Json dump files from previous scenario exports."
              : 'None of the files provided had the required format. Please provide Json dump files from previous scenario exports.'
          });
        } else {
          this.filesToImport = [...jsons];
          resolve(null);
        }
      });
    };
  }

  importSubmit() {
    this.isImportSubmitted = true;

    if (this.canSaveImport && this.filesToImport) {
      this.validate.emit(this.filesToImport);
    }
  }

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }
}

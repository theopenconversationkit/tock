import { Component, EventEmitter, OnDestroy, Output } from '@angular/core';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { Subject } from 'rxjs';
import { readFileAsText } from '../../commons/utils';

@Component({
  selector: 'tock-scenario-import',
  templateUrl: './scenario-import.component.html',
  styleUrls: ['./scenario-import.component.scss']
})
export class ScenarioImportComponent implements OnDestroy {
  destroy = new Subject();

  @Output() validate = new EventEmitter<JSON[]>();

  constructor(public dialogRef: NbDialogRef<ScenarioImportComponent>) {}

  isImportSubmitted: boolean = false;
  importForm: FormGroup = new FormGroup({
    importFile: new FormControl('', [Validators.required]),
    filesSources: new FormArray([], [Validators.required])
  });

  get importFile(): FormControl {
    return this.importForm.get('importFile') as FormControl;
  }
  get filesSources(): FormArray {
    return this.importForm.get('filesSources') as FormArray;
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

  onFileChange(event: Event) {
    this.filesInError = [];
    const target = event.currentTarget as HTMLInputElement;
    if (target.files?.length) {
      const files: FileList = target.files;

      for (let index = 0; index < files.length; index++) {
        const file = files[index];
        if (file?.type !== 'application/json') {
          this.setWrongFileFormatError(file.name);
        } else {
          this.filesSources.push(new FormControl(file));
        }
      }
    }
  }

  filesInError = [];
  setWrongFileFormatError(filename) {
    this.importFile.setErrors({
      custom:
        "A least one of the selected file has the wrong format and won't be imported. Please provide Json dump files from previous scenario exports."
    });
    this.filesInError.push(filename);
  }

  importSubmit() {
    this.isImportSubmitted = true;
    if (this.canSaveImport) {
      let readers = [];

      this.filesSources.value.forEach((file) => {
        readers.push(readFileAsText(file));
      });

      Promise.all(readers).then((values) => {
        let jsons = [];
        values.forEach((result) => {
          if (typeof result.data === 'string') {
            let importJson;
            try {
              importJson = JSON.parse(result.data);
            } catch (error) {
              this.setWrongFileFormatError(result.fileName);
              return;
            }
            if (!importJson.data?.scenarioItems) {
              this.setWrongFileFormatError(result.fileName);
            } else {
              jsons.push(importJson);
            }
          } else {
            this.setWrongFileFormatError(result.fileName);
          }
        });

        if (jsons.length) {
          this.validate.emit(jsons);
        } else {
          this.importFile.setErrors({
            custom:
              'None of the files provided had the required format. Please provide Json dump files from previous scenario exports.'
          });
        }
      });
    }
  }

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }
}

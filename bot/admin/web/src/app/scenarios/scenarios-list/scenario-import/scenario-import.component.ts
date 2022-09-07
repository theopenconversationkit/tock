import { Component, EventEmitter, Output } from '@angular/core';
import { AbstractControl, AsyncValidatorFn, FormControl, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { NbToastrService } from '@nebular/theme';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError, first } from 'rxjs/operators';

import { readFileAsText } from '../../../shared/utils';
import { FileValidators } from '../../../shared/validators';
import { Scenario, SCENARIO_STATE } from '../../models';
import { ScenarioService } from '../../services/scenario.service';
import { StateService } from '../../../core-nlp/state.service';
import { DialogService } from '../../../core-nlp/dialog.service';
import { ConfirmDialogComponent } from '../../../shared-nlp/confirm-dialog/confirm-dialog.component';

type JsonFile = { filename: string; data: Scenario };

@Component({
  selector: 'tock-scenario-import',
  templateUrl: './scenario-import.component.html',
  styleUrls: ['./scenario-import.component.scss']
})
export class ScenarioImportComponent {
  @Output() onClose = new EventEmitter<boolean>();

  constructor(
    private dialogService: DialogService,
    private toastrService: NbToastrService,
    private stateService: StateService,
    private scenarioService: ScenarioService
  ) {}

  private filesToImport = [];

  importFilesInError: string[] = [];
  filesInError: string[] = [];
  isImportSubmitted: boolean = false;
  loading: boolean = false;
  importForm: FormGroup = new FormGroup({
    filesSources: new FormControl([], [Validators.required, FileValidators.typeSupported(['application/json'])], [this.checkFilesFormat()])
  });

  get filesSources(): FormControl {
    return this.importForm.get('filesSources') as FormControl;
  }

  get canSaveImport(): boolean {
    return this.isImportSubmitted ? this.importForm.valid : this.importForm.dirty;
  }

  close(): Observable<any> {
    const validAction = 'yes';
    if (this.importForm.dirty) {
      const dialogRef = this.dialogService.openDialog(ConfirmDialogComponent, {
        context: {
          title: `Cancel import scenario${this.filesToImport.length > 1 ? 's' : ''}`,
          subtitle: 'Are you sure you want to cancel ? Changes will not be saved.',
          action: validAction
        }
      });
      dialogRef.onClose.subscribe((result) => {
        if (result === validAction) {
          this.onClose.emit(true);
        }
      });
      return dialogRef.onClose;
    } else {
      this.onClose.emit(true);
      return of(validAction);
    }
  }

  closeAlertImportFilesInError(): void {
    this.importFilesInError = [];
  }

  checkFilesFormat(): AsyncValidatorFn {
    return async (control: AbstractControl): Promise<ValidationErrors> | null => {
      this.filesInError = [];
      const filesNameWithWrongFormat: string[] = [];
      const readers = [];

      control.value.forEach((file: File) => {
        readers.push(readFileAsText(file));
      });

      const jsons: JsonFile[] = [];
      await Promise.all(readers).then((values) => {
        values.forEach((result) => {
          if (typeof result.data === 'string') {
            let importJson: Scenario;
            try {
              importJson = JSON.parse(result.data);
            } catch (error) {
              filesNameWithWrongFormat.push(result.fileName);
              return;
            }
            if (!importJson.data?.scenarioItems) {
              filesNameWithWrongFormat.push(result.fileName);
            } else {
              jsons.push({ filename: result.fileName, data: importJson });
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
      this.importAllScenarios(this.filesToImport);
    }
  }

  importAllScenarios(importJsons: JsonFile[]): void {
    this.loading = true;

    forkJoin(importJsons.map((json: JsonFile) => this.importScenario(json)))
      .pipe(first())
      .subscribe((res: []) => {
        this.loading = false;
        this.importFilesInError = res.filter((r) => typeof r === 'string');

        if (!this.importFilesInError.length) {
          this.onClose.emit(true);
        } else {
          this.toastrService.warning(`Not all scenarios were imported`, 'Warning', {
            duration: 5000,
            status: 'warning'
          });

          this.filesSources.setValue(this.filesSources.value.filter((f: File) => this.importFilesInError.includes(f.name)));
        }

        if (this.importFilesInError.length !== importJsons.length) {
          this.toastrService.success(`Scenario successfully imported`, 'Success', {
            duration: 5000,
            status: 'success'
          });
        }
      });
  }

  importScenario(json: JsonFile): Observable<Scenario | string> {
    delete json.data.id;
    delete json.data.applicationId;
    delete json.data.createDate;
    delete json.data.updateDate;
    delete json.data.sagaId;

    json.data.state = SCENARIO_STATE.draft;
    json.data.applicationId = this.stateService.currentApplication._id;

    return this.scenarioService.postScenario(json.data).pipe(
      first(),
      catchError((_err) => of(json.filename))
    );
  }
}

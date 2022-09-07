import { Component, EventEmitter, OnDestroy, Output } from '@angular/core';
import { AbstractControl, AsyncValidatorFn, FormControl, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { NbToastrService } from '@nebular/theme';
import { Observable, of, Subject } from 'rxjs';
import { first } from 'rxjs/operators';

import { readFileAsText } from '../../../shared/utils';
import { FileValidators } from '../../../shared/validators';
import { Scenario, SCENARIO_STATE } from '../../models';
import { ScenarioService } from '../../services/scenario.service';
import { StateService } from '../../../core-nlp/state.service';
import { DialogService } from '../../../core-nlp/dialog.service';
import { ConfirmDialogComponent } from '../../../shared-nlp/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'tock-scenario-import',
  templateUrl: './scenario-import.component.html',
  styleUrls: ['./scenario-import.component.scss']
})
export class ScenarioImportComponent implements OnDestroy {
  destroy = new Subject();

  @Output() onClose = new EventEmitter<boolean>();

  constructor(
    private dialogService: DialogService,
    private toastrService: NbToastrService,
    private stateService: StateService,
    private scenarioService: ScenarioService
  ) {}

  private filesToImport = [];

  filesInError = [];
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
          this.filesInError = [];
          this.isImportSubmitted = false;
          this.importForm.reset();
          this.onClose.emit(true);
        }
      });
      return dialogRef.onClose;
    } else {
      this.filesInError = [];
      this.isImportSubmitted = false;
      this.importForm.reset();
      this.onClose.emit(true);
      return of(validAction);
    }
  }

  checkFilesFormat(): AsyncValidatorFn {
    return async (control: AbstractControl): Promise<ValidationErrors> | null => {
      this.filesInError = [];
      const filesNameWithWrongFormat: string[] = [];
      const readers = [];

      control.value.forEach((file: File) => {
        readers.push(readFileAsText(file));
      });

      let jsons: Scenario[] = [];
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
      this.importAllScenarios(this.filesToImport);
    }
  }

  importAllScenarios(importJsons: JSON[], index = 0) {
    this.loading = true;
    if (index < importJsons.length && importJsons[index]) {
      this.importScenario(importJsons, index);
    } else {
      this.loading = false;
      this.toastrService.success(`Scenario successfully imported`, 'Success', {
        duration: 5000,
        status: 'success'
      });
      this.onClose.emit(true);
    }
  }

  importScenario(importJsons, index) {
    const json = importJsons[index];
    delete json.id;
    delete json.applicationId;
    delete json.createDate;
    delete json.updateDate;
    delete json.sagaId;

    json.state = SCENARIO_STATE.draft;

    json.applicationId = this.stateService.currentApplication._id;
    this.scenarioService
      .postScenario(json)
      .pipe(first())
      .subscribe({
        next: (_newScenario) => {
          this.importAllScenarios(importJsons, index + 1);
        },
        error: () => {
          this.importAllScenarios(importJsons, index + 1);
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }
}

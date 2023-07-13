import { Component, EventEmitter, Input, Output } from '@angular/core';
import { AbstractControl, AsyncValidatorFn, FormControl, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError, first } from 'rxjs/operators';

import { readFileAsText } from '../../../shared/utils';
import { FileValidators } from '../../../shared/validators';
import { ScenarioGroup, ScenarioVersion, SCENARIO_STATE } from '../../models';
import { ScenarioService } from '../../services';
import { deepCopy } from '../../../shared/utils';
import { ChoiceDialogComponent } from '../../../shared/components';

enum nameConflictResolution {
  new = 'new',
  add = 'add'
}

type ScenarioGroupImport = ScenarioGroup & {
  _addOrChangeName?: 'notYetDetermined' | nameConflictResolution;
  _newName?: string;
  _targetScenarioGroupId?: string;
};

type JsonFile = { filename: string; data: ScenarioGroupImport };

@Component({
  selector: 'tock-scenario-import',
  templateUrl: './scenario-import.component.html',
  styleUrls: ['./scenario-import.component.scss']
})
export class ScenarioImportComponent {
  @Input() scenariosGroups!: ScenarioGroup[];
  @Output() onClose = new EventEmitter<boolean>();

  readonly nameConflictResolution = nameConflictResolution;

  private filesToImport: JsonFile[] = [];

  importFilesInError: string[] = [];
  filesInError: string[] = [];
  isImportSubmitted: boolean = false;
  loading: boolean = false;
  importForm: FormGroup = new FormGroup({
    filesSources: new FormControl<File[]>(
      [],
      [Validators.required, FileValidators.mimeTypeSupported(['application/json'])],
      [this.checkFilesFormat()]
    )
  });

  get filesSources(): FormControl {
    return this.importForm.get('filesSources') as FormControl;
  }

  get canSaveImport(): boolean {
    return this.isImportSubmitted ? this.importForm.valid : this.importForm.dirty;
  }

  constructor(private nbDialogService: NbDialogService, private toastrService: NbToastrService, private scenarioService: ScenarioService) {}

  close(): Observable<any> {
    const validAction = 'yes';
    if (this.importForm.dirty) {
      const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
        context: {
          title: `Cancel import scenario${this.filesToImport.length > 1 ? 's' : ''}`,
          subtitle: 'Are you sure you want to cancel ? Changes will not be saved.',
          actions: [
            { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
            { actionName: validAction, buttonStatus: 'danger' }
          ],
          modalStatus: 'danger'
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
        values.forEach((result: { fileName: string; data: string }) => {
          try {
            let scenarioGroup: ScenarioGroupImport = JSON.parse(result.data);

            if (scenarioGroup.name && scenarioGroup['data']?.scenarioItems) {
              // backward compatibility : import of old scenarios export format
              console.log('BACKWARD COMPATIBILITY IMPORT');
              type LegacyScenarioFormat = ScenarioGroupImport & { data: any };
              const scenarioGroupCopy = deepCopy(scenarioGroup) as LegacyScenarioFormat;
              scenarioGroup = {
                name: scenarioGroupCopy.name,
                description: scenarioGroupCopy.description,
                category: scenarioGroupCopy.category,
                tags: scenarioGroupCopy.tags,
                enabled: scenarioGroupCopy.enabled,
                unknownAnswerId: '',
                versions: [
                  {
                    data: scenarioGroupCopy.data,
                    state: SCENARIO_STATE.draft,
                    comment: ''
                  }
                ]
              };
              // backward compatibility end
            } else if (scenarioGroup.name && scenarioGroup.versions?.length) {
              if (!scenarioGroup.unknownAnswerId) scenarioGroup.unknownAnswerId = '';

              scenarioGroup.versions.forEach((scenarioVersion: ScenarioVersion) => {
                if (!scenarioVersion.data?.scenarioItems || !scenarioVersion.data?.mode) {
                  filesNameWithWrongFormat.push(result.fileName);
                }
              });
            } else {
              filesNameWithWrongFormat.push(result.fileName);
            }

            if (!filesNameWithWrongFormat.includes(result.fileName)) {
              jsons.push({ filename: result.fileName, data: scenarioGroup });
            }
          } catch (error) {
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
      this.checkScenarioGroupsNamesConflicts();
    }
  }

  scenarioGroupNameConflictToResolve;

  nameConflictForm: FormGroup = new FormGroup(
    {
      addOrChangeName: new FormControl<boolean>(null, Validators.required),
      newGroupName: new FormControl<string>('', this.isGroupNameUnic.bind(this))
    },
    {
      validators: [this.newGroupNameValidation]
    }
  );

  get addOrChangeName(): FormControl {
    return this.nameConflictForm.get('addOrChangeName') as FormControl;
  }
  get newGroupName(): FormControl {
    return this.nameConflictForm.get('newGroupName') as FormControl;
  }

  isGroupNameUnic(c: FormControl) {
    if (!this.scenariosGroups) return null;

    if (this.scenariosGroups.find((sg) => sg.name === c.value?.trim()))
      return { custom: 'This name is already used by an existing scenario group' };
    return null;
  }

  get canResolveConflict(): boolean {
    return this.nameConflictForm.valid;
  }

  newGroupNameValidation(formGroup: FormGroup) {
    if (formGroup.value.addOrChangeName === nameConflictResolution.new) {
      return Validators.required(formGroup.get('newGroupName'))
        ? {
            custom: 'A new name is required'
          }
        : null;
    }
    return null;
  }

  checkScenarioGroupsNamesConflicts() {
    this.scenarioGroupNameConflictToResolve = undefined;

    for (let index = 0; index < this.filesToImport.length; index++) {
      const file = this.filesToImport[index];
      const existingGroup = this.scenariosGroups.find((sg) => sg.name === file.data.name);
      if (existingGroup) {
        if (!file.data._addOrChangeName || file.data._addOrChangeName === 'notYetDetermined') {
          this.nameConflictForm.reset();
          file.data._targetScenarioGroupId = existingGroup.id;
          file.data._addOrChangeName = 'notYetDetermined';
          this.scenarioGroupNameConflictToResolve = file;
          return;
        }
      }
    }
    this.importAllScenarios(this.filesToImport);
  }

  resolveConflict() {
    if (this.nameConflictForm.value.addOrChangeName === nameConflictResolution.add) {
      this.scenarioGroupNameConflictToResolve.data._addOrChangeName = nameConflictResolution.add;
    } else {
      this.scenarioGroupNameConflictToResolve.data._addOrChangeName = nameConflictResolution.new;
      this.scenarioGroupNameConflictToResolve.data._newName = this.nameConflictForm.value.newGroupName;
    }

    this.checkScenarioGroupsNamesConflicts();
  }

  importAllScenarios(importJsons: JsonFile[]): void {
    this.loading = true;

    forkJoin(importJsons.map((json: JsonFile) => this.importOrAddScenario(json)))
      .pipe(first())
      .subscribe((res: []) => {
        this.loading = false;

        let flattenRes = [].concat(...res);
        this.importFilesInError = flattenRes.filter((r) => typeof r === 'string');

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

  importOrAddScenario(json: JsonFile): Observable<(ScenarioGroup | ScenarioVersion | string)[]> {
    delete json.data.id;
    delete json.data.creationDate;
    delete json.data.updateDate;
    delete json.data.enabled;
    json.data.versions.forEach((version) => {
      if (version.state === SCENARIO_STATE.current) {
        version.state = SCENARIO_STATE.draft;
        version.comment += ` ${SCENARIO_STATE.current}`;
      }
      delete version.id;
      delete version.creationDate;
      delete version.updateDate;
    });

    if (!json.data._addOrChangeName) {
      return forkJoin([
        this.scenarioService.importScenarioGroup(json.data).pipe(
          first(),
          catchError((_err) => of(json.filename))
        )
      ]);
    } else if (json.data._addOrChangeName === nameConflictResolution.new) {
      json.data.name = json.data._newName;
      return forkJoin([
        this.scenarioService.importScenarioGroup(json.data).pipe(
          first(),
          catchError((_err) => of(json.filename))
        )
      ]);
    } else if (json.data._addOrChangeName === nameConflictResolution.add) {
      let versionsPosts: Observable<ScenarioVersion | string>[] = [];
      json.data.versions.forEach((version) => {
        versionsPosts.push(
          this.scenarioService.postScenarioVersion(json.data._targetScenarioGroupId, version).pipe(
            first(),
            catchError((_err) => of(json.filename))
          )
        );
      });

      return forkJoin(versionsPosts);
    }
  }
}

import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { StateService } from '../../core-nlp/state.service';
import { RestService } from '../../core-nlp/rest/rest.service';
import { NbDialogService, NbToastrService, NbWindowService } from '@nebular/theme';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { Observable, Subject, debounceTime, takeUntil } from 'rxjs';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { VectorDbProvider, VectorDbProvidersConfiguration, ProvidersConfigurations } from './models/providers-configuration';
import { VectorDbSettings } from './models/vector-db-settings';
import { deepCopy, getExportFileName, readFileAsText } from '../../shared/utils';
import { ChoiceDialogComponent, DebugViewerWindowComponent } from '../../shared/components';
import { saveAs } from 'file-saver-es';
import { FileValidators } from '../../shared/validators';
import { ProvidersConfigurationParam } from '../../shared/model/ai-settings';
import { TranslocoService } from '@jsverse/transloco';

interface VectorDbSettingsForm {
  id: FormControl<string>;
  enabled: FormControl<boolean>;
  vectorDbProvider: FormControl<VectorDbProvider>;
  setting: FormGroup<any>;
}

@Component({
  selector: 'tock-vector-db-settings',
  templateUrl: './vector-db-settings.component.html',
  styleUrls: ['./vector-db-settings.component.scss']
})
export class VectorDbSettingsComponent implements OnInit {
  destroy$: Subject<unknown> = new Subject();

  loading: boolean = false;

  isSubmitted: boolean = false;

  configurations: BotApplicationConfiguration[];

  providersConfigurations = ProvidersConfigurations;

  settingsBackup: VectorDbSettings;

  @ViewChild('exportConfirmationModal') exportConfirmationModal: TemplateRef<any>;
  @ViewChild('importModal') importModal: TemplateRef<any>;

  constructor(
    public state: StateService,
    private rest: RestService,
    private toastrService: NbToastrService,
    private botConfiguration: BotConfigurationService,
    private nbWindowService: NbWindowService,
    private nbDialogService: NbDialogService,
    private transloco: TranslocoService
  ) {}

  ngOnInit(): void {
    this.form.valueChanges.pipe(debounceTime(200), takeUntil(this.destroy$)).subscribe(() => {
      this.setActivationDisabledState();
    });

    this.form
      .get('vectorDbProvider')
      .valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((provider: VectorDbProvider) => {
        this.initFormSettings(provider);
      });

    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((confs: BotApplicationConfiguration[]) => {
      delete this.settingsBackup;

      // Reset form on configuration change
      this.form.reset();
      // Reset formGroup control too, if any
      this.resetFormGroupControls();

      this.loading = true;
      this.configurations = confs;

      if (confs.length) {
        this.getVectorDbSettingsLoader().subscribe((res) => {
          const settings = res;

          if (settings?.id) {
            this.settingsBackup = deepCopy(settings);
            setTimeout(() => {
              this.initForm(settings);
            });
          }

          this.loading = false;
        });
      } else {
        this.loading = false;
      }
    });
  }

  private getVectorDbSettingsLoader(): Observable<VectorDbSettings> {
    const url = `/gen-ai/bots/${this.state.currentApplication.name}/configuration/vector-store`;
    return this.rest.get<VectorDbSettings>(url, (settings: VectorDbSettings) => settings);
  }

  form = new FormGroup<VectorDbSettingsForm>({
    id: new FormControl(null),
    enabled: new FormControl({ value: undefined, disabled: !this.canBeActivated() }),
    vectorDbProvider: new FormControl(undefined, [Validators.required]),
    setting: new FormGroup<any>({})
  });

  get enabled(): FormControl {
    return this.form.get('enabled') as FormControl;
  }

  get vectorDbProvider(): FormControl {
    return this.form.get('vectorDbProvider') as FormControl;
  }

  get currentVectorDbProvider(): VectorDbProvidersConfiguration {
    return ProvidersConfigurations.find((e) => e.key === this.vectorDbProvider.value);
  }

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  canBeActivated(): boolean {
    return this.form ? this.form.valid : false;
  }

  setActivationDisabledState(): void {
    if (this.canBeActivated()) {
      this.enabled.enable();
    } else {
      this.enabled.disable();
    }
  }

  initForm(settings: VectorDbSettings): void {
    this.initFormSettings(settings.setting.provider);
    this.form.patchValue({
      vectorDbProvider: settings.setting.provider
    });
    this.form.patchValue(settings);
    this.form.markAsPristine();
  }

  initFormSettings(provider: VectorDbProvider): void {
    let requiredConfiguration: VectorDbProvidersConfiguration = ProvidersConfigurations.find((c) => c.key === provider);

    if (requiredConfiguration) {
      // Purge existing controls that may contain values incompatible with a new control with the same name if provider change
      this.resetFormGroupControls();

      requiredConfiguration.params.forEach((param) => {
        this.form.controls['setting'].addControl(param.key, new FormControl(param.defaultValue, Validators.required));
      });

      this.form.controls['setting'].addControl('provider', new FormControl(provider));
    }
  }

  resetFormGroupControls() {
    const existingGroupKeys = Object.keys(this.form.controls['setting'].controls);
    existingGroupKeys.forEach((key) => {
      this.form.controls['setting'].removeControl(key);
    });
  }

  cancel(): void {
    this.initForm(this.settingsBackup);
  }

  submit(): void {
    this.isSubmitted = true;
    if (this.canSave && this.form.dirty) {
      this.loading = true;
      const formValue: VectorDbSettings = deepCopy(this.form.value) as unknown as VectorDbSettings;
      formValue.namespace = this.state.currentApplication.namespace;
      formValue.botId = this.state.currentApplication.name;

      delete formValue['vectorDbProvider'];

      const url = `/gen-ai/bots/${this.state.currentApplication.name}/configuration/vector-store`;
      this.rest.post(url, formValue, null, null, true).subscribe({
        next: (vectorDbSettings: VectorDbSettings) => {
          this.settingsBackup = vectorDbSettings;
          this.form.patchValue(vectorDbSettings);
          this.form.markAsPristine();
          this.isSubmitted = false;
          this.toastrService.success(
            this.transloco.translate('configuration.vector-db-settings.settingsSavedSuccess'),
            this.transloco.translate('common.messages.success'),
            {
              duration: 5000,
              status: 'success'
            }
          );
          this.loading = false;
        },
        error: (error) => {
          this.toastrService.danger(
            this.transloco.translate('common.messages.an-error-occured'),
            this.transloco.translate('common.messages.error'),
            {
              duration: 5000,
              status: 'danger'
            }
          );

          if (error.error) {
            this.nbWindowService.open(DebugViewerWindowComponent, {
              title: this.transloco.translate('common.messages.an-error-occured'),
              context: {
                debug: error.error
              }
            });
          }
          this.loading = false;
        }
      });
    }
  }

  get hasExportableData(): boolean {
    if (this.vectorDbProvider.value) return true;

    const formValue: VectorDbSettings = deepCopy(this.form.value) as unknown as VectorDbSettings;

    return Object.values(formValue).some((entry) => {
      return entry && (typeof entry !== 'object' || Object.keys(entry).length !== 0);
    });
  }

  sensitiveParams: { label: string; key: string; include: boolean; param: ProvidersConfigurationParam }[];

  exportSettings() {
    this.sensitiveParams = [];

    const shouldConfirm =
      this.vectorDbProvider.value &&
      this.currentVectorDbProvider.params.some((entry) => {
        return entry.confirmExport;
      });

    if (shouldConfirm) {
      this.currentVectorDbProvider.params.forEach((entry) => {
        if (entry.confirmExport) {
          this.sensitiveParams.push({
            label: this.transloco.translate('configuration.vector-db-settings.vectorDbProviderTitle'),
            key: 'setting',
            include: false,
            param: entry
          });
        }
      });

      this.exportConfirmationModalRef = this.nbDialogService.open(this.exportConfirmationModal);
    } else {
      this.downloadSettings();
    }
  }

  exportConfirmationModalRef;

  closeExportConfirmationModal() {
    this.exportConfirmationModalRef.close();
  }

  confirmExportSettings() {
    this.downloadSettings();
    this.closeExportConfirmationModal();
  }

  downloadSettings() {
    const formValue: VectorDbSettings = deepCopy(this.form.value) as unknown as VectorDbSettings;
    delete formValue['vectorDbProvider'];
    delete formValue['id'];
    delete formValue['enabled'];

    if (this.sensitiveParams?.length) {
      this.sensitiveParams.forEach((sensitiveParam) => {
        if (!sensitiveParam.include) {
          delete formValue[sensitiveParam.key][sensitiveParam.param.key];
        }
      });
    }

    const jsonBlob = new Blob([JSON.stringify(formValue)], {
      type: 'application/json'
    });

    const exportFileName = getExportFileName(
      this.state.currentApplication.namespace,
      this.state.currentApplication.name,
      this.transloco.translate('configuration.vector-db-settings.title'),
      'json'
    );

    saveAs(jsonBlob, exportFileName);

    this.toastrService.show(
      this.transloco.translate('configuration.vector-db-settings.exportSuccess'),
      this.transloco.translate('configuration.vector-db-settings.title'),
      {
        duration: 3000,
        status: 'success'
      }
    );
  }

  importModalRef;

  importSettings() {
    this.isImportSubmitted = false;
    this.importForm.reset();
    this.importModalRef = this.nbDialogService.open(this.importModal);
  }

  closeImportModal() {
    this.importModalRef.close();
  }

  isImportSubmitted: boolean = false;

  importForm: FormGroup = new FormGroup({
    fileSource: new FormControl<File[]>([], {
      nonNullable: true,
      validators: [Validators.required, FileValidators.mimeTypeSupported(['application/json'])]
    })
  });

  get fileSource(): FormControl {
    return this.importForm.get('fileSource') as FormControl;
  }

  get canSaveImport(): boolean {
    return this.isImportSubmitted ? this.importForm.valid : this.importForm.dirty;
  }

  submitImportSettings() {
    this.isImportSubmitted = true;
    if (this.canSaveImport) {
      const file = this.fileSource.value[0];

      readFileAsText(file).then((fileContent) => {
        const settings = JSON.parse(fileContent.data);

        const hasCompatibleProvider = settings.setting?.provider && Object.values(VectorDbProvider).includes(settings.setting.provider);

        if (!hasCompatibleProvider) {
          this.toastrService.show(
            this.transloco.translate('configuration.vector-db-settings.importError'),
            this.transloco.translate('configuration.vector-db-settings.importModalTitle'),
            {
              duration: 6000,
              status: 'danger'
            }
          );
          return;
        }

        this.initForm(settings);
        this.form.markAsDirty();

        this.closeImportModal();
      });
    }
  }

  confirmSettingsDeletion() {
    const confirmAction = this.transloco.translate('common.actions.delete');
    const cancelAction = this.transloco.translate('common.actions.cancel');

    const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
      context: {
        title: this.transloco.translate('configuration.vector-db-settings.settingsDeletionTitle'),
        subtitle: this.transloco.translate('configuration.vector-db-settings.confirmDeletionSubtitle'),
        modalStatus: 'danger',
        actions: [
          { actionName: cancelAction, buttonStatus: 'basic' },
          { actionName: confirmAction, buttonStatus: 'danger' }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result?.toLowerCase() === confirmAction.toLowerCase()) {
        this.deleteSettings();
      }
    });
  }

  deleteSettings() {
    const url = `/gen-ai/bots/${this.state.currentApplication.name}/configuration/vector-store`;
    this.rest.delete<boolean>(url).subscribe(() => {
      delete this.settingsBackup;
      this.form.reset();
      this.form.markAsPristine();
      this.toastrService.success(
        this.transloco.translate('configuration.vector-db-settings.settingsDeletedSuccess'),
        this.transloco.translate('common.messages.success'),
        {
          duration: 5000,
          status: 'success'
        }
      );
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}

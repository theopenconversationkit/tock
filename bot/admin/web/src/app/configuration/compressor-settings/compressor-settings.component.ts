import { Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Observable, Subject, debounceTime, takeUntil } from 'rxjs';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { NbDialogService, NbToastrService, NbWindowService } from '@nebular/theme';
import { RestService } from '../../core-nlp/rest/rest.service';
import { StateService } from '../../core-nlp/state.service';
import { CompressorSettings } from './models/compressor-settings';
import { deepCopy, getExportFileName, readFileAsText } from '../../shared/utils';
import { CompressorProvider, CompressorProvidersConfiguration, ProvidersConfigurations } from './models/providers-configuration';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ChoiceDialogComponent, DebugViewerWindowComponent } from '../../shared/components';
import { ProvidersConfigurationParam } from '../../shared/model/ai-settings';
import { saveAs } from 'file-saver-es';
import { FileValidators } from '../../shared/validators';

interface CompressorSettingsForm {
  id: FormControl<string>;
  enabled: FormControl<boolean>;
  compressorProvider: FormControl<CompressorProvider>;
  setting: FormGroup<any>;
}

@Component({
  selector: 'tock-compressor-settings',
  templateUrl: './compressor-settings.component.html',
  styleUrl: './compressor-settings.component.scss'
})
export class CompressorSettingsComponent implements OnInit, OnDestroy {
  destroy$: Subject<unknown> = new Subject();

  loading: boolean = false;

  isSubmitted: boolean = false;

  configurations: BotApplicationConfiguration[];

  providersConfigurations = ProvidersConfigurations;

  settingsBackup: CompressorSettings;

  @ViewChild('exportConfirmationModal') exportConfirmationModal: TemplateRef<any>;
  @ViewChild('importModal') importModal: TemplateRef<any>;

  constructor(
    private state: StateService,
    private rest: RestService,
    private toastrService: NbToastrService,
    private botConfiguration: BotConfigurationService,
    private nbWindowService: NbWindowService,
    private nbDialogService: NbDialogService
  ) {}

  ngOnInit(): void {
    this.form.valueChanges.pipe(debounceTime(200), takeUntil(this.destroy$)).subscribe(() => {
      this.setActivationDisabledState();
    });

    this.form
      .get('compressorProvider')
      .valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((provider: CompressorProvider) => {
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
        this.getCompressorSettingsLoader().subscribe((res) => {
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

  private getCompressorSettingsLoader(): Observable<CompressorSettings> {
    const url = `/gen-ai/bots/${this.state.currentApplication.name}/configuration/document-compressor`;
    return this.rest.get<CompressorSettings>(url, (settings: CompressorSettings) => settings);
  }

  form = new FormGroup<CompressorSettingsForm>({
    id: new FormControl(null),
    enabled: new FormControl({ value: undefined, disabled: !this.canBeActivated() }),
    compressorProvider: new FormControl(undefined, [Validators.required]),
    setting: new FormGroup<any>({})
  });

  get enabled(): FormControl {
    return this.form.get('enabled') as FormControl;
  }

  get compressorProvider(): FormControl {
    return this.form.get('compressorProvider') as FormControl;
  }

  get currentCompressorProvider(): CompressorProvidersConfiguration {
    return ProvidersConfigurations.find((e) => e.key === this.compressorProvider.value);
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

  initForm(settings: CompressorSettings): void {
    this.initFormSettings(settings.setting.provider);
    this.form.patchValue({
      compressorProvider: settings.setting.provider
    });

    this.form.patchValue(settings);
    this.form.markAsPristine();
  }

  initFormSettings(provider: CompressorProvider): void {
    let requiredConfiguration: CompressorProvidersConfiguration = ProvidersConfigurations.find((c) => c.key === provider);

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
      const formValue: CompressorSettings = deepCopy(this.form.value) as unknown as CompressorSettings;
      formValue.namespace = this.state.currentApplication.namespace;
      formValue.botId = this.state.currentApplication.name;

      delete formValue['compressorProvider'];

      const url = `/gen-ai/bots/${this.state.currentApplication.name}/configuration/document-compressor`;
      this.rest.post(url, formValue, null, null, true).subscribe({
        next: (compressorSettings: CompressorSettings) => {
          this.settingsBackup = compressorSettings;
          this.form.patchValue(compressorSettings);
          this.form.markAsPristine();
          this.isSubmitted = false;
          this.toastrService.success(`Compressor settings succesfully saved`, 'Success', {
            duration: 5000,
            status: 'success'
          });
          this.loading = false;
        },
        error: (error) => {
          this.toastrService.danger('An error occured', 'Error', {
            duration: 5000,
            status: 'danger'
          });

          if (error.error) {
            this.nbWindowService.open(DebugViewerWindowComponent, {
              title: 'An error occured',
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
    if (this.compressorProvider.value) return true;

    const formValue: CompressorSettings = deepCopy(this.form.value) as unknown as CompressorSettings;

    return Object.values(formValue).some((entry) => {
      return entry && (typeof entry !== 'object' || Object.keys(entry).length !== 0);
    });
  }

  sensitiveParams: { label: string; key: string; include: boolean; param: ProvidersConfigurationParam }[];

  exportSettings() {
    this.sensitiveParams = [];

    const shouldConfirm =
      this.compressorProvider.value &&
      this.currentCompressorProvider.params.some((entry) => {
        return entry.confirmExport;
      });

    if (shouldConfirm) {
      this.currentCompressorProvider.params.forEach((entry) => {
        if (entry.confirmExport) {
          this.sensitiveParams.push({ label: 'Compressor provider', key: 'setting', include: false, param: entry });
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
    const formValue: CompressorSettings = deepCopy(this.form.value) as unknown as CompressorSettings;
    delete formValue['compressorProvider'];
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
      'Compressor settings',
      'json'
    );

    saveAs(jsonBlob, exportFileName);

    this.toastrService.show(`Compressor settings dump provided`, 'Compressor settings dump', {
      duration: 3000,
      status: 'success'
    });
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

        const hasCompatibleProvider = settings.setting?.provider && Object.values(CompressorProvider).includes(settings.setting.provider);

        if (!hasCompatibleProvider) {
          this.toastrService.show(
            `The file supplied does not reference a compatible provider. Please check the file.`,
            'Compressor settings import fails',
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
    const confirmAction = 'Delete';
    const cancelAction = 'Cancel';

    const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
      context: {
        title: `Delete compressor settings`,
        subtitle: `Are you sure you want to delete the currently saved compressor settings?`,
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
    const url = `/gen-ai/bots/${this.state.currentApplication.name}/configuration/document-compressor`;
    this.rest.delete<boolean>(url).subscribe(() => {
      delete this.settingsBackup;
      this.form.reset();
      this.form.markAsPristine();
      this.toastrService.success(`Compressor settings succesfully deleted`, 'Success', {
        duration: 5000,
        status: 'success'
      });
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}

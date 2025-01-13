import { Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Observable, Subject, debounceTime, takeUntil } from 'rxjs';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { DefaultPrompt, EngineConfigurations } from './models/engines-configuration';
import { SentenceGenerationSettings } from './models/sentence-generation-settings';
import { StateService } from '../../core-nlp/state.service';
import { RestService } from '../../core-nlp/rest/rest.service';
import { NbDialogService, NbToastrService, NbWindowService } from '@nebular/theme';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import {
  AiEngineSettingKeyName,
  EnginesConfiguration,
  AiEngineProvider,
  ProvidersConfigurationParam
} from '../../shared/model/ai-settings';
import { deepCopy, getExportFileName, readFileAsText } from '../../shared/utils';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ChoiceDialogComponent, DebugViewerWindowComponent } from '../../shared/components';
import { saveAs } from 'file-saver-es';
import { FileValidators } from '../../shared/validators';

interface GenAiSettingsForm {
  id: FormControl<string>;
  enabled: FormControl<boolean>;
  nbSentences: FormControl<number>;
  llmEngine: FormControl<AiEngineProvider>;
  llmSetting: FormGroup<any>;
}

@Component({
  selector: 'tock-sentence-generation-settings',
  templateUrl: './sentence-generation-settings.component.html',
  styleUrls: ['./sentence-generation-settings.component.scss']
})
export class SentenceGenerationSettingsComponent implements OnInit, OnDestroy {
  destroy$: Subject<unknown> = new Subject();

  configurations: BotApplicationConfiguration[];

  engineConfigurations = EngineConfigurations;

  defaultPrompt = DefaultPrompt;

  settingsBackup: SentenceGenerationSettings;

  isSubmitted: boolean = false;

  loading: boolean = false;

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
      .get('llmEngine')
      .valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((engine: AiEngineProvider) => {
        this.initFormSettings(engine);
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
        this.getSentenceGenerationSettingsLoader().subscribe((res) => {
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

  form = new FormGroup<GenAiSettingsForm>({
    id: new FormControl(null),
    enabled: new FormControl({ value: undefined, disabled: !this.canBeActivated() }),
    nbSentences: new FormControl(10),

    llmEngine: new FormControl(undefined, [Validators.required]),
    llmSetting: new FormGroup<any>({})
  });

  get enabled(): FormControl {
    return this.form.get('enabled') as FormControl;
  }
  get nbSentences(): FormControl {
    return this.form.get('nbSentences') as FormControl;
  }
  get llmEngine(): FormControl {
    return this.form.get('llmEngine') as FormControl;
  }

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  initFormSettings(provider: AiEngineProvider): void {
    let requiredConfiguration: EnginesConfiguration = EngineConfigurations.find((c) => c.key === provider);

    if (requiredConfiguration) {
      // Purge existing controls that may contain values incompatible with a new control with the same name after engine change
      this.resetFormGroupControls();

      requiredConfiguration.params.forEach((param) => {
        this.form.controls['llmSetting'].addControl(param.key, new FormControl(param.defaultValue, Validators.required));
      });

      this.form.controls['llmSetting'].addControl('provider', new FormControl(provider));
    }
  }

  resetFormGroupControls() {
    const existingGroupKeys = Object.keys(this.form.controls['llmSetting'].controls);
    existingGroupKeys.forEach((key) => {
      this.form.controls['llmSetting'].removeControl(key);
    });
  }

  private getSentenceGenerationSettingsLoader(): Observable<SentenceGenerationSettings> {
    const url = `/configuration/bots/${this.state.currentApplication.name}/sentence-generation/configuration`;
    return this.rest.get<SentenceGenerationSettings>(url, (settings: SentenceGenerationSettings) => settings);
  }

  initForm(settings: SentenceGenerationSettings) {
    this.initFormSettings(settings.llmSetting.provider);
    this.form.patchValue({
      llmEngine: settings.llmSetting.provider
    });
    this.form.patchValue(settings);
    this.form.markAsPristine();
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

  get currentLlmEngine(): EnginesConfiguration {
    return EngineConfigurations.find((e) => e.key === this.llmEngine.value);
  }

  cancel(): void {
    this.initForm(this.settingsBackup);
  }

  submit(): void {
    this.isSubmitted = true;
    if (this.canSave && this.form.dirty) {
      this.loading = true;
      const formValue: SentenceGenerationSettings = deepCopy(this.form.value) as unknown as SentenceGenerationSettings;
      formValue.namespace = this.state.currentApplication.namespace;
      formValue.botId = this.state.currentApplication.name;

      delete formValue['llmEngine'];

      const url = `/configuration/bots/${this.state.currentApplication.name}/sentence-generation/configuration`;
      this.rest.post(url, formValue, null, null, true).subscribe({
        next: (genAiSettings: SentenceGenerationSettings) => {
          this.settingsBackup = genAiSettings;
          this.form.patchValue(genAiSettings);
          this.form.markAsPristine();
          this.isSubmitted = false;
          this.toastrService.success(`Sentence generation settings succesfully saved`, 'Success', {
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
    if (this.llmEngine.value) return true;

    const formValue: SentenceGenerationSettings = deepCopy(this.form.value) as unknown as SentenceGenerationSettings;

    return Object.values(formValue).some((entry) => {
      return entry && (typeof entry !== 'object' || Object.keys(entry).length !== 0);
    });
  }

  sensitiveParams: { label: string; key: string; include: boolean; param: ProvidersConfigurationParam }[];

  exportSettings() {
    this.sensitiveParams = [];

    const shouldConfirm =
      this.llmEngine.value &&
      this.currentLlmEngine.params.some((entry) => {
        return entry.confirmExport;
      });

    if (shouldConfirm) {
      [{ label: 'LLM engine', key: AiEngineSettingKeyName.llmSetting, params: this.currentLlmEngine.params }].forEach((engine) => {
        this.currentLlmEngine.params.forEach((entry) => {
          if (entry.confirmExport) {
            this.sensitiveParams.push({ label: 'LLM engine', key: engine.key, include: false, param: entry });
          }
        });
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
    const formValue: SentenceGenerationSettings = deepCopy(this.form.value) as unknown as SentenceGenerationSettings;
    delete formValue['llmEngine'];
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
      'Sentence generation settings',
      'json'
    );

    saveAs(jsonBlob, exportFileName);

    this.toastrService.show(`Sentence generation settings dump provided`, 'Sentence generation settings dump', {
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

        const hasCompatibleProvider =
          settings.llmSetting?.provider && Object.values(AiEngineProvider).includes(settings.llmSetting.provider);

        if (!hasCompatibleProvider) {
          this.toastrService.show(
            `The file supplied does not reference a compatible provider. Please check the file.`,
            'Sentence generation settings import fails',
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
        title: `Delete sentence generation settings`,
        subtitle: `Are you sure you want to delete the currently saved sentence generation settings?`,
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
    const url = `/configuration/bots/${this.state.currentApplication.name}/sentence-generation/configuration`;
    this.rest.delete<boolean>(url).subscribe(() => {
      delete this.settingsBackup;
      this.form.reset();
      this.form.markAsPristine();
      this.toastrService.success(`Sentence generation settings succesfully deleted`, 'Success', {
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

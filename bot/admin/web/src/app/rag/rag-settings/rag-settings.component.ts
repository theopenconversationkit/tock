import { Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { debounceTime, forkJoin, Observable, of, Subject, take, takeUntil } from 'rxjs';
import { BotService } from '../../bot/bot-service';
import { StoryDefinitionConfiguration, StorySearchQuery } from '../../bot/model/story';
import { RestService } from '../../core-nlp/rest/rest.service';
import { StateService } from '../../core-nlp/state.service';
import {
  EnginesConfigurations,
  QuestionCondensing_prompt_ConfigurationParam,
  QuestionAnswering_prompt_ConfigurationParam
} from './models/engines-configurations';
import { RagSettings } from './models';
import { NbDialogService, NbToastrService, NbWindowService } from '@nebular/theme';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { deepCopy, getExportFileName, readFileAsText } from '../../shared/utils';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { DebugViewerWindowComponent } from '../../shared/components/debug-viewer-window/debug-viewer-window.component';
import {
  AiEngineSettingKeyName,
  EnginesConfiguration,
  AiEngineProvider,
  ProvidersConfigurationParam
} from '../../shared/model/ai-settings';
import { ChoiceDialogComponent } from '../../shared/components';
import { saveAs } from 'file-saver-es';
import { FileValidators } from '../../shared/validators';

interface RagSettingsForm {
  id: FormControl<string>;
  enabled: FormControl<boolean>;

  noAnswerSentence: FormControl<string>;
  noAnswerStoryId: FormControl<string>;

  indexSessionId: FormControl<string>;
  indexName: FormControl<string>;

  documentsRequired: FormControl<boolean>;

  condenseQuestionLlmEngine: FormControl<AiEngineProvider>;
  condenseQuestionLlmSetting: FormGroup<any>;
  condenseQuestionPrompt: FormGroup<any>;

  questionAnsweringLlmEngine: FormControl<AiEngineProvider>;
  questionAnsweringLlmSetting: FormGroup<any>;
  questionAnsweringPrompt: FormGroup<any>;

  emEngine: FormControl<AiEngineProvider>;
  emSetting: FormGroup<any>;
}

@Component({
  selector: 'tock-rag-settings',
  templateUrl: './rag-settings.component.html',
  styleUrls: ['./rag-settings.component.scss']
})
export class RagSettingsComponent implements OnInit, OnDestroy {
  destroy$: Subject<unknown> = new Subject();

  configurations: BotApplicationConfiguration[];

  enginesConfigurations = EnginesConfigurations;

  engineSettingKeyName = AiEngineSettingKeyName;

  questionCondensing_prompt_ConfigurationParam = QuestionCondensing_prompt_ConfigurationParam;

  questionAnswering_prompt_ConfigurationParam = QuestionAnswering_prompt_ConfigurationParam;

  availableStories: StoryDefinitionConfiguration[];

  filteredStories$: Observable<StoryDefinitionConfiguration[]>;

  settingsBackup: RagSettings;

  isSubmitted: boolean = false;

  loading: boolean = false;

  @ViewChild('exportConfirmationModal') exportConfirmationModal: TemplateRef<any>;
  @ViewChild('importModal') importModal: TemplateRef<any>;

  constructor(
    private botService: BotService,
    private state: StateService,
    private rest: RestService,
    private toastrService: NbToastrService,
    private botConfiguration: BotConfigurationService,
    private nbWindowService: NbWindowService,
    private nbDialogService: NbDialogService
  ) {}

  ngOnInit(): void {
    this.form.valueChanges.pipe(takeUntil(this.destroy$), debounceTime(200)).subscribe(() => {
      this.setActivationDisabledState();
    });

    this.form
      .get('condenseQuestionLlmEngine')
      .valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((engine: AiEngineProvider) => {
        this.initFormSettings(AiEngineSettingKeyName.condenseQuestionLlmSetting, engine);
        this.initFormPrompt('condenseQuestionPrompt');
      });

    this.form
      .get('questionAnsweringLlmEngine')
      .valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((engine: AiEngineProvider) => {
        this.initFormSettings(AiEngineSettingKeyName.questionAnsweringLlmSetting, engine);
        this.initFormPrompt('questionAnsweringPrompt');
      });

    this.form
      .get('emEngine')
      .valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((engine: AiEngineProvider) => {
        this.initFormSettings(AiEngineSettingKeyName.emSetting, engine);
      });

    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((confs: BotApplicationConfiguration[]) => {
      delete this.settingsBackup;

      // Reset form on configuration change
      this.form.reset();

      // Reset formGroup controls too, if any
      this.resetFormGroupControls(AiEngineSettingKeyName.condenseQuestionLlmSetting);
      this.resetFormGroupControls(AiEngineSettingKeyName.questionAnsweringLlmSetting);
      this.resetFormGroupControls(AiEngineSettingKeyName.emSetting);

      this.loading = true;
      this.configurations = confs;

      if (confs.length) {
        forkJoin([this.getStoriesLoader(), this.getRagSettingsLoader()]).subscribe((res) => {
          this.availableStories = res[0];

          const settings = res[1];
          if (settings?.id) {
            if (!settings.noAnswerStoryId) {
              settings.noAnswerStoryId = null;
            }
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

  private getRagSettingsLoader(): Observable<RagSettings> {
    const url = `/configuration/bots/${this.state.currentApplication.name}/rag`;
    return this.rest.get<RagSettings>(url, (settings: RagSettings) => settings);
  }

  form = new FormGroup<RagSettingsForm>({
    id: new FormControl(null),
    enabled: new FormControl({ value: undefined, disabled: !this.canRagBeActivated() }),
    noAnswerSentence: new FormControl(undefined, [Validators.required]),
    noAnswerStoryId: new FormControl(undefined),
    indexSessionId: new FormControl(undefined),
    indexName: new FormControl(undefined),
    documentsRequired: new FormControl(undefined),

    condenseQuestionLlmEngine: new FormControl(undefined, [Validators.required]),
    condenseQuestionLlmSetting: new FormGroup<any>({}),
    condenseQuestionPrompt: new FormGroup<any>({
      formatter: new FormControl(undefined),
      template: new FormControl(undefined, [Validators.required])
    }),

    questionAnsweringLlmEngine: new FormControl(undefined, [Validators.required]),
    questionAnsweringLlmSetting: new FormGroup<any>({}),
    questionAnsweringPrompt: new FormGroup<any>({
      formatter: new FormControl(undefined),
      template: new FormControl(undefined, [Validators.required])
    }),

    emEngine: new FormControl(undefined, [Validators.required]),
    emSetting: new FormGroup<any>({})
  });

  get enabled(): FormControl {
    return this.form.get('enabled') as FormControl;
  }

  get condenseQuestionLlmEngine(): FormControl {
    return this.form.get('condenseQuestionLlmEngine') as FormControl;
  }

  get questionAnsweringLlmEngine(): FormControl {
    return this.form.get('questionAnsweringLlmEngine') as FormControl;
  }

  get emEngine(): FormControl {
    return this.form.get('emEngine') as FormControl;
  }

  get noAnswerSentence(): FormControl {
    return this.form.get('noAnswerSentence') as FormControl;
  }
  get noAnswerStoryId(): FormControl {
    return this.form.get('noAnswerStoryId') as FormControl;
  }

  get indexSessionId(): FormControl {
    return this.form.get('indexSessionId') as FormControl;
  }

  get documentsRequired(): FormControl {
    return this.form.get('documentsRequired') as FormControl;
  }

  get indexName(): FormControl {
    return this.form.get('indexName') as FormControl;
  }

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  get getCurrentStoryLabel(): string {
    const currentStory = this.availableStories?.find((story) => story.storyId === this.noAnswerStoryId.value);
    return currentStory?.name || '';
  }

  initFormPrompt(group: 'condenseQuestionPrompt' | 'questionAnsweringPrompt'): void {
    let defaultPrompt;
    if (group === 'condenseQuestionPrompt') {
      defaultPrompt = QuestionCondensing_prompt_ConfigurationParam.defaultValue;
    }
    if (group === 'questionAnsweringPrompt') {
      defaultPrompt = QuestionAnswering_prompt_ConfigurationParam.defaultValue;
    }

    this.form.get(group).patchValue({
      // 'jinja2' | 'f-string'
      formatter: 'f-string',
      prompt: defaultPrompt
    });
  }

  initFormSettings(group: AiEngineSettingKeyName, provider: AiEngineProvider): void {
    let requiredConfiguration: EnginesConfiguration = EnginesConfigurations[group].find((c) => c.key === provider);

    if (requiredConfiguration) {
      // Purge existing controls that may contain values incompatible with a new control with the same name after engine change
      this.resetFormGroupControls(group);

      requiredConfiguration.params.forEach((param) => {
        this.form.controls[group].addControl(param.key, new FormControl(param.defaultValue, Validators.required));
      });

      this.form.controls[group].addControl('provider', new FormControl(provider));
    }
  }

  resetFormGroupControls(group: AiEngineSettingKeyName) {
    const existingGroupKeys = Object.keys(this.form.controls[group].controls);
    existingGroupKeys.forEach((key) => {
      this.form.controls[group].removeControl(key);
    });
  }

  initForm(settings: RagSettings) {
    this.initFormSettings(AiEngineSettingKeyName.condenseQuestionLlmSetting, settings.condenseQuestionLlmSetting?.provider);
    this.initFormSettings(AiEngineSettingKeyName.questionAnsweringLlmSetting, settings.questionAnsweringLlmSetting?.provider);
    this.initFormSettings(AiEngineSettingKeyName.emSetting, settings.emSetting?.provider);

    this.form.patchValue({
      condenseQuestionLlmEngine: settings.condenseQuestionLlmSetting?.provider,
      questionAnsweringLlmEngine: settings.questionAnsweringLlmSetting?.provider,
      emEngine: settings.emSetting?.provider
    });
    this.form.patchValue(settings);

    this.initFormPrompt('condenseQuestionPrompt');
    this.initFormPrompt('questionAnsweringPrompt');

    this.form.markAsPristine();
  }

  canRagBeActivated(): boolean {
    return this.form ? this.form.valid && this.indexSessionId.value?.length : false;
  }

  setActivationDisabledState(): void {
    if (this.canRagBeActivated()) {
      this.enabled.enable();
    } else {
      this.enabled.disable();
    }
  }

  get currentCondenseQuestionLlmEngine(): EnginesConfiguration {
    return EnginesConfigurations[AiEngineSettingKeyName.condenseQuestionLlmSetting].find(
      (e) => e.key === this.condenseQuestionLlmEngine.value
    );
  }

  get currentQuestionAnsweringLlmEngine(): EnginesConfiguration {
    return EnginesConfigurations[AiEngineSettingKeyName.questionAnsweringLlmSetting].find(
      (e) => e.key === this.questionAnsweringLlmEngine.value
    );
  }

  get currentEmEngine(): EnginesConfiguration {
    return EnginesConfigurations[AiEngineSettingKeyName.emSetting].find((e) => e.key === this.emEngine.value);
  }

  private getStoriesLoader(): Observable<StoryDefinitionConfiguration[]> {
    return this.botService
      .getStories(
        new StorySearchQuery(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale,
          0,
          10000,
          undefined,
          undefined,
          false
        )
      )
      .pipe(take(1));
  }

  isStoryEnabled(story) {
    for (let i = 0; i < story.features.length; i++) {
      if (!story.features[i].enabled && !story.features[i].switchToStoryId && !story.features[i].endWithStoryId) {
        return false;
      }
    }
    return true;
  }

  storySelectedChange(storyId: string): void {
    this.noAnswerStoryId.patchValue(storyId);
    this.form.markAsDirty();
  }

  onStoryChange(value: string): void {
    if (value?.trim() == '') {
      this.removeNoAnswerStoryId();
    }
  }

  removeNoAnswerStoryId(): void {
    this.noAnswerStoryId.patchValue(null);
    this.form.markAsDirty();
  }

  filterStoriesList(e: string): void {
    this.filteredStories$ = of(this.availableStories.filter((optionValue) => optionValue.name.toLowerCase().includes(e.toLowerCase())));
  }

  storyInputFocus(): void {
    this.filteredStories$ = of(this.availableStories);
  }

  storyInputBlur(e: FocusEvent): void {
    setTimeout(() => {
      // timeout needed to avoid reseting input and filtered stories when clicking on autocomplete suggestions (which fires blur event)
      const target: HTMLInputElement = e.target as HTMLInputElement;
      target.value = this.getCurrentStoryLabel;

      this.filteredStories$ = of(this.availableStories);
    }, 100);
  }

  cancel(): void {
    this.initForm(this.settingsBackup);
  }

  submit(): void {
    this.isSubmitted = true;
    if (this.canSave && this.form.dirty) {
      this.loading = true;
      const formValue: RagSettings = deepCopy(this.form.value) as unknown as RagSettings;
      delete formValue['llmEngine'];
      delete formValue['emEngine'];
      formValue.namespace = this.state.currentApplication.namespace;
      formValue.botId = this.state.currentApplication.name;
      formValue.noAnswerStoryId = this.noAnswerStoryId.value === 'null' ? null : this.noAnswerStoryId.value;

      const url = `/configuration/bots/${this.state.currentApplication.name}/rag`;
      this.rest.post(url, formValue, null, null, true).subscribe({
        next: (ragSettings: RagSettings) => {
          if (!ragSettings.noAnswerStoryId) {
            ragSettings.noAnswerStoryId = null;
          }
          this.settingsBackup = ragSettings;

          this.indexName.reset();

          this.form.patchValue(ragSettings);
          this.form.markAsPristine();

          this.isSubmitted = false;
          this.toastrService.success(`Rag settings succesfully saved`, 'Success', {
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
    if (this.condenseQuestionLlmEngine.value || this.questionAnsweringLlmEngine.value || this.emEngine.value) return true;

    const formValue: RagSettings = deepCopy(this.form.value) as unknown as RagSettings;

    return Object.values(formValue).some((entry) => {
      return entry && (typeof entry !== 'object' || Object.keys(entry).length !== 0);
    });
  }

  sensitiveParams: { label: string; key: string; include: boolean; param: ProvidersConfigurationParam }[];

  exportSettings() {
    this.sensitiveParams = [];

    const shouldConfirm =
      (this.condenseQuestionLlmEngine.value || this.questionAnsweringLlmEngine.value || this.emEngine.value) &&
      [(this.currentCondenseQuestionLlmEngine.params, this.currentQuestionAnsweringLlmEngine.params, this.currentEmEngine.params)].some(
        (engine) => {
          return engine.some((entry) => {
            return entry.confirmExport;
          });
        }
      );

    if (shouldConfirm) {
      [
        {
          label: 'Question condensing LLM engine',
          key: AiEngineSettingKeyName.condenseQuestionLlmSetting,
          params: this.currentCondenseQuestionLlmEngine.params
        },
        {
          label: 'Question answering LLM engine',
          key: AiEngineSettingKeyName.questionAnsweringLlmSetting,
          params: this.currentQuestionAnsweringLlmEngine.params
        },
        { label: 'Embedding engine', key: AiEngineSettingKeyName.emSetting, params: this.currentEmEngine.params }
      ].forEach((engine) => {
        engine.params.forEach((entry) => {
          if (entry.confirmExport) {
            this.sensitiveParams.push({ label: engine.label, key: engine.key, include: false, param: entry });
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
    const formValue: RagSettings = deepCopy(this.form.value) as unknown as RagSettings;
    delete formValue['llmEngine'];
    delete formValue['emEngine'];
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
      'Rag settings',
      'json'
    );

    saveAs(jsonBlob, exportFileName);

    this.toastrService.show(`Rag settings dump provided`, 'Rag settings dump', { duration: 3000, status: 'success' });
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

        const hasCompatibleProvider = Object.values(AiEngineSettingKeyName).some((ekn) => {
          return settings[ekn]?.provider && Object.values(AiEngineProvider).includes(settings[ekn].provider);
        });

        if (!hasCompatibleProvider) {
          this.toastrService.show(
            `The file supplied does not reference a compatible provider. Please check the file.`,
            'Rag settings import fails',
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
        title: `Delete Rag settings`,
        subtitle: `Are you sure you want to delete the currently saved Rag settings?`,
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
    const url = `/configuration/bots/${this.state.currentApplication.name}/rag`;
    this.rest.delete<boolean>(url).subscribe(() => {
      delete this.settingsBackup;
      this.form.reset();
      this.form.markAsPristine();
      this.toastrService.success(`Rag settings succesfully deleted`, 'Success', {
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

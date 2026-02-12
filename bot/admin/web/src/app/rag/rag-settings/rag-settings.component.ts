/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { debounceTime, forkJoin, Observable, Subject, takeUntil, pairwise } from 'rxjs';
import { NbDialogRef, NbDialogService, NbToastrService, NbWindowService } from '@nebular/theme';

import { RestService } from '../../core-nlp/rest/rest.service';
import { StateService } from '../../core-nlp/state.service';
import { EnginesConfigurations, QuestionCondensing_prompt, QuestionAnswering_prompt } from './models/engines-configurations';
import { RagSettings } from './models';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { DebugViewerWindowComponent } from '../../shared/components/debug-viewer-window/debug-viewer-window.component';
import { deepCopy, getExportFileName, readFileAsText } from '../../shared/utils';
import {
  AiEngineSettingKeyName,
  EnginesConfiguration,
  AiEngineProvider,
  ProvidersConfigurationParam,
  PromptTypeKeyName,
  PromptDefinitionFormatter
} from '../../shared/model/ai-settings';
import { ChoiceDialogComponent } from '../../shared/components';
import { saveAs } from 'file-saver-es';
import { FileValidators } from '../../shared/validators';

interface RagSettingsForm {
  id: FormControl<string>;
  enabled: FormControl<boolean>;

  debugEnabled: FormControl<boolean>;

  indexSessionId: FormControl<string>;
  indexName: FormControl<string>;

  maxDocumentsRetrieved: FormControl<number>;

  questionCondensingLlmProvider: FormControl<AiEngineProvider>;
  questionCondensingLlmSetting: FormGroup<any>;
  questionCondensingPrompt: FormGroup<any>;

  maxMessagesFromHistory: FormControl<number>;

  questionAnsweringLlmProvider: FormControl<AiEngineProvider>;
  questionAnsweringLlmSetting: FormGroup<any>;
  questionAnsweringPrompt: FormGroup<any>;

  emProvider: FormControl<AiEngineProvider>;
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

  questionCondensing_prompt = QuestionCondensing_prompt;

  questionAnswering_prompt = QuestionAnswering_prompt;

  settingsBackup: RagSettings;

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
    this.form.valueChanges.pipe(takeUntil(this.destroy$), debounceTime(200)).subscribe(() => {
      this.setActivationDisabledState();
    });

    this.form
      .get('questionCondensingLlmProvider')
      .valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((engine: AiEngineProvider) => {
        this.initFormSettings(AiEngineSettingKeyName.questionCondensingLlmSetting, engine);
      });

    this.form
      .get('questionAnsweringLlmProvider')
      .valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((engine: AiEngineProvider) => {
        this.initFormSettings(AiEngineSettingKeyName.questionAnsweringLlmSetting, engine);
      });

    this.form
      .get('emProvider')
      .valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((engine: AiEngineProvider) => {
        this.initFormSettings(AiEngineSettingKeyName.emSetting, engine);
      });

    [PromptTypeKeyName.questionCondensingPrompt, PromptTypeKeyName.questionAnsweringPrompt].forEach((wich) => {
      this.form
        .get(wich)
        .valueChanges.pipe(pairwise(), takeUntil(this.destroy$))
        .subscribe(([prev, next]) => {
          if (
            next?.formatter === PromptDefinitionFormatter.jinja2 &&
            prev?.formatter === PromptDefinitionFormatter.fstring &&
            next?.template?.length
          ) {
            this.fStringToJinja(wich);
          }
        });
    });

    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((confs: BotApplicationConfiguration[]) => {
      delete this.settingsBackup;

      // Reset form on configuration change
      this.form.reset();

      this.setFormDefaultValues();

      // Reset formGroup controls too, if any
      this.resetFormGroupControls(AiEngineSettingKeyName.questionCondensingLlmSetting);
      this.resetFormGroupControls(AiEngineSettingKeyName.questionAnsweringLlmSetting);
      this.resetFormGroupControls(AiEngineSettingKeyName.emSetting);

      // Reset accordions states
      this.accordionItemsExpandedState = undefined;

      this.initFormPrompt(PromptTypeKeyName.questionCondensingPrompt);
      this.initFormPrompt(PromptTypeKeyName.questionAnsweringPrompt);

      this.loading = true;
      this.configurations = confs;

      if (confs.length) {
        forkJoin([this.getRagSettingsLoader()]).subscribe((res) => {
          const settings = res[0];
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

  private getRagSettingsLoader(): Observable<RagSettings> {
    const url = `/gen-ai/bots/${this.state.currentApplication.name}/configuration/rag`;
    return this.rest.get<RagSettings>(url, (settings: RagSettings) => settings);
  }

  form = new FormGroup<RagSettingsForm>({
    id: new FormControl(null),
    enabled: new FormControl({ value: undefined, disabled: !this.canRagBeActivated() }),

    debugEnabled: new FormControl({ value: undefined, disabled: !this.canRagBeActivated() }),

    indexSessionId: new FormControl(undefined),
    indexName: new FormControl(undefined),

    maxDocumentsRetrieved: new FormControl(undefined),

    questionCondensingLlmProvider: new FormControl(undefined, [Validators.required]),
    questionCondensingLlmSetting: new FormGroup({}),
    questionCondensingPrompt: new FormGroup({}),

    maxMessagesFromHistory: new FormControl(undefined),

    questionAnsweringLlmProvider: new FormControl(undefined, [Validators.required]),
    questionAnsweringLlmSetting: new FormGroup({}),
    questionAnsweringPrompt: new FormGroup({}),

    emProvider: new FormControl(undefined, [Validators.required]),
    emSetting: new FormGroup({})
  });

  get enabled(): FormControl {
    return this.form.get('enabled') as FormControl;
  }

  get debugEnabled(): FormControl {
    return this.form.get('debugEnabled') as FormControl;
  }

  get questionCondensingLlmProvider(): FormControl {
    return this.form.get('questionCondensingLlmProvider') as FormControl;
  }

  get questionAnsweringLlmProvider(): FormControl {
    return this.form.get('questionAnsweringLlmProvider') as FormControl;
  }

  get emProvider(): FormControl {
    return this.form.get('emProvider') as FormControl;
  }

  get maxMessagesFromHistory(): FormControl {
    return this.form.get('maxMessagesFromHistory') as FormControl;
  }

  get indexSessionId(): FormControl {
    return this.form.get('indexSessionId') as FormControl;
  }

  get maxDocumentsRetrieved(): FormControl {
    return this.form.get('maxDocumentsRetrieved') as FormControl;
  }

  get indexName(): FormControl {
    return this.form.get('indexName') as FormControl;
  }

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  accordionItemsExpandedState: Map<string, boolean>;

  isAccordionItemsExpanded(itemName: string): boolean {
    if (!this.accordionItemsExpandedState) {
      this.accordionItemsExpandedState = new Map();
    }

    if (!this.accordionItemsExpandedState.has(itemName)) {
      switch (itemName) {
        case 'questionConsensingConfiguration':
          this.accordionItemsExpandedState.set(
            'questionConsensingConfiguration',
            this.form.get('questionCondensingLlmProvider').invalid ||
              this.form.get(AiEngineSettingKeyName.questionCondensingLlmSetting).invalid
          );
          break;
        case 'questionCondensingPrompt':
          this.accordionItemsExpandedState.set(
            'questionCondensingPrompt',
            this.form.get(PromptTypeKeyName.questionCondensingPrompt).invalid
          );
          break;
        case 'questionAnsweringConfiguration':
          this.accordionItemsExpandedState.set(
            'questionAnsweringConfiguration',
            this.form.get('questionAnsweringLlmProvider').invalid ||
              this.form.get(AiEngineSettingKeyName.questionAnsweringLlmSetting).invalid
          );
          break;
        case 'questionAnsweringPrompt':
          this.accordionItemsExpandedState.set('questionAnsweringPrompt', this.form.get(PromptTypeKeyName.questionAnsweringPrompt).invalid);
          break;
        case 'embeddingConfiguration':
          this.accordionItemsExpandedState.set(
            'embeddingConfiguration',
            this.form.get('emProvider').invalid || this.form.get('emSetting').invalid
          );
          break;
      }
    }

    return this.accordionItemsExpandedState.get(itemName);
  }

  shouldDisplayPromptParam(parentGroup: string, param: ProvidersConfigurationParam) {
    // Goal : We want templates to use the Jinja2 format by default.
    if (param.key === 'formatter') {
      // We only care about the “formatter” param
      if (this.form.get(parentGroup).get(param.key).value === PromptDefinitionFormatter.jinja2) {
        // If the format is already Jinja2, we can hide the choice control
        return false;
      }
    }
    return true;
  }

  fStringToJinja(group: string): void {
    const source = this.form.get(group).get('template').value;
    const result = source.replace(/{(.*?)}/g, '{{$1}}');
    this.form.get(group).patchValue({
      template: result
    });
  }

  initFormPrompt(group: PromptTypeKeyName): void {
    this.resetFormGroupControls(group);

    let params: ProvidersConfigurationParam[];

    if (group === PromptTypeKeyName.questionCondensingPrompt) {
      params = QuestionCondensing_prompt;
    }
    if (group === 'questionAnsweringPrompt') {
      params = QuestionAnswering_prompt;
    }

    params.forEach((param) => {
      this.form.controls[group].addControl(param.key, new FormControl(param.defaultValue, Validators.required));
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

  resetFormGroupControls(group: string): void {
    const existingGroupKeys = Object.keys(this.form.controls[group].controls);
    existingGroupKeys.forEach((key) => {
      this.form.controls[group].removeControl(key);
    });
  }

  setFormDefaultValues(): void {
    this.form.patchValue({
      debugEnabled: false,
      maxMessagesFromHistory: 5,
      maxDocumentsRetrieved: 4
    });
  }

  initForm(settings: RagSettings): void {
    this.initFormSettings(AiEngineSettingKeyName.questionCondensingLlmSetting, settings.questionCondensingLlmSetting?.provider);
    this.initFormSettings(AiEngineSettingKeyName.questionAnsweringLlmSetting, settings.questionAnsweringLlmSetting?.provider);
    this.initFormSettings(AiEngineSettingKeyName.emSetting, settings.emSetting?.provider);

    this.form.patchValue({
      questionCondensingLlmProvider: settings.questionCondensingLlmSetting?.provider,
      questionAnsweringLlmProvider: settings.questionAnsweringLlmSetting?.provider,
      emProvider: settings.emSetting?.provider
    });
    this.form.patchValue(settings);

    this.initFormPrompt(PromptTypeKeyName.questionCondensingPrompt);
    this.initFormPrompt(PromptTypeKeyName.questionAnsweringPrompt);

    this.form.patchValue({
      questionCondensingPrompt: settings.questionCondensingPrompt,
      questionAnsweringPrompt: settings.questionAnsweringPrompt
    });

    this.accordionItemsExpandedState = undefined;

    this.form.markAsPristine();
  }

  canRagBeActivated(): boolean {
    return this.form ? this.form.valid && this.indexSessionId.value?.length : false;
  }

  setActivationDisabledState(): void {
    if (this.canRagBeActivated()) {
      this.enabled.enable();
      this.debugEnabled.enable();
    } else {
      this.enabled.disable();
      this.debugEnabled.disable();
    }
  }

  get currentQuestionCondensingConfig(): EnginesConfiguration {
    return EnginesConfigurations[AiEngineSettingKeyName.questionCondensingLlmSetting].find(
      (e) => e.key === this.questionCondensingLlmProvider.value
    );
  }

  get currentQuestionAnsweringConfig(): EnginesConfiguration {
    return EnginesConfigurations[AiEngineSettingKeyName.questionAnsweringLlmSetting].find(
      (e) => e.key === this.questionAnsweringLlmProvider.value
    );
  }

  get currentEmConfig(): EnginesConfiguration {
    return EnginesConfigurations[AiEngineSettingKeyName.emSetting].find((e) => e.key === this.emProvider.value);
  }

  cancel(): void {
    this.initForm(this.settingsBackup);
  }

  submit(): void {
    this.isSubmitted = true;
    if (this.canSave && this.form.dirty) {
      this.loading = true;
      const formValue: RagSettings = deepCopy(this.form.value) as unknown as RagSettings;
      delete formValue['questionCondensingLlmProvider'];
      delete formValue['questionAnsweringLlmProvider'];
      delete formValue['emProvider'];
      formValue.namespace = this.state.currentApplication.namespace;
      formValue.botId = this.state.currentApplication.name;

      const url = `/gen-ai/bots/${this.state.currentApplication.name}/configuration/rag`;
      this.rest.post(url, formValue, null, null, true).subscribe({
        next: (ragSettings: RagSettings) => {
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
    if (this.questionCondensingLlmProvider.value || this.questionAnsweringLlmProvider.value || this.emProvider.value) return true;

    const formValue: RagSettings = deepCopy(this.form.value) as unknown as RagSettings;

    return Object.values(formValue).some((entry) => {
      return entry && (typeof entry !== 'object' || Object.keys(entry).length !== 0);
    });
  }

  sensitiveParams: { label: string; key: string; include: boolean; param: ProvidersConfigurationParam }[];

  exportSettings(): void {
    this.sensitiveParams = [];

    const shouldConfirm =
      (this.questionCondensingLlmProvider.value || this.questionAnsweringLlmProvider.value || this.emProvider.value) &&
      [(this.currentQuestionCondensingConfig?.params, this.currentQuestionAnsweringConfig?.params, this.currentEmConfig?.params)].some(
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
          key: AiEngineSettingKeyName.questionCondensingLlmSetting,
          params: this.currentQuestionCondensingConfig?.params
        },
        {
          label: 'Question answering LLM engine',
          key: AiEngineSettingKeyName.questionAnsweringLlmSetting,
          params: this.currentQuestionAnsweringConfig?.params
        },
        { label: 'Embedding engine', key: AiEngineSettingKeyName.emSetting, params: this.currentEmConfig?.params }
      ].forEach((engine) => {
        engine.params?.forEach((entry) => {
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

  exportConfirmationModalRef: NbDialogRef<any>;

  closeExportConfirmationModal(): void {
    this.exportConfirmationModalRef.close();
  }

  confirmExportSettings(): void {
    this.downloadSettings();
    this.closeExportConfirmationModal();
  }

  downloadSettings(): void {
    const formValue: RagSettings = deepCopy(this.form.value) as unknown as RagSettings;
    delete formValue['questionCondensingLlmProvider'];
    delete formValue['questionAnsweringLlmProvider'];
    delete formValue['emProvider'];
    delete formValue['id'];
    delete formValue['enabled'];
    delete formValue['indexName'];

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

  importModalRef: NbDialogRef<any>;

  importSettings(): void {
    this.isImportSubmitted = false;
    this.importForm.reset();
    this.importModalRef = this.nbDialogService.open(this.importModal);
  }

  closeImportModal(): void {
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

  submitImportSettings(): void {
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

  confirmSettingsDeletion(): void {
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

  deleteSettings(): void {
    const url = `/gen-ai/bots/${this.state.currentApplication.name}/configuration/rag`;
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

import { Component, ElementRef, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Location } from '@angular/common';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef, NbDialogService, NbMenuService, NbToastrService } from '@nebular/theme';
import { FileValidators } from '../shared/validators';
import { getDialogMessageUserAvatar, readFileAsText } from '../shared/utils';
import { AiEngineProvider, AiEngineSettingKeyName, EnginesConfiguration, PromptDefinitionFormatter } from '../shared/model/ai-settings';
import { EnginesConfigurations, QuestionAnsweringDefaultPrompt } from '../rag/rag-settings/models/engines-configurations';
import { BotApplicationConfiguration } from '../core/model/configuration';
import { Observable, Subject, filter, forkJoin, takeUntil } from 'rxjs';
import { BotConfigurationService } from '../core/bot-configuration.service';
import { RagSettings } from '../rag/rag-settings/models';
import { StateService } from '../core-nlp/state.service';
import { RestService } from '../core-nlp/rest/rest.service';
import { TestMessage } from '../test/model/test';
import { Sentence } from '../shared/model/dialog-data';

interface PlaygroundForm {
  questionAnsweringLlmProvider: FormControl<AiEngineProvider>;
  questionAnsweringLlmSetting: FormGroup<any>;
  questionAnsweringPromptTemplate: FormControl<string>;

  prompt: FormControl<string>;
}

@Component({
  selector: 'tock-playground',
  templateUrl: './playground.component.html',
  styleUrl: './playground.component.scss'
})
export class PlaygroundComponent implements OnInit, OnDestroy {
  destroy$: Subject<unknown> = new Subject();

  loading: boolean = false;

  @ViewChild('textareaPromptRef') textareaPromptRef: ElementRef;
  @ViewChild('importModal') importModal: TemplateRef<any>;
  @ViewChild('resultWrapper') private resultWrapper: ElementRef;

  configurations: BotApplicationConfiguration[];

  isSubmitted: boolean = false;

  enginesConfigurations = EnginesConfigurations;

  engineSettingKeyName = AiEngineSettingKeyName;

  llmSettingsIsExpanded: boolean = false;

  testQueryInProgress: boolean = false;

  messages: { message: TestMessage; observabilityInfo?: any }[] = [];

  messagesHistory: { prompt: string; message: TestMessage; observabilityInfo?: any }[] = [];

  messagesHistoryCursor: number = 0;

  question_answering_prompt: string;

  constructor(
    private botConfiguration: BotConfigurationService,
    public state: StateService,
    private rest: RestService,
    private location: Location,
    private nbDialogService: NbDialogService,
    private toastrService: NbToastrService,
    private nbMenuService: NbMenuService
  ) {
    this.question_answering_prompt = (this.location.getState() as any)?.question_answering_prompt;
  }

  ngOnInit(): void {
    this.questionAnsweringLlmProvider.valueChanges.pipe(takeUntil(this.destroy$)).subscribe((engine: AiEngineProvider) => {
      this.initFormSettings(AiEngineSettingKeyName.questionAnsweringLlmSetting, engine);
    });

    this.nbMenuService
      .onItemClick()
      .pipe(filter(({ tag }) => tag === 'prompt-template-shortcuts'))
      .subscribe((args) => {
        const type = (args.item as any).type;
        this.loadPromptTemplate(type);
      });

    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((confs: BotApplicationConfiguration[]) => {
      this.loading = true;
      this.configurations = confs;

      if (confs.length) {
        forkJoin([this.getRagSettingsLoader()]).subscribe((res) => {
          const settings = res[0];
          if (settings?.id) {
            setTimeout(() => {
              this.initForm(settings);
            });
          } else {
            this.form.reset();
            this.llmSettingsIsExpanded = true;
          }

          if (this.question_answering_prompt) {
            this.form.patchValue({ prompt: this.question_answering_prompt });
          }

          this.loading = false;
        });
      } else {
        this.form.reset();

        this.loading = false;
      }
    });
  }

  private getRagSettingsLoader(): Observable<RagSettings> {
    const url = `/gen-ai/bots/${this.state.currentApplication.name}/configuration/rag`;
    return this.rest.get<RagSettings>(url, (settings: RagSettings) => settings);
  }

  form = new FormGroup<PlaygroundForm>({
    questionAnsweringLlmProvider: new FormControl(undefined, [Validators.required]),
    questionAnsweringLlmSetting: new FormGroup({}),
    questionAnsweringPromptTemplate: new FormControl(),
    prompt: new FormControl('')
  });

  get questionAnsweringLlmProvider(): FormControl {
    return this.form.get('questionAnsweringLlmProvider') as FormControl;
  }
  get questionAnsweringLlmSetting(): FormControl {
    return this.form.get('questionAnsweringLlmSetting') as FormControl;
  }
  get prompt(): FormControl {
    return this.form.get('prompt') as FormControl;
  }
  get questionAnsweringPromptTemplate(): FormControl {
    return this.form.get('questionAnsweringPromptTemplate') as FormControl;
  }

  get currentQuestionAnsweringConfig(): EnginesConfiguration {
    return EnginesConfigurations[AiEngineSettingKeyName.questionAnsweringLlmSetting].find(
      (e) => e.key === this.questionAnsweringLlmProvider.value
    );
  }

  getCurrentProviderLabel(): string {
    return this.enginesConfigurations[this.engineSettingKeyName.questionAnsweringLlmSetting].find(
      (conf) => conf.key === this.questionAnsweringLlmProvider.value
    )?.label;
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

  initForm(settings: RagSettings): void {
    this.initFormSettings(AiEngineSettingKeyName.questionAnsweringLlmSetting, settings.questionAnsweringLlmSetting?.provider);

    this.form.patchValue({
      questionAnsweringLlmProvider: settings.questionAnsweringLlmSetting?.provider
    });
    this.form.patchValue(settings);

    this.form.patchValue({
      questionAnsweringPromptTemplate: settings.questionAnsweringPrompt?.template
    });

    this.form.markAsPristine();

    this.llmSettingsIsExpanded = !this.form.valid;
  }

  preventDefault(event: Event): void {
    event.preventDefault();
  }

  submit(event?: Event): void {
    if (event) {
      this.preventDefault(event);
    }

    if (!this.form.valid) return;

    let m = this.prompt.value;
    if (!m || m.trim().length === 0) {
      return;
    }

    this.talk(m.trim());
  }

  talk(message: string) {
    this.testQueryInProgress = true;
    this.messages = [];

    const formValue = this.form.value;

    const payload = {
      llmSetting: {
        ...formValue.questionAnsweringLlmSetting,
        provider: formValue.questionAnsweringLlmProvider
      },
      prompt: {
        formatter: PromptDefinitionFormatter.jinja2,
        inputs: {},
        template: message
      }
    };

    const url = `/gen-ai/bots/${this.state.currentApplication.name}/completion/playground`;
    return this.rest.post(url, payload).subscribe({
      next: (res: { answer: string; observabilityInfo?: any }) => {
        this.messages.push({
          message: new TestMessage(true, new Sentence(0, [], res.answer)),
          observabilityInfo: res.observabilityInfo
        });

        this.messagesHistory.push({
          prompt: message,
          message: new TestMessage(true, new Sentence(0, [], res.answer)),
          observabilityInfo: res.observabilityInfo
        });

        this.messagesHistoryCursor = this.messagesHistory.length - 1;

        delete this.testQueryInProgress;
        this.resultWrapper.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'start' });
      },
      error: (error) => {
        console.log(error);
        this.toastrService.danger('An error occured', 'Error', {
          duration: 5000,
          status: 'danger'
        });
        delete this.testQueryInProgress;
      }
    });
  }

  messageHistoryMove(forward: boolean) {
    let hEntry;

    if (!forward) {
      if (this.messagesHistoryCursor > 0) {
        this.messagesHistoryCursor--;
        hEntry = this.messagesHistory[this.messagesHistoryCursor];
      }
    } else {
      if (this.messagesHistoryCursor < this.messagesHistory.length - 1) {
        this.messagesHistoryCursor++;
        hEntry = this.messagesHistory[this.messagesHistoryCursor];
      }
    }

    if (hEntry) {
      this.messages = [
        {
          message: hEntry.message,
          observabilityInfo: hEntry.observabilityInfo
        }
      ];

      this.form.patchValue({ prompt: hEntry.prompt });
    }
  }

  getUserAvatar(isBot: boolean): string {
    return getDialogMessageUserAvatar(isBot);
  }

  openObservabilityTrace(message) {
    window.open(message.observabilityInfo.traceUrl, '_blank');
  }

  expandTextareaPrompt() {
    const defaultHeight = 160;
    const textarea = this.textareaPromptRef?.nativeElement;
    if (textarea.offsetHeight <= defaultHeight) {
      textarea.style.height = 'auto';
      textarea.style.height = Math.max(textarea.scrollHeight + 5, defaultHeight) + 'px';
    } else {
      textarea.style.height = `${defaultHeight}px`;
    }
  }

  getPromptShortcutsMargin() {
    const textarea = this.textareaPromptRef?.nativeElement;
    if (textarea && textarea.clientHeight < textarea.scrollHeight) return '0 1em 0 0';

    return '0';
  }

  promptTemplateShortcuts = [
    { title: 'Load current bot prompt', type: 'current' },
    { title: 'Load default prompt', type: 'default' },
    { title: 'Clear prompt', type: 'clear' }
  ];

  loadPromptTemplate(wich: 'current' | 'default' | 'clear') {
    if (wich === 'current') {
      this.form.patchValue({ prompt: this.questionAnsweringPromptTemplate.value });
    }
    if (wich === 'default') {
      this.form.patchValue({ prompt: QuestionAnsweringDefaultPrompt });
    }

    if (wich === 'clear') {
      this.form.patchValue({ prompt: '' });
    }
  }

  clearHistory() {
    this.messages = [];
    this.messagesHistory = [];
    this.messagesHistoryCursor = 0;
  }

  // IMPORT

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

        this.closeImportModal();
      });
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}

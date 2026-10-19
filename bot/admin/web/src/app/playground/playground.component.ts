import { ChangeDetectorRef, Component, ElementRef, NgZone, OnDestroy, OnInit, TemplateRef, ViewChild, inject } from '@angular/core';
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
import { SSEEvent } from '../test/test.service';
import { ActionReport, PlayerId, PlayerType, Sentence } from '../shared/model/dialog-data';
import { TranslocoService } from '@jsverse/transloco';

interface PlaygroundForm {
  questionAnsweringLlmProvider: FormControl<AiEngineProvider>;
  questionAnsweringLlmSetting: FormGroup<any>;
  questionAnsweringPromptTemplate: FormControl<string>;
  prompt: FormControl<string>;
}

@Component({
    selector: 'tock-playground',
    templateUrl: './playground.component.html',
    styleUrl: './playground.component.scss',
    standalone: false
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

  private cd = inject(ChangeDetectorRef);
  private ngZone = inject(NgZone);

  private static readonly STREAMING_STORAGE_KEY = 'playground.streaming';

  _streaming: boolean = sessionStorage.getItem(PlaygroundComponent.STREAMING_STORAGE_KEY) === 'true';

  set streaming(value: boolean) {
    this._streaming = value;
    sessionStorage.setItem(PlaygroundComponent.STREAMING_STORAGE_KEY, String(value));
  }
  get streaming(): boolean {
    return this._streaming;
  }

  messages: { message: TestMessage; debug: any; observabilityInfo?: any; responseTime?: number; ttft?: number }[] = [];

  messagesHistory: { prompt: string; message: TestMessage; debug: any; observabilityInfo?: any; responseTime?: number; ttft?: number }[] =
    [];

  messagesHistoryCursor: number = 0;

  question_answering_prompt: string;

  promptTemplateShortcuts = [
    { title: 'Load current bot prompt', type: 'current' },
    { title: 'Load default prompt', type: 'default' },
    { title: 'Clear prompt', type: 'clear' }
  ];

  constructor(
    private botConfiguration: BotConfigurationService,
    public state: StateService,
    private rest: RestService,
    private location: Location,
    private nbDialogService: NbDialogService,
    private toastrService: NbToastrService,
    private nbMenuService: NbMenuService,
    private transloco: TranslocoService
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

    this.transloco
      .selectTranslateObject('playground', {}, 'playground')
      .pipe(takeUntil(this.destroy$))
      .subscribe((t) => {
        this.promptTemplateShortcuts = [
          { title: t.loadCurrentBotPrompt, type: 'current' },
          { title: t.loadDefaultPrompt, type: 'default' },
          { title: t.clearPrompt, type: 'clear' }
        ];
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

    const startTime = Date.now();

    if (this.streaming) {
      return this.talkStreaming(message, payload, startTime);
    }

    const url = `/gen-ai/bots/${this.state.currentApplication.name}/completion/playground`;
    return this.rest.post(url, payload).subscribe({
      next: (res: { answer: string; observabilityInfo?: any; ttft?: number }) => {
        const responseTime = Date.now() - startTime;
        this.pushResult(message, res.answer, res.observabilityInfo, responseTime, res.ttft);
        delete this.testQueryInProgress;
        this.resultWrapper.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'start' });
      },
      error: (error) => {
        console.log(error);
        this.toastrService.danger(
          this.transloco.translate('common.messages.an-error-occured'),
          this.transloco.translate('common.messages.error'),
          {
            duration: 5000,
            status: 'danger'
          }
        );
        delete this.testQueryInProgress;
      }
    });
  }

  /**
   * Build the chat-ui message entry from a raw LLM answer and push it to the
   * current messages + history. The answer may be a plain string or a RAG-style
   * JSON object ({ answer, ... }); in the latter case a debug ActionReport is attached.
   */
  private pushResult(prompt: string, answer: string, observabilityInfo: any, responseTime: number, ttft?: number): void {
    let stringAnswer;
    let actionreport;
    try {
      const parsedAnswer = JSON.parse(answer);
      if (typeof parsedAnswer !== 'object' || parsedAnswer === null || Array.isArray(parsedAnswer)) {
        stringAnswer = answer;
      } else if (parsedAnswer.answer === undefined || parsedAnswer.answer === null) {
        // JSON object without an 'answer' field: show the raw JSON.
        stringAnswer = answer;
      } else {
        stringAnswer = parsedAnswer.answer;
        const botFakePlayer = new PlayerId('fake-player', PlayerType.bot);
        const botMessage = new Sentence(0, null, parsedAnswer.answer);
        actionreport = new ActionReport(botFakePlayer, new Date(), botMessage, '1234', true);
        actionreport.ragDebug = { answer: parsedAnswer };
      }
    } catch (e) {
      stringAnswer = answer;
    }

    this.messages = [
      {
        message: new TestMessage(true, new Sentence(0, [], stringAnswer)),
        debug: actionreport ?? undefined,
        observabilityInfo,
        responseTime,
        ttft
      }
    ];

    this.messagesHistory.push({
      prompt,
      message: new TestMessage(true, new Sentence(0, [], stringAnswer)),
      debug: actionreport ?? undefined,
      observabilityInfo,
      responseTime,
      ttft
    });

    this.messagesHistoryCursor = this.messagesHistory.length - 1;
  }

  /**
   * Streaming variant of talk(): consume the SSE completion endpoint, painting the
   * answer token by token, then finalize on the 'answer' event (which carries ttft).
   */
  private talkStreaming(prompt: string, payload: any, startTime: number): void {
    let accumulatedTokens = '';
    let streamingEntry: { message: TestMessage; debug: any; observabilityInfo?: any; responseTime?: number; ttft?: number } | null = null;

    this.completionStream(payload)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (event: SSEEvent) => {
          if (event.event === 'token') {
            try {
              const tokenData = JSON.parse(event.data || '{}');
              accumulatedTokens += tokenData.content || '';

              // The raw LLM stream may be JSON (e.g. a RAG-style {"answer": "..."}). In that
              // case we don't want the JSON scaffolding to scroll by: display only the
              // progressively-parsed `answer` value. For plain text, display it as-is.
              const display = this.computeStreamingDisplay(accumulatedTokens);

              // Run inside Angular's zone and force synchronous change detection so each
              // token is painted as it arrives (the rapid XHR readystate burst would
              // otherwise be coalesced into a single render).
              this.ngZone.run(() => {
                if (!streamingEntry) {
                  streamingEntry = { message: new TestMessage(true, new Sentence(0, [], display)), debug: undefined };
                  this.messages = [streamingEntry];
                } else {
                  streamingEntry.message = new TestMessage(true, new Sentence(0, [], display));
                }
                this.cd.detectChanges();
              });
            } catch (e) {
              console.error('Error parsing token event', e);
            }
          } else if (event.event === 'answer') {
            try {
              const answerData = JSON.parse(event.data || '{}');
              const responseTime = Date.now() - startTime;
              this.ngZone.run(() => {
                // Replace the temporary progressive bubble with the finalized, parsed entry.
                this.pushResult(prompt, answerData.answer ?? accumulatedTokens, answerData.observabilityInfo, responseTime, answerData.ttft);
                delete this.testQueryInProgress;
                this.cd.detectChanges();
              });
              this.resultWrapper?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'start' });
            } catch (e) {
              console.error('Error parsing answer event', e);
            }
          } else if (event.event === 'error') {
            this.ngZone.run(() => {
              delete this.testQueryInProgress;
              this.cd.detectChanges();
            });
            this.toastrService.danger(
              this.transloco.translate('common.messages.an-error-occured'),
              this.transloco.translate('common.messages.error'),
              { duration: 5000, status: 'danger' }
            );
          }
        },
        error: (error) => {
          console.log(error);
          this.ngZone.run(() => {
            delete this.testQueryInProgress;
            this.cd.detectChanges();
          });
          this.toastrService.danger(
            this.transloco.translate('common.messages.an-error-occured'),
            this.transloco.translate('common.messages.error'),
            { duration: 5000, status: 'danger' }
          );
        }
      });
  }

  /**
   * Raw SSE client for the playground streaming completion endpoint.
   * Mirrors TestService.talkStream: incremental parsing of an XHR text stream.
   */
  private completionStream(payload: any): Observable<SSEEvent> {
    return new Observable((observer) => {
      const url = this.rest.url + `/gen-ai/bots/${this.state.currentApplication.name}/completion/playground/stream`;

      const xhr = new XMLHttpRequest();
      xhr.open('POST', url);
      xhr.setRequestHeader('Content-Type', 'application/json');
      xhr.withCredentials = true;

      let lastProcessedIndex = 0;

      xhr.onreadystatechange = () => {
        if (xhr.readyState >= 3 && xhr.responseText.length > lastProcessedIndex) {
          const newData = xhr.responseText.substring(lastProcessedIndex);
          const lines = newData.split('\n');

          for (let i = 0; i < lines.length - 1; i++) {
            const line = lines[i];
            if (line.startsWith('event:')) {
              const eventType = line.substring(6).trim();
              const nextLineIndex = i + 1;
              if (nextLineIndex < lines.length) {
                const dataLine = lines[nextLineIndex];
                if (dataLine.startsWith('data:')) {
                  const eventData = dataLine.substring(5).trim();
                  observer.next({ event: eventType, data: eventData });
                }
              }
            }
          }

          lastProcessedIndex = xhr.responseText.length;
        }

        if (xhr.readyState === 4) {
          if (xhr.status === 200 || xhr.status === 201) {
            observer.complete();
          } else {
            observer.error(new Error(`HTTP ${xhr.status}: ${xhr.statusText}`));
          }
        }
      };

      xhr.onerror = () => {
        observer.error(new Error('Network error'));
      };

      xhr.send(JSON.stringify(payload));

      return () => {
        xhr.abort();
      };
    });
  }

  /**
   * Decide what to display for a partially-streamed buffer.
   * - If the buffer looks like a JSON object/array, reveal only the progressively-parsed
   *   `answer` string value (empty until the key starts), so the JSON scaffolding never scrolls by.
   * - Otherwise (plain text), display the buffer as-is.
   */
  private computeStreamingDisplay(buffer: string): string {
    const trimmed = buffer.trimStart();
    if (trimmed.startsWith('{') || trimmed.startsWith('[')) {
      return this.extractPartialJsonString(buffer, 'answer') ?? '';
    }
    return buffer;
  }

  /**
   * Tolerant extractor for the (possibly unterminated) string value of a top-level key
   * in a partial/streaming JSON buffer. Returns null if the key hasn't appeared yet.
   */
  private extractPartialJsonString(buffer: string, key: string): string | null {
    const keyMatch = buffer.match(new RegExp(`"${key}"\\s*:\\s*"`));
    if (!keyMatch || keyMatch.index === undefined) return null;

    const start = keyMatch.index + keyMatch[0].length;
    const escapeMap: { [k: string]: string } = { n: '\n', t: '\t', r: '\r', b: '\b', f: '\f', '"': '"', '\\': '\\', '/': '/' };

    let result = '';
    for (let i = start; i < buffer.length; i++) {
      const ch = buffer[i];
      if (ch === '\\') {
        const next = buffer[i + 1];
        if (next === undefined) break; // escape sequence not fully received yet
        result += escapeMap[next] ?? next;
        i++;
      } else if (ch === '"') {
        break; // end of the string value
      } else {
        result += ch;
      }
    }
    return result;
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
          debug: hEntry.debug,
          observabilityInfo: hEntry.observabilityInfo,
          responseTime: hEntry.responseTime
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

  promptExpanded = false;

  expandTextareaPrompt() {
    const defaultHeight = 160;
    const textarea = this.textareaPromptRef?.nativeElement;
    if (!textarea) return;
    this.promptExpanded = !this.promptExpanded;
    if (this.promptExpanded) {
      textarea.style.height = 'auto';
      textarea.style.height = Math.max(textarea.scrollHeight + 5, defaultHeight) + 'px';
    } else {
      textarea.style.height = `${defaultHeight}px`;
    }
  }

  get canExpandPrompt(): boolean {
    const textarea = this.textareaPromptRef?.nativeElement;
    if (!textarea) return false;
    return this.promptExpanded || textarea.scrollHeight > textarea.clientHeight;
  }

  getPromptShortcutsMargin() {
    const textarea = this.textareaPromptRef?.nativeElement;
    if (textarea && textarea.clientHeight < textarea.scrollHeight) return '0 1em 0 0';
    return '0';
  }

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
            this.transloco.translate('playground.playground.importError'),
            this.transloco.translate('playground.playground.importModalTitle'),
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

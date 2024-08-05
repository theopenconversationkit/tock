import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { debounceTime, forkJoin, Observable, of, Subject, take, takeUntil } from 'rxjs';
import { BotService } from '../../bot/bot-service';
import { StoryDefinitionConfiguration, StorySearchQuery } from '../../bot/model/story';
import { RestService } from '../../core-nlp/rest/rest.service';
import { StateService } from '../../core-nlp/state.service';
import { DefaultPrompt, EnginesConfigurations } from './models/engines-configurations';
import { RagSettings } from './models';
import { NbToastrService, NbWindowService } from '@nebular/theme';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { deepCopy } from '../../shared/utils';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { DebugViewerWindowComponent } from '../../shared/components/debug-viewer-window/debug-viewer-window.component';
import { EnginesConfiguration, LLMProvider } from '../../shared/model/ai-settings';

interface RagSettingsForm {
  id: FormControl<string>;
  enabled: FormControl<boolean>;

  noAnswerSentence: FormControl<string>;
  noAnswerStoryId: FormControl<string>;

  indexSessionId: FormControl<string>;

  llmEngine: FormControl<LLMProvider>;
  llmSetting: FormGroup<any>;
  emEngine: FormControl<LLMProvider>;
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

  defaultPrompt = DefaultPrompt;

  availableStories: StoryDefinitionConfiguration[];

  filteredStories$: Observable<StoryDefinitionConfiguration[]>;

  settingsBackup: RagSettings;

  isSubmitted: boolean = false;

  loading: boolean = false;

  constructor(
    private botService: BotService,
    private state: StateService,
    private rest: RestService,
    private toastrService: NbToastrService,
    private botConfiguration: BotConfigurationService,
    private nbWindowService: NbWindowService
  ) {}

  ngOnInit(): void {
    this.form.valueChanges.pipe(takeUntil(this.destroy$), debounceTime(200)).subscribe(() => {
      this.setActivationDisabledState();
    });

    this.form
      .get('llmEngine')
      .valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((engine: LLMProvider) => {
        this.initFormSettings('llmSetting', engine);
      });

    this.form
      .get('emEngine')
      .valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((engine: LLMProvider) => {
        this.initFormSettings('emSetting', engine);
      });

    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((confs: BotApplicationConfiguration[]) => {
      this.loading = true;
      this.configurations = confs;
      this.form.reset();
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

  form = new FormGroup<RagSettingsForm>({
    id: new FormControl(null),
    enabled: new FormControl({ value: undefined, disabled: !this.canRagBeActivated() }),
    noAnswerSentence: new FormControl(undefined, [Validators.required]),
    noAnswerStoryId: new FormControl(undefined),
    indexSessionId: new FormControl(undefined),
    llmEngine: new FormControl(undefined, [Validators.required]),
    llmSetting: new FormGroup<any>({}),
    emEngine: new FormControl(undefined, [Validators.required]),
    emSetting: new FormGroup<any>({})
  });

  get enabled(): FormControl {
    return this.form.get('enabled') as FormControl;
  }
  get llmEngine(): FormControl {
    return this.form.get('llmEngine') as FormControl;
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

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  get getCurrentStoryLabel(): string {
    const currentStory = this.availableStories?.find((story) => story.storyId === this.noAnswerStoryId.value);
    return currentStory?.name || '';
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

  initFormSettings(group: 'llmSetting' | 'emSetting', provider: LLMProvider): void {
    let requiredConfiguration: EnginesConfiguration = EnginesConfigurations[group].find((c) => c.key === provider);

    if (requiredConfiguration) {
      // Purge existing controls that may contain values incompatible with a new control with the same name after engine change
      const existingGroupKeys = Object.keys(this.form.controls[group].controls);
      existingGroupKeys.forEach((key) => {
        this.form.controls[group].removeControl(key);
      });

      requiredConfiguration.params.forEach((param) => {
        this.form.controls[group].addControl(param.key, new FormControl(param.defaultValue, Validators.required));
      });

      this.form.controls[group].addControl('provider', new FormControl(provider));
    }
  }

  private getRagSettingsLoader(): Observable<RagSettings> {
    const url = `/configuration/bots/${this.state.currentApplication.name}/rag`;
    return this.rest.get<RagSettings>(url, (settings: RagSettings) => settings);
  }

  initForm(settings: RagSettings) {
    this.initFormSettings('llmSetting', settings.llmSetting.provider);
    this.initFormSettings('emSetting', settings.emSetting.provider);
    this.form.patchValue({
      llmEngine: settings.llmSetting.provider,
      emEngine: settings.emSetting.provider
    });
    this.form.patchValue(settings);
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

  get currentLlmEngine(): EnginesConfiguration {
    return EnginesConfigurations['llmSetting'].find((e) => e.key === this.llmEngine.value);
  }

  get currentEmEngine(): EnginesConfiguration {
    return EnginesConfigurations['emSetting'].find((e) => e.key === this.emEngine.value);
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

  cancel(): void {
    this.initForm(this.settingsBackup);
  }

  submit(): void {
    this.isSubmitted = true;
    if (this.canSave && this.form.dirty) {
      this.loading = true;
      const formValue: RagSettings = deepCopy(this.form.value) as unknown as RagSettings;
      formValue.namespace = this.state.currentApplication.namespace;
      formValue.botId = this.state.currentApplication.name;
      formValue.noAnswerStoryId = this.noAnswerStoryId.value === 'null' ? null : this.noAnswerStoryId.value;
      delete formValue['llmEngine'];
      delete formValue['emEngine'];

      const url = `/configuration/bots/${this.state.currentApplication.name}/rag`;
      this.rest.post(url, formValue, null, null, true).subscribe({
        next: (ragSettings: RagSettings) => {
          if (!ragSettings.noAnswerStoryId) {
            ragSettings.noAnswerStoryId = null;
          }
          this.settingsBackup = ragSettings;
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

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}

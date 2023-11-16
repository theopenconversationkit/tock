import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { debounceTime, Subject, take, takeUntil } from 'rxjs';
import { BotService } from '../../bot/bot-service';
import { StoryDefinitionConfigurationSummary, StorySearchQuery } from '../../bot/model/story';
import { RestService } from '../../core-nlp/rest/rest.service';
import { StateService } from '../../core-nlp/state.service';
import { EnginesConfiguration, EnginesConfigurations } from './models/engines-configurations';
import { LLMProvider, RagSettings } from './models';
import { NbToastrService } from '@nebular/theme';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { deepCopy } from '../../shared/utils';
import { BotApplicationConfiguration } from '../../core/model/configuration';

interface RagSettingsForm {
  id: FormControl<string>;
  enabled: FormControl<boolean>;

  noAnswerSentence: FormControl<string>;
  noAnswerStoryId: FormControl<string>;

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

  availableStories: StoryDefinitionConfigurationSummary[];

  settingsBackup: RagSettings;

  isSubmitted: boolean = false;

  loading: boolean = false;

  scrolled: boolean = false;
  prevScrollVal: number;

  @HostListener('window:scroll')
  onScroll() {
    const offset = 78;
    const verticalOffset = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop || 0;

    if (verticalOffset === 0 && this.prevScrollVal > offset) return; // deal with <nb-select> reseting page scroll when opening select

    this.scrolled = verticalOffset > offset ? true : false;
    this.prevScrollVal = verticalOffset;
  }

  constructor(
    private botService: BotService,
    private state: StateService,
    private rest: RestService,
    private toastrService: NbToastrService,
    private botConfiguration: BotConfigurationService
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
        this.loadAvailableStories();
        this.load();
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

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  initFormSettings(group: 'llmSetting' | 'emSetting', provider: LLMProvider) {
    let requiredConfiguration: EnginesConfiguration = EnginesConfigurations[group].find((c) => c.key === provider);

    if (requiredConfiguration) {
      // Purge existing controls that may contain values incompatible with a new control with the same name
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

  private load() {
    const url = `/configuration/bots/${this.state.currentApplication.name}/rag`;
    this.rest
      .get<RagSettings>(url, (settings: RagSettings) => settings)
      .subscribe((settings: RagSettings) => {
        if (settings?.id) {
          this.settingsBackup = deepCopy(settings);
          this.initForm(settings);
        }
        this.loading = false;
      });
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

  cancel(): void {
    this.initForm(this.settingsBackup);
  }

  canRagBeActivated(): boolean {
    return this.form ? this.form.valid : false;
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

  private loadAvailableStories(): void {
    this.botService
      .searchStories(
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
      .pipe(take(1))
      .subscribe((stories: StoryDefinitionConfigurationSummary[]) => {
        this.availableStories = stories;
      });
  }

  submit(): void {
    this.isSubmitted = true;
    if (this.canSave && this.form.dirty) {
      const formValue: RagSettings = deepCopy(this.form.value) as unknown as RagSettings;
      formValue.namespace = this.state.currentApplication.namespace;
      formValue.botId = this.state.currentApplication.name;
      delete formValue['llmEngine'];
      delete formValue['emEngine'];

      const url = `/configuration/bots/${this.state.currentApplication.name}/rag`;
      this.rest.post(url, formValue).subscribe((ragSettings: RagSettings) => {
        this.settingsBackup = ragSettings;
        this.form.patchValue(ragSettings);
        this.form.markAsPristine();
        this.isSubmitted = false;
        this.toastrService.success(`Rag settings succesfully saved`, 'Success', {
          duration: 5000,
          status: 'success'
        });
      });
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}

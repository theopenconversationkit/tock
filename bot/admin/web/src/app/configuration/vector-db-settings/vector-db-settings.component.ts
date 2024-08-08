import { Component, OnInit } from '@angular/core';
import { StateService } from '../../core-nlp/state.service';
import { RestService } from '../../core-nlp/rest/rest.service';
import { NbToastrService, NbWindowService } from '@nebular/theme';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { Observable, Subject, debounceTime, takeUntil } from 'rxjs';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { VectorDbProvider, ProvidersConfiguration, ProvidersConfigurations } from './models/providers-configuration';
import { VectorDbSettings } from './models/vector-db-settings';
import { deepCopy } from '../../shared/utils';
import { DebugViewerWindowComponent } from '../../shared/components';

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

  constructor(
    public state: StateService,
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
      .get('vectorDbProvider')
      .valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((provider: VectorDbProvider) => {
        this.initFormSettings(provider);
      });

    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((confs: BotApplicationConfiguration[]) => {
      this.loading = true;
      this.configurations = confs;
      this.form.reset();

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
    const url = `/configuration/bots/${this.state.currentApplication.name}/vector-store`;
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

  get currentVectorDbProvider(): ProvidersConfiguration {
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
    let requiredConfiguration: ProvidersConfiguration = ProvidersConfigurations.find((c) => c.key === provider);

    if (requiredConfiguration) {
      // Purge existing controls that may contain values incompatible with a new control with the same name if provider change
      const existingGroupKeys = Object.keys(this.form.controls['setting'].controls);
      existingGroupKeys.forEach((key) => {
        this.form.controls['setting'].removeControl(key);
      });

      requiredConfiguration.params.forEach((param) => {
        let defaultValue = param.defaultValue;
        if (param.computedDefaultValue) {
          defaultValue = param.computedDefaultValue({
            namespace: this.state.currentApplication.namespace,
            botId: this.state.currentApplication.name
          });
        }

        this.form.controls['setting'].addControl(
          param.key,
          new FormControl({ value: defaultValue, disabled: param.disabled }, Validators.required)
        );
      });

      this.form.controls['setting'].addControl('provider', new FormControl(provider));
    }
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

      const url = `/configuration/bots/${this.state.currentApplication.name}/vector-store`;
      this.rest.post(url, formValue, null, null, true).subscribe({
        next: (vectorDbSettings: VectorDbSettings) => {
          this.settingsBackup = vectorDbSettings;
          this.form.patchValue(vectorDbSettings);
          this.form.markAsPristine();
          this.isSubmitted = false;
          this.toastrService.success(`Vector DB settings succesfully saved`, 'Success', {
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

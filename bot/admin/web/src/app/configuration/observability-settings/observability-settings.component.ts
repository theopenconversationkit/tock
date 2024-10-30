import { Component, OnDestroy, OnInit } from '@angular/core';
import { StateService } from '../../core-nlp/state.service';
import { RestService } from '../../core-nlp/rest/rest.service';
import { NbDialogService, NbToastrService, NbWindowService } from '@nebular/theme';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { Observable, Subject, debounceTime, takeUntil } from 'rxjs';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ObservabilityProvider, ProvidersConfiguration, ProvidersConfigurations } from './models/providers-configuration';
import { ObservabilitySettings } from './models/observability-settings';
import { deepCopy } from '../../shared/utils';
import { ChoiceDialogComponent, DebugViewerWindowComponent } from '../../shared/components';

interface ObservabilitySettingsForm {
  id: FormControl<string>;
  enabled: FormControl<boolean>;
  observabilityProvider: FormControl<ObservabilityProvider>;
  setting: FormGroup<any>;
}

@Component({
  selector: 'tock-observability-settings',
  templateUrl: './observability-settings.component.html',
  styleUrls: ['./observability-settings.component.scss']
})
export class ObservabilitySettingsComponent implements OnInit, OnDestroy {
  destroy$: Subject<unknown> = new Subject();

  loading: boolean = false;

  isSubmitted: boolean = false;

  configurations: BotApplicationConfiguration[];

  providersConfigurations = ProvidersConfigurations;

  settingsBackup: ObservabilitySettings;

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
      .get('observabilityProvider')
      .valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((provider: ObservabilityProvider) => {
        this.initFormSettings(provider);
      });

    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((confs: BotApplicationConfiguration[]) => {
      delete this.settingsBackup;
      this.loading = true;
      this.configurations = confs;
      this.form.reset();

      if (confs.length) {
        this.getObservabilitySettingsLoader().subscribe((res) => {
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

  private getObservabilitySettingsLoader(): Observable<ObservabilitySettings> {
    const url = `/configuration/bots/${this.state.currentApplication.name}/observability`;
    return this.rest.get<ObservabilitySettings>(url, (settings: ObservabilitySettings) => settings);
  }

  form = new FormGroup<ObservabilitySettingsForm>({
    id: new FormControl(null),
    enabled: new FormControl({ value: undefined, disabled: !this.canBeActivated() }),
    observabilityProvider: new FormControl(undefined, [Validators.required]),
    setting: new FormGroup<any>({})
  });

  get enabled(): FormControl {
    return this.form.get('enabled') as FormControl;
  }

  get observabilityProvider(): FormControl {
    return this.form.get('observabilityProvider') as FormControl;
  }

  get currentObservabilityProvider(): ProvidersConfiguration {
    return ProvidersConfigurations.find((e) => e.key === this.observabilityProvider.value);
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

  initForm(settings: ObservabilitySettings): void {
    this.initFormSettings(settings.setting.provider);
    this.form.patchValue({
      observabilityProvider: settings.setting.provider
    });
    this.form.patchValue(settings);
    this.form.markAsPristine();
  }

  initFormSettings(provider: ObservabilityProvider): void {
    let requiredConfiguration: ProvidersConfiguration = ProvidersConfigurations.find((c) => c.key === provider);

    if (requiredConfiguration) {
      // Purge existing controls that may contain values incompatible with a new control with the same name if provider change
      const existingGroupKeys = Object.keys(this.form.controls['setting'].controls);
      existingGroupKeys.forEach((key) => {
        this.form.controls['setting'].removeControl(key);
      });

      requiredConfiguration.params.forEach((param) => {
        this.form.controls['setting'].addControl(param.key, new FormControl(param.defaultValue, Validators.required));
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
      const formValue: ObservabilitySettings = deepCopy(this.form.value) as unknown as ObservabilitySettings;
      formValue.namespace = this.state.currentApplication.namespace;
      formValue.botId = this.state.currentApplication.name;

      delete formValue['observabilityProvider'];

      const url = `/configuration/bots/${this.state.currentApplication.name}/observability`;
      this.rest.post(url, formValue, null, null, true).subscribe({
        next: (observabilitySettings: ObservabilitySettings) => {
          this.settingsBackup = observabilitySettings;
          this.form.patchValue(observabilitySettings);
          this.form.markAsPristine();
          this.isSubmitted = false;
          this.toastrService.success(`Observability settings succesfully saved`, 'Success', {
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

  confirmSettingsDeletion() {
    const confirmAction = 'Delete';
    const cancelAction = 'Cancel';

    const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
      context: {
        title: `Delete observability settings`,
        subtitle: `Are you sure you want to delete the currently saved observability settings?`,
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
    const url = `/configuration/bots/${this.state.currentApplication.name}/observability`;
    this.rest.delete<boolean>(url).subscribe(() => {
      delete this.settingsBackup;
      this.form.reset();
      this.form.markAsPristine();
      this.toastrService.success(`Observability settings succesfully deleted`, 'Success', {
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

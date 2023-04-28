import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { Observable, of, Subject } from 'rxjs';
import { take, takeUntil } from 'rxjs/operators';

import { BotService } from '../../../bot/bot-service';
import { StoryDefinitionConfigurationSummary, StorySearchQuery } from '../../../bot/model/story';
import { StateService } from '../../../core-nlp/state.service';
import { ChoiceDialogComponent } from '../../../shared/components';
import { ScenarioSettings } from '../../models';
import { ScenarioSettingsService } from '../../services';

@Component({
  selector: 'tock-scenarios-settings',
  templateUrl: './scenarios-settings.component.html',
  styleUrls: ['./scenarios-settings.component.scss']
})
export class ScenariosSettingsComponent implements OnInit, OnDestroy {
  @Output() onClose = new EventEmitter<boolean>();

  private destroy$ = new Subject();

  loading: boolean = false;
  isSubmitted: boolean = false;

  availableStories: StoryDefinitionConfigurationSummary[] = [];

  form = new FormGroup({
    actionRepetitionNumber: new FormControl<number>(2, [Validators.required, Validators.min(0)]),
    redirectStoryId: new FormControl<string>({ value: null, disabled: false })
  });

  get actionRepetitionNumber(): FormControl {
    return this.form.get('actionRepetitionNumber') as FormControl;
  }

  get redirectStoryId(): FormControl {
    return this.form.get('redirectStoryId') as FormControl;
  }

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  constructor(
    private botService: BotService,
    private nbDialogService: NbDialogService,
    private toastrService: NbToastrService,
    private scenarioSettingsService: ScenarioSettingsService,
    private stateService: StateService
  ) {}

  ngOnInit(): void {
    this.actionRepetitionNumber.valueChanges.pipe(takeUntil(this.destroy$)).subscribe((value) => {
      if (value === 0) {
        this.redirectStoryId.reset();
        this.redirectStoryId.disable();
        this.redirectStoryId.clearValidators();
        this.form.updateValueAndValidity();
      } else {
        this.redirectStoryId.setValidators(Validators.required);
        this.redirectStoryId.enable();
        this.form.updateValueAndValidity();
      }
    });

    this.getStories();
    this.getSettings();
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  getSettings(): void {
    this.loading = true;

    this.scenarioSettingsService
      .getSettings(this.stateService.currentApplication._id)
      .pipe(take(1))
      .subscribe({
        next: (settings: ScenarioSettings) => {
          this.form.patchValue({
            actionRepetitionNumber: settings.actionRepetitionNumber,
            redirectStoryId: settings.redirectStoryId
          });
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  getStories(): void {
    // voir pour spécifier une catégorie "satisfaction" afin d'éviter de mettre une taille de 10.000
    this.botService
      .searchStories(
        new StorySearchQuery(
          this.stateService.currentApplication.namespace,
          this.stateService.currentApplication.name,
          this.stateService.currentLocale,
          0,
          10000,
          undefined,
          undefined,
          false
        )
      )
      .pipe(take(1))
      .subscribe((stories: StoryDefinitionConfigurationSummary[]) => {
        this.availableStories = stories.filter((story) => story.category !== 'faq');
      });
  }

  close(): Observable<any> {
    const validAction = 'yes';
    if (this.form.dirty) {
      const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
        context: {
          title: `Cancel edit settings`,
          subtitle: 'Are you sure you want to cancel ? Changes will not be saved.',
          actions: [
            { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
            { actionName: validAction, buttonStatus: 'danger' }
          ],
          modalStatus: 'danger'
        }
      });
      dialogRef.onClose.subscribe((result) => {
        if (result === validAction) {
          this.onClose.emit(true);
        }
      });
      return dialogRef.onClose;
    } else {
      this.onClose.emit(true);
      return of(validAction);
    }
  }

  save(): void {
    this.isSubmitted = true;

    if (this.canSave) {
      this.saveSettings(this.form.value as ScenarioSettings);
    }
  }

  saveSettings(settings: ScenarioSettings): void {
    this.loading = true;

    this.scenarioSettingsService
      .saveSettings(this.stateService.currentApplication._id, settings)
      .pipe(take(1))
      .subscribe({
        next: () => {
          this.loading = false;
          this.onClose.emit(true);
          this.toastrService.success(`Settings successfully updated`, 'Success');
        },
        error: () => {
          this.loading = false;
        }
      });
  }
}

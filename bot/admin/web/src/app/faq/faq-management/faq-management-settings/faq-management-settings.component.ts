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

import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Observable, of, Subject } from 'rxjs';
import { take, takeUntil } from 'rxjs/operators';
import { NbToastrService } from '@nebular/theme';

import { BotService } from '../../../bot/bot-service';
import { DialogService } from '../../../core-nlp/dialog.service';
import { StoryDefinitionConfigurationSummary, StorySearchQuery } from '../../../bot/model/story';
import { StateService } from '../../../core-nlp/state.service';
import { Settings } from '../../models';
import { FaqService } from '../../services/faq.service';
import { ChoiceDialogComponent } from '../../../shared/components';

interface SettingsForm {
  satisfactionEnabled: FormControl<boolean>;
  satisfactionStoryId?: FormControl<string>;
}

@Component({
  selector: 'tock-faq-management-settings',
  templateUrl: './faq-management-settings.component.html',
  styleUrls: ['./faq-management-settings.component.scss']
})
export class FaqManagementSettingsComponent implements OnInit, OnDestroy {
  @Output() onClose = new EventEmitter<boolean>();
  @Output() onSave = new EventEmitter<Settings>();

  private readonly destroy$: Subject<boolean> = new Subject();
  loading: boolean = false;
  isSubmitted: boolean = false;

  availableStories: StoryDefinitionConfigurationSummary[] = [];

  form = new FormGroup<SettingsForm>({
    satisfactionEnabled: new FormControl(false),
    satisfactionStoryId: new FormControl({ value: null, disabled: true })
  });

  get satisfactionEnabled(): FormControl {
    return this.form.get('satisfactionEnabled') as FormControl;
  }

  get satisfactionStoryId(): FormControl {
    return this.form.get('satisfactionStoryId') as FormControl;
  }

  get canSave(): boolean {
    return this.form.valid;
  }

  constructor(
    private botService: BotService,
    private stateService: StateService,
    private faqService: FaqService,
    private dialogService: DialogService,
    private toastrService: NbToastrService
  ) {}

  ngOnInit(): void {
    this.satisfactionEnabled.valueChanges.pipe(takeUntil(this.destroy$)).subscribe((value) => {
      if (!value) {
        this.satisfactionStoryId.reset();
        this.satisfactionStoryId.disable();
        this.satisfactionStoryId.clearValidators();
        this.form.updateValueAndValidity();
      } else {
        this.satisfactionStoryId.setValidators(Validators.required);
        this.satisfactionStoryId.enable();
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

    this.faqService
      .getSettings(this.stateService.currentApplication._id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (settings: Settings) => {
          this.form.patchValue({
            satisfactionEnabled: settings.satisfactionEnabled,
            satisfactionStoryId: settings.satisfactionStoryId
          });
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  getStories(): void {
    this.botService
      .searchStories(
        new StorySearchQuery(
          this.stateService.currentApplication.namespace,
          this.stateService.currentApplication.name,
          this.stateService.currentLocale,
          0,
          10000
        )
      )
      .pipe(takeUntil(this.destroy$))
      .subscribe((stories: StoryDefinitionConfigurationSummary[]) => {
        this.availableStories = stories.filter((story) => story.category !== 'faq');
      });
  }

  close(): Observable<any> {
    const action = 'yes';
    if (this.form.dirty) {
      const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
        context: {
          title: `Cancel edit settings`,
          subtitle: 'Are you sure you want to cancel ? Changes will not be saved.',
          actions: [
            { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
            { actionName: action, buttonStatus: 'danger' }
          ]
        }
      });
      dialogRef.onClose.subscribe((result) => {
        if (result === action) {
          this.onClose.emit(true);
        }
      });
      return dialogRef.onClose;
    } else {
      this.onClose.emit(true);
      return of(action);
    }
  }

  save(): void {
    this.isSubmitted = true;

    if (this.canSave) {
      if (!this.satisfactionEnabled.value) {
        const action = 'yes';
        const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
          context: {
            title: `Disable satisfaction`,
            subtitle: 'This will disable the satisfaction question for all FAQs. Do you confirm ?',
            actions: [
              { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
              { actionName: action, buttonStatus: 'danger' }
            ]
          }
        });
        dialogRef.onClose.subscribe((result) => {
          if (result === action) {
            this.saveSettings(this.form.value as Settings);
          }
        });
      } else {
        this.saveSettings(this.form.value as Settings);
      }
    }
  }

  saveSettings(settings: Settings): void {
    this.loading = true;

    this.faqService
      .saveSettings(this.stateService.currentApplication._id, settings)
      .pipe(take(1))
      .subscribe({
        next: () => {
          this.loading = false;
          this.form.reset();
          this.close();
          this.toastrService.success(`Settings successfully updated`, 'Success', { duration: 5000 });
        },
        error: () => {
          this.loading = false;
        }
      });
  }
}

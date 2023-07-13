import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogService, NbTagComponent, NbTagInputAddEvent } from '@nebular/theme';
import { Observable, of, take } from 'rxjs';

import { ScenarioAnswer, ScenarioGroup } from '../../models';
import { ScenarioService } from '../../services';
import { ChoiceDialogComponent } from '../../../shared/components';
import { StateService } from '../../../core-nlp/state.service';
import { UserInterfaceType } from '../../../core/model/configuration';
import { I18nLabel } from '../../../bot/model/i18n';
import { BotService } from '../../../bot/bot-service';

interface ScenarioGroupEditForm {
  category: FormControl<string>;
  description: FormControl<string>;
  name: FormControl<string>;
  tags: FormArray<FormControl<string>>;
  unknownAnswers: FormArray<FormGroup<ScenarioUnknownAnswerForm>>;
}

interface ScenarioUnknownAnswerForm {
  locale: FormControl<string>;
  answer: FormControl<string>;
  interfaceType: FormControl<number>;
}

export type ScenarioEditOnSave = {
  scenarioGroup: ScenarioGroup;
  unknownAnswers: ScenarioAnswer[];
  redirect: boolean;
  i18nLabel?: I18nLabel;
};

@Component({
  selector: 'tock-scenario-edit',
  templateUrl: './scenario-edit.component.html',
  styleUrls: ['./scenario-edit.component.scss']
})
export class ScenarioEditComponent implements OnChanges {
  @Input() loading: boolean;
  @Input() scenarioGroup!: ScenarioGroup;

  @Output() onClose = new EventEmitter<boolean>();
  @Output() onSave = new EventEmitter<ScenarioEditOnSave>();

  isSubmitted: boolean = false;
  categories: string[];
  i18nLabel: I18nLabel;
  categoriesAutocompleteValues: Observable<string[]>;
  tagsAutocompleteValues: Observable<string[]>;
  form = new FormGroup<ScenarioGroupEditForm>({
    category: new FormControl(),
    description: new FormControl(),
    name: new FormControl(undefined, Validators.required),
    tags: new FormArray([]),
    unknownAnswers: new FormArray([])
  });

  get category(): FormControl {
    return this.form.get('category') as FormControl;
  }

  get description(): FormControl {
    return this.form.get('description') as FormControl;
  }

  get name(): FormControl {
    return this.form.get('name') as FormControl;
  }

  get tags(): FormArray {
    return this.form.get('tags') as FormArray;
  }

  get unknownAnswers(): FormArray<FormGroup<ScenarioUnknownAnswerForm>> {
    return this.form.get('unknownAnswers') as FormArray;
  }

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  constructor(
    private botService: BotService,
    private nbDialogService: NbDialogService,
    private scenarioService: ScenarioService,
    private stateService: StateService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (!this.unknownAnswers.value.length) this.initUnknownAnswer();

    if (changes.scenarioGroup?.currentValue) {
      const scenarioGroup: ScenarioGroup = changes.scenarioGroup.currentValue;

      this.resetForm();
      this.isSubmitted = false;
      this.form.patchValue(scenarioGroup);

      if (scenarioGroup.tags?.length) {
        scenarioGroup.tags.forEach((tag) => {
          this.tags.push(new FormControl(tag));
        });
      }

      if (scenarioGroup.unknownAnswerId) this.loadI18nLabels(scenarioGroup.unknownAnswerId);
    }

    this.categories = [...this.scenarioService.getState().categories];
    this.tagsAutocompleteValues = of([...this.scenarioService.getState().tags]);
  }

  private loadI18nLabels(unknownAnswerId: string): void {
    this.loading = true;

    this.botService
      .i18nLabel(unknownAnswerId)
      .pipe(take(1))
      .subscribe({
        next: (i18nLabel: I18nLabel) => {
          this.i18nLabel = i18nLabel;
          if (i18nLabel) this.feedUnknownAnswers(i18nLabel);
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  private resetForm(): void {
    this.name.reset();
    this.category.reset();
    this.description.reset();
    this.tags.reset();
    this.unknownAnswers.controls.forEach((unknownAnswer) => {
      unknownAnswer.get('answer').reset();
    });
  }

  private initUnknownAnswer(): void {
    this.stateService.currentApplication.supportedLocales.forEach((supportedLocale: string) => {
      if (!this.unknownAnswers.value.find((answer: ScenarioAnswer) => answer.locale === supportedLocale)) {
        this.unknownAnswers.push(this.addLocaleAnswer(supportedLocale));
      }
    });
  }

  private addLocaleAnswer(locale: string): FormGroup {
    return new FormGroup<ScenarioUnknownAnswerForm>({
      locale: new FormControl(locale),
      interfaceType: new FormControl(UserInterfaceType.textChat),
      answer: new FormControl(null, Validators.required)
    });
  }

  private feedUnknownAnswers(i18nLabel: I18nLabel): void {
    this.unknownAnswers.controls.forEach((unknownAnswer) => {
      const unknownAnswerLabel = i18nLabel.i18n.find(
        (i) => i.locale === unknownAnswer.get('locale').value && i.interfaceType === unknownAnswer.get('interfaceType').value
      )?.label;

      if (unknownAnswerLabel) unknownAnswer.get('answer').setValue(unknownAnswerLabel);
    });
  }

  resetLocaleUnknownAnswer(i: number): void {
    this.unknownAnswers.at(i).get('answer').reset();
    this.form.markAsDirty();
    this.form.markAsTouched();
  }

  updateTagsAutocompleteValues(event: any): void {
    this.tagsAutocompleteValues = of(
      this.scenarioService.getState().tags.filter((tag) => tag.toLowerCase().includes(event.target.value.toLowerCase()))
    );
  }

  tagAdd({ value, input }: NbTagInputAddEvent): void {
    if (value && !this.tags.value.find((v: string) => v.toUpperCase() === value.toUpperCase())) {
      this.tags.push(new FormControl(value));
      this.form.markAsDirty();
      this.form.markAsTouched();
    }

    input.nativeElement.value = '';
  }

  tagRemove(tag: NbTagComponent): void {
    const tagToRemove = this.tags.value.findIndex((t: string) => t === tag.text);

    if (tagToRemove !== -1) {
      this.tags.removeAt(tagToRemove);
      this.form.markAsDirty();
      this.form.markAsTouched();
    }
  }

  close(): Observable<any> {
    const validAction = 'yes';
    if (this.form.dirty) {
      const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
        context: {
          title: `Cancel ${this.scenarioGroup?.id ? 'edit' : 'create'} scenario`,
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

  save(redirect = false): void {
    this.isSubmitted = true;

    if (this.canSave) {
      const enabled = typeof this.scenarioGroup.enabled === 'boolean' ? this.scenarioGroup.enabled : null;
      const { category, description, name, tags, unknownAnswers } = this.form.value;

      this.onSave.emit({
        redirect,
        scenarioGroup: {
          id: this.scenarioGroup.id,
          unknownAnswerId: this.scenarioGroup.unknownAnswerId,
          category,
          description,
          name,
          tags,
          enabled
        },
        unknownAnswers: unknownAnswers as ScenarioAnswer[],
        i18nLabel: this.i18nLabel
      });
    }
  }

  eventPreventDefault(e: KeyboardEvent): void {
    e.preventDefault();
  }
}

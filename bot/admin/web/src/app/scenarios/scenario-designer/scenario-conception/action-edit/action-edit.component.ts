import { Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { FormArray, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { Observable, of } from 'rxjs';

import { StateService } from '../../../../core-nlp/state.service';
import {
  ScenarioVersion,
  ScenarioItem,
  ScenarioContext,
  Handler,
  ACTION_OR_CONTEXT_NAME_MINLENGTH,
  ScenarioAnswer,
  ScenarioActionDefinition
} from '../../../models';
import { getContrastYIQ, getScenarioActionDefinitions, getScenarioActions, normalizedSnakeCaseUpper } from '../../../commons/utils';
import { entityColor, qualifiedName, qualifiedRole } from '../../../../model/nlp';
import { UserInterfaceType } from '../../../../core/model/configuration';
import { deepCopy } from '../../../../shared/utils';
import { StoryDefinitionConfigurationSummary } from '../../../../bot/model/story';

type InputOrOutputContext = 'input' | 'output';

interface ActionEditForm {
  description: FormControl<string>;
  name: FormControl<string>;
  handler: FormControl<string>;
  inputContextNames: FormArray<FormControl<string>>;
  outputContextNames: FormArray<FormControl<string>>;
  answerId: FormControl<string>;
  trigger: FormControl<string>;
  targetStory: FormControl<string>;
  final: FormControl<boolean>;
  answers: FormArray<FormControl<ScenarioAnswer>>;
  unknownAnswers: FormArray<FormGroup<ScenarioAnswerForm>>;
}

interface ScenarioAnswerForm {
  locale: FormControl<string>;
  answer: FormControl<string>;
  interfaceType: FormControl<number>;
}
@Component({
  selector: 'tock-scenario-action-edit',
  templateUrl: './action-edit.component.html',
  styleUrls: ['./action-edit.component.scss']
})
export class ActionEditComponent implements OnInit {
  @Input() item: ScenarioItem;
  @Input() contexts: ScenarioContext[];
  @Input() scenario: ScenarioVersion;
  @Input() isReadonly: boolean;
  @Input() readonly avalaibleHandlers: Handler[] = [];
  @Input() readonly availableStories: StoryDefinitionConfigurationSummary[];

  @Output() saveModifications = new EventEmitter();
  @Output() deleteDefinition = new EventEmitter<ScenarioActionDefinition>();

  @ViewChild('inputContextsInput') inputContextsInput: ElementRef;
  @ViewChild('outputContextsInput') outputContextsInput: ElementRef;

  qualifiedName = qualifiedName;

  handlers: string[] = [];
  triggers: string[] = [];
  currentLocale: string;
  supportedLocales: string[] = [];
  showAnswersLocales: boolean = false;

  constructor(public dialogRef: NbDialogRef<ActionEditComponent>, private stateService: StateService) {
    this.currentLocale = stateService.currentLocale;
    this.supportedLocales = stateService.currentApplication.supportedLocales;
  }

  isSubmitted: boolean = false;

  form: FormGroup = new FormGroup<ActionEditForm>({
    name: new FormControl(undefined, [
      Validators.required,
      Validators.minLength(ACTION_OR_CONTEXT_NAME_MINLENGTH),
      this.isActionNameUnic()
    ]),
    description: new FormControl(),
    handler: new FormControl(),
    answers: new FormArray([]),
    answerId: new FormControl(),
    inputContextNames: new FormArray([]),
    outputContextNames: new FormArray([]),
    trigger: new FormControl(undefined, [this.actionHasHandler()]),
    targetStory: new FormControl(),
    unknownAnswers: new FormArray([]),
    final: new FormControl(false)
  });

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  get name(): FormControl {
    return this.form.get('name') as FormControl;
  }
  get description(): FormControl {
    return this.form.get('description') as FormControl;
  }
  get handler(): FormControl {
    return this.form.get('handler') as FormControl;
  }
  get answers(): FormArray {
    return this.form.get('answers') as FormArray;
  }
  get inputContextNames(): FormArray {
    return this.form.get('inputContextNames') as FormArray;
  }
  get outputContextNames(): FormArray {
    return this.form.get('outputContextNames') as FormArray;
  }
  get trigger(): FormControl {
    return this.form.get('trigger') as FormControl;
  }
  get targetStory(): FormControl {
    return this.form.get('targetStory') as FormControl;
  }
  get unknownAnswers(): FormArray {
    return this.form.get('unknownAnswers') as FormArray;
  }

  ngOnInit(): void {
    this.form.patchValue(this.item.actionDefinition);

    if (this.item.actionDefinition?.inputContextNames?.length) {
      this.item.actionDefinition.inputContextNames.forEach((contextName) => {
        this.inputContextNames.push(new FormControl(contextName));
      });
    }
    if (this.item.actionDefinition?.outputContextNames?.length) {
      this.item.actionDefinition.outputContextNames.forEach((contextName) => {
        this.outputContextNames.push(new FormControl(contextName));
      });
    }

    if (this.item.final) {
      this.form.patchValue({
        ...this.form.value,
        final: true
      });
    }

    if (!this.name.value) {
      this.form.patchValue({
        ...this.form.value,
        description: this.item.text
      });
    }

    this.handlers = this.avalaibleHandlers.map((handler) => handler.name);
    this.triggers = this.scenario.data.triggers || [];

    this.item.actionDefinition?.answers?.forEach((ua) => {
      this.answers.push(this.addLocaleAnswer(ua.locale, ua.answer));
    });

    this.item.actionDefinition?.unknownAnswers?.forEach((ua) => {
      this.unknownAnswers.push(this.addLocaleAnswer(ua.locale, ua.answer));
    });

    this.supportedLocales.forEach((sl) => {
      if (!this.answers.value.find((an) => an.locale === sl)) {
        this.answers.push(this.addLocaleAnswer(sl));
      }

      if (!this.unknownAnswers.value.find((an) => an.locale === sl)) {
        this.unknownAnswers.push(this.addLocaleAnswer(sl));
      }
    });

    // we sort the answers formArray so that the currentLocale is always displayed first
    const answers = this.answers.value.sort((a, b) => {
      if (a.locale === this.currentLocale) return -1;
      if (b.locale === this.currentLocale) return 1;
      return 0;
    });
    this.answers.patchValue(answers);
  }

  private addLocaleAnswer(locale: string, answer = ''): FormGroup<ScenarioAnswerForm> {
    return new FormGroup({
      locale: new FormControl(locale),
      interfaceType: new FormControl(UserInterfaceType.textChat),
      answer: new FormControl(answer)
    });
  }

  resetLocaleUnknownAnswer(i: number): void {
    this.unknownAnswers.at(i).get('answer').reset();
    this.form.markAsDirty();
    this.form.markAsTouched();
  }

  private isActionNameUnic(): ValidatorFn {
    return (c: FormControl): ValidationErrors | null => {
      if (!c.value || !this.scenario) return null;

      let formatedValue = normalizedSnakeCaseUpper(c.value.trim());

      const allOtherActionDefinitionNames = getScenarioActions(this.scenario)
        .filter((action) => action !== this.item)
        .filter((item) => item.actionDefinition)
        .map((action) => action.actionDefinition.name);

      if (allOtherActionDefinitionNames.includes(formatedValue)) {
        return { custom: 'This name is already used by another action' };
      }

      if (this.scenario.data.contexts.find((ctx) => ctx.name === formatedValue))
        return { custom: 'Action cannot have the same name as a context' };

      return null;
    };
  }

  private actionHasHandler(): ValidatorFn {
    return (c: FormControl): ValidationErrors | null => {
      if (!c.value) return null;

      return c.value && !this.handler.value ? { custom: 'An event can only be positioned on an action with a handler' } : null;
    };
  }

  targetStoryWasDeleted(): boolean {
    if (this.targetStory.value && !this.availableStories.find((as) => as.storyId === this.targetStory.value)) return true;

    return false;
  }

  findStory(id: string): StoryDefinitionConfigurationSummary | undefined {
    return this.availableStories.find((availableStorie) => availableStorie.storyId === id);
  }

  copyDescToAnswer(): void {
    const answers = this.form.controls.answers.value;
    const currLocale = answers.find((a) => a.locale === this.currentLocale);
    currLocale.answer = this.description.value;
    this.form.controls.answers.setValue(answers);
    this.form.markAsDirty();
  }

  copyDescToName(): void {
    this.form.patchValue({
      ...this.form.value,
      name: this.description.value
    });
    this.formatActionName();
    this.form.markAsDirty();
  }

  contextsAutocompleteValues: Observable<string[]>;

  updateContextsAutocompleteValues(event?: KeyboardEvent): void {
    let results = this.contexts.map((ctx) => ctx.name);

    ['inputContextNames', 'outputContextNames'].forEach((wich: string) => {
      results = results.filter((r: string) => {
        return !this[wich].value.includes(r);
      });
    });

    if (event) {
      const targetEvent = event.target as HTMLInputElement;
      results = results.filter((ctxName: string) => ctxName.includes(targetEvent.value.toUpperCase()));
    }

    this.contextsAutocompleteValues = of(results);
  }

  formatActionName(): void {
    if (this.name.value) {
      this.form.patchValue({
        ...this.form.value,
        name: normalizedSnakeCaseUpper(this.name.value)
      });
    }
  }

  inputContextsAddError: any = {};
  outputContextsAddError: any = {};

  resetContextsAddErrors(): void {
    this.inputContextsAddError = {};
    this.outputContextsAddError = {};
  }

  getContextInputElemRef(wich: InputOrOutputContext): ElementRef {
    if (wich === 'input') return this.inputContextsInput;
    if (wich === 'output') return this.outputContextsInput;
  }

  getContextNamesRef(wich: InputOrOutputContext): FormArray {
    if (wich === 'input') return this.inputContextNames;
    if (wich === 'output') return this.outputContextNames;
  }

  getContextAddErrorRef(wich: InputOrOutputContext): { errors: {} } {
    if (wich === 'input') return this.inputContextsAddError;
    if (wich === 'output') return this.outputContextsAddError;
  }

  addContext(wich: InputOrOutputContext): void {
    const eventTarget = this.getContextInputElemRef(wich).nativeElement;
    const ctxName = normalizedSnakeCaseUpper(eventTarget.value);

    this.resetContextsAddErrors();

    if (!ctxName) return;

    if (ctxName.length < ACTION_OR_CONTEXT_NAME_MINLENGTH) {
      this.getContextAddErrorRef(wich).errors = { minlength: { requiredLength: ACTION_OR_CONTEXT_NAME_MINLENGTH } };

      return;
    }

    const actions = getScenarioActionDefinitions(this.scenario);

    if (actions.find((act) => act.name === ctxName)) {
      this.getContextAddErrorRef(wich).errors = { custom: 'A context cannot have the same name as an action' };
      return;
    }

    if (this.getContextNamesRef(wich).value.find((ctx: string) => ctx == ctxName)) {
      this.getContextAddErrorRef(wich).errors = { custom: `This ${wich} context is already associated with this action` };
      return;
    }

    const otherWich = wich == 'input' ? 'output' : 'input';
    if (this.getContextNamesRef(otherWich).value.find((ctx: string) => ctx == ctxName)) {
      this.getContextAddErrorRef(wich).errors = {
        custom: `This context is already associated with the ${otherWich} contexts of this action`
      };
      return;
    }

    this.getContextNamesRef(wich).push(new FormControl(ctxName));
    eventTarget.value = '';
    this.form.markAsDirty();
    this.updateContextsAutocompleteValues();
  }

  removeContext(wich: InputOrOutputContext, contextName: string): void {
    const contextNamesArray = this.getContextNamesRef(wich);
    contextNamesArray.removeAt(contextNamesArray.value.findIndex((ctx: string) => ctx === contextName));
    this.form.markAsDirty();
  }

  removeActionDefinition(): void {
    this.deleteDefinition.emit();
  }

  getContextByName(context: string): ScenarioContext[] {
    return [
      this.contexts.find((ctx) => {
        return ctx.name == context;
      })
    ];
  }

  getContextEntityColor(context: ScenarioContext): string | undefined {
    if (context.entityType) return entityColor(qualifiedRole(context.entityType, context.entityRole));
  }

  getContextEntityContrast(context: ScenarioContext): string | undefined {
    if (context.entityType) return getContrastYIQ(entityColor(qualifiedRole(context.entityType, context.entityRole)));
  }

  save(): void {
    this.isSubmitted = true;

    if (this.canSave) {
      if (this.handler.value === '') this.handler.setValue(null);
      this.saveModifications.emit(this.form.value);
    }
  }

  checkUnvalidatedInputs() {
    for (let wich in InOrOut) {
      const ctxInput = this.getContextInputElemRef(wich as InOrOut)?.nativeElement;
      if (ctxInput?.value.trim().length) {
        return false;
      }
    }

    return true;
  }

  cancel(): void {
    this.dialogRef.close();
  }
}

import { Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { FormArray, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { Observable, of } from 'rxjs';

import { StateService } from '../../../../core-nlp/state.service';
import { ScenarioVersion, ScenarioItem, ScenarioContext, Handler, ACTION_OR_CONTEXT_NAME_MINLENGTH } from '../../../models';
import { getContrastYIQ, getScenarioActionDefinitions, getScenarioActions, normalizedSnakeCaseUpper } from '../../../commons/utils';
import { entityColor, qualifiedName, qualifiedRole } from '../../../../model/nlp';
import { UserInterfaceType } from '../../../../core/model/configuration';

type InputOrOutputContext = 'input' | 'output';

@Component({
  selector: 'scenario-action-edit',
  templateUrl: './action-edit.component.html',
  styleUrls: ['./action-edit.component.scss']
})
export class ActionEditComponent implements OnInit {
  @Input() item: ScenarioItem;
  @Input() contexts: ScenarioContext[];
  @Input() scenario: ScenarioVersion;
  @Input() isReadonly: boolean;
  @Input() readonly avalaibleHandlers: Handler[] = [];

  @Output() saveModifications = new EventEmitter();
  @Output() deleteDefinition = new EventEmitter();
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

  form: FormGroup = new FormGroup({
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
  get currentLocaleAnswer(): FormControl {
    return this.form.get('answers').value.find((a) => a.locale === this.currentLocale) as FormControl;
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
      this.answers.push(new FormControl(ua));
    });

    this.item.actionDefinition?.unknownAnswers?.forEach((ua) => {
      this.unknownAnswers.push(new FormControl(ua));
    });

    this.supportedLocales.forEach((sl) => {
      if (!this.answers.value.find((an) => an.locale === sl)) {
        this.answers.push(this.addLocaleAnswer(sl));
      }

      if (!this.unknownAnswers.value.find((an) => an.locale === sl)) {
        this.unknownAnswers.push(this.addLocaleAnswer(sl));
      }
    });
  }

  private addLocaleAnswer(locale: string): FormControl {
    return new FormControl({
      locale: locale,
      interfaceType: UserInterfaceType.textChat
    });
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

  cancel(): void {
    this.dialogRef.close();
  }
}

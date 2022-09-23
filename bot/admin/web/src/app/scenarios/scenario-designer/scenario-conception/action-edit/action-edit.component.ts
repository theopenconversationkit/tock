import { Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { FormArray, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { Observable, of } from 'rxjs';

import { StateService } from '../../../../core-nlp/state.service';
import { ScenarioVersion, ScenarioItem, ScenarioContext } from '../../../models';
import { getContrastYIQ, getScenarioActionDefinitions, getScenarioActions, normalizedSnakeCaseUpper } from '../../../commons/utils';
import { entityColor, qualifiedName, qualifiedRole } from '../../../../model/nlp';

const ENTITY_NAME_MINLENGTH = 5;

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
  @Input() readonly avalaibleHandlers: string[];

  @Output() saveModifications = new EventEmitter();
  @Output() deleteDefinition = new EventEmitter();
  @ViewChild('inputContextsInput') inputContextsInput: ElementRef;
  @ViewChild('outputContextsInput') outputContextsInput: ElementRef;

  private qualifiedName = qualifiedName;

  isSubmitted: boolean = false;

  form: FormGroup = new FormGroup({
    name: new FormControl(undefined, [Validators.required, Validators.minLength(ENTITY_NAME_MINLENGTH), this.isActionNameUnic()]),
    description: new FormControl(),
    handler: new FormControl(),
    answer: new FormControl(),
    answerId: new FormControl(),
    inputContextNames: new FormArray([]),
    outputContextNames: new FormArray([]),
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
  get answer(): FormControl {
    return this.form.get('answer') as FormControl;
  }
  get inputContextNames(): FormArray {
    return this.form.get('inputContextNames') as FormArray;
  }
  get outputContextNames(): FormArray {
    return this.form.get('outputContextNames') as FormArray;
  }

  constructor(public dialogRef: NbDialogRef<ActionEditComponent>, protected state: StateService) {}

  ngOnInit(): void {
    this.form.patchValue(this.item.tickActionDefinition);

    if (this.item.tickActionDefinition?.inputContextNames?.length) {
      this.item.tickActionDefinition.inputContextNames.forEach((contextName) => {
        this.inputContextNames.push(new FormControl(contextName));
      });
    }
    if (this.item.tickActionDefinition?.outputContextNames?.length) {
      this.item.tickActionDefinition.outputContextNames.forEach((contextName) => {
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
  }

  isActionNameUnic(): ValidatorFn {
    return (c: FormControl): ValidationErrors | null => {
      if (!c.value || !this.scenario) return null;

      let formatedValue = normalizedSnakeCaseUpper(c.value.trim());

      const allOtherActionDefinitionNames = getScenarioActions(this.scenario)
        .filter((action) => action !== this.item)
        .filter((item) => item.tickActionDefinition)
        .map((action) => action.tickActionDefinition.name);

      if (allOtherActionDefinitionNames.includes(formatedValue)) {
        return { custom: 'This name is already used by another action' };
      }

      if (this.scenario.data.contexts.find((ctx) => ctx.name === formatedValue))
        return { custom: 'Action cannot have the same name as a context' };

      return null;
    };
  }

  copyDescToAnswer(): void {
    this.form.patchValue({
      ...this.form.value,
      answer: this.description.value
    });
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

  handlersAutocompleteValues: Observable<string[]>;

  updateHandlersAutocompleteValues(event?: KeyboardEvent): void {
    this.avalaibleHandlers;

    let results = this.avalaibleHandlers;

    if (event) {
      const targetEvent = event.target as HTMLInputElement;
      results = results.filter((handlerName: string) => handlerName.toLowerCase().includes(targetEvent.value.trim().toLowerCase()));
    }

    this.handlersAutocompleteValues = of(results);
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

  addContext(wich: 'input' | 'output'): void {
    const eventTarget = this[`${wich}ContextsInput`].nativeElement as HTMLInputElement;
    const ctxName = normalizedSnakeCaseUpper(eventTarget.value);

    this.resetContextsAddErrors();

    if (!ctxName) return;

    if (ctxName.length < ENTITY_NAME_MINLENGTH) {
      this[`${wich}ContextsAddError`] = {
        errors: { minlength: { requiredLength: ENTITY_NAME_MINLENGTH } }
      };
      return;
    }

    const actions = getScenarioActionDefinitions(this.scenario);

    if (actions.find((act) => act.name === ctxName)) {
      this[`${wich}ContextsAddError`] = { errors: { custom: 'A context cannot have the same name as an action' } };
      return;
    }

    const currentContextNamesArray = this[`${wich}ContextNames`];
    if (currentContextNamesArray.value.find((ctx: string) => ctx == ctxName)) {
      this[`${wich}ContextsAddError`] = {
        errors: { custom: `This ${wich} context is already associated with this action` }
      };
      return;
    }

    const otherWich = wich == 'input' ? 'output' : 'input';
    const otherContextNamesArray = this[`${otherWich}ContextNames`];
    if (otherContextNamesArray.value.find((ctx: string) => ctx == ctxName)) {
      this[`${wich}ContextsAddError`] = {
        errors: {
          custom: `This context is already associated with the ${otherWich} contexts of this action`
        }
      };
      return;
    }

    currentContextNamesArray.push(new FormControl(ctxName));
    eventTarget.value = '';
    this.form.markAsDirty();
  }

  removeContext(wich: 'input' | 'output', contextName: string): void {
    const contextNamesArray = this[`${wich}ContextNames`];
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
    if (this.canSave) this.saveModifications.emit(this.form.value);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}

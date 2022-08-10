import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { StateService } from 'src/app/core-nlp/state.service';
import { Scenario, scenarioItem, TickContext } from '../../../models';
import { getContrastYIQ, getScenarioActions, normalizedSnakeCase } from '../../../commons/utils';
import { Observable, of } from 'rxjs';
import { entityColor, qualifiedName, qualifiedRole } from '../../../../model/nlp';

const ENTITY_NAME_MINLENGTH = 5;

@Component({
  selector: 'scenario-action-edit',
  templateUrl: './action-edit.component.html',
  styleUrls: ['./action-edit.component.scss']
})
export class ActionEditComponent implements OnInit {
  @Input() item: scenarioItem;
  @Input() contexts: TickContext[];
  @Input() scenario: Scenario;
  @Output() saveModifications = new EventEmitter();
  @Output() deleteDefinition = new EventEmitter();
  @ViewChild('inputContextsInput') inputContextsInput: ElementRef;
  @ViewChild('outputContextsInput') outputContextsInput: ElementRef;

  qualifiedName = qualifiedName;

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

  form: FormGroup = new FormGroup({
    name: new FormControl(undefined, [
      Validators.required,
      Validators.minLength(ENTITY_NAME_MINLENGTH),
      this.notUsedName.bind(this)
    ]),
    description: new FormControl(),
    handler: new FormControl(),
    answer: new FormControl(),
    inputContextNames: new FormArray([]),
    outputContextNames: new FormArray([]),
    final: new FormControl(false)
  });

  notUsedName(c: FormControl) {
    if (!this.scenario) return null;

    const allOtherActionDefinitionNames = getScenarioActions(this.scenario)
      .filter((action) => action !== this.item)
      .filter((item) => item.tickActionDefinition)
      .map((action) => action.tickActionDefinition.name);

    return allOtherActionDefinitionNames.includes(c.value)
      ? {
          custom: 'This name is already used by another action'
        }
      : null;
  }

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

  copyDescToAnswer() {
    this.form.patchValue({
      ...this.form.value,
      answer: this.description.value
    });
  }
  copyDescToName() {
    this.form.patchValue({
      ...this.form.value,
      name: this.description.value
    });
    this.formatActionName();
  }

  contextsAutocompleteValues: Observable<string[]>;

  updateContextsAutocompleteValues(event?: KeyboardEvent): void {
    let results = this.contexts.map((ctx) => ctx.name);

    ['inputContextNames', 'outputContextNames'].forEach((wich) => {
      results = results.filter((r) => {
        return !this[wich].value.includes(r);
      });
    });

    if (event) {
      const targetEvent = event.target as HTMLInputElement;
      results = results.filter((ctxName) => ctxName.includes(targetEvent.value.toUpperCase()));
    }

    this.contextsAutocompleteValues = of(results);
  }

  formatActionName(): void {
    if (this.name.value) {
      this.form.patchValue({
        ...this.form.value,
        name: normalizedSnakeCase(this.name.value)
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
    const ctxName = normalizedSnakeCase(eventTarget.value);

    this.resetContextsAddErrors();

    if (!ctxName) return;

    if (ctxName.length < ENTITY_NAME_MINLENGTH) {
      this[`${wich}ContextsAddError`] = {
        errors: { minlength: { requiredLength: ENTITY_NAME_MINLENGTH } }
      };
      return;
    }

    const currentContextNamesArray = this[`${wich}ContextNames`];
    const otherWich = wich == 'input' ? 'output' : 'input';
    const otherContextNamesArray = this[`${otherWich}ContextNames`];

    if (currentContextNamesArray.value.find((ctx) => ctx == ctxName)) {
      this[`${wich}ContextsAddError`] = {
        errors: { custom: `This ${wich} context is already associated with this action` }
      };
      return;
    }
    if (otherContextNamesArray.value.find((ctx) => ctx == ctxName)) {
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
    contextNamesArray.removeAt(contextNamesArray.value.findIndex((ctx) => ctx === contextName));
    this.form.markAsDirty();
  }

  removeActionDefinition(): void {
    this.deleteDefinition.emit();
  }

  getContextByName(outputContext) {
    return [
      this.contexts.find((ctx) => {
        return ctx.name == outputContext;
      })
    ];
  }

  getContextEntityColor(context) {
    if (context.entityType)
      return entityColor(qualifiedRole(context.entityType, context.entityRole));
  }

  getContextEntityContrast(context) {
    if (context.entityType)
      return getContrastYIQ(entityColor(qualifiedRole(context.entityType, context.entityRole)));
  }

  isSubmitted: boolean = false;
  save(): void {
    this.isSubmitted = true;
    if (this.canSave) this.saveModifications.emit(this.form.value);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { StateService } from 'src/app/core-nlp/state.service';
import { scenarioItem, TickContext } from '../../models';

const ENTITY_NAME_MINLENGTH = 5;

@Component({
  selector: 'scenario-action-edit',
  templateUrl: './action-edit.component.html',
  styleUrls: ['./action-edit.component.scss']
})
export class ActionEditComponent implements OnInit {
  @Input() item: scenarioItem;
  @Input() contexts: TickContext[];
  @Output() saveModifications = new EventEmitter();
  @Output() deleteDefinition = new EventEmitter();
  constructor(public dialogRef: NbDialogRef<ActionEditComponent>, protected state: StateService) {}

  ngOnInit(): void {
    this.form.patchValue(this.item.tickActionDefinition);

    if (this.item.tickActionDefinition?.inputContexts?.length) {
      this.item.tickActionDefinition.inputContexts.forEach((contextName) => {
        this.inputContexts.push(new FormControl(contextName));
      });
    }
    if (this.item.tickActionDefinition?.outputContexts?.length) {
      this.item.tickActionDefinition.outputContexts.forEach((contextName) => {
        this.outputContexts.push(new FormControl(contextName));
      });
    }
  }

  form: FormGroup = new FormGroup({
    name: new FormControl(undefined, [
      Validators.required,
      Validators.minLength(ENTITY_NAME_MINLENGTH)
    ]),
    description: new FormControl(),
    handler: new FormControl(),
    answer: new FormControl(),
    inputContexts: new FormArray([]),
    outputContexts: new FormArray([])
  });

  get canSave(): boolean {
    return this.form.valid;
  }

  get name(): FormControl {
    return this.form.get('name') as FormControl;
  }

  get inputContexts(): FormArray {
    return this.form.get('inputContexts') as FormArray;
  }

  get outputContexts(): FormArray {
    return this.form.get('outputContexts') as FormArray;
  }

  snakeCase(str: string): string {
    return str
      .trim()
      .toUpperCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/\s+/g, '_')
      .replace(/[^A-Za-z0-9_]*/g, '');
  }

  formatActionName(): void {
    this.form.patchValue({
      ...this.form,
      name: this.snakeCase(this.name.value)
    });
  }

  addContext(wich: 'input' | 'output', event: KeyboardEvent): void {
    const eventTarget = event.target as HTMLInputElement;
    const ctxName = this.snakeCase(eventTarget.value);
    if (ctxName && ctxName.length >= ENTITY_NAME_MINLENGTH) {
      this[`${wich}Contexts`].push(new FormControl(ctxName));
      eventTarget.value = '';
    }
  }

  removeContext(wich: 'input' | 'output', contextName: string): void {
    const contextArray = this[`${wich}Contexts`];
    contextArray.removeAt(contextArray.value.findIndex((ctx) => ctx === contextName));
  }

  removeActionDefinition(): void {
    this.deleteDefinition.emit();
  }

  save(): void {
    this.saveModifications.emit(this.form.value);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}

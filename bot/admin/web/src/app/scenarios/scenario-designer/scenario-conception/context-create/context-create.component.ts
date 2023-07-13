import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';

import { normalizedSnakeCaseUpper, getScenarioActionDefinitions } from '../../../commons/utils';
import { ACTION_OR_CONTEXT_NAME_MINLENGTH, ScenarioVersionExtended } from '../../../models';

@Component({
  selector: 'tock-scenario-context-create',
  templateUrl: './context-create.component.html',
  styleUrls: ['./context-create.component.scss']
})
export class ContextCreateComponent {
  @Input() scenario: ScenarioVersionExtended;
  @Output() validate = new EventEmitter();

  constructor(private dialogRef: NbDialogRef<ContextCreateComponent>) {}

  form: FormGroup = new FormGroup({
    name: new FormControl<string>(undefined, [
      Validators.required,
      Validators.minLength(ACTION_OR_CONTEXT_NAME_MINLENGTH),
      this.isContextNameUnic.bind(this)
    ])
  });

  isSubmitted: boolean = false;

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  get name(): FormControl {
    return this.form.get('name') as FormControl;
  }

  formatContextName(): void {
    if (this.name.value) {
      this.form.patchValue({
        ...this.form.value,
        name: normalizedSnakeCaseUpper(this.name.value)
      });
    }
  }

  private isContextNameUnic(c: FormControl): null | {} {
    if (!c.value || !this.scenario) return null;

    let formatedValue = normalizedSnakeCaseUpper(c.value.trim());

    if (this.scenario.data.contexts.find((ctx) => ctx.name === formatedValue))
      return { custom: 'Context cannot have the same name as another existing context' };

    const actions = getScenarioActionDefinitions(this.scenario);

    if (actions.find((act) => act.name === formatedValue)) return { custom: 'Context cannot have the same name as an action' };

    return null;
  }

  save(): void {
    this.isSubmitted = true;
    this.formatContextName();
    if (this.canSave) this.validate.emit(this.form.value);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}

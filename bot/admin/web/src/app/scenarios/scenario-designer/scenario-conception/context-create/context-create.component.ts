import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { Subject } from 'rxjs';
import { normalizedSnakeCaseUpper, getScenarioActionDefinitions } from '../../../commons/utils';
import { ScenarioVersionExtended } from '../../../models';

const ENTITY_NAME_MINLENGTH = 5;

@Component({
  selector: 'scenario-context-create',
  templateUrl: './context-create.component.html',
  styleUrls: ['./context-create.component.scss']
})
export class ContextCreateComponent {
  destroy = new Subject();

  @Input() scenario: ScenarioVersionExtended;
  @Output() validate = new EventEmitter();

  constructor(public dialogRef: NbDialogRef<ContextCreateComponent>) {}

  form: FormGroup = new FormGroup({
    name: new FormControl(undefined, [Validators.required, Validators.minLength(ENTITY_NAME_MINLENGTH), this.isContextNameUnic.bind(this)])
  });

  isSubmitted: boolean = false;

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  get name(): FormControl {
    return this.form.get('name') as FormControl;
  }

  formatContextName() {
    if (this.name.value) {
      this.form.patchValue({
        ...this.form.value,
        name: normalizedSnakeCaseUpper(this.name.value)
      });
    }
  }

  isContextNameUnic(c: FormControl) {
    if (!c.value || !this.scenario) return null;

    let formatedValue = normalizedSnakeCaseUpper(c.value.trim());

    if (this.scenario.data.contexts.find((ctx) => ctx.name === formatedValue))
      return { custom: 'Context cannot have the same name as another context' };

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

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }
}

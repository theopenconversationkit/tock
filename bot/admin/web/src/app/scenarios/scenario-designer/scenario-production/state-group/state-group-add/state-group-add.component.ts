import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { normalizedSnakeCaseUpper } from '../../../../commons/utils';

const ENTITY_NAME_MINLENGTH = 5;

@Component({
  selector: 'scenario-production-state-group-add',
  templateUrl: './state-group-add.component.html',
  styleUrls: ['./state-group-add.component.scss']
})
export class ScenarioProductionStateGroupAddComponent {
  @Input() usedNames: string[];
  @Output() validate = new EventEmitter();

  constructor(public dialogRef: NbDialogRef<ScenarioProductionStateGroupAddComponent>) {}

  form: FormGroup = new FormGroup({
    name: new FormControl(undefined, [Validators.required, Validators.minLength(ENTITY_NAME_MINLENGTH), this.notUsedName.bind(this)])
  });

  notUsedName(c: FormControl) {
    if (!this.usedNames) return null;
    return this.usedNames.includes(c.value)
      ? {
          custom: 'This name is already used as an action or intent name'
        }
      : null;
  }

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

  save(): void {
    this.isSubmitted = true;
    this.formatContextName();
    if (this.canSave) this.validate.emit(this.form.value);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}

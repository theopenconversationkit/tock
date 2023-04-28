import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';

import { normalizedSnakeCaseUpper } from '../../../../commons/utils';
import { ACTION_OR_CONTEXT_NAME_MINLENGTH } from '../../../../models';

@Component({
  selector: 'tock-scenario-production-state-group-add',
  templateUrl: './state-group-add.component.html',
  styleUrls: ['./state-group-add.component.scss']
})
export class ScenarioProductionStateGroupAddComponent {
  @Input() usedNames: string[];
  @Output() validate = new EventEmitter();

  constructor(private dialogRef: NbDialogRef<ScenarioProductionStateGroupAddComponent>) {}

  form: FormGroup = new FormGroup({
    name: new FormControl<string>(undefined, [
      Validators.required,
      Validators.minLength(ACTION_OR_CONTEXT_NAME_MINLENGTH),
      this.notUsedName.bind(this)
    ])
  });

  private notUsedName(c: FormControl): null | { custom: string } {
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

  formatContextName(): void {
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

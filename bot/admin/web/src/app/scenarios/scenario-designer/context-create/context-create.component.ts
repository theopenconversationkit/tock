import { Component, EventEmitter, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { normalizedSnakeCase } from '../../commons/utils';

const ENTITY_NAME_MINLENGTH = 5;

@Component({
  selector: 'scenario-context-create',
  templateUrl: './context-create.component.html',
  styleUrls: ['./context-create.component.scss']
})
export class ContextCreateComponent {
  @Output() validate = new EventEmitter();

  constructor(public dialogRef: NbDialogRef<ContextCreateComponent>) {}

  form: FormGroup = new FormGroup({
    name: new FormControl(undefined, [
      Validators.required,
      Validators.minLength(ENTITY_NAME_MINLENGTH)
    ])
  });

  isSubmitted: boolean = false;

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  get name(): FormControl {
    return this.form.get('name') as FormControl;
  }

  formatContextName() {
    this.form.patchValue({
      ...this.form.value,
      name: normalizedSnakeCase(this.name.value)
    });
  }

  save(): void {
    this.isSubmitted = true;
    if (this.canSave) this.validate.emit(this.form.value);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}

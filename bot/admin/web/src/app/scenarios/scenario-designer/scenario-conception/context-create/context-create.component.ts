import { Component, EventEmitter, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { Subject } from 'rxjs';
import { normalizedSnakeCaseUpper } from '../../../commons/utils';

const ENTITY_NAME_MINLENGTH = 5;

@Component({
  selector: 'scenario-context-create',
  templateUrl: './context-create.component.html',
  styleUrls: ['./context-create.component.scss']
})
export class ContextCreateComponent {
  destroy = new Subject();

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

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }
}

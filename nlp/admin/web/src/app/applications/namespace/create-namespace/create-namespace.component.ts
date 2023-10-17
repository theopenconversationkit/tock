import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';

@Component({
  selector: 'tock-create-namespace',
  templateUrl: './create-namespace.component.html',
  styleUrls: ['./create-namespace.component.scss']
})
export class CreateNamespaceComponent {
  @Output() validate = new EventEmitter();

  constructor(private dialogRef: NbDialogRef<CreateNamespaceComponent>) {}

  form: FormGroup = new FormGroup({
    name: new FormControl<string>(undefined, [Validators.required, this.noWhitespaceValidator])
  });

  public noWhitespaceValidator(control: FormControl) {
    return (control.value || '').trim().length ? null : { whitespace: true };
  }

  isSubmitted: boolean = false;

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  get name(): FormControl {
    return this.form.get('name') as FormControl;
  }

  save(): void {
    this.isSubmitted = true;
    if (this.canSave) {
      this.validate.emit(this.form.value);
    }
  }

  cancel(): void {
    this.dialogRef.close();
  }
}

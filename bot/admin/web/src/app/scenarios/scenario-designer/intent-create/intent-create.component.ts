import { Component, EventEmitter, Inject, Input, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { of } from 'rxjs';
import { StateService } from 'src/app/core-nlp/state.service';
import { normalizedCamelCase } from '../../commons/utils';

@Component({
  selector: 'scenario-intent-create',
  templateUrl: './intent-create.component.html',
  styleUrls: ['./intent-create.component.scss']
})
export class IntentCreateComponent implements OnInit {
  @Input() intentSentence: string;
  @Output() createIntentEvent = new EventEmitter();
  categories: string[] = [];
  categoryAutocompleteValues;

  constructor(public dialogRef: NbDialogRef<IntentCreateComponent>, private state: StateService) {}

  ngOnInit(): void {
    this.state.currentIntentsCategories.subscribe((c) => {
      this.categories = c.map((cat) => cat.category);
      this.categoryAutocompleteValues = of(this.categories);
    });

    if (this.intentSentence.trim()) {
      this.form.patchValue({ label: this.intentSentence });
      this.copyLabelToName();
    }
  }

  form: FormGroup = new FormGroup({
    label: new FormControl(undefined, Validators.required),
    name: new FormControl(undefined, Validators.required),
    category: new FormControl('scenarios'),
    description: new FormControl()
  });

  get label(): FormControl {
    return this.form.get('label') as FormControl;
  }

  get name(): FormControl {
    return this.form.get('name') as FormControl;
  }

  copyLabelToName(): void {
    if (!this.nameInitialized) {
      this.form.patchValue({ name: this.formatName(this.label.value) });
    }
  }

  nameInitialized: boolean = false;
  formatNameInput(): void {
    this.nameInitialized = true;
    this.form.patchValue({ name: this.formatName(this.name.value) });
  }

  private formatName(name: string): string {
    return normalizedCamelCase(name);
    // .normalize('NFD')
    // .replace(/[\u0300-\u036f]/g, '')
    // .replace(/[^A-Za-z_-]*/g, '')
    // .toLowerCase()
    // .trim();
  }

  get canSave(): boolean {
    return this.form.valid;
  }

  cancel(): void {
    this.dialogRef.close();
  }

  save() {
    this.createIntentEvent.emit(this.form.value);
  }
}

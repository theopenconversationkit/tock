import { Component, EventEmitter, Inject, Input, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { of } from 'rxjs';
import { StateService } from 'src/app/core-nlp/state.service';
import { normalizedCamelCase } from '../../../commons/utils';
import { scenarioItem } from '../../../models';

@Component({
  selector: 'scenario-intent-create',
  templateUrl: './intent-create.component.html',
  styleUrls: ['./intent-create.component.scss']
})
export class IntentCreateComponent implements OnInit {
  @Input() item: scenarioItem;
  @Output() createIntentEvent = new EventEmitter();
  categories: string[] = [];
  categoryAutocompleteValues;
  isSubmitted: boolean = false;

  constructor(public dialogRef: NbDialogRef<IntentCreateComponent>, private state: StateService) {}

  ngOnInit(): void {
    this.state.currentIntentsCategories.subscribe((c) => {
      this.categories = c.map((cat) => cat.category);
      this.categoryAutocompleteValues = of(this.categories);
    });

    if (this.item.text.trim()) {
      this.form.patchValue({ label: this.item.text });
      this.copyLabelToName();
    }

    if (this.item.main) {
      this.form.patchValue({ primary: true });
    }
  }

  form: FormGroup = new FormGroup({
    label: new FormControl(undefined, Validators.required),
    name: new FormControl(undefined, Validators.required),
    category: new FormControl('scenarios'),
    description: new FormControl(),
    primary: new FormControl(false)
  });

  get label(): FormControl {
    return this.form.get('label') as FormControl;
  }

  get name(): FormControl {
    return this.form.get('name') as FormControl;
  }

  get category(): FormControl {
    return this.form.get('category') as FormControl;
  }

  get description(): FormControl {
    return this.form.get('description') as FormControl;
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
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  cancel(): void {
    this.dialogRef.close();
  }

  save() {
    this.isSubmitted = true;

    if (this.canSave) {
      this.createIntentEvent.emit(this.form.value);
    }
  }
}

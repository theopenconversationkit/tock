import { Component, EventEmitter, Inject, Input, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { of, Subject } from 'rxjs';
import { StateService } from '../../../../core-nlp/state.service';
import { Intent, nameFromQualifiedName } from '../../../../model/nlp';
import { getScenarioIntentDefinitions, normalizedCamelCase } from '../../../commons/utils';
import { ScenarioVersion, ScenarioItem } from '../../../models';

@Component({
  selector: 'scenario-intent-create',
  templateUrl: './intent-create.component.html',
  styleUrls: ['./intent-create.component.scss']
})
export class IntentCreateComponent implements OnInit {
  destroy = new Subject();

  @Input() item: ScenarioItem;
  @Input() scenario: ScenarioVersion;
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

    this.form.markAsDirty();
  }

  form: FormGroup = new FormGroup({
    label: new FormControl(undefined, Validators.required),
    name: new FormControl(undefined, [Validators.required, this.isIntentNameUnic.bind(this)]),
    category: new FormControl('scenarios'),
    description: new FormControl(),
    primary: new FormControl(false)
  });

  isIntentNameUnic(c: FormControl) {
    if (!this.scenario) return null;

    let isNotUnic;
    let errorString;
    if (c.value === nameFromQualifiedName(Intent.unknown)) {
      isNotUnic = true;
      errorString = `The string "${nameFromQualifiedName(Intent.unknown)}" is not allowed as an intent name`;
    } else {
      if (this.scenario.data.triggers?.find((trigger) => trigger === c.value)) {
        isNotUnic = true;
        errorString = 'There is already an event with the same name';
      } else {
        const allIntentDefs = getScenarioIntentDefinitions(this.scenario);
        const nameExistInScenario = allIntentDefs.find((intentDef) => intentDef.name === c.value);
        if (nameExistInScenario) {
          isNotUnic = true;
          errorString = 'This name is already used by another intent of this scenario';
        } else {
          const intentAlreadyExist = this.state.intentExists(c.value);
          if (intentAlreadyExist) {
            isNotUnic = true;
            errorString = 'This name is already used by an existing intent of this or another application';
          }
        }
      }
    }

    return isNotUnic ? { custom: errorString } : null;
  }

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
  }

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  cancel(): void {
    this.dialogRef.close();
  }

  save() {
    this.isSubmitted = true;
    if (this.canSave) this.createIntentEvent.emit(this.form.value);
  }

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }
}

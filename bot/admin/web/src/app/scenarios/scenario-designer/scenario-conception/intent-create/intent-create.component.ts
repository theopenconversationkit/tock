import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { Subject } from 'rxjs';
import { take } from 'rxjs/operators';
import { StateService } from '../../../../core-nlp/state.service';
import { Intent, nameFromQualifiedName } from '../../../../model/nlp';
import { getScenarioIntentDefinitions, normalizedCamelCase } from '../../../commons/utils';
import { ScenarioVersion, ScenarioItem } from '../../../models';

interface IntentCreateForm {
  label: FormControl<string>;
  name: FormControl<string>;
  category: FormControl<string>;
  description: FormControl<string>;
  primary: FormControl<boolean>;
}

@Component({
  selector: 'tock-scenario-intent-create',
  templateUrl: './intent-create.component.html',
  styleUrls: ['./intent-create.component.scss']
})
export class IntentCreateComponent implements OnInit, OnDestroy {
  destroy = new Subject();

  @Input() item: ScenarioItem;
  @Input() scenario: ScenarioVersion;
  @Output() createIntentEvent = new EventEmitter();
  categories: string[] = [];
  isSubmitted: boolean = false;

  constructor(private dialogRef: NbDialogRef<IntentCreateComponent>, private stateService: StateService) {}

  ngOnInit(): void {
    this.stateService.currentIntentsCategories.pipe(take(1)).subscribe((c) => {
      this.categories = c.map((cat) => cat.category);
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

  form: FormGroup = new FormGroup<IntentCreateForm>({
    label: new FormControl(undefined, Validators.required),
    name: new FormControl(undefined, [Validators.required, this.isIntentNameUnic.bind(this)]),
    category: new FormControl('scenarios'),
    description: new FormControl(),
    primary: new FormControl(false)
  });

  private isIntentNameUnic(control: FormControl): null | {} {
    if (!this.scenario) return null;

    let isNotUnic;
    let errorString;
    if (control.value === nameFromQualifiedName(Intent.unknown)) {
      isNotUnic = true;
      errorString = `The string "${nameFromQualifiedName(Intent.unknown)}" is not allowed as an intent name`;
    } else {
      if (this.scenario.data.triggers?.find((trigger) => trigger === control.value)) {
        isNotUnic = true;
        errorString = 'There is already an event with the same name';
      } else {
        const allIntentDefs = getScenarioIntentDefinitions(this.scenario);
        const nameExistInScenario = allIntentDefs.find((intentDef) => intentDef.name === control.value);
        if (nameExistInScenario) {
          isNotUnic = true;
          errorString = 'This name is already used by another intent of this scenario';
        } else {
          const intentAlreadyExist = this.stateService.intentExists(control.value);
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
    this.destroy.next(true);
    this.destroy.complete();
  }
}

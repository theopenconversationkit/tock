import { Component, EventEmitter, Input, Output } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';

import { getScenarioIntentDefinitions, normalizedCamelCase } from '../../../commons/utils';
import { ScenarioVersionExtended } from '../../../models';

@Component({
  selector: 'tock-trigger-create',
  templateUrl: './trigger-create.component.html',
  styleUrls: ['./trigger-create.component.scss']
})
export class TriggerCreateComponent {
  @Input() scenarioVersion: ScenarioVersionExtended;
  @Output() validate = new EventEmitter();

  isSubmitted: boolean = false;

  form: FormGroup = new FormGroup({
    trigger: new FormControl<string>(undefined, [
      Validators.required,
      Validators.minLength(5),
      this.isAlreadyPresentInTriggerList(),
      this.isTriggerNameUnic()
    ])
  });

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  get trigger(): FormControl {
    return this.form.get('trigger') as FormControl;
  }

  constructor(private dialogRef: NbDialogRef<TriggerCreateComponent>) {}

  formatTriggerName(): void {
    if (this.trigger.value) {
      this.trigger.patchValue(normalizedCamelCase(this.trigger.value).toLowerCase());
    }
  }

  /**
   * Checks if the trigger name is already present in the list
   * @returns {ValidationErrors|null} return custom error or null
   */
  private isAlreadyPresentInTriggerList(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value || !this.scenarioVersion) return null;

      const formatedValue = normalizedCamelCase(control.value.trim()).toLowerCase();

      const triggerAlreadyDefined = this.scenarioVersion.data.triggers?.find((trigger) => trigger === formatedValue);

      return triggerAlreadyDefined ? { custom: 'Event cannot have the same name as another event' } : null;
    };
  }

  /**
   * Checks if the trigger name matches the name of an intention
   * @returns {ValidationErrors|null} return custom error or null
   */
  private isTriggerNameUnic(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value || !this.scenarioVersion) return null;

      const formatedValue = normalizedCamelCase(control.value.trim()).toLowerCase();
      const intents = getScenarioIntentDefinitions(this.scenarioVersion);
      const triggerAsSameNameLikeIntent = intents.find((intent) => intent.name.toLowerCase() === formatedValue);

      return triggerAsSameNameLikeIntent ? { custom: 'There is already an intent with the same name' } : null;
    };
  }

  save(): void {
    this.isSubmitted = true;
    this.formatTriggerName();
    if (this.canSave) this.validate.emit(this.trigger.value);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}

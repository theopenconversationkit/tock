/**
 * About form problems reporting
 */

import {AbstractControl, FormControl, FormGroup } from "@angular/forms";

type ValidFormState = {
  formValid: true
}

export type InvalidFormProblems = {
  formValid: false,

  items: Array<{
    controlLabel: string;
    errorLabel: string;
  }>
}

export type FormProblems = ValidFormState | InvalidFormProblems;

export const NoProblems: ValidFormState = { formValid: true };

/**
 * Check if user should be warned about a Control state
 * @param control Angular Form Control
 */
export function isControlAlert(control: AbstractControl): boolean {
  return control.invalid && (control.dirty || control.touched);
}

/**
 * Collect problems, when Form invalid
 *
 * @param form Angular Form
 * @param customErrorLabels Errors alternative labels
 */
export function collectProblems(form: FormGroup, customErrorLabels: { [key: string]: string}): InvalidFormProblems {
  return {
    formValid: false,

    // for each controls in form
    items: Object.entries(form.controls)
      .filter(([key, control]) => isControlAlert(control)) // which are touched or dirty
      .map(([key, control]) => {
      // for each errors in control
      return Object.entries(control.errors).map(([errorKey, errorValue]) => {
        // collect error
        return {
          controlLabel: key,
          errorLabel: customErrorLabels[key+"_"+errorKey] || errorKey
        };
      });
    }).reduce((acc, val) => acc.concat(val), []) // flatMap ES5 equivalent (concat first level arrays)
  };
}

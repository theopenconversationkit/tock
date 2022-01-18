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

export const NAME_MINLENGTH = 6;
export const NAME_MAXLENGTH = 40;
export const DESCRIPTION_MAXLENGTH = 500;
export const ANSWER_MAXLENGTH = 800;

export const DEFAULT_ERROR_MAPPING = {
  name_minlength: `Name must be at least ${NAME_MINLENGTH} characters`,
  name_maxlength: `Name must be less than ${NAME_MAXLENGTH} characters`,
  name_required: "Name required",
  name_error: "Invalid name",
  description_maxlength: `Description must be less than ${DESCRIPTION_MAXLENGTH} characters`,
  description_error: "Invalid description",
  answer_required: "Answer required",
  utterances_required: "One question required at least"
};

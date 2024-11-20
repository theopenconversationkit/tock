import { FormArray, FormControl, FormGroup } from '@angular/forms';

export type ExtractFormControlTyping<T> = {
  [K in keyof T]: T[K] extends FormControl<infer U>
    ? U
    : T[K] extends FormArray<FormControl<infer U>>
    ? Array<U>
    : T[K] extends FormArray<FormGroup<infer U>>
    ? Array<Partial<ExtractFormControlTyping<U>>>
    : T[K] extends FormGroup<infer U>
    ? Partial<ExtractFormControlTyping<U>>
    : T[K];
};

export type GenericObject<T> = { [key: string]: T };

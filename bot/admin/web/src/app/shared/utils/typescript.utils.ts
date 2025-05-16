/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

export type ToFormType<T> = FormGroup<{
  [K in keyof T]: T[K] extends object
    ? T[K] extends Date
      ? FormControl<T[K] | null>
      : T[K] extends unknown[]
      ? FormArray<ToFormType<T[K] extends (infer V)[] ? V : T[K]>>
      : ToFormType<T[K]>
    : FormControl<T[K] | null>;
}>;

export type GenericObject<T> = { [key: string]: T };

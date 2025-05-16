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

import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export class FileValidators {
  static mimeTypeSupported(mimeTypes: string[]): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!Array.isArray(mimeTypes) || !Array.isArray(control.value)) {
        throw new TypeError('invalid argument. The parameter must be an array');
      }

      if (!mimeTypes.length) {
        throw new Error('the mimeTypes parameter cannot be empty');
      }

      const filesNameWithWrongType: string[] = [];

      control.value.forEach((f: File) => {
        if (!(f instanceof File)) {
          throw new TypeError(`invalid arguments. ${f} must be a File object`);
        } else if (!mimeTypes.includes(f.type)) {
          filesNameWithWrongType.push(f.name);
        }
      });

      return filesNameWithWrongType.length ? { filesNameWithWrongType } : null;
    };
  }
}

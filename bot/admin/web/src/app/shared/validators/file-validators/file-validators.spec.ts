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

import { AbstractControl } from '@angular/forms';

import { FileValidators } from './file-validators';

describe('File validators', () => {
  [
    { description: 'null', value: null },
    { description: 'undefined', value: undefined },
    { description: 'a string', value: '' },
    { description: 'a boolean', value: true },
    { description: 'an object', value: {} }
  ].forEach((test) => {
    it(`should return an error when the control is ${test.description}`, () => {
      const control = { value: test.value };

      const valid = () => {
        FileValidators.mimeTypeSupported([])(control as AbstractControl);
      };

      expect(valid).toThrowError('invalid argument. The parameter must be an array');
    });
  });

  [
    { description: 'null', value: null },
    { description: 'undefined', value: undefined },
    { description: 'a string', value: '' },
    { description: 'a boolean', value: true },
    { description: 'an object', value: {} }
  ].forEach((test) => {
    it(`should return an error when the types parameter is ${test.description}`, () => {
      const control = { value: [] };

      const valid = () => {
        FileValidators.mimeTypeSupported(test.value as unknown as any)(control as AbstractControl);
      };

      expect(valid).toThrowError('invalid argument. The parameter must be an array');
    });
  });

  it('should return an error when the types parameter is an empty array of string', () => {
    const control = { value: [] };

    const valid = () => {
      FileValidators.mimeTypeSupported([])(control as AbstractControl);
    };

    expect(valid).toThrowError('the mimeTypes parameter cannot be empty');
  });

  it('should return an error when at least one file is not of type file', () => {
    const control = { value: [new File(['content'], 'file'), 'test'] };

    const valid = () => {
      FileValidators.mimeTypeSupported(['application/json'])(control as AbstractControl);
    };

    expect(valid).toThrowError('invalid arguments. test must be a File object');
  });

  it('should return null when all files have the correct type(s) specified', () => {
    const control = {
      value: [
        new File(['content'], 'file1', { type: 'application/json' }),
        new File(['content'], 'file2', { type: 'application/json' }),
        new File(['content'], 'file3', { type: 'application/json' })
      ]
    };

    const valid = FileValidators.mimeTypeSupported(['application/json'])(control as AbstractControl);

    expect(valid).toBeNull();
  });

  it('should return an object with an array of filenames when files do not have the correct type(s) specified', () => {
    const control = {
      value: [
        new File(['content'], 'file1', { type: 'application/json' }),
        new File(['content'], 'file2', { type: 'application/xml' }),
        new File(['content'], 'file3', { type: 'application/json' }),
        new File(['content'], 'file4', { type: 'application/pdf' })
      ]
    };
    const expectResult = { filesNameWithWrongType: ['file2', 'file4'] };

    const valid = FileValidators.mimeTypeSupported(['application/json'])(control as AbstractControl);

    expect(valid).toEqual(expectResult);
  });
});

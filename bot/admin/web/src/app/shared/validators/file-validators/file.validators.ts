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

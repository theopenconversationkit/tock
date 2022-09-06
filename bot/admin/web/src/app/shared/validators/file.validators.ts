import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export class FileValidators {
  static typeSupported(types: string[]): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;

      if (!Array.isArray(control.value)) {
        throw new TypeError('invalid argument. The parameter must be an array');
      }

      const filesNameWithWrongType: string[] = [];

      control.value.forEach((f: File) => {
        if (!(f instanceof File)) {
          throw new TypeError(`invalid arguments. ${f} must be a File object`);
        } else if (!types.includes(f.type)) {
          filesNameWithWrongType.push(f.name);
        }
      });

      return filesNameWithWrongType.length ? { filesNameWithWrongType } : null;
    };
  }
}

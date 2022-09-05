import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export class FileValidators {
  static typeSupported(types: string[]): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;

      const filesNameWithWrongType: string[] = [];

      control.value.forEach((f: File) => {
        if (!types.includes(f.type)) filesNameWithWrongType.push(f.name);
      });

      return filesNameWithWrongType.length ? { filesNameWithWrongType } : null;
    };
  }
}

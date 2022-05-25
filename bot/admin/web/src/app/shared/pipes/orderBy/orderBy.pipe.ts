import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'orderBy'
})
export class OrderByPipe implements PipeTransform {
  transform(array: any, field: string, reverse: boolean = false, secondField?: string): any[] {
    if (!Array.isArray(array)) {
      throw new TypeError('Invalid array argument');
    }

    if (!field) {
      throw new Error('The field parameter cannot be empty');
    }

    array.sort((a: any, b: any) => {
      if (a[field] == null || a[field] == '') return 1;

      if (b[field] == null || b[field] == '') return -1;

      if (secondField) {
        return a[field].localeCompare(b[field]) || a[secondField].localeCompare(b[secondField]);
      }
      return a[field].localeCompare(b[field]);
    });

    return reverse ? array.reverse() : array;
  }
}

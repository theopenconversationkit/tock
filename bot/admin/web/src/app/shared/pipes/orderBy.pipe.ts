import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'orderBy'
})
export class OrderByPipe implements PipeTransform {
  transform(array: any, field: string, reverse: boolean = false, secondField?: string): any[] {
    if (!Array.isArray(array) || !field) {
      return array;
    }

    array.sort((a: any, b: any) => {
      if (!a[field] || !b[field]) return 0;

      if (secondField) {
        return a[field].localeCompare(b[field]) || a[secondField].localeCompare(b[secondField]);
      }
      return a[field].localeCompare(b[field]);
    });

    return reverse ? array.reverse() : array;
  }
}

import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'sortByOrder' })
export class SortByOrderPipe implements PipeTransform {
  transform(items: Record<string, string>, order: string[] = []): { key: string; value: string }[] {
    return Object.entries(items)
      .sort((a, b) => {
        const indexA = order.indexOf(a[0]);
        const indexB = order.indexOf(b[0]);

        if (indexA !== -1 && indexB !== -1) {
          return indexA - indexB;
        }

        if (indexA !== -1) return -1;
        if (indexB !== -1) return 1;

        return 0;
      })
      .map(([key, value]) => ({ key, value }));
  }
}

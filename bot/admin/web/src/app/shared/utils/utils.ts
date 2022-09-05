export interface OrderBy {
  criteria: string;
  reverse: boolean;
  secondField?: string;
}

export function orderBy<T>(array: T[], field: string, reverse: boolean = false, secondField?: string): T[] {
  if (!Array.isArray(array)) {
    throw new TypeError('Invalid array argument');
  }

  if (!field) {
    throw new Error('The field parameter cannot be empty');
  }

  const sortedArray = [...array].sort((a: T, b: T) => {
    if (a[field] == null || a[field] == '') return 1;

    if (b[field] == null || b[field] == '') return -1;

    if (secondField) {
      return a[field].localeCompare(b[field]) || a[secondField].localeCompare(b[secondField]);
    }
    return a[field].localeCompare(b[field]);
  });

  return reverse ? sortedArray.reverse() : sortedArray;
}

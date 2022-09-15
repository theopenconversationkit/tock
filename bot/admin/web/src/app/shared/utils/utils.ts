import { saveAs } from 'file-saver-es';

export interface OrderBy {
  criteria: string;
  reverse: boolean;
  secondField?: string;
}

export function orderBy<T>(array: T[], field: string, reverse: boolean = false, secondField?: string): T[] {
  if (!Array.isArray(array)) {
    throw new TypeError('invalid array argument. The parameter must be an array');
  }

  if (!field) {
    throw new Error('the field parameter cannot be empty');
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

export function readFileAsText(file: File): Promise<any> {
  return new Promise(function (resolve, reject) {
    let fr = new FileReader();

    fr.onload = function () {
      resolve({ fileName: file.name, data: fr.result });
    };

    fr.onerror = function () {
      reject(fr);
    };

    fr.readAsText(file);
  });
}

export function exportJsonDump(obj: Object, fileName: string): void {
  saveAs(
    new Blob([JSON.stringify(obj)], {
      type: 'application/json'
    }),
    fileName + '.json'
  );
}

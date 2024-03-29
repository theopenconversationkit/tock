import { saveAs } from 'file-saver-es';

export interface OrderBy {
  criteria: string;
  reverse: boolean;
  secondField?: string;
}

/**
 * Return an array of elements ordered according to the specified criteria
 * @param {Array<T>} array array of objects of the same type
 * @param {string} field object key used as sorting criteria
 * @param {boolean} reverse sort order, ascending or descending
 * @param {string} secondField object key used as sorting criteria, used in case the values of the first criterion are equal
 * @returns {Array<T>}
 */
export function orderBy<T>(array: T[], field: string, reverse: boolean = false, secondField?: string): T[] {
  if (!Array.isArray(array)) {
    throw new TypeError('invalid array argument. The parameter must be an array');
  }

  if (!array.every((v: T) => typeof v === 'object' && v !== null && !Array.isArray(v))) {
    throw new TypeError('invalid array argument. The array must contain only objects');
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

/**
 * Makes a deep copy of the object. (types and circular dependencies are not preserved).
 * @param {Object} obj Object to copy
 * @returns {Object} copy of the object
 */
export function deepCopy<T>(obj: T): T {
  return JSON.parse(JSON.stringify(obj));
}

/**
 * Evaluate if argument is of primitive type.
 * @param {any} arg variable to evaluate
 * @returns {boolean} return true if arg is a primitive
 */
export function isPrimitive(arg: any): boolean {
  var type = typeof arg;
  return arg == null || (type != 'object' && type != 'function');
}

/**
 * Evaluate if argument is of object type.
 * @param {any} arg variable to evaluate
 * @returns {boolean} return true if arg is an object
 */
export function isObject(arg: any): boolean {
  return arg && typeof arg === 'object' && !Array.isArray(arg);
}

/**
 * Check if an array contains a given array.
 * @param {Array} data the array to look in
 * @param {Array} arr the array to find
 * @returns {boolean} return true if the data object contains the given array
 */
export function includesArray(data: any[], arr: any[]): boolean {
  return data.some((e) => Array.isArray(e) && e.every((o, i) => Object.is(arr[i], o)));
}

export function getContrastYIQ(hexcolor: string, blackOutput?: string, whiteOutput?: string): string {
  if (!hexcolor) return '';
  blackOutput = blackOutput || 'black';
  whiteOutput = whiteOutput || 'white';

  hexcolor = hexcolor.replace('#', '');

  let r = parseInt(hexcolor.substring(0, 2), 16);
  let g = parseInt(hexcolor.substring(2, 4), 16);
  let b = parseInt(hexcolor.substring(4, 6), 16);
  let yiq = (r * 299 + g * 587 + b * 114) / 1000;

  return yiq >= 140 ? blackOutput : whiteOutput;
}

export async function copyToClipboard(text: string): Promise<void> {
  if (navigator.clipboard) {
    await navigator.clipboard.writeText(text);
  } else {
    const textarea = document.createElement('textarea');
    textarea.value = text;
    document.body.appendChild(textarea);
    textarea.focus();
    textarea.select();
    try {
      document.execCommand('copy');
    } catch (err) {
      console.error('unable to copy to clipboard', err);
    }
    document.body.removeChild(textarea);
  }
}

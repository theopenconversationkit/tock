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
  return !!arg && typeof arg === 'object' && !Array.isArray(arg);
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

/**
 * Return dark or light color depending on input color.
 * @param {string} hexcolor a color in hexa
 * @param {string} blackOutput the dark color to return to be readable in front of hexcolor
 * @param {string} whiteOutput the light color to return to be readable in front of hexcolor
 * @returns {blackOutput | whiteOutput} the dark or light color depending on the heaxcolor given
 */
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

/**
 * Darken the given hexcolor if it is too light to be readable
 * @param {string} hexcolor a color in hexadecimal format
 * @returns {string} a darken version of hexcolor or unchanged hexcolor
 */
export function darkenIfTooLight(hexcolor: string): string {
  if (!hexcolor) return '';
  hexcolor = hexcolor.replace('#', '');

  let r = parseInt(hexcolor.substring(0, 2), 16);
  let g = parseInt(hexcolor.substring(2, 4), 16);
  if (isNaN(g)) return hexcolor;
  let b = parseInt(hexcolor.substring(4, 6), 16);

  let yiq = (r * 299 + g * 587 + b * 114) / 1000;

  return yiq > 180 ? shadeColor(hexcolor, -80) : '#' + hexcolor;
}

/**
 * Change the given hexcolor intensity by the given amount
 * @param {string} hexcolor a color in hexadecimal format
 * @param {string} amount the positive or negative amount to add to each color component
 * @returns {string} the modified color in hexadecimal format
 */
export function shadeColor(hexcolor: string, amount: number) {
  hexcolor = hexcolor.replace('#', '');

  let R = parseInt(hexcolor.substring(0, 2), 16);
  let G = parseInt(hexcolor.substring(2, 4), 16);
  let B = parseInt(hexcolor.substring(4, 6), 16);

  R = Math.round(Math.min(R + amount, 255));
  G = Math.round(Math.min(G + amount, 255));
  B = Math.round(Math.min(B + amount, 255));

  const RR = R.toString(16).length == 1 ? '0' + R.toString(16) : R.toString(16);
  const GG = G.toString(16).length == 1 ? '0' + G.toString(16) : G.toString(16);
  const BB = B.toString(16).length == 1 ? '0' + B.toString(16) : B.toString(16);

  return '#' + RR + GG + BB;
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

export function getPropertyByNameSpace(namespace: string, obj: Object): any {
  const path = namespace.split('.');

  if (path.length === 1) {
    if (path[0].trim().length === 0) return obj;
    return obj[path[0]];
  }

  return path.reduce((acc, current) => {
    if (acc) return acc[current];
    else return obj[current];
  }, undefined);
}

/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

/**
 * Interpolates between two hex colors based on a value between 0 and 1.
 *
 * @param {number} value - A value between 0 and 1. Clamped to this range if outside.
 * @param {string} [colorStart="#acbef4"] - The starting hex color (e.g., "#acbef4").
 * @param {string} [colorEnd="#3366ff"] - The ending hex color (e.g., "#3366ff").
 * @returns {string} The interpolated hex color as a string.
 *
 * @example
 * // Returns a color halfway between #acbef4 and #3366ff
 * const interpolatedColor = getInterpolatedColor(0.5, "#acbef4", "#3366ff");
 */
export function getInterpolatedColor(value: number, colorStart: string = '#acbef4', colorEnd: string = '#3366ff'): string {
  // Clamp value between 0 and 1
  const clampedValue = Math.min(1, Math.max(0, value));

  // Parse hex colors to RGB
  const parseHex = (hex: string) => {
    const r = parseInt(hex.slice(1, 3), 16);
    const g = parseInt(hex.slice(3, 5), 16);
    const b = parseInt(hex.slice(5, 7), 16);
    return { r, g, b };
  };

  const start = parseHex(colorStart);
  const end = parseHex(colorEnd);

  // Interpolate each channel
  const r = Math.round(start.r + (end.r - start.r) * clampedValue);
  const g = Math.round(start.g + (end.g - start.g) * clampedValue);
  const b = Math.round(start.b + (end.b - start.b) * clampedValue);

  // Convert back to hex
  return `#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}`;
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

/**
 * Rounds the minutes of a given date up to the nearest ten and resets seconds and milliseconds to zero.
 * Automatically handles overflow to the next hour, day, month, or year if necessary.
 *
 * @param {Date} date - The input date to round.
 * @returns {Date} A new Date object with minutes rounded up to the nearest ten and seconds/milliseconds set to zero.
 */
export function roundMinutesToNextTen(date: Date): Date {
  const newDate = new Date(date);
  const minutes = newDate.getMinutes();
  const roundedMinutes = Math.ceil(minutes / 10) * 10;
  newDate.setMinutes(roundedMinutes, 0, 0);
  return newDate;
}

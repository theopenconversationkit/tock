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

/**
 * Normalize a string : Replaces accented characters with their non-accented version.
 * @param {string} str string to normalize
 * @returns {string} the cleaned string
 */
export function normalize(str: string): string {
  return str.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
}

/**
 * Remove special chars from a string
 * @param {string} str string to normalize
 * @returns {string} the cleaned string
 */
export function removeSpecialChars(str: string): string {
  return str.replace(/[.,\/#!$%\^&\*;:{}=\-_`'~()?]/g, '');
}

/**
 * Normalize a string, remove special chars and spaces
 * @param {string} str string to normalize
 * @returns {string} the cleaned string
 */
export function normalizeString(str: string): string {
  return removeSpecialChars(normalize(str)).replace(/\s+/g, '');
}

/**
 * Normalize a string and transform to snake case
 * @param {string} str string to proceed
 * @returns {string} proceeded string
 */
export function normalizedSnakeCase(str: string): string {
  return normalize(str)
    .replace(/-/g, ' ')
    .replace(/\s+/g, ' ')
    .replace(/[^A-Za-z0-9_\s]*/g, '')
    .trim()
    .replace(/\s+/g, '_');
}

/**
 * Normalize a string, transform to snake case and uppercase
 * @param {string} str string to proceed
 * @returns {string} the proceeded string
 */
export function normalizedSnakeCaseUpper(str: string): string {
  return normalizedSnakeCase(str).toUpperCase();
}

/**
 * Normalize a string and transform to camel case
 * @param {string} str string to proceed
 * @returns {string} the proceeded string
 */
export function normalizedCamelCase(str: string): string {
  return normalize(str)
    .trim()
    .toLowerCase()
    .replace(/[^a-zA-Z0-9]+(.)/g, (m, chr) => {
      return chr.toUpperCase();
    });
}

/**
 * Converts a snake_case string into a human-readable display label by:
 * 1. Replacing underscores with spaces.
 * 2. Capitalizing only the first letter of the resulting string.
 *
 * @example
 * // returns "Not found"
 * snakeCaseToDisplayLabel("not_found");
 *
 * @param {string} value - The snake_case string to convert (e.g., "not_found").
 * @returns {string} The formatted display label (e.g., "Not found").
 */
export function snakeCaseToDisplayLabel(value: string): string {
  return value.replace(/_/g, ' ').replace(/^(.)/, (c) => c.toUpperCase());
}

export function isUrl(str: string): boolean {
  const reg = /^(?:http(s)?:\/\/)[\w.-]+(?:\.[\w\.-]+)+[\w\-\._~:/?#[\]@!\$&'\(\)\*\+,;=.]+$/;
  return reg.test(str);
}

export const toISOStringWithoutOffset = (date: Date) => {
  if (!date) return null;
  const pad = (n) => `${Math.floor(Math.abs(n))}`.padStart(2, '0');
  return (
    date.getFullYear() +
    '-' +
    pad(date.getMonth() + 1) +
    '-' +
    pad(date.getDate()) +
    'T' +
    pad(date.getHours()) +
    ':' +
    pad(date.getMinutes()) +
    ':' +
    pad(date.getSeconds())
  );
};

export function getExportFileName(namespace: string, botName: string, type: string, extension: string, typeOption?: string): string {
  let fileName = [normalizedCamelCase(namespace), normalizedCamelCase(botName), normalizedCamelCase(type)];

  if (typeOption?.length) {
    fileName.push(normalizedCamelCase(typeOption));
  }

  const currentdate = new Date();

  fileName.push([currentdate.getDate(), currentdate.getMonth() + 1, currentdate.getFullYear()].join('-'));

  fileName.push([currentdate.getHours(), currentdate.getMinutes(), currentdate.getSeconds()].join('-'));

  return [fileName.join('_'), extension].join('.');
}

/**
 * To use when css ellipsis is not an option. Truncate given string to maxlen if necessary
 * @param {string} str string to proceed
 * @param {number} maxlen the length limit
 * @param {boolean} useWordBoundary if true, avoid cuting inside words
 * @param {boolean} htmlEllipsis to use or not the HTML entity code for ellipsis
 * @returns {string} the proceeded string
 */
export function truncateString(str, maxlen, useWordBoundary, htmlEllipsis = true) {
  if (str.length <= maxlen) {
    return str;
  }
  const ellipsis = htmlEllipsis ? '&hellip;' : '...';
  const subString = str.slice(0, maxlen - 1); // the original check
  return (useWordBoundary ? subString.slice(0, subString.lastIndexOf(' ')) : subString) + ellipsis;
}

/**
 * Normalize a string : Replaces accented characters with their non-accented version.
 * @param {string} str string to normalize
 * @returns {string} the cleaned string
 */
export function normalize(str: string): string {
  return str.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
}

/**
 * Normalize a string, remove special chars and spaces
 * @param {string} str string to normalize
 * @returns {string} the cleaned string
 */
export function normalizeString(str: string): string {
  return normalize(str)
    .replace(/[.,\/#!$%\^&\*;:{}=\-_`'~()?]/g, '')
    .replace(/\s+/g, '');
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
    })
    .replace(/[^A-Za-z0-9]*/g, '');
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

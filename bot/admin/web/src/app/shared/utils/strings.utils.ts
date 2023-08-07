export function normalize(str: string): string {
  return str.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
}

export function normalizedSnakeCase(str: string): string {
  return normalize(str)
    .replace(/-/g, ' ')
    .replace(/\s+/g, ' ')
    .replace(/[^A-Za-z0-9_\s]*/g, '')
    .trim()
    .replace(/\s+/g, '_');
}

export function normalizedSnakeCaseUpper(str: string): string {
  return normalizedSnakeCase(str).toUpperCase();
}

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
  const pad = n => `${Math.floor(Math.abs(n))}`.padStart(2, '0');
  return date.getFullYear() +
    '-' + pad(date.getMonth() + 1) +
    '-' + pad(date.getDate()) +
    'T' + pad(date.getHours()) +
    ':' + pad(date.getMinutes()) +
    ':' + pad(date.getSeconds())
};

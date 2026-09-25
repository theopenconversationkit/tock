import type en from '../assets/i18n/en.json';

declare module '@jsverse/transloco' {
  interface TranslocoTypeConfig {
    value: typeof en;
  }
}

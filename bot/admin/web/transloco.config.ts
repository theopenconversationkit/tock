import { TranslocoGlobalConfig } from '@jsverse/transloco-utils';

const config: TranslocoGlobalConfig = {
  rootTranslationsPath: 'src/assets/i18n/',
  langs: ['en', 'fr'],
  scopePathMap: {
    analytics: './assets/i18n/analytics',
    applications: './assets/i18n/applications',
    bot: './assets/i18n/bot',
    configuration: './assets/i18n/configuration',
    faq: './assets/i18n/faq',
    'language-understanding': './assets/i18n/language-understanding',
    metrics: './assets/i18n/metrics',
    playground: './assets/i18n/playground',
    quality: './assets/i18n/quality',
    rag: './assets/i18n/rag',
    test: './assets/i18n/test'
  },
  keysManager: {
    input: 'src',
    output: 'src/assets/i18n',
    addMissingKeys: false,
    unflat: false,
    emitErrorOnExtraKeys: true
  }
};

export default config;

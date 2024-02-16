import { I18nLabelStateQuery } from '../../model/i18n';

export interface I18nFilters {
  search: string;
  locale: I18nLocaleFilters;
  category: string;
  state: I18nLabelStateQuery;
  usage: number;
}

export enum I18nLocaleFilters {
  ALL = 'ALL',
  CURRENT = 'CURRENT',
  SUPPORTED = 'SUPPORTED',
  NOT_SUPPORTED = 'NOT_SUPPORTED'
}

export const I18nCategoryFilterAll = 'ALL';

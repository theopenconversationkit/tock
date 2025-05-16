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

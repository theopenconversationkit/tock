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

import { Entry, PaginatedQuery, SearchMark } from '../../model/commons';
import { PaginatedResult } from '../../model/nlp';
import { FaqDefinition } from '../models';

export class FaqSearchQuery extends PaginatedQuery {
  constructor(
    public override namespace: string,
    public override applicationName: string,
    public override language: string,
    public override start: number,
    public override size: number,
    public tags: string[] = [],
    public override searchMark?: SearchMark,
    public search?: string,
    public override sort?: Entry<string, boolean>[],
    public enabled: Boolean = null,
    public user?: string,
    public allButUser?: string
  ) {
    super(namespace, applicationName, language, start, size, searchMark, sort);
  }
}

export interface PaginatedFaqResult extends PaginatedResult<FaqDefinition> {
  faq: FaqDefinition[];
}

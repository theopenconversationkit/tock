import { Entry, PaginatedQuery, SearchMark } from '../../model/commons';
import { PaginatedResult } from '../../model/nlp';
import { FaqDefinition } from '../models';

export class FaqSearchQuery extends PaginatedQuery {
  constructor(
    public namespace: string,
    public applicationName: string,
    public language: string,
    public start: number,
    public size: number,
    public tags: string[] = [],
    public searchMark?: SearchMark,
    public search?: string,
    public sort?: Entry<string, boolean>[],
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

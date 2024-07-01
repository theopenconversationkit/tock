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

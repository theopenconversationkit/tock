import {Entry, PaginatedQuery, SearchMark } from "src/app/model/commons";

export class QaSearchQuery extends PaginatedQuery {

  constructor(public namespace: string,
              public applicationName: string,
              public language: string,
              public start: number,
              public size: number,
              public tags: string[] = [],
              public searchMark?: SearchMark,
              public search?: string,
              public sort?: Entry<string, boolean>[],
              public enabled: boolean = false,
              public user?:string,
              public allButUser?:string) {
    super(namespace, applicationName, language, start, size, searchMark, sort)
  }
}

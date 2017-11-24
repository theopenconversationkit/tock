/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Component, Input} from "@angular/core";
import {PaginatedResult, SearchQuery, Sentence, SentenceStatus} from "../model/nlp";
import {NlpService} from "../nlp-tabs/nlp.service";
import {StateService} from "../core/state.service";
import {ScrollComponent} from "../scroll/scroll.component";
import {PaginatedQuery} from "../model/commons";
import {Observable} from "rxjs/Observable";

@Component({
  selector: 'tock-sentences-scroll',
  templateUrl: './sentences-scroll.component.html',
  styleUrls: ['./sentences-scroll.component.css']
})
export class SentencesScrollComponent extends ScrollComponent<Sentence> {

  @Input() filter: SentenceFilter;
  @Input() displayArchiveButton: boolean = true;
  @Input() displayProbabilities: boolean = false;
  @Input() displayStatus: boolean = false;

  constructor(state: StateService, private nlp: NlpService) {
    super(state);
  }

  search(query: PaginatedQuery): Observable<PaginatedResult<Sentence>> {
    return this.nlp.searchSentences(new SearchQuery(
      query.namespace,
      query.applicationName,
      query.language,
      query.start,
      query.size,
      this.filter.search,
      this.filter.intentId,
      this.filter.status,
      !this.filter.entityType || this.filter.entityType.length === 0 ? null : this.filter.entityType,
      !this.filter.entityRole || this.filter.entityRole.length === 0 ? null : this.filter.entityRole,
      this.filter.modifiedAfter)
    );
  }


  dataEquals(d1: Sentence, d2: Sentence): boolean {
    return d1.text === d2.text
  }
}

export class SentenceFilter {
  constructor(public search?: string,
              public intentId?: string,
              public status?: SentenceStatus[],
              public entityType?: string,
              public entityRole?: string,
              public modifiedAfter?: Date) {
  }

  clone(): SentenceFilter {
    return new SentenceFilter(this.search, this.intentId, this.status, this.entityType, this.entityRole, this.modifiedAfter);
  }
}

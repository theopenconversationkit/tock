import {Injectable} from '@angular/core';
import {Qa, QaStatus} from '../common/model/qa';
import {MOCK_QA_TITLES, MOCK_QA_DEFAULT_LABEL, MOCK_QA_DEFAULT_DESCRIPTION} from '../common/mock/qa.mock';

import {empty, Observable, of } from 'rxjs';
import {PaginatedResult, SearchQuery, Sentence, SentenceStatus} from 'src/app/model/nlp';

@Injectable()
export class QaService {

  // generate fake data for pagination
  mockData: Qa[] = Array<number>(19999).fill(0).map((_, index) => {
    return {
      label: `${MOCK_QA_DEFAULT_LABEL} ${index+1}`,
      description: `${MOCK_QA_DEFAULT_DESCRIPTION} ${index+1}`,
      title: MOCK_QA_TITLES[index % MOCK_QA_TITLES.length], // pick random (rotating) title
      enabled: (index < 5),
      status: QaStatus.model
    };
  });

  constructor() {
  }

  // fake real backend call
  public save(qa: Qa, cancel$: Observable<any> = empty()): Observable<Qa> {
    let dirty = false;

    this.mockData = this.mockData.map(item => {
      if (item.label === qa.label) { // arbitrary chosen unicity key, there must be a better one
        dirty = true;
        return qa;
      } else {
        return item;
      }
    });

    if (!dirty) { // when no match
      this.mockData.push(qa);
    }

    return of(qa);
  }

  // fake real backend call
  searchQas(query: SearchQuery): Observable<PaginatedResult<Qa>> {

    // guard against empty case
    if (query.start < 0 || query.size <= 0) {
      return of({
        start: 0,
        end: 0,
        rows: [],
        total: this.mockData.length
      });
    }

    // simulate backend text search
    const data = this.mockData
      .filter(qa => {
        const predicates: Array<(Qa) => boolean> = [];

        predicates.push(
          (qa) => qa.status !== QaStatus.deleted
        );

        const lowerSearch = (query.search || '').toLowerCase().trim();
        if (lowerSearch) {
          predicates.push(
            (qa) => qa.label.toLowerCase().includes(lowerSearch) ||
              qa.description.toLowerCase().includes(lowerSearch) ||
              qa.title.toLowerCase().includes(lowerSearch)
          );
        }

        if (query.onlyToReview) {
          predicates.push(
            (qa) => (true === qa.enabled)
          );
        }

        // Return true when all criterias pass
        return predicates.reduce((accepted, predicate) => {
          return accepted && predicate(qa);
        }, true);
      });

    // simulate backend pagination
    const chunk = data.slice(query.start, query.start + query.size);

    // simulate backend output
    return of({
      start: query.start,
      end: query.start + query.size,
      rows: chunk,
      total: data.length
    });
  }


}

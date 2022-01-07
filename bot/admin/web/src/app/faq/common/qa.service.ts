import {Injectable} from '@angular/core';
import {MOCK_QA_TITLES, MOCK_QA_DEFAULT_LABEL, MOCK_QA_DEFAULT_DESCRIPTION, MOCK_FREQUENT_QUESTIONS} from '../common/mock/qa.mock';

import {empty, Observable, of } from 'rxjs';
import {PaginatedResult, SearchQuery, Sentence, SentenceStatus} from 'src/app/model/nlp';
import { FrequentQuestion, QaStatus } from './model/frequent-question';

@Injectable()
export class QaService {

  // generate fake data for pagination
  mockData: FrequentQuestion[] = Array<number>(19999).fill(0).map((_, index) => {
    const template = MOCK_FREQUENT_QUESTIONS[index % MOCK_FREQUENT_QUESTIONS.length];

    return {
      utterances: [{value: `${template.utterances[0].value} ${index+1}`, primary: true}],
      answer: `${template.answer} ${index+1}`,
      title: template.title, // pick random (rotating) title
      enabled: (index < 5),
      status: QaStatus.model,
      tags: []
    };
  });

  constructor() {
  }

  // fake real backend call
  public save(fq: FrequentQuestion, cancel$: Observable<any> = empty()): Observable<FrequentQuestion> {
    let dirty = false;

    this.mockData = this.mockData.map(item => {
      if (item.title === fq.title) { // arbitrary chosen unicity key, there must be a better one
        dirty = true;
        return fq;
      } else {
        return item;
      }
    });

    if (!dirty) { // when no match
      this.mockData.push(fq);
    }

    return of(fq);
  }

  // fake real backend call
  searchQas(query: SearchQuery): Observable<PaginatedResult<FrequentQuestion>> {

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
      .filter(fq => {
        const predicates: Array<(FrequentQuestion) => boolean> = [];

        predicates.push(
          (fq) => fq.status !== QaStatus.deleted
        );

        const lowerSearch = (query.search || '').toLowerCase().trim();
        if (lowerSearch) {
          predicates.push(
            (fq) => (fq.label || '').toLowerCase().includes(lowerSearch) ||
              (fq.description || '').toLowerCase().includes(lowerSearch) ||
              (fq.title || '').toLowerCase().includes(lowerSearch)
          );
        }

        if (query.onlyToReview) {
          predicates.push(
            (fq) => (true === fq.enabled)
          );
        }

        // Return true when all criterias pass
        return predicates.reduce((accepted, predicate) => {
          return accepted && predicate(fq);
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

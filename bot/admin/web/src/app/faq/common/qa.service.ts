import {Injectable} from '@angular/core';
import {MOCK_QA_TITLES, MOCK_QA_DEFAULT_LABEL, MOCK_QA_DEFAULT_DESCRIPTION, MOCK_FREQUENT_QUESTIONS} from '../common/mock/qa.mock';

import {empty, Observable, of } from 'rxjs';
import {PaginatedResult, SearchQuery, Sentence, SentenceStatus} from 'src/app/model/nlp';
import { FrequentQuestion, Utterance } from './model/frequent-question';
import { QaSearchQuery } from './model/qa-search-query';

@Injectable()
export class QaService {

  appIdByAppName = new Map<string, string>(); // for mock purpose only

  // generate fake data for pagination
  mockData: FrequentQuestion[] = [];

  constructor() {
  }

  // add random data at initialization until real backend is there instead
  public setupMockData({applicationId, applicationName, language}:
                         {applicationId: string, applicationName: string, language: string}): void {

    this.appIdByAppName.set(applicationName, applicationId);

    // when there is already data for given bot / language
    if (this.mockData.some((fq: FrequentQuestion) => fq.applicationId === applicationId && fq.language == language)) {
      // no need to add mock data
      return;
    }

    const seed = (new Date().getTime()); // timestamp as random seed
    const now = new Date();

    const someData: FrequentQuestion[] = Array<number>(1).fill(0).map((_, index) => {
      const template = MOCK_FREQUENT_QUESTIONS[index % MOCK_FREQUENT_QUESTIONS.length];

      return {
        id: ''+(index + 1) + '_' + seed,
        utterances: [`${template.utterances[0]} ${index+1}`],
        answer: `${template.answer} ${index+1}`,
        title: template.title, // pick random (rotating) title
        enabled: (index < 5),
        status: SentenceStatus.model,
        tags: (template.tags  || []).slice(),
        applicationId,
        language,
        creationDate: now,
        updateDate: now
      };
    });

    someData.forEach(fq => this.mockData.push(fq));
  }

  // fake real backend call
  public save(fq: FrequentQuestion, cancel$: Observable<any> = empty()): Observable<FrequentQuestion> {

    let dirty = false;

    this.mockData = this.mockData.map(item => {
      if (fq.id && item.id === fq.id) { // arbitrary chosen unicity key, there must be a better one
        dirty = true;
        fq.updateDate = new Date();
        return fq;
      } else {
        return item;
      }
    });

    if (!dirty) { // when no match
      fq.id = String(Math.trunc(Math.random() * 1000000));
      this.mockData.push(fq);
    }

    return of(fq);
  }

  // fake real backend call
  searchQas(query: QaSearchQuery): Observable<PaginatedResult<FrequentQuestion>> {

    // Because for historical reason, PaginatedResilt hold a application name instead of application id
    const queryApplicationId = this.appIdByAppName.get(query.applicationName);

    // guard against empty case
    if (query.start < 0 || query.size <= 0) {
      return of({
        start: 0,
        end: 0,
        rows: [],
        total: this.mockData.length
      });
    }

    // simulate backend query (filtering on status, application name etc..)
    const data = this.mockData
      .filter(fq => {
        const predicates: Array<(FrequentQuestion) => boolean> = [];

        predicates.push(
          (fq: FrequentQuestion) => fq.status !== SentenceStatus.deleted
        );

        predicates.push(
          (fq: FrequentQuestion) => fq.applicationId === queryApplicationId
        );

        const lowerSearch = (query.search || '').toLowerCase().trim();
        if (lowerSearch) {
          predicates.push(
            (fq: FrequentQuestion) => (fq.answer || '').toLowerCase().includes(lowerSearch) ||
              (fq.description || '').toLowerCase().includes(lowerSearch) ||
              fq.utterances.some((u: Utterance) => u.toLowerCase().includes(lowerSearch)) ||
              (fq.title || '').toLowerCase().includes(lowerSearch)
          );
        }

        if(query.tags.length > 0) {
          predicates.push(
            (fq: FrequentQuestion) => fq.tags.some(
              tag => query.tags.some(queryTag => queryTag.toLowerCase() === tag.toLowerCase())
            )
          );
        }

        if (query.enabled) {
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

  getAvailableTags(applicationId: string, language: string): Observable<string[]> {
    const tagSet = new Set<string>();

    // simulate backend aggregation query
    this.mockData
      .filter(fq => fq.applicationId === applicationId && fq.language === language)
      .forEach(tagSet.add.bind(tagSet));

    return of(Array.from(tagSet));
  }


}

import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { tap, take, share } from 'rxjs/operators';
import { StateService } from '../../core-nlp/state.service';
import { PaginatedQuery } from '../../model/commons';
import { SearchQuery, SentencesResult } from '../../model/nlp';
import { NlpService } from '../../nlp-tabs/nlp.service';
import { stringifiedCleanObject } from '../commons/utils';
import { ScenarioItem, ScenarioVersion, ScenarioVersionExtended } from '../models';
import { ScenarioService } from '../services/scenario.service';

export type ScenarioItemExtended = ScenarioItem & { _sentencesLoading?: boolean };

@Injectable({
  providedIn: 'root'
})
export class ScenarioDesignerService {
  constructor(private scenarioService: ScenarioService, private router: Router, protected state: StateService, private nlp: NlpService) {}

  public scenarioDesignerCommunication = new Subject<any>();

  saveScenario(scenarioVersion: ScenarioVersionExtended): Observable<ScenarioVersion> {
    const cleanScenario = JSON.parse(stringifiedCleanObject(scenarioVersion));
    delete cleanScenario.creationDate;
    delete cleanScenario.updateDate;

    return this.scenarioService
      .updateScenarioVersion(scenarioVersion._scenarioGroupId, cleanScenario)
      .pipe(tap((data) => this.updateScenarioBackup(data)));
  }

  exitDesigner(): void {
    this.router.navigateByUrl('/scenarios');
  }

  updateScenarioBackup(data: ScenarioVersion): void {
    this.scenarioDesignerCommunication.next({
      type: 'updateScenarioBackup',
      data: data
    });
  }

  createSearchIntentsQuery(params: { searchString?: string; intentId?: string }): SearchQuery {
    const cursor: number = 0;
    const pageSize: number = 50;
    const mark = null;
    const paginatedQuery: PaginatedQuery = this.state.createPaginatedQuery(cursor, pageSize, mark);
    return new SearchQuery(
      paginatedQuery.namespace,
      paginatedQuery.applicationName,
      paginatedQuery.language,
      paginatedQuery.start,
      paginatedQuery.size,
      paginatedQuery.searchMark,
      params.searchString || null,
      params.intentId || null
    );
  }

  grabIntentSentences(item: ScenarioItemExtended): Observable<SentencesResult> {
    if (!item.intentDefinition?.intentId) return;

    item._sentencesLoading = true;
    const searchQuery: SearchQuery = this.createSearchIntentsQuery({
      intentId: item.intentDefinition.intentId
    });

    const nlpSubscription = this.nlp.searchSentences(searchQuery).pipe(take(1), share());

    nlpSubscription.subscribe((sentencesResearch) => {
      item.intentDefinition._sentences = sentencesResearch.rows;
      item._sentencesLoading = false;
    });
    return nlpSubscription;
  }
}

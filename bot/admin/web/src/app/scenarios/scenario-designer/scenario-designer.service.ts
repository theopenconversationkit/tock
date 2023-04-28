import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { tap, take, share } from 'rxjs/operators';

import { ClassifiedEntity, SearchQuery, SentencesResult } from '../../model/nlp';
import { NlpService } from '../../nlp-tabs/nlp.service';
import { stringifiedCleanObject } from '../commons/utils';
import { ScenarioItem, ScenarioVersion, ScenarioVersionExtended, TempEntity } from '../models';
import { ScenarioService } from '../services';

export type ScenarioItemExtended = ScenarioItem & { _sentencesLoading?: boolean };

@Injectable({
  providedIn: 'root'
})
export class ScenarioDesignerService {
  constructor(private scenarioService: ScenarioService, private router: Router, private nlp: NlpService) {}

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
    // the set time out method is used to wait for the exit of the full screen mode if it is active
    setTimeout(() => {
      this.router.navigateByUrl('/scenarios');
    });
  }

  updateScenarioBackup(data: ScenarioVersion): void {
    this.scenarioDesignerCommunication.next({
      type: 'updateScenarioBackup',
      data: data
    });
  }

  grabIntentSentences(item: ScenarioItemExtended): Observable<SentencesResult> {
    if (!item.intentDefinition?.intentId) return;

    item._sentencesLoading = true;
    const searchQuery: SearchQuery = this.scenarioService.createSearchIntentsQuery({
      intentId: item.intentDefinition.intentId
    });

    const sentenceSubscription = this.nlp.searchSentences(searchQuery).pipe(take(1), share());

    sentenceSubscription.subscribe((sentencesResearch) => {
      // In the case where the scenario comes from an export=>import, all the sentences have been preserved in the tempSentences. But if the sentences exist on the current application, we can delete them. Since they already exist, we won't have to recreate them.
      sentencesResearch.rows.forEach((sentence) => {
        const existingTempSentenceIndex = item.intentDefinition.sentences.findIndex(
          (s) => s.language === sentence.language && s.query === sentence.text
        );
        if (existingTempSentenceIndex >= 0) {
          // But an existing classified sentence may have been transferred to tempSentences because we wanted to add or remove an entity to it. In this case it should not be purged
          if (
            this.areEntitiesIdentical(
              sentence.classification.entities,
              item.intentDefinition.sentences[existingTempSentenceIndex].classification.entities
            )
          ) {
            item.intentDefinition.sentences.splice(existingTempSentenceIndex, 1);
          }
        }
      });

      item.intentDefinition._sentences = sentencesResearch.rows;
      item._sentencesLoading = false;
    });
    return sentenceSubscription;
  }

  areEntitiesIdentical(entitiesA: ClassifiedEntity[], entitiesB: TempEntity[]): boolean {
    if (entitiesA.length != entitiesB.length) return false;
    let entitiesAreEquals = true;
    entitiesA.forEach((entityA) => {
      if (
        !entitiesB.find((entityB) => {
          return (
            entityB.type === entityA.type && entityB.role === entityA.role && entityB.start === entityA.start && entityB.end === entityA.end
          );
        })
      )
        entitiesAreEquals = false;
    });
    return entitiesAreEquals;
  }
}

import { DatePipe } from '@angular/common';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, forkJoin, iif, merge, Observable, of } from 'rxjs';
import { map, tap, switchMap, filter, concatMap, take } from 'rxjs/operators';

import { BotService } from '../../bot/bot-service';
import { CreateI18nLabelsRequest, I18nLabel, I18nLocalizedLabel } from '../../bot/model/i18n';
import { ApplicationService } from '../../core-nlp/applications.service';
import { StateService } from '../../core-nlp/state.service';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { Application } from '../../model/application';
import { PaginatedQuery } from '../../model/commons';
import { Intent, SearchQuery, SentencesResult } from '../../model/nlp';
import { NlpService } from '../../nlp-tabs/nlp.service';
import { deepCopy, exportJsonDump } from '../../shared/utils';
import { normalizedSnakeCase, stringifiedCleanObject } from '../commons/utils';
import {
  ScenarioVersion,
  TickStory,
  ScenarioVersionExtended,
  ScenarioGroupExtended,
  ScenarioGroup,
  ExportableScenarioGroup,
  SCENARIO_STATE,
  ScenarioGroupUpdate,
  Handler,
  TempSentence,
  ScenarioAnswer
} from '../models';
import { ScenarioApiService } from './scenario.api.service';

interface ScenarioState {
  loaded: boolean;
  loading: boolean;
  scenariosGroups: ScenarioGroupExtended[];
  tags: string[];
  categories: string[];
}

const scenariosInitialState: ScenarioState = {
  loaded: false,
  loading: false,
  scenariosGroups: [],
  tags: [],
  categories: []
};

@Injectable()
export class ScenarioService {
  private _state: BehaviorSubject<ScenarioState>;
  state$: Observable<ScenarioState>;

  constructor(
    private botService: BotService,
    private scenarioApiService: ScenarioApiService,
    private applicationService: ApplicationService,
    private datePipe: DatePipe,
    private router: Router,
    private botConfigurationService: BotConfigurationService,
    protected stateService: StateService,
    private nlp: NlpService
  ) {
    this._state = new BehaviorSubject(scenariosInitialState);
    this.state$ = this._state.asObservable();
    this.observeConfigurationChanges();
  }

  observeConfigurationChanges(): void {
    let obervable = this.stateService.configurationChange.pipe(
      switchMap(() => this.botConfigurationService.grabConfigurations()),
      tap((configurations) => {
        if (configurations?.length) this.getScenariosGroups(true);
      })
    );
    obervable.subscribe();
  }

  getState(): ScenarioState {
    return this._state.getValue();
  }

  setState(state: ScenarioState): void {
    return this._state.next(state);
  }

  setScenariosGroupsLoading(): void {
    this.setState({
      ...this.getState(),
      loading: true,
      loaded: false
    });
  }

  setScenariosGroupsUnloading(): void {
    this.setState({
      ...this.getState(),
      loading: false,
      loaded: false
    });
  }

  setScenariosGroupsData(scenariosCollection: ScenarioGroupExtended[]): void {
    this.setState({
      ...this.getState(),
      loading: false,
      loaded: true,
      scenariosGroups: scenariosCollection,
      categories: this.updateCategoriesCache(scenariosCollection),
      tags: this.updateTagsCache(scenariosCollection)
    });
  }

  getScenariosGroups(forceReload: boolean = false): Observable<Array<ScenarioGroupExtended>> {
    if (forceReload) {
      this.setScenariosGroupsUnloading();
    }

    const scenariosState = this.state$;
    const notLoaded = scenariosState.pipe(
      filter((state) => !state.loaded && !state.loading),
      tap(() => this.setScenariosGroupsLoading()),
      switchMap(() => this.scenarioApiService.getScenariosGroups()),
      tap((scenariosCollection) => this.setScenariosGroupsData(scenariosCollection)),
      switchMap(() => scenariosState),
      map((state) => state.scenariosGroups)
    );
    const loaded = scenariosState.pipe(
      filter((state) => state.loaded === true),
      map((state) => state.scenariosGroups)
    );

    return merge(notLoaded, loaded);
  }

  getScenarioVersion(scenarioGroupId: string, scenarioVersionId: string): Observable<ScenarioVersionExtended | never> {
    let scenarioGroupInfo: ScenarioGroupExtended;
    return this.getScenariosGroups().pipe(
      switchMap(() => this.state$),
      map((data) => data.scenariosGroups.find((sg) => sg.id === scenarioGroupId)),
      tap((group) => {
        scenarioGroupInfo = group;
      }),
      // here we don't just filter as we want to let the stream flow if the version isn't found to inform the user on the component side
      map((group) => (group ? group.versions.find((gv) => gv.id === scenarioVersionId) : undefined)),
      concatMap((version) => {
        return iif(
          () => version && typeof version.data === 'undefined',
          // the version exist but it's data isn't yet loaded, let's call the endpoint
          this.grabScenarioVersionWithData(scenarioGroupId, scenarioVersionId).pipe(
            map((version) => ({
              // we store the scenarioGroup name and id as expandos of the scenarioVersion
              _name: scenarioGroupInfo.name,
              _scenarioGroupId: scenarioGroupInfo.id,
              ...version
            }))
          ),
          iif(
            () => version && typeof version.data !== 'undefined',
            // the versiuon exist and it's data is already loaded
            of({
              // we store the scenarioGroup name and id as expandos of the scenarioVersion
              _name: scenarioGroupInfo?.name,
              _scenarioGroupId: scenarioGroupInfo?.id,
              ...version
            }),
            // the version doesn't exist at all, we send null to inform the requesting component
            of(null)
          )
        );
      })
    );
  }

  private grabScenarioVersionWithData(scenarioGroupId: string, scenarioVersionId: string): Observable<ScenarioVersion> {
    return this.scenarioApiService
      .getScenarioVersion(scenarioGroupId, scenarioVersionId)
      .pipe(tap((scenarioVersionData) => this.setScenarioVersionData(scenarioGroupId, scenarioVersionData)));
  }

  setScenarioVersionData(scenarioGroupId: string, scenarioVersionData: ScenarioVersion): void {
    const state = this.getState();
    const scenarioGroup = state.scenariosGroups.find((sg) => sg.id === scenarioGroupId);
    const scenarioGroupIndex = state.scenariosGroups.indexOf(scenarioGroup);
    const scenarioVersion = scenarioGroup.versions.find((sv) => sv.id === scenarioVersionData.id);
    const scenarioVersionIndex = scenarioGroup.versions.indexOf(scenarioVersion);
    state.scenariosGroups[scenarioGroupIndex].versions[scenarioVersionIndex] = {
      ...scenarioVersion,
      ...scenarioVersionData
    };
    this.setState(state);
  }

  postScenarioGroup(scenarioGroup: ScenarioGroup): Observable<ScenarioGroupExtended> {
    return this.scenarioApiService.postScenarioGroup(scenarioGroup).pipe(
      tap((newScenarioGroup) => {
        const state = this.getState();
        state.scenariosGroups = [...state.scenariosGroups, newScenarioGroup];
        state.categories = this.updateCategoriesCache(state.scenariosGroups);
        state.tags = this.updateTagsCache(state.scenariosGroups);
        this.setState(state);
      })
    );
  }

  importScenarioGroup(scenarioGroup: ScenarioGroup): Observable<ScenarioGroupExtended> {
    return this.scenarioApiService.importScenarioGroup(scenarioGroup).pipe(
      tap((newScenarioGroup) => {
        const state = this.getState();
        state.scenariosGroups = [...state.scenariosGroups, newScenarioGroup];
        state.categories = this.updateCategoriesCache(state.scenariosGroups);
        state.tags = this.updateTagsCache(state.scenariosGroups);
        this.setState(state);
      })
    );
  }

  updateScenarioGroup(scenarioGroup: ScenarioGroupUpdate): Observable<ScenarioGroupExtended> {
    const state = this.getState();
    const existingScenarioGroup = state.scenariosGroups.find((s) => s.id === scenarioGroup.id);
    const existingScenarioGroupIndex = state.scenariosGroups.indexOf(existingScenarioGroup);
    if (existingScenarioGroupIndex > -1) {
      return this.scenarioApiService.updateScenarioGroup(scenarioGroup).pipe(
        tap((modifiedScenarioGroup) => {
          state.scenariosGroups[existingScenarioGroupIndex] = modifiedScenarioGroup;
          state.categories = this.updateCategoriesCache(state.scenariosGroups);
          state.tags = this.updateTagsCache(state.scenariosGroups);
          this.setState(state);
        })
      );
    }
  }

  deleteScenarioGroup(scenarioGroupId: string): Observable<boolean> {
    return this.scenarioApiService.deleteScenarioGroup(scenarioGroupId).pipe(
      tap(() => {
        const state = this.getState();
        state.scenariosGroups = state.scenariosGroups.filter((s) => s.id !== scenarioGroupId);
        state.categories = this.updateCategoriesCache(state.scenariosGroups);
        state.tags = this.updateTagsCache(state.scenariosGroups);
        this.setState(state);
      })
    );
  }

  patchScenarioGroupState(scenarioGroupUpdate: Partial<ScenarioGroupExtended>): void {
    const state = this.getState();
    const existingScenarioGroup = state.scenariosGroups.find((s) => s.id === scenarioGroupUpdate.id);
    const existingScenarioGroupIndex = state.scenariosGroups.indexOf(existingScenarioGroup);
    if (existingScenarioGroupIndex > -1) {
      state.scenariosGroups[existingScenarioGroupIndex] = {
        ...state.scenariosGroups[existingScenarioGroupIndex],
        ...scenarioGroupUpdate
      };
      this.setState(state);
    }
  }

  postScenarioVersion(scenarioGroupId: string, scenarioVersion: ScenarioVersion): Observable<ScenarioVersion> {
    const state = this.getState();
    const existingScenarioGroup = state.scenariosGroups.find((s) => s.id === scenarioGroupId);
    const existingScenarioGroupIndex = state.scenariosGroups.indexOf(existingScenarioGroup);
    if (existingScenarioGroupIndex > -1) {
      return this.scenarioApiService.postScenarioVersion(scenarioGroupId, scenarioVersion).pipe(
        tap((newScenario) => {
          state.scenariosGroups[existingScenarioGroupIndex].versions.push(newScenario);
          this.setState(state);
        })
      );
    }
  }

  updateScenarioVersion(scenarioGroupId: string, scenarioVersion: ScenarioVersion): Observable<ScenarioVersion> {
    const state = this.getState();
    const existingScenarioGroup = state.scenariosGroups.find((s) => s.id === scenarioGroupId);
    const existingScenarioGroupIndex = state.scenariosGroups.indexOf(existingScenarioGroup);
    if (existingScenarioGroupIndex > -1) {
      const existingScenarioVersion = existingScenarioGroup.versions.find((sv) => sv.id === scenarioVersion.id);
      const existingScenarioVersionIndex = existingScenarioGroup.versions.indexOf(existingScenarioVersion);
      if (existingScenarioVersionIndex > -1) {
        return this.scenarioApiService.updateScenarioVersion(scenarioGroupId, scenarioVersion).pipe(
          tap((modifiedScenarioVersion) => {
            state.scenariosGroups[existingScenarioGroupIndex].versions[existingScenarioVersionIndex] = modifiedScenarioVersion;
            this.setState(state);
          })
        );
      }
    }
  }

  deleteScenarioVersion(scenarioGroupId: string, scenarioVersionId: string): Observable<boolean> {
    const state = this.getState();
    const existingScenarioGroup = state.scenariosGroups.find((s) => s.id === scenarioGroupId);
    const existingScenarioGroupIndex = state.scenariosGroups.findIndex((s) => s === existingScenarioGroup);
    if (existingScenarioGroupIndex > -1) {
      return this.scenarioApiService.deleteScenarioVersion(scenarioGroupId, scenarioVersionId).pipe(
        tap(() => {
          if (existingScenarioGroup.versions.length === 1) {
            // if we delete the last version of the group, we delete the whole group
            state.scenariosGroups = state.scenariosGroups.filter((s) => s.id !== scenarioGroupId);
            state.categories = this.updateCategoriesCache(state.scenariosGroups);
            state.tags = this.updateTagsCache(state.scenariosGroups);
          } else {
            const scenarioGroup = state.scenariosGroups[existingScenarioGroupIndex];
            const scenarioVersion = scenarioGroup.versions.find((v) => v.id === scenarioVersionId);

            state.scenariosGroups[existingScenarioGroupIndex] = {
              ...existingScenarioGroup,
              enabled: scenarioVersion.state === SCENARIO_STATE.current ? null : scenarioGroup.enabled,
              versions: existingScenarioGroup.versions.filter((s) => s.id !== scenarioVersionId)
            };
          }

          this.setState(state);
        })
      );
    }
  }

  getActionHandlers(): Observable<Handler[]> {
    return this.scenarioApiService.getActionHandlers();
  }

  postTickStory(tickStory: TickStory): Observable<TickStory> {
    return this.scenarioApiService.postTickStory(tickStory);
  }

  private updateCategoriesCache(scenarios: ScenarioGroupExtended[]): string[] {
    return [...new Set([...scenarios.map((v) => v.category)])].sort().filter((c) => c);
  }

  private updateTagsCache(scenarios: ScenarioGroupExtended[]): string[] {
    return [
      ...new Set(
        <string>[].concat.apply(
          [],
          scenarios.map((s: ScenarioGroupExtended) => s.tags)
        )
      )
    ]
      .sort()
      .filter((t) => t);
  }

  createSearchIntentsQuery(params: { searchString?: string; intentId?: string }): SearchQuery {
    const cursor: number = 0;
    const pageSize: number = 50;
    const mark = null;
    const paginatedQuery: PaginatedQuery = this.stateService.createPaginatedQuery(cursor, pageSize, mark);
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

  loadScenariosAndDownload(scenariosGroups: ScenarioGroup[], exportableGroups: ExportableScenarioGroup[]): void {
    const scenariosLoaders: Observable<ScenarioVersionExtended>[] = [];
    const sentencesLoaders: Observable<SentencesResult>[] = [];

    exportableGroups.forEach((exportableGroup: ExportableScenarioGroup) => {
      exportableGroup.versionsIds.forEach((versionId: string) => {
        scenariosLoaders.push(this.getScenarioVersion(exportableGroup.id, versionId).pipe(take(1)));
      });
    });

    forkJoin(scenariosLoaders).subscribe(() => {
      const listedIntentIds: string[] = [];
      exportableGroups.forEach((exportableGroup: ExportableScenarioGroup) => {
        const scenarioGroup: ScenarioGroup = scenariosGroups.find((g) => g.id === exportableGroup.id);
        exportableGroup.group = deepCopy(scenarioGroup);
        exportableGroup.group.versions = exportableGroup.group.versions.filter((v) => exportableGroup.versionsIds.includes(v.id));

        exportableGroup.group.versions.forEach((version: ScenarioVersion) => {
          version.data?.scenarioItems.forEach((item) => {
            if (item.intentDefinition?.intentId) {
              const existingIntent: Intent = this.stateService.findIntentById(item.intentDefinition.intentId);

              if (existingIntent) {
                if (!listedIntentIds.includes(item.intentDefinition.intentId)) {
                  listedIntentIds.push(item.intentDefinition.intentId);
                  const searchQuery: SearchQuery = this.createSearchIntentsQuery({
                    intentId: item.intentDefinition.intentId
                  });

                  sentencesLoaders.push(this.nlp.searchSentences(searchQuery).pipe(take(1)));
                }
              }
            }
          });
        });
      });

      if (!sentencesLoaders.length) {
        exportableGroups.forEach((exportableGroup) => {
          this.downloadScenarioGroup(exportableGroup.group);
        });
      } else {
        forkJoin(sentencesLoaders).subscribe((results: SentencesResult[]) => {
          results.forEach((sentencesResults: SentencesResult) => {
            sentencesResults.rows.forEach((sentence) => {
              exportableGroups.forEach((exportableGroup) => {
                exportableGroup.versionsIds.forEach((versionId) => {
                  const version: ScenarioVersion = exportableGroup.group.versions.find((v) => v.id === versionId);
                  version.data?.scenarioItems.forEach((item) => {
                    const intentId = item.intentDefinition?.intentId;
                    if (intentId === sentence.classification.intentId) {
                      const app = this.stateService.currentApplication;
                      item.intentDefinition.sentences.push(
                        new TempSentence(app.namespace, app.name, sentence.language, sentence.text, false, '')
                      );
                    }
                  });
                });
              });
            });
          });

          exportableGroups.forEach((exportableGroup) => {
            this.downloadScenarioGroup(exportableGroup.group);
          });
        });
      }
    });
  }

  saveAnswers(answers: ScenarioAnswer[]): Observable<I18nLabel> {
    const request = new CreateI18nLabelsRequest('scenario', answers[0].answer, answers[0].locale, this.createI18nLocalizedLabels(answers));

    return this.botService.createI18nLabels(request);
  }

  patchAnswer(i18nLabel: I18nLabel, answers: ScenarioAnswer[]): Observable<boolean> {
    i18nLabel.i18n = this.createI18nLocalizedLabels(answers);

    return this.botService.saveI18nLabel(i18nLabel);
  }

  private createI18nLocalizedLabels(answers: ScenarioAnswer[]): I18nLocalizedLabel[] {
    return answers.map(
      (answerToPatch) => new I18nLocalizedLabel(answerToPatch.locale, answerToPatch.interfaceType, answerToPatch.answer, true, null, [])
    );
  }

  private downloadScenarioGroup(scenarioGroup: ScenarioGroup): void {
    this.applicationService.retrieveCurrentApplication().subscribe((currentApplication: Application) => {
      const fileName = [
        currentApplication.name,
        'SCENARIO',
        normalizedSnakeCase(scenarioGroup.name),
        this.datePipe.transform(scenarioGroup.creationDate, 'yyyy-MM-dd')
      ].join('_');

      exportJsonDump(JSON.parse(stringifiedCleanObject(scenarioGroup)), fileName);
    });
  }

  /**
   * Method to redirect to the designer from a group of scenarios.
   * We retrieve the last version created in draft status. If there is none, we take the current version, otherwise the last available version.
   * @param {ScenarioGroup} scenarioGroup
   */
  redirectToDesigner(scenarioGroup: ScenarioGroup): void {
    let scenarioToOpen: ScenarioVersion;
    const drafts = scenarioGroup.versions.filter((scn) => scn.state === SCENARIO_STATE.draft);

    if (drafts.length) {
      scenarioToOpen = drafts.sort((a, b) => new Date(b.creationDate).getTime() - new Date(a.creationDate).getTime())[0];
    } else {
      const current = scenarioGroup.versions.filter((scn) => scn.state === SCENARIO_STATE.current);

      if (current.length) {
        scenarioToOpen = current[current.length - 1];
      } else {
        scenarioToOpen = scenarioGroup.versions[scenarioGroup.versions.length - 1];
      }
    }

    this.router.navigateByUrl(`/scenarios/${scenarioGroup.id}/${scenarioToOpen.id}`);
  }
}

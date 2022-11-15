import { DatePipe } from '@angular/common';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, forkJoin, iif, merge, Observable, of } from 'rxjs';
import { map, tap, switchMap, filter, concatMap, take } from 'rxjs/operators';

import { ApplicationService } from '../../core-nlp/applications.service';
import { Application } from '../../model/application';
import { exportJsonDump } from '../../shared/utils';
import { deepCopy, normalizedSnakeCase, stringifiedCleanObject } from '../commons/utils';
import {
  ScenarioDebug,
  ScenarioVersion,
  TickStory,
  ScenarioVersionExtended,
  ScenarioGroupExtended,
  ScenarioGroup,
  ExportableScenarioGroup,
  SCENARIO_STATE
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
    private scenarioApiService: ScenarioApiService,
    private applicationService: ApplicationService,
    private datePipe: DatePipe,
    private router: Router
  ) {
    this._state = new BehaviorSubject(scenariosInitialState);
    this.state$ = this._state.asObservable();
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

  setScenariosGroupsData(scenariosCollection): void {
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

  postScenarioGroup(scenarioGroup: ScenarioGroupExtended): Observable<ScenarioGroupExtended> {
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

  importScenarioGroup(scenarioGroup: ScenarioGroupExtended): Observable<ScenarioGroupExtended> {
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

  updateScenarioGroup(
    scenarioGroup: Omit<ScenarioGroupExtended, 'creationDate' | 'updateDate' | 'versions'>
  ): Observable<ScenarioGroupExtended> {
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

  patchScenarioGroupState(scenarioGroupUpdate: Partial<ScenarioGroupExtended>) {
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
            state.scenariosGroups[existingScenarioGroupIndex] = {
              ...existingScenarioGroup,
              versions: existingScenarioGroup.versions.filter((s) => s.id !== scenarioVersionId)
            };
          }

          this.setState(state);
        })
      );
    }
  }

  getActionHandlers(): Observable<string[]> {
    return this.scenarioApiService.getActionHandlers();
  }

  getScenarioDebug(): Observable<ScenarioDebug> {
    return this.scenarioApiService.getScenarioDebug();
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

  loadScenariosAndDownload(scenariosGroups: ScenarioGroup[], exportableGroups: ExportableScenarioGroup[]): void {
    const loaders: Observable<ScenarioVersionExtended>[] = [];
    exportableGroups.forEach((group) => {
      group.versions.forEach((versionId) => {
        loaders.push(this.getScenarioVersion(group.id, versionId).pipe(take(1)));
      });
    });

    forkJoin(loaders).subscribe(() => {
      exportableGroups.forEach((group) => {
        const scenarioGroup = scenariosGroups.find((g) => g.id === group.id);
        const copy = deepCopy(scenarioGroup);
        copy.versions = copy.versions.filter((v) => group.versions.includes(v.id));
        this.downloadScenarioGroup(copy);
      });
    });
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

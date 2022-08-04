import { Injectable } from '@angular/core';
import { BehaviorSubject, merge, Observable, of } from 'rxjs';
import { map, tap, switchMap, filter } from 'rxjs/operators';

import { Scenario } from '../models';
import { ScenarioApiService } from './scenario.api.service';

interface ScenarioState {
  loaded: boolean;
  loading: boolean;
  scenarios: Scenario[];
  tags: string[];
  categories: string[];
}

const scenariosInitialState: ScenarioState = {
  loaded: false,
  loading: false,
  scenarios: [],
  tags: [],
  categories: []
};

@Injectable()
export class ScenarioService {
  private _state: BehaviorSubject<ScenarioState>;
  state$: Observable<ScenarioState>;

  constructor(private scenarioApiService: ScenarioApiService) {
    this._state = new BehaviorSubject(scenariosInitialState);
    this.state$ = this._state.asObservable();
  }

  getState(): ScenarioState {
    return this._state.getValue();
  }
  setState(state: ScenarioState): void {
    return this._state.next(state);
  }

  setScenariosLoading(): void {
    let state = this.getState();
    state = {
      ...state,
      loading: true,
      loaded: false
    };
    this.setState(state);
  }

  setScenariosUnloading(): void {
    let state = this.getState();
    state = {
      ...state,
      loading: false,
      loaded: false
    };
    this.setState(state);
  }

  setScenariosData(scenariosCollection): void {
    let state = this.getState();
    state = {
      ...state,
      loading: false,
      loaded: true,
      scenarios: scenariosCollection,
      categories: this.updateCategoriesCache(scenariosCollection),
      tags: this.updateTagsCache(scenariosCollection)
    };
    this.setState(state);
  }

  getScenarios(forceReload: boolean = false): Observable<Array<Scenario>> {
    if (forceReload) {
      this.setScenariosUnloading();
    }

    const scenariosState = this.state$;
    const notLoaded = scenariosState.pipe(
      filter((state) => !state.loaded && !state.loading),
      tap(() => this.setScenariosLoading()),
      switchMap(() => this.scenarioApiService.getScenarios()),
      tap((scenariosCollection) => this.setScenariosData(scenariosCollection)),
      switchMap(() => scenariosState),
      map((state) => state.scenarios)
    );
    const loaded = scenariosState.pipe(
      filter((state) => state.loaded === true),
      map((state) => state.scenarios)
    );
    return merge(notLoaded, loaded);
  }

  // getScenario(id: string): Observable<Scenario> {
  //   return this.getScenarios().pipe(
  //     switchMap(() => this.state$),
  //     mergeMap((state) => state.scenarios),
  //     filter((scenario) => scenario.id === id)
  //   );
  // }

  getScenario(id: string): Observable<Scenario | never> {
    return this.getScenarios().pipe(
      switchMap(() => this.state$),
      switchMap((state: ScenarioState) => {
        let res: Scenario = state.scenarios.find((s) => s.id === id);
        if (res) return of(res);
        else return of(null);
      })
    );
  }

  postScenario(scenario: Scenario): Observable<Scenario> {
    return this.scenarioApiService.postScenario(scenario).pipe(
      tap((newScenario) => {
        let state = this.getState();
        state.scenarios = [...state.scenarios, newScenario];
        state.categories = this.updateCategoriesCache(state.scenarios);
        state.tags = this.updateTagsCache(state.scenarios);
        this.setState(state);
      })
    );
  }

  putScenario(id: string, scenario: Scenario): Observable<Scenario> {
    return this.scenarioApiService.putScenario(id, scenario).pipe(
      tap((modifiedScenario) => {
        let state = this.getState();
        const scenario = state.scenarios.find((s) => s.id === id);
        if (scenario) {
          const index = state.scenarios.indexOf(scenario);
          state.scenarios[index] = modifiedScenario;
          state.categories = this.updateCategoriesCache(state.scenarios);
          state.tags = this.updateTagsCache(state.scenarios);
          this.setState(state);
        }
      })
    );
  }

  deleteScenario(id: string): Observable<any> {
    return this.scenarioApiService.deleteScenario(id).pipe(
      tap(() => {
        let state = this.getState();
        state.scenarios = state.scenarios.filter((s) => s.id !== id);
        state.categories = this.updateCategoriesCache(state.scenarios);
        state.tags = this.updateTagsCache(state.scenarios);
        this.setState(state);
      })
    );
  }

  updateCategoriesCache(scenarios: Scenario[]): string[] {
    return [...new Set([...scenarios.map((v) => v.category)])].sort().filter((c) => c);
  }

  updateTagsCache(scenarios: Scenario[]): string[] {
    return [
      ...new Set(
        <string>[].concat.apply(
          [],
          scenarios.map((s: Scenario) => s.tags)
        )
      )
    ]
      .sort()
      .filter((t) => t);
  }

  postTickStory(tickStory) {
    return this.scenarioApiService.postTickStory(tickStory);
  }
}

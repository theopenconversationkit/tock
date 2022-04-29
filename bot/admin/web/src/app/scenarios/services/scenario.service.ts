/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, merge, Observable } from 'rxjs';
import { map, tap, switchMap, filter, mergeMap } from 'rxjs/operators';
import { Scenario } from '../models';
import { ScenarioApiService } from './scenario.api.service';

interface ScenarioState {
  loaded: boolean;
  loading: boolean;
  scenarios: Scenario[];
}

const scenariosInitialState: ScenarioState = {
  loaded: false,
  loading: false,
  scenarios: []
};

@Injectable()
export class ScenarioService {
  private _state: BehaviorSubject<ScenarioState>;
  state$: Observable<ScenarioState>;

  constructor(private scenarioApiService: ScenarioApiService, private httpClient: HttpClient) {
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

  setScenariosData(scenariosCollection): void {
    let state = this.getState();
    state = {
      ...state,
      loading: false,
      loaded: true,
      scenarios: scenariosCollection
    };
    this.setState(state);
  }

  getScenarios(): Observable<Array<Scenario>> {
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

  getScenario(id: number): Observable<Scenario> {
    return this.getScenarios().pipe(
      switchMap(() => this.state$),
      mergeMap((state) => state.scenarios),
      filter((scenario) => scenario.id === id)
    );
  }

  putScenario(id: number, scenario: Scenario): Observable<Scenario> {
    return this.scenarioApiService.putScenario(id, scenario).pipe(
      tap((modifiedScenario) => {
        let state = this.getState();
        const scenario = state.scenarios.find((s) => s.id === id);
        if (scenario) {
          const index = state.scenarios.indexOf(scenario);
          state.scenarios[index] = modifiedScenario;
          this.setState(state);
        }
      })
    );
  }

  deleteScenario(id: number): Observable<any> {
    return this.scenarioApiService.deleteScenario(id).pipe(
      tap(() => {
        let state = this.getState();
        state.scenarios = state.scenarios.filter((s) => s.id !== id);
        this.setState(state);
      })
    );
  }

  getScenariosTreeGrid(): Observable<Array<any>> {
    return this.getScenarios().pipe(map(this.buildTreeNodeByCategory));
  }

  // HELPERS

  buildTreeNodeByCategory(scenarios: Array<Scenario>): Array<any> {
    const scenariosByCatagory = new Map();

    scenarios.forEach((s) => {
      let category = scenariosByCatagory.get(s.category);

      if (!category) {
        category = [];
        scenariosByCatagory.set(s.category, category);
      }

      category.push(s);
    });

    scenariosByCatagory.forEach((t) => {
      t = t.sort((a: Scenario, b: Scenario) => {
        const firstScenarioName = a.name.toUpperCase();
        const secondScenarioName = b.name.toUpperCase();

        if (firstScenarioName < secondScenarioName) return -1;
        else if (firstScenarioName > secondScenarioName) return 1;
        else return 0;
      });
    });

    return Array.from(scenariosByCatagory, ([key, value]) => ({
      data: {
        category: key,
        expandable: true
      },
      children: value.map((v: Scenario) => {
        return {
          data: v
        };
      })
    }));
  }
}

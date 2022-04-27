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
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { RestService } from '../../core-nlp/rest/rest.service';
import { Scenario } from '../models';

@Injectable()
export class ScenarioService {
  private tmpBaseHref = 'http://localhost:3000';

  constructor(private rest: RestService, private httpClient: HttpClient) {}

  getScenario(id: number): Observable<Scenario> {
    return this.httpClient.get<Scenario>(`${this.tmpBaseHref}/scenarios/${id}`);
  }

  getScenarios(): Observable<Array<Scenario>> {
    return this.httpClient.get<Array<Scenario>>(`${this.tmpBaseHref}/scenarios`);
  }

  getScenariosTreeGrid(): Observable<Array<any>> {
    return this.getScenarios().pipe(map(this.buildTreeNodeByCategory));
  }

  deleteScenario(id: number): Observable<any> {
    return this.httpClient.delete(`${this.tmpBaseHref}/scenarios/${id}`);
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

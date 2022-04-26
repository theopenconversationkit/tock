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

import { RestService } from '../../core-nlp/rest/rest.service';
import { Scenario } from '../models';

@Injectable()
export class ScenarioService {
  private tmpBaseHref = 'http://localhost:3000';

  constructor(private rest: RestService, private httpClient: HttpClient) {}

  getScenarios(): Observable<Array<Scenario>> {
    return this.httpClient.get<Array<Scenario>>(`${this.tmpBaseHref}/scenarios`);
  }

  getScenario(id: number): Observable<Scenario> {
    return this.httpClient.get<Scenario>(`${this.tmpBaseHref}/scenarios/${id}`);
  }
}

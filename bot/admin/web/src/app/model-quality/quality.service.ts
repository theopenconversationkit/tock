/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

import { RestService } from '../core-nlp/rest/rest.service';
import { Injectable } from '@angular/core';
import {
  EntityTestError,
  EntityTestErrorQueryResult,
  IntentQA,
  IntentTestError,
  IntentTestErrorQueryResult,
  LogCountResult,
  LogStat,
  LogStatsQuery,
  TestBuildStat,
  TestErrorQuery
} from '../model/nlp';
import { Observable } from 'rxjs';

@Injectable()
export class QualityService {
  constructor(private rest: RestService) {}

  logStats(query: LogStatsQuery): Observable<LogStat[]> {
    return this.rest.post('/logs/stats', query, LogStat.fromJSONArray);
  }

  countStats(query: LogStatsQuery): Observable<LogCountResult> {
    return this.rest.post('/logs/count/stats', query, LogCountResult.fromJSON);
  }

  intentQA(query: LogStatsQuery): Observable<IntentQA[]> {
    return this.rest.post(`/logs/intent/stats`, query, IntentQA.fromJSONArray);
  }

  searchIntentErrors(query: TestErrorQuery): Observable<IntentTestErrorQueryResult> {
    return this.rest.post('/test/intent-errors', query, IntentTestErrorQueryResult.fromJSON);
  }

  searchIntentErrorsBlob(query: TestErrorQuery): Observable<Blob> {
    return this.rest.post('/test/intent-errors', query, (j) => new Blob([JSON.stringify(j)], { type: 'application/json' }));
  }

  deleteIntentError(error: IntentTestError): Observable<boolean> {
    return this.rest.post('/test/intent-error/delete', error);
  }

  searchEntityErrors(query: TestErrorQuery): Observable<EntityTestErrorQueryResult> {
    return this.rest.post('/test/entity-errors', query, EntityTestErrorQueryResult.fromJSON);
  }

  searchEntityErrorsBlob(query: TestErrorQuery): Observable<Blob> {
    return this.rest.post('/test/entity-errors', query, (j) => new Blob([JSON.stringify(j)], { type: 'application/json' }));
  }

  deleteEntityError(error: EntityTestError): Observable<boolean> {
    return this.rest.post('/test/entity-error/delete', error);
  }

  buildStats(query: TestErrorQuery): Observable<TestBuildStat[]> {
    return this.rest.post('/test/stats', query, TestBuildStat.fromJSONArray);
  }
}

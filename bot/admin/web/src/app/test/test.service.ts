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

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RestService } from '../core-nlp/rest/rest.service';
import { StateService } from '../core-nlp/state.service';
import {
  BotDialogRequest,
  BotDialogResponse,
  TestPlan,
  TestPlanExecution,
  XRayPlanExecutionConfiguration,
  XRayPlanExecutionResult,
  XRayTestPlan
} from './model/test';
import { Observable, Subject } from 'rxjs';

export interface SSEEvent {
  event?: string;
  data?: string;
}

@Injectable()
export class TestService {
  constructor(private rest: RestService, private state: StateService, private http: HttpClient) {}

  talk(query: BotDialogRequest, debug = false, sourceWithContent = false): Observable<BotDialogResponse> {
    let params: { debug?: boolean; sourceWithContent?: boolean } = {};
    if (debug) params.debug = true;
    if (sourceWithContent) params.sourceWithContent = true;
    return this.rest.post('/test/talk', query, BotDialogResponse.fromJSON, null, false, params);
  }

  talkStream(query: BotDialogRequest, debug = false, sourceWithContent = false): Observable<SSEEvent> {
    return new Observable(observer => {
      let params = new URLSearchParams();
      if (debug) params.set('debug', 'true');
      if (sourceWithContent) params.set('sourceWithContent', 'true');

      const url = this.rest.url + '/test/talk/stream' + (params.toString() ? '?' + params : '');
      const postData = JSON.stringify(query);

      const xhr = new XMLHttpRequest();
      xhr.open('POST', url);
      xhr.setRequestHeader('Content-Type', 'application/json');
      xhr.withCredentials = true;

      // [SSE-DEBUG] timestamped tracing of stream reception in the browser.
      // Remove once streaming fluidity is confirmed.
      const t0 = performance.now();
      const ts = () => `+${(performance.now() - t0).toFixed(0)}ms`;
      console.debug(`[SSE-DEBUG] ${ts()} request sent`);

      let lastProcessedIndex = 0;

      xhr.onreadystatechange = () => {
        if (xhr.readyState >= 3 && xhr.responseText.length > lastProcessedIndex) {
          console.debug(
            `[SSE-DEBUG] ${ts()} onreadystatechange readyState=${xhr.readyState} ` +
              `+${xhr.responseText.length - lastProcessedIndex} chars (total=${xhr.responseText.length})`
          );
          const newData = xhr.responseText.substring(lastProcessedIndex);
          const lines = newData.split('\n');

          for (let i = 0; i < lines.length - 1; i++) {
            const line = lines[i];
            if (line.startsWith('event:')) {
              const eventType = line.substring(6).trim();
              const nextLineIndex = i + 1;
              if (nextLineIndex < lines.length) {
                const dataLine = lines[nextLineIndex];
                if (dataLine.startsWith('data:')) {
                  const eventData = dataLine.substring(5).trim();
                  console.debug(`[SSE-DEBUG] ${ts()} emit event=${eventType} data=${eventData.substring(0, 60)}`);
                  observer.next({ event: eventType, data: eventData });
                }
              }
            }
          }

          lastProcessedIndex = xhr.responseText.length;
        }

        if (xhr.readyState === 4) {
          if (xhr.status === 200) {
            observer.complete();
          } else {
            observer.error(new Error(`HTTP ${xhr.status}: ${xhr.statusText}`));
          }
        }
      };

      xhr.onerror = () => {
        observer.error(new Error('Network error'));
      };

      xhr.send(postData);

      return () => {
        xhr.abort();
      };
    });
  }

  getTestPlans(): Observable<TestPlan[]> {
    return this.rest.get('/test/plans/', TestPlan.fromJSONArray);
  }

  getTestPlan(testPlanId: string): Observable<TestPlan> {
    return this.rest.get(`/test/plan/${testPlanId}`, TestPlan.fromJSON);
  }

  getTestPlanExecutions(planId: string): Observable<TestPlanExecution[]> {
    return this.rest.get(`/test/plan/${planId}/executions`, TestPlanExecution.fromJSONArray);
  }

  getTestPlanExecutionStatus(planId: string, executionId: string): Observable<TestPlanExecution> {
    return this.rest.get(`/test/plan/${planId}/executions/${executionId}`, TestPlanExecution.fromJSON);
  }

  saveTestPlan(plan: TestPlan): Observable<boolean> {
    return this.rest.post(`/test/plan`, plan);
  }

  // REST service called when "Launch" button is pressed
  runTestPlan(planId: string): Observable<string> {
    return this.rest.post(`/test/plan/${planId}/run`, this.state.createApplicationScopedQuery());
  }

  removeTestPlan(planId: string): Observable<boolean> {
    return this.rest.delete(`/test/plan/${planId}`);
  }

  removeDialogFromTestPlan(planId: string, dialogId: string): Observable<boolean> {
    return this.rest.post(`/test/plan/${planId}/dialog/delete/${dialogId}`, this.state.createApplicationScopedQuery());
  }

  executeXRay(conf: XRayPlanExecutionConfiguration): Observable<XRayPlanExecutionResult> {
    return this.rest.post(`/xray/execute`, conf, XRayPlanExecutionResult.fromJSON);
  }

  getXrayTestPlans(): Observable<XRayTestPlan[]> {
    return this.rest.get(`/xray/test/plans`, XRayTestPlan.fromJSONArray);
  }
}

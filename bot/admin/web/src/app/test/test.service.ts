/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Injectable} from "@angular/core";
import {RestService} from "tock-nlp-admin/src/app/core/rest/rest.service";
import {StateService} from "tock-nlp-admin/src/app/core/state.service";
import {Observable} from "rxjs/Observable";
import {
  BotDialogRequest,
  BotDialogResponse,
  TestPlan,
  TestPlanExecution
} from "./model/test";

@Injectable()
export class TestService {

  constructor(private rest: RestService,
              private state: StateService) {
  }

  talk(query: BotDialogRequest): Observable<BotDialogResponse> {
    return this.rest.post("/test/talk", query, BotDialogResponse.fromJSON);
  }

  getTestPlans(): Observable<TestPlan[]> {
    return this.rest.get("/test/plans", TestPlan.fromJSONArray);
  }

  getTestPlanExecutions(planId: string): Observable<TestPlanExecution[]> {
    return this.rest.get(`/test/plan/${planId}/executions`, TestPlanExecution.fromJSONArray);
  }

  saveTestPlan(plan: TestPlan): Observable<boolean> {
    return this.rest.post(`/test/plan`, plan);
  }

  runTestPlan(planId: string): Observable<TestPlanExecution> {
    return this.rest.post(`/test/plan/${planId}/run`, this.state.createApplicationScopedQuery(), TestPlanExecution.fromJSON);
  }

  removeTestPlan(planId: string): Observable<boolean> {
    return this.rest.delete(`/test/plan/${planId}`);
  }

  removeDialogFromTestPlan(planId: string, dialogId: string): Observable<boolean> {
    return this.rest.post(`/test/plan/${planId}/dialog/delete/${dialogId}`, this.state.createApplicationScopedQuery());
  }

}

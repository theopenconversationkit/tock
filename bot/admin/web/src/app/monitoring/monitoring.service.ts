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

import {Injectable, OnDestroy} from "@angular/core";
import {RestService} from "tock-nlp-admin/src/app/core/rest/rest.service";
import {StateService} from "tock-nlp-admin/src/app/core/state.service";
import {UserReportQueryResult, UserSearchQuery} from "./model/users";
import {Observable} from "rxjs/Observable";
import {DialogReportQuery, DialogReportQueryResult} from "./model/dialogs";
import {TestPlan} from "../test/model/test";

@Injectable()
export class MonitoringService implements OnDestroy {


  constructor(private rest: RestService,
              private state: StateService) {
  }

  ngOnDestroy(): void {
  }

  users(query: UserSearchQuery): Observable<UserReportQueryResult> {
    return this.rest.post("/users/search", query, UserReportQueryResult.fromJSON);
  }

  dialogs(query: DialogReportQuery): Observable<DialogReportQueryResult> {
    return this.rest.post("/dialogs/search", query, DialogReportQueryResult.fromJSON);
  }

  getTestPlansByNamespaceAndNlpModel(): Observable<TestPlan[]> {
    return this.rest.post(`/application/plans`, this.state.createApplicationScopedQuery(), TestPlan.fromJSONArray);
  }

  addDialogToTestPlan(planId: string, dialogId: string): Observable<boolean> {
    return this.rest.post(`/test/plan/${planId}/dialog/${dialogId}`, this.state.createApplicationScopedQuery());
  }

}

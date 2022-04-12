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

import { Injectable, OnDestroy } from '@angular/core';
import { RestService } from '../core-nlp/rest/rest.service';
import { StateService } from '../core-nlp/state.service';
import { UserAnalyticsQueryResult, UserReportQueryResult, UserSearchQuery } from './users/users';
import { Observable } from 'rxjs';
import { DialogReportQuery, DialogReportQueryResult } from './dialogs/dialogs';
import { TestPlan } from '../test/model/test';
import { DialogReport } from '../shared/model/dialog-data';
import { ApplicationDialogFlow, DialogFlowRequest } from './flow/flow';
import { UserAnalyticsPreferences } from './preferences/UserAnalyticsPreferences';

@Injectable()
export class AnalyticsService implements OnDestroy {
  userAnalyticsSettings: string;

  constructor(private rest: RestService, private state: StateService) {
    this.userAnalyticsSettings = localStorage.getItem('_tock_analytics_settings');
  }

  ngOnDestroy(): void {}

  users(query: UserSearchQuery): Observable<UserReportQueryResult> {
    return this.rest.post('/users/search', query, UserReportQueryResult.fromJSON);
  }

  usersAnalytics(query: DialogFlowRequest): Observable<UserAnalyticsQueryResult> {
    return this.rest.post('/analytics/users', query, UserAnalyticsQueryResult.fromJSON);
  }

  messagesAnalytics(request: DialogFlowRequest): Observable<UserAnalyticsQueryResult> {
    return this.rest.post('/analytics/messages', request, UserAnalyticsQueryResult.fromJSON);
  }

  messagesAnalyticsByConfiguration(
    request: DialogFlowRequest
  ): Observable<UserAnalyticsQueryResult> {
    return this.rest.post(
      '/analytics/messages/byConfiguration',
      request,
      UserAnalyticsQueryResult.fromJSON
    );
  }

  messagesAnalyticsByConnectorType(
    request: DialogFlowRequest
  ): Observable<UserAnalyticsQueryResult> {
    return this.rest.post(
      '/analytics/messages/byConnectorType',
      request,
      UserAnalyticsQueryResult.fromJSON
    );
  }

  messagesAnalyticsByDayOfWeek(request: DialogFlowRequest): Observable<UserAnalyticsQueryResult> {
    return this.rest.post(
      '/analytics/messages/byDayOfWeek',
      request,
      UserAnalyticsQueryResult.fromJSON
    );
  }

  messagesAnalyticsByHour(request: DialogFlowRequest): Observable<UserAnalyticsQueryResult> {
    return this.rest.post('/analytics/messages/byHour', request, UserAnalyticsQueryResult.fromJSON);
  }

  messagesAnalyticsByDateAndIntent(
    request: DialogFlowRequest
  ): Observable<UserAnalyticsQueryResult> {
    return this.rest.post(
      '/analytics/messages/byDateAndIntent',
      request,
      UserAnalyticsQueryResult.fromJSON
    );
  }

  messagesAnalyticsByIntent(request: DialogFlowRequest): Observable<UserAnalyticsQueryResult> {
    return this.rest.post(
      '/analytics/messages/byIntent',
      request,
      UserAnalyticsQueryResult.fromJSON
    );
  }

  messagesAnalyticsByDateAndStory(
    request: DialogFlowRequest
  ): Observable<UserAnalyticsQueryResult> {
    return this.rest.post(
      '/analytics/messages/byDateAndStory',
      request,
      UserAnalyticsQueryResult.fromJSON
    );
  }

  messagesAnalyticsByStory(request: DialogFlowRequest): Observable<UserAnalyticsQueryResult> {
    return this.rest.post(
      '/analytics/messages/byStory',
      request,
      UserAnalyticsQueryResult.fromJSON
    );
  }

  messagesAnalyticsByStoryCategory(
    request: DialogFlowRequest
  ): Observable<UserAnalyticsQueryResult> {
    return this.rest.post(
      '/analytics/messages/byStoryCategory',
      request,
      UserAnalyticsQueryResult.fromJSON
    );
  }

  messagesAnalyticsByStoryType(request: DialogFlowRequest): Observable<UserAnalyticsQueryResult> {
    return this.rest.post(
      '/analytics/messages/byStoryType',
      request,
      UserAnalyticsQueryResult.fromJSON
    );
  }

  messagesAnalyticsByStoryLocale(request: DialogFlowRequest): Observable<UserAnalyticsQueryResult> {
    return this.rest.post(
      '/analytics/messages/byStoryLocale',
      request,
      UserAnalyticsQueryResult.fromJSON
    );
  }

  messagesAnalyticsByActionType(request: DialogFlowRequest): Observable<UserAnalyticsQueryResult> {
    return this.rest.post(
      '/analytics/messages/byActionType',
      request,
      UserAnalyticsQueryResult.fromJSON
    );
  }

  dialogs(query: DialogReportQuery): Observable<DialogReportQueryResult> {
    return this.rest.post('/dialogs/search', query, DialogReportQueryResult.fromJSON);
  }

  dialog(applicationId: string, dialogId: string): Observable<DialogReport> {
    return this.rest.get(`/dialog/${applicationId}/${dialogId}`, DialogReport.fromJSON);
  }

  getTestPlansByNamespaceAndNlpModel(): Observable<TestPlan[]> {
    return this.rest.post(
      `/application/plans`,
      this.state.createApplicationScopedQuery(),
      TestPlan.fromJSONArray
    );
  }

  addDialogToTestPlan(planId: string, dialogId: string): Observable<boolean> {
    return this.rest.post(
      `/test/plan/${planId}/dialog/${dialogId}`,
      this.state.createApplicationScopedQuery()
    );
  }

  getApplicationFlow(request: DialogFlowRequest): Observable<ApplicationDialogFlow> {
    return this.rest.post(`/flow`, request, ApplicationDialogFlow.fromJSON);
  }

  onUserAnalyticsSettingsChange(settings: string) {
    this.userAnalyticsSettings = settings;
    localStorage.setItem('_tock_analytics_settings', this.userAnalyticsSettings);
  }

  getUserPreferences(): UserAnalyticsPreferences {
    if (this.userAnalyticsSettings != null) {
      return JSON.parse(this.userAnalyticsSettings);
    } else {
      return UserAnalyticsPreferences.defaultConfiguration();
    }
  }
}

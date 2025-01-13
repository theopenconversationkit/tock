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

import { Component } from '@angular/core';
import { AnalyticsService } from '../analytics.service';
import { UserReport, UserSearchQuery } from './users';
import { StateService } from '../../core-nlp/state.service';
import { DialogReportQuery } from '../dialogs/dialogs';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { ActionReport, DialogReport } from '../../shared/model/dialog-data';
import { ScrollComponent } from '../../scroll/scroll.component';
import { Observable } from 'rxjs';
import { PaginatedResult } from '../../model/nlp';
import { PaginatedQuery } from '../../model/commons';
import { NbToastrService } from '@nebular/theme';
import { BotApplicationConfiguration, ConnectorType } from '../../core/model/configuration';
import { getDialogMessageUserAvatar, getDialogMessageUserQualifier } from '../../shared/utils';

export class UserFilter {
  constructor(public flags: string[], public displayTests: boolean, public from?: Date, public to?: Date, public intent: string = '') {}
}

@Component({
  selector: 'tock-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss']
})
export class UsersComponent extends ScrollComponent<UserReport> {
  filter: UserFilter = new UserFilter([], false);
  selectedUser: UserReport;
  selectedPlanId: string;
  configurations: BotApplicationConfiguration[];

  loadingDialog: boolean = false;

  constructor(
    state: StateService,
    private analytics: AnalyticsService,
    private botConfiguration: BotConfigurationService,
    private toastrService: NbToastrService
  ) {
    super(state);
    this.botConfiguration.configurations.subscribe((configs) => {
      this.configurations = configs;
      this.refresh();
    });
  }

  reload() {
    this.selectedUser = null;
    this.refresh();
  }

  protected override loadResults(result: PaginatedResult<UserReport>, init: boolean): boolean {
    if (super.loadResults(result, init)) {
      if (this.data.length !== 0 && this.selectedUser == null) {
        this.selectedUser = this.data[0];
        this.loadDialog(this.selectedUser);
      }
      return true;
    } else {
      return false;
    }
  }

  containsFlag(flag: string): boolean {
    return this.filter.flags.indexOf(flag) !== -1;
  }

  getConnector(applicationId: string): ConnectorType {
    let connectors = this.configurations.filter((config) => config.applicationId === applicationId).map((config) => config.connectorType);
    return connectors && connectors.length > 0 ? connectors[0] : null;
  }

  toggleFlag(flag: string) {
    if (!this.containsFlag(flag)) {
      this.filter.flags.push(flag);
      this.refresh();
    } else {
      this.removeFlag(flag);
    }
  }

  removeFlag(flag: string) {
    const i = this.filter.flags.indexOf(flag);
    if (i !== -1) {
      this.filter.flags.splice(i, 1);
      this.refresh();
    }
  }

  changeBefore() {
    this.waitAndRefresh();
  }

  changeAfter() {
    this.waitAndRefresh();
  }

  waitAndRefresh() {
    setTimeout((_) => this.reload());
  }

  override search(query: PaginatedQuery): Observable<PaginatedResult<UserReport>> {
    return this.analytics.users(this.buildUserSearchQuery(query));
  }

  override dataEquals(d1: UserReport, d2: UserReport): boolean {
    return d1.playerId === d2.playerId;
  }

  private buildUserSearchQuery(query: PaginatedQuery): UserSearchQuery {
    return new UserSearchQuery(
      query.namespace,
      query.applicationName,
      query.language,
      query.start,
      query.size,
      null,
      this.filter.from,
      this.filter.to,
      this.filter.flags,
      this.filter.displayTests
    );
  }

  private buildDialogQuery(user: UserReport): DialogReportQuery {
    const app = this.state.currentApplication;
    const language = this.state.currentLocale;
    return new DialogReportQuery(app.namespace, app.name, language, 0, 1, true, user.playerId);
  }

  loadDialog(user: UserReport) {
    this.loadingDialog = true;
    this.analytics.dialogs(this.buildDialogQuery(user)).subscribe((r) => {
      if (r.rows.length != 0) {
        // we store nlpStats related to the action as an expando of the action itself
        r.rows[0].actions?.forEach((action) => {
          let actionNlpStats = r.nlpStats.find((ns) => ns.actionId === action.id);
          if (actionNlpStats) action._nlpStats = actionNlpStats.stats;
        });

        user.userDialog = r.rows[0];
        this.analytics.getTestPlansByNamespaceAndNlpModel().subscribe((r) => (user.testPlans = r));
      }
      user.displayDialogs = true;
      this.loadingDialog = false;
      this.selectedUser = user;
    });
  }

  addDialogToTestPlan(planId: string, dialog: DialogReport) {
    if (!planId) {
      this.toastrService.show(`Please select a Plan first`, 'Error', { duration: 3000 });
      return;
    }
    this.analytics
      .addDialogToTestPlan(planId, dialog.id)
      .subscribe((_) => this.toastrService.show(`Dialog added to plan`, 'Dialog Added', { duration: 3000 }));
  }

  getUserName(action: ActionReport): string {
    return getDialogMessageUserQualifier(action.isBot());
  }

  getUserAvatar(action: ActionReport): string {
    return getDialogMessageUserAvatar(action.isBot());
  }

  getUserPicture(user: UserReport): string {
    return user.userPreferences.picture ? user.userPreferences.picture : getDialogMessageUserAvatar(false);
  }
}

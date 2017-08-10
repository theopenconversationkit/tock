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

import {Component} from "@angular/core";
import {MonitoringService} from "../monitoring.service";
import {UserReport, UserSearchQuery} from "../model/users";
import {StateService} from "tock-nlp-admin/src/app/core/state.service";
import {DialogReportQuery} from "../model/dialogs";
import {BotConfigurationService} from "../../core/bot-configuration.service";
import {MdSnackBar} from "@angular/material";
import {DialogReport} from "../../shared/model/dialog-data";
import {ScrollComponent} from "tock-nlp-admin/src/app/scroll/scroll.component";
import {Observable} from "rxjs/Observable";
import {PaginatedResult} from "tock-nlp-admin/src/app/model/nlp";
import {PaginatedQuery} from "tock-nlp-admin/src/app/model/commons";

@Component({
  selector: 'tock-user-timelines',
  templateUrl: './user-timelines.component.html',
  styleUrls: ['./user-timelines.component.css']
})
export class UserTimelinesComponent extends ScrollComponent<UserReport> {

  filter: UserFilter = new UserFilter();


  constructor(state: StateService,
              private monitoring: MonitoringService,
              private botConfiguration: BotConfigurationService,
              private snackBar: MdSnackBar) {
    super(state);
    this.botConfiguration.configurations.subscribe(_ => this.refresh());
  }


  search(query: PaginatedQuery): Observable<PaginatedResult<UserReport>> {
    return this.monitoring.users(this.buildUserSearchQuery(query));
  }

  dataEquals(d1: UserReport, d2: UserReport): boolean {
    return d1.playerId === d2.playerId;
  }

  private buildUserSearchQuery(query: PaginatedQuery): UserSearchQuery {
    return new UserSearchQuery(
      query.namespace,
      query.applicationName,
      query.language,
      query.start,
      query.size,
      this.filter.name,
      this.filter.from,
      this.filter.to);
  }


  private buildDialogQuery(user: UserReport): DialogReportQuery {
    const app = this.state.currentApplication;
    const language = this.state.currentLocale;
    return new DialogReportQuery(
      app.namespace,
      app.name,
      language,
      0,
      1,
      true,
      user.playerId);
  }

  closeDialog(user: UserReport) {
    user.displayDialogs = false;
  }

  loadDialog(user: UserReport) {
    this.monitoring.dialogs(this.buildDialogQuery(user)).subscribe(r => {
      if(r.rows.length != 0) {
        user.userDialog = r.rows[0];
        this.monitoring.getTestPlansByNamespaceAndNlpModel().subscribe(r => user.testPlans = r);
      }
      user.displayDialogs = true;
    });
  }

  addDialogToTestPlan(planId: string, dialog: DialogReport) {
    if (!planId) {
      this.snackBar.open(`Please select a Plan first`, "Error", {duration: 3000});
      return;
    }
    this.monitoring.addDialogToTestPlan(planId, dialog.id)
      .subscribe(_ => this.snackBar.open(`Dialog added to plan`, "Dialog Added", {duration: 3000}));
  }

}

export class UserFilter {
  constructor(public name?: string,
              public from?: Date,
              public to?: Date) {
  }
}

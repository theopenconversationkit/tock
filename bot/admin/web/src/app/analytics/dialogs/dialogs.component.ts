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

import { mergeMap } from 'rxjs/operators';
import { Component } from '@angular/core';
import { ScrollComponent } from '../../scroll/scroll.component';
import { Observable } from 'rxjs';
import { PaginatedResult } from '../../model/nlp';
import { PaginatedQuery } from '../../model/commons';
import { DialogReport } from '../../shared/model/dialog-data';
import { AnalyticsService } from '../analytics.service';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { StateService } from '../../core-nlp/state.service';
import { DialogReportQuery } from './dialogs';
import { ActivatedRoute } from '@angular/router';
import { ConnectorType } from '../../core/model/configuration';
import { BotSharedService } from '../../shared/bot-shared.service';
import { NbToastrService } from '@nebular/theme';

@Component({
  selector: 'tock-dialogs',
  templateUrl: './dialogs.component.html',
  styleUrls: ['./dialogs.component.css']
})
export class DialogsComponent extends ScrollComponent<DialogReport> {
  filter: DialogFilter = new DialogFilter(true, false);
  state: StateService;
  connectorTypes: ConnectorType[] = [];
  private loaded = false;

  constructor(
    state: StateService,
    private analytics: AnalyticsService,
    private botConfiguration: BotConfigurationService,
    private toastrService: NbToastrService,
    private route: ActivatedRoute,
    public botSharedService: BotSharedService
  ) {
    super(state);
    this.state = state;
    this.botConfiguration.configurations.subscribe((_) => this.refresh());
    this.botSharedService.getConnectorTypes().subscribe((confConf) => {
      this.connectorTypes = confConf.map((it) => it.connectorType);
    });
  }

  waitAndRefresh() {
    setTimeout((_) => this.refresh());
  }

  search(query: PaginatedQuery): Observable<PaginatedResult<DialogReport>> {
    return this.route.queryParams.pipe(
      mergeMap((params) => {
        if (!this.loaded) {
          if (params['dialogId']) this.filter.dialogId = params['dialogId'];
          if (params['text']) this.filter.text = params['text'];
          if (params['intentName']) this.filter.intentName = params['intentName'];
          this.loaded = true;
        }
        return this.analytics.dialogs(this.buildDialogQuery(query));
      })
    );
  }

  dataEquals(d1: DialogReport, d2: DialogReport): boolean {
    return d1.id === d2.id;
  }

  viewAllWithThisText() {
    this.filter.dialogId = null;
    this.refresh();
  }

  private buildDialogQuery(query: PaginatedQuery): DialogReportQuery {
    return new DialogReportQuery(
      query.namespace,
      query.applicationName,
      query.language,
      query.start,
      query.size,
      this.filter.exactMatch,
      null,
      this.filter.dialogId,
      this.filter.text,
      this.filter.intentName,
      this.filter.connectorType,
      this.filter.displayTests
    );
  }

  addDialogToTestPlan(planId: string, dialog: DialogReport) {
    if (!planId) {
      this.toastrService.show(`Please select a Plan first`, 'Error', { duration: 3000 });
      return;
    }
    this.analytics
      .addDialogToTestPlan(planId, dialog.id)
      .subscribe((_) =>
        this.toastrService.show(`Dialog added to plan`, 'Dialog Added', { duration: 3000 })
      );
  }
}

export class DialogFilter {
  constructor(
    public exactMatch: boolean,
    public displayTests: boolean,
    public dialogId?: string,
    public text?: string,
    public intentName?: string,
    public connectorType?: ConnectorType
  ) {}
}

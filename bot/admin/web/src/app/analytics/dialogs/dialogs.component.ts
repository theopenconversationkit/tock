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

import { mergeMap, filter } from 'rxjs/operators';
import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { ScrollComponent } from '../../scroll/scroll.component';
import { Observable } from 'rxjs';
import { PaginatedResult } from '../../model/nlp';
import { PaginatedQuery } from '../../model/commons';
import { DialogReport } from '../../shared/model/dialog-data';
import { AnalyticsService } from '../analytics.service';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { StateService } from '../../core-nlp/state.service';
import { DialogReportQuery } from './dialogs';
import { ActivatedRoute, UrlSegment } from '@angular/router';
import { ConnectorType } from '../../core/model/configuration';
import { BotSharedService } from '../../shared/bot-shared.service';
import { NbToastrService } from '@nebular/theme';
import { saveAs } from 'file-saver-es';

@Component({
  selector: 'tock-dialogs',
  templateUrl: './dialogs.component.html',
  styleUrls: ['./dialogs.component.css']
})
export class DialogsComponent extends ScrollComponent<DialogReport> implements OnChanges {
  filter: DialogFilter = new DialogFilter(true, false);
  state: StateService;
  connectorTypes: ConnectorType[] = [];
  configurationNameList: string[];
  private loaded = false;
  @Input() ratingFilter: number[];

  intents: string[];

  dialogReportQuery: DialogReportQuery;

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

    this.botConfiguration.configurations.subscribe((configs) => {
      this.isSatisfactionRoute().subscribe((res) => {
        this.botSharedService.getIntentsByApplication(this.state.currentApplication._id).subscribe((intents) => (this.intents = intents));

        this.configurationNameList = configs.filter((item) => item.targetConfigurationId == null).map((item) => item.applicationId);
        if (res) {
          this.ratingFilter = [1, 2, 3, 4, 5];
        }
        this.refresh();
      });
    });
    this.botSharedService.getConnectorTypes().subscribe((confConf) => {
      this.connectorTypes = confConf.map((it) => it.connectorType);
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['ratingFilter'].currentValue != changes['ratingFilter'].previousValue) {
      this.refresh();
    }
  }
  waitAndRefresh() {
    setTimeout((_) => this.refresh());
  }

  search(query: PaginatedQuery): Observable<PaginatedResult<DialogReport>> {
    this.buildDialogQuery(query);
    return this.route.queryParams.pipe(
      mergeMap((params) => {
        if (!this.loaded) {
          if (params['dialogId']) this.filter.dialogId = params['dialogId'];
          if (params['text']) this.filter.text = params['text'];
          if (params['intentName']) this.filter.intentName = params['intentName'];
          this.loaded = true;
        }
        return this.analytics.dialogs(this.dialogReportQuery);
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

  private buildDialogQuery(query: PaginatedQuery) {
    this.dialogReportQuery = new DialogReportQuery(
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
      this.filter.displayTests,
      this.ratingFilter,
      this.filter.configuration,
      this.filter.intentsToHide
    );
  }

  isSatisfactionRoute() {
    return this.route.url.pipe(
      filter((val: UrlSegment[]) => {
        return val[0].path == 'satisfaction';
      })
    );
  }

  exportDialogs() {
    this.analytics.downloadDialogsCsv(this.dialogReportQuery).subscribe((blob) => {
      saveAs(blob, 'dialogs_with_rating.csv');
    });
    this.analytics.downloadDialogsWithIntentsCsv(this.dialogReportQuery).subscribe((blob) => {
      saveAs(blob, 'dialogs_with_rating_and_intents.csv');
    });
  }
}

export class DialogFilter {
  constructor(
    public exactMatch: boolean,
    public displayTests: boolean,
    public dialogId?: string,
    public text?: string,
    public intentName?: string,
    public connectorType?: ConnectorType,
    public ratings?: number[],
    public configuration?: string,

    public intentsToHide?: string[]
  ) {}
}

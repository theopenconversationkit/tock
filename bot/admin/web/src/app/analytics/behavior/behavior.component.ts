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

import { AfterViewInit, Component } from '@angular/core';
// import * as html2pdf from 'html2pdf.js';
import { StateService } from 'src/app/core-nlp/state.service';
import { BotConfigurationService } from 'src/app/core/bot-configuration.service';
import { BotApplicationConfiguration, ConnectorType } from 'src/app/core/model/configuration';

import { AnalyticsService } from '../analytics.service';
import { DialogFlowRequest } from '../flow/flow';
import { UserAnalyticsPreferences } from '../preferences/UserAnalyticsPreferences';
import { UserAnalyticsQueryResult } from '../users/users';
import { UserFilter } from '../users/users.component';
import { toISOStringWithoutOffset } from '../../shared/utils';
import { SelectBotEvent } from '../../shared/components';

@Component({
  selector: 'tock-behavior',
  templateUrl: './behavior.component.html',
  styleUrls: ['./behavior.component.css']
})
export class BehaviorComponent implements AfterViewInit {
  startDate: Date;
  endDate: Date;
  selectedConnectorId: string;
  selectedConfigurationName: string;
  displayTests = false;

  filter: UserFilter = new UserFilter([], false);
  loading = false;

  messagesByStoryData: UserAnalyticsQueryResult;
  messagesByStoryTypeData: UserAnalyticsQueryResult;
  messagesByStoryCategoryData: UserAnalyticsQueryResult;
  messagesByStoryLocaleData: UserAnalyticsQueryResult;
  messagesByIntentData: UserAnalyticsQueryResult;
  messagesByDayOfWeekData: UserAnalyticsQueryResult;
  messagesByHourData: UserAnalyticsQueryResult;
  messagesByActionTypeData: UserAnalyticsQueryResult;

  messagesByStoryLoading: boolean = false;
  messagesByStoryTypeLoading: boolean = false;
  messagesByStoryCategoryLoading: boolean = false;
  messagesByStoryLocaleLoading: boolean = false;
  messagesByIntentLoading: boolean = false;
  messagesByDayOfWeekLoading: boolean = false;
  messagesByHourLoading: boolean = false;
  messagesByActionTypeLoading: boolean = false;

  configurations: BotApplicationConfiguration[];
  userPreferences: UserAnalyticsPreferences;

  constructor(private state: StateService, private analytics: AnalyticsService, private botConfiguration: BotConfigurationService) {
    this.botConfiguration.configurations.subscribe((configs) => {
      this.configurations = configs;
    });
    this.userPreferences = this.analytics.getUserPreferences();
  }

  ngAfterViewInit() {
    this.search();
  }

  search() {
    this.buildMessagesByStoryCharts();
    this.buildMessagesByIntentCharts();
    this.buildMessagesByDayOfWeekCharts();
    this.buildMessagesByHourCharts();
    this.buildMessagesByActionTypeCharts();
    this.buildMessagesByStoryCategoryCharts();
    this.buildMessagesByStoryTypeCharts();
    this.buildMessagesByStoryLocaleCharts();
  }

  getConnectorColor(connector: string): string {
    let color;
    switch (connector) {
      case 'messenger': {
        color = '#0084ff';
        break;
      }
      case 'ga': {
        color = '#fabc05';
        break;
      }
      case 'alexa': {
        color = '#3dc3ef';
        break;
      }
      case 'slack': {
        color = '#e01f5c';
        break;
      }
      case 'rocket': {
        color = '#dc2727';
        break;
      }
      case 'twitter': {
        color = '#1ca3f3';
        break;
      }
      case 'whatsapp': {
        color = '#41c352';
        break;
      }
      case 'teams': {
        color = '#5d67cf';
        break;
      }
      case 'businesschat': {
        color = '#58e951';
        break;
      }
      case 'web': {
        color = '#878f9c';
        break;
      }
      default: {
        color = '#f3745d';
        break;
      }
    }
    return color;
  }

  getFileName(): string {
    let fileName = 'Export-' + this.startDate.toLocaleDateString();
    if (this.endDate != null) {
      fileName += '-' + this.endDate.toLocaleDateString();
    }
    fileName += '.pdf';
    return fileName;
  }

  // onPdfAction() {
  //   const options = {
  //     filename: this.getFileName(),
  //     image: { type: 'jpeg ', quality: 0.95 },
  //     html2canvas: {},
  //     jsPDF: { orientation: 'landscape' }
  //   };
  //   const content: Element = document.getElementById('element-id');
  //   html2pdf().from(content).set(options).save();
  // }

  getConnector(connectorId: string): ConnectorType {
    let connectors = this.configurations.filter((config) => config.connectorType.id === connectorId).map((config) => config.connectorType);
    return connectors && connectors.length > 0 ? connectors[0] : null;
  }

  private reload(force?: boolean) {
    if (this.startDate != null && !this.loading) {
      this.filter.from = this.startDate;
      this.filter.to = this.endDate;
      this.search();
    }
  }

  private buildMessagesByStoryCharts() {
    if (!this.userPreferences.graphs.behavior.messagesByStory) return;
    this.messagesByStoryLoading = true;
    this.analytics.messagesAnalyticsByStory(this.buildMessagesSearchQuery()).subscribe((result) => {
      this.messagesByStoryData = result;
      this.messagesByStoryLoading = false;
    });
  }

  private buildMessagesByStoryTypeCharts() {
    if (!this.userPreferences.graphs.behavior.messagesByStoryType) return;
    this.messagesByStoryTypeLoading = true;
    this.analytics.messagesAnalyticsByStoryType(this.buildMessagesSearchQuery()).subscribe((result) => {
      this.messagesByStoryTypeData = result;
      this.messagesByStoryTypeLoading = false;
    });
  }

  private buildMessagesByStoryCategoryCharts() {
    if (!this.userPreferences.graphs.behavior.messagesByStoryCategory) return;
    this.messagesByStoryCategoryLoading = true;
    this.analytics.messagesAnalyticsByStoryCategory(this.buildMessagesSearchQuery()).subscribe((result) => {
      this.messagesByStoryCategoryData = result;
      this.messagesByStoryCategoryLoading = false;
    });
  }

  private buildMessagesByStoryLocaleCharts() {
    if (!this.userPreferences.graphs.behavior.messagesByLocale) return;
    this.messagesByStoryLocaleLoading = true;
    this.analytics.messagesAnalyticsByStoryLocale(this.buildMessagesSearchQuery()).subscribe((result) => {
      this.messagesByStoryLocaleData = result;
      this.messagesByStoryLocaleLoading = false;
    });
  }

  private buildMessagesByIntentCharts() {
    if (!this.userPreferences.graphs.behavior.messagesByIntent) return;
    this.messagesByIntentLoading = true;
    this.analytics.messagesAnalyticsByIntent(this.buildMessagesSearchQuery()).subscribe((result) => {
      this.messagesByIntentData = result;
      this.messagesByIntentLoading = false;
    });
  }

  private buildMessagesByDayOfWeekCharts() {
    if (!this.userPreferences.graphs.behavior.messagesByDayOfWeek) return;
    this.messagesByDayOfWeekLoading = true;
    this.analytics.messagesAnalyticsByDayOfWeek(this.buildMessagesSearchQuery()).subscribe((result) => {
      this.messagesByDayOfWeekData = result;
      this.messagesByDayOfWeekLoading = false;
    });
  }

  private buildMessagesByHourCharts() {
    if (!this.userPreferences.graphs.behavior.messagesByHourOfDay) return;
    this.messagesByHourLoading = true;
    this.analytics.messagesAnalyticsByHour(this.buildMessagesSearchQuery()).subscribe((result) => {
      this.messagesByHourData = result;
      this.messagesByHourLoading = false;
    });
  }

  private buildMessagesByActionTypeCharts() {
    if (!this.userPreferences.graphs.behavior.messagesByActionType) return;
    this.messagesByActionTypeLoading = true;
    this.analytics.messagesAnalyticsByActionType(this.buildMessagesSearchQuery()).subscribe((result) => {
      this.messagesByActionTypeData = result;
      this.messagesByActionTypeLoading = false;
    });
  }

  private buildMessagesSearchQuery(): DialogFlowRequest {
    return new DialogFlowRequest(
      this.state.currentApplication.namespace,
      this.state.currentApplication.name,
      this.state.currentLocale,
      this.state.currentApplication.name,
      this.selectedConfigurationName,
      this.selectedConnectorId,
      toISOStringWithoutOffset(this.filter.from),
      toISOStringWithoutOffset(this.filter.to),
      this.displayTests,
      this.filter.intent
    );
  }

  datesChanged(dates: [Date, Date]) {
    this.startDate = dates[0];
    this.endDate = dates[1];
    this.state.dateRange = {
      start: dates[0],
      end: dates[1],
      rangeInDays: this.state.dateRange.rangeInDays
    };
    this.reload(true);
  }

  selectedConfigurationChanged(event?: SelectBotEvent) {
    this.selectedConfigurationName = !event ? null : event.configurationName;
    this.selectedConnectorId = !event ? null : event.configurationId;
    this.reload(true);
  }

  collapsedChange(event?: boolean) {
    setTimeout((_) => this.reload(false));
  }

  waitAndRefresh() {
    setTimeout((_) => this.reload(true));
  }
}

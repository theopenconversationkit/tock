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

import { Component, OnInit } from '@angular/core';
// import * as html2pdf from 'html2pdf.js';
import { StateService } from 'src/app/core-nlp/state.service';
import { BotConfigurationService } from 'src/app/core/bot-configuration.service';
import { BotApplicationConfiguration, ConnectorType } from 'src/app/core/model/configuration';

import { AnalyticsService } from '../analytics.service';
import { ChartData } from '../chart/ChartData';
import { DialogFlowRequest } from '../flow/flow';
import { UserAnalyticsPreferences } from '../preferences/UserAnalyticsPreferences';
import { UserAnalyticsQueryResult } from '../users/users';
import { UserFilter } from '../users/users.component';
import { toISOStringWithoutOffset } from '../../shared/utils';
import { SelectBotEvent } from '../../shared/components';

@Component({
  selector: 'tock-activity',
  templateUrl: './activity.component.html',
  styleUrls: ['./activity.component.css']
})
export class ActivityComponent implements OnInit {
  startDate: Date;
  endDate: Date;
  selectedConnectorId: string;
  selectedConfigurationName: string;
  displayTests = false;

  filter: UserFilter = new UserFilter([], false);
  usersChart: ChartData;

  messagesByTypeData: UserAnalyticsQueryResult;
  messagesByDaysData: UserAnalyticsQueryResult;
  messagesByStoryData: UserAnalyticsQueryResult;
  messagesByIntentData: UserAnalyticsQueryResult;
  messagesByConfigurationData: UserAnalyticsQueryResult;
  messagesByConnectorData: UserAnalyticsQueryResult;

  messagesByTypeLoading = false;
  messagesByDaysLoading = false;
  messagesByStoryLoading = false;
  messagesByIntentLoading = false;
  messagesByConfigurationLoading = false;
  messagesByConnectorLoading = false;

  messagesPercentageLoading = false;

  globalUsersCount: number[];
  //   globalMessagesCount: number[];
  configurations: BotApplicationConfiguration[];
  connectors: string[];

  userPreferences: UserAnalyticsPreferences;

  currentFilterNbMessages = 0;
  beforeCurrentFilterNbMessages = 0;
  variationMessagesPercentage = 0;
  activeUsersLoading: boolean;
  usersPercentageLoading: boolean;
  activeUsersData: UserAnalyticsQueryResult;
  currentFilterNbUsers: number;
  beforeCurrentFilterNbUsers: number;
  variationUsersPercentage: number;

  constructor(private state: StateService, private analytics: AnalyticsService, private botConfiguration: BotConfigurationService) {
    this.botConfiguration.configurations.subscribe((configs) => {
      this.configurations = configs;
    });
    this.userPreferences = this.analytics.getUserPreferences();
  }

  ngOnInit(): void {
    this.reload();
  }

  getNumberOfDays(): number {
    return Number(
      !this.filter.to || !this.filter.from ? 1 : ((this.filter.to.getTime() - this.filter.from.getTime()) / (1000 * 3600 * 24)).toFixed(0)
    );
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

  private reload() {
    if (this.startDate != null) {
      this.filter.from = this.startDate;
      this.filter.to = this.endDate;
      this.buildMessagesCharts();
      this.buildMessagesByDaysCharts();
      this.buildMessagesByStoryCharts();
      this.buildMessagesByIntentCharts();
      this.buildMessagesByConfigurationCharts();
      this.buildMessagesByConnectorCharts();
      this.buildActiveUsersCharts();
    }
  }

  private getTotalNumber(usersData): number {
    let count = [];
    usersData.forEach((value) => {
      count.push(value.reduce((x, y) => x + y));
    });
    return count.reduce((x, y) => x + y);
  }

  private buildMessagesCharts() {
    this.messagesByTypeLoading = true;
    this.messagesPercentageLoading = true;
    this.analytics.messagesAnalytics(this.buildMessagesSearchQuery()).subscribe((result) => {
      this.connectors = result.connectorsType;
      this.messagesByTypeData = result;
      this.messagesByTypeLoading = false;
      this.currentFilterNbMessages = this.getTotalNumber(result.usersData);

      this.analytics.messagesAnalytics(this.buildPreviousDateSearchQuery(this.getNumberOfDays())).subscribe((result) => {
        this.beforeCurrentFilterNbMessages = this.getTotalNumber(result.usersData);
        this.variationMessagesPercentage = this.messagesVariationPercentage(
          this.beforeCurrentFilterNbMessages,
          this.currentFilterNbMessages
        );
        this.messagesPercentageLoading = false;
      });
    });
  }

  private buildActiveUsersCharts() {
    this.activeUsersLoading = true;
    this.usersPercentageLoading = true;
    this.analytics.usersAnalytics(this.buildMessagesSearchQuery()).subscribe((result) => {
      this.connectors = result.connectorsType;
      this.activeUsersData = result;
      this.activeUsersLoading = false;
      this.currentFilterNbUsers = this.getTotalNumber(result.usersData);

      this.analytics.usersAnalytics(this.buildPreviousDateSearchQuery(this.getNumberOfDays())).subscribe((result) => {
        this.beforeCurrentFilterNbUsers = this.getTotalNumber(result.usersData);
        this.variationUsersPercentage = this.messagesVariationPercentage(this.beforeCurrentFilterNbUsers, this.currentFilterNbUsers);
        this.usersPercentageLoading = false;
      });
    });
  }

  metricIncreased(before, current): boolean {
    return current > before;
  }

  getBigIcon(before, current) {
    if (current == before) {
      return 'dash-lg';
    } else {
      return this.metricIncreased(before, current) ? 'arrow-up-right' : 'arrow-down-right';
    }
  }

  getSmallIcon(before, current) {
    if (current == before) {
      return 'dash';
    } else {
      return this.metricIncreased(before, current) ? 'arrow-up-short' : 'arrow-down-short';
    }
  }

  getIconStatus(before, current) {
    if (current == before) {
      return 'basic';
    } else {
      return this.metricIncreased(before, current) ? 'success' : 'danger';
    }
  }

  messagesVariationPercentage(before, current): number {
    if (before == current) {
      return 0;
    }
    if (current == 0) {
      return 100;
    }
    if (before == 0) {
      before = 1;
    }
    return Number(((current / before) * 100).toFixed(0));
  }

  private buildMessagesByDaysCharts() {
    if (!this.userPreferences.graphs.activity.messagesByDays) return;
    this.messagesByDaysLoading = true;
    this.analytics.messagesAnalytics(this.buildMessagesSearchQuery()).subscribe((result) => {
      this.connectors = result.connectorsType;
      this.messagesByDaysData = result;
      this.messagesByDaysLoading = false;
    });
  }

  private buildMessagesByStoryCharts() {
    if (!this.userPreferences.graphs.activity.messagesByStory) return;
    this.messagesByStoryLoading = true;
    this.analytics.messagesAnalyticsByDateAndStory(this.buildMessagesSearchQuery()).subscribe((result) => {
      this.messagesByStoryData = result;
      this.messagesByStoryLoading = false;
    });
  }

  private buildMessagesByIntentCharts() {
    if (!this.userPreferences.graphs.activity.messagesByIntent) return;
    this.messagesByIntentLoading = false;
    this.analytics.messagesAnalyticsByDateAndIntent(this.buildMessagesSearchQuery()).subscribe((result) => {
      this.messagesByIntentData = result;
      this.messagesByIntentLoading = false;
    });
  }

  private buildMessagesByConfigurationCharts() {
    if (!this.userPreferences.graphs.activity.messagesByConfiguration) return;
    this.messagesByConfigurationLoading = true;
    this.analytics.messagesAnalyticsByConfiguration(this.buildMessagesSearchQuery()).subscribe((result) => {
      this.messagesByConfigurationData = result;
      this.messagesByConfigurationLoading = false;
    });
  }

  private buildMessagesByConnectorCharts() {
    if (!this.userPreferences.graphs.activity.messagesByConnector) return;
    this.messagesByConnectorLoading = true;
    this.analytics.messagesAnalyticsByConnectorType(this.buildMessagesSearchQuery()).subscribe((result) => {
      this.messagesByConnectorData = result;
      this.messagesByConnectorLoading = false;
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
      this.displayTests
    );
  }

  private buildPreviousDateSearchQuery(nbDays: number): DialogFlowRequest {
    if (!this.filter.to) {
      this.filter.to = this.filter.from;
    }
    const oldFromDate = new Date(this.filter.from.getTime());
    oldFromDate.setDate(oldFromDate.getDate() - nbDays);
    const oldToDate = new Date(this.filter.to.getTime());
    oldToDate.setDate(oldToDate.getDate() - nbDays);
    return new DialogFlowRequest(
      this.state.currentApplication.namespace,
      this.state.currentApplication.name,
      this.state.currentLocale,
      this.state.currentApplication.name,
      this.selectedConfigurationName,
      this.selectedConnectorId,
      toISOStringWithoutOffset(oldFromDate),
      toISOStringWithoutOffset(oldToDate),
      this.displayTests
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
    this.reload();
  }

  selectedConfigurationChanged(event?: SelectBotEvent) {
    this.selectedConfigurationName = !event ? null : event.configurationName;
    this.selectedConnectorId = !event ? null : event.configurationId;
    this.reload();
  }

  collapsedChange(event?: boolean) {
    setTimeout((_) => this.reload());
  }

  waitAndRefresh() {
    setTimeout((_) => this.reload());
  }
}

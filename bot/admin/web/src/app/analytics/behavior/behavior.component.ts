import {Component, ViewChild, AfterViewInit} from '@angular/core';
import {UserAnalyticsQueryResult, UserSearchQuery} from "../users/users";
import {DialogFlowRequest} from "../flow/flow";
import {SelectBotEvent} from "../../shared/select-bot/select-bot.component";
import {StateService} from 'src/app/core-nlp/state.service';
import {AnalyticsService} from '../analytics.service';
import {BotConfigurationService} from 'src/app/core/bot-configuration.service';
import {PaginatedQuery} from 'src/app/model/commons';
import {Observable} from 'rxjs';
import {UserFilter} from '../users/users.component';
import {ChartData} from '../chart/ChartData';
import {BotApplicationConfiguration, ConnectorType} from 'src/app/core/model/configuration';
import * as html2pdf from 'html2pdf.js'

@Component({
  selector: 'tock-behavior',
  templateUrl: './behavior.component.html',
  styleUrls: ['./behavior.component.css']
})
export class BehaviorComponent implements AfterViewInit {

  @ViewChild('messagesByStoryItem') messagesByStoryItem;
  @ViewChild('messagesByIntentItem') messagesByIntentItem;
  @ViewChild('messagesByDayOfWeekItem') messagesByDayOfWeekItem;
  @ViewChild('messagesByHourItem') messagesByHourItem;
  @ViewChild('messagesByActionTypeItem') messagesByActionTypeItem;
  @ViewChild('messagesByStoryCategoryItem') messagesByStoryCategoryItem;
  @ViewChild('messagesByStoryTypeItem') messagesByStoryTypeItem;
  @ViewChild('messagesByStoryLocaleItem') messagesByStoryLocaleItem;

  startDate: Date;
  endDate: Date;
  selectedConnectorId: string;
  selectedConfigurationName: string;
  displayTests = true;
  pretty = true;

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

  globalUsersCount: number[];
//   globalMessagesCount: number[];
  configurations: BotApplicationConfiguration[];
  connectors: string[];

  constructor(private state: StateService,
              private analytics: AnalyticsService,
              private botConfiguration: BotConfigurationService) {
    this.botConfiguration.configurations.subscribe(configs => {
        this.configurations = configs;
      }
    )
  }
  ngAfterViewInit() {
    this.messagesByStoryItem.open();
  }

  getConnectorColor(connector: string): string {
    let color;
    switch(connector) {
      case "messenger": {
        color = "#0084ff";
        break;
      }
      case "ga": {
        color = "#fabc05";
        break;
      }
      case "alexa": {
        color = "#3dc3ef";
        break;
      }
      case "slack": {
        color = "#e01f5c";
        break;
      }
      case "rocket": {
        color = "#dc2727";
        break;
      }
      case "twitter": {
        color = "#1ca3f3";
        break;
      }
      case "whatsapp": {
        color = "#41c352";
        break;
      }
      case "teams": {
        color = "#5d67cf";
        break;
      }
      case "businesschat": {
        color = "#58e951";
        break;
      }
      case "web": {
        color = "#878f9c";
        break;
      }
      default: {
        color = "#f3745d";
        break;
      }
    }
    return color;
  }

  getFileName():string{
    let fileName = "Export-" + this.startDate.toLocaleDateString();
    if(this.endDate != null){
      fileName+="-" + this.endDate.toLocaleDateString();
    }
    fileName += ".pdf";
    return fileName;
  }

  onPdfAction() {
    const options = {
      filename: this.getFileName(),
      image: {type: 'jpeg ', quality: 0.95},
      html2canvas: {},
      jsPDF: {orientation: 'landscape'}
    };
    const content: Element = document.getElementById('element-id');
    html2pdf()
      .from(content)
      .set(options)
      .save()
  }

  getConnector(connectorId: string): ConnectorType {
    let connectors = this.configurations.filter(config => config.connectorType.id === connectorId).map(config => config.connectorType)
    return connectors && connectors.length > 0 ? connectors[0] : null
  }

  findUsers(query: PaginatedQuery): Observable<UserAnalyticsQueryResult> {
    return this.analytics.usersAnalytics(this.buildUserSearchQuery(query));
  }

  private reload(force?: boolean) {
    if (this.startDate != null && !this.loading) {
      this.filter.from = this.startDate;
      this.filter.to = this.endDate;
//       this.usersGraph(that);
      if (this.messagesByStoryItem && this.messagesByStoryItem.expanded) {
        if (force || !this.messagesByStoryData) {
          this.buildMessagesByStoryCharts();
        }
      } else if (force) {
        this.messagesByStoryData = null;
      }
      if (this.messagesByIntentItem && this.messagesByIntentItem.expanded) {
        if (force || !this.messagesByIntentData) {
          this.buildMessagesByIntentCharts();
        }
      } else if (force) {
        this.messagesByIntentData = null;
      }
      if (this.messagesByDayOfWeekItem && this.messagesByDayOfWeekItem.expanded) {
        if (force || !this.messagesByDayOfWeekData) {
          this.buildMessagesByDayOfWeekCharts();
        }
      } else if (force) {
        this.messagesByDayOfWeekData = null;
      }
      if (this.messagesByHourItem && this.messagesByHourItem.expanded) {
        if (force || !this.messagesByHourData) {
          this.buildMessagesByHourCharts();
        }
      } else if (force) {
        this.messagesByHourData = null;
      }
      if (this.messagesByActionTypeItem && this.messagesByActionTypeItem.expanded) {
        if (force || !this.messagesByActionTypeData) {
          this.buildMessagesByActionTypeCharts();
        }
      } else if (force) {
        this.messagesByActionTypeData = null;
      }
      if (this.messagesByStoryCategoryItem && this.messagesByStoryCategoryItem.expanded) {
        if (force || !this.messagesByStoryCategoryData) {
          this.buildMessagesByStoryCategoryCharts();
        }
      } else if (force) {
        this.messagesByStoryCategoryData = null;
      }
      if (this.messagesByStoryTypeItem && this.messagesByStoryTypeItem.expanded) {
        if (force || !this.messagesByStoryTypeData) {
          this.buildMessagesByStoryTypeCharts();
        }
      } else if (force) {
        this.messagesByStoryTypeData = null;
      }
      if (this.messagesByStoryLocaleItem && this.messagesByStoryLocaleItem.expanded) {
        if (force || !this.messagesByStoryLocaleData) {
          this.buildMessagesByStoryLocaleCharts();
        }
      } else if (force) {
        this.messagesByStoryLocaleData = null;
      }
    }
  }

  private buildMessagesByStoryCharts() {
    this.loading = true;
    this.analytics.messagesAnalyticsByStory(this.buildMessagesSearchQuery()).subscribe(
      result => {
        this.messagesByStoryData = result;
        this.loading = false;
        this.messagesByStoryItem.open();
      }
    )
  }

  private buildMessagesByStoryTypeCharts() {
    this.loading = true;
    this.analytics.messagesAnalyticsByStoryType(this.buildMessagesSearchQuery()).subscribe(
      result => {
        this.messagesByStoryTypeData = result;
        this.loading = false;
        this.messagesByStoryTypeItem.open();
      }
    )
  }

  private buildMessagesByStoryCategoryCharts() {
    this.loading = true;
    this.analytics.messagesAnalyticsByStoryCategory(this.buildMessagesSearchQuery()).subscribe(
      result => {
        this.messagesByStoryCategoryData = result;
        this.loading = false;
        this.messagesByStoryCategoryItem.open();
      }
    )
  }

  private buildMessagesByStoryLocaleCharts() {
    this.loading = true;
    this.analytics.messagesAnalyticsByStoryLocale(this.buildMessagesSearchQuery()).subscribe(
      result => {
        this.messagesByStoryLocaleData = result;
        this.loading = false;
        this.messagesByStoryLocaleItem.open();
      }
    )
  }

  private buildMessagesByIntentCharts() {
    this.loading = true;
    this.analytics.messagesAnalyticsByIntent(this.buildMessagesSearchQuery()).subscribe(
      result => {
        this.messagesByIntentData = result;
        this.loading = false;
        this.messagesByIntentItem.open();
      }
    )
  }

  private buildMessagesByDayOfWeekCharts() {
    this.loading = true;
    this.analytics.messagesAnalyticsByDayOfWeek(this.buildMessagesSearchQuery()).subscribe(
      result => {
        this.messagesByDayOfWeekData = result;
        this.loading = false;
        this.messagesByDayOfWeekItem.open();
      }
    )
  }

  private buildMessagesByHourCharts() {
    this.loading = true;
    this.analytics.messagesAnalyticsByHour(this.buildMessagesSearchQuery()).subscribe(
      result => {
        this.messagesByHourData = result;
        this.loading = false;
        this.messagesByHourItem.open();
      }
    )
  }

  private buildMessagesByActionTypeCharts() {
    this.loading = true;
    this.analytics.messagesAnalyticsByActionType(this.buildMessagesSearchQuery()).subscribe(
      result => {
        this.messagesByActionTypeData = result;
        this.loading = false;
        this.messagesByActionTypeItem.open();
      }
    )
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
      this.filter.displayTests);
  }

  private buildMessagesSearchQuery(): DialogFlowRequest {
    return new DialogFlowRequest(
      this.state.currentApplication.namespace,
      this.state.currentApplication.name,
      this.state.currentLocale,
      this.state.currentApplication.name,
      this.selectedConfigurationName,
      this.selectedConnectorId,
      this.filter.from,
      this.filter.to,
      this.displayTests
    );
  }

  datesChanged(dates: [Date, Date]) {
    this.startDate = dates[0];
    this.endDate = dates[1];
    this.reload(true);
  }

  selectedConfigurationChanged(event?: SelectBotEvent) {
    this.selectedConfigurationName = !event ? null : event.configurationName;
    this.selectedConnectorId = !event ? null : event.configurationId;
    this.reload(true);
  }

  collapsedChange(event?: boolean) {
    setTimeout(_ => this.reload(false));
  }

  waitAndRefresh() {
    setTimeout(_ => this.reload(true));
  }
}

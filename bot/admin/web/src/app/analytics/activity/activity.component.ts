import {AfterViewInit, Component, ViewChild} from '@angular/core';
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
import { PreferencesComponent } from '../preferences/preferences.component';
import { UserAnalyticsPreferences } from '../preferences/UserAnalyticsPreferences';
import { SettingsService } from 'src/app/core-nlp/settings.service';

@Component({
  selector: 'tock-activity',
  templateUrl: './activity.component.html',
  styleUrls: ['./activity.component.css']
})
export class ActivityComponent implements AfterViewInit {

  @ViewChild('messagesByTypeItem') messagesByTypeItem;
  @ViewChild('messagesByStoryItem') messagesByStoryItem;
  @ViewChild('messagesByIntentItem') messagesByIntentItem;
  @ViewChild('messagesByConfigurationItem') messagesByConfigurationItem;
  @ViewChild('messagesByConnectorItem') messagesByConnectorItem;

  startDate: Date;
  endDate: Date;
  selectedConnectorId: string;
  selectedConfigurationName: string;
  displayTests = false;

  filter: UserFilter = new UserFilter([], false);
  loading = false;
  usersChart: ChartData;

  messagesByTypeData: UserAnalyticsQueryResult;
  messagesByStoryData: UserAnalyticsQueryResult;
  messagesByIntentData: UserAnalyticsQueryResult;
  messagesByConfigurationData: UserAnalyticsQueryResult;
  messagesByConnectorData: UserAnalyticsQueryResult;

  messagesByTypeLoading = false;
  messagesByStoryLoading = false;
  messagesByIntentLoading = false;
  messagesByConfigurationLoading = false;
  messagesByConnectorLoading = false;

  globalUsersCount: number[];
//   globalMessagesCount: number[];
  configurations: BotApplicationConfiguration[];
  connectors: string[];

  userPreferences: UserAnalyticsPreferences;

  constructor(private state: StateService,
              private analytics: AnalyticsService,
              private botConfiguration: BotConfigurationService) {
    this.botConfiguration.configurations.subscribe(configs => {
        this.configurations = configs;
      }
    )
    this.userPreferences = this.analytics.getUserPreferences();
  }

  ngAfterViewInit() {
    let selectedGraphs = this.getSelectedGraphToDisplay()
    if(selectedGraphs.length > 0){
      setTimeout(() => {
        selectedGraphs[0].open();
      });
    }
  }

  getSelectedGraphToDisplay(): any[] {
    let selectedGraphs = []
    if(this.userPreferences.graphs.activity.messagesAll == true) {
      selectedGraphs.push(this.messagesByTypeItem);
    }
    if(this.userPreferences.graphs.activity.messagesByStory == true) {
      selectedGraphs.push(this.messagesByStoryItem);
    }
    if(this.userPreferences.graphs.activity.messagesByIntent == true) {
      selectedGraphs.push(this.messagesByIntentItem);
    }
    if(this.userPreferences.graphs.activity.messagesByConfiguration == true) {
      selectedGraphs.push(this.messagesByConfigurationItem);
    }
    if(this.userPreferences.graphs.activity.messagesByConnector == true) {
      selectedGraphs.push(this.messagesByConnectorItem);
    }
    return selectedGraphs
  }

  isGraphTypeSelected(graphType: string): boolean {
    return this.userPreferences.selectedChartType.toString() == "all" || this.userPreferences.selectedChartType.toString() == graphType
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
      if (this.messagesByTypeItem && this.messagesByTypeItem.expanded) {
        if (force || !this.messagesByTypeData) {
          this.buildMessagesCharts();
        }
      } else if (force) {
        this.messagesByTypeData = null;
      }
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
      if (this.messagesByConfigurationItem && this.messagesByConfigurationItem.expanded) {
        if (force || !this.messagesByConfigurationData) {
          this.buildMessagesByConfigurationCharts();
        }
      } else if (force) {
        this.messagesByConfigurationData = null;
      }
      if (this.messagesByConnectorItem && this.messagesByConnectorItem.expanded) {
        if (force || !this.messagesByConnectorData) {
          this.buildMessagesByConnectorCharts();
        }
      } else if (force) {
        this.messagesByConnectorData = null;
      }
    }
  }

  private usersGraph(that: this) {
    this.findUsers(this.state.createPaginatedQuery(0)).subscribe(
      result => {
        this.connectors = result.connectorsType;
        let graphdata = [];

        result.dates.forEach(function (date, index) {
          graphdata.push([date].concat(result.usersData[index]))
        });
        this.globalUsersCount = new Array(this.connectors.length).fill(0);
        result.usersData.forEach(function (userData) {
          that.connectors.forEach(function (value, index,) {
            that.globalUsersCount[index] += userData[index]
          })
        })
        let columnNames = ["Day"].concat(this.connectors.map((c, i) => c + "  " + this.globalUsersCount[i]))
        let connectorColor = this.connectors.map(connectorType => that.getConnectorColor(connectorType))
        let options = {
          legend: {position: 'right'},
          colors: connectorColor,
          pointSize: 5, is3D: true
        };
        this.usersChart = new ChartData("LineChart", graphdata, columnNames, options, '500', '1000');
        this.loading = false;
      }
    )
  }

  private buildMessagesCharts() {
    this.loading = true;
    this.messagesByTypeLoading = true;
    this.analytics.messagesAnalytics(this.buildMessagesSearchQuery()).subscribe(
      result => {
        this.connectors = result.connectorsType;
        this.messagesByTypeData = result;
        this.loading = false;
        this.messagesByTypeItem.open();
        this.messagesByTypeLoading = false;
      }
    )
  }

  private buildMessagesByStoryCharts() {
    this.loading = true;
    this.messagesByStoryLoading = true;
    this.analytics.messagesAnalyticsByDateAndStory(this.buildMessagesSearchQuery()).subscribe(
      result => {
        this.messagesByStoryData = result;
        this.loading = false;
        this.messagesByStoryItem.open();
        this.messagesByStoryLoading = false;
      }
    )
  }

  private buildMessagesByIntentCharts() {
    this.loading = true;
    this.messagesByIntentLoading = false;
    this.analytics.messagesAnalyticsByDateAndIntent(this.buildMessagesSearchQuery()).subscribe(
      result => {
        this.messagesByIntentData = result;
        this.loading = false;
        this.messagesByIntentItem.open();
        this.messagesByIntentLoading = false;
      }
    )
  }

  private buildMessagesByConfigurationCharts() {
    this.loading = true;
    this.messagesByConfigurationLoading = true;
    this.analytics.messagesAnalyticsByConfiguration(this.buildMessagesSearchQuery()).subscribe(
      result => {
        this.messagesByConfigurationData = result;
        this.loading = false;
        this.messagesByConfigurationItem.open();
        this.messagesByConfigurationLoading = false;
      }
    )
  }

  private buildMessagesByConnectorCharts() {
    this.loading = true;
    this.messagesByConnectorLoading = true;
    this.analytics.messagesAnalyticsByConnectorType(this.buildMessagesSearchQuery()).subscribe(
      result => {
        this.messagesByConnectorData = result;
        this.loading = false;
        this.messagesByConnectorItem.open();
        this.messagesByConnectorLoading = false;
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
    this.state.dateRange = {start: dates[0], end: dates[1], rangeInDays: this.state.dateRange.rangeInDays}
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

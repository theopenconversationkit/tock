import {Component} from '@angular/core';
import {UserAnalyticsQueryResult, UserSearchQuery} from "../model/users";
import {StateService} from 'src/app/core-nlp/state.service';
import {MonitoringService} from '../monitoring.service';
import {BotConfigurationService} from 'src/app/core/bot-configuration.service';
import {PaginatedQuery} from 'src/app/model/commons';
import {Observable} from 'rxjs';
import {UserFilter} from '../history/history.component';
import {ChartData} from '../model/ChartData';
import {BotApplicationConfiguration, ConnectorType} from 'src/app/core/model/configuration';
import * as html2pdf from 'html2pdf.js'
import {MessagesAnalyticsQuery} from '../model/MessagesAnalyticsQuery';

@Component({
  selector: 'app-analytics',
  templateUrl: './analytics.component.html',
  styleUrls: ['./analytics.component.css']
})
export class AnalyticsComponent {

  startDate: Date;
  endDate: Date;

  filter: UserFilter = new UserFilter([], false);
  loadingUsers: boolean = false;
  usersChart: ChartData;
  messagesChart: ChartData;
  globalUsersCount: number[];
  globalMessagesCount: number[];
  configurations: BotApplicationConfiguration[];
  connectors: string[];

  constructor(private state: StateService,
              private monitoring: MonitoringService,
              private botConfiguration: BotConfigurationService) {
    this.botConfiguration.configurations.subscribe(configs => {
        this.configurations = configs;
      }
    )
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

  onExportClick() {

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
    return this.monitoring.usersAnalytics(this.buildUserSearchQuery(query));
  }

  search() {
    let that = this;
    this.loadingUsers = true;
    if (this.startDate != null) {
      this.filter.from = this.startDate;
      this.filter.to = this.endDate;
      this.usersGraph(that);
      this.messagesGraph(that);
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
        this.usersChart = new ChartData("Users", "LineChart", graphdata, columnNames, options, 500, 1000);
        this.loadingUsers = false;
      }
    )
  }

  private messagesGraph(that: this) {
    this.monitoring.messagesAnalytics(this.buildMessagesSearchQuery()).subscribe(
      result => {
        this.connectors = result.connectorsType;
        let graphdata = [];
        result.dates.forEach(function (date, index) {
          graphdata.push([date].concat(result.usersData[index]))
        });
        this.globalMessagesCount = new Array(this.connectors.length).fill(0);
        result.usersData.forEach(function (userData) {
          that.connectors.forEach(function (value, index,) {
            that.globalMessagesCount[index] += userData[index]
          })
        })
        let columnNames = ["Day"].concat(this.connectors.map((c, i) => c + "  " + this.globalMessagesCount[i]))
        let connectorColor = this.connectors.map(connectorType => that.getConnectorColor(connectorType))
        let options = {
          legend: {position: 'right'},
          colors: connectorColor,
          pointSize: 5, is3D: true
        };
        this.messagesChart = new ChartData("Messages", "LineChart", graphdata, columnNames, options, 500, 1000);
        this.loadingUsers = false;
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

  private buildMessagesSearchQuery(): MessagesAnalyticsQuery{
    return new MessagesAnalyticsQuery(
      this.state.currentApplication.namespace,
      this.state.currentApplication.name,
      this.state.currentApplication.name,
      this.filter.from,
      this.filter.to
    )
  }

  datesChanged(dates: [Date, Date]) {
    console.debug('Date range changed: start=' + dates[0] + ', end=' + dates[1]);
    this.startDate = dates[0];
    this.endDate = dates[1];
    this.search();
  }
}

/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

import {Component, Input, OnChanges, SimpleChanges} from "@angular/core";
import {UserAnalyticsQueryResult} from "../users/users";
import {ChartData} from './ChartData';
import * as html2pdf from 'html2pdf.js'

@Component({
  selector: 'tock-chart',
  templateUrl: './chart.component.html',
  styleUrls: ['./chart.component.css']
})
export class ChartComponent implements OnChanges {

  @Input()
  pdfId: string;

  @Input()
  type: string;

  @Input()
  title: string;

  @Input()
  pretty = true;

  @Input()
  stacked = false;

  @Input()
  allSeries = false;

  @Input()
  data: UserAnalyticsQueryResult;

  @Input()
  isLoading = false;

  mainChart: ChartData;
  altChart: ChartData;
  isFlipped = false;

  constructor() { }

  ngOnChanges(changes: SimpleChanges){
     this.rebuild();
  }

  rebuild(): void {
    if (this.data) {
      if (this.data.dates.length == 1) {
        this.mainChart = this.buildPieChart(this.data, this.type);
      } else {
        if (this.type == 'PieChart') {
          this.mainChart = this.buildPieChartFromDates(this.data, this.type);
        } else {
          this.mainChart = this.buildChartByDate(this.data, this.type);
          this.altChart = this.buildPieChartFromDates(this.data);
        }
      }
    } else {
      this.mainChart = null;
      this.altChart = null;
    }
  }

  buildChartByDate(result: UserAnalyticsQueryResult, chartType?: string, width?: string) {
      let dates = result.dates;
      let series = result.connectorsType;
      let data = result.usersData;
      let rows = [];

      dates.forEach(function (date, index) {
        rows.push([date].concat(data[index]))
      });
      let serieCount = new Array(series.length).fill(0);
      data.forEach(function (data) {
        series.forEach(function (value, index,) {
          serieCount[index] += data[index]
        })
      })
      let seriesLabels = ["Date"].concat(series.map((c, i) => c + "  (" + serieCount[i] + ")"))
      let colors = series.map(serie => this.getColor(serie, series))
      let options = {
        aggregationTarget: this.allSeries ? 'category' : 'auto',
        focusTarget: this.allSeries ? 'category' : 'datum',
        animation: {
          startup: true,
          duration: 1000,
          easing: 'inAndOut'
        },
        legend: {position: 'right'},
        colors: colors,
//         dataOpacity: 0.5,
        pointSize: 5
      };
      if (this.stacked) {
        (options as any).isStacked = true;
      }
      if (this.pretty) {
        (options as any).curveType = 'function';
      }
      return new ChartData(chartType ? chartType : this.stacked ? "AreaChart" : "LineChart",
        rows, seriesLabels, options, '500', width ? width : '100%');
  }

  buildPieChart(result: UserAnalyticsQueryResult, chartType?: string, width?: string) {
      let series = result.connectorsType;
      let data = result.usersData;
      let rows = [];

      series.forEach(function (serie, index) {
        rows.push([serie].concat(data[0][index]))
      });
      let colors = series.map(serie => this.getColor(serie, series))
      let options = {
        pieHole: 0.5,
        colors: colors
      };
      if (this.pretty) {
        (options as any).is3D = true;
      }
      return new ChartData(chartType ? chartType : 'PieChart', rows, undefined, options, '500', width ? width : '100%');
  }

  buildPieChartFromDates(result: UserAnalyticsQueryResult, chartType?: string, width?: string) {
      let series = result.connectorsType;
      let data = result.usersData;
      let rows = [];

      let serieCount = new Array(series.length).fill(0);
      data.forEach(function (data) {
        series.forEach(function (value, index,) {
          serieCount[index] += data[index]
        })
      })

      series.forEach(function (serie, index) {
        rows.push([serie].concat(serieCount[index]))
      });
      let colors = series.map(serie => this.getColor(serie, series))
      let options = {
        pieHole: 0.5,
        colors: colors
      };
      if (this.pretty) {
        (options as any).is3D = true;
      }
      return new ChartData(chartType ? chartType : 'PieChart', rows, undefined, options, '500', width ? width : '100%');
  }

  getColor(serie: string, series: string[]): string {
    let colors = ["#0084ff", "#fabc05", "#3dc3ef", "#e01f5c", "#dc2727", "#1ca3f3", "#41c352", "#5d67cf", "#58e951",
      "#878f9c", "#f3745d"];
    let serieIndex = series.indexOf(serie);
    let colorIndex = serieIndex % colors.length;
    return colors[colorIndex];
  }

  chartIcon(): string {
    return this.mainChart.type == 'PieChart' ? 'pie-chart-outline' : 'bar-chart-outline';
  }

  onFlipAction() {
    this.isFlipped = !this.isFlipped;
  }

  onCsvAction() {
    let columnsNumber = this.mainChart.data[0].length;
    let csv = '';
    if (this.mainChart.columnNames) {
      this.mainChart.columnNames.forEach(function (column, index) {
        csv += column;
        if (index + 1 < columnsNumber) {
          csv += ',';
        }
      })
      csv += '\n';
    }
    this.mainChart.data.forEach(function (row) {
      row.forEach(function (value, index) {
        csv += value;
        if (index + 1 < columnsNumber) {
          csv += ',';
        }
      })
      csv += '\n';
    })
    const blob = new Blob([csv], { type: 'text/csv' });
    const url= window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = this.getFileName('csv');
    a.click();
  }

  getFileName(exportType: string):string{
    return `tock-${exportType}export-` + this.title + `.${exportType}`;
  }

  onPdfAction() {
    const options = {
      filename: this.getFileName('pdf'),
      image: {type: 'jpeg ', quality: 0.95},
      html2canvas: {scale: 0.9 },
      jsPDF: {orientation: 'landscape'},
      pagebreak: { mode: 'avoid-all' }
    };
    let element = document.getElementById(this.pdfId)
    html2pdf()
      .set(options)
      .from(element)
      .save()
  }
}

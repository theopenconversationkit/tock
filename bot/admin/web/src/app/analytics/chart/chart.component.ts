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
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { NbDialogRef, NbDialogService } from '@nebular/theme';
// import * as html2pdf from 'html2pdf.js';
import { ChartDialogComponent } from '../chart-dialog/chart-dialog.component';

import { UserAnalyticsPreferences } from '../preferences/UserAnalyticsPreferences';
import { UserAnalyticsQueryResult } from '../users/users';
import { UserFilter } from '../users/users.component';
import { ChartData, GraphInfo } from './ChartData';

@Component({
  selector: 'tock-chart',
  templateUrl: './chart.component.html',
  styleUrls: ['./chart.component.css']
})
export class ChartComponent implements OnChanges {
  @Input()
  pdfId: string;

  @Input()
  type: string = 'line';

  @Input()
  title: string;

  @Input()
  filter: UserFilter;

  @Input()
  data: UserAnalyticsQueryResult;

  @Input()
  isLoading = false;

  @Input()
  chartPreferences: UserAnalyticsPreferences;

  @Input()
  isFullScreen = false;

  @Input()
  isMultiChart = true;

  @Input()
  seriesSelectionList: number[] = [];

  mainChart: ChartData;
  altChart: ChartData;
  isFlipped = false;

  intentsList: string[] = [];

  chartOptions: any;
  pieChartOptions: any;

  @Output()
  intentChanged: EventEmitter<UserFilter> = new EventEmitter();

  constructor(private dialogService: NbDialogService) {}

  expand() {
    this.dialogService.open(ChartDialogComponent, {
      context: {
        data: this.data,
        title: this.title,
        type: this.type,
        userPreferences: this.chartPreferences,
        isMultiChart: this.isMultiChart,
        seriesSelectionList: this.seriesSelectionList
      }
    });
  }

  refresh() {
    this.intentChanged.emit(this.filter);
  }

  ngOnChanges(changes: SimpleChanges) {
    this.rebuild();
  }

  rebuild(): void {
    if (this.data) {
      if (this.data.dates.length == 1) {
        this.mainChart = this.buildPieChart(this.data, this.type);
      } else {
        if (this.type == 'PieChart') {
          this.mainChart = this.buildPieChartFromDates(this.data, this.type);
        } else if (this.type == 'Calendar') {
          this.mainChart = this.buildCalendarChart(this.data);
        } else {
          this.initSelectionList();
          if (this.type == 'line') {
            this.mainChart = this.buildChartByDate(this.data, this.type);
          } else if (this.type == 'pie') {
            this.mainChart = this.buildPieChartFromDates(this.data);
          }
        }
      }
    } else {
      this.mainChart = null;
      this.altChart = null;
      this.intentsList = [];
    }
  }

  initSelectionList() {
    if (this.seriesSelectionList.length == 0 && this.data.connectorsType.length > 5) {
      this.seriesSelectionList = Array.from({ length: 5 }, (v, k) => k);
    }
  }

  updateGraph() {
    this.seriesSelectionList.sort((a, b) => a - b);
    this.rebuild();
  }

  changeChartType(type: string) {
    this.type = type;
    this.rebuild();
  }

  displayMultipleSelectComponent(): boolean {
    return this.data.connectorsType.length > 5 && this.type !== 'PieChart';
  }

  getDataFromSelection(data: string[]) {
    let result = [];
    if (this.seriesSelectionList.length > 0) {
      this.seriesSelectionList.forEach((value, index) => {
        result.push(data[value]);
      });
      return result;
    } else {
      return data;
    }
  }

  getColumnsLength(series: string[]): number {
    if (this.seriesSelectionList.length > 0) {
      return this.seriesSelectionList.length;
    } else {
      return series.length;
    }
  }

  buildCalendarChart(result: UserAnalyticsQueryResult) {
    let dates = result.dates;
    let that = this;
    let data = result.usersData;
    let rows = [];

    const unique = (value, index, self) => {
      return self.indexOf(value) === index;
    };
    const years = dates.map((date) => new Date(date).getFullYear()).filter(unique);

    dates.forEach(function (date, index) {
      rows.push([new Date(date)].concat(that.getDataFromSelection(data[index]).reduce((x, y) => x + y)));
    });
    this.chartOptions = {
      tooltip: {
        position: 'top'
      },
      visualMap: {
        min: 0,
        calculable: true,
        orient: 'horizontal',
        left: 'center',
        top: 'top',
        color: ['#143db8', '#3366ff', '#f0f4ff']
      },
      calendar: this.getCalendarList(years),
      series: this.getSeriesList(years, rows)
    };
    return new ChartData('Calendar', rows, null, null, '500', '100%');
  }

  getCalendarList(years: any[]) {
    return years.map((year, index) => {
      return {
        cellSize: ['auto', 15],
        top: index != 0 ? 190 * (index - 1) + 260 : 90,
        range: year,
        itemStyle: {
          borderWidth: 0.5
        }
      };
    });
  }

  getSeriesList(years: any[], rows: any[]) {
    return years.map((_, index) => {
      return {
        type: 'heatmap',
        coordinateSystem: 'calendar',
        calendarIndex: index,
        data: rows
      };
    });
  }

  buildChartByDate(result: UserAnalyticsQueryResult, chartType?: string, width?: string) {
    let dates = result.dates;
    let series: any[] = result.connectorsType;
    let that = this;
    let data = result.usersData;
    let rows = [];

    dates.forEach(function (date, index) {
      if (chartType === 'Calendar') {
        rows.push([new Date(date)].concat(that.getDataFromSelection(data[index]).reduce((x, y) => x + y)));
      } else {
        rows.push([date].concat(that.getDataFromSelection(data[index])));
      }
    });
    let seriesCounters = new Array(this.getColumnsLength(series)).fill(0);
    let seriesNumber = that.getDataFromSelection(series).length;
    const seriesValues = new Array(seriesNumber);
    for (let index = 0; index < seriesNumber; index++) {
      seriesValues[index] = new Array(data.length).fill(0);
    }
    that.getDataFromSelection(series).forEach(function (serie, serieIndex) {
      let serieDataIndex = series.indexOf(serie);
      data.forEach(function (d, index) {
        let serieDateValue = d[serieDataIndex];
        seriesValues[serieIndex][index] = serieDateValue;
        seriesCounters[serieIndex] += serieDateValue;
      });
    });
    let seriesLabels = this.getDataFromSelection(series).map((c, i) => c + '(' + seriesCounters[i] + ')');
    let colors = series.map((serie) => this.getColor(serie, series));
    this.chartOptions = {
      tooltip: {
        trigger: this.getTooltipType(this.chartPreferences.lineConfig.focusTarget),
        axisPointer: {
          type: 'cross',
          label: {
            backgroundColor: '#6a7985'
          }
        }
      },
      grid: {
        left: '5%',
        right: '5%'
      },
      legend: {
        data: seriesLabels,
        type: 'scroll',
        orient: 'horizontal',
        bottom: 0,
        textStyle: {
          color: '#8f9bb3'
        }
      },
      color: colors,
      xAxis: {
        type: 'category',
        data: dates
      },
      yAxis: {
        type: 'value'
      },
      series: this.getSeriesData(
        seriesValues,
        seriesLabels,
        this.chartPreferences.lineConfig.curvedLines,
        this.chartPreferences.lineConfig.stacked
      )
    };

    return new ChartData(
      chartType ? chartType : this.chartPreferences.lineConfig.stacked ? 'AreaChart' : 'LineChart',
      rows,
      ['Date'].concat(seriesLabels),
      null,
      '500',
      width ? width : '100%'
    );
  }

  getTooltipType(isItem) {
    if (isItem) {
      return 'item';
    }
    return 'axis';
  }

  getSeriesData(result, columns, smooth, area) {
    return result.slice(0, columns.length).map((element, index) => {
      return {
        name: columns[index],
        data: element,
        type: 'line',
        smooth: smooth,
        areaStyle: this.isAreaChart(area)
      };
    });
  }
  isAreaChart(area) {
    if (area) {
      return {};
    }
    return null;
  }

  buildPieChart(result: UserAnalyticsQueryResult, chartType?: string, width?: string) {
    let series = result.connectorsType;
    let rows = [];
    this.intentsList = result.intents;
    series.forEach(function (serie, index) {
      let data = result.usersData;
      rows.push(new GraphInfo(data[0][index], serie));
    });
    let colors = series.map((serie) => this.getColor(serie, series));

    this.chartOptions = {
      tooltip: {
        trigger: 'item',
        formatter: '{b} : {c} ({d}%)'
      },
      legend: {
        type: 'scroll',
        orient: 'horizontal',
        bottom: 0,
        textStyle: {
          color: '#8f9bb3'
        }
      },
      color: colors,
      grid: {
        left: '0%',
        right: '0%',
        top: '0%',
        bottom: '0%'
      },
      series: [
        {
          type: 'pie',
          center: ['50%', '50%'],
          data: rows,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    };
    return new ChartData(chartType ? chartType : 'PieChart', rows, series, null, '500', width ? width : '100%');
  }

  buildPieChartFromDates(result: UserAnalyticsQueryResult, chartType?: string, width?: string) {
    let series: any[] = result.connectorsType;
    let data = result.usersData;
    let rows = [];
    let that = this;

    let serieCount = new Array(this.getColumnsLength(series)).fill(0);
    data.forEach(function (data) {
      that.getDataFromSelection(series).forEach(function (value, index) {
        serieCount[index] += that.getDataFromSelection(data)[index];
      });
    });

    that.getDataFromSelection(series).forEach(function (serie, index) {
      rows.push(new GraphInfo(serieCount[index], serie));
    });
    let colors = series.map((serie) => this.getColor(serie, series));
    this.chartOptions = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b} : {c} ({d}%)'
      },
      legend: {
        type: 'scroll',
        orient: 'horizontal',
        bottom: 0,
        textStyle: {
          color: '#8f9bb3'
        }
      },
      color: colors,
      series: [
        {
          type: 'pie',
          data: rows,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    };
    return new ChartData(chartType ? chartType : 'PieChart', rows, ['Date'].concat(series), undefined, '500', width ? width : '100%');
  }

  getColor(serie: string, series: string[]): string {
    let colors = ['#0084ff', '#fabc05', '#3dc3ef', '#e01f5c', '#dc2727', '#1ca3f3', '#41c352', '#5d67cf', '#58e951', '#878f9c', '#f3745d'];
    let serieIndex = series.indexOf(serie);
    let colorIndex = serieIndex % colors.length;
    return colors[colorIndex];
  }

  chartIcon(): string {
    return this.mainChart.type == 'PieChart' ? 'pie-chart' : 'bar-chart';
  }

  onFlipAction() {
    this.isFlipped = !this.isFlipped;
  }

  onCsvAction() {
    let columnsNumber = this.mainChart.data[0].length;
    if (this.mainChart.type == 'PieChart') {
      columnsNumber = this.mainChart.data.length;
    }
    let csv = '';
    if (this.mainChart.columnNames) {
      this.mainChart.columnNames.forEach(function (column, index) {
        csv += column;
        if (index + 1 < columnsNumber) {
          csv += ',';
        }
      });
      csv += '\n';
    }
    if (this.mainChart.type == 'PieChart') {
      this.mainChart.data.forEach(function (data, index) {
        csv += (data as unknown as GraphInfo).value;
        if (index + 1 < columnsNumber) {
          csv += ',';
        }
      });
      csv += '\n';
    } else {
      this.mainChart.data.forEach(function (row) {
        row.forEach(function (value, index) {
          csv += value;
          if (index + 1 < columnsNumber) {
            csv += ',';
          }
        });
        csv += '\n';
      });
    }

    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = this.getFileName('csv');
    a.click();
  }

  getFileName(exportType: string): string {
    return `tock-${exportType}export-` + this.title + `.${exportType}`;
  }

  // onPdfAction() {
  //   const options = {
  //     margin: 0,
  //     filename: this.getFileName('pdf'),
  //     image: { type: 'jpeg ', quality: 0.95 },
  //     html2canvas: { scale: 1 },
  //     jsPDF: { orientation: 'landscape', format: 'a2', compress: true },
  //     pagebreak: { mode: ['avoid-all', 'css', 'legacy'] }
  //   };
  //   let element = document.getElementById(this.pdfId);
  //   html2pdf().from(element).set(options).save();
  // }
}

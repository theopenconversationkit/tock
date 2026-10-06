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
import { Component, inject, Input, OnChanges, SimpleChanges } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import type { EChartsOption } from 'echarts';

import { DashboardUsage, WidgetState } from '../../models/dashboard.model';

@Component({
  selector: 'tock-questions-answered',
  templateUrl: './questions-answered.component.html',
  styleUrls: ['./questions-answered.component.scss'],
  standalone: false
})
export class QuestionsAnsweredComponent implements OnChanges {
  @Input() usage: DashboardUsage;
  @Input() state: WidgetState = WidgetState.loading;
  @Input() period: number = 30;

  WidgetState = WidgetState;

  chartOptions: EChartsOption;

  private readonly transloco = inject(TranslocoService);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['usage']) {
      this.initChart();
    }
  }

  get delta(): number | null {
    if (!this.usage?.previousTotal) return null;
    return (this.usage.total - this.usage.previousTotal) / this.usage.previousTotal;
  }

  private initChart(): void {
    if (!this.usage?.byDate?.length) {
      this.chartOptions = undefined;
      return;
    }

    const current = this.usage.byDate;
    const previous = this.usage.previousByDate ?? [];

    const currentName = this.transloco.translate('dashboard.questions-answered.series-current');
    const previousName = this.transloco.translate('dashboard.questions-answered.series-previous');

    this.chartOptions = {
      color: [this.themeColor('--text-disabled-color', '#c5cee0'), this.themeColor('--color-primary-default', '#3366ff')],
      legend: {
        data: [previousName, currentName],
        right: 0,
        top: 0,
        icon: 'roundRect',
        itemWidth: 10,
        itemHeight: 10,
        textStyle: { fontSize: 11 }
      },
      grid: {
        left: 0,
        right: 8,
        top: 32,
        bottom: 0,
        containLabel: true
      },
      tooltip: {
        trigger: 'axis',
        // The two series are aligned by index, not by date: the tooltip has to spell
        // out which day each value belongs to.
        formatter: (params) => this.formatTooltip(params as unknown[], current, previous)
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: current.map((entry) => this.formatDate(entry.date)),
        axisTick: { show: false },
        // Without this the 90 day range overlaps its own labels.
        axisLabel: { hideOverlap: true }
      },
      yAxis: {
        type: 'value',
        // Low volume bots would otherwise get fractional ticks.
        minInterval: 1,
        axisLabel: { formatter: (value: number) => this.formatCount(value) }
      },
      series: [
        {
          // Declared first so it is drawn behind the current period.
          name: previousName,
          type: 'line',
          smooth: true,
          showSymbol: false,
          lineStyle: { width: 1.5, type: 'dashed' },
          data: previous.map((entry) => entry.count),
          z: 1
        },
        {
          name: currentName,
          type: 'line',
          smooth: true,
          showSymbol: false,
          areaStyle: { opacity: 0.18 },
          data: current.map((entry) => entry.count),
          z: 2
        }
      ]
    };
  }

  private formatTooltip(params: unknown[], current: { date: string }[], previous: { date: string }[]): string {
    const rows = (params as { seriesName: string; value: number; dataIndex: number; marker: string }[]) ?? [];
    if (!rows.length) return '';

    const index = rows[0].dataIndex;

    return rows
      .map((row) => {
        const source = row.seriesName === this.transloco.translate('dashboard.questions-answered.series-current') ? current : previous;
        const date = source[index]?.date;
        const label = date ? this.formatDate(date) : '';
        return `${row.marker} ${label} — ${this.formatCount(row.value ?? 0)}`;
      })
      .join('<br/>');
  }

  /** Read from the active Nebular theme so the chart follows light and dark. */
  private themeColor(variable: string, fallback: string): string {
    const value = getComputedStyle(document.body).getPropertyValue(variable)?.trim();
    return value || fallback;
  }

  private formatDate(isoDate: string): string {
    return new Intl.DateTimeFormat(this.transloco.getActiveLang(), {
      day: '2-digit',
      month: 'short'
    }).format(new Date(isoDate));
  }

  private formatCount(value: number): string {
    return new Intl.NumberFormat(this.transloco.getActiveLang()).format(value);
  }
}

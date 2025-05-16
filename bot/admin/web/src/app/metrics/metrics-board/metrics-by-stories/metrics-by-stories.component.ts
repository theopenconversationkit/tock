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

import { Component, Input, OnInit } from '@angular/core';
import { NbCalendarRange, NbDialogRef } from '@nebular/theme';
import { EChartsOption } from 'echarts';
import { NGX_ECHARTS_CONFIG } from 'ngx-echarts';
import { Observable, take } from 'rxjs';
import { RestService } from '../../../core-nlp/rest/rest.service';
import { StateService } from '../../../core-nlp/state.service';
import { Pagination } from '../../../shared/components';
import { heuristicValueColorDetection } from '../../commons/utils';
import { IndicatorDefinition, IndicatorValueDefinition, MetricResult, StorySummary } from '../../models';

@Component({
  selector: 'tock-metrics-by-stories',
  templateUrl: './metrics-by-stories.component.html',
  styleUrls: ['./metrics-by-stories.component.scss'],
  providers: [
    {
      provide: NGX_ECHARTS_CONFIG,
      useFactory: () => ({ echarts: () => import('echarts') })
    }
  ]
})
export class MetricsByStoriesComponent implements OnInit {
  @Input() indicatorName: string;
  @Input() indicatorLabel: string;
  @Input() indicators: IndicatorDefinition[];
  @Input() stories: StorySummary[];
  @Input() range: NbCalendarRange<Date>;

  metrics: MetricResult[];
  pagination: Pagination;

  constructor(public dialogRef: NbDialogRef<MetricsByStoriesComponent>, private stateService: StateService, private rest: RestService) {}

  ngOnInit(): void {
    this.getIndicatorMetricsQuery(this.indicatorName)
      .pipe(take(1))
      .subscribe((metrics: MetricResult[]) => {
        this.metrics = metrics;
        if (this.metrics.length) {
          const size = 6;
          this.pagination = { start: 0, end: Math.min(this.storiesIds.length, size), size: size, total: this.storiesIds.length };
          this.initIndicatorByStoriesChart();
        }
      });
  }

  paginationChange(): void {
    this.pagination.end = this.pagination.start + this.pagination.size;
    this.initIndicatorByStoriesChart();
  }

  private getIndicatorMetricsQuery(indicatorName: string): Observable<MetricResult[]> {
    const query = {
      filter: {
        indicatorNames: [indicatorName],
        creationDateSince: this.range.start,
        creationDateUntil: this.range.end
      },
      groupBy: ['TRACKED_STORY_ID', 'TYPE', 'INDICATOR_NAME', 'INDICATOR_VALUE_NAME']
    };
    const url = `/bot/${this.stateService.currentApplication.name}/metrics`;
    return this.rest.post(url, query);
  }

  storiesCharts: EChartsOption[];

  get storiesIds(): string[] {
    const storiesIds = new Set<string>();
    this.metrics.forEach((metric) => {
      if (this.getStorySummaryById(metric.row.trackedStoryId)) storiesIds.add(metric.row.trackedStoryId);
    });
    return [...storiesIds].sort((a, b) => {
      return this.getStoryNameById(a).localeCompare(this.getStoryNameById(b));
    });
  }

  private initIndicatorByStoriesChart(): void {
    this.storiesCharts = [];

    const paginatedStoriesIds = this.storiesIds.slice(this.pagination.start, this.pagination.end);

    paginatedStoriesIds.forEach((storyId: string) => {
      const entries = [];
      const storyIndicators = this.metrics.filter((metric) => metric.row.trackedStoryId === storyId);
      const indicatorMetricsReplies = storyIndicators.filter((indMetric) => indMetric.row.type === 'QUESTION_REPLIED');
      let repliesCount = 0;
      indicatorMetricsReplies.forEach((imr) => {
        if (this.getIndicatorValueByName(imr.row.indicatorName, imr.row.indicatorValueName)) {
          const valueLabel = this.getIndicatorValueLabelByName(imr.row.indicatorName, imr.row.indicatorValueName);
          entries.push({
            value: imr.count,
            name: valueLabel,
            itemStyle: { color: heuristicValueColorDetection(valueLabel) }
          });
        }
        repliesCount += imr.count;
      });

      let indicatorMetricsQuestion = storyIndicators.find((indMetric) => indMetric.row.type === 'QUESTION_ASKED');
      if (indicatorMetricsQuestion) {
        const conversionRate = indicatorMetricsQuestion.count - repliesCount;
        if (conversionRate) {
          entries.push({
            value: conversionRate,
            name: 'No answer given',
            itemStyle: { color: '#aaa' }
          });
        }
      }

      this.storiesCharts.push({
        name: this.getStoryNameById(storyId),
        tooltip: {
          trigger: 'item',
          formatter: function (params) {
            return `${params.data.name} :<br /><strong>${params.percent}%</strong> (${params.data.value})`;
          }
        },
        calculable: true,
        series: [
          {
            name: 'dimension metrics',
            type: 'pie',
            radius: [30, 60],
            itemStyle: {
              borderRadius: 4
            },
            data: entries
          }
        ]
      });
    });
  }

  private getStorySummaryById(id: string): StorySummary {
    return this.stories.find((story) => story._id === id);
  }

  private getStoryNameById(id: string): string {
    return this.getStorySummaryById(id).name;
  }

  private getIndicatorByName(name: string): IndicatorDefinition {
    return this.indicators.find((indicator) => indicator.name === name);
  }

  private getIndicatorValueByName(indicatorname: string, indicatorValueName: string): IndicatorValueDefinition {
    return this.getIndicatorByName(indicatorname).values.find((value) => value.name === indicatorValueName);
  }

  private getIndicatorValueLabelByName(indicatorname: string, indicatorValueName: string): string {
    return this.getIndicatorValueByName(indicatorname, indicatorValueName).label;
  }

  cancel(): void {
    this.dialogRef.close();
  }
}

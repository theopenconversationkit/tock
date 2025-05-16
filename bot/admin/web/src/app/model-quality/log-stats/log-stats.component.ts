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

import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { StateService } from '../../core-nlp/state.service';
import { QualityService } from '../quality.service';
import { LogStat, LogStatsQuery } from '../../model/nlp';
import { formatStatDate } from '../../model/commons';

@Component({
  selector: 'tock-log-stats',
  templateUrl: './log-stats.component.html',
  styleUrls: ['./log-stats.component.scss']
})
export class LogStatsComponent implements OnInit, OnDestroy {
  destroy = new Subject();

  public probabilityChartOptions: any;

  public statsChartOptions: any;

  public durationChartOptions: any;

  public intent: string = '';

  public nodata: boolean = false;

  onlyCurrentLocale: boolean = false;

  constructor(public state: StateService, private quality: QualityService) {}

  ngOnInit(): void {
    this.search();
    this.state.configurationChange.pipe(takeUntil(this.destroy)).subscribe((_) => this.search());
  }

  search(): void {
    let i = this.intent === '' ? null : this.intent;
    this.quality
      .logStats(
        new LogStatsQuery(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale,
          i,
          null,
          this.onlyCurrentLocale
        )
      )
      .subscribe((result: LogStat[]) => {
        if (result.length === 0) {
          this.nodata = true;
        } else {
          this.nodata = false;

          this.buildStatsChart(result);

          this.buildProbabilityChart(result);

          this.buildDurationChart(result);
        }
      });
  }

  buildStatsChart(result: LogStat[]) {
    const countData = result.map((p) => {
      return [p.day, p.count];
    });

    const errorData = result.map((p) => {
      return [p.day, p.error];
    });
    this.statsChartOptions = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
          label: {
            backgroundColor: '#6a7985'
          }
        }
      },
      legend: {
        data: ['Calls', 'Errors'],
        textStyle: {
          color: '#8f9bb3'
        }
      },
      color: ['#0095ff', '#ff3d71'],
      yAxis: {
        type: 'value'
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        axisLabel: {
          formatter: formatStatDate
        }
      },
      series: [
        {
          name: 'Calls',
          type: 'line',
          areaStyle: {},
          smooth: true,
          data: countData
        },
        {
          name: 'Errors',
          type: 'line',
          areaStyle: {},
          smooth: true,
          data: errorData
        }
      ]
    };
  }

  buildProbabilityChart(result: LogStat[]) {
    const intentsData = result.map((p) => {
      return [p.day, Math.round(10000 * p.averageIntentProbability) / 100];
    });

    const entitiesData = result.map((p) => {
      return [p.day, Math.round(10000 * p.averageEntitiesProbability) / 100];
    });
    this.probabilityChartOptions = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
          label: {
            backgroundColor: '#6a7985'
          }
        }
      },
      legend: {
        data: ['Intent average probability', 'Entity average probability'],
        textStyle: {
          color: '#8f9bb3'
        }
      },
      color: ['#ff3d71', '#0095ff'],
      yAxis: {
        axisLabel: {
          formatter: function (value) {
            return value + '%';
          }
        }
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        axisLabel: {
          formatter: formatStatDate
        }
      },
      series: [
        {
          name: 'Intent average probability',
          type: 'line',
          areaStyle: {},
          smooth: true,
          data: intentsData
        },
        {
          name: 'Entity average probability',
          type: 'line',
          areaStyle: {},
          smooth: true,
          data: entitiesData
        }
      ]
    };
  }

  buildDurationChart(result: LogStat[]) {
    const durationData = result.map((p) => {
      return [p.day, Math.round(10000 * p.averageDuration) / 10000];
    });
    this.durationChartOptions = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
          label: {
            backgroundColor: '#6a7985'
          }
        }
      },
      legend: {
        data: ['Average call duration'],
        textStyle: {
          color: '#8f9bb3'
        }
      },
      color: ['#0095ff'],
      yAxis: {
        axisLabel: {
          formatter: function (value) {
            return value + 'ms';
          }
        }
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        axisLabel: {
          formatter: formatStatDate
        }
      },
      series: [
        {
          name: 'Average call duration',
          type: 'line',
          areaStyle: {},
          smooth: true,
          data: durationData
        }
      ]
    };
  }

  ngOnDestroy() {
    this.destroy.next(true);
    this.destroy.complete();
  }
}

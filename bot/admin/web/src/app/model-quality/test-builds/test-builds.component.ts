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
import { TestErrorQuery } from '../../model/nlp';
import { formatStatDateTime } from '../../model/commons';
import moment from 'moment';

let maxDurationUnit: string = 'ms';

function parseDuration(d): number {
  const r = moment.duration(d);
  if (r.asMinutes() > 60) {
    maxDurationUnit = 'h';
  } else if (maxDurationUnit !== 'h' && r.asSeconds() > 60) {
    maxDurationUnit = 'min';
  } else if (maxDurationUnit !== 'h' && maxDurationUnit !== 'min' && r.asMilliseconds() > 1000) {
    maxDurationUnit = 's';
  }

  return Math.round(r.asMilliseconds());
}

function displayDuration(d): string {
  const m = moment(d);
  switch (maxDurationUnit) {
    case 'h':
      return m.format('h[h]mm');
    case 'min':
      return m.format('m[min] s[s]');
    case 's':
      return m.format('s[s] SSS[ms]');
    default:
      return m.format('s[s] SSS[ms]');
  }
}

@Component({
  selector: 'tock-test-builds',
  templateUrl: './test-builds.component.html',
  styleUrls: ['./test-builds.component.scss']
})
export class TestBuildsComponent implements OnInit, OnDestroy {
  destroy = new Subject();

  public errors: Array<any>;
  public durations: Array<any>;
  public sizes: Array<any>;
  public errorChartOptions: any;
  public durationChartOptions: any;
  public sizeChartOptions: any;
  public nodata: boolean = false;
  public loading: boolean = false;

  public intent: string = '';
  public modifiedAfter?: Date;

  constructor(public state: StateService, private quality: QualityService) {
    this.modifiedAfter = new Date();
    this.modifiedAfter.setMonth(this.modifiedAfter.getMonth() - 1);
  }

  ngOnInit(): void {
    this.state.configurationChange.pipe(takeUntil(this.destroy)).subscribe((_) => this.search(this.modifiedAfter));
    this.search(this.modifiedAfter);
  }

  search(date): void {
    if (date) this.modifiedAfter = date;
    this.loading = true;
    this.quality
      .buildStats(TestErrorQuery.createWithoutSize(this.state, this.intent === '' ? undefined : this.intent, this.modifiedAfter))
      .subscribe((result) => {
        this.loading = false;

        if (result.length === 0) {
          this.nodata = true;
          return;
        }

        this.nodata = false;

        maxDurationUnit = 'ms';

        const errorData = result.map((p) => [
          p.date,
          p.nbSentencesTested === 0 ? 0 : Math.round(10000 * (p.errors / p.nbSentencesTested)) / 100
        ]);

        const intentData = result.map((p) => [
          p.date,
          p.nbSentencesTested === 0 ? 0 : Math.round(10000 * (p.intentErrors / p.nbSentencesTested)) / 100
        ]);

        const entityData = result.map((p) => [
          p.date,
          p.nbSentencesTested === 0 ? 0 : Math.round(10000 * (p.entityErrors / p.nbSentencesTested)) / 100
        ]);

        this.errorChartOptions = {
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
            data: ['Errors', 'Intent Errors', 'Entity Errors'],
            textStyle: {
              color: '#8f9bb3'
            }
          },
          color: ['#ff3d71', '#0095ff', '#028A0F'],
          yAxis: {
            axisLabel: {
              formatter: (value) => value + '%'
            }
          },
          xAxis: {
            type: 'category',
            boundaryGap: false,
            axisLabel: {
              formatter: formatStatDateTime
            }
          },
          series: [
            {
              name: 'Errors',
              type: 'line',
              areaStyle: {},
              smooth: true,
              data: errorData,
              formatter: (value) => value + '%'
            },
            {
              name: 'Intent Errors',
              type: 'line',
              areaStyle: {},
              smooth: true,
              data: intentData,
              formatter: (value) => value + '%'
            },
            {
              name: 'Entity Errors',
              type: 'line',
              areaStyle: {},
              smooth: true,
              data: entityData,
              formatter: (value) => value + '%'
            }
          ]
        };

        const modelData = result.map((p) => [p.date, p.nbSentencesInModel]);

        const testData = result.map((p) => [p.date, p.nbSentencesTested]);

        this.sizes = [
          {
            data: modelData,
            label: 'Model size'
          },
          {
            data: testData,
            label: 'Test set size'
          }
        ];
        this.sizeChartOptions = {
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
            data: ['Model size', 'Test set size'],
            textStyle: {
              color: '#8f9bb3'
            }
          },
          color: ['#0095ff', '#028A0F'],
          yAxis: {},
          xAxis: {
            type: 'category',
            boundaryGap: false,
            axisLabel: {
              formatter: formatStatDateTime
            }
          },
          series: [
            {
              name: 'Model size',
              type: 'line',
              areaStyle: {},
              smooth: true,
              data: modelData
            },
            {
              name: 'Test set size',
              type: 'line',
              areaStyle: {},
              smooth: true,
              data: testData
            }
          ]
        };

        const modelDurationData = result.map((p) => [p.date, parseDuration(p.buildModelDuration)]);

        const testDurationData = result.map((p) => [p.date, parseDuration(p.testModelDuration)]);

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
            data: ['Build Model duration', 'Test Model duration'],
            textStyle: {
              color: '#8f9bb3'
            }
          },
          color: ['#0095ff', '#028A0F'],
          yAxis: {
            axisLabel: {
              formatter: displayDuration
            }
          },
          xAxis: {
            type: 'category',
            boundaryGap: false,
            axisLabel: {
              formatter: formatStatDateTime
            }
          },
          series: [
            {
              name: 'Build Model duration',
              type: 'line',
              areaStyle: {},
              smooth: true,
              data: modelDurationData
            },
            {
              name: 'Test Model duration',
              type: 'line',
              areaStyle: {},
              smooth: true,
              data: testDurationData
            }
          ]
        };
        /*
           = {
          tooltips: {
            callbacks: {
              label: function (i, d) {
                return displayDuration(d.datasets[i.datasetIndex].data[i.index].y);
              }
            }
          },
          scales: {
            yAxes: [{
              ticks: {
                userCallback: function (value) {
                  return displayDuration(value);
                }
              }
            }],
            xAxes: [{
              type: "time",
              time: {
                unit: "hour",
                displayFormats: {
                  hour: 'D/M H[h]'
                }
              }
            }]
          }
        };
           */
      });
  }
  ngOnDestroy() {
    this.destroy.next(true);
    this.destroy.complete();
  }
}

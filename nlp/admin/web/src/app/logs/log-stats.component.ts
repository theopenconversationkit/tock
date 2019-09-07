/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Component, OnInit} from "@angular/core";
import {QualityService} from "../quality-nlp/quality.service";
import {StateService} from "../core-nlp/state.service";
import {LogStatsQuery} from "../model/nlp";

@Component({
  selector: 'tock-log-stats',
  templateUrl: './log-stats.component.html',
  styleUrls: ['./log-stats.component.css']
})
export class LogStatsComponent implements OnInit {

  public stats: Array<any>;
  public probability: Array<any>;
  public duration: Array<any>;
  public statsChartOptions: any = {
    scales: {
      xAxes: [{
        type: "time",
        time: {
          unit: "day",
          displayFormats: {
            day: 'D/M'
          }
        }
      }]
    }
  };
  public probabilityChartOptions: any = {
    scales: {
      yAxes: [{
        type: "linear",
        min: 0,
        max: 100,
        ticks: {
          callback: function (value) {
            return value + "%"
          }
        },
        scaleLabel: {
          display: true,
          labelString: "Percentage"
        }
      }],
      xAxes: [{
        type: "time",
        time: {
          unit: "day",
          displayFormats: {
            day: 'D/M'
          }
        }
      }]
    }
  };
  public durationChartOptions: any = {
    scales: {
      yAxes: [{
        ticks: {
          callback: function (value) {
            return value + "ms"
          }
        }
      }],
      xAxes: [{
        type: "time",
        time: {
          unit: "day",
          displayFormats: {
            day: 'D/M'
          }
        }
      }]
    }
  };
  public lineChartLegend: boolean = true;
  public lineChartType: string = 'line';
  public intent: string = "";
  public nodata: boolean = false;
  onlyCurrentLocale: boolean = false;

  constructor(public state: StateService, private quality: QualityService) {

  }

  ngOnInit(): void {
    this.search();
  }

  search(): void {
    let i = this.intent === "_all_" || this.intent === "" ? null : this.intent;
    this.quality.logStats(
      new LogStatsQuery(
        this.state.currentApplication.namespace,
        this.state.currentApplication.name,
        this.state.currentLocale,
        i,
        null,
        this.onlyCurrentLocale))
      .subscribe(result => {
        if (result.length === 0) {
          this.nodata = true;
        } else {
          this.nodata = false;

          const countData = result.map(p => {
            return {
              x: p.day,
              y: p.count,
            };
          });

          const errorData = result.map(p => {
            return {
              x: p.day,
              y: p.error,
            };
          });

          this.stats = [
            {
              data: errorData,
              label: "Errors"
            },
            {
              data: countData,
              label: "Calls"
            }
          ];

          const intentsData = result.map(p => {
            return {
              x: p.day,
              y: Math.round(10000 * p.averageIntentProbability) / 100,
            };
          });

          const entitiesData = result.map(p => {
            return {
              x: p.day,
              y: Math.round(10000 * p.averageEntitiesProbability) / 100,
            };
          });

          this.probability = [
            {
              data: intentsData,
              label: "Intent average probability"
            },
            {
              data: entitiesData,
              label: "Entity average probability"
            }
          ];

          const durationData = result.map(p => {
            return {
              x: p.day,
              y: p.averageDuration,
            };
          });

          this.duration = [
            {
              data: durationData,
              label: "Average call duration"
            }
          ];
        }
      })
  }
}

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

import {Component, OnDestroy, OnInit} from "@angular/core";
import {StateService} from "../core-nlp/state.service";
import * as moment from 'moment';
import {QualityService} from "../quality-nlp/quality.service";
import {TestErrorQuery} from "../model/nlp";
import {Subscription} from "rxjs";

let maxDurationUnit: string = "ms";

function parseDuration(d): number {
  const r = moment.duration(d);
  if (r.asMinutes() > 60) {
    maxDurationUnit = "h";
  } else if (maxDurationUnit !== "h" && r.asSeconds() > 60) {
    maxDurationUnit = "min";
  } else if (maxDurationUnit !== "h" && maxDurationUnit !== "min" && r.asMilliseconds() > 1000) {
    maxDurationUnit = "s";
  }

  return r.asMilliseconds()
}

function displayDuration(d): string {
  const m = moment(d);
  switch (maxDurationUnit) {
    case "h" :
      return m.format("h[h]mm");
    case "min" :
      return m.format("m[min] s[s]");
    case "s" :
      return m.format("s[s] SSS[ms]");
    default :
      return m.format("s[s] SSS[ms]");
  }
}

@Component({
  selector: 'tock-test-builds',
  templateUrl: './test-builds.component.html',
  styleUrls: ['./test-builds.component.css']
})
export class TestBuildsComponent implements OnInit, OnDestroy {

  public errors: Array<any>;
  public durations: Array<any>;
  public sizes: Array<any>;
  public errorChartOptions: any = {
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
          unit: "hour",
          displayFormats: {
            hour: 'D/M H[h]'
          }
        }
      }]
    }
  };
  public durationChartOptions: any = {
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
  public sizeChartOptions: any = {
    scales: {
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
  public lineChartLegend: boolean = true;
  public lineChartType: string = 'line';
  public nodata: boolean = false;

  public intent: string = "";
  public modifiedAfter?: Date;

  private subscription: Subscription;

  constructor(public state: StateService, private quality: QualityService) {
    this.modifiedAfter = new Date();
    this.modifiedAfter.setMonth(this.modifiedAfter.getMonth() - 1);
  }

  ngOnInit(): void {
    this.search();
    this.subscription = this.state.configurationChange.subscribe(_ => this.search());
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  search(): void {
    console.log(this.modifiedAfter)
    this.quality.buildStats(
      TestErrorQuery.createWithoutSize(this.state, this.intent === "" ? undefined : this.intent, this.modifiedAfter)
    )
      .subscribe(result => {
        if (result.length === 0) {
          this.nodata = true;
          return;
        }
        this.nodata = false;
        maxDurationUnit = "ms";
        const errorData = result.map(p => {
          return {
            x: p.date,
            y: p.nbSentencesTested === 0 ? 0 : Math.round(10000 * (p.errors / p.nbSentencesTested)) / 100,
          };
        });
        const intentData = result.map(p => {
          return {
            x: p.date,
            y: p.nbSentencesTested === 0 ? 0 : Math.round(10000 * (p.intentErrors / p.nbSentencesTested)) / 100,
          };
        });
        const entityData = result.map(p => {
          return {
            x: p.date,
            y: p.nbSentencesTested === 0 ? 0 : Math.round(10000 * (p.entityErrors / p.nbSentencesTested)) / 100,
          };
        });
        this.errors = [
          {
            data: errorData,
            label: "Errors"
          },
          {
            data: intentData,
            label: "Intent Errors"
          },
          {
            data: entityData,
            label: "Entity Errors"
          }
        ];
        const modelData = result.map(p => {
          return {
            x: p.date,
            y: p.nbSentencesInModel,
          };
        });
        const testData = result.map(p => {
          return {
            x: p.date,
            y: p.nbSentencesTested,
          };
        });
        this.sizes = [
          {
            data: modelData,
            label: "Model size"
          },
          {
            data: testData,
            label: "Test set size"
          }
        ];
        const modelDurationData = result.map(p => {
          return {
            x: p.date,
            y: parseDuration(p.buildModelDuration),
          };
        });
        const testDurationData = result.map(p => {
          return {
            x: p.date,
            y: parseDuration(p.testModelDuration),
          };
        });
        this.durations = [
          {
            data: modelDurationData,
            label: "Build Model duration"
          },
          {
            data: testDurationData,
            label: "Test Model duration"
          }
        ]
      });
  }

}

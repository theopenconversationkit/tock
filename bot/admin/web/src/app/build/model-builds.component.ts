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
import { Component, OnInit } from '@angular/core';
import * as moment from 'moment';

import { ApplicationService } from '../core-nlp/applications.service';
import { StateService } from '../core-nlp/state.service';
import { ModelBuild } from '../model/application';
import { PaginatedQuery } from '../model/commons';


@Component({
  selector: 'tock-model-builds',
  templateUrl: './model-builds.component.html',
  styleUrls: ['./model-builds.component.css']
})
export class ModelBuildsComponent implements OnInit{

  dataSource: ModelBuild[] = [];
  totalSize: number;
  pageSize: number = 10;
  pageIndex: number = 1;
  loading: boolean = false;

  constructor(private state: StateService,
              private applicationService: ApplicationService) {

  }

  ngOnInit(): void {
    this.refresh();
  }

  duration(d: number): string {
    const duration = moment.duration(d, 's');
    return this.formatDuration(duration.get('hours')) + ":"
      + this.formatDuration(duration.get('minutes')) + ":"
      + this.formatDuration(duration.get('seconds'));
  }

  private formatDuration(d: number): string {
    return d <= 9 ? "0" + d : d.toString()
  }

  intentName(build: ModelBuild): string {
    if (build.intentId) {
      const i = this.state.findIntentById(build.intentId);
      return i ? i.intentLabel() : "unknown";
    } else {
      return "";
    }
  }

  intentOrEntityName(build: ModelBuild): string {
    const i = this.intentName(build);
    if (i) {
      return i;
    } else {
      const e = this.state.findEntityTypeByName(build.entityTypeName);
      return e ? e.simpleName() : "";
    }
  }

  getIndex(){
    if(this.pageIndex > 0) return this.pageIndex - 1;
    else return this.pageIndex
  }

  refresh() {
    this.loading = true;
    const startIndex = this.getIndex() * this.pageSize;
    this.applicationService.builds(
      new PaginatedQuery(
        this.state.currentApplication.namespace,
        this.state.currentApplication.name,
        this.state.currentLocale,
        startIndex,
        this.pageSize
      )
    ).subscribe(r => {
      this.loading = false;
      this.totalSize = r.total;
      this.dataSource = r.data;
    });
  }
}

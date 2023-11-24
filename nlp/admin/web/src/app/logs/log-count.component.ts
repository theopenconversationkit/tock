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
import {LogCount} from '../model/nlp';
import { StateService } from '../core-nlp/state.service';
import { QualityService } from '../quality-nlp/quality.service';
import {LogCountQuery} from "../model/nlp";

@Component({
  selector: 'tock-log-count',
  templateUrl: './log-count.component.html',
  styleUrls: ['./log-count.component.css']
})
export class LogCountComponent implements OnInit {
  dataSource: LogCount[] = [];

  intent: string = '';
  minCount: number = 1;

  totalSize: number;
  pageSize: number = 10;
  pageIndex: number = 1;
  loading: boolean = false;

  constructor(
    public state: StateService,
    private qualityService: QualityService,
  ) {}

  ngOnInit(): void {
    this.search();
  }

  getIndex() {
    if (this.pageIndex > 0) return this.pageIndex - 1;
    else return this.pageIndex;
  }
  search() {
    this.loading = true;
    const startIndex = this.getIndex() * this.pageSize;
    this.qualityService
      .countStats(LogCountQuery.create(this.state, startIndex, this.pageSize, this.intent === '' ? undefined : this.intent, this.minCount))
      .subscribe((r) => {
        this.loading = false;
        this.totalSize = r.total;
        this.dataSource = r.rows;
      });
  }

}

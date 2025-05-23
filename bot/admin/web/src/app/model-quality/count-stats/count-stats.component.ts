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
import { LogCount, LogCountQuery } from '../../model/nlp';
import { StateService } from '../../core-nlp/state.service';
import { QualityService } from '../quality.service';
import { Subject, debounceTime, takeUntil } from 'rxjs';
import { Pagination } from '../../shared/components';
import { FormControl, FormGroup } from '@angular/forms';

interface FilterForm {
  intent: FormControl<string>;
  minCount: FormControl<number>;
}

@Component({
  selector: 'tock-count-stats',
  templateUrl: './count-stats.component.html',
  styleUrls: ['./count-stats.component.scss']
})
export class CountStatsComponent implements OnInit, OnDestroy {
  destroy = new Subject();

  dataSource: LogCount[] = [];

  loading: boolean = false;

  pagination: Pagination = {
    start: 0,
    end: undefined,
    size: 10,
    total: undefined
  };

  constructor(public state: StateService, private qualityService: QualityService) {}

  ngOnInit(): void {
    this.form.valueChanges.pipe(debounceTime(500), takeUntil(this.destroy)).subscribe(() => {
      if (isNaN(parseInt(this.minCount.value))) {
        this.minCount.patchValue(1);
      }
      this.search();
    });

    this.state.configurationChange.pipe(takeUntil(this.destroy)).subscribe((_) => this.search());

    this.search();
  }

  form = new FormGroup<FilterForm>({
    intent: new FormControl(),
    minCount: new FormControl(1)
  });

  get intent(): FormControl {
    return this.form.get('intent') as FormControl;
  }

  get minCount(): FormControl {
    return this.form.get('minCount') as FormControl;
  }

  refresh(): void {
    this.search(this.pagination.start, this.pagination.size);
  }

  search(start: number = 0, size: number = this.pagination.size): void {
    this.loading = true;

    this.qualityService
      .countStats(
        LogCountQuery.create(this.state, start, size, this.intent.value === '' ? undefined : this.intent.value, this.minCount.value)
      )
      .subscribe((result) => {
        this.pagination.total = result.total;

        this.pagination.end = Math.min(start + this.pagination.size, this.pagination.total);

        this.dataSource = result.rows;
        this.pagination.start = start;

        this.loading = false;
      });
  }

  ngOnDestroy() {
    this.destroy.next(true);
    this.destroy.complete();
  }
}

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
import { IntentQA, LogStatsQuery } from '../../model/nlp';
import { Subject, debounceTime, takeUntil } from 'rxjs';
import { StateService } from '../../core-nlp/state.service';
import { QualityService } from '../quality.service';
import { FormControl, FormGroup } from '@angular/forms';

interface FilterForm {
  minOccurrences: FormControl<number>;
}

@Component({
  selector: 'tock-intent-quality',
  templateUrl: './intent-quality.component.html',
  styleUrls: ['./intent-quality.component.scss']
})
export class IntentQualityComponent implements OnInit, OnDestroy {
  destroy = new Subject();

  public dataSource: IntentQA[];

  loading: boolean = false;

  constructor(private state: StateService, private quality: QualityService) {}

  ngOnInit(): void {
    this.form.valueChanges.pipe(debounceTime(500), takeUntil(this.destroy)).subscribe(() => {
      if (isNaN(parseInt(this.minOccurrences.value))) {
        this.minOccurrences.patchValue(1);
      }
      this.search();
    });

    this.state.configurationChange.pipe(takeUntil(this.destroy)).subscribe((_) => this.search());

    this.search();
  }

  form = new FormGroup<FilterForm>({
    minOccurrences: new FormControl(30)
  });

  get minOccurrences(): FormControl {
    return this.form.get('minOccurrences') as FormControl;
  }

  search(): void {
    this.loading = true;
    this.quality
      .intentQA(
        new LogStatsQuery(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale,
          '',
          this.minOccurrences.value
        )
      )
      .subscribe((result) => {
        const r = result.map((p) => {
          return new IntentQA(this.state.intentLabelByName(p.intent1), this.state.intentLabelByName(p.intent2), p.occurrences, p.average);
        });
        this.dataSource = r;
        this.loading = false;
      });
  }

  ngOnDestroy() {
    this.destroy.next(true);
    this.destroy.complete();
  }
}

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

import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { debounceTime, Subject, takeUntil } from 'rxjs';

export interface IndicatorsFilter {
  search: string;
  dimensions: Array<string>;
}

interface IndicatorsFilterForm {
  search: FormControl<string>;
  dimensions: FormControl<string[]>;
}

@Component({
  selector: 'tock-indicators-filters',
  templateUrl: './indicators-filters.component.html',
  styleUrls: ['./indicators-filters.component.scss']
})
export class IndicatorsFiltersComponent implements OnInit, OnDestroy {
  destroy = new Subject();

  @Input() dimensionsCache: string[];

  @Output() onFilter = new EventEmitter<IndicatorsFilter>();

  form = new FormGroup<IndicatorsFilterForm>({
    search: new FormControl(),
    dimensions: new FormControl([])
  });

  get search(): FormControl {
    return this.form.get('search') as FormControl;
  }

  get dimensions(): FormControl {
    return this.form.get('dimensions') as FormControl;
  }

  get isFiltered(): boolean {
    return !!(this.search.value || this.dimensions.value?.length);
  }

  ngOnInit() {
    this.form.valueChanges.pipe(debounceTime(500), takeUntil(this.destroy)).subscribe(() => {
      this.onFilter.emit(this.form.value as IndicatorsFilter);
    });
  }

  clearFilters(): void {
    this.search.reset();
    this.dimensions.reset([]);
  }

  ngOnDestroy() {
    this.destroy.next(true);
    this.destroy.complete();
  }
}

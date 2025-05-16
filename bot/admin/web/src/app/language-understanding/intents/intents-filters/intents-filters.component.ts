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

import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { debounceTime, Subject, takeUntil } from 'rxjs';

interface IntentsFilterForm {
  search: FormControl<string>;
}

export interface IntentsFilter {
  search: string;
}

@Component({
  selector: 'tock-intents-filters',
  templateUrl: './intents-filters.component.html',
  styleUrls: ['./intents-filters.component.scss']
})
export class IntentsFiltersComponent implements OnInit, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  @Output() onFilter = new EventEmitter<IntentsFilter>();

  form = new FormGroup<IntentsFilterForm>({
    search: new FormControl()
  });

  get search(): FormControl {
    return this.form.get('search') as FormControl;
  }

  ngOnInit(): void {
    this.form.valueChanges.pipe(debounceTime(300), takeUntil(this.destroy$)).subscribe(() => {
      this.onFilter.emit(this.form.value as IntentsFilter);
    });
  }

  clearSearch(): void {
    this.search.reset();
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}

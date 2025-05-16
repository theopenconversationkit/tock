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

interface StoriesFiltersForm {
  search: FormControl<string>;
  categories: FormControl<string[]>;
  configuredStoriesOnly: FormControl<boolean>;
  sortStoriesByModificationDate: FormControl<boolean>;
}

export interface StoriesFilters {
  search?: string;
  categories?: string[];
  configuredStoriesOnly?: boolean;
  sortStoriesByModificationDate?: boolean;
}

@Component({
  selector: 'tock-stories-filter',
  templateUrl: './stories-filter.component.html',
  styleUrls: ['./stories-filter.component.scss']
})
export class StoriesFilterComponent implements OnInit, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  @Input() categories;
  @Output() onFilter = new EventEmitter<StoriesFilters>();
  @Output() onDownload = new EventEmitter();
  @Output() onPrepareUpload = new EventEmitter();

  form = new FormGroup<StoriesFiltersForm>({
    search: new FormControl(),
    categories: new FormControl([]),
    configuredStoriesOnly: new FormControl(true),
    sortStoriesByModificationDate: new FormControl(false)
  });

  get search(): FormControl {
    return this.form.get('search') as FormControl;
  }

  get configuredStoriesOnly(): FormControl {
    return this.form.get('configuredStoriesOnly') as FormControl;
  }

  get sortStoriesByModificationDate(): FormControl {
    return this.form.get('sortStoriesByModificationDate') as FormControl;
  }

  ngOnInit(): void {
    this.form.valueChanges.pipe(debounceTime(300), takeUntil(this.destroy$)).subscribe(() => {
      this.onFilter.emit(this.form.value as StoriesFilters);
    });
  }

  clearSearch(): void {
    this.search.reset();
  }

  download() {
    this.onDownload.emit();
  }

  prepareUpload() {
    this.onPrepareUpload.emit();
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}

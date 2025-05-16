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

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { Subject, debounceTime, takeUntil } from 'rxjs';
import { StateService } from '../../../core-nlp/state.service';
import { I18nCategoryFilterAll, I18nFilters, I18nLocaleFilters } from '../models';
import { I18nLabelStateQuery } from '../../model/i18n';

interface I18nFiltersForm {
  search: FormControl<string>;
  locale: FormControl<I18nLocaleFilters>;
  category: FormControl<string>;
  state: FormControl<I18nLabelStateQuery>;
  usage: FormControl<number>;
}

@Component({
  selector: 'tock-i18n-filters',
  templateUrl: './i18n-filters.component.html',
  styleUrls: ['./i18n-filters.component.scss']
})
export class I18nFiltersComponent implements OnInit {
  private readonly destroy$: Subject<boolean> = new Subject();

  @Input() categories: string[];

  notUsedFromPossibleValues: number[] = [-1, 1, 7, 30, 365];

  I18nLocaleFilters = I18nLocaleFilters;
  I18nLabelStateQuery = I18nLabelStateQuery;
  I18nCategoryFilterAll = I18nCategoryFilterAll;

  @Output() onSearch = new EventEmitter<I18nFilters>();
  @Output() onValidateAll = new EventEmitter();
  @Output() onTranslate = new EventEmitter();
  @Output() onImport = new EventEmitter();
  @Output() onExport = new EventEmitter();

  constructor(public state: StateService) {}

  ngOnInit(): void {
    this.form.valueChanges.pipe(debounceTime(250), takeUntil(this.destroy$)).subscribe(() => this.submitFiltersChange());
  }

  form = new FormGroup<I18nFiltersForm>({
    search: new FormControl(),
    locale: new FormControl(),
    category: new FormControl(),
    state: new FormControl(),
    usage: new FormControl()
  });

  getFormControl(formControlName: string): FormControl {
    return this.form.get(formControlName) as FormControl;
  }

  resetControl(ctrl: FormControl, input?: HTMLInputElement): void {
    ctrl.reset();
    if (input) {
      input.value = '';
    }
  }

  submitFiltersChange() {
    const form = this.form.value;
    const filters = {
      search: form.search,
      locale: form.locale || I18nLocaleFilters.ALL,
      category: form.category && form.category != I18nCategoryFilterAll ? form.category : undefined,
      state: form.state || I18nLabelStateQuery.ALL,
      usage: form.usage > 0 ? form.usage : undefined
    };

    this.onSearch.emit(filters);
  }

  notUsedFromLabel(possibleNumber: number): string {
    switch (possibleNumber) {
      case 1:
        return 'Not used since yesterday';
      case 7:
        return 'Not used since last week';
      case 30:
        return 'Not used since last month';
      case 365:
        return 'Not used since last year';
      case -1:
      default:
        return 'All';
    }
  }

  validateAll() {
    this.onValidateAll.emit();
  }

  translate() {
    this.onTranslate.emit();
  }

  import() {
    this.onImport.emit();
  }

  export() {
    this.onExport.emit();
  }
}

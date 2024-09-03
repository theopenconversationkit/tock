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

import { Component, Input, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { BotService } from '../../bot-service';
import { saveAs } from 'file-saver-es';
import { I18LabelQuery, I18nLabelStateQuery } from '../../model/i18n';
import { I18nCategoryFilterAll, I18nFilters } from '../models';
import { StateService } from '../../../core-nlp/state.service';
import { getExportFileName } from '../../../shared/utils';

interface ExportLabelsForm {
  filteredOnly: FormControl<string>;
  format: FormControl<string>;
}

@Component({
  selector: 'tock-i18n-export-action',
  templateUrl: './i18n-export.component.html',
  styleUrls: ['./i18n-export.component.scss']
})
export class I18nExportComponent implements OnInit {
  downloading = false;

  isSubmitted: boolean = false;

  @Input() i18nFilters: I18nFilters;

  constructor(private nbDialogRef: NbDialogRef<I18nExportComponent>, private botService: BotService, private state: StateService) {}

  ngOnInit(): void {
    if (
      this.i18nFilters.search?.trim().length ||
      (this.i18nFilters.category && this.i18nFilters.category !== I18nCategoryFilterAll) ||
      this.i18nFilters.state !== I18nLabelStateQuery.ALL ||
      (this.i18nFilters.usage && this.i18nFilters.usage > -1)
    ) {
      this.form.patchValue({ filteredOnly: 'filtered' });
    }
  }

  form = new FormGroup<ExportLabelsForm>({
    filteredOnly: new FormControl('all', [Validators.required]),
    format: new FormControl('json', [Validators.required])
  });

  get filteredOnly(): FormControl {
    return this.form.get('filteredOnly') as FormControl;
  }

  get format(): FormControl {
    return this.form.get('format') as FormControl;
  }

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  export(): void {
    this.isSubmitted = true;

    if (this.canSave) {
      this.downloading = true;

      const exportOption = this.filteredOnly.value !== 'all' ? 'Filtered' : 'All';
      const exportFileName = getExportFileName(
        this.state.currentApplication.namespace,
        this.state.currentApplication.name,
        'Answers',
        this.format.value,
        exportOption
      );

      let query;

      if (this.filteredOnly.value !== 'all') {
        query = new I18LabelQuery(
          this.computeLabelFilter(),
          this.computeCategoryFilterValue(),
          this.computeStatusFilterValue(),
          this.computeNotUsedSinceDaysFilterValue()
        );
      }

      if (this.format.value === 'json') {
        this.botService.downloadI18nLabelsJson(query).subscribe((blob) => {
          saveAs(blob, exportFileName);
          this.cancel();
        });
      }

      if (this.format.value === 'csv') {
        this.botService.downloadI18nLabelsCsv(query).subscribe((blob) => {
          saveAs(blob, exportFileName);
          this.cancel();
        });
      }
    }
  }

  computeLabelFilter(): string {
    return this.i18nFilters.search?.trim().length ? this.i18nFilters.search : undefined;
  }

  computeCategoryFilterValue(): string {
    return this.i18nFilters.category?.trim().length ? this.i18nFilters.category : undefined;
  }

  computeStatusFilterValue(): I18nLabelStateQuery {
    return this.i18nFilters.state;
  }

  computeNotUsedSinceDaysFilterValue(): number {
    return this.i18nFilters.usage ? this.i18nFilters.usage : undefined;
  }

  cancel(): void {
    this.nbDialogRef.close({});
  }
}

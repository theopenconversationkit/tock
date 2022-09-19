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
import { I18LabelQuery, I18nLabel, I18nLabelStateQuery } from '../model/i18n';
import { BotService } from '../bot-service';
import { StateService } from '../../core-nlp/state.service';
import { PageEvent } from '@angular/material/paginator';
import { saveAs } from 'file-saver-es';
import { FileUploader } from 'ng2-file-upload';
import { I18nController } from './i18n-label.component';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { NbToastrService, NbWindowRef, NbWindowService } from '@nebular/theme';
import { I18nExportComponent } from './i18n-export.component';
import { I18nImportComponent } from './i18n-import.component';

@Component({
  selector: 'tock-i18n',
  templateUrl: './i18n.component.html',
  styleUrls: ['./i18n.component.css']
})
export class I18nComponent extends I18nController implements OnInit {
  originalI18n: I18nLabel[];
  i18n: I18nLabel[];
  filteredI18n: I18nLabel[] = [];
  filterString = '';
  filterOption = '';
  filterLocale = 'supported';
  locales = new Set<string>();
  loading = false;
  private doNotFilterByCategory = 'All';
  selectedCategory: string = this.doNotFilterByCategory;
  allCategories: string[] = [];
  notUsedFrom = -1;
  notUsedFromPossibleValues: number[] = [-1, 1, 7, 30];

  pageEvent: PageEvent;
  pageSize = 5;
  pageSizeOptions = [5, 10, 25, 100];

  exportWindow: NbWindowRef;
  importWindow: NbWindowRef;

  private searchUpdated: Subject<string> = new Subject<string>();

  constructor(
    public state: StateService,
    private botService: BotService,
    private toastrService: NbToastrService,
    private windowService: NbWindowService
  ) {
    super(state, [], null);
  }

  ngOnInit() {
    this.load();
    this.state.currentApplicationEmitter.subscribe((_) => this.load());
    this.searchUpdated
      .asObservable()
      .pipe(debounceTime(200))
      .pipe(distinctUntilChanged())
      .subscribe((v) => this.filterImpl(v));
  }

  controller(): I18nController {
    return this;
  }

  pagedItems(): I18nLabel[] {
    const i = this.i18n;
    const e = this.pageEvent;
    if (i.length <= this.pageSize) {
      return i;
    } else if (e) {
      const start = e.pageIndex * e.pageSize;
      return this.filteredI18n.slice(start, Math.min(start + e.pageSize, i.length));
    } else {
      return this.filteredI18n.slice(0, this.pageSize);
    }
  }

  private load() {
    this.loading = true;
    this.botService.i18nLabels().subscribe((r) => {
      this.loading = false;
      this.localeBase = r.localeBase;
      this.originalI18n = r.labels;
      this.i18n = this.originalI18n;
      this.initCategories(this.i18n);

      this.i18n.sort((a, b) => {
        return a.category.localeCompare(b.category);
      });
      this.filterImpl(this.filterString);
      this.fillLabels(this.locales);
      this.filterByLocales();
    });
  }

  private filterByLocales() {
    this.i18n = this.i18n.map((l) => {
      l.i18n = l.i18n.filter((i) => this.locales.has(i.locale));
      return l;
    });
  }

  private setCategoryOnFirstItem(i18n: I18nLabel[]) {
    let category: string;
    i18n.forEach((i) => {
      if (category !== i.category) {
        category = i.category;
        i.firstCategory = true;
      } else {
        i.firstCategory = false;
      }
    });
  }

  private initCategories(i18n: I18nLabel[]) {
    this.allCategories = [this.doNotFilterByCategory];
    i18n.forEach((i) => {
      if (this.allCategories.indexOf(i.category) === -1) {
        this.allCategories.push(i.category);
      }
    });
    this.allCategories.sort();
  }

  deleteLabel(label: I18nLabel) {
    this.i18n.splice(this.i18n.indexOf(label), 1);
    this.filteredI18n.splice(this.filteredI18n.indexOf(label), 1);
  }

  filterChanged() {
    this.filterImpl(this.filterString);
    this.refresh();
  }

  filter(value: string) {
    this.searchUpdated.next(value);
  }

  isDeleteLabelAllowed() {
    const noLocaleFilter = this.filterLocale === '';
    const currentLocale = this.filterLocale === 'current';
    const supportedLocales = this.filterLocale === 'supported';
    return (
      noLocaleFilter ||
      supportedLocales ||
      (currentLocale && this.state.currentApplication.supportedLocales.length == 1)
    );
  }

  private filterImpl(value: string) {
    // Set locales filter (labels eventually filtered)
    const currentLocale = this.filterLocale === 'current';
    const supportedLocales = this.filterLocale === 'supported';
    const notSupportedLocales = this.filterLocale === 'not_supported';
    const filteredLocales = new Set<string>();
    if (currentLocale) {
      filteredLocales.add(this.state.currentLocale);
    } else if (supportedLocales) {
      this.state.currentApplication.supportedLocales.forEach((locale) => {
        filteredLocales.add(locale);
      });
    } else if (notSupportedLocales) {
      this.i18n.forEach((label) => {
        label.i18n.forEach((i18nLabel) => {
          if (!this.state.currentApplication.supportedLocales.includes(i18nLabel.locale)) {
            filteredLocales.add(i18nLabel.locale);
          }
        });
      });
    } else {
      this.i18n.forEach((l) => l.i18n.forEach((i) => filteredLocales.add(i.locale)));
    }

    // Filter labels
    const hideNotValidated = this.filterOption === 'validated';
    const hideValidated = this.filterOption === 'not_validated';
    const filterText = value ? value.trim().toLowerCase() : '';
    const notUsedFromDate = Date.now() - 1000 * 60 * 60 * 24 * this.notUsedFrom;
    this.filteredI18n = this.i18n.filter((label) => {
      return (
        (filteredLocales.size < 1 ||
          label.i18n.some((i18nLabel) => filteredLocales.has(i18nLabel.locale))) &&
        (!hideValidated ||
          label.i18n.some((i18nLabel) => !i18nLabel.validated && i18nLabel.label.length !== 0)) &&
        (!hideNotValidated || label.i18n.some((i18nLabel) => i18nLabel.validated)) &&
        (filterText.length === 0 ||
          (label.defaultLabel && label.defaultLabel.toLowerCase().indexOf(filterText) !== -1) ||
          label.i18n.some(
            (i18nLabel) =>
              i18nLabel.label.length !== 0 &&
              i18nLabel.label.toLowerCase().indexOf(filterText) !== -1
          )) &&
        (this.selectedCategory === this.doNotFilterByCategory ||
          label.category === this.selectedCategory) &&
        (this.notUsedFrom === -1 ||
          !label.lastUpdate ||
          label.lastUpdate.getTime() < notUsedFromDate)
      );
    });
    this.setCategoryOnFirstItem(this.filteredI18n);

    this.locales = filteredLocales;
    this.filterByLocales();
  }

  refresh() {
    this.load();
  }

  complete() {
    this.loading = true;
    this.botService.completeI18nLabels(this.i18n).subscribe((r) => {
      this.load();
      this.loading = false;
      const n = r.nbTranslations;
      if (n === 0) {
        this.toastrService.show(`No label translated`, 'UPDATE', { duration: 2000 });
      } else if (n === 1) {
        this.toastrService.show(`1 label translated`, 'UPDATE', { duration: 2000 });
      } else {
        this.toastrService.show(`${n} labels translated`, 'UPDATE', { duration: 2000 });
      }
    });
  }

  validateAll() {
    this.i18n.forEach((i) => {
      i.i18n.forEach((l) => {
        if (l.label && l.label.trim().length !== 0) {
          l.validated = true;
        }
      });
    });
    this.botService
      .saveI18nLabels(this.i18n)
      .subscribe((_) =>
        this.toastrService.show(`All labels validated`, 'Validate', { duration: 3000 })
      );
  }

  openExportWindow() {
    if (this.exportWindow) {
      this.exportWindow.close();
    }
    this.exportWindow = this.windowService.open(I18nExportComponent, {
      title: 'Export As',
      context: {
        exportAs: (type: string, all: boolean) => this.download(type, all)
      }
    });
  }

  openImportWindow() {
    if (this.importWindow) {
      this.importWindow.close();
    }
    this.importWindow = this.windowService.open(I18nImportComponent, {
      title: 'Import Labels from File',
      context: {
        importFrom: (type: string, uploader: FileUploader) => this.upload(type, uploader),
        refresh: () => this.refresh()
      }
    });
  }

  upload(type: string, uploader: FileUploader) {
    switch (type) {
      case 'CSV':
        this.botService.prepareI18nCsvDumpUploader(uploader);
        break;
      case 'JSON':
        this.botService.prepareI18nJsonDumpUploader(uploader);
        break;
    }
    uploader.uploadAll();
  }

  download(format: string, all: boolean) {
    switch (format) {
      case 'CSV':
        this.downloadCsv(all);
        break;
      case 'JSON':
        this.downloadJson(all);
        break;
    }
  }

  downloadCsv(all: boolean) {
    if (all) {
      this.botService.downloadAllI18nLabelsCsv().subscribe((blob) => {
        saveAs(blob, 'labels.csv');
        this.toastrService.show(`Export provided`, 'Export', { duration: 2000 });
      });
    } else {
      const query = new I18LabelQuery(
        this.computeLabelFilter(),
        this.computeCategoryFilterValue(),
        this.computeStatusFilterValue(),
        this.computeNotUsedSinceDaysFilterValue()
      );
      this.botService.downloadI18nLabelsCsv(query).subscribe((blob) => {
        saveAs(blob, 'labels' + query.toString() + '.csv');
        this.toastrService.show(`Export provided`, 'Export', { duration: 2000 });
      });
    }
  }

  downloadJson(all: boolean) {
    if (all) {
      this.botService.downloadAllI18nLabelsJson().subscribe((blob) => {
        saveAs(blob, 'labels.json');
        this.toastrService.show(`Export provided`, 'Export', { duration: 2000 });
      });
    } else {
      const query = new I18LabelQuery(
        this.computeLabelFilter(),
        this.computeCategoryFilterValue(),
        this.computeStatusFilterValue(),
        this.computeNotUsedSinceDaysFilterValue()
      );
      this.botService.downloadI18nLabelsJson(query).subscribe((blob) => {
        saveAs(blob, 'labels' + query.toString() + '.json');
        this.toastrService.show(`Export provided`, 'Export', { duration: 2000 });
      });
    }
  }

  private computeLabelFilter() {
    return this.filterString && this.filterString.trim().length > 0 ? this.filterString : undefined;
  }

  private computeCategoryFilterValue() {
    if (this.selectedCategory === this.doNotFilterByCategory) {
      return undefined;
    }
    if (this.selectedCategory && this.selectedCategory.trim().length > 0) {
      return this.selectedCategory;
    }
    return undefined;
  }

  private computeStatusFilterValue(): I18nLabelStateQuery {
    let statusFilter: I18nLabelStateQuery;
    switch (this.filterOption) {
      case 'validated':
        statusFilter = I18nLabelStateQuery.VALIDATED;
        break;
      case 'not_validated':
        statusFilter = I18nLabelStateQuery.NOT_VALIDATED;
        break;
      default:
        statusFilter = I18nLabelStateQuery.ALL;
        break;
    }
    return statusFilter;
  }

  private computeNotUsedSinceDaysFilterValue(): number {
    if (this.notUsedFrom && this.notUsedFrom > 0) {
      return this.notUsedFrom;
    }
    return undefined;
  }

  notUsedFromLabel(possibleNumber: number): string {
    switch (possibleNumber) {
      case 1:
        return 'Not used since yesterday';
      case 7:
        return 'Not used since last week';
      case 30:
        return 'Not used since last month';
      case -1:
      default:
        return 'All';
    }
  }
}

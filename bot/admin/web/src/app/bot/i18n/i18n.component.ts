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

import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { I18nLabel, I18nLabelStateQuery, I18nLocalizedLabel, userInterfaces } from '../model/i18n';
import { BotService } from '../bot-service';
import { StateService } from '../../core-nlp/state.service';
import { Subject } from 'rxjs';
import { take, takeUntil } from 'rxjs/operators';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { Pagination } from '../../shared/components';
import { I18nCategoryFilterAll, I18nFilters, I18nLocaleFilters } from './models';
import { I18nExportComponent } from './i18n-export/i18n-export.component';
import { I18nImportComponent } from './i18n-import/i18n-import.component';
import { DOCUMENT } from '@angular/common';

@Component({
  selector: 'tock-i18n',
  templateUrl: './i18n.component.html',
  styleUrls: ['./i18n.component.css']
})
export class I18nComponent implements OnInit, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  loading: boolean = false;

  pagination: Pagination = {
    start: 0,
    end: undefined,
    size: 5,
    total: undefined
  };

  i18nFilters: I18nFilters = {
    search: null,
    locale: I18nLocaleFilters.ALL,
    category: I18nCategoryFilterAll,
    state: I18nLabelStateQuery.ALL,
    usage: null
  };

  i18n: I18nLabel[];

  localeBase: string;

  filteredI18n: I18nLabel[] = [];

  allCategories: string[] = [];

  showEmptyLabels: boolean = true;

  constructor(
    public state: StateService,
    private botService: BotService,
    private toastrService: NbToastrService,
    private dialogService: NbDialogService,
    @Inject(DOCUMENT) private document: Document
  ) {}

  ngOnInit() {
    this.state.configurationChange.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.load();
    });

    this.load();
  }

  refresh() {
    this.load();
  }

  private load() {
    this.loading = true;

    this.botService.i18nLabels().subscribe((result) => {
      this.localeBase = result.localeBase;

      this.i18n = result.labels.sort((a, b) => {
        return a.category.localeCompare(b.category);
      });

      this.filterI18n();

      this.initCategories(this.i18n);

      this.fillMissingLabels();

      this.loading = false;
    });
  }

  fillMissingLabels() {
    const locales: Set<string> = new Set<string>(this.state.currentApplication.supportedLocales);

    this.i18n.forEach((i) => {
      //add non present i18n
      locales.forEach((locale) => {
        userInterfaces.forEach((userInterface) => {
          if (!i.label(locale, userInterface)) {
            i.i18n.push(new I18nLocalizedLabel(locale, userInterface, '', false, null, []));
          }
        });
      });

      i.i18n.sort((a, b) => {
        if (a.locale === b.locale) {
          const interfaceDiff = a.interfaceType - b.interfaceType;
          if (interfaceDiff === 0) {
            return a.connectorId === b.connectorId
              ? -1
              : a.connectorId === null || (b.connectorId !== null && b.connectorId < a.connectorId)
              ? 0
              : 1;
          } else {
            return a.interfaceType - b.interfaceType;
          }
        } else return a.locale === this.localeBase ? -1 : b.locale === this.localeBase ? 1 : b.locale < a.locale ? 1 : -1;
      });
    });
  }

  onSearch(i18nFilters: I18nFilters) {
    this.i18nFilters = i18nFilters;
    this.filterI18n();
  }

  filterI18n() {
    this.filteredI18n = this.i18n;

    const filteredLocales = this.getFilteredLocales();

    this.filteredI18n = this.filteredI18n.filter((label) => {
      return label.i18n.some((i18nLabel) => filteredLocales.includes(i18nLabel.locale));
    });

    if (this.i18nFilters.state === I18nLabelStateQuery.VALIDATED) {
      this.filteredI18n = this.filteredI18n.filter((label) => {
        return label.i18n.some((i18nLabel) => i18nLabel.validated);
      });
    }

    if (this.i18nFilters.state === I18nLabelStateQuery.NOT_VALIDATED) {
      this.filteredI18n = this.filteredI18n.filter((label) => {
        return label.i18n.some((i18nLabel) => !i18nLabel.validated && i18nLabel.label.length);
      });
    }

    if (this.i18nFilters.category && this.i18nFilters.category != I18nCategoryFilterAll) {
      this.filteredI18n = this.filteredI18n.filter((label) => {
        return label.category === this.i18nFilters.category;
      });
    }

    if (this.i18nFilters.usage > 0) {
      const notUsedFromDate = Date.now() - 1000 * 60 * 60 * 24 * this.i18nFilters.usage;
      this.filteredI18n = this.filteredI18n.filter((label) => {
        return label.lastUpdate && label.lastUpdate.getTime() < notUsedFromDate;
      });
    }

    if (this.i18nFilters.search) {
      const searchStr = this.i18nFilters.search.toLowerCase().trim();
      this.filteredI18n = this.filteredI18n.filter((label) => {
        return (
          label.defaultLabel?.toLowerCase().trim().includes(searchStr) ||
          label.i18n.some((i18nLabel) => i18nLabel.label.length && i18nLabel.label.toLowerCase().trim().includes(searchStr))
        );
      });
    }

    this.paginationChange();
  }

  getFilteredLocales() {
    if (this.i18nFilters.locale === I18nLocaleFilters.CURRENT) {
      return [this.state.currentLocale];
    }

    if (this.i18nFilters.locale === I18nLocaleFilters.SUPPORTED) {
      return this.state.currentApplication.supportedLocales;
    }

    const filteredLocales = new Set();

    if (this.i18nFilters.locale === I18nLocaleFilters.NOT_SUPPORTED) {
      this.i18n.forEach((label) => {
        label.i18n.forEach((i18nLabel) => {
          if (!this.state.currentApplication.supportedLocales.includes(i18nLabel.locale)) {
            filteredLocales.add(i18nLabel.locale);
          }
        });
      });
    }

    if (!this.i18nFilters.locale || this.i18nFilters.locale === I18nLocaleFilters.ALL) {
      this.i18n.forEach((label) =>
        label.i18n.forEach((i18nLabel) => {
          filteredLocales.add(i18nLabel.locale);
        })
      );
    }

    return [...filteredLocales];
  }

  paginationChange(): void {
    this.pagination.total = this.filteredI18n.length;
    this.pagination.end = Math.min(this.pagination.start + this.pagination.size, this.filteredI18n.length);

    this.scrollToTop();
  }

  scrollToTop(): void {
    const currentScroll = this.document.documentElement.scrollTop || this.document.body.scrollTop;
    if (currentScroll > 0) {
      window.requestAnimationFrame(this.scrollToTop.bind(this));
      window.scrollTo(0, currentScroll - currentScroll / 2);
    }
  }

  pagedItems(): I18nLabel[] {
    return this.filteredI18n.slice(this.pagination.start, Math.min(this.pagination.start + this.pagination.size, this.i18n.length));
  }

  private initCategories(i18n: I18nLabel[]) {
    const allCategories = new Set<string>();
    i18n.forEach((i) => {
      allCategories.add(i.category);
    });
    this.allCategories = [...allCategories].sort();
  }

  labelDeleted(label: I18nLabel) {
    this.i18n.splice(this.i18n.indexOf(label), 1);
    this.filteredI18n.splice(this.filteredI18n.indexOf(label), 1);
  }

  isDeleteLabelAllowed() {
    return (
      this.i18nFilters.locale === I18nLocaleFilters.ALL ||
      this.i18nFilters.locale === I18nLocaleFilters.SUPPORTED ||
      (this.i18nFilters.locale === I18nLocaleFilters.CURRENT && this.state.currentApplication.supportedLocales.length === 1)
    );
  }

  onValidateAll() {
    this.i18n.forEach((i) => {
      i.i18n.forEach((l) => {
        if (l.label && l.label.trim().length !== 0) {
          l.validated = true;
        }
      });
    });
    this.botService
      .saveI18nLabels(this.i18n)
      .subscribe((_) => this.toastrService.show(`All labels validated`, 'Validate', { duration: 3000 }));
  }

  onTranslate() {
    this.loading = true;
    this.botService.completeI18nLabels(this.i18n).subscribe({
      next: (r) => {
        this.load();
        this.loading = false;

        const n = r.nbTranslations;
        let mssg = `No label translated`;
        if (n > 0) {
          if (n === 1) {
            mssg = `1 label translated`;
          } else {
            mssg = `${n} labels translated`;
          }
        }

        this.toastrService.show(mssg, 'UPDATE', { duration: 2000 });
      },
      error: (error) => {
        if (error?.error?.errors && Array.isArray(error.error.errors)) {
          const mssg = [];
          error.error.errors.forEach((e) => {
            if (e.message) {
              mssg.push(e.message);
            }
          });
          if (mssg.length) {
            this.toastrService.danger(mssg.join(''), 'Translation failed', { duration: 5000 });
          }
        }
        this.loading = false;
      }
    });
  }

  onImport() {
    this.dialogService
      .open(I18nImportComponent)
      .componentRef.instance.onUploadComplete.pipe(take(1))
      .subscribe((res) => {
        this.refresh();
      });
  }

  onExport() {
    this.dialogService.open(I18nExportComponent, {
      context: {
        i18nFilters: this.i18nFilters
      }
    });
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

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}

/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Component, OnInit} from "@angular/core";
import {I18nLabel} from "../model/i18n";
import {BotService} from "../bot-service";
import {StateService} from "../../core-nlp/state.service";
import {MatSnackBar, PageEvent} from "@angular/material";
import {saveAs} from "file-saver";
import {FileItem, FileUploader, ParsedResponseHeaders} from "ng2-file-upload";
import {I18nController} from "./i18n-label.component";
import {Subject} from "rxjs";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";

@Component({
  selector: 'tock-i18n',
  templateUrl: './i18n.component.html',
  styleUrls: ['./i18n.component.css']
})
export class I18nComponent extends I18nController implements OnInit {

  originalI18n: I18nLabel[];
  i18n: I18nLabel[];
  filteredI18n: I18nLabel[] = [];
  filterString: string = "";
  filterOption: string = "";
  loading: boolean = false;
  private doNotFilterByCategory = "All";
  selectedCategory: string = this.doNotFilterByCategory;
  allCategories: string[] = [];
  notUsedFrom: number = -1;

  displayImportExport: boolean = false;
  displayUpload: boolean = false;
  uploadType: string = "Csv";
  public uploader: FileUploader;

  pageEvent: PageEvent;
  pageSize: number = 5;
  pageSizeOptions = [5, 10, 25, 100];

  private searchUpdated: Subject<string> = new Subject<string>();

  constructor(public state: StateService,
              private botService: BotService,
              private snackBar: MatSnackBar) {
    super(state, [], null);
  }

  ngOnInit() {
    this.load();
    this.uploader = new FileUploader({removeAfterUpload: true});
    this.uploader.onCompleteItem =
      (item: FileItem, response: string, status: number, headers: ParsedResponseHeaders) => {
        this.displayUpload = false;
        this.refresh();
      };
    this.state.currentApplicationEmitter.subscribe(_ => this.load());
    this.searchUpdated.asObservable().pipe(debounceTime(200)).pipe(distinctUntilChanged()).subscribe(v => this.filterImpl(v));
  }

  controller(): I18nController {
    return this
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
    this.botService.i18nLabels().subscribe(r => {
      this.loading = false;
      this.localeBase = r.localeBase;
      this.originalI18n = r.labels;
      this.filterBySupportedLocales();
      this.initCategories(this.i18n);
      this.fillLabels();

      this.i18n.sort((a, b) => {
          return a.category.localeCompare(b.category);
        }
      );
      this.filterImpl(this.filterString);
    });
  }

  private filterBySupportedLocales() {
    const supportedLocales = this.state.currentApplication.supportedLocales;
    this.i18n = this.originalI18n.map(l => {
        l.i18n = l.i18n.filter(i => supportedLocales.indexOf(i.locale) !== -1);
        return l;
      }
    );
  }

  private setCategoryOnFirstItem(i18n: I18nLabel[]) {
    let category: string;
    i18n.forEach(i => {
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
    i18n.forEach(i => {
      if (this.allCategories.indexOf(i.category) === -1) {
        this.allCategories.push(i.category);
      }
    });
    this.allCategories.sort()
  }

  deleteLabel(label: I18nLabel) {
    this.i18n.splice(this.i18n.indexOf(label), 1);
    this.filteredI18n.splice(this.filteredI18n.indexOf(label), 1);
  }

  onSelectedCategoryChange() {
    this.filterImpl(this.filterString);
  }

  onNotUsedFromChange() {
    this.filterImpl(this.filterString);
  }

  filterValidatedChange() {
    this.filterImpl(this.filterString);
  }

  filter(value: string) {
    this.searchUpdated.next(value);
  }

  private filterImpl(value: string) {
    const hideNotValidated = this.filterOption == "validated";
    const hideValidated = this.filterOption == "not_validated";
    const v = value ? value.trim().toLowerCase() : "";
    const notUsedFromDate = Date.now() - 1000 * 60 * 60 * 24 * this.notUsedFrom;
    this.filteredI18n = this.i18n.filter(i => {
      return (!hideValidated || i.i18n.some(label => !label.validated && label.label.length !== 0))
        && (!hideNotValidated || i.i18n.some(label => label.validated))
        && (v.length === 0
          || (i.defaultLabel && i.defaultLabel.toLowerCase().indexOf(v) !== -1)
          || i.i18n.some(label => label.label.length !== 0 && label.label.toLowerCase().indexOf(v) !== -1)
        )
        && (this.selectedCategory === this.doNotFilterByCategory || i.category === this.selectedCategory)
        && (this.notUsedFrom == -1 || !i.lastUpdate || i.lastUpdate.getTime() < notUsedFromDate)
    });
    this.setCategoryOnFirstItem(this.filteredI18n);
  }

  refresh() {
    this.load();
  }

  complete() {
    this.loading = true;
    this.botService.completeI18nLabels(this.i18n).subscribe(r => {
      this.load();
      this.loading = false;
      const n = r.nbTranslations;
      if (n === 0) {
        this.snackBar.open(`No label translated`, "UPDATE", {duration: 1000})
      } else if (n === 1) {
        this.snackBar.open(`1 label translated`, "UPDATE", {duration: 1000})
      } else {
        this.snackBar.open(`${n} labels translated`, "UPDATE", {duration: 1000})
      }
    });
  }

  validateAll() {
    this.i18n.forEach(i => {
      i.i18n.forEach(l => {
        if (l.label && l.label.trim().length !== 0) {
          l.validated = true;
        }
      });
    });
    this.botService
      .saveI18nLabels(this.i18n)
      .subscribe(_ => this.snackBar.open(`All labels validated`, "Validate", {duration: 3000}));
  }

  downloadCsv() {
    this.botService.downloadI18nLabelsCsv()
      .subscribe(blob => {
        saveAs(blob, "labels.csv");
        this.snackBar.open(`Export provided`, "Export", {duration: 1000});
      })
  }

  downloadJson() {
    this.botService.downloadI18nLabelsJson()
      .subscribe(blob => {
        saveAs(blob, "labels.json");
        this.snackBar.open(`Export provided`, "Export", {duration: 1000});
      })
  }

  prepareCsvUpload() {
    this.displayUpload = true;
    this.uploadType = 'Csv';
  }

  prepareJsonUpload() {
    this.displayUpload = true;
    this.uploadType = 'Json';
  }

  upload() {
    if (this.uploadType === 'Csv') {
      this.botService.prepareI18nCsvDumpUploader(this.uploader);
    } else {
      this.botService.prepareI18nJsonDumpUploader(this.uploader);
    }
    this.uploader.uploadAll()
  }

}

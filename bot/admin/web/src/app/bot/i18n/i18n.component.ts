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
import {StateService} from "tock-nlp-admin/src/app/core/state.service";
import {MdSnackBar, PageEvent} from "@angular/material";
import {saveAs} from "file-saver";
import {FileItem, FileUploader, ParsedResponseHeaders} from "ng2-file-upload";
import {I18nController} from "./i18n-label.component";

@Component({
  selector: 'tock-i18n',
  templateUrl: './i18n.component.html',
  styleUrls: ['./i18n.component.css']
})
export class I18nComponent extends I18nController implements OnInit {

  i18n: I18nLabel[];
  filteredI18n: I18nLabel[];
  filterString: string = "";
  filterOption: string = "";
  loading: boolean = false;
  private doNotFilterByCategory = "All";
  selectedCategory: string = this.doNotFilterByCategory;
  allCategories: string[] = [];

  displayImportExport: boolean = false;
  displayUpload: boolean = false;
  uploadType: string = "Csv";
  public uploader: FileUploader;

  pageEvent: PageEvent;
  pageSize: number = 5;
  pageSizeOptions = [5, 10, 25, 100];

  constructor(public state: StateService,
              private botService: BotService,
              private snackBar: MdSnackBar) {
    super(state, []);
  }

  ngOnInit() {
    this.load();
    this.uploader = new FileUploader({removeAfterUpload: true});
    this.uploader.onCompleteItem =
      (item: FileItem, response: string, status: number, headers: ParsedResponseHeaders) => {
        this.displayUpload = false;
        this.refresh();
      };
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
      this.i18n = r;
      this.initCategories(this.i18n);
      this.sortLabels();

      this.i18n.sort((a, b) => {
          return a.category.localeCompare(b.category);
        }
      );
      this.filter(this.filterString);
    });
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
    this.filter(this.filterString);
  }

  filterValidatedChange() {
    this.filter(this.filterString);
  }

  filter(value: string) {
    const hideNotValidated = this.filterOption == "validated";
    const hideValidated = this.filterOption == "not_validated";
    const v = value ? value.trim().toLowerCase() : "";
    this.filteredI18n = this.i18n.filter(i => {
      return (!hideValidated || i.i18n.some(label => !label.validated && label.label.length !== 0))
        && (!hideNotValidated || i.i18n.some(label => label.validated))
        && (v.length === 0 || i.i18n.some(label => label.label.length !== 0 && label.label.toLowerCase().indexOf(v) !== -1))
        && (this.selectedCategory === this.doNotFilterByCategory || i.category === this.selectedCategory)
    });

    this.setCategoryOnFirstItem(this.filteredI18n);
  }

  refresh() {
    this.load();
  }

  complete() {
    this.loading = true;
    this.botService.completeI18nLabels(this.i18n).subscribe(_ => {
      this.load();
      this.loading = false;
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
    if(this.uploadType === 'Csv') {
      this.botService.prepareI18nCsvDumpUploader(this.uploader);
    } else {
      this.botService.prepareI18nJsonDumpUploader(this.uploader);
    }
    this.uploader.uploadAll()
  }

}

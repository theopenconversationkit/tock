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
import {UserInterfaceType} from "../../core/model/configuration";
import {I18nLabel, I18nLocalizedLabel} from "../model/i18n";
import {BotService} from "../bot-service";
import {StateService} from "tock-nlp-admin/src/app/core/state.service";
import {MdSnackBar} from "@angular/material";
import {saveAs} from "file-saver";

@Component({
  selector: 'app-i18n',
  templateUrl: './i18n.component.html',
  styleUrls: ['./i18n.component.css']
})
export class I18nComponent implements OnInit {

  userInterfaces = [UserInterfaceType.textChat, UserInterfaceType.voiceAssistant];
  i18n: I18nLabel[];
  filteredI18n: I18nLabel[];
  filterString: string = "";
  filterValidated: boolean = false;
  loading: boolean = false;
  private doNotFilterByCategory = "All";
  selectedCategory: string = this.doNotFilterByCategory;
  allCategories: string[] = [];

  constructor(
    public state:StateService,
    private botService: BotService,
    private snackBar: MdSnackBar) {
  }

  ngOnInit() {
    this.load();
  }

  private load() {
    this.loading = true;
    this.botService.i18nLabels().subscribe(r => {
      this.loading = false;
      this.i18n = r;
      const locales = this.state.currentApplication.supportedLocales;
      this.initCategories(this.i18n);

      this.i18n.forEach(i => {
          //add non present i18n
        locales.forEach(locale => {
            this.userInterfaces.forEach(userInterface => {
              if (!i.label(locale, userInterface)) {
                i.i18n.push(new I18nLocalizedLabel(locale, userInterface, "", false, []));
              }
            })
          });
          i.i18n.sort((a, b) => {
              if (a.locale === b.locale)
                return a.interfaceType - b.interfaceType;
              else return b.locale.localeCompare(a.locale);
            }
          );
        }
      );

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

  deleteLabel(label:I18nLabel) {
    this.i18n.splice(this.i18n.indexOf(label), 1);
    this.filteredI18n.splice(this.filteredI18n.indexOf(label), 1);
    this.botService
      .deleteI18nLabel(label)
      .subscribe(_ => this.snackBar.open(`Label "${label.defaultLabel().label}" deleted`, "Delete", {duration: 3000}));
    }

  onSelectedCategoryChange() {
    this.filter(this.filterString);
    if (this.selectedCategory !== this.doNotFilterByCategory) {
      this.filteredI18n = this.filteredI18n.filter(i => i.category === this.selectedCategory)
    }
  }

  filterValidatedChange() {
    this.filter(this.filterString);
  }

  filter(value: string) {
    if (value) {
      const v = value.trim().toLowerCase();
      if (v.length !== 0) {
        this.filteredI18n = this.i18n.filter(i => {
            if (this.filterValidated && !i.i18n.some(label => !label.validated)) {
              return false;
            }
            const r = i.i18n.some(label => label.label.length != 0 && label.label.toLowerCase().indexOf(v) !== -1);
            return r;
          }
        );
        this.setCategoryOnFirstItem(this.filteredI18n);
        return;
      }

    }
    if (this.filterValidated) {
      this.filteredI18n = this.i18n.filter(i => {
        return i.i18n.some(label => !label.validated);
      });
    } else {
      this.filteredI18n = this.i18n;
    }
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

  addAlternative(i18n: I18nLabel, label: I18nLocalizedLabel, index: number, value: string) {
    label.alternatives[index] = value;
    this.save(i18n, label);
  }

  removeAlternative(i18n: I18nLabel, label: I18nLocalizedLabel, index: number) {
    label.alternatives.splice(index, 1);
    this.save(i18n, label);
  }

  addNewAlternative(label: I18nLocalizedLabel) {
    label.alternatives.push("");
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

  save(i18n: I18nLabel, label: I18nLocalizedLabel) {
    this.botService
      .saveI18nLabel(i18n)
      .subscribe(_ => this.snackBar.open(`Label validated`, "Validate", {duration: 3000}));
  }

  downloadExport() {
      this.botService.downloadI18nLabelsExport()
        .subscribe(blob => {
          saveAs(blob, "labels.csv");
          this.snackBar.open(`Export provided`, "Export", {duration: 1000});
        })
  }

}

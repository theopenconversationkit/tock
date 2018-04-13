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
import {StateService} from "tock-nlp-admin/src/app/core/state.service";
import {Component, Input, OnInit} from "@angular/core";
import {I18nLabel, I18nLocalizedLabel, userInterfaces} from "../model/i18n";
import {BotConfigurationService} from "../../core/bot-configuration.service";
import {BotService} from "../bot-service";
import {MdSnackBar} from "@angular/material";
import {ConnectorType, UserInterfaceType} from "../../core/model/configuration";
import {BotSharedService} from "../../shared/bot-shared.service";

@Component({
  selector: 'tock-i18n-label',
  templateUrl: './i18n-label.component.html',
  styleUrls: ['./i18n-label.component.css']
})
export class I18nLabelComponent implements OnInit {

  @Input()
  i: I18nLabel;

  @Input()
  deleteAllowed:boolean = true;

  @Input()
  i18nController: I18nController;

  @Input()
  intent:string;

  connectorTypes: ConnectorType[] = [];

  constructor(public state: StateService,
              public config: BotConfigurationService,
              private botService: BotService,
              private snackBar: MdSnackBar,
              private botSharedService: BotSharedService) {
  }

  ngOnInit(): void {
    this
      .botSharedService
      .getConnectorTypes()
      .subscribe(
        c => {
          this.connectorTypes = c;
        }
      );

    if (!this.i18nController) {
      this.i18nController = new I18nController(this.state, [this.i]);
      this.i18nController.sortLabels();
    }
  }

  deleteLabel(label: I18nLabel) {
    this.i18nController.deleteLabel(label);
    let l = "";
    if (label.i18n.length !== 0) {
      l = label.defaultLocalizedLabel().label;
      if (!l || l.trim().length === 0) {
        l = label._id;
      }
    }
    this.botService
      .deleteI18nLabel(label)
      .subscribe(_ => this.snackBar.open(`Label ${l} deleted`, "Delete", {duration: 3000}));
  }

  save(i18n: I18nLabel) {
    this.botService
      .saveI18nLabel(i18n)
      .subscribe(_ => this.snackBar.open(`Label updated`, "Update", {duration: 3000}));
  }

  removeLocalizedLabel(i18n: I18nLabel, label: I18nLocalizedLabel) {
    i18n.i18n.splice(i18n.i18n.indexOf(label), 1);
    if (i18n.i18n.length === 0) {
      this.deleteLabel(i18n);
    } else {
      this.save(i18n);
    }
  }

  addLocalizedLabelForConnector(i18n: I18nLabel, label: I18nLocalizedLabel, connectorId: string) {
    i18n.i18n.push(new I18nLocalizedLabel(label.locale, label.interfaceType, "", false, connectorId, []));
    this.save(i18n);
    this.i18nController.sortLabels();
  }

  addAlternative(i18n: I18nLabel, label: I18nLocalizedLabel, index: number, value: string) {
    label.alternatives[index] = value;
    this.save(i18n);
  }

  removeAlternative(i18n: I18nLabel, label: I18nLocalizedLabel, index: number) {
    label.alternatives.splice(index, 1);
    this.save(i18n);
  }

  addNewAlternative(label: I18nLocalizedLabel) {
    label.alternatives.push("");
  }
}

export class I18nController {

  constructor(public state: StateService, public i18n: I18nLabel[]) {
  }

  deleteLabel(label: I18nLabel) {
    //do nothing
  }

  sortLabels() {
    const locales = this.state.currentApplication.supportedLocales;
    this.i18n.forEach(i => {
        //add non present i18n
        locales.forEach(locale => {
          userInterfaces.forEach(userInterface => {
            if (!i.label(locale, userInterface)) {
              i.i18n.push(new I18nLocalizedLabel(locale, userInterface, "", false, null, []));
            }
          })
        });
        i.i18n.sort((a, b) => {
            if (a.locale === b.locale) {
              const interfaceDiff = a.interfaceType - b.interfaceType;
              if (interfaceDiff === 0) {
                return (a.connectorId === b.connectorId) ? 0 : (a.connectorId === null || (b.connectorId !== null && b.connectorId < a.connectorId)) ? 1 : -1;
              } else {
                return a.interfaceType - b.interfaceType;
              }
            }
            else return b.locale < a.locale ? 1 : -1;
          }
        );
      }
    );
  }
}

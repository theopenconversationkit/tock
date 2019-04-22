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


import {defaultUserInterfaceType, UserInterfaceType} from "../../core/model/configuration";
import {environment} from "../../../environments/environment";
import {isNullOrUndefined} from "util";

export const userInterfaces = [UserInterfaceType.textChat, UserInterfaceType.voiceAssistant];

export class I18nLabels {

  constructor(public labels: I18nLabel[],
              public localeBase: string) {

  }

  static fromJSON(json: any): I18nLabels {
    const value = Object.create(I18nLabels.prototype);
    const result = Object.assign(value, json, {
      labels: I18nLabel.fromJSONArray(json.labels)
    });

    return result;
  }
}

export class I18nLabel {

  firstCategory: boolean;

  constructor(public _id: string,
              public category: string,
              public i18n: I18nLocalizedLabel[],
              public defaultLabel?: string,
              public statCount?: number,
              public lastUpdate?: Date,
              public unhandledLocaleStats?: I18nLabelStat[],
              public version?: number) {
  }

  defaultLocalizedLabel(): I18nLocalizedLabel {
    const d = this.label(this.i18n[0].locale, defaultUserInterfaceType);
    return d ? d : this.i18n[0];
  }

  label(locale: string, userInterface: UserInterfaceType, connectorId?: string): I18nLocalizedLabel {
    const l = this.i18n.find(i => i.locale === locale && i.interfaceType === userInterface && i.connectorId === connectorId);
    return l ? l : this.i18n.find(i => i.locale === locale && i.interfaceType === userInterface);
  }

  hasLocalAndInterfaceWithASpecifiedConnector(locale: string, userInterface: UserInterfaceType): boolean {
    return this.i18n.some(i => i.locale === locale && i.interfaceType === userInterface && !isNullOrUndefined(i.connectorId))
  }

  hasLocaleAndInterfaceAndConnector(locale: string, userInterface: UserInterfaceType, connectorId?: string): boolean {
    return this.i18n.some(i => i.locale === locale && i.interfaceType === userInterface && i.connectorId === connectorId);
  }

  static fromJSON(json: any): I18nLabel {
    const value = Object.create(I18nLabel.prototype);
    const result = Object.assign(value, json, {
      i18n: I18nLocalizedLabel.fromJSONArray(json.i18n),
      unhandledLocaleStats: I18nLabelStat.fromJSONArray(json.unhandledLocaleStats),
      lastUpdate: json.lastUpdate ? new Date(Date.parse(json.lastUpdate)) : null
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): I18nLabel[] {
    return json ? json.map(I18nLabel.fromJSON) : [];
  }
}

export class I18nLocalizedLabel {

  constructor(public locale: string,
              public interfaceType: UserInterfaceType,
              public label: string,
              public validated: boolean,
              public connectorId: string,
              public alternatives: string[],
              public stats?: I18nLabelStat[]) {

  }

  iconUrl(): string {
    return environment.serverUrl + "/connectorIcon/" + this.connectorId + "/icon.svg";
  }

  displayStats(): string {
    return this.stats && this.stats.length !== 0 ? this.stats.map(s => s.display()).join(",") : "";
  }

  mergedStats(): I18nLabelStat {
    if (this.stats && this.stats.length > 0) {
      if (this.stats.length === 1) {
        return this.stats[0]
      }
      let sum = this.stats[0].count;
      let lastUpdate = this.stats[0].lastUpdate;
      for (let i = 1; i < this.stats.length; i++) {
        sum += this.stats[i].count;
        lastUpdate = lastUpdate.getTime() > this.stats[i].lastUpdate.getTime() ? this.stats[i].lastUpdate : lastUpdate;
      }
      return new I18nLabelStat(this.locale, this.interfaceType, this.connectorId, sum, lastUpdate);
    } else {
      return null;
    }
  }

  static fromJSON(json: any): I18nLocalizedLabel {
    const value = Object.create(I18nLocalizedLabel.prototype);
    const result = Object.assign(value, json, {
      interfaceType: UserInterfaceType[json.interfaceType],
      stats: I18nLabelStat.fromJSONArray(json.stats)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): I18nLocalizedLabel[] {
    return json ? json.map(I18nLocalizedLabel.fromJSON) : [];
  }
}

export class I18nLabelStat {

  constructor(public locale: string,
              public interfaceType: UserInterfaceType,
              public connectorId: string,
              public count: number,
              public lastUpdate: Date) {
  }

  display(): string {
    return `[${this.locale}-${UserInterfaceType[this.interfaceType]}-${this.connectorId ? this.connectorId : ''}:${this.count}-${this.lastUpdate}]`
  }

  static fromJSON(json: any): I18nLabelStat {
    const value = Object.create(I18nLabelStat.prototype);
    const result = Object.assign(value, json, {
      interfaceType: UserInterfaceType[json.interfaceType],
      lastUpdate: json.lastUpdate ? new Date(Date.parse(json.lastUpdate)) : null
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): I18nLabelStat[] {
    return json ? json.map(I18nLabelStat.fromJSON) : [];
  }
}

export class CreateI18nLabelRequest {

  constructor(public category: string,
              public label:string,
              public locale:string) {
  }
}

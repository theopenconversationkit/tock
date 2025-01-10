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

import { EntityDefinition, Intent, NlpEngineType, Sentence } from './nlp';
import { flatMap, JsonUtils } from './commons';

export class Application {
  constructor(
    public name: string,
    public label: string,
    public namespace: string,
    public intents: Intent[],
    public supportedLocales: string[],
    public nlpEngineType: NlpEngineType,
    public mergeEngineTypes: boolean,
    public useEntityModels: boolean,
    public supportSubEntities: boolean,
    public unknownIntentThreshold: number,
    public knownIntentThreshold: number,
    public normalizeText: boolean,
    public namespaceIntents: Intent[],
    public _id?: string
  ) {}

  clone(): Application {
    return new Application(
      this.name,
      this.label,
      this.namespace,
      this.intents.slice(0),
      this.supportedLocales.slice(0),
      this.nlpEngineType,
      this.mergeEngineTypes,
      this.useEntityModels,
      this.supportSubEntities,
      this.unknownIntentThreshold,
      this.knownIntentThreshold,
      this.normalizeText,
      this.namespaceIntents,
      this._id
    );
  }

  isOtherNamespaceIntent(intent: Intent): boolean {
    return this.namespaceIntents.findIndex((i) => i.namespace === intent.namespace && i.name === intent.name) !== -1;
  }

  removeIntentById(id: string) {
    this.intents.forEach((e, i) => {
      if (e._id === id) {
        this.intents.splice(i, 1);
        return;
      }
    });
  }

  removeIntentByNamespaceAndName(namespace: string, name: string) {
    this.intents.forEach((e, i) => {
      if (e.namespace === namespace && e.name === name) {
        this.intents.splice(i, 1);
        return;
      }
    });
  }

  intentById(id: string): Intent {
    return this.intents.find((i) => i._id === id);
  }

  supportLocale(locale: string): boolean {
    return this.supportedLocales.some((l) => l === locale);
  }

  allEntities(): EntityDefinition[] {
    const alreadySeen = new Set();
    return flatMap(this.intents, (i) => i.entities)
      .filter((v) => {
        const value = v.qualifiedRole;
        if (alreadySeen.has(value)) {
          return false;
        } else {
          alreadySeen.add(value);
          return true;
        }
      })
      .sort((a, b) => {
        const c = a.entityTypeName.localeCompare(b.entityTypeName);
        if (c === 0 && a.role !== b.role) {
          const entityName = a.simpleEntityName();
          if (a.role === entityName) {
            return -1;
          } else if (b.role === entityName) {
            return 1;
          } else {
            return a.role.localeCompare(b.role);
          }
        } else {
          return c;
        }
      });
  }

  static fromJSON(json: any): Application {
    const value = Object.create(Application.prototype);
    const result = Object.assign(value, json, {
      intents: Intent.fromJSONArray(json.intents),
      nlpEngineType: NlpEngineType.fromJSON(json.nlpEngineType),
      label: json.label || json.name,
      namespaceIntents: Intent.fromJSONArray(json.namespaceIntents)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): Application[] {
    return json ? json.map(Application.fromJSON) : [];
  }
}

export class ApplicationImportConfiguration {
  constructor(public newApplicationName?: string) {}
}

export class ImportReport {
  constructor(
    public applicationsImported: string[],
    public entitiesImported: string[],
    public intentsImported: string[],
    public sentencesImported: number,
    public faqsImported: number,
    public success: boolean,
    public modified: boolean,
    public errorMessages: string[]
  ) {}

  static fromJSON(json: any): ImportReport {
    const value = Object.create(ImportReport.prototype);
    const result = Object.assign(value, json, {});
    return result;
  }
}

export class ModelBuild {
  constructor(
    public applicationId: string,
    public language: string,
    public type: string,
    public nbSentences: number,
    public duration: string,
    public error: boolean,
    public date: Date,
    public errorMessage?: string,
    public intentId?: string,
    public entityTypeName?: string
  ) {}

  static fromJSON(json: any): ModelBuild {
    const value = Object.create(ModelBuild.prototype);
    const result = Object.assign(value, json, {});
    return result;
  }

  static fromJSONArray(json?: Array<any>): ModelBuild[] {
    return json ? json.map(ModelBuild.fromJSON) : [];
  }
}

export class ModelBuildQueryResult {
  constructor(public total: number, public data: ModelBuild[]) {}

  static fromJSON(json: any): ModelBuildQueryResult {
    const value = Object.create(ModelBuildQueryResult.prototype);
    const result = Object.assign(value, json, {
      data: ModelBuild.fromJSONArray(json.data)
    });
    return result;
  }
}

export class NlpApplicationConfiguration {
  constructor(
    public tokenizerConfiguration: NlpModelConfiguration,
    public intentConfiguration: NlpModelConfiguration,
    public entityConfiguration: NlpModelConfiguration
  ) {}

  static fromJSON(json: any): NlpApplicationConfiguration {
    const value = Object.create(NlpApplicationConfiguration.prototype);
    const result = Object.assign(value, json, {
      tokenizerConfiguration: NlpModelConfiguration.fromJSON(json.tokenizerConfiguration),
      intentConfiguration: NlpModelConfiguration.fromJSON(json.intentConfiguration),
      entityConfiguration: NlpModelConfiguration.fromJSON(json.entityConfiguration)
    });
    return result;
  }
}

export class NlpModelConfiguration {
  constructor(public properties: Map<string, string>) {}

  toProperties(): string {
    let s = '#properties';
    this.properties.forEach((value, key) => (s = s + '\n' + key + '=' + value));
    return s;
  }

  toJSON() {
    return { properties: JsonUtils.mapToObject(this.properties) };
  }

  static parseProperties(properties: string): NlpModelConfiguration {
    const r = new Map();
    properties.match(/[^\r\n]+/g).forEach((line) => {
      let l = line.trim();
      if (!l.startsWith('#') && l.length !== 0) {
        let i = l.indexOf('=');
        if (i !== -1) {
          r.set(l.substring(0, i).trim(), i === l.length - 1 ? '' : l.substring(i + 1, l.length).trim());
        }
      }
    });
    return new NlpModelConfiguration(r);
  }

  static fromJSON(json: any): NlpModelConfiguration {
    const value = Object.create(NlpModelConfiguration.prototype);
    const result = Object.assign(value, json, {
      properties: JsonUtils.jsonToMap(json.properties)
    });
    return result;
  }
}

export class UserLog {
  constructor(
    public namespace: string,
    public applicationId: string,
    public login: string,
    public actionType: string,
    public newData: any,
    public date: Date,
    public error: boolean
  ) {}

  data(): string {
    return this.newData ? JSON.stringify(this.newData, null, 2) : '';
  }

  static fromJSON(json?: any): UserLog {
    const value = Object.create(UserLog.prototype);

    const result = Object.assign(value, json, {
      sentence: json.sentence ? Sentence.fromJSON(json.sentence) : null
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): UserLog[] {
    return json ? json.map(UserLog.fromJSON) : [];
  }
}

export class UserLogQueryResult {
  constructor(public total: number, public logs: UserLog[]) {}

  static fromJSON(json: any): UserLogQueryResult {
    const value = Object.create(UserLogQueryResult.prototype);
    const result = Object.assign(value, json, {
      logs: UserLog.fromJSONArray(json.logs)
    });
    return result;
  }
}

export class UserNamespace {
  constructor(public namespace: string, public login: string, public owner: boolean, public current: boolean) {}

  static fromJSON(json?: any): UserNamespace {
    const value = Object.create(UserNamespace.prototype);

    const result = Object.assign(value, json, {});

    return result;
  }

  static fromJSONArray(json?: Array<any>): UserNamespace[] {
    return json ? json.map(UserNamespace.fromJSON) : [];
  }
}

export class NamespaceConfiguration {
  constructor(
    public namespace: string,
    public defaultSharingConfiguration: NamespaceSharingConfiguration,
    public namespaceImportConfiguration: Map<string, NamespaceSharingConfiguration>
  ) {}

  static fromJSON(json?: any): NamespaceConfiguration {
    if (!json || !json.namespace) {
      return null;
    }
    const value = Object.create(NamespaceConfiguration.prototype);

    const result = Object.assign(value, json, {
      namespace: json.namespace,
      defaultSharingConfiguration: NamespaceSharingConfiguration.fromJSON(json.defaultSharingConfiguration),
      namespaceImportConfiguration: JsonUtils.jsonToMap(json.namespaceImportConfiguration)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): NamespaceConfiguration[] {
    return json ? json.map(NamespaceConfiguration.fromJSON) : [];
  }
}

export class NamespaceSharingConfiguration {
  constructor(public model: boolean = false, public stories: boolean = false) {}

  static fromJSON(json?: any): NamespaceSharingConfiguration {
    const value = Object.create(NamespaceSharingConfiguration.prototype);

    const result = Object.assign(value, json, {});

    return result;
  }
}

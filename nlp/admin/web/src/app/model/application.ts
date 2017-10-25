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

import {EntityDefinition, NlpEngineType} from "./nlp";
import {flatMap} from "./commons";

export class Application {

  constructor(public name: string,
              public namespace: string,
              public intents: Intent[],
              public supportedLocales: string[],
              public nlpEngineType: NlpEngineType,
              public mergeEngineTypes: boolean,
              public supportSubEntities: boolean,
              public _id?: string) {
  }

  clone(): Application {
    return new Application(
      this.name,
      this.namespace,
      this.intents.slice(0),
      this.supportedLocales.slice(0),
      this.nlpEngineType,
      this.mergeEngineTypes,
      this.supportSubEntities,
      this._id)
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
    return this.intents.find(i => i._id === id);
  }

  supportLocale(locale: string): boolean {
    return this.supportedLocales.some(l => l === locale);
  }

  allEntities(): EntityDefinition[] {
    const alreadySeen = new Set();
    return flatMap(this.intents, (i => i.entities))
      .filter(v => {
        const value = v.qualifiedRole;
        if (alreadySeen.has(value)) {
          return false
        } else {
          alreadySeen.add(value);
          return true
        }
      })
      .sort((a, b) => {
        const c = a.entityTypeName.localeCompare(b.entityTypeName);
        if (c === 0) {
          return a.role.localeCompare(b.role);
        } else {
          return c;
        }
      })
  }

  static fromJSON(json: any): Application {
    const value = Object.create(Application.prototype);
    const result = Object.assign(value, json, {
      intents: Intent.fromJSONArray(json.intents),
      nlpEngineType: NlpEngineType.fromJSON(json.nlpEngineType)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): Application[] {
    return json ? json.map(Application.fromJSON) : [];
  }
}

export class Intent {

  public static unknown = "tock:unknown";

  constructor(public name: string,
              public namespace: string,
              public entities: EntityDefinition[],
              public applications: String[],
              public mandatoryStates: String[],
              public _id?: string) {
    EntityDefinition.sortEntities(entities);
  }

  qualifiedName(): string {
    return `${this.namespace}:${this.name}`;
  }

  isUnknownIntent(): boolean {
    return this.qualifiedName() === Intent.unknown;
  }

  containsEntity(name: string, role: string): boolean {
    return this.entities.some(e => e.entityTypeName === name && e.role === role)
  }

  containsEntityRole(role: string): boolean {
    return this.entities.some(e => e.role === role)
  }

  removeEntity(entity: EntityDefinition) {
    this.entities = this.entities.filter(e => e.entityTypeName !== entity.entityTypeName || e.role !== entity.role)
  }

  addEntity(entity: EntityDefinition) {
    if (!this.entities.some(e => e.entityTypeName === entity.entityTypeName && e.role === entity.role)) {
      this.entities.push(entity);
      EntityDefinition.sortEntities(this.entities);
    }
  }

  static fromJSON(json: any): Intent {
    const value = Object.create(Intent.prototype);
    const result = Object.assign(value, json, {
      entities: EntityDefinition.sortEntities(EntityDefinition.fromJSONArray(json.entities)),
    });


    return result;
  }

  static fromJSONArray(json?: Array<any>): Intent[] {
    return json ? json.map(Intent.fromJSON) : [];
  }
}

export class ApplicationImportConfiguration {

  constructor(public newApplicationName?: string) {

  }
}

export class ImportReport {
  constructor(public applicationsImported: string[],
              public entitiesImported: string[],
              public intentsImported: string[],
              public sentencesImported: number,
              public success: boolean,
              public modified: boolean,
              public errorMessages: string[]) {
  }

  static fromJSON(json: any): ImportReport {
    const value = Object.create(ImportReport.prototype);
    const result = Object.assign(value, json, {});
    return result;
  }
}

export class ModelBuild {

  constructor(public applicationId: string,
              public language: string,
              public type: string,
              public nbSentences: number,
              public duration: Date,
              public error: boolean,
              public date: Date,
              public errorMessage?: string,
              public intentId?: string) {
  }

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

  constructor(public total: number,
              public data: ModelBuild[]) {
  }

  static fromJSON(json: any): ModelBuildQueryResult {
    const value = Object.create(ModelBuildQueryResult.prototype);
    const result = Object.assign(value, json, {
      data: ModelBuild.fromJSONArray(json.data)
    });
    return result;
  }

}

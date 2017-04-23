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

import {ApplicationScopedQuery, PaginatedQuery} from "./commons";
import {User} from "./auth";

export class EntityDefinition {

  qualifiedRole: string;
  entityColor: string;

  constructor(public entityTypeName: string,
              public role: string) {
    this.qualifiedRole = qualifiedRole(entityTypeName, role);
    this.entityColor = entityColor(this.qualifiedRole)
  }

  qualifiedName(user: User): string {
    return qualifiedName(user, this.entityTypeName, this.role);
  }

  static fromJSON(json?: any): EntityDefinition {

    const value = Object.create(EntityDefinition.prototype);

    const qualified = qualifiedRole(json.entityTypeName, json.role);

    const result = Object.assign(value, json, {
      qualifiedRole: qualified,
      entityColor: entityColor(qualified)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): EntityDefinition[] {
    return json ? json.map(EntityDefinition.fromJSON) : [];
  }
}

export class EntityType {

  constructor(public name: string, public description: string) {
  }

  qualifiedName(user: User): string {
    return qualifiedNameWithoutRole(user, this.name);
  }

  simpleName(): string {
    return entityNameFromQualifiedName(this.name);
  }

  entityColor(): string {
    return entityColor(this.name);
  }

  static fromJSON(json?: any): EntityType {

    const value = Object.create(EntityType.prototype);

    const result = Object.assign(value, json, {});

    return result;
  }

  static fromJSONArray(json?: Array<any>): EntityType[] {
    return json ? json.map(EntityType.fromJSON) : [];
  }
}

export class Sentence {

  constructor(public text: string,
              public language: string,
              public applicationId: string,
              public status: SentenceStatus,
              public classification: Classification,
              public creationDate: Date,
              public updateDate: Date) {
  }

  clone(): Sentence {
    return new Sentence(this.text, this.language, this.applicationId, this.status, this.classification, this.creationDate, this.updateDate);
  }

  entityValue(entity: ClassifiedEntity): string {
    if (entity.value) {
      return JSON.stringify(entity.value);
    } else {
      return this.entityText(entity);
    }
  }

  entityText(entity: ClassifiedEntity): string {
    return this.text.substring(entity.start, entity.end);
  }

  overlapEntity(start: number, end: number): boolean {
    for (const e of this.classification.entities) {
      if (!(e.end <= start || e.start >= end)) {
        return true;
      }
    }
    return false;
  }

  static fromJSON(json?: any): Sentence {
    const value = Object.create(Sentence.prototype);

    let v;
    if (json.value) {
      switch (json.value.type) {
        case "DateEntityValue" :
          v = DateEntityValue.fromJSON(json.value);
          break;
        case "DateIntervalEntityValue" :
          v = DateIntervalEntityValue.fromJSON(json.value);
          break;
        default:
          v = json.value
      }
    }

    const result = Object.assign(value, json, {
      status: SentenceStatus[json.status],
      classification: Classification.fromJSON(json.classification),
      value: v
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): Sentence[] {
    return json ? json.map(Sentence.fromJSON) : [];
  }
}

export class DateEntityValue {
  constructor(public date: Date, public grain: string) {

  }

  static fromJSON(json?: any): DateEntityValue {
    const value = Object.create(DateEntityValue.prototype);
    const result = Object.assign(value, json, {});

    return result;
  }
}

export class DateIntervalEntityValue {
  constructor(public date: DateEntityValue, public toDate: DateEntityValue) {

  }

  static fromJSON(json?: any): DateIntervalEntityValue {
    const value = Object.create(DateIntervalEntityValue.prototype);
    const result = Object.assign(value, json, {
      date: DateEntityValue.fromJSON(json.date),
      toDate: DateEntityValue.fromJSON(json.toDate)
    });

    return result;
  }
}

export class Classification {

  constructor(public intentId: string,
              public entities: ClassifiedEntity[],
              public intentProbability: number,
              public entitiesProbability: number) {
  }

  clone(): Classification {
    return new Classification(this.intentId, this.entities.slice(0), this.intentProbability, this.entitiesProbability);
  }

  static fromJSON(json?: any): Classification {
    const value = Object.create(Classification.prototype);
    const result = Object.assign(value, json, {
      entities: ClassifiedEntity.fromJSONArray(json.entities)
    });

    return result;
  }

}

export class ClassifiedEntity {

  qualifiedRole: string;
  entityColor: string;

  constructor(public type: string,
              public role: string,
              public start: number,
              public end: number,
              public value?: any) {
    this.qualifiedRole = qualifiedRole(type, role);
    this.entityColor = entityColor(this.qualifiedRole)
  }

  qualifiedName(user: User): string {
    return qualifiedName(user, this.type, this.role);
  }

  static sort(entityA: ClassifiedEntity, entityB: ClassifiedEntity): number {
    if (entityA.start < entityB.start) {
      return -1
    }
    else if (entityA.start > entityB.start) {
      return 1;
    }
    return 0;
  }

  static fromJSON(json?: any): ClassifiedEntity {
    const value = Object.create(ClassifiedEntity.prototype);

    const qualified = qualifiedRole(json.type, json.role);

    const result = Object.assign(value, json, {
      qualifiedRole: qualified,
      entityColor: entityColor(qualified)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): ClassifiedEntity[] {
    return json ? json.map(ClassifiedEntity.fromJSON) : [];
  }

}

export enum SentenceStatus {
  inbox, validated, model, deleted
}

export class NlpEngineType {
  constructor(public name: string) {
  }

  static fromJSON(json: any): NlpEngineType {
    const value = Object.create(NlpEngineType.prototype);
    const result = Object.assign(value, json, {});

    return result;
  }

  static fromJSONArray(json?: Array<any>): NlpEngineType[] {
    return json ? json.map(NlpEngineType.fromJSON) : [];
  }
}


export class ParseQuery extends ApplicationScopedQuery {

  constructor(public namespace: string,
              public applicationName: string,
              public language: string,
              public query: string,
              public engineType: NlpEngineType) {
    super(namespace, applicationName, language)
  }
}

export class SearchQuery extends PaginatedQuery {

  constructor(public namespace: string,
              public applicationName: string,
              public language: string,
              public start: number,
              public size: number,
              public search?: string,
              public intentId?: string,
              public status?: SentenceStatus[]) {
    super(namespace, applicationName, language, start, size)
  }
}

export class SentencesResult {

  constructor(public sentences: Sentence[],
              public total: number,
              public start: number,
              public end: number) {
  }

  static fromJSON(json?: any): SentencesResult {
    const value = Object.create(SentencesResult.prototype);

    const result = Object.assign(value, json, {
      sentences: Sentence.fromJSONArray(json.sentences),
    });

    return result;
  }
}


function hashCode(str: string): number {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash);
  }
  return hash;
}

function intToRGB(i: number): string {
  const c = (i & 0x00FFFFFF)
    .toString(16)
    .toUpperCase();

  return "00000".substring(0, 6 - c.length) + c;
}

function increaseBrightness(initialHex: string, percent: number): string {
  // strip the leading # if it's there
  let hex = initialHex.replace(/^\s*#|\s*$/g, '');

  // convert 3 char codes --> 6, e.g. `E0F` --> `EE00FF`
  if (hex.length == 3) {
    hex = hex.replace(/(.)/g, '$1$1');
  }

  const
    r = parseInt(hex.substr(0, 2), 16),
    g = parseInt(hex.substr(2, 2), 16),
    b = parseInt(hex.substr(4, 2), 16);

  return '#' +
    ((0 | (1 << 8) + r + (256 - r) * percent / 100).toString(16)).substr(1) +
    ((0 | (1 << 8) + g + (256 - g) * percent / 100).toString(16)).substr(1) +
    ((0 | (1 << 8) + b + (256 - b) * percent / 100).toString(16)).substr(1);
}

export function entityColor(str: string): string {
  return increaseBrightness(intToRGB(hashCode(str)), 50);
}

export function qualifiedRole(type: string, role: string): string {
  const split = type.split(":");
  if (role === split[1]) {
    return role;
  } else if (role.length == 0) {
    return split[1];
  } else {
    return `${split[1]}:${role}`;
  }
}

export function qualifiedName(user: User, type: string, role: string): string {
  const split = type.split(":");
  if (split[0] !== user.organization) {
    return `[${split[0]}]${qualifiedRole(type, role)}`;
  } else {
    return qualifiedRole(type, role);
  }
}

export function qualifiedNameWithoutRole(user: User, type: string): string {
  const split = type.split(":");
  if (split[0] !== user.organization) {
    return type;
  } else {
    return split[1];
  }
}

export function entityNameFromQualifiedName(qualifiedName: string): string {
  return qualifiedName.split(":")[1];
}




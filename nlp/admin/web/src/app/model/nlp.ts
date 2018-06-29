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

import {ApplicationScopedQuery, Entry, JsonUtils, PaginatedQuery, SearchMark} from "./commons";
import {User} from "./auth";
import {isNullOrUndefined} from "util";
import {StateService} from "../core/state.service";

export class EntityDefinition {

  public static sortEntities(entities: EntityDefinition[]): EntityDefinition[] {
    return entities.sort((e1, e2) => e1.role.localeCompare(e2.role));
  }

  qualifiedRole: string;
  entityColor: string;

  constructor(public entityTypeName: string,
              public role: string,
              public atStartOfDay?: Boolean) {
    this.qualifiedRole = qualifiedRole(entityTypeName, role);
    this.entityColor = entityColor(this.qualifiedRole);
  }

  simpleEntityName(): string {
    return entityNameFromQualifiedName(this.entityTypeName);
  }

  qualifiedName(user: User): string {
    return qualifiedName(user, this.entityTypeName, this.role);
  }

  isDateType(): boolean {
    return this.entityTypeName === "duckling:datetime";
  }

  static fromJSON(json?: any): EntityDefinition {

    const value = Object.create(EntityDefinition.prototype);

    const qualified = qualifiedRole(json.entityTypeName, json.role);
    if (qualified == undefined) {
      return undefined;
    }

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

export class UpdateEntityDefinitionQuery extends ApplicationScopedQuery {

  constructor(public namespace: string,
              public applicationName: string,
              public language: string,
              public entity: EntityDefinition) {
    super(namespace, applicationName, language);
  }
}

export class PredefinedValueQuery {

  constructor(public entityTypeName: string,
              public predefinedValue: string,
              public locale: string,
              public oldPredefinedValue?: string) {
  }

}

export class PredefinedLabelQuery {

  constructor(public entityTypeName: string,
              public predefinedValue: string,
              public locale: string,
              public label: string) {
  }

}

export class EntityType {

  constructor(public name: string,
              public description: string,
              public subEntities: EntityDefinition[],
              public predefinedValues?: PredefinedValue[]) {
  }

  qualifiedName(user: User): string {
    return qualifiedNameWithoutRole(user, this.name);
  }

  simpleName(): string {
    return entityNameFromQualifiedName(this.name);
  }

  namespace(): string {
    return namespaceFromQualifiedName(this.name);
  }

  entityColor(): string {
    return entityColor(this.name);
  }

  qualifiedEntityColor(user: User): string {
    return entityColor(this.qualifiedName(user));
  }

  containsEntityRole(role: string): boolean {
    return this.subEntities.some(e => e.role === role)
  }

  addEntity(entity: EntityDefinition) {
    if (!this.subEntities.some(e => e.entityTypeName === entity.entityTypeName && e.role === entity.role)) {
      this.subEntities.push(entity);
      EntityDefinition.sortEntities(this.subEntities);
    }
  }

  static fromJSON(json?: any): EntityType {
    if (!json) {
      return
    }
    const value = Object.create(EntityType.prototype);

    const result = Object.assign(value, json, {
      subEntities: EntityDefinition.fromJSONArray(json.subEntities),
      predefinedValues: PredefinedValue.fromJSONArray(json.predefinedValues)
        .sort((a, b) => a.value.localeCompare(b.value))
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): EntityType[] {
    return json ? json.map(EntityType.fromJSON) : [];
  }
}

export abstract class EntityContainer {

  private editedSubEntities: EntityWithSubEntities[];

  getEditedSubEntities(): EntityWithSubEntities[] {
    if (!this.editedSubEntities) {
      this.editedSubEntities =
        this.getEntities()
          .filter(e => e.subEntities.length !== 0)
          .map(e => new EntityWithSubEntities(this.getText().substring(e.start, e.end), e));
    }
    return this.editedSubEntities;
  }

  addEditedSubEntities(entity: ClassifiedEntity): EntityWithSubEntities {
    let e = this.findEditedSubEntities(entity);
    if (e) {
      this.editedSubEntities.splice(this.editedSubEntities.indexOf(e), 1);
      e = e.clone();
    } else {
      e = new EntityWithSubEntities(this.getText().substring(entity.start, entity.end), entity);
    }
    this.editedSubEntities.push(e);
    this.editedSubEntities.sort((e1, e2) => e1.start < e2.start ? -1 : (e1.start > e2.start ? 1 : 0))
    return e;
  }

  findEditedSubEntities(entity: ClassifiedEntity): EntityWithSubEntities {
    return this.getEditedSubEntities().find(e => e.start === entity.start);
  }

  abstract clone(): EntityContainer

  abstract getText(): string

  abstract getEntities(): ClassifiedEntity[]

  abstract addEntity(e: ClassifiedEntity)

  abstract removeEntity(e: ClassifiedEntity)

  overlappedEntity(start: number, end: number): ClassifiedEntity {
    for (const e of this.getEntities()) {
      if (!(e.end <= start || e.start >= end)) {
        return e;
      }
    }
    return null;
  }

  entityValue(entity: ClassifiedEntity): string {
    if (entity.value) {
      //clone the value
      const cloned = JSON.parse(JSON.stringify(entity.value));
      this.removeTypeForValue(cloned);

      return JSON.stringify(cloned);
    } else {
      return this.entityText(entity);
    }
  }

  private removeTypeForValue(v) {
    if (v.constructor.name === "Object") {
      for (let property in v) {
        if (v.hasOwnProperty(property))
          if (property === "@type") {
            v["@type"] = undefined;
          } else {
            this.removeTypeForValue(v[property]);
          }
      }
    }
  }

  entityText(entity: ClassifiedEntity): string {
    return this.getText().substring(entity.start, entity.end);
  }

}

export class Intent {

  public static unknown = "tock:unknown";

  constructor(public name: string,
              public namespace: string,
              public entities: EntityDefinition[],
              public applications: string[],
              public mandatoryStates: string[],
              public sharedIntents: string[],
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

export class Sentence extends EntityContainer {

  private withSubEntities: EntityWithSubEntities[];
  private intentLabel: string;

  constructor(public text: string,
              public language: string,
              public applicationId: string,
              public status: SentenceStatus,
              public classification: Classification,
              public creationDate: Date,
              public updateDate: Date,
              public key?: string,) {
    super()
  }

  getIntentLabel(state: StateService): string {
    if (!this.intentLabel) {
      const intent = state.findIntentById(this.classification.intentId);
      this.intentLabel = intent ? intent.name : nameFromQualifiedName(Intent.unknown);
    }
    return this.intentLabel;
  }

  getText(): string {
    return this.text;
  }

  getEntities(): ClassifiedEntity[] {
    return this.classification.entities;
  }

  addEntity(e: ClassifiedEntity) {
    this.classification.entities.push(e);
    this.classification.entities.sort((e1, e2) => e1.start - e2.start);
  }

  removeEntity(e: ClassifiedEntity) {
    this.classification.entities.splice(this.classification.entities.indexOf(e, 0), 1);
  }

  statusDisplayed(): string {
    switch (this.status) {
      case SentenceStatus.deleted :
        return "Deleted";
      case SentenceStatus.inbox :
        return "Inbox";
      case SentenceStatus.model :
        return "Included in model";
      case SentenceStatus.validated :
        return "Validated";
    }
    return "unknown";
  }

  statusColor(): string {
    switch (this.status) {
      case SentenceStatus.deleted :
        return "red";
      case SentenceStatus.inbox :
        return "lightblue";
      case SentenceStatus.model :
        return "lightgreen";
      case SentenceStatus.validated :
        return "mediumspringgreen ";
    }
    return "orange";
  }

  clone(): Sentence {
    return new Sentence(
      this.text,
      this.language,
      this.applicationId,
      this.status,
      this.classification,
      this.creationDate,
      this.updateDate,
      this.key);
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

export class EntityWithSubEntities extends EntityContainer {

  qualifiedRole: string;
  entityColor: string;
  type: string;
  role: string;
  start: number;
  end: number;
  probability: number;

  startSelection: number;
  endSelection: number;

  constructor(public text: string, public entity: ClassifiedEntity) {
    super();
    this.qualifiedRole = entity.qualifiedRole;
    this.entityColor = entity.entityColor;
    this.type = entity.type;
    this.role = entity.role;
    this.start = entity.start;
    this.end = entity.end;
    this.probability = entity.probability;
  }

  hasProbability(): boolean {
    return this.entity.hasProbability();
  }

  qualifiedName(user: User): string {
    return this.entity.qualifiedName(user);
  }

  setSelection(start: number, end: number) {
    this.startSelection = start;
    this.endSelection = end;
  }

  hasSelection(): boolean {
    return !isNullOrUndefined(this.startSelection) && !isNullOrUndefined(this.endSelection);
  }

  cleanupSelection() {
    this.startSelection = undefined;
    this.endSelection = undefined;
  }

  clone(): EntityWithSubEntities {
    return new EntityWithSubEntities(
      this.text,
      this.entity);
  }

  getText(): string {
    return this.text;
  }

  getEntities(): ClassifiedEntity[] {
    return this.entity.subEntities;
  }

  addEntity(e: ClassifiedEntity) {
    this.entity.subEntities.push(e);
    this.entity.subEntities.sort((e1, e2) => e1.start - e2.start);
  }

  removeEntity(e: ClassifiedEntity) {
    this.entity.subEntities.splice(this.entity.subEntities.indexOf(e, 0), 1);
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

  public displayOtherIntents: boolean;

  constructor(public intentId: string,
              public entities: ClassifiedEntity[],
              public intentProbability: number,
              public entitiesProbability: number,
              public otherIntentsProbabilities: Map<string, number>,
              /**
               * The last usage date (for a real user) if any.
               */
              public lastUsage?: Date,
              /**
               * The total number of uses of this sentence.
               */
              public usageCount?: number) {
  }

  hasIntentProbability(): boolean {
    return this.intentProbability && !isNaN(this.intentProbability);
  }

  hasEntitiesProbability(): boolean {
    return this.entitiesProbability && !isNaN(this.entitiesProbability);
  }

  clone(): Classification {
    return new Classification(
      this.intentId,
      this.entities.slice(0),
      this.intentProbability,
      this.entitiesProbability,
      this.otherIntentsProbabilities,
      this.lastUsage,
      this.usageCount);
  }

  static fromJSON(json?: any): Classification {
    const value = Object.create(Classification.prototype);
    const result = Object.assign(value, json, {
      entities: ClassifiedEntity.fromJSONArray(json.entities),
      otherIntentsProbabilities: JsonUtils.jsonToMap(json.otherIntentsProbabilities)
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
              public subEntities: ClassifiedEntity[],
              public value?: any,
              public probability?: number) {
    this.qualifiedRole = qualifiedRole(type, role);
    this.entityColor = entityColor(this.qualifiedRole)
  }

  hasProbability(): boolean {
    return this.probability && !isNaN(this.probability);
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
      entityColor: entityColor(qualified),
      subEntities: ClassifiedEntity.fromJSONArray(json.subEntities)
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
              public checkExistingQuery: boolean,
              public state?: string,) {
    super(namespace, applicationName, language)
  }
}

export class SearchQuery extends PaginatedQuery {

  constructor(public namespace: string,
              public applicationName: string,
              public language: string,
              public start: number,
              public size: number,
              public searchMark?: SearchMark,
              public search?: string,
              public intentId?: string,
              public status?: SentenceStatus[],
              public entityType?: string,
              public entityRole?: string,
              public modifiedAfter?: Date,
              public sort?: Entry<string, boolean>[]) {
    super(namespace, applicationName, language, start, size, searchMark, sort)
  }
}

export interface PaginatedResult<T> {

  rows: T[];
  total: number;
  start: number;
  end: number;
}

export class SentencesResult implements PaginatedResult<Sentence> {

  constructor(public rows: Sentence[],
              public total: number,
              public start: number,
              public end: number) {
  }

  static fromJSON(json?: any): SentencesResult {
    const value = Object.create(SentencesResult.prototype);

    const result = Object.assign(value, json, {
      rows: Sentence.fromJSONArray(json.sentences),
    });

    return result;
  }
}

export class LogsQuery extends PaginatedQuery {

  constructor(public namespace: string,
              public applicationName: string,
              public language: string,
              public start: number,
              public size: number,
              public searchMark?: SearchMark,
              public search?: string) {
    super(namespace, applicationName, language, start, size, searchMark)
  }
}

export class LogsResult implements PaginatedResult<Log> {

  constructor(public rows: Log[],
              public total: number,
              public start: number,
              public end: number) {
  }

  static fromJSON(json?: any): LogsResult {
    const value = Object.create(LogsResult.prototype);

    const result = Object.assign(value, json, {
      rows: Log.fromJSONArray(json.logs),
    });

    return result;
  }
}

export class Log {

  constructor(public dialogId: string,
              public intent: string,
              public request: any,
              public durationInMS: number,
              public error: boolean,
              public date: Date,
              public sentence?: Sentence,
              public response?: any) {
  }

  textRequest(): string {
    return this.request.queries[0];
  }

  requestDetails(): string {
    return JSON.stringify(this.request, null, 2);
  }

  responseDetails(): string {
    return this.response ? JSON.stringify(this.response, null, 2) : "none";
  }

  static fromJSON(json?: any): Log {
    const value = Object.create(Log.prototype);

    const result = Object.assign(value, json, {
      sentence: json.sentence ? Sentence.fromJSON(json.sentence) : null,
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): Log[] {
    return json ? json.map(Log.fromJSON) : [];
  }
}

export class LogStatsQuery extends ApplicationScopedQuery {

  constructor(public namespace: string,
              public applicationName: string,
              public language: string,
              public intent?: string) {
    super(namespace, applicationName, language)
  }
}

export class LogStat {

  constructor(public day: Date,
              public error: number,
              public count: number,
              public averageDuration: number,
              public averageIntentProbability: number,
              public averageEntitiesProbability: number) {

  }

  static fromJSON(json?: any): LogStat {
    const value = Object.create(LogStat.prototype);

    const result = Object.assign(value, json, {});

    return result;
  }

  static fromJSONArray(json?: Array<any>): LogStat[] {
    return json ? json.map(LogStat.fromJSON) : [];
  }

}

export class IntentTestErrorQueryResult {

  constructor(public total: number,
              public data: IntentTestError[]) {
  }

  static fromJSON(json?: any): IntentTestErrorQueryResult {
    const value = Object.create(IntentTestErrorQueryResult.prototype);

    const result = Object.assign(value, json, {
      data: IntentTestError.fromJSONArray(json.data)
    });

    return result;
  }
}

export class IntentTestError {

  constructor(public sentence: Sentence,
              public currentIntent: string,
              public wrongIntent: string,
              public count: number,
              public averageErrorProbability: number,
              public total: number,
              public firstDetectionDate: Date) {
  }

  currentIntentName(): string {
    return entityNameFromQualifiedName(this.currentIntent);
  }

  wrongIntentName(): string {
    return entityNameFromQualifiedName(this.wrongIntent);
  }

  static fromJSON(json?: any): IntentTestError {
    const value = Object.create(IntentTestError.prototype);

    const result = Object.assign(value, json, {
      sentence: Sentence.fromJSON(json.sentence)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): IntentTestError[] {
    return json ? json.map(IntentTestError.fromJSON) : [];
  }

}

export class EntityTestErrorQueryResult {

  constructor(public total: number,
              public data: EntityTestError[]) {
  }

  static fromJSON(json?: any): EntityTestErrorQueryResult {
    const value = Object.create(EntityTestErrorQueryResult.prototype);

    const result = Object.assign(value, json, {
      data: EntityTestError.fromJSONArray(json.data)
    });

    return result;
  }
}

export class EntityTestError {

  constructor(public originalSentence: Sentence,
              public sentence: Sentence,
              public count: number,
              public averageErrorProbability: number,
              public total: number,
              public firstDetectionDate: Date) {
  }

  static fromJSON(json?: any): EntityTestError {
    const value = Object.create(EntityTestError.prototype);

    const result = Object.assign(value, json, {
      originalSentence: Sentence.fromJSON(json.originalSentence),
      sentence: Sentence.fromJSON(json.sentence)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): EntityTestError[] {
    return json ? json.map(EntityTestError.fromJSON) : [];
  }

}

export class TestErrorQuery {

  constructor(public applicationId: string,
              public language: string,
              public start: number,
              public size: number) {
  }
}

export class TestBuildStat {

  constructor(public errors: number,
              public nbSentencesInModel: number,
              public nbSentencesTested: number,
              public buildModelDuration: any,
              public testModelDuration: any,
              public date: Date) {
  }

  static fromJSON(json?: any): TestBuildStat {
    const value = Object.create(TestBuildStat.prototype);

    const result = Object.assign(value, json, {});

    return result;
  }

  static fromJSONArray(json?: Array<any>): TestBuildStat[] {
    return json ? json.map(TestBuildStat.fromJSON) : [];
  }

}

export class UpdateSentencesQuery extends ApplicationScopedQuery {

  constructor(public namespace: string,
              public applicationName: string,
              public language: string,
              public selectedSentences: Sentence[],
              public searchQuery?: SearchQuery,
              public newIntentId?: string,
              public oldEntity?: EntityDefinition,
              public newEntity?: EntityDefinition) {
    super(namespace, applicationName, language)
  }
}

export class UpdateSentencesReport {
  constructor(public nbUpdates: number) {
  }

  static fromJSON(json?: any): UpdateSentencesReport {
    const value = Object.create(UpdateSentencesReport.prototype);

    const result = Object.assign(value, json, {});

    return result;
  }
}

export class PredefinedValue {

  constructor(public value: string,
              public labels: Map<string, string[]>) {
  }

  static fromJSON(json?: any): PredefinedValue {
    if (!json) {
      return null;
    }

    const value = Object.create(PredefinedValue.prototype);

    const result = Object.assign(value, json, {
      labels: JsonUtils.jsonToMap(json.labels)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): PredefinedValue[] {
    return json ? json.map(PredefinedValue.fromJSON) : [];
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
  return qualifiedName ? qualifiedName.split(":")[1] : "error";
}

export function namespaceFromQualifiedName(qualifiedName: string): string {
  return qualifiedName ? qualifiedName.split(":")[0] : "error";
}

export function nameFromQualifiedName(qualifiedName: string): string {
  return qualifiedName ? qualifiedName.split(":")[1] : "error";
}

export function getRoles(intents: Intent[], entityType?: string): string[] {
  const roles = new Set();
  intents.forEach(
    intent => intent.entities.forEach(
      entity => {
        if (!entityType || entityType.length === 0 || entity.entityTypeName === entityType) {
          roles.add(entity.role);
        }
      }
    )
  );
  return Array.from(roles.values()).sort();
}




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

import { map } from 'rxjs/operators';
import { EventEmitter, Injectable } from '@angular/core';
import { Application, UserNamespace } from '../model/application';
import { AuthService } from './auth/auth.service';
import { AuthListener } from './auth/auth.listener';
import { User, UserRole } from '../model/auth';
import { SettingsService } from './settings.service';
import { ApplicationScopedQuery, Entry, groupBy, PaginatedQuery, SearchMark } from '../model/commons';
import {
  EntityDefinition,
  EntityType,
  Intent,
  IntentsCategory,
  nameFromQualifiedName,
  NlpEngineType,
  PredefinedLabelQuery,
  PredefinedValueQuery,
  UpdateEntityDefinitionQuery
} from '../model/nlp';
import { BehaviorSubject, Observable, Subject } from 'rxjs';

@Injectable()
export class StateService implements AuthListener {
  static DEFAULT_LOCALE = 'en';
  static DEFAULT_ENGINE = new NlpEngineType('opennlp');

  locales: Entry<string, string>[];
  supportedNlpEngines: NlpEngineType[];

  user: User;
  namespaces: UserNamespace[];
  applications: Application[];

  dateRange = { start: null, end: null, rangeInDays: null };

  readonly entityTypes: BehaviorSubject<EntityType[]> = new BehaviorSubject([]);
  readonly entities: BehaviorSubject<EntityDefinition[]> = new BehaviorSubject([]);
  readonly currentIntents: BehaviorSubject<Intent[]> = new BehaviorSubject([]);
  readonly currentIntentsCategories: BehaviorSubject<IntentsCategory[]> = new BehaviorSubject([]);
  readonly currentNamespaceIntentsCategories: BehaviorSubject<IntentsCategory[]> = new BehaviorSubject([]);
  readonly configurationChange: Subject<boolean> = new Subject();

  currentApplication: Application;
  currentLocale: string = StateService.DEFAULT_LOCALE;

  readonly currentApplicationEmitter: EventEmitter<Application> = new EventEmitter();
  readonly resetConfigurationEmitter: EventEmitter<boolean> = new EventEmitter();

  static intentExistsInApp(app: Application, intentName: string): boolean {
    for (let j = 0; j < app.intents.length; j++) {
      if (app.intents[j].name === intentName) {
        return true;
      }
    }
    return false;
  }

  static intentIdExistsInApp(app: Application, intentId: string): boolean {
    for (let j = 0; j < app.intents.length; j++) {
      if (app.intents[j]._id === intentId) {
        return true;
      }
    }
    return false;
  }

  constructor(private auth: AuthService, private settings: SettingsService) {
    this.auth.addListener(this);
  }

  hasRole(role: UserRole): boolean {
    return this.user && this.user.roles.indexOf(role) !== -1;
  }

  resetConfiguration() {
    this.resetConfigurationEmitter.emit(true);
  }

  changeApplication(application: Application) {
    if (application) {
      if (this.currentApplication !== application) {
        this.currentApplication = application;
        this.currentApplicationEmitter.emit(application);
        this.sortIntents();
        if (application.supportedLocales.indexOf(this.currentLocale) === -1) {
          this.changeLocale(application.supportedLocales[0]);
        } else {
          this.entities.next(application.allEntities());
          this.configurationChange.next(true);
        }
        this.settings.onApplicationChange(this.currentApplication.name);
      }
    }
  }

  changeApplicationWithName(applicationName: string) {
    this.changeApplication(this.applications.find((a) => a.name === applicationName));
  }

  changeLocale(locale: string) {
    if (this.currentLocale !== locale) {
      this.currentLocale = locale;
      this.entities.next(this.currentApplication.allEntities());
      this.configurationChange.next(true);
      this.settings.onLocaleChange(this.currentLocale);
    }
  }

  private sortIntents() {
    this.currentApplication.intents.sort((a, b) => a.intentLabel().localeCompare(b.intentLabel()));
    this.currentIntents.next(this.currentApplication.intents);
    const categories = [];
    groupBy(this.currentApplication.intents, (i) => (i.category ? i.category : 'default')).forEach((intents, category) => {
      categories.push(new IntentsCategory(category, intents));
    });
    this.currentIntentsCategories.next(categories.sort((a, b) => a.category.localeCompare(b.category)));

    const namespaceCategories = [];
    groupBy(this.currentApplication.intents.concat(this.currentApplication.namespaceIntents), (i) =>
      i.category ? i.category : 'default'
    ).forEach((intents, category) => {
      namespaceCategories.push(new IntentsCategory(category, intents));
    });
    this.currentNamespaceIntentsCategories.next(namespaceCategories.sort((a, b) => a.category.localeCompare(b.category)));
  }

  addIntent(intent: Intent) {
    this.currentApplication.intents.push(intent);
    this.sortIntents();
  }

  updateIntent(intent: Intent) {
    this.currentApplication.removeIntentById(intent._id);
    this.currentApplication.intents.push(intent);
    this.sortIntents();
  }

  removeIntent(intent: Intent) {
    this.currentApplication.removeIntentById(intent._id);
    this.sortIntents();
  }

  findIntentById(id: string): Intent {
    return this.currentApplication.intents.find((i) => i._id === id);
  }

  findSharedNamespaceIntentById(id: string): Intent {
    return this.findIntentById(id) ?? this.currentApplication.namespaceIntents.find((i) => i._id === id);
  }

  isOtherNamespaceIntent(intent: Intent): boolean {
    return intent && this.currentApplication.isOtherNamespaceIntent(intent);
  }

  findIntentByName(name: string): Intent {
    return this.currentApplication.intents.find((i) => i.name === name);
  }

  intentLabelByName(name: string): string {
    const n = name.indexOf(':') == -1 ? name : nameFromQualifiedName(name);
    const i = this.findIntentByName(n);
    if (i == null) {
      return name;
    } else {
      return i.intentLabel();
    }
  }

  findEntityTypeByName(name: string): EntityType {
    return this.entityTypes.getValue().find((e) => e.name === name);
  }

  entityTypesSortedByName(): Observable<EntityType[]> {
    return this.entityTypes.pipe(map((e) => e.sort((e1, e2) => e1.simpleName().localeCompare(e2.simpleName()))));
  }

  removeEntityTypeByName(name: string) {
    const entityToRemove = this.findEntityTypeByName(name);
    const entities = this.entityTypes.getValue().slice(0);
    entities.splice(entities.indexOf(entityToRemove), 1);
    this.entityTypes.next(entities);
  }

  removeSubEntityByRole(entityType: EntityType, role: string) {
    entityType.subEntities.splice(
      entityType.subEntities.findIndex((e) => e.role === role),
      1
    );
  }

  localeName(code: string): string {
    return this.locales ? this.locales.find((l) => l.first === code).second : code;
  }

  sortApplications() {
    this.applications = this.applications.sort((a, b) => a.name.localeCompare(b.name));
  }

  intentExists(intentName: string): boolean {
    for (let i = 0; i < this.applications.length; i++) {
      const a = this.applications[i];
      if (StateService.intentExistsInApp(a, intentName)) {
        return true;
      }
    }
    return false;
  }

  intentExistsInOtherApplication(intentName: string): boolean {
    for (let i = 0; i < this.applications.length; i++) {
      const a = this.applications[i];
      if (a._id !== this.currentApplication._id) {
        if (StateService.intentExistsInApp(a, intentName)) {
          return true;
        }
      }
    }
    return false;
  }

  intentIdExistsInOtherApplication(intentId: string): boolean {
    for (let i = 0; i < this.applications.length; i++) {
      const a = this.applications[i];
      if (a._id !== this.currentApplication._id) {
        if (StateService.intentIdExistsInApp(a, intentId)) {
          return true;
        }
      }
    }
    return false;
  }

  findCurrentApplication(): Application {
    if (!this.currentApplication && this.applications) {
      if (this.settings.currentApplicationName) {
        this.changeApplication(this.applications.find((a) => a.name === this.settings.currentApplicationName));
      }
      if (!this.currentApplication && this.applications.length != 0) {
        this.changeApplication(this.applications[0]);
      }
    }
    return this.currentApplication;
  }

  login(user: User) {
    this.user = user;
    this.resetConfiguration();
  }

  logout() {
    this.user = null;
    this.currentApplication = null;
    this.namespaces = null;
    this.applications = null;
  }

  createApplicationScopedQuery(): ApplicationScopedQuery {
    return new ApplicationScopedQuery(
      this.currentApplication ? this.currentApplication.namespace : '',
      this.currentApplication ? this.currentApplication.name : '',
      this.currentLocale
    );
  }

  createUpdateEntityDefinitionQuery(entity: EntityDefinition): UpdateEntityDefinitionQuery {
    return new UpdateEntityDefinitionQuery(this.currentApplication.namespace, this.currentApplication.name, this.currentLocale, entity);
  }

  createPaginatedQuery(start: number, size?: number, searchMark?: SearchMark): PaginatedQuery {
    return new PaginatedQuery(
      this.currentApplication.namespace,
      this.currentApplication.name,
      this.currentLocale,
      start,
      size ? size : 1000,
      searchMark
    );
  }

  createPredefinedValueQuery(entityTypeName: string, predefinedValue: string, oldPredefinedValue?: string): PredefinedValueQuery {
    return new PredefinedValueQuery(entityTypeName, predefinedValue.trim(), this.currentLocale, oldPredefinedValue);
  }

  createPredefinedLabelQuery(entityTypeName: string, predefinedValue: string, locale: string, label: string): PredefinedLabelQuery {
    return new PredefinedLabelQuery(entityTypeName, predefinedValue.trim(), locale, label.trim());
  }

  otherThanCurrentLocales(): string[] {
    return this.currentApplication.supportedLocales.filter((l) => l !== this.currentLocale);
  }
}

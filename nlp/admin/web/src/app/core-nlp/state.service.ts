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


import {map} from 'rxjs/operators';
import {EventEmitter, Injectable} from "@angular/core";
import {Application} from "../model/application";
import {AuthService} from "./auth/auth.service";
import {AuthListener} from "./auth/auth.listener";
import {User, UserRole} from "../model/auth";
import {SettingsService} from "./settings.service";
import {ApplicationScopedQuery, Entry, groupBy, PaginatedQuery, SearchMark} from "../model/commons";
import {
  EntityDefinition,
  EntityType,
  Intent,
  IntentsCategory,
  NlpEngineType,
  PredefinedLabelQuery,
  PredefinedValueQuery,
  UpdateEntityDefinitionQuery
} from "../model/nlp";
import {BehaviorSubject, Observable} from "rxjs";

@Injectable()
export class StateService implements AuthListener {

  static DEFAULT_LOCALE = "en";
  static DEFAULT_ENGINE = new NlpEngineType("opennlp");

  locales: Entry<string, string>[];
  supportedNlpEngines: NlpEngineType[];

  user: User;
  applications: Application[];
  entityTypes: BehaviorSubject<EntityType[]> = new BehaviorSubject([]);
  entities: BehaviorSubject<EntityDefinition[]> = new BehaviorSubject([]);
  currentIntents: BehaviorSubject<Intent[]> = new BehaviorSubject([]);
  currentIntentsCategories: BehaviorSubject<IntentsCategory[]> = new BehaviorSubject([]);

  currentApplication: Application;
  currentLocale: string = StateService.DEFAULT_LOCALE;

  readonly currentApplicationEmitter: EventEmitter<Application> = new EventEmitter();
  readonly currentLocaleEmitter: EventEmitter<string> = new EventEmitter();
  readonly resetConfigurationEmitter: EventEmitter<boolean> = new EventEmitter();

  static intentExistsInApp(app: Application, intentName: string): boolean {
    for (let j = 0; j < app.intents.length; j++) {
      if (app.intents[j].name === intentName) {
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

  currentEngine(): NlpEngineType {
    return this.currentApplication.nlpEngineType;
  }

  changeApplication(application: Application) {
    if (application) {
      this.currentApplication = application;
      this.currentApplicationEmitter.emit(application);
      this.sortIntents();
      this.entities.next(application.allEntities());
      if (application.supportedLocales.indexOf(this.currentLocale) === -1) {
        this.changeLocale(application.supportedLocales[0])
      }
      this.settings.onApplicationChange(this.currentApplication.name);
    }
  }

  changeApplicationWithName(applicationName: string) {
    this.changeApplication(this.applications.find(a => a.name === applicationName));
  }

  changeLocale(locale: string) {
    this.currentLocale = locale;
    this.entities.next(this.currentApplication.allEntities());
    this.currentLocaleEmitter.emit(locale);
    this.settings.onLocaleChange(this.currentLocale);
  }

  private sortIntents() {
    this.currentApplication.intents.sort((a, b) => a.intentLabel().localeCompare(b.intentLabel()));
    this.currentIntents.next(this.currentApplication.intents);
    const categories = [];
    groupBy(this.currentApplication.intents, i => i.category ? i.category : "default")
      .forEach((intents, category) => {
        categories.push(new IntentsCategory(category, intents));
      });
    this.currentIntentsCategories.next(
      categories.sort((a, b) => a.category.localeCompare(b.category))
    );
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
    return this.currentApplication.intents.find(i => i._id === id);
  }

  findIntentByName(name: string): Intent {
    return this.currentApplication.intents.find(i => i.name === name);
  }

  findEntityTypeByName(name: string): EntityType {
    return this.entityTypes.getValue().find(e => e.name === name);
  }

  entityTypesSortedByName(): Observable<EntityType[]> {
    return this.entityTypes.pipe(map(e => e.sort((e1, e2) => e1.simpleName().localeCompare(e2.simpleName()))));
  }

  removeEntityTypeByName(name: string) {
    const entityToRemove = this.findEntityTypeByName(name);
    const entities = this.entityTypes.getValue().slice(0);
    entities.splice(entities.indexOf(entityToRemove), 1);
    this.entityTypes.next(entities);
  }

  removeSubEntityByRole(entityType: EntityType, role: string) {
    entityType.subEntities.splice(entityType.subEntities.findIndex(e => e.role === role), 1);
  }

  localeName(code: string): string {
    return this.locales ? this.locales.find(l => l.first === code).second : code;
  }

  sortApplications() {
    this.applications = this.applications.sort((a, b) => a.name.localeCompare(b.name))
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

  findCurrentApplication(): Application {
    if (!this.currentApplication && this.applications) {
      if (this.settings.currentApplicationName) {
        this.changeApplication(this.applications.find(a => a.name === this.settings.currentApplicationName));
      }
      if (!this.currentApplication && this.applications.length != 0) {
        this.changeApplication(this.applications[0]);
      }
    }
    return this.currentApplication;
  }

  login(user: User) {
    this.user = user;
  }

  logout() {
    this.user = null;
    this.currentApplication = null;
    this.applications = null;
  }

  createApplicationScopedQuery(): ApplicationScopedQuery {
    return new ApplicationScopedQuery(
      this.currentApplication ? this.currentApplication.namespace : "",
      this.currentApplication ? this.currentApplication.name : "",
      this.currentLocale
    )
  }

  createUpdateEntityDefinitionQuery(entity: EntityDefinition): UpdateEntityDefinitionQuery {
    return new UpdateEntityDefinitionQuery(
      this.currentApplication.namespace,
      this.currentApplication.name,
      this.currentLocale,
      entity
    )
  }

  createPaginatedQuery(start: number, size: number, searchMark?: SearchMark): PaginatedQuery {
    return new PaginatedQuery(
      this.currentApplication.namespace,
      this.currentApplication.name,
      this.currentLocale,
      start,
      size,
      searchMark
    );
  }

  createPredefinedValueQuery(entityTypeName: string, predefinedValue: string, oldPredefinedValue?: string): PredefinedValueQuery {
    return new PredefinedValueQuery(
      entityTypeName,
      predefinedValue.trim(),
      this.currentLocale,
      oldPredefinedValue
    );
  }

  createPredefinedLabelQuery(entityTypeName: string, predefinedValue: string, locale: string, label: string): PredefinedLabelQuery {
    return new PredefinedLabelQuery(
      entityTypeName,
      predefinedValue.trim(),
      locale,
      label.trim()
    );
  }

}


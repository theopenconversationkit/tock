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

import {EventEmitter, Injectable} from "@angular/core";
import {Application} from "../model/application";
import {AuthService} from "./auth/auth.service";
import {AuthListener} from "./auth/auth.listener";
import {AuthenticateResponse, User} from "../model/auth";
import {SettingsService} from "./settings.service";
import {ApplicationScopedQuery, Entry} from "../model/commons";
import {environment} from "../../environments/environment";
import {EntityType, NlpEngineType} from "../model/nlp";

@Injectable()
export class StateService implements AuthListener {

  static DEFAULT_LOCALE = "en";
  static DEFAULT_ENGINE = new NlpEngineType("opennlp");

  locales: Entry<string, string>[];
  supportedNlpEngines: NlpEngineType[];

  user: User;
  applications: Application[];
  entityTypes: EntityType[];

  currentApplication: Application;
  currentLocale: string = StateService.DEFAULT_LOCALE;

  readonly currentApplicationEmitter: EventEmitter<Application> = new EventEmitter();
  readonly currentLocaleEmitter: EventEmitter<string> = new EventEmitter();
  readonly resetConfigurationEmitter: EventEmitter<boolean> = new EventEmitter();

  constructor(private auth: AuthService, private settings: SettingsService) {
    this.auth.addListener(this);
    //hack for dev env
    if (environment.autologin) {
      this.auth.login("password", new AuthenticateResponse(true, "admin@vsct.fr", "vsc"));
    }
  }

  resetConfiguration() {
    this.resetConfigurationEmitter.emit(true);
  }

  currentEngine(): NlpEngineType {
    return this.currentApplication.nlpEngineType;
  }

  changeApplication(application: Application) {
    this.currentApplication = application;
    this.currentApplicationEmitter.emit(application);
    if (application.supportedLocales.indexOf(this.currentLocale) === -1) {
      this.changeLocale(application.supportedLocales[0])
    }
  }

  changeApplicationWithName(applicationName: string) {
    this.changeApplication(this.applications.find(a => a.name === applicationName));
  }

  changeLocale(locale: string) {
    this.currentLocale = locale;
    this.currentLocaleEmitter.emit(locale);
  }

  findEntityTypeByName(name: string): EntityType {
    return this.entityTypes.find(e => e.name === name);
  }

  entityTypesSortedByName(): EntityType[] {
    return this.entityTypes ? this.entityTypes.sort((e1, e2) => e1.simpleName().localeCompare(e2.simpleName())) : this.entityTypes;
  }

  entityRoles(): string[] {
    const roles = new Set();
    this.currentApplication.intents.forEach(
      intent => intent.entities.forEach(
        entity => roles.add(entity.role)
      )
    );
    return Array.from(roles.values()).sort();
  }

  removeEntityTypeByName(name: string) {
    const entityToRemove = this.findEntityTypeByName(name);
    this.entityTypes.splice(this.entityTypes.indexOf(entityToRemove), 1)
  }

  localeName(code: string): string {
    return this.locales ? this.locales.find(l => l.first === code).second : code;
  }

  sortApplications() {
    this.applications = this.applications.sort((a, b) => a.name.localeCompare(b.name))
  }

  intentExists(intentName: string): boolean {
    for (let i = 0; i < this.applications.length; i++) {
      for (let j = 0; j < this.applications[i].intents.length; j++) {
        if (this.applications[i].intents[j].name === intentName) {
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
      this.currentApplication.namespace,
      this.currentApplication.name,
      this.currentLocale
    )
  }

}

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

import {Injectable} from "@angular/core";
import {Application} from "../model/application";
import {AuthService} from "./auth/auth.service";
import {AuthListener} from "./auth/auth.listener";
import {User, AuthenticateResponse} from "../model/auth";
import {SettingsService} from "./settings.service";
import {Entry} from "../model/commons";
import {environment} from "../../environments/environment";
import {NlpEngineType, EntityType} from "../model/nlp";

@Injectable()
export class StateService implements AuthListener {

  static DEFAULT_LOCALE=  "en";
  static DEFAULT_ENGINE=  new NlpEngineType("opennlp");

  user: User;
  applications: Application[];
  locales: Entry<string,string>[];
  entityTypes:EntityType[];

  currentApplication: Application;
  currentLocale:string = StateService.DEFAULT_LOCALE;
  currentEngine:NlpEngineType = StateService.DEFAULT_ENGINE;

  constructor(private auth: AuthService, private settings: SettingsService) {
    this.auth.addListener(this);
    //hack for dev env
    if (environment.autologin) {
      this.auth.login("password", new AuthenticateResponse(true, "admin@vsct.fr", "vsc"));
    }
  }

  findEntityTypeByName(name:string) {
    return this.entityTypes.find(e => e.name === name);
  }

  localeName(code:string) : string {
    return this.locales.find(l => l.first === code).second;
  }

  sortApplications() {
    this.applications = this.applications.sort( (a,b) => a.name.localeCompare(b.name))
  }

  findCurrentApplication(): Application {
    if (!this.currentApplication && this.applications) {
      if (this.settings.currentApplicationName) {
        this.currentApplication = this.applications.find(a => a.name === this.settings.currentApplicationName);
      }
      if (!this.currentApplication && this.applications.length != 0) {
        this.currentApplication = this.applications[0];
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

}

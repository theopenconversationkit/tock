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

import {Injectable, OnDestroy, OnInit} from "@angular/core";
import {RestService} from "tock-nlp-admin/src/app/core/rest/rest.service";
import {StateService} from "tock-nlp-admin/src/app/core/state.service";
import {Observable} from "rxjs/Observable";
import {ApplicationScopedQuery} from "tock-nlp-admin/src/app/model/commons";
import {BotApplicationConfiguration} from "./model/configuration";
import {BehaviorSubject} from "rxjs/BehaviorSubject";

@Injectable()
export class BotConfigurationService implements OnInit, OnDestroy {

  private currentApplicationUnsuscriber: any;
  private currentLocaleUnsuscriber: any;

  //only rest configurations
  restConfigurations: BehaviorSubject<BotApplicationConfiguration[]> = new BehaviorSubject([]);
  //all configurations
  configurations: BehaviorSubject<BotApplicationConfiguration[]> = new BehaviorSubject([]);
  //has rest configuration
  hasRestConfigurations: BehaviorSubject<boolean> = new BehaviorSubject(false);

  constructor(private rest: RestService,
              private state: StateService) {
    this.currentApplicationUnsuscriber = this.state.currentApplicationEmitter.subscribe(_ => this.updateConfigurations());
    this.currentLocaleUnsuscriber = this.state.currentLocaleEmitter.subscribe(_ => this.updateConfigurations());
    this.updateConfigurations();
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.currentApplicationUnsuscriber.unsubscribe();
    this.currentLocaleUnsuscriber.unsubscribe();
  }

  updateConfigurations() {
    this.getConfigurations(this.state.createApplicationScopedQuery())
      .subscribe(c => {
        this.configurations.next(c);
        let rest = c.filter(c => c.connectorType.isRest());
        this.restConfigurations.next(rest);
        this.hasRestConfigurations.next(rest.length !== 0);
      });
  }

  private getConfigurations(query: ApplicationScopedQuery): Observable<BotApplicationConfiguration[]> {
    return this.rest.post("/configuration/bots", query, BotApplicationConfiguration.fromJSONArray);
  }

  saveConfiguration(conf: BotApplicationConfiguration): Observable<any> {
    return this.rest.post("/configuration/bot", conf);
  }

  deleteConfiguration(conf: BotApplicationConfiguration): Observable<boolean> {
    return this.rest.delete(`/configuration/bot/${conf._id}`);
  }

}

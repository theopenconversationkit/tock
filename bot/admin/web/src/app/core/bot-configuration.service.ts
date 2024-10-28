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

import { Injectable, OnDestroy } from '@angular/core';
import { RestService } from '../core-nlp/rest/rest.service';
import { StateService } from '../core-nlp/state.service';
import { BehaviorSubject, Observable, Subscription, forkJoin, share } from 'rxjs';
import { ApplicationScopedQuery } from '../model/commons';
import { BotApplicationConfiguration, BotConfiguration, ConnectorType } from './model/configuration';
import { SynchronizationConfiguration } from './model/synchronizationConfiguration';

@Injectable()
export class BotConfigurationService implements OnDestroy {
  private subscription: Subscription;

  //only rest configurations
  readonly restConfigurations: BehaviorSubject<BotApplicationConfiguration[]> = new BehaviorSubject([]);
  //all configurations
  readonly configurations: BehaviorSubject<BotApplicationConfiguration[]> = new BehaviorSubject([]);
  //has rest configuration
  readonly hasRestConfigurations: BehaviorSubject<boolean> = new BehaviorSubject(false);
  //supported connectors
  readonly supportedConnectors: BehaviorSubject<ConnectorType[]> = new BehaviorSubject([]);
  //bots
  readonly bots: BehaviorSubject<BotConfiguration[]> = new BehaviorSubject([]);

  constructor(private rest: RestService, private state: StateService) {
    this.subscription = this.state.configurationChange.subscribe((_) => {
      this.updateConfigurations();
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  updateConfigurations() {
    if (this.state.currentApplication) {
      const loaders = [this.getBots(this.state.currentApplication.name), this.getConfigurations(this.state.createApplicationScopedQuery())];

      forkJoin(loaders).subscribe(
        ([botConfigurations, botApplicationConfigurations]: [BotConfiguration[], BotApplicationConfiguration[]]) => {
          this.bots.next(botConfigurations);

          this.configurations.next(botApplicationConfigurations);
          let rest = botApplicationConfigurations.filter((conf) => conf.connectorType.isRest());
          this.restConfigurations.next(rest);
          this.hasRestConfigurations.next(rest.length !== 0);
          const connectors = [];
          botApplicationConfigurations.forEach((conf) => {
            if (!conf.connectorType.isRest() && !connectors.some((e) => conf.connectorType.id === e.id)) {
              connectors.push(conf.connectorType);
            }
          });
          this.supportedConnectors.next(connectors);
        }
      );
    }
  }

  private getConfigurations(query: ApplicationScopedQuery): Observable<BotApplicationConfiguration[]> {
    return this.rest.post('/configuration/bots', query, BotApplicationConfiguration.fromJSONArray);
  }

  saveConfiguration(conf: BotApplicationConfiguration): Observable<any> {
    return this.rest.post('/configuration/bot', conf);
  }

  deleteConfiguration(conf: BotApplicationConfiguration): Observable<boolean> {
    return this.rest.delete(`/configuration/bot/${conf._id}`);
  }

  synchronize(conf: SynchronizationConfiguration): Observable<any> {
    return this.rest.post('/configuration/synchronization', conf);
  }

  getBotsPending: Observable<BotConfiguration[]>;
  private getBots(botId: string): Observable<BotConfiguration[]> {
    if (!this.getBotsPending) {
      this.getBotsPending = this.rest.get(`/bots/${botId}`, BotConfiguration.fromJSONArray).pipe(share());
    }
    return this.getBotsPending;
  }

  findApplicationConfigurationById(id: string): BotApplicationConfiguration {
    const i = this.configurations.getValue().filter((c) => c._id === id);
    if (i.length === 0) {
      return null;
    } else {
      return i[0];
    }
  }

  findApplicationConfigurationByApplicationId(applicationId: string): BotApplicationConfiguration {
    const i = this.configurations.getValue().filter((c) => c.applicationId === applicationId);
    if (i.length === 0) {
      return null;
    } else {
      return i[0];
    }
  }

  saveBot(conf: BotConfiguration): Observable<any> {
    return this.rest.post('/bot', conf);
  }

  findValidPath(connectorType: ConnectorType): string {
    const bots = this.bots.getValue();
    const baseTargetPath = `/io/${this.state.user.organization
      .toLowerCase()
      .replace(/\s/g, '')}/${this.state.currentApplication.name.replace(/\s/g, '_')}/${connectorType.id}`;
    let targetPath = baseTargetPath;
    let index = 1;
    while (bots.findIndex((b) => b.configurations && b.configurations.findIndex((c) => c.path === targetPath) !== -1) !== -1) {
      targetPath = baseTargetPath + index++;
    }
    return targetPath;
  }

  findValidId(name: string): string {
    const bots = this.bots.getValue();
    const baseId = name;
    let targetId = baseId;
    let index = 1;
    while (bots.findIndex((b) => b.configurations && b.configurations.findIndex((c) => c.applicationId === targetId) !== -1) !== -1) {
      targetId = baseId + index++;
    }
    return targetId;
  }
}

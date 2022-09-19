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

import { share, tap } from 'rxjs/operators';
import { Injectable, OnDestroy } from '@angular/core';
import { RestService } from '../core-nlp/rest/rest.service';
import { Observable, of } from 'rxjs';
import { ConnectorType, ConnectorTypeConfiguration } from '../core/model/configuration';
import { NlpCallStats } from './model/dialog-data';
import { AdminConfiguration } from './model/conf';

@Injectable()
export class BotSharedService implements OnDestroy {
  private connectorTypes: ConnectorTypeConfiguration[];
  configuration: AdminConfiguration;

  constructor(private rest: RestService) {
    this.getConfiguration().subscribe((r) => (this.configuration = r));
  }

  ngOnDestroy(): void {}

  getConnectorTypes(): Observable<ConnectorTypeConfiguration[]> {
    if (this.connectorTypes) {
      return of(this.connectorTypes);
    } else {
      return this.rest
        .get(`/connectorTypes`, ConnectorTypeConfiguration.fromJSONArray)
        .pipe(tap((c) => (this.connectorTypes = c)));
    }
  }

  findConnectorConfiguration(connectorType: ConnectorType): ConnectorTypeConfiguration {
    let r = undefined;
    if (this.connectorTypes) {
      r = this.connectorTypes.find((c) => c.connectorType.id === connectorType.id);
    }
    if (r) {
      return r;
    } else {
      //should not happen
      return new ConnectorTypeConfiguration(connectorType, [], '');
    }
  }

  getNlpDialogStats(actionId: string): Observable<NlpCallStats> {
    return this.rest.get(`/action/nlp-stats/${actionId}`, NlpCallStats.fromJSON);
  }

  getConfigurationPending: Observable<AdminConfiguration>;
  getConfiguration(): Observable<AdminConfiguration> {
    if (this.configuration) {
      return of(this.configuration);
    } else {
      if (!this.getConfigurationPending) {
        this.getConfigurationPending = this.rest
          .get(`/configuration`, AdminConfiguration.fromJSON)
          .pipe(share());
      }
      return this.getConfigurationPending;
    }
  }
}

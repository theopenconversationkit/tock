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

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { BotApplicationConfiguration, ConnectorType } from '../../core/model/configuration';
import { BotSharedService } from '../../shared/bot-shared.service';
import { StateService } from '../../core-nlp/state.service';
import { BotConfigurationService } from '../../core/bot-configuration.service';

@Component({
  selector: 'tock-bot-configuration',
  templateUrl: './bot-configuration.component.html',
  styleUrls: ['./bot-configuration.component.css']
})
export class BotConfigurationComponent implements OnInit {
  @Input()
  configuration: BotApplicationConfiguration;

  @Output()
  onRemove = new EventEmitter<boolean>();

  @Output()
  onValidate = new EventEmitter<boolean>();

  connectorTypes: ConnectorType[] = [];
  connectorTypesAndRestType: ConnectorType[] = [];

  constructor(public botSharedService: BotSharedService, private state: StateService, private botConfiguration: BotConfigurationService) {}

  ngOnInit(): void {
    this.botSharedService.getConnectorTypes().subscribe((confConf) => {
      const c = confConf.map((it) => it.connectorType).sort((a, b) => a.id.localeCompare(b.id));
      this.connectorTypes = c.filter((conn) => !conn.isRest());
      const rest = c.find((conn) => conn.isRest());
      this.connectorTypesAndRestType = c.filter((conn) => !conn.isRest());
      this.connectorTypesAndRestType.push(rest);
      if (!this.configuration._id && c.length > 0) {
        this.configuration.connectorType = c[0];
        this.changeConnectorType();
      }
    });
  }

  remove() {
    this.onRemove.emit(true);
  }

  update() {
    this.onValidate.emit(true);
  }

  changeConnectorType() {
    this.configuration.path = this.botConfiguration.findValidPath(this.configuration.connectorType);
    this.configuration.applicationId = this.botConfiguration.findValidId(this.configuration.name);
  }

  changePath() {
    this.configuration.path = this.configuration.path.toLowerCase();
  }
}

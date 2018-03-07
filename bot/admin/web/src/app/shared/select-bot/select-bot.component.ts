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

import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {BotConfigurationService} from "../../core/bot-configuration.service";
import {BotApplicationConfiguration, ConnectorType} from "../../core/model/configuration";

@Component({
  selector: 'tock-select-bot',
  templateUrl: './select-bot.component.html',
  styleUrls: ['./select-bot.component.css']
})
export class SelectBotComponent implements OnInit {

  @Input()
  configurationId: string;

  @Input()
  displayConnectorChoice: boolean = true;

  @Output()
  private configurationIdChange = new EventEmitter<string>();

  configurations: BotApplicationConfiguration[];

  botNames: string[];
  connectorTypes: ConnectorType[];
  currentBotName: string;
  currentConnectorType: ConnectorType;

  constructor(private botConfiguration: BotConfigurationService) {
  }

  ngOnInit() {
    this.botConfiguration.restConfigurations
      .subscribe(conf => {
        setTimeout(_ => {
          if (conf.length !== 0) {
            this.botNames = Array.from(new Set(conf.map(c => c.name))).sort();
            if (!this.configurationId) {
              this.configurationId = conf[0]._id;
            }
            this.changeConf(conf.find(c => c._id === this.configurationId), conf);
          } else {
            this.configurations = [];
          }

        });
      });
  }

  private changeConf(conf: BotApplicationConfiguration, configurations: BotApplicationConfiguration[]) {
    this.currentBotName = conf.name;
    this.currentConnectorType = conf.ownerConnectorType;
    this.connectorTypes = configurations.filter(c => c.name === conf.name).map(c => c.ownerConnectorType);
    this.configurationId = conf._id;
    this.configurations = configurations;
    this.configurationIdChange.emit(conf._id);
  }

  changeBotName(botName: string) {
    this.changeConf(this.configurations.find(c => c.name === botName), this.configurations)
  }

  changeConnectorType(connectorType: ConnectorType) {
    this.changeConf(
      this.configurations.find(
        c => c.name === this.currentBotName
          && c.ownerConnectorType.id === connectorType.id),
      this.configurations)
  }
}

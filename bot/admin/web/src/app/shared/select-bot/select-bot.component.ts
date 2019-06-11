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

  @Input()
  allowNoSelection: boolean = false;

  @Output()
  private configurationIdChange = new EventEmitter<string>();

  @Input()
  allowNoConfigurationSelection: boolean = false;

  @Input()
  useRestConfiguration: boolean = true;

  configurations: BotApplicationConfiguration[];

  botNames: string[];
  connectorTypes: ConnectorType[];
  currentBotName: string;
  currentConnectorType: ConnectorType;

  constructor(private botConfiguration: BotConfigurationService) {
  }

  private getName(conf: BotApplicationConfiguration): string {
    return this.displayConnectorChoice ? conf.name : conf.botId;
  }

  ngOnInit() {
    (this.displayConnectorChoice && this.useRestConfiguration
      ? this.botConfiguration.restConfigurations : this.botConfiguration.configurations)
      .subscribe(conf => {
        setTimeout(_ => {
          if (conf.length !== 0 && conf !== this.configurations) {
            this.botNames = Array.from(new Set(conf.map(c => this.getName(c)))).sort();
            const retainedConfs = conf.filter(c => this.useRestConfiguration || !c.connectorType.isRest());
            const containsCurrentSelection = this.configurationId && retainedConfs.some(c => c._id === this.configurationId);
            if (!this.allowNoSelection && !containsCurrentSelection) {
              this.configurationId = conf[0]._id;
            }
            if (this.configurationId) {
              if(!containsCurrentSelection) {
                this.changeConf(conf.find(c => c._id === this.configurationId), retainedConfs);
              }
            } else {
              this.currentBotName = 'None';
              this.configurations = retainedConfs;
              if(!containsCurrentSelection) {
                this.configurationIdChange.emit(null)
              }
            }
          } else {
            if (conf.length === 0) {
              this.currentBotName = 'None';
              this.configurations = conf;
              this.configurationIdChange.emit(null)
            }
          }

        });
      });
  }

  private changeConf(conf: BotApplicationConfiguration, configurations: BotApplicationConfiguration[]) {
    if (conf) {
      this.currentBotName = this.getName(conf);
      this.currentConnectorType = conf.ownConnectorType();
      this.connectorTypes = configurations
        .filter(c => c.name === conf.name)
        .map(c => c.ownConnectorType());
      this.configurationId = conf._id;
      this.configurations = configurations;
      this.configurationIdChange.emit(conf._id);
    } else {
      this.currentBotName = 'None';
      this.configurationIdChange.emit(null)
    }
  }

  changeBotName(botName: string) {
    this.changeConf(this.configurations.find(c => this.getName(c) === botName), this.configurations)
  }

  changeConnectorType(connectorType: ConnectorType) {
    let conf = this.configurations.find(
      c => this.getName(c) === this.currentBotName
        && c.ownConnectorType().id === connectorType.id);
    if (conf) {
      this.changeConf(conf, this.configurations);
    } else {
      this.configurationIdChange.emit("all")
    }
  }
}

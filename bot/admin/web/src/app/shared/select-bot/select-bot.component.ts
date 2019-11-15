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
import {BotApplicationConfiguration} from "../../core/model/configuration";
import {DialogService} from "../../core-nlp/dialog.service";

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

  @Output()
  private selectionChange = new EventEmitter<SelectBotEvent>();

  @Input()
  allowNoConfigurationSelection: boolean = false;

  @Input()
  returnsRestConfiguration: boolean = false;

  @Input()
  noConfigurationLabel:string = "No configuration";

  configurations: BotApplicationConfiguration[];

  botNames: string[];
  allConfigurations: BotApplicationConfiguration[];
  currentConfigurations: BotApplicationConfiguration[];
  currentBotName: string;
  currentConfiguration: BotApplicationConfiguration;

  constructor(
    private botConfiguration: BotConfigurationService,
    private dialog: DialogService) {
  }

  private getName(conf: BotApplicationConfiguration): string {
    return this.displayConnectorChoice ? conf.name : conf.botId;
  }

  ngOnInit() {
    this.botConfiguration.configurations
      .subscribe(conf => {
        this.allConfigurations = conf;
        setTimeout(_ => {
          if (conf.length !== 0 && conf !== this.configurations) {
            const retainedConfs = conf.filter(c => c.targetConfigurationId == null);
            this.botNames = Array.from(new Set(retainedConfs.map(c => this.getName(c)))).sort();
            const containsCurrentSelection = this.configurationId && retainedConfs.some(c => c._id === this.configurationId);
            if (!this.allowNoSelection && !containsCurrentSelection && retainedConfs.length !== 0) {
              this.configurationId = retainedConfs[0]._id;
            }
            if (this.configurationId) {
              if (!containsCurrentSelection) {
                this.changeConf(conf.find(c => c._id === this.configurationId), retainedConfs, this.allowNoConfigurationSelection);
              }
            } else {
              this.currentBotName = 'None';
              this.configurations = retainedConfs;
              if (!containsCurrentSelection) {
                this.selectionChange.emit(null);
                this.configurationIdChange.emit(null);
              }
            }
          } else {
            if (conf.length === 0) {
              this.currentBotName = 'None';
              this.configurations = conf.filter(c => c.targetConfigurationId == null);
              this.selectionChange.emit(null);
              this.configurationIdChange.emit(null);
            }
          }

        });
      });
  }

  private changeConf(conf: BotApplicationConfiguration, configurations: BotApplicationConfiguration[], noConnectorSelection: boolean) {
    if (conf) {
      this.currentBotName = this.getName(conf);
      this.currentConfiguration = conf;
      this.currentConfigurations = configurations
        .filter(c => c.name === conf.name);
      this.configurationId = conf._id;
      this.configurations = configurations;
      const confResult = this.returnsRestConfiguration ? BotApplicationConfiguration.getRestConfiguration(this.allConfigurations, conf) : conf;
      if (confResult) {
        this.selectionChange.emit(new SelectBotEvent(confResult.name, noConnectorSelection, noConnectorSelection ? null : confResult._id));
        this.configurationIdChange.emit(confResult._id);
      } else {
        this.dialog.notify("Test Configuration not found");
      }
    } else {
      this.currentBotName = 'None';
      this.selectionChange.emit(null);
      this.configurationIdChange.emit(null);
    }
  }

  changeBotName() {
    this.changeConf(this.configurations.find(c => this.getName(c) === this.currentBotName), this.configurations, this.allowNoConfigurationSelection)
  }

  changeConnectorType() {
    let conf = this.currentConfiguration;
    if (conf) {
      this.changeConf(conf, this.configurations, false);
    } else {
      this.selectionChange.emit(new SelectBotEvent(this.currentBotName, true));
      this.configurationIdChange.emit(null);
    }
  }
}

export class SelectBotEvent {

  constructor(
    public configurationName: string,
    public all: boolean,
    public configurationId?: string
  ) {

  }

  equals(e: SelectBotEvent): boolean {
    return e
      && this.configurationId === e.configurationId
      && this.all === e.all
      && this.configurationName === e.configurationName
  }
}

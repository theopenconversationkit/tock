/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

import { Component, Input, OnInit } from '@angular/core';
import { NbComponentStatus, NbDialogRef } from '@nebular/theme';
import { BotApplicationConfiguration, BotConfiguration } from '../../../core/model/configuration';
import { BotConfigurationService } from '../../../core/bot-configuration.service';

@Component({
  selector: 'tock-bot-configuration-dialog',
  templateUrl: './select-bot-configuration-dialog.component.html',
  styleUrls: ['./select-bot-configuration-dialog.component.css']
})
export class SelectBotConfigurationDialogComponent implements OnInit {
  @Input()
  title: string;
  selectedConfig: BotConfiguration;

  botApplicationConfigurations: BotConfiguration[];
  valid = true;

  constructor(private dialogRef: NbDialogRef<SelectBotConfigurationDialogComponent>, private botConfiguration: BotConfigurationService) {}

  ngOnInit() {
    this.load();
  }

  private load() {
    this.botConfiguration.configurations.subscribe((applicationConfigurations) => {
      const configsByName = this.groupByName(applicationConfigurations);
      const bots = this.botConfiguration.bots.getValue();
      this.botApplicationConfigurations = Array.from(configsByName.values()).map((connectorConfigurations) => {
        const existingConf = bots.find((botConfig) => botConfig.name === connectorConfigurations[0].name);
        if (existingConf) {
          existingConf.configurations = connectorConfigurations;
          return existingConf;
        }
        const firstBotConfiguration = connectorConfigurations[0];
        return new BotConfiguration(
          firstBotConfiguration.botId,
          firstBotConfiguration.name,
          firstBotConfiguration.namespace,
          firstBotConfiguration.nlpModel,
          connectorConfigurations
        );
      });
    });
  }

  private groupByName(applicationConfigurations: BotApplicationConfiguration[]) {
    const botConfigurationsByName = new Map<string, BotApplicationConfiguration[]>();
    applicationConfigurations.forEach((configuration) => {
      const configs = botConfigurationsByName.get(configuration.name);
      if (!configs) {
        botConfigurationsByName.set(configuration.name, [configuration]);
      } else {
        configs.push(configuration);
      }
    });
    return botConfigurationsByName;
  }

  close() {
    this.dialogRef.close();
  }

  submit() {
    this.valid = this.selectedConfig != null;
    if (this.valid) {
      this.dialogRef.close(this.selectedConfig);
    }
  }

  validationStatus(): NbComponentStatus {
    return this.valid ? 'basic' : 'danger';
  }
}

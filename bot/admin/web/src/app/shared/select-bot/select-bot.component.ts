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
@Component({
  selector: 'tock-select-bot',
  templateUrl: './select-bot.component.html',
  styleUrls: ['./select-bot.component.css']
})
export class SelectBotComponent implements OnInit {

  @Input()
  configurationId: string;

  @Output()
  private configurationIdChange = new EventEmitter<string>();

  constructor(public botConfiguration: BotConfigurationService) {
  }

  ngOnInit() {
    this.botConfiguration.restConfigurations
      .subscribe(conf => {
        setTimeout(_ => {
          if (conf.length !== 0) {
            this.changeConfiguration(conf[0]._id);
          } else {
            this.changeConfiguration(null);
          }
        });
      });
  }

  changeConfiguration(applicationConfigurationId: string) {
    this.configurationId = applicationConfigurationId;
    this.configurationIdChange.emit(applicationConfigurationId);
  }
}

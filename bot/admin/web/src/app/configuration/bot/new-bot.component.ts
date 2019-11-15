/*
 * Copyright (C) 2017/2019 VSCT
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

import {Component, OnInit} from "@angular/core";
import {StateService} from "../../core-nlp/state.service";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {BotApplicationConfiguration, ConnectorType} from "../../core/model/configuration";
import {BotSharedService} from "../../shared/bot-shared.service";
import {Router} from "@angular/router";
import {Application} from "../../model/application";
import {ApplicationService} from "../../core-nlp/applications.service";
import {BotConfigurationService} from "../../core/bot-configuration.service";

@Component({
  selector: 'tock-new-bot',
  templateUrl: './new-bot.component.html',
  styleUrls: ['./new-bot.component.css']
})
export class NewBotComponent implements OnInit {

  firstFormGroup: FormGroup;
  secondFormGroup: FormGroup;
  channel: FormControl = new FormControl('', Validators.required);

  connectorTypes: ConnectorType[] = [];

  constructor(
    private _formBuilder: FormBuilder,
    public state: StateService,
    private botSharedService: BotSharedService,
    private router: Router,
    private applicationService: ApplicationService,
    private botConfiguration: BotConfigurationService) {
  }

  ngOnInit() {
    this
      .botSharedService
      .getConnectorTypes()
      .subscribe(
        confConf => {
          const c = confConf.map(it => it.connectorType);
          this.connectorTypes = c.filter(co => !co.isRest()).sort((a, b) => a.id.localeCompare(b.id));
        });
    this.firstFormGroup = this._formBuilder.group({
      firstCtrl: ['', Validators.required]
    });
    this.secondFormGroup = this._formBuilder.group({});
    this.secondFormGroup.registerControl("channel", this.channel)
  }

  validate() {
    const locale = this.firstFormGroup.value.firstCtrl;
    const channel = this.channel.value;
    const newApp = new Application("new_assistant", this.state.user.organization, [], [locale], StateService.DEFAULT_ENGINE, true, true, false);
    this.applicationService.saveApplication(newApp)
      .subscribe(app => {
        this.applicationService.refreshCurrentApplication(app);
        const path = this.botConfiguration.findValidPath(channel);
        const conf = new BotApplicationConfiguration(
          this.state.currentApplication.name,
          this.state.currentApplication.name,
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          channel,
          this.state.currentApplication.name,
          new Map<string, string>(),
          null,
          null,
          null,
          path,
          true
        );
        this.botConfiguration.saveConfiguration(conf)
          .subscribe(_ => {
            this.botConfiguration.updateConfigurations();
            this.router.navigateByUrl("/nlp/try");
          });

      });
  }

}

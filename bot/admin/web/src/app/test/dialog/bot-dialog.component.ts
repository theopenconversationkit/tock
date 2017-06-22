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

import {Component, OnDestroy, OnInit} from "@angular/core";
import {TestService} from "../test.service";
import {StateService} from "tock-nlp-admin/src/app/core/state.service";
import {RestService} from "tock-nlp-admin/src/app/core/rest/rest.service";
import {BotDialogRequest, TestMessage} from "../model/test";
import {BotConfigurationService} from "../../core/bot-configuration.service";
import {BotApplicationConfiguration} from "../../core/model/configuration";
import {BehaviorSubject} from "rxjs/BehaviorSubject";
import {ReplaySubject} from "rxjs/ReplaySubject";

@Component({
  selector: 'tock-bot-dialog',
  templateUrl: './bot-dialog.component.html',
  styleUrls: ['./bot-dialog.component.css']
})
export class BotDialogComponent implements OnInit, OnDestroy {

  currentConfigurationId: string;

  userMessage: string = "";
  messages: TestMessage[] = [];

  loading: boolean;

  private errorUnsuscriber: any;

  constructor(private state: StateService,
              private test: TestService,
              private rest: RestService,
              public botConfiguration: BotConfigurationService) {
  }

  ngOnInit() {
    this.load();
    this.errorUnsuscriber = this.rest.errorEmitter.subscribe(e =>
      this.loading = false
    )
  }

  load() {
    this.botConfiguration.configurations
      .subscribe(conf => {
        if (conf.length !== 0) {
          this.currentConfigurationId = conf[0]._id;
        } else {
          this.currentConfigurationId = null;
        }
      })
  }

  changeConfiguration(applicationConfigurationId: string) {
    this.userMessage = "";
    this.messages = [];
    this.loading = false;
  }

  submit() {
    let m = this.userMessage;
    if (!m || m.trim().length === 0) {
      return;
    }
    m = m.trim();
    this.messages.push(new TestMessage(false, m));
    this.userMessage = "";
    this.loading = true;
    this.test
      .talk(
        new BotDialogRequest(
          this.currentConfigurationId,
          m,
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale))
      .subscribe(r => {
        this.loading = false;
        r.messages.forEach(m => {
          this.messages.push(new TestMessage(true, null, m));
        });
      });
  }

  ngOnDestroy() {
    this.errorUnsuscriber.unsubscribe();
  }

}

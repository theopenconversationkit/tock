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

import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { TestService } from '../test.service';
import { StateService } from '../../core-nlp/state.service';
import { RestService } from '../../core-nlp/rest/rest.service';
import { BotDialogRequest, TestMessage, XRayTestPlan } from '../model/test';
import { BotMessage, Sentence } from '../../shared/model/dialog-data';
import { BotSharedService } from '../../shared/bot-shared.service';
import { SelectBotEvent } from '../../shared/select-bot/select-bot.component';
import { randomString } from '../../model/commons';
import { Subscription } from 'rxjs';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { SentenceFilter } from '../../sentences-scroll/sentences-scroll.component';
import { NbToastrService } from '@nebular/theme';
import { AnalyticsService } from '../../analytics/analytics.service';
import { APP_BASE_HREF } from '@angular/common';

@Component({
  selector: 'tock-bot-dialog',
  templateUrl: './bot-dialog.component.html',
  styleUrls: ['./bot-dialog.component.css']
})
export class BotDialogComponent implements OnInit, OnDestroy {
  currentConfigurationId: string;

  userMessage: string = '';
  messages: TestMessage[] = [];

  xrayAvailable: boolean = false;
  xrayTestName: string = '';
  isXrayTestNameFilled: boolean = false;
  jiraIdentifier: string = '';
  xrayTestPlans: XRayTestPlan[];
  selectTestPlans: string[];
  filter: SentenceFilter = new SentenceFilter();

  xrayTestIdentifier: string = '';
  isXrayTestIdentifierFilled: boolean = false;

  loading: boolean;
  private userModifierId: string = randomString();

  private errorUnsuscriber: any;
  private subscription: Subscription;
  testContext = false;

  constructor(
    public state: StateService,
    private test: TestService,
    private rest: RestService,
    private shared: BotSharedService,
    private toastrService: NbToastrService,
    private dialog: MatDialog,
    @Inject(APP_BASE_HREF) public baseHref: string
  ) {}

  ngOnInit() {
    this.errorUnsuscriber = this.rest.errorEmitter.subscribe((e) => (this.loading = false));
    this.subscription = this.state.configurationChange.subscribe((_) => this.clear());
    this.fillTestPlanFilter();
  }

  private fillTestPlanFilter() {
    this.shared.getConfiguration().subscribe((r) => {
      this.xrayAvailable = r.xrayAvailable;
      if (r.xrayAvailable) {
        this.test.getXrayTestPlans().subscribe((testPlans) => {
          this.xrayTestPlans = testPlans;
        });
      }
    });
  }

  changeConfiguration(selectBotEvent: SelectBotEvent) {
    this.userMessage = '';
    this.messages = [];
    this.loading = false;
    this.currentConfigurationId = selectBotEvent ? selectBotEvent.configurationId : null;
  }

  onNewMessage(message: BotMessage) {
    this.talk(message);
  }

  submit() {
    if (!this.currentConfigurationId) {
      this.toastrService.show(`Please select a Bot first`, 'Error', { duration: 3000 });
      return;
    }
    let m = this.userMessage;
    if (!m || m.trim().length === 0) {
      return;
    }
    m = m.trim();
    this.talk(new Sentence(0, [], m));
  }

  private talk(message: BotMessage) {
    const userAction = new TestMessage(false, message);
    this.messages.push(userAction);
    this.userMessage = '';
    this.loading = true;
    this.test
      .talk(
        new BotDialogRequest(
          this.currentConfigurationId,
          message,
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale,
          this.userModifierId
        )
      )
      .subscribe((r) => {
        this.loading = false;
        userAction.locale = r.userLocale;
        userAction.hasNlpStats = r.hasNlpStats;
        userAction.actionId = r.userActionId;
        r.messages.forEach((m) => {
          this.messages.push(new TestMessage(true, m));
        });
      });
  }

  displayNlpStats(m: TestMessage) {
    this.shared.getNlpDialogStats(m.actionId).subscribe((r) => {
      this.dialog.open(DisplayNlpStatsComponent, {
        data: {
          request: r.nlpQueryAsJson(),
          response: r.nlpResultAsJson()
        }
      });
    });
  }

  clear() {
    this.messages = [];
    this.userModifierId = randomString();
    this.clearTestControl();
  }

  clearTestControl() {
    this.testContext = false;
    this.xrayTestName = '';
    this.jiraIdentifier = '';
  }

  ngOnDestroy() {
    this.errorUnsuscriber.unsubscribe();
  }

  enableTestContext() {
    if (this.testContext == false) {
      this.talk(new Sentence(0, [], '_test_'));
      this.testContext = true;
    } else {
      this.clearTestControl();
      this.talk(new Sentence(0, [], '_end_test_'));
    }
  }

  updateSaveButtonStatus($event: any) {
    this.isXrayTestNameFilled = $event != '';
  }

  updateUpdateButtonStatus($event: any) {
    this.isXrayTestIdentifierFilled = $event != '';
  }

  saveDialogToXray() {
    let jiraPart: string = this.jiraIdentifier != '' ? ', ' + this.jiraIdentifier : '';
    this.talk(new Sentence(0, [], '_xray_ ' + this.xrayTestName + jiraPart));
  }

  updateDialogXray() {
    this.talk(new Sentence(0, [], '_xray_update_ ' + this.xrayTestIdentifier));
  }

  printSelectedTestPlans($event: string[] | string) {}

  removeXrayTestIdentifier() {
    this.xrayTestIdentifier = '';
    this.isXrayTestIdentifierFilled = false;
  }

  removeXrayTestName() {
    this.xrayTestName = '';
    this.isXrayTestNameFilled = false;
  }
}

@Component({
  selector: 'tock-display-nlp-stats',
  template: `<h1 mat-dialog-title>Nlp Stats</h1>
    <div mat-dialog-content>
      Request:
      <pre>{{ data.request }}</pre>
      Response:
      <pre>{{ data.response }}</pre>
    </div>
    <div mat-dialog-actions>
      <button
        mat-raised-button
        mat-dialog-close
        color="primary"
      >
        Close
      </button>
    </div>`
})
export class DisplayNlpStatsComponent {
  constructor(public dialogRef: MatDialogRef<DisplayNlpStatsComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {}
}

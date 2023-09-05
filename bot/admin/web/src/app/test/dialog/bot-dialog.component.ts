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

import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { TestService } from '../test.service';
import { StateService } from '../../core-nlp/state.service';
import { RestService } from '../../core-nlp/rest/rest.service';
import { BotDialogRequest, TestMessage, XRayTestPlan } from '../model/test';
import { BotMessage, Sentence } from '../../shared/model/dialog-data';
import { BotSharedService } from '../../shared/bot-shared.service';
import { SelectBotEvent } from '../../shared/select-bot/select-bot.component';
import { PaginatedQuery, randomString } from '../../model/commons';
import { Observable, of, Subject, take, takeUntil } from 'rxjs';
import { SentenceFilter } from '../../sentences-scroll/sentences-scroll.component';
import { NbDialogService, NbToastrService } from '@nebular/theme';

import { SearchQuery } from '../../model/nlp';
import { ChatUiComponent } from '../../shared/components';
import { NlpService } from '../../nlp-tabs/nlp.service';
import { NlpStatsDisplayComponent } from './nlp-stats-display/nlp-stats-display.component';

@Component({
  selector: 'tock-bot-dialog',
  templateUrl: './bot-dialog.component.html',
  styleUrls: ['./bot-dialog.component.scss']
})
export class BotDialogComponent implements OnInit, OnDestroy {
  destroy = new Subject();

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

  testContext = false;

  _debug: boolean = false;

  set debug(value: boolean) {
    this._debug = value;
    this.shared.session_storage = { ...this.shared.session_storage, ...{ test: { debug: value } } };
  }
  get debug() {
    return this._debug;
  }

  @ViewChild('chatUi') private chatUi: ChatUiComponent;

  constructor(
    public state: StateService,
    private test: TestService,
    private rest: RestService,
    private shared: BotSharedService,
    private toastrService: NbToastrService,
    private nbDialogService: NbDialogService,
    private nlp: NlpService
  ) {}

  ngOnInit() {
    this.rest.errorEmitter.pipe(takeUntil(this.destroy)).subscribe((e) => (this.loading = false));
    this.state.configurationChange.pipe(takeUntil(this.destroy)).subscribe((_) => this.clear());
    this.fillTestPlanFilter();
    this.getRecentSentences();

    if (this.shared.session_storage?.test?.debug) {
      this._debug = this.shared.session_storage.test.debug;
    }
  }

  private fillTestPlanFilter(): void {
    this.shared.getConfiguration().subscribe((r) => {
      this.xrayAvailable = r.xrayAvailable;
      if (r.xrayAvailable) {
        this.test.getXrayTestPlans().subscribe((testPlans) => {
          this.xrayTestPlans = testPlans;
        });
      }
    });
  }

  changeConfiguration(selectBotEvent: SelectBotEvent): void {
    this.userMessage = '';
    this.messages = [];
    this.loading = false;
    this.currentConfigurationId = selectBotEvent ? selectBotEvent.configurationId : null;
  }

  onNewMessage(message: BotMessage): void {
    this.talk(message);
  }

  submit(): void {
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

  private talk(message: BotMessage): void {
    this.userMessageAutocompleteValues = of([]);
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
        ),
        this.debug
      )
      .subscribe((r) => {
        this.loading = false;
        userAction.locale = r.userLocale;
        userAction.hasNlpStats = r.hasNlpStats;
        userAction.actionId = r.userActionId;
        r.messages.forEach((m) => {
          this.messages.push(new TestMessage(true, m, undefined, undefined, undefined));

          setTimeout(() => this.chatUi.scrollToBottom());
        });
      });
  }

  displayNlpStats(m: TestMessage): void {
    this.shared.getNlpDialogStats(m.actionId).subscribe((response) => {
      this.nbDialogService.open(NlpStatsDisplayComponent, {
        context: {
          data: {
            request: response.nlpQueryAsJson(),
            response: response.nlpResultAsJson()
          }
        }
      });
    });
  }

  displayDebug(message: TestMessage): void {}

  clear(): void {
    this.messages = [];
    this.userModifierId = randomString();
    this.clearTestControl();
  }

  clearTestControl(): void {
    this.testContext = false;
    this.xrayTestName = '';
    this.jiraIdentifier = '';
  }

  enableTestContext(): void {
    if (this.testContext == false) {
      this.talk(new Sentence(0, [], '_test_'));
      this.testContext = true;
    } else {
      this.clearTestControl();
      this.talk(new Sentence(0, [], '_end_test_'));
    }
  }

  updateSaveButtonStatus($event: any): void {
    this.isXrayTestNameFilled = $event != '';
  }

  updateUpdateButtonStatus($event: any): void {
    this.isXrayTestIdentifierFilled = $event != '';
  }

  saveDialogToXray(): void {
    let jiraPart: string = this.jiraIdentifier != '' ? ', ' + this.jiraIdentifier : '';
    this.talk(new Sentence(0, [], '_xray_ ' + this.xrayTestName + jiraPart));
  }

  updateDialogXray(): void {
    this.talk(new Sentence(0, [], '_xray_update_ ' + this.xrayTestIdentifier));
  }

  printSelectedTestPlans($event: string[] | string): void {}

  removeXrayTestIdentifier(): void {
    this.xrayTestIdentifier = '';
    this.isXrayTestIdentifierFilled = false;
  }

  removeXrayTestName(): void {
    this.xrayTestName = '';
    this.isXrayTestNameFilled = false;
  }

  getUserAvatar(isBot: boolean): string {
    if (isBot) return this.userIdentities.bot.avatar;
    return this.userIdentities.client.avatar;
  }

  userIdentities = {
    client: { name: 'Human', avatar: 'assets/images/scenario-client.svg' },
    bot: { name: 'Bot', avatar: 'assets/images/scenario-bot.svg' }
  };

  recentSentences: string[];
  userMessageAutocompleteValues: Observable<string[]>;

  getRecentSentences(): void {
    const cursor: number = 0;
    const pageSize: number = 50;
    const mark = null;
    const paginatedQuery: PaginatedQuery = this.state.createPaginatedQuery(cursor, pageSize, mark);
    const searchQuery = new SearchQuery(
      paginatedQuery.namespace,
      paginatedQuery.applicationName,
      paginatedQuery.language,
      paginatedQuery.start,
      paginatedQuery.size,
      paginatedQuery.searchMark
    );
    this.nlp
      .searchSentences(searchQuery)
      .pipe(take(1))
      .subscribe((res) => {
        this.recentSentences = res.rows.map((r) => r.text);
      });
  }

  updateUserMessageAutocompleteValues(event?: KeyboardEvent): void {
    if (this.loading) return;

    let results = this.recentSentences;

    if (event) {
      const targetEvent = event.target as HTMLInputElement;
      results = results.filter((sentence: string) => sentence.toLowerCase().includes(targetEvent.value.trim().toLowerCase()));
    }

    this.userMessageAutocompleteValues = of(results);
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}

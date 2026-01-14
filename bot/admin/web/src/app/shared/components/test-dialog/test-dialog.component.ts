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

import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Observable, Subject, of, take, takeUntil } from 'rxjs';
import { PaginatedQuery, randomString } from '../../../model/commons';
import { StateService } from '../../../core-nlp/state.service';
import { SearchQuery } from '../../../model/nlp';
import { NlpService } from '../../../core-nlp/nlp.service';
import { getDialogMessageUserAvatar } from '../../utils';
import { BotMessage, Sentence } from '../../model/dialog-data';
import { NbDialogService, NbToastrService, NbWindowRef, NbWindowState } from '@nebular/theme';
import { BotDialogRequest, BotDialogResponse, TestMessage } from '../../../test/model/test';

import { BotSharedService } from '../../bot-shared.service';
import { ChatUiComponent } from '../chat-ui/chat-ui.component';
import { NlpStatsDisplayComponent } from '../../../test/dialog/nlp-stats-display/nlp-stats-display.component';
import { RestService } from '../../../core-nlp/rest/rest.service';
import { ReplayDialogActionType, TestDialogService } from './test-dialog.service';
import { BotConfigurationService } from '../../../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../../../core/model/configuration';
import { currentConfigurationSelection } from '../bot-configuration-selector/bot-configuration-selector.component';
import { TextareaAutocompleteDirective } from '../../directives';

@Component({
  selector: 'tock-test-dialog',
  templateUrl: './test-dialog.component.html',
  styleUrl: './test-dialog.component.scss'
})
export class TestDialogComponent implements OnInit, OnDestroy {
  destroy = new Subject();

  loading: boolean = true;

  testQueryInProgress: boolean;

  configurations: BotApplicationConfiguration[];
  currentConfigurationId: string;

  locale: string;

  userMessage: string = '';
  messages: TestMessage[] = [];

  showOptions: boolean = false;

  userModifierId: string = randomString();

  recentSentences: string[];
  userMessageAutocompleteValues: Observable<string[]>;

  _debug: boolean = false;

  set debug(value: boolean) {
    this._debug = value;
    this.shared.session_storage = { ...this.shared.session_storage, ...{ test: { ...this.shared.session_storage?.test, debug: value } } };
  }
  get debug() {
    return this._debug;
  }

  _sourceWithContent: boolean = false;

  set sourceWithContent(value: boolean) {
    this._sourceWithContent = value;
    this.shared.session_storage = {
      ...this.shared.session_storage,
      ...{ test: { ...this.shared.session_storage?.test, sourceWithContent: value } }
    };
  }
  get sourceWithContent() {
    return this._sourceWithContent;
  }

  @ViewChild('chatUi') private chatUi: ChatUiComponent;

  @ViewChild(TextareaAutocompleteDirective) textareaAutocompleteDirectiveRef: TextareaAutocompleteDirective<HTMLTextAreaElement>;

  constructor(
    private botConfiguration: BotConfigurationService,
    public state: StateService,
    private nlp: NlpService,
    private toastrService: NbToastrService,
    private shared: BotSharedService,
    private nbDialogService: NbDialogService,
    private rest: RestService,
    private testDialogService: TestDialogService,
    protected windowRef: NbWindowRef
  ) {}

  ngOnInit() {
    this.state.configurationChange.pipe(takeUntil(this.destroy)).subscribe((res) => {
      if (this.locale != this.state.currentLocale) this.locale = this.state.currentLocale;
    });

    this.locale = this.state.currentLocale;

    this.rest.errorEmitter.pipe(takeUntil(this.destroy)).subscribe((e) => (this.loading = false));

    this.botConfiguration.configurations.pipe(takeUntil(this.destroy)).subscribe((confs) => {
      this.loading = true;

      this.configurations = confs;

      this.initCurrentConfiguration();

      this.getRecentSentences();
    });

    if (this.shared.session_storage?.test?.debug) {
      this._debug = this.shared.session_storage.test.debug;
    }

    if (this.shared.session_storage?.test?.sourceWithContent) {
      this._sourceWithContent = this.shared.session_storage.test.sourceWithContent;
    }

    this.windowRef.stateChange.pipe(takeUntil(this.destroy)).subscribe((change) => {
      if (change.newState === NbWindowState.MINIMIZED) {
        this.testDialogService.storeMessages(this.messages);
      }

      if ([NbWindowState.MAXIMIZED, NbWindowState.FULL_SCREEN].includes(change.newState)) {
        const storedMessages = this.testDialogService.getStoredMessages();
        if (storedMessages?.length > 0) {
          this.messages = storedMessages;
          setTimeout(() => this.chatUi.scrollToBottom());
        }
      }
    });

    this.testDialogService.defineLocaleObservable.pipe(takeUntil(this.destroy)).subscribe((locale) => {
      this.locale = locale;
    });

    this.testDialogService.defineApplicationIdObservable.pipe(takeUntil(this.destroy)).subscribe((applicationId) => {
      this.setCurrentConfigurationByApplicationId(applicationId);
    });

    this.testDialogService.testSentenceObservable.pipe(takeUntil(this.destroy)).subscribe((sentenceText) => {
      this.talk(new Sentence(0, [], sentenceText));
    });
  }

  initCurrentConfiguration(): void {
    const previousConfigurationId = this.currentConfigurationId;

    if (this.configurations.length) {
      const retainedConfs = this.configurations
        .filter((c) => c.targetConfigurationId == null)
        .sort((c1, c2) => c1.applicationId.localeCompare(c2.applicationId));

      const currentConf = retainedConfs[0];
      const currentConfRest = BotApplicationConfiguration.getRestConfiguration(this.configurations, currentConf);

      this.currentConfigurationId = currentConfRest._id;

      if (previousConfigurationId !== this.currentConfigurationId) {
        this.clear();
      }
    }
  }

  setCurrentConfigurationByApplicationId(applicationId: string): void {
    let requiredApplication = this.configurations.find((a) => a.applicationId === applicationId);
    if (requiredApplication) {
      if (!requiredApplication.targetConfigurationId) {
        requiredApplication = this.configurations.find((a) => a.targetConfigurationId === requiredApplication._id);
      }

      this.currentConfigurationId = requiredApplication._id;
    }
  }

  changeCurrentConfiguration(newConfiguration: currentConfigurationSelection): void {
    this.currentConfigurationId = newConfiguration.restConfiguration._id;
  }

  swapOptions(): void {
    this.showOptions = !this.showOptions;
  }

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
        this.loading = false;
      });
  }

  updateUserMessageAutocompleteValues(event?: KeyboardEvent): void {
    if (this.loading) return;

    let results = this.recentSentences;

    if (event) {
      const targetEvent = event.target as HTMLInputElement;
      results = results.filter((sentence: string) => sentence.toLowerCase().includes(targetEvent.value.trim().toLowerCase()));
      this.updateMessageInputHeight(targetEvent);
    }

    this.userMessageAutocompleteValues = of(results);

    if (event && event.key !== 'Escape') this.textareaAutocompleteDirectiveRef.updatePosition();
  }

  updateMessageInputHeight(element): void {
    if (element) {
      element.style.height = 'auto';
      element.style.height = Math.min(element.scrollHeight + 2, 250) + 'px';
    }
  }

  getUserAvatar(isBot: boolean): string {
    return getDialogMessageUserAvatar(isBot);
  }

  onUserMessageChange(value: string): void {
    this.userMessage = value ?? '';
  }

  insertCarriage(): void {
    this.onUserMessageChange(this.userMessage + '\n');
  }

  onNewMessage(message: BotMessage): void {
    this.talk(message);
  }

  submit(event?: Event): void {
    if (event) event.preventDefault();

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

    setTimeout(() => {
      this.textareaAutocompleteDirectiveRef.hide();
    });
  }

  talkRequest(query: BotDialogRequest, debug: boolean = false, sourceWithContent: boolean = false): Observable<BotDialogResponse> {
    let params: { debug?: boolean; sourceWithContent?: boolean } = {};
    if (debug) params.debug = true;
    if (sourceWithContent) params.sourceWithContent = true;
    return this.rest.post('/test/talk', query, BotDialogResponse.fromJSON, null, false, params);
  }

  private talk(message: BotMessage): void {
    this.userMessageAutocompleteValues = of([]);
    const userAction = new TestMessage(false, message);
    this.messages.push(userAction);
    this.userMessage = '';
    this.testQueryInProgress = true;
    this.talkRequest(
      new BotDialogRequest(
        this.currentConfigurationId,
        message,
        this.state.currentApplication.namespace,
        this.state.currentApplication.name,
        this.locale,
        this.userModifierId
      ),
      this.debug,
      this.sourceWithContent
    )
      .pipe(take(1))
      .subscribe({
        next: (r) => {
          delete this.testQueryInProgress;

          userAction.locale = r.userLocale;
          userAction.hasNlpStats = r.hasNlpStats;
          userAction.actionId = r.userActionId;
          r.messages.forEach((m) => {
            this.messages.push(new TestMessage(true, m, undefined, undefined, undefined));

            setTimeout(() => this.chatUi.scrollToBottom());
          });

          this.testDialogService.replayDialogNext(ReplayDialogActionType.RESULT_SUCCESS);
        },
        error: (error) => {
          delete this.testQueryInProgress;

          const errorMessage = new Sentence(0, undefined, 'An error occured');
          this.messages.push(new TestMessage(true, errorMessage, undefined, undefined, undefined));

          this.testDialogService.replayDialogNext(ReplayDialogActionType.RESULT_ERROR);
        }
      });
  }

  clear(): void {
    this.messages = [];
    this.userModifierId = randomString();
  }

  displayNlpStats(m: TestMessage): void {
    this.shared
      .getNlpDialogStats(m.actionId)
      .pipe(take(1))
      .subscribe((response) => {
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

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }

  normalizeLocaleCode(code: string): string {
    return StateService.normalizeLocaleCode(code);
  }
}

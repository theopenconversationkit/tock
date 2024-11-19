import { Component, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
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
import { SelectBotEvent } from '../select-bot/select-bot.component';
import { TestDialogService } from './test-dialog.service';
import { BotConfigurationService } from '../../../core/bot-configuration.service';
import { BotApplicationConfiguration, ConnectorType } from '../../../core/model/configuration';

@Component({
  selector: 'tock-test-dialog',
  templateUrl: './test-dialog.component.html',
  styleUrl: './test-dialog.component.scss'
})
export class TestDialogComponent implements OnInit, OnDestroy {
  destroy = new Subject();

  loading: boolean = true;

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

    this.testDialogService.defineConnectorTypeObservable.pipe(takeUntil(this.destroy)).subscribe((connectorType) => {
      this.setCurrentConfigurationByConnectorType(connectorType);
    });

    this.testDialogService.testSentenceObservable.pipe(takeUntil(this.destroy)).subscribe((sentenceText) => {
      // this.talk(new Sentence(0, [], sentenceText));
    });
  }

  setCurrentConfigurationByApplicationId(applicationId: string): void {
    let requiredApplication = this.configurations.find((a) => a.applicationId === applicationId);
    if (requiredApplication) {
      console.log(this.configurations);
      console.log(requiredApplication);

      if (requiredApplication.targetConfigurationId) {
        requiredApplication = this.configurations.find((a) => a._id === requiredApplication.targetConfigurationId);
      }

      this.currentConfigurationId = requiredApplication._id;
    }
  }

  setCurrentConfigurationByConnectorType(connectorType: ConnectorType): void {
    // console.log(connectorType);
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
    }

    this.userMessageAutocompleteValues = of(results);
  }

  getUserAvatar(isBot: boolean): string {
    return getDialogMessageUserAvatar(isBot);
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
    this.loading = true;
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

  clear(): void {
    this.messages = [];
    this.userModifierId = randomString();
  }

  changeConfiguration(selectBotEvent: SelectBotEvent): void {
    if (selectBotEvent?.configurationId !== this.currentConfigurationId) {
      this.messages = [];
      this.currentConfigurationId = selectBotEvent.configurationId;
    }

    this.loading = false;
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
}

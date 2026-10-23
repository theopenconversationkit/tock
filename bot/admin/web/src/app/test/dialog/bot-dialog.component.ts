import { ChangeDetectorRef, Component, NgZone, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { TestService } from '../test.service';
import { StateService } from '../../core-nlp/state.service';
import { RestService } from '../../core-nlp/rest/rest.service';
import { BotDialogRequest, TestMessage, XRayTestPlan } from '../model/test';
import { BotMessage, Sentence, ActionReport, PlayerId, PlayerType } from '../../shared/model/dialog-data';
import { BotSharedService } from '../../shared/bot-shared.service';
import { PaginatedQuery, randomString } from '../../model/commons';
import { Observable, of, Subject, take, takeUntil } from 'rxjs';

import { NbDialogService, NbToastrService } from '@nebular/theme';

import { SearchQuery, SentenceStatus } from '../../model/nlp';
import { ChatUiComponent, SelectBotEvent } from '../../shared/components';
import { NlpService } from '../../core-nlp/nlp.service';
import { NlpStatsDisplayComponent } from './nlp-stats-display/nlp-stats-display.component';
import { getDialogMessageUserAvatar } from '../../shared/utils';
import { TranslocoService } from '@jsverse/transloco';

export class SentenceFilter {
  constructor(
    public search?: string,
    public intentId?: string,
    public status?: SentenceStatus[],
    public entityType?: string,
    public entityRolesToInclude: string[] = [],
    public entityRolesToExclude: string[] = [],
    public modifiedAfter?: Date,
    public modifiedBefore?: Date,
    public onlyToReview: boolean = false,
    public searchSubEntities: boolean = false,
    public user?: string,
    public allButUser?: string,
    public maxIntentProbability: number = 100,
    public minIntentProbability: number = 0,
    public configuration?: string
  ) {}

  clone(): SentenceFilter {
    return new SentenceFilter(
      this.search,
      this.intentId,
      this.status,
      this.entityType,
      this.entityRolesToInclude,
      this.entityRolesToInclude,
      this.modifiedAfter,
      this.modifiedBefore,
      this.onlyToReview,
      this.searchSubEntities,
      this.user,
      this.allButUser,
      this.maxIntentProbability,
      this.minIntentProbability,
      this.configuration
    );
  }
}

@Component({
    selector: 'tock-bot-dialog',
    templateUrl: './bot-dialog.component.html',
    styleUrls: ['./bot-dialog.component.scss'],
    standalone: false
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
  testQueryInProgress: boolean;
  private userModifierId: string = randomString();

  testContext = false;

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

  _streaming: boolean = false;

  set streaming(value: boolean) {
    this._streaming = value;
    this.shared.session_storage = {
      ...this.shared.session_storage,
      ...{ test: { ...this.shared.session_storage?.test, streaming: value } }
    };
  }
  get streaming() {
    return this._streaming;
  }

  @ViewChild('chatUi') private chatUi: ChatUiComponent;

  constructor(
    public state: StateService,
    private test: TestService,
    private rest: RestService,
    private shared: BotSharedService,
    private toastrService: NbToastrService,
    private nbDialogService: NbDialogService,
    private nlp: NlpService,
    private transloco: TranslocoService,
    private cd: ChangeDetectorRef,
    private ngZone: NgZone
  ) {}

  ngOnInit() {
    this.rest.errorEmitter.pipe(takeUntil(this.destroy)).subscribe((e) => (this.loading = false));

    this.state.configurationChange.pipe(takeUntil(this.destroy)).subscribe((_) => {
      this.loading = true;
      this.clear();
      this.fillTestPlanFilter();
      this.getRecentSentences();
    });

    this.fillTestPlanFilter();
    this.getRecentSentences();

    if (this.shared.session_storage?.test?.debug) {
      this._debug = this.shared.session_storage.test.debug;
    }

    if (this.shared.session_storage?.test?.sourceWithContent) {
      this._sourceWithContent = this.shared.session_storage.test.sourceWithContent;
    }

    if (this.shared.session_storage?.test?.streaming) {
      this._streaming = this.shared.session_storage.test.streaming;
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

  getUserMessageInputHeight() {
    const linHeight = 1.5;
    const padding = 2 * 0.5;
    return linHeight * this.userMessage.split(/\n/).length + padding + 'rem';
  }

  onUserMessageChange(value: string) {
    this.userMessage = value ?? '';
  }

  insertCarriage() {
    this.onUserMessageChange(this.userMessage + '\n');
  }

  submit(): void {
    if (!this.currentConfigurationId) {
      this.toastrService.show(
        this.transloco.translate('test.bot-dialog.selectBotFirstError'),
        this.transloco.translate('test.bot-dialog.errorTitle'),
        { duration: 3000 }
      );
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
    this.testQueryInProgress = true;

    const request = new BotDialogRequest(
      this.currentConfigurationId,
      message,
      this.state.currentApplication.namespace,
      this.state.currentApplication.name,
      this.state.currentLocale,
      this.userModifierId
    );

    if (this.streaming) {
      this.talkStreaming(request, userAction);
    } else {
      this.loading = true;
      this.test
        .talk(request, this.debug, this.sourceWithContent)
        .subscribe((r) => {
          this.loading = false;
          delete this.testQueryInProgress;
          userAction.locale = r.userLocale;
          userAction.hasNlpStats = r.hasNlpStats;
          userAction.actionId = r.userActionId;
          let ragDebugAttached = false;
          r.messages.forEach((m) => {
            const botAction = new ActionReport(
              new PlayerId('bot', PlayerType.bot),
              new Date(),
              m,
              r.userActionId || randomString(),
              true,
              undefined,
              this.state.currentApplication._id
            );
            const hasContent = (m as any).text?.trim().length > 0 || (m as any).messages?.length > 0;
            if (r.ragDebug && !ragDebugAttached && hasContent) {
              botAction.ragDebug = r.ragDebug;
              ragDebugAttached = true;
            }
            this.messages.push(new TestMessage(true, m, undefined, undefined, undefined, botAction));

            setTimeout(() => this.chatUi.scrollToBottom());
          });
        });
    }
  }

  private talkStreaming(request: BotDialogRequest, userAction: TestMessage): void {
    let accumulatedTokens = '';
    let currentBotMessage: TestMessage | null = null;

    this.test.talkStream(request, this.debug, this.sourceWithContent)
      .pipe(takeUntil(this.destroy))
      .subscribe({
        next: (event) => {
          if (event.event === 'token') {
            try {
              const tokenData = JSON.parse(event.data || '{}');
              accumulatedTokens += tokenData.content || '';

              this.ngZone.run(() => {
                if (!currentBotMessage) {
                  currentBotMessage = new TestMessage(true, new Sentence(0, [], accumulatedTokens), undefined, undefined, undefined);
                  this.messages.push(currentBotMessage);
                } else {
                  currentBotMessage.message = new Sentence(0, [], accumulatedTokens);
                }
                this.cd.detectChanges();
              });

              setTimeout(() => this.chatUi.scrollToBottom());
            } catch (e) {
              console.error('Error parsing token event', e);
            }
          } else if (event.event === 'answer') {
            try {
              delete this.testQueryInProgress;

              const answerData = JSON.parse(event.data || '{}');
              userAction.locale = answerData.userLocale;
              userAction.hasNlpStats = answerData.hasNlpStats;
              userAction.actionId = answerData.userActionId;

              if (answerData.messages && answerData.messages.length > 0) {
                if (currentBotMessage) {
                  this.messages.pop();
                  currentBotMessage = null;
                }
                let ragDebugAttached = false;
                answerData.messages.forEach((m: any) => {
                  const parsedMessage = BotMessage.fromJSON(m);
                  const botAction = new ActionReport(
                    new PlayerId('bot', PlayerType.bot),
                    new Date(),
                    parsedMessage,
                    answerData.userActionId || randomString(),
                    true,
                    undefined,
                    this.state.currentApplication._id
                  );
                  const hasContent = (m as any).text?.trim().length > 0 || (m as any).messages?.length > 0;
                  if (answerData.ragDebug && !ragDebugAttached && hasContent) {
                    botAction.ragDebug = answerData.ragDebug;
                    ragDebugAttached = true;
                  }
                  this.messages.push(new TestMessage(true, parsedMessage, undefined, undefined, undefined, botAction));
                });
              }

              setTimeout(() => this.chatUi.scrollToBottom());
            } catch (e) {
              console.error('Error parsing answer event', e);
            }
          } else if (event.event === 'error') {
            delete this.testQueryInProgress;

            try {
              const errorData = JSON.parse(event.data || '{}');
              const errorMessage = new Sentence(0, undefined, errorData.error || 'An error occurred');
              this.messages.push(new TestMessage(true, errorMessage, undefined, undefined, undefined));
            } catch (e) {
              const errorMessage = new Sentence(0, undefined, 'An error occurred');
              this.messages.push(new TestMessage(true, errorMessage, undefined, undefined, undefined));
            }
          }
        },
        error: (error) => {
          delete this.testQueryInProgress;

          const errorMessage = new Sentence(0, undefined, 'Connection error: ' + (error?.message || 'Unknown error'));
          this.messages.push(new TestMessage(true, errorMessage, undefined, undefined, undefined));
        },
        complete: () => {
          delete this.testQueryInProgress;
        }
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
    return getDialogMessageUserAvatar(isBot);
  }

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

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}

import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { ActionReport, Debug, DialogReport, SentenceWithFootnotes } from '../../../shared/model/dialog-data';
import { ConnectorType } from '../../../core/model/configuration';
import { StateService } from '../../../core-nlp/state.service';
import { DialogReportQuery } from '../dialogs';
import { AnalyticsService } from '../../analytics.service';
import { BotConfigurationService } from '../../../core/bot-configuration.service';
import { ActivatedRoute, Router, UrlSegment } from '@angular/router';
import { BotSharedService } from '../../../shared/bot-shared.service';
import { PaginatedQuery, SearchMark } from '../../../model/commons';
import { BehaviorSubject, Observable, Subject, filter, mergeMap, take, takeUntil } from 'rxjs';
import { PaginatedResult, Sentence } from '../../../model/nlp';
import { saveAs } from 'file-saver-es';
import { getDialogMessageUserAvatar, getDialogMessageUserQualifier, getExportFileName } from '../../../shared/utils';

export class DialogFilter {
  constructor(
    public exactMatch: boolean,
    public displayTests: boolean,
    public dialogId?: string,
    public text?: string,
    public intentName?: string,
    public connectorType?: ConnectorType,
    public ratings?: number[],
    public configuration?: string,
    public intentsToHide?: string[],
    public isGenAiRagDialog?: boolean
  ) {}
}

@Component({
  selector: 'tock-dialogs-list',
  templateUrl: './dialogs-list.component.html',
  styleUrls: ['./dialogs-list.component.scss']
})
export class DialogsListComponent implements OnInit, OnChanges, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  totalDialogsCount: BehaviorSubject<string> = new BehaviorSubject<string>('');

  loading: boolean = false;

  total: number = -1;
  cursor: number = 0;
  pageSize: number = 10;
  mark: SearchMark;
  add: boolean = true;

  data: DialogReport[] = [];

  filter: DialogFilter = new DialogFilter(true, false);

  connectorTypes: ConnectorType[] = [];

  configurationNameList: string[];

  private loaded = false;

  @Input() ratingFilter: number[];

  intents: string[];

  dialogReportQuery: DialogReportQuery;

  constructor(
    public state: StateService,
    private analytics: AnalyticsService,
    private botConfiguration: BotConfigurationService,
    private route: ActivatedRoute,
    public botSharedService: BotSharedService,
    private router: Router
  ) {
    this.state = state;

    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((configs) => {
      this.isSatisfactionRoute().subscribe((res) => {
        this.botSharedService.getIntentsByApplication(this.state.currentApplication._id).subscribe((intents) => (this.intents = intents));

        this.configurationNameList = configs.filter((item) => item.targetConfigurationId == null).map((item) => item.applicationId);

        if (res) {
          this.ratingFilter = [1, 2, 3, 4, 5];
        }
        this.refresh();
      });
    });

    this.botSharedService
      .getConnectorTypes()
      .pipe(take(1))
      .subscribe((confConf) => {
        this.connectorTypes = confConf.map((it) => it.connectorType);
      });
  }

  ngOnInit() {
    this.load();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['ratingFilter'].currentValue != changes['ratingFilter'].previousValue) {
      this.refresh();
    }
  }

  refresh() {
    this.cursor = 0;
    this.loading = false;
    this.total = -1;
    this.mark = null;
    this.data = [];
    this.load();
  }

  onScroll() {
    this.load();
  }

  load() {
    if (!this.loading && (this.total === -1 || this.total > this.cursor)) {
      this.loading = true;
      const init = this.total === -1;
      this.search(this.paginatedQuery()).subscribe((s) => this.loadResults(s, init));
    }
  }

  protected paginatedQuery(): PaginatedQuery {
    return this.state.createPaginatedQuery(this.cursor, this.pageSize, this.mark);
  }

  search(query: PaginatedQuery): Observable<PaginatedResult<DialogReport>> {
    this.dialogReportQuery = new DialogReportQuery(
      query.namespace,
      query.applicationName,
      query.language,
      query.start,
      query.size,
      this.filter.exactMatch,
      null,
      this.filter.dialogId,
      this.filter.text,
      this.filter.intentName,
      this.filter.connectorType,
      this.filter.displayTests,
      this.ratingFilter,
      this.filter.configuration,
      this.filter.intentsToHide,
      this.filter.isGenAiRagDialog
    );

    return this.route.queryParams.pipe(
      mergeMap((params) => {
        if (!this.loaded) {
          if (params['dialogId']) this.filter.dialogId = params['dialogId'];
          if (params['text']) this.filter.text = params['text'];
          if (params['intentName']) this.filter.intentName = params['intentName'];
          this.loaded = true;
        }
        return this.analytics.dialogs(this.dialogReportQuery);
      })
    );
  }

  protected loadResults(result: PaginatedResult<DialogReport>, init: boolean): boolean {
    //skip parallel initialization
    if (init && this.data.length !== 0) {
      return false;
    }
    if (this.add) {
      Array.prototype.push.apply(this.data, result.rows);
    } else {
      this.data = result.rows;
    }
    this.cursor = result.end;
    this.total = result.total;
    this.totalDialogsCount.next(this.formattedTotal());
    this.loading = false;

    return true;
  }

  formattedTotal() {
    return this.total !== 1000000 ? this.total.toString() : this.total + '+';
  }

  waitAndRefresh() {
    setTimeout((_) => this.refresh());
  }

  dataEquals(d1: DialogReport, d2: DialogReport): boolean {
    return d1.id === d2.id;
  }

  viewAllWithThisText() {
    this.filter.dialogId = null;
    this.refresh();
  }

  isSatisfactionRoute() {
    return this.route.url.pipe(
      filter((val: UrlSegment[]) => {
        return val[0].path == 'satisfaction';
      })
    );
  }

  exportDialogs() {
    const exportFileName = getExportFileName(
      this.state.currentApplication.namespace,
      this.state.currentApplication.name,
      'dialogs_with_rating',
      'csv'
    );
    this.analytics.downloadDialogsCsv(this.dialogReportQuery).subscribe((blob) => {
      saveAs(blob, exportFileName);
    });

    const exportFileName2 = getExportFileName(
      this.state.currentApplication.namespace,
      this.state.currentApplication.name,
      'dialogs_with_rating_and_intents',
      'csv'
    );
    this.analytics.downloadDialogsWithIntentsCsv(this.dialogReportQuery).subscribe((blob) => {
      saveAs(blob, exportFileName2);
    });
  }

  getUserName(action: ActionReport): string {
    return getDialogMessageUserQualifier(action.isBot());
  }

  getUserAvatar(action: ActionReport): string {
    return getDialogMessageUserAvatar(action.isBot());
  }

  createFaq(action: ActionReport, actionsStack: ActionReport[]) {
    const actionIndex = actionsStack.findIndex((act) => act === action);
    if (actionIndex > 0) {
      const answerSentence = action.message as unknown as SentenceWithFootnotes;
      const answer = answerSentence.text;

      let question;
      const questionAction = actionsStack[actionIndex - 1];

      if (questionAction.message.isDebug()) {
        const actionDebug = questionAction.message as unknown as Debug;
        question = actionDebug.data.condense_question || actionDebug.data.user_question;
      } else if (!questionAction.isBot()) {
        const questionSentence = questionAction.message as unknown as Sentence;
        question = questionSentence.text;
      }

      if (question && answer) {
        this.router.navigate(['faq/management'], { state: { question, answer } });
      }
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}

import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges} from '@angular/core';
import { Observable, of } from 'rxjs';
import { ReplaySubject } from 'rxjs';
import { Subject } from 'rxjs';
import {filter, map, switchMap, takeUntil } from 'rxjs/operators';
import { AnalyticsService } from 'src/app/analytics/analytics.service';
import { DialogReportQuery } from 'src/app/analytics/dialogs/dialogs';
import { StateService } from 'src/app/core-nlp/state.service';
import { DialogReport, Sentence } from 'src/app/shared/model/dialog-data';

type RETAIN_MODE = 'ONLY_BOT' | 'ONLY_USER' | 'BOTH';

function noAccents(value?: string): string {
  if (!value) {
    return value;
  }
  // see https://stackoverflow.com/questions/5700636/using-javascript-to-perform-text-matches-with-without-accented-characters
  return value.normalize('NFD').replace(/[\u0300-\u036f]/g, "");
}

@Component({
  selector: 'tock-train-sidebar',
  templateUrl: './train-sidebar.component.html',
  styleUrls: ['./train-sidebar.component.scss']
})
export class TrainSidebarComponent implements OnInit, OnDestroy, OnChanges {

  @Input()
  sentence?: Sentence;

  @Output()
  onClose = new EventEmitter<void>();

  @Output()
  onSentenceSelect = new EventEmitter<string>();

  public dialogReports: DialogReport[] = [];


  public retainMode: RETAIN_MODE = 'BOTH';

  private readonly sentenceChanged$ = new ReplaySubject<Sentence>(1);
  public readonly dialogChanged$ = new Subject<DialogReport>();

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);

  constructor(
    private readonly analyticsService: AnalyticsService,
    private readonly state: StateService
  ) { }

  ngOnInit(): void {
    this.observeSentenceConversation();

    if (this.sentence) {
      this.sentenceChanged$.next(this.sentence);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['sentence']){
      const sentence = changes['sentence'].currentValue;
      this.sentenceChanged$.next(sentence);
    }
  }

  toggleOnlyUser():  void {
    if (this.retainMode === 'ONLY_USER') {
      this.retainMode = 'BOTH';
    } else {
      this.retainMode = 'ONLY_USER';
    }
  }

  toggleOnlyBot():  void {
    if (this.retainMode === 'ONLY_BOT') {
      this.retainMode = 'BOTH';
    } else {
      this.retainMode = 'ONLY_BOT';
    }
  }

  isOnlyBot(): boolean {
    return (this.retainMode === 'ONLY_BOT');
  }

  isOnlyUser(): boolean {
    return (this.retainMode === 'ONLY_USER');
  }

  isBothAgents(): boolean {
    return (this.retainMode === 'BOTH');
  }

  mustRetain(isBot: boolean): boolean {
    return (this.retainMode === 'BOTH') ||
      (this.retainMode === 'ONLY_BOT' && isBot) ||
      (this.retainMode === 'ONLY_USER' && !isBot);
  }

  close(): void {
    this.onClose.emit();
  }

  dialogNumber(dialog: DialogReport): number {
    return this.dialogReports.indexOf(dialog) + 1;
  }

  selectDialog(dialogReport: DialogReport, index: number): void {
    this.dialogChanged$.next(dialogReport);
  }

  debugThat(truc: any): string {
    console.log("debug", truc);
    return "truc";
  }

  observeSentenceConversation(): void {
    this.sentenceChanged$
      .pipe(
        takeUntil(this.destroy$),
        filter(value => !!value), // only truthly values
        switchMap(this.fetchDialogs.bind(this)),
      ).subscribe(reports => {

        this.dialogReports = reports;
        this.dialogChanged$.next(reports[0]);
    });
  }

  private buildDialogQuery(sentenceText: string): DialogReportQuery {
    const app = this.state.currentApplication;
    const language = this.state.currentLocale;
    return new DialogReportQuery(
      app.namespace,
      app.name,
      language,
      0,
      99999,
      true,
      null,
      null,
      sentenceText
    );
  }

  fetchDialogs(sentence: Sentence): Observable<DialogReport[]> {
    const query: DialogReportQuery = this.buildDialogQuery(sentence.text);

    return this.analyticsService.dialogs(query)
      .pipe(
        map(res => res.rows)
      );
  }

  isSelectedSentence(messageText?: string): boolean {
    if (!messageText) {
      return false;
    }

    return noAccents(messageText) === noAccents(this.sentence.text);
  }

  isSelectedComplexSentence(texts?: Map<String, string>): boolean {
    if (!texts) {
      return false;
    }

    return noAccents(texts.get('text')) === noAccents(this.sentence.text);
  }

  sentenceSelect(value: string, isBot?: boolean): void {
    if (isBot) {
      return;
    }
    this.onSentenceSelect.next(value || '');
  }
}

import { ChangeDetectorRef, Component, ElementRef, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges } from '@angular/core';
import { Subject } from 'rxjs';
import { take } from 'rxjs/operators';

import { AnalyticsService } from '../../../../analytics/analytics.service';
import { DialogReportQuery } from '../../../../analytics/dialogs/dialogs';
import { StateService } from '../../../../core-nlp/state.service';
import { ActionReport, Debug, DialogReport, Sentence, SentenceWithFootnotes } from '../../../../shared/model/dialog-data';
import { SentenceExtended } from '../sentence-training.component';
import { getDialogMessageUserAvatar, getDialogMessageUserQualifier } from '../../../utils';
import { Router } from '@angular/router';

@Component({
  selector: 'tock-sentence-training-dialog',
  templateUrl: './sentence-training-dialog.component.html',
  styleUrls: ['./sentence-training-dialog.component.scss']
})
export class SentenceTrainingDialogComponent implements OnChanges, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  @Input() sentence!: SentenceExtended;
  @Output() onClose = new EventEmitter();
  @Output() onSearchSentence = new EventEmitter();

  constructor(
    private state: StateService,
    private readonly analyticsService: AnalyticsService,
    private readonly elementRef: ElementRef,
    private cd: ChangeDetectorRef,
    private router: Router
  ) {}

  dialogs: DialogReport[] = [];
  displayedDialog: DialogReport;
  displayedDialogIndex: number = 0;

  close(): void {
    this.onClose.emit();
  }

  displayDialog(index: number): void {
    this.displayedDialogIndex = index;
    this.displayedDialog = this.dialogs[this.displayedDialogIndex];
    this.scrollToCurrent();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (
      !changes.sentence?.previousValue ||
      (changes.sentence?.currentValue && changes.sentence.currentValue.text != changes.sentence.previousValue.text)
    ) {
      this.sentence = changes.sentence.currentValue;

      const query: DialogReportQuery = this.buildDialogQuery(this.sentence.text);
      this.analyticsService
        .dialogs(query)
        .pipe(take(1))
        .subscribe((res) => {
          this.dialogs = res.rows;
          this.displayedDialogIndex = 0;
          this.displayedDialog = this.dialogs[this.displayedDialogIndex];
          this.scrollToCurrent();
          this.cd.markForCheck();
        });
    }
  }

  updateSentence(sentence: SentenceExtended): void {
    this.sentence = sentence;
  }

  scrollToCurrent(): void {
    window.setTimeout(() => {
      const nativeElement: HTMLElement = this.elementRef.nativeElement;
      const found: Element | null = nativeElement.querySelector('.currentsentence');
      if (found) {
        found.scrollIntoView({
          behavior: 'smooth',
          block: 'nearest',
          inline: 'start'
        });
      }
    }, 200);
  }

  private buildDialogQuery(sentenceText: string): DialogReportQuery {
    const app = this.state.currentApplication;
    const language = this.state.currentLocale;
    return new DialogReportQuery(app.namespace, app.name, language, 0, 99999, true, null, null, sentenceText);
  }

  isCurrentSentence(action: ActionReport): boolean {
    if (action.isBot()) return false;
    if (action.message.isSentence()) {
      // Hack because the use of fromJSON static functions doesn't instanciate correctly classes and so here action.message is only of type BotMessage
      let msg = action.message as unknown as Sentence;
      return msg.text == this.sentence.text;
    }
    return false;
  }

  getUserName(action: ActionReport): string {
    return getDialogMessageUserQualifier(action.isBot());
  }

  getUserAvatar(action: ActionReport): string {
    return getDialogMessageUserAvatar(action.isBot());
  }

  searchSentence(action: ActionReport): void {
    if (action.isBot()) return;
    if (action.message.isSentence()) {
      this.onSearchSentence.emit(action.message);
    }
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

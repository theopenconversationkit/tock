import { Component, EventEmitter, Input, OnDestroy, Output } from '@angular/core';
import { ActionReport, Debug, DialogReport, Sentence, SentenceWithFootnotes } from '../../../model/dialog-data';
import { getDialogMessageUserAvatar, getDialogMessageUserQualifier } from '../../../utils';
import { NbDialogService } from '@nebular/theme';
import { TestDialogService } from '../../test-dialog/test-dialog.service';
import { ReportComponent } from '../../report/report.component';
import { Router } from '@angular/router';
import { StateService } from '../../../../core-nlp/state.service';
import { Subject } from 'rxjs';

@Component({
  selector: 'tock-chat-ui-dialog-logger',

  templateUrl: './chat-ui-dialog-logger.component.html',
  styleUrl: './chat-ui-dialog-logger.component.scss'
})
export class ChatUiDialogLoggerComponent implements OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  @Input() dialog: DialogReport;

  @Input() userMessageIsClickable?: boolean;
  @Input() botMessageIsClickable?: boolean;

  @Output() onMessageClicked = new EventEmitter();

  @Input() highlightedAction?: ActionReport;

  constructor(
    private testDialogService: TestDialogService,
    private nbDialogService: NbDialogService,
    private router: Router,
    public state: StateService
  ) {}

  getUserName(action: ActionReport): string {
    return getDialogMessageUserQualifier(action.isBot());
  }

  getUserAvatar(action: ActionReport): string {
    return getDialogMessageUserAvatar(action.isBot());
  }

  jumpToDialog(dialogId: string, actionId: string) {
    this.router.navigate(
      [`analytics/dialogs/${this.state.currentApplication.namespace}/${this.state.currentApplication._id}/${dialogId}`],
      {
        fragment: actionId
      }
    );
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

  testDialogSentence(action: ActionReport) {
    // TO DO : pass locale when it will be present in the message
    this.testDialogService.testSentenceDialog({
      sentenceText: (action.message as unknown as Sentence).text,
      applicationId: action.applicationId
      // sentenceLocale: action.message.locale
    });
  }

  replayDialog() {
    this.testDialogService.replayDialog(this.dialog);
  }

  // containsReport(action: ActionReport): boolean {
  //   return (parseInt(action.id) % 10) % 2 === 0;
  // }

  // openReport(action: ActionReport) {
  //   this.nbDialogService.open(ReportComponent, {
  //     context: {
  //       actionReport: action
  //     }
  //   });
  // }

  openObservabilityDetails(action: ActionReport) {
    window.open(action.metadata.observabilityInfo.traceUrl, '_blank');
  }

  messageClicked(action: ActionReport): void {
    this.onMessageClicked.emit(action);
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}

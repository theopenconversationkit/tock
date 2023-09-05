import { Component, ElementRef, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges } from '@angular/core';
import { Subject } from 'rxjs';
import { take } from 'rxjs/operators';

import { AnalyticsService } from '../../../analytics/analytics.service';
import { DialogReportQuery } from '../../../analytics/dialogs/dialogs';
import { StateService } from '../../../core-nlp/state.service';
import { ActionReport, DialogReport, Sentence } from '../../../shared/model/dialog-data';
import { SentenceExtended } from '../faq-training.component';

@Component({
  selector: 'tock-faq-training-dialog',
  templateUrl: './faq-training-dialog.component.html',
  styleUrls: ['./faq-training-dialog.component.scss']
})
export class FaqTrainingDialogComponent implements OnChanges, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  @Input() sentence!: SentenceExtended;
  @Output() onClose = new EventEmitter();
  @Output() onSearchSentence = new EventEmitter();

  constructor(private state: StateService, private readonly analyticsService: AnalyticsService, private readonly elementRef: ElementRef) {}

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
    if (action.isBot()) return this.userIdentities.bot.name;
    return this.userIdentities.client.name;
  }

  getUserAvatar(action: ActionReport): string {
    if (action.isBot()) return this.userIdentities.bot.avatar;
    return this.userIdentities.client.avatar;
  }

  userIdentities = {
    client: { name: 'Human', avatar: 'assets/images/scenario-client.svg' },
    bot: { name: 'Bot', avatar: 'assets/images/scenario-bot.svg' }
  };

  searchSentence(action: ActionReport): void {
    if (action.isBot()) return;
    if (action.message.isSentence()) {
      this.onSearchSentence.emit(action.message);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}

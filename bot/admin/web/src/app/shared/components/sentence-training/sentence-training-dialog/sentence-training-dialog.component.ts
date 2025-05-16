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
      const found: Element | null = nativeElement.querySelector('.highlightedAction');
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

  getCurrentSentenceAction(): ActionReport {
    return this.displayedDialog?.actions.find((action) => {
      return this.isCurrentSentence(action);
    });
  }

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

/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

import {
  Component,
  ElementRef,
  EventEmitter,
  Input, NgZone,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {Observable, ReplaySubject, Subject} from 'rxjs';
import {filter, map, switchMap, takeUntil} from 'rxjs/operators';
import {AnalyticsService} from 'src/app/analytics/analytics.service';
import {DialogReportQuery} from 'src/app/analytics/dialogs/dialogs';
import {StateService} from 'src/app/core-nlp/state.service';
import {DialogReport, Sentence} from 'src/app/shared/model/dialog-data';
import {noAccents} from '../../common/util/string-utils';

type RETAIN_MODE = 'ONLY_BOT' | 'ONLY_USER' | 'BOTH';

@Component({
  selector: 'tock-train-sidepanel',
  templateUrl: './train-sidepanel.component.html',
  styleUrls: ['./train-sidepanel.component.scss']
})
export class TrainSidepanelComponent implements OnInit, OnDestroy, OnChanges {

  @Input()
  sentence?: Sentence;

  @Output()
  onClose = new EventEmitter<void>();

  @Output()
  onSentenceSelect = new EventEmitter<string>();

  public dialogReports: DialogReport[] = [];
  public selectedIndex = 0;


  public retainMode: RETAIN_MODE = 'BOTH';

  private readonly sentenceChanged$ = new ReplaySubject<Sentence>(1);
  public readonly dialogChanged$ = new Subject<DialogReport>();

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);

  constructor(
    private readonly analyticsService: AnalyticsService,
    private readonly state: StateService,
    private readonly elementRef: ElementRef,
    private readonly zone: NgZone
  ) {
  }

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
    if (changes['sentence']) {
      const sentence = changes['sentence'].currentValue;
      this.sentenceChanged$.next(sentence);
    }
  }

  toggleOnlyUser(): void {
    if (this.retainMode === 'ONLY_USER') {
      this.retainMode = 'BOTH';
    } else {
      this.retainMode = 'ONLY_USER';
    }
  }

  toggleOnlyBot(): void {
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

  // hacky function:
  scrollIntoFirstActiveItem() {
    const nativeElement: HTMLElement = this.elementRef.nativeElement;

    // because we access directly DOM element without mutating a state that Angular use (scroll position)
    // this way there will be no need of dirty check from Angular side
    this.zone.runOutsideAngular(() => {
      window.setTimeout(() => {
        const found: Element | null = nativeElement.querySelector(".bubble-container.tock--active");
        if (found) {
          found.scrollIntoView({ "block": "center", "behavior": "smooth"});
        }
      }, 200); // wait angular rendered things
    })
  }

  selectDialog(dialogReport: DialogReport, index: number): void {
    this.selectedIndex = index;
    this.dialogChanged$.next(dialogReport);
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

      this.scrollIntoFirstActiveItem();
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

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
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output
} from '@angular/core';
import { Router } from '@angular/router';
import {SelectionModel} from "@angular/cdk/collections";
import {Observable, of, ReplaySubject} from "rxjs";
import {delay, take, tap} from 'rxjs/operators';

import {Intent, Sentence, SentenceStatus} from "../../../model/nlp";
import {StateService} from "../../../core-nlp/state.service";
import {DialogService} from "../../../core-nlp/dialog.service";
import {SentencesService} from "../../common/sentences.service";
import {SelectionMode} from "../../common/model/selection-mode";
import {truncate} from "../../common/util/string-utils";
import {isDocked, ViewMode} from "../../common/model/view-mode";
import { UserRole } from '../../../model/auth';

@Component({
  selector: 'tock-train-grid-item',
  templateUrl: './train-grid-item.component.html',
  styleUrls: ['./train-grid-item.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush // better perfs when list is huge
})
export class TrainGridItemComponent implements OnInit, OnDestroy {

  @Input()
  sentence: Sentence;

  @Input()
  intents: Intent[];

  @Input()
  selection: SelectionModel<Sentence>;

  @Input()
  selectionMode: SelectionMode;

  @Input()
  viewMode: ViewMode;

  @Output()
  onRemove = new EventEmitter<boolean>();

  @Output()
  onValidate = new EventEmitter<boolean>();

  @Output()
  onUnknown = new EventEmitter<boolean>();

  @Output()
  onDetails = new EventEmitter<Sentence>();

  @Output()
  onToggle= new EventEmitter<boolean>();

  selectedIntentId: string;

  public selectedIntent = {
    placeholder: '',
    probability: null
  };

  public cardCssClass = "tock--opened"; // card closing animation

  public UserRole = UserRole;

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);

  constructor(
    private readonly ref: ChangeDetectorRef,
    private readonly dialog: DialogService,
    private readonly router: Router,
    private readonly sentencesService: SentencesService,
    public readonly state: StateService
  ) {}

  ngOnInit(): void {
    this.selectedIntent = {
      placeholder: this.sentence.getIntentLabel(this.state),
      probability: Math.trunc(this.sentence.classification.intentProbability * 100)
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  public redirectToFaqManagement(): void {
    this.router.navigate(['faq/qa'], { state: { question: this.sentence.getText() }})
  }

  public async validate(): Promise<void> {
    const intentId = this.sentence.classification.intentId;
    if (!intentId) {
      this.dialog.notify(`Please select an intent first`);
      return;
    }

    if (intentId === Intent.unknown) {
      this.sentence.classification.intentId = Intent.unknown;
      this.sentence.classification.entities = [];
    }
    this.sentence.status = SentenceStatus.validated;

    await this.sentencesService.save(this.sentence, this.destroy$)
      .pipe(take(1))
      .toPromise();

    this.dialog.notify(`Validated`,
      truncate(this.sentence.text), {duration: 2000, status: "basic"});

    this.hide().subscribe(_ => {
      this.onValidate.emit(true);
    });
  }

  public async unknown(): Promise<void> {
    this.sentence.classification.intentId = Intent.unknown;
    this.sentence.classification.entities = [];
    this.sentence.status = SentenceStatus.validated;

    await this.sentencesService.save(this.sentence, this.destroy$)
      .pipe(take(1))
      .toPromise();

    this.dialog.notify(`Unknown`,
      truncate(this.sentence.text), {duration: 2000, status: "basic"});

    this.hide().subscribe(_ => {
      this.onUnknown.emit(true);
    });
  }

  public async remove(): Promise<void> {
    this.sentence.status = SentenceStatus.deleted;

    await this.sentencesService.save(this.sentence, this.destroy$)
      .pipe(take(1))
      .toPromise();

    this.dialog.notify(`Deleted`,
      truncate(this.sentence.text), {duration: 2000, status: "basic"});


    this.hide().subscribe(_ => {
      this.onRemove.emit(true);
    });
  }

  public details(): void {
    this.onDetails.emit(this.sentence);
  }

  private hide(): Observable<boolean> {
    this.cardCssClass = 'tock--closed';

    return of(true)
      .pipe(
        delay(500),
        tap(_ => this.cardCssClass = 'tock--hidden')
      );
  }

  toggle(): void {
    this.selection.toggle(this.sentence);

    // tell to transition from SELECT_ALL to SELECT_SOME or from SELECT_NONE to SELECT_SOME
    if (this.selection.isSelected(this.sentence)) {
      this.onToggle.next(true);
    } else {
      this.onToggle.next(false);
    }
  }

  changeIntent(intentId: string): void {
    const prevSentence = this.sentence;
    this.sentence = this.sentence.withIntent(this.state, intentId);
    this.ref.detectChanges(); // because ChangeDetectionStrategy is OnPush

    this.updateSelection(prevSentence, this.sentence);
  }

  updateSelection(prevSentence: Sentence, newSentence: Sentence): void {
    // we update version of sentence in selection set, so that bulk actions could reuse selection directly

    if (this.selection.isSelected(prevSentence)) { // when previously it was selected
      this.selection.toggle(prevSentence); // remove previous version from selection set
      this.selection.select(newSentence); // add new version to selection set

    } else { // when previously it was not selected, a business rule says we now want it selected
      this.selection.select(newSentence); // add new version to selection set
      this.onToggle.next(true); // tell upper components to transition to SELECT_SOME
    }
  }

  isSelected(): boolean {
    switch (this.selectionMode) {
      case SelectionMode.SELECT_ALWAYS:
        return true;
      case SelectionMode.SELECT_NEVER:
        return false;
      case SelectionMode.SELECT_SOME:
      default:
        return this.selection.isSelected(this.sentence);
    }
  }

  isDocked(): boolean {
    return isDocked(this.viewMode);
  }
}

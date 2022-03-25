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

import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {SentenceStatus} from 'src/app/model/nlp';
import {FilterOption} from 'src/app/search/filter/search-filter.component';
import {DEFAULT_FAQ_SENTENCE_SORT, FaqSentenceFilter, TrainGridComponent} from './train-grid/train-grid.component';
import {StateService} from "../../core-nlp/state.service";
import {Sentence} from "../../model/nlp";
import {ReplaySubject} from 'rxjs';
import {WithSidePanel} from '../common/mixin/with-side-panel';
import {takeUntil} from "rxjs/operators";

@Component({
  selector: 'tock-train',
  templateUrl: './train.component.html',
  styleUrls: ['./train.component.scss']
})
export class TrainComponent extends WithSidePanel() implements OnInit, OnDestroy {

  selectedSentence?: Sentence;

  public filter: FaqSentenceFilter;
  @ViewChild(TrainGridComponent) grid;

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);

  constructor(
    private readonly state: StateService
  ) {
    super();
  }

  ngOnInit(): void {
    this.clearFilter();

    this.initSidePanel(this.destroy$);

    // clear things when app change
    this.state.currentApplicationEmitter
      .pipe(takeUntil(this.destroy$))
      .subscribe(_ => {
        this.selectedSentence = undefined;
        this.clearFilter();
        this.grid.refresh(); // seems no need, but to be secure
        this.undock()
      });
  }

  ngOnDestroy() {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  clearFilter(): void {
    this.filter = {
      sort: [DEFAULT_FAQ_SENTENCE_SORT],
      maxIntentProbability: 100,
      minIntentProbability: 0,
      onlyToReview: false,
      search: null,
      status: [SentenceStatus.inbox],
      clone: function () {
        return {...this};
      }
    };
  }

  details(sentence: Sentence): void {
    if (!this.isDocked()) {
      this.selectedSentence = sentence;
      this.dock();
    } else if (this.selectedSentence?.text === sentence.text) {
      this.undock();
    } else {
      this.selectedSentence = sentence;
    }
  }

  search(filter: Partial<FaqSentenceFilter>): void {
    this.filter.search = filter.search;
    this.filter.sort = filter.sort;
    this.grid.refresh();
  }

  sentenceSelect(sentence: string): void {
    this.filter.search = sentence.trim();
    this.filter = {...this.filter, clone: this.filter.clone}; // trigger detection change
  }
}

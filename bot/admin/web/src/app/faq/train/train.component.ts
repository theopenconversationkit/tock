import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {SentenceStatus} from 'src/app/model/nlp';
import {FilterOption} from 'src/app/search/filter/search-filter.component';
import {DEFAULT_FAQ_SENTENCE_SORT, FaqSentenceFilter, TrainGridComponent} from './train-grid/train-grid.component';
import {StateService} from "../../core-nlp/state.service";
import {Sentence} from "../../model/nlp";
import {ViewMode, toggleSmallScreenMode, toggleWideScreenMode, isDocked, dock, undock} from "../common/model/view-mode";
import { fromEvent, Observable, ReplaySubject } from 'rxjs';
import { debounceTime, takeUntil } from 'rxjs/operators';
import {WithSidePanel} from '../common/mixin/with-side-panel';

@Component({
  selector: 'tock-train',
  templateUrl: './train.component.html',
  styleUrls: ['./train.component.scss']
})
export class TrainComponent extends WithSidePanel() implements OnInit, OnDestroy {

  NO_INTENT_FILTER = new FilterOption('-1', 'All');
  UNKNOWN_INTENT_FILTER = new FilterOption('tock:unknown', 'Unknown');

  selectedSentence?: Sentence;
  applicationName: string;

  public filter: FaqSentenceFilter;
  @ViewChild(TrainGridComponent) grid;

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);

  constructor(
    private readonly state: StateService
  ) {
    super();
  }

  ngOnInit(): void {
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

    this.applicationName = this.state.currentApplication.name;

    this.initSidePanel(this.destroy$);
  }

  ngOnDestroy() {
    this.destroy$.next(true);
    this.destroy$.complete();
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
    this.filter = { ...this.filter, clone: this.filter.clone }; // trigger detection change

    //this.grid.refresh();
  }
}

import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {SentenceStatus} from 'src/app/model/nlp';
import {FilterOption} from 'src/app/search/filter/search-filter.component';
import {DEFAULT_FAQ_SENTENCE_SORT, FaqSentenceFilter, TrainGridComponent} from './train-grid/train-grid.component';
import {StateService} from "../../core-nlp/state.service";
import {Sentence} from "../../model/nlp";
import {ViewMode, toggleSmallScreenMode, toggleWideScreenMode, isDocked, dock, undock} from "../common/model/view-mode";
import { fromEvent, ReplaySubject } from 'rxjs';
import { debounceTime, takeUntil } from 'rxjs/operators';

@Component({
  selector: 'tock-train',
  templateUrl: './train.component.html',
  styleUrls: ['./train.component.scss']
})
export class TrainComponent implements OnInit, OnDestroy {

  NO_INTENT_FILTER = new FilterOption('-1', 'All');
  UNKNOWN_INTENT_FILTER = new FilterOption('tock:unknown', 'Unknown');

  viewMode: ViewMode = 'FULL_WIDTH';

  selectedSentence?: Sentence;
  applicationName: string;

  public filter: FaqSentenceFilter;
  @ViewChild(TrainGridComponent) grid;

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);

  constructor(
    private readonly state: StateService
  ) {
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

    this.adjustViewMode(); // check actual screen width
    this.observeWindowWidth();
  }

  ngOnDestroy() {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  private observeWindowWidth(): void {
    const screenSizeChanged$ = fromEvent(window, 'resize')
      .pipe(takeUntil(this.destroy$), debounceTime(1000))
      .subscribe(this.adjustViewMode.bind(this));
  }

  private adjustViewMode(): void {
    console.log("window width", window.innerWidth);

    if (window.innerWidth < 1620) {
      this.viewMode = toggleSmallScreenMode(this.viewMode);
    } else {
      this.viewMode = toggleWideScreenMode(this.viewMode);
    }
  }

  details(sentence: Sentence): void {
    if (this.viewMode === 'FULL_WIDTH') {
      this.dock(sentence);
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

  dock(sentence: Sentence): void {
    this.selectedSentence = sentence;
    this.viewMode = dock(this.viewMode);
  }

  undock(): void {
    this.selectedSentence = null;
    this.viewMode = undock(this.viewMode);
  }

  isDocked(): boolean {
    return isDocked(this.viewMode);
  }

  sentenceSelect(sentence: string): void {
    this.filter.search = sentence.trim();
    this.filter = { ...this.filter, clone: this.filter.clone }; // trigger detection change

    //this.grid.refresh();
  }
}

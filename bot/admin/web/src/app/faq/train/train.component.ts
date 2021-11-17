import { Component, OnInit, ViewChild } from '@angular/core';
import { SentenceStatus } from 'src/app/model/nlp';
import { FilterOption } from 'src/app/search/filter/search-filter.component';
import {FaqSentenceFilter, TrainGridComponent} from './train-grid/train-grid.component';

@Component({
  selector: 'tock-train',
  templateUrl: './train.component.html',
  styleUrls: ['./train.component.scss']
})
export class TrainComponent implements OnInit {

  NO_INTENT_FILTER = new FilterOption('-1', 'All');
  UNKNOWN_INTENT_FILTER = new FilterOption('tock:unknown', 'Unknown');

  public filter: FaqSentenceFilter;

  @ViewChild(TrainGridComponent) grid;

  constructor() { }

  ngOnInit(): void {
    this.filter =   {
      maxIntentProbability: 100,
      minIntentProbability: 0,
      onlyToReview: false,
      search: null,
      status: [SentenceStatus.inbox],
      clone: function () {
        return {... this};
      }
    };
  }

  search(filter: Partial<FaqSentenceFilter>): void {

    this.filter.search = filter.search;

    this.grid.refresh();
  }

}

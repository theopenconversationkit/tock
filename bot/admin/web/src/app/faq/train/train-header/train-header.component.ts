import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import {FaqSentenceFilter} from "../train-grid/train-grid.component";
import {SentenceStatus} from "../../../model/nlp";

@Component({
  selector: 'tock-train-header',
  templateUrl: './train-header.component.html',
  styleUrls: ['./train-header.component.scss']
})
export class TrainHeaderComponent implements OnInit {

  public filter: FaqSentenceFilter;

  @Output()
  onSearch = new EventEmitter<Partial<FaqSentenceFilter>>();

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

  search() {
    this.onSearch.emit(this.filter);
  }

}

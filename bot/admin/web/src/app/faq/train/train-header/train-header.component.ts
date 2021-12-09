import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';
import {DEFAULT_FAQ_SENTENCE_SORT, FaqSentenceFilter} from "../train-grid/train-grid.component";
import {SentenceStatus} from "../../../model/nlp";

@Component({
  selector: 'tock-train-header',
  templateUrl: './train-header.component.html',
  styleUrls: ['./train-header.component.scss']
})
export class TrainHeaderComponent implements OnInit {

  @Input()
  filter: FaqSentenceFilter;

  @Output()
  onSearch = new EventEmitter<Partial<FaqSentenceFilter>>();

  constructor() { }

  ngOnInit(): void {
  }

  toggleSort(): void {
    const sortEntry = this.filter.sort[0];
    sortEntry.second = !sortEntry.second;

    this.onSearch.emit(this.filter);
  }

  get isAscending(): boolean {
    return !this.filter.sort[0].second;
  }

  search() {
    this.onSearch.emit(this.filter);
  }

  searchChange(value): void {
    this.search();
  }
}

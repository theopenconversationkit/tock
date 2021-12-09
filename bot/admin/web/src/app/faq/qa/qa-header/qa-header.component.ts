import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FaqQaFilter } from '../qa-grid/qa-grid.component';

@Component({
  selector: 'tock-qa-header',
  templateUrl: './qa-header.component.html',
  styleUrls: ['./qa-header.component.scss']
})
export class QaHeaderComponent implements OnInit {

  @Input()
  filter: FaqQaFilter;

  @Output()
  onSearch = new EventEmitter<Partial<FaqQaFilter>>();

  onlyActives = false;

  constructor() { }

  ngOnInit(): void {
  }

  search() {
    this.filter.onlyActives = (true === this.onlyActives);
    this.onSearch.emit(this.filter);
  }

  searchChange(value): void {
    this.search();
  }

  toggleOnlyActives(value: boolean) {
    this.onlyActives = value;
    this.search();
  }

}

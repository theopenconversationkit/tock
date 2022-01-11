import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NbMenuItem } from '@nebular/theme';
import {isDocked, ViewMode } from '../../common/model/view-mode';
import { FaqQaFilter } from '../qa-grid/qa-grid.component';

@Component({
  selector: 'tock-qa-header',
  templateUrl: './qa-header.component.html',
  styleUrls: ['./qa-header.component.scss']
})
export class QaHeaderComponent implements OnInit {

  @Input()
  filter: FaqQaFilter;

  @Input()
  viewMode: ViewMode;

  @Output()
  onSearch = new EventEmitter<Partial<FaqQaFilter>>();

  @Output()
  onImport = new EventEmitter<void>();

  @Output()
  onNew = new EventEmitter<void>();

  readonly menuItems: NbMenuItem[] = [
    {
      title: 'Export FAQ'
    },
    {
      title: 'Import FAQ'
    },
    {
      title: 'New FAQ'
    }
  ];

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

  importFaq(): void {
    this.onImport.next(null);
  }

  newFrequentQuestion(): void {
    this.onNew.next(null);
  }

  isDocked(): boolean {
    return isDocked(this.viewMode);
  }


}

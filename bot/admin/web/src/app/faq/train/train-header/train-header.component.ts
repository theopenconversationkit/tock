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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FaqSentenceFilter} from "../train-grid/train-grid.component";

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

  constructor() {
  }

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

  clearSearch() {
    this.filter.search = null;
  }
}

/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

import { PaginatedResult } from '../model/nlp';
import { StateService } from '../core-nlp/state.service';
import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { PaginatedQuery, SearchMark } from '../model/commons';
import { Observable, Subscription } from 'rxjs';

@Component({
  selector: 'tock-scroll',
  templateUrl: './scroll.component.html',
  styleUrls: ['./scroll.component.css']
})
export class ScrollComponent<T> implements OnInit, OnDestroy {
  @Input() title: string;
  @Input() loadOnInit: boolean = true;

  cursor: number = 0;
  pageSize: number = 10;
  total: number = -1;
  loading: boolean = false;
  data: Array<T> = [];
  add: boolean = true;
  mark: SearchMark;

  private subscription: Subscription;

  constructor(protected state: StateService) {}

  ngOnInit() {
    if (this.loadOnInit) {
      this.load();
    }
    this.subscription = this.state.configurationChange.subscribe((_) => this.refresh());
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  reset() {
    this.resetCursor();
    this.loading = false;
    this.total = -1;
    this.mark = null;
  }

  resetCursor() {
    this.cursor = 0;
  }

  refresh() {
    this.reset();
    this.data = [];
    this.load();
  }

  load() {
    if (!this.loading && (this.total === -1 || this.total > this.cursor)) {
      this.loading = true;
      const init = this.total === -1;
      this.search(this.paginatedQuery()).subscribe((s) => this.loadResults(s, init));
    }
  }

  protected searchMark(t: T): SearchMark {
    return null;
  }

  protected paginatedQuery(): PaginatedQuery {
    return this.state.createPaginatedQuery(this.cursor, this.pageSize, this.mark);
  }

  protected loadResults(result: PaginatedResult<T>, init: boolean): boolean {
    //skip parallel initialization
    if (init && this.data.length !== 0) {
      return false;
    }
    if (this.add) {
      Array.prototype.push.apply(this.data, result.rows);
    } else {
      this.data = result.rows;
    }
    this.cursor = result.end;
    this.total = result.total;
    this.loading = false;
    if (!this.mark && this.data && this.data.length !== 0) {
      this.mark = this.searchMark(this.data[0]);
    }
    return true;
  }

  search(query: PaginatedQuery): Observable<PaginatedResult<T>> {
    throw 'please override method search';
  }

  dataEquals(d1: T, d2: T): boolean {
    throw 'please override method equals';
  }

  onScroll() {
    this.load();
  }

  onClose(row: T) {
    let r = this.data.filter((r) => this.dataEquals(r, row))[0];
    this.data.splice(this.data.indexOf(r), 1);
    this.total -= 1;
    this.cursor -= 1;
    if (this.total !== 0 && this.data.length === 0) {
      this.load();
    }
  }

  formattedTotal() {
    return this.total !== 1000000 ? this.total.toString() : this.total + '+';
  }
}

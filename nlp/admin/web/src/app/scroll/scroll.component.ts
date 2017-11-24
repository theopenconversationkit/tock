/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {PaginatedResult} from "../model/nlp";
import {StateService} from "../core/state.service";
import {Component, Input, OnDestroy, OnInit} from "@angular/core";
import {PaginatedQuery} from "../model/commons";
import {Observable} from "rxjs/Observable";

@Component({
  selector: 'tock-scroll',
  templateUrl: './scroll.component.html',
  styleUrls: ['./scroll.component.css']
})
export class ScrollComponent<T> implements OnInit, OnDestroy {

  @Input() title: string;

  cursor: number = 0;
  pageSize: number = 10;
  total: number = -1;
  loading: boolean = false;
  data: Array<T> = [];

  private currentApplicationUnsuscriber: any;
  private currentLocaleUnsuscriber: any;

  constructor(protected state: StateService) {
  }

  ngOnInit() {
    this.load();
    this.currentApplicationUnsuscriber = this.state.currentApplicationEmitter.subscribe(_ => this.refresh());
    this.currentLocaleUnsuscriber = this.state.currentLocaleEmitter.subscribe(_ => this.refresh());
  }

  ngOnDestroy() {
    this.currentApplicationUnsuscriber.unsubscribe();
    this.currentLocaleUnsuscriber.unsubscribe();
  }

  refresh() {
    this.loading = false;
    this.cursor = 0;
    this.total = -1;
    this.data = [];
    this.load();
  }

  load() {
    if (!this.loading && (this.total === -1 || this.total > this.cursor)) {
      this.loading = true;
      const init = this.total === -1;
      this.search(this.paginatedQuery())
        .subscribe(s => this.loadResults(s, init));
    }
  }

  protected paginatedQuery(): PaginatedQuery {
    return this.state.createPaginatedQuery(this.cursor, this.pageSize);
  }

  private loadResults(result: PaginatedResult<T>, init: boolean) {
    //skip parallel initialization
    if (init && this.data.length !== 0) {
      return;
    }
    Array.prototype.push.apply(this.data, result.rows);
    this.cursor = result.end;
    this.total = result.total;
    this.loading = false;
  }

  search(query: PaginatedQuery): Observable<PaginatedResult<T>> {
    throw "please override method search"
  }

  dataEquals(d1: T, d2: T): boolean {
    throw "please override method equals"
  }

  onScroll() {
    this.load();
  }

  onClose(row: T) {
    let r = this.data.filter(r => this.dataEquals(r, row))[0];
    this.data.splice(this.data.indexOf(r), 1);
    this.total -= 1;
    this.cursor -= 1;
  }

}

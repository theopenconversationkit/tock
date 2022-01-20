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

import {AfterViewInit, Component, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import {Entry, PaginatedQuery} from 'src/app/model/commons';
import {FaqDefinition} from '../../common/model/faq-definition';
import { MatPaginator } from '@angular/material/paginator';
import {DataSource, SelectionModel } from '@angular/cdk/collections';
import { BehaviorSubject, Observable } from 'rxjs';
import { ReplaySubject } from 'rxjs';
import { Sort } from '@angular/material/sort';
import { ScrollComponent } from 'src/app/scroll/scroll.component';
import { UserRole } from 'src/app/model/auth';
import { StateService } from 'src/app/core-nlp/state.service';
import { NlpService } from 'src/app/nlp-tabs/nlp.service';
import { DialogService } from 'src/app/core-nlp/dialog.service';
import {PaginatedResult, SearchQuery, Sentence} from 'src/app/model/nlp';
import { of } from 'rxjs';
import { FaqDefinitionService } from '../../common/faq-definition.service';
import { ViewMode } from '../../common/model/view-mode';
import { QaSidebarEditorService } from '../sidebars/qa-sidebar-editor.service';
import { takeUntil } from 'rxjs/operators';
import { QaSearchQuery } from '../../common/model/qa-search-query';


@Component({
  selector: 'tock-qa-grid',
  templateUrl: './qa-grid.component.html',
  styleUrls: ['./qa-grid.component.scss']
})
export class QaGridComponent extends ScrollComponent<FaqDefinition> implements AfterViewInit, OnDestroy, OnInit {

  @Input()
  viewMode : ViewMode;

  @Input()
  filter: FaqQaFilter;

  @Output()
  onDetails = new EventEmitter<FaqDefinition>();

  @Output()
  onEdit= new EventEmitter<FaqDefinition>();

  @ViewChild(MatPaginator)
  paginator: MatPaginator;

  UserRole = UserRole;
  pageIndex: number = 0;
  displayedColumns = [];
  dataSource: QaDataSource | null;

  selectedItem?: FaqDefinition;

  numHidden = 0;

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);
  public readonly currentIntents$: Observable<FaqDefinition[]>;

  private sort: Sort[] = [];

  constructor(public readonly state: StateService,
              private readonly sidebarEditorService: QaSidebarEditorService,
              private readonly qaService: FaqDefinitionService,
              private readonly dialog: DialogService) {
    super(state);
  }

  ngOnInit(): void {
    this.dataSource = new QaDataSource();

    super.ngOnInit();
  }

  ngAfterViewInit(): void {
    this.paginator.page
      .pipe(takeUntil(this.destroy$))
      .subscribe(e => {
      this.add = false;
      if (this.pageSize === e.pageSize) {
        this.cursor = Math.floor(e.pageIndex * e.pageSize);
      } else {
        this.cursor = 0;
        this.pageSize = e.pageSize;
      }
      this.load();
    });

    this.observeEditModeExit(); // unselect item when user undock or dock another panel
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  observeEditModeExit(): void {
    this.sidebarEditorService.registerActionHandler('exit-edit-mode', this.destroy$, evt => {
      this.selectedItem = undefined; // force item de-selection

      return of({
        outcome: 'adhoc-action-done'
      });
    })
  }

  edit(fq: FaqDefinition): void {
    this.selectedItem = fq;
    this.onEdit.emit(fq);
  }

  remove(fq: FaqDefinition): void {
    fq.status = 'Deleted';
  }

  toSearchQuery(query: PaginatedQuery): QaSearchQuery {

    const result = new QaSearchQuery(
      query.namespace,
      query.applicationName,
      query.language,
      query.start,
      query.size,
      this.filter.tags || [],
      null, /* NOTE: There is a weird behavior when set */
      this.filter.search,
      this.filter.sort,
      this.filter.onlyActives
    );
    return result;
  }

  search(query: PaginatedQuery): Observable<PaginatedResult<FaqDefinition>> {
    return this.qaService.searchQas(this.toSearchQuery(query));
  }

  dataEquals(q1: FaqDefinition, q2: FaqDefinition): boolean {
    return q1.title === q2.title
  }

  protected loadResults(result: PaginatedResult<FaqDefinition>, init: boolean): boolean {
    if (super.loadResults(result, init)) {
      this.dataSource.setNewValues(result.rows);
      this.pageIndex = Math.floor(result.start / this.pageSize);
      this.add = true;
      return true;
    } else {
      return false;
    }
  }
}

export class FaqQaFilter {
  constructor(
    public onlyActives?: boolean,
    public search?: string,
    public sort?: Entry<string, boolean>[],
    public tags?: string[],
  ) {
  }

  clone(): FaqQaFilter {
    return { ...this }; // shallow copy
  }
}

export class QaDataSource extends DataSource<FaqDefinition> {

  private subject = new BehaviorSubject([]);

  refreshDataSource() {
    this.subject.next(this.subject.value.slice(0));
  }

  setNewValues(values: FaqDefinition[]) {
    this.subject.next(values);
  }

  getData(): FaqDefinition[] {
    return this.subject.getValue();
  }

  connect(): Observable<FaqDefinition[]> {
    return this.subject;
  }

  disconnect() {
  }
}

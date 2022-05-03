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

import {AfterViewInit, Component, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild} from '@angular/core';
import {PaginatedQuery} from 'src/app/model/commons';
import {FaqDefinition} from '../../common/model/faq-definition';
import {MatPaginator} from '@angular/material/paginator';
import {DataSource} from '@angular/cdk/collections';
import {BehaviorSubject, Observable, of, ReplaySubject} from 'rxjs';
import {ScrollComponent} from 'src/app/scroll/scroll.component';
import {UserRole} from 'src/app/model/auth';
import {StateService} from 'src/app/core-nlp/state.service';
import {PaginatedResult} from 'src/app/model/nlp';
import {FaqDefinitionService} from '../../common/faq-definition.service';
import {ViewMode} from '../../common/model/view-mode';
import {takeUntil} from 'rxjs/operators';
import {FaqSearchQuery} from '../../common/model/faq-search-query';
import {FaqDefinitionSidepanelEditorService} from "../sidepanels/faq-definition-sidepanel-editor.service";
import {FaqDefinitionResult} from "../../common/model/faq-definition-result";
import {FaqDefinitionFilter} from "../../common/model/faq-definition-filter";


@Component({
  selector: 'tock-faq-grid',
  templateUrl: './faq-grid.component.html',
  styleUrls: ['./faq-grid.component.scss']
})
export class FaqGridComponent extends ScrollComponent<FaqDefinition> implements AfterViewInit, OnDestroy, OnInit {

  @Input()
  viewMode: ViewMode;

  @Input()
  filter: FaqDefinitionFilter;

  @Output()
  onDetails = new EventEmitter<FaqDefinition>();

  @Output()
  onEdit = new EventEmitter<FaqDefinition>();

  @ViewChild(MatPaginator)
  paginator: MatPaginator;

  UserRole = UserRole;
  pageIndex: number = 0;
  displayedColumns = [];
  dataSource: FaqDataSource | null;

  selectedItem?: FaqDefinition;

  numHidden = 0;

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);

  constructor(public readonly state: StateService,
              private readonly sidepanelEditorService: FaqDefinitionSidepanelEditorService,
              private readonly qaService: FaqDefinitionService) {
    super(state);
  }

  ngOnInit(): void {
    this.dataSource = new FaqDataSource();

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
    this.sidepanelEditorService.registerActionHandler('exit-edit-mode', this.destroy$, evt => {
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
    fq.status = 'deleted';
  }

  toSearchQuery(query: PaginatedQuery): FaqSearchQuery {
    return new FaqSearchQuery(
      query.namespace,
      query.applicationName,
      query.language,
      query.start,
      query.size,
      this.filter.tags || [],
      null, /* NOTE: There is a weird behavior when set */
      this.filter.search,
      this.filter.sort,
      this.filter.enabled
    );
  }

  search(query: PaginatedQuery): Observable<FaqDefinitionResult> {
    return this.qaService.searchFaq(this.toSearchQuery(query));
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

export class FaqDataSource extends DataSource<FaqDefinition> {

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

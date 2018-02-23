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

import {saveAs} from "file-saver";
import {AfterViewInit, Component, EventEmitter, Input, Output, ViewChild} from "@angular/core";
import {PaginatedResult, SearchQuery, Sentence, SentenceStatus} from "../model/nlp";
import {NlpService} from "../nlp-tabs/nlp.service";
import {StateService} from "../core/state.service";
import {ScrollComponent} from "../scroll/scroll.component";
import {PaginatedQuery, SearchMark} from "../model/commons";
import {Observable} from "rxjs/Observable";
import {MdPaginator, MdSnackBar} from "@angular/material";
import {UserRole} from "../model/auth";
import {DataSource, SelectionModel} from "@angular/cdk/collections";
import {BehaviorSubject} from "rxjs/BehaviorSubject";

@Component({
  selector: 'tock-sentences-scroll',
  templateUrl: './sentences-scroll.component.html',
  styleUrls: ['./sentences-scroll.component.css']
})
export class SentencesScrollComponent extends ScrollComponent<Sentence> implements AfterViewInit {

  UserRole = UserRole;

  @Input() filter: SentenceFilter;
  @Input() displayArchiveButton: boolean = true;
  @Input() displayProbabilities: boolean = false;
  @Input() displayStatus: boolean = false;
  @Output() selectedSentences: EventEmitter<Sentence[]> = new EventEmitter<Sentence[]>();

  pageIndex: number = 0;

  tableView: boolean = false;
  displayedColumns = [];
  @ViewChild(MdPaginator) paginator: MdPaginator;
  dataSource: SentencesDataSource | null;
  selection: SelectionModel<Sentence> = new SelectionModel<Sentence>(true, []);

  constructor(state: StateService,
              private nlp: NlpService,
              private snackBar: MdSnackBar) {
    super(state);
  }


  protected searchMark(t: Sentence): SearchMark {
    return new SearchMark(
      t.text,
      t.updateDate
    );
  }

  ngOnInit(): void {
    if (this.displayStatus) {
      this.displayedColumns = ['select', 'text', 'currentIntent', 'status'];
    } else {
      this.displayedColumns = ['select', 'text', 'currentIntent'];
    }
    this.dataSource = new SentencesDataSource();

    super.ngOnInit();
  }

  ngAfterViewInit(): void {
    this.paginator.page.subscribe(e => {
      this.add = false;
      if (this.pageSize === e.pageSize) {
        this.cursor = e.pageIndex * e.pageSize;
      } else {
        this.cursor = 0;
        this.pageSize = e.pageSize;
      }
      this.load();
    })
  }

  reset(): void {
    super.reset();
    this.pageIndex = 0;
    this.selection.clear();
    this.fireSelectionChange();
  }

  toSearchQuery(query: PaginatedQuery): SearchQuery {
    return new SearchQuery(
      query.namespace,
      query.applicationName,
      query.language,
      query.start,
      query.size,
      query.searchMark,
      this.filter.search,
      this.filter.intentId,
      this.filter.status,
      !this.filter.entityType || this.filter.entityType.length === 0 ? null : this.filter.entityType,
      !this.filter.entityRole || this.filter.entityRole.length === 0 ? null : this.filter.entityRole,
      this.filter.modifiedAfter)
  }

  search(query: PaginatedQuery): Observable<PaginatedResult<Sentence>> {
    return this.nlp.searchSentences(this.toSearchQuery(query));
  }

  dataEquals(d1: Sentence, d2: Sentence): boolean {
    return d1.text === d2.text
  }

  downloadSentencesDump() {
    setTimeout(_ => {
      this.nlp.getSentencesDump(
        this.state.currentApplication,
        this.toSearchQuery(this.paginatedQuery()),
        this.state.hasRole(UserRole.technicalAdmin))
        .subscribe(blob => {
          saveAs(blob, this.state.currentApplication.name + "_sentences.json");
          this.snackBar.open(`Dump provided`, "Dump", {duration: 1000});
        })
    }, 1);
  }

  switchToScrollView() {
    this.reset();
    this.tableView = false;
    this.refresh();
  }

  switchToTableView() {
    this.reset();
    this.tableView = true;
    this.refresh();
  }

  protected loadResults(result: PaginatedResult<Sentence>, init: boolean): boolean {
    if (super.loadResults(result, init)) {
      this.dataSource.setNewValues(result.rows);
      this.pageIndex = result.start / this.pageSize;
      this.add = true;
      return true;
    } else {
      return false;
    }
  }

  private fireSelectionChange() {
    this.selectedSentences.emit(this.tableView ? this.selection.selected : null);
  }

  toggle(sentence: Sentence) {
    this.selection.toggle(sentence);
    this.fireSelectionChange();
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.getData().length;
    return numSelected == numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.getData().forEach(row => this.selection.select(row));
    this.fireSelectionChange();
  }
}

export class SentenceFilter {
  constructor(public search?: string,
              public intentId?: string,
              public status?: SentenceStatus[],
              public entityType?: string,
              public entityRole?: string,
              public modifiedAfter?: Date) {
  }

  clone(): SentenceFilter {
    return new SentenceFilter(this.search, this.intentId, this.status, this.entityType, this.entityRole, this.modifiedAfter);
  }
}

export class SentencesDataSource extends DataSource<Sentence> {

  private subject = new BehaviorSubject([]);

  setNewValues(values: Sentence[]) {
    this.subject.next(values);
  }

  getData(): Sentence[] {
    return this.subject.getValue();
  }

  connect(): Observable<Sentence[]> {
    return this.subject;
  }

  disconnect() {
  }
}

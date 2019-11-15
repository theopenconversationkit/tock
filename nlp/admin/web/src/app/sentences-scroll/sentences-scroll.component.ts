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
import {AfterViewInit, Component, EventEmitter, Input, Output, ViewChild, ViewEncapsulation} from "@angular/core";
import {PaginatedResult, SearchQuery, Sentence, SentenceStatus, UpdateSentencesQuery} from "../model/nlp";
import {NlpService} from "../nlp-tabs/nlp.service";
import {StateService} from "../core-nlp/state.service";
import {ScrollComponent} from "../scroll/scroll.component";
import {Entry, PaginatedQuery, SearchMark} from "../model/commons";
import {BehaviorSubject, Observable} from "rxjs";
import {MatPaginator} from "@angular/material";
import {UserRole} from "../model/auth";
import {DataSource, SelectionModel} from "@angular/cdk/collections";
import {Sort} from "@angular/material/sort/typings/sort";
import {DialogService} from "../core-nlp/dialog.service";
import {ConfirmDialogComponent} from "../shared-nlp/confirm-dialog/confirm-dialog.component";
import {MatDialog} from "@angular/material/dialog";

@Component({
  selector: 'tock-sentences-scroll',
  templateUrl: './sentences-scroll.component.html',
  styleUrls: ['./sentences-scroll.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class SentencesScrollComponent extends ScrollComponent<Sentence> implements AfterViewInit {

  UserRole = UserRole;

  @Input() filter: SentenceFilter;
  @Input() displayUnknownButton: boolean = true;
  @Input() displayProbabilities: boolean = false;
  @Input() displayStatus: boolean = false;

  @Output() selectedSentences: EventEmitter<Sentence[]> = new EventEmitter<Sentence[]>();

  pageIndex: number = 0;

  tableView: boolean = false;
  advancedView: boolean = false;
  displayedColumns = [];
  @ViewChild(MatPaginator, {static: false}) paginator: MatPaginator;
  dataSource: SentencesDataSource | null;
  selection: SelectionModel<Sentence> = new SelectionModel<Sentence>(true, []);
  sentenceToUpdate: Sentence;

  private sort: Sort[] = [];

  constructor(state: StateService,
              private nlp: NlpService,
              private dialog: DialogService,
              private matDialog: MatDialog) {
    super(state);
  }

  protected searchMark(t: Sentence): SearchMark {
    return new SearchMark(
      t.text,
      t.updateDate
    );
  }

  private initColumns() {
    if (this.displayStatus) {
      if (this.advancedView) {
        this.displayedColumns = ['select', 'text', 'currentIntent', 'update', 'status', 'lastUpdate', 'intentProbability', 'entitiesProbability', 'lastUsage', 'usageCount', 'unknownCount'];
      } else {
        this.displayedColumns = ['select', 'text', 'currentIntent', 'update', 'status'];
      }
    } else {
      if (this.advancedView) {
        this.displayedColumns = ['select', 'text', 'currentIntent', 'update', 'lastUpdate', 'intentProbability', 'entitiesProbability', 'lastUsage', 'usageCount', 'unknownCount'];
      } else {
        this.displayedColumns = ['select', 'text', 'currentIntent', 'update'];
      }
    }
  }

  ngOnInit(): void {
    this.initColumns();
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

  resetCursor() {
    super.resetCursor();
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
      !this.filter.entityRolesToInclude || this.filter.entityRolesToInclude.length === 0 ? [] : this.filter.entityRolesToInclude,
      !this.filter.entityRolesToExclude || this.filter.entityRolesToExclude.length === 0 ? [] : this.filter.entityRolesToExclude,
      this.filter.modifiedAfter,
      this.filter.modifiedBefore,
      this.tableView && this.sort.length !== 0
        ? this.sort.map(s => new Entry<string, boolean>(s.active, s.direction === 'asc'))
        : null,
      this.filter.onlyToReview,
      this.filter.searchSubEntities
    )
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
          this.dialog.notify(`Dump provided`, "Dump");
        })
    }, 1);
  }

  switchToScrollView() {
    this.sort = [];
    this.reset();
    this.tableView = false;
    this.refresh();
  }

  switchToTableView() {
    this.reset();
    this.tableView = true;
    this.refresh();
  }

  sortChange(s: Sort) {
    this.sort.splice(0, 0, s);
    for (let i = this.sort.length - 1; i >= 0; --i) {
      if (this.sort[i].direction === '' || (i > 0 && this.sort[i].active === s.active)) {
        this.sort.splice(i, 1)
      }
    }
    this.data = [];
    this.resetCursor();
    this.load();
  }

  switchAdvancedView(advanced: boolean) {
    this.advancedView = advanced;
    this.initColumns();
    this.dataSource.refreshDataSource();
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

  onUpdate(sentence: Sentence) {
    this.sentenceToUpdate = sentence;
  }

  onEndUpdate(sentence: Sentence) {
    if (sentence) {
      this.refresh();
    }
    this.sentenceToUpdate = undefined;
  }

  onDelete() {
    let dialogRef = this.dialog.open(this.matDialog, ConfirmDialogComponent, {
      data: {
        title: "Delete Selected Sentences",
        subtitle: "Are you sure?",
        action: "Delete"
      }
    });
    dialogRef.afterClosed().subscribe(result => {
        if (result === "delete") {
          this.update(SentenceStatus.deleted);
        }
      }
    );
  }

  onValidate() {
    this.update(SentenceStatus.validated);
  }

  private update(status: SentenceStatus) {
    if (this.selection.selected.length === 0) {
      this.dialog.notify("Please select at least one sentence first");
    } else {
      this.loading = true;
      this.nlp.updateSentences(
        new UpdateSentencesQuery(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale,
          this.selection.selected,
          null,
          null,
          null,
          null,
          status
        ))
        .subscribe(() => {
          this.dialog.notify("Sentences updated", "Update");
          this.refresh()
        });
    }
  }
}

export class SentenceFilter {
  constructor(public search?: string,
              public intentId?: string,
              public status?: SentenceStatus[],
              public entityType?: string,
              public entityRolesToInclude: string[] = [],
              public entityRolesToExclude: string[] = [],
              public modifiedAfter?: Date,
              public modifiedBefore?: Date,
              public onlyToReview: boolean = false,
              public searchSubEntities: boolean = false) {
  }

  clone(): SentenceFilter {
    return new SentenceFilter(this.search, this.intentId, this.status, this.entityType, this.entityRolesToInclude, this.entityRolesToInclude, this.modifiedAfter, this.modifiedBefore, this.onlyToReview, this.searchSubEntities);
  }
}

export class SentencesDataSource extends DataSource<Sentence> {

  private subject = new BehaviorSubject([]);

  refreshDataSource() {
    this.subject.next(this.subject.value.slice(0));
  }

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

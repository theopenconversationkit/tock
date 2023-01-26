/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import { saveAs } from 'file-saver-es';
import { AfterViewInit, Component, EventEmitter, Input, OnInit, Output, ViewChild, ViewEncapsulation } from '@angular/core';
import { PaginatedResult, SearchQuery, Sentence, SentenceStatus, SentencesTextQuery, UpdateSentencesQuery } from '../model/nlp';
import { NlpService } from '../nlp-tabs/nlp.service';
import { StateService } from '../core-nlp/state.service';
import { ScrollComponent } from '../scroll/scroll.component';
import { Entry, PaginatedQuery, SearchMark } from '../model/commons';
import { Observable } from 'rxjs';
import { MatPaginator } from '@angular/material/paginator';
import { UserRole } from '../model/auth';
import { SelectionModel } from '@angular/cdk/collections';
import { DialogService } from '../core-nlp/dialog.service';
import { ConfirmDialogComponent } from '../shared-nlp/confirm-dialog/confirm-dialog.component';
import { NbSortDirection, NbSortRequest } from '@nebular/theme';

interface TreeNode<T> {
  data: T;
  children?: TreeNode<T>[];
  expanded?: boolean;
}

@Component({
  selector: 'tock-sentences-scroll',
  templateUrl: './sentences-scroll.component.html',
  styleUrls: ['./sentences-scroll.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class SentencesScrollComponent extends ScrollComponent<Sentence> implements OnInit, AfterViewInit {
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
  @ViewChild(MatPaginator) paginator: MatPaginator;
  selection: SelectionModel<Sentence> = new SelectionModel<Sentence>(true, []);
  sentenceToUpdate: Sentence;
  nodes: TreeNode<Sentence>[] = [];

  private sort: NbSortRequest[] = [];

  constructor(state: StateService, private nlp: NlpService, private dialog: DialogService) {
    super(state);
  }

  protected searchMark(t: Sentence): SearchMark {
    return new SearchMark(t.text, t.updateDate);
  }

  private initColumns() {
    let columns = ['select', 'text', 'currentIntent', 'update'];
    if (this.displayStatus) {
      columns.push('status');
    }
    if (this.advancedView) {
      columns.push('lastUpdate', 'intentProbability', 'entitiesProbability', 'lastUsage', 'usageCount', 'unknownCount');
    }
    this.displayedColumns = columns;
  }

  ngOnInit(): void {
    this.initColumns();

    super.ngOnInit();
  }

  toNodes(data: Sentence[]): TreeNode<Sentence>[] {
    return Array.from(data, (element) => {
      return {
        expanded: false,
        data: element,
        children: []
      };
    });
  }

  ngAfterViewInit(): void {
    this.paginator.page.subscribe((e) => {
      this.add = false;
      if (this.pageSize === e.pageSize) {
        this.cursor = Math.floor(e.pageIndex * e.pageSize);
      } else {
        this.cursor = 0;
        this.pageSize = e.pageSize;
      }
      this.load();
    });
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
      this.tableView && this.sort.length !== 0 ? this.sort.map((s) => new Entry<string, boolean>(s.column, s.direction === 'asc')) : null,
      this.filter.onlyToReview,
      this.filter.searchSubEntities,
      this.filter.user,
      this.filter.allButUser,
      this.filter.maxIntentProbability / 100,
      this.filter.minIntentProbability / 100,
      this.filter.configuration
    );
  }

  search(query: PaginatedQuery): Observable<PaginatedResult<Sentence>> {
    return this.nlp.searchSentences(this.toSearchQuery(query));
  }

  dataEquals(d1: Sentence, d2: Sentence): boolean {
    return d1.text === d2.text;
  }

  downloadSentencesDump() {
    setTimeout((_) => {
      this.nlp
        .getSentencesDump(
          this.state.currentApplication,
          this.toSearchQuery(this.paginatedQuery()),
          this.state.hasRole(UserRole.technicalAdmin)
        )
        .subscribe((blob) => {
          saveAs(blob, this.state.currentApplication.name + '_sentences.json');
          this.dialog.notify(`Dump provided`, 'Dump');
        });
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

  sortChange(s: NbSortRequest) {
    this.sort.splice(0, 0, s);
    for (let i = this.sort.length - 1; i >= 0; --i) {
      if (this.sort[i].direction === '' || (i > 0 && this.sort[i].column === s.column)) {
        this.sort.splice(i, 1);
      }
    }
    this.data = [];
    this.resetCursor();
    this.load();
  }

  switchAdvancedView(advanced: boolean) {
    this.advancedView = advanced;
    this.initColumns();
    this.nodes = [...this.nodes]; // Just refresh the table
  }

  protected loadResults(result: PaginatedResult<Sentence>, init: boolean): boolean {
    if (super.loadResults(result, init)) {
      this.nodes = this.toNodes(result.rows);
      this.pageIndex = Math.floor(result.start / this.pageSize);
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
    const numRows = this.nodes.length;
    return numSelected == numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ? this.selection.clear() : this.nodes.forEach((row) => this.selection.select(row.data));
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
    let dialogRef = this.dialog.openDialog(ConfirmDialogComponent, {
      context: {
        title: 'Delete Selected Sentences',
        subtitle: 'Are you sure?',
        action: 'Delete'
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result === 'delete') {
        this.update(SentenceStatus.deleted);
      }
    });
  }

  onDownloadSelected() {
    if (this.selection.selected.length === 0) {
      this.dialog.notify('Please select at least one sentence first');
    } else {
      setTimeout((_) => {
        const theSelectedSentences = this.selection.selected;
        const theAppQuery = this.state.createApplicationScopedQuery();
        const theQuery = new SentencesTextQuery(
          theAppQuery.namespace,
          theAppQuery.applicationName,
          theAppQuery.language,
          theSelectedSentences.map((s) => s.text)
        );
        this.nlp
          .getSentencesQueryDump(this.state.currentApplication, theQuery, this.state.hasRole(UserRole.technicalAdmin))
          .subscribe((blob) => {
            saveAs(blob, this.state.currentApplication.name + '_selected_sentences.json');
            this.dialog.notify(`Dump provided`, 'Dump');
          });
      }, 1);
    }
  }

  onValidate() {
    this.update(SentenceStatus.validated);
  }

  private update(status: SentenceStatus) {
    if (this.selection.selected.length === 0) {
      this.dialog.notify('Please select at least one sentence first');
    } else {
      this.loading = true;
      this.nlp
        .updateSentences(
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
          )
        )
        .subscribe(() => {
          this.dialog.notify('Sentences updated', 'Update');
          this.refresh();
        });
    }
  }

  getDirection(col: string) {
    for (let s of this.sort) {
      if (s.column === col) return s.direction;
    }
    return NbSortDirection.NONE;
  }
}

export class SentenceFilter {
  constructor(
    public search?: string,
    public intentId?: string,
    public status?: SentenceStatus[],
    public entityType?: string,
    public entityRolesToInclude: string[] = [],
    public entityRolesToExclude: string[] = [],
    public modifiedAfter?: Date,
    public modifiedBefore?: Date,
    public onlyToReview: boolean = false,
    public searchSubEntities: boolean = false,
    public user?: string,
    public allButUser?: string,
    public maxIntentProbability: number = 100,
    public minIntentProbability: number = 0,
    public configuration?: string
  ) {}

  clone(): SentenceFilter {
    return new SentenceFilter(
      this.search,
      this.intentId,
      this.status,
      this.entityType,
      this.entityRolesToInclude,
      this.entityRolesToInclude,
      this.modifiedAfter,
      this.modifiedBefore,
      this.onlyToReview,
      this.searchSubEntities,
      this.user,
      this.allButUser,
      this.maxIntentProbability,
      this.minIntentProbability,
      this.configuration
    );
  }
}

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

import {BehaviorSubject, Observable, ReplaySubject} from "rxjs";
import {DataSource, SelectionModel} from "@angular/cdk/collections";
import {AfterViewInit, Component, EventEmitter, Input, OnDestroy, Output, ViewChild} from "@angular/core";
import {MatPaginator} from "@angular/material/paginator";
import {Sort} from "@angular/material/sort";
import {DialogService} from "../../../core-nlp/dialog.service";
import {StateService} from "../../../core-nlp/state.service";
import {UserRole} from "../../../model/auth";
import {Entry, PaginatedQuery, SearchMark} from "../../../model/commons";
import {Intent, PaginatedResult, SearchQuery, Sentence, SentenceStatus} from "../../../model/nlp";
import {NlpService} from "../../../nlp-tabs/nlp.service";
import {ScrollComponent} from "../../../scroll/scroll.component";
import {BatchActionName} from "../train-toolbar/train-toolbar.component";
import {SelectionMode} from "../../common/model/selection-mode";
import {SentencesService} from "../../common/sentences.service";
import {ViewMode} from "../../common/model/view-mode";

@Component({
  selector: 'tock-train-grid',
  templateUrl: './train-grid.component.html',
  styleUrls: ['./train-grid.component.scss']
})
export class TrainGridComponent extends ScrollComponent<Sentence> implements AfterViewInit, OnDestroy {

  @Input()
  filter: FaqSentenceFilter;

  @Input()
  viewMode: ViewMode;

  @Output()
  onDetails = new EventEmitter<Sentence>();

  @ViewChild(MatPaginator) paginator: MatPaginator;

  selection: SelectionModel<Sentence> = new SelectionModel<Sentence>(true, []);
  selectionMode: SelectionMode = SelectionMode.SELECT_NEVER;

  UserRole = UserRole;
  pageIndex: number = 0;
  displayedColumns = [];
  dataSource: SentencesDataSource | null;

  numHidden = 0;

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);
  public readonly currentIntents$: Observable<Intent[]>;

  private sort: Sort[] = [];

  constructor(state: StateService,
              private nlp: NlpService,
              private dialog: DialogService,
              private readonly sentencesService: SentencesService) {
    super(state);

    this.currentIntents$ = state.currentIntents;
  }

  protected searchMark(t: Sentence): SearchMark {
    return new SearchMark(
      t.text,
      t.updateDate
    );
  }


  ngOnInit(): void {
    this.dataSource = new SentencesDataSource();

    super.ngOnInit();
  }

  ngAfterViewInit(): void {
    this.paginator.page.subscribe(e => {
      this.add = false;
      if (this.pageSize === e.pageSize) {
        this.cursor = Math.floor(e.pageIndex * e.pageSize);
      } else {
        this.cursor = 0;
        this.pageSize = e.pageSize;
      }
      this.load();
    })
  }

  ngOnDestroy() {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  toolbarStyleClass(): string {
    return this.viewMode === 'FULL_WIDTH' ? 'w-25' : 'w-50';
  }

  async onBatchAction(actionName: BatchActionName): Promise<void> {
    let actionLabel: string;

    if (!this?.selection?.selected?.length) {
      this.dialog.notify('',
        "Aucune sélection",
        {duration: 2000, status: "info"});
      return;
    }

    for (let sentence of this.selection.selected) {

      switch (actionName) {
        case BatchActionName.delete :
          actionLabel = 'delete';
          sentence.status = SentenceStatus.deleted;
          break;

        case BatchActionName.unknown :
          actionLabel = 'set unknown';
          sentence.classification.intentId = Intent.unknown;
          sentence.classification.entities = [];
          sentence.status = SentenceStatus.validated;
          break;

        case BatchActionName.validate:
          actionLabel = 'validate';
          const intentId = sentence.classification.intentId;
          if (intentId === Intent.unknown) {
            sentence.classification.intentId = Intent.unknown;
            sentence.classification.entities = [];
          }
          sentence.status = SentenceStatus.validated;
          break;

        default:
          throw new Error(`unhandled action: ${actionName}`);
      }
    }

    await this.sentencesService.saveBulk(this.selection.selected, this.destroy$)
      .toPromise();

    this.dialog.notify(actionLabel,
      `${actionLabel} ${this.selection.selected.length} sentences`,
      {duration: 2000, status: "basic"});

    this.refresh();
  }

  details(sentence: Sentence): void {
    this.onDetails.emit(sentence);
  }

  /**
   * this allows the transition from SELECT_NEVER/SELECT_ALWAYS to SELECT_SOME mode
   * because it was costly/too complicated to recompute "All elements are selected?" at each cycle
   */
  onToggle(active: boolean): void {
    this.selectionMode = SelectionMode.SELECT_SOME;
  }



  refresh() {
    this.selection.clear();
    super.refresh();
  }

  onToggleSelectAll(value: boolean): void {
    if (!value) {
      this.selectionMode = SelectionMode.SELECT_NEVER;
      this.selection.clear();
    } else {
      this.selectionMode = SelectionMode.SELECT_ALWAYS;
      this.data.forEach(data => this.selection.select(data));
    }
  }

  isAllSelected(): boolean {
    return this.selectionMode === SelectionMode.SELECT_ALWAYS;
  }

  refreshOnEmpty() {
    if (this.data.length - (++this.numHidden) === 0) {
      this.numHidden = 0;
      this.refresh();
    }
  }

  resetCursor() {
    super.resetCursor();
    this.pageIndex = 0;
  }

  toSearchQuery(query: PaginatedQuery): SearchQuery {

    const result = new SearchQuery(
      query.namespace,
      query.applicationName,
      query.language,
      query.start,
      query.size,
      null, /* NOTE: There is a weird behavior when set */
      this.filter.search,
      this.filter.intentId,
      this.filter.status,
      null,
      [],
      [],
      null,
      null,
      this.filter.sort,
      this.filter.onlyToReview,
      null,
      null,
      null,
      this.filter.maxIntentProbability / 100,
      this.filter.minIntentProbability / 100
    );
    return result;
  }

  search(query: PaginatedQuery): Observable<PaginatedResult<Sentence>> {
    return this.nlp.searchSentences(this.toSearchQuery(query));
  }

  dataEquals(d1: Sentence, d2: Sentence): boolean {
    return d1.text === d2.text
  }

  protected loadResults(result: PaginatedResult<Sentence>, init: boolean): boolean {
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

export const DEFAULT_FAQ_SENTENCE_SORT: Entry<string, boolean> = new Entry("creationDate", false);

export class FaqSentenceFilter {
  constructor(
    public search?: string,
    public sort?: Entry<string, boolean>[],
    public intentId?: string,
    public status?: SentenceStatus[],
    public onlyToReview: boolean = false,
    public maxIntentProbability: number = 100,
    public minIntentProbability: number = 0) {
  }

  clone(): FaqSentenceFilter {
    return new FaqSentenceFilter(
      this.search, this.sort, this.intentId, this.status, this.onlyToReview,
      this.maxIntentProbability, this.minIntentProbability
    );
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


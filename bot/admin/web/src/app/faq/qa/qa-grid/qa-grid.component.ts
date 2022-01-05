import {AfterViewInit, Component, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import {Entry, PaginatedQuery} from 'src/app/model/commons';

import {FrequentQuestion} from '../../common/model/frequent-question';
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
import { QaService } from '../../common/qa.service';
import { ViewMode } from '../../common/model/view-mode';


@Component({
  selector: 'tock-qa-grid',
  templateUrl: './qa-grid.component.html',
  styleUrls: ['./qa-grid.component.scss']
})
export class QaGridComponent extends ScrollComponent<FrequentQuestion> implements AfterViewInit, OnDestroy, OnInit {

  @Input()
  viewMode : ViewMode;

  @Input()
  filter: FaqQaFilter;

  @Output()
  onDetails = new EventEmitter<FrequentQuestion>();

  @Output()
  onEdit= new EventEmitter<FrequentQuestion>();

  @ViewChild(MatPaginator) paginator: MatPaginator;


  UserRole = UserRole;
  pageIndex: number = 0;
  displayedColumns = [];
  dataSource: QaDataSource | null;

  numHidden = 0;

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);
  public readonly currentIntents$: Observable<FrequentQuestion[]>;

  private sort: Sort[] = [];

  constructor(state: StateService,
              private qaService: QaService,
              private dialog: DialogService) {
    super(state);
  }

  ngOnInit(): void {
    this.dataSource = new QaDataSource();

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

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  edit(fq: FrequentQuestion): void {
    this.onEdit.emit(fq);
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
      null,
      null,
      null,
      [],
      [],
      null,
      null,
      this.filter.sort,
      this.filter.onlyActives /* NOTE: Use this as a placeholder for a future backend field */
    );
    return result;
  }

  search(query: PaginatedQuery): Observable<PaginatedResult<FrequentQuestion>> {
    return this.qaService.searchQas(this.toSearchQuery(query));
  }

  dataEquals(q1: FrequentQuestion, q2: FrequentQuestion): boolean {
    return q1.title === q2.title
  }

  protected loadResults(result: PaginatedResult<FrequentQuestion>, init: boolean): boolean {
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
    public sort?: Entry<string, boolean>[]) {
  }

  clone(): FaqQaFilter {
    return { ...this }; // shallow copy
  }
}

export class QaDataSource extends DataSource<FrequentQuestion> {

  private subject = new BehaviorSubject([]);

  refreshDataSource() {
    this.subject.next(this.subject.value.slice(0));
  }

  setNewValues(values: FrequentQuestion[]) {
    this.subject.next(values);
  }

  getData(): FrequentQuestion[] {
    return this.subject.getValue();
  }

  connect(): Observable<FrequentQuestion[]> {
    return this.subject;
  }

  disconnect() {
  }
}

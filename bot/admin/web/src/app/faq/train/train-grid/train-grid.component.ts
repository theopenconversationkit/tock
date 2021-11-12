
import { DataSource } from "@angular/cdk/collections";
import { AfterViewInit, Component, EventEmitter, Input, Output, ViewChild, ViewEncapsulation } from "@angular/core";
import { MatPaginator } from "@angular/material/paginator";
import { Sort } from "@angular/material/sort";
import { BehaviorSubject, Observable } from "rxjs";
import { DialogService } from "../../../core-nlp/dialog.service";
import { StateService } from "../../../core-nlp/state.service";
import { UserRole } from "../../../model/auth";
import { Entry, PaginatedQuery, SearchMark } from "../../../model/commons";
import {
  Intent,
  PaginatedResult,
  SearchQuery,
  Sentence,
  SentenceStatus
} from "../../../model/nlp";
import { NlpService } from "../../../nlp-tabs/nlp.service";
import { ScrollComponent } from "../../../scroll/scroll.component";


@Component({
  selector: 'tock-train-grid',
  templateUrl: './train-grid.component.html',
  styleUrls: ['./train-grid.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class TrainGridComponent extends ScrollComponent<Sentence> implements AfterViewInit {

  @Input() filter: FaqSentenceFilter;
  @ViewChild(MatPaginator) paginator: MatPaginator;

  UserRole = UserRole;
  pageIndex: number = 0;
  displayedColumns = [];
  dataSource: SentencesDataSource | null;

  public readonly currentIntents$: Observable<Intent[]>;

  private sort: Sort[] = [];

  constructor(state: StateService,
    private nlp: NlpService,
    private dialog: DialogService) {
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

  resetCursor() {
    super.resetCursor();
    this.pageIndex = 0;
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
      null,
      [],
      [],
      null,
      null,
      this.sort.map(s => new Entry<string, boolean>(s.active, s.direction === 'asc')),
      this.filter.onlyToReview,
      null,
      null,
      null,
      this.filter.maxIntentProbability / 100,
      this.filter.minIntentProbability / 100
    );
  }

  search(query: PaginatedQuery): Observable<PaginatedResult<Sentence>> {
    return this.nlp.searchSentences(this.toSearchQuery(query));
  }

  dataEquals(d1: Sentence, d2: Sentence): boolean {
    return d1.text === d2.text
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

export class FaqSentenceFilter {
  constructor(public search?: string,
    public intentId?: string,
    public status?: SentenceStatus[],
    public onlyToReview: boolean = false,
    public maxIntentProbability: number = 100,
    public minIntentProbability: number = 0) {
  }

  clone(): FaqSentenceFilter {
    return new FaqSentenceFilter(
      this.search, this.intentId, this.status, this.onlyToReview,
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


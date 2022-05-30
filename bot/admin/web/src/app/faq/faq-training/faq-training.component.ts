import { SelectionModel } from '@angular/cdk/collections';
import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { NbToastrService } from '@nebular/theme';
import { Observable, Subject, Subscription } from 'rxjs';
import { take, takeUntil, share } from 'rxjs/operators';

import { StateService } from '../../core-nlp/state.service';
import { PaginatedQuery } from '../../model/commons';
import { Intent, PaginatedResult, SearchQuery, Sentence, SentenceStatus } from '../../model/nlp';
import { NlpService } from '../../nlp-tabs/nlp.service';
import { Pagination } from '../../shared/pagination/pagination.component';
import { Action, FaqTrainingFilter } from '../models';
import { truncate } from '../../model/commons';
import { FaqTrainingListComponent } from './faq-training-list/faq-training-list.component';
import { FaqTrainingDialogComponent } from './faq-training-dialog/faq-training-dialog.component';
import { FaqTrainingFiltersComponent } from './faq-training-filters/faq-training-filters.component';

export type SentenceExtended = Sentence & { _selected?: boolean };

@Component({
  selector: 'tock-faq-training',
  templateUrl: './faq-training.component.html',
  styleUrls: ['./faq-training.component.scss']
})
export class FaqTrainingComponent implements OnInit, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  @ViewChild('faqTrainingList') faqTrainingList: FaqTrainingListComponent;
  @ViewChild('faqTrainingDialog') faqTrainingDialog: FaqTrainingDialogComponent;
  @ViewChild('faqTrainingFilter') faqTrainingFilter: FaqTrainingFiltersComponent;

  selection: SelectionModel<SentenceExtended> = new SelectionModel<SentenceExtended>(true, []);
  Action: typeof Action = Action;
  filters = {
    search: null,
    sort: [{ first: 'creationDate', second: false }],
    intentId: null,
    status: [0],
    onlyToReview: false,
    maxIntentProbability: 100,
    minIntentProbability: 0
  };

  loading: boolean = false;

  sentences: SentenceExtended[] = [];

  constructor(
    private nlp: NlpService,
    private state: StateService,
    private toastrService: NbToastrService
  ) {}

  ngOnInit(): void {
    this.loadData();
    this.state.configurationChange.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.loadData();
      this.closeDetails();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  filterFaqTraining(filters: FaqTrainingFilter): void {
    this.selection.clear();
    this.filters = { ...this.filters, ...filters };
    this.loadData();
  }

  sortFaqTraining(sort: boolean): void {
    this.selection.clear();
    this.filters.sort[0].second = sort;
    this.loadData();
  }

  pagination: Pagination = {
    start: 0,
    end: undefined,
    size: 10,
    total: undefined
  };

  paginationChange() {
    this.loadData(this.pagination.start, this.pagination.size);
  }

  onScroll() {
    if (this.loading || this.pagination.end >= this.pagination.total) return;

    return this.loadData(this.pagination.end, this.pagination.size, true, false);
  }

  loadData(
    start: number = 0,
    size: number = this.pagination.size,
    add: boolean = false,
    showLoadingSpinner: boolean = true
  ): Observable<PaginatedResult<SentenceExtended>> {
    if (showLoadingSpinner) this.loading = true;

    let search = this.search(this.state.createPaginatedQuery(start, size)).pipe(
      takeUntil(this.destroy$),
      share()
    );

    search.subscribe({
      next: (data: PaginatedResult<SentenceExtended>) => {
        this.pagination.total = data.total;
        this.pagination.end = data.end;

        if (add) {
          this.sentences = [...this.sentences, ...data.rows];
        } else {
          this.sentences = data.rows;
          this.pagination.start = data.start;
        }

        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });

    return search;
  }

  search(query: PaginatedQuery): Observable<PaginatedResult<SentenceExtended>> {
    return this.nlp.searchSentences(this.toSearchQuery(query));
  }

  toSearchQuery(query: PaginatedQuery): SearchQuery {
    const result = new SearchQuery(
      query.namespace,
      query.applicationName,
      query.language,
      query.start,
      query.size,
      null /* NOTE: There is a weird behavior when set */,
      this.filters.search,
      this.filters.intentId,
      this.filters.status,
      null,
      [],
      [],
      null,
      null,
      this.filters.sort,
      this.filters.onlyToReview,
      null,
      null,
      null,
      this.filters.maxIntentProbability / 100,
      this.filters.minIntentProbability / 100
    );
    return result;
  }

  dialogDetailsSentence: SentenceExtended;

  unselectAllSentences(): void {
    this.sentences.forEach((s) => (s._selected = false));
  }

  closeDetails(): void {
    this.unselectAllSentences();
    this.dialogDetailsSentence = undefined;
  }

  showDetails(sentence: SentenceExtended): void {
    this.unselectAllSentences();

    if (this.dialogDetailsSentence && this.dialogDetailsSentence == sentence) {
      this.dialogDetailsSentence = undefined;
    } else {
      sentence._selected = true;
      this.dialogDetailsSentence = sentence;
    }
  }

  async handleAction({ action, sentence }): Promise<void> {
    const actionTitle = this.setActionTitle(action);

    this.setSentenceAccordingToAction(action, sentence);

    await this.nlp.updateSentence(sentence).pipe(take(1)).toPromise();
    this.pagination.end--;
    this.pagination.total--;

    if (this.selection.isSelected(sentence)) {
      this.selection.deselect(sentence);
    }
    this.sentences = this.sentences.filter((s) => sentence.text !== s.text);

    this.toastrService.success(truncate(sentence.text), actionTitle, {
      duration: 2000,
      status: 'basic'
    });

    if (this.sentences.length < 1) this.loadData();
  }

  async handleBatchAction(action: Action): Promise<void> {
    if (!this.selection?.selected?.length) {
      this.toastrService.warning('No data selected', 'Warning', {
        duration: 2000,
        status: 'warning'
      });
      return;
    }
    const actionTitle = this.setActionTitle(action);

    for (let sentence of this.selection.selected) {
      this.setSentenceAccordingToAction(action, sentence);
    }

    await Promise.all(
      this.selection.selected.map(async (sentence) => {
        await this.nlp.updateSentence(sentence).pipe(take(1)).toPromise();
        this.sentences = this.sentences.filter((s) => sentence.text !== s.text);
        this.pagination.end--;
        this.pagination.total--;
      })
    );

    this.toastrService.success(
      `${actionTitle} ${this.selection.selected.length} sentences`,
      actionTitle,
      {
        duration: 2000,
        status: 'basic'
      }
    );

    this.selection.clear();
    if (this.sentences.length < 1) this.loadData();
  }

  private setSentenceAccordingToAction(action: Action, sentence: SentenceExtended): void {
    switch (action) {
      case Action.DELETE:
        sentence.status = SentenceStatus.deleted;
        break;
      case Action.UNKNOWN:
        sentence.classification.intentId = Intent.unknown;
        sentence.classification.entities = [];
        sentence.status = SentenceStatus.validated;
        break;
      case Action.VALIDATE:
        const intentId = sentence.classification.intentId;

        if (!intentId) {
          this.toastrService.show(`Please select an intent first`);
          break;
        }
        if (intentId === Intent.unknown) {
          sentence.classification.intentId = Intent.unknown;
          sentence.classification.entities = [];
        }
        sentence.status = SentenceStatus.validated;
        break;
    }
  }

  private setActionTitle(action: Action): string {
    switch (action) {
      case Action.DELETE:
        return 'Delete';
      case Action.UNKNOWN:
        return 'Unknown';
      case Action.VALIDATE:
        return 'Validate';
    }
  }

  retrieveSentence(sentence: SentenceExtended, tryCount = 0): Subscription | void {
    let exists = this.sentences.find((stnce) => {
      return stnce.text == sentence.text;
    });

    if (exists) {
      this.unselectAllSentences();
      exists._selected = true;
      this.faqTrainingDialog.updateSentence(sentence);
      setTimeout(() => {
        this.faqTrainingList.scrollToSentence(sentence);
      }, 200);
    } else {
      if (this.pagination.size * tryCount < 50) {
        let scrollObservable = this.onScroll();
        if (scrollObservable) {
          return scrollObservable.pipe(take(1)).subscribe(() => {
            setTimeout(() => {
              this.retrieveSentence(sentence, tryCount++);
            }, 200);
          });
        }
      }

      this.faqTrainingFilter.updateFilter({
        search: sentence.text
      });
    }
  }
}

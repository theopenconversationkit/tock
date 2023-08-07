import { SelectionModel } from '@angular/cdk/collections';
import { Component, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { NbToastrService } from '@nebular/theme';
import { lastValueFrom, Observable, Subject, Subscription } from 'rxjs';
import { take, takeUntil, share } from 'rxjs/operators';

import { StateService } from '../../../core-nlp/state.service';
import { PaginatedQuery, truncate } from '../../../model/commons';
import { Intent, PaginatedResult, SearchQuery, Sentence, SentenceStatus } from '../../../model/nlp';
import { NlpService } from '../../../nlp-tabs/nlp.service';
import { Pagination } from '..';
import { Action, SentenceTrainingFilter, SentenceTrainingMode } from './models';
import { SentenceTrainingListComponent } from './sentence-training-list/sentence-training-list.component';
import { SentenceTrainingDialogComponent } from './sentence-training-dialog/sentence-training-dialog.component';
import { SentenceTrainingFiltersComponent } from './sentence-training-filters/sentence-training-filters.component';

import { SentenceFilter } from '../../../sentences-scroll/sentences-scroll.component';

export type SentenceExtended = Sentence & { _selected?: boolean };

@Component({
  selector: 'tock-sentence-training',
  templateUrl: './sentence-training.component.html',
  styleUrls: ['./sentence-training.component.scss']
})
export class SentenceTrainingComponent implements OnInit, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  @Input() sentenceTrainingMode: SentenceTrainingMode;

  @ViewChild('sentenceTrainingList') sentenceTrainingList: SentenceTrainingListComponent;
  @ViewChild('sentenceTrainingDialog') sentenceTrainingDialog: SentenceTrainingDialogComponent;
  @ViewChild('sentenceTrainingFilter') sentenceTrainingFilter: SentenceTrainingFiltersComponent;

  selection: SelectionModel<SentenceExtended> = new SelectionModel<SentenceExtended>(true, []);
  Action: typeof Action = Action;
  filters = {
    search: null,
    sort: [{ first: 'creationDate', second: false }],
    intentId: null,
    status: [],
    onlyToReview: false,
    maxIntentProbability: 100,
    minIntentProbability: 0
  };

  loading: boolean = false;
  isFilteredUnknown: boolean = false;

  sentences: SentenceExtended[] = [];

  constructor(private nlp: NlpService, private state: StateService, private toastrService: NbToastrService) {}

  ngOnInit(): void {
    this.initFilters();
    this.loadData();
    this.state.configurationChange.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.loadData();
      this.closeDetails();
    });
  }

  filterSentenceTraining(filters: SentenceTrainingFilter): void {
    this.isFilteredUnknown = filters.showUnknown;

    this.selection.clear();
    this.initFilters();

    this.filters = { ...this.filters, ...filters };
    this.loadData();
  }

  initFilters() {
    if (this.sentenceTrainingMode === SentenceTrainingMode.INBOX) {
      this.filters = { ...this.filters, ...{ intentId: null } };
      this.filters = { ...this.filters, ...{ status: [SentenceStatus.inbox] } };
    }

    if (this.sentenceTrainingMode === SentenceTrainingMode.UNKNOWN || this.isFilteredUnknown) {
      this.filters = { ...this.filters, ...{ intentId: Intent.unknown } };
    }

    if (this.sentenceTrainingMode === SentenceTrainingMode.RAGEXCLUDED) {
      this.filters = { ...this.filters, ...{ intentId: Intent.ragExcluded } };
    }

    if ([Intent.unknown, Intent.ragExcluded].includes(this.filters.intentId)) {
      this.filters = { ...this.filters, ...{ status: [SentenceStatus.validated, SentenceStatus.model] } };
    } else {
      this.filters = { ...this.filters, ...{ status: [SentenceStatus.inbox] } };
    }
  }

  sortSentenceTraining(sort: boolean): void {
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
    showLoadingSpinner: boolean = true,
    partialReload: boolean = false
  ): Observable<PaginatedResult<SentenceExtended>> {
    if (showLoadingSpinner) this.loading = true;

    let search = this.search(this.state.createPaginatedQuery(start, size)).pipe(takeUntil(this.destroy$), share());

    search.subscribe({
      next: (data: PaginatedResult<SentenceExtended>) => {
        this.pagination.total = data.total;
        if (!partialReload || this.pagination.end > this.pagination.total) {
          this.pagination.end = data.end;
        }

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

    await lastValueFrom(this.nlp.updateSentence(sentence));

    this.pagination.total--;
    this.loadSentencesAfterActionPerformed();

    if (this.selection.isSelected(sentence)) {
      this.selection.deselect(sentence);
    }
    this.sentences = this.sentences.filter((s) => sentence.text !== s.text);

    this.toastrService.success(truncate(sentence.text), actionTitle, {
      duration: 2000,
      status: 'basic'
    });
  }

  async handleBatchAction(action: Action): Promise<void> {
    const actionTitle = this.setActionTitle(action);
    let actionPerformed = 0;

    for (let sentence of this.selection.selected) {
      this.setSentenceAccordingToAction(action, sentence);
    }

    await Promise.all(
      this.selection.selected.map(async (sentence) => {
        await this.nlp.updateSentence(sentence).pipe(take(1)).toPromise();
        this.sentences = this.sentences.filter((s) => sentence.text !== s.text);
        this.pagination.total--;
        actionPerformed++;
      })
    );

    this.loadSentencesAfterActionPerformed(actionPerformed);

    this.toastrService.success(`${actionTitle} ${this.selection.selected.length} sentences`, actionTitle, {
      duration: 2000,
      status: 'basic'
    });

    this.selection.clear();
  }

  private loadSentencesAfterActionPerformed(actionPerformed: number = 1): void {
    if (this.pagination.end <= this.pagination.total) {
      const start = this.pagination.end - actionPerformed >= 0 ? this.pagination.end - actionPerformed : 0;
      this.loadData(start, actionPerformed, true, true, true);
    } else {
      this.pagination.end = this.pagination.end - actionPerformed;
      const start = this.pagination.end - this.pagination.size >= this.pagination.size ? this.pagination.end - this.pagination.size : 0;
      if (this.pagination.start > 0 && this.pagination.start === this.pagination.total) {
        this.loadData(start);
      }
    }
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
      case Action.RAGEXCLUDED:
        sentence.classification.intentId = Intent.ragExcluded;
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
      case Action.RAGEXCLUDED:
        return 'Rag excluded';
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
      this.sentenceTrainingDialog.updateSentence(sentence);
      setTimeout(() => {
        this.sentenceTrainingList.scrollToSentence(sentence);
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

      this.sentenceTrainingFilter.updateFilter({
        search: sentence.text,
        showUnknown: false
      });
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}

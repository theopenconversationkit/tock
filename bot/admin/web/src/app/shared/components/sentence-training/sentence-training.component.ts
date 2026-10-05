import { SelectionModel } from '@angular/cdk/collections';
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  HostListener,
  Inject,
  Input,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { Observable, Subject, Subscription } from 'rxjs';
import { take, takeUntil, share } from 'rxjs/operators';

import { StateService } from '../../../core-nlp/state.service';
import { PaginatedQuery } from '../../../model/commons';
import {
  EntityDefinition,
  Intent,
  PaginatedResult,
  SearchQuery,
  Sentence,
  SentenceStatus,
  TranslateSentencesQuery,
  UpdateSentencesQuery
} from '../../../model/nlp';
import { NlpService } from '../../../core-nlp/nlp.service';
import { ChoiceDialogComponent, Pagination } from '..';
import { Action, SentenceTrainingMode } from './models';
import { SentenceTrainingDialogComponent } from './sentence-training-dialog/sentence-training-dialog.component';
import { SentenceTrainingFiltersComponent } from './sentence-training-filters/sentence-training-filters.component';
import { UserRole } from '../../../model/auth';
import { saveAs } from 'file-saver-es';
import { SentenceTrainingService } from './sentence-training.service';
import { getSentenceId } from './commons/utils';
import { DOCUMENT } from '@angular/common';
import { getExportFileName, scrollToPageTop } from '../../utils';
import { TranslocoService } from '@jsverse/transloco';

export type SentenceExtended = Sentence & { _showDialog?: boolean; _showStatsDetails?: boolean; _intentBeforeClassification?: string };

@Component({
  selector: 'tock-sentence-training',
  templateUrl: './sentence-training.component.html',
  styleUrls: ['./sentence-training.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SentenceTrainingComponent implements OnInit, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  @Input() sentenceTrainingMode: SentenceTrainingMode;

  @ViewChild('sentenceTrainingDialog') sentenceTrainingDialog: SentenceTrainingDialogComponent;

  @ViewChild('sentenceTrainingFilter') sentenceTrainingFilter: SentenceTrainingFiltersComponent;

  loading: boolean = false;
  unloading: boolean = false;

  sentences: SentenceExtended[] = [];

  selection: SelectionModel<SentenceExtended> = new SelectionModel<SentenceExtended>(true, []);

  Action: typeof Action = Action;

  SentenceTrainingMode = SentenceTrainingMode;

  filters: Partial<SearchQuery> = {
    search: null,
    sort: null, //[{ first: 'creationDate', second: false }],
    intentId: null,
    status: [],
    onlyToReview: false,
    maxIntentProbability: 100,
    minIntentProbability: 0
  };

  isSorted: boolean = false;

  constructor(
    private nlp: NlpService,
    private state: StateService,
    private sentenceTrainingService: SentenceTrainingService,
    private elementRef: ElementRef,
    private toastrService: NbToastrService,
    private nbDialogService: NbDialogService,
    @Inject(DOCUMENT) private document: Document,
    private cd: ChangeDetectorRef,
    private transloco: TranslocoService
  ) {}

  ngOnInit(): void {
    this.initFilters();
    this.loadData();
    this.state.configurationChange.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.loadData();
      this.closeDetails();
    });
  }

  @HostListener('document:click', ['$event'])
  documentClick(event: MouseEvent) {
    this.sentenceTrainingService.documentClick(event);
  }

  handleToggleSelectAll(value: boolean): void {
    if (!value) {
      this.selection.clear();
    } else {
      this.sentences.forEach((sentence) => this.selection.select(sentence));
    }
  }

  toggleSort(): void {
    this.isSorted = !this.isSorted;
    this.sortSentenceTraining(this.isSorted);
  }

  filterSentenceTraining(filters: Partial<SearchQuery>): void {
    this.selection.clear();
    this.initFilters();

    this.filters = { ...this.filters, ...filters };
    this.loadData();
  }

  initFilters(): void {
    if (this.sentenceTrainingMode === SentenceTrainingMode.INBOX) {
      this.filters = { ...this.filters, ...{ intentId: null } };
      this.filters = { ...this.filters, ...{ status: [SentenceStatus.inbox] } };
    }

    if (this.sentenceTrainingMode === SentenceTrainingMode.UNKNOWN) {
      this.filters = { ...this.filters, ...{ intentId: Intent.unknown } };
    }

    if (this.sentenceTrainingMode === SentenceTrainingMode.RAGEXCLUDED) {
      this.filters = { ...this.filters, ...{ intentId: Intent.ragExcluded } };
    }

    if ([Intent.unknown, Intent.ragExcluded].includes(this.filters.intentId)) {
      this.filters = { ...this.filters, ...{ status: [SentenceStatus.validated, SentenceStatus.model] } };
    }
  }

  sortSentenceTraining(sort: boolean): void {
    this.selection.clear();
    if (!this.filters.sort) this.filters.sort = [{ first: 'creationDate', second: false }];
    this.filters.sort[0].second = sort;
    this.loadData();
  }

  pagination: Pagination = {
    start: 0,
    end: undefined,
    size: 10,
    total: undefined
  };

  paginationChange(): void {
    this.selection.clear();
    this.loadData(this.pagination.start, this.pagination.size, false, true, false, true);
  }

  onScroll(): Observable<PaginatedResult<SentenceExtended>> {
    if (this.loading || this.pagination.end >= this.pagination.total) return;

    return this.loadData(this.pagination.end, this.pagination.size, true, false);
  }

  refresh(clearSelection?: boolean): void {
    if (clearSelection) this.selection.clear();
    this.loadData();
  }

  loadData(
    start: number = 0,
    size: number = this.pagination.size,
    add: boolean = false,
    showLoadingSpinner: boolean = true,
    partialReload: boolean = false,
    scrollToTop: Boolean = false
  ): Observable<PaginatedResult<SentenceExtended>> {
    if (showLoadingSpinner) this.loading = true;

    let search = this.search(this.state.createPaginatedQuery(start, size)).pipe(share(), takeUntil(this.destroy$));

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

        if (scrollToTop) {
          scrollToPageTop(this.document);
        }
        this.loading = false;
        this.cd.markForCheck();
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
    return new SearchQuery(
      query.namespace,
      query.applicationName,
      query.language,
      query.start,
      query.size,
      null,
      this.filters.search,
      this.filters.intentId,
      this.filters.status,
      this.filters.entityType,
      this.filters.entityRolesToInclude,
      this.filters.entityRolesToExclude,
      this.filters.modifiedAfter,
      this.filters.modifiedBefore,
      this.filters.sort,
      this.filters.onlyToReview,
      this.filters.searchSubEntities,
      this.filters.user,
      this.filters.allButUser,
      this.filters.maxIntentProbability / 100,
      this.filters.minIntentProbability / 100,
      this.filters.configuration
    );
  }

  dialogDetailsSentence: SentenceExtended;

  unselectAllSentences(): void {
    this.sentences.forEach((s) => (s._showDialog = false));
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
      sentence._showDialog = true;
      this.dialogDetailsSentence = sentence;
    }
    this.cd.markForCheck();
  }

  clearSentence(sentence: SentenceExtended) {
    this.pagination.total--;
    this.loadSentencesAfterActionPerformed();
    this.sentences = this.sentences.filter((s) => sentence.text !== s.text);
  }

  async handleBatchAction(action: Action): Promise<void> {
    const actionTitle = this.getActionTitle(action);
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

    this.toastrService.success(
      this.transloco.translate('shared.sentence-training.messages.batch-action-success', {
        action: actionTitle,
        count: this.selection.selected.length
      }),
      actionTitle,
      { duration: 2000, status: 'basic' }
    );

    this.selection.clear();

    this.cd.markForCheck();
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
          this.toastrService.show(this.transloco.translate('shared.sentence-training.errors.please-select-intent'));
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

  private getActionTitle(action: Action): string {
    switch (action) {
      case Action.DELETE:
        return this.transloco.translate('shared.sentence-training.action-titles.delete');
      case Action.UNKNOWN:
        return this.transloco.translate('shared.sentence-training.action-titles.unknown');
      case Action.RAGEXCLUDED:
        return this.transloco.translate('shared.sentence-training.action-titles.ragexcluded');
      case Action.VALIDATE:
        return this.transloco.translate('shared.sentence-training.action-titles.validate');
    }
  }

  retrieveSentence(sentence: SentenceExtended, tryCount = 0): Subscription | void {
    if (this.unloading) return;

    let exists = this.sentences.find((stnce) => {
      return stnce.text == sentence.text;
    });

    if (exists) {
      this.unselectAllSentences();
      exists._showDialog = true;
      this.sentenceTrainingDialog.updateSentence(sentence);
      setTimeout(() => {
        this.scrollToSentence(sentence);
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
        search: sentence.text
      });
    }
  }

  scrollToSentence(sentence: SentenceExtended): void {
    const id = getSentenceId(sentence);
    const nativeElement: HTMLElement = this.elementRef.nativeElement;
    const found: Element | null = nativeElement.querySelector(`#${id}`);
    if (found) found.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'start' });
  }

  downloadSentencesDump(): void {
    this.nlp
      .getSentencesDump(
        this.state.currentApplication,
        this.toSearchQuery(this.state.createPaginatedQuery(0)),
        this.state.hasRole(UserRole.technicalAdmin)
      )
      .subscribe((blob) => {
        const exportFileName = getExportFileName(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          'sentences',
          'json'
        );
        saveAs(blob, exportFileName);
        this.toastrService.success(
          this.transloco.translate('shared.sentence-training.success.dump-provided'),
          this.transloco.translate('shared.sentence-training.success.sentences-dump-title')
        );
      });
  }

  getCurrentSearchQuery(): SearchQuery {
    return this.toSearchQuery(this.state.createPaginatedQuery(0, 10000));
  }

  changeSentencesIntent(intentId: string, changeAll?: boolean): void {
    if (!this.selection.selected.length && !changeAll) {
      const action = this.transloco.translate('shared.sentence-training.dialogs.change-entity-all-action');
      const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
        context: {
          title: this.transloco.translate('shared.sentence-training.dialogs.no-sentence-selected-title'),
          subtitle: this.transloco.translate('shared.sentence-training.dialogs.change-intent-all-subtitle'),
          actions: [
            { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
            { actionName: action, buttonStatus: 'danger' }
          ],
          modalStatus: 'danger'
        }
      });
      dialogRef.onClose.subscribe((result) => {
        if (result?.toLowerCase() === action.toLowerCase()) {
          this.changeSentencesIntent(intentId, true);
        }
      });
    } else {
      this.nlp
        .updateSentences(
          new UpdateSentencesQuery(
            this.state.currentApplication.namespace,
            this.state.currentApplication.name,
            this.state.currentLocale,
            this.selection.selected.length ? this.selection.selected : [],
            this.selection.selected.length ? null : this.getCurrentSearchQuery(),
            intentId
          )
        )
        .subscribe((r) => {
          const n = r.nbUpdates;
          let message = `No sentence updated`;
          if (n === 0) {
            message = this.transloco.translate('shared.sentence-training.messages.no-sentence-updated');
          } else if (n === 1) {
            message = this.transloco.translate('shared.sentence-training.messages.one-sentence-updated');
          } else {
            message = this.transloco.translate('shared.sentence-training.messages.multiple-sentences-updated', { count: n });
          }
          this.toastrService.show(message, this.transloco.translate('common.actions.update'), { duration: 2000 });

          this.refresh(true);
        });
    }
  }

  changeSentencesEntity(entities: { old: EntityDefinition; new: EntityDefinition }, changeAll?: boolean): void {
    if (!this.selection.selected.length && !changeAll) {
      const action = this.transloco.translate('shared.sentence-training.dialogs.change-entity-all-action');
      const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
        context: {
          title: this.transloco.translate('shared.sentence-training.dialogs.no-sentence-selected-title'),
          subtitle: this.transloco.translate('shared.sentence-training.dialogs.change-entity-all-subtitle'),
          actions: [
            { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
            { actionName: action, buttonStatus: 'danger' }
          ],
          modalStatus: 'danger'
        }
      });
      dialogRef.onClose.subscribe((result) => {
        if (result?.toLowerCase() === action.toLowerCase()) {
          this.changeSentencesEntity(entities, true);
        }
      });
    } else {
      this.nlp
        .updateSentences(
          new UpdateSentencesQuery(
            this.state.currentApplication.namespace,
            this.state.currentApplication.name,
            this.state.currentLocale,
            this.selection.selected.length ? this.selection.selected : [],
            this.selection.selected.length ? null : this.getCurrentSearchQuery(),
            null,
            entities.old,
            entities.new
          )
        )
        .subscribe((r) => {
          const n = r.nbUpdates;
          let message = `No sentence updated`;
          if (n === 0) {
            message = this.transloco.translate('shared.sentence-training.messages.no-sentence-updated');
          } else if (n === 1) {
            message = this.transloco.translate('shared.sentence-training.messages.one-sentence-updated');
          } else {
            message = this.transloco.translate('shared.sentence-training.messages.multiple-sentences-updated', { count: n });
          }
          this.toastrService.show(message, this.transloco.translate('common.actions.update'), { duration: 2000 });

          this.refresh(true);
        });
    }
  }

  translateSentences(locale: string, translateAll?: boolean): void {
    if (!this.selection.selected.length && !translateAll) {
      const action = this.transloco.translate('shared.sentence-training.dialogs.translate-all-action');
      const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
        context: {
          title: this.transloco.translate('shared.sentence-training.dialogs.no-sentence-selected-title'),
          subtitle: this.transloco.translate('shared.sentence-training.dialogs.translate-all-subtitle'),
          actions: [
            { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
            { actionName: action, buttonStatus: 'danger' }
          ],
          modalStatus: 'danger'
        }
      });
      dialogRef.onClose.subscribe((result) => {
        if (result?.toLowerCase() === action.toLowerCase()) {
          this.translateSentences(locale, true);
        }
      });
    } else {
      this.nlp
        .translateSentences(
          new TranslateSentencesQuery(
            this.state.currentApplication.namespace,
            this.state.currentApplication.name,
            this.state.currentLocale,
            locale,
            this.selection.selected.length ? this.selection.selected : [],
            this.selection.selected.length ? null : this.getCurrentSearchQuery()
          )
        )
        .subscribe((r) => {
          const n = r.nbTranslations;
          let message = `No sentence translated`;
          if (n === 0) {
            message = this.transloco.translate('shared.sentence-training.messages.no-sentence-translated');
          } else if (n === 1) {
            message = this.transloco.translate('shared.sentence-training.messages.one-sentence-translated');
          } else {
            message = this.transloco.translate('shared.sentence-training.messages.multiple-sentences-translated', { count: n });
          }
          this.toastrService.show(message, this.transloco.translate('common.actions.update'), { duration: 2000 });
        });
    }
  }

  ngOnDestroy(): void {
    this.unloading = true;
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}

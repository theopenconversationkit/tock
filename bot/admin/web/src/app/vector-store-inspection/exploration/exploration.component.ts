import { Component, inject, OnDestroy, OnInit, QueryList, ViewChildren } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';

import { BotConfigurationService } from '../../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { Pagination } from '../../shared/components/pagination/pagination.component';
import { AnomalyCode, IndexAnomaly, IndexStats, InspectedDocument, VectorStoreIndex } from '../models/vector-store-inspection.models';
import { VectorStoreInspectionService } from '../services/vector-store-inspection.service';
import { VectorStoreInspectionStateService } from '../services/vector-store-inspection-state.service';
import { NbTooltipDirective } from '@nebular/theme';

/**
 * Ingestion report for a single index: counts, anomaly signals, and a
 * paginated list of documents that can be expanded down to their chunks.
 *
 * Pinning a chunk here arms the diagnostic view: pinned chunks always appear
 * in its results, even when no retrieval channel returns them.
 */
@Component({
  selector: 'tock-vector-store-exploration',
  templateUrl: './exploration.component.html',
  styleUrl: './exploration.component.scss',
  standalone: false
})
export class ExplorationComponent implements OnInit, OnDestroy {
  destroy$: Subject<unknown> = new Subject();
  loading: boolean = true;

  configurations: BotApplicationConfiguration[];

  indexes: VectorStoreIndex[] = [];
  currentIndex: VectorStoreIndex | null = null;

  stats: IndexStats | null = null;
  anomalies: IndexAnomaly[] = [];
  documents: InspectedDocument[] = [];

  textFilter: string = '';
  activeAnomaly: AnomalyCode | null = null;

  pagination: Pagination = { start: 0, end: 0, size: 25, total: 0 };

  @ViewChildren(NbTooltipDirective) tooltips: QueryList<NbTooltipDirective>;

  private readonly botConfiguration = inject(BotConfigurationService);
  private readonly inspection = inject(VectorStoreInspectionService);
  public readonly state = inject(VectorStoreInspectionStateService);
  private documentsRequestId = 0;

  ngOnInit(): void {
    // Read the list from the stream rather than from the loadIndexes result:
    // the state service pushes the list before the preselected index, and
    // nb-select can only match a selection against options it already holds.
    this.state.indexes$.pipe(takeUntil(this.destroy$)).subscribe((indexes) => {
      this.indexes = indexes;
    });

    this.state.currentIndex$.pipe(takeUntil(this.destroy$)).subscribe((index) => {
      const changed = index?.indexName !== this.currentIndex?.indexName;
      this.currentIndex = index;

      if (changed) this.documentsRequestId++;

      if (index && changed) {
        // A different index invalidates the current page. Pins are left alone:
        // they deliberately survive so two ingestions can be compared.
        this.resetPagination();
        this.fetchDocuments();
      }
    });

    // Namespace and bot can be switched at any time from the header. Indexes
    // belong to a namespace/bot pair, so everything held in the feature state
    // has to be dropped and reloaded.
    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((confs) => {
      this.configurations = confs;

      const botChanged = this.state.applyBotContext(this.botKey(confs));

      if (!confs.length) {
        this.loading = false;
        return;
      }

      if (botChanged) {
        this.resetFilters();
        this.stats = null;
        this.anomalies = [];
        this.documents = [];
        this.fetchIndexes();
      } else if (this.currentIndex) {
        // Coming back to the view with the same bot: the index list is already
        // cached, only the page needs refreshing.
        this.fetchDocuments();
      } else {
        this.fetchIndexes();
      }
    });
  }

  fetchIndexes(): void {
    this.loading = true;

    this.state
      .loadIndexes(true)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (indexes) => {
          // No index at all: nothing will arrive through currentIndex$ either,
          // so the spinner has to be released here.
          if (!indexes.length) this.loading = false;
        },
        error: () => (this.loading = false)
      });
  }

  fetchDocuments(): void {
    if (!this.currentIndex) return;

    this.loading = true;
    const requestId = ++this.documentsRequestId;

    this.inspection
      .getDocuments({
        indexName: this.currentIndex.indexName,
        filter: {
          text: this.textFilter?.trim() || null,
          anomaly: this.activeAnomaly
        },
        start: this.pagination.start,
        size: this.pagination.size,
        includeStats: true,
        includeChunks: true
      })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          if (requestId !== this.documentsRequestId) return;
          this.stats = response.stats;
          this.anomalies = response.anomalies;
          this.documents = response.rows;

          // A new object reference is required: the shared pagination component
          // recomputes its summary in ngOnChanges, which never fires on an
          // in-place mutation.
          this.pagination = { ...this.pagination, total: response.total, end: response.end };

          this.loading = false;
        },
        error: () => {
          if (requestId === this.documentsRequestId) this.loading = false;
        }
      });
  }

  private botKey(confs: BotApplicationConfiguration[]): string | null {
    if (!confs.length) return null;
    return `${confs[0].namespace}/${confs[0].botId}`;
  }

  selectIndex(index: VectorStoreIndex): void {
    this.state.selectIndex(index);
  }

  /**
   * The shared pagination component mutates the pagination object in place and
   * emits nothing, so there is no event payload to read here.
   */
  paginationChange(): void {
    this.fetchDocuments();
  }

  applyTextFilter(): void {
    this.resetPagination();
    this.fetchDocuments();
  }

  clearTextFilter(): void {
    this.textFilter = '';
    this.applyTextFilter();
  }

  selectAnomaly(code: AnomalyCode | null): void {
    this.hideTooltips();
    this.activeAnomaly = code;
    this.resetPagination();
    this.fetchDocuments();
  }

  /**
   * Nebular mounts tooltips in a detached overlay. When nb-select closes its
   * option list, the option is destroyed without the directive ever receiving
   * a mouseleave, so the overlay stays on screen forever. Closing them by hand
   * is the only reliable way out.
   */
  hideTooltips(): void {
    this.tooltips?.forEach((tooltip) => tooltip.hide());
  }

  get hasActiveFilter(): boolean {
    return !!this.activeAnomaly || !!this.textFilter?.trim();
  }

  clearFilters(): void {
    this.resetFilters();
    this.fetchDocuments();
  }

  private resetFilters(): void {
    this.activeAnomaly = null;
    this.textFilter = '';
    this.resetPagination();
  }

  private resetPagination(): void {
    this.pagination = { ...this.pagination, start: 0, end: 0, total: 0 };
  }

  trackByDocumentId(_index: number, document: InspectedDocument): string {
    return document.documentId;
  }

  ngOnDestroy(): void {
    this.destroy$.next(null);
    this.destroy$.complete();
  }
}

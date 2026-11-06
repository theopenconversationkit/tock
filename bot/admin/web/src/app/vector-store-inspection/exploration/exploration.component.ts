import { Component, inject, OnDestroy, OnInit, QueryList, ViewChildren } from '@angular/core';
import { Subject, exhaustMap, finalize, take, takeUntil, takeWhile, timer } from 'rxjs';
import { TranslocoService } from '@jsverse/transloco';
import { DialogService } from '../../core-nlp/dialog.service';
import { ChoiceDialogComponent } from '../../shared/components';
import { KnowledgeBaseJobState } from '../../knowledge-base/models';
import { VectorDbProvider } from '../../configuration/vector-db-settings/models/providers-configuration';

import { BotConfigurationService } from '../../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { Pagination } from '../../shared/components/pagination/pagination.component';
import {
  AnomalyCode,
  IndexAnomaly,
  IndexStats,
  InspectedDocument,
  VectorStoreIndex,
  InspectedChunk,
  IndexMetadata,
  VectorStoreMutation
} from '../models/vector-store-inspection.models';
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
  private metadataRequestId = 0;
  private readonly contextChanged$ = new Subject<void>();
  private readonly dialogs = inject(DialogService);
  private readonly transloco = inject(TranslocoService);

  managementEnabled = false;
  managementBusy = false;
  metadataLoading = false;
  indexMetadata: IndexMetadata | null = null;
  embeddingModel = '';
  managementError: string | null = null;
  metadataError: string | null = null;
  managementMessage: string | null = null;
  pendingJobId: string | null = null;

  get canSetEmbeddingModel(): boolean {
    if (!this.indexMetadata) return false;
    const value = this.indexMetadata.metadata['embedding_model'];
    return value == null || (typeof value === 'string' && !value.trim());
  }

  get hasMetadata(): boolean {
    return !!this.indexMetadata && Object.keys(this.indexMetadata.metadata).length > 0;
  }

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

      if (changed) {
        this.documentsRequestId++;
        this.metadataRequestId++;
        this.indexMetadata = null;
        this.embeddingModel = '';
        this.stats = null;
        this.documents = [];
        this.anomalies = [];
        this.metadataError = null;
        this.metadataLoading = false;
      }
      if (!index) this.loading = false;

      if (index && changed) {
        // A different index invalidates the current page. Pins are left alone:
        // they deliberately survive so two ingestions can be compared.
        this.resetPagination();
        this.fetchDocuments();
        this.fetchMetadata();
      }
    });

    // Namespace and bot can be switched at any time from the header. Indexes
    // belong to a namespace/bot pair, so everything held in the feature state
    // has to be dropped and reloaded.
    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((confs) => {
      this.configurations = confs;

      const botChanged = this.state.applyBotContext(this.botKey(confs));

      if (botChanged) {
        this.contextChanged$.next();
        this.managementBusy = false;
        this.managementEnabled = false;
        this.managementError = null;
        this.managementMessage = null;
        this.pendingJobId = null;
      }
      if (!confs.length) {
        this.loading = false;
        return;
      }
      this.state
        .loadCapabilities()
        .pipe(takeUntil(this.destroy$), takeUntil(this.contextChanged$))
        .subscribe({
          next: (capabilities) => {
            this.managementEnabled = capabilities.provider === VectorDbProvider.PGVector;
            this.fetchMetadata();
          },
          error: () => {
            this.managementEnabled = false;
          }
        });
      if (botChanged) {
        this.resetFilters();
        this.stats = null;
        this.anomalies = [];
        this.documents = [];
        this.fetchIndexes();
      } else if (this.currentIndex) {
        // Coming back to the view with the same bot: reload the list so an index created
        // meanwhile (e.g. from the knowledge base) is visible, then reconcile the selection.
        // Selection kept: currentIndex$ sees no name change and does not refetch, so refresh the
        // page here. Selection changed: currentIndex$ drives the reload. Never both, so no double fetch.
        this.loading = true;
        this.state
          .refreshIndexes()
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: (selectionKept) => {
              if (selectionKept) this.fetchDocuments();
              else if (!this.currentIndex) this.loading = false;
            },
            error: () => (this.loading = false)
          });
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
          if (!response.rows.length && response.total > 0 && this.pagination.start >= response.total) {
            this.pagination = { ...this.pagination, start: Math.floor((response.total - 1) / this.pagination.size) * this.pagination.size };
            this.fetchDocuments();
            return;
          }
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

  fetchMetadata(): void {
    if (!this.managementEnabled || !this.currentIndex) return;
    const requestId = ++this.metadataRequestId;
    this.metadataLoading = true;
    this.metadataError = null;
    this.inspection
      .getIndexMetadata(this.currentIndex.indexName)
      .pipe(takeUntil(this.destroy$), takeUntil(this.contextChanged$))
      .subscribe({
        next: (metadata) => {
          if (requestId !== this.metadataRequestId) return;
          this.indexMetadata = metadata;
          this.metadataLoading = false;
        },
        error: (error) => {
          if (requestId !== this.metadataRequestId) return;
          this.indexMetadata = null;
          this.metadataLoading = false;
          this.metadataError = this.errorKey(error);
        }
      });
  }

  refresh(): void {
    this.state
      .refreshIndexes()
      .pipe(takeUntil(this.destroy$), takeUntil(this.contextChanged$))
      .subscribe({
        next: (kept) => {
          if (kept) {
            this.fetchDocuments();
            this.fetchMetadata();
          }
        },
        error: (error) => {
          this.managementError = this.errorKey(error);
        }
      });
  }

  saveEmbeddingModel(): void {
    if (this.managementBusy || !this.currentIndex || !this.canSetEmbeddingModel || !this.embeddingModel.trim()) return;
    const indexName = this.currentIndex.indexName;
    const model = this.embeddingModel.trim();
    this.confirm(
      'vsi.management.add_model',
      this.transloco.translate('vsi.management.confirm_model', { model, index: indexName }),
      () => {
        this.managementBusy = true;
        this.managementError = null;
        this.managementMessage = null;
        this.inspection
          .setEmbeddingModel(indexName, model)
          .pipe(
            takeUntil(this.destroy$),
            takeUntil(this.contextChanged$),
            finalize(() => {
              this.managementBusy = false;
            })
          )
          .subscribe({
            next: (metadata) => {
              this.metadataRequestId++;
              this.metadataLoading = false;
              this.indexMetadata = metadata;
              this.embeddingModel = '';
              this.managementMessage = 'vsi.management.model_saved';
            },
            error: (error) => {
              this.managementError = this.errorKey(error);
              this.fetchMetadata();
            }
          });
      },
      'primary'
    );
  }

  deleteChunk(chunk: InspectedChunk, document: InspectedDocument): void {
    if (this.managementBusy || !this.managementEnabled || !this.currentIndex || !chunk.rowId) return;
    const index = this.currentIndex;
    const unpublish = index.isCurrent && chunk.metadata?.['source_type'] === 'internal_kb';
    const last = (this.stats?.chunkCount ?? index.chunkCount) === 1;
    const title = unpublish ? 'vsi.management.unpublish_chunk' : 'vsi.management.delete_chunk';
    let message = this.transloco.translate(unpublish ? 'vsi.management.confirm_unpublish' : 'vsi.management.confirm_chunk', {
      title: document.title,
      chunk: chunk.chunk,
      index: index.indexName
    });
    if (last) message += '\n\n' + this.transloco.translate('vsi.management.last_chunk_warning');
    if (!unpublish) message += '\n\n' + this.transloco.translate('vsi.management.reingestion_warning');
    this.confirm(title, message, () => {
      this.managementBusy = true;
      this.managementError = null;
      this.managementMessage = null;
      this.inspection
        .deleteChunk({
          indexName: index.indexName,
          rowId: chunk.rowId,
          expectedCurrent: index.isCurrent,
          allowEmptyIndex: last,
          unpublishKnowledgeBase: unpublish
        })
        .pipe(takeUntil(this.destroy$), takeUntil(this.contextChanged$))
        .subscribe({
          next: (result) => this.afterMutation(result, 'vsi.management.chunk_deleted'),
          error: (error) => {
            this.managementBusy = false;
            this.managementError = this.errorKey(error);
            this.refresh();
          }
        });
    });
  }

  deleteIndex(): void {
    if (this.managementBusy || !this.managementEnabled || !this.currentIndex || this.currentIndex.isCurrent) return;
    const index = this.currentIndex;
    this.confirm(
      'vsi.management.delete_index',
      this.transloco.translate('vsi.management.confirm_index', { index: index.indexName, count: index.chunkCount }),
      () => {
        this.managementBusy = true;
        this.managementError = null;
        this.managementMessage = null;
        this.inspection
          .deleteIndex(index.indexName)
          .pipe(takeUntil(this.destroy$), takeUntil(this.contextChanged$))
          .subscribe({
            next: (result) => this.afterMutation(result, 'vsi.management.index_deleted'),
            error: (error) => {
              this.managementBusy = false;
              this.managementError = this.errorKey(error);
              this.refresh();
            }
          });
      }
    );
  }

  private afterMutation(result: VectorStoreMutation, success: string): void {
    if (result.jobId) {
      this.pendingJobId = result.jobId;
      this.followUnpublication();
    } else {
      this.managementBusy = false;
      this.managementMessage = success;
      this.refresh();
    }
  }

  followUnpublication(): void {
    if (!this.pendingJobId) return;
    this.managementBusy = true;
    this.managementError = null;
    this.managementMessage = 'vsi.management.unpublish_pending';
    timer(0, 2000)
      .pipe(
        exhaustMap(() => this.inspection.getMutationJob(this.pendingJobId)),
        takeWhile((job) => job.state === KnowledgeBaseJobState.QUEUED || job.state === KnowledgeBaseJobState.RUNNING, true),
        take(61),
        takeUntil(this.destroy$),
        takeUntil(this.contextChanged$),
        finalize(() => {
          this.managementBusy = false;
        })
      )
      .subscribe({
        next: (job) => {
          if (job.state === KnowledgeBaseJobState.COMPLETED || job.state === KnowledgeBaseJobState.FAILED) {
            this.pendingJobId = null;
            if (job.state === KnowledgeBaseJobState.FAILED || job.failures?.length || job.error) {
              this.managementMessage = null;
              this.managementError = 'vsi.management.errors.unpublish_failed';
            } else this.managementMessage = 'vsi.management.unpublished';
            this.refresh();
          }
        },
        error: () => {
          this.managementError = 'vsi.management.errors.job_status_unavailable';
        }
      });
  }

  private confirm(title: string, message: string, action: () => void, status = 'danger'): void {
    const botKey = this.botKey(this.configurations ?? []);
    const indexName = this.currentIndex?.indexName;
    this.dialogs
      .openDialog(ChoiceDialogComponent, {
        context: {
          title: this.transloco.translate(title),
          subtitle: message,
          modalStatus: status,
          actions: [
            { actionName: this.transloco.translate('vsi.management.cancel'), buttonStatus: 'basic', ghost: true, returnValue: false },
            { actionName: this.transloco.translate('vsi.management.confirm'), buttonStatus: status, returnValue: true }
          ]
        }
      })
      .onClose.pipe(takeUntil(this.destroy$), takeUntil(this.contextChanged$))
      .subscribe((confirmed) => {
        if (
          confirmed === true &&
          !this.managementBusy &&
          botKey === this.botKey(this.configurations ?? []) &&
          indexName === this.currentIndex?.indexName
        )
          action();
      });
  }

  private errorKey(error: any): string {
    const message = error?.error?.errors?.find(
      (item: any) => typeof item.message === 'string' && item.message.startsWith('vsi.management.errors.')
    )?.message;
    return message ?? 'vsi.management.errors.operation_failed';
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
    this.contextChanged$.complete();
  }
}

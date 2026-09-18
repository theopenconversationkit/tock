/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { NbToastrService } from '@nebular/theme';
import { TranslocoService } from '@jsverse/transloco';
import { Subject, debounceTime, takeUntil } from 'rxjs';

import { BotConfigurationService } from '../../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { DialogService } from '../../core-nlp/dialog.service';
import { ChoiceDialogComponent } from '../../shared/components';
import { Pagination } from '../../shared/components/pagination/pagination.component';
import {
  KnowledgeBaseEntry,
  KnowledgeBaseEntryStatus,
  KnowledgeBaseExportEnvelope,
  KnowledgeBaseProjectionState,
  KnowledgeBaseSearchQuery,
  KnowledgeBaseSortField,
  KnowledgeBaseSyncStatus,
  SortDirection
} from '../models';
import { KnowledgeBaseMockScenario } from '../services/knowledge-base-mock-data';
import { KnowledgeBaseMockService } from '../services/knowledge-base-mock.service';
import { KnowledgeBaseService } from '../services/knowledge-base.service';

@Component({
  selector: 'tock-knowledge-base-entries-board',
  templateUrl: './entries-board.component.html',
  styleUrl: './entries-board.component.scss',
  standalone: false
})
export class KnowledgeBaseEntriesBoardComponent implements OnInit, OnDestroy {
  private botConfiguration = inject(BotConfigurationService);
  private knowledgeBaseService = inject(KnowledgeBaseService);
  private dialogService = inject(DialogService);
  private toastrService = inject(NbToastrService);
  private transloco = inject(TranslocoService);
  private router = inject(Router);

  // MOCK ONLY — resolves to null as soon as the module stops providing the mock,
  // which hides the demo scenario selector without any further change.
  private mockService = inject(KnowledgeBaseMockService, { optional: true });

  destroy$: Subject<unknown> = new Subject();

  loading: boolean = true;
  syncing: boolean = false;

  configurations: BotApplicationConfiguration[];

  entries: KnowledgeBaseEntry[] = [];
  tags: string[] = [];
  syncStatus: KnowledgeBaseSyncStatus;

  EntryStatus = KnowledgeBaseEntryStatus;
  ProjectionState = KnowledgeBaseProjectionState;

  sortField: KnowledgeBaseSortField = 'updatedAt';
  sortDirection: SortDirection = 'desc';

  pagination: Pagination = { start: 0, end: 0, size: 25, total: 0 };

  /** Ids selected for a bulk action. Kept across pages on purpose: a filtered, paged
   *  selection is exactly how a post import publication is done. */
  selection = new Set<string>();
  bulkRunning: boolean = false;
  exporting: boolean = false;

  filtersForm = new FormGroup({
    search: new FormControl<string>(''),
    status: new FormControl<KnowledgeBaseEntryStatus | null>(null),
    projectionState: new FormControl<KnowledgeBaseProjectionState | null>(null),
    tag: new FormControl<string | null>(null)
  });

  // MOCK ONLY — demo scenario selector, to be removed along with the mock service.
  mockScenarios: KnowledgeBaseMockScenario[] = Object.values(KnowledgeBaseMockScenario);
  currentMockScenario: KnowledgeBaseMockScenario;

  get mockEnabled(): boolean {
    return !!this.mockService;
  }

  ngOnInit(): void {
    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((confs) => {
      this.configurations = confs;
      if (confs.length) this.refresh();
    });

    this.filtersForm.valueChanges.pipe(debounceTime(300), takeUntil(this.destroy$)).subscribe(() => {
      this.pagination.start = 0;
      this.fetchEntries();
    });

    this.mockService?.scenario$.pipe(takeUntil(this.destroy$)).subscribe((scenario) => (this.currentMockScenario = scenario));
  }

  refresh(): void {
    this.fetchSyncStatus();
    this.fetchTags();
    this.fetchEntries();
  }

  fetchEntries(): void {
    this.loading = true;

    const query: KnowledgeBaseSearchQuery = {
      search: this.filtersForm.value.search,
      status: this.filtersForm.value.status,
      projectionState: this.filtersForm.value.projectionState,
      tag: this.filtersForm.value.tag,
      sort: this.sortField,
      direction: this.sortDirection,
      start: this.pagination.start,
      size: this.pagination.size
    };

    this.knowledgeBaseService
      .searchEntries(query)
      .pipe(takeUntil(this.destroy$))
      .subscribe((result) => {
        this.entries = result.rows;
        this.pagination.total = result.total;
        this.pagination.start = result.start;
        this.pagination.end = result.end;
        this.loading = false;
      });
  }

  fetchSyncStatus(): void {
    this.knowledgeBaseService
      .getSyncStatus()
      .pipe(takeUntil(this.destroy$))
      .subscribe((status) => (this.syncStatus = status));
  }

  fetchTags(): void {
    this.knowledgeBaseService
      .getTags()
      .pipe(takeUntil(this.destroy$))
      .subscribe((tags) => (this.tags = tags));
  }

  // ---------------------------------------------------------------- Sorting and paging

  setSort(field: KnowledgeBaseSortField): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = field === 'title' ? 'asc' : 'desc';
    }
    this.fetchEntries();
  }

  paginationChange(): void {
    this.fetchEntries();
  }

  // ---------------------------------------------------------------- Filters

  get hasActiveFilters(): boolean {
    const value = this.filtersForm.value;
    return !!(value.search || value.status || value.projectionState || value.tag);
  }

  resetFilters(): void {
    this.filtersForm.reset({ search: '', status: null, projectionState: null, tag: null });
  }

  filterByProjectionState(state: KnowledgeBaseProjectionState): void {
    this.filtersForm.patchValue({ projectionState: state, status: null, search: '', tag: null });
  }

  // ---------------------------------------------------------------- Entry actions

  createEntry(): void {
    this.router.navigateByUrl('/knowledge-base/new');
  }

  editEntry(entry: KnowledgeBaseEntry): void {
    this.router.navigateByUrl(`/knowledge-base/detail/${entry.id}`);
  }

  deleteEntry(entry: KnowledgeBaseEntry): void {
    const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
      context: {
        title: this.transloco.translate('knowledge-base.entries-board.delete_entry_title'),
        subtitle: this.transloco.translate('knowledge-base.entries-board.delete_entry_subtitle', { title: entry.title }),
        actions: [
          { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
          { actionName: 'delete', buttonStatus: 'danger' }
        ]
      }
    });

    dialogRef.onClose.pipe(takeUntil(this.destroy$)).subscribe((result) => {
      if (result !== 'delete') return;

      this.knowledgeBaseService
        .deleteEntry(entry.id)
        .pipe(takeUntil(this.destroy$))
        .subscribe(() => {
          this.toastrService.show(
            this.transloco.translate('knowledge-base.entries-board.entry_deleted_message', { title: entry.title }),
            this.transloco.translate('knowledge-base.entries-board.success_title'),
            { duration: 4000, status: 'success' }
          );
          this.refresh();
        });
    });
  }

  // ---------------------------------------------------------------- Index actions

  synchronize(): void {
    this.syncing = true;

    this.knowledgeBaseService
      .synchronize()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (result) => {
          this.syncing = false;
          this.toastrService.show(
            this.transloco.translate('knowledge-base.entries-board.synchronized_message', {
              projected: result.projected,
              removed: result.removed
            }),
            this.transloco.translate('knowledge-base.entries-board.success_title'),
            { duration: 5000, status: 'success' }
          );
          this.refresh();
        },
        error: () => {
          this.syncing = false;
          this.toastrService.show(
            this.transloco.translate('knowledge-base.entries-board.synchronization_failed_message'),
            this.transloco.translate('knowledge-base.entries-board.error_title'),
            { duration: 6000, status: 'danger' }
          );
        }
      });
  }

  createIndex(): void {
    const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
      context: {
        title: this.transloco.translate('knowledge-base.entries-board.create_index_title'),
        subtitle: this.transloco.translate('knowledge-base.entries-board.create_index_subtitle'),
        actions: [
          { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
          { actionName: 'create', buttonStatus: 'primary' }
        ]
      }
    });

    dialogRef.onClose.pipe(takeUntil(this.destroy$)).subscribe((result) => {
      if (result !== 'create') return;

      this.syncing = true;
      this.knowledgeBaseService
        .createIndex()
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (res) => {
            this.syncing = false;
            this.toastrService.show(
              this.transloco.translate('knowledge-base.entries-board.index_created_message', { projected: res.projected }),
              this.transloco.translate('knowledge-base.entries-board.success_title'),
              { duration: 6000, status: 'success' }
            );
            this.refresh();
          },
          error: () => {
            this.syncing = false;
            this.toastrService.show(
              this.transloco.translate('knowledge-base.entries-board.index_creation_failed_message'),
              this.transloco.translate('knowledge-base.entries-board.error_title'),
              { duration: 6000, status: 'danger' }
            );
          }
        });
    });
  }

  // ---------------------------------------------------------------- Selection and bulk actions

  isSelected(entry: KnowledgeBaseEntry): boolean {
    return this.selection.has(entry.id);
  }

  toggleSelection(entry: KnowledgeBaseEntry): void {
    if (this.selection.has(entry.id)) this.selection.delete(entry.id);
    else this.selection.add(entry.id);
  }

  get allOnPageSelected(): boolean {
    return !!this.entries.length && this.entries.every((entry) => this.selection.has(entry.id));
  }

  toggleSelectAllOnPage(): void {
    if (this.allOnPageSelected) this.entries.forEach((entry) => this.selection.delete(entry.id));
    else this.entries.forEach((entry) => this.selection.add(entry.id));
  }

  clearSelection(): void {
    this.selection.clear();
  }

  get selectionCount(): number {
    return this.selection.size;
  }

  bulkUpdateStatus(status: KnowledgeBaseEntryStatus): void {
    if (!this.selectionCount) return;

    this.bulkRunning = true;

    this.knowledgeBaseService
      .bulkUpdateStatus([...this.selection], status)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (result) => {
          this.bulkRunning = false;
          this.clearSelection();
          this.toastrService.show(
            this.transloco.translate(
              status === KnowledgeBaseEntryStatus.PUBLISHED ? 'knowledge-base.bulk.published_message' : 'knowledge-base.bulk.unpublished_message',
              { count: result.succeeded }
            ),
            this.transloco.translate('knowledge-base.entries-board.success_title'),
            { duration: 5000, status: result.failed ? 'warning' : 'success' }
          );
          this.refresh();
        },
        error: () => {
          this.bulkRunning = false;
          this.toastrService.show(
            this.transloco.translate('knowledge-base.bulk.failed_message'),
            this.transloco.translate('knowledge-base.entries-board.error_title'),
            { duration: 6000, status: 'danger' }
          );
        }
      });
  }

  // ---------------------------------------------------------------- Import / export

  openImport(): void {
    this.router.navigateByUrl('/knowledge-base/import');
  }

  /**
   * Single pivot format on purpose. A spreadsheet friendly export would only make sense
   * paired with a matching re-import, which is a separate subject.
   */
  exportEntries(): void {
    this.exporting = true;

    this.knowledgeBaseService
      .exportEntries()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (envelope) => {
          this.exporting = false;
          this.download(envelope);
        },
        error: () => (this.exporting = false)
      });
  }

  private download(envelope: KnowledgeBaseExportEnvelope): void {
    const blob = new Blob([JSON.stringify(envelope, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    const stamp = new Date().toISOString().slice(0, 10);

    link.href = url;
    link.download = `knowledge-base-${envelope.botId || 'bot'}-${stamp}.json`;
    link.click();

    URL.revokeObjectURL(url);
  }

  // ---------------------------------------------------------------- Display helpers

  projectionStatus(entry: KnowledgeBaseEntry): string {
    switch (entry.projectionState) {
      case KnowledgeBaseProjectionState.INDEXED:
        return 'success';
      case KnowledgeBaseProjectionState.PENDING:
        return 'warning';
      case KnowledgeBaseProjectionState.ORPHAN:
        return 'danger';
      default:
        return 'basic';
    }
  }

  projectionIcon(entry: KnowledgeBaseEntry): string {
    switch (entry.projectionState) {
      case KnowledgeBaseProjectionState.INDEXED:
        return 'check-circle';
      case KnowledgeBaseProjectionState.PENDING:
        return 'clock-history';
      case KnowledgeBaseProjectionState.ORPHAN:
        return 'exclamation-octagon';
      default:
        return 'dash-circle';
    }
  }

  trackById(_index: number, entry: KnowledgeBaseEntry): string {
    return entry.id;
  }

  // MOCK ONLY
  changeMockScenario(scenario: KnowledgeBaseMockScenario): void {
    if (!this.mockService) return;

    this.mockService.setScenario(scenario);
    this.resetFilters();
    this.pagination.start = 0;
    this.refresh();
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}

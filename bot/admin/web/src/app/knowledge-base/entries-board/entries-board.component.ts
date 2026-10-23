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
import { Observable, Subject, debounceTime, interval, exhaustMap, takeUntil, takeWhile, filter, catchError, of } from 'rxjs';

import { BotConfigurationService } from '../../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { DialogService } from '../../core-nlp/dialog.service';
import { StateService } from '../../core-nlp/state.service';
import { UserRole } from '../../model/auth';
import { ChoiceDialogComponent } from '../../shared/components';
import { Pagination } from '../../shared/components/pagination/pagination.component';
import { copyToClipboard } from '../../shared/utils';
import {
  KnowledgeBaseEntry,
  KnowledgeBaseEntryStatus,
  KnowledgeBaseIndexState,
  KnowledgeBaseJobState,
  KnowledgeBaseJobType,
  KnowledgeBaseExportEnvelope,
  KnowledgeBaseJob,
  KnowledgeBaseProjectionState,
  KnowledgeBaseSearchQuery,
  KnowledgeBaseSortField,
  KnowledgeBaseSyncStatus,
  SortDirection,
  isJobFinished
} from '../models';
import { KnowledgeBaseMockScenario } from '../services/knowledge-base-mock-data';
import { KnowledgeBaseMockService } from '../services/knowledge-base-mock.service';
import { KnowledgeBaseService } from '../services/knowledge-base.service';
import { KnowledgeBaseCreateIndexDialogComponent } from './create-index-dialog/create-index-dialog.component';

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
  private state = inject(StateService);
  private toastrService = inject(NbToastrService);
  private transloco = inject(TranslocoService);
  private router = inject(Router);

  // MOCK ONLY — resolves to null as soon as the module stops providing the mock,
  // which hides the demo scenario selector without any further change.
  private mockService = inject(KnowledgeBaseMockService, { optional: true });

  destroy$: Subject<unknown> = new Subject();

  private jobChanged$ = new Subject<void>();
  private botKey: string | null = null;

  loading: boolean = true;

  configurations: BotApplicationConfiguration[];

  entries: KnowledgeBaseEntry[] = [];
  tags: string[] = [];
  syncStatus: KnowledgeBaseSyncStatus;

  EntryStatus = KnowledgeBaseEntryStatus;
  ProjectionState = KnowledgeBaseProjectionState;
  IndexState = KnowledgeBaseIndexState;

  /** Import, export and index creation / re-creation are admin only. */
  get isAdmin(): boolean {
    return this.state.hasRole(UserRole.admin);
  }

  /**
   * `switchIndex` chosen in the create dialog for the CREATE_INDEX job in flight, so the completion
   * message can tell the "bot switched onto the new index" case from the "snapshot" case. Reset once
   * consumed; null when the running job was not started from this session (discovery poll).
   */
  private pendingCreateSwitch: boolean | null = null;

  sortField: KnowledgeBaseSortField = 'updatedAt';
  sortDirection: SortDirection = 'desc';

  pagination: Pagination = { start: 0, end: 0, size: 25, total: 0 };

  /** Ids selected for a bulk action. Kept across pages on purpose: a filtered, paged
   *  selection is exactly how a post import publication is done. */
  selection = new Set<string>();
  exporting: boolean = false;

  /** Batch job currently running on this bot, whoever started it. */
  job: KnowledgeBaseJob | null = null;

  get jobRunning(): boolean {
    return !!this.job && !isJobFinished(this.job);
  }

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
      const key = confs.length ? `${confs[0].namespace}/${confs[0].botId}` : null;
      if (key !== this.botKey) {
        this.jobChanged$.next();
        this.job = null;
        this.selection.clear();
        this.botKey = key;
      }
      this.configurations = confs;
      if (confs.length) this.refresh();
    });

    this.filtersForm.valueChanges.pipe(debounceTime(300), takeUntil(this.destroy$)).subscribe(() => {
      this.pagination.start = 0;
      this.fetchEntries();
    });

    this.mockService?.scenario$.pipe(takeUntil(this.destroy$)).subscribe((scenario) => (this.currentMockScenario = scenario));

    interval(2000)
      .pipe(
        filter(() => !!this.configurations?.length && !this.jobRunning),
        exhaustMap(() =>
          this.knowledgeBaseService.getActiveJob().pipe(
            takeUntil(this.jobChanged$),
            catchError(() => of(null))
          )
        ),
        takeUntil(this.destroy$)
      )
      .subscribe((job) => {
        if (job) this.followJob(job);
      });
  }

  private entriesRequest = 0;

  refresh(): void {
    this.fetchSyncStatus();
    this.fetchTags();
    this.fetchEntries();
  }

  fetchEntries(): void {
    this.loading = true;
    const requestId = ++this.entriesRequest;
    const botKey = this.botKey;

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
      .subscribe({
        next: (result) => {
          if (requestId !== this.entriesRequest || botKey !== this.botKey) return;
          this.entries = result.rows;
          this.pagination.total = result.total;
          this.pagination.start = result.start;
          this.pagination.end = result.end;
          this.loading = false;
        },
        error: () => {
          if (requestId === this.entriesRequest) this.loading = false;
        }
      });
  }

  fetchSyncStatus(): void {
    const botKey = this.botKey;
    this.knowledgeBaseService
      .getSyncStatus()
      .pipe(takeUntil(this.destroy$))
      .subscribe((status) => {
        if (botKey === this.botKey) this.syncStatus = status;
      });
  }

  fetchTags(): void {
    const botKey = this.botKey;
    this.knowledgeBaseService
      .getTags()
      .pipe(takeUntil(this.destroy$))
      .subscribe((tags) => {
        if (botKey === this.botKey) this.tags = tags;
      });
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
        title: this.transloco.translate('knowledge-base.entry-detail.delete_dialog_title'),
        subtitle: this.transloco.translate(
          entry.projectionState === KnowledgeBaseProjectionState.INDEXED
            ? 'knowledge-base.entry-detail.delete_dialog_subtitle_indexed'
            : 'knowledge-base.entry-detail.delete_dialog_subtitle_draft',
          { title: entry.title }
        ),
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
        .subscribe((job) => {
          this.followJob(job);
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

  verifyIndex(): void {
    this.runJob(this.knowledgeBaseService.verifyIndex());
  }

  synchronize(): void {
    this.runJob(this.knowledgeBaseService.synchronize());
  }

  createIndex(): void {
    const dialogRef = this.dialogService.openDialog(KnowledgeBaseCreateIndexDialogComponent, {
      context: {
        indexState: this.syncStatus?.indexState ?? KnowledgeBaseIndexState.NONE,
        ragEnabled: !!this.syncStatus?.ragEnabled,
        otherRowCount: this.syncStatus?.otherRowCount ?? null
      }
    });

    dialogRef.onClose.pipe(takeUntil(this.destroy$)).subscribe((switchIndex) => {
      // undefined means the dialog was cancelled; false is a genuine "snapshot" confirmation.
      if (typeof switchIndex !== 'boolean') return;
      this.pendingCreateSwitch = switchIndex;
      this.runJob(this.knowledgeBaseService.createIndex(switchIndex));
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
    // No guard on a running job: everything is queued server side, so actions line up
    // instead of competing.
    if (!this.selectionCount) return;

    this.runJob(this.knowledgeBaseService.bulkUpdateStatus([...this.selection], status));
  }

  // ---------------------------------------------------------------- Job follow up

  /**
   * Polls a job until it finishes, on the same principle as the dataset run follow up.
   * A job that comes back already finished — a single entry handled inline by the server —
   * is simply displayed, never polled.
   */
  private followJob(job: KnowledgeBaseJob): void {
    if (this.jobRunning) return; // The discovery poll picks up the queued successor.
    this.jobChanged$.next();
    this.job = job;

    if (isJobFinished(job)) {
      this.onJobFinished(job);
      return;
    }

    interval(800)
      .pipe(
        exhaustMap(() => this.knowledgeBaseService.getJob(job.id)),
        takeWhile((current) => !isJobFinished(current), true),
        takeUntil(this.jobChanged$),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (current) => {
          this.job = current;
          if (isJobFinished(current)) this.onJobFinished(current);
        },
        error: () => (this.job = null)
      });
  }

  private onJobFinished(job: KnowledgeBaseJob): void {
    this.job = job;
    this.clearSelection();
    this.refresh();

    // Consume the create choice for this job, whatever its outcome, so a later discovery poll never
    // reads a stale value.
    const createSwitch = job.type === KnowledgeBaseJobType.CREATE_INDEX ? this.pendingCreateSwitch : null;
    if (job.type === KnowledgeBaseJobType.CREATE_INDEX) this.pendingCreateSwitch = null;

    if (job.state === KnowledgeBaseJobState.FAILED) {
      this.notifyJobFailed();
    } else if (job.type === KnowledgeBaseJobType.CREATE_INDEX) {
      this.notifyIndexCreated(job, createSwitch);
    } else if (job.type === KnowledgeBaseJobType.VERIFY_INDEX || job.type === KnowledgeBaseJobType.REPAIR_INDEX) {
      this.notifyIndexChecked(job);
    } else {
      this.notifyJobDone(job);
    }

    // The card stays on screen a moment so the outcome can be read, then clears itself.
    setTimeout(
      () => {
        if (this.job?.id === job.id && isJobFinished(this.job)) this.job = null;
      },
      job.failures.length ? 15000 : 4000
    );
  }

  private notifyJobFailed(): void {
    this.toastrService.show(
      this.transloco.translate('knowledge-base.job.failed_message'),
      this.transloco.translate('knowledge-base.entries-board.error_title'),
      { duration: 6000, status: 'danger' }
    );
  }

  private notifyJobDone(job: KnowledgeBaseJob): void {
    this.toastrService.show(
      this.transloco.translate(`knowledge-base.job.done_${job.type.toLowerCase()}`, {
        projected: job.projected,
        removed: job.removed,
        failed: job.failures.length
      }),
      this.transloco.translate('knowledge-base.entries-board.success_title'),
      { duration: 6000, status: job.failures.length ? 'warning' : 'success' }
    );
  }

  /**
   * VERIFY_INDEX and REPAIR_INDEX report against what the index actually holds once the job is over,
   * so the toast is built from the post-job counts rather than from what the job wrote. When every
   * published entry is present and no orphan remains it is a plain success; any gap turns it into a
   * warning that stays longer and points to the next step (repair after a verify, failures after a repair).
   */
  private notifyIndexChecked(job: KnowledgeBaseJob): void {
    const counts = job.syncStatus?.counts;
    if (!counts) {
      // No post-job status to summarize: fall back to the generic completion message.
      this.notifyJobDone(job);
      return;
    }

    if (counts.indexed === counts.published && counts.orphan === 0) {
      this.toastrService.show(
        this.transloco.translate('knowledge-base.job.verify_repair_up_to_date', {
          indexed: counts.indexed,
          published: counts.published
        }),
        this.transloco.translate('knowledge-base.entries-board.success_title'),
        { duration: 6000, status: 'success' }
      );
      return;
    }

    const parts = [
      this.transloco.translate('knowledge-base.job.verify_repair_gap', { indexed: counts.indexed, published: counts.published })
    ];
    if (counts.orphan > 0) {
      parts.push(this.transloco.translate('knowledge-base.job.verify_repair_gap_orphan', { orphan: counts.orphan }));
    }
    parts.push(
      this.transloco.translate(
        job.type === KnowledgeBaseJobType.VERIFY_INDEX ? 'knowledge-base.job.verify_gap_hint' : 'knowledge-base.job.repair_gap_hint'
      )
    );

    this.toastrService.show(parts.join(' '), this.transloco.translate('knowledge-base.entries-board.warning_title'), {
      duration: 12000,
      status: 'warning'
    });
  }

  /**
   * CREATE_INDEX ends on a dialog rather than a toast: when the bot was not switched onto the new
   * index, its session id is the only handle the user has on it, so it is shown with a copy action
   * instead of scrolling away in a toast. `switched` comes from the create dialog choice; a job picked
   * up from the discovery poll has no choice recorded, so the bot's resulting session id is compared
   * to the created one instead.
   */
  private notifyIndexCreated(job: KnowledgeBaseJob, switched: boolean | null): void {
    // Whether the bot actually switched is a server fact, not the dialog checkbox: the worker only switches when the
    // create finished with no failures onto a confirmed collection, and `enqueue` may even return a different,
    // already-active create job than the one this checkbox described. Trust the resulting bot config when the sync
    // status is available; fall back to the requested intent only when it is not, and never claim a switch on failures.
    const botSwitched = job.syncStatus
      ? !!job.indexSessionId && job.syncStatus.indexSessionId === job.indexSessionId
      : switched === true && job.failures.length === 0;

    const subtitle = botSwitched
      ? this.transloco.translate('knowledge-base.job.create_done_switch', { projected: job.projected })
      : job.failures.length > 0
        ? this.transloco.translate('knowledge-base.job.create_done_failures', {
            projected: job.projected,
            failed: job.failures.length,
            indexSessionId: job.indexSessionId
          })
        : this.transloco.translate('knowledge-base.job.create_done_snapshot', {
            projected: job.projected,
            indexSessionId: job.indexSessionId
          });

    const closeAction = {
      actionName: this.transloco.translate('knowledge-base.job.create_done_close_button'),
      buttonStatus: botSwitched ? 'primary' : 'basic',
      ghost: !botSwitched,
      returnValue: 'close'
    };
    const actions = botSwitched
      ? [closeAction]
      : [
          closeAction,
          {
            actionName: this.transloco.translate('knowledge-base.job.create_done_copy_button'),
            buttonStatus: 'primary',
            returnValue: 'copy'
          }
        ];

    const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
      context: {
        title: this.transloco.translate('knowledge-base.job.create_done_title'),
        subtitle,
        actions
      }
    });

    dialogRef.onClose.pipe(takeUntil(this.destroy$)).subscribe((result) => {
      if (result === 'copy' && job.indexSessionId) {
        copyToClipboard(job.indexSessionId);
        this.toastrService.show(
          this.transloco.translate('knowledge-base.job.session_id_copied'),
          this.transloco.translate('knowledge-base.entries-board.success_title'),
          { duration: 3000, status: 'success' }
        );
      }
    });
  }

  private runJob(request: Observable<KnowledgeBaseJob>): void {
    request.pipe(takeUntil(this.destroy$)).subscribe({
      next: (job) => this.followJob(job),
      error: () =>
        this.toastrService.show(
          this.transloco.translate('knowledge-base.job.failed_message'),
          this.transloco.translate('knowledge-base.entries-board.error_title'),
          { duration: 6000, status: 'danger' }
        )
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

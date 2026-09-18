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
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslocoService } from '@jsverse/transloco';
import { NbToastrService } from '@nebular/theme';
import { Subject, interval, switchMap, takeUntil, takeWhile } from 'rxjs';

import { BotConfigurationService } from '../../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { DialogService } from '../../core-nlp/dialog.service';
import { ChoiceDialogComponent } from '../../shared/components';
import { markedParser } from '../../shared/utils/markup.utils';
import {
  KnowledgeBaseEntry,
  KnowledgeBaseEntryPayload,
  KnowledgeBaseEntryStatus,
  KnowledgeBaseIndexMode,
  KnowledgeBaseJob,
  KnowledgeBaseJobState,
  KnowledgeBaseProjectionState,
  KnowledgeBaseSyncStatus,
  isJobFinished
} from '../models';
import { KnowledgeBaseService } from '../services/knowledge-base.service';

@Component({
  selector: 'tock-knowledge-base-entry-detail',
  templateUrl: './entry-detail.component.html',
  styleUrl: './entry-detail.component.scss',
  standalone: false
})
export class KnowledgeBaseEntryDetailComponent implements OnInit, OnDestroy {
  private botConfiguration = inject(BotConfigurationService);
  private knowledgeBaseService = inject(KnowledgeBaseService);
  private dialogService = inject(DialogService);
  private toastrService = inject(NbToastrService);
  private transloco = inject(TranslocoService);
  private sanitizer = inject(DomSanitizer);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  destroy$: Subject<unknown> = new Subject();

  loading: boolean = true;
  saving: boolean = false;

  /** Projection job queued by the last save. May sit behind a running batch. */
  job: KnowledgeBaseJob | null = null;

  configurations: BotApplicationConfiguration[];

  entry: KnowledgeBaseEntry | null = null;
  syncStatus: KnowledgeBaseSyncStatus;

  EntryStatus = KnowledgeBaseEntryStatus;
  ProjectionState = KnowledgeBaseProjectionState;

  answerPreview: boolean = false;
  tagInput: string = '';

  form = new FormGroup({
    // The title carries the primary phrasing of the entry. It plays the role a question would
    // in a FAQ, without imposing the question / answer dichotomy.
    title: new FormControl<string>('', [Validators.required, Validators.maxLength(300)]),
    // One empty row by default, a "+" button adds more. Rows left empty are dropped on save,
    // so hints stay optional without forcing the user to delete the default row.
    searchHints: new FormArray<FormControl<string>>([new FormControl<string>('', { nonNullable: true })]),
    content: new FormControl<string>('', [Validators.required]),
    sourceUrl: new FormControl<string | null>(null, [Validators.pattern(/^https?:\/\/.+/)]),
    tags: new FormControl<string[]>([], { nonNullable: true }),
    status: new FormControl<KnowledgeBaseEntryStatus>(KnowledgeBaseEntryStatus.DRAFT, { nonNullable: true })
  });

  get title() {
    return this.form.controls.title;
  }

  get searchHints(): FormArray<FormControl<string>> {
    return this.form.controls.searchHints;
  }

  get content() {
    return this.form.controls.content;
  }

  get sourceUrl() {
    return this.form.controls.sourceUrl;
  }

  get tags(): string[] {
    return this.form.controls.tags.value;
  }

  get isNew(): boolean {
    return !this.entry;
  }

  get isPublished(): boolean {
    return this.form.controls.status.value === KnowledgeBaseEntryStatus.PUBLISHED;
  }

  /** Title and non empty hints, offered as shortcuts in the retrieval test. */
  get retrievalSuggestions(): string[] {
    return [this.title.value, ...this.searchHints.controls.map((control) => control.value)]
      .map((value) => (value ?? '').trim())
      .filter((value) => value.length > 0);
  }

  ngOnInit(): void {
    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((confs) => {
      this.configurations = confs;
      if (confs.length) this.load();
    });
  }

  load(): void {
    this.knowledgeBaseService
      .getSyncStatus()
      .pipe(takeUntil(this.destroy$))
      .subscribe((status) => (this.syncStatus = status));

    const entryId = this.route.snapshot.paramMap.get('id');

    if (!entryId) {
      this.loading = false;
      return;
    }

    this.knowledgeBaseService
      .getEntry(entryId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (entry) => {
          this.entry = entry;
          this.patchForm(entry);
          this.loading = false;
        },
        error: () => {
          this.loading = false;
          this.toastrService.show(
            this.transloco.translate('knowledge-base.entry-detail.entry_not_found'),
            this.transloco.translate('knowledge-base.entries-board.error_title'),
            { duration: 5000, status: 'danger' }
          );
          this.backToList();
        }
      });
  }

  private patchForm(entry: KnowledgeBaseEntry): void {
    this.form.patchValue({
      title: entry.title,
      content: entry.content,
      sourceUrl: entry.sourceUrl,
      tags: [...entry.tags],
      status: entry.status
    });

    this.searchHints.clear();
    const hints = entry.searchHints.length ? entry.searchHints : [''];
    hints.forEach((hint) => this.searchHints.push(new FormControl<string>(hint, { nonNullable: true })));

    this.form.markAsPristine();
  }

  // ---------------------------------------------------------------- Variants

  addHint(): void {
    this.searchHints.push(new FormControl<string>('', { nonNullable: true }));
    this.form.markAsDirty();
  }

  removeHint(index: number): void {
    this.searchHints.removeAt(index);
    if (!this.searchHints.length) this.addHint();
    this.form.markAsDirty();
  }

  // ---------------------------------------------------------------- Tags

  addTag(): void {
    const value = this.tagInput.trim();
    if (!value || this.tags.includes(value)) {
      this.tagInput = '';
      return;
    }

    this.form.controls.tags.setValue([...this.tags, value]);
    this.form.markAsDirty();
    this.tagInput = '';
  }

  removeTag(tag: string): void {
    this.form.controls.tags.setValue(this.tags.filter((t) => t !== tag));
    this.form.markAsDirty();
  }

  // ---------------------------------------------------------------- Answer preview

  /**
   * Preview uses the studio shared marked parser, which sanitizes its output with
   * DOMPurify. No WYSIWYG here on purpose: the content is embedded and handed to the
   * LLM as context, where markup is noise rather than value.
   */
  get answerPreviewHtml(): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(markedParser.parse(this.content.value ?? '') as string);
  }

  toggleAnswerPreview(): void {
    this.answerPreview = !this.answerPreview;
  }

  // ---------------------------------------------------------------- Save

  /**
   * What saving will actually do, given where the entry comes from and where it is going.
   * Everything is real time, so the user is told before (tooltip) and after (toast).
   */
  get saveTransition(): 'create_draft' | 'create_published' | 'publish' | 'unpublish' | 'update_published' | 'update_draft' {
    const targetPublished = this.isPublished;

    if (this.isNew) return targetPublished ? 'create_published' : 'create_draft';

    const wasPublished = this.entry.status === KnowledgeBaseEntryStatus.PUBLISHED;

    if (targetPublished && !wasPublished) return 'publish';
    if (!targetPublished && wasPublished) return 'unpublish';
    return targetPublished ? 'update_published' : 'update_draft';
  }

  get saveTooltipKey(): string {
    if (this.isPublished && !this.hasIndex) return 'knowledge-base.entry-detail.save_tooltip_no_index';
    return `knowledge-base.entry-detail.save_tooltip_${this.saveTransition}`;
  }

  get hasIndex(): boolean {
    return !!this.syncStatus && this.syncStatus.indexMode !== KnowledgeBaseIndexMode.NONE;
  }

  get canSave(): boolean {
    return this.form.valid && this.form.dirty && !this.saving;
  }

  private buildPayload(): KnowledgeBaseEntryPayload {
    return {
      title: this.title.value.trim(),
      searchHints: this.searchHints.controls.map((control) => control.value.trim()).filter((value) => value.length > 0),
      content: this.content.value,
      sourceUrl: this.sourceUrl.value?.trim() || null,
      tags: this.tags,
      status: this.form.controls.status.value
    };
  }

  save(): void {
    if (!this.form.valid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    const payload = this.buildPayload();
    const transition = this.saveTransition;
    const indexed = this.hasIndex;

    const request = this.entry
      ? this.knowledgeBaseService.updateEntry(this.entry.id, payload)
      : this.knowledgeBaseService.createEntry(payload);

    request.pipe(takeUntil(this.destroy$)).subscribe({
      next: ({ entry, job }) => {
        const created = !this.entry;
        this.entry = entry;
        this.patchForm(entry);

        // The entry is written; its projection is queued and followed like any other job.
        this.followJob(job, transition, indexed);

        // Land on the entry route so a reload, or the retrieval test, keeps working.
        if (created) this.router.navigateByUrl(`/knowledge-base/detail/${entry.id}`);
      },
      error: () => {
        this.saving = false;
        this.toastrService.show(
          this.transloco.translate('knowledge-base.entry-detail.save_failed_message'),
          this.transloco.translate('knowledge-base.entries-board.error_title'),
          { duration: 6000, status: 'danger' }
        );
      }
    });
  }

  /**
   * Polls the projection job until it finishes. Queuing it rather than writing inline is what
   * keeps a save issued during a batch from racing it on the same index; the cost is a second
   * of waiting, the button staying busy meanwhile.
   */
  private followJob(job: KnowledgeBaseJob, transition: string, indexed: boolean): void {
    this.job = job;

    if (isJobFinished(job)) {
      this.onJobFinished(job, transition, indexed);
      return;
    }

    interval(600)
      .pipe(
        switchMap(() => this.knowledgeBaseService.getJob(job.id)),
        takeWhile((current) => !isJobFinished(current), true),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (current) => {
          this.job = current;
          if (isJobFinished(current)) this.onJobFinished(current, transition, indexed);
        },
        error: () => {
          this.job = null;
          this.saving = false;
        }
      });
  }

  private onJobFinished(job: KnowledgeBaseJob, transition: string, indexed: boolean): void {
    this.saving = false;
    this.job = null;

    this.knowledgeBaseService
      .getSyncStatus()
      .pipe(takeUntil(this.destroy$))
      .subscribe((status) => (this.syncStatus = status));

    if (job.state === KnowledgeBaseJobState.FAILED || job.failures.length) {
      this.toastrService.show(
        job.failures.length ? job.failures[0].error : this.transloco.translate('knowledge-base.job.failed_message'),
        this.transloco.translate('knowledge-base.entries-board.error_title'),
        { duration: 8000, status: 'danger' }
      );
      return;
    }

    const messageKey =
      this.isPublished && !indexed ? 'knowledge-base.entry-detail.saved_no_index' : `knowledge-base.entry-detail.saved_${transition}`;

    this.toastrService.show(this.transloco.translate(messageKey), this.transloco.translate('knowledge-base.entries-board.success_title'), {
      duration: 5000,
      status: 'success'
    });
  }

  delete(): void {
    if (!this.entry) return;

    const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
      context: {
        title: this.transloco.translate('knowledge-base.entry-detail.delete_dialog_title'),
        subtitle: this.transloco.translate(
          // Being explicit about the effect on the bot matters more than the technical detail:
          // deleting an indexed entry takes it out of the answers right away.
          this.entry.projectionState === KnowledgeBaseProjectionState.INDEXED
            ? 'knowledge-base.entry-detail.delete_dialog_subtitle_indexed'
            : 'knowledge-base.entry-detail.delete_dialog_subtitle_draft',
          { title: this.entry.title }
        ),
        actions: [
          { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
          { actionName: 'delete', buttonStatus: 'danger' }
        ]
      }
    });

    dialogRef.onClose.pipe(takeUntil(this.destroy$)).subscribe((result) => {
      if (result !== 'delete') return;

      // The removal job is picked up by the list through getActiveJob, so there is nothing
      // to wait for here.
      this.knowledgeBaseService
        .deleteEntry(this.entry.id)
        .pipe(takeUntil(this.destroy$))
        .subscribe(() => this.backToList());
    });
  }

  cancel(): void {
    if (!this.form.dirty) {
      this.backToList();
      return;
    }

    const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
      context: {
        title: this.transloco.translate('knowledge-base.entry-detail.discard_title'),
        subtitle: this.transloco.translate('knowledge-base.entry-detail.discard_subtitle'),
        actions: [
          { actionName: 'stay', buttonStatus: 'basic', ghost: true },
          { actionName: 'discard', buttonStatus: 'danger' }
        ]
      }
    });

    dialogRef.onClose.pipe(takeUntil(this.destroy$)).subscribe((result) => {
      if (result === 'discard') this.backToList();
    });
  }

  backToList(): void {
    this.router.navigateByUrl('/knowledge-base');
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}

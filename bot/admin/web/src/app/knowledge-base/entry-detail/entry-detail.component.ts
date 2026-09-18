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
import { Subject, takeUntil } from 'rxjs';

import { BotConfigurationService } from '../../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { DialogService } from '../../core-nlp/dialog.service';
import { ChoiceDialogComponent } from '../../shared/components';
import { markedParser } from '../../shared/utils/markup.utils';
import {
  KnowledgeBaseEntry,
  KnowledgeBaseEntryPayload,
  KnowledgeBaseEntryStatus,
  KnowledgeBaseProjectionState,
  KnowledgeBaseSyncStatus
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

  configurations: BotApplicationConfiguration[];

  entry: KnowledgeBaseEntry | null = null;
  syncStatus: KnowledgeBaseSyncStatus;

  EntryStatus = KnowledgeBaseEntryStatus;
  ProjectionState = KnowledgeBaseProjectionState;

  answerPreview: boolean = false;
  tagInput: string = '';

  form = new FormGroup({
    title: new FormControl<string>('', [Validators.required, Validators.maxLength(120)]),
    question: new FormControl<string>('', [Validators.required, Validators.maxLength(300)]),
    // One empty row by default, a "+" button adds more. Rows left empty are dropped on save,
    // so variants stay optional without forcing the user to delete the default row.
    questionVariants: new FormArray<FormControl<string>>([new FormControl<string>('', { nonNullable: true })]),
    answer: new FormControl<string>('', [Validators.required]),
    sourceUrl: new FormControl<string | null>(null, [Validators.pattern(/^https?:\/\/.+/)]),
    tags: new FormControl<string[]>([], { nonNullable: true }),
    status: new FormControl<KnowledgeBaseEntryStatus>(KnowledgeBaseEntryStatus.DRAFT, { nonNullable: true })
  });

  get title() {
    return this.form.controls.title;
  }

  get question() {
    return this.form.controls.question;
  }

  get questionVariants(): FormArray<FormControl<string>> {
    return this.form.controls.questionVariants;
  }

  get answer() {
    return this.form.controls.answer;
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

  /** Question and non empty variants, offered as shortcuts in the retrieval test. */
  get retrievalSuggestions(): string[] {
    return [this.question.value, ...this.questionVariants.controls.map((control) => control.value)]
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
      question: entry.question,
      answer: entry.answer,
      sourceUrl: entry.sourceUrl,
      tags: [...entry.tags],
      status: entry.status
    });

    this.questionVariants.clear();
    const variants = entry.questionVariants.length ? entry.questionVariants : [''];
    variants.forEach((variant) => this.questionVariants.push(new FormControl<string>(variant, { nonNullable: true })));

    this.form.markAsPristine();
  }

  // ---------------------------------------------------------------- Title convenience

  /** The title defaults to the question as long as the user has not written one. */
  onQuestionBlur(): void {
    if (!this.title.value?.trim() && this.question.value?.trim()) {
      this.title.setValue(this.question.value.trim().slice(0, 120));
      this.title.markAsDirty();
    }
  }

  // ---------------------------------------------------------------- Variants

  addVariant(): void {
    this.questionVariants.push(new FormControl<string>('', { nonNullable: true }));
    this.form.markAsDirty();
  }

  removeVariant(index: number): void {
    this.questionVariants.removeAt(index);
    if (!this.questionVariants.length) this.addVariant();
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
   * DOMPurify. No WYSIWYG here on purpose: the answer is embedded and handed to the
   * LLM as context, where markup is noise rather than value.
   */
  get answerPreviewHtml(): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(markedParser.parse(this.answer.value ?? '') as string);
  }

  toggleAnswerPreview(): void {
    this.answerPreview = !this.answerPreview;
  }

  // ---------------------------------------------------------------- Save

  get canSave(): boolean {
    return this.form.valid && this.form.dirty && !this.saving;
  }

  private buildPayload(): KnowledgeBaseEntryPayload {
    return {
      title: this.title.value.trim(),
      question: this.question.value.trim(),
      questionVariants: this.questionVariants.controls.map((control) => control.value.trim()).filter((value) => value.length > 0),
      answer: this.answer.value,
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

    const request = this.entry
      ? this.knowledgeBaseService.updateEntry(this.entry.id, payload)
      : this.knowledgeBaseService.createEntry(payload);

    request.pipe(takeUntil(this.destroy$)).subscribe({
      next: (entry) => {
        const created = !this.entry;
        this.entry = entry;
        this.patchForm(entry);
        this.saving = false;

        this.knowledgeBaseService
          .getSyncStatus()
          .pipe(takeUntil(this.destroy$))
          .subscribe((status) => (this.syncStatus = status));

        this.toastrService.show(
          this.transloco.translate(
            payload.status === KnowledgeBaseEntryStatus.PUBLISHED
              ? 'knowledge-base.entry-detail.saved_and_projected_message'
              : 'knowledge-base.entry-detail.saved_draft_message'
          ),
          this.transloco.translate('knowledge-base.entries-board.success_title'),
          { duration: 4000, status: 'success' }
        );

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

  delete(): void {
    if (!this.entry) return;

    const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
      context: {
        title: this.transloco.translate('knowledge-base.entries-board.delete_entry_title'),
        subtitle: this.transloco.translate('knowledge-base.entries-board.delete_entry_subtitle', { title: this.entry.title }),
        actions: [
          { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
          { actionName: 'delete', buttonStatus: 'danger' }
        ]
      }
    });

    dialogRef.onClose.pipe(takeUntil(this.destroy$)).subscribe((result) => {
      if (result !== 'delete') return;

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

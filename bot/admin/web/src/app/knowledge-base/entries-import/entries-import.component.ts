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
import { Router } from '@angular/router';
import { TranslocoService } from '@jsverse/transloco';
import { NbToastrService } from '@nebular/theme';
import { Subject, interval, exhaustMap, takeUntil, takeWhile } from 'rxjs';

import { BotConfigurationService } from '../../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import {
  KnowledgeBaseDuplicatePolicy,
  KnowledgeBaseEntryStatus,
  KnowledgeBaseImportCandidate,
  KnowledgeBaseImportCandidateState,
  KnowledgeBaseImportResult,
  KnowledgeBaseImportSource,
  KnowledgeBaseJob,
  KnowledgeBaseJobState,
  KnowledgeBaseJobType,
  isJobFinished
} from '../models';
import { KnowledgeBaseService } from '../services/knowledge-base.service';
import { KnowledgeBaseImportFormatError, ParsedImportFile, parseImportFile } from '../utils/import.utils';

type ImportStep = 'file' | 'locale' | 'preview' | 'report';

/**
 * Import of a knowledge base export envelope or of a raw FAQ export.
 *
 * Nothing is written before the user has seen the preview, and imported entries are always
 * created as drafts. Publishing them is a separate, explicit bulk action offered in the report.
 */
@Component({
  selector: 'tock-knowledge-base-entries-import',
  templateUrl: './entries-import.component.html',
  styleUrl: './entries-import.component.scss',
  standalone: false
})
export class KnowledgeBaseEntriesImportComponent implements OnInit, OnDestroy {
  private botConfiguration = inject(BotConfigurationService);
  private knowledgeBaseService = inject(KnowledgeBaseService);
  private toastrService = inject(NbToastrService);
  private transloco = inject(TranslocoService);
  private router = inject(Router);

  destroy$: Subject<unknown> = new Subject();

  /** namespace/botId the wizard was opened on, used to leave when the header switches bot. */
  private currentBotKey: string | null = null;

  ImportSource = KnowledgeBaseImportSource;
  CandidateState = KnowledgeBaseImportCandidateState;
  DuplicatePolicy = KnowledgeBaseDuplicatePolicy;

  step: ImportStep = 'file';
  busy: boolean = false;

  fileName: string | null = null;
  rawContent: string | null = null;
  formatError: string | null = null;

  parsed: ParsedImportFile | null = null;
  selectedLocale: string | null = null;

  candidates: KnowledgeBaseImportCandidate[] = [];
  duplicatePolicy: KnowledgeBaseDuplicatePolicy = KnowledgeBaseDuplicatePolicy.SKIP;

  result: KnowledgeBaseImportResult | null = null;
  publishJob: KnowledgeBaseJob | null = null;
  published: boolean = false;

  get publishing(): boolean {
    return !!this.publishJob && !isJobFinished(this.publishJob);
  }

  ngOnInit(): void {
    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((confs) => {
      if (!confs.length) return;

      const botKey = this.botKey(confs);

      // The namespace/bot can be switched at any time from the header. An import in progress belongs
      // to the previous bot, so leave the wizard for the list rather than write into a foreign bot.
      // The configurations subject replays on subscription, so the first emission is the bot we opened
      // on, never a change: it must be recorded, not redirected on.
      if (this.currentBotKey !== null && botKey !== this.currentBotKey) {
        this.currentBotKey = botKey;
        this.backToList();
        return;
      }

      this.currentBotKey = botKey;
    });
  }

  private botKey(confs: BotApplicationConfiguration[]): string | null {
    return confs.length ? `${confs[0].namespace}/${confs[0].botId}` : null;
  }

  // ---------------------------------------------------------------- File

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.reset();
    this.fileName = file.name;

    const reader = new FileReader();
    reader.onload = () => {
      this.rawContent = reader.result as string;
      this.detectFormat();
    };
    reader.onerror = () => (this.formatError = 'knowledge-base.import.error_unreadable_file');
    reader.readAsText(file);
  }

  private detectFormat(): void {
    try {
      this.parsed = parseImportFile(this.rawContent);
      this.formatError = null;

      if (this.parsed.source === KnowledgeBaseImportSource.FAQ) {
        if (!this.parsed.locales.length) {
          this.formatError = 'knowledge-base.import.error_no_locale';
          this.parsed = null;
          return;
        }

        // A single locale needs no question, it is simply stated in the preview.
        this.selectedLocale = this.parsed.locales.length === 1 ? this.parsed.locales[0] : null;
        if (this.selectedLocale) {
          this.buildPreview();
        } else {
          this.step = 'locale';
        }
        return;
      }

      this.buildPreview();
    } catch (error) {
      this.parsed = null;
      this.formatError = error instanceof KnowledgeBaseImportFormatError ? error.reasonKey : 'knowledge-base.import.error_unknown_format';
    }
  }

  selectLocale(locale: string): void {
    this.selectedLocale = locale;
    this.buildPreview();
  }

  // ---------------------------------------------------------------- Preview

  private buildPreview(): void {
    this.busy = true;
    const reparsed = parseImportFile(this.rawContent, this.selectedLocale);
    this.parsed = reparsed;

    this.knowledgeBaseService
      .previewImport(reparsed.rows)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (candidates) => {
          this.candidates = candidates;
          this.busy = false;
          this.step = 'preview';
        },
        error: () => {
          this.busy = false;
        }
      });
  }

  countOf(state: KnowledgeBaseImportCandidateState): number {
    return this.candidates.filter((candidate) => candidate.state === state).length;
  }

  /** Entries that will actually be written, given the duplicate policy. */
  get importableCount(): number {
    const duplicates = this.countOf(KnowledgeBaseImportCandidateState.DUPLICATE);
    const news = this.countOf(KnowledgeBaseImportCandidateState.NEW);

    return this.duplicatePolicy === KnowledgeBaseDuplicatePolicy.SKIP ? news : news + duplicates;
  }

  get hasIssues(): boolean {
    return this.candidates.some((candidate) => candidate.issues.length);
  }

  // ---------------------------------------------------------------- Apply

  runImport(): void {
    this.busy = true;

    this.knowledgeBaseService
      .importEntries(this.candidates, this.duplicatePolicy)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (result) => {
          this.result = result;
          this.busy = false;
          this.step = 'report';
          if (result.job) this.followJob(result.job);
        },
        error: () => {
          this.busy = false;
          this.toastrService.show(
            this.transloco.translate('knowledge-base.import.failed_message'),
            this.transloco.translate('knowledge-base.entries-board.error_title'),
            { duration: 6000, status: 'danger' }
          );
        }
      });
  }

  /** Bulk publication of what was just imported, offered right after the review. */
  publishImported(): void {
    if (!this.result?.entryIds.length || this.publishing) return;

    this.knowledgeBaseService
      .bulkUpdateStatus(this.result.entryIds, KnowledgeBaseEntryStatus.PUBLISHED)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (job) => this.followJob(job),
        error: () => this.publishFailed()
      });
  }

  /** Publishing a hundred imported entries is a batch like any other: it is polled. */
  private followJob(job: KnowledgeBaseJob): void {
    this.publishJob = job;

    if (isJobFinished(job)) {
      this.published = job.type === KnowledgeBaseJobType.PUBLISH && job.state === KnowledgeBaseJobState.COMPLETED && !job.failures.length;
      return;
    }

    interval(800)
      .pipe(
        exhaustMap(() => this.knowledgeBaseService.getJob(job.id)),
        takeWhile((current) => !isJobFinished(current), true),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (current) => {
          this.publishJob = current;
          if (isJobFinished(current))
            this.published =
              current.type === KnowledgeBaseJobType.PUBLISH &&
              current.state === KnowledgeBaseJobState.COMPLETED &&
              !current.failures.length;
        },
        error: () => this.publishFailed()
      });
  }

  private publishFailed(): void {
    this.publishJob = null;
    this.toastrService.show(
      this.transloco.translate('knowledge-base.job.failed_message'),
      this.transloco.translate('knowledge-base.entries-board.error_title'),
      { duration: 6000, status: 'danger' }
    );
  }

  // ---------------------------------------------------------------- Navigation

  reset(): void {
    this.step = 'file';
    this.fileName = null;
    this.rawContent = null;
    this.formatError = null;
    this.parsed = null;
    this.selectedLocale = null;
    this.candidates = [];
    this.result = null;
    this.publishJob = null;
    this.published = false;
  }

  backToList(): void {
    this.router.navigateByUrl('/knowledge-base');
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}

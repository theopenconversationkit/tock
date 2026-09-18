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

import { Component, OnDestroy, inject } from '@angular/core';
import { Router } from '@angular/router';
import { TranslocoService } from '@jsverse/transloco';
import { NbToastrService } from '@nebular/theme';
import { Subject, takeUntil } from 'rxjs';

import {
  KnowledgeBaseDuplicatePolicy,
  KnowledgeBaseEntryStatus,
  KnowledgeBaseImportCandidate,
  KnowledgeBaseImportCandidateState,
  KnowledgeBaseImportResult,
  KnowledgeBaseImportSource
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
export class KnowledgeBaseEntriesImportComponent implements OnDestroy {
  private knowledgeBaseService = inject(KnowledgeBaseService);
  private toastrService = inject(NbToastrService);
  private transloco = inject(TranslocoService);
  private router = inject(Router);

  destroy$: Subject<unknown> = new Subject();

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
  publishing: boolean = false;
  published: boolean = false;

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
      .subscribe((candidates) => {
        this.candidates = candidates;
        this.busy = false;
        this.step = 'preview';
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
    if (!this.result?.entryIds.length) return;

    this.publishing = true;

    this.knowledgeBaseService
      .bulkUpdateStatus(this.result.entryIds, KnowledgeBaseEntryStatus.PUBLISHED)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (bulk) => {
          this.publishing = false;
          this.published = true;
          this.toastrService.show(
            this.transloco.translate('knowledge-base.bulk.published_message', { count: bulk.succeeded }),
            this.transloco.translate('knowledge-base.entries-board.success_title'),
            { duration: 5000, status: bulk.failed ? 'warning' : 'success' }
          );
        },
        error: () => {
          this.publishing = false;
          this.toastrService.show(
            this.transloco.translate('knowledge-base.bulk.failed_message'),
            this.transloco.translate('knowledge-base.entries-board.error_title'),
            { duration: 6000, status: 'danger' }
          );
        }
      });
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

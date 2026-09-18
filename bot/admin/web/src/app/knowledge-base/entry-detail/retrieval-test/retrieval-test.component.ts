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

import { Component, Input, OnDestroy, inject } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';

import {
  KnowledgeBaseIndexMode,
  KnowledgeBaseRetrievalHit,
  KnowledgeBaseRetrievalTest,
  KnowledgeBaseSyncStatus,
  entryChunkId
} from '../../models';
import { KnowledgeBaseService } from '../../services/knowledge-base.service';

/**
 * Answers a single question: would this entry be retrieved for that user question,
 * and at what rank.
 *
 * Runs the vector store search with the entry chunk pinned, so the response carries
 * the entry either with its rank or with the `not_retrieved` outcome. No dedicated
 * endpoint, and no reimplementation of the diagnostic view: for anything finer
 * (varying k, fetchK, search mode, compression, comparing runs) the user is handed
 * over to the diagnostic with the chunk already pinned.
 */
@Component({
  selector: 'tock-knowledge-base-retrieval-test',
  templateUrl: './retrieval-test.component.html',
  styleUrl: './retrieval-test.component.scss',
  standalone: false
})
export class KnowledgeBaseRetrievalTestComponent implements OnDestroy {
  private knowledgeBaseService = inject(KnowledgeBaseService);
  private router = inject(Router);

  destroy$: Subject<unknown> = new Subject();

  /** Null while the entry has not been created yet. */
  @Input() entryId: string | null = null;
  @Input() syncStatus: KnowledgeBaseSyncStatus;
  /** Question and variants currently typed in the form, offered as one click shortcuts. */
  @Input() suggestions: string[] = [];
  /** True when the entry being edited is a draft, hence absent from the index. */
  @Input() draft: boolean = false;
  /** True when the form holds unsaved changes. */
  @Input() dirty: boolean = false;

  questionControl = new FormControl<string>('');

  running: boolean = false;
  result: KnowledgeBaseRetrievalTest | null = null;

  get hasIndex(): boolean {
    return !!this.syncStatus && this.syncStatus.indexMode !== KnowledgeBaseIndexMode.NONE;
  }

  /** The test needs a projected entry: no index, or an unsaved one, and it cannot run. */
  get available(): boolean {
    return this.hasIndex && !!this.entryId;
  }

  get unavailableReasonKey(): string {
    if (!this.hasIndex) return 'knowledge-base.retrieval-test.unavailable_no_index';
    return 'knowledge-base.retrieval-test.unavailable_not_saved';
  }

  get canRun(): boolean {
    return this.available && !this.running && !!this.questionControl.value?.trim();
  }

  useSuggestion(suggestion: string): void {
    this.questionControl.setValue(suggestion);
  }

  run(): void {
    if (!this.canRun) return;

    this.running = true;
    this.result = null;

    this.knowledgeBaseService
      .searchAndLocateEntry({ question: this.questionControl.value.trim(), entryId: this.entryId })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (result) => {
          this.result = result;
          this.running = false;
        },
        error: () => (this.running = false)
      });
  }

  isTestedEntry(hit: KnowledgeBaseRetrievalHit): boolean {
    return !!this.entryId && hit.kbEntryId === this.entryId;
  }

  /** Hands over to the inspection diagnostic, chunk id passed as a route parameter. */
  openInDiagnostic(): void {
    if (!this.entryId) return;

    this.router.navigate(['/vector-store-inspection/diagnostic'], {
      queryParams: {
        chunkId: entryChunkId(this.entryId),
        question: this.questionControl.value?.trim() || null
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}

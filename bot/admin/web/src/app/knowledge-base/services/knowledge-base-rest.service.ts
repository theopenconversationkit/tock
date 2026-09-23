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

import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RestService } from '../../core-nlp/rest/rest.service';
import { StateService } from '../../core-nlp/state.service';
import { PaginatedResult } from '../../model/nlp';
import { KnowledgeBaseService } from './knowledge-base.service';
import { ParsedImportRow } from '../utils/import.utils';
import * as KB from '../models';

@Injectable()
export class KnowledgeBaseRestService extends KnowledgeBaseService {
  private readonly rest = inject(RestService);
  private readonly state = inject(StateService);
  private url(path: string): string {
    return `/bots/${encodeURIComponent(this.state.currentApplication.name)}/knowledge-base${path}`;
  }
  searchEntries(query: KB.KnowledgeBaseSearchQuery): Observable<PaginatedResult<KB.KnowledgeBaseEntry>> {
    const params = new URLSearchParams();
    Object.entries(query).forEach(([key, value]) => {
      if (value !== null && value !== undefined && value !== '') params.set(key, String(value));
    });
    return this.rest.get(this.url(`/entries?${params}`), (value) => value);
  }
  getEntry(id: string): Observable<KB.KnowledgeBaseEntry> {
    return this.rest.get(this.url(`/entries/${encodeURIComponent(id)}`), (value) => value);
  }
  createEntry(payload: KB.KnowledgeBaseEntryPayload): Observable<KB.KnowledgeBaseEntrySaveResult> {
    return this.rest.post(this.url('/entries'), payload);
  }
  updateEntry(id: string, payload: KB.KnowledgeBaseEntryPayload): Observable<KB.KnowledgeBaseEntrySaveResult> {
    return this.rest.put(this.url(`/entries/${encodeURIComponent(id)}`), payload);
  }
  deleteEntry(id: string): Observable<KB.KnowledgeBaseJob> {
    return this.rest.post(this.url(`/entries/${encodeURIComponent(id)}/delete`), {});
  }
  getTags(): Observable<string[]> {
    return this.rest.getArray(this.url('/tags'), (value) => value);
  }
  getSyncStatus(): Observable<KB.KnowledgeBaseSyncStatus> {
    return this.rest.get(this.url('/sync'), (value) => value);
  }
  synchronize(): Observable<KB.KnowledgeBaseJob> {
    return this.rest.post(this.url('/sync'), {});
  }
  verifyIndex(): Observable<KB.KnowledgeBaseJob> {
    return this.rest.post(this.url('/verify'), {});
  }
  createIndex(): Observable<KB.KnowledgeBaseJob> {
    return this.rest.post(this.url('/index'), {});
  }
  bulkUpdateStatus(entryIds: string[], status: KB.KnowledgeBaseEntryStatus): Observable<KB.KnowledgeBaseJob> {
    return this.rest.post(this.url('/bulk-status'), { entryIds, status });
  }
  getJob(id: string): Observable<KB.KnowledgeBaseJob> {
    return this.rest.get(this.url(`/jobs/${encodeURIComponent(id)}`), (value) => value);
  }
  getActiveJob(): Observable<KB.KnowledgeBaseJob | null> {
    return this.rest.get(this.url('/jobs/active'), (value) => (value?.id ? value : null));
  }
  searchAndLocateEntry(payload: { question: string; entryId?: string | null }): Observable<KB.KnowledgeBaseRetrievalTest> {
    return this.rest.post(this.url('/retrieval-test'), payload);
  }
  previewImport(rows: ParsedImportRow[]): Observable<KB.KnowledgeBaseImportCandidate[]> {
    return this.rest.post(this.url('/import/preview'), { rows });
  }
  importEntries(
    candidates: KB.KnowledgeBaseImportCandidate[],
    duplicatePolicy: KB.KnowledgeBaseDuplicatePolicy
  ): Observable<KB.KnowledgeBaseImportResult> {
    return this.rest.post(this.url('/import'), { candidates, duplicatePolicy });
  }
  exportEntries(): Observable<KB.KnowledgeBaseExportEnvelope> {
    return this.rest.get(this.url('/export'), (value) => value);
  }
}

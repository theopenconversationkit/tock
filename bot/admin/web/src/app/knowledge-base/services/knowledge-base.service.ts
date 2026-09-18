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

import { Observable } from 'rxjs';

import { PaginatedResult } from '../../model/nlp';
import {
  KnowledgeBaseBulkResult,
  KnowledgeBaseDuplicatePolicy,
  KnowledgeBaseEntry,
  KnowledgeBaseEntryPayload,
  KnowledgeBaseEntryStatus,
  KnowledgeBaseExportEnvelope,
  KnowledgeBaseImportCandidate,
  KnowledgeBaseImportResult,
  KnowledgeBaseRetrievalTest,
  KnowledgeBaseSearchQuery,
  KnowledgeBaseSyncResult,
  KnowledgeBaseSyncStatus
} from '../models';
import { ParsedImportRow } from '../utils/import.utils';

/**
 * API surface of the knowledge base.
 *
 * Declared as an abstract class rather than an interface so it can be used as an
 * Angular injection token. Two implementations are expected:
 *  - KnowledgeBaseMockService, used while the backend endpoints are being
 *    specified and reviewed;
 *  - a REST implementation calling /gen-ai/bots/:botId/knowledge-base/*, once
 *    those routes land.
 *
 * Switching between them is a single provider change in the module. No component
 * should ever reference the mock directly.
 *
 * Intended REST contract:
 *   GET    /bots/:botId/knowledge-base/entries           searchEntries
 *   GET    /bots/:botId/knowledge-base/entries/:entryId  getEntry
 *   POST   /bots/:botId/knowledge-base/entries           createEntry
 *   PUT    /bots/:botId/knowledge-base/entries/:entryId  updateEntry
 *   DELETE /bots/:botId/knowledge-base/entries/:entryId  deleteEntry
 *   GET    /bots/:botId/knowledge-base/tags              getTags
 *   GET    /bots/:botId/knowledge-base/sync              getSyncStatus
 *   POST   /bots/:botId/knowledge-base/sync              synchronize
 *   POST   /bots/:botId/knowledge-base/index             createIndex
 *   POST   /bots/:botId/knowledge-base/bulk-status       bulkUpdateStatus
 *   POST   /bots/:botId/knowledge-base/import/preview    previewImport
 *   POST   /bots/:botId/knowledge-base/import            importEntries
 *   GET    /bots/:botId/knowledge-base/export            exportEntries
 *
 * The namespace is resolved server side from the session, as for the datasets API.
 */
export abstract class KnowledgeBaseService {
  /** Paginated, filtered and sorted list of entries for the current bot. */
  abstract searchEntries(query: KnowledgeBaseSearchQuery): Observable<PaginatedResult<KnowledgeBaseEntry>>;

  abstract getEntry(entryId: string): Observable<KnowledgeBaseEntry>;

  /**
   * Creates an entry. A published entry is projected synchronously: the call
   * returns only once the row has been embedded and written to the current index,
   * so that an editorial fix is immediately effective.
   */
  abstract createEntry(payload: KnowledgeBaseEntryPayload): Observable<KnowledgeBaseEntry>;

  /** Same synchronous projection contract as createEntry. */
  abstract updateEntry(entryId: string, payload: KnowledgeBaseEntryPayload): Observable<KnowledgeBaseEntry>;

  /** Removes the entry and its rows from the current index. */
  abstract deleteEntry(entryId: string): Observable<boolean>;

  /** Distinct tags in use, for the list filter and the editor autocomplete. */
  abstract getTags(): Observable<string[]>;

  /**
   * Origin and state of the index session currently configured on the bot, with
   * the counts the synchronization banner is built from.
   */
  abstract getSyncStatus(): Observable<KnowledgeBaseSyncStatus>;

  /** Reprojects every published entry and removes orphan rows from the current index. */
  abstract synchronize(): Observable<KnowledgeBaseSyncResult>;

  /**
   * Standalone mode: creates an index session from the knowledge base alone,
   * writes the rows and returns the session so the RAG configuration can be updated.
   */
  abstract createIndex(): Observable<KnowledgeBaseSyncResult>;

  /**
   * Answers "would this entry be retrieved for that question, and at what rank".
   *
   * No dedicated endpoint: the REST implementation calls the existing vector store
   * inspection search with the entry chunk id in `pinnedChunkIds`. Pinned chunks are
   * guaranteed to appear in the response, carrying `not_retrieved` when no channel
   * returned them, which is precisely the case of interest here.
   *
   * That search service is expected to be lifted from the vector-store-inspection
   * module into shared/services so both features depend on the abstraction rather
   * than on each other.
   */
  abstract searchAndLocateEntry(payload: { question: string; entryId?: string | null }): Observable<KnowledgeBaseRetrievalTest>;

  // ------------------------------------------------------------------ Bulk actions

  /**
   * Publishes or unpublishes several entries at once.
   *
   * Publishing a hundred entries means a hundred projections: the implementation issues a
   * single grouped call rather than a loop of per entry requests, and a partial failure leaves
   * the affected entries pending instead of failing the whole operation. Same batched
   * projection capability as index repair and standalone index creation.
   */
  abstract bulkUpdateStatus(entryIds: string[], status: KnowledgeBaseEntryStatus): Observable<KnowledgeBaseBulkResult>;

  // ------------------------------------------------------------------ Import / export

  /**
   * Confronts parsed rows with the existing entries to flag duplicates, without writing
   * anything. Reading and mapping the file itself is done client side by
   * `utils/import.utils.ts`, which knows nothing of the store.
   */
  abstract previewImport(rows: ParsedImportRow[]): Observable<KnowledgeBaseImportCandidate[]>;

  /** Applies a previewed import. Entries are always created as drafts. */
  abstract importEntries(
    candidates: KnowledgeBaseImportCandidate[],
    duplicatePolicy: KnowledgeBaseDuplicatePolicy
  ): Observable<KnowledgeBaseImportResult>;

  /** Versioned envelope holding every entry, free of anything environment specific. */
  abstract exportEntries(): Observable<KnowledgeBaseExportEnvelope>;
}

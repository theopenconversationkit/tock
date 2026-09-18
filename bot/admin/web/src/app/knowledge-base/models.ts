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

export enum KnowledgeBaseEntryStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED'
}

/**
 * State of an entry relative to the index session currently configured on the bot.
 * Computed server side, never sent by the front.
 */
export enum KnowledgeBaseProjectionState {
  /** Published and present in the current index, content up to date */
  INDEXED = 'INDEXED',
  /** Published but absent from the current index, or present with a stale content */
  PENDING = 'PENDING',
  /** Still present in the current index while no longer published */
  ORPHAN = 'ORPHAN',
  /** Draft, not meant to be projected */
  NONE = 'NONE'
}

/**
 * Origin of the index session currently configured on the bot.
 * TOCK_MANAGED: the index session was created by Tock from the knowledge base (standalone mode).
 * EXTERNAL: the index session was produced by a third party ingestion pipeline (mixed mode).
 * NONE: no index session configured on the bot yet.
 */
export enum KnowledgeBaseIndexMode {
  NONE = 'NONE',
  TOCK_MANAGED = 'TOCK_MANAGED',
  EXTERNAL = 'EXTERNAL'
}

export interface KnowledgeBaseEntry {
  id: string;
  namespace: string;
  /** List and not a scalar so that cross bot sharing stays possible later. Always a single element for now. */
  botIds: string[];
  title: string;
  question: string;
  questionVariants: string[];
  answer: string;
  sourceUrl: string | null;
  tags: string[];
  status: KnowledgeBaseEntryStatus;
  /** Hash of the projected content, used to detect a stale projection */
  contentHash: string;
  projectionState: KnowledgeBaseProjectionState;
  projectedAt: string | null; // ISO 8601
  projectedIndexSessionId: string | null;
  createdAt: string; // ISO 8601
  createdBy: string;
  updatedAt: string | null; // ISO 8601
  updatedBy: string | null;
}

export type KnowledgeBaseEntryPayload = Pick<
  KnowledgeBaseEntry,
  'title' | 'question' | 'questionVariants' | 'answer' | 'sourceUrl' | 'tags' | 'status'
>;

export type KnowledgeBaseSortField = 'title' | 'updatedAt' | 'status';
export type SortDirection = 'asc' | 'desc';

export interface KnowledgeBaseSearchQuery {
  search?: string | null;
  status?: KnowledgeBaseEntryStatus | null;
  projectionState?: KnowledgeBaseProjectionState | null;
  tag?: string | null;
  sort?: KnowledgeBaseSortField;
  direction?: SortDirection;
  start: number;
  size: number;
}

export interface KnowledgeBaseCounts {
  total: number;
  draft: number;
  published: number;
  indexed: number;
  pending: number;
  orphan: number;
}

export interface KnowledgeBaseSyncStatus {
  indexMode: KnowledgeBaseIndexMode;
  indexSessionId: string | null;
  indexName: string | null;
  /**
   * False when the index was not produced by Tock and the embedding model used to build it
   * cannot be determined. Writing into such an index may silently produce unusable vectors
   * when the dimension happens to match.
   */
  embeddingModelKnown: boolean;
  embeddingModel: string | null;
  lastProjectionAt: string | null; // ISO 8601
  counts: KnowledgeBaseCounts;
}

export interface KnowledgeBaseSyncResult {
  status: KnowledgeBaseSyncStatus;
  projected: number;
  removed: number;
  failed: number;
}

export type KnowledgeBaseSourceType = 'internal_kb' | 'document';

export interface KnowledgeBaseRetrievalHit {
  rank: number;
  score: number;
  title: string;
  source: string | null;
  sourceType: KnowledgeBaseSourceType;
  /** Set when sourceType is internal_kb */
  kbEntryId: string | null;
  content: string;
}

export interface KnowledgeBaseRetrievalTest {
  question: string;
  indexSessionId: string;
  k: number;
  /** Rank of the tested entry in the results, null when it did not make the top k */
  entryRank: number | null;
  hits: KnowledgeBaseRetrievalHit[];
}

/**
 * Chunk identifier of a knowledge base entry, as `get_chunk_identifier()` builds it
 * server side: `{metadata.id}:{metadata.chunk}`.
 *
 * A knowledge base row carries the entry id as `metadata.id` and a single chunk,
 * so the identifier is derivable with no round trip. It is what gets passed as
 * `pinnedChunkIds` to the vector store search, and as a route parameter when
 * opening the entry in the inspection diagnostic.
 */
export function entryChunkId(entryId: string): string {
  return `${entryId}:1/1`;
}

// --------------------------------------------------------------------------
// Import / export
// --------------------------------------------------------------------------

export const KNOWLEDGE_BASE_EXPORT_FORMAT = 'tock-knowledge-base';
export const KNOWLEDGE_BASE_EXPORT_VERSION = 1;

/** Entry as carried by an export file: nothing environment specific. */
export interface KnowledgeBaseExportedEntry extends KnowledgeBaseEntryPayload {
  /** Id in the source environment. Informative, used as a secondary matching key. */
  sourceId: string | null;
}

export interface KnowledgeBaseExportEnvelope {
  format: typeof KNOWLEDGE_BASE_EXPORT_FORMAT;
  version: number;
  exportedAt: string;
  namespace: string;
  botId: string;
  entries: KnowledgeBaseExportedEntry[];
}

/** Which of the two accepted shapes a dropped file turned out to be. */
export enum KnowledgeBaseImportSource {
  /** Knowledge base export envelope */
  KNOWLEDGE_BASE = 'KNOWLEDGE_BASE',
  /** Raw FAQ export: a bare array of FaqDefinition */
  FAQ = 'FAQ'
}

export enum KnowledgeBaseDuplicatePolicy {
  /** Keep the existing entry untouched */
  SKIP = 'SKIP',
  /** Overwrite the existing entry with the imported content */
  UPDATE = 'UPDATE',
  /** Create a second entry regardless */
  CREATE = 'CREATE'
}

export enum KnowledgeBaseImportCandidateState {
  NEW = 'NEW',
  DUPLICATE = 'DUPLICATE',
  /** Unusable, will not be imported whatever the policy */
  REJECTED = 'REJECTED'
}

export interface KnowledgeBaseImportCandidate {
  payload: KnowledgeBaseEntryPayload;
  sourceId: string | null;
  state: KnowledgeBaseImportCandidateState;
  /** Set when state is DUPLICATE */
  existingEntryId: string | null;
  /** i18n keys describing what was dropped or why the row was rejected */
  issues: string[];
  /** FAQ only, shown for triage: disabled FAQs were the ones diverted to feed the corpus */
  faqEnabled: boolean | null;
}

/** Result of reading a file, before anything is written. */
export interface KnowledgeBaseImportPreview {
  source: KnowledgeBaseImportSource;
  /** Locales found in the file. FAQ exports may carry several; KB exports carry none. */
  locales: string[];
  selectedLocale: string | null;
  candidates: KnowledgeBaseImportCandidate[];
}

export interface KnowledgeBaseImportResult {
  created: number;
  updated: number;
  skipped: number;
  failed: number;
  /** Ids of the entries created or updated, so they can be published in bulk right away */
  entryIds: string[];
}

export interface KnowledgeBaseBulkOutcome {
  entryId: string;
  ok: boolean;
  /** i18n key when ok is false */
  error: string | null;
}

export interface KnowledgeBaseBulkResult {
  succeeded: number;
  failed: number;
  outcomes: KnowledgeBaseBulkOutcome[];
}

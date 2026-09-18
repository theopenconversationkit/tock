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

import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { delay, map, tap } from 'rxjs/operators';

import { StateService } from '../../core-nlp/state.service';
import { PaginatedResult } from '../../model/nlp';
import { deepCopy } from '../../shared/utils';
import { KnowledgeBaseService } from './knowledge-base.service';
import { ParsedImportRow, normalizeQuestion } from '../utils/import.utils';
import {
  KNOWLEDGE_BASE_EXPORT_FORMAT,
  KNOWLEDGE_BASE_EXPORT_VERSION,
  KnowledgeBaseBulkOutcome,
  KnowledgeBaseBulkResult,
  KnowledgeBaseCounts,
  KnowledgeBaseDuplicatePolicy,
  KnowledgeBaseExportEnvelope,
  KnowledgeBaseImportCandidate,
  KnowledgeBaseImportCandidateState,
  KnowledgeBaseImportResult,
  KnowledgeBaseEntry,
  KnowledgeBaseEntryPayload,
  KnowledgeBaseEntryStatus,
  KnowledgeBaseIndexMode,
  KnowledgeBaseProjectionState,
  KnowledgeBaseRetrievalHit,
  KnowledgeBaseRetrievalTest,
  KnowledgeBaseSearchQuery,
  KnowledgeBaseSyncResult,
  KnowledgeBaseSyncStatus
} from '../models';
import {
  buildMockEntries,
  KnowledgeBaseMockScenario,
  MOCK_DOCUMENT_HITS,
  MOCK_EMBEDDING_MODEL,
  MOCK_EXTERNAL_INDEX_NAME,
  MOCK_EXTERNAL_INDEX_SESSION_ID,
  MOCK_INDEX_NAME,
  MOCK_INDEX_SESSION_ID
} from './knowledge-base-mock-data';

/**
 * MOCK implementation of KnowledgeBaseService.
 *
 * Feeds the functional prototype from an in-memory seed so the whole feature can be
 * demonstrated and discussed before any backend exists. Swapped for the REST
 * implementation by a single provider line in KnowledgeBaseModule.
 *
 * Everything specific to the mock (demo scenarios, latency, naive scoring) is marked
 * MOCK ONLY and disappears with this file.
 */
@Injectable()
export class KnowledgeBaseMockService extends KnowledgeBaseService {
  private stateService = inject(StateService);

  // --------------------------------------------------------------------- Mock state

  private latency: number = 350;

  private scenarioSubject = new BehaviorSubject<KnowledgeBaseMockScenario>(KnowledgeBaseMockScenario.OUT_OF_SYNC);
  scenario$ = this.scenarioSubject.asObservable();

  private entriesSubject = new BehaviorSubject<KnowledgeBaseEntry[]>([]);
  entries$ = this.entriesSubject.asObservable();

  private syncStatusSubject = new BehaviorSubject<KnowledgeBaseSyncStatus | null>(null);
  syncStatus$ = this.syncStatusSubject.asObservable();

  private get entries(): KnowledgeBaseEntry[] {
    return this.entriesSubject.getValue();
  }

  private set entries(value: KnowledgeBaseEntry[]) {
    this.entriesSubject.next(value);
    this.refreshSyncStatus();
  }

  constructor() {
    super();
    this.applyScenario(this.scenarioSubject.getValue());
  }

  // --------------------------------------------------------------------- Entries

  searchEntries(query: KnowledgeBaseSearchQuery): Observable<PaginatedResult<KnowledgeBaseEntry>> {
    const filtered = this.applyFilters(this.entries, query);
    const sorted = this.applySort(filtered, query);
    const start = Math.min(query.start, Math.max(0, sorted.length - 1));
    const rows = sorted.slice(start, start + query.size);

    return of({
      rows: deepCopy(rows),
      total: sorted.length,
      start,
      end: start + rows.length
    } as PaginatedResult<KnowledgeBaseEntry>).pipe(delay(this.latency));
  }

  getEntry(entryId: string): Observable<KnowledgeBaseEntry> {
    const entry = this.entries.find((e) => e.id === entryId);
    if (!entry) return throwError(() => new Error('ENTRY_NOT_FOUND')).pipe(delay(this.latency));

    return of(deepCopy(entry)).pipe(delay(this.latency));
  }

  createEntry(payload: KnowledgeBaseEntryPayload): Observable<KnowledgeBaseEntry> {
    const created: KnowledgeBaseEntry = {
      id: `kb-${Date.now()}`,
      namespace: this.stateService.currentApplication.namespace,
      botIds: [this.stateService.currentApplication.name],
      ...payload,
      contentHash: this.hash(payload),
      projectionState:
        payload.status === KnowledgeBaseEntryStatus.PUBLISHED && this.hasIndex()
          ? KnowledgeBaseProjectionState.INDEXED
          : KnowledgeBaseProjectionState.NONE,
      projectedAt: payload.status === KnowledgeBaseEntryStatus.PUBLISHED && this.hasIndex() ? new Date().toISOString() : null,
      projectedIndexSessionId:
        payload.status === KnowledgeBaseEntryStatus.PUBLISHED && this.hasIndex() ? this.currentIndexSessionId() : null,
      createdAt: new Date().toISOString(),
      createdBy: this.currentUser(),
      updatedAt: null,
      updatedBy: null
    };

    return of(created).pipe(
      delay(this.latency),
      tap(() => (this.entries = [created, ...this.entries])),
      map((entry) => deepCopy(entry))
    );
  }

  updateEntry(entryId: string, payload: KnowledgeBaseEntryPayload): Observable<KnowledgeBaseEntry> {
    const index = this.entries.findIndex((e) => e.id === entryId);
    if (index === -1) return throwError(() => new Error('ENTRY_NOT_FOUND')).pipe(delay(this.latency));

    const previous = this.entries[index];
    const contentHash = this.hash(payload);
    const contentChanged = contentHash !== previous.contentHash;

    const updated: KnowledgeBaseEntry = {
      ...previous,
      ...payload,
      contentHash,
      projectionState: this.nextProjectionState(previous, payload.status, contentChanged),
      updatedAt: new Date().toISOString(),
      updatedBy: this.currentUser()
    };

    // Projection is synchronous on save: a published entry is embedded and upserted right away,
    // an unpublished one has its rows removed right away.
    if (updated.projectionState === KnowledgeBaseProjectionState.INDEXED) {
      updated.projectedAt = new Date().toISOString();
      updated.projectedIndexSessionId = this.currentIndexSessionId();
    } else {
      updated.projectedAt = null;
      updated.projectedIndexSessionId = null;
    }

    return of(updated).pipe(
      delay(this.latency),
      tap(() => (this.entries = [...this.entries.slice(0, index), updated, ...this.entries.slice(index + 1)])),
      map((entry) => deepCopy(entry))
    );
  }

  deleteEntry(entryId: string): Observable<boolean> {
    return of(true).pipe(
      delay(this.latency),
      tap(() => (this.entries = this.entries.filter((e) => e.id !== entryId)))
    );
  }

  getTags(): Observable<string[]> {
    const tags = [...new Set(this.entries.flatMap((e) => e.tags))].sort((a, b) => a.localeCompare(b));
    return of(tags).pipe(delay(this.latency));
  }

  // --------------------------------------------------------------------- Index and synchronization

  getSyncStatus(): Observable<KnowledgeBaseSyncStatus> {
    this.refreshSyncStatus();
    return of(deepCopy(this.syncStatusSubject.getValue())).pipe(delay(this.latency));
  }

  /** Reprojects every published entry and removes the orphan rows from the current index. */
  synchronize(): Observable<KnowledgeBaseSyncResult> {
    const projected = this.entries.filter(
      (e) => e.status === KnowledgeBaseEntryStatus.PUBLISHED && e.projectionState === KnowledgeBaseProjectionState.PENDING
    ).length;
    const removed = this.entries.filter((e) => e.projectionState === KnowledgeBaseProjectionState.ORPHAN).length;
    const now = new Date().toISOString();
    const sessionId = this.currentIndexSessionId();

    const synchronized = this.entries.map((entry) => {
      if (entry.status === KnowledgeBaseEntryStatus.PUBLISHED) {
        return { ...entry, projectionState: KnowledgeBaseProjectionState.INDEXED, projectedAt: now, projectedIndexSessionId: sessionId };
      }
      return { ...entry, projectionState: KnowledgeBaseProjectionState.NONE, projectedAt: null, projectedIndexSessionId: null };
    });

    return of(null).pipe(
      delay(this.latency * 4),
      tap(() => {
        this.entries = synchronized;
        this.lastProjectionAt = now;
        this.refreshSyncStatus();
      }),
      map(() => ({
        status: deepCopy(this.syncStatusSubject.getValue()),
        projected,
        removed,
        failed: 0
      }))
    );
  }

  /**
   * Standalone mode: Tock creates the index session itself from the knowledge base,
   * writes the rows and returns the resulting session so that the RAG configuration can be updated.
   */
  createIndex(): Observable<KnowledgeBaseSyncResult> {
    return of(null).pipe(
      delay(this.latency * 6),
      map(() => {
        this.indexMode = KnowledgeBaseIndexMode.TOCK_MANAGED;
        this.indexSessionId = MOCK_INDEX_SESSION_ID;
        this.indexName = MOCK_INDEX_NAME;
        this.embeddingModelKnown = true;

        const projected = this.entries.filter((e) => e.status === KnowledgeBaseEntryStatus.PUBLISHED).length;
        const now = new Date().toISOString();
        this.entries = this.entries.map((entry) =>
          entry.status === KnowledgeBaseEntryStatus.PUBLISHED
            ? {
                ...entry,
                projectionState: KnowledgeBaseProjectionState.INDEXED,
                projectedAt: now,
                projectedIndexSessionId: MOCK_INDEX_SESSION_ID
              }
            : { ...entry, projectionState: KnowledgeBaseProjectionState.NONE, projectedAt: null, projectedIndexSessionId: null }
        );
        this.lastProjectionAt = now;
        this.refreshSyncStatus();
        return { status: deepCopy(this.syncStatusSubject.getValue()), projected, removed: 0, failed: 0 };
      })
    );
  }

  // --------------------------------------------------------------------- Retrieval test (see the abstract class)

  searchAndLocateEntry(payload: { question: string; entryId?: string | null }): Observable<KnowledgeBaseRetrievalTest> {
    const k = 10;
    const question = payload.question.trim().toLowerCase();

    const scored = this.entries
      .filter((e) => e.status === KnowledgeBaseEntryStatus.PUBLISHED)
      .map((entry) => ({ entry, score: this.naiveScore(question, entry) }))
      .filter((candidate) => candidate.score > 0)
      .sort((a, b) => b.score - a.score)
      .slice(0, 6);

    const kbHits: KnowledgeBaseRetrievalHit[] = scored.map((candidate) => ({
      rank: 0,
      score: candidate.score,
      title: candidate.entry.title,
      source: candidate.entry.sourceUrl,
      sourceType: 'internal_kb',
      kbEntryId: candidate.entry.id,
      content: `${candidate.entry.title}\n${candidate.entry.question}\n${candidate.entry.answer}`
    }));

    const documentHits: KnowledgeBaseRetrievalHit[] =
      this.indexMode === KnowledgeBaseIndexMode.EXTERNAL
        ? MOCK_DOCUMENT_HITS.map((doc, i) => ({
            rank: 0,
            score: 0.62 - i * 0.04,
            title: doc.title,
            source: doc.source,
            sourceType: 'document' as const,
            kbEntryId: null,
            content: doc.content
          }))
        : [];

    const hits = [...kbHits, ...documentHits]
      .sort((a, b) => b.score - a.score)
      .slice(0, k)
      .map((hit, i) => ({ ...hit, rank: i + 1 }));

    const tested = payload.entryId ? hits.find((hit) => hit.kbEntryId === payload.entryId) : undefined;

    return of({
      question: payload.question,
      indexSessionId: this.currentIndexSessionId(),
      k,
      entryRank: tested ? tested.rank : null,
      hits
    }).pipe(delay(this.latency * 3));
  }

  // --------------------------------------------------------------------- Bulk actions

  bulkUpdateStatus(entryIds: string[], status: KnowledgeBaseEntryStatus): Observable<KnowledgeBaseBulkResult> {
    const targeted = new Set(entryIds);
    const now = new Date().toISOString();
    const sessionId = this.currentIndexSessionId();

    const updated = this.entries.map((entry) => {
      if (!targeted.has(entry.id)) return entry;

      const projected = status === KnowledgeBaseEntryStatus.PUBLISHED && this.hasIndex();

      return {
        ...entry,
        status,
        projectionState: projected ? KnowledgeBaseProjectionState.INDEXED : KnowledgeBaseProjectionState.NONE,
        projectedAt: projected ? now : null,
        projectedIndexSessionId: projected ? sessionId : null,
        updatedAt: now,
        updatedBy: this.currentUser()
      };
    });

    const outcomes: KnowledgeBaseBulkOutcome[] = entryIds.map((entryId) => ({ entryId, ok: true, error: null }));

    // Latency grows with the batch size: a grouped projection is still work.
    return of(null).pipe(
      delay(this.latency + entryIds.length * 15),
      map(() => {
        this.entries = updated;
        return { succeeded: outcomes.length, failed: 0, outcomes };
      })
    );
  }

  // --------------------------------------------------------------------- Import / export

  previewImport(rows: ParsedImportRow[]): Observable<KnowledgeBaseImportCandidate[]> {
    const existing = new Map(this.entries.map((entry) => [normalizeQuestion(entry.question), entry.id]));

    const candidates: KnowledgeBaseImportCandidate[] = rows.map((row) => {
      if (row.rejected) {
        return {
          payload: row.payload,
          sourceId: row.sourceId,
          state: KnowledgeBaseImportCandidateState.REJECTED,
          existingEntryId: null,
          issues: row.issues,
          faqEnabled: row.faqEnabled
        };
      }

      const existingEntryId = existing.get(normalizeQuestion(row.payload.question)) ?? null;

      return {
        payload: row.payload,
        sourceId: row.sourceId,
        state: existingEntryId ? KnowledgeBaseImportCandidateState.DUPLICATE : KnowledgeBaseImportCandidateState.NEW,
        existingEntryId,
        issues: row.issues,
        faqEnabled: row.faqEnabled
      };
    });

    return of(candidates).pipe(delay(this.latency));
  }

  importEntries(
    candidates: KnowledgeBaseImportCandidate[],
    duplicatePolicy: KnowledgeBaseDuplicatePolicy
  ): Observable<KnowledgeBaseImportResult> {
    const result: KnowledgeBaseImportResult = { created: 0, updated: 0, skipped: 0, failed: 0, entryIds: [] };
    const now = new Date().toISOString();
    let working = [...this.entries];

    candidates.forEach((candidate, offset) => {
      if (candidate.state === KnowledgeBaseImportCandidateState.REJECTED) {
        result.skipped++;
        return;
      }

      if (candidate.state === KnowledgeBaseImportCandidateState.DUPLICATE && duplicatePolicy === KnowledgeBaseDuplicatePolicy.SKIP) {
        result.skipped++;
        return;
      }

      if (candidate.state === KnowledgeBaseImportCandidateState.DUPLICATE && duplicatePolicy === KnowledgeBaseDuplicatePolicy.UPDATE) {
        const index = working.findIndex((entry) => entry.id === candidate.existingEntryId);

        if (index === -1) {
          result.failed++;
          return;
        }

        working[index] = {
          ...working[index],
          ...candidate.payload,
          contentHash: this.hash(candidate.payload),
          updatedAt: now,
          updatedBy: this.currentUser()
        };
        result.updated++;
        result.entryIds.push(working[index].id);
        return;
      }

      const created: KnowledgeBaseEntry = {
        id: `kb-import-${Date.now()}-${offset}`,
        namespace: this.stateService.currentApplication.namespace,
        botIds: [this.stateService.currentApplication.name],
        ...candidate.payload,
        contentHash: this.hash(candidate.payload),
        // Imported entries land as drafts, never projected: nothing reaches the index
        // until someone publishes them.
        projectionState: KnowledgeBaseProjectionState.NONE,
        projectedAt: null,
        projectedIndexSessionId: null,
        createdAt: now,
        createdBy: this.currentUser(),
        updatedAt: null,
        updatedBy: null
      };

      working = [created, ...working];
      result.created++;
      result.entryIds.push(created.id);
    });

    return of(null).pipe(
      delay(this.latency + candidates.length * 10),
      map(() => {
        this.entries = working;
        return result;
      })
    );
  }

  exportEntries(): Observable<KnowledgeBaseExportEnvelope> {
    const envelope: KnowledgeBaseExportEnvelope = {
      format: KNOWLEDGE_BASE_EXPORT_FORMAT,
      version: KNOWLEDGE_BASE_EXPORT_VERSION,
      exportedAt: new Date().toISOString(),
      namespace: this.stateService.currentApplication?.namespace ?? '',
      botId: this.stateService.currentApplication?.name ?? '',
      entries: this.entries.map((entry) => ({
        sourceId: entry.id,
        title: entry.title,
        question: entry.question,
        questionVariants: entry.questionVariants,
        answer: entry.answer,
        sourceUrl: entry.sourceUrl,
        tags: entry.tags,
        status: entry.status
      }))
    };

    return of(envelope).pipe(delay(this.latency));
  }

  // --------------------------------------------------------------------- Mock scenario handling

  /** MOCK ONLY — switches the demo scenario. Remove along with the scenario selector. */
  setScenario(scenario: KnowledgeBaseMockScenario): void {
    this.applyScenario(scenario);
    this.scenarioSubject.next(scenario);
  }

  private indexMode: KnowledgeBaseIndexMode = KnowledgeBaseIndexMode.TOCK_MANAGED;
  private indexSessionId: string | null = MOCK_INDEX_SESSION_ID;
  private indexName: string | null = MOCK_INDEX_NAME;
  private embeddingModelKnown: boolean = true;
  private lastProjectionAt: string | null = null;

  private applyScenario(scenario: KnowledgeBaseMockScenario): void {
    const namespace = this.stateService.currentApplication?.namespace ?? 'app';
    const botId = this.stateService.currentApplication?.name ?? 'bot';
    const seeded = buildMockEntries(namespace, botId);

    switch (scenario) {
      case KnowledgeBaseMockScenario.NO_INDEX:
        this.indexMode = KnowledgeBaseIndexMode.NONE;
        this.indexSessionId = null;
        this.indexName = null;
        this.embeddingModelKnown = true;
        this.lastProjectionAt = null;
        this.entries = seeded.map((entry) => ({
          ...entry,
          projectionState: KnowledgeBaseProjectionState.NONE,
          projectedAt: null,
          projectedIndexSessionId: null
        }));
        break;

      case KnowledgeBaseMockScenario.IN_SYNC:
        this.indexMode = KnowledgeBaseIndexMode.TOCK_MANAGED;
        this.indexSessionId = MOCK_INDEX_SESSION_ID;
        this.indexName = MOCK_INDEX_NAME;
        this.embeddingModelKnown = true;
        this.lastProjectionAt = new Date().toISOString();
        this.entries = seeded.map((entry) => ({
          ...entry,
          projectionState:
            entry.status === KnowledgeBaseEntryStatus.PUBLISHED ? KnowledgeBaseProjectionState.INDEXED : KnowledgeBaseProjectionState.NONE
        }));
        break;

      case KnowledgeBaseMockScenario.OUT_OF_SYNC:
        this.indexMode = KnowledgeBaseIndexMode.TOCK_MANAGED;
        this.indexSessionId = MOCK_INDEX_SESSION_ID;
        this.indexName = MOCK_INDEX_NAME;
        this.embeddingModelKnown = true;
        this.lastProjectionAt = null;
        this.entries = seeded;
        break;

      case KnowledgeBaseMockScenario.EXTERNAL_INDEX:
        this.indexMode = KnowledgeBaseIndexMode.EXTERNAL;
        this.indexSessionId = MOCK_EXTERNAL_INDEX_SESSION_ID;
        this.indexName = MOCK_EXTERNAL_INDEX_NAME;
        this.embeddingModelKnown = false;
        this.lastProjectionAt = null;
        this.entries = seeded.map((entry) => ({
          ...entry,
          projectionState:
            entry.status === KnowledgeBaseEntryStatus.PUBLISHED ? KnowledgeBaseProjectionState.PENDING : KnowledgeBaseProjectionState.NONE,
          projectedAt: null,
          projectedIndexSessionId: null
        }));
        break;

      case KnowledgeBaseMockScenario.NO_ENTRIES:
        this.indexMode = KnowledgeBaseIndexMode.EXTERNAL;
        this.indexSessionId = MOCK_EXTERNAL_INDEX_SESSION_ID;
        this.indexName = MOCK_EXTERNAL_INDEX_NAME;
        this.embeddingModelKnown = false;
        this.lastProjectionAt = null;
        this.entries = [];
        break;
    }
  }

  // --------------------------------------------------------------------- Private helpers

  private hasIndex(): boolean {
    return this.indexMode !== KnowledgeBaseIndexMode.NONE;
  }

  private currentIndexSessionId(): string | null {
    return this.indexSessionId;
  }

  private currentUser(): string {
    // TODO confirm the actual field on the User type (email / login / name) before the REST implementation.
    return this.stateService.user?.email?.split('@')[0] ?? 'unknown';
  }

  private nextProjectionState(
    previous: KnowledgeBaseEntry,
    status: KnowledgeBaseEntryStatus,
    contentChanged: boolean
  ): KnowledgeBaseProjectionState {
    if (!this.hasIndex()) return KnowledgeBaseProjectionState.NONE;

    // Unpublishing is as synchronous as publishing: the rows are removed from the index
    // right away, so the entry falls back to NONE and never to ORPHAN. Deferring the removal
    // would leave the bot answering with an entry the user just withdrew.
    //
    // ORPHAN is reserved for rows the current index holds while no entry in force accounts
    // for them: an index session change, a removal that failed because the store was
    // unreachable, or a write made outside Tock. It is never produced by a normal edit.
    if (status === KnowledgeBaseEntryStatus.DRAFT) {
      return KnowledgeBaseProjectionState.NONE;
    }

    // A published entry is projected synchronously on save, so it is immediately up to date,
    // whether its content changed or not.
    void contentChanged;
    void previous;
    return KnowledgeBaseProjectionState.INDEXED;
  }

  private refreshSyncStatus(): void {
    const entries = this.entriesSubject.getValue();

    const counts: KnowledgeBaseCounts = {
      total: entries.length,
      draft: entries.filter((e) => e.status === KnowledgeBaseEntryStatus.DRAFT).length,
      published: entries.filter((e) => e.status === KnowledgeBaseEntryStatus.PUBLISHED).length,
      indexed: entries.filter((e) => e.projectionState === KnowledgeBaseProjectionState.INDEXED).length,
      pending: entries.filter((e) => e.projectionState === KnowledgeBaseProjectionState.PENDING).length,
      orphan: entries.filter((e) => e.projectionState === KnowledgeBaseProjectionState.ORPHAN).length
    };

    this.syncStatusSubject.next({
      indexMode: this.indexMode,
      indexSessionId: this.indexSessionId,
      // The physical index name is NEVER built client side: PGVector and OpenSearch
      // normalize it differently (PGVector replaces every character outside
      // [a-z0-9_] with '_', OpenSearch keeps hyphens). It is resolved server side
      // and read from the indexes endpoint. The mock therefore stores it instead
      // of deriving it.
      indexName: this.indexName,
      embeddingModelKnown: this.embeddingModelKnown,
      embeddingModel: this.embeddingModelKnown ? MOCK_EMBEDDING_MODEL : null,
      lastProjectionAt: this.lastProjectionAt,
      counts
    });
  }

  private applyFilters(entries: KnowledgeBaseEntry[], query: KnowledgeBaseSearchQuery): KnowledgeBaseEntry[] {
    const search = query.search?.trim().toLowerCase();

    return entries.filter((entry) => {
      if (query.status && entry.status !== query.status) return false;
      if (query.projectionState && entry.projectionState !== query.projectionState) return false;
      if (query.tag && !entry.tags.includes(query.tag)) return false;

      if (search) {
        const haystack = [entry.title, entry.question, ...entry.questionVariants, entry.answer, ...entry.tags].join(' ').toLowerCase();
        if (!haystack.includes(search)) return false;
      }

      return true;
    });
  }

  private applySort(entries: KnowledgeBaseEntry[], query: KnowledgeBaseSearchQuery): KnowledgeBaseEntry[] {
    const field = query.sort ?? 'updatedAt';
    const direction = query.direction ?? 'desc';

    return [...entries].sort((a, b) => {
      let cmp = 0;

      switch (field) {
        case 'title':
          cmp = a.title.localeCompare(b.title);
          break;
        case 'status':
          cmp = a.status.localeCompare(b.status);
          break;
        case 'updatedAt':
          cmp = new Date(a.updatedAt ?? a.createdAt).getTime() - new Date(b.updatedAt ?? b.createdAt).getTime();
          break;
      }

      return direction === 'asc' ? cmp : -cmp;
    });
  }

  /** MOCK ONLY — stands in for the server side content hash. */
  private hash(payload: KnowledgeBaseEntryPayload): string {
    const raw = [payload.title, payload.question, ...payload.questionVariants, payload.answer, payload.sourceUrl ?? ''].join('|');
    let hash = 0;
    for (let i = 0; i < raw.length; i++) {
      hash = (hash << 5) - hash + raw.charCodeAt(i);
      hash |= 0;
    }
    return `h${Math.abs(hash)}`;
  }

  /** MOCK ONLY — crude lexical overlap, just enough to make the retrieval test plausible. */
  private naiveScore(question: string, entry: KnowledgeBaseEntry): number {
    const tokens = question.split(/\W+/).filter((token) => token.length > 3);
    if (!tokens.length) return 0;

    const haystack = [entry.title, entry.question, ...entry.questionVariants, entry.answer].join(' ').toLowerCase();
    const matched = tokens.filter((token) => haystack.includes(token)).length;

    return matched === 0 ? 0 : Math.min(0.95, 0.45 + (matched / tokens.length) * 0.5);
  }
}

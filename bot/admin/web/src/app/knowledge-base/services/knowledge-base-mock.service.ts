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
import { delay, map } from 'rxjs/operators';

import { StateService } from '../../core-nlp/state.service';
import { PaginatedResult } from '../../model/nlp';
import { deepCopy } from '../../shared/utils';
import { KnowledgeBaseService } from './knowledge-base.service';
import { ParsedImportRow, normalizeTitle } from '../utils/import.utils';
import {
  KNOWLEDGE_BASE_EXPORT_FORMAT,
  KNOWLEDGE_BASE_EXPORT_VERSION,
  KnowledgeBaseCounts,
  KnowledgeBaseDuplicatePolicy,
  KnowledgeBaseEntry,
  KnowledgeBaseEntryPayload,
  KnowledgeBaseEntrySaveResult,
  KnowledgeBaseEntryStatus,
  KnowledgeBaseExportEnvelope,
  KnowledgeBaseImportCandidate,
  KnowledgeBaseImportCandidateState,
  KnowledgeBaseImportResult,
  KnowledgeBaseIndexMode,
  KnowledgeBaseJob,
  KnowledgeBaseJobState,
  KnowledgeBaseJobType,
  KnowledgeBaseProjectionState,
  KnowledgeBaseRetrievalHit,
  KnowledgeBaseRetrievalTest,
  KnowledgeBaseSearchQuery,
  KnowledgeBaseSyncStatus,
  isJobFinished
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

  createEntry(payload: KnowledgeBaseEntryPayload): Observable<KnowledgeBaseEntrySaveResult> {
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
      map((entry) => {
        this.entries = [created, ...this.entries];
        return {
          entry: deepCopy(entry),
          job: this.queueEntryProjection(KnowledgeBaseJobType.SAVE_ENTRY, entry.id, payload.status)
        };
      })
    );
  }

  updateEntry(entryId: string, payload: KnowledgeBaseEntryPayload): Observable<KnowledgeBaseEntrySaveResult> {
    const index = this.entries.findIndex((e) => e.id === entryId);
    if (index === -1) return throwError(() => new Error('ENTRY_NOT_FOUND')).pipe(delay(this.latency));

    const previous = this.entries[index];
    const contentHash = this.hash(payload);
    const contentChanged = contentHash !== previous.contentHash;

    const updated: KnowledgeBaseEntry = {
      ...previous,
      ...payload,
      contentHash,
      // The projection fields are left untouched here: they reflect what the index holds,
      // and only the queued job is entitled to change them.
      updatedAt: new Date().toISOString(),
      updatedBy: this.currentUser()
    };

    return of(updated).pipe(
      delay(this.latency),
      map((entry) => {
        this.entries = [...this.entries.slice(0, index), updated, ...this.entries.slice(index + 1)];
        return {
          entry: deepCopy(entry),
          job: this.queueEntryProjection(KnowledgeBaseJobType.SAVE_ENTRY, entry.id, payload.status)
        };
      })
    );
  }

  deleteEntry(entryId: string): Observable<KnowledgeBaseJob> {
    const removed = this.entries.find((e) => e.id === entryId) ?? null;
    const wasIndexed = removed?.projectionState === KnowledgeBaseProjectionState.INDEXED;

    return of(null).pipe(
      delay(this.latency),
      map(() => {
        this.entries = this.entries.filter((e) => e.id !== entryId);

        return this.enqueue(KnowledgeBaseJobType.DELETE_ENTRY, 1, () => {
          this.refreshSyncStatus();
          return { projected: 0, removed: wasIndexed ? 1 : 0 };
        });
      })
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
  synchronize(): Observable<KnowledgeBaseJob> {
    const pending = this.entries.filter(
      (e) => e.status === KnowledgeBaseEntryStatus.PUBLISHED && e.projectionState === KnowledgeBaseProjectionState.PENDING
    );
    const orphans = this.entries.filter((e) => e.projectionState === KnowledgeBaseProjectionState.ORPHAN);

    return this.startJob(KnowledgeBaseJobType.REPAIR_INDEX, pending.length + orphans.length, () => {
      const now = new Date().toISOString();
      const sessionId = this.currentIndexSessionId();

      this.entries = this.entries.map((entry) =>
        entry.status === KnowledgeBaseEntryStatus.PUBLISHED
          ? { ...entry, projectionState: KnowledgeBaseProjectionState.INDEXED, projectedAt: now, projectedIndexSessionId: sessionId }
          : { ...entry, projectionState: KnowledgeBaseProjectionState.NONE, projectedAt: null, projectedIndexSessionId: null }
      );
      this.lastProjectionAt = now;
      this.refreshSyncStatus();

      return { projected: pending.length, removed: orphans.length };
    });
  }

  /**
   * Standalone mode: Tock creates the index session itself from the knowledge base,
   * writes the rows and returns the resulting session so that the RAG configuration can be updated.
   */
  createIndex(): Observable<KnowledgeBaseJob> {
    const publishedCount = this.entries.filter((e) => e.status === KnowledgeBaseEntryStatus.PUBLISHED).length;

    return this.startJob(KnowledgeBaseJobType.CREATE_INDEX, publishedCount, () => {
      this.indexMode = KnowledgeBaseIndexMode.TOCK_MANAGED;
      this.indexSessionId = MOCK_INDEX_SESSION_ID;
      this.indexName = MOCK_INDEX_NAME;
      this.embeddingModelKnown = true;

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

      return { projected: publishedCount, removed: 0 };
    });
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
      content: `${candidate.entry.title}\n${candidate.entry.searchHints.join('\n')}\n${candidate.entry.content}`
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

  // --------------------------------------------------------------------- Bulk actions and jobs

  bulkUpdateStatus(entryIds: string[], status: KnowledgeBaseEntryStatus): Observable<KnowledgeBaseJob> {
    const targeted = new Set(entryIds);
    const type = status === KnowledgeBaseEntryStatus.PUBLISHED ? KnowledgeBaseJobType.PUBLISH : KnowledgeBaseJobType.UNPUBLISH;

    return this.startJob(type, entryIds.length, () => {
      const now = new Date().toISOString();
      const sessionId = this.currentIndexSessionId();
      let projected = 0;
      let removed = 0;

      this.entries = this.entries.map((entry) => {
        if (!targeted.has(entry.id)) return entry;

        const willProject = status === KnowledgeBaseEntryStatus.PUBLISHED && this.hasIndex();
        if (willProject) projected++;
        else if (entry.projectionState === KnowledgeBaseProjectionState.INDEXED) removed++;

        return {
          ...entry,
          status,
          projectionState: willProject ? KnowledgeBaseProjectionState.INDEXED : KnowledgeBaseProjectionState.NONE,
          projectedAt: willProject ? now : null,
          projectedIndexSessionId: willProject ? sessionId : null,
          updatedAt: now,
          updatedBy: this.currentUser()
        };
      });

      return { projected, removed };
    });
  }

  getJob(jobId: string): Observable<KnowledgeBaseJob> {
    return of(null).pipe(
      delay(150),
      map(() => deepCopy(this.advance(jobId)))
    );
  }

  getActiveJob(): Observable<KnowledgeBaseJob | null> {
    const running = this.jobs.map((tracked) => this.advance(tracked.job.id)).find((job) => !isJobFinished(job)) ?? null;

    return of(running ? deepCopy(running) : null).pipe(delay(150));
  }

  // --------------------------------------------------------------------- Import / export

  previewImport(rows: ParsedImportRow[]): Observable<KnowledgeBaseImportCandidate[]> {
    const existing = new Map(this.entries.map((entry) => [normalizeTitle(entry.title), entry.id]));

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

      const existingEntryId = existing.get(normalizeTitle(row.payload.title)) ?? null;

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
        searchHints: entry.searchHints,
        content: entry.content,
        sourceUrl: entry.sourceUrl,
        tags: entry.tags,
        status: entry.status
      }))
    };

    return of(envelope).pipe(delay(this.latency));
  }

  // --------------------------------------------------------------------- Job machinery (MOCK ONLY)

  /**
   * Jobs run one after another, as the server queue would, and are advanced on read from the
   * elapsed time rather than by a timer: nothing to leak, and progress stays deterministic
   * for a demo. The effect on the store is applied once, when a job reaches its end.
   */
  private jobs: { job: KnowledgeBaseJob; startAt: number; durationMs: number; apply: () => { projected: number; removed: number } }[] = [];

  private readonly perItemMs = 120;

  /** End of the last queued job, so a new one lines up behind it instead of running in parallel. */
  private get queueFreeAt(): number {
    return this.jobs.reduce((latest, tracked) => Math.max(latest, tracked.startAt + tracked.durationMs), Date.now());
  }

  private enqueue(type: KnowledgeBaseJobType, total: number, apply: () => { projected: number; removed: number }): KnowledgeBaseJob {
    const job: KnowledgeBaseJob = {
      id: `job-${Date.now()}-${this.jobs.length}`,
      type,
      state: KnowledgeBaseJobState.QUEUED,
      startedAt: new Date().toISOString(),
      endedAt: null,
      progress: { total, done: 0, failed: 0 },
      failures: [],
      projected: 0,
      removed: 0,
      syncStatus: null,
      error: null
    };

    this.jobs.push({
      job,
      startAt: this.queueFreeAt,
      // A floor keeps very small jobs visible in the demo.
      durationMs: Math.max(700, total * this.perItemMs),
      apply
    });

    return deepCopy(this.advance(job.id));
  }

  private startJob(
    type: KnowledgeBaseJobType,
    total: number,
    apply: () => { projected: number; removed: number }
  ): Observable<KnowledgeBaseJob> {
    return of(null).pipe(
      delay(this.latency),
      map(() => this.enqueue(type, total, apply))
    );
  }

  private advance(jobId: string): KnowledgeBaseJob {
    const tracked = this.jobs.find((candidate) => candidate.job.id === jobId);
    if (!tracked) throw new Error('JOB_NOT_FOUND');

    const { job, startAt, durationMs } = tracked;
    if (isJobFinished(job)) return job;

    const now = Date.now();

    if (now < startAt) {
      job.state = KnowledgeBaseJobState.QUEUED;
      return job;
    }

    const ratio = Math.min(1, (now - startAt) / durationMs);
    job.progress.done = Math.floor(job.progress.total * ratio);
    job.state = ratio >= 1 ? KnowledgeBaseJobState.COMPLETED : KnowledgeBaseJobState.RUNNING;

    if (job.state === KnowledgeBaseJobState.COMPLETED) {
      const outcome = tracked.apply();
      job.progress.done = job.progress.total;
      job.projected = outcome.projected;
      job.removed = outcome.removed;
      job.endedAt = new Date().toISOString();
      this.refreshSyncStatus();
      job.syncStatus = this.syncStatusSubject.getValue();
    }

    return job;
  }

  /**
   * Projection of a single entry. Queued exactly like a batch, so a save issued while a batch
   * is running lines up behind it instead of racing it on the same index.
   */
  private queueEntryProjection(type: KnowledgeBaseJobType, entryId: string, status: KnowledgeBaseEntryStatus): KnowledgeBaseJob {
    return this.enqueue(type, 1, () => {
      const index = this.entries.findIndex((entry) => entry.id === entryId);
      if (index === -1) return { projected: 0, removed: 0 };

      const entry = this.entries[index];
      const wasIndexed = entry.projectionState === KnowledgeBaseProjectionState.INDEXED;
      const willProject = status === KnowledgeBaseEntryStatus.PUBLISHED && this.hasIndex();
      const now = new Date().toISOString();

      const projectedEntry = {
        ...entry,
        projectionState: willProject ? KnowledgeBaseProjectionState.INDEXED : KnowledgeBaseProjectionState.NONE,
        projectedAt: willProject ? now : null,
        projectedIndexSessionId: willProject ? this.currentIndexSessionId() : null
      };

      this.entries = [...this.entries.slice(0, index), projectedEntry, ...this.entries.slice(index + 1)];

      return { projected: willProject ? 1 : 0, removed: !willProject && wasIndexed ? 1 : 0 };
    });
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
        const haystack = [entry.title, ...entry.searchHints, entry.content, ...entry.tags].join(' ').toLowerCase();
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
    const raw = [payload.title, ...payload.searchHints, payload.content, payload.sourceUrl ?? ''].join('|');
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

    const haystack = [entry.title, ...entry.searchHints, entry.content].join(' ').toLowerCase();
    const matched = tokens.filter((token) => haystack.includes(token)).length;

    return matched === 0 ? 0 : Math.min(0.95, 0.45 + (matched / tokens.length) * 0.5);
  }
}

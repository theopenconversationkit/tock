import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, map, tap } from 'rxjs';

import { VectorStoreInspectionService } from './vector-store-inspection.service';
import { DocumentSearchType } from '../../rag/rag-settings/models/engines-configurations';
import { ChunkId, SearchRun, VectorStoreCapabilities, VectorStoreIndex } from '../models/vector-store-inspection.models';

export const MAX_PINNED_CHUNKS = 50;
/**
 * State shared by the exploration and diagnostic views.
 *
 * Both views are separate routes, so component state would be destroyed on
 * navigation. Pinning only earns its keep if a chunk pinned while browsing an
 * index is still pinned once the user moves to the diagnostic view and asks a
 * question, so that state lives here instead.
 *
 * Provided at module level, not root: the state is scoped to the feature and
 * resets when the user leaves it.
 */
@Injectable()
export class VectorStoreInspectionStateService {
  private readonly inspection = inject(VectorStoreInspectionService);

  private readonly indexes$$ = new BehaviorSubject<VectorStoreIndex[]>([]);
  private readonly currentIndex$$ = new BehaviorSubject<VectorStoreIndex | null>(null);
  private readonly capabilities$$ = new BehaviorSubject<VectorStoreCapabilities | null>(null);
  private readonly pinnedChunkIds$$ = new BehaviorSubject<ChunkId[]>([]);
  private readonly searchType$$ = new BehaviorSubject<DocumentSearchType>(DocumentSearchType.HYBRID_SEARCH);

  readonly indexes$: Observable<VectorStoreIndex[]> = this.indexes$$.asObservable();
  readonly currentIndex$: Observable<VectorStoreIndex | null> = this.currentIndex$$.asObservable();
  readonly capabilities$: Observable<VectorStoreCapabilities | null> = this.capabilities$$.asObservable();
  readonly pinnedChunkIds$: Observable<ChunkId[]> = this.pinnedChunkIds$$.asObservable();
  readonly searchType$: Observable<DocumentSearchType> = this.searchType$$.asObservable();

  private readonly referenceRun$$ = new BehaviorSubject<SearchRun | null>(null);

  readonly referenceRun$: Observable<SearchRun | null> = this.referenceRun$$.asObservable();

  private currentBotKey: string | null = null;

  get referenceRun(): SearchRun | null {
    return this.referenceRun$$.value;
  }

  /**
   * Freezes a run as the comparison baseline. Set explicitly rather than
   * implicitly from the previous search, so several variants can be tried
   * against a stable point of reference.
   */
  setReferenceRun(run: SearchRun | null): void {
    this.referenceRun$$.next(run);
  }

  get currentIndex(): VectorStoreIndex | null {
    return this.currentIndex$$.value;
  }

  get currentIndexName(): string | null {
    return this.currentIndex$$.value?.indexName ?? null;
  }

  get pinnedChunkIds(): ChunkId[] {
    return this.pinnedChunkIds$$.value;
  }

  get searchType(): DocumentSearchType {
    return this.searchType$$.value;
  }

  /**
   * Loads the index list once per visit and preselects the one currently used
   * by the bot, which is the expected starting point in the vast majority of
   * cases. Subsequent calls are no-ops so navigating between the two views
   * does not refetch.
   */
  loadIndexes(force = false): Observable<VectorStoreIndex[]> {
    if (!force && this.indexes$$.value.length) {
      return this.indexes$;
    }

    return this.inspection.getIndexes().pipe(
      tap((response) => {
        this.indexes$$.next(response.indexes);

        if (!this.currentIndex$$.value) {
          const preselected = response.indexes.find((index) => index.isCurrent) ?? response.indexes[0] ?? null;
          this.currentIndex$$.next(preselected);
        }
      }),
      map((response) => response.indexes)
    );
  }

  loadCapabilities(): Observable<VectorStoreCapabilities> {
    return this.inspection.getCapabilities().pipe(tap((c) => this.capabilities$$.next(c)));
  }

  selectIndex(index: VectorStoreIndex): void {
    if (index.indexName === this.currentIndex$$.value?.indexName) {
      return;
    }
    // Pins deliberately survive an index change: comparing what a chunk does
    // across two ingestions is the reason they exist. A pinned chunk absent
    // from the newly selected index surfaces as not_retrieved rather than
    // being silently dropped.
    this.currentIndex$$.next(index);
  }

  setSearchType(searchType: DocumentSearchType): void {
    this.searchType$$.next(searchType);
  }

  isPinned(chunkId: ChunkId): boolean {
    return this.pinnedChunkIds$$.value.includes(chunkId);
  }

  canPin(chunkId: ChunkId): boolean {
    return this.isPinned(chunkId) || this.pinnedChunkIds$$.value.length < MAX_PINNED_CHUNKS;
  }

  togglePin(chunkId: ChunkId): void {
    const current = this.pinnedChunkIds$$.value;
    if (!current.includes(chunkId) && current.length >= MAX_PINNED_CHUNKS) return;
    this.pinnedChunkIds$$.next(current.includes(chunkId) ? current.filter((id) => id !== chunkId) : [...current, chunkId]);
  }

  clearPins(): void {
    this.pinnedChunkIds$$.next([]);
  }

  /**
   * Drops everything scoped to a bot, but only when the bot actually changed.
   *
   * botConfiguration.configurations is a BehaviorSubject: it replays on every
   * subscription, so simply resetting on each emission would wipe the pins
   * every time the user navigates between the two views.
   *
   * Returns true when a reset happened, so callers know they must refetch.
   */
  applyBotContext(botKey: string | null): boolean {
    if (botKey === this.currentBotKey) return false;

    this.currentBotKey = botKey;

    this.indexes$$.next([]);
    this.currentIndex$$.next(null);
    this.capabilities$$.next(null);
    this.pinnedChunkIds$$.next([]);
    this.referenceRun$$.next(null);

    return true;
  }

  /**
   * Clears everything scoped to a bot. Indexes belong to a namespace/bot pair,
   * so switching bots invalidates the list, the selection and the pins alike:
   * a chunk id from another bot's index means nothing here.
   */
  reset(): void {
    this.indexes$$.next([]);
    this.currentIndex$$.next(null);
    this.capabilities$$.next(null);
    this.pinnedChunkIds$$.next([]);
    this.referenceRun$$.next(null);
  }
}

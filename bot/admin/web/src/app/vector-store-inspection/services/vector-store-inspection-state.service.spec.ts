import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { VectorStoreInspectionStateService } from './vector-store-inspection-state.service';
import { VectorStoreInspectionService } from './vector-store-inspection.service';
import { IndexListResponse, VectorStoreIndex } from '../models/vector-store-inspection.models';

function makeIndex(name: string, overrides: Partial<VectorStoreIndex> = {}): VectorStoreIndex {
  return {
    indexName: name,
    indexSessionId: `${name}-session`,
    indexDatetime: '2024-01-01T00:00:00Z',
    documentCount: 1,
    chunkCount: 1,
    isCurrent: false,
    ...overrides
  };
}

describe('VectorStoreInspectionStateService', () => {
  let service: VectorStoreInspectionStateService;
  // Reassigned per test before refreshIndexes is called; the mock reads it lazily.
  let response: IndexListResponse;

  beforeEach(() => {
    response = { indexes: [] };

    TestBed.configureTestingModule({
      providers: [
        VectorStoreInspectionStateService,
        { provide: VectorStoreInspectionService, useValue: { getIndexes: () => of(response) } }
      ]
    });

    service = TestBed.inject(VectorStoreInspectionStateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('refreshIndexes', () => {
    it('keeps the selection and swaps in its fresh object when the index is still present', () => {
      const stale = makeIndex('a', { documentCount: 1 });
      service.selectIndex(stale);

      const fresh = makeIndex('a', { documentCount: 99 });
      response = { indexes: [fresh, makeIndex('b')] };

      let kept: boolean | undefined;
      service.refreshIndexes().subscribe((value) => (kept = value));

      expect(kept).toBeTrue();
      expect(service.currentIndex).toBe(fresh);
      expect(service.currentIndex).not.toBe(stale);
      expect(service.currentIndex!.documentCount).toBe(99);
    });

    it('publishes the fresh index list', () => {
      service.selectIndex(makeIndex('a'));
      response = { indexes: [makeIndex('a'), makeIndex('b')] };

      let indexes: VectorStoreIndex[] | undefined;
      service.indexes$.subscribe((value) => (indexes = value));

      service.refreshIndexes().subscribe();

      expect(indexes).toBe(response.indexes);
    });

    it('falls back to the flagged current index when the selection is gone', () => {
      service.selectIndex(makeIndex('a'));

      const current = makeIndex('c', { isCurrent: true });
      response = { indexes: [makeIndex('b'), current] };

      let kept: boolean | undefined;
      service.refreshIndexes().subscribe((value) => (kept = value));

      expect(kept).toBeFalse();
      expect(service.currentIndex).toBe(current);
    });

    it('falls back to the first index when the selection is gone and none is flagged current', () => {
      service.selectIndex(makeIndex('a'));

      const first = makeIndex('b');
      response = { indexes: [first, makeIndex('d')] };

      let kept: boolean | undefined;
      service.refreshIndexes().subscribe((value) => (kept = value));

      expect(kept).toBeFalse();
      expect(service.currentIndex).toBe(first);
    });

    it('clears the selection when the list is empty', () => {
      service.selectIndex(makeIndex('a'));
      response = { indexes: [] };

      let kept: boolean | undefined;
      service.refreshIndexes().subscribe((value) => (kept = value));

      expect(kept).toBeFalse();
      expect(service.currentIndex).toBeNull();
    });

    it('selects the flagged current index when nothing was selected yet', () => {
      const current = makeIndex('c', { isCurrent: true });
      response = { indexes: [makeIndex('a'), current] };

      let kept: boolean | undefined;
      service.refreshIndexes().subscribe((value) => (kept = value));

      expect(kept).toBeFalse();
      expect(service.currentIndex).toBe(current);
    });
  });
});

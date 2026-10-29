import { DocumentSearchType } from '../../rag/rag-settings/models/engines-configurations';
import { SearchRequest, SearchResponse, SearchResultChunk, SearchRun } from '../models/vector-store-inspection.models';
import { buildRunComparison } from './run-comparison';

function chunk(rank: number | null, outcome: SearchResultChunk['outcome'] = 'kept'): SearchResultChunk {
  return {
    chunkId: 'document:1/1',
    documentId: 'document',
    title: 'Document',
    chunk: '1/1',
    content: 'Content',
    ranks: { vector: rank, fts: null, rrf: null },
    scores: { vector: null, fts: null, rrf: null, compressor: null },
    outcome,
    pinned: outcome === 'not_retrieved'
  };
}

function run(indexName: string, results: SearchResultChunk[], pinnedChunkIds: string[] = []): SearchRun {
  return {
    request: {
      indexName,
      searchType: DocumentSearchType.SIMILARITY_SEARCH,
      query: 'query',
      k: 4,
      compressionEnabled: false,
      pinnedChunkIds
    } as SearchRequest,
    response: { results } as SearchResponse,
    label: indexName,
    ranAt: new Date()
  };
}

describe('buildRunComparison', () => {
  const reference = run('index-a', [chunk(1)]);

  it('does not claim an ingestion issue when a chunk was not checked in the new index', () => {
    const comparison = buildRunComparison(reference, run('index-b', []));

    expect(comparison.rows[0].absenceReason).toBe('unknown');
  });

  it('reports an absent chunk when it was pinned but not found in the new index', () => {
    const comparison = buildRunComparison(reference, run('index-b', [], ['document:1/1']));

    expect(comparison.rows[0].absenceReason).toBe('absent_from_index');
  });

  it('reports a ranking issue when a pinned chunk exists outside the fetched window', () => {
    const comparison = buildRunComparison(reference, run('index-b', [chunk(null, 'not_retrieved')], ['document:1/1']));

    expect(comparison.rows[0].absenceReason).toBe('outside_k');
  });

  it('reports a chunk that stops reaching the model on a threshold change, at the same rank', () => {
    const before = run('index-a', [chunk(1, 'kept')]);
    const after = run('index-a', [chunk(1, 'below_min_score')]);

    const comparison = buildRunComparison(before, after);

    expect(comparison.rows[0].delta).toBe('stable');
    expect(comparison.rows[0].passedOnChange).toBe('no_longer_passed_on');
    expect(comparison.noLongerPassedOn).toBe(1);
    expect(comparison.nowPassedOn).toBe(0);
    expect(comparison.commonInTopK).toBe(0);
  });

  it('counts padded chunks as passed on to the model', () => {
    const before = run('index-a', [chunk(1, 'filled_below_threshold')]);
    const after = run('index-a', [chunk(1, 'kept')]);

    const comparison = buildRunComparison(before, after);

    expect(comparison.rows[0].passedOnChange).toBeUndefined();
    expect(comparison.commonInTopK).toBe(1);
  });

  it('orders passed-on changes consistently whatever the comparison direction', () => {
    const stable: SearchResultChunk = { ...chunk(1, 'kept'), chunkId: 'a:1/1' };
    const moved: SearchResultChunk = { ...chunk(5, 'kept'), chunkId: 'b:1/1' };
    const before = run('index-a', [stable, moved]);
    const after = run('index-a', [
      { ...stable, outcome: 'below_min_score' },
      { ...moved, ranks: { ...moved.ranks, vector: 2 }, outcome: 'below_min_score' }
    ]);

    const forward = buildRunComparison(before, after).rows.map((row) => row.chunkId);
    const reversedInput = buildRunComparison(
      run('index-a', [moved, stable]),
      run('index-a', [...after.response.results].reverse())
    ).rows.map((row) => row.chunkId);

    expect(forward).toEqual(reversedInput);
  });
});

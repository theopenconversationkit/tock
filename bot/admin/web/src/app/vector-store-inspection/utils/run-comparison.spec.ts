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
      fetchK: 10,
      k: 4,
      compressionEnabled: false,
      compressionStage: 'beforeCut',
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

    expect(comparison.rows[0].absenceReason).toBe('outside_fetch_k');
  });
});

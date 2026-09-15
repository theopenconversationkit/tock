import { DocumentSearchType } from '../../rag/rag-settings/models/engines-configurations';
import {
  AbsenceReason,
  RunComparison,
  RunComparisonRow,
  RunDelta,
  RunDiffField,
  SearchRequest,
  SearchResultChunk,
  SearchRun
} from '../models/vector-store-inspection.models';

/**
 * The rank that actually orders the results, which depends on the mode: RRF
 * when both channels ran, otherwise the single active channel. Comparing a
 * vector rank against an RRF rank would be meaningless.
 */
function effectiveRank(result: SearchResultChunk, searchType: DocumentSearchType): number | null {
  if (searchType === DocumentSearchType.HYBRID_SEARCH) return result.ranks.rrf;
  if (searchType === DocumentSearchType.FULL_TEXT_SEARCH) return result.ranks.fts;
  return result.ranks.vector;
}

function sameKeyWords(a: string[] | undefined, b: string[] | undefined): boolean {
  const left = (a ?? []).join('|').toLowerCase();
  const right = (b ?? []).join('|').toLowerCase();
  return left === right;
}

/**
 * Names what changed between the two runs. The banner shows this list: the
 * more entries it holds, the less interpretable the delta, which is a useful
 * implicit warning rather than something to hide.
 */
export function diffRequests(reference: SearchRequest, current: SearchRequest): RunDiffField[] {
  const changed: RunDiffField[] = [];

  if (reference.indexName !== current.indexName) changed.push('index');
  if (reference.searchType !== current.searchType) changed.push('searchType');
  if (reference.query.trim() !== current.query.trim()) changed.push('query');
  if (!sameKeyWords(reference.keyWords, current.keyWords)) changed.push('keyWords');
  if (reference.fetchK !== current.fetchK) changed.push('fetchK');
  if (reference.k !== current.k) changed.push('k');
  if (
    reference.compressionEnabled !== current.compressionEnabled ||
    (current.compressionEnabled && reference.compressionStage !== current.compressionStage)
  ) {
    changed.push('compression');
  }

  return changed;
}

/**
 * Rapprochement between two runs, keyed by chunk id.
 *
 * Absence is deliberately split in two. A chunk missing from the current run
 * because the two runs target different indexes is an ingestion problem; a
 * chunk missing from the same index simply fell out of the fetched window,
 * which is a ranking problem. Collapsing them would blur the two diagnoses the
 * tool exists to separate.
 */
export function buildRunComparison(reference: SearchRun, current: SearchRun): RunComparison {
  const changedFields = diffRequests(reference.request, current.request);
  const indexChanged = changedFields.includes('index');

  const referenceRanks = new Map<string, number | null>();
  const referenceChunks = new Map<string, SearchResultChunk>();
  for (const result of reference.response.results) {
    referenceRanks.set(result.chunkId, effectiveRank(result, reference.request.searchType));
    referenceChunks.set(result.chunkId, result);
  }

  const currentRanks = new Map<string, number | null>();
  const currentChunks = new Map<string, SearchResultChunk>();
  for (const result of current.response.results) {
    currentRanks.set(result.chunkId, effectiveRank(result, current.request.searchType));
    currentChunks.set(result.chunkId, result);
  }

  const chunkIds = new Set<string>([...referenceChunks.keys(), ...currentChunks.keys()]);
  const rows: RunComparisonRow[] = [];

  let lost = 0;
  let gained = 0;

  for (const chunkId of chunkIds) {
    const rankReference = referenceRanks.get(chunkId) ?? null;
    const rankCurrent = currentRanks.get(chunkId) ?? null;
    const chunk = currentChunks.get(chunkId) ?? referenceChunks.get(chunkId)!;

    let delta: RunDelta;
    let magnitude = 0;
    let absenceReason: AbsenceReason | undefined;

    if (rankReference !== null && rankCurrent === null) {
      delta = 'lost';
      lost++;
      const currentChunk = currentChunks.get(chunkId);
      const wasCheckedInCurrentIndex = current.request.pinnedChunkIds?.includes(chunkId);
      absenceReason =
        currentChunk?.outcome === 'not_retrieved'
          ? 'outside_fetch_k'
          : indexChanged
          ? wasCheckedInCurrentIndex
            ? 'absent_from_index'
            : 'unknown'
          : 'outside_fetch_k';
    } else if (rankReference === null && rankCurrent !== null) {
      delta = 'gained';
      gained++;
    } else if (rankReference !== null && rankCurrent !== null && rankReference !== rankCurrent) {
      delta = 'moved';
      magnitude = Math.abs(rankCurrent - rankReference);
    } else {
      delta = 'stable';
    }

    rows.push({
      chunkId,
      title: chunk.title,
      chunk: chunk.chunk,
      rankReference,
      rankCurrent,
      delta,
      magnitude,
      absenceReason,
      pinned: chunk.pinned
    });
  }

  // Losses and gains first: they are the structural changes. Movements follow,
  // widest first. Stable rows are noise and end up last, collapsed by the UI.
  const order: Record<RunDelta, number> = { lost: 0, gained: 1, moved: 2, stable: 3 };
  rows.sort((a, b) => {
    if (order[a.delta] !== order[b.delta]) return order[a.delta] - order[b.delta];
    if (a.delta === 'moved') return b.magnitude - a.magnitude;
    return (a.rankCurrent ?? a.rankReference ?? 0) - (b.rankCurrent ?? b.rankReference ?? 0);
  });

  const referenceTopK = new Set(reference.response.results.filter((result) => result.outcome === 'kept').map((result) => result.chunkId));
  const commonInTopK = current.response.results.filter((result) => result.outcome === 'kept' && referenceTopK.has(result.chunkId)).length;

  return { reference, current, changedFields, rows, commonInTopK, lost, gained };
}

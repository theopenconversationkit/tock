/*
 * Copyright (C) 2017/2026 SNCF Connect & Tech
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

import { ChunkId, ChunkOutcome } from '../models/vector-store-inspection.models';

/**
 * MIRROR — keep in sync with the backend:
 * gen-ai/orchestrator-server/.../impls/document_compressor/rerank_selection.py
 * (select_reranked_documents). This reproduces that exact selection so changing
 * Minimum score or Max documents recomputes outcomes instantly without a new
 * search. Any change to the selection logic there MUST be mirrored here, and the
 * shared cases MUST be updated on both sides (rerank-selection.spec.ts and its
 * Python test_rerank_selection counterpart).
 */
export interface ScoredCandidate {
  chunkId: ChunkId;
  /** The compressor (reranker) score; missing is treated as 0. */
  score: number | null;
}

const scoreOf = (candidate: ScoredCandidate): number => candidate.score ?? 0;

/** The chunk ids the compressor would keep, in no particular order. */
export function selectRerankedChunkIds(
  candidates: ScoredCandidate[],
  minScore: number,
  maxDocuments: number,
  fillToMaxDocuments: boolean
): Set<ChunkId> {
  // Array.prototype.sort is stable (ES2019+), so ties keep input order like the
  // backend's stable sort. Candidates come in retrieval order, which is also the
  // order the backend providers hand their scored documents over in, so a tie
  // at the max documents boundary is broken the same way on both sides.
  const ranked = [...candidates].sort((a, b) => scoreOf(b) - scoreOf(a));
  const above = ranked.filter((candidate) => scoreOf(candidate) >= minScore);
  const below = ranked.filter((candidate) => scoreOf(candidate) < minScore);

  const result = above.slice(0, maxDocuments);
  if (fillToMaxDocuments && result.length < maxDocuments) {
    result.push(...below.slice(0, maxDocuments - result.length));
  }
  return new Set(result.map((candidate) => candidate.chunkId));
}

/**
 * Per-candidate outcome, reproducing the inspection service's labelling:
 * kept / filled_below_threshold for survivors, below_min_score / reranked_out
 * for the rest.
 */
export function recomputeCompressionOutcomes(
  candidates: ScoredCandidate[],
  minScore: number,
  maxDocuments: number,
  fillToMaxDocuments: boolean
): Map<ChunkId, ChunkOutcome> {
  const kept = selectRerankedChunkIds(candidates, minScore, maxDocuments, fillToMaxDocuments);
  const outcomes = new Map<ChunkId, ChunkOutcome>();
  for (const candidate of candidates) {
    const score = scoreOf(candidate);
    if (kept.has(candidate.chunkId)) {
      outcomes.set(candidate.chunkId, score < minScore ? 'filled_below_threshold' : 'kept');
    } else if (score < minScore) {
      outcomes.set(candidate.chunkId, 'below_min_score');
    } else {
      outcomes.set(candidate.chunkId, 'reranked_out');
    }
  }
  return outcomes;
}

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

import { recomputeCompressionOutcomes, ScoredCandidate, selectRerankedChunkIds } from './rerank-selection';

// Cases duplicated verbatim from the Python test_rerank_selection (a shared JSON
// fixture across the two build trees is impractical). Keep them in sync.
interface Case {
  name: string;
  scores: [string, number | null][];
  minScore: number;
  maxDocuments: number;
  fill: boolean;
  expected: string[];
}

const CASES: Case[] = [
  { name: 'all_above_under_max', scores: [['a', 0.9], ['b', 0.8], ['c', 0.7]], minScore: 0.5, maxDocuments: 5, fill: false, expected: ['a', 'b', 'c'] },
  { name: 'above_over_max', scores: [['a', 0.9], ['b', 0.8], ['c', 0.7], ['d', 0.6]], minScore: 0.5, maxDocuments: 2, fill: false, expected: ['a', 'b'] },
  { name: 'below_no_fill', scores: [['a', 0.9], ['b', 0.3], ['c', 0.2]], minScore: 0.5, maxDocuments: 3, fill: false, expected: ['a'] },
  { name: 'below_with_fill', scores: [['a', 0.9], ['b', 0.3], ['c', 0.2]], minScore: 0.5, maxDocuments: 3, fill: true, expected: ['a', 'b', 'c'] },
  { name: 'fill_partial', scores: [['a', 0.9], ['b', 0.3], ['c', 0.2], ['d', 0.1]], minScore: 0.5, maxDocuments: 2, fill: true, expected: ['a', 'b'] },
  { name: 'unsorted_input', scores: [['a', 0.2], ['b', 0.95], ['c', 0.6]], minScore: 0.5, maxDocuments: 5, fill: false, expected: ['b', 'c'] },
  { name: 'missing_score_as_zero', scores: [['a', 0.8], ['b', null]], minScore: 0.5, maxDocuments: 5, fill: false, expected: ['a'] },
  { name: 'ties_keep_input_order', scores: [['a', 0.5], ['b', 0.5]], minScore: 0.5, maxDocuments: 5, fill: false, expected: ['a', 'b'] },
  // A tie right at the max documents boundary: the earlier candidate in retrieval order wins.
  { name: 'tie_at_max_boundary', scores: [['a', 0.7], ['b', 0.7], ['c', 0.95]], minScore: 0.5, maxDocuments: 2, fill: false, expected: ['c', 'a'] }
];

function candidates(scores: [string, number | null][]): ScoredCandidate[] {
  return scores.map(([chunkId, score]) => ({ chunkId, score }));
}

describe('selectRerankedChunkIds', () => {
  CASES.forEach((testCase) => {
    it(`matches the backend selection: ${testCase.name}`, () => {
      const kept = selectRerankedChunkIds(candidates(testCase.scores), testCase.minScore, testCase.maxDocuments, testCase.fill);
      expect([...kept].sort()).toEqual([...testCase.expected].sort());
    });
  });
});

describe('recomputeCompressionOutcomes', () => {
  it('labels kept, filled, below and reranked-out', () => {
    const outcomes = recomputeCompressionOutcomes(
      candidates([
        ['a', 0.9],
        ['b', 0.3],
        ['c', 0.2],
        ['d', 0.8]
      ]),
      0.5,
      2,
      true
    );

    // Above threshold: a (0.9), d (0.8). Kept up to max 2 => a, d.
    expect(outcomes.get('a')).toBe('kept');
    expect(outcomes.get('d')).toBe('kept');
    // Below threshold and not filled (max already reached): b, c => below_min_score.
    expect(outcomes.get('b')).toBe('below_min_score');
    expect(outcomes.get('c')).toBe('below_min_score');
  });

  it('marks filled candidates below the threshold', () => {
    const outcomes = recomputeCompressionOutcomes(
      candidates([
        ['a', 0.9],
        ['b', 0.3]
      ]),
      0.5,
      3,
      true
    );

    expect(outcomes.get('a')).toBe('kept');
    expect(outcomes.get('b')).toBe('filled_below_threshold');
  });

  it('marks above-threshold losers as reranked_out', () => {
    const outcomes = recomputeCompressionOutcomes(
      candidates([
        ['a', 0.9],
        ['b', 0.8],
        ['c', 0.7]
      ]),
      0.5,
      1,
      false
    );

    expect(outcomes.get('a')).toBe('kept');
    expect(outcomes.get('b')).toBe('reranked_out');
    expect(outcomes.get('c')).toBe('reranked_out');
  });
});

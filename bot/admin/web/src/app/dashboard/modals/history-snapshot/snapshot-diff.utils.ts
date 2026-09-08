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
/**
 * Pure diff helpers for the history snapshot modal. Kept out of the component so the
 * flattening and line-diff logic stays unit-testable.
 *
 * Snapshots are already sanitized server side: a removed key (an API key, a secret) is
 * simply absent from both sides, so it never surfaces as a change. Callers must not
 * infer anything from a missing key.
 */

export interface KvDiffRow {
  key: string;
  before: string | null;
  after: string | null;
  changed: boolean;
}

export interface KvDiffResult {
  rows: KvDiffRow[];
  changedCount: number;
}

export type TextDiffKind = 'add' | 'del' | 'ctx';

export interface TextDiffLine {
  kind: TextDiffKind;
  text: string;
}

/** Flattens nested objects to dotted keys; arrays are joined for a readable value. */
export function flattenSnapshot(value: unknown, prefix = ''): Record<string, string> {
  const out: Record<string, string> = {};

  if (value === null || value === undefined) {
    return out;
  }

  for (const [key, raw] of Object.entries(value as Record<string, unknown>)) {
    const path = prefix ? `${prefix}.${key}` : key;

    if (Array.isArray(raw)) {
      out[path] = raw.join(', ');
    } else if (raw !== null && typeof raw === 'object') {
      Object.assign(out, flattenSnapshot(raw, path));
    } else {
      out[path] = String(raw);
    }
  }

  return out;
}

/**
 * Key/value diff between two (already flattened-eligible) objects. When `previous` is
 * null — the first change of its type — every row is reported as unchanged so the modal
 * can present the current state alone.
 */
export function buildKvDiff(previous: Record<string, unknown> | null, current: Record<string, unknown>): KvDiffResult {
  const prev = previous ? flattenSnapshot(previous) : null;
  const cur = flattenSnapshot(current);

  const keys = [...new Set([...(prev ? Object.keys(prev) : []), ...Object.keys(cur)])].sort();

  let changedCount = 0;
  const rows = keys.map((key) => {
    const before = prev ? (key in prev ? prev[key] : null) : null;
    const after = key in cur ? cur[key] : null;
    const changed = prev !== null && before !== after;
    if (changed) changedCount++;
    return { key, before, after, changed };
  });

  return { rows, changedCount };
}

/** Longest common subsequence line diff, rendered as an ordered list of lines. */
export function buildTextDiff(before: string, after: string): TextDiffLine[] {
  const oldLines = String(before ?? '').split('\n');
  const newLines = String(after ?? '').split('\n');
  const n = oldLines.length;
  const m = newLines.length;

  const dp: number[][] = Array.from({ length: n + 1 }, () => new Array(m + 1).fill(0));
  for (let i = n - 1; i >= 0; i--) {
    for (let j = m - 1; j >= 0; j--) {
      dp[i][j] = oldLines[i] === newLines[j] ? dp[i + 1][j + 1] + 1 : Math.max(dp[i + 1][j], dp[i][j + 1]);
    }
  }

  const out: TextDiffLine[] = [];
  let i = 0;
  let j = 0;
  while (i < n && j < m) {
    if (oldLines[i] === newLines[j]) {
      out.push({ kind: 'ctx', text: oldLines[i] });
      i++;
      j++;
    } else if (dp[i + 1][j] >= dp[i][j + 1]) {
      out.push({ kind: 'del', text: oldLines[i] });
      i++;
    } else {
      out.push({ kind: 'add', text: newLines[j] });
      j++;
    }
  }
  while (i < n) out.push({ kind: 'del', text: oldLines[i++] });
  while (j < m) out.push({ kind: 'add', text: newLines[j++] });

  return out;
}

/** Added / removed / kept classification for a string list (prompt-context tags). */
export interface TagDiff {
  value: string;
  status: 'added' | 'removed' | 'kept';
}

export function buildTagDiff(previous: string[] | null, current: string[]): TagDiff[] {
  const before = new Set(previous ?? []);
  const after = new Set(current ?? []);
  const all = [...new Set([...(previous ?? []), ...current])];

  return all.map((value) => {
    if (previous === null) return { value, status: 'kept' as const };
    if (!before.has(value)) return { value, status: 'added' as const };
    if (!after.has(value)) return { value, status: 'removed' as const };
    return { value, status: 'kept' as const };
  });
}

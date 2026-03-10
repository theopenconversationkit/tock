/**
 * ObjectDiffService
 *
 * Compares two arbitrary JSON-serialisable objects and produces an HTML
 * representation of each, styled like a JSON viewer, with diff highlights.
 *
 * Highlight classes (apply your own CSS):
 *   .diff-added   — key/value present in A but not in B  (shown in pane A)
 *   .diff-removed — key/value present in B but not in A  (shown in pane B)
 *   .diff-changed — key exists in both but value differs  (shown in both panes)
 *
 * Rules
 * ─────
 * Objects  → recursive key-by-key diff.
 *            • Key only in A  → entire subtree marked .diff-added   in A.
 *            • Key only in B  → entire subtree marked .diff-removed in B.
 *            • Key in both, same value  → no highlight.
 *            • Key in both, different value:
 *                - If both sides are objects/arrays → recurse.
 *                - Otherwise → mark value .diff-changed in both panes.
 *
 * Arrays   → LCS-based item-by-item diff (same Myers algorithm as the
 *            Markdown service).  Items matched by position after LCS.
 *            • Item only in A → .diff-added in A.
 *            • Item only in B → .diff-removed in B.
 *            • Item in both but different → recurse if objects, else
 *              .diff-changed on the scalar value.
 *
 * Primitives (string, number, boolean, null) → compared by value.
 *
 * Output
 * ──────
 * { htmlA: string, htmlB: string }
 * Both strings are self-contained HTML fragments (no external dependencies)
 * ready to be bound via [innerHTML] in Angular (use bypassSecurityTrustHtml).
 */
import { Injectable } from '@angular/core';

export interface ObjectDiffResult {
  htmlA: string;
  htmlB: string;
}

// ─── Types ────────────────────────────────────────────────────────────────────

type JsonValue = string | number | boolean | null | JsonValue[] | { [key: string]: JsonValue };

type DiffStatus = 'equal' | 'added' | 'removed' | 'changed';

// Internal diff tree — one node per value, carrying status for each side.
interface DiffNode {
  /** Status as seen from pane A */
  statusA: DiffStatus;
  /** Status as seen from pane B */
  statusB: DiffStatus;
  /** Scalar value for A side (primitives) */
  valueA?: JsonValue;
  /** Scalar value for B side (primitives) */
  valueB?: JsonValue;
  /** For objects: ordered list of [key, childNode] */
  objectEntries?: [string, DiffNode][];
  /** For arrays: ordered list of child nodes */
  arrayItems?: DiffNode[];
  /** Node kind */
  kind: 'primitive' | 'object' | 'array';
}

// ─── LCS for arrays ───────────────────────────────────────────────────────────

function lcsIndices(a: JsonValue[], b: JsonValue[]): [number, number][] {
  const m = a.length,
    n = b.length;
  const dp: number[][] = Array.from({ length: m + 1 }, () => new Array(n + 1).fill(0));
  for (let i = m - 1; i >= 0; i--)
    for (let j = n - 1; j >= 0; j--) dp[i][j] = deepEqual(a[i], b[j]) ? dp[i + 1][j + 1] + 1 : Math.max(dp[i + 1][j], dp[i][j + 1]);

  const pairs: [number, number][] = [];
  let i = 0,
    j = 0;
  while (i < m && j < n) {
    if (deepEqual(a[i], b[j])) {
      pairs.push([i, j]);
      i++;
      j++;
    } else if (dp[i + 1][j] >= dp[i][j + 1]) i++;
    else j++;
  }
  return pairs;
}

// ─── Deep equality ────────────────────────────────────────────────────────────

function deepEqual(a: JsonValue, b: JsonValue): boolean {
  return JSON.stringify(a) === JSON.stringify(b);
}

// ─── Core diff engine ─────────────────────────────────────────────────────────

function diffValues(a: JsonValue, b: JsonValue): DiffNode {
  // Both primitives (or one side is primitive)
  if (!isObject(a) && !isArray(a) && !isObject(b) && !isArray(b)) {
    const equal = deepEqual(a, b);
    return {
      kind: 'primitive',
      statusA: equal ? 'equal' : 'changed',
      statusB: equal ? 'equal' : 'changed',
      valueA: a,
      valueB: b
    };
  }

  // Type mismatch (e.g. array vs object) → treat as changed scalars
  if (isArray(a) !== isArray(b) || isObject(a) !== isObject(b)) {
    return {
      kind: 'primitive',
      statusA: 'changed',
      statusB: 'changed',
      valueA: a,
      valueB: b
    };
  }

  // Both arrays
  if (isArray(a) && isArray(b)) {
    return diffArrays(a as JsonValue[], b as JsonValue[]);
  }

  // Both objects
  if (isObject(a) && isObject(b)) {
    return diffObjects(a as Record<string, JsonValue>, b as Record<string, JsonValue>);
  }

  // Fallback
  return { kind: 'primitive', statusA: 'equal', statusB: 'equal', valueA: a, valueB: b };
}

function diffObjects(a: Record<string, JsonValue>, b: Record<string, JsonValue>): DiffNode {
  // Preserve insertion order: keys from A first, then new keys from B.
  const allKeys = [...new Set([...Object.keys(a), ...Object.keys(b)])];
  const entries: [string, DiffNode][] = [];

  for (const key of allKeys) {
    const inA = Object.prototype.hasOwnProperty.call(a, key);
    const inB = Object.prototype.hasOwnProperty.call(b, key);

    if (inA && inB) {
      entries.push([key, diffValues(a[key], b[key])]);
    } else if (inA) {
      entries.push([key, makeAdded(a[key])]);
    } else {
      entries.push([key, makeRemoved(b[key])]);
    }
  }

  // Parent status: 'equal' only if all children are equal
  const anyChanged = entries.some(([, n]) => n.statusA !== 'equal' || n.statusB !== 'equal');
  return {
    kind: 'object',
    statusA: anyChanged ? 'changed' : 'equal',
    statusB: anyChanged ? 'changed' : 'equal',
    objectEntries: entries
  };
}

function diffArrays(a: JsonValue[], b: JsonValue[]): DiffNode {
  const matched = lcsIndices(a, b);
  const matchedA = new Set(matched.map(([i]) => i));
  const matchedB = new Set(matched.map(([, j]) => j));

  // Build ordered item list by merging A and B sequences
  const items: DiffNode[] = [];

  // We'll walk through matched pairs and fill gaps
  let prevAi = -1,
    prevBj = -1;
  for (const [ai, bj] of [...matched, [a.length, b.length]]) {
    // Unmatched A items (deleted from B)
    for (let i = prevAi + 1; i < ai; i++) {
      if (!matchedA.has(i)) items.push(makeAdded(a[i]));
    }
    // Unmatched B items (inserted in B)
    for (let j = prevBj + 1; j < bj; j++) {
      if (!matchedB.has(j)) items.push(makeRemoved(b[j]));
    }
    // Matched pair
    if (ai < a.length && bj < b.length) {
      items.push(diffValues(a[ai], b[bj]));
    }
    prevAi = ai;
    prevBj = bj;
  }

  const anyChanged = items.some((n) => n.statusA !== 'equal' || n.statusB !== 'equal');
  return {
    kind: 'array',
    statusA: anyChanged ? 'changed' : 'equal',
    statusB: anyChanged ? 'changed' : 'equal',
    arrayItems: items
  };
}

/** Wrap a value (and all its descendants) as existing only in A.
 *  statusA='added'   → green in pane A.
 *  statusB='removed' → hidden in pane B (node doesn't exist in B).
 */
function makeAdded(value: JsonValue): DiffNode {
  return wrapEntire(value, 'added', 'removed');
}

/** Wrap a value (and all its descendants) as existing only in B.
 *  statusA='removed' → hidden in pane A (node doesn't exist in A).
 *  statusB='added'   → red in pane B.
 */
function makeRemoved(value: JsonValue): DiffNode {
  return wrapEntire(value, 'removed', 'added');
}

function wrapEntire(value: JsonValue, statusA: DiffStatus, statusB: DiffStatus): DiffNode {
  if (isArray(value)) {
    return {
      kind: 'array',
      statusA,
      statusB,
      arrayItems: (value as JsonValue[]).map((v) => wrapEntire(v, statusA, statusB))
    };
  }
  if (isObject(value)) {
    return {
      kind: 'object',
      statusA,
      statusB,
      objectEntries: Object.entries(value as Record<string, JsonValue>).map(([k, v]) => [k, wrapEntire(v, statusA, statusB)])
    };
  }
  return { kind: 'primitive', statusA, statusB, valueA: value, valueB: value };
}

// ─── HTML renderer ────────────────────────────────────────────────────────────
//
// Highlight rules (token-only — indentation is NEVER coloured):
//
// Pane A:
//   • Key   added   (only in A)          → diff-added   on the key token
//   • Key   changed (exists in B, differs) → diff-changed on the key token
//   • Value added   (primitive, only in A) → diff-added   on the value token
//   • Value changed (primitive, differs)   → diff-changed on the value token
//   • "removed" nodes are hidden (they don't exist in A)
//
// Pane B:
//   • Key   removed (only in B)          → diff-removed on the key token
//   • Value removed (primitive, only in B)→ diff-removed on the value token
//   • No yellow/green ever appear in B
//   • "added" nodes are hidden (they don't exist in B)
//
// For object/array nodes that are added/removed en bloc, each descendant
// key and primitive value inherits the added/removed status recursively
// (already encoded in the DiffNode tree by wrapEntire).

type Side = 'A' | 'B';
const INDENT = 2; // spaces per depth level

/**
 * Returns the CSS class to apply to a KEY token on the given side.
 *
 *  A | added   → diff-added    (key only in A)
 *  A | changed → diff-changed  (key in both, something inside differs)
 *  B | added   → diff-removed  (key only in B — statusB='added' = B-only)
 */
function keyClass(status: DiffStatus, side: Side): string {
  if (side === 'A') {
    if (status === 'added') return 'diff-added';
    if (status === 'changed') return 'diff-changed';
  } else {
    // statusB='added' means this node exists only in B → show red
    if (status === 'added') return 'diff-removed';
  }
  return '';
}

/**
 * Returns the CSS class to apply to a SCALAR VALUE token on the given side.
 *
 *  A | added   → diff-added
 *  A | changed → diff-changed
 *  B | added   → diff-removed  (B-only value)
 */
function valueClass(status: DiffStatus, side: Side): string {
  if (side === 'A') {
    if (status === 'added') return 'diff-added';
    if (status === 'changed') return 'diff-changed';
  } else {
    if (status === 'added') return 'diff-removed';
  }
  return '';
}

function spanWrap(content: string, cls: string): string {
  return cls ? `<span class="${cls}">${content}</span>` : content;
}

function renderNode(node: DiffNode, side: Side, depth: number): string | null {
  const status = side === 'A' ? node.statusA : node.statusB;
  const indent = ' '.repeat(depth * INDENT);
  const indentChild = ' '.repeat((depth + 1) * INDENT);

  // ── Visibility gate ─────────────────────────────────────────────────────────
  // statusA='removed' → node only exists in B → omit from pane A
  // statusB='removed' → node only exists in A → omit from pane B
  if (side === 'A' && status === 'removed') return null;
  if (side === 'B' && status === 'removed') return null;

  // ── Primitive ────────────────────────────────────────────────────────────────
  if (node.kind === 'primitive') {
    const value = side === 'A' ? node.valueA : node.valueB;
    const cls = valueClass(status, side);
    return spanWrap(formatPrimitive(value), cls);
  }

  // ── Object ───────────────────────────────────────────────────────────────────
  if (node.kind === 'object') {
    const entries = node.objectEntries ?? [];
    if (entries.length === 0) return '{}';

    const lines: string[] = ['{'];

    for (const [key, child] of entries) {
      const childRendered = renderNode(child, side, depth + 1);
      if (childRendered === null) continue; // hidden on this side

      const childStatus = side === 'A' ? child.statusA : child.statusB;
      const comma = ''; // commas added after the loop on visible items

      const kCls = keyClass(childStatus, side);
      const keyHtml = spanWrap(`"${escHtml(key)}"`, kCls);

      lines.push(`${indentChild}${keyHtml}: ${childRendered}`);
    }

    // Add commas to all lines except the last entry line and the closing brace
    const result = addTrailingCommas(lines);
    result.push(`${indent}}`);
    return result.join('\n');
  }

  // ── Array ────────────────────────────────────────────────────────────────────
  if (node.kind === 'array') {
    const items = node.arrayItems ?? [];
    if (items.length === 0) return '[]';

    const lines: string[] = ['['];

    for (const child of items) {
      const childRendered = renderNode(child, side, depth + 1);
      if (childRendered === null) continue;

      const childStatus = side === 'A' ? child.statusA : child.statusB;
      const vCls = valueClass(childStatus, side);

      // For array items that are primitives, the span is already applied by
      // renderNode above.  For object/array items we don't add an extra span —
      // individual tokens inside are already annotated recursively.
      lines.push(`${indentChild}${childRendered}`);
    }

    const result = addTrailingCommas(lines);
    result.push(`${indent}]`);
    return result.join('\n');
  }

  return '';
}

/**
 * Given a lines array starting with the opening bracket/brace line, adds a
 * trailing comma to every entry line except the last one.
 * Returns the lines WITHOUT the closing bracket (caller appends it).
 */
function addTrailingCommas(lines: string[]): string[] {
  // lines[0] is the opening '{' or '[' — skip it.
  // Lines[1..n] are entry lines — add comma to all but the last.
  const entries = lines.slice(1);
  return [lines[0], ...entries.map((line, i) => (i < entries.length - 1 ? line + ',' : line))];
}

function formatPrimitive(v: JsonValue | undefined): string {
  if (v === null) return 'null';
  if (typeof v === 'string') return `"${v}"`;
  return String(v);
}

function escHtml(s: string): string {
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

function isArray(v: JsonValue): boolean {
  return Array.isArray(v);
}
function isObject(v: JsonValue): boolean {
  return v !== null && typeof v === 'object' && !Array.isArray(v);
}

// ─── Public service ───────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class ObjectDiffService {
  /**
   * Diff two JSON-serialisable objects.
   * Returns two HTML strings (one per side) ready for [innerHTML] binding.
   *
   * Wrap with <pre> for monospaced display, e.g.:
   *   <pre class="json-diff" [innerHTML]="result.htmlA"></pre>
   */
  diff(objA: unknown, objB: unknown): ObjectDiffResult {
    const a = objA as JsonValue;
    const b = objB as JsonValue;
    const tree = diffValues(a, b);

    return {
      htmlA: renderNode(tree, 'A', 0) ?? '',
      htmlB: renderNode(tree, 'B', 0) ?? ''
    };
  }
}

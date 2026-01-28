/**
 * markdown-diff.worker.ts
 *
 * Web Worker — tout le calcul de diff se fait dans ce thread séparé.
 *
 * Protocol (structured-clone, strings uniquement) :
 *   Request  (main → worker) : { id: string; textA: string; textB: string }
 *   Response (worker → main) : { id: string; textA: string; textB: string }
 *                            | { id: string; error: string }
 */

/// <reference lib="webworker" />

const TOKEN_RE = new RegExp(
  [
    '`{3}[^\\n]*\\n[\\s\\S]*?`{3}',
    '~{3}[^\\n]*\\n[\\s\\S]*?~{3}',
    '`[^`\\n]+`',
    '\\*{3}|_{3}',
    '\\*{2}|_{2}',
    '\\*|_',
    '~~',
    '!\\[.*?\\]\\(.*?\\)',
    '\\[.*?\\]\\(.*?\\)',
    '^#{1,6}\\s',
    '&[a-zA-Z]+;|&#\\d+;',
    '\\n',
    "[\\wÀ-ÿ'\u2019-]+",
    '.'
  ].join('|'),
  'gm'
);

function tokenise(text: string): string[] {
  return text.match(TOKEN_RE) ?? [];
}

type DiffOp = { type: 'equal' | 'insert' | 'delete'; tokens: string[] };

function myersDiff(a: string[], b: string[]): DiffOp[] {
  const m = a.length,
    n = b.length;
  const dp = Array.from({ length: m + 1 }, () => new Array(n + 1).fill(0));
  for (let i = m - 1; i >= 0; i--)
    for (let j = n - 1; j >= 0; j--) dp[i][j] = a[i] === b[j] ? dp[i + 1][j + 1] + 1 : Math.max(dp[i + 1][j], dp[i][j + 1]);

  const ops: DiffOp[] = [];
  let i = 0,
    j = 0;
  const push = (type: DiffOp['type'], token: string) => {
    const last = ops[ops.length - 1];
    last && last.type === type ? last.tokens.push(token) : ops.push({ type, tokens: [token] });
  };
  while (i < m || j < n) {
    if (i < m && j < n && a[i] === b[j]) {
      push('equal', a[i]);
      i++;
      j++;
    } else if (j < n && (i >= m || dp[i][j + 1] >= dp[i + 1][j])) {
      push('insert', b[j]);
      j++;
    } else {
      push('delete', a[i]);
      i++;
    }
  }
  return ops;
}

const CODE_BLOCK_RE = /^(`{3}|~{3})/;

function buildSide(ops: DiffOp[], side: 'delete' | 'insert'): string {
  const cls = side === 'delete' ? 'added' : 'removed';
  const flat: { token: string; changed: boolean }[] = [];
  for (const op of ops) {
    if (op.type === 'equal') for (const t of op.tokens) flat.push({ token: t, changed: false });
    else if (op.type === side) for (const t of op.tokens) flat.push({ token: t, changed: true });
  }

  let out = '',
    i = 0;
  while (i < flat.length) {
    const { token, changed } = flat[i];
    if (CODE_BLOCK_RE.test(token)) {
      out += token;
      i++;
      continue;
    }
    if (!changed) {
      out += token;
      i++;
      continue;
    }

    let j = i;
    while (j < flat.length && flat[j].changed && !CODE_BLOCK_RE.test(flat[j].token)) j++;

    const run = flat.slice(i, j).map((f) => f.token);
    let s = 0,
      e = run.length;
    while (s < e && /^\s+$/.test(run[s])) {
      out += run[s++];
    }
    while (e > s && /^\s+$/.test(run[e - 1])) e--;

    const inner = run.slice(s, e);
    out += inner.length && inner.some((t) => !/^\s+$/.test(t)) ? `<span class="${cls}">${inner.join('')}</span>` : inner.join('');
    for (let k = e; k < run.length; k++) out += run[k];
    i = j;
  }
  return out;
}

addEventListener('message', ({ data }) => {
  const { id, textA, textB } = data as { id: string; textA: string; textB: string };
  try {
    const ops = myersDiff(tokenise(textA), tokenise(textB));
    postMessage({ id, textA: buildSide(ops, 'delete'), textB: buildSide(ops, 'insert') });
  } catch (err) {
    postMessage({ id, error: err instanceof Error ? err.message : String(err) });
  }
});

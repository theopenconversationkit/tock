#   Copyright (C) 2025-2026 Credit Mutuel Arkea
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#
"""Provider-agnostic helpers to split oversized documents into bounded windows.

A cross-encoder reranker served by vLLM rejects any (query + document) pair
longer than the model context. Oversized chunks (e.g. a large table the
ingestion tool never splits) therefore make the whole request fail. These pure
functions flatten such a chunk in a format-agnostic way and cut it into
"windows" whose length is guaranteed to stay within a character budget, so each
window can be scored separately and the scores mapped back to the parent.

The budget is expressed in characters: with the GTE (XLM-R SentencePiece)
tokenizer one token always covers at least one character, so a character budget
is a safe upper bound on the token count without needing the tokenizer itself.
"""
import html
import re

# Block-level markup becomes a line break...
_TAG_NEWLINE = re.compile(r'</?(?:tr|br|p|div|li|h[1-6])\b[^>]*>', re.IGNORECASE)
# ...table cells become a column separator...
_TAG_PIPE = re.compile(r'</?(?:td|th)\b[^>]*>', re.IGNORECASE)
# ...any remaining tag (even broken/unclosed) becomes a space.
_TAG_ANY = re.compile(r'<[^>]*>')
# Markdown table rule lines, e.g. |---|:--:|
_MD_RULE = re.compile(r'^\s*\|?\s*:?-{2,}:?\s*(?:\|\s*:?-{2,}:?\s*)*\|?\s*$', re.MULTILINE)
_SPACES = re.compile(r'[ \t]+')
_NEWLINES = re.compile(r'\n{3,}')


def flatten(text: str) -> str:
    """Normalize possibly-broken markup into plain, newline-separated text.

    Regex-only and tolerant of invalid HTML: it never parses a DOM, so unclosed
    or nested tags degrade gracefully instead of raising.
    """
    text = _TAG_NEWLINE.sub('\n', text)
    text = _TAG_PIPE.sub('|', text)
    text = _TAG_ANY.sub(' ', text)
    text = html.unescape(text)
    text = _MD_RULE.sub('', text)
    text = _SPACES.sub(' ', text)
    text = _NEWLINES.sub('\n\n', text)
    return text.strip()


def cut_at_boundary(text: str, limit: int) -> str:
    """Return a non-empty prefix of ``text`` no longer than ``limit`` chars.

    Prefer cutting on the last newline, else the last space, else hard-cut at
    ``limit``. The ``> 0`` guards guarantee a non-empty result so callers that
    loop on the remainder always make progress.
    """
    if len(text) <= limit:
        return text
    window = text[:limit]
    newline = window.rfind('\n')
    if newline > 0:
        return window[:newline]
    space = window.rfind(' ')
    if space > 0:
        return window[:space]
    return window


def pack_lines(body: str, piece_budget: int) -> list[str]:
    """Greedily pack ``body`` lines into pieces of at most ``piece_budget`` chars.

    A single line longer than the budget is hard-split (on space boundaries when
    possible).
    """
    pieces: list[str] = []
    current: list[str] = []
    current_len = 0

    def flush():
        nonlocal current, current_len
        if current:
            pieces.append('\n'.join(current))
            current = []
            current_len = 0

    for line in body.split('\n'):
        if len(line) > piece_budget:
            flush()
            remaining = line
            while remaining:
                chunk = cut_at_boundary(remaining, piece_budget)
                pieces.append(chunk)
                remaining = remaining[len(chunk):].lstrip(' ')
            continue
        # +1 accounts for the '\n' joined before this line
        added_len = len(line) + (1 if current else 0)
        if current and current_len + added_len > piece_budget:
            flush()
            current = [line]
            current_len = len(line)
        else:
            current.append(line)
            current_len += added_len
    flush()
    return pieces


def make_windows(text: str, budget: int) -> list[str]:
    """Split ``text`` into windows each guaranteed ``len(window) <= budget``.

    If the flattened text already fits the budget it is returned as a single
    window. Otherwise every window is ``head + "\\n" + piece`` where ``head`` is
    a shared prefix (title/column headers) so each row keeps its context.
    """
    flat = flatten(text)
    if len(flat) <= budget:
        return [flat]

    head = cut_at_boundary(flat, min(500, budget // 5))
    body = flat[len(head):].lstrip('\n')
    piece_budget = budget - len(head) - 1

    windows = [head + '\n' + piece for piece in pack_lines(body, piece_budget)]

    # Final hard guarantee, whatever happened above.
    return [w if len(w) <= budget else w[:budget] for w in windows]

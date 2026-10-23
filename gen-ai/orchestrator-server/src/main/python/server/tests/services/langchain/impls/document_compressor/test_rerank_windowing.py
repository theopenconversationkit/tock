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
import random
import string

from gen_ai_orchestrator.services.langchain.impls.document_compressor.rerank_windowing import (
    cut_at_boundary,
    flatten,
    make_windows,
    pack_lines,
)

BUDGET = 400


def _head_of(windows: list[str]) -> str:
    return windows[0].split('\n', 1)[0]


def test_short_text_returned_unchanged_single_window():
    text = 'A short chunk that easily fits the budget.'
    windows = make_windows(text, BUDGET)
    assert windows == [text]


def test_flatten_is_format_agnostic_on_broken_html():
    text = '<table><tr><td>a</td><td>b</tr><tr><td>c<td>d</table>'
    flat = flatten(text)
    assert '<' not in flat and '>' not in flat
    assert '|' in flat


def test_flatten_unescapes_entities_and_drops_markdown_rule():
    assert '&' in flatten('a &amp; b')
    flat = flatten('| h1 | h2 |\n|---|:--:|\n| v1 | v2 |')
    assert '---' not in flat


def _assert_windows_valid(windows, budget, expected_head):
    assert len(windows) > 1
    for window in windows:
        assert len(window) <= budget
        assert window.startswith(expected_head)


def test_oversized_valid_html_table_windows_within_budget():
    rows = ''.join(f'<tr><td>row {i} col a</td><td>row {i} col b</td></tr>' for i in range(200))
    text = f'<table><tr><th>Header A</th><th>Header B</th></tr>{rows}</table>'
    windows = make_windows(text, BUDGET)
    _assert_windows_valid(windows, BUDGET, _head_of(windows))


def test_oversized_broken_html_table_windows_within_budget():
    # Missing </tr>, unclosed tags, nested table.
    rows = ''.join(f'<tr><td>row {i}<td>val {i}' for i in range(200))
    text = f'<table><tr><th>Title<table><tr><td>nested{rows}'
    windows = make_windows(text, BUDGET)
    _assert_windows_valid(windows, BUDGET, _head_of(windows))


def test_oversized_markdown_table_windows_within_budget():
    header = '| Name | Value |\n|------|-------|\n'
    rows = ''.join(f'| name {i} | value {i} |\n' for i in range(200))
    windows = make_windows(header + rows, BUDGET)
    _assert_windows_valid(windows, BUDGET, _head_of(windows))


def test_oversized_csv_like_text_windows_within_budget():
    text = '\n'.join(f'name {i},value {i},extra column {i}' for i in range(200))
    windows = make_windows(text, BUDGET)
    _assert_windows_valid(windows, BUDGET, _head_of(windows))


def test_single_line_longer_than_budget_is_hard_split():
    pieces = pack_lines('x' * 2000, 100)
    assert len(pieces) > 1
    for piece in pieces:
        assert len(piece) <= 100
    assert ''.join(pieces) == 'x' * 2000


def test_make_windows_on_single_huge_wordless_line():
    windows = make_windows('y' * 5000, BUDGET)
    for window in windows:
        assert len(window) <= BUDGET


def test_cut_at_boundary_prefers_newline_then_space():
    assert cut_at_boundary('abc\ndef ghi', 7) == 'abc'
    assert cut_at_boundary('abcde fghij', 8) == 'abcde'
    assert cut_at_boundary('abcdefgh', 4) == 'abcd'


def test_property_no_window_ever_exceeds_budget():
    alphabet = string.ascii_letters + string.digits + ' \n\t<>/|&;-:,.'
    rng = random.Random(1234)
    for _ in range(50):
        budget = rng.randint(50, 600)
        length = rng.randint(0, 4000)
        text = ''.join(rng.choice(alphabet) for _ in range(length))
        for window in make_windows(text, budget):
            assert len(window) <= budget

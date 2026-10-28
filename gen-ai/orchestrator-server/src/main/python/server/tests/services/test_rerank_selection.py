#   Copyright (C) 2026 Credit Mutuel Arkea
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
"""Cases for the shared reranker selection.

The front mirror (bot/admin/web, rerank-selection.spec.ts) duplicates these same
cases verbatim — a shared JSON fixture across the Python and Angular build trees
is impractical, so keep the two suites in sync by hand.
"""

import pytest
from langchain_core.documents import Document

from gen_ai_orchestrator.services.langchain.impls.document_compressor.rerank_selection import (
    select_reranked_documents,
)


def _docs(scores: list[tuple[str, float | None]]) -> list[Document]:
    documents = []
    for name, score in scores:
        metadata = {'id': name}
        if score is not None:
            metadata['reranker_score'] = score
        documents.append(Document(page_content=name, metadata=metadata))
    return documents


# (name, scores, min_score, max_documents, fill, expected_ids)
CASES = [
    (
        'all_above_under_max',
        [('a', 0.9), ('b', 0.8), ('c', 0.7)],
        0.5,
        5,
        False,
        ['a', 'b', 'c'],
    ),
    (
        'above_over_max',
        [('a', 0.9), ('b', 0.8), ('c', 0.7), ('d', 0.6)],
        0.5,
        2,
        False,
        ['a', 'b'],
    ),
    (
        'below_no_fill',
        [('a', 0.9), ('b', 0.3), ('c', 0.2)],
        0.5,
        3,
        False,
        ['a'],
    ),
    (
        'below_with_fill',
        [('a', 0.9), ('b', 0.3), ('c', 0.2)],
        0.5,
        3,
        True,
        ['a', 'b', 'c'],
    ),
    (
        'fill_partial',
        [('a', 0.9), ('b', 0.3), ('c', 0.2), ('d', 0.1)],
        0.5,
        2,
        True,
        ['a', 'b'],
    ),
    (
        'unsorted_input',
        [('a', 0.2), ('b', 0.95), ('c', 0.6)],
        0.5,
        5,
        False,
        ['b', 'c'],
    ),
    (
        'missing_score_as_zero',
        [('a', 0.8), ('b', None)],
        0.5,
        5,
        False,
        ['a'],
    ),
    (
        'ties_keep_input_order',
        [('a', 0.5), ('b', 0.5)],
        0.5,
        5,
        False,
        ['a', 'b'],
    ),
]


@pytest.mark.parametrize(
    'scores,min_score,max_documents,fill,expected',
    [(c[1], c[2], c[3], c[4], c[5]) for c in CASES],
    ids=[c[0] for c in CASES],
)
def test_select_reranked_documents(scores, min_score, max_documents, fill, expected):
    selected = select_reranked_documents(
        _docs(scores),
        min_score=min_score,
        max_documents=max_documents,
        fill_to_max_documents=fill,
    )
    assert [doc.metadata['id'] for doc in selected] == expected

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
"""Shared reranker selection, used identically by every compressor provider.

MIRROR — keep in sync with the studio front:
``bot/admin/web/src/app/vector-store-inspection/utils/rerank-selection.ts``
reproduces this exact selection so the diagnostic can recompute outcomes when the
thresholds change without calling the backend. Any change to the selection logic
here MUST be mirrored there, and the shared cases MUST be updated on both sides
(``tests/services/test_rerank_selection.py`` and its ``rerank-selection.spec.ts``
counterpart).
"""

from typing import Sequence

from langchain_core.documents import Document


def select_reranked_documents(
    scored_docs: Sequence[Document],
    min_score: float,
    max_documents: int,
    fill_to_max_documents: bool,
) -> list[Document]:
    """Select the documents a reranker keeps from its scored candidates.

    Sort by ``reranker_score`` descending (missing score treated as 0, ties keep
    input order via the stable sort — providers hand ``scored_docs`` over in
    retrieval order, so ties go to the better retrieved candidate), keep those at or above ``min_score`` up to
    ``max_documents``, then — when ``fill_to_max_documents`` is set and the result
    is still short — complete it with the best below-threshold documents.
    """
    ranked = sorted(
        scored_docs,
        key=lambda doc: doc.metadata.get('reranker_score', 0),
        reverse=True,
    )
    above_threshold = [
        doc for doc in ranked if doc.metadata.get('reranker_score', 0) >= min_score
    ]
    below_threshold = [
        doc for doc in ranked if doc.metadata.get('reranker_score', 0) < min_score
    ]

    result = above_threshold[:max_documents]
    if fill_to_max_documents and len(result) < max_documents:
        result.extend(below_threshold[: max_documents - len(result)])

    return result

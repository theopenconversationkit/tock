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
"""The scored retriever must return exactly what the default retriever returns."""

from unittest.mock import MagicMock

import pytest
from langchain_core.documents import Document
from langchain_core.embeddings import DeterministicFakeEmbedding
from langchain_core.vectorstores import InMemoryVectorStore

from gen_ai_orchestrator.services.langchain.factories.vector_stores.pgvector_factory import (
    VECTOR_SCORE_METADATA_KEY,
    PGVectorFactory,
    ScoredVectorStoreRetriever,
)
from gen_ai_orchestrator.services.langchain.factories.vector_stores.postgresql_text_retriever import (
    FTS_SCORE_METADATA_KEY,
)
from gen_ai_orchestrator.services.langchain.rag_response_builder import (
    _footnote_metadata,
)


def _store() -> InMemoryVectorStore:
    store = InMemoryVectorStore(embedding=DeterministicFakeEmbedding(size=16))
    store.add_documents(
        [
            Document(page_content=f'chunk {i}', metadata={'id': str(i), 'group': i % 2})
            for i in range(8)
        ]
    )
    return store


def _ids(documents: list[Document]) -> list[str]:
    return [document.metadata['id'] for document in documents]


@pytest.mark.parametrize(
    'search_kwargs',
    [
        {'k': 3},
        {'k': 5, 'filter': lambda document: document.metadata['group'] == 0},
    ],
)
def test_same_documents_and_order_as_default_retriever(search_kwargs):
    store = _store()
    expected = store.as_retriever(search_kwargs=search_kwargs).invoke('chunk 3')
    scored = ScoredVectorStoreRetriever(
        vectorstore=store, search_kwargs=search_kwargs
    ).invoke('chunk 3')

    assert _ids(scored) == _ids(expected)
    assert all(VECTOR_SCORE_METADATA_KEY in document.metadata for document in scored)
    if 'filter' in search_kwargs:
        assert all(document.metadata['group'] == 0 for document in scored)


@pytest.mark.asyncio
async def test_async_path_matches_sync_path_and_scores_follow_order():
    store = _store()
    retriever = ScoredVectorStoreRetriever(vectorstore=store, search_kwargs={'k': 4})

    sync_documents = retriever.invoke('chunk 1')
    async_documents = await retriever.ainvoke('chunk 1')

    assert _ids(async_documents) == _ids(sync_documents)
    # InMemoryVectorStore returns a similarity (higher is closer) where PGVector
    # returns a distance, so 1 - score increases along the order here: only the
    # monotonic relation with the order is asserted.
    scores = [
        document.metadata[VECTOR_SCORE_METADATA_KEY] for document in sync_documents
    ]
    assert scores == sorted(scores)


def test_factory_retriever_keeps_the_default_run_name():
    factory = MagicMock(spec=PGVectorFactory)
    factory.get_vector_store.return_value = _store()
    retriever = PGVectorFactory.get_vector_store_retriever(
        factory, search_kwargs={'k': 2}
    )

    assert isinstance(retriever, ScoredVectorStoreRetriever)
    # Same span name as the default retriever in Langfuse traces.
    assert retriever.get_name() == 'VectorStoreRetriever'


def test_footnotes_do_not_carry_the_inspection_scores():
    document = Document(
        page_content='text',
        metadata={
            'id': 'doc',
            'title': 'Doc',
            'reranker_score': 0.8,
            VECTOR_SCORE_METADATA_KEY: 0.9,
            FTS_SCORE_METADATA_KEY: 0.3,
        },
    )

    assert _footnote_metadata(document) == {
        'id': 'doc',
        'title': 'Doc',
        'reranker_score': 0.8,
    }
    # The document itself keeps them for the inspection.
    assert VECTOR_SCORE_METADATA_KEY in document.metadata

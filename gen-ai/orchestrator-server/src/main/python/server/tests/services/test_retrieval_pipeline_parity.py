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
"""Parity between the runtime RAG chain and the vector store inspection.

Both consume the same shared retrieval/compression pipeline. These tests feed
both entry points (``create_rag_chain`` and the inspection ``search``) the same
fake retrievers and the same fake compressor, then assert that the documents
reaching the answer step in the runtime are exactly those labelled kept /
filled_below_threshold by the diagnostic, in the same order — for similarity,
full-text and hybrid search, with and without a compressor, and on a compressor
fallback.
"""

from unittest.mock import AsyncMock, MagicMock, patch

import pytest
from langchain_core.documents import Document
from langchain_core.runnables import RunnableLambda

from gen_ai_orchestrator.models.vector_stores.vector_store_search_type import (
    DocumentSearchType,
)
from gen_ai_orchestrator.routers.requests.requests import RAGRequest
from gen_ai_orchestrator.routers.requests.vector_store_inspection_requests import (
    VectorStoreInspectionSearchRequest,
)
from gen_ai_orchestrator.routers.responses.vector_store_inspection_responses import (
    ChunkOutcome,
)
from gen_ai_orchestrator.services.langchain.rag_chain_builder import (
    create_rag_chain,
    get_chunk_identifier,
)
from gen_ai_orchestrator.services.vector_store_inspection.vector_store_inspection_service import (
    search as inspection_search,
)

K = 3
MIN_SCORE = 0.5
# Reranker scores keyed by chunk id, shared by the runtime and the diagnostic.
RERANKER_SCORES = {
    'a:1/1': 0.30,
    'b:1/1': 0.70,
    'c:1/1': 0.95,
    'd:1/1': 0.40,
    'e:1/1': 0.60,
}
VECTOR_DOCS = [('a', 0.90), ('b', 0.80), ('c', 0.70)]
FTS_DOCS = [('c', 0.85), ('d', 0.60), ('e', 0.55)]


def _doc(name: str, retrieval_score: float) -> Document:
    return Document(
        page_content=f'content {name}',
        metadata={
            'id': name,
            'chunk': '1/1',
            'title': name,
            'retrieval_score': retrieval_score,
        },
    )


class _FakeRetriever:
    """Stands in for the scored retrievers: returns fresh scored copies."""

    def __init__(self, docs: list[tuple[str, float]]):
        self._docs = docs

    async def ainvoke(self, input=None, **kwargs) -> list[Document]:
        return [_doc(name, score) for name, score in self._docs]

    def prepare_query(self, keywords: list[str]) -> str:
        return ' OR '.join(keywords)


class _FakeCompressor:
    """Reproduces the shared compressor selection (and its fallback)."""

    def __init__(self, fill: bool, fallback: bool = False):
        self.fill = fill
        self.fallback = fallback
        self.last_fallback_info = None

    def compress_documents(self, documents, query):
        self.last_fallback_info = None
        if self.fallback:
            self.last_fallback_info = {
                'reason': 'bad_response',
                'returned_documents': min(len(documents), K),
            }
            return list(documents)[:K]
        for document in documents:
            document.metadata['reranker_score'] = RERANKER_SCORES[
                get_chunk_identifier(document)
            ]
        ranked = sorted(
            documents, key=lambda d: d.metadata['reranker_score'], reverse=True
        )
        above = [d for d in ranked if d.metadata['reranker_score'] >= MIN_SCORE]
        below = [d for d in ranked if d.metadata['reranker_score'] < MIN_SCORE]
        result = above[:K]
        if self.fill and len(result) < K:
            result.extend(below[: K - len(result)])
        return result


def _compressor_setting(fill: bool) -> dict:
    return {
        'provider': 'BloomzRerank',
        'endpoint': 'http://localhost:8082',
        'label': 'entailment',
        'min_score': MIN_SCORE,
        'max_documents': K,
        'fill_to_max_documents': fill,
    }


def _vector_store_setting() -> dict:
    return {
        'provider': 'PGVector',
        'host': 'localhost',
        'port': 5432,
        'database': 'test',
        'username': 'test',
        'password': {'type': 'Raw', 'secret': 'test'},
    }


def _rag_request(search_type: DocumentSearchType, with_compressor: bool) -> RAGRequest:
    payload = {
        'dialog': {'history': [], 'tags': []},
        'question_condensing_llm_setting': {
            'provider': 'OpenAI',
            'api_key': {'type': 'Raw', 'secret': 'secret'},
            'temperature': 1.0,
            'model': 'gpt-4o',
        },
        'question_condensing_prompt': {
            'formatter': 'f-string',
            'template': 'condense',
            'inputs': {},
        },
        'question_answering_llm_setting': {
            'provider': 'OpenAI',
            'api_key': {'type': 'Raw', 'secret': 'secret'},
            'temperature': 1.0,
            'model': 'gpt-4o',
        },
        'question_answering_prompt': {
            'formatter': 'f-string',
            'template': 'Context: {context}',
            'inputs': {},
        },
        'embedding_question_em_setting': {
            'provider': 'OpenAI',
            'api_key': {'type': 'Raw', 'secret': 'secret'},
            'model': 'text-embedding-ada-002',
        },
        'document_index_name': 'idx',
        'document_search_params': {
            'provider': 'PGVector',
            'filter': None,
            'k': K,
            'search_type': search_type.value,
        },
        'vector_store_setting': _vector_store_setting(),
    }
    if with_compressor:
        payload['compressor_setting'] = _compressor_setting(fill=False)
    return RAGRequest(**payload)


def _inspection_request(
    search_type: DocumentSearchType, with_compressor: bool, fill: bool
) -> VectorStoreInspectionSearchRequest:
    values = {
        'vector_store_setting': _vector_store_setting(),
        'embedding_question_em_setting': {
            'provider': 'OpenAI',
            'api_key': {'type': 'Raw', 'secret': 'secret'},
            'model': 'text-embedding-ada-002',
        },
        'index_name_prefix': 'idx',
        'index_name': 'idx',
        'search_type': search_type.value,
        'query': 'q',
        'key_words': ['cancel', 'contract'],
        'k': K,
        'compression_enabled': with_compressor,
    }
    if with_compressor:
        values['compressor_setting'] = _compressor_setting(fill=fill)
        values['compression_override'] = {
            'min_score': MIN_SCORE,
            'max_documents': K,
            'fill_to_max_documents': fill,
        }
    return VectorStoreInspectionSearchRequest.model_validate(values)


def _fake_factory() -> MagicMock:
    factory = MagicMock()
    factory.get_vector_store_retriever.return_value = _FakeRetriever(VECTOR_DOCS)
    factory.get_text_store_retriever.return_value = _FakeRetriever(FTS_DOCS)
    return factory


def _em_factory() -> MagicMock:
    model = MagicMock()
    model.aembed_query = AsyncMock(return_value=[0.1, 0.2])
    factory = MagicMock()
    factory.get_embedding_model.return_value = model
    return factory


def _llm_factory(payload: str) -> MagicMock:
    factory = MagicMock()
    factory.get_language_model.return_value = RunnableLambda(lambda _x: payload)
    return factory


def _compressor_factory(compressor) -> MagicMock:
    factory = MagicMock()
    factory.get_compressor.return_value = compressor
    return factory


async def _runtime_documents(
    search_type: DocumentSearchType, compressor
) -> list[str]:
    request = _rag_request(search_type, with_compressor=compressor is not None)
    condensation = _llm_factory(
        '{"condensed_question": "q", "key_words": ["cancel", "contract"]}'
    )
    answer = _llm_factory('{}')
    builder = 'gen_ai_orchestrator.services.langchain.rag_chain_builder'
    with (
        patch(f'{builder}.get_llm_factory', side_effect=[condensation, answer]),
        patch(f'{builder}.get_em_factory', return_value=_em_factory()),
        patch(f'{builder}.get_vector_store_factory', return_value=_fake_factory()),
        patch(
            f'{builder}.get_compressor_factory',
            return_value=_compressor_factory(compressor),
        ),
    ):
        chain = create_rag_chain(request)
        result = await chain.ainvoke({'question': 'q', 'chat_history': []})
    return [get_chunk_identifier(document) for document in result['documents']]


async def _diagnostic_survivors(
    search_type: DocumentSearchType, with_compressor: bool, fill: bool, compressor
) -> list[str]:
    request = _inspection_request(search_type, with_compressor, fill)
    service = (
        'gen_ai_orchestrator.services.vector_store_inspection'
        '.vector_store_inspection_service'
    )
    with (
        patch(f'{service}.get_em_factory', return_value=_em_factory()),
        patch(f'{service}._pg_factory', return_value=_fake_factory()),
        patch(
            f'{service}.get_compressor_factory',
            return_value=_compressor_factory(compressor),
        ),
    ):
        response = await inspection_search(request)

    survivors = [
        result
        for result in response.results
        if result.outcome
        in (ChunkOutcome.KEPT, ChunkOutcome.FILLED_BELOW_THRESHOLD)
    ]
    # The runtime hands documents to the LLM in the compressor's order (reranked
    # score desc) when it ran; otherwise in retrieval order. The diagnostic lists
    # results in retrieval order with outcome labels, so reorder by compressor
    # score to recover the order the LLM actually sees.
    if any(result.scores.compressor is not None for result in survivors):
        survivors.sort(key=lambda result: result.scores.compressor, reverse=True)
    return [result.chunk_id for result in survivors]


@pytest.mark.parametrize(
    'search_type',
    [
        DocumentSearchType.SIMILARITY_SEARCH,
        DocumentSearchType.FULL_TEXT_SEARCH,
        DocumentSearchType.HYBRID_SEARCH,
    ],
)
@pytest.mark.parametrize('with_compressor', [False, True])
@pytest.mark.asyncio
async def test_runtime_and_diagnostic_agree_on_surviving_documents(
    search_type, with_compressor
):
    compressor = _FakeCompressor(fill=False) if with_compressor else None
    runtime_ids = await _runtime_documents(search_type, compressor)

    diagnostic_compressor = _FakeCompressor(fill=False) if with_compressor else None
    diagnostic_ids = await _diagnostic_survivors(
        search_type, with_compressor, fill=False, compressor=diagnostic_compressor
    )

    assert runtime_ids == diagnostic_ids
    assert set(runtime_ids) == set(diagnostic_ids)


@pytest.mark.asyncio
async def test_runtime_and_diagnostic_agree_with_fill_to_max_documents():
    runtime_ids = await _runtime_documents(
        DocumentSearchType.HYBRID_SEARCH, _FakeCompressor(fill=True)
    )
    diagnostic_ids = await _diagnostic_survivors(
        DocumentSearchType.HYBRID_SEARCH,
        with_compressor=True,
        fill=True,
        compressor=_FakeCompressor(fill=True),
    )

    assert runtime_ids == diagnostic_ids


@pytest.mark.asyncio
async def test_runtime_and_diagnostic_agree_on_compressor_fallback():
    runtime_ids = await _runtime_documents(
        DocumentSearchType.HYBRID_SEARCH, _FakeCompressor(fill=False, fallback=True)
    )
    diagnostic_ids = await _diagnostic_survivors(
        DocumentSearchType.HYBRID_SEARCH,
        with_compressor=True,
        fill=False,
        compressor=_FakeCompressor(fill=False, fallback=True),
    )

    assert runtime_ids == diagnostic_ids

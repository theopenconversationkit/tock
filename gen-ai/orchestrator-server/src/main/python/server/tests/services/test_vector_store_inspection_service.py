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
from unittest.mock import AsyncMock, MagicMock, patch

import pytest
from langchain_core.documents import Document
from pydantic import ValidationError

from gen_ai_orchestrator.routers.requests.vector_store_inspection_requests import (
    VectorStoreInspectionSearchRequest,
)
from gen_ai_orchestrator.routers.responses.vector_store_inspection_responses import (
    ChunkOutcome,
    FunnelStageStatus,
)
from gen_ai_orchestrator.services.langchain.rag_chain_builder import (
    calculate_rrf_scores,
)
from gen_ai_orchestrator.services.vector_store_inspection.vector_store_inspection_service import (
    Candidate,
    _compress,
    _fallback_detail,
    _fallback_reason,
    search,
)


def _search_request(**updates) -> VectorStoreInspectionSearchRequest:
    values = {
        'vector_store_setting': {'provider': 'PGVector', 'host': 'localhost', 'port': 5432,
                                 'database': 'test', 'username': 'test', 'password': {'type': 'Raw', 'secret': 'test'}},
        'embedding_question_em_setting': {
            'provider': 'OpenAI',
            'api_key': {'type': 'Raw', 'secret': 'test'},
            'model': 'text-embedding-test',
        },
        'index_name_prefix': 'ns_test_bot_test_session_',
        'index_name': 'ns_test_bot_test_session_one',
        'search_type': 'HYBRID_SEARCH',
        'query': 'cancel a contract',
        'key_words': ['cancel', 'contract'],
        'k': 1,
        'compression_enabled': False,
    }
    values.update(updates)
    return VectorStoreInspectionSearchRequest.model_validate(values)


def _document(document_id: str, chunk: str = '1/1') -> Document:
    title = f"Title {document_id}"
    return Document(
        page_content=f"{title}\n\nContent {document_id}",
        metadata={'id': document_id, 'chunk': chunk, 'title': title},
    )


def test_search_request_rejects_hybrid_search_without_keywords():
    with pytest.raises(ValidationError, match='key_words is required'):
        _search_request(key_words=[])


def test_search_request_requires_compressor_setting_when_enabled():
    with pytest.raises(ValidationError, match='compressor_setting is required'):
        _search_request(compression_enabled=True)


def test_calculate_rrf_scores_fuses_ranks_by_chunk_identifier():
    first = _document('first')
    second = _document('second')

    scores = calculate_rrf_scores([[first, second], [second]], k=60)

    assert scores[('first', '1/1')] == pytest.approx(1 / 61)
    assert scores[('second', '1/1')] == pytest.approx(1 / 62 + 1 / 61)


@pytest.mark.asyncio
async def test_search_exposes_channel_scores_ranks_and_top_k_outcomes():
    def _scored(name: str, key: str, score: float) -> Document:
        document = _document(name)
        document.metadata[key] = score
        return document

    embedding_model = MagicMock()
    embedding_model.aembed_query = AsyncMock(return_value=[0.1, 0.2])
    em_factory = MagicMock()
    em_factory.get_embedding_model.return_value = embedding_model

    # The shared (scored) retrievers already expose 1 - distance / ts_rank in
    # metadata['vector_score'] / metadata['fts_score']; the inspection reads
    # them back per channel.
    vector_retriever = MagicMock()
    vector_retriever.ainvoke = AsyncMock(
        return_value=[
            _scored('first', 'vector_score', 0.9),
            _scored('second', 'vector_score', 0.7),
        ]
    )
    fts_retriever = MagicMock()
    fts_retriever.prepare_query = MagicMock(return_value='cancel OR contract')
    fts_retriever.ainvoke = AsyncMock(
        return_value=[
            _scored('second', 'fts_score', 0.8),
            _scored('first', 'fts_score', 0.4),
        ]
    )
    pg_factory = MagicMock()
    pg_factory.get_vector_store_retriever.return_value = vector_retriever
    pg_factory.get_text_store_retriever.return_value = fts_retriever

    with (
        patch(
            'gen_ai_orchestrator.services.vector_store_inspection.vector_store_inspection_service.get_em_factory',
            return_value=em_factory,
        ),
        patch(
            'gen_ai_orchestrator.services.vector_store_inspection.vector_store_inspection_service._pg_factory',
            return_value=pg_factory,
        ),
    ):
        response = await search(_search_request())

    assert [result.chunk_id for result in response.results] == [
        'first:1/1',
        'second:1/1',
    ]
    assert response.results[0].ranks.vector == 1
    assert response.results[0].ranks.fts == 2
    assert response.results[0].ranks.rrf == 1
    assert response.results[0].scores.vector == pytest.approx(0.9)
    assert response.results[0].scores.fts == pytest.approx(0.4)
    assert response.results[0].outcome == ChunkOutcome.KEPT
    assert response.results[1].outcome == ChunkOutcome.CUT_BY_TOP_K
    assert response.funnel.vector.count == 2
    assert response.funnel.fts.count == 2
    assert response.funnel.rrf.count == 2
    assert response.funnel.compression.status == FunnelStageStatus.DISABLED
    assert response.funnel.top_k_cut.count == 1
    assert response.funnel.top_k_cut.discarded == 1


@pytest.mark.asyncio
async def test_compression_reports_threshold_and_fill_outcomes():
    candidates = [Candidate(_document(name)) for name in ['first', 'second', 'third']]
    compressor = MagicMock()

    def compress_documents(documents, query):
        scores = {'first': 0.2, 'second': 0.9, 'third': 0.1}
        for document in documents:
            document.metadata['reranker_score'] = scores[document.metadata['id']]
        return [documents[1], documents[0]]

    compressor.compress_documents.side_effect = compress_documents
    compressor.last_fallback_info = None
    compressor_factory = MagicMock()
    compressor_factory.get_compressor.return_value = compressor
    request = _search_request(
        compression_enabled=True,
        compressor_setting={
            'provider': 'BloomzRerank',
            'endpoint': 'http://compressor.test',
            'min_score': 0.5,
            'max_documents': 2,
            'fill_to_max_documents': True,
        },
        compression_override={
            'min_score': 0.5,
            'max_documents': 2,
            'fill_to_max_documents': True,
        },
    )

    with patch(
        'gen_ai_orchestrator.services.vector_store_inspection.vector_store_inspection_service.get_compressor_factory',
        return_value=compressor_factory,
    ):
        compressed_ids, outcomes, scores, windows, stage = await _compress(
            request, candidates
        )

    assert compressed_ids == ['second:1/1', 'first:1/1']
    assert outcomes == {
        'first:1/1': ChunkOutcome.FILLED_BELOW_THRESHOLD,
        'second:1/1': ChunkOutcome.KEPT,
        'third:1/1': ChunkOutcome.BELOW_MIN_SCORE,
    }
    assert scores == {'first:1/1': 0.2, 'second:1/1': 0.9, 'third:1/1': 0.1}
    assert stage.status == FunnelStageStatus.APPLIED
    assert stage.count == 2
    compressor_factory.get_compressor.assert_called_once_with()
    assert request.compressor_setting.min_score == 0.5


@pytest.mark.asyncio
async def test_compression_fallback_keeps_runtime_truncation_and_labels_the_rest():
    candidates = [Candidate(_document(name)) for name in ['first', 'second', 'third']]
    compressor = MagicMock()

    def compress_documents(documents, query):
        compressor.last_fallback_info = {
            'reason': 'bad_response',
            'status_code': 503,
            'error': 'Service Unavailable',
            'returned_documents': 2,
            'total_documents': 3,
        }
        return list(documents)[:2]

    compressor.compress_documents.side_effect = compress_documents
    compressor.last_fallback_info = None
    compressor_factory = MagicMock()
    compressor_factory.get_compressor.return_value = compressor
    request = _search_request(
        compression_enabled=True,
        compressor_setting={
            'provider': 'BloomzRerank',
            'endpoint': 'http://compressor.test',
            'min_score': 0.5,
            'max_documents': 2,
            'fill_to_max_documents': False,
        },
    )

    with patch(
        'gen_ai_orchestrator.services.vector_store_inspection.vector_store_inspection_service.get_compressor_factory',
        return_value=compressor_factory,
    ):
        compressed_ids, outcomes, scores, windows, stage = await _compress(
            request, candidates
        )

    assert compressed_ids == ['first:1/1', 'second:1/1']
    assert outcomes == {
        'first:1/1': ChunkOutcome.KEPT,
        'second:1/1': ChunkOutcome.KEPT,
        'third:1/1': ChunkOutcome.CUT_BY_FALLBACK,
    }
    assert scores == {}
    assert windows == {}
    assert stage.status == FunnelStageStatus.FAILED_FALLBACK
    assert stage.reason == 'bad_response'
    assert stage.detail == 'Service Unavailable'
    assert stage.count == 2


@pytest.mark.parametrize(
    'fallback_info, expected_reason, expected_detail',
    [
        (
            {'reason': 'query_too_long', 'budget': 420},
            'query_too_long',
            'budget=420 chars',
        ),
        (
            {'reason': 'exception', 'error_message': 'timed out'},
            'exception',
            'timed out',
        ),
        ({'reason': 'something_new'}, 'exception', None),
    ],
)
def test_fallback_reason_is_always_translatable(
    fallback_info, expected_reason, expected_detail
):
    assert _fallback_reason(fallback_info) == expected_reason
    assert _fallback_detail(fallback_info) == expected_detail


@pytest.mark.asyncio
async def test_compression_error_is_a_failure_not_a_fallback():
    candidates = [Candidate(_document(name)) for name in ['first', 'second']]
    compressor_factory = MagicMock()
    compressor_factory.get_compressor.side_effect = ValueError('bad compressor setting')
    request = _search_request(
        compression_enabled=True,
        compressor_setting={
            'provider': 'BloomzRerank',
            'endpoint': 'http://compressor.test',
            'min_score': 0.5,
            'max_documents': 2,
            'fill_to_max_documents': False,
        },
    )

    with patch(
        'gen_ai_orchestrator.services.vector_store_inspection.vector_store_inspection_service.get_compressor_factory',
        return_value=compressor_factory,
    ):
        compressed_ids, outcomes, scores, windows, stage = await _compress(
            request, candidates
        )

    # The runtime would fail the request: nothing is passed on.
    assert compressed_ids == []
    assert set(outcomes.values()) == {ChunkOutcome.COMPRESSION_FAILED}
    assert stage.status == FunnelStageStatus.FAILED
    assert stage.reason == 'error'
    assert stage.detail == 'bad compressor setting'
    assert stage.count == 0

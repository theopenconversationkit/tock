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
from unittest.mock import MagicMock, patch

import pytest
from langchain_core.documents import Document

from gen_ai_orchestrator.errors.exceptions.document_compressor.document_compressor_exceptions import (
    GenAIDocumentCompressorErrorException,
)
from gen_ai_orchestrator.services.langchain.impls.document_compressor.vllm_rerank import (
    VllmRerank,
)


def _make_rerank(**kwargs) -> VllmRerank:
    return VllmRerank(model='BAAI/bge-reranker-v2-m3', **kwargs)


def test_compress_documents_returns_empty_list_when_no_documents():
    rerank = _make_rerank()
    assert rerank.compress_documents(documents=[], query='question') == []


@patch('gen_ai_orchestrator.services.langchain.impls.document_compressor.vllm_rerank.requests.post')
def test_compress_documents_filters_by_min_score_and_sorts(mocked_post):
    documents = [
        Document(page_content='low relevance'),
        Document(page_content='high relevance'),
        Document(page_content='medium relevance'),
    ]
    mocked_response = MagicMock()
    mocked_response.status_code = 200
    mocked_response.json.return_value = {
        'results': [
            {'index': 0, 'relevance_score': 0.1},
            {'index': 1, 'relevance_score': 0.9},
            {'index': 2, 'relevance_score': 0.6},
        ]
    }
    mocked_post.return_value = mocked_response

    rerank = _make_rerank(min_score=0.5, max_documents=50)
    result = rerank.compress_documents(documents=documents, query='question')

    assert [doc.page_content for doc in result] == [
        'high relevance',
        'medium relevance',
    ]
    assert result[0].metadata['reranker_score'] == 0.9
    mocked_post.assert_called_once()
    call_kwargs = mocked_post.call_args[1]
    assert call_kwargs['url'].endswith('/v1/rerank')
    assert call_kwargs['json']['model'] == 'BAAI/bge-reranker-v2-m3'
    assert call_kwargs['json']['query'] == 'question'


@patch('gen_ai_orchestrator.services.langchain.impls.document_compressor.vllm_rerank.requests.post')
def test_compress_documents_truncates_to_max_documents(mocked_post):
    documents = [Document(page_content=f'doc {i}') for i in range(3)]
    mocked_response = MagicMock()
    mocked_response.status_code = 200
    mocked_response.json.return_value = {
        'results': [
            {'index': 0, 'relevance_score': 0.9},
            {'index': 1, 'relevance_score': 0.8},
            {'index': 2, 'relevance_score': 0.7},
        ]
    }
    mocked_post.return_value = mocked_response

    rerank = _make_rerank(min_score=0.5, max_documents=2)
    result = rerank.compress_documents(documents=documents, query='question')

    assert len(result) == 2
    assert [doc.page_content for doc in result] == ['doc 0', 'doc 1']


@patch('gen_ai_orchestrator.services.langchain.impls.document_compressor.vllm_rerank.requests.post')
def test_compress_documents_fills_to_max_documents_with_below_threshold(mocked_post):
    documents = [Document(page_content=f'doc {i}') for i in range(3)]
    mocked_response = MagicMock()
    mocked_response.status_code = 200
    mocked_response.json.return_value = {
        'results': [
            {'index': 0, 'relevance_score': 0.9},
            {'index': 1, 'relevance_score': 0.2},
            {'index': 2, 'relevance_score': 0.1},
        ]
    }
    mocked_post.return_value = mocked_response

    rerank = _make_rerank(min_score=0.5, max_documents=3, fill_to_max_documents=True)
    result = rerank.compress_documents(documents=documents, query='question')

    assert [doc.page_content for doc in result] == ['doc 0', 'doc 1', 'doc 2']


@patch('gen_ai_orchestrator.services.langchain.impls.document_compressor.vllm_rerank.requests.post')
def test_compress_documents_skips_out_of_bounds_indices(mocked_post):
    documents = [
        Document(page_content='doc 0'),
        Document(page_content='doc 1'),
    ]
    mocked_response = MagicMock()
    mocked_response.status_code = 200
    mocked_response.json.return_value = {
        'results': [
            {'index': 0, 'relevance_score': 0.9},
            {'index': 1, 'relevance_score': 0.8},
            {'index': 99, 'relevance_score': 0.7},  # out of bounds
        ]
    }
    mocked_post.return_value = mocked_response

    rerank = _make_rerank(min_score=0.5)
    result = rerank.compress_documents(documents=documents, query='question')

    assert len(result) == 2
    assert [doc.page_content for doc in result] == ['doc 0', 'doc 1']


@patch('gen_ai_orchestrator.services.langchain.impls.document_compressor.vllm_rerank.requests.post')
def test_compress_documents_fault_tolerant_falls_back_on_http_error(mocked_post):
    documents = [Document(page_content='doc')]
    mocked_response = MagicMock()
    mocked_response.status_code = 500
    mocked_response.reason = 'Internal Server Error'
    mocked_response.text = 'Server error'
    mocked_post.return_value = mocked_response

    rerank = _make_rerank(is_fault_tolerant=True)
    result = rerank.compress_documents(documents=documents, query='question')

    assert result == documents


@patch('gen_ai_orchestrator.services.langchain.impls.document_compressor.vllm_rerank.requests.post')
def test_compress_documents_not_fault_tolerant_raises_on_http_error(mocked_post):
    documents = [Document(page_content='doc')]
    mocked_response = MagicMock()
    mocked_response.status_code = 500
    mocked_response.reason = 'Internal Server Error'
    mocked_response.text = 'Server error'
    mocked_post.return_value = mocked_response

    rerank = _make_rerank(is_fault_tolerant=False)

    with pytest.raises(GenAIDocumentCompressorErrorException):
        rerank.compress_documents(documents=documents, query='question')


@patch('gen_ai_orchestrator.services.langchain.impls.document_compressor.vllm_rerank.requests.post')
def test_compress_documents_fault_tolerant_falls_back_on_exception(mocked_post):
    documents = [Document(page_content='doc')]
    mocked_post.side_effect = Exception('Connection failed')

    rerank = _make_rerank(is_fault_tolerant=True)
    result = rerank.compress_documents(documents=documents, query='question')

    assert result == documents


@patch('gen_ai_orchestrator.services.langchain.impls.document_compressor.vllm_rerank.requests.post')
def test_compress_documents_not_fault_tolerant_raises_on_exception(mocked_post):
    documents = [Document(page_content='doc')]
    mocked_post.side_effect = Exception('Connection failed')

    rerank = _make_rerank(is_fault_tolerant=False)

    with pytest.raises(GenAIDocumentCompressorErrorException):
        rerank.compress_documents(documents=documents, query='question')


@patch('gen_ai_orchestrator.services.langchain.impls.document_compressor.vllm_rerank.requests.post')
def test_endpoint_with_path_prefix_is_handled_correctly(mocked_post):
    documents = [Document(page_content='doc')]
    mocked_response = MagicMock()
    mocked_response.status_code = 200
    mocked_response.json.return_value = {'results': [{'index': 0, 'relevance_score': 0.9}]}
    mocked_post.return_value = mocked_response

    rerank = _make_rerank(endpoint='https://passerelle/inference/reranker')
    rerank.compress_documents(documents=documents, query='question')

    call_kwargs = mocked_post.call_args[1]
    assert call_kwargs['url'] == 'https://passerelle/inference/reranker/v1/rerank'


@patch('gen_ai_orchestrator.services.langchain.impls.document_compressor.vllm_rerank.requests.post')
def test_endpoint_with_trailing_slash_is_handled_correctly(mocked_post):
    documents = [Document(page_content='doc')]
    mocked_response = MagicMock()
    mocked_response.status_code = 200
    mocked_response.json.return_value = {'results': [{'index': 0, 'relevance_score': 0.9}]}
    mocked_post.return_value = mocked_response

    rerank = _make_rerank(endpoint='http://localhost:8000/')
    rerank.compress_documents(documents=documents, query='question')

    call_kwargs = mocked_post.call_args[1]
    assert call_kwargs['url'] == 'http://localhost:8000/v1/rerank'


def _ok_response(results):
    response = MagicMock()
    response.status_code = 200
    response.json.return_value = {'results': results}
    return response


# ---------------------------------------------------------------------------
# Windowing integration
# ---------------------------------------------------------------------------

_OVERSIZED = '\n'.join(f'row {i} some data column value' for i in range(300))


@patch('gen_ai_orchestrator.services.langchain.impls.document_compressor.vllm_rerank.requests.post')
def test_payload_is_plain_page_contents_when_no_oversized_doc(mocked_post):
    documents = [Document(page_content='a'), Document(page_content='b')]
    mocked_post.return_value = _ok_response(
        [{'index': 0, 'relevance_score': 0.9}, {'index': 1, 'relevance_score': 0.8}]
    )

    rerank = _make_rerank()
    rerank.compress_documents(documents=documents, query='question')

    payload = mocked_post.call_args[1]['json']['documents']
    assert payload == ['a', 'b']


@patch('gen_ai_orchestrator.services.langchain.impls.document_compressor.vllm_rerank.requests.post')
def test_oversized_doc_is_windowed_and_scored_by_max(mocked_post):
    documents = [Document(page_content='short'), Document(page_content=_OVERSIZED)]

    def side_effect(**kwargs):
        docs = kwargs['json']['documents']
        n = len(docs)
        results = []
        for i in range(n):
            score = 0.6 if i == 0 else (0.95 if i == n - 1 else 0.55)
            results.append({'index': i, 'relevance_score': score})
        return _ok_response(results)

    mocked_post.side_effect = side_effect

    rerank = _make_rerank(max_input_tokens=1100, min_score=0.5, max_documents=50)
    result = rerank.compress_documents(documents=documents, query='q')

    payload = mocked_post.call_args[1]['json']['documents']
    budget = 1100 - len('q') - 32
    # short doc stays one text, oversized is split into several windows
    assert len(payload) > 2
    assert all(len(text) <= budget for text in payload)

    oversized = documents[1]
    windows = len(payload) - 1
    assert oversized.metadata['reranker_windows'] == windows
    assert oversized.metadata['reranker_score'] == 0.95  # max over its windows
    assert documents[0].metadata['reranker_score'] == 0.6
    assert 'reranker_windows' not in documents[0].metadata
    assert oversized in result and documents[0] in result


@patch('gen_ai_orchestrator.services.langchain.impls.document_compressor.vllm_rerank.requests.post')
def test_mapping_is_correct_when_results_out_of_order(mocked_post):
    documents = [Document(page_content='a'), Document(page_content='b')]
    mocked_post.return_value = _ok_response(
        [{'index': 1, 'relevance_score': 0.9}, {'index': 0, 'relevance_score': 0.2}]
    )

    rerank = _make_rerank(min_score=0.0)
    rerank.compress_documents(documents=documents, query='q')

    assert documents[0].metadata['reranker_score'] == 0.2
    assert documents[1].metadata['reranker_score'] == 0.9


@patch('gen_ai_orchestrator.services.langchain.impls.document_compressor.vllm_rerank.requests.post')
def test_max_documents_applies_on_parents_not_windows(mocked_post):
    documents = [
        Document(page_content=_OVERSIZED),
        Document(page_content='short one'),
        Document(page_content='short two'),
    ]
    mocked_post.side_effect = lambda **kwargs: _ok_response(
        [
            {'index': i, 'relevance_score': 0.9}
            for i in range(len(kwargs['json']['documents']))
        ]
    )

    rerank = _make_rerank(max_input_tokens=1100, min_score=0.5, max_documents=2)
    result = rerank.compress_documents(documents=documents, query='q')

    # Oversized yields many windows but still counts as a single parent.
    assert len(result) == 2
    assert all(doc in documents for doc in result)
    assert len({id(doc) for doc in result}) == 2


@patch('gen_ai_orchestrator.services.langchain.impls.document_compressor.vllm_rerank.logger')
@patch('gen_ai_orchestrator.services.langchain.impls.document_compressor.vllm_rerank.requests.post')
def test_query_too_long_skips_request(mocked_post, mocked_logger):
    documents = [Document(page_content=f'doc {i}') for i in range(5)]

    rerank = _make_rerank(max_input_tokens=500, max_documents=3)
    result = rerank.compress_documents(documents=documents, query='q')

    mocked_post.assert_not_called()
    assert [doc.page_content for doc in result] == ['doc 0', 'doc 1', 'doc 2']
    assert rerank.last_fallback_info['reason'] == 'query_too_long'
    assert rerank.last_fallback_info['returned_documents'] == 3
    assert rerank.last_fallback_info['total_documents'] == 5
    assert 'budget' in rerank.last_fallback_info
    mocked_logger.warning.assert_called_once()


@patch('gen_ai_orchestrator.services.langchain.impls.document_compressor.vllm_rerank.requests.post')
def test_successful_call_resets_previous_fallback_info(mocked_post):
    documents = [Document(page_content='doc')]

    error_response = MagicMock()
    error_response.status_code = 500
    error_response.reason = 'Internal Server Error'
    error_response.text = 'Server error'
    mocked_post.return_value = error_response

    rerank = _make_rerank(is_fault_tolerant=True)
    rerank.compress_documents(documents=documents, query='q')
    assert rerank.last_fallback_info is not None

    mocked_post.return_value = _ok_response([{'index': 0, 'relevance_score': 0.9}])
    rerank.compress_documents(documents=documents, query='q')
    assert rerank.last_fallback_info is None

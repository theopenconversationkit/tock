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

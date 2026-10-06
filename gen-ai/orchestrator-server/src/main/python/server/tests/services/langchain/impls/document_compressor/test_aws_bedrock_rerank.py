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
from gen_ai_orchestrator.services.langchain.impls.document_compressor.aws_bedrock_rerank import (
    AwsBedrockRerank,
)

MODEL_ARN = 'arn:aws:bedrock:us-west-2::foundation-model/amazon.rerank-v1:0'


def _make_rerank(**kwargs) -> AwsBedrockRerank:
    return AwsBedrockRerank(model_arn=MODEL_ARN, **kwargs)


def test_compress_documents_returns_empty_list_when_no_documents():
    rerank = _make_rerank()
    assert rerank.compress_documents(documents=[], query='question') == []


@patch(
    'gen_ai_orchestrator.services.langchain.impls.document_compressor.aws_bedrock_rerank.BedrockRerank'
)
def test_compress_documents_filters_by_min_score_and_sorts(mocked_bedrock_rerank_cls):
    documents = [
        Document(page_content='low relevance'),
        Document(page_content='high relevance'),
        Document(page_content='medium relevance'),
    ]
    mocked_client = MagicMock()
    mocked_client.rerank.return_value = [
        {'index': 0, 'relevance_score': 0.1},
        {'index': 1, 'relevance_score': 0.9},
        {'index': 2, 'relevance_score': 0.6},
    ]
    mocked_bedrock_rerank_cls.return_value = mocked_client

    rerank = _make_rerank(min_score=0.5, max_documents=50)
    result = rerank.compress_documents(documents=documents, query='question')

    assert [doc.page_content for doc in result] == [
        'high relevance',
        'medium relevance',
    ]
    assert result[0].metadata['retriever_score'] == 0.9
    mocked_client.rerank.assert_called_once_with(
        documents=documents, query='question', top_n=len(documents)
    )


@patch(
    'gen_ai_orchestrator.services.langchain.impls.document_compressor.aws_bedrock_rerank.BedrockRerank'
)
def test_compress_documents_truncates_to_max_documents(mocked_bedrock_rerank_cls):
    documents = [Document(page_content=f'doc {i}') for i in range(3)]
    mocked_client = MagicMock()
    mocked_client.rerank.return_value = [
        {'index': 0, 'relevance_score': 0.9},
        {'index': 1, 'relevance_score': 0.8},
        {'index': 2, 'relevance_score': 0.7},
    ]
    mocked_bedrock_rerank_cls.return_value = mocked_client

    rerank = _make_rerank(min_score=0.5, max_documents=2)
    result = rerank.compress_documents(documents=documents, query='question')

    assert len(result) == 2
    assert [doc.page_content for doc in result] == ['doc 0', 'doc 1']


@patch(
    'gen_ai_orchestrator.services.langchain.impls.document_compressor.aws_bedrock_rerank.BedrockRerank'
)
def test_compress_documents_fills_to_max_documents_with_below_threshold(
    mocked_bedrock_rerank_cls,
):
    documents = [Document(page_content=f'doc {i}') for i in range(3)]
    mocked_client = MagicMock()
    mocked_client.rerank.return_value = [
        {'index': 0, 'relevance_score': 0.9},
        {'index': 1, 'relevance_score': 0.2},
        {'index': 2, 'relevance_score': 0.1},
    ]
    mocked_bedrock_rerank_cls.return_value = mocked_client

    rerank = _make_rerank(min_score=0.5, max_documents=3, fill_to_max_documents=True)
    result = rerank.compress_documents(documents=documents, query='question')

    assert [doc.page_content for doc in result] == ['doc 0', 'doc 1', 'doc 2']


@patch(
    'gen_ai_orchestrator.services.langchain.impls.document_compressor.aws_bedrock_rerank.BedrockRerank'
)
def test_compress_documents_fault_tolerant_falls_back_to_original_documents(
    mocked_bedrock_rerank_cls,
):
    documents = [Document(page_content='doc')]
    mocked_client = MagicMock()
    mocked_client.rerank.side_effect = Exception('boom')
    mocked_bedrock_rerank_cls.return_value = mocked_client

    rerank = _make_rerank(is_fault_tolerant=True)
    result = rerank.compress_documents(documents=documents, query='question')

    assert result == documents


@patch(
    'gen_ai_orchestrator.services.langchain.impls.document_compressor.aws_bedrock_rerank.BedrockRerank'
)
def test_compress_documents_not_fault_tolerant_raises(mocked_bedrock_rerank_cls):
    documents = [Document(page_content='doc')]
    mocked_client = MagicMock()
    mocked_client.rerank.side_effect = Exception('boom')
    mocked_bedrock_rerank_cls.return_value = mocked_client

    rerank = _make_rerank(is_fault_tolerant=False)

    with pytest.raises(GenAIDocumentCompressorErrorException):
        rerank.compress_documents(documents=documents, query='question')

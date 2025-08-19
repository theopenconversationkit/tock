#   Copyright (C) 2024-2025 Credit Mutuel Arkea
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
import os
from unittest.mock import AsyncMock, MagicMock, patch

import pytest
from langchain_core.documents import Document
from langchain_core.messages import AIMessage, HumanMessage
from requests.exceptions import HTTPError

from gen_ai_orchestrator.errors.exceptions.document_compressor.document_compressor_exceptions import \
    GenAIDocumentCompressorUnknownLabelException
from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIGuardCheckException,
)
from gen_ai_orchestrator.models.guardrail.bloomz.bloomz_guardrail_setting import (
    BloomzGuardrailSetting,
)
from gen_ai_orchestrator.models.rag.rag_models import LLMAnswer
from gen_ai_orchestrator.routers.requests.requests import RAGRequest
from gen_ai_orchestrator.services.langchain import rag_chain
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    get_guardrail_factory,
)
from gen_ai_orchestrator.services.langchain.impls.document_compressor.bloomz_rerank import BloomzRerank
from gen_ai_orchestrator.services.langchain.rag_chain import (
    check_guardrail_output,
    execute_rag_chain,
)


@patch('gen_ai_orchestrator.services.langchain.impls.document_compressor.bloomz_rerank.requests.post')
@patch('gen_ai_orchestrator.services.langchain.factories.langchain_factory.get_compressor_factory')
@patch('gen_ai_orchestrator.services.langchain.factories.langchain_factory.get_callback_handler_factory')
@patch('gen_ai_orchestrator.services.langchain.rag_chain.create_rag_chain')
@patch('gen_ai_orchestrator.services.langchain.rag_chain.RAGCallbackHandler')
@patch('gen_ai_orchestrator.services.langchain.rag_chain.rag_guard')
@patch('gen_ai_orchestrator.services.langchain.rag_chain.RAGResponse')
@patch('gen_ai_orchestrator.services.langchain.rag_chain.RAGDebugData')
@patch('gen_ai_orchestrator.services.langchain.rag_chain.get_llm_answer')
@pytest.mark.asyncio
async def test_rag_chain(
        mocked_get_llm_answer,
        mocked_rag_debug_data,
        mocked_rag_response,
        mocked_rag_guard,
        mocked_callback_init,
        mocked_create_rag_chain,
        mocked_get_callback_handler_factory,
        mocked_get_document_compressor_factory,
        mocked_guardrail_parse,
):
    """Test the full execute_qa_chain method by mocking all external calls."""
    # Build a test RAGRequest
    query_dict = {
        'dialog': {
            'history': [
                {'text': 'Hello, how can I do this?', 'type': 'HUMAN'},
                {'text': 'you can do this with the following method ....', 'type': 'AI'}
            ],
            'tags': []
        },
        'question_answering_llm_setting': {
            'provider': 'OpenAI',
            'api_key': {'type': 'Raw', 'secret': 'ab7***************************A1IV4B'},
            'temperature': 1.2,
            'model': 'gpt-3.5-turbo',
        },
        'question_answering_prompt': {
            'formatter': 'f-string',
            'template': """Use the following context to answer the question at the end.
If you don't know the answer, just say {no_answer}.

Context:
{context}

Question:
{question}

Answer in {locale}:""",
            'inputs': {
                'question': 'How to get started playing guitar ?',
                'no_answer': 'Sorry, I don t know.',
                'locale': 'French',
            }
        },
        'embedding_question_em_setting': {
            'provider': 'OpenAI',
            'api_key': {'type': 'Raw', 'secret': 'ab7***************************A1IV4B'},
            'model': 'text-embedding-ada-002',
        },
        'document_index_name': 'my-index-name',
        'document_search_params': {
            'provider': 'OpenSearch',
            'filter': [
                {
                    'term': {
                        'metadata.index_session_id.keyword': '352d2466-17c5-4250-ab20-d7c823daf035'
                    }
                }
            ],
            'k': 4,
        },
        'vector_store_setting': {
            'provider': 'OpenSearch',
            'host': 'localhost',
            'port': 9200,
            'username': 'admin',
            'password': {
                'type': 'Raw',
                'secret': 'admin',
            },
        },
        'observability_setting': {
            'provider': 'Langfuse',
            'url': 'http://localhost:3000',
            'secret_key': {
                'type': 'Raw',
                'secret': 'sk-********************be8f',
            },
            'public_key': 'pk-lf-5e374dc6-e194-4b37-9c07-b77e68ef7d2c',
        },
        'guardrail_setting': {
            'provider': 'BloomzGuardrail',
            'api_base': 'http://test-guard.com',
            'max_score': 0.5,
        },
        'compressor_setting': {
            'provider': 'BloomzRerank',
            'min_score': 0.7,
            'endpoint': 'http://test-rerank.com',
        },
        'documents_required': True,
    }
    request = RAGRequest(**query_dict)
    inputs = {
        **request.question_answering_prompt.inputs,
        'chat_history': [
            HumanMessage(content='Hello, how can I do this?'),
            AIMessage(content='you can do this with the following method ....'),
        ],
    }
    docs = [Document(
        page_content='some page content',
        metadata={'id': '123-abc', 'title': 'my-title', 'source': None},
    )]
    response = {
        'answer': {
            'status': '',
            'answer': 'an answer from llm',
            'topic': None,
            'suggested_topics': None,
            'context': []
        },
        'documents': docs
    }
    llm_answer = LLMAnswer(**response['answer'])

    # Setup mock factories/init return value
    observability_factory_instance = mocked_get_callback_handler_factory.return_value
    mocked_callback = mocked_callback_init.return_value
    mocked_langfuse_callback = observability_factory_instance.get_callback_handler()
    mocked_chain = mocked_create_rag_chain.return_value
    mocked_chain.ainvoke = AsyncMock(return_value=response)
    mocked_rag_answer = mocked_chain.ainvoke.return_value

    mocked_response = MagicMock()
    mocked_response.status_code = 200
    mocked_response.content = {'response': []}

    mocked_guardrail_parse.return_value = mocked_response

    # Call function
    await execute_rag_chain(request, debug=True)

    # Assert that the given observability_setting is used
    mocked_get_callback_handler_factory.assert_called_once_with(
        setting=request.observability_setting
    )
    # Assert qa chain is ainvoke()d with the expected settings from request
    mocked_chain.ainvoke.assert_called_once_with(
        input=inputs,
        config={'callbacks': [mocked_callback, mocked_langfuse_callback]},
    )
    # Assert the response is build using the expected settings
    mocked_rag_response.assert_called_once_with(
        answer=llm_answer,
        footnotes=set(),
        debug=mocked_rag_debug_data(request, mocked_rag_answer, mocked_callback, 1),
        observability_info=None
    )
    mocked_get_document_compressor_factory(
        setting=request.compressor_setting
    )
    # Assert the rag guardrail is called
    mocked_guardrail_parse.assert_called_once_with(
        os.path.join(request.guardrail_setting.api_base, 'guardrail'),
        json={'text': [mocked_rag_answer['answer']['answer']]},
    )
    # Assert the rag guard is called
    mocked_rag_guard.assert_called_once_with(
        inputs, llm_answer, response, request.documents_required
    )


@patch('gen_ai_orchestrator.services.langchain.impls.guardrail.bloomz_guardrail.requests.post')
def test_guardrail_parse_succeed_with_toxicities_encountered(
        mocked_guardrail_response,
):
    guardrail = get_guardrail_factory(
        BloomzGuardrailSetting(
            provider='BloomzGuardrail', max_score=0.5, api_base='http://test-guard.com'
        )
    ).get_parser()
    rag_response = {
        'answer': {
            'status': '',
            'answer': 'This is a sample text.',
            'topic': None,
            'suggested_topics': None,
            'context': []
        }
    }

    mocked_response = MagicMock()
    mocked_response.status_code = 200
    mocked_response.json.return_value = {
        'response': [
            [
                {'label': 'racism', 'score': 0.1},
                {'label': 'insult', 'score': 0.2},
                {'label': 'threat', 'score': 0.7},
                {'label': 'hate speech', 'score': 0.95},
            ]
        ]
    }

    mocked_guardrail_response.return_value = mocked_response
    guardrail_output = guardrail.parse(rag_response['answer']['answer'])

    mocked_guardrail_response.assert_called_once_with(
        os.path.join(guardrail.endpoint, 'guardrail'),
        json={'text': [rag_response['answer']['answer']]},
    )
    assert guardrail_output == {
        'content': 'This is a sample text.',
        'output_toxicity': True,
        'output_toxicity_reason': ['threat', 'hate speech'],
    }


@patch('gen_ai_orchestrator.services.langchain.impls.guardrail.bloomz_guardrail.requests.post')
def test_guardrail_parse_fail(mocked_guardrail_response):
    guardrail = get_guardrail_factory(
        BloomzGuardrailSetting(
            provider='BloomzGuardrail', max_score=0.5, api_base='http://test-guard.com'
        )
    ).get_parser()
    rag_response = {
        'answer': {
            'status': '',
            'answer': 'This is a sample text.',
            'topic': None,
            'suggested_topics': None,
            'context': []
        }
    }

    mocked_response = MagicMock()
    mocked_response.status_code = 500
    mocked_guardrail_response.return_value = mocked_response

    with pytest.raises(
            HTTPError,
            match=f"Error {mocked_response.status_code}. Bloomz guardrail didn't respond as expected.",
    ):
        guardrail.parse(rag_response['answer']['answer'])

    mocked_guardrail_response.assert_called_once_with(
        os.path.join(guardrail.endpoint, 'guardrail'),
        json={'text': [rag_response['answer']['answer']]},
    )


@patch('gen_ai_orchestrator.services.langchain.impls.document_compressor.bloomz_rerank.requests.post')
def test_compress_documents_should_succeed(mocked_rerank):
    bloomz_reranker = BloomzRerank(label='entailement', endpoint='http://example.com')
    documents = [
        Document(
            page_content='Page content 1',
            metadata={
                'source': 'doc1.pdf',
                'file_path': 'doc1.pdf',
                'page': 57,
                'total_pages': 104,
                'Producer': 'GPL Ghostscript 9.05',
                'CreationDate': "D:20230828165103+02'00'",
                'ModDate': "D:20230828165103+02'00'",
                'Title': 'DGRC 2023 - ABEI',
                'Creator': 'PDFCreator Version 1.6.2',
                'Author': 'F9261',
                'Keywords': '',
                'Subject': '',
            },
        ),
        Document(
            page_content='Contenu du document 8',
            metadata={
                'source': 'incident - v5.pdf',
                'file_path': 'incident - v5.pdf',
                'page': 19,
                'total_pages': 23,
                'Author': 'F0421',
                'CreationDate': "D:20231212161411+01'00'",
                'ModDate': "D:20231212161411+01'00'",
                'Producer': 'Microsoft: Print To PDF',
                'Title': "Microsoft Word - P1 - Département des Risques - Déclaration d'un incident - v5.doc",
            },
        ),
    ]

    mocked_response = MagicMock()
    mocked_response.status_code = 200
    mocked_response.json.return_value = {
        'response': [
            [
                {'label': 'entailement', 'score': 0.1},
                {'label': 'neutral', 'score': 0.2},
                {'label': 'contradiction', 'score': 0.7},
            ],
            [
                {'label': 'entailement', 'score': 0.8},
                {'label': 'neutral', 'score': 0.2},
                {'label': 'contradiction', 'score': 0.7},
            ],
        ]
    }
    mocked_rerank.return_value = mocked_response

    result = bloomz_reranker.compress_documents(documents=documents, query='Some query')

    mocked_rerank.assert_called_once()
    assert result == [
        Document(
            page_content='Contenu du document 8',
            metadata={
                'source': 'incident - v5.pdf',
                'file_path': 'incident - v5.pdf',
                'page': 19,
                'total_pages': 23,
                'Author': 'F0421',
                'CreationDate': "D:20231212161411+01'00'",
                'ModDate': "D:20231212161411+01'00'",
                'Producer': 'Microsoft: Print To PDF',
                'Title': "Microsoft Word - P1 - Département des Risques - Déclaration d'un incident - v5.doc",
                'retriever_score': 0.8,
            },
        )
    ]


@patch('gen_ai_orchestrator.services.langchain.impls.document_compressor.bloomz_rerank.requests.post')
def test_compress_documents_with_unknown_label(mocked_rerank):
    bloomz_reranker = BloomzRerank(label='unknown_label', endpoint='http://example.com')
    documents = [
        Document(
            page_content='Page content 1',
            metadata={
                'source': 'doc1.pdf',
            },
        ),
        Document(
            page_content='Contenu du document 8',
            metadata={
                'source': 'incident - v5.pdf',
            },
        ),
    ]

    mocked_response = MagicMock()
    mocked_response.status_code = 200
    mocked_response.json.return_value = {
        'response': [
            [
                {'label': 'entailement', 'score': 0.1},
            ]
        ]
    }
    mocked_rerank.return_value = mocked_response

    with pytest.raises(GenAIDocumentCompressorUnknownLabelException) as exc:
        bloomz_reranker.compress_documents(documents=documents, query='Some query')

    assert exc.value.error_code.value == 6002
    assert exc.value.message == 'Unknown Document Compressor label.'
    assert exc.value.detail == 'Check the Document Compressor label you sent.'


def test_check_guardrail_output_find_toxicities():
    guardrail_output = {
        'content': 'This is a sample text.',
        'output_toxicity': True,
        'output_toxicity_reason': ['threat', 'hate speech'],
    }

    with pytest.raises(GenAIGuardCheckException) as exc_found:
        check_guardrail_output(guardrail_output)

    assert exc_found.value.error_code.value == 1004
    assert 'Guard check failed.' in exc_found.value.message


def test_check_guardrail_output_is_ok():
    guardrail_output = {
        'content': 'This is a sample text.',
        'output_toxicity': False,
        'output_toxicity_reason': [],
    }

    assert check_guardrail_output(guardrail_output) is True


@patch('gen_ai_orchestrator.services.langchain.rag_chain.rag_log')
def test_rag_guard_fails_if_no_docs_in_valid_answer(mocked_log):
    question = 'Hi!'
    response = {
        'answer': {
            'status': 'found_in_context',
            'answer': 'a valid answer'
        },
        'documents': [],
    }
    try:
        rag_chain.rag_guard(question, LLMAnswer(**response['answer']), response, documents_required=True)
    except Exception as e:
        assert isinstance(e, GenAIGuardCheckException)


@patch('gen_ai_orchestrator.services.langchain.rag_chain.rag_log')
def test_rag_guard_accepts_no_answer_even_with_docs(mocked_log):
    question = 'Hi!'
    response = {
        'answer': {
            'status': 'not_found_in_context',
            'answer': 'Sorry, I don t know.',
            'context': [
                {
                    'chunk': 1,
                    'sentences': ["str1"],
                }
            ]
        },
        'documents': ['a doc as a string'],
    }
    rag_chain.rag_guard(question, LLMAnswer(**response['answer']), response, documents_required=True)
    # No answer found in the retrieved context. The documents are therefore removed from the RAG response.
    assert response['documents'] == []


@patch('gen_ai_orchestrator.services.langchain.rag_chain.rag_log')
def test_rag_guard_valid_answer_with_docs(mocked_log):
    question = 'Hi!'
    response = {
        'answer': {
            'status': 'found_in_context',
            'answer': 'a valid answer',
        },
        'documents': ['doc1', 'doc2'],
    }
    rag_chain.rag_guard(question, LLMAnswer(**response['answer']), response, documents_required=True)
    assert response['documents'] == ['doc1', 'doc2']


@patch('gen_ai_orchestrator.services.langchain.rag_chain.rag_log')
def test_rag_guard_no_answer_with_no_docs(mocked_log):
    question = 'Hi!'
    response = {
        'answer': {
            'status': 'not_found_in_context',
            'answer': 'Sorry, I don t know.'
        },
        'documents': [],
    }
    rag_chain.rag_guard(question, LLMAnswer(**response['answer']), response, documents_required=True)
    assert response['documents'] == []


@patch('gen_ai_orchestrator.services.langchain.rag_chain.rag_log')
def test_rag_guard_without_no_answer_input(mocked_log):
    """Test that __rag_guard handles missing no_answer input correctly."""
    question = 'Hi!'
    response = {
        'answer': {
            'status': 'found_in_context',
            'answer': 'a valid answer',
        },
        'documents': [],
    }
    with pytest.raises(GenAIGuardCheckException) as exc:
        rag_chain.rag_guard(question, LLMAnswer(**response['answer']), response, documents_required=True)

    mocked_log.assert_called_once()

    assert isinstance(exc.value, GenAIGuardCheckException)

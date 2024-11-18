#   Copyright (C) 2024 Credit Mutuel Arkea
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
from fastapi import HTTPException
from langchain_core.documents import Document
from langchain_core.messages import AIMessage, HumanMessage
from requests.exceptions import HTTPError

from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIGuardCheckException,
    GenAIUnknownLabelException,
)
from gen_ai_orchestrator.models.guardrail.bloomz.bloomz_guardrail_setting import (
    BloomzGuardrailSetting,
)
from gen_ai_orchestrator.models.vector_stores.vectore_store_provider import (
    VectorStoreProvider,
)
from gen_ai_orchestrator.routers.requests.requests import RagQuery
from gen_ai_orchestrator.services.contextual_compressor.bloomz_rerank import (
    BloomzRerank,
)
from gen_ai_orchestrator.services.langchain import rag_chain
from gen_ai_orchestrator.services.langchain.callbacks.retriever_json_callback_handler import (
    RetrieverJsonCallbackHandler,
)
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    get_guardrail_factory,
)
from gen_ai_orchestrator.services.langchain.rag_chain import (
    check_guardrail_output,
    execute_qa_chain,
    get_condense_question,
    get_llm_prompts,
)

# 'Mock an item where it is used, not where it came from.'
# (https://www.toptal.com/python/an-introduction-to-mocking-in-python)
# See https://docs.python.org/3/library/unittest.mock.html#where-to-patch
# Here:
# --> Not where it came from:
# @patch('llm_orchestrator.services.langchain.factories.langchain_factory.get_llm_factory')
# --> But where it is used (in the execute_qa_chain method of the llm_orchestrator.services.langchain.rag_chain
# module that imports get_llm_factory):


@patch(
    'gen_ai_orchestrator.services.langchain.rag_chain.ContextualCompressionRetriever'
)
@patch('gen_ai_orchestrator.services.contextual_compressor.bloomz_rerank.requests.post')
@patch(
    'gen_ai_orchestrator.services.langchain.factories.langchain_factory.get_callback_handler_factory'
)
@patch('gen_ai_orchestrator.services.langchain.rag_chain.get_llm_factory')
@patch('gen_ai_orchestrator.services.langchain.rag_chain.get_em_factory')
@patch('gen_ai_orchestrator.services.langchain.rag_chain.get_vector_store_factory')
@patch('gen_ai_orchestrator.services.langchain.rag_chain.PromptTemplate')
@patch('gen_ai_orchestrator.services.langchain.rag_chain.__find_input_variables')
@patch(
    'gen_ai_orchestrator.services.langchain.rag_chain.ConversationalRetrievalChain.from_llm'
)
@patch('gen_ai_orchestrator.services.langchain.rag_chain.RetrieverJsonCallbackHandler')
@patch('gen_ai_orchestrator.services.langchain.rag_chain.__rag_guard')
@patch('gen_ai_orchestrator.services.langchain.rag_chain.RagResponse')
@patch('gen_ai_orchestrator.services.langchain.rag_chain.TextWithFootnotes')
@patch('gen_ai_orchestrator.services.langchain.rag_chain.RagDebugData')
@pytest.mark.asyncio
async def test_rag_chain(
    mocked_rag_debug_data,
    mocked_text_with_footnotes,
    mocked_rag_response,
    mocked_rag_guard,
    mocked_callback_init,
    mocked_chain_builder,
    mocked_find_input_variables,
    mocked_prompt_template,
    mocked_get_vector_store_factory,
    mocked_get_em_factory,
    mocked_get_llm_factory,
    mocked_get_callback_handler_factory,
    mocked_guardrail_parse,
    mocked_compressor_builder,
):
    """Test the full execute_qa_chain method by mocking all external calls."""
    # Build a test RagQuery
    query_dict = {
        'history': [
            {'text': 'Hello, how can I do this?', 'type': 'HUMAN'},
            {
                'text': 'you can do this with the following method ....',
                'type': 'AI',
            },
        ],
        'question_answering_llm_setting': {
            'provider': 'OpenAI',
            'api_key': {'type': 'Raw', 'value': 'ab7***************************A1IV4B'},
            'temperature': 1.2,
            'prompt': """Use the following context to answer the question at the end.
If you don't know the answer, just say {no_answer}.

Context:
{context}

Question:
{question}

Answer in {locale}:""",
            'model': 'gpt-3.5-turbo',
        },
        'question_answering_prompt_inputs': {
            'question': 'How to get started playing guitar ?',
            'no_answer': 'Sorry, I don t know.',
            'locale': 'French',
        },
        'embedding_question_em_setting': {
            'provider': 'OpenAI',
            'api_key': {'type': 'Raw', 'value': 'ab7***************************A1IV4B'},
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
                'value': 'admin',
            },
        },
        'observability_setting': {
            'provider': 'Langfuse',
            'url': 'http://localhost:3000',
            'secret_key': {
                'type': 'Raw',
                'value': 'sk-lf-93c4f78f-4096-416b-a6e3-ceabe45abe8f',
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
    }
    query = RagQuery(**query_dict)

    # Setup mock factories/init return value
    em_factory_instance = mocked_get_em_factory.return_value
    llm_factory_instance = mocked_get_llm_factory.return_value
    observability_factory_instance = mocked_get_callback_handler_factory.return_value
    vector_store_factory_instance = mocked_get_vector_store_factory.return_value
    mocked_chain = mocked_chain_builder.return_value
    mocked_callback = mocked_callback_init.return_value
    mocked_compressor = mocked_compressor_builder.return_value
    mocked_langfuse_callback = observability_factory_instance.get_callback_handler()
    mocked_chain.ainvoke = AsyncMock(
        return_value={'answer': 'an answer from llm', 'source_documents': []}
    )
    mocked_rag_answer = mocked_chain.ainvoke.return_value

    mocked_response = MagicMock()
    mocked_response.status_code = 200
    mocked_response.content = {'response': []}

    mocked_guardrail_parse.return_value = mocked_response

    # Call function
    await execute_qa_chain(query, debug=True)

    # Assert factories are called with the expected settings from query
    mocked_get_llm_factory.assert_called_once_with(
        setting=query.question_answering_llm_setting
    )
    mocked_get_em_factory.assert_called_once_with(
        setting=query.embedding_question_em_setting
    )
    mocked_get_vector_store_factory.assert_called_once_with(
        setting=query.vector_store_setting,
        index_name=query.document_index_name,
        embedding_function=em_factory_instance.get_embedding_model(),
    )
    mocked_get_callback_handler_factory.assert_called_once_with(
        setting=query.observability_setting
    )

    # Assert LangChain qa chain is created using the expected settings from query
    mocked_chain_builder.assert_called_once_with(
        llm=llm_factory_instance.get_language_model(),
        retriever=mocked_compressor,
        return_source_documents=True,
        return_generated_question=True,
        combine_docs_chain_kwargs={
            # PromptTemplate must be mocked or searching for params in it will fail
            'prompt': mocked_prompt_template(
                template=query.question_answering_llm_setting.prompt,
                input_variables=['no_answer', 'context', 'question', 'locale'],
            )
        },
    )
    # Assert qa chain is ainvoke()d with the expected settings from query
    mocked_chain.ainvoke.assert_called_once_with(
        input={
            **query.question_answering_prompt_inputs,
            'chat_history': [
                HumanMessage(content='Hello, how can I do this?'),
                AIMessage(content='you can do this with the following method ....'),
            ],
        },
        config={'callbacks': [mocked_callback, mocked_langfuse_callback]},
    )
    # Assert the response is build using the expected settings
    mocked_rag_response.assert_called_once_with(
        # TextWithFootnotes must be mocked or mapping the footnotes will fail
        answer=mocked_text_with_footnotes(
            text=mocked_rag_answer['answer'], footnotes=[]
        ),
        debug=mocked_rag_debug_data(query, mocked_rag_answer, mocked_callback, 1),
    )

    mocked_guardrail_parse.assert_called_once_with(
        os.path.join(query.guardrail_setting.api_base, 'guardrail'),
        json={'text': [mocked_rag_answer['answer']]},
    )
    mocked_compressor_builder.assert_called_once()


@patch('gen_ai_orchestrator.services.guardrail.bloomz_guardrail.requests.post')
def test_guardrail_parse_succeed_with_toxicities_encountered(
    mocked_guardrail_response,
):
    guardrail = get_guardrail_factory(
        BloomzGuardrailSetting(
            provider='BloomzGuardrail', max_score=0.5, api_base='http://test-guard.com'
        )
    ).get_parser()
    rag_response = {'answer': 'This is a sample text.'}

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
    guardrail_output = guardrail.parse(rag_response['answer'])

    mocked_guardrail_response.assert_called_once_with(
        os.path.join(guardrail.endpoint, 'guardrail'),
        json={'text': [rag_response['answer']]},
    )
    assert guardrail_output == {
        'content': 'This is a sample text.',
        'output_toxicity': True,
        'output_toxicity_reason': ['threat', 'hate speech'],
    }


@patch('gen_ai_orchestrator.services.guardrail.bloomz_guardrail.requests.post')
def test_guardrail_parse_fail(mocked_guardrail_response):
    guardrail = get_guardrail_factory(
        BloomzGuardrailSetting(
            provider='BloomzGuardrail', max_score=0.5, api_base='http://test-guard.com'
        )
    ).get_parser()
    rag_response = {'answer': 'This is a sample text.'}

    mocked_response = MagicMock()
    mocked_response.status_code = 500
    mocked_guardrail_response.return_value = mocked_response

    with pytest.raises(
        HTTPError,
        match=f"Error {mocked_response.status_code}. Bloomz guardrail didn't respond as expected.",
    ):
        guardrail.parse(rag_response['answer'])

    mocked_guardrail_response.assert_called_once_with(
        os.path.join(guardrail.endpoint, 'guardrail'),
        json={'text': [rag_response['answer']]},
    )


@patch('gen_ai_orchestrator.services.contextual_compressor.bloomz_rerank.requests.post')
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
                'Title': "Microsoft Word - P1 - DÃ©partement des Risques - DÃ©claration d'un incident - v5.doc",
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
                'Title': "Microsoft Word - P1 - DÃ©partement des Risques - DÃ©claration d'un incident - v5.doc",
                'retriever_score': 0.8,
            },
        )
    ]


@patch('gen_ai_orchestrator.services.contextual_compressor.bloomz_rerank.requests.post')
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

    with pytest.raises(GenAIUnknownLabelException) as exc:
        bloomz_reranker.compress_documents(documents=documents, query='Some query')

    assert exc.value.error_code.value == 1006
    assert exc.value.message == 'Unknown label.'
    assert exc.value.detail == 'Check the label you sent.'


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


def test_find_input_variables():
    template = 'This is a {sample} text with {multiple} curly brace sections'
    input_vars = rag_chain.__find_input_variables(template)
    assert input_vars == ['sample', 'multiple']


@patch('gen_ai_orchestrator.services.langchain.rag_chain.__rag_log')
def test_rag_guard_fails_if_no_docs_in_valid_answer(mocked_log):
    inputs = {'no_answer': "Sorry, I don't know."}
    response = {
        'answer': 'a valid answer',
        'source_documents': [],
    }
    try:
        rag_chain.__rag_guard(inputs, response)
    except Exception as e:
        assert isinstance(e, GenAIGuardCheckException)


@patch('gen_ai_orchestrator.services.langchain.rag_chain.__rag_log')
def test_rag_guard_removes_docs_if_no_answer(mocked_log):
    inputs = {'no_answer': "Sorry, I don't know."}
    response = {
        'answer': "Sorry, I don't know.",
        'source_documents': ['a doc as a string'],
    }
    rag_chain.__rag_guard(inputs, response)
    assert response['source_documents'] == []


def test_get_llm_prompts_one_record():
    handler = RetrieverJsonCallbackHandler()
    handler.on_text(text='LLM 1')
    llm_1, llm_2 = get_llm_prompts(handler)
    assert llm_1 is None
    assert llm_2 == 'LLM 1'


def test_get_llm_prompts_one_record():
    handler = RetrieverJsonCallbackHandler()
    handler.on_text(text='LLM 1')
    handler.on_text(text='LLM 2')
    llm_1, llm_2 = get_llm_prompts(handler)
    assert llm_1 == 'LLM 1'
    assert llm_2 == 'LLM 2'


def test_get_condense_question_none():
    handler = RetrieverJsonCallbackHandler()
    handler.on_text(text='LLM 1')
    handler.on_chain_start(
        serialized={},
        inputs={
            'input_documents': [],
            'question': 'Is this a question ?',
            'chat_history': 'chat_history',
        },
    )
    question = get_condense_question(handler)
    assert question is None


def test_get_condense_question():
    handler = RetrieverJsonCallbackHandler()
    handler.on_text(text='LLM 1')
    handler.on_text(text='LLM 2')
    handler.on_chain_start(
        serialized={},
        inputs={
            'input_documents': [],
            'question': 'Is this a question ?',
            'chat_history': 'chat_history',
        },
    )
    question = get_condense_question(handler)
    assert question == 'Is this a question ?'

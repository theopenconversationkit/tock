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
from unittest.mock import patch, AsyncMock

import pytest
from langchain_core.messages import AIMessage, HumanMessage

from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIGuardCheckException,
)
from gen_ai_orchestrator.models.vector_stores.vectore_store_provider import (
    VectorStoreProvider,
)
from gen_ai_orchestrator.routers.requests.requests import RagQuery
from gen_ai_orchestrator.services.langchain import rag_chain
from gen_ai_orchestrator.services.langchain.callbacks.retriever_json_callback_handler import (
    RetrieverJsonCallbackHandler,
)
from gen_ai_orchestrator.services.langchain.rag_chain import (
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
@patch('gen_ai_orchestrator.services.langchain.factories.langchain_factory.get_callback_handler_factory')
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
    mocked_get_callback_handler_factory
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
        'vector_store_setting': {
            'provider': 'OpenSearch',
            'index_name': 'my-index-name',
            'index_session_id': '352d2466-17c5-4250-ab20-d7c823daf035',
            'k': 4,
            'host': 'localhost',
            'port': 9200,
            'username': 'admin',
            'password': {
                'type': 'Raw',
                'value': 'admin',
            }
        },
        'observability_setting': {
            'provider': 'Langfuse',
            'url': 'http://localhost:3000',
            'secret_key': {
              'type': 'Raw',
              'value': 'sk-lf-93c4f78f-4096-416b-a6e3-ceabe45abe8f'
            },
            'public_key': 'pk-lf-5e374dc6-e194-4b37-9c07-b77e68ef7d2c'
        }
    }
    query = RagQuery(**query_dict)

    # Setup mock factories/init return value
    em_factory_instance = mocked_get_em_factory.return_value
    llm_factory_instance = mocked_get_llm_factory.return_value
    observability_factory_instance = mocked_get_callback_handler_factory.return_value
    vector_store_factory_instance = mocked_get_vector_store_factory.return_value
    mocked_chain = mocked_chain_builder.return_value
    mocked_callback = mocked_callback_init.return_value
    mocked_langfuse_callback = observability_factory_instance.get_callback_handler()
    mocked_chain.ainvoke = AsyncMock(return_value={'answer': 'an answer from llm', 'source_documents': []})
    mocked_rag_answer = mocked_chain.ainvoke.return_value

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
        embedding_function=em_factory_instance.get_embedding_model()
    )
    mocked_get_callback_handler_factory.assert_called_once_with(
        setting=query.observability_setting
    )
    # Assert LangChain qa chain is created using the expected settings from query
    mocked_chain_builder.assert_called_once_with(
        llm=llm_factory_instance.get_language_model(),
        retriever=vector_store_factory_instance.get_vector_store_retriever(),
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

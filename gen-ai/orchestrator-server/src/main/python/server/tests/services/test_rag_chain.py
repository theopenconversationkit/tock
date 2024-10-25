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
from langchain_core.documents import Document
from langchain_core.messages import AIMessage, HumanMessage

from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIGuardCheckException,
)
from gen_ai_orchestrator.routers.requests.requests import RagQuery
from gen_ai_orchestrator.services.langchain import rag_chain
from gen_ai_orchestrator.services.langchain.rag_chain import (
    execute_rag_chain,
)


@patch('gen_ai_orchestrator.services.langchain.factories.langchain_factory.get_callback_handler_factory')
@patch('gen_ai_orchestrator.services.langchain.rag_chain.create_rag_chain')
@patch('gen_ai_orchestrator.services.langchain.rag_chain.RAGCallbackHandler')
@patch('gen_ai_orchestrator.services.langchain.rag_chain.rag_guard')
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
    mocked_create_rag_chain,
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
            'inputs' : {
                'question': 'How to get started playing guitar ?',
                'no_answer': 'Sorry, I don t know.',
                'locale': 'French',
            }
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
    inputs = {
        **query.question_answering_prompt.inputs,
        'chat_history': [
            HumanMessage(content='Hello, how can I do this?'),
            AIMessage(content='you can do this with the following method ....'),
        ],
    }
    docs = [Document(
        page_content='some page content',
        metadata={'id':1, 'title':'my-title', 'source': None},
    )]
    response = {'answer': 'an answer from llm', 'documents': docs}

    # Setup mock factories/init return value
    observability_factory_instance = mocked_get_callback_handler_factory.return_value
    mocked_callback = mocked_callback_init.return_value
    mocked_langfuse_callback = observability_factory_instance.get_callback_handler()
    mocked_chain = mocked_create_rag_chain.return_value
    mocked_chain.ainvoke = AsyncMock(return_value=response)
    mocked_rag_answer = mocked_chain.ainvoke.return_value

    # Call function
    await execute_rag_chain(query, debug=True)

    # Assert that the given observability_setting is used
    mocked_get_callback_handler_factory.assert_called_once_with(
        setting=query.observability_setting
    )
    # Assert qa chain is ainvoke()d with the expected settings from query
    mocked_chain.ainvoke.assert_called_once_with(
        input=inputs,
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
    # Assert the rag guard is called
    mocked_rag_guard.assert_called_once_with(
        inputs, response
    )


@patch('gen_ai_orchestrator.services.langchain.rag_chain.rag_log')
def test_rag_guard_fails_if_no_docs_in_valid_answer(mocked_log):
    inputs = {'no_answer': "Sorry, I don't know."}
    response = {
        'answer': 'a valid answer',
        'documents': [],
    }
    try:
        rag_chain.rag_guard(inputs, response)
    except Exception as e:
        assert isinstance(e, GenAIGuardCheckException)


@patch('gen_ai_orchestrator.services.langchain.rag_chain.rag_log')
def test_rag_guard_removes_docs_if_no_answer(mocked_log):
    inputs = {'no_answer': "Sorry, I don't know."}
    response = {
        'answer': "Sorry, I don't know.",
        'documents': ['a doc as a string'],
    }
    rag_chain.rag_guard(inputs, response)
    assert response['documents'] == []


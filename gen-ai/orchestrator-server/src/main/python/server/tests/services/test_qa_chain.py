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
from unittest.mock import AsyncMock, patch

import pytest
from langchain_core.documents import Document

from gen_ai_orchestrator.models.vector_stores.vectore_store_provider import (
    VectorStoreProvider,
)
from gen_ai_orchestrator.routers.requests.requests import QAQuery
from gen_ai_orchestrator.services.langchain.qa_chain import execute_qa_chain


# "Mock an item where it is used, not where it came from."
# (https://www.toptal.com/python/an-introduction-to-mocking-in-python)
# See https://docs.python.org/3/library/unittest.mock.html#where-to-patch
# Here:
# --> Not where it came from:
# @patch("llm_orchestrator.services.langchain.factories.langchain_factory.get_llm_factory")
# --> But where it is used (in the execute_qa_chain method of the llm_orchestrator.services.langchain.rag_chain
# module that imports get_llm_factory):
@patch('gen_ai_orchestrator.services.langchain.qa_chain.get_em_factory')
@patch('gen_ai_orchestrator.services.langchain.qa_chain.get_vector_store_factory')
@patch('gen_ai_orchestrator.services.langchain.qa_chain.QAResponse')
@patch('gen_ai_orchestrator.services.langchain.qa_chain.build_chain')
@patch('gen_ai_orchestrator.services.langchain.qa_chain.Source')
@pytest.mark.asyncio
async def test_qa_chain(
    mocked_source,
    mocked_build_chain,
    mocked_qa_response,
    mocked_get_vector_store_factory,
    mocked_get_em_factory,
):
    """Test the full execute_qa_chain method by mocking all external calls."""
    # Build a test RagQuery
    query_dict = {
        'user_query': 'How to get started playing guitar ?',
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
        'documents_required': True,
    }
    query = QAQuery(**query_dict)

    # Setup mock factories/init return value
    em_factory_instance = mocked_get_em_factory.return_value
    vector_store_factory_instance = mocked_get_vector_store_factory.return_value
    mocked_chain = mocked_build_chain.return_value
    mocked_chain.ainvoke = AsyncMock(
        return_value=[
            Document(
                page_content='Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum ut placerat dolor.',
                metadata={'title': 'Title', 'source': 'URL'},
            )
        ]
    )
    mocked_qa_answer = mocked_chain.ainvoke.return_value

    # Call function
    await execute_qa_chain(query)

    # Assert factories are called with the expected settings from query
    mocked_get_em_factory.assert_called_once_with(
        setting=query.embedding_question_em_setting
    )
    mocked_get_vector_store_factory.assert_called_once_with(
        vector_store_provider=VectorStoreProvider.OPEN_SEARCH,
        embedding_function=em_factory_instance.get_embedding_model(),
        index_name=query.document_index_name,
    )
    # Assert LangChain qa chain is created using the expected settings from query
    mocked_build_chain.assert_called_once_with(
        vector_store_factory_instance.get_vector_store().as_retriever(
            search_kwargs=query.document_search_params.to_dict()
        )
    )

    # Assert qa chain is ainvoke()d with the expected settings from query
    mocked_chain.ainvoke.assert_called_once_with(query.user_query)

    # Assert the response is build using the expected settings
    mocked_qa_response.assert_called_once_with(
        # TextWithFootnotes must be mocked or mapping the footnotes will fail
        documents=set(
            [
                mocked_source(
                    title=source.metadata['title'],
                    url=source.metadata['source'],
                    content=source.page_content,
                )
                for source in mocked_qa_answer
            ]
        ),
    )

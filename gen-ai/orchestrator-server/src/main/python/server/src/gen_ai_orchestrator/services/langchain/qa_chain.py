#   Copyright (C) 2023-2026 Credit Mutuel Arkea
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
"""
Module for the QA Chain
It uses LangChain to perform a Conversational Retrieval Chain
"""

import logging
import time

from langchain_core.retrievers import BaseRetriever
from langchain_core.runnables import Runnable

from gen_ai_orchestrator.errors.handlers.openai.openai_exception_handler import (
    openai_exception_handler,
)
from gen_ai_orchestrator.errors.handlers.opensearch.opensearch_exception_handler import (
    opensearch_exception_handler,
)
from gen_ai_orchestrator.models.rag.rag_models import Source
from gen_ai_orchestrator.models.vector_stores.vectore_store_provider import (
    VectorStoreProvider,
)
from gen_ai_orchestrator.routers.requests.requests import QARequest
from gen_ai_orchestrator.routers.responses.responses import QAResponse
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    get_em_factory,
    get_vector_store_factory,
)

logger = logging.getLogger(__name__)


@opensearch_exception_handler
@openai_exception_handler(provider='OpenAI or AzureOpenAIService')
async def execute_qa_chain(request: QARequest) -> QAResponse:
    """
    Execute the QA chain using the specified embedding settings in the request.

    Args:
        request (QARequest): The QA request containing the necessary information for question-answering.

    Returns:
        QAResponse: The QA response with the document sources.

    """
    logger.info('QA chain - Start of execution...')
    start_time = time.time()

    conversational_qa_chain = create_qa_chain(request)

    response = await conversational_qa_chain.ainvoke(request.user_query)

    qa_duration = '{:.2f}'.format(time.time() - start_time)
    logger.info('QA chain - End of execution. (Duration : %s seconds)', qa_duration)

    return QAResponse(
        documents=set(
            map(
                lambda doc: Source(
                    title=doc.metadata['title'],
                    url=doc.metadata['source'],
                    content=doc.page_content,
                ),
                response,
            )
        ),
    )


def build_chain(retriever: BaseRetriever) -> Runnable:
    """
    Create the QA chain using the specified retriever.

    Args:
        retriever: The retriever used in the QA system
    Returns:
        The QA chain.
    """
    return retriever


def create_qa_chain(request: QARequest) -> Runnable:
    """
    Create the QA chain from QARequest, using the Embedding settings specified in the request

    Args:
        request: The QA request
    Returns:
        The QA chain.
    """
    em_factory = get_em_factory(setting=request.embedding_question_em_setting)
    vector_store = get_vector_store_factory(
        vector_store_provider=VectorStoreProvider.OPEN_SEARCH,
        embedding_function=em_factory.get_embedding_model(),
        index_name=request.document_index_name,
    ).get_vector_store()

    retriever = vector_store.as_retriever(
        search_kwargs=request.document_search_params.to_dict()
    )

    logger.debug('QA chain - Create a QA chain')

    return build_chain(retriever)

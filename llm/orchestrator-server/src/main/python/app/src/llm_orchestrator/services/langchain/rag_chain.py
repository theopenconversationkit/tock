#   Copyright (C) 2023-2024 Credit Mutuel Arkea
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
Module for the RAG Chain
It uses LangChain to perform a Conversational Retrieval Chain
"""

import logging
import re
import time
from logging import ERROR, WARNING

from langchain.chains import ConversationalRetrievalChain, LLMChain
from langchain.memory import ChatMessageHistory
from langchain_core.prompts import PromptTemplate

from llm_orchestrator.errors.exceptions.exceptions import (
    GenAIGuardCheckException,
)
from llm_orchestrator.errors.handlers.openai.openai_exception_handler import (
    openai_exception_handler,
)
from llm_orchestrator.errors.handlers.opensearch.opensearch_exception_handler import (
    opensearch_exception_handler,
)
from llm_orchestrator.models.errors.errors_models import ErrorInfo
from llm_orchestrator.models.rag.rag_models import (
    ChatMessageType,
    Footnote,
    TextWithFootnotes,
)
from llm_orchestrator.models.vector_stores.vectore_store_provider import (
    VectorStoreProvider,
)
from llm_orchestrator.routers.requests.requests import RagQuery
from llm_orchestrator.routers.responses.responses import RagResponse
from llm_orchestrator.services.langchain.callbacks.retriever_json_callback_handler import (
    RetrieverJsonCallbackHandler,
)
from llm_orchestrator.services.langchain.factories.langchain_factory import (
    get_em_factory,
    get_llm_factory,
    get_vector_store_factory,
)

logger = logging.getLogger(__name__)


@opensearch_exception_handler
@openai_exception_handler(provider='OpenAI or AzureOpenAIService')
def execute_qa_chain(query: RagQuery, debug: bool) -> RagResponse:
    """
    RAG chain execution, using the LLM and Embedding settings specified in the query

    Args:
        query: The RAG query
        debug: True if RAG data debug should be returned with the response.
    Returns:
        The RAG response (Answer and document sources)
    """

    logger.info('RAG chain - Start of execution...')
    start_time = time.time()

    llm_factory = get_llm_factory(setting=query.question_answering_llm_setting)
    em_factory = get_em_factory(setting=query.embedding_question_em_setting)
    vector_store_factory = get_vector_store_factory(
        vector_store_provider=VectorStoreProvider.OPEN_SEARCH,
        embedding_function=em_factory.get_embedding_model(),
        index_name=query.document_index_name,
    )

    logger.debug('RAG chain - Create a ConversationalRetrievalChain from LLM')
    conversational_retrieval_chain = ConversationalRetrievalChain.from_llm(
        llm=llm_factory.get_language_model(),
        retriever=vector_store_factory.get_vector_store().as_retriever(
            search_kwargs=query.document_search_params.to_dict()
        ),
        return_source_documents=True,
        return_generated_question=True,
        combine_docs_chain_kwargs={
            'prompt': PromptTemplate(
                template=llm_factory.setting.prompt,
                input_variables=__find_input_variables(llm_factory.setting.prompt),
            )
        },
    )

    logger.debug(
        'RAG chain - Use chat history: %s', 'Yes' if len(query.history) > 0 else 'No'
    )
    message_history = ChatMessageHistory()
    for msg in query.history:
        if ChatMessageType.HUMAN == msg.type:
            message_history.add_user_message(msg.text)
        else:
            message_history.add_ai_message(msg.text)

    inputs = {
        **query.question_answering_prompt_inputs,
        'chat_history': message_history.messages,
    }

    logger.debug(
        'RAG chain - Use RetrieverJsonCallbackHandler for debugging : %s',
        debug,
    )
    records_callback_handler = RetrieverJsonCallbackHandler()
    response = conversational_retrieval_chain.invoke(
        input=inputs,
        config={'callbacks': [records_callback_handler] if debug else []},
    )

    # RAG Guard
    __rag_guard(inputs, response)

    logger.info(
        'RAG chain - End of execution. (Duration : %.2f seconds)',
        time.time() - start_time,
    )
    # Returning RAG response
    return RagResponse(
        answer=TextWithFootnotes(
            text=response['answer'],
            footnotes=set(
                map(
                    lambda doc: Footnote(
                        identifier=f'{doc.metadata["id"]}',
                        title=doc.metadata['title'],
                        url=doc.metadata['url'],
                    ),
                    response['source_documents'],
                )
            ),
        ),
        debug=records_callback_handler.show_records() if debug else None,
    )


def __find_input_variables(template):
    """
    Search for input variables on a given template

    Args:
        template: the template to search on
    """

    motif = r'\{([^}]+)\}'
    variables = re.findall(motif, template)
    return variables


def __rag_guard(inputs, response):
    """
    If a 'no_answer' input was given as a rag setting,
    then the RAG system should give no further response when no source document has been found.
    And, when the RAG system responds with the 'no_answer' phrase,
    then the source documents are removed from the response.

    Args:
        inputs: question answering prompt inputs
        response: the RAG response
    """

    if 'no_answer' in inputs:
        if (
            response['answer'] != inputs['no_answer']
            and response['source_documents'] == []
        ):
            message = 'The RAG gives an answer when no document has been found!'
            __rag_log(level=ERROR, message=message, inputs=inputs, response=response)
            raise GenAIGuardCheckException(ErrorInfo(cause=message))

        if (
            response['answer'] == inputs['no_answer']
            and response['source_documents'] != []
        ):
            message = 'The RAG gives no answer for user question, but some documents has been found!'
            __rag_log(level=WARNING, message=message, inputs=inputs, response=response)
            # Remove source documents
            response['source_documents'] = []


def __rag_log(level, message, inputs, response):
    """
    RAG logging

    Args:
        level: logging level
        message: message to log
        inputs: question answering prompt inputs
        response: the RAG response
    """

    logger.log(
        level,
        '%(message)s \n'
        'RAG chain - question="%(question)s", answer="%(answer)s", documents="%(documents)s"',
        {
            'message': message,
            'question': inputs['question'],
            'answer': response['answer'],
            'documents': response['source_documents'],
        },
    )

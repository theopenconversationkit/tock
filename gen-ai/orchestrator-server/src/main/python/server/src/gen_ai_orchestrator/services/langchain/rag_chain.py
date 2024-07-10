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
from typing import List, Optional

from langchain.chains import ConversationalRetrievalChain
from langchain.memory import ChatMessageHistory
from langchain_core.prompts import PromptTemplate

from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIGuardCheckException,
)
from gen_ai_orchestrator.errors.handlers.openai.openai_exception_handler import (
    openai_exception_handler,
)
from gen_ai_orchestrator.errors.handlers.opensearch.opensearch_exception_handler import (
    opensearch_exception_handler,
)
from gen_ai_orchestrator.models.errors.errors_models import ErrorInfo
from gen_ai_orchestrator.models.observability.observability_trace import ObservabilityTrace
from gen_ai_orchestrator.models.rag.rag_models import (
    ChatMessageType,
    Footnote,
    RagDebugData,
    RagDocument,
    RagDocumentMetadata,
    TextWithFootnotes,
)
from gen_ai_orchestrator.models.vector_stores.vectore_store_provider import (
    VectorStoreProvider,
)
from gen_ai_orchestrator.routers.requests.requests import RagQuery
from gen_ai_orchestrator.routers.responses.responses import RagResponse
from gen_ai_orchestrator.services.langchain.callbacks.retriever_json_callback_handler import (
    RetrieverJsonCallbackHandler,
)
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    get_em_factory,
    get_llm_factory,
    get_vector_store_factory,
    get_compressor_factory,
    create_observability_callback_handler,
)

from langchain.retrievers import ContextualCompressionRetriever

logger = logging.getLogger(__name__)


@opensearch_exception_handler
@openai_exception_handler(provider='OpenAI or AzureOpenAIService')
async def execute_qa_chain(query: RagQuery, debug: bool) -> RagResponse:
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

    conversational_retrieval_chain = create_rag_chain(query=query)

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

    callback_handlers = []
    records_callback_handler = RetrieverJsonCallbackHandler()
    if debug:
        # Debug callback handler
        callback_handlers.append(records_callback_handler)
    if query.observability_setting is not None:
        # Langfuse callback handler
        callback_handlers.append(
            create_observability_callback_handler(
                observability_setting=query.observability_setting,
                trace_name=ObservabilityTrace.RAG))

    response = await conversational_retrieval_chain.ainvoke(
        input=inputs,
        config={'callbacks': callback_handlers},
    )

    # RAG Guard
    __rag_guard(inputs, response)

    # Calculation of RAG processing time
    rag_duration = '{:.2f}'.format(time.time() - start_time)
    logger.info('RAG chain - End of execution. (Duration : %s seconds)', rag_duration)

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
                        content=doc.page_content,
                    ),
                    response['source_documents'],
                )
            ),
        ),
        debug=get_rag_debug_data(
            query, response, records_callback_handler, rag_duration
        )
        if debug
        else None
    )


def create_rag_chain(query: RagQuery) -> ConversationalRetrievalChain:
    """
    Create the RAG chain from RagQuery, using the LLM and Embedding settings specified in the query

    Args:
        query: The RAG query
    Returns:
        The RAG chain.
    """
    llm_factory = get_llm_factory(setting=query.question_answering_llm_setting)
    em_factory = get_em_factory(setting=query.embedding_question_em_setting)
    vector_store_factory = get_vector_store_factory(
        vector_store_provider=VectorStoreProvider.OPEN_SEARCH,
        embedding_function=em_factory.get_embedding_model(),
        index_name=query.document_index_name,
    )
    compressor = get_compressor_factory(param=query.document_compressor_params)

    logger.debug('RAG chain - Create a ConversationalRetrievalChain from LLM')
    compressor = compressor.get_compressor()
    retriever = vector_store_factory.get_vector_store().as_retriever(
        search_kwargs=query.document_search_params.to_dict()
    )
    return ConversationalRetrievalChain.from_llm(
        llm=llm_factory.get_language_model(),
        retriever=ContextualCompressionRetriever(base_compressor=compressor, base_retriever=retriever),
        return_source_documents=True,
        return_generated_question=True,
        combine_docs_chain_kwargs={
            'prompt': PromptTemplate(
                template=llm_factory.setting.prompt,
                input_variables=__find_input_variables(llm_factory.setting.prompt),
            )
        },
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


def get_rag_documents(handler: RetrieverJsonCallbackHandler) -> List[RagDocument]:
    """
    Get documents used on RAG context

    Args:
        handler: the callback handler
    """

    on_chain_start_records = handler.show_records('on_chain_start_records')
    return [
        # Get first 100 char of content
        RagDocument(
            content=doc['page_content'][0:100] + '...',
            metadata=RagDocumentMetadata(**doc['metadata']),
        )
        for doc in on_chain_start_records[0]['inputs']['input_documents']
    ]


def get_condense_question(handler: RetrieverJsonCallbackHandler) -> Optional[str]:
    """Get the condensed question"""

    on_text_records = handler.show_records('on_text_records')
    # If the handler records 2 texts (prompts), this means that 2 LLM providers are invoked
    if len(on_text_records) == 2:
        # So the user question is condensed
        on_chain_start_records = handler.show_records('on_chain_start_records')
        return on_chain_start_records[0]['inputs']['question']
    else:
        # Else, the user's question was not formulated
        return None


def get_llm_prompts(handler: RetrieverJsonCallbackHandler) -> (Optional[str], str):
    """Get used llm prompt"""

    on_text_records = handler.show_records('on_text_records')
    # If the handler records 2 texts (prompts), this means that 2 LLM providers are invoked
    if len(on_text_records) == 2:
        return on_text_records[0]['text'], on_text_records[1]['text']

    # Else, only the LLM for "question answering" was invoked
    return None, on_text_records[0]['text']


def get_rag_debug_data(
        query, response, records_callback_handler, rag_duration
) -> RagDebugData:
    """RAG debug data assembly"""

    return RagDebugData(
        user_question=query.question_answering_prompt_inputs['question'],
        condense_question_prompt=get_llm_prompts(records_callback_handler)[0],
        condense_question=get_condense_question(records_callback_handler),
        question_answering_prompt=get_llm_prompts(records_callback_handler)[1],
        documents=get_rag_documents(records_callback_handler),
        document_index_name=query.document_index_name,
        document_search_params=query.document_search_params,
        answer=response['answer'],
        duration=rag_duration,
    )

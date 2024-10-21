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
import time
from functools import partial
from logging import ERROR, WARNING
from typing import List

from langchain.chains import ConversationalRetrievalChain
from langchain_community.chat_message_histories import ChatMessageHistory
from langchain_core.documents import Document
from langchain_core.output_parsers import StrOutputParser
from langchain_core.prompts import PromptTemplate as LangChainPromptTemplate, ChatPromptTemplate, MessagesPlaceholder
from langchain_core.runnables import RunnablePassthrough, RunnableParallel

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
from gen_ai_orchestrator.routers.requests.requests import RagQuery
from gen_ai_orchestrator.routers.responses.responses import RagResponse
from gen_ai_orchestrator.services.langchain.callbacks.rag_callback_handler import (
    RAGCallbackHandler,
)
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    get_em_factory,
    get_llm_factory,
    get_vector_store_factory, create_observability_callback_handler,
)
from gen_ai_orchestrator.services.utils.prompt_utility import validate_prompt_template

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
        **query.question_answering_prompt.inputs,
        'chat_history': message_history.messages,
    }

    logger.debug(
        'RAG chain - Use RetrieverJsonCallbackHandler for debugging : %s',
        debug,
    )

    callback_handlers = []
    records_callback_handler = RAGCallbackHandler()
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
    rag_guard(inputs, response)

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
                        url=doc.metadata['source'],
                        content=get_source_content(doc),
                    ),
                    response['documents'],
                )
            ),
        ),
        debug=get_rag_debug_data(
            query, records_callback_handler, rag_duration
        )
        if debug
        else None
    )

def get_source_content(doc: Document) -> str:
    """
    Find and delete the title followed by two line breaks

    The concatenation model used  is {title}\n\n{content_page}.
    It is also used in chain_rag.py on the orchestrator server, when fetching sources.
    The aim is to remove the ‘title’ prefix from the document content when sending the sources.

    """
    title_prefix = f"{doc.metadata['title']}\n\n"
    if doc.page_content.startswith(title_prefix):
        return doc.page_content[len(title_prefix):]
    else:
        return doc.page_content


def create_rag_chain(query: RagQuery) -> ConversationalRetrievalChain:
    """
    Create the RAG chain from RagQuery, using the LLM and Embedding settings specified in the query.

    Args:
        query: The RAG query
    Returns:
        The RAG chain.
    """

    llm_factory = get_llm_factory(setting=query.question_answering_llm_setting)
    em_factory = get_em_factory(setting=query.embedding_question_em_setting)
    vector_store_factory = get_vector_store_factory(
        setting=query.vector_store_setting,
        index_name=query.document_index_name,
        embedding_function=em_factory.get_embedding_model()
    )
    retriever = vector_store_factory.get_vector_store_retriever(query.document_search_params.to_dict())

    # Log progress and validate prompt template
    logger.info('RAG chain - Validating LLM prompt template')
    validate_prompt_template(query.question_answering_prompt)

    logger.debug('RAG chain - Document index name: %s', query.document_index_name)

    # Build LLM and prompt templates
    llm = llm_factory.get_language_model()
    rag_prompt = build_rag_prompt(query)

    # Construct the RAG chain using the prompt and LLM
    rag_chain = construct_rag_chain(llm, rag_prompt)

    # Build the chat chain for question contextualization
    chat_chain = build_chat_chain(llm)

    # Function to contextualize the question based on chat history
    contextualize_question_fn = partial(contextualize_question, chat_chain=chat_chain)

    # Final RAG chain with retriever and source documents
    rag_chain_with_source = contextualize_question_fn | RunnableParallel(
        {"question": RunnablePassthrough(), "documents": retriever}
    ).assign(answer=rag_chain)

    return rag_chain_with_source


def build_rag_prompt(query: RagQuery) -> LangChainPromptTemplate:
    """
    Build the RAG prompt template.
    """
    return LangChainPromptTemplate.from_template(
        template=query.question_answering_prompt.template,
        template_format=query.question_answering_prompt.formatter.value,
        partial_variables=query.question_answering_prompt.inputs
    )

def construct_rag_chain(llm, rag_prompt):
    """
    Construct the RAG chain from LLM and prompt.
    """
    return {
        "context": lambda x: "\n\n".join(doc.page_content for doc in x["documents"])
    } | rag_prompt | llm | StrOutputParser(name="rag_chain_output")

def build_chat_chain(llm) -> ChatPromptTemplate:
    """
    Build the chat chain for contextualizing questions.
    """
    return ChatPromptTemplate.from_messages([
        ("system", """Given a chat history and the latest user question which might reference context in \
        the chat history, formulate a standalone question which can be understood without the chat history. \
        Do NOT answer the question, just reformulate it if needed and otherwise return it as is."""),
        MessagesPlaceholder(variable_name="chat_history"),
        ("human", "{question}"),
    ]) | llm | StrOutputParser(name="chat_chain_output")

def contextualize_question(inputs: dict, chat_chain) -> str:
    """
    Contextualize the question based on the chat history.
    """
    if inputs.get("chat_history") and len(inputs["chat_history"]) > 0:
        return chat_chain
    return inputs["question"]

def rag_guard(inputs, response):
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
                and response['documents'] == []
        ):
            message = 'The RAG gives an answer when no document has been found!'
            rag_log(level=ERROR, message=message, inputs=inputs, response=response)
            raise GenAIGuardCheckException(ErrorInfo(cause=message))

        if (
                response['answer'] == inputs['no_answer']
                and response['documents'] != []
        ):
            message = 'The RAG gives no answer for user question, but some documents has been found!'
            rag_log(level=WARNING, message=message, inputs=inputs, response=response)
            # Remove source documents
            response['documents'] = []


def rag_log(level, message, inputs, response):
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
            'documents': response['documents'],
        },
    )


def get_rag_documents(handler: RAGCallbackHandler) -> List[RagDocument]:
    """
    Get documents used on RAG context

    Args:
        handler: the callback handler
    """

    return [
        # Get first 100 char of content
        RagDocument(
            content=doc.page_content[0:len(doc.metadata['title'])+100] + '...',
            metadata=RagDocumentMetadata(**doc.metadata),
        )
        for doc in handler.records['documents']
    ]


def get_rag_debug_data(
        query: RagQuery, records_callback_handler: RAGCallbackHandler, rag_duration
) -> RagDebugData:
    """RAG debug data assembly"""

    return RagDebugData(
        user_question=query.question_answering_prompt.inputs['question'],
        condense_question_prompt=records_callback_handler.records['chat_prompt'],
        condense_question=records_callback_handler.records['chat_chain_output'],
        question_answering_prompt=records_callback_handler.records['rag_prompt'],
        documents=get_rag_documents(records_callback_handler),
        document_index_name=query.document_index_name,
        document_search_params=query.document_search_params,
        answer=records_callback_handler.records['rag_chain_output'],
        duration=rag_duration,
    )

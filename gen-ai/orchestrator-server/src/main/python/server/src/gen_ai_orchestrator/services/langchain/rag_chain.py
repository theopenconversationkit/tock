#   Copyright (C) 2023-2025 Credit Mutuel Arkea
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

import json
import logging
import time
from functools import partial
from logging import ERROR, WARNING
from typing import List, Optional

from langchain.chains.conversational_retrieval.base import (
    ConversationalRetrievalChain,
)
from langchain.retrievers.contextual_compression import (
    ContextualCompressionRetriever,
)
from langchain_community.chat_message_histories import ChatMessageHistory
from langchain_core.callbacks import BaseCallbackHandler
from langchain_core.documents import Document
from langchain_core.output_parsers import StrOutputParser, JsonOutputParser
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.prompts import PromptTemplate as LangChainPromptTemplate
from langchain_core.runnables import (
    RunnableParallel,
    RunnablePassthrough,
    RunnableSerializable,
)
from langchain_core.vectorstores import VectorStoreRetriever
from langfuse.callback import CallbackHandler as LangfuseCallbackHandler
from typing_extensions import Any

from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIGuardCheckException,
)
from gen_ai_orchestrator.errors.handlers.openai.openai_exception_handler import (
    openai_exception_handler,
)
from gen_ai_orchestrator.errors.handlers.opensearch.opensearch_exception_handler import (
    opensearch_exception_handler,
)
from gen_ai_orchestrator.models.document_compressor.document_compressor_setting import (
    BaseDocumentCompressorSetting,
)
from gen_ai_orchestrator.models.errors.errors_models import ErrorInfo
from gen_ai_orchestrator.models.observability.observability_trace import (
    ObservabilityTrace,
)
from gen_ai_orchestrator.models.prompt.prompt_formatter import PromptFormatter
from gen_ai_orchestrator.models.prompt.prompt_template import PromptTemplate
from gen_ai_orchestrator.models.rag.rag_models import (
    ChatMessageType,
    Footnote,
    RAGDebugData,
    RAGDocument,
    RAGDocumentMetadata,
    LLMAnswer,
)
from gen_ai_orchestrator.routers.requests.requests import RAGRequest
from gen_ai_orchestrator.routers.responses.responses import (
    ObservabilityInfo,
    RAGResponse,
)
from gen_ai_orchestrator.services.langchain.callbacks.rag_callback_handler import (
    RAGCallbackHandler,
)
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    create_observability_callback_handler,
    get_compressor_factory,
    get_em_factory,
    get_guardrail_factory,
    get_llm_factory,
    get_vector_store_factory,
)
from gen_ai_orchestrator.services.observability.observabilty_service import (
    get_observability_info,
)
from gen_ai_orchestrator.services.utils.prompt_utility import (
    validate_prompt_template,
)

logger = logging.getLogger(__name__)

@opensearch_exception_handler
@openai_exception_handler(provider='OpenAI or AzureOpenAIService')
async def execute_rag_chain(
    request: RAGRequest,
    debug: bool,
    custom_observability_handler: Optional[BaseCallbackHandler] = None,
) -> RAGResponse:
    """
    RAG chain execution, using the LLM and Embedding settings specified in the request

    Args:
        request: The RAG request
        debug: True if RAG data debug should be returned with the response.
        custom_observability_handler: Custom observability handler
    Returns:
        The RAG response (Answer and document sources)
    """

    logger.info('RAG chain - Start of execution...')
    start_time = time.time()

    conversational_retrieval_chain = create_rag_chain(request=request)

    message_history = ChatMessageHistory()
    session_id = None
    user_id = None
    tags = []
    if request.dialog:
        for msg in request.dialog.history:
            if ChatMessageType.HUMAN == msg.type:
                message_history.add_user_message(msg.text)
            else:
                message_history.add_ai_message(msg.text)
        session_id = (request.dialog.dialog_id,)
        user_id = (request.dialog.user_id,)
        tags = (request.dialog.tags,)

    logger.debug(
        'RAG chain - Use chat history: %s',
        'Yes' if len(message_history.messages) > 0 else 'No',
    )

    inputs = {
        **request.question_answering_prompt.inputs,
        'chat_history': message_history.messages,
    }

    logger.debug(
        'RAG chain - Use RAGCallbackHandler for debugging : %s',
        debug,
    )

    callback_handlers = []
    records_callback_handler = RAGCallbackHandler()
    observability_handler = None
    if debug:
        # Debug callback handler
        callback_handlers.append(records_callback_handler)
    if custom_observability_handler is not None:
        callback_handlers.append(custom_observability_handler)
    if request.observability_setting is not None:
        # Langfuse callback handler
        observability_handler = create_observability_callback_handler(
            observability_setting=request.observability_setting,
            trace_name=ObservabilityTrace.RAG.value,
            session_id=session_id,
            user_id=user_id,
            tags=tags,
        )
        callback_handlers.append(observability_handler)

    response = await conversational_retrieval_chain.ainvoke(
        input=inputs,
        config={'callbacks': callback_handlers},
    )
    llm_answer = LLMAnswer(**response['answer'])

    # RAG Guard
    rag_guard(inputs, llm_answer, response, request.documents_required)

    # Guardrail
    if request.guardrail_setting:
        guardrail = get_guardrail_factory(
            setting=request.guardrail_setting
        ).get_parser()
        guardrail_output = guardrail.parse(llm_answer.answer)
        check_guardrail_output(guardrail_output)

    # Calculation of RAG processing time
    rag_duration = '{:.2f}'.format(time.time() - start_time)
    logger.info('RAG chain - End of execution. (Duration : %s seconds)', rag_duration)

    # Returning RAG response
    return RAGResponse(
        answer=llm_answer,
        footnotes={
            Footnote(
                identifier=doc.metadata['id'],
                title=doc.metadata['title'],
                url=doc.metadata['source'],
                content=get_source_content(doc),
                score=doc.metadata.get('retriever_score', None),
            )
            for doc, ctx in zip(response["documents"], llm_answer.context)
            if ctx.sentences
        },
        observability_info=get_observability_info(observability_handler),
        debug=get_rag_debug_data(request, records_callback_handler, rag_duration)
        if debug
        else None,
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
        return doc.page_content[len(title_prefix) :]
    else:
        return doc.page_content


def create_rag_chain(
    request: RAGRequest, vector_db_async_mode: Optional[bool] = True
) -> RunnableSerializable[Any, dict[str, Any]]:
    """
    Create the RAG chain from RAGRequest, using the LLM and Embedding settings specified in the request.

    Args:
        request: The RAG request
        vector_db_async_mode: enable/disable the async_mode for vector DB client (if supported). Default to True.
    Returns:
        The RAG chain.
    """

    # Log progress and validate prompt template
    logger.info('RAG chain - Validating LLM prompt template')
    validate_prompt_template(
        request.question_answering_prompt, 'Question answering prompt'
    )
    if request.question_condensing_prompt is not None:
        validate_prompt_template(
            request.question_condensing_prompt, 'Question condensing prompt'
        )

    question_condensing_llm_factory = None
    if request.question_condensing_llm_setting is not None:
        question_condensing_llm_factory = get_llm_factory(
            setting=request.question_condensing_llm_setting
        )
    question_answering_llm_factory = get_llm_factory(
        setting=request.question_answering_llm_setting
    )
    em_factory = get_em_factory(setting=request.embedding_question_em_setting)
    vector_store_factory = get_vector_store_factory(
        setting=request.vector_store_setting,
        index_name=request.document_index_name,
        embedding_function=em_factory.get_embedding_model(),
    )

    retriever = vector_store_factory.get_vector_store_retriever(
        search_kwargs=request.document_search_params.to_dict(),
        async_mode=vector_db_async_mode,
    )
    if request.compressor_setting:
        retriever = add_document_compressor(retriever, request.compressor_setting)

    logger.debug('RAG chain - Document index name: %s', request.document_index_name)

    # Build LLM and prompt templates
    question_condensing_llm = None
    if question_condensing_llm_factory is not None:
        question_condensing_llm = question_condensing_llm_factory.get_language_model()
    question_answering_llm = question_answering_llm_factory.get_language_model()
    rag_prompt = build_rag_prompt(request)

    # Construct the RAG chain using the prompt and LLM,
    # This chain will consume the documents retrieved by the retriever as input.
    rag_chain = construct_rag_chain(question_answering_llm, rag_prompt)

    # Build the chat chain for question contextualization
    chat_chain = build_question_condensation_chain(
        question_condensing_llm
        if question_condensing_llm is not None
        else question_answering_llm,
        request.question_condensing_prompt,
    )

    # Function to contextualize the question based on chat history
    contextualize_question_fn = partial(contextualize_question, chat_chain=chat_chain)

    # Final RAG chain with retriever and source documents
    rag_chain_with_retriever = (
        contextualize_question_fn
        | RunnableParallel({'documents': retriever, 'question': RunnablePassthrough()})
        | RunnablePassthrough.assign(answer=rag_chain)
    )

    return rag_chain_with_retriever


def build_rag_prompt(request: RAGRequest) -> LangChainPromptTemplate:
    """
    Build the RAG prompt template.
    """
    return LangChainPromptTemplate.from_template(
        template=request.question_answering_prompt.template,
        template_format=request.question_answering_prompt.formatter.value,
        partial_variables=request.question_answering_prompt.inputs,
    )


def construct_rag_chain(llm, rag_prompt):
    """
    Construct the RAG chain from LLM and prompt.
    """
    return (
        {
            'context': lambda inputs: '\n\n'.join(
                doc.page_content for doc in inputs['documents']
            ),
            'question': lambda inputs: inputs[
                'question'
            ],  # Override the user's original question with the condensed one
        }
        | rag_prompt
        | llm
        | JsonOutputParser(pydantic_object=LLMAnswer, name='rag_chain_output')
    )
# TODO MASS
# https://medium.com/@saurabhzodex/memory-enhanced-rag-chatbot-with-langchain-integrating-chat-history-for-context-aware-845100184c4f
def build_question_condensation_chain(
    llm, prompt: Optional[PromptTemplate]
) -> ChatPromptTemplate:
    """
    Build the chat chain for contextualizing questions.
    """
    if prompt is None:
        # Default prompt
        prompt = PromptTemplate(
            formatter=PromptFormatter.F_STRING,
            inputs={},
            template='Given a chat history and the latest user question which might reference context in \
the chat history, formulate a standalone question which can be understood without the chat history. \
Do NOT answer the question, just reformulate it if needed and otherwise return it as is.',
        )

    # TODO MASS: a mettre ds la default config
        prompt.template = """You are an assistant whose sole task is to STRICTLY REFORMULATE the user's latest message into a single standalone phrase, in the same language, while respecting the STYLE (register, tone, capitalization, level of formality). Apply the following rules:

        1) Question detection: treat the message as a question if it ends with a question mark, or contains an interrogative word (e.g. who, what, which, how, why, where, when, how much), or has an interrogative construction (e.g. "is it", "can you", "could you", "would you").  
           - IF it is a question → REFORMULATE it AS A QUESTION. Preserve tone, register (formal/informal, you vs. thou, etc.), capitalization. Add a single "?" at the end if necessary. Do not add any new information.

        2) IF the message is NOT a question (greeting, statement, command, fragment) → REFORMULATE WITHOUT TURNING IT INTO A QUESTION. Do not rephrase it into interrogative form.

        3) Do not guess: if the message is short, vague, or incomplete (e.g. "to understand", "the rate"), DO NOT invent missing meaning. Return it as is or make minimal grammatical adjustments — but do not add content.

        4) References to context: if the message is anaphoric (e.g. "and the rate?") AND the provided chat history clearly contains the antecedent, you may replace the anaphor with a minimal standalone version (e.g. "What is the interest rate?"). If the antecedent is absent or unclear → leave the message unchanged.

        5) Do not answer the message. Do not provide explanations, labels, or metadata. Return **only** the reformulated phrase (a single line).
        """

    return (
        ChatPromptTemplate.from_messages(
            [
                ('system', prompt.template),
                MessagesPlaceholder(variable_name='chat_history'),
                ('human', '{question}'),
            ]
        ).partial(**prompt.inputs)
        | llm
        | StrOutputParser(name='chat_chain_output')
    )


def contextualize_question(inputs: dict, chat_chain) -> str:
    """
    Contextualize the question based on the chat history.
    """
    if inputs.get('chat_history') and len(inputs['chat_history']) > 0:
        return chat_chain
    return inputs['question']


def rag_guard(question, answer, response, documents_required):
    """
    Validates the RAG system's response based on the presence or absence of source documents
    and the `documentsRequired` setting.

    Args:
        question: user question
        answer: the LLM answer
        response: the RAG response
        documents_required (bool): Specifies whether documents are mandatory for the response.
    """

    if documents_required and answer.status == "found_in_context" and len(response['documents']) == 0:
        message = 'No documents were retrieved, yet an answer was attempted.'
        rag_log(level=ERROR, message=message, question=question, answer=answer.answer, response=response)
        raise GenAIGuardCheckException(ErrorInfo(cause=message))

    if answer.status == "not_found_in_context" and len(response['documents']) > 0:
        # If the answer is not found in context and some documents are retrieved, so we remove them from the RAG response.
        message = 'No answer found in the retrieved context. The documents are therefore removed from the RAG response.'
        rag_log(level=WARNING, message=message, question=question, answer=answer.answer, response=response)
        response['documents'] = []


def rag_log(level, message, question, answer, response):
    """
    RAG logging

    Args:
        level: logging level
        message: message to log
        question: question answering prompt inputs
        answer: LLM answer
        response: the RAG response
    """

    logger.log(
        level,
        '%(message)s \n'
        'RAG chain - question="%(question)s", answer="%(answer)s", documents="%(documents)s"',
        {
            'message': message,
            'question': question,
            'answer': answer,
            'documents': len(response['documents']),
        },
    )


def get_rag_documents(handler: RAGCallbackHandler) -> List[RAGDocument]:
    """
    Get documents used on RAG context

    Args:
        handler: the RAG Callback Handler
    """

    return [
        # Get first 100 char of content
        RAGDocument(
            content=doc.page_content[0 : len(doc.metadata['title']) + 100] + '...',
            metadata=RAGDocumentMetadata(**doc.metadata),
        )
        for doc in handler.records['documents']
    ]

def get_llm_answer(rag_chain_output) -> LLMAnswer:
    return LLMAnswer(**json.loads(rag_chain_output.strip().removeprefix("```json").removesuffix("```").strip()))

def get_rag_debug_data(
    request: RAGRequest, records_callback_handler: RAGCallbackHandler, rag_duration
) -> RAGDebugData:
    """RAG debug data assembly"""

    history = []
    if request.dialog:
        history = request.dialog.history

    return RAGDebugData(
        user_question=request.question_answering_prompt.inputs['question'],
        question_condensing_prompt=records_callback_handler.records['chat_prompt'],
        question_condensing_history=history,
        condensed_question=records_callback_handler.records['chat_chain_output'],
        question_answering_prompt=records_callback_handler.records['rag_prompt'],
        documents=get_rag_documents(records_callback_handler),
        document_index_name=request.document_index_name,
        document_search_params=request.document_search_params,
        answer=get_llm_answer(records_callback_handler.records['rag_chain_output']),
        duration=rag_duration,
    )


def check_guardrail_output(guardrail_output: dict) -> bool:
    """Checks if the guardrail detected toxicities.
    Args:
        guardrail_output: The guardrail output dictionnary
    Returns:
        Returns True if nothing is detected, raises an exception otherwise.
    """
    if guardrail_output['output_toxicity']:
        message = f"Toxicity detected in LLM output ({','.join(guardrail_output['output_toxicity_reason'])})"
        raise GenAIGuardCheckException(ErrorInfo(cause=message))
    return True


def add_document_compressor(
    retriever: VectorStoreRetriever, compressor_settings: BaseDocumentCompressorSetting
) -> ContextualCompressionRetriever:
    """
    Adds a compressor to the retriever.
    Args:
        retriever : the Base retriever
        compressor_settings : the compressor settings
    Returns:
        New retriever with compressing feature.
    """
    compressor = get_compressor_factory(setting=compressor_settings).get_compressor()

    return ContextualCompressionRetriever(
        base_retriever=retriever,
        base_compressor=compressor,
    )

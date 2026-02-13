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
Module for the RAG Chain
It uses LangChain to perform a Conversational Retrieval Chain
"""

import json
import logging
import time
from functools import partial
from operator import itemgetter
from typing import List, Optional, Tuple

from langchain.retrievers.contextual_compression import (
    ContextualCompressionRetriever,
)
from langchain_community.chat_message_histories import ChatMessageHistory
from langchain_core.callbacks import BaseCallbackHandler
from langchain_core.documents import Document
from langchain_core.messages import AIMessage, HumanMessage
from langchain_core.output_parsers import JsonOutputParser, StrOutputParser
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.prompts import PromptTemplate as LangChainPromptTemplate
from langchain_core.runnables import (
    RunnableConfig,
    RunnableLambda,
    RunnableParallel,
    RunnablePassthrough,
    RunnableSerializable,
)
from langchain_core.runnables.config import RunnableConfig
from langchain_core.vectorstores import VectorStoreRetriever
from langfuse import get_client, propagate_attributes
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
    LLMAnswer,
    RAGDebugData,
    RAGDocument,
    RAGDocumentMetadata,
)
from gen_ai_orchestrator.routers.requests.requests import RAGRequest
from gen_ai_orchestrator.routers.responses.responses import RAGResponse
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
        custom_observability_handler: Custom observability handler (Used in the tooling run_experiment.py script)
    Returns:
        The RAG response (Answer and document sources)
    """

    logger.info('RAG chain - Start of execution...')
    start_time = time.time()

    conversational_retrieval_chain = create_rag_chain(
        request=request, vector_db_async_mode=False
    )

    message_history = ChatMessageHistory()
    metadata = {}

    if request.dialog:
        for msg in request.dialog.history:
            if ChatMessageType.HUMAN == msg.type:
                message_history.add_user_message(msg.text)
            else:
                message_history.add_ai_message(msg.text)
        session_id = request.dialog.dialog_id
        user_id = request.dialog.user_id
        tags = request.dialog.tags or []

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
        )
        callback_handlers.append(observability_handler)

    metadata = {}
    if user_id is not None:
        metadata['langfuse_user_id'] = user_id
    if session_id is not None:
        metadata['langfuse_session_id'] = session_id
    if tags:
        metadata['langfuse_tags'] = tags

    response = await conversational_retrieval_chain.ainvoke(
        input=inputs,
        config=RunnableConfig(
            callbacks=callback_handlers,
            metadata=metadata,
        ),
    )
    llm_answer = LLMAnswer(**response['answer'])

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

    # Group contexts by chunk id
    contexts_by_chunk = {
        ctx.chunk: ctx
        for ctx in (llm_answer.context_usage or [])
        if ctx.used_in_response
    }

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
            for doc in response['documents']
            if doc.metadata['id'] in contexts_by_chunk
        },
        observability_info=get_observability_info(
            observability_handler,
            ObservabilityTrace.RAG.value if observability_handler is not None else None,
        ),
        debug=get_rag_debug_data(request, records_callback_handler, rag_duration)
        if debug
        else None,
    )


def get_callback_handlers(
    request, debug
) -> Tuple[Optional[RAGCallbackHandler], Optional[object],]:
    records_handler = RAGCallbackHandler() if debug else None
    observability_handler = None

    if request.observability_setting is not None:
        if request.dialog:
            session_id = request.dialog.dialog_id
            user_id = request.dialog.user_id
            tags = request.dialog.tags
        else:
            session_id = None
            user_id = None
            tags = None
        observability_handler = create_observability_callback_handler(
            observability_setting=request.observability_setting,
            trace_name=ObservabilityTrace.RAG.value,
            session_id=session_id,
            user_id=user_id,
            tags=tags,
        )

    return (
        records_handler,
        observability_handler,
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

    # Fallback in case of missing condensing LLM setting using the answering LLM setting.
    if question_condensing_llm is not None:
        condensing_llm = question_condensing_llm
    else:
        condensing_llm = question_answering_llm

    # Build the chat chain for question contextualization
    chat_chain = build_question_condensation_chain(
        condensing_llm, request.question_condensing_prompt
    )
    rag_prompt = build_rag_prompt(request)

    # Function to contextualize the question based on chat history
    contextualize_question_fn = partial(contextualize_question, chat_chain=chat_chain)

    # Calculate the condensed question
    with_condensed_question = RunnableParallel(
        {
            'condensed_question': contextualize_question_fn,
            'question': itemgetter('question'),
            'chat_history': itemgetter('chat_history'),
        }
    )

    def retrieve_with_variants(inputs):
        variants = [
            # inputs["question"], Deactivated. It's an example to prove the multi retriever process
            inputs['condensed_question']
        ]
        docs = []
        for v in variants:
            docs.extend(retriever.invoke(v))
        # Deduplicate docs
        unique_docs = {d.metadata['id']: d for d in docs}

        # TODO [DERCBOT-1649] Apply the RRF Algo on unique_docs.
        return list(unique_docs.values())

    # Build the RAG inputs
    rag_inputs = with_condensed_question | RunnableParallel(
        {
            'question': itemgetter('condensed_question'),
            'chat_history': itemgetter('chat_history'),
            'documents': RunnableLambda(retrieve_with_variants),
        }
    )

    return rag_inputs | RunnablePassthrough.assign(
        answer=(
            {
                'context': lambda x: json.dumps(
                    [
                        {
                            'chunk_id': doc.metadata['id'],
                            'chunk_text': doc.page_content,
                        }
                        for doc in x['documents']
                    ],
                    ensure_ascii=False,
                    indent=2,
                ),
                'chat_history': format_chat_history,
            }
            | rag_prompt
            | question_answering_llm
            | JsonOutputParser(pydantic_object=LLMAnswer, name='rag_chain_output')
        )
    )


def build_rag_prompt(request: RAGRequest) -> LangChainPromptTemplate:
    """
    Build the RAG prompt template.
    """
    return LangChainPromptTemplate.from_template(
        template=request.question_answering_prompt.template,
        template_format=request.question_answering_prompt.formatter.value,
        partial_variables=request.question_answering_prompt.inputs,
    )


def format_chat_history(x):
    messages = []
    for msg in x['chat_history']:
        if isinstance(msg, HumanMessage):
            messages.append({'user': msg.content})
        elif isinstance(msg, AIMessage):
            messages.append({'assistant': msg.content})
    return json.dumps(messages, ensure_ascii=False, indent=2)


def build_question_condensation_chain(
    llm, prompt: Optional[PromptTemplate]
) -> ChatPromptTemplate:
    """
    Build the chat chain for contextualizing questions.
    """
    # TODO deprecated : All Gen configurations are supposed to have this prompt now. It is mandatory in the RAG configuration.
    if prompt is None:
        # Default prompt
        prompt = PromptTemplate(
            formatter=PromptFormatter.F_STRING,
            inputs={},
            template="""
You are a helpful assistant that reformulates questions.

You are given:
- The conversation history between the user and the assistant
- The most recent user question

Your task:
- Reformulate the user’s latest question into a clear, standalone query.
- Incorporate relevant context from the conversation history.
- Do NOT answer the question.
- If the history does not provide additional context, keep the question as is.

Return only the reformulated question.
""",
        )

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

    if handler.records['documents'] is None:
        return []

    return [
        # Get first 100 char of content
        RAGDocument(
            content=doc.page_content[0 : len(doc.metadata['title']) + 100] + '...',
            metadata=RAGDocumentMetadata(**doc.metadata),
        )
        for doc in handler.records['documents']
    ]


def get_llm_answer(rag_chain_output) -> LLMAnswer:
    if rag_chain_output is None:
        return LLMAnswer()

    return LLMAnswer(
        **json.loads(
            rag_chain_output.strip().removeprefix('```json').removesuffix('```').strip()
        )
    )


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

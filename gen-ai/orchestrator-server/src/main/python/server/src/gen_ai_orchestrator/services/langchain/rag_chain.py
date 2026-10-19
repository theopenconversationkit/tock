#   Copyright (C) 2026 Credit Mutuel Arkea
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
RAG Service
-----------
Entry point for the Retrieval-Augmented Generation pipeline.

Responsibilities
~~~~~~~~~~~~~~~~
* Build the LangChain conversational RAG chain.
* Populate the chat history from the dialog.
* Register callback handlers (debug, observability).
* Invoke the chain and apply the guardrail.
* Return a fully assembled RAGResponse.
"""

import json
import logging
import time
from typing import AsyncGenerator, Optional

from langchain_community.chat_message_histories import ChatMessageHistory
from langchain_core.callbacks import BaseCallbackHandler
from langchain_core.runnables.config import RunnableConfig

from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIGuardCheckException,
)
from gen_ai_orchestrator.errors.handlers.aws_bedrock.aws_bedrock_exception_handler import (
    aws_bedrock_exception_handler,
)
from gen_ai_orchestrator.errors.handlers.openai.openai_exception_handler import (
    openai_exception_handler,
)
from gen_ai_orchestrator.errors.handlers.opensearch.opensearch_exception_handler import (
    opensearch_exception_handler,
)
from gen_ai_orchestrator.models.errors.errors_models import ErrorInfo
from gen_ai_orchestrator.models.rag.rag_models import (
    ChatMessageType,
    LLMAnswer,
)
from gen_ai_orchestrator.routers.requests.requests import RAGRequest
from gen_ai_orchestrator.routers.responses.responses import RAGResponse
from gen_ai_orchestrator.services.langchain.callbacks.rag_callback_handler import (
    RAGCallbackHandler,
)
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    create_observability_callback_handler,
    get_guardrail_factory,
)
from gen_ai_orchestrator.services.langchain.rag_chain_builder import (
    create_rag_chain,
    create_rag_chain_parts,
)
from gen_ai_orchestrator.services.langchain.rag_response_builder import (
    build_rag_response,
)

logger = logging.getLogger(__name__)

# ---------------------------------------------------------------------------
# Chat history helpers
# ---------------------------------------------------------------------------


def build_message_history(request: RAGRequest) -> ChatMessageHistory:
    """Populate a ChatMessageHistory from the dialog history in the request."""
    history = ChatMessageHistory()
    if not request.dialog:
        return history

    for msg in request.dialog.history:
        if ChatMessageType.HUMAN == msg.type:
            history.add_user_message(msg.text)
        else:
            history.add_ai_message(msg.text)

    return history


def extract_dialog_metadata(request: RAGRequest) -> tuple[str | None, str | None, list]:
    """Return (session_id, user_id, tags) from the request dialog, if present."""
    if not request.dialog:
        return None, None, []
    return (
        request.dialog.dialog_id,
        request.dialog.user_id,
        request.dialog.tags or [],
    )


# ---------------------------------------------------------------------------
# Callback handler setup
# ---------------------------------------------------------------------------


def build_callback_handlers(
    request: RAGRequest,
    debug: bool,
    records_handler: RAGCallbackHandler,
    custom_handler: Optional[BaseCallbackHandler],
) -> tuple[list[BaseCallbackHandler], Optional[BaseCallbackHandler]]:
    """
    Build the list of LangChain callback handlers to attach to the chain.

    Returns (handlers_list, observability_handler) so the observability
    handler can be referenced later when building the response.
    """
    handlers: list[BaseCallbackHandler] = []
    observability_handler = None

    if debug:
        handlers.append(records_handler)

    if custom_handler is not None:
        handlers.append(custom_handler)

    if request.observability_setting is not None:
        observability_handler = create_observability_callback_handler(
            observability_setting=request.observability_setting,
        )
        handlers.append(observability_handler)

    return handlers, observability_handler


def build_runnable_metadata(
    session_id: str | None,
    user_id: str | None,
    tags: list,
) -> dict:
    """Build the metadata dict forwarded to LangChain's RunnableConfig."""
    metadata = {}
    if user_id:
        metadata['langfuse_user_id'] = user_id
    if session_id:
        metadata['langfuse_session_id'] = session_id
    if tags:
        metadata['langfuse_tags'] = tags
    return metadata


# ---------------------------------------------------------------------------
# Main entry point
# ---------------------------------------------------------------------------


@opensearch_exception_handler
@aws_bedrock_exception_handler(provider='AwsBedrock')
@openai_exception_handler(provider='OpenAI or AzureOpenAIService')
async def execute_rag_chain(
    request: RAGRequest,
    debug: bool,
    custom_observability_handler: Optional[BaseCallbackHandler] = None,
) -> RAGResponse:
    """
    Execute the full RAG pipeline and return a RAGResponse.

    Steps
    -----
    1. Build the LangChain chain.
    2. Populate chat history & extract dialog metadata.
    3. Configure callback handlers.
    4. Invoke the chain.
    5. Run the guardrail check.
    6. Assemble and return the response.
    """
    logger.info('RAG chain - Start of execution...')
    start_time = time.time()

    chain = create_rag_chain(request=request)

    message_history = build_message_history(request)
    session_id, user_id, tags = extract_dialog_metadata(request)

    records_handler = RAGCallbackHandler()
    callback_handlers, observability_handler = build_callback_handlers(
        request=request,
        debug=debug,
        records_handler=records_handler,
        custom_handler=custom_observability_handler,
    )

    chain_output = await chain.ainvoke(
        input={
            **request.question_answering_prompt.inputs,
            'chat_history': message_history.messages,
        },
        config=RunnableConfig(
            callbacks=callback_handlers,
            metadata=build_runnable_metadata(session_id, user_id, tags),
        ),
    )

    llm_answer = LLMAnswer(**chain_output['answer'])

    # Guardrail
    if request.guardrail_setting:
        guardrail = get_guardrail_factory(
            setting=request.guardrail_setting
        ).get_parser()
        guardrail_output = guardrail.parse(llm_answer.answer)
        check_guardrail_output(guardrail_output)

    rag_duration = '{:.3f}'.format(time.time() - start_time)
    logger.info('RAG chain - End of execution. (Duration: %s seconds)', rag_duration)

    return build_rag_response(
        chain_output=chain_output,
        llm_answer=llm_answer,
        request=request,
        records_callback_handler=records_handler,
        observability_handler=observability_handler,
        rag_duration=float(rag_duration),
        debug=debug,
    )


@opensearch_exception_handler
@aws_bedrock_exception_handler(provider='AwsBedrock')
@openai_exception_handler(provider='OpenAI or AzureOpenAIService')
async def execute_rag_chain_stream(
    request: RAGRequest,
    debug: bool,
    custom_observability_handler: Optional[BaseCallbackHandler] = None,
) -> AsyncGenerator[dict, None]:
    """
    Execute the RAG pipeline and stream the answer via Server-Sent Events.

    Emits:
      - one 'token' event per new fragment of the answer text, extracted from
        the incremental output of the answer JsonOutputParser;
      - a final 'answer' event carrying the complete RAGResponse (JSON),
        OR an 'error' event if the post-stream guardrail blocks the answer.

    Guardrail: tokens are streamed as they are produced, so individual tokens
    cannot be blocked before being sent. The toxicity guardrail is therefore
    applied once on the fully assembled answer, before the terminal 'answer'
    event. When it triggers, an 'error' event is emitted instead of 'answer',
    so the canonical/final answer surfaced to the user is the error rather than
    the toxic content (same guarantee as the non-streaming POST /rag).
    """
    logger.info('RAG chain (stream) - Start of execution...')
    start_time = time.time()

    # The chain is split in two: retrieval/condensation cannot be streamed token
    # by token (intermediate RunnableParallel steps would receive partial inputs),
    # so it runs eagerly; only the answer generation is streamed.
    rag_inputs, answer_chain = create_rag_chain_parts(request=request)

    message_history = build_message_history(request)
    session_id, user_id, tags = extract_dialog_metadata(request)

    records_handler = RAGCallbackHandler()
    callback_handlers, observability_handler = build_callback_handlers(
        request=request,
        debug=debug,
        records_handler=records_handler,
        custom_handler=custom_observability_handler,
    )
    config = RunnableConfig(
        callbacks=callback_handlers,
        metadata=build_runnable_metadata(session_id, user_id, tags),
    )

    # 1. Retrieval + condensation (eager).
    prepared = await rag_inputs.ainvoke(
        input={
            **request.question_answering_prompt.inputs,
            'chat_history': message_history.messages,
        },
        config=config,
    )

    # 2. Answer generation (streamed token by token).
    answer_dict = {}
    previous_answer = ''

    async for event in answer_chain.astream_events(
        input=prepared, config=config, version='v2'
    ):
        kind = event['event']

        # Stream the incremental 'answer' field parsed from the QA JSON output.
        if kind == 'on_parser_stream' and event.get('name') == 'rag_chain_output':
            partial = event['data'].get('chunk') or {}
            current_answer = partial.get('answer') or ''
            if len(current_answer) > len(previous_answer):
                delta = current_answer[len(previous_answer):]
                previous_answer = current_answer
                yield {
                    'event': 'token',
                    'data': json.dumps(
                        {'type': 'token', 'content': delta}, ensure_ascii=False
                    ),
                }

        # The answer chain end carries the fully parsed LLMAnswer dict.
        elif kind == 'on_chain_end' and not event.get('parent_ids'):
            answer_dict = event['data']['output']

    chain_output = {**prepared, 'answer': answer_dict}
    llm_answer = LLMAnswer(**answer_dict)

    # Post-stream guardrail: validate the fully assembled answer. On toxicity, emit an
    # 'error' event instead of the 'answer' event so the final answer surfaced to the
    # user is the error, not the toxic content (parity with the non-streaming /rag route).
    if request.guardrail_setting:
        guardrail = get_guardrail_factory(
            setting=request.guardrail_setting
        ).get_parser()
        guardrail_output = guardrail.parse(llm_answer.answer)
        try:
            check_guardrail_output(guardrail_output)
        except GenAIGuardCheckException as exc:
            logger.warning('RAG chain (stream) - Guardrail blocked the answer.')
            yield {
                'event': 'error',
                'data': json.dumps(
                    {'message': exc.message, 'detail': exc.detail},
                    ensure_ascii=False,
                ),
            }
            return

    # In streaming mode the answer parser receives incremental chunks, never a full
    # AIMessage, so RAGCallbackHandler.on_chain_start never records 'rag_chain_output'.
    # build_rag_debug_data would then parse a None record into a default LLMAnswer
    # ({"display_answer": false}). Backfill the record with the fully parsed answer so the
    # debug.answer matches the top-level answer.
    if debug and not records_handler.records.get('rag_chain_output'):
        records_handler.records['rag_chain_output'] = json.dumps(
            answer_dict, ensure_ascii=False
        )

    rag_duration = float('{:.3f}'.format(time.time() - start_time))
    logger.info(
        'RAG chain (stream) - End of execution. (Duration: %s seconds)', rag_duration
    )

    response = build_rag_response(
        chain_output=chain_output,
        llm_answer=llm_answer,
        request=request,
        records_callback_handler=records_handler,
        observability_handler=observability_handler,
        rag_duration=rag_duration,
        debug=debug,
    )
    yield {
        'event': 'answer',
        'data': response.model_dump_json(exclude_none=True),
    }


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

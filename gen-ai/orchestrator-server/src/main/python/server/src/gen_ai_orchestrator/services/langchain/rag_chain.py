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

import logging
import time
from typing import Optional

from langchain_community.chat_message_histories import ChatMessageHistory
from langchain_core.callbacks import BaseCallbackHandler
from langchain_core.runnables.config import RunnableConfig

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

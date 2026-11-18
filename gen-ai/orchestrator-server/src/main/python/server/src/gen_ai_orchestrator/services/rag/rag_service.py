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
"""Module for the RAG Service"""

import json
import logging
from typing import AsyncGenerator

from fastapi.encoders import jsonable_encoder

from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIOrchestratorException,
    GenAIUnknownErrorException,
)
from gen_ai_orchestrator.errors.handlers.fastapi.fastapi_handler import (
    create_error_response,
)
from gen_ai_orchestrator.models.errors.errors_models import ErrorInfo
from gen_ai_orchestrator.routers.requests.requests import RAGRequest
from gen_ai_orchestrator.routers.responses.responses import RAGResponse
from gen_ai_orchestrator.services.langchain.rag_chain import (
    execute_rag_chain,
    execute_rag_chain_stream,
)

logger = logging.getLogger(__name__)


async def rag(request: RAGRequest, debug: bool) -> RAGResponse:
    """Launch execution of the RAG chain"""
    return await execute_rag_chain(request, debug)


async def rag_stream(
    request: RAGRequest, debug: bool
) -> AsyncGenerator[dict, None]:
    """Launch streaming execution of the RAG chain (Server-Sent Events)"""
    try:
        async for event in execute_rag_chain_stream(request, debug):
            yield event
    except Exception as exc:
        # The HTTP 200 is already sent: the error cannot go through the FastAPI
        # exception handlers, so it is reported as a terminal 'error' event.
        logger.error('RAG chain (stream) - error during streaming', exc_info=True)
        if not isinstance(exc, GenAIOrchestratorException):
            exc = GenAIUnknownErrorException(
                ErrorInfo(error=exc.__class__.__name__, cause=str(exc))
            )
        yield {
            'event': 'error',
            'data': json.dumps(
                jsonable_encoder(create_error_response(exc)), ensure_ascii=False
            ),
        }

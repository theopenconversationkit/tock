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

from typing import AsyncGenerator

from gen_ai_orchestrator.routers.requests.requests import RAGRequest
from gen_ai_orchestrator.routers.responses.responses import RAGResponse
from gen_ai_orchestrator.services.langchain.rag_chain import (
    execute_rag_chain,
    execute_rag_chain_stream,
)


async def rag(request: RAGRequest, debug: bool) -> RAGResponse:
    """Launch execution of the RAG chain"""
    return await execute_rag_chain(request, debug)


async def rag_stream(
    request: RAGRequest, debug: bool
) -> AsyncGenerator[dict, None]:
    """Launch streaming execution of the RAG chain (Server-Sent Events)"""
    async for event in execute_rag_chain_stream(request, debug):
        yield event

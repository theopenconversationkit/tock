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
"""Internal KB projection API, called by the admin worker."""

from fastapi import APIRouter

from gen_ai_orchestrator.routers.requests.knowledge_base_requests import (
    KnowledgeBaseDeleteRequest,
    KnowledgeBaseIndexRequest,
    KnowledgeBaseTargetRequest,
)
from gen_ai_orchestrator.routers.responses.knowledge_base_responses import (
    KnowledgeBaseRowsResponse,
    KnowledgeBaseWriteResponse,
)
from gen_ai_orchestrator.services.knowledge_base.knowledge_base_service import (
    delete_entries,
    index_entries,
    inspect_rows,
)

knowledge_base_router = APIRouter(prefix='/knowledge-base', tags=['Knowledge Base'])


@knowledge_base_router.post('/index')
async def index(request: KnowledgeBaseIndexRequest) -> KnowledgeBaseWriteResponse:
    return await index_entries(request)


@knowledge_base_router.post('/delete')
async def delete(request: KnowledgeBaseDeleteRequest) -> KnowledgeBaseWriteResponse:
    return await delete_entries(request)


@knowledge_base_router.post('/rows')
async def rows(request: KnowledgeBaseTargetRequest) -> KnowledgeBaseRowsResponse:
    return await inspect_rows(request)

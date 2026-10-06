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
"""Internal API used by the Tock admin server to inspect vector stores."""

from fastapi import APIRouter

from gen_ai_orchestrator.routers.requests.vector_store_inspection_requests import (
    VectorStoreInspectionCapabilitiesRequest,
    VectorStoreInspectionCondenseRequest,
    VectorStoreInspectionDocumentsRequest,
    VectorStoreInspectionIndexesRequest,
    VectorStoreInspectionSearchRequest,
)
from gen_ai_orchestrator.routers.responses.vector_store_inspection_responses import (
    CondenseResponse,
    IndexListResponse,
    SearchResponse,
    VectorStoreCapabilitiesResponse,
    VectorStoreInspectionDocumentsResponse,
)
from gen_ai_orchestrator.services.vector_store_inspection.vector_store_inspection_service import (
    condense,
    get_capabilities,
    get_documents,
    get_indexes,
    search,
)

vector_store_inspection_router = APIRouter(
    prefix='/vector-store-inspection', tags=['Vector Store Inspection']
)


@vector_store_inspection_router.post('/capabilities')
async def inspect_capabilities(
    request: VectorStoreInspectionCapabilitiesRequest,
) -> VectorStoreCapabilitiesResponse:
    return await get_capabilities(request)


@vector_store_inspection_router.post('/indexes')
async def inspect_indexes(
    request: VectorStoreInspectionIndexesRequest,
) -> IndexListResponse:
    return await get_indexes(request)


@vector_store_inspection_router.post('/documents')
async def inspect_documents(
    request: VectorStoreInspectionDocumentsRequest,
) -> VectorStoreInspectionDocumentsResponse:
    return await get_documents(request)


@vector_store_inspection_router.post('/condense')
async def inspect_condensation(
    request: VectorStoreInspectionCondenseRequest,
) -> CondenseResponse:
    return await condense(request)


@vector_store_inspection_router.post('/search')
async def inspect_search(
    request: VectorStoreInspectionSearchRequest,
) -> SearchResponse:
    return await search(request)

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
from gen_ai_orchestrator.routers.requests.vector_store_management_requests import (
    ChunkTargetRequest,
    DeleteChunkRequest,
    DeleteIndexRequest,
    IndexTargetRequest,
    SetEmbeddingModelRequest,
)
from gen_ai_orchestrator.routers.responses.vector_store_inspection_responses import (
    CondenseResponse,
    IndexListResponse,
    SearchResponse,
    VectorStoreCapabilitiesResponse,
    VectorStoreInspectionDocumentsResponse,
)
from gen_ai_orchestrator.routers.responses.vector_store_management_responses import (
    ChunkDescriptionResponse,
    IndexMetadataResponse,
    VectorStoreWriteResponse,
)
from gen_ai_orchestrator.services.vector_store_inspection import (
    vector_store_management_service as management,
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


@vector_store_inspection_router.post('/index-metadata')
async def index_metadata(request: IndexTargetRequest) -> IndexMetadataResponse:
    return await management.index_metadata(request)


@vector_store_inspection_router.post('/embedding-model')
async def set_embedding_model(
    request: SetEmbeddingModelRequest,
) -> IndexMetadataResponse:
    return await management.set_embedding_model(request)


@vector_store_inspection_router.post('/chunk')
async def describe_chunk(request: ChunkTargetRequest) -> ChunkDescriptionResponse:
    return await management.describe_chunk(request)


@vector_store_inspection_router.post('/delete-chunk')
async def delete_chunk(request: DeleteChunkRequest) -> VectorStoreWriteResponse:
    return await management.delete_chunk(request)


@vector_store_inspection_router.post('/delete-index')
async def delete_index(request: DeleteIndexRequest) -> VectorStoreWriteResponse:
    return await management.delete_index(request)

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
"""Scoped PGVector writes without implicitly creating collections or embedding content."""

import json

from langchain_community.embeddings import FakeEmbeddings
from sqlalchemy import text

from gen_ai_orchestrator.routers.requests.vector_store_management_requests import (
    ChunkTargetRequest,
    DeleteChunkRequest,
    DeleteIndexRequest,
    IndexTargetRequest,
    SetEmbeddingModelRequest,
)
from gen_ai_orchestrator.routers.responses.vector_store_management_responses import (
    ChunkDescriptionResponse,
    IndexMetadataResponse,
    VectorStoreWriteResponse,
)
from gen_ai_orchestrator.services.vector_store_inspection.vector_store_inspection_service import (
    _assert_allowed_index,
    _pg_factory,
)


def _factory(request: IndexTargetRequest):
    _assert_allowed_index(request.index_name, request.index_name_prefix)
    return _pg_factory(
        request.vector_store_setting, request.index_name, FakeEmbeddings(size=1)
    )


async def _collection(connection, name: str, lock: bool = False):
    if (
        await connection.execute(text("SELECT to_regclass('langchain_pg_collection')"))
    ).scalar() is None:
        return None
    statement = 'SELECT uuid, cmetadata FROM langchain_pg_collection WHERE name = :name'
    if lock:
        statement += ' FOR UPDATE'
    return (
        (await connection.execute(text(statement), {'name': name})).mappings().first()
    )


async def index_metadata(request: IndexTargetRequest) -> IndexMetadataResponse:
    async with _factory(request).pool.async_engine.connect() as connection:
        collection = await _collection(connection, request.index_name)
        if collection is None:
            return IndexMetadataResponse(error='index_not_found')
        return IndexMetadataResponse(metadata=collection['cmetadata'] or {})


async def set_embedding_model(
    request: SetEmbeddingModelRequest,
) -> IndexMetadataResponse:
    async with _factory(request).pool.async_engine.begin() as connection:
        collection = await _collection(connection, request.index_name, lock=True)
        if collection is None:
            return IndexMetadataResponse(error='index_not_found')
        metadata = dict(collection['cmetadata'] or {})
        existing = metadata.get('embedding_model')
        # Only null/missing/blank strings are absent. Malformed non-string values must not be overwritten.
        if existing is not None and (
            not isinstance(existing, str)
            or (existing.strip() and metadata.get('embedding_model_source') != 'manual')
        ):
            return IndexMetadataResponse(
                metadata=metadata, error='model_already_defined'
            )
        # Compare under the same lock: a stale add/edit must not overwrite a newer declaration.
        current = existing if isinstance(existing, str) and existing.strip() else None
        if request.expected_embedding_model != current:
            return IndexMetadataResponse(metadata=metadata, error='model_changed')
        metadata['embedding_model'] = request.embedding_model
        metadata['embedding_model_source'] = 'manual'
        await connection.execute(
            text(
                'UPDATE langchain_pg_collection SET cmetadata = CAST(:metadata AS json) WHERE uuid = :uuid'
            ),
            {'metadata': json.dumps(metadata), 'uuid': collection['uuid']},
        )
        return IndexMetadataResponse(metadata=metadata)


async def _chunk(connection, collection_id, row_id: str):
    return (
        (
            await connection.execute(
                text(
                    'SELECT cmetadata FROM langchain_pg_embedding WHERE collection_id = :collection AND id = :id'
                ),
                {'collection': collection_id, 'id': row_id},
            )
        )
        .mappings()
        .first()
    )


async def _count(connection, collection_id) -> int:
    return int(
        (
            await connection.execute(
                text(
                    'SELECT COUNT(*) FROM langchain_pg_embedding WHERE collection_id = :collection'
                ),
                {'collection': collection_id},
            )
        ).scalar_one()
    )


async def describe_chunk(request: ChunkTargetRequest) -> ChunkDescriptionResponse:
    async with _factory(request).pool.async_engine.connect() as connection:
        collection = await _collection(connection, request.index_name)
        if collection is None:
            return ChunkDescriptionResponse(error='index_not_found')
        chunk = await _chunk(connection, collection['uuid'], request.row_id)
        if chunk is None:
            return ChunkDescriptionResponse(error='chunk_not_found')
        return ChunkDescriptionResponse(
            metadata=chunk['cmetadata'] or {},
            chunk_count=await _count(connection, collection['uuid']),
        )


async def delete_chunk(request: DeleteChunkRequest) -> VectorStoreWriteResponse:
    async with _factory(request).pool.async_engine.begin() as connection:
        collection = await _collection(connection, request.index_name, lock=True)
        if collection is None:
            return VectorStoreWriteResponse(error='index_not_found')
        chunk = await _chunk(connection, collection['uuid'], request.row_id)
        if chunk is None:
            return VectorStoreWriteResponse(error='chunk_not_found')
        if (
            request.protect_knowledge_base
            and (chunk['cmetadata'] or {}).get('source_type') == 'internal_kb'
        ):
            return VectorStoreWriteResponse(error='knowledge_base_chunk')
        if (
            not request.allow_empty_index
            and await _count(connection, collection['uuid']) == 1
        ):
            return VectorStoreWriteResponse(error='confirmation_changed')
        result = await connection.execute(
            text(
                'DELETE FROM langchain_pg_embedding WHERE collection_id = :collection AND id = :id'
            ),
            {'collection': collection['uuid'], 'id': request.row_id},
        )
        return VectorStoreWriteResponse(deleted_count=result.rowcount)


async def delete_index(request: DeleteIndexRequest) -> VectorStoreWriteResponse:
    factory = _factory(request)
    if request.index_name == request.protected_index_name:
        return VectorStoreWriteResponse(error='current_index_protected')
    async with factory.pool.async_engine.begin() as connection:
        collection = await _collection(connection, request.index_name, lock=True)
        if collection is None:
            return VectorStoreWriteResponse(error='index_not_found')
        result = await connection.execute(
            text(
                'DELETE FROM langchain_pg_embedding WHERE collection_id = :collection'
            ),
            {'collection': collection['uuid']},
        )
        await connection.execute(
            text('DELETE FROM langchain_pg_collection WHERE uuid = :uuid'),
            {'uuid': collection['uuid']},
        )
        return VectorStoreWriteResponse(deleted_count=result.rowcount)

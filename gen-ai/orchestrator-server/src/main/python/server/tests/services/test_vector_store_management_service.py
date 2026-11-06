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
"""Real SQL regression tests, using an explicitly supplied disposable PostgreSQL database."""

import asyncio
import json
import os
from types import SimpleNamespace
from unittest.mock import patch
from uuid import uuid4

import pytest
import pytest_asyncio
from fastapi import HTTPException
from pydantic import ValidationError
from sqlalchemy import text
from sqlalchemy.ext.asyncio import create_async_engine

from gen_ai_orchestrator.routers.requests.vector_store_inspection_requests import (
    VectorStoreInspectionDocumentsRequest,
)
from gen_ai_orchestrator.routers.requests.vector_store_management_requests import (
    ChunkTargetRequest,
    DeleteChunkRequest,
    DeleteIndexRequest,
    IndexTargetRequest,
    SetEmbeddingModelRequest,
)
from gen_ai_orchestrator.services.vector_store_inspection import (
    vector_store_inspection_service as inspection,
)
from gen_ai_orchestrator.services.vector_store_inspection import (
    vector_store_management_service as service,
)

PREFIX = 'ns_test_bot_test_session_'
INDEX = PREFIX + 'one'
OTHER = PREFIX + 'two'


def target(**kwargs):
    return dict(index_name=INDEX, index_name_prefix=PREFIX, **kwargs)


def test_scope_is_checked_before_opening_a_store():
    with patch.object(service, '_pg_factory') as factory:
        with pytest.raises(HTTPException) as error:
            service._factory(
                IndexTargetRequest(index_name='another_bot', index_name_prefix=PREFIX)
            )
        assert error.value.status_code == 403
        factory.assert_not_called()


@pytest.mark.parametrize('value', ['', '   ', 'x' * 301])
def test_invalid_model_is_rejected(value):
    with pytest.raises(ValidationError):
        SetEmbeddingModelRequest(**target(embedding_model=value))


@pytest_asyncio.fixture
async def database():
    url = os.environ.get('VSI_TEST_PG_URL')
    if not url:
        pytest.skip('Set VSI_TEST_PG_URL to a disposable PostgreSQL database')
    bootstrap = create_async_engine(url)
    schema = 'vsi_' + uuid4().hex
    async with bootstrap.begin() as connection:
        await connection.execute(text(f'CREATE SCHEMA {schema}'))
    engine = create_async_engine(
        url, connect_args={'options': f'-csearch_path={schema}'}
    )
    try:
        async with engine.begin() as connection:
            await connection.execute(
                text("""CREATE TABLE langchain_pg_collection (
                uuid uuid PRIMARY KEY, name text UNIQUE NOT NULL, cmetadata json)""")
            )
            await connection.execute(
                text("""CREATE TABLE langchain_pg_embedding (
                id text PRIMARY KEY, collection_id uuid REFERENCES langchain_pg_collection(uuid) ON DELETE CASCADE,
                cmetadata jsonb, document text)""")
            )
            for name in [INDEX, OTHER]:
                await connection.execute(
                    text(
                        'INSERT INTO langchain_pg_collection VALUES (:uuid, :name, NULL)'
                    ),
                    {'uuid': uuid4(), 'name': name},
                )
            # Same logical chunk id in several physical rows: deleting one must preserve the others.
            for identifier, name, source in [
                ('row-1', INDEX, 'document'),
                ('row-2', INDEX, 'document'),
                ('other-row', OTHER, 'internal_kb'),
            ]:
                await connection.execute(
                    text("""INSERT INTO langchain_pg_embedding
                    SELECT :id, uuid, CAST(:metadata AS jsonb), 'content' FROM langchain_pg_collection WHERE name=:name"""),
                    {
                        'id': identifier,
                        'name': name,
                        'metadata': json.dumps(
                            {
                                'id': 'doc',
                                'title': 'Title',
                                'chunk': '1/1',
                                'source_type': source,
                                'kb_entry_id': 'entry',
                            }
                        ),
                    },
                )
        with patch.object(
            service,
            '_pg_factory',
            return_value=SimpleNamespace(pool=SimpleNamespace(async_engine=engine)),
        ):
            yield engine
    finally:
        await engine.dispose()
        async with bootstrap.begin() as connection:
            await connection.execute(text(f'DROP SCHEMA {schema} CASCADE'))
        await bootstrap.dispose()


@pytest.mark.asyncio
async def test_missing_index_is_never_created(database):
    missing = dict(index_name=PREFIX + 'missing', index_name_prefix=PREFIX)
    assert (
        await service.index_metadata(IndexTargetRequest(**missing))
    ).error == 'index_not_found'
    assert (
        await service.set_embedding_model(
            SetEmbeddingModelRequest(**missing, embedding_model='model')
        )
    ).error == 'index_not_found'
    assert (
        await service.delete_index(
            DeleteIndexRequest(**missing, protected_index_name=INDEX)
        )
    ).error == 'index_not_found'
    async with database.connect() as connection:
        assert (
            await connection.execute(
                text('SELECT COUNT(*) FROM langchain_pg_collection')
            )
        ).scalar_one() == 2


@pytest.mark.asyncio
async def test_legacy_metadata_and_unknown_fields_are_preserved(database):
    assert (await service.index_metadata(IndexTargetRequest(**target()))).metadata == {}
    async with database.begin() as connection:
        await connection.execute(
            text(
                'UPDATE langchain_pg_collection SET cmetadata=CAST(:metadata AS json) WHERE name=:name'
            ),
            {
                'name': INDEX,
                'metadata': json.dumps(
                    {'custom': {'nested': [1, 2]}, 'embedding_model': ' '}
                ),
            },
        )
    response = await service.set_embedding_model(
        SetEmbeddingModelRequest(**target(embedding_model=' historical-model '))
    )
    assert response.error is None
    assert response.metadata == {
        'custom': {'nested': [1, 2]},
        'embedding_model': 'historical-model',
    }
    assert (
        await service.set_embedding_model(
            SetEmbeddingModelRequest(**target(embedding_model='replacement'))
        )
    ).error == 'model_already_defined'


@pytest.mark.asyncio
async def test_two_simultaneous_writes_cannot_overwrite_a_model(database):
    results = await asyncio.gather(
        *[
            service.set_embedding_model(
                SetEmbeddingModelRequest(**target(embedding_model=model))
            )
            for model in ['first-model', 'second-model']
        ]
    )
    assert sum(result.error is None for result in results) == 1
    assert sum(result.error == 'model_already_defined' for result in results) == 1
    winner = next(
        result.metadata['embedding_model'] for result in results if result.error is None
    )
    assert (await service.index_metadata(IndexTargetRequest(**target()))).metadata[
        'embedding_model'
    ] == winner


@pytest.mark.asyncio
async def test_chunk_deletion_uses_physical_id_and_collection_scope(database):
    wrong_collection = await service.delete_chunk(
        DeleteChunkRequest(**target(row_id='other-row', protect_knowledge_base=False))
    )
    assert wrong_collection.error == 'chunk_not_found'
    result = await service.delete_chunk(
        DeleteChunkRequest(**target(row_id='row-1', protect_knowledge_base=True))
    )
    assert result.deleted_count == 1
    assert (
        await service.describe_chunk(ChunkTargetRequest(**target(row_id='row-2')))
    ).chunk_count == 1
    async with database.connect() as connection:
        assert set(
            (
                await connection.execute(text('SELECT id FROM langchain_pg_embedding'))
            ).scalars()
        ) == {'row-2', 'other-row'}


@pytest.mark.asyncio
async def test_last_chunk_requires_confirmation_and_keeps_empty_collection(database):
    await service.delete_chunk(
        DeleteChunkRequest(**target(row_id='row-1', protect_knowledge_base=True))
    )
    request = DeleteChunkRequest(**target(row_id='row-2', protect_knowledge_base=True))
    assert (await service.delete_chunk(request)).error == 'confirmation_changed'
    assert (
        await service.delete_chunk(
            request.model_copy(update={'allow_empty_index': True})
        )
    ).deleted_count == 1
    assert (await service.index_metadata(IndexTargetRequest(**target()))).error is None
    assert (
        await service.delete_index(
            DeleteIndexRequest(**target(protected_index_name=INDEX))
        )
    ).error == 'current_index_protected'


@pytest.mark.asyncio
async def test_current_kb_chunk_cannot_be_directly_deleted(database):
    request = DeleteChunkRequest(
        index_name=OTHER,
        index_name_prefix=PREFIX,
        row_id='other-row',
        protect_knowledge_base=True,
        allow_empty_index=True,
    )
    assert (await service.delete_chunk(request)).error == 'knowledge_base_chunk'
    # The same old-index copy is removable without changing its source.
    assert (
        await service.delete_chunk(
            request.model_copy(update={'protect_knowledge_base': False})
        )
    ).deleted_count == 1


@pytest.mark.asyncio
async def test_index_deletion_is_scoped_and_removes_all_its_rows(database):
    assert (
        await service.delete_index(
            DeleteIndexRequest(**target(protected_index_name=OTHER))
        )
    ).deleted_count == 2
    assert (
        await service.index_metadata(IndexTargetRequest(**target()))
    ).error == 'index_not_found'
    async with database.connect() as connection:
        assert (
            await connection.execute(text('SELECT name FROM langchain_pg_collection'))
        ).scalars().all() == [OTHER]
        assert (
            await connection.execute(text('SELECT id FROM langchain_pg_embedding'))
        ).scalars().all() == ['other-row']


@pytest.mark.asyncio
async def test_exploration_exposes_distinct_physical_ids_for_duplicate_logical_chunks(
    database,
):
    with patch.object(
        inspection,
        '_pg_factory',
        return_value=SimpleNamespace(pool=SimpleNamespace(async_engine=database)),
    ):
        response = await inspection.get_documents(
            VectorStoreInspectionDocumentsRequest(**target(), include_stats=False)
        )
    chunks = response.rows[0].chunks
    assert {chunk.row_id for chunk in chunks} == {'row-1', 'row-2'}
    assert {chunk.chunk_id for chunk in chunks} == {'doc:1/1'}


@pytest.mark.asyncio
@pytest.mark.parametrize(
    'filters, expected_ids',
    [
        ({'text': 'doc'}, ['doc']),
        ({'text': 'OC'}, ['doc']),
        ({'text': ' DOC '}, ['doc']),
        ({'text': 'tit'}, ['doc']),
        ({'text': 'ONT'}, ['doc']),
        ({'text': 'missing'}, []),
        ({'text': 'doc', 'document_id': 'other'}, []),
        ({'document_id': 'do'}, []),
    ],
)
async def test_exploration_text_filter_matches_document_id_title_or_content(
    database, filters, expected_ids
):
    with patch.object(
        inspection,
        '_pg_factory',
        return_value=SimpleNamespace(pool=SimpleNamespace(async_engine=database)),
    ):
        response = await inspection.get_documents(
            VectorStoreInspectionDocumentsRequest(**target(), filter=filters)
        )
    assert [row.document_id for row in response.rows] == expected_ids
    assert response.total == len(expected_ids)
    if expected_ids:
        # Matching by document ID keeps all its chunks, only in the selected index.
        assert {chunk.row_id for chunk in response.rows[0].chunks} == {
            'row-1',
            'row-2',
        }

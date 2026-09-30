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
"""Idempotent KB writes. Ordinary RAG documents, with no change to retrieval."""

import asyncio
import hashlib
import logging
from datetime import datetime, timezone

from fastapi import HTTPException
from langchain_core.documents import Document
from langchain_core.embeddings import FakeEmbeddings
from sqlalchemy import text

from gen_ai_orchestrator.routers.requests.knowledge_base_requests import (
    KnowledgeBaseDeleteRequest,
    KnowledgeBaseDocument,
    KnowledgeBaseIndexRequest,
    KnowledgeBaseTargetRequest,
)
from gen_ai_orchestrator.routers.responses.knowledge_base_responses import (
    KnowledgeBaseIndexStateResponse,
    KnowledgeBaseRowsResponse,
    KnowledgeBaseStoredRow,
    KnowledgeBaseWriteResponse,
    KnowledgeBaseWriteResult,
)
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    get_em_factory,
    get_vector_store_factory,
)
from gen_ai_orchestrator.services.langchain.factories.vector_stores.pgvector_factory import (
    PGVectorFactory,
)

logger = logging.getLogger(__name__)


def row_id(index_name: str, entry_id: str) -> str:
    # PGVector ids are unique across ALL collections, unlike OpenSearch ids.
    return 'kb-' + hashlib.sha256((index_name + '/' + entry_id).encode()).hexdigest()


def content_hash(content: str, source: str | None) -> str:
    return hashlib.sha256((content + '\n' + (source or '')).encode()).hexdigest()


def build_document(entry: KnowledgeBaseDocument, session: str) -> Document:
    hints = '\n'.join(entry.search_hints)
    body = (hints + '\n\n' if hints else '') + entry.content
    content = f'{entry.title}\n\n```markdown\n{body}\n```'
    return Document(
        page_content=content,
        metadata={
            'index_session_id': session,
            'index_datetime': datetime.now(timezone.utc).isoformat(),
            'id': entry.entry_id,
            'chunk': '1/1',
            'title': entry.title,
            'source': entry.source_url,
            'source_type': 'internal_kb',
            'kb_entry_id': entry.entry_id,
            'kb_content_hash': content_hash(content, entry.source_url),
        },
    )


def factory_for(request: KnowledgeBaseTargetRequest, embedding=None):
    if not request.index_name.startswith(request.index_name_prefix):
        raise HTTPException(403, 'Index is outside the current bot')
    return get_vector_store_factory(
        setting=request.vector_store_setting,
        index_name=request.index_name,
        embedding_function=embedding or FakeEmbeddings(size=1),
    )


def recognize_row(
    identifier, content: str | None, metadata: dict
) -> KnowledgeBaseStoredRow:
    """
    Pure recognition of one raw KB row (PGVector or OpenSearch shape) into a KnowledgeBaseStoredRow. A content_hash is
    only recomputed for a well-formed single-chunk row whose stored content still matches its title/source; anything
    else yields an empty hash, which the admin worker treats as "needs re-projection".
    """
    recomputable = (
        'source' in metadata
        and metadata.get('id') == metadata.get('kb_entry_id')
        and metadata.get('chunk') == '1/1'
        and (content or '').startswith(str(metadata.get('title', '')) + '\n\n')
    )
    return KnowledgeBaseStoredRow(
        row_id=str(identifier),
        entry_id=str(
            metadata.get('kb_entry_id') or metadata.get('id') or identifier
        ),
        content_hash=(
            content_hash(content or '', metadata.get('source')) if recomputable else ''
        ),
        title=str(metadata.get('title') or ''),
    )


async def stored_rows(factory) -> list[KnowledgeBaseStoredRow]:
    if isinstance(factory, PGVectorFactory):
        async with factory.pool.async_engine.connect() as connection:
            result = await connection.execute(
                text("""
                SELECT e.id, e.document, e.cmetadata
                FROM langchain_pg_embedding e
                JOIN langchain_pg_collection c ON c.uuid = e.collection_id
                WHERE c.name = :name AND e.cmetadata->>'source_type' = 'internal_kb'
            """),
                {'name': factory.index_name},
            )
            rows = [(r.id, r.document, r.cmetadata or {}) for r in result]
    else:

        def read():
            from opensearchpy.helpers import scan

            client = factory.get_vector_store().client
            if not client.indices.exists(index=factory.index_name):
                return []
            # The ingestion mapping may expose source_type as text or keyword.
            hits = scan(
                client,
                index=factory.index_name,
                query={
                    'query': {
                        'bool': {
                            'should': [
                                {
                                    'term': {
                                        'metadata.source_type.keyword': 'internal_kb'
                                    }
                                },
                                {'term': {'metadata.source_type': 'internal_kb'}},
                            ],
                            'minimum_should_match': 1,
                        }
                    }
                },
            )
            return [
                (
                    h['_id'],
                    h['_source'].get('text', ''),
                    h['_source'].get('metadata', {}),
                )
                for h in hits
            ]

        rows = await asyncio.to_thread(read)
    return [
        recognize_row(identifier, content, metadata)
        for identifier, content, metadata in rows
    ]


async def inspect_rows(
    request: KnowledgeBaseTargetRequest,
) -> KnowledgeBaseRowsResponse:
    return KnowledgeBaseRowsResponse(rows=await stored_rows(factory_for(request)))


async def collection_state(factory) -> KnowledgeBaseIndexStateResponse:
    """
    Read the state of the collection backing an index WITHOUT ever creating it. PGVector: a raw read of
    langchain_pg_collection / langchain_pg_embedding (going through PGVector would get_or_create the collection).
    OpenSearch: only existence is observable, so counts and cmetadata stay None.
    """
    if isinstance(factory, PGVectorFactory):
        async with factory.pool.async_engine.connect() as connection:
            # Fresh database: the langchain tables are created lazily on first write. Absent table => no collection,
            # so report exists=False instead of letting the SELECT raise an UndefinedTable error.
            if (
                await connection.execute(
                    text("SELECT to_regclass('langchain_pg_collection')")
                )
            ).scalar() is None:
                return KnowledgeBaseIndexStateResponse(exists=False)
            collection = (
                await connection.execute(
                    text(
                        'SELECT uuid, cmetadata FROM langchain_pg_collection WHERE name = :name'
                    ),
                    {'name': factory.index_name},
                )
            ).first()
            if collection is None:
                return KnowledgeBaseIndexStateResponse(exists=False)
            counts = (
                await connection.execute(
                    text("""
                    SELECT
                        count(*) AS total,
                        count(*) FILTER (WHERE cmetadata->>'source_type' = 'internal_kb') AS kb
                    FROM langchain_pg_embedding
                    WHERE collection_id = :uuid
                """),
                    {'uuid': collection.uuid},
                )
            ).first()
            return KnowledgeBaseIndexStateResponse(
                exists=True,
                row_count=int(counts.total),
                kb_row_count=int(counts.kb),
                cmetadata=collection.cmetadata or {},
            )

    def read():
        client = factory.get_vector_store().client
        return client.indices.exists(index=factory.index_name)

    exists = await asyncio.to_thread(read)
    return KnowledgeBaseIndexStateResponse(exists=bool(exists))


async def index_state(
    request: KnowledgeBaseTargetRequest,
) -> KnowledgeBaseIndexStateResponse:
    return await collection_state(factory_for(request))


async def index_entries(
    request: KnowledgeBaseIndexRequest,
) -> KnowledgeBaseWriteResponse:
    embedding = get_em_factory(request.em_setting).get_embedding_model()
    factory = factory_for(request, embedding)
    # Guard implicit creation: without collection_metadata (i.e. not a creation job), refuse to let PGVector's
    # get_or_create silently spawn an uncertified collection. A creation job carries the metadata and may create it.
    if isinstance(factory, PGVectorFactory):
        if request.collection_metadata is None and not (
            await collection_state(factory)
        ).exists:
            # Not a creation job and the collection is absent: refuse to let PGVector's
            # get_or_create silently spawn an uncertified collection. Surface a per-entry
            # signal so the worker reports a missing index rather than a generic failure.
            return KnowledgeBaseWriteResponse(
                results=[
                    KnowledgeBaseWriteResult(
                        entry_id=entry.entry_id,
                        error='knowledge-base.job.index_missing',
                    )
                    for entry in request.entries
                ]
            )
        store = factory.get_vector_store(
            collection_metadata=request.collection_metadata
        )
    else:
        # OpenSearch collections carry no Tock contract metadata; collection_metadata is a PGVector-only concept.
        store = factory.get_vector_store()
    results = []
    for entry in request.entries:
        identifier = row_id(request.index_name, entry.entry_id)
        try:
            await store.aadd_documents(
                [build_document(entry, request.index_session_id)], ids=[identifier]
            )
            results.append(
                KnowledgeBaseWriteResult(
                    entry_id=entry.entry_id, row_ids=[identifier], count=1
                )
            )
        except Exception:
            # Provider errors may include credentials, request bodies or connection strings.
            logger.warning('KB indexing failed for entry %s', entry.entry_id)
            results.append(
                KnowledgeBaseWriteResult(
                    entry_id=entry.entry_id, error='knowledge-base.job.index_failed'
                )
            )
    return KnowledgeBaseWriteResponse(results=results)


async def delete_entries(
    request: KnowledgeBaseDeleteRequest,
) -> KnowledgeBaseWriteResponse:
    factory = factory_for(request)
    owned = {row.row_id: row.entry_id for row in await stored_rows(factory)}
    results = []
    for entry in request.entries:
        ids = entry.row_ids or [row_id(request.index_name, entry.entry_id)]
        # Never allow a caller to remove documentary rows, even with a guessed id.
        ids = [
            identifier for identifier in ids if owned.get(identifier) == entry.entry_id
        ]
        try:
            if ids:
                if isinstance(factory, PGVectorFactory):
                    await factory.get_vector_store().adelete(ids, collection_only=True)
                else:
                    # The synchronous helper handles missing rows and refreshes before returning.
                    await asyncio.to_thread(factory.get_vector_store().delete, ids)
            results.append(
                KnowledgeBaseWriteResult(
                    entry_id=entry.entry_id, row_ids=ids, count=len(ids)
                )
            )
        except Exception:
            logger.warning('KB deletion failed for entry %s', entry.entry_id)
            results.append(
                KnowledgeBaseWriteResult(
                    entry_id=entry.entry_id, error='knowledge-base.job.delete_failed'
                )
            )
    return KnowledgeBaseWriteResponse(results=results)

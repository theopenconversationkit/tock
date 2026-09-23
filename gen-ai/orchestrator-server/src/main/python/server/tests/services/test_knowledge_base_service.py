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
import os
from types import SimpleNamespace
from unittest.mock import AsyncMock, MagicMock, patch

import pytest
from fastapi import HTTPException
from gen_ai_orchestrator.routers.requests.knowledge_base_requests import (
    KnowledgeBaseDeleteRequest,
    KnowledgeBaseDocument,
    KnowledgeBaseIndexRequest,
    KnowledgeBaseTargetRequest,
)
from gen_ai_orchestrator.routers.responses.knowledge_base_responses import (
    KnowledgeBaseStoredRow,
)
from gen_ai_orchestrator.services.knowledge_base import knowledge_base_service as kb
from gen_ai_orchestrator.services.langchain.factories.vector_stores.pgvector_factory import (
    PGVectorFactory,
)
from gen_ai_orchestrator.services.langchain.rag_chain_builder import (
    get_chunk_identifier,
    get_web_source_url,
)
from gen_ai_orchestrator.services.langchain.rag_response_builder import (
    get_source_content,
)
from langchain_core.embeddings import DeterministicFakeEmbedding


def request(**updates):
    values = dict(
        index_name='ns_test_bot_kb_session_one',
        index_name_prefix='ns_test_bot_kb_session_',
        index_session_id='one',
        em_setting={
            'provider': 'OpenAI',
            'api_key': {'type': 'Raw', 'secret': 'test'},
            'model': 'test',
        },
        entries=[
            dict(
                entry_id='entry',
                title='Titre',
                search_hints=['synonyme'],
                content='Contenu',
            )
        ],
    )
    values.update(updates)
    return KnowledgeBaseIndexRequest.model_validate(values)


def test_document_compatible_with_runtime_and_nullable_source():
    doc = kb.build_document(request().entries[0], 'one')
    assert doc.page_content == 'Titre\n\n```markdown\nsynonyme\n\nContenu\n```'
    assert doc.metadata['source'] is None
    assert get_chunk_identifier(doc) == 'entry:1/1'
    assert get_web_source_url(doc) is None
    assert get_source_content(doc) == '```markdown\nsynonyme\n\nContenu\n```'
    assert doc.metadata['kb_content_hash'] == kb.content_hash(doc.page_content, None)


def test_row_ids_are_stable_and_isolated_between_sessions_and_bots():
    assert kb.row_id('one', 'entry') == kb.row_id('one', 'entry')
    assert (
        len({kb.row_id(index, 'entry') for index in ['one', 'two', 'another-bot']}) == 3
    )


def test_url_changes_projection_hash():
    assert kb.content_hash('text', None) != kb.content_hash(
        'text', 'https://example.org'
    )


def test_index_outside_bot_is_rejected_before_factory_creation():
    with patch.object(kb, 'get_vector_store_factory') as factory:
        with pytest.raises(HTTPException) as error:
            kb.factory_for(
                KnowledgeBaseTargetRequest(
                    index_name='other', index_name_prefix='ns_test_'
                )
            )
        assert error.value.status_code == 403
        factory.assert_not_called()


@pytest.mark.asyncio
async def test_partial_failure_has_per_entry_ack_and_does_not_leak_secret():
    store = SimpleNamespace(
        aadd_documents=AsyncMock(side_effect=[RuntimeError('secret-password'), ['ok']])
    )
    factory = SimpleNamespace(get_vector_store=lambda: store)
    entries = [dict(entry_id=e, title='Title', content='Body') for e in ['bad', 'good']]
    with (
        patch.object(kb, 'factory_for', return_value=factory),
        patch.object(kb, 'get_em_factory'),
    ):
        response = await kb.index_entries(request(entries=entries))
    assert response.results[0].error == 'knowledge-base.job.index_failed'
    assert response.results[1].count == 1
    assert 'secret-password' not in response.model_dump_json()


@pytest.mark.asyncio
async def test_delete_is_collection_scoped_and_cannot_remove_documentary_rows():
    factory = MagicMock(spec=PGVectorFactory)
    store = SimpleNamespace(adelete=AsyncMock())
    factory.get_vector_store.return_value = store
    owned = [
        KnowledgeBaseStoredRow(
            row_id='owned', entry_id='entry', content_hash='hash', title='Title'
        )
    ]
    deletion = KnowledgeBaseDeleteRequest(
        index_name='ns_test_one',
        index_name_prefix='ns_test_',
        entries=[
            {'entry_id': 'entry', 'row_ids': ['owned', 'documentary', 'another-entry']}
        ],
    )
    with (
        patch.object(kb, 'factory_for', return_value=factory),
        patch.object(kb, 'stored_rows', AsyncMock(return_value=owned)),
    ):
        result = await kb.delete_entries(deletion)
    store.adelete.assert_awaited_once_with(['owned'], collection_only=True)
    assert result.results[0].count == 1


@pytest.mark.asyncio
async def test_delete_missing_entry_is_idempotent():
    factory = MagicMock(spec=PGVectorFactory)
    with (
        patch.object(kb, 'factory_for', return_value=factory),
        patch.object(kb, 'stored_rows', AsyncMock(return_value=[])),
    ):
        response = await kb.delete_entries(
            KnowledgeBaseDeleteRequest(
                index_name='ns_test_one',
                index_name_prefix='ns_test_',
                entries=[{'entry_id': 'absent'}],
            )
        )
    assert response.results[0].count == 0
    assert response.results[0].error is None
    factory.get_vector_store.assert_not_called()


@pytest.mark.asyncio
@pytest.mark.skipif(
    not os.environ.get('KB_TEST_PG_PORT'),
    reason='Set KB_TEST_PG_PORT to an isolated pgvector test database',
)
async def test_pgvector_real_upsert_session_isolation_deletion_and_external_drift():
    setting = dict(
        provider='PGVector',
        host='127.0.0.1',
        port=int(os.environ['KB_TEST_PG_PORT']),
        username='postgres',
        password={'type': 'Raw', 'secret': 'kb-test-password'},
        database='kb_test',
    )
    embedding = DeterministicFakeEmbedding(size=8)
    em_factory = SimpleNamespace(get_embedding_model=lambda: embedding)
    first = request(vector_store_setting=setting)
    second = request(
        vector_store_setting=setting,
        index_name='ns_test_bot_kb_session_two',
        index_session_id='two',
    )
    with patch.object(kb, 'get_em_factory', return_value=em_factory):
        await kb.index_entries(first)
        await kb.index_entries(second)
        changed = first.model_copy(
            update={
                'entries': [
                    KnowledgeBaseDocument(
                        entry_id='entry',
                        title='Titre',
                        content='Nouvelle version',
                        source_url='https://example.org',
                    )
                ]
            }
        )
        await kb.index_entries(changed)
        await kb.index_entries(changed)
    rows_one = (await kb.inspect_rows(first)).rows
    rows_two = (await kb.inspect_rows(second)).rows
    assert len(rows_one) == len(rows_two) == 1
    assert rows_one[0].row_id != rows_two[0].row_id
    assert rows_one[0].content_hash != rows_two[0].content_hash
    # Drift must be computed from the real content, not the metadata's cached hash.
    from sqlalchemy import text

    factory = kb.factory_for(first)
    async with factory.pool.async_engine.begin() as connection:
        await connection.execute(
            text(
                "UPDATE langchain_pg_embedding SET document='external change' WHERE id=:id"
            ),
            {'id': rows_one[0].row_id},
        )
    assert (await kb.inspect_rows(first)).rows[0].content_hash != rows_one[
        0
    ].content_hash
    response = await kb.delete_entries(
        KnowledgeBaseDeleteRequest(
            vector_store_setting=first.vector_store_setting,
            index_name=first.index_name,
            index_name_prefix=first.index_name_prefix,
            entries=[{'entry_id': 'entry'}],
        )
    )
    assert response.results[0].count == 1
    assert (await kb.inspect_rows(first)).rows == []
    assert len((await kb.inspect_rows(second)).rows) == 1
    # Keep the test repeatable without affecting any non-test collection.
    for r in (first, second):
        await kb.factory_for(r).get_vector_store().adelete_collection()


@pytest.mark.asyncio
@pytest.mark.skipif(
    not os.environ.get('KB_TEST_OS_PORT'),
    reason='Set KB_TEST_OS_PORT to an isolated OpenSearch instance',
)
async def test_opensearch_real_creation_upsert_inspection_pins_and_owned_deletion():
    from uuid import uuid4

    from gen_ai_orchestrator.routers.requests.vector_store_inspection_requests import (
        VectorStoreInspectionSearchRequest,
    )
    from gen_ai_orchestrator.services.vector_store_inspection import (
        opensearch_inspection_service as inspection,
    )
    from langchain_community.vectorstores.opensearch_vector_search import (
        OpenSearchVectorSearch,
    )
    from langchain_core.documents import Document

    prefix = 'ns-kb-test-' + uuid4().hex + '-session-'
    name = prefix + 'one'
    embedding = DeterministicFakeEmbedding(size=8)
    store = OpenSearchVectorSearch(
        opensearch_url='http://127.0.0.1:' + os.environ['KB_TEST_OS_PORT'],
        index_name=name,
        embedding_function=embedding,
        use_ssl=False,
        verify_certs=False,
    )
    factory = SimpleNamespace(index_name=name, get_vector_store=lambda: store)
    req = request(index_name=name, index_name_prefix=prefix)
    try:
        with (
            patch.object(kb, 'factory_for', return_value=factory),
            patch.object(
                kb,
                'get_em_factory',
                return_value=SimpleNamespace(get_embedding_model=lambda: embedding),
            ),
        ):
            first = await kb.index_entries(req)
            assert first.results[0].error is None
            second = await kb.index_entries(req)
            assert first.results[0].row_ids == second.results[0].row_ids
            assert len((await kb.inspect_rows(req)).rows) == 1
            documentary = Document(
                page_content='Unrelated external documentary content',
                metadata={
                    'id': 'external',
                    'chunk': '1/1',
                    'title': 'Document',
                    'source': 'https://example.org',
                },
            )
            await store.aadd_documents([documentary], ids=['documentary-row'])
            listed = await inspection.indexes(factory, prefix)
            assert listed.indexes[0].chunk_count == 2
            assert listed.indexes[0].document_count == 2
            # Exact same embedded text: this document is first; the KB pin is fetched separately.
            search = VectorStoreInspectionSearchRequest.model_validate(
                dict(
                    index_name=name,
                    index_name_prefix=prefix,
                    embedding_question_em_setting=req.em_setting,
                    query=documentary.page_content,
                    search_type='SIMILARITY_SEARCH',
                    fetch_k=1,
                    k=1,
                    compression_stage='after_cut',
                    pinned_chunk_ids=['entry:1/1'],
                )
            )
            result = await inspection.search(search, factory)
            assert result.results[0].document_id == 'external'
            assert (
                next(
                    hit for hit in result.results if hit.document_id == 'entry'
                ).outcome.value
                == 'not_retrieved'
            )
            deleted = await kb.delete_entries(
                KnowledgeBaseDeleteRequest(
                    index_name=name,
                    index_name_prefix=prefix,
                    entries=[
                        {
                            'entry_id': 'entry',
                            'row_ids': [first.results[0].row_ids[0], 'documentary-row'],
                        }
                    ],
                )
            )
            assert deleted.results[0].count == 1
            assert (await kb.inspect_rows(req)).rows == []
            assert store.client.get(index=name, id='documentary-row')['found']
    finally:
        if store.client.indices.exists(index=name):
            store.client.indices.delete(index=name)


@pytest.mark.asyncio
async def test_opensearch_pinned_chunk_outside_results_has_no_rank_and_is_not_retrieved():
    from gen_ai_orchestrator.routers.requests.vector_store_inspection_requests import (
        VectorStoreInspectionSearchRequest,
    )
    from gen_ai_orchestrator.services.vector_store_inspection import (
        opensearch_inspection_service as inspection,
    )
    from langchain_core.documents import Document

    document = Document(
        page_content='Document\n\nBody',
        metadata={
            'id': 'external',
            'chunk': '1/1',
            'title': 'Document',
            'source': None,
        },
    )
    store = SimpleNamespace(
        asimilarity_search_with_score=AsyncMock(return_value=[(document, 0.9)]),
        client=MagicMock(),
    )
    store.client.search.return_value = {
        'hits': {
            'hits': [
                {
                    '_source': {
                        'text': 'Titre\n\nContenu',
                        'metadata': {
                            'id': 'entry',
                            'chunk': '1/1',
                            'title': 'Titre',
                            'source': None,
                            'source_type': 'internal_kb',
                            'kb_entry_id': 'entry',
                        },
                    }
                }
            ]
        }
    }
    req = VectorStoreInspectionSearchRequest.model_validate(
        dict(
            index_name='index',
            index_name_prefix='index',
            embedding_question_em_setting=request().em_setting,
            query='real question',
            search_type='SIMILARITY_SEARCH',
            fetch_k=1,
            k=1,
            compression_stage='after_cut',
            pinned_chunk_ids=['entry:1/1'],
        )
    )
    response = await inspection.search(
        req, SimpleNamespace(get_vector_store=lambda: store)
    )
    assert response.results[0].outcome.value == 'kept'
    pinned = response.results[1]
    assert pinned.pinned and pinned.document_id == 'entry'
    assert pinned.outcome.value == 'not_retrieved'
    assert pinned.ranks.vector is None


@pytest.mark.asyncio
async def test_opensearch_delete_uses_refreshing_sync_helper_and_preserves_other_sources():
    store = SimpleNamespace(delete=MagicMock(return_value=True))
    factory = SimpleNamespace(get_vector_store=lambda: store)
    owned = [
        KnowledgeBaseStoredRow(
            row_id='owned', entry_id='entry', content_hash='hash', title='Title'
        )
    ]
    deletion = KnowledgeBaseDeleteRequest(
        index_name='index',
        index_name_prefix='index',
        entries=[{'entry_id': 'entry', 'row_ids': ['owned', 'documentary']}],
    )
    with (
        patch.object(kb, 'factory_for', return_value=factory),
        patch.object(kb, 'stored_rows', AsyncMock(return_value=owned)),
    ):
        response = await kb.delete_entries(deletion)
    store.delete.assert_called_once_with(['owned'])
    assert response.results[0].count == 1

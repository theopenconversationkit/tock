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
"""Read-only PGVector exploration and retrieval diagnostics."""

import asyncio
import copy
import time
from dataclasses import dataclass, field
from datetime import datetime
from typing import Any, Optional

from fastapi import HTTPException
from langchain_community.embeddings import FakeEmbeddings
from langchain_core.callbacks import BaseCallbackHandler
from langchain_core.documents import Document
from langchain_core.messages import AIMessage, HumanMessage
from langchain_core.prompt_values import ChatPromptValue
from langchain_core.runnables.config import RunnableConfig
from sqlalchemy import TextClause, bindparam, text

from gen_ai_orchestrator.configurations.environment.settings import (
    application_settings,
)
from gen_ai_orchestrator.models.rag.rag_models import ChatMessageType
from gen_ai_orchestrator.models.vector_stores.vector_store_provider import (
    VectorStoreProvider,
)
from gen_ai_orchestrator.models.vector_stores.vector_store_search_type import (
    DocumentSearchType,
)
from gen_ai_orchestrator.routers.requests.vector_store_inspection_requests import (
    AnomalyCode,
    CompressionStage,
    PinnedRankStrategy,
    VectorStoreInspectionCapabilitiesRequest,
    VectorStoreInspectionCondenseRequest,
    VectorStoreInspectionDocumentsRequest,
    VectorStoreInspectionIndexesRequest,
    VectorStoreInspectionSearchRequest,
)
from gen_ai_orchestrator.routers.responses.vector_store_inspection_responses import (
    ChannelRanks,
    ChannelScores,
    ChunkOutcome,
    CondenseResponse,
    FunnelStage,
    FunnelStageStatus,
    IndexAnomaly,
    IndexListResponse,
    IndexStats,
    InspectedChunk,
    InspectedDocument,
    SearchFunnel,
    SearchResponse,
    SearchResultChunk,
    VectorStoreCapabilitiesResponse,
    VectorStoreIndexDescription,
    VectorStoreInspectionDocumentsResponse,
)
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    get_compressor_factory,
    get_em_factory,
    get_llm_factory,
    get_vector_store_factory,
)
from gen_ai_orchestrator.services.langchain.factories.vector_stores.pgvector_factory import (
    PGVectorFactory,
)
from gen_ai_orchestrator.services.langchain.rag_chain_builder import (
    build_question_condensation_chain,
    calculate_rrf_scores,
    get_chunk_identifier,
    get_document_key,
)
from gen_ai_orchestrator.services.langchain.rag_response_builder import (
    get_source_content,
)

RRF_K = 60
NEAR_EMPTY_LENGTH = 50

CONTENT_SQL = """
CASE
    WHEN COALESCE(e.cmetadata->>'title', '') <> ''
     AND e.document LIKE (e.cmetadata->>'title') || E'\\n\\n%'
    THEN substring(e.document FROM char_length(e.cmetadata->>'title') + 3)
    ELSE COALESCE(e.document, '')
END
"""


@dataclass
class Candidate:
    document: Document
    ranks: dict[str, Optional[int]] = field(default_factory=dict)
    scores: dict[str, Optional[float]] = field(default_factory=dict)

    @property
    def chunk_id(self) -> str:
        return get_chunk_identifier(self.document)


class PromptCaptureHandler(BaseCallbackHandler):
    """Capture the complete prompt value used by the condensation chain."""

    def __init__(self):
        self.prompt = ''

    def on_chain_end(self, outputs: Any, **kwargs: Any) -> None:
        if isinstance(outputs, ChatPromptValue):
            self.prompt = '\n\n'.join(
                f"{message.type}: {message.content}" for message in outputs.messages
            )


def _assert_allowed_index(index_name: str, index_name_prefix: str) -> None:
    if not index_name.startswith(index_name_prefix):
        raise HTTPException(status_code=403, detail='Index is outside the current bot')


def _normalise_datetime(value: Optional[str]) -> str:
    if not value:
        return ''
    try:
        return datetime.strptime(value, '%Y-%m-%d_%Hh%Mm%S').isoformat()
    except ValueError:
        return value


def _source(metadata: dict[str, Any]) -> Optional[str]:
    value = metadata.get('source') or metadata.get('reference')
    return str(value) if value else None


def _document_id(document: Document) -> str:
    return str(document.metadata.get('id') or '')


def _pg_factory(setting, index_name: str, embedding_function) -> PGVectorFactory:
    factory = get_vector_store_factory(
        setting=setting,
        index_name=index_name,
        embedding_function=embedding_function,
    )
    if not isinstance(factory, PGVectorFactory):
        raise HTTPException(
            status_code=501,
            detail='Vector store inspection is currently available for PGVector only',
        )
    return factory


async def _fetch_all(engine, statement: TextClause, params: dict) -> list:
    async with engine.connect() as connection:
        result = await connection.execute(statement, params)
        return list(result.mappings().all())


async def get_capabilities(
    request: VectorStoreInspectionCapabilitiesRequest,
) -> VectorStoreCapabilitiesResponse:
    provider = (
        request.vector_store_setting.provider
        if request.vector_store_setting
        else application_settings.vector_store_provider
    )
    if provider == VectorStoreProvider.PGVECTOR:
        return VectorStoreCapabilitiesResponse(
            provider=VectorStoreProvider.PGVECTOR,
            search_types=list(DocumentSearchType),
            supports_scores=True,
            supports_index_listing=True,
            supports_metadata_filter=True,
        )
    if provider == VectorStoreProvider.OPEN_SEARCH:
        return VectorStoreCapabilitiesResponse(
            provider=VectorStoreProvider.OPEN_SEARCH,
            search_types=[DocumentSearchType.SIMILARITY_SEARCH],
            supports_scores=True,
            supports_index_listing=False,
            supports_metadata_filter=True,
            notes=['hybrid_and_fts_not_implemented', 'inspection_not_implemented'],
        )
    raise HTTPException(status_code=501, detail='Unsupported vector store provider')


async def get_indexes(
    request: VectorStoreInspectionIndexesRequest,
) -> IndexListResponse:
    factory = _pg_factory(
        request.vector_store_setting,
        request.index_name_prefix,
        FakeEmbeddings(size=1),
    )
    rows = await _fetch_all(
        factory.pool.async_engine,
        text("""
            SELECT
                c.name AS index_name,
                COALESCE(MAX(e.cmetadata->>'index_session_id'), '') AS index_session_id,
                COALESCE(MAX(e.cmetadata->>'index_datetime'), '') AS index_datetime,
                COUNT(DISTINCT e.cmetadata->>'id')
                    FILTER (WHERE e.cmetadata->>'id' IS NOT NULL) AS document_count,
                COUNT(e.id) AS chunk_count
            FROM langchain_pg_collection c
            LEFT JOIN langchain_pg_embedding e ON e.collection_id = c.uuid
            WHERE left(c.name, char_length(:prefix)) = :prefix
            GROUP BY c.name
            ORDER BY MAX(e.cmetadata->>'index_datetime') DESC NULLS LAST, c.name
        """),
        {'prefix': request.index_name_prefix},
    )
    return IndexListResponse(
        indexes=[
            VectorStoreIndexDescription(
                index_name=row['index_name'],
                index_session_id=row['index_session_id'],
                index_datetime=_normalise_datetime(row['index_datetime']),
                document_count=int(row['document_count']),
                chunk_count=int(row['chunk_count']),
            )
            for row in rows
        ]
    )


def _documents_page_statement() -> TextClause:
    return text(f"""
        WITH chunks AS (
            SELECT
                e.cmetadata->>'id' AS document_id,
                COALESCE(e.cmetadata->>'title', '') AS title,
                COALESCE(
                    NULLIF(e.cmetadata->>'source', ''),
                    NULLIF(e.cmetadata->>'reference', '')
                ) AS source,
                COALESCE(e.cmetadata->>'index_session_id', '') AS index_session_id,
                e.document,
                char_length({CONTENT_SQL}) AS content_length
            FROM langchain_pg_embedding e
            JOIN langchain_pg_collection c ON c.uuid = e.collection_id
            WHERE c.name = :index_name
              AND e.cmetadata->>'id' IS NOT NULL
        ), documents AS (
            SELECT
                document_id,
                MAX(title) AS title,
                MAX(source) AS source,
                MAX(index_session_id) AS index_session_id,
                COUNT(*) AS chunk_count,
                BOOL_OR(content_length < :near_empty_length) AS near_empty,
                BOOL_OR(
                    source IS NOT NULL AND source !~* '^https?://'
                ) AS non_url_source,
                BOOL_OR(
                    title ILIKE :text_pattern OR document ILIKE :text_pattern
                ) AS text_match
            FROM chunks
            GROUP BY document_id
        ), duplicate_titles AS (
            SELECT title
            FROM documents
            WHERE title <> ''
            GROUP BY title
            HAVING COUNT(*) > 1
        ), filtered AS (
            SELECT d.*, duplicate_titles.title IS NOT NULL AS duplicate_title
            FROM documents d
            LEFT JOIN duplicate_titles ON duplicate_titles.title = d.title
            WHERE (
                CAST(:document_id AS text) IS NULL
                OR d.document_id = :document_id
              )
              AND (CAST(:text_filter AS text) IS NULL OR d.text_match)
              AND (
                CAST(:anomaly AS text) IS NULL
                OR (:anomaly = 'near_empty_chunk' AND d.near_empty)
                OR (:anomaly = 'non_url_source' AND d.non_url_source)
                OR (:anomaly = 'duplicate_title' AND duplicate_titles.title IS NOT NULL)
              )
        )
        SELECT *, COUNT(*) OVER () AS total
        FROM filtered
        ORDER BY title, document_id
        OFFSET :start LIMIT :size
    """)


def _stats_statement() -> TextClause:
    return text(f"""
        WITH chunks AS (
            SELECT
                e.cmetadata->>'id' AS document_id,
                COALESCE(e.cmetadata->>'title', '') AS title,
                COALESCE(
                    NULLIF(e.cmetadata->>'source', ''),
                    NULLIF(e.cmetadata->>'reference', '')
                ) AS source,
                e.cmetadata->>'index_datetime' AS index_datetime,
                char_length({CONTENT_SQL}) AS content_length
            FROM langchain_pg_embedding e
            JOIN langchain_pg_collection c ON c.uuid = e.collection_id
            WHERE c.name = :index_name
              AND e.cmetadata->>'id' IS NOT NULL
        ), documents AS (
            SELECT
                document_id,
                MAX(title) AS title,
                BOOL_OR(content_length < :near_empty_length) AS near_empty,
                BOOL_OR(source IS NOT NULL AND source !~* '^https?://')
                    AS non_url_source
            FROM chunks
            GROUP BY document_id
        ), duplicate_titles AS (
            SELECT title
            FROM documents
            WHERE title <> ''
            GROUP BY title
            HAVING COUNT(*) > 1
        )
        SELECT
            (SELECT COUNT(*) FROM documents) AS document_count,
            (SELECT COUNT(*) FROM chunks) AS chunk_count,
            COALESCE(
                (SELECT COUNT(*)::float FROM chunks)
                    / NULLIF((SELECT COUNT(*) FROM documents), 0),
                0
            ) AS chunks_per_document_avg,
            COALESCE(
                (SELECT percentile_cont(0.5) WITHIN GROUP (ORDER BY content_length)
                 FROM chunks),
                0
            ) AS chunk_length_median,
            COALESCE((SELECT MAX(index_datetime) FROM chunks), '') AS index_datetime,
            (SELECT COUNT(*) FROM documents WHERE near_empty) AS near_empty_count,
            (SELECT COUNT(*) FROM documents WHERE non_url_source) AS non_url_count,
            (SELECT COUNT(*) FROM documents d
             JOIN duplicate_titles t ON t.title = d.title) AS duplicate_title_count
    """)


async def get_documents(
    request: VectorStoreInspectionDocumentsRequest,
) -> VectorStoreInspectionDocumentsResponse:
    _assert_allowed_index(request.index_name, request.index_name_prefix)
    factory = _pg_factory(
        request.vector_store_setting,
        request.index_name,
        FakeEmbeddings(size=1),
    )
    engine = factory.pool.async_engine
    filters = request.filter
    text_filter = filters.text.strip() if filters and filters.text else None
    params = {
        'index_name': request.index_name,
        'near_empty_length': NEAR_EMPTY_LENGTH,
        'text_filter': text_filter,
        'text_pattern': f"%{text_filter}%" if text_filter else '%',
        'document_id': filters.document_id if filters else None,
        'anomaly': filters.anomaly.value if filters and filters.anomaly else None,
        'start': request.start,
        'size': request.size,
    }
    page_task = _fetch_all(engine, _documents_page_statement(), params)
    stats_task = (
        _fetch_all(engine, _stats_statement(), params)
        if request.include_stats
        else asyncio.sleep(0, result=[])
    )
    page_rows, stats_rows = await asyncio.gather(page_task, stats_task)

    document_ids = [row['document_id'] for row in page_rows]
    chunks_by_document: dict[str, list[InspectedChunk]] = {}
    if request.include_chunks and document_ids:
        chunks_statement = text("""
            SELECT e.document, e.cmetadata
            FROM langchain_pg_embedding e
            JOIN langchain_pg_collection c ON c.uuid = e.collection_id
            WHERE c.name = :index_name
              AND e.cmetadata->>'id' IN :document_ids
            ORDER BY
                e.cmetadata->>'id',
                CASE
                    WHEN e.cmetadata->>'chunk' ~ '^[0-9]+/'
                    THEN split_part(e.cmetadata->>'chunk', '/', 1)::int
                    ELSE 2147483647
                END,
                e.cmetadata->>'chunk'
        """).bindparams(bindparam('document_ids', expanding=True))
        chunk_rows = await _fetch_all(
            engine,
            chunks_statement,
            {'index_name': request.index_name, 'document_ids': document_ids},
        )
        for row in chunk_rows:
            metadata = dict(row['cmetadata'] or {})
            document = Document(page_content=row['document'] or '', metadata=metadata)
            content = get_source_content(document)
            chunks_by_document.setdefault(_document_id(document), []).append(
                InspectedChunk(
                    chunk_id=get_chunk_identifier(document),
                    chunk=str(metadata.get('chunk') or ''),
                    content=content,
                    content_length=len(content),
                    metadata=metadata,
                )
            )

    rows = [
        InspectedDocument(
            document_id=row['document_id'],
            title=row['title'],
            source=row['source'],
            chunk_count=int(row['chunk_count']),
            index_session_id=row['index_session_id'],
            chunks=(
                chunks_by_document.get(row['document_id'], [])
                if request.include_chunks
                else None
            ),
        )
        for row in page_rows
    ]
    total = int(page_rows[0]['total']) if page_rows else 0
    stats = None
    anomalies: list[IndexAnomaly] = []
    if stats_rows:
        stats_row = stats_rows[0]
        stats = IndexStats(
            document_count=int(stats_row['document_count']),
            chunk_count=int(stats_row['chunk_count']),
            chunks_per_document_avg=float(stats_row['chunks_per_document_avg']),
            chunk_length_median=float(stats_row['chunk_length_median']),
            index_datetime=_normalise_datetime(stats_row['index_datetime']),
        )
        anomalies = [
            IndexAnomaly(
                code=AnomalyCode.NEAR_EMPTY_CHUNK,
                count=int(stats_row['near_empty_count']),
                severity='warning',
            ),
            IndexAnomaly(
                code=AnomalyCode.NON_URL_SOURCE,
                count=int(stats_row['non_url_count']),
                severity='info',
            ),
            IndexAnomaly(
                code=AnomalyCode.DUPLICATE_TITLE,
                count=int(stats_row['duplicate_title_count']),
                severity='info',
            ),
        ]
    return VectorStoreInspectionDocumentsResponse(
        stats=stats,
        anomalies=anomalies,
        rows=rows,
        total=total,
        start=request.start,
        end=request.start + len(rows),
    )


async def condense(
    request: VectorStoreInspectionCondenseRequest,
) -> CondenseResponse:
    started = time.perf_counter()
    llm = get_llm_factory(request.question_condensing_llm_setting).get_language_model()
    chain = build_question_condensation_chain(llm, request.question_condensing_prompt)
    history = [
        HumanMessage(message.text)
        if message.type == ChatMessageType.HUMAN
        else AIMessage(message.text)
        for message in request.chat_history
    ]
    capture = PromptCaptureHandler()
    result = await chain.ainvoke(
        {'question': request.question, 'chat_history': history},
        config=RunnableConfig(callbacks=[capture]),
    )
    return CondenseResponse(
        condensed_question=result['condensed_question'],
        key_words=result.get('key_words') or [],
        effective_prompt=capture.prompt,
        duration=time.perf_counter() - started,
    )


async def _fts_documents(factory: PGVectorFactory, query: str, k: int):
    rows = await _fetch_all(
        factory.pool.async_engine,
        text("""
            WITH q AS (
                SELECT websearch_to_tsquery('french', unaccent(:query)) AS ts_query
            )
            SELECT e.document, e.cmetadata, ts_rank(e.fts_vector, q.ts_query) AS score
            FROM langchain_pg_embedding e
            JOIN langchain_pg_collection c ON c.uuid = e.collection_id
            CROSS JOIN q
            WHERE c.name = :index_name AND e.fts_vector @@ q.ts_query
            ORDER BY score DESC, e.id
            LIMIT :k
        """),
        {'query': query, 'index_name': factory.index_name, 'k': k},
    )
    return [
        (
            Document(
                page_content=row['document'] or '',
                metadata=dict(row['cmetadata'] or {}),
            ),
            float(row['score']),
        )
        for row in rows
    ]


def _keywords_query(keywords: list[str]) -> str:
    return ' OR '.join(keyword.strip() for keyword in keywords if keyword.strip())


async def _pinned_documents(
    factory: PGVectorFactory, pinned_ids: list[str]
) -> dict[str, Document]:
    if not pinned_ids:
        return {}
    statement = text("""
        SELECT e.document, e.cmetadata
        FROM langchain_pg_embedding e
        JOIN langchain_pg_collection c ON c.uuid = e.collection_id
        WHERE c.name = :index_name
          AND concat(e.cmetadata->>'id', ':', e.cmetadata->>'chunk') IN :chunk_ids
    """).bindparams(bindparam('chunk_ids', expanding=True))
    rows = await _fetch_all(
        factory.pool.async_engine,
        statement,
        {'index_name': factory.index_name, 'chunk_ids': pinned_ids},
    )
    documents = [
        Document(
            page_content=row['document'] or '',
            metadata=dict(row['cmetadata'] or {}),
        )
        for row in rows
    ]
    return {get_chunk_identifier(document): document for document in documents}


async def _pinned_vector_metrics(
    factory: PGVectorFactory,
    pinned_ids: list[str],
    embedding: list[float],
    exact_rank: bool,
) -> dict[str, tuple[Optional[int], float]]:
    if not pinned_ids:
        return {}
    rank_sql = (
        """
        1 + (
            SELECT COUNT(*)
            FROM langchain_pg_embedding ranked
            WHERE ranked.collection_id = target.collection_id
              AND (ranked.embedding <=> CAST(:embedding AS vector)) < target.distance
        )
    """
        if exact_rank
        else 'NULL'
    )
    statement = text(f"""
        WITH targets AS (
            SELECT
                e.collection_id,
                concat(e.cmetadata->>'id', ':', e.cmetadata->>'chunk') AS chunk_id,
                e.embedding <=> CAST(:embedding AS vector) AS distance
            FROM langchain_pg_embedding e
            JOIN langchain_pg_collection c ON c.uuid = e.collection_id
            WHERE c.name = :index_name
              AND concat(e.cmetadata->>'id', ':', e.cmetadata->>'chunk') IN :chunk_ids
        )
        SELECT target.chunk_id, target.distance, {rank_sql} AS rank
        FROM targets target
    """).bindparams(bindparam('chunk_ids', expanding=True))
    rows = await _fetch_all(
        factory.pool.async_engine,
        statement,
        {
            'embedding': '[' + ','.join(map(str, embedding)) + ']',
            'index_name': factory.index_name,
            'chunk_ids': pinned_ids,
        },
    )
    return {
        row['chunk_id']: (
            int(row['rank']) if row['rank'] is not None else None,
            1.0 - float(row['distance']),
        )
        for row in rows
    }


async def _pinned_fts_metrics(
    factory: PGVectorFactory,
    pinned_ids: list[str],
    query: str,
    exact_rank: bool,
) -> dict[str, tuple[Optional[int], float]]:
    if not pinned_ids:
        return {}
    rank_sql = (
        """
        1 + (
            SELECT COUNT(*)
            FROM langchain_pg_embedding ranked
            CROSS JOIN q
            WHERE ranked.collection_id = target.collection_id
              AND ranked.fts_vector @@ q.ts_query
              AND ts_rank(ranked.fts_vector, q.ts_query) > target.score
        )
    """
        if exact_rank
        else 'NULL'
    )
    statement = text(f"""
        WITH q AS (
            SELECT websearch_to_tsquery('french', unaccent(:query)) AS ts_query
        ), targets AS (
            SELECT
                e.collection_id,
                concat(e.cmetadata->>'id', ':', e.cmetadata->>'chunk') AS chunk_id,
                ts_rank(e.fts_vector, q.ts_query) AS score
            FROM langchain_pg_embedding e
            JOIN langchain_pg_collection c ON c.uuid = e.collection_id
            CROSS JOIN q
            WHERE c.name = :index_name
              AND e.fts_vector @@ q.ts_query
              AND concat(e.cmetadata->>'id', ':', e.cmetadata->>'chunk') IN :chunk_ids
        )
        SELECT target.chunk_id, target.score, {rank_sql} AS rank
        FROM targets target
    """).bindparams(bindparam('chunk_ids', expanding=True))
    rows = await _fetch_all(
        factory.pool.async_engine,
        statement,
        {'query': query, 'index_name': factory.index_name, 'chunk_ids': pinned_ids},
    )
    return {
        row['chunk_id']: (
            int(row['rank']) if row['rank'] is not None else None,
            float(row['score']),
        )
        for row in rows
    }


def _result(
    candidate: Candidate,
    outcome: ChunkOutcome,
    pinned: bool,
) -> SearchResultChunk:
    document = candidate.document
    return SearchResultChunk(
        chunk_id=candidate.chunk_id,
        document_id=_document_id(document),
        title=str(document.metadata.get('title') or ''),
        chunk=str(document.metadata.get('chunk') or ''),
        content=get_source_content(document),
        ranks=ChannelRanks(**candidate.ranks),
        scores=ChannelScores(**candidate.scores),
        outcome=outcome,
        pinned=pinned,
        metadata=document.metadata,
    )


async def _compress(
    request: VectorStoreInspectionSearchRequest,
    candidates: list[Candidate],
) -> tuple[list[str], dict[str, ChunkOutcome], dict[str, float], FunnelStage]:
    override = request.compression_override
    updates = {}
    if override:
        updates = {
            'min_score': override.min_score,
            'max_documents': override.max_documents,
            'fill_to_max_documents': override.fill_to_max_documents,
        }
    setting = request.compressor_setting.model_copy(update=updates)
    compressor = get_compressor_factory(
        setting=setting, is_fault_tolerant=False
    ).get_compressor()
    documents = [copy.deepcopy(candidate.document) for candidate in candidates]
    try:
        compressed = await asyncio.to_thread(
            compressor.compress_documents, documents, request.query
        )
    except Exception as exception:
        return (
            [candidate.chunk_id for candidate in candidates],
            {candidate.chunk_id: ChunkOutcome.KEPT for candidate in candidates},
            {},
            FunnelStage(
                status=FunnelStageStatus.FAILED_FALLBACK,
                count=len(candidates),
                reason=str(exception),
            ),
        )

    documents_by_id = {get_chunk_identifier(doc): doc for doc in documents}
    compressed_ids = [get_chunk_identifier(doc) for doc in compressed]
    compressed_set = set(compressed_ids)
    scores = {
        chunk_id: float(document.metadata['retriever_score'])
        for chunk_id, document in documents_by_id.items()
        if document.metadata.get('retriever_score') is not None
    }
    min_score = float(setting.min_score or 0)
    outcomes = {}
    for candidate in candidates:
        score = scores.get(candidate.chunk_id)
        if candidate.chunk_id in compressed_set:
            outcomes[candidate.chunk_id] = (
                ChunkOutcome.FILLED_BELOW_THRESHOLD
                if score is not None and score < min_score
                else ChunkOutcome.KEPT
            )
        elif score is not None and score < min_score:
            outcomes[candidate.chunk_id] = ChunkOutcome.BELOW_MIN_SCORE
        else:
            outcomes[candidate.chunk_id] = ChunkOutcome.RERANKED_OUT
    return (
        compressed_ids,
        outcomes,
        scores,
        FunnelStage(
            status=FunnelStageStatus.APPLIED,
            count=len(compressed_ids),
        ),
    )


async def search(request: VectorStoreInspectionSearchRequest) -> SearchResponse:
    started = time.perf_counter()
    _assert_allowed_index(request.index_name, request.index_name_prefix)
    embedding_model = get_em_factory(
        request.embedding_question_em_setting
    ).get_embedding_model()
    factory = _pg_factory(
        request.vector_store_setting, request.index_name, embedding_model
    )
    use_vector = request.search_type in {
        DocumentSearchType.SIMILARITY_SEARCH,
        DocumentSearchType.HYBRID_SEARCH,
    }
    use_fts = request.search_type in {
        DocumentSearchType.FULL_TEXT_SEARCH,
        DocumentSearchType.HYBRID_SEARCH,
    }

    embedding = await embedding_model.aembed_query(request.query) if use_vector else []
    vector_task = (
        factory.get_vector_store().asimilarity_search_with_score_by_vector(
            embedding=embedding, k=request.fetch_k
        )
        if use_vector
        else asyncio.sleep(0, result=[])
    )
    fts_query = _keywords_query(request.key_words)
    fts_task = (
        _fts_documents(factory, fts_query, request.fetch_k)
        if use_fts
        else asyncio.sleep(0, result=[])
    )
    vector_rows, fts_rows = await asyncio.gather(vector_task, fts_task)

    vector_docs = [document for document, _ in vector_rows]
    fts_docs = [document for document, _ in fts_rows]
    candidates: dict[str, Candidate] = {}
    for rank, (document, distance) in enumerate(vector_rows, start=1):
        candidate = candidates.setdefault(
            get_chunk_identifier(document), Candidate(document=document)
        )
        candidate.ranks['vector'] = rank
        candidate.scores['vector'] = 1.0 - float(distance)
    for rank, (document, score) in enumerate(fts_rows, start=1):
        candidate = candidates.setdefault(
            get_chunk_identifier(document), Candidate(document=document)
        )
        candidate.ranks['fts'] = rank
        candidate.scores['fts'] = float(score)

    is_hybrid = use_vector and use_fts
    if is_hybrid:
        rrf_by_key = calculate_rrf_scores([vector_docs, fts_docs], RRF_K)
        retrieved = sorted(
            candidates.values(),
            key=lambda candidate: rrf_by_key[get_document_key(candidate.document)],
            reverse=True,
        )
        for rank, candidate in enumerate(retrieved, start=1):
            candidate.ranks['rrf'] = rank
            candidate.scores['rrf'] = rrf_by_key[get_document_key(candidate.document)]
    elif use_vector:
        retrieved = [candidates[get_chunk_identifier(doc)] for doc in vector_docs]
    else:
        retrieved = [candidates[get_chunk_identifier(doc)] for doc in fts_docs]

    outcomes: dict[str, ChunkOutcome] = {}
    compressor_scores: dict[str, float] = {}
    if not request.compression_enabled:
        final = retrieved[: request.k]
        final_ids = {candidate.chunk_id for candidate in final}
        outcomes.update(
            {
                candidate.chunk_id: (
                    ChunkOutcome.KEPT
                    if candidate.chunk_id in final_ids
                    else ChunkOutcome.CUT_BY_TOP_K
                )
                for candidate in retrieved
            }
        )
        compression_stage = FunnelStage(status=FunnelStageStatus.DISABLED, count=None)
        top_k_stage = FunnelStage(
            status=FunnelStageStatus.APPLIED,
            count=len(final),
            discarded=len(retrieved) - len(final),
        )
    elif request.compression_stage == CompressionStage.AFTER_CUT:
        cut = retrieved[: request.k]
        outcomes.update(
            {
                candidate.chunk_id: ChunkOutcome.CUT_BY_TOP_K
                for candidate in retrieved[request.k :]
            }
        )
        (
            compressed_ids,
            compression_outcomes,
            compressor_scores,
            compression_stage,
        ) = await _compress(request, cut)
        outcomes.update(compression_outcomes)
        top_k_stage = FunnelStage(
            status=FunnelStageStatus.APPLIED,
            count=len(cut),
            discarded=len(retrieved) - len(cut),
        )
    else:
        (
            compressed_ids,
            compression_outcomes,
            compressor_scores,
            compression_stage,
        ) = await _compress(request, retrieved)
        outcomes.update(compression_outcomes)
        final_ids = set(compressed_ids[: request.k])
        for chunk_id in compressed_ids[request.k :]:
            outcomes[chunk_id] = ChunkOutcome.CUT_BY_TOP_K
        top_k_stage = FunnelStage(
            status=FunnelStageStatus.APPLIED,
            count=len(final_ids),
            discarded=max(0, len(compressed_ids) - len(final_ids)),
        )

    for candidate in retrieved:
        candidate.scores['compressor'] = compressor_scores.get(candidate.chunk_id)

    pinned_set = set(request.pinned_chunk_ids)
    results = [
        _result(
            candidate,
            outcomes.get(candidate.chunk_id, ChunkOutcome.CUT_BY_TOP_K),
            candidate.chunk_id in pinned_set,
        )
        for candidate in retrieved
    ]

    missing_pins = [pin for pin in request.pinned_chunk_ids if pin not in candidates]
    stored_pins = await _pinned_documents(factory, missing_pins)
    existing_missing_pins = [pin for pin in missing_pins if pin in stored_pins]
    vector_metrics = {}
    fts_metrics = {}
    if request.pinned_rank_strategy != PinnedRankStrategy.TRUNCATED:
        exact = request.pinned_rank_strategy == PinnedRankStrategy.EXACT_RANK
        metric_tasks = []
        if use_vector:
            metric_tasks.append(
                _pinned_vector_metrics(factory, existing_missing_pins, embedding, exact)
            )
        else:
            metric_tasks.append(asyncio.sleep(0, result={}))
        if use_fts:
            metric_tasks.append(
                _pinned_fts_metrics(factory, existing_missing_pins, fts_query, exact)
            )
        else:
            metric_tasks.append(asyncio.sleep(0, result={}))
        vector_metrics, fts_metrics = await asyncio.gather(*metric_tasks)

    for chunk_id in existing_missing_pins:
        document = stored_pins[chunk_id]
        candidate = Candidate(document=document)
        if chunk_id in vector_metrics:
            candidate.ranks['vector'], candidate.scores['vector'] = vector_metrics[
                chunk_id
            ]
        if chunk_id in fts_metrics:
            candidate.ranks['fts'], candidate.scores['fts'] = fts_metrics[chunk_id]
        results.append(_result(candidate, ChunkOutcome.NOT_RETRIEVED, True))

    return SearchResponse(
        funnel=SearchFunnel(
            vector=FunnelStage(
                status=(
                    FunnelStageStatus.APPLIED
                    if use_vector
                    else FunnelStageStatus.SKIPPED
                ),
                count=len(vector_rows) if use_vector else None,
            ),
            fts=FunnelStage(
                status=(
                    FunnelStageStatus.APPLIED if use_fts else FunnelStageStatus.SKIPPED
                ),
                count=len(fts_rows) if use_fts else None,
            ),
            rrf=FunnelStage(
                status=(
                    FunnelStageStatus.APPLIED
                    if is_hybrid
                    else FunnelStageStatus.SKIPPED
                ),
                count=len(retrieved) if is_hybrid else None,
            ),
            top_k_cut=top_k_stage,
            compression=compression_stage,
        ),
        compression_stage=request.compression_stage,
        results=results,
        duration=time.perf_counter() - started,
    )

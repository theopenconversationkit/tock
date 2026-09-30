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
"""OpenSearch index listing and similarity diagnostics, also used by the KB."""

import asyncio
import time

from fastapi import HTTPException
from langchain_core.documents import Document

from gen_ai_orchestrator.routers.requests.vector_store_inspection_requests import (
    CompressionStage,
)
from gen_ai_orchestrator.routers.responses.vector_store_inspection_responses import (
    ChunkOutcome,
    FunnelStage,
    FunnelStageStatus,
    IndexListResponse,
    SearchFunnel,
    SearchResponse,
    VectorStoreIndexDescription,
)


async def indexes(factory, prefix):
    def read():
        from opensearchpy.exceptions import NotFoundError

        client = factory.get_vector_store().client
        try:
            names = client.indices.get(index=prefix + '*')
        except NotFoundError:
            return []
        rows = []
        for name in names:
            if not name.startswith(prefix):
                continue
            id_mapping = (
                names[name]
                .get('mappings', {})
                .get('properties', {})
                .get('metadata', {})
                .get('properties', {})
                .get('id', {})
            )
            id_field = (
                'metadata.id'
                if id_mapping.get('type') == 'keyword'
                else 'metadata.id.keyword'
            )
            response = client.search(
                index=name,
                body={
                    'size': 1,
                    'track_total_hits': True,
                    'aggs': {'documents': {'cardinality': {'field': id_field}}},
                },
            )
            hits = response['hits']
            total = hits['total']
            count = total['value'] if isinstance(total, dict) else total
            metadata = (
                hits['hits'][0]['_source'].get('metadata', {}) if hits['hits'] else {}
            )
            rows.append(
                VectorStoreIndexDescription(
                    index_name=name,
                    index_session_id=metadata.get(
                        'index_session_id', name[len(prefix) :]
                    ),
                    index_datetime=str(metadata.get('index_datetime', '')),
                    document_count=int(response['aggregations']['documents']['value']),
                    chunk_count=int(count),
                )
            )
        return rows

    return IndexListResponse(indexes=await asyncio.to_thread(read))


async def search(request, factory):
    # Local import avoids a cycle and reuses the exact diagnostic response/compression logic.
    from gen_ai_orchestrator.services.vector_store_inspection.vector_store_inspection_service import (
        Candidate,
        _compress,
        _result,
    )

    started = time.perf_counter()
    from gen_ai_orchestrator.models.vector_stores.vector_store_search_type import (
        DocumentSearchType,
    )

    if request.search_type != DocumentSearchType.SIMILARITY_SEARCH:
        raise HTTPException(400, 'OpenSearch supports similarity search only')
    store = factory.get_vector_store()
    rows = await store.asimilarity_search_with_score(request.query, k=request.fetch_k)
    candidates = []
    for rank, (doc, score) in enumerate(rows, start=1):
        candidates.append(
            Candidate(
                document=doc, ranks={'vector': rank}, scores={'vector': float(score)}
            )
        )
    cut = candidates[: request.k]
    outcomes = {c.chunk_id: ChunkOutcome.CUT_BY_TOP_K for c in candidates}
    compression = FunnelStage(status=FunnelStageStatus.DISABLED, count=None)
    cut_count = len(cut)
    discarded = max(0, len(candidates) - cut_count)
    if request.compression_enabled:
        compressed_ids, compressed_outcomes, scores, compression = await _compress(
            request,
            cut
            if request.compression_stage == CompressionStage.AFTER_CUT
            else candidates,
        )
        outcomes.update(compressed_outcomes)
        if request.compression_stage == CompressionStage.BEFORE_CUT:
            cut_count = min(len(compressed_ids), request.k)
            discarded = max(0, len(compressed_ids) - request.k)
            for chunk_id in compressed_ids[request.k :]:
                outcomes[chunk_id] = ChunkOutcome.CUT_BY_TOP_K
        for candidate in candidates:
            candidate.scores['compressor'] = scores.get(candidate.chunk_id)
    else:
        outcomes.update({c.chunk_id: ChunkOutcome.KEPT for c in cut})
    pins = set(request.pinned_chunk_ids)
    results = [_result(c, outcomes[c.chunk_id], c.chunk_id in pins) for c in candidates]
    missing = pins - {c.chunk_id for c in candidates}
    if missing:

        def fetch_pins():
            pairs = [pin.rsplit(':', 1) for pin in missing if ':' in pin]
            should = [
                {
                    'bool': {
                        'must': [
                            {
                                'bool': {
                                    'should': [
                                        {'term': {'metadata.id.keyword': doc}},
                                        {'term': {'metadata.id': doc}},
                                    ],
                                    'minimum_should_match': 1,
                                }
                            },
                            {
                                'bool': {
                                    'should': [
                                        {'term': {'metadata.chunk.keyword': chunk}},
                                        {'term': {'metadata.chunk': chunk}},
                                    ],
                                    'minimum_should_match': 1,
                                }
                            },
                        ]
                    }
                }
                for doc, chunk in pairs
            ]
            if not should:
                return []
            response = store.client.search(
                index=request.index_name,
                body={
                    'size': 50,
                    'query': {'bool': {'should': should, 'minimum_should_match': 1}},
                },
            )
            return [
                Document(
                    page_content=h['_source'].get('text', ''),
                    metadata=h['_source'].get('metadata', {}),
                )
                for h in response['hits']['hits']
            ]

        for doc in await asyncio.to_thread(fetch_pins):
            candidate = Candidate(document=doc)
            if candidate.chunk_id in missing:
                results.append(_result(candidate, ChunkOutcome.NOT_RETRIEVED, True))
    skipped = FunnelStage(status=FunnelStageStatus.SKIPPED, count=None)
    return SearchResponse(
        funnel=SearchFunnel(
            vector=FunnelStage(status=FunnelStageStatus.APPLIED, count=len(candidates)),
            fts=skipped,
            rrf=skipped,
            top_k_cut=FunnelStage(
                status=FunnelStageStatus.APPLIED,
                count=cut_count,
                discarded=discarded,
            ),
            compression=compression,
        ),
        compression_stage=request.compression_stage,
        results=results,
        duration=time.perf_counter() - started,
    )

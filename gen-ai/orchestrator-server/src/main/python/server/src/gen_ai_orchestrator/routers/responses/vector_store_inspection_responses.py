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
"""Response models for vector store inspection."""

from enum import Enum
from typing import Any, Optional

from pydantic import BaseModel, Field

from gen_ai_orchestrator.models.vector_stores.vector_store_provider import (
    VectorStoreProvider,
)
from gen_ai_orchestrator.models.vector_stores.vector_store_search_type import (
    DocumentSearchType,
)
from gen_ai_orchestrator.routers.requests.vector_store_inspection_requests import (
    AnomalyCode,
    CompressionStage,
)


class VectorStoreCapabilitiesResponse(BaseModel):
    provider: VectorStoreProvider
    search_types: list[DocumentSearchType]
    supports_scores: bool
    supports_index_listing: bool
    supports_metadata_filter: bool
    notes: list[str] = Field(default_factory=list)


class VectorStoreIndexDescription(BaseModel):
    index_name: str
    index_session_id: str
    index_datetime: str
    document_count: int
    chunk_count: int


class IndexListResponse(BaseModel):
    indexes: list[VectorStoreIndexDescription] = Field(default_factory=list)


class IndexAnomaly(BaseModel):
    code: AnomalyCode
    count: int
    severity: str


class IndexStats(BaseModel):
    document_count: int
    chunk_count: int
    chunks_per_document_avg: float
    chunk_length_median: float
    index_datetime: str


class InspectedChunk(BaseModel):
    chunk_id: str
    chunk: str
    content: str
    content_length: int
    metadata: dict[str, Any]


class InspectedDocument(BaseModel):
    document_id: str
    title: str
    source: Optional[str]
    chunk_count: int
    index_session_id: str
    chunks: Optional[list[InspectedChunk]] = None


class VectorStoreInspectionDocumentsResponse(BaseModel):
    stats: Optional[IndexStats] = None
    anomalies: list[IndexAnomaly] = Field(default_factory=list)
    rows: list[InspectedDocument] = Field(default_factory=list)
    total: int
    start: int
    end: int


class CondenseResponse(BaseModel):
    condensed_question: str
    key_words: list[str] = Field(default_factory=list)
    effective_prompt: str
    duration: float


class FunnelStageStatus(str, Enum):
    APPLIED = 'applied'
    SKIPPED = 'skipped'
    DISABLED = 'disabled'
    FAILED_FALLBACK = 'failed_fallback'


class FunnelStage(BaseModel):
    status: FunnelStageStatus
    count: Optional[int]
    reason: Optional[str] = None
    discarded: Optional[int] = None


class SearchFunnel(BaseModel):
    vector: FunnelStage
    fts: FunnelStage
    rrf: FunnelStage
    top_k_cut: FunnelStage
    compression: FunnelStage


class ChannelRanks(BaseModel):
    vector: Optional[int] = None
    fts: Optional[int] = None
    rrf: Optional[int] = None


class ChannelScores(BaseModel):
    vector: Optional[float] = None
    fts: Optional[float] = None
    rrf: Optional[float] = None
    compressor: Optional[float] = None


class ChunkOutcome(str, Enum):
    KEPT = 'kept'
    CUT_BY_TOP_K = 'cut_by_top_k'
    BELOW_MIN_SCORE = 'below_min_score'
    RERANKED_OUT = 'reranked_out'
    FILLED_BELOW_THRESHOLD = 'filled_below_threshold'
    NOT_RETRIEVED = 'not_retrieved'


class SearchResultChunk(BaseModel):
    chunk_id: str
    document_id: str
    title: str
    chunk: str
    content: str
    ranks: ChannelRanks
    scores: ChannelScores
    outcome: ChunkOutcome
    pinned: bool
    metadata: Optional[dict[str, Any]] = None


class SearchResponse(BaseModel):
    funnel: SearchFunnel
    compression_stage: CompressionStage
    results: list[SearchResultChunk]
    duration: float

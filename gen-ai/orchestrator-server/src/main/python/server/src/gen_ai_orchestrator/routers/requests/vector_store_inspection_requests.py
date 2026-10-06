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
"""Request models for vector store inspection."""

from enum import Enum
from typing import Optional

from pydantic import BaseModel, Field, model_validator

from gen_ai_orchestrator.models.document_compressor.document_compressor_types import (
    DocumentCompressorSetting,
)
from gen_ai_orchestrator.models.em.em_types import EMSetting
from gen_ai_orchestrator.models.llm.llm_types import LLMSetting
from gen_ai_orchestrator.models.prompt.prompt_template import PromptTemplate
from gen_ai_orchestrator.models.rag.rag_models import ChatMessage
from gen_ai_orchestrator.models.vector_stores.vector_store_search_type import (
    DocumentSearchType,
)
from gen_ai_orchestrator.models.vector_stores.vector_store_types import (
    VectorStoreSetting,
)


class AnomalyCode(str, Enum):
    NEAR_EMPTY_CHUNK = 'near_empty_chunk'
    NON_URL_SOURCE = 'non_url_source'
    DUPLICATE_TITLE = 'duplicate_title'


class CompressionStage(str, Enum):
    BEFORE_CUT = 'before_cut'
    AFTER_CUT = 'after_cut'


class PinnedRankStrategy(str, Enum):
    TRUNCATED = 'truncated'
    SCORE_ONLY = 'score_only'
    EXACT_RANK = 'exact_rank'


class VectorStoreInspectionCapabilitiesRequest(BaseModel):
    vector_store_setting: Optional[VectorStoreSetting] = None


class VectorStoreInspectionIndexesRequest(BaseModel):
    vector_store_setting: Optional[VectorStoreSetting] = None
    index_name_prefix: str = Field(min_length=1)


class DocumentsFilter(BaseModel):
    text: Optional[str] = None
    document_id: Optional[str] = None
    anomaly: Optional[AnomalyCode] = None


class VectorStoreInspectionDocumentsRequest(BaseModel):
    vector_store_setting: Optional[VectorStoreSetting] = None
    index_name_prefix: str = Field(min_length=1)
    index_name: str = Field(min_length=1)
    filter: Optional[DocumentsFilter] = None
    start: int = Field(default=0, ge=0)
    size: int = Field(default=25, ge=1, le=100)
    include_stats: bool = True
    include_chunks: bool = True


class VectorStoreInspectionCondenseRequest(BaseModel):
    question_condensing_llm_setting: LLMSetting
    question_condensing_prompt: PromptTemplate
    question: str = Field(min_length=1)
    chat_history: list[ChatMessage] = Field(default_factory=list)


class CompressionOverride(BaseModel):
    min_score: float = Field(ge=0, le=1)
    max_documents: int = Field(ge=1, le=500)
    fill_to_max_documents: bool = False


class VectorStoreInspectionSearchRequest(BaseModel):
    vector_store_setting: Optional[VectorStoreSetting] = None
    embedding_question_em_setting: EMSetting
    compressor_setting: Optional[DocumentCompressorSetting] = None
    index_name_prefix: str = Field(min_length=1)
    index_name: str = Field(min_length=1)
    search_type: DocumentSearchType
    query: str = Field(min_length=1)
    key_words: list[str] = Field(default_factory=list)
    fetch_k: int = Field(ge=1, le=500)
    k: int = Field(ge=1, le=500)
    compression_enabled: bool = False
    compression_stage: CompressionStage
    compression_override: Optional[CompressionOverride] = None
    pinned_chunk_ids: list[str] = Field(default_factory=list, max_length=50)
    pinned_rank_strategy: PinnedRankStrategy = PinnedRankStrategy.SCORE_ONLY

    @model_validator(mode='after')
    def validate_search(self):
        if self.search_type in {
            DocumentSearchType.FULL_TEXT_SEARCH,
            DocumentSearchType.HYBRID_SEARCH,
        } and not any(keyword.strip() for keyword in self.key_words):
            raise ValueError('key_words is required for full-text and hybrid search')
        if self.compression_enabled and self.compressor_setting is None:
            raise ValueError(
                'compressor_setting is required when compression is enabled'
            )
        return self

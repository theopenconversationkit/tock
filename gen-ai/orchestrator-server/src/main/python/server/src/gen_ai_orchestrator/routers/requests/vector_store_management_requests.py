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
"""Internal PGVector management requests. Bot scope is supplied by the admin server."""

from typing import Optional

from pydantic import BaseModel, Field, field_validator

from gen_ai_orchestrator.models.vector_stores.vector_store_types import (
    VectorStoreSetting,
)


class IndexTargetRequest(BaseModel):
    vector_store_setting: Optional[VectorStoreSetting] = None
    index_name_prefix: str = Field(min_length=1)
    index_name: str = Field(min_length=1)


class SetEmbeddingModelRequest(IndexTargetRequest):
    embedding_model: str = Field(min_length=1, max_length=300)

    @field_validator('embedding_model')
    @classmethod
    def non_blank_model(cls, value: str) -> str:
        if not value.strip():
            raise ValueError('An embedding model is required')
        return value.strip()


class ChunkTargetRequest(IndexTargetRequest):
    row_id: str = Field(min_length=1)


class DeleteChunkRequest(ChunkTargetRequest):
    protect_knowledge_base: bool
    allow_empty_index: bool = False


class DeleteIndexRequest(IndexTargetRequest):
    # Required, although nullable: an absent RAG configuration has no protected index.
    protected_index_name: Optional[str]

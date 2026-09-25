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
"""Internal requests: the admin server resolves all settings and index names."""

from typing import Optional

from pydantic import BaseModel, Field

from gen_ai_orchestrator.models.em.em_types import EMSetting
from gen_ai_orchestrator.models.vector_stores.vector_store_types import (
    VectorStoreSetting,
)


class KnowledgeBaseTargetRequest(BaseModel):
    vector_store_setting: Optional[VectorStoreSetting] = None
    index_name: str = Field(min_length=1)
    index_name_prefix: str = Field(min_length=1)


class KnowledgeBaseDocument(BaseModel):
    entry_id: str = Field(min_length=1, max_length=100)
    title: str = Field(min_length=1, max_length=300)
    search_hints: list[str] = Field(default_factory=list, max_length=30)
    content: str = Field(min_length=1, max_length=12000)
    source_url: Optional[str] = None


class KnowledgeBaseIndexRequest(KnowledgeBaseTargetRequest):
    em_setting: EMSetting
    index_session_id: str = Field(min_length=1)
    entries: list[KnowledgeBaseDocument] = Field(min_length=1, max_length=100)


class KnowledgeBaseDeletion(BaseModel):
    entry_id: str
    row_ids: list[str] = Field(default_factory=list, max_length=1000)


class KnowledgeBaseDeleteRequest(KnowledgeBaseTargetRequest):
    entries: list[KnowledgeBaseDeletion] = Field(min_length=1, max_length=100)

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
"""Per-entry acknowledgements and the actual KB rows in an index."""

from typing import Optional

from pydantic import BaseModel, Field


class KnowledgeBaseWriteResult(BaseModel):
    entry_id: str
    row_ids: list[str] = Field(default_factory=list)
    count: int = 0
    error: Optional[str] = None


class KnowledgeBaseWriteResponse(BaseModel):
    results: list[KnowledgeBaseWriteResult]


class KnowledgeBaseStoredRow(BaseModel):
    row_id: str
    entry_id: str
    content_hash: str
    title: str


class KnowledgeBaseRowsResponse(BaseModel):
    rows: list[KnowledgeBaseStoredRow]

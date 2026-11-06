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
"""Management results with stable error codes, relayed by the admin server."""

from typing import Any, Optional

from pydantic import BaseModel, Field


class IndexMetadataResponse(BaseModel):
    metadata: dict[str, Any] = Field(default_factory=dict)
    error: Optional[str] = None


class ChunkDescriptionResponse(BaseModel):
    metadata: dict[str, Any] = Field(default_factory=dict)
    chunk_count: int = 0
    error: Optional[str] = None


class VectorStoreWriteResponse(BaseModel):
    deleted_count: int = 0
    error: Optional[str] = None

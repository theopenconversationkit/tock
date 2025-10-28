#   Copyright (C) 2023-2025 Credit Mutuel Arkea
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
"""Model for creating BaseVectorStoreSearchParams."""

from abc import ABC, abstractmethod

from pydantic import BaseModel, Field

from gen_ai_orchestrator.models.vector_stores.vectore_store_provider import (
    VectorStoreProvider,
)


class BaseVectorStoreSearchParams(ABC, BaseModel):
    """A base class for specifying a Vector Store Search Params."""

    provider: VectorStoreProvider = Field(
        description='The Vector Store Provider.',
        examples=[VectorStoreProvider.OPEN_SEARCH],
    )
    k: int = Field(
        description='The number of Documents to return.',
        examples=[3],
        default=4,
    )

    @abstractmethod
    def to_dict(self) -> dict:
        pass

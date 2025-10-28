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
"""Model for creating PGVectorParams."""

from typing import Literal, Optional

from pydantic import Field

from gen_ai_orchestrator.models.vector_stores.vector_store_search_params import (
    BaseVectorStoreSearchParams,
)
from gen_ai_orchestrator.models.vector_stores.vectore_store_provider import (
    VectorStoreProvider,
)


class PGVectorParams(BaseVectorStoreSearchParams):
    """
    The PGVector params. Used to search with filter.
    https://api.python.langchain.com/en/latest/vectorstores/langchain_postgres.vectorstores.PGVector.html
    """

    provider: Literal[VectorStoreProvider.PGVECTOR] = Field(
        description='The Vector Store Provider.',
        examples=[VectorStoreProvider.PGVECTOR],
        default=VectorStoreProvider.PGVECTOR,
    )
    filter: Optional[dict] = Field(
        description='Filter by metadata.',
        examples=[{'tag1': 'vector stores'}],
        default=None,
    )

    def to_dict(self):
        return {
            'k': self.k,
            'filter': self.filter,
        }

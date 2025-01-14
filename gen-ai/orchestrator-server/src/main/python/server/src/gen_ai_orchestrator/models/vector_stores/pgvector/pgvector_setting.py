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
"""Model for creating PGVectorStoreSetting."""

from typing import Literal

from pydantic import Field

from gen_ai_orchestrator.models.security.raw_secret_key.raw_secret_key import (
    RawSecretKey,
)
from gen_ai_orchestrator.models.security.security_types import SecretKey
from gen_ai_orchestrator.models.vector_stores.vector_store_setting import (
    BaseVectorStoreSetting,
)
from gen_ai_orchestrator.models.vector_stores.vectore_store_provider import (
    VectorStoreProvider,
)


class PGVectorStoreSetting(BaseVectorStoreSetting):
    """
    A class for PGVector Vector Store Setting.
    Usage docs: https://github.com/pgvector/pgvector
    """

    provider: Literal[VectorStoreProvider.PGVECTOR] = Field(
        description='The Vector Store Provider.',
        examples=[VectorStoreProvider.PGVECTOR],
        default=VectorStoreProvider.PGVECTOR
    )
    host: str = Field(
        description='The hostname of postgres server', examples=['localhost'],
        default='localhost'
    )
    port: int = Field(
        description='The postgres server port', examples=['5432'],
        default='5432'
    )
    username: str = Field(
        description='The vector store connection username.',
        examples=['postgres'],
    )
    password: SecretKey = Field(
        description='The vector store connection password.',
        examples=[RawSecretKey(secret='postgres')],
    )
    database: str = Field(
        description='The postgres name', examples=['postgres'],
        default='postgres'
    )




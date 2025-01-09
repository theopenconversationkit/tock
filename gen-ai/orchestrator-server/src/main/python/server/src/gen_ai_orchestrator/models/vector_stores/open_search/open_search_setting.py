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
"""Model for creating OpenSearchVectorStoreSetting."""

from typing import Literal

from pydantic import AnyUrl, Field

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


class OpenSearchVectorStoreSetting(BaseVectorStoreSetting):
    """
    A class for OpenSearch Vector Store Setting.
    Usage docs: https://opensearch.org/docs
    """

    provider: Literal[VectorStoreProvider.OPEN_SEARCH] = Field(
        description='The Vector Store Provider.',
        examples=[VectorStoreProvider.OPEN_SEARCH],
        default=VectorStoreProvider.OPEN_SEARCH
    )
    host: str = Field(
        description='The hostname of OpenSearch server', examples=['localhost'],
        default='localhost'
    )
    port: int = Field(
        description='The OpenSearch server port', examples=['9200'],
        default='9200'
    )
    username: str = Field(
        description='The vector store connection username.',
        examples=['admin'],
    )
    password: SecretKey = Field(
        description='The vector store connection password.',
        examples=[RawSecretKey(secret='*************')],
    )


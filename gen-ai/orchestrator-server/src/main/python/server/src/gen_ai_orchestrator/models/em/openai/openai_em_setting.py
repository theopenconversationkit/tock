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
"""Model for creating OpenAIEMSetting."""

from typing import Literal

from pydantic import Field

from gen_ai_orchestrator.models.em.em_provider import EMProvider
from gen_ai_orchestrator.models.em.em_setting import BaseEMSetting
from gen_ai_orchestrator.models.security.raw_secret_key.raw_secret_key import (
    RawSecretKey,
)
from gen_ai_orchestrator.models.security.security_types import SecretKey


class OpenAIEMSetting(BaseEMSetting):
    """
    A class for OpenAI Embedding Model Setting.
    Usage docs: https://platform.openai.com/docs/guides/embeddings
    """

    provider: Literal[EMProvider.OPEN_AI] = Field(
        description='The Embedding Model Provider.', examples=[EMProvider.OPEN_AI]
    )
    api_key: SecretKey = Field(
        description='The secret that stores the API key used to authenticate requests to the AI Provider API.',
        examples=[RawSecretKey(secret='ab7-************-A1IV4B')],
    )
    model: str = Field(description='The model id', examples=['text-embedding-ada-002'])
    base_url: str = Field(
        description='The OpenAI endpoint base URL',
        examples=['https://api.openai.com/v1'],
        default='https://api.openai.com/v1',
    )
    number_of_chunk_per_request: int = Field(
        description='The number of chunks sent per request, take care of your rate limits. Default value is 50.',
        default=50,
    )

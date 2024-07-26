#   Copyright (C) 2023-2024 Credit Mutuel Arkea
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
"""Model for creating BaseLLMSetting."""

from typing import Optional

from pydantic import BaseModel, Field

from gen_ai_orchestrator.models.llm.llm_provider import LLMProvider
from gen_ai_orchestrator.models.security.raw_secret_key.raw_secret_key import (
    RawSecretKey,
)
from gen_ai_orchestrator.models.security.security_types import SecretKey


class BaseLLMSetting(BaseModel):
    """A base class for Large Language Model Setting."""

    provider: LLMProvider = Field(description='The Large Language Model Provider.')
    api_key: Optional[SecretKey] = Field(
        description='The secret that stores the API key used to authenticate requests to the AI Provider API.',
        examples=[RawSecretKey(value='ab7-14Ed2-dfg2F-A1IV4B')],
        default=None,
    )
    temperature: float = Field(
        description='The temperature that controls the randomness of the text generated.',
        examples=['1.2'],
        ge=0,
        le=2,
    )
    prompt: str = Field(
        description='The prompt to generate completions for.',
        examples=['How to learn to ride a bike without wheels!'],
        min_length=1,
    )

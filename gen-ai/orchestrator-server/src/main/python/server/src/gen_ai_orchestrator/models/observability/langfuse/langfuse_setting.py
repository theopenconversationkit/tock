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
"""Model for creating LangfuseObservabilitySetting."""

from typing import Literal, Optional

from pydantic import AnyUrl, Field

from gen_ai_orchestrator.models.observability.observability_provider import (
    ObservabilityProvider,
)
from gen_ai_orchestrator.models.observability.observability_setting import (
    BaseObservabilitySetting,
)
from gen_ai_orchestrator.models.security.raw_secret_key.raw_secret_key import (
    RawSecretKey,
)
from gen_ai_orchestrator.models.security.security_types import SecretKey


class LangfuseObservabilitySetting(BaseObservabilitySetting):
    """
    A class for Langfuse Observability Setting.
    Usage docs: https://langfuse.com/docs
    """

    provider: Literal[ObservabilityProvider.LANGFUSE] = Field(
        description='The Observability Provider.', examples=[ObservabilityProvider.LANGFUSE]
    )
    url: AnyUrl = Field(
        description='The Langfuse server url', examples=['https://cloud.langfuse.com'], default='http://localhost:3000'
    )
    public_url: Optional[AnyUrl] = Field(
        default=None,
        description="Optional public URL for Langfuse server",
        examples=["https://public.langfuse.com"]
    )
    secret_key: SecretKey = Field(
        description='Stores the secret key used to authenticate requests to the Observability Provider API.',
        examples=[RawSecretKey(secret='sk-********************be8f')],
    )
    public_key: str = Field(
        description='Stores the public key used to authenticate requests to the Observability Provider API.',
        examples=['pk-lf-5e374dc6-e194-4b37-9c07-b77e68ef7d2c'],
    )

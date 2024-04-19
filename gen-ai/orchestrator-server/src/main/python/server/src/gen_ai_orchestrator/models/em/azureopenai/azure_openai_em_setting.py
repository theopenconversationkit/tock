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
"""Model for creating AzureOpenAIEMSetting."""

from typing import Literal

from pydantic import Field, HttpUrl

from gen_ai_orchestrator.models.em.em_provider import EMProvider
from gen_ai_orchestrator.models.em.em_setting import BaseEMSetting
from gen_ai_orchestrator.models.security.raw_secret_key.raw_secret_key import RawSecretKey
from gen_ai_orchestrator.models.security.security_types import SecretKey


class AzureOpenAIEMSetting(BaseEMSetting):
    """
    A class for Azure OpenAI Embedding Model Setting.
    Usage docs: https://learn.microsoft.com/en-us/azure/ai-services/openai/reference
    """

    provider: Literal[EMProvider.AZURE_OPEN_AI_SERVICE] = Field(
        description='The Embedding Model Provider.',
        examples=[EMProvider.AZURE_OPEN_AI_SERVICE],
    )
    api_key: SecretKey = Field(
        description='The secret that stores the API key used to authenticate requests to the AI Provider API.',
        examples=[RawSecretKey(value='ab7-14Ed2-dfg2F-A1IV4B')]
    )
    deployment_name: str = Field(
        description='The deployment name you chose when you deployed the model.',
        examples=['my-deployment-name'],
    )
    api_base: HttpUrl = Field(
        description='The API base url / Azure endpoint',
        examples=['https://doc.tock.ai/tock'],
    )
    api_version: str = Field(
        description='The API version to use for this operation.',
        examples=['2023-05-15'],
    )

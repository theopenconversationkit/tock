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
"""Model for creating OpenAIEMSetting."""

from typing import Literal

from pydantic import Field

from gen_ai_orchestrator.models.em.em_provider import EMProvider
from gen_ai_orchestrator.models.em.em_setting import BaseEMSetting


class OpenAIEMSetting(BaseEMSetting):
    """
    A class for OpenAI Embedding Model Setting.
    Usage docs: https://platform.openai.com/docs/guides/embeddings
    """

    provider: Literal[EMProvider.OPEN_AI] = Field(
        description='The Embedding Model Provider.', examples=[EMProvider.OPEN_AI]
    )
    model: str = Field(description='The model id', examples=['text-embedding-ada-002'])

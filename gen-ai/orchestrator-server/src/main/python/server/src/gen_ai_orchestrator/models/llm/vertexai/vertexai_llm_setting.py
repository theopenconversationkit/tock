#   Copyright (C) 2024 Credit Mutuel Arkea
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
#  Copyright (C) 2017/2021 e-voyageurs technologies
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
"""Model for creating VertexAILLMSetting."""

from typing import Literal

from pydantic import Field

from gen_ai_orchestrator.models.llm.llm_provider import LLMProvider
from gen_ai_orchestrator.models.llm.llm_setting import BaseLLMSetting


class VertexAILLMSetting(BaseLLMSetting):
    """
    A class for VertexAI Large Language Model Setting.
    Usage docs: https://cloud.google.com/vertex-ai/generative-ai/docs/learn/models
    """

    provider: Literal[LLMProvider.VERTEX_AI] = Field(
        description='The Large Language Model Provider.',
        examples=[LLMProvider.VERTEX_AI],
    )
    model: str = Field(
        description='The model id', examples=['gemini-1.5-pro-001'], min_length=1
    )
    project_id: str = Field(
        description='The project ID related to the AI service request',
        examples=['my-project-id'],
    )
    location: str = Field(
        description='The location context for the AI service request',
        examples=['europe-west4'],
    )

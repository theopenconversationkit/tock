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
"""Model for creating HuggingFaceTGILLMSetting."""

from typing import Literal

from pydantic import Field

from gen_ai_orchestrator.models.llm.llm_provider import LLMProvider
from gen_ai_orchestrator.models.llm.llm_setting import BaseLLMSetting


class HuggingFaceTGILLMSetting(BaseLLMSetting):
    """
    A class for Hugging Face TGI Large Language Model Setting.
    """

    provider: Literal[LLMProvider.HUGGING_FACE_TGI] = Field(
        description='The Large Language Model Provider.',
        examples=[LLMProvider.HUGGING_FACE_TGI],
    )
    repetition_penalty: float = Field(
        description='Penalty on model repetition.', default=1.0, examples=[1.0]
    )
    max_new_tokens: int = Field(
        description='Maximum length of the llm response in tokens.',
        default=256,
        examples=[256],
    )
    api_base: str = Field(
        description='TGI API base URL.', examples=['https://doc.tock.ai/tock/fr/']
    )

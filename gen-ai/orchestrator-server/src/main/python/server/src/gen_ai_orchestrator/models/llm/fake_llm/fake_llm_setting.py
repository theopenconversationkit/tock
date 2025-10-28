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
"""Model for creating FakeLLMSetting."""

from typing import List, Literal

from pydantic import Field

from gen_ai_orchestrator.models.llm.llm_provider import LLMProvider
from gen_ai_orchestrator.models.llm.llm_setting import BaseLLMSetting


class FakeLLMSetting(BaseLLMSetting):
    """
    A class for Fake Large Language Model Setting.
    Usage docs: https://js.langchain.com/docs/integrations/llms/fake
    """
    provider: Literal[LLMProvider.FAKE_LLM] = Field(
        description='The Large Language Model provider.'
    )
    responses: List[str] = Field(
        description='The responses given when the Fake LLM is called'
    )

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
"""Model for creating FakeLLMFactory"""

from langchain.base_language import BaseLanguageModel
from langchain_community.chat_models.fake import FakeListChatModel

from gen_ai_orchestrator.models.llm.fake_llm.fake_llm_setting import (
    FakeLLMSetting,
)
from gen_ai_orchestrator.services.langchain.factories.llm.llm_factory import (
    LangChainLLMFactory,
)


class FakeLLMFactory(LangChainLLMFactory):
    """A class for LangChain Fake LLM Factory"""
    setting: FakeLLMSetting

    def get_language_model(self) -> BaseLanguageModel:
        return FakeListChatModel(responses=self.setting.responses)

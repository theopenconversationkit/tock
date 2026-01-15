#   Copyright (C) 2023-2026 Credit Mutuel Arkea
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
"""Model for creating OllamaLLMFactory"""
from typing import Optional

from langchain.base_language import BaseLanguageModel
from langchain_community.chat_models import ChatOllama
from langchain_core.runnables import RunnableConfig
from langchain_core.runnables.utils import Input, Output

from gen_ai_orchestrator.errors.handlers.ollama.ollama_exception_handler import (
    ollama_exception_handler,
)
from gen_ai_orchestrator.models.llm.ollama.ollama_llm_setting import (
    OllamaLLMSetting,
)
from gen_ai_orchestrator.services.langchain.factories.llm.llm_factory import (
    LangChainLLMFactory,
)


class OllamaLLMFactory(LangChainLLMFactory):
    """A class for LangChain Ollama LLM Factory"""

    setting: OllamaLLMSetting

    def get_language_model(self) -> BaseLanguageModel:
        return ChatOllama(
            base_url=self.setting.base_url,
            model=self.setting.model,
            temperature=self.setting.temperature
        )

    @ollama_exception_handler(provider='Ollama')
    async def invoke(self, _input: Input, config: Optional[RunnableConfig] = None) -> Output:
        return await super().invoke(_input, config)

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
"""Model for creating OpenAILLMFactory"""
from typing import Optional

from langchain.base_language import BaseLanguageModel
from langchain_core.runnables import RunnableConfig
from langchain_core.runnables.utils import Input, Output
from langchain_openai import ChatOpenAI

from gen_ai_orchestrator.configurations.environment.settings import (
    application_settings,
)
from gen_ai_orchestrator.errors.handlers.openai.openai_exception_handler import (
    openai_exception_handler,
)
from gen_ai_orchestrator.models.llm.openai.openai_llm_setting import (
    OpenAILLMSetting,
)
from gen_ai_orchestrator.services.langchain.factories.llm.llm_factory import (
    LangChainLLMFactory, rate_limiter,
)
from gen_ai_orchestrator.services.security.security_service import (
    fetch_secret_key_value,
)


class OpenAILLMFactory(LangChainLLMFactory):
    """A class for LangChain OpenAI LLM Factory"""

    setting: OpenAILLMSetting

    def get_language_model(self) -> BaseLanguageModel:
        return ChatOpenAI(
            openai_api_key=fetch_secret_key_value(self.setting.api_key),
            base_url=self.setting.base_url,
            model_name=self.setting.model,
            temperature=self.setting.temperature,
            request_timeout=application_settings.llm_provider_timeout,
            max_retries=application_settings.llm_provider_max_retries,
            rate_limiter=rate_limiter if application_settings.llm_rate_limits else None
        )

    @openai_exception_handler(provider='OpenAI')
    async def invoke(self, _input: Input, config: Optional[RunnableConfig] = None) -> Output:
        return await super().invoke(_input, config)

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
"""Model for creating AzureOpenAILLMFactory"""
from langchain.base_language import BaseLanguageModel
from langchain_google_vertexai.chat_models import ChatVertexAI
from langchain_openai import AzureChatOpenAI

from gen_ai_orchestrator.configurations.environment.settings import (
    application_settings,
)
from gen_ai_orchestrator.models.llm.vertexai.vertexai_llm_setting import (
    VertexAILLMSetting,
)
from gen_ai_orchestrator.services.langchain.factories.llm.llm_factory import (
    LangChainLLMFactory,
)
from gen_ai_orchestrator.services.security.security_service import (
    fetch_secret_key_value,
)


class VertexAILLMFactory(LangChainLLMFactory):
    """A class for LangChain Azure OpenAI LLM Factory"""

    setting: VertexAILLMSetting

    def get_language_model(self) -> BaseLanguageModel:
        return ChatVertexAI()

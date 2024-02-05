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
"""Model for creating AzureOpenAIEMFactory"""

from langchain.embeddings.base import Embeddings
from langchain_openai import AzureOpenAIEmbeddings

from llm_orchestrator.errors.handlers.openai.openai_exception_handler import (
    openai_exception_handler,
)
from llm_orchestrator.models.em.azureopenai.azure_openai_em_setting import (
    AzureOpenAIEMSetting,
)
from llm_orchestrator.services.langchain.factories.em.em_factory import (
    LangChainEMFactory,
)


class AzureOpenAIEMFactory(LangChainEMFactory):
    """A class for LangChain Azure OpenAI Embedding Factory"""

    setting: AzureOpenAIEMSetting

    def get_embedding_model(self) -> Embeddings:
        return AzureOpenAIEmbeddings(
            openai_api_key=self.setting.api_key,
            openai_api_version=self.setting.api_version,
            azure_endpoint=str(self.setting.api_base),
            azure_deployment=self.setting.deployment_name,
        )

    @openai_exception_handler(provider='AzureOpenAIService')
    def check_embedding_model_setting(self) -> bool:
        return super().check_embedding_model_setting()

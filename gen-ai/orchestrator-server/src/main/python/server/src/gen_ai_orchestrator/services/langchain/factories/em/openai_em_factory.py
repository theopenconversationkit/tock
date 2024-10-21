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
"""Model for creating OpenAIEMFactory"""
from typing import List

from langchain.embeddings.base import Embeddings
from langchain_openai import OpenAIEmbeddings
from openai import base_url

from gen_ai_orchestrator.configurations.environment.settings import (
    application_settings,
)
from gen_ai_orchestrator.errors.handlers.openai.openai_exception_handler import (
    openai_exception_handler,
)
from gen_ai_orchestrator.models.em.openai.openai_em_setting import (
    OpenAIEMSetting,
)
from gen_ai_orchestrator.models.security.raw_secret_key.raw_secret_key import (
    RawSecretKey,
)
from gen_ai_orchestrator.services.langchain.factories.em.em_factory import (
    LangChainEMFactory,
)
from gen_ai_orchestrator.services.security.security_service import (
    fetch_secret_key_value,
)


class OpenAIEMFactory(LangChainEMFactory):
    """A class for LangChain OpenAI Embedding Factory"""

    setting: OpenAIEMSetting

    def get_embedding_model(self) -> Embeddings:
        return OpenAIEmbeddings(
            openai_api_key=fetch_secret_key_value(self.setting.api_key),
            base_url=self.setting.base_url,
            model=self.setting.model,
            timeout=application_settings.em_provider_timeout,
        )

    @openai_exception_handler(provider='OpenAI')
    async def embed_query(self, text: str) -> List[float]:
        return await super().embed_query(text)

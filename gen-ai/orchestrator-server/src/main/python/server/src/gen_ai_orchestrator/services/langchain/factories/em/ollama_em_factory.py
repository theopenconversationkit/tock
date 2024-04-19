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
"""Model for creating OllamaEMFactory"""
from gen_ai_orchestrator.errors.handlers.ollama.ollama_exception_handler import ollama_exception_handler
from gen_ai_orchestrator.models.em.ollama.ollama_em_setting import OllamaEMSetting
from gen_ai_orchestrator.services.langchain.factories.em.em_factory import (
    LangChainEMFactory,
)
from langchain.embeddings.base import Embeddings
from langchain_community.embeddings import OllamaEmbeddings
from typing import List


class OllamaEMFactory(LangChainEMFactory):
    """A class for LangChain Ollama Embedding Factory"""

    setting: OllamaEMSetting

    def get_embedding_model(self) -> Embeddings:
        return OllamaEmbeddings(
            base_url=self.setting.base_url,
            model=self.setting.model,
        )

    @ollama_exception_handler(provider='Ollama')
    async def embed_query(self, text: str) -> List[float]:
        return await super().embed_query(text)

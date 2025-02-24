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
"""Model for creating PGVectorFactory"""
import logging
from typing import Optional

from langchain_core.vectorstores import VectorStoreRetriever
from langchain_postgres import PGVector

from gen_ai_orchestrator.configurations.environment.settings import application_settings
from gen_ai_orchestrator.models.vector_stores.pgvector.pgvector_setting import PGVectorStoreSetting
from gen_ai_orchestrator.services.langchain.factories.vector_stores.vector_store_factory import (
    LangChainVectorStoreFactory,
)
from gen_ai_orchestrator.services.security.security_service import fetch_secret_key_value
from gen_ai_orchestrator.utils.strings import obfuscate

logger = logging.getLogger(__name__)


class PGVectorFactory(LangChainVectorStoreFactory):
    """
    A class for LangChain PGVector Factory
    https://api.python.langchain.com/en/latest/vectorstores/langchain_postgres.vectorstores.PGVector.html
    """

    setting: PGVectorStoreSetting

    def get_vector_store(self, async_mode: Optional[bool] = True) -> PGVector:
        password = fetch_secret_key_value(self.setting.password)
        logger.info(
            'PostgreSQL user credentials: %s:%s',
            self.setting.username,
            obfuscate(password),
        )

        return PGVector(
            embeddings=self.embedding_function,
            collection_name=self.index_name,
            connection=f'postgresql+psycopg://{self.setting.username}:{password}@{self.setting.host}:{self.setting.port}/{self.setting.database}',
            use_jsonb=True,
            async_mode=async_mode
        )

    def get_vector_store_retriever(self, search_kwargs: dict, async_mode: Optional[bool] = True) -> VectorStoreRetriever:
        return self.get_vector_store(async_mode).as_retriever(
            search_kwargs=search_kwargs
        )

    async def check_vector_store_connection(self) -> bool:
        """
        To check the connection information, we perform a vector search :The objective is not the search result itself.
        We do this because the langchain implementation doesn't provide us with a method on the
        VectorStore to check the connection independently.
        """
        await self.get_vector_store().asimilarity_search(
            query=application_settings.vector_store_test_query, k=application_settings.vector_store_test_max_docs_retrieved)
        return True

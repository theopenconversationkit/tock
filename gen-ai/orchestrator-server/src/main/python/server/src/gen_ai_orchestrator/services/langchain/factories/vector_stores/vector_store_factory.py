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
"""Module for the LangChain Vector Store Factory"""

import logging
from abc import ABC, abstractmethod
from typing import List, Optional

from langchain_core.documents import Document
from langchain_core.embeddings import Embeddings
from langchain_core.vectorstores import VectorStore, VectorStoreRetriever
from pydantic import BaseModel, ConfigDict

from gen_ai_orchestrator.configurations.environment.settings import (
    application_settings,
)
from gen_ai_orchestrator.errors.exceptions.vector_store.vector_store_exceptions import (
    GenAIVectorStoreNoDocumentRetrievedException,
)
from gen_ai_orchestrator.errors.handlers.opensearch.opensearch_exception_handler import (
    opensearch_exception_handler,
)
from gen_ai_orchestrator.models.errors.errors_models import ErrorInfo
from gen_ai_orchestrator.models.vector_stores.vector_store_setting import (
    BaseVectorStoreSetting,
)

logger = logging.getLogger(__name__)


class LangChainVectorStoreFactory(ABC, BaseModel):
    """A base class for LangChain Vector Store Factory"""

    setting: BaseVectorStoreSetting
    embedding_function: Embeddings
    index_name: str
    model_config = ConfigDict(arbitrary_types_allowed=True)

    @abstractmethod
    def get_vector_store(self, async_mode: Optional[bool] = True) -> VectorStore:
        """
        Fabric the Vector Store.
        Args:
            async_mode: enable/disable the async_mode for vector DB client (if supported). Default to True.
        :return: VectorStore the interface for Vector Database.
        """
        pass

    @abstractmethod
    def get_vector_store_retriever(self, search_kwargs: dict, async_mode: Optional[bool] = True) -> VectorStoreRetriever:
        """
        Fabric the Vector Store and return it as retriever
        Args:
            search_kwargs: the search filter
            async_mode: enable/disable the async_mode for vector DB client (if supported). Default to True.
        :return: A VectorStoreRetriever.
        """
        pass

    @opensearch_exception_handler
    async def check_vector_store_setting(self) -> bool:
        """
        check the vector store setting validity

        Returns:
            True if the setting is valid.

        Raises:
            BusinessException: For incorrect setting
        """
        logger.info('Invoke vector store provider to check setting')
        documents: List[Document] = await self.get_vector_store().asimilarity_search(
            query=application_settings.vector_store_test_query, k=application_settings.vector_store_test_max_docs_retrieved
        )
        logger.debug('Invocation successful')
        logger.debug('[index: %s], [query: %s], [document count: %s]', self.index_name, application_settings.vector_store_test_query, len(documents))
        if len(documents) > 0 :
            return True
        else:
            logger.warning('No documents retrieved from the Vector Store')
            raise GenAIVectorStoreNoDocumentRetrievedException(ErrorInfo(
                provider=self.setting.provider.value,
                error='No documents retrieved',
                cause='Index not found or data not ingested'
            ))

    @abstractmethod
    async def check_vector_store_connection(self) -> bool:
        """Check vector store connection and authentication"""
        pass

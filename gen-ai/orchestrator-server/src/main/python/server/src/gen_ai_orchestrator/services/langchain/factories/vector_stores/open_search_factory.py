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
"""Model for creating OpenSearchFactory"""
import logging

from langchain_community.vectorstores.opensearch_vector_search import OpenSearchVectorSearch
from langchain_core.vectorstores import VectorStoreRetriever

from gen_ai_orchestrator.configurations.environment.settings import (
    application_settings,
    is_prod_environment
)
from gen_ai_orchestrator.errors.handlers.opensearch.opensearch_exception_handler import opensearch_exception_handler
from gen_ai_orchestrator.models.vector_stores.open_search.open_search_setting import OpenSearchVectorStoreSetting
from gen_ai_orchestrator.services.langchain.factories.vector_stores.vector_store_factory import (
    LangChainVectorStoreFactory,
)
from gen_ai_orchestrator.services.security.security_service import fetch_secret_key_value
from gen_ai_orchestrator.utils.strings import obfuscate

logger = logging.getLogger(__name__)


class OpenSearchFactory(LangChainVectorStoreFactory):
    """
    A class for LangChain OpenSearch Factory
    https://www.elastic.co/guide/en/enterprise-search-clients/python/current/connecting.html
    """

    setting: OpenSearchVectorStoreSetting

    def get_vector_store(self) -> OpenSearchVectorSearch:
        password = fetch_secret_key_value(self.setting.password)
        logger.info(
            'OpenSearch user credentials: %s:%s',
            self.setting.username,
            obfuscate(password),
        )
        return OpenSearchVectorSearch(
            opensearch_url=f'https://{self.setting.host}:{self.setting.port}',
            http_auth=(
                self.setting.username,
                fetch_secret_key_value(self.setting.password),
            ),
            use_ssl=is_prod_environment,
            verify_certs=is_prod_environment,
            # Expected hostname on the server certificate.
            # By default, is the same as host. If set to False, it will not verify hostname on certificate
            ssl_assert_hostname=self.setting.host if is_prod_environment else False,
            ssl_show_warn=is_prod_environment,
            index_name=self.index_name,
            embedding_function=self.embedding_function,
            timeout=application_settings.vector_store_timeout,
        )

    def get_vector_store_retriever(self, search_kwargs: dict) -> VectorStoreRetriever:
        return self.get_vector_store().as_retriever(
            search_kwargs=search_kwargs
        )

    @opensearch_exception_handler
    async def check_vector_store_connection(self) -> bool:
        """To check the connection information, we ask for basic information about the cluster."""
        self.get_vector_store().client.info()
        return True

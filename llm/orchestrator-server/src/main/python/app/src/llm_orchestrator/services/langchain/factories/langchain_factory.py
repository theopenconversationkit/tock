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

import logging

from langchain_core.embeddings import Embeddings

from llm_orchestrator.errors.exceptions.exceptions import (
    GenAIUnknownProviderSettingException,
    VectorStoreUnknownException,
)
from llm_orchestrator.models.em.azureopenai.azure_openai_em_setting import (
    AzureOpenAIEMSetting,
)
from llm_orchestrator.models.em.em_setting import BaseEMSetting
from llm_orchestrator.models.em.openai.openai_em_setting import OpenAIEMSetting
from llm_orchestrator.models.llm.azureopenai.azure_openai_llm_setting import (
    AzureOpenAILLMSetting,
)
from llm_orchestrator.models.llm.llm_setting import BaseLLMSetting
from llm_orchestrator.models.llm.openai.openai_llm_setting import (
    OpenAILLMSetting,
)
from llm_orchestrator.models.vector_stores.vectore_store_provider import (
    VectorStoreProvider,
)
from llm_orchestrator.services.langchain.factories.em.azure_openai_em_factory import (
    AzureOpenAIEMFactory,
)
from llm_orchestrator.services.langchain.factories.em.em_factory import (
    LangChainEMFactory,
)
from llm_orchestrator.services.langchain.factories.em.openai_em_factory import (
    OpenAIEMFactory,
)
from llm_orchestrator.services.langchain.factories.llm.azure_openai_llm_factory import (
    AzureOpenAILLMFactory,
)
from llm_orchestrator.services.langchain.factories.llm.llm_factory import (
    LangChainLLMFactory,
)
from llm_orchestrator.services.langchain.factories.llm.openai_llm_factory import (
    OpenAILLMFactory,
)
from llm_orchestrator.services.langchain.factories.vector_stores.open_search_factory import (
    OpenSearchFactory,
)
from llm_orchestrator.services.langchain.factories.vector_stores.vector_store_factory import (
    LangChainVectorStoreFactory,
)

logger = logging.getLogger(__name__)


def get_llm_factory(setting: BaseLLMSetting) -> LangChainLLMFactory:
    logger.info('Get LLM Factory for the given setting')
    if isinstance(setting, OpenAILLMSetting):
        logger.debug('LLM Factory - OpenAILLMFactory')
        return OpenAILLMFactory(setting=setting)
    elif isinstance(setting, AzureOpenAILLMSetting):
        logger.debug('LLM Factory - AzureOpenAILLMFactory')
        return AzureOpenAILLMFactory(setting=setting)
    else:
        raise GenAIUnknownProviderSettingException()


def get_em_factory(setting: BaseEMSetting) -> LangChainEMFactory:
    logger.info('Get Embedding Model Factory for the given setting')
    if isinstance(setting, OpenAIEMSetting):
        logger.debug('EM Factory - OpenAIEMFactory')
        return OpenAIEMFactory(setting=setting)
    elif isinstance(setting, AzureOpenAIEMSetting):
        logger.debug('EM Factory - AzureOpenAIEMFactory')
        return AzureOpenAIEMFactory(setting=setting)
    else:
        raise GenAIUnknownProviderSettingException()


def get_vector_store_factory(
    vector_store_provider: VectorStoreProvider,
    embedding_function: Embeddings,
    index_name: str,
) -> LangChainVectorStoreFactory:
    logger.info('Get Vector Store Factory for the given provider')
    if VectorStoreProvider.OPEN_SEARCH == vector_store_provider:
        logger.debug('Vector Store Factory - OpenSearchFactory')
        return OpenSearchFactory(
            embedding_function=embedding_function, index_name=index_name
        )
    else:
        raise VectorStoreUnknownException()

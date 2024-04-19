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
"""
Module for the LangChain Factory.
It manages the creation of :
    - LLM Factory
    - EM Factory
    - Vector Store Factory
"""

import logging
from typing import Optional

from langchain_core.embeddings import Embeddings
from langfuse.callback import CallbackHandler as LangfuseCallbackHandler

from gen_ai_orchestrator.configurations.environment.settings import open_search_password, open_search_username, \
    application_settings
from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIUnknownProviderSettingException,
)
from gen_ai_orchestrator.errors.exceptions.observability.observability_exceptions import \
    GenAIUnknownObservabilityProviderSettingException
from gen_ai_orchestrator.errors.exceptions.vector_store.vector_store_exceptions import \
    GenAIUnknownVectorStoreProviderSettingException
from gen_ai_orchestrator.models.em.azureopenai.azure_openai_em_setting import (
    AzureOpenAIEMSetting,
)
from gen_ai_orchestrator.models.em.em_setting import BaseEMSetting
from gen_ai_orchestrator.models.em.ollama.ollama_em_setting import OllamaEMSetting
from gen_ai_orchestrator.models.em.openai.openai_em_setting import OpenAIEMSetting
from gen_ai_orchestrator.models.llm.azureopenai.azure_openai_llm_setting import (
    AzureOpenAILLMSetting,
)
from gen_ai_orchestrator.models.llm.fake_llm.fake_llm_setting import FakeLLMSetting
from gen_ai_orchestrator.models.llm.llm_setting import BaseLLMSetting
from gen_ai_orchestrator.models.llm.ollama.ollama_llm_setting import OllamaLLMSetting
from gen_ai_orchestrator.models.llm.openai.openai_llm_setting import (
    OpenAILLMSetting,
)
from gen_ai_orchestrator.models.observability.langfuse.langfuse_setting import LangfuseObservabilitySetting
from gen_ai_orchestrator.models.observability.observability_setting import BaseObservabilitySetting
from gen_ai_orchestrator.models.observability.observability_trace import ObservabilityTrace
from gen_ai_orchestrator.models.observability.observability_type import ObservabilitySetting
from gen_ai_orchestrator.models.security.raw_secret_key.raw_secret_key import RawSecretKey
from gen_ai_orchestrator.models.vector_stores.open_search.open_search_setting import OpenSearchVectorStoreSetting
from gen_ai_orchestrator.models.vector_stores.vector_store_types import VectorStoreSetting
from gen_ai_orchestrator.routers.requests.requests import RagQuery
from gen_ai_orchestrator.services.langchain.factories.callback_handlers.callback_handlers_factory import \
    LangChainCallbackHandlerFactory
from gen_ai_orchestrator.services.langchain.factories.callback_handlers.langfuse_callback_handler_factory import \
    LangfuseCallbackHandlerFactory
from gen_ai_orchestrator.services.langchain.factories.em.azure_openai_em_factory import (
    AzureOpenAIEMFactory,
)
from gen_ai_orchestrator.services.langchain.factories.em.em_factory import (
    LangChainEMFactory,
)
from gen_ai_orchestrator.services.langchain.factories.em.ollama_em_factory import OllamaEMFactory
from gen_ai_orchestrator.services.langchain.factories.em.openai_em_factory import (
    OpenAIEMFactory,
)
from gen_ai_orchestrator.services.langchain.factories.llm.azure_openai_llm_factory import (
    AzureOpenAILLMFactory,
)
from gen_ai_orchestrator.services.langchain.factories.llm.fake_llm_factory import FakeLLMFactory
from gen_ai_orchestrator.services.langchain.factories.llm.llm_factory import (
    LangChainLLMFactory,
)
from gen_ai_orchestrator.services.langchain.factories.llm.ollama_llm_factory import OllamaLLMFactory
from gen_ai_orchestrator.services.langchain.factories.llm.openai_llm_factory import (
    OpenAILLMFactory,
)
from gen_ai_orchestrator.services.langchain.factories.vector_stores.open_search_factory import (
    OpenSearchFactory,
)
from gen_ai_orchestrator.services.langchain.factories.vector_stores.vector_store_factory import (
    LangChainVectorStoreFactory,
)

logger = logging.getLogger(__name__)


def get_llm_factory(setting: BaseLLMSetting) -> LangChainLLMFactory:
    """
    Creates an LangChain LLM Factory according to the given setting
    Args:
        setting: The LLM setting

    Returns:
        The LangChain LLM Factory, or raise an exception otherwise
    """

    logger.info('Get LLM Factory for the given setting')
    if isinstance(setting, OpenAILLMSetting):
        logger.debug('LLM Factory - OpenAILLMFactory')
        return OpenAILLMFactory(setting=setting)
    elif isinstance(setting, AzureOpenAILLMSetting):
        logger.debug('LLM Factory - AzureOpenAILLMFactory')
        return AzureOpenAILLMFactory(setting=setting)
    elif isinstance(setting, FakeLLMSetting):
        logger.debug('LLM Factory - FakeLLMFactory')
        return FakeLLMFactory(setting=setting)
    elif isinstance(setting, OllamaLLMSetting):
        logger.debug('LLM Factory - OllamaLLMFactory')
        return OllamaLLMFactory(setting=setting)
    else:
        raise GenAIUnknownProviderSettingException()


def get_em_factory(setting: BaseEMSetting) -> LangChainEMFactory:
    """
    Creates an LangChain EM Factory according to the given setting
    Args:
        setting: The EM setting

    Returns:
        The LangChain EM Factory, or raise an exception otherwise
    """

    logger.info('Get Embedding Model Factory for the given setting')
    if isinstance(setting, OpenAIEMSetting):
        logger.debug('EM Factory - OpenAIEMFactory')
        return OpenAIEMFactory(setting=setting)
    elif isinstance(setting, AzureOpenAIEMSetting):
        logger.debug('EM Factory - AzureOpenAIEMFactory')
        return AzureOpenAIEMFactory(setting=setting)
    elif isinstance(setting, OllamaEMSetting):
        logger.debug('LLM Factory - OllamaEMFactory')
        return OllamaEMFactory(setting=setting)
    else:
        raise GenAIUnknownProviderSettingException()


def get_vector_store_factory(
        setting: Optional[VectorStoreSetting],
        index_name: str,
        embedding_function: Embeddings
) -> LangChainVectorStoreFactory:
    """
    Creates an LangChain Vector Store Factory according to the vector store provider
    Args:
        setting: The vector store setting
        index_name: The index name
        embedding_function: The embedding function

    Returns:
        The LangChain Vector Store Factory, or raise an exception otherwise
    """

    logger.info('Get Vector Store Factory for the given provider')
    if setting is None:
        logger.debug('Vector Store Factory (based on env variables) - OpenSearchFactory')
        return OpenSearchFactory(
            setting=OpenSearchVectorStoreSetting(
                host=application_settings.open_search_host,
                port=application_settings.open_search_port,
                username=open_search_username,
                password=RawSecretKey(value=open_search_password),
            ),
            index_name=index_name,
            embedding_function=embedding_function
        )
    elif isinstance(setting, OpenSearchVectorStoreSetting):
        logger.debug('Vector Store Factory (based on RAG query) - OpenSearchFactory')
        return OpenSearchFactory(
            setting=setting,
            index_name=index_name,
            embedding_function=embedding_function
        )
    else:
        raise GenAIUnknownVectorStoreProviderSettingException()


def get_callback_handler_factory(setting: BaseObservabilitySetting) -> LangChainCallbackHandlerFactory:
    """
    Creates a Langchain Callback Handler Factory according to the given setting
    Args:
        setting: The Observability setting

    Returns:
        The Observability Factory, or raise an exception otherwise
    """

    logger.info('Get Observability Factory for the given setting')
    if isinstance(setting, LangfuseObservabilitySetting):
        logger.debug('Observability Factory - LangfuseCallbackHandlerFactory')
        return LangfuseCallbackHandlerFactory(setting=setting)
    else:
        raise GenAIUnknownObservabilityProviderSettingException()


def create_observability_callback_handler(
        observability_setting: Optional[ObservabilitySetting],
        trace_name: ObservabilityTrace) -> Optional[LangfuseCallbackHandler]:
    """
    Create the Observability Callback Handler

    Args:
        observability_setting: The Observability Settings
        trace_name: The trace name

    Returns:
        The Observability Callback Handler
    """
    if observability_setting is not None:
        return get_callback_handler_factory(setting=observability_setting).get_callback_handler(
            trace_name=trace_name.value)

    return None

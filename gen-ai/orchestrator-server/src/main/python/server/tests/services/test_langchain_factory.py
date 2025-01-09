#   Copyright (C) 2024-2025 Credit Mutuel Arkea
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

import pytest

from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIUnknownProviderSettingException,
)
from gen_ai_orchestrator.errors.exceptions.observability.observability_exceptions import (
    GenAIUnknownObservabilityProviderSettingException,
)
from gen_ai_orchestrator.errors.exceptions.vector_store.vector_store_exceptions import (
    GenAIUnknownVectorStoreProviderSettingException,
)
from gen_ai_orchestrator.models.contextual_compressor.compressor_provider import (
    ContextualCompressorProvider,
)
from gen_ai_orchestrator.models.em.azureopenai.azure_openai_em_setting import (
    AzureOpenAIEMSetting,
)
from gen_ai_orchestrator.models.em.bloomz.bloomz_em_setting import (
    BloomzEMSetting,
)
from gen_ai_orchestrator.models.em.em_provider import EMProvider
from gen_ai_orchestrator.models.em.openai.openai_em_setting import (
    OpenAIEMSetting,
)
from gen_ai_orchestrator.models.guardrail.guardrail_provider import (
    GuardrailProvider,
)
from gen_ai_orchestrator.models.llm.azureopenai.azure_openai_llm_setting import (
    AzureOpenAILLMSetting,
)
from gen_ai_orchestrator.models.llm.fake_llm.fake_llm_setting import (
    FakeLLMSetting,
)
from gen_ai_orchestrator.models.llm.llm_provider import LLMProvider
from gen_ai_orchestrator.models.llm.openai.openai_llm_setting import (
    OpenAILLMSetting,
)
from gen_ai_orchestrator.models.observability.langfuse.langfuse_setting import (
    LangfuseObservabilitySetting,
)
from gen_ai_orchestrator.models.observability.observability_provider import (
    ObservabilityProvider,
)
from gen_ai_orchestrator.models.observability.observability_type import (
    ObservabilitySetting,
)
from gen_ai_orchestrator.models.vector_stores.open_search.open_search_setting import (
    OpenSearchVectorStoreSetting,
)
from gen_ai_orchestrator.models.vector_stores.pgvector.pgvector_setting import (
    PGVectorStoreSetting,
)
from gen_ai_orchestrator.models.vector_stores.vectore_store_provider import (
    VectorStoreProvider,
)
from gen_ai_orchestrator.services.langchain.factories.callback_handlers.langfuse_callback_handler_factory import (
    LangfuseCallbackHandlerFactory,
)
from gen_ai_orchestrator.services.langchain.factories.contextual_compressor.bloomz_compressor_factory import (
    BloomzCompressorFactory,
    BloomzCompressorSetting,
)
from gen_ai_orchestrator.services.langchain.factories.em.azure_openai_em_factory import (
    AzureOpenAIEMFactory,
)
from gen_ai_orchestrator.services.langchain.factories.em.bloomz_em_factory import (
    BloomzEMFactory,
)
from gen_ai_orchestrator.services.langchain.factories.em.openai_em_factory import (
    OpenAIEMFactory,
)
from gen_ai_orchestrator.services.langchain.factories.guardrail.bloomz_guardrail_factory import (
    BloomzGuardrailFactory,
    BloomzGuardrailSetting,
)
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    get_callback_handler_factory,
    get_compressor_factory,
    get_em_factory,
    get_guardrail_factory,
    get_llm_factory,
    get_vector_store_factory,
)
from gen_ai_orchestrator.services.langchain.factories.llm.azure_openai_llm_factory import (
    AzureOpenAILLMFactory,
)
from gen_ai_orchestrator.services.langchain.factories.llm.fake_llm_factory import (
    FakeLLMFactory,
)
from gen_ai_orchestrator.services.langchain.factories.llm.openai_llm_factory import (
    OpenAILLMFactory,
)
from gen_ai_orchestrator.services.langchain.factories.vector_stores.open_search_factory import (
    OpenSearchFactory,
)
from gen_ai_orchestrator.services.langchain.factories.vector_stores.pgvector_factory import (
    PGVectorFactory,
)


def test_get_unknown_llm_factory():
    with pytest.raises(GenAIUnknownProviderSettingException):
        get_llm_factory(setting='settings with incorrect type')


def test_get_open_ai_llm_factory():
    open_ai = get_llm_factory(
        setting=OpenAILLMSetting(
            **{
                'provider': 'OpenAI',
                'api_key': {
                    'type': 'Raw',
                    'secret': 'ab7***************************A1IV4B',
                },
                'model': 'model',
                'temperature': '0',
                'prompt': 'List 3 ice cream flavors.',
            }
        )
    )
    assert open_ai.setting.provider == LLMProvider.OPEN_AI
    assert isinstance(open_ai, OpenAILLMFactory)


def test_get_azure_open_ai_llm_factory():
    azure_open_ai = get_llm_factory(
        setting=AzureOpenAILLMSetting(
            **{
                'provider': 'AzureOpenAIService',
                'api_key': {
                    'type': 'Raw',
                    'secret': 'ab7***************************A1IV4B',
                },
                'deployment_name': 'deployment_name',
                'model': 'gpt-4o',
                'api_base': 'https://doc.tock.ai/tock',
                'api_version': 'version',
                'temperature': '0',
                'prompt': 'List 3 ice cream flavors.',
            }
        )
    )
    assert azure_open_ai.setting.provider == LLMProvider.AZURE_OPEN_AI_SERVICE
    assert isinstance(azure_open_ai, AzureOpenAILLMFactory)


def test_get_fake_llm_factory():
    fake_llm = get_llm_factory(
        setting=FakeLLMSetting(
            **{
                'provider': 'FakeLLM',
                'api_key': {
                    'type': 'Raw',
                    'secret': 'ab7***************************A1IV4B',
                },
                'temperature': '0',
                'prompt': 'List 3 ice cream flavors.',
                'responses': ['1. vanilla\n2. chocolate\n3. strawberry'],
            }
        )
    )
    assert fake_llm.setting.provider == LLMProvider.FAKE_LLM
    assert isinstance(fake_llm, FakeLLMFactory)


def test_get_unknown_em_factory():
    with pytest.raises(GenAIUnknownProviderSettingException):
        get_em_factory(setting='settings with incorrect type')


def test_get_open_ai_em_factory():
    open_ai = get_em_factory(
        setting=OpenAIEMSetting(
            **{
                'provider': 'OpenAI',
                'api_key': {
                    'type': 'Raw',
                    'secret': 'ab7***************************A1IV4B',
                },
                'model': 'model',
                'prompt': 'List 3 ice cream flavors.',
            }
        )
    )
    assert open_ai.setting.provider == EMProvider.OPEN_AI
    assert isinstance(open_ai, OpenAIEMFactory)


def test_get_azure_open_ai_em_factory():
    azure_open_ai = get_em_factory(
        setting=AzureOpenAIEMSetting(
            **{
                'provider': 'AzureOpenAIService',
                'api_key': {
                    'type': 'Raw',
                    'secret': 'ab7***************************A1IV4B',
                },
                'deployment_name': 'deployment_name',
                'model': 'text-embedding-ada-002',
                'api_base': 'https://doc.tock.ai/tock',
                'api_version': 'version',
                'prompt': 'List 3 ice cream flavors.',
            }
        )
    )
    assert azure_open_ai.setting.provider == EMProvider.AZURE_OPEN_AI_SERVICE
    assert isinstance(azure_open_ai, AzureOpenAIEMFactory)


def test_get_bloomz_em_factory():
    bloomz = get_em_factory(
        setting=BloomzEMSetting(
            **{
                'provider': 'Bloomz',
                'api_base': 'https://exemple.apibase',
                'pooling': 'last',
            }
        )
    )
    assert bloomz.setting.provider == EMProvider.BLOOMZ
    assert isinstance(bloomz, BloomzEMFactory)


def test_get_open_search_vector_store_factory():
    em_factory = get_em_factory(
        setting=OpenAIEMSetting(
            **{
                'provider': 'OpenAI',
                'api_key': {
                    'type': 'Raw',
                    'secret': 'ab7***************************A1IV4B',
                },
                'model': 'model',
                'prompt': 'List 3 ice cream flavors.',
            }
        )
    )
    open_search = get_vector_store_factory(
        setting=OpenSearchVectorStoreSetting(
            **{
                'provider': 'OpenSearch',
                'host': 'localhost',
                'port': 9200,
                'password': {
                    'type': 'Raw',
                    'secret': 'ab7***************************A1IV4B',
                },
                'username': 'admin',
            }
        ),
        index_name='my-index-name',
        embedding_function=em_factory.get_embedding_model(),
    )
    pgvector = get_vector_store_factory(
        setting=PGVectorStoreSetting(
            **{
                'provider': 'PGVector',
                'host': 'localhost',
                'port': 5432,
                'password': {
                    'type': 'Raw',
                    'secret': 'ab7***************************A1IV4B',
                },
                'username': 'postgres',
                'database': 'postgres',
            }
        ),
        index_name='my-index-name',
        embedding_function=em_factory.get_embedding_model(),
    )
    assert isinstance(open_search, OpenSearchFactory)
    assert isinstance(pgvector, PGVectorFactory)


def test_get_unknown_vector_store_factory():
    with pytest.raises(GenAIUnknownVectorStoreProviderSettingException):
        get_vector_store_factory(
            setting='an incorrect vector store provider',
            index_name=None,
            embedding_function=None,
        )


def test_get_unknown_observability_factory():
    with pytest.raises(GenAIUnknownObservabilityProviderSettingException):
        get_callback_handler_factory(setting='settings with incorrect type')


def test_get_langfuse_observability_factory():
    langfuse_factory = get_callback_handler_factory(
        setting=ObservabilitySetting(
            **{
                'provider': 'Langfuse',
                'secret_key': {
                    'type': 'Raw',
                    'secret': 'ab7***************************A1IV4B',
                },
                'public_key': 'df41*********f',
                'url': 'https://myServer:3000',
            }
        )
    )
    assert langfuse_factory.setting.provider == ObservabilityProvider.LANGFUSE
    assert isinstance(langfuse_factory, LangfuseCallbackHandlerFactory)


def test_get_bloomz_guardrail_factory():
    guardrail = get_guardrail_factory(
        setting=BloomzGuardrailSetting(
            provider='BloomzGuardrail', api_base='http://guardrail.com', max_score=0.6
        )
    )

    assert guardrail.setting.provider == GuardrailProvider.BLOOMZ
    assert isinstance(guardrail, BloomzGuardrailFactory)


def test_get_bloomz_compressor_factory():
    compressor = get_compressor_factory(
        setting=BloomzCompressorSetting(
            provider='BloomzRerank',
            min_score=0.5,
            endpoint='http://compressor.com',
            max_documents=25,
            label='label',
        )
    )

    assert compressor.setting.provider == ContextualCompressorProvider.BLOOMZ
    assert isinstance(compressor, BloomzCompressorFactory)

#   Copyright (C) 2023 Credit Mutuel Arkea
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
from llm_orchestrator.errors.exceptions.exceptions import BusinessException
from llm_orchestrator.models.em.azureopenai.azure_openai_em_setting import (
    AzureOpenAIEMSetting,
)
from llm_orchestrator.models.em.em_setting import BaseEMSetting
from llm_orchestrator.models.em.openai.openai_em_setting import OpenAIEMSetting
from llm_orchestrator.models.errors.errors_models import ErrorCode
from llm_orchestrator.models.llm.azureopenai.azure_openai_llm_setting import (
    AzureOpenAILLMSetting,
)
from llm_orchestrator.models.llm.llm_setting import BaseLLMSetting
from llm_orchestrator.models.llm.openai.openai_llm_setting import (
    OpenAILLMSetting,
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


def get_llm_factory(setting: BaseLLMSetting) -> LangChainLLMFactory:
    if isinstance(setting, OpenAILLMSetting):
        return OpenAILLMFactory(setting=setting)
    elif isinstance(setting, AzureOpenAILLMSetting):
        return AzureOpenAILLMFactory(setting=setting)
    else:
        raise BusinessException(ErrorCode.E10)


def get_em_factory(setting: BaseEMSetting) -> LangChainEMFactory:
    if isinstance(setting, OpenAIEMSetting):
        return OpenAIEMFactory(setting=setting)
    elif isinstance(setting, AzureOpenAIEMSetting):
        return AzureOpenAIEMFactory(setting=setting)
    else:
        raise BusinessException(ErrorCode.E10)

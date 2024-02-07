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
"""Module defining generic type alias"""

from typing import Annotated, Union

from fastapi import Body

from gen_ai_orchestrator.models.em.azureopenai.azure_openai_em_setting import (
    AzureOpenAIEMSetting,
)
from gen_ai_orchestrator.models.em.openai.openai_em_setting import OpenAIEMSetting
from gen_ai_orchestrator.models.llm.azureopenai.azure_openai_llm_setting import (
    AzureOpenAILLMSetting,
)
from gen_ai_orchestrator.models.llm.fake_llm.fake_llm_setting import (
    FakeLLMSetting,
)
from gen_ai_orchestrator.models.llm.openai.openai_llm_setting import (
    OpenAILLMSetting,
)
from gen_ai_orchestrator.models.vector_stores.open_search.open_search_params import (
    OpenSearchParams,
)

LLMSetting = Annotated[
    Union[OpenAILLMSetting, AzureOpenAILLMSetting, FakeLLMSetting],
    Body(discriminator='provider'),
]

EMSetting = Annotated[
    Union[OpenAIEMSetting, AzureOpenAIEMSetting], Body(discriminator='provider')
]

DocumentSearchParams = Annotated[
    Union[OpenSearchParams], Body(discriminator='provider')
]

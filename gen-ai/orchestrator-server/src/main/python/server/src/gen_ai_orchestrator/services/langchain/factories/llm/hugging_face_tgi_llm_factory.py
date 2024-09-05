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
"""Model for creating HuggingFaceTGILLMFactory"""

from typing import Optional

from langchain.base_language import BaseLanguageModel
from langchain_core.runnables import RunnableConfig
from langchain_core.runnables.utils import Input, Output
from langchain_huggingface import HuggingFaceEndpoint

from gen_ai_orchestrator.errors.handlers.huggingfacetgi.hugging_face_exception_handler import (
    hugging_face_exception_handler,
)
from gen_ai_orchestrator.models.llm.huggingfacetgi.hugging_face_tgi_llm_setting import (
    HuggingFaceTGILLMSetting,
)
from gen_ai_orchestrator.services.langchain.factories.llm.llm_factory import (
    LangChainLLMFactory,
)


class HuggingFaceTGILLMFactory(LangChainLLMFactory):
    """A class for LangChain Hugging Face LLM Factory"""

    setting: HuggingFaceTGILLMSetting

    def get_language_model(self) -> BaseLanguageModel:
        return HuggingFaceEndpoint(
            endpoint_url=self.setting.api_base,
            temperature=self.setting.temperature,
            repetition_penalty=self.setting.repetition_penalty,
            max_new_tokens=self.setting.max_new_tokens,
            server_kwargs={'trust_env': True},
        )

    @hugging_face_exception_handler(provider='HuggingFaceTGI')
    async def invoke(
        self, _input: Input, config: Optional[RunnableConfig] = None
    ) -> Output:
        return await super().invoke(_input, config)

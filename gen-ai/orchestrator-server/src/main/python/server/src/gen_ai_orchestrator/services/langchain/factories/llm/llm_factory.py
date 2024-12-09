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
"""Module for the LangChain Large Language Model Factory"""

import logging
from abc import ABC, abstractmethod
from typing import Optional

from langchain.base_language import BaseLanguageModel
from langchain.callbacks.base import BaseCallbackHandler as LangchainBaseCallbackHandler
from langchain_core.rate_limiters import BaseRateLimiter, InMemoryRateLimiter
from langchain_core.runnables import RunnableConfig
from langchain_core.runnables.utils import Input, Output
from pydantic import BaseModel

from gen_ai_orchestrator.configurations.environment.settings import application_settings
from gen_ai_orchestrator.models.llm.llm_setting import BaseLLMSetting

logger = logging.getLogger(__name__)


class LangChainLLMFactory(ABC, BaseModel):
    """A base class for LangChain Large Language Model Factory"""

    setting: BaseLLMSetting

    @abstractmethod
    def get_language_model(self) -> BaseLanguageModel:
        """
        Fabric the language model to call.
        :return: BaseLanguageModel the interface for Language models.
        """
        pass

    async def check_llm_setting(
            self, observability_callback_handler: Optional[LangchainBaseCallbackHandler] = None) -> bool:
        """
        check the LLM setting validity, by pinging the AI Provider API
        Args:
            observability_callback_handler : The Observability Callback Handler

        Returns:
            True if the setting is valid.

        Raises:
            BusinessException: For incorrect setting
        """
        logger.info('Invoke LLM provider to check setting')
        query = 'Hi, are you there?'
        response = await self.invoke(query, config={
            "callbacks": [observability_callback_handler] if observability_callback_handler else []
        })
        logger.info('Invocation successful')
        logger.debug('[query: %s], [response: %s]', query, response)
        return True

    async def invoke(self, _input: Input, config: Optional[RunnableConfig] = None) -> Output:
        """
        This is a delegate method that performs the llm invoke.

        Args:
            _input: The input to the runnable.
            config: A config to use when invoking the runnable.

        Returns:
            The output of the runnable.
        """
        return await self.get_language_model().ainvoke(_input, config)


rate_limiter = InMemoryRateLimiter(
    # We can only make a request once every 10 seconds!!
    requests_per_second=0.1,
    # Wake up every 100 ms to check whether allowed to make a request,
    check_every_n_seconds=0.1,
    # Controls the maximum burst size.
    max_bucket_size=10,
)
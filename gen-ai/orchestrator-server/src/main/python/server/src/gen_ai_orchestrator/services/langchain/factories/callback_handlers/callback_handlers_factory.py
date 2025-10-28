#   Copyright (C) 2023-2025 Credit Mutuel Arkea
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
"""Module for the LangChain Callback Handler Factory"""

import logging
from abc import ABC, abstractmethod
from typing import Any

from langfuse.callback.langchain import LangchainCallbackHandler
from pydantic import BaseModel

from gen_ai_orchestrator.models.observability.observability_setting import (
    BaseObservabilitySetting,
)

logger = logging.getLogger(__name__)


class LangChainCallbackHandlerFactory(ABC, BaseModel):
    """A base class for LangChain Callback Handler Factory"""

    setting: BaseObservabilitySetting

    @abstractmethod
    def get_callback_handler(self, **kwargs: Any) -> LangchainCallbackHandler:
        """
        Fabric a callback handler.
        :return: LangchainCallbackHandler.
        """
        pass

    @abstractmethod
    def check_observability_setting(self) -> bool:
        """
        check the Callback Handler setting validity
        :return: True if the setting is valid.
        :raises BusinessException: For incorrect setting
        """
        pass



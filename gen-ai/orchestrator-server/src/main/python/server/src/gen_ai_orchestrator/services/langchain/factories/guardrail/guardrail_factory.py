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
"""Module for the Guardrail Factory"""

from abc import ABC, abstractmethod

from langchain_core.output_parsers import BaseOutputParser
from pydantic import BaseModel

from gen_ai_orchestrator.models.guardrail.guardrail_setting import (
    BaseGuardrailSetting,
)


class GuardrailFactory(ABC, BaseModel):
    """A base class for Guardrail Factory"""

    setting: BaseGuardrailSetting

    @abstractmethod
    def get_parser(self) -> BaseOutputParser:
        pass

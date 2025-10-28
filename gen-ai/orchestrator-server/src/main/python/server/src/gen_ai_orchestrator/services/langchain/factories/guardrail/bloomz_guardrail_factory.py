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
"""Model for creating BloomzGuardrailFactory"""

from langchain_core.output_parsers import BaseOutputParser

from gen_ai_orchestrator.models.guardrail.bloomz.bloomz_guardrail_setting import (
    BloomzGuardrailSetting,
)
from gen_ai_orchestrator.services.langchain.factories.guardrail.guardrail_factory import (
    GuardrailFactory,
)
from gen_ai_orchestrator.services.langchain.impls.guardrail.bloomz_guardrail import (
    BloomzGuardrailOutputParser,
)


class BloomzGuardrailFactory(GuardrailFactory):
    """A class for Bloomz Guardrail Factory"""

    setting: BloomzGuardrailSetting

    def get_parser(self) -> BaseOutputParser:
        return BloomzGuardrailOutputParser(
            max_score=self.setting.max_score, endpoint=self.setting.api_base
        )

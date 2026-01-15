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
""""
Observability Exception Module
List of all Observability exceptions managed by Gen AI Orchestrator
"""
from typing import Optional

from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIOrchestratorException,
)
from gen_ai_orchestrator.models.errors.errors_models import (
    ErrorCode,
    ErrorInfo,
)


class GenAIUnknownObservabilityProviderException(GenAIOrchestratorException):
    """Unknown Observability Provider"""

    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.OBSERVABILITY_UNKNOWN_PROVIDER, info)


class GenAIUnknownObservabilityProviderSettingException(GenAIOrchestratorException):
    """Unknown Observability Provider Setting"""

    def __init__(self, info: Optional[ErrorInfo] = None):
        super().__init__(ErrorCode.OBSERVABILITY_UNKNOWN_PROVIDER_SETTING, info)


class GenAIObservabilityErrorException(GenAIOrchestratorException):
    """Observability returns a non-OK (>=400) HTTP status code."""

    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.OBSERVABILITY_API_ERROR, info)


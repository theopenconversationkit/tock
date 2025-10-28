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
"""Module of the Langfuse exception handlers"""

import logging

from langfuse.api.core import ApiError

from gen_ai_orchestrator.models.errors.errors_models import ErrorInfo
from gen_ai_orchestrator.models.observability.observability_provider import (
    ObservabilityProvider,
)

logger = logging.getLogger(__name__)


def create_error_info_langfuse(
    exc: ApiError
) -> ErrorInfo:
    """
    Create ErrorInfo for a Langfuse error

    Args:
        exc: the Langfuse Api error
    Returns:
        The ErrorInfo with the Langfuse Api error parameters
    """

    return ErrorInfo(
        provider=ObservabilityProvider.LANGFUSE.value,
        error=exc.__class__.__name__,
        cause=str(exc),
    )

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
"""Module of the Ollama handlers"""

import logging

from langchain_community.llms.ollama import OllamaEndpointNotFoundError

from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIAuthenticationException,
    GenAIConnectionErrorException,
)
from gen_ai_orchestrator.models.errors.errors_models import ErrorInfo

logger = logging.getLogger(__name__)


def ollama_exception_handler(provider: str):
    """
    Managing Ollama exceptions

    Args:
        provider: The AI Provider type
    """

    def decorator(func):
        """A decorator of handler function"""

        async def wrapper(*args, **kwargs):
            """Exception handling logic"""

            try:
                return await func(*args, **kwargs)
            except OllamaEndpointNotFoundError as exc:
                logger.error(exc)
                raise GenAIConnectionErrorException(
                    create_error_info_ollama(exc, provider)
                )

        return wrapper

    return decorator


def create_error_info_ollama(exc: OllamaEndpointNotFoundError, provider: str) -> ErrorInfo:
    """
    Create ErrorInfo for an Ollama error

    Args:
        exc: the Ollama error
        provider: the AI provider type
    Returns:
        The ErrorInfo with the Ollama error parameters
    """
    return ErrorInfo(
        provider=provider, error=exc.__class__.__name__, cause=str(exc)
    )

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
"""
AI Provider Exception Module
List of all AI Provider exceptions managed by Gen AI Orchestrator
"""

from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIOrchestratorException,
)
from gen_ai_orchestrator.models.errors.errors_models import (
    ErrorCode,
    ErrorInfo,
)


class AIProviderBadRequestException(GenAIOrchestratorException):
    """
    An exception indicating that a request cannot be executed because it is invalid.
    """

    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.AI_PROVIDER_BAD_REQUEST, info)


class AIProviderAPIErrorException(GenAIOrchestratorException):
    """
    Unknown AI Provider error.
    """

    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.AI_PROVIDER_API_ERROR, info)


class AIProviderAPIResourceNotFoundException(GenAIOrchestratorException):
    """
    API key or token was invalid, expired, or revoked.
    """

    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.AI_PROVIDER_API_RESOURCE_NOT_FOUND, info)


class AIProviderAPIModelException(GenAIOrchestratorException):
    """
    Unknown AI Provider model.
    """

    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.AI_PROVIDER_API_MODEL_NOT_FOUND, info)


class AIProviderAPIDeploymentNotFoundException(GenAIOrchestratorException):
    """
    Unknown AI Provider deployment.
    """

    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.AI_PROVIDER_API_DEPLOYMENT_NOT_FOUND, info)


class AIProviderAPIBadRequestException(GenAIOrchestratorException):
    """
    Bad request.
    """

    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.AI_PROVIDER_API_BAD_REQUEST, info)


class AIProviderAPIContextLengthExceededException(GenAIOrchestratorException):
    """
    The model's context length has been exceeded.
    """

    def __init__(self, info: ErrorInfo):
        super().__init__(
            ErrorCode.AI_PROVIDER_API_CONTEXT_LENGTH_EXCEEDED_BAD_REQUEST, info
        )

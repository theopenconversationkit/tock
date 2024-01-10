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
from typing import Optional

from llm_orchestrator.models.errors.errors_models import (
    ErrorCode,
    ErrorInfo,
    ErrorMessages,
)


class GenAIOrchestratorException(Exception):
    def __init__(self, error_code: ErrorCode, info: Optional[ErrorInfo] = None):
        error_message = ErrorMessages().get_message(error_code)
        self.message = error_message.message
        self.detail = error_message.detail
        self.error_code = error_code
        self.info = info


class GenAIConnectionErrorException(GenAIOrchestratorException):
    """
    Connection error. due to
    - Network connectivity or proxy
    - Request timed out
    - SSL errors
    """

    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.GEN_AI_CONNECTION_ERROR, info)


class GenAIUnknownErrorException(GenAIOrchestratorException):
    """Unknown error"""

    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.GEN_AI_UNKNOWN_ERROR, info)


class GenAIAuthenticationException(GenAIOrchestratorException):
    """
    API key or token was invalid, expired, or revoked.
    """

    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.GEN_AI_AUTHENTICATION_ERROR, info)


class GenAIUnknownProviderException(GenAIOrchestratorException):
    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.AI_PROVIDER_UNKNOWN, info)


class GenAIUnknownProviderSettingException(GenAIOrchestratorException):
    def __init__(self, info: Optional[ErrorInfo] = None):
        super().__init__(ErrorCode.GEN_AI_UNKNOWN_PROVIDER_SETTING, info)


class VectorStoreUnknownException(GenAIOrchestratorException):
    def __init__(self):
        super().__init__(ErrorCode.VECTOR_STORE_UNKNOWN)

#   Copyright (C) 2023 Credit Mutuel Arkea
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

from llm_orchestrator.models.errors.errors_models import (
    ErrorCode,
    ErrorInfo,
    ErrorMessages,
)


class BusinessException(Exception):
    def __init__(self, error_code: ErrorCode, info: ErrorInfo):
        error_message = ErrorMessages().get_message(error_code)
        self.message = error_message.message
        self.detail = error_message.detail
        self.error_code = error_code
        self.info = info


class UnknownProviderException(BusinessException):
    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.PROVIDER_NOT_FOUND, info)


class InvalidQueryException(BusinessException):
    """
    An exception indicating that a query cannot be executed because it is invalid.
    """

    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.PROVIDER_BAD_QUERY, info)


class ProviderAPIErrorException(BusinessException):
    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.PROVIDER_API_ERROR, info)


class ProviderAPIConnectionException(BusinessException):
    """
    Two cases handled :
    - Connection error. Example: Internet or proxy connectivity / Azure endpoint non existent
    - Request timed out
    """

    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.PROVIDER_API_CONNECTION_ERROR, info)


class ProviderAPIAuthenticationException(BusinessException):
    """
    API key or token was invalid, expired, or revoked.
    """

    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.PROVIDER_API_AUTHENTICATION_ERROR, info)


class ProviderAPIBadRequestException(BusinessException):
    """
    Bad request.
    """

    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.PROVIDER_API_BAD_REQUEST, info)


class ProviderAPIContextLengthExceededException(BusinessException):
    """
    The model's context length has been exceeded.
    """

    def __init__(self, info: ErrorInfo):
        super().__init__(
            ErrorCode.PROVIDER_API_CONTEXT_LENGTH_EXCEEDED_BAD_REQUEST, info
        )


class ProviderAPIResourceNotFoundException(BusinessException):
    """
    API key or token was invalid, expired, or revoked.
    """

    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.PROVIDER_API_RESOURCE_NOT_FOUND, info)


class ProviderAPIModelException(BusinessException):
    """
    Unknown AI provider model.
    """

    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.PROVIDER_API_MODEL_NOT_FOUND, info)


class ProviderAPIDeploymentException(BusinessException):
    """
    Unknown AI provider deployment.
    """

    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.PROVIDER_API_DEPLOYMENT_NOT_FOUND, info)

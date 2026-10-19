#   Copyright (C) 2025-2026 Credit Mutuel Arkea
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
"""Module of the AWS Bedrock handlers"""

import inspect
import logging

from botocore.exceptions import (
    ClientError,
    EndpointConnectionError,
    NoCredentialsError,
)

from gen_ai_orchestrator.errors.exceptions.ai_provider.ai_provider_exceptions import (
    AIProviderAPIBadRequestException,
    AIProviderAPIErrorException,
    AIProviderAPIModelException,
)
from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIAuthenticationException,
    GenAIConnectionErrorException,
)
from gen_ai_orchestrator.models.errors.errors_models import ErrorInfo

logger = logging.getLogger(__name__)

# AWS error codes considered as authentication issues
_AUTHENTICATION_ERROR_CODES = {
    'AccessDeniedException',
    'UnrecognizedClientException',
    'InvalidSignatureException',
    'ExpiredTokenException',
}
# AWS error codes indicating an unknown/not-accessible model
_MODEL_NOT_FOUND_ERROR_CODES = {
    'ResourceNotFoundException',
}
# AWS error codes indicating a bad request
_BAD_REQUEST_ERROR_CODES = {
    'ValidationException',
}


def aws_bedrock_exception_handler(provider: str):
    """
    Managing AWS Bedrock exceptions

    Args:
        provider: The AI Provider type
    """

    def decorator(func):
        """A decorator of handler function"""

        if inspect.isasyncgenfunction(func):

            async def wrapper(*args, **kwargs):
                """Exception handling logic for async generators"""
                try:
                    async for item in func(*args, **kwargs):
                        yield item
                except (
                    NoCredentialsError,
                    EndpointConnectionError,
                    ClientError,
                ) as exc:
                    _translate_aws_bedrock_error(exc, provider)

            return wrapper

        async def wrapper(*args, **kwargs):
            """Exception handling logic"""

            try:
                return await func(*args, **kwargs)
            except (
                NoCredentialsError,
                EndpointConnectionError,
                ClientError,
            ) as exc:
                _translate_aws_bedrock_error(exc, provider)

        return wrapper

    return decorator


def _translate_aws_bedrock_error(exc: Exception, provider: str):
    """Map an AWS Bedrock error to the matching GenAI orchestrator exception."""
    logger.error(exc)
    if isinstance(exc, NoCredentialsError):
        raise GenAIAuthenticationException(
            create_error_info_aws_bedrock(exc, provider)
        )
    if isinstance(exc, EndpointConnectionError):
        raise GenAIConnectionErrorException(
            create_error_info_aws_bedrock(exc, provider)
        )
    if isinstance(exc, ClientError):
        _manage_client_error(exc, provider)
    raise exc


def create_error_info_aws_bedrock(exc: Exception, provider: str) -> ErrorInfo:
    """
    Create ErrorInfo for an AWS Bedrock error

    Args:
        exc: the AWS Bedrock error
        provider: the AI provider type
    Returns:
        The ErrorInfo with the AWS Bedrock error parameters
    """
    return ErrorInfo(provider=provider, error=exc.__class__.__name__, cause=str(exc))


def _manage_client_error(exc: ClientError, provider: str):
    """
    Manage a boto3/botocore ClientError raised by the Bedrock API

    Args:
        exc: the AWS Bedrock client error
        provider: the AI provider type
    Returns:
        Raise a specific Gen AI Orchestrator exception according to the AWS error code
    """

    error_code = exc.response.get('Error', {}).get('Code')
    if error_code in _AUTHENTICATION_ERROR_CODES:
        raise GenAIAuthenticationException(create_error_info_aws_bedrock(exc, provider))
    elif error_code in _MODEL_NOT_FOUND_ERROR_CODES:
        raise AIProviderAPIModelException(create_error_info_aws_bedrock(exc, provider))
    elif error_code in _BAD_REQUEST_ERROR_CODES:
        raise AIProviderAPIBadRequestException(
            create_error_info_aws_bedrock(exc, provider)
        )
    else:
        raise AIProviderAPIErrorException(create_error_info_aws_bedrock(exc, provider))

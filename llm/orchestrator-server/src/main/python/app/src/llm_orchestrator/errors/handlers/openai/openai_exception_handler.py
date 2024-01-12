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

import logging

from openai import (
    APIConnectionError,
    APIError,
    AuthenticationError,
    BadRequestError,
    NotFoundError,
    OpenAIError,
)

from llm_orchestrator.errors.exceptions.ai_provider.ai_provider_exceptions import (
    AIProviderAPIBadRequestException,
    AIProviderAPIContextLengthExceededException,
    AIProviderAPIDeploymentNotFoundException,
    AIProviderAPIErrorException,
    AIProviderAPIModelException,
    AIProviderAPIResourceNotFoundException,
)
from llm_orchestrator.errors.exceptions.exceptions import (
    GenAIAuthenticationException,
    GenAIConnectionErrorException,
)
from llm_orchestrator.models.errors.errors_models import ErrorInfo

logger = logging.getLogger(__name__)


def openai_exception_handler(provider: str):
    def decorator(func):
        def wrapper(*args, **kwargs):
            try:
                return func(*args, **kwargs)
            except APIConnectionError as exc:
                logger.error(exc)
                raise GenAIConnectionErrorException(
                    create_error_info_openai(exc, provider)
                )
            except AuthenticationError as exc:
                logger.error(exc)
                raise GenAIAuthenticationException(
                    create_error_info_openai(exc, provider)
                )
            except NotFoundError as exc:
                logger.error(exc)
                if 'model_not_found' == exc.code:
                    raise AIProviderAPIModelException(
                        create_error_info_openai(exc, provider)
                    )
                elif 'DeploymentNotFound' == exc.code:
                    raise AIProviderAPIDeploymentNotFoundException(
                        create_error_info_openai(exc, provider)
                    )
                else:
                    raise AIProviderAPIResourceNotFoundException(
                        create_error_info_openai(exc, provider)
                    )
            except BadRequestError as exc:
                logger.error(exc)
                if 'context_length_exceeded' == exc.code:
                    raise AIProviderAPIContextLengthExceededException(
                        create_error_info_openai(exc, provider)
                    )
                else:
                    raise AIProviderAPIBadRequestException(
                        create_error_info_openai(exc, provider)
                    )
            except APIError as exc:
                logger.error(exc)
                raise AIProviderAPIErrorException(
                    create_error_info_openai(exc, provider)
                )

        return wrapper

    return decorator


def create_error_info_openai(exc: OpenAIError, provider: str) -> ErrorInfo:
    if isinstance(exc, APIError):
        return ErrorInfo(
            provider=provider,
            error=exc.__class__.__name__,
            cause=exc.message,
            request=f'[{exc.request.method}] {exc.request.url}',
        )
    else:
        return ErrorInfo(
            provider=provider, error=exc.__class__.__name__, cause=str(exc)
        )

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

import logging

from openai import (
    APIConnectionError,
    APIError,
    AuthenticationError,
    BadRequestError,
    NotFoundError,
)

from llm_orchestrator.errors.exceptions.exceptions import (
    ProviderAPIAuthenticationException,
    ProviderAPIBadRequestException,
    ProviderAPIConnectionException,
    ProviderAPIContextLengthExceededException,
    ProviderAPIDeploymentException,
    ProviderAPIErrorException,
    ProviderAPIModelException,
    ProviderAPIResourceNotFoundException,
)
from llm_orchestrator.models.errors.errors_models import ErrorInfo

logger = logging.getLogger(__name__)


def factory_openai_exception_handler(func):
    def wrapper(self, *args, **kwargs):
        try:
            return func(self, *args, **kwargs)
        except APIConnectionError as exc:
            logger.error(exc)
            raise ProviderAPIConnectionException(
                create_error_info_openai(exc, self.setting.provider)
            )
        except AuthenticationError as exc:
            logger.error(exc)
            raise ProviderAPIAuthenticationException(
                create_error_info_openai(exc, self.setting.provider)
            )
        except NotFoundError as exc:
            logger.error(exc)
            if 'model_not_found' == exc.code:
                raise ProviderAPIModelException(
                    create_error_info_openai(exc, self.setting.provider)
                )
            elif 'DeploymentNotFound' == exc.code:
                raise ProviderAPIDeploymentException(
                    create_error_info_openai(exc, self.setting.provider)
                )
            else:
                raise ProviderAPIResourceNotFoundException(
                    create_error_info_openai(exc, self.setting.provider)
                )
        except BadRequestError as exc:
            logger.error(exc)
            if 'context_length_exceeded' == exc.code:
                raise ProviderAPIContextLengthExceededException(
                    create_error_info_openai(exc, self.setting.provider)
                )
            else:
                raise ProviderAPIBadRequestException(
                    create_error_info_openai(exc, self.setting.provider)
                )
        except APIError as exc:
            logger.error(exc)
            raise ProviderAPIErrorException(
                create_error_info_openai(exc, self.setting.provider)
            )

    return wrapper


def create_error_info_openai(exc: APIError, provider: str) -> ErrorInfo:
    return ErrorInfo(
        provider=provider,
        error=exc.__class__.__name__,
        cause=exc.message,
        request=f'[{exc.request.method}] {exc.request.url}',
    )

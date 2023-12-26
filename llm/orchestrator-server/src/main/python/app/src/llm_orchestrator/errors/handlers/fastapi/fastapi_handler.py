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

from fastapi import Request, status
from fastapi.encoders import jsonable_encoder
from fastapi.responses import JSONResponse

from llm_orchestrator.errors.exceptions.exceptions import BusinessException
from llm_orchestrator.models.errors.errors_models import ErrorInfo
from llm_orchestrator.routers.responses.responses import ErrorResponse

logger = logging.getLogger(__name__)


def business_exception_handler(_, exc: BusinessException) -> JSONResponse:
    return JSONResponse(
        status_code=status.HTTP_400_BAD_REQUEST,
        content=jsonable_encoder(
            ErrorResponse(
                code=exc.error_code.value,
                message=exc.message,
                detail=exc.detail,
                info=exc.info,
            )
        ),
    )


def create_error_info_not_found(
    request: Request, provider: str, accepted_values: list[str]
) -> ErrorInfo:
    return ErrorInfo(
        provider=provider,
        error='NotFoundError',
        cause=f'Accepted values are : {accepted_values}',
        request=f'[{request.method}] {request.url}',
    )


def create_error_info_bad_request(
    request: Request,
    provider: str,
    cause: str = 'The provider ID given for setting is not correct.',
) -> ErrorInfo:
    return ErrorInfo(
        provider=provider,
        error='BadRequestError',
        cause=cause,
        request=f'[{request.method}] {request.url}',
    )


def create_error_response(exc: BusinessException) -> ErrorResponse:
    return ErrorResponse(
        code=exc.error_code.value,
        message=exc.message,
        detail=exc.detail,
        info=exc.info,
    )

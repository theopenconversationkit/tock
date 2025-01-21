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
"""Module of the FastAPI handlers"""

from fastapi import Request, status
from fastapi.encoders import jsonable_encoder
from fastapi.responses import JSONResponse

from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIOrchestratorException,
    GenAIUnknownErrorException,
)
from gen_ai_orchestrator.models.errors.errors_models import ErrorInfo
from gen_ai_orchestrator.routers.responses.responses import ErrorResponse


def business_exception_handler(_, exc: GenAIOrchestratorException) -> JSONResponse:
    """Business exception handler. It manages a Gen AI Orchestrator exception"""

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


def generic_exception_handler(_, exc: Exception):
    """Generic exception handler. It manages all exceptions"""

    return business_exception_handler(
        _,
        GenAIUnknownErrorException(
            ErrorInfo(
                error=exc.__class__.__name__,
                cause=str(exc),
            )
        ),
    )


def create_error_info_not_found(
    http_request: Request, provider: str, accepted_values: list[str]
) -> ErrorInfo:
    """
    Create ErrorInfo for a not found error

    Args:
        http_request: the http request
        provider: the AI provider type
        accepted_values: the accepted values (expected)
    Returns:
        The ErrorInfo with all given parameters
    """

    return ErrorInfo(
        provider=provider,
        error='NotFoundError',
        cause=f'Accepted values are : {accepted_values}',
        request=f'[{http_request.method}] {http_request.url}',
    )


def create_error_info_bad_request(
    http_request: Request,
    provider: str,
    cause: str = 'The AI provider ID given for setting is not correct.',
) -> ErrorInfo:
    """
    Create ErrorInfo for a bad request error

    Args:
        http_request: the http request
        provider: the AI provider type
        cause: the error cause
    Returns:
        The ErrorInfo with all given parameters
    """

    return ErrorInfo(
        provider=provider,
        error='BadRequestError',
        cause=cause,
        request=f'[{http_request.method}] {http_request.url}',
    )


def create_error_response(exc: GenAIOrchestratorException) -> ErrorResponse:
    """
    Create ErrorResponse for a Gen AI Orchestrator exception

    Args:
        exc: the Gen AI Orchestrator exception
    Returns:
        The ErrorResponse with the exception parameters
    """

    return ErrorResponse(
        code=exc.error_code.value,
        message=exc.message,
        detail=exc.detail,
        info=exc.info,
    )

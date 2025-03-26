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
"""Router Module for Document Compressor Providers"""

import logging

from fastapi import APIRouter, Request

from gen_ai_orchestrator.errors.exceptions.ai_provider.ai_provider_exceptions import (
    AIProviderBadRequestException,
)
from gen_ai_orchestrator.errors.exceptions.document_compressor.document_compressor_exceptions import \
    GenAIUnknownDocumentCompressorProviderException
from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIOrchestratorException,
)
from gen_ai_orchestrator.errors.handlers.fastapi.fastapi_handler import (
    create_error_info_bad_request,
    create_error_info_not_found,
    create_error_response,
)
from gen_ai_orchestrator.models.document_compressor.bloomz.bloomz_compressor_setting import BloomzCompressorSetting
from gen_ai_orchestrator.models.document_compressor.document_compressor_provider import DocumentCompressorProvider
from gen_ai_orchestrator.models.document_compressor.document_compressor_types import DocumentCompressorSetting
from gen_ai_orchestrator.routers.requests.requests import DocumentCompressorProviderSettingStatusRequest
from gen_ai_orchestrator.routers.responses.responses import (
    ProviderSettingStatusResponse, DocumentCompressorProviderResponse,
)
from gen_ai_orchestrator.services.document_compressor.document_compressor_service import \
    check_document_compressor_setting

logger = logging.getLogger(__name__)

document_compressor_providers_router = APIRouter(
    prefix='/document-compressor-providers',
    tags=['Document Compressor Providers'],
    responses={404: {'description': 'Not found'}},
)


@document_compressor_providers_router.get('')
async def get_all_document_compressor_providers() -> list[DocumentCompressorProvider]:
    """
    Returns:
        List of available Document Compressor Providers
    """
    return [provider.value for provider in DocumentCompressorProvider]


@document_compressor_providers_router.get('/{provider_id}')
async def get_document_compressor_provider_by_id(
    http_request: Request, provider_id: str
) -> DocumentCompressorProviderResponse:
    """
    Get Document Compressor Provider by ID
    Args:
        http_request: The http request
        provider_id: The provider id

    Returns:
        The Document Compressor Provider Response

    Raises:
        GenAIUnknownProviderException: if the provider is unknown
    """

    # Request validation
    validate_document_compressor_provider(http_request, provider_id)

    return DocumentCompressorProviderResponse(provider=DocumentCompressorProvider(provider_id))


@document_compressor_providers_router.get('/{provider_id}/setting/example')
async def get_document_compressor_provider_setting_by_id(
    http_request: Request, provider_id: DocumentCompressorProvider
) -> DocumentCompressorSetting:
    """
    Get a setting example for a given Document Compressor Provider ID
    Args:
        http_request: The http request
        provider_id: The provider id

    Returns:
        The Document Compressor Provider Setting

    Raises:
        GenAIUnknownDocumentCompressorProviderException: if the provider is unknown
    """

    # Request validation
    validate_document_compressor_provider(http_request, provider_id)

    if provider_id == DocumentCompressorProvider.BLOOMZ:
        return BloomzCompressorSetting(
            provider=DocumentCompressorProvider.BLOOMZ,
            max_documents=3,
            min_score=0.91002147,
            endpoint="http://localhost:8082",
            label="LABEL_1"
        )


@document_compressor_providers_router.post('/{provider_id}/setting/status')
async def check_document_compressor_provider_setting(
    http_request: Request, provider_id: str, request: DocumentCompressorProviderSettingStatusRequest
) -> ProviderSettingStatusResponse:
    """
    Check the validity of a given Document Compressor Provider Setting
    Args:
        http_request: The http request
        provider_id: The provider id
        request: The request of the Document Compressor Provider Setting to be checked

    Returns:
        ProviderSettingStatusResponse

    Raises:
        AIProviderBadRequestException: if the provider ID is not consistent with the request body
    """

    logger.info('Start Document Compressor setting check for provider %s', provider_id)
    # Request validation
    validate_query(http_request, provider_id, request.setting)

    try:
        # Document Compressor setting check
        check_document_compressor_setting(request.setting)

        logger.info('The Document Compressor setting is valid')
        return ProviderSettingStatusResponse(valid=True)
    except GenAIOrchestratorException as exc:
        logger.info('The Document Compressor setting is invalid!')
        logger.error(exc)
        return ProviderSettingStatusResponse(errors=[create_error_response(exc)])


def validate_query(http_request: Request, provider_id: str, setting: DocumentCompressorSetting):
    """
    Check the consistency of the Provider ID with the request body
    Args:
        http_request: The http request
        provider_id: The provider ID
        setting:  The Document Compressor Provider Setting

    Raises:
        AIProviderBadRequestException: if the provider ID is not consistent with the request body
    """

    logger.debug('Document Compressor setting - Request validation')
    validate_document_compressor_provider(http_request, provider_id)
    if provider_id != setting.provider:
        raise AIProviderBadRequestException(
            create_error_info_bad_request(http_request, provider_id)
        )


def validate_document_compressor_provider(http_request: Request, provider_id: str):
    """
    Check existence of Document Compressor Provider by ID
    Args:
        http_request: The http request
        provider_id: The provider ID

    Raises:
        GenAIUnknownDocumentCompressorProviderException: if the provider is unknown
    """

    if not DocumentCompressorProvider.has_value(provider_id):
        raise GenAIUnknownDocumentCompressorProviderException(
            create_error_info_not_found(
                http_request, provider_id, [provider.value for provider in DocumentCompressorProvider]
            )
        )

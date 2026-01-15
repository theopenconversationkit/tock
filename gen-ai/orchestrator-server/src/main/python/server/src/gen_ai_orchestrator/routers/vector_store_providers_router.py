#   Copyright (C) 2023-2026 Credit Mutuel Arkea
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
"""Router Module for Vector Store Providers"""

import logging

from fastapi import APIRouter, Request

from gen_ai_orchestrator.errors.exceptions.ai_provider.ai_provider_exceptions import (
    AIProviderBadRequestException,
)
from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIOrchestratorException,
)
from gen_ai_orchestrator.errors.exceptions.vector_store.vector_store_exceptions import (
    GenAIUnknownVectorStoreProviderException,
)
from gen_ai_orchestrator.errors.handlers.fastapi.fastapi_handler import (
    create_error_info_bad_request,
    create_error_info_not_found,
    create_error_response,
)
from gen_ai_orchestrator.models.security.raw_secret_key.raw_secret_key import (
    RawSecretKey,
)
from gen_ai_orchestrator.models.vector_stores.open_search.open_search_setting import (
    OpenSearchVectorStoreSetting,
)
from gen_ai_orchestrator.models.vector_stores.vector_store_types import (
    VectorStoreSetting,
)
from gen_ai_orchestrator.models.vector_stores.vectore_store_provider import (
    VectorStoreProvider,
)
from gen_ai_orchestrator.routers.requests.requests import (
    VectorStoreProviderSettingStatusRequest,
)
from gen_ai_orchestrator.routers.responses.responses import (
    ProviderSettingStatusResponse,
    VectorStoreProviderResponse,
)
from gen_ai_orchestrator.services.vector_store.vector_store_service import (
    check_vector_store_setting,
)

logger = logging.getLogger(__name__)

vector_store_providers_router = APIRouter(
    prefix='/vector-store-providers',
    tags=['Vector Store Providers'],
    responses={404: {'description': 'Not found'}},
)


@vector_store_providers_router.get('')
async def get_all_vector_store_providers() -> list[VectorStoreProvider]:
    """
    Returns:
        List of available Vector Store Providers
    """
    return [provider.value for provider in VectorStoreProvider]


@vector_store_providers_router.get('/{provider_id}')
async def get_vector_store_provider_by_id(
    http_request: Request, provider_id: str
) -> VectorStoreProviderResponse:
    """
    Get Vector Store Provider by ID
    Args:
        http_request: The http request
        provider_id: The provider id

    Returns:
        The Vector Store Provider Response

    Raises:
        GenAIUnknownProviderException: if the provider is unknown
    """

    # Request validation
    validate_vector_store_provider(http_request, provider_id)

    return VectorStoreProviderResponse(provider=VectorStoreProvider(provider_id))


@vector_store_providers_router.get('/{provider_id}/setting/example')
async def get_vector_store_provider_setting_by_id(
    http_request: Request, provider_id: VectorStoreProvider
) -> VectorStoreSetting:
    """
    Get a setting example for a given Vector Store Provider ID
    Args:
        http_request: The http request
        provider_id: The provider id

    Returns:
        The Vector Store Provider Setting

    Raises:
        GenAIUnknownVectorStoreProviderException: if the provider is unknown
    """

    # Request validation
    validate_vector_store_provider(http_request, provider_id)

    if provider_id == VectorStoreProvider.OPEN_SEARCH:
        return OpenSearchVectorStoreSetting(
            provider=VectorStoreProvider.OPEN_SEARCH,
            host='localhost',
            port=9200,
            username='admin',
            password=RawSecretKey(secret='admin'),
        )


@vector_store_providers_router.post('/{provider_id}/setting/status')
async def check_vector_store_provider_setting(
    http_request: Request, provider_id: str, request: VectorStoreProviderSettingStatusRequest
) -> ProviderSettingStatusResponse:
    """
    Check the validity of a given Vector Store Provider Setting
    Args:
        http_request: The http request
        provider_id: The provider id
        request: The request of the Vector Store Provider Setting to be checked

    Returns:
        ProviderSettingStatusResponse

    Raises:
        AIProviderBadRequestException: if the provider ID is not consistent with the request body
    """

    logger.info('Start Vector Store setting check for provider %s', provider_id)
    # Request validation
    if request.vector_store_setting is not None:
        validate_query(http_request, provider_id, request.vector_store_setting)

    try:
        # Vector Store setting check
        await check_vector_store_setting(request.vector_store_setting, request.em_setting, request.document_index_name)

        logger.info('The Vector Store setting is valid')
        return ProviderSettingStatusResponse(valid=True)
    except GenAIOrchestratorException as exc:
        logger.info('The Vector Store setting is invalid!')
        logger.error(exc)
        return ProviderSettingStatusResponse(errors=[create_error_response(exc)])


def validate_query(http_request: Request, provider_id: str, setting: VectorStoreSetting):
    """
    Check the consistency of the Provider ID with the request body
    Args:
        http_request: The http request
        provider_id: The provider ID
        setting:  The Vector Store Provider Setting

    Raises:
        AIProviderBadRequestException: if the provider ID is not consistent with the request body
    """

    logger.debug('VectorStore setting - Request validation')
    validate_vector_store_provider(http_request, provider_id)
    if provider_id != setting.provider:
        raise AIProviderBadRequestException(
            create_error_info_bad_request(http_request, provider_id)
        )


def validate_vector_store_provider(http_request: Request, provider_id: str):
    """
    Check existence of Vector Store Provider by ID
    Args:
        http_request: The http request
        provider_id: The provider ID

    Raises:
        GenAIUnknownVectorStoreProviderException: if the provider is unknown
    """

    if not VectorStoreProvider.has_value(provider_id):
        raise GenAIUnknownVectorStoreProviderException(
            create_error_info_not_found(
                http_request, provider_id, [provider.value for provider in VectorStoreProvider]
            )
        )

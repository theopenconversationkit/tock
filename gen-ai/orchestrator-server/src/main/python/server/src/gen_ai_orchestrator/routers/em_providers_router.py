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
"""Router Module for Embedding Model Providers"""

import logging

from fastapi import APIRouter, Request

from gen_ai_orchestrator.errors.exceptions.ai_provider.ai_provider_exceptions import (
    AIProviderBadQueryException,
)
from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIOrchestratorException,
    GenAIUnknownProviderException,
)
from gen_ai_orchestrator.errors.handlers.fastapi.fastapi_handler import (
    create_error_info_bad_request,
    create_error_info_not_found,
    create_error_response,
)
from gen_ai_orchestrator.models.em.azureopenai.azure_openai_em_setting import (
    AzureOpenAIEMSetting,
)
from gen_ai_orchestrator.models.em.bloomz.bloomz_em_setting import (
    BloomzEMSetting,
)
from gen_ai_orchestrator.models.em.em_provider import EMProvider
from gen_ai_orchestrator.models.em.em_types import EMSetting
from gen_ai_orchestrator.models.em.openai.openai_em_setting import (
    OpenAIEMSetting,
)
from gen_ai_orchestrator.models.security.raw_secret_key.raw_secret_key import (
    RawSecretKey,
)
from gen_ai_orchestrator.routers.requests.requests import (
    EMProviderSettingStatusQuery,
)
from gen_ai_orchestrator.routers.responses.responses import (
    EMProviderResponse,
    ProviderSettingStatusResponse,
)
from gen_ai_orchestrator.services.em.em_service import check_em_setting

logger = logging.getLogger(__name__)

em_providers_router = APIRouter(
    prefix='/em-providers',
    tags=['Embedding Model Providers'],
    responses={404: {'description': 'Not found'}},
)


@em_providers_router.get('')
async def get_all_em_providers() -> list[EMProvider]:
    """
    Returns:
        List of available Embedding Model Providers
    """
    return [provider.value for provider in EMProvider]


@em_providers_router.get('/{provider_id}')
async def get_em_provider_by_id(
    request: Request, provider_id: str
) -> EMProviderResponse:
    """
    Get Embedding Model Provider by ID
    Args:
        request: The http request
        provider_id: The provider id

    Returns:
        The EM Provider Response.

    Raises:
        GenAIUnknownProviderException: if the provider is unknown
    """

    # Query validation
    validate_em_provider(request, provider_id)

    return EMProviderResponse(provider=EMProvider(provider_id))


@em_providers_router.get('/{provider_id}/setting/example')
async def get_em_provider_setting_by_id(
    request: Request, provider_id: EMProvider
) -> EMSetting:
    """
    Get a setting example for a given EM Provider ID
    Args:
        request: The http request
        provider_id: The provider id

    Returns:
        The EM Provider Setting

    Raises:
        GenAIUnknownProviderException: if the provider is unknown
    """

    # Query validation
    validate_em_provider(request, provider_id)

    if provider_id == EMProvider.OPEN_AI:
        return OpenAIEMSetting(
            provider=EMProvider.OPEN_AI,
            api_key=RawSecretKey(secret='ab7***************************A1IV4B'),
            model='gpt-3.5-turbo',
        )
    elif provider_id == EMProvider.AZURE_OPEN_AI_SERVICE:
        return AzureOpenAIEMSetting(
            provider=EMProvider.AZURE_OPEN_AI_SERVICE,
            api_key=RawSecretKey(secret='ab7***************************A1IV4B'),
            deployment_name='my-deployment-name',
            model='text-embedding-ada-002',
            api_base='https://doc.tock.ai/tock',
            api_version='2023-05-15',
        )
    elif provider_id == EMProvider.BLOOMZ:
        return BloomzEMSetting(
            provider=EMProvider.BLOOMZ,
            api_base='https://doc.tock.ai/tock',
            pooling='last',
        )


@em_providers_router.post('/{provider_id}/setting/status')
async def check_em_provider_setting(
    request: Request, provider_id: str, query: EMProviderSettingStatusQuery
) -> ProviderSettingStatusResponse:
    """
    Check the validity of a given EM Provider Setting
    Args:
        request: The http request
        provider_id: The provider id
        query: The query of the EM Provider Setting to be checked

    Returns:
        ProviderSettingStatusResponse

    Raises:
        AIProviderBadQueryException: if the provider ID is not consistent with the request body
    """

    # Query validation
    validate_query(request, provider_id, query.setting)

    try:
        # EM setting check
        await check_em_setting(query.setting)

        return ProviderSettingStatusResponse(valid=True)
    except GenAIOrchestratorException as exc:
        logger.error(exc)
        return ProviderSettingStatusResponse(errors=[create_error_response(exc)])


def validate_query(request: Request, provider_id: str, setting: EMSetting):
    """
    Check the consistency of the Provider ID with the request body
    Args:
        request: The http request
        provider_id: The provider ID
        setting:  The EM Provider Setting

    Raises:
        AIProviderBadQueryException: if the provider ID is not consistent with the request body
    """

    validate_em_provider(request, provider_id)
    if provider_id != setting.provider:
        raise AIProviderBadQueryException(
            create_error_info_bad_request(request, provider_id)
        )


def validate_em_provider(request: Request, provider_id: str):
    """
    Check existence of EM Provider by ID
    Args:
        request: The http request
        provider_id: The provider ID

    Raises:
        GenAIUnknownProviderException: if the provider is unknown
    """

    if not EMProvider.has_value(provider_id):
        raise GenAIUnknownProviderException(
            create_error_info_not_found(
                request, provider_id, [provider.value for provider in EMProvider]
            )
        )

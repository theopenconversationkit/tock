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
"""Router Module for Observability Providers"""

import logging

from fastapi import APIRouter, Request

from gen_ai_orchestrator.errors.exceptions.ai_provider.ai_provider_exceptions import (
    AIProviderBadQueryException,
)
from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIOrchestratorException,
    GenAIUnknownProviderException,
)
from gen_ai_orchestrator.errors.exceptions.observability.observability_exceptions import \
    GenAIUnknownObservabilityProviderException
from gen_ai_orchestrator.errors.handlers.fastapi.fastapi_handler import (
    create_error_info_bad_request,
    create_error_info_not_found,
    create_error_response,
)
from gen_ai_orchestrator.models.observability.observability_provider import ObservabilityProvider
from gen_ai_orchestrator.models.observability.observability_type import ObservabilitySetting
from gen_ai_orchestrator.models.security.raw_secret_key.raw_secret_key import (
    RawSecretKey,
)
from gen_ai_orchestrator.routers.requests.requests import (
    ObservabilityProviderSettingStatusQuery,
)
from gen_ai_orchestrator.routers.responses.responses import (
    ObservabilityProviderResponse,
    ProviderSettingStatusResponse,
)
from gen_ai_orchestrator.services.observability.observabilty_service import check_observability_setting

logger = logging.getLogger(__name__)

observability_providers_router = APIRouter(
    prefix='/observability-providers',
    tags=['Observability Providers'],
    responses={404: {'description': 'Not found'}},
)


@observability_providers_router.get('')
async def get_all_observability_providers() -> list[ObservabilityProvider]:
    """
    Returns:
        List of available Observability Providers
    """
    return [provider.value for provider in ObservabilityProvider]


@observability_providers_router.get('/{provider_id}')
async def get_observability_provider_by_id(
    request: Request, provider_id: str
) -> ObservabilityProviderResponse:
    """
    Get Observability Provider by ID
    Args:
        request: The http request
        provider_id: The provider id

    Returns:
        The Observability Provider Response

    Raises:
        GenAIUnknownProviderException: if the provider is unknown
    """

    # Query validation
    validate_observability_provider(request, provider_id)

    return ObservabilityProviderResponse(provider=ObservabilityProvider(provider_id))


@observability_providers_router.get('/{provider_id}/setting/example')
async def get_observability_provider_setting_by_id(
    request: Request, provider_id: ObservabilityProvider
) -> ObservabilitySetting:
    """
    Get a setting example for a given Observability Provider ID
    Args:
        request: The http request
        provider_id: The provider id

    Returns:
        The Observability Provider Setting

    Raises:
        GenAIUnknownObservabilityProviderException: if the provider is unknown
    """

    # Query validation
    validate_observability_provider(request, provider_id)

    if provider_id == ObservabilityProvider.LANGFUSE:
        return ObservabilitySetting(
            provider=ObservabilityProvider.LANGFUSE,
            secret_key=RawSecretKey(value='sk-lf-93c4f78f-4096-416b-a6e3-ceabe45abe8f'),
            public_key='pk-lf-5e374dc6-e194-4b37-9c07-b77e68ef7d2c',
            url="https://cloud.langfuse.com"
        )


@observability_providers_router.post('/{provider_id}/setting/status')
async def check_observability_provider_setting(
    request: Request, provider_id: str, query: ObservabilityProviderSettingStatusQuery
) -> ProviderSettingStatusResponse:
    """
    Check the validity of a given Observability Provider Setting
    Args:
        request: The http request
        provider_id: The provider id
        query: The query of the Observability Provider Setting to be checked

    Returns:
        ProviderSettingStatusResponse

    Raises:
        AIProviderBadQueryException: if the provider ID is not consistent with the request body
    """

    logger.info('Start Observability setting check for provider %s', provider_id)
    # Query validation
    validate_query(request, provider_id, query.setting)

    try:
        # Observability setting check
        check_observability_setting(query.setting)

        logger.info('The Observability setting is valid')
        return ProviderSettingStatusResponse(valid=True)
    except GenAIOrchestratorException as exc:
        logger.info('The Observability setting is invalid!')
        logger.error(exc)
        return ProviderSettingStatusResponse(errors=[create_error_response(exc)])


def validate_query(request: Request, provider_id: str, setting: ObservabilitySetting):
    """
    Check the consistency of the Provider ID with the request body
    Args:
        request: The http request
        provider_id: The provider ID
        setting:  The Observability Provider Setting

    Raises:
        AIProviderBadQueryException: if the provider ID is not consistent with the request body
    """

    logger.debug('Observability setting - Query validation')
    validate_observability_provider(request, provider_id)
    if provider_id != setting.provider:
        raise AIProviderBadQueryException(
            create_error_info_bad_request(request, provider_id)
        )


def validate_observability_provider(request: Request, provider_id: str):
    """
    Check existence of Observability Provider by ID
    Args:
        request: The http request
        provider_id: The provider ID

    Raises:
        GenAIUnknownObservabilityProviderException: if the provider is unknown
    """

    if not ObservabilityProvider.has_value(provider_id):
        raise GenAIUnknownObservabilityProviderException(
            create_error_info_not_found(
                request, provider_id, [provider.value for provider in ObservabilityProvider]
            )
        )

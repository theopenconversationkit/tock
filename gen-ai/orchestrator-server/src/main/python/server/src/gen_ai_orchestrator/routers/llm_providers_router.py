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
"""Router Module for Large Language Model Providers"""

import logging

from fastapi import APIRouter, Request

from gen_ai_orchestrator.errors.exceptions.ai_provider.ai_provider_exceptions import (
    AIProviderBadRequestException,
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
from gen_ai_orchestrator.models.llm.azureopenai.azure_openai_llm_setting import (
    AzureOpenAILLMSetting,
)
from gen_ai_orchestrator.models.llm.llm_provider import LLMProvider
from gen_ai_orchestrator.models.llm.llm_types import LLMSetting
from gen_ai_orchestrator.models.llm.openai.openai_llm_setting import (
    OpenAILLMSetting,
)
from gen_ai_orchestrator.models.security.raw_secret_key.raw_secret_key import (
    RawSecretKey,
)
from gen_ai_orchestrator.routers.requests.requests import (
    LLMProviderSettingStatusRequest,
)
from gen_ai_orchestrator.routers.responses.responses import (
    LLMProviderResponse,
    ProviderSettingStatusResponse,
)
from gen_ai_orchestrator.services.llm.llm_service import check_llm_setting

logger = logging.getLogger(__name__)

llm_providers_router = APIRouter(
    prefix='/llm-providers',
    tags=['Large Language Model Providers'],
    responses={404: {'description': 'Not found'}},
)


@llm_providers_router.get('')
async def get_all_llm_providers() -> list[LLMProvider]:
    """
    Returns:
        List of available Large Language Model Providers
    """
    return [provider.value for provider in LLMProvider]


@llm_providers_router.get('/{provider_id}')
async def get_llm_provider_by_id(
    http_request: Request, provider_id: str
) -> LLMProviderResponse:
    """
    Get Large Language Model Provider by ID
    Args:
        http_request: The http request
        provider_id: The provider id

    Returns:
        The LLM Provider Response

    Raises:
        GenAIUnknownProviderException: if the provider is unknown
    """

    # Request validation
    validate_llm_provider(http_request, provider_id)

    return LLMProviderResponse(provider=LLMProvider(provider_id))


@llm_providers_router.get('/{provider_id}/setting/example')
async def get_llm_provider_setting_by_id(
    http_request: Request, provider_id: LLMProvider
) -> LLMSetting:
    """
    Get a setting example for a given LLM Provider ID
    Args:
        http_request: The http request
        provider_id: The provider id

    Returns:
        The LLM Provider Setting

    Raises:
        GenAIUnknownProviderException: if the provider is unknown
    """

    # Request validation
    validate_llm_provider(http_request, provider_id)

    if provider_id == LLMProvider.OPEN_AI:
        return OpenAILLMSetting(
            provider=LLMProvider.OPEN_AI,
            api_key=RawSecretKey(secret='ab7***************************A1IV4B'),
            model='gpt-3.5-turbo',
            temperature=1.3,
            prompt='How to learn to ride a bike without wheels!',
        )
    elif provider_id == LLMProvider.AZURE_OPEN_AI_SERVICE:
        return AzureOpenAILLMSetting(
            provider=LLMProvider.AZURE_OPEN_AI_SERVICE,
            api_key=RawSecretKey(secret='ab7***************************A1IV4B'),
            deployment_name='my-deployment-name',
            model='gpt-4o',
            api_base='https://doc.tock.ai/tock',
            api_version='2023-05-15',
            temperature=0.7,
            prompt='How to learn to ride a bike without wheels!',
        )


@llm_providers_router.post('/{provider_id}/setting/status')
async def check_llm_provider_setting(
    http_request: Request, provider_id: str, request: LLMProviderSettingStatusRequest
) -> ProviderSettingStatusResponse:
    """
    Check the validity of a given LLM Provider Setting
    Args:
        http_request: The http request
        provider_id: The provider id
        request: The request of the LLM Provider Setting to be checked

    Returns:
        ProviderSettingStatusResponse

    Raises:
        AIProviderBadRequestException: if the provider ID is not consistent with the request body
    """

    logger.info('Start LLM setting check for provider %s', provider_id)
    # Request validation
    validate_query(http_request, provider_id, request.setting)

    try:
        # LLM setting check
        await check_llm_setting(request)

        logger.info('The LLM setting is valid')
        return ProviderSettingStatusResponse(valid=True)
    except GenAIOrchestratorException as exc:
        logger.info('The LLM setting is invalid!')
        logger.error(exc)
        return ProviderSettingStatusResponse(errors=[create_error_response(exc)])


def validate_query(http_request: Request, provider_id: str, setting: LLMSetting):
    """
    Check the consistency of the Provider ID with the request body
    Args:
        http_request: The http request
        provider_id: The provider ID
        setting:  The LLM Provider Setting

    Raises:
        AIProviderBadRequestException: if the provider ID is not consistent with the request body
    """

    logger.debug('LLM setting - Request validation')
    validate_llm_provider(http_request, provider_id)
    if provider_id != setting.provider:
        raise AIProviderBadRequestException(
            create_error_info_bad_request(http_request, provider_id)
        )


def validate_llm_provider(http_request: Request, provider_id: str):
    """
    Check existence of LLM Provider by ID
    Args:
        http_request: The http request
        provider_id: The provider ID

    Raises:
        GenAIUnknownProviderException: if the provider is unknown
    """

    if not LLMProvider.has_value(provider_id):
        raise GenAIUnknownProviderException(
            create_error_info_not_found(
                http_request, provider_id, [provider.value for provider in LLMProvider]
            )
        )

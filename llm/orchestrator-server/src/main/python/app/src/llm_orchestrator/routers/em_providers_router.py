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

from fastapi import APIRouter, HTTPException, Request

from llm_orchestrator.errors.exceptions.ai_provider.ai_provider_exceptions import (
    AIProviderBadQueryException,
)
from llm_orchestrator.errors.exceptions.exceptions import (
    GenAIOrchestratorException,
    GenAIUnknownProviderException,
)
from llm_orchestrator.errors.handlers.fastapi.fastapi_handler import (
    create_error_info_bad_request,
    create_error_info_not_found,
    create_error_response,
)
from llm_orchestrator.models.em.azureopenai.azure_openai_em_setting import (
    AzureOpenAIEMSetting,
)
from llm_orchestrator.models.em.em_provider import EMProvider
from llm_orchestrator.models.em.openai.openai_em_setting import OpenAIEMSetting
from llm_orchestrator.models.errors.errors_models import ErrorCode
from llm_orchestrator.routers.requests.requests import (
    EMProviderSettingStatusQuery,
)
from llm_orchestrator.routers.requests.types import EMSetting
from llm_orchestrator.routers.responses.responses import (
    EMProviderResponse,
    ProviderSettingStatusResponse,
)
from llm_orchestrator.services.em.em_service import check_em_setting

logger = logging.getLogger(__name__)

em_providers_router = APIRouter(
    prefix='/em-providers',
    tags=['Embedding Model Providers'],
    responses={404: {'description': 'Not found'}},
)


@em_providers_router.get('')
async def get_all_em_providers() -> list[EMProvider]:
    return [provider.value for provider in EMProvider]


@em_providers_router.get('/{provider_id}')
async def get_em_provider_by_id(
    request: Request, provider_id: str
) -> EMProviderResponse:
    # Query validation
    validate_em_provider(request, provider_id)

    return EMProviderResponse(provider=EMProvider(provider_id))


@em_providers_router.get('/{provider_id}/setting/example')
async def get_em_provider_setting_by_id(provider_id: EMProvider) -> EMSetting:
    if provider_id == EMProvider.OPEN_AI:
        return OpenAIEMSetting(
            provider=EMProvider.OPEN_AI,
            api_key='123-abc-456-def',
            model='gpt-3.5-turbo',
        )
    elif provider_id == EMProvider.AZURE_OPEN_AI_SERVICE:
        return AzureOpenAIEMSetting(
            provider=EMProvider.AZURE_OPEN_AI_SERVICE,
            api_key='123-abc-456-def',
            deployment_name='my-deployment-name',
            api_base='https://doc.tock.ai/tock',
            api_version='2023-05-15',
        )
    else:
        raise HTTPException(status_code=400, detail=ErrorCode.E21)


@em_providers_router.post('/{provider_id}/setting/status')
async def check_em_provider_setting(
    request: Request, provider_id: str, query: EMProviderSettingStatusQuery
) -> ProviderSettingStatusResponse:
    # Query validation
    validate_query(request, provider_id, query.setting)

    try:
        # EM setting check
        check_em_setting(query.setting)

        return ProviderSettingStatusResponse(valid=True)
    except GenAIOrchestratorException as exc:
        logger.error(exc)
        return ProviderSettingStatusResponse(errors=[create_error_response(exc)])


def validate_query(request: Request, provider_id: str, setting: EMSetting):
    validate_em_provider(request, provider_id)
    if provider_id != setting.provider:
        raise AIProviderBadQueryException(
            create_error_info_bad_request(request, provider_id)
        )


def validate_em_provider(request: Request, provider_id: str):
    if not EMProvider.has_value(provider_id):
        raise GenAIUnknownProviderException(
            create_error_info_not_found(
                request, provider_id, [provider.value for provider in EMProvider]
            )
        )

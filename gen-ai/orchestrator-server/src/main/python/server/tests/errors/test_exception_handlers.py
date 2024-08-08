#   Copyright (C) 2024 Credit Mutuel Arkea
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
import httpx
import pytest
from openai import (
    APIConnectionError,
    APIError,
    AuthenticationError,
    BadRequestError,
    NotFoundError,
)
from opensearchpy import (
    AuthenticationException as OpenSearchAuthenticationException,
)
from opensearchpy import ImproperlyConfigured as OpenSearchImproperlyConfigured
from opensearchpy import NotFoundError as OpenSearchNotFoundError
from opensearchpy import TransportError as OpenSearchTransportError

from gen_ai_orchestrator.errors.exceptions.ai_provider.ai_provider_exceptions import (
    AIProviderAPIBadRequestException,
    AIProviderAPIContextLengthExceededException,
    AIProviderAPIDeploymentNotFoundException,
    AIProviderAPIErrorException,
    AIProviderAPIModelException,
    AIProviderAPIResourceNotFoundException,
)
from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIAuthenticationException,
    GenAIConnectionErrorException,
)
from gen_ai_orchestrator.errors.exceptions.vector_store.opensearch_exceptions import (
    GenAIOpenSearchIndexNotFoundException,
    GenAIOpenSearchResourceNotFoundException,
    GenAIOpenSearchSettingException,
    GenAIOpenSearchTransportException,
)
from gen_ai_orchestrator.errors.handlers.openai.openai_exception_handler import (
    openai_exception_handler,
)
from gen_ai_orchestrator.errors.handlers.opensearch.opensearch_exception_handler import (
    opensearch_exception_handler,
)

_request = httpx.Request('GET', 'https://www.dock.tock.ai')
_response = httpx.Response(request=_request, status_code=400)

@pytest.mark.asyncio
async def test_openai_exception_handler_api_connection_error():
    @openai_exception_handler(provider='OpenAI or AzureOpenAIService')
    async def decorated_function(*args, **kwargs):
        raise APIConnectionError(message='error', request=_request)

    with pytest.raises(GenAIConnectionErrorException):
        await decorated_function()

@pytest.mark.asyncio
async def test_openai_exception_handler_authentication_error():
    @openai_exception_handler(provider='OpenAI or AzureOpenAIService')
    async def decorated_function(*args, **kwargs):
        raise AuthenticationError(message='error', response=_response, body=None)

    with pytest.raises(GenAIAuthenticationException):
        await decorated_function()


@pytest.mark.asyncio
async def test_openai_exception_handler_model_not_found_error():
    @openai_exception_handler(provider='OpenAI or AzureOpenAIService')
    async def decorated_function(*args, **kwargs):
        raise NotFoundError(
            message='error', response=_response, body={'code': 'model_not_found'}
        )

    with pytest.raises(AIProviderAPIModelException):
        await decorated_function()

@pytest.mark.asyncio
async def test_openai_exception_handler_resource_not_found_error():
    @openai_exception_handler(provider='OpenAI or AzureOpenAIService')
    async def decorated_function(*args, **kwargs):
        raise NotFoundError(message='error', response=_response, body=None)

    with pytest.raises(AIProviderAPIResourceNotFoundException):
        await decorated_function()

@pytest.mark.asyncio
async def test_openai_exception_handler_deployment_not_found_error():
    @openai_exception_handler(provider='OpenAI or AzureOpenAIService')
    async def decorated_function(*args, **kwargs):
        raise NotFoundError(
            message='error', response=_response, body={'code': 'DeploymentNotFound'}
        )

    with pytest.raises(AIProviderAPIDeploymentNotFoundException):
        await decorated_function()

@pytest.mark.asyncio
async def test_openai_exception_handler_bad_request_context_len_error():
    @openai_exception_handler(provider='OpenAI or AzureOpenAIService')
    async def decorated_function(*args, **kwargs):
        raise BadRequestError(
            message='error',
            response=_response,
            body={'code': 'context_length_exceeded'},
        )

    with pytest.raises(AIProviderAPIContextLengthExceededException):
        await decorated_function()

@pytest.mark.asyncio
async def test_openai_exception_handler_bad_request_error():
    @openai_exception_handler(provider='OpenAI or AzureOpenAIService')
    async def decorated_function(*args, **kwargs):
        raise BadRequestError(message='error', response=_response, body=None)

    with pytest.raises(AIProviderAPIBadRequestException):
        await decorated_function()

@pytest.mark.asyncio
async def test_openai_exception_handler_api_error():
    @openai_exception_handler(provider='OpenAI or AzureOpenAIService')
    async def decorated_function(*args, **kwargs):
        raise APIError(message='error', request=_request, body=None)

    with pytest.raises(AIProviderAPIErrorException):
        await decorated_function()

@pytest.mark.asyncio
async def test_opensearch_exception_handler_improperly_configured():
    @opensearch_exception_handler
    async def decorated_function(*args, **kwargs):
        raise OpenSearchImproperlyConfigured()

    with pytest.raises(GenAIOpenSearchSettingException):
        await decorated_function()

@pytest.mark.asyncio
async def test_opensearch_exception_handler_connexion_error():
    @opensearch_exception_handler
    async def decorated_function(*args, **kwargs):
        raise OpenSearchAuthenticationException(
            'status_code', 'there was an error', 'some info'
        )

    with pytest.raises(GenAIAuthenticationException):
        await decorated_function()

@pytest.mark.asyncio
async def test_opensearch_exception_handler_resource_not_found_error():
    @opensearch_exception_handler
    async def decorated_function(*args, **kwargs):
        raise OpenSearchNotFoundError('400', 'there was an error')

    with pytest.raises(GenAIOpenSearchResourceNotFoundException):
        await decorated_function()

@pytest.mark.asyncio
async def test_opensearch_exception_handler_index_not_found_error():
    @opensearch_exception_handler
    async def decorated_function(*args, **kwargs):
        raise OpenSearchNotFoundError('400', 'index_not_found_exception')

    with pytest.raises(GenAIOpenSearchIndexNotFoundException):
        await decorated_function()

@pytest.mark.asyncio
async def test_opensearch_exception_handler_transport_error():
    @opensearch_exception_handler
    async def decorated_function(*args, **kwargs):
        raise OpenSearchTransportError('400', 'there was an error')

    with pytest.raises(GenAIOpenSearchTransportException):
        await decorated_function()

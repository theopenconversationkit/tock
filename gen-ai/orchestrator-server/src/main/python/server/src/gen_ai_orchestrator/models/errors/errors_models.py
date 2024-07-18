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
"""Module for Error Models"""

from enum import Enum, unique
from typing import Dict, Optional

from pydantic import BaseModel, Field
from pydantic.json_schema import GetJsonSchemaHandler, JsonSchemaValue
from pydantic_core import core_schema

from gen_ai_orchestrator.models.llm.llm_provider import LLMProvider


@unique
class ErrorCode(Enum):
    """Enumeration to list application error codes"""

    # Gen AI Orchestrator Errors
    GEN_AI_UNKNOWN_ERROR = 1000
    GEN_AI_CONNECTION_ERROR = 1001
    GEN_AI_AUTHENTICATION_ERROR = 1002
    GEN_AI_UNKNOWN_PROVIDER_SETTING = 1003
    GEN_AI_GUARD_CHECK_ERROR = 1004
    GEN_AI_PROMPT_TEMPLATE_ERROR = 1005

    # AI Provider Errors
    AI_PROVIDER_UNKNOWN = 2000
    AI_PROVIDER_BAD_QUERY = 2001
    AI_PROVIDER_API_ERROR = 2002
    AI_PROVIDER_API_RESOURCE_NOT_FOUND = 2003
    AI_PROVIDER_API_MODEL_NOT_FOUND = 2004
    AI_PROVIDER_API_DEPLOYMENT_NOT_FOUND = 2005
    AI_PROVIDER_API_BAD_REQUEST = 2006
    AI_PROVIDER_API_CONTEXT_LENGTH_EXCEEDED_BAD_REQUEST = 2007

    # Vector Store Errors
    VECTOR_STORE_UNKNOWN = 3000

    # OpenSearch Errors
    OPEN_SEARCH_SETTINGS_ERROR = 4000
    OPEN_SEARCH_TRANSPORT_ERROR = 4001
    OPEN_SEARCH_RESOURCE_NOT_FOUND = 4002
    OPEN_SEARCH_INDEX_NOT_FOUND = 4003

    # Observability Errors
    OBSERVABILITY_UNKNOWN_PROVIDER = 5000
    OBSERVABILITY_UNKNOWN_PROVIDER_SETTING = 5001
    OBSERVABILITY_API_ERROR = 5002

    # Compressor Errors
    COMPRESSOR_UNKNOWN = 6000

    @classmethod
    def __get_pydantic_json_schema__(
        cls, core_schema: core_schema.JsonSchema, handler: GetJsonSchemaHandler
    ) -> Dict:
        """
        Document error codes using the names in the enum so that they are more comprehensible in the openAPI spec.
        """
        return {
            'enum': [item.value for item in cls],
            'description': '\n'.join(
                [f'* `{item.value}`: {item.name}' for item in cls]
            ),
            'type': 'string',
        }


class ErrorMessage(BaseModel):
    """Model for Error message and its detail"""

    message: str = Field(
        description='The Gen AI orchestrator error message',
        examples=['Authentication error to the AI Provider API.'],
    )
    detail: Optional[str] = Field(
        description='The AI orchestrator error detail. It provides help or a solution',
        examples=[
            'Check your API key or token and make sure it is correct and active.'
        ],
        default=None,
    )


class ErrorInfo(BaseModel):
    """Error info model"""

    provider: str = Field(
        description='The AI Provider ID',
        examples=[LLMProvider.AZURE_OPEN_AI_SERVICE],
        default='',
    )
    error: str = Field(
        description='The error', examples=['BadRequestError'], default=''
    )
    cause: str = Field(
        description='The error cause',
        examples=['Invalid value for query parameter'],
        default='',
    )
    request: str = Field(
        description='The AI Provider API or the AI Orchestrator API',
        default='',
        examples=['[POST] https://api.openai.com/v1/chat/completions'],
    )


class ErrorMessages:
    """List of all error messages corresponding to the error code"""

    ERROR_MESSAGES = {
        # Gen AI Orchestrator Errors
        ErrorCode.GEN_AI_UNKNOWN_ERROR: ErrorMessage(message='Unknown error.'),
        ErrorCode.GEN_AI_CONNECTION_ERROR: ErrorMessage(
            message='Connection error.',
            detail='Check the requested URL, your network settings, proxy configuration, '
            'SSL certificates, or firewall rules.',
        ),
        ErrorCode.GEN_AI_AUTHENTICATION_ERROR: ErrorMessage(
            message='Authentication error.',
            detail='Check the API key or token, and make sure it is correct.',
        ),
        ErrorCode.GEN_AI_UNKNOWN_PROVIDER_SETTING: ErrorMessage(
            message='Unknown AI provider setting.'
        ),
        ErrorCode.GEN_AI_GUARD_CHECK_ERROR: ErrorMessage(
            message='Guard check failed.',
        ),
        ErrorCode.GEN_AI_PROMPT_TEMPLATE_ERROR: ErrorMessage(
            message='Prompt Template Error.',
            detail='Check the template syntax.',
        ),
        # AI Provider Errors
        ErrorCode.AI_PROVIDER_UNKNOWN: ErrorMessage(message='Unknown AI Provider.'),
        ErrorCode.AI_PROVIDER_BAD_QUERY: ErrorMessage(
            message='Bad query.', detail='The request seems to be invalid.'
        ),
        ErrorCode.AI_PROVIDER_API_ERROR: ErrorMessage(message='AI Provider API error.'),
        ErrorCode.AI_PROVIDER_API_RESOURCE_NOT_FOUND: ErrorMessage(
            message='An AI Provider resource was not found.',
            detail='The request URL base is correct, but the path or a query parameter is not.',
        ),
        ErrorCode.AI_PROVIDER_API_MODEL_NOT_FOUND: ErrorMessage(
            message='Unknown AI Provider model.',
            detail='Consult the official documentation for accepted values.',
        ),
        ErrorCode.AI_PROVIDER_API_DEPLOYMENT_NOT_FOUND: ErrorMessage(
            message='Unknown AI Provider deployment.',
            detail='Consult the official documentation for accepted values.',
        ),
        ErrorCode.AI_PROVIDER_API_BAD_REQUEST: ErrorMessage(
            message='AI Provider API error.', detail='Bad request.'
        ),
        ErrorCode.AI_PROVIDER_API_CONTEXT_LENGTH_EXCEEDED_BAD_REQUEST: ErrorMessage(
            message="The model's context length has been exceeded.",
            detail='Reduce the length of the prompt message.',
        ),
        # Vector Store Errors
        ErrorCode.VECTOR_STORE_UNKNOWN: ErrorMessage(message='Unknown vector store.'),
        # OpenSearch Errors
        ErrorCode.OPEN_SEARCH_SETTINGS_ERROR: ErrorMessage(
            message='The OpenSearch is improperly configured.',
            detail='The config passed to the client is inconsistent or invalid.',
        ),
        ErrorCode.OPEN_SEARCH_TRANSPORT_ERROR: ErrorMessage(
            message='The OpenSearch returns 4xx http error, or connection error.',
        ),
        ErrorCode.OPEN_SEARCH_RESOURCE_NOT_FOUND: ErrorMessage(
            message='The OpenSearch resource was not found.',
        ),
        ErrorCode.OPEN_SEARCH_INDEX_NOT_FOUND: ErrorMessage(
            message='The OpenSearch index was not found.',
            detail='Ensure that the index exists and create it if it does not.',
        ),
        # Observability Errors
        ErrorCode.OBSERVABILITY_UNKNOWN_PROVIDER: ErrorMessage(
            message='Unknown Observability Provider.'
        ),
        ErrorCode.OBSERVABILITY_UNKNOWN_PROVIDER_SETTING: ErrorMessage(
            message='Unknown Observability Provider Settings.'
        ),
        ErrorCode.OBSERVABILITY_API_ERROR: ErrorMessage(
            message='API error.',
        ),
        # Compressor Errors
        ErrorCode.COMPRESSOR_UNKNOWN: ErrorMessage(message='Unknown compressor provider.'),
    }

    def get_message(self, code: ErrorCode) -> ErrorMessage:
        return self.ERROR_MESSAGES.get(code, ErrorMessage(message='Unknown error'))


error_messages = ErrorMessages()

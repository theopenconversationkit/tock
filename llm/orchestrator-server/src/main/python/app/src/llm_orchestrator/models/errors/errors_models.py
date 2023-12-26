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
from enum import Enum, unique
from typing import Dict, Optional

from pydantic import BaseModel, Field
from pydantic.json_schema import GetJsonSchemaHandler, JsonSchemaValue
from pydantic_core import core_schema

from llm_orchestrator.models.llm.llm_provider import LLMProvider


@unique
class ErrorCode(Enum):
    PROVIDER_NOT_FOUND = 1000
    PROVIDER_BAD_QUERY = 1001

    PROVIDER_API_ERROR = 2000
    PROVIDER_API_CONNECTION_ERROR = 2001
    PROVIDER_API_AUTHENTICATION_ERROR = 2002

    PROVIDER_API_RESOURCE_NOT_FOUND = 2003
    PROVIDER_API_MODEL_NOT_FOUND = 2004
    PROVIDER_API_DEPLOYMENT_NOT_FOUND = 2005

    PROVIDER_API_BAD_REQUEST = 2006
    PROVIDER_API_CONTEXT_LENGTH_EXCEEDED_BAD_REQUEST = 2007

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
    message: str = Field(
        description='The AI orchestrator error message',
        examples=['Authentication error to the AI provider API.'],
    )
    detail: Optional[str] = Field(
        description='The AI orchestrator error detail. It provides help or a solution',
        examples=[
            'Check your API key or token and make sure it is correct and active.'
        ],
        default=None,
    )


class ErrorInfo(BaseModel):
    provider: str = Field(
        description='The AI provider ID', examples=[LLMProvider.AZURE_OPEN_AI_SERVICE]
    )
    error: str = Field(description='The error', examples=['BadRequestError'])
    cause: str = Field(
        description='The error cause', examples=['Invalid value for query parameter']
    )
    request: str = Field(
        description='The AI provider API or the AI Orchestrator API',
        examples=['[POST] https://api.openai.com/v1/chat/completions'],
    )


class ErrorMessages:
    ERROR_MESSAGES = {
        ErrorCode.PROVIDER_NOT_FOUND: ErrorMessage(message='Unknown AI provider.'),
        ErrorCode.PROVIDER_BAD_QUERY: ErrorMessage(
            message='Bad query.', detail='The request seems to be invalid.'
        ),
        ErrorCode.PROVIDER_API_ERROR: ErrorMessage(message='AI provider API error.'),
        ErrorCode.PROVIDER_API_AUTHENTICATION_ERROR: ErrorMessage(
            message='Authentication error to the AI provider API.',
            detail='Check your API key or token and make sure it is correct and active.',
        ),
        # RESOURCE NOT FOUND
        ErrorCode.PROVIDER_API_RESOURCE_NOT_FOUND: ErrorMessage(
            message='An AI provider resource was not found.',
            detail='The request URL base is correct, but the path or a query parameter is not.',
        ),
        ErrorCode.PROVIDER_API_MODEL_NOT_FOUND: ErrorMessage(
            message='Unknown AI provider model.',
            detail='Consult the official documentation for accepted values.',
        ),
        ErrorCode.PROVIDER_API_DEPLOYMENT_NOT_FOUND: ErrorMessage(
            message='Unknown AI provider deployment.',
            detail='Consult the official documentation for accepted values.',
        ),
        ErrorCode.PROVIDER_API_CONNECTION_ERROR: ErrorMessage(
            message='Connection error to the AI provider API.',
            detail='Check the requested URL, your network settings, proxy configuration, '
            'SSL certificates, or firewall rules.',
        ),
        # BAD REQUEST
        ErrorCode.PROVIDER_API_BAD_REQUEST: ErrorMessage(
            message='AI provider API error.', detail='Bad request.'
        ),
        ErrorCode.PROVIDER_API_CONTEXT_LENGTH_EXCEEDED_BAD_REQUEST: ErrorMessage(
            message="The model's context length has been exceeded.",
            detail='Reduce the length of the prompt message.',
        ),
    }

    def get_message(self, code: ErrorCode) -> ErrorMessage:
        return self.ERROR_MESSAGES.get(code, ErrorMessage(message='Unknown error'))

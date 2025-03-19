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
#   Copyright (C) 2023 Credit Mutuel Arkea
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
"""Router Module for Prompt Completion"""

import logging

from fastapi import APIRouter

from gen_ai_orchestrator.routers.requests.requests import CompletionRequest
from gen_ai_orchestrator.routers.responses.responses import PlaygroundResponse, SentenceGenerationResponse
from gen_ai_orchestrator.services.completion.completion_service import (
    generate_sentences, generate,
)

logger = logging.getLogger(__name__)

completion_router = APIRouter(
    prefix='/completion',
    tags=['Prompt completion'],
)

@completion_router.post('/')
async def completion(request: CompletionRequest) -> PlaygroundResponse:
    """
    Playground API

    Args:
        request: The completion request

    Returns:
        The LLM answer and observability info.

    Raises:
        GenAIPromptTemplateException: if the prompt template is incorrect
    """

    logger.info('Completion')
    return await generate(request)


@completion_router.post('/sentences')
async def completion_sentences(request: CompletionRequest) -> SentenceGenerationResponse:
    """
    Sentence Generation API

    Args:
        request: The Sentence Generation Request

    Returns:
        The list of generated sentences.

    Raises:
        GenAIPromptTemplateException: if the prompt template is incorrect
    """

    logger.info('Generate sentences from %s', request.prompt.inputs)
    return await generate_sentences(request)


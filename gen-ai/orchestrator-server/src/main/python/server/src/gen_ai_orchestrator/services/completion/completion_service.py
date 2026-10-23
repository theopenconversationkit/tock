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
"""Module for the Completion Service"""

import json
import logging
import time
from typing import AsyncGenerator

from langchain_core.output_parsers import (
    NumberedListOutputParser,
    StrOutputParser,
)
from langchain_core.prompts import PromptTemplate as LangChainPromptTemplate

from gen_ai_orchestrator.errors.handlers.aws_bedrock.aws_bedrock_exception_handler import (
    aws_bedrock_exception_handler,
)
from gen_ai_orchestrator.errors.handlers.openai.openai_exception_handler import (
    openai_exception_handler,
)
from gen_ai_orchestrator.models.observability.observability_trace import (
    ObservabilityTrace,
)
from gen_ai_orchestrator.routers.requests.requests import CompletionRequest
from gen_ai_orchestrator.routers.responses.responses import (
    PlaygroundResponse,
    SentenceGenerationResponse,
)
from gen_ai_orchestrator.services.langchain.callbacks.completion_callback_handler import (
    CompletionCallbackHandler,
)
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    create_observability_callback_handler,
    get_llm_factory,
)
from gen_ai_orchestrator.services.observability.observabilty_service import (
    get_observability_info,
)
from gen_ai_orchestrator.services.utils.prompt_utility import (
    validate_prompt_template,
)

logger = logging.getLogger(__name__)


@aws_bedrock_exception_handler(provider='AwsBedrock')
@openai_exception_handler(provider='OpenAI or AzureOpenAIService')
async def generate(
    request: CompletionRequest,
) -> PlaygroundResponse:
    """
    Generate answer using a language model based on the provided request.

    :param request: A PlaygroundRequest object containing the llm setting.
    :return: A PlaygroundResponse object containing the answer and observability info.
    """
    logger.info('Prompt completion (Playground) - Start of execution...')
    start_time = time.time()

    logger.info('Prompt completion (Playground) - template validation')
    validate_prompt_template(request.prompt, 'Playground prompt')

    parser = StrOutputParser()
    prompt = LangChainPromptTemplate.from_template(
        template=request.prompt.template,
        template_format=request.prompt.formatter.value,
    )
    model = get_llm_factory(request.llm_setting).get_language_model()

    chain = prompt | model | parser

    config = None
    observability_handler = None
    # Create a RunnableConfig containing the observability callback handler
    if request.observability_setting is not None:
        # Langfuse callback handler
        observability_handler = create_observability_callback_handler(
            observability_setting=request.observability_setting,
            trace_name=ObservabilityTrace.PLAYGROUND.value,
            session_id=None,
            user_id=None,
            tags=None,
        )
        config = {'callbacks': [observability_handler]}

    parsedLlmAnswer = await chain.ainvoke(request.prompt.inputs, config=config)

    logger.info(
        'Prompt completion (Playground) - End of execution. (Duration : %.2f seconds)',
        time.time() - start_time,
    )

    return PlaygroundResponse(
        answer=parsedLlmAnswer,
        observability_info=get_observability_info(
            observability_handler,
            ObservabilityTrace.PLAYGROUND.value,
        ),
    )


@aws_bedrock_exception_handler(provider='AwsBedrock')
@openai_exception_handler(provider='OpenAI or AzureOpenAIService')
async def generate_stream(
    request: CompletionRequest,
) -> AsyncGenerator[dict, None]:
    """
    Generate an answer using a language model and stream it via Server-Sent Events.

    Emits:
      - one 'token' event per new fragment of the answer text;
      - a final 'answer' event carrying the complete PlaygroundResponse (JSON),
        including the time-to-first-token (ttft).

    :param request: A CompletionRequest object containing the llm setting and prompt.
    """
    logger.info('Prompt completion (Playground, stream) - Start of execution...')
    start_time = time.time()

    logger.info('Prompt completion (Playground, stream) - template validation')
    validate_prompt_template(request.prompt, 'Playground prompt')

    parser = StrOutputParser()
    prompt = LangChainPromptTemplate.from_template(
        template=request.prompt.template,
        template_format=request.prompt.formatter.value,
    )
    model = get_llm_factory(request.llm_setting).get_language_model()

    chain = prompt | model | parser

    # TTFT measurement callback (fires on the first streamed token).
    completion_handler = CompletionCallbackHandler()
    callbacks: list = [completion_handler]

    observability_handler = None
    if request.observability_setting is not None:
        observability_handler = create_observability_callback_handler(
            observability_setting=request.observability_setting,
            trace_name=ObservabilityTrace.PLAYGROUND.value,
            session_id=None,
            user_id=None,
            tags=None,
        )
        callbacks.append(observability_handler)

    config = {'callbacks': callbacks}

    full_answer = ''
    async for chunk in chain.astream(request.prompt.inputs, config=config):
        if chunk:
            full_answer += chunk
            yield {
                'event': 'token',
                'data': json.dumps(
                    {'type': 'token', 'content': chunk}, ensure_ascii=False
                ),
            }

    logger.info(
        'Prompt completion (Playground, stream) - End of execution. '
        '(Duration : %.2f seconds)',
        time.time() - start_time,
    )

    response = PlaygroundResponse(
        answer=full_answer,
        observability_info=get_observability_info(
            observability_handler,
            ObservabilityTrace.PLAYGROUND.value,
        ),
        ttft=completion_handler.ttft,
    )
    yield {
        'event': 'answer',
        'data': response.model_dump_json(exclude_none=True),
    }


@aws_bedrock_exception_handler(provider='AwsBedrock')
@openai_exception_handler(provider='OpenAI or AzureOpenAIService')
async def generate_sentences(
    request: CompletionRequest,
) -> SentenceGenerationResponse:
    """
    Generate sentences using a language model based on the provided request,
    and split the generated content into a list of sentences using a specific parser.

    :param request: A PlaygroundRequest object containing the llm setting.
    :return: A GenerateSentencesResponse object containing the list of sentences.
    """
    logger.info('Prompt completion (Sentence Generation) - Start of execution...')
    start_time = time.time()

    logger.info('Prompt completion (Sentence Generation) - template validation')
    validate_prompt_template(request.prompt, 'Sentence generation prompt')

    parser = NumberedListOutputParser()
    prompt = LangChainPromptTemplate.from_template(
        template=request.prompt.template,
        template_format=request.prompt.formatter.value,
        partial_variables={'format_instructions': parser.get_format_instructions()},
    )
    model = get_llm_factory(request.llm_setting).get_language_model()

    chain = prompt | model | parser

    config = None
    # Create a RunnableConfig containing the observability callback handler
    if request.observability_setting is not None:
        config = {
            'callbacks': [
                create_observability_callback_handler(
                    observability_setting=request.observability_setting,
                    trace_name=ObservabilityTrace.SENTENCE_GENERATION.value,
                )
            ]
        }

    sentences = await chain.ainvoke(request.prompt.inputs, config=config)

    logger.info(
        'Prompt completion (Sentence Generation) - End of execution. (Duration : %.2f seconds)',
        time.time() - start_time,
    )

    return SentenceGenerationResponse(sentences=sentences)

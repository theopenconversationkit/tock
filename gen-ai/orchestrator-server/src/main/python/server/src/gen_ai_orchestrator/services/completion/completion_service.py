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
"""Module for the Completion Service"""

import logging
import time

from jinja2 import Template, TemplateError
from langchain_core.output_parsers import NumberedListOutputParser
from langchain_core.prompts import PromptTemplate as LangChainPromptTemplate
from langchain_core.runnables import RunnableConfig

from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIPromptTemplateException,
)
from gen_ai_orchestrator.errors.handlers.huggingfacetgi.hugging_face_exception_handler import (
    hugging_face_exception_handler,
)
from gen_ai_orchestrator.errors.handlers.openai.openai_exception_handler import (
    openai_exception_handler,
)
from gen_ai_orchestrator.models.errors.errors_models import ErrorInfo
from gen_ai_orchestrator.models.observability.observability_trace import (
    ObservabilityTrace,
)
from gen_ai_orchestrator.models.prompt.prompt_formatter import PromptFormatter
from gen_ai_orchestrator.models.prompt.prompt_template import PromptTemplate
from gen_ai_orchestrator.routers.requests.requests import (
    SentenceGenerationQuery,
)
from gen_ai_orchestrator.routers.responses.responses import (
    SentenceGenerationResponse,
)
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    create_observability_callback_handler,
    get_llm_factory,
)

logger = logging.getLogger(__name__)


@hugging_face_exception_handler(provider='HuggingFaceTGI')
@openai_exception_handler(provider='OpenAI or AzureOpenAIService')
async def generate_and_split_sentences(
    query: SentenceGenerationQuery,
) -> SentenceGenerationResponse:
    """
    Generate sentences using a language model based on the provided query,
    and split the generated content into a list of sentences using a specific parser.

    :param query: A GenerateSentencesQuery object containing the llm setting.
    :return: A GenerateSentencesResponse object containing the list of sentences.
    """
    logger.info('Prompt completion - Start of execution...')
    start_time = time.time()

    logger.info('Prompt completion - template validation')
    validate_prompt_template(query.prompt)

    parser = NumberedListOutputParser()
    prompt = LangChainPromptTemplate.from_template(
        template=query.prompt.template,
        template_format=query.prompt.formatter.value,
        partial_variables={'format_instructions': parser.get_format_instructions()},
    )
    model = get_llm_factory(query.llm_setting).get_language_model()

    chain = prompt | model | parser

    config = None
    # Create a RunnableConfig containing the observability callback handler
    if query.observability_setting is not None:
        config = {
            'callbacks': [
                create_observability_callback_handler(
                    observability_setting=query.observability_setting,
                    trace_name=ObservabilityTrace.SENTENCE_GENERATION,
                )
            ]
        }

    sentences = await chain.ainvoke(query.prompt.inputs, config=config)

    logger.info(
        'Prompt completion - End of execution. (Duration : %.2f seconds)',
        time.time() - start_time,
    )

    return SentenceGenerationResponse(sentences=sentences)


def validate_prompt_template(prompt: PromptTemplate):
    """
    Prompt template validation

    Args:
        prompt: The prompt template

    Returns:
        Nothing.
    Raises:
        GenAIPromptTemplateException: if template is incorrect
    """
    if PromptFormatter.JINJA2 == prompt.formatter:
        try:
            Template(prompt.template).render(prompt.inputs)
        except TemplateError as exc:
            logger.error('Prompt completion - template validation failed!')
            logger.error(exc)
            raise GenAIPromptTemplateException(
                ErrorInfo(
                    error=exc.__class__.__name__,
                    cause=str(exc),
                )
            )

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
"""Module for the Large language Model Service"""

import logging

from langchain_core.messages import AIMessage
from langchain_core.output_parsers import BaseOutputParser

from gen_ai_orchestrator.models.llm.llm_types import LLMSetting
from gen_ai_orchestrator.models.observability.observability_trace import ObservabilityTrace
from gen_ai_orchestrator.routers.requests.requests import LLMProviderSettingStatusQuery
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    get_llm_factory, get_callback_handler_factory,
)
from gen_ai_orchestrator.services.langchain.factories.llm.llm_factory import (
    LangChainLLMFactory,
)

logger = logging.getLogger(__name__)


async def check_llm_setting(query: LLMProviderSettingStatusQuery) -> bool:
    """
    Run a check for a given LLM setting.

    Args:
        query: The query for the LLM Provider Setting Status

    Returns:
         True for a valid LLM setting. Raise exception otherwise.
    """

    logger.info('Get the LLM Factory, then check the LLM setting.')
    langfuse_callback_handler = None
    if query.observability_setting is not None:
        langfuse_callback_handler = get_callback_handler_factory(
            setting=query.observability_setting).get_callback_handler(
            trace_name=ObservabilityTrace.CHECK_LLM_SETTINGS.value)

    return await get_llm_factory(query.setting).check_llm_setting(langfuse_callback_handler)


def llm_inference_with_parser(
    llm_factory: LangChainLLMFactory, parser: BaseOutputParser
) -> AIMessage:
    """
    Perform LLM inference and format the output content based on the given parser.

    :param llm_factory: LangChain LLM Factory.
    :param parser: Parser to format the output.

    :return: Result of the language model inference with the content formatted.
    """

    # Change the prompt with added format instructions
    format_instructions = parser.get_format_instructions()
    formatted_prompt = llm_factory.setting.prompt + '\n' + format_instructions

    # Inference of the LLM with the formatted prompt
    llm_output = llm_factory.invoke(formatted_prompt)

    # Apply the parsing on the LLM output
    llm_output.content = parser.parse(llm_output.content)

    return llm_output

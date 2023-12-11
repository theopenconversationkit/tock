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
from langchain.output_parsers import CommaSeparatedListOutputParser

from gen_ai_orchestrator.routers.requests.requests import GenerateSentencesQuery
from gen_ai_orchestrator.routers.responses.responses import (
    GenerateSentencesResponse,
)
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    get_llm_factory,
)
from gen_ai_orchestrator.services.llm.llm_service import llm_inference_with_parser


def generate_and_split_sentences(
    query: GenerateSentencesQuery,
) -> GenerateSentencesResponse:
    """
    Generate sentences using a language model based on the provided query,
    and split the generated content into a list of sentences using a specific parser.

    :param query: A GenerateSentencesQuery object containing the llm setting.
    :return: A GenerateSentencesResponse object containing the list of sentences.
    """

    parser = CommaSeparatedListOutputParser()
    llm_output = llm_inference_with_parser(
        llm_factory=get_llm_factory(query.llm_setting), parser=parser
    )

    return GenerateSentencesResponse(sentences=llm_output.content)

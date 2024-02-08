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

from langchain.output_parsers import ResponseSchema, StructuredOutputParser
from langchain_core.prompts import PromptTemplate

from gen_ai_orchestrator.routers.requests.requests import GenerateSentencesQuery
from gen_ai_orchestrator.routers.responses.responses import (
    GenerateSentencesResponse,
)
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    get_llm_factory,
)


def generate_and_split_sentences(
        query: GenerateSentencesQuery,
) -> GenerateSentencesResponse:
    """
    Generate sentences using a language model based on the provided query,
    and split the generated content into a list of sentences using a specific parser.

    :param query: A GenerateSentencesQuery object containing the llm setting.
    :return: A GenerateSentencesResponse object containing the list of sentences.
    """
    response_schemas = [
        ResponseSchema(name="sentences", description="the list of generated sentences"),

    ]
    parser = StructuredOutputParser.from_response_schemas(response_schemas)

    llm_factory = get_llm_factory(query.llm_setting)
    # validate_jinja2(template, input_variables=["input_language", "content"])
    # il faudrait faire la validation mais la remonté d'erreur se fait via log et c pas ouf
    #  parser = CommaSeparatedListOutputParser()
    format_instructions = parser.get_format_instructions()
    prompt = PromptTemplate.from_template(query.prompt.template,
                                          partial_variables={"format_instructions": format_instructions},
                                          template_format=query.prompt.formatter)
    model = llm_factory.get_language_model()

    chain = prompt | model | parser
    response = chain.invoke(query.prompt.inputs)

    return GenerateSentencesResponse(sentences=response['sentences'])

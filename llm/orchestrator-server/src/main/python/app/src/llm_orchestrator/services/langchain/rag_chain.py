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
from llm_orchestrator.models.rag.rag_models import Footnote, TextWithFootnotes
from llm_orchestrator.routers.requests.requests import RagQuery
from llm_orchestrator.routers.responses.responses import RagResponse
from llm_orchestrator.services.langchain.factories.langchain_factory import (
    get_em_factory,
    get_llm_factory,
)


def execute_qa_chain(query: RagQuery, debug: bool) -> RagResponse:
    llm_factory = get_llm_factory(query.question_answering_llm_setting)
    em_factory = get_em_factory(query.embedding_question_em_setting)

    # Instantiate LangChain, using :
    # llm_factory.get_language_model()
    # em_factory.get_embedding_model()

    # Fake answer
    return RagResponse(
        answer=TextWithFootnotes(
            text='{} - {} - {}'.format(
                llm_factory.get_language_model(),
                em_factory.get_embedding_model(),
                debug,
            ),
            footnotes=[
                Footnote(
                    identifier='1',
                    title='Tock Documentation',
                    url='https://doc.tock.ai/tock',
                ),
                Footnote(identifier='2', title='Other source'),
            ],
        ),
        debug=[],
    )

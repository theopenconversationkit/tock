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
from llm_orchestrator.routers.requests.requests import RagQuery
from llm_orchestrator.routers.responses.responses import RagResponse
from llm_orchestrator.services.langchain.rag_chain import execute_qa_chain


def qa_ask(query: RagQuery, debug: bool) -> RagResponse:
    return execute_qa_chain(query, debug)

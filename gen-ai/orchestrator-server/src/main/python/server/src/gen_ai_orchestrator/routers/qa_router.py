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
"""QA Router Module"""

from fastapi import APIRouter

from gen_ai_orchestrator.routers.requests.requests import QAQuery
from gen_ai_orchestrator.routers.responses.responses import QAResponse
from gen_ai_orchestrator.services.qa.qa_service import qa

qa_router = APIRouter(prefix='/qa', tags=['Question Answering'])


@qa_router.post('')
async def ask_qa(query: QAQuery) -> QAResponse:
    """
    ## Ask a QA System
    Ask question to a QA System, and return sources founded in a knowledge base (documents)
    """
    return await qa(query)

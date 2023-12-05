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
from typing import Any

from pydantic import BaseModel, Field

from llm_orchestrator.models.em.em_provider import EMProvider
from llm_orchestrator.models.llm.llm_provider import LLMProvider
from llm_orchestrator.models.rag.rag_models import TextWithFootnotes


class LLMProviderResponse(BaseModel):
    provider: LLMProvider


class EMProviderResponse(BaseModel):
    provider: EMProvider


class RagResponse(BaseModel):
    answer: TextWithFootnotes = Field(
        description='The RAF answer, with outside sources'
    )
    debug: list[Any] = Field(
        description='Debug data',
        examples=[{'action': 'retrieve', 'result': 'OK', 'errors': []}],
    )

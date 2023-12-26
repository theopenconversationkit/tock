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

from pydantic import BaseModel, Field

from llm_orchestrator.models.em.em_types import EMSetting
from llm_orchestrator.models.llm.llm_types import LLMSetting
from llm_orchestrator.models.rag.rag_models import ChatMessage, MetadataFilter


class LLMProviderSettingStatusQuery(BaseModel):
    setting: LLMSetting = Field(description='The LLM provider setting to be checked.')


class EMProviderSettingStatusQuery(BaseModel):
    setting: EMSetting = Field(
        description='The Embedding Model provider setting to be checked.'
    )


class RagQuery(BaseModel):
    question: str = Field(
        description='The user question.',
        examples=['How to get started playing guitar ?'],
    )
    history: list[ChatMessage] = Field(
        description="Conversation history, used to reformulate the user's question."
    )
    # condense_question_llm_setting: LLMSetting =
    #   Field(description="LLM setting, used to condense the user's question.")
    question_answering_llm_setting: LLMSetting = Field(
        description='LLM setting, used to perform a QA Prompt.'
    )
    embedding_question_em_setting: EMSetting = Field(
        description="Embedding model setting, used to calculate the user's question vector."
    )
    index_name: str = Field(
        description='Index name corresponding to a document collection in the vector database.',
        examples=['my-index-name'],
    )
    metadata_filters: list[MetadataFilter] = Field(
        description='Metadata filters, used to search for specific documents. The AND operator is applied, '
        'so all filters should be satisfied.'
    )

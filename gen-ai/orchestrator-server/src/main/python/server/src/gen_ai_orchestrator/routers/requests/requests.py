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
"""Module for Request Models"""

from typing import Any, Optional

from pydantic import BaseModel, Field

from gen_ai_orchestrator.models.em.em_types import EMSetting
from gen_ai_orchestrator.models.llm.llm_types import LLMSetting
from gen_ai_orchestrator.models.observability.observability_type import ObservabilitySetting
from gen_ai_orchestrator.models.prompt.prompt_template import PromptTemplate
from gen_ai_orchestrator.models.rag.rag_models import ChatMessage
from gen_ai_orchestrator.models.vector_stores.vector_store_types import VectorStoreSetting, DocumentSearchParams


class LLMProviderSettingStatusQuery(BaseModel):
    """The query for the LLM Provider Setting Status"""

    setting: LLMSetting = Field(description='The LLM Provider setting to be checked.')
    observability_setting: Optional[ObservabilitySetting] = Field(
        description='The observability settings', default=None
    )


class EMProviderSettingStatusQuery(BaseModel):
    """The query for the EM Provider Setting Status"""

    setting: EMSetting = Field(
        description='The Embedding Model Provider setting to be checked.'
    )


class ObservabilityProviderSettingStatusQuery(BaseModel):
    """The query for the Observability Provider Setting Status"""

    setting: ObservabilitySetting = Field(
        description='The Observability Provider setting to be checked.'
    )


class BaseQuery(BaseModel):
    """The Base query model"""

    embedding_question_em_setting: EMSetting = Field(
        description="Embedding model setting, used to calculate the user's question vector."
    )
    document_index_name: str = Field(
        description='Index name corresponding to a document collection in the vector database.',
    )
    document_search_params: DocumentSearchParams = Field(
        description='The document search parameters. Ex: number of documents, metadata filter',
    )
    vector_store_setting: Optional[VectorStoreSetting] = Field(
        description='The vector store settings.',
        default=None
    )
    observability_setting: Optional[ObservabilitySetting] = Field(
        description='The observability settings.', default=None
    )


class QAQuery(BaseQuery):
    user_query: str = Field(
        description="The user's request. Will be sent as is to the model."
    )

    model_config = {
        'json_schema_extra': {
            'examples': [
                {
                    'embedding_question_em_setting': {
                        'provider': 'OpenAI',
                        'api_key': {
                            'type': 'Raw',
                            'value': 'ab7***************************A1IV4B',
                        },
                        'model': 'text-embedding-ada-002',
                    },
                    'user_query': 'How to get started playing guitar ?',
                    'document_index_name': 'my-index-name',
                    'document_search_params': {
                        'provider': 'OpenSearch',
                        'filter': [
                            {
                                'term': {
                                    'metadata.index_session_id.keyword': '352d2466-17c5-4250-ab20-d7c823daf035'
                                }
                            }
                        ],
                        'k': 4,
                    },
                }
            ]
        }
    }


class VectorStoreProviderSettingStatusQuery(BaseModel):
    """The query for the Vector Store Provider Setting Status"""

    vector_store_setting: Optional[VectorStoreSetting] = Field(
        description='The Vector Store Provider setting to be checked.',
        default=None
    )
    em_setting: Optional[EMSetting] = Field(
        description="Embedding model setting, used to calculate the user's question vector.",
        default=None
    )
    document_index_name: Optional[str] = Field(
        description='Index name corresponding to a document collection in the vector database.',
        default=None
    )


class RagQuery(BaseQuery):
    """The RAG query model"""

    history: list[ChatMessage] = Field(
        description="Conversation history, used to reformulate the user's question."
    )
    question_answering_prompt_inputs: Any = Field(
        description='Key-value inputs for the llm prompt when used as a template. Please note that the '
        'chat_history field must not be specified here, it will be override by the history field',
    )
    # condense_question_llm_setting: LLMSetting =
    #   Field(description="LLM setting, used to condense the user's question.")
    # condense_question_prompt_inputs: Any = (
    #         Field(
    #             description='Key-value inputs for the condense question llm prompt, when used as a template.',
    #         ),
    #     )
    question_answering_llm_setting: LLMSetting = Field(
        description='LLM setting, used to perform a QA Prompt.'
    )

    model_config = {
        'json_schema_extra': {
            'examples': [
                {
                    'history': [
                        {'text': 'Hello, how can I do this?', 'type': 'HUMAN'},
                        {
                            'text': 'you can do this with the following method ....',
                            'type': 'AI',
                        },
                    ],
                    'question_answering_llm_setting': {
                        'provider': 'OpenAI',
                        'api_key': {
                            'type': 'Raw',
                            'value': 'ab7***************************A1IV4B',
                        },
                        'temperature': 1.2,
                        'prompt': """Use the following context to answer the question at the end.
If you don't know the answer, just say {no_answer}.

Context:
{context}

Question:
{question}

Answer in {locale}:""",
                        'model': 'gpt-3.5-turbo',
                    },
                    'question_answering_prompt_inputs': {
                        'question': 'How to get started playing guitar ?',
                        'no_answer': "Sorry, I don't know.",
                        'locale': 'French',
                    },
                    'embedding_question_em_setting': {
                        'provider': 'OpenAI',
                        'api_key': {
                            'type': 'Raw',
                            'value': 'ab7***************************A1IV4B',
                        },
                        'model': 'text-embedding-ada-002',
                    },
                    'document_index_name': 'my-index-name',
                    'document_search_params': {
                        'provider': 'OpenSearch',
                        'filter': [
                            {
                                'term': {
                                    'metadata.index_session_id.keyword': '352d2466-17c5-4250-ab20-d7c823daf035'
                                }
                            }
                        ],
                        'k': 4,
                    },
                    'observability_setting': None,
                    'vector_store_setting': None,
                }
            ]
        }
    }


class SentenceGenerationQuery(BaseModel):
    """The sentence generation query model"""

    llm_setting: LLMSetting = Field(
        description='LLM setting, used to perform a sentences generation.'
    )
    prompt: PromptTemplate = Field(
        description='Prompt, used to create prompt with inputs and jinja template '
    )
    observability_setting: Optional[ObservabilitySetting] = Field(
        description='The observability settings.', default=None
    )

#   Copyright (C) 2023-2025 Credit Mutuel Arkea
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

from pyasn1.type.univ import Boolean
from pydantic import BaseModel, Field

from gen_ai_orchestrator.models.document_compressor.document_compressor_types import DocumentCompressorSetting
from gen_ai_orchestrator.models.em.em_types import EMSetting
from gen_ai_orchestrator.models.guardrail.guardrail_types import (
    GuardrailSetting,
)
from gen_ai_orchestrator.models.llm.llm_types import LLMSetting
from gen_ai_orchestrator.models.observability.observability_type import (
    ObservabilitySetting,
)
from gen_ai_orchestrator.models.prompt.prompt_template import PromptTemplate
from gen_ai_orchestrator.models.rag.rag_models import ChatMessage
from gen_ai_orchestrator.models.vector_stores.vector_store_types import (
    DocumentSearchParams,
    VectorStoreSetting,
)


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


class DocumentCompressorProviderSettingStatusQuery(BaseModel):
    """The query for the Document Compressor Provider Setting Status"""

    setting: DocumentCompressorSetting = Field(description='The Document Compressor Provider setting to be checked.')


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
        description='The vector store settings.', default=None
    )
    observability_setting: Optional[ObservabilitySetting] = Field(
        description='The observability settings.', default=None
    )
    compressor_setting: Optional[DocumentCompressorSetting] = Field(
        description='Compressor settings, to rerank relevant documents returned by retriever.',
        default=None,
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
                            'secret': 'ab7***************************A1IV4B',
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
        description='The Vector Store Provider setting to be checked.', default=None
    )
    em_setting: Optional[EMSetting] = Field(
        description="Embedding model setting, used to calculate the user's question vector.",
        default=None,
    )
    document_index_name: Optional[str] = Field(
        description='Index name corresponding to a document collection in the vector database.',
        default=None,
    )

class DialogDetails(BaseModel):
    """The dialog details model"""

    dialog_id: Optional[str] = Field(
        description="The dialog/session ID, attached to the observability traces if "
                    "the observability provider support it.",
        default=None, examples=["uuid-0123"])
    user_id: Optional[str] = Field(
        description="The user ID, attached to the observability traces if the observability provider support it",
        default=None, examples=["address@mail.com"])
    history: list[ChatMessage] = Field(
        description="Conversation history, used to reformulate the user's question.")
    tags: list[str] = Field(
        description='List of tags, attached to the observability trace, if the observability provider support it.',
        examples=[["my-Tag"]])


class RagQuery(BaseQuery):
    """The RAG query model"""

    dialog: Optional[DialogDetails] = Field(description='The user dialog details.')
    # condense_question_llm_setting: LLMSetting =
    #   Field(description="LLM setting, used to condense the user's question.")
    # condense_question_prompt: PromptTemplate = Field(
    #         description='Prompt template, used to create a prompt with inputs for jinja and fstring format'
    #     )
    question_answering_llm_setting: LLMSetting = Field(
        description='LLM setting, used to perform a QA Prompt.'
    )
    question_answering_prompt : PromptTemplate = Field(
        description='Prompt template, used to create a prompt with inputs for jinja and fstring format'
    )
    guardrail_setting: Optional[GuardrailSetting] = Field(
        description='Guardrail settings, to classify LLM output toxicity.', default=None
    )
    documents_required: Optional[bool] = Field(
        description='Specifies whether the presence of documents is mandatory for generating answers. '
                    'If set to True, the system will only provide answers when relevant documents are found. '
                    'If set to False, the system can respond without requiring document sources. Default is True.',
        default=True,
    )

    model_config = {
        'json_schema_extra': {
            'examples': [
                {
                    'dialog' : {
                        'history': [
                            {'text': 'Hello, how can I do this?', 'type': 'HUMAN'},
                            {
                                'text': 'you can do this with the following method ....',
                                'type': 'AI',
                            },
                        ]
                    },
                    'question_answering_llm_setting': {
                        'provider': 'OpenAI',
                        'api_key': {
                            'type': 'Raw',
                            'secret': 'ab7***************************A1IV4B',
                        },
                        'temperature': 1.2,
                        'model': 'gpt-3.5-turbo',
                    },
                    'question_answering_prompt': {
                        'formatter': 'f-string',
                        'template': """Use the following context to answer the question at the end.
If you don't know the answer, just say {no_answer}.

Context:
{context}

Question:
{question}

Answer in {locale}:""",
                        'inputs': {
                            'question': 'How to get started playing guitar ?',
                            'no_answer': 'Sorry, I don t know.',
                            'locale': 'French',
                        }
                    },
                    'embedding_question_em_setting': {
                        'provider': 'OpenAI',
                        'api_key': {
                            'type': 'Raw',
                            'secret': 'ab7***************************A1IV4B',
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
                    'guardrail_setting': {
                        'provider': 'BloomzGuardrail',
                        'api_base': 'https://*********',
                        'max_score': 0.3,
                    },
                    'compressor_setting': {
                        'provider': 'BloomzRerank',
                        'min_score': 0.7,
                        'max_documents': 10,
                        'label': 'entailment',
                        'endpoint': 'https://*********',
                    },
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

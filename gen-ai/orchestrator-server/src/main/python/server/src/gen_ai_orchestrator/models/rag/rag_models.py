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
"""Module for RAG Models"""

from enum import Enum, unique
from typing import List, Optional


from pydantic import AnyUrl, BaseModel, Field, HttpUrl

from gen_ai_orchestrator.models.vector_stores.vector_store_types import DocumentSearchParams


class Source(BaseModel):
    """A source model, used to associate document sources with the QA response"""

    title: str = Field(description='Source title', examples=['Tock Documentation'])
    url: Optional[AnyUrl] = Field(
        description='Source url', examples=['https://doc.tock.ai/tock/'], default=None
    )
    content: str = Field(
        description='Source content', examples=['Tock: The Open Conversation Kit']
    )

    def __eq__(self, other):
        """
        Source are identified by their title and URL.
        When the identifier or the content are not the same for identical sources,
        this means that the sources are parts of the same document.
        """
        return (
            isinstance(other, Source)
            and self.title == other.title
            and self.url == other.url
            and self.content == other.content
        )

    def __hash__(self):
        return hash((self.title, self.url, self.content))


class Footnote(Source):
    """A footnote model, used to associate document sources with the RAG answer"""

    identifier: str = Field(description='Footnote identifier', examples=['1'])


class TextWithFootnotes(BaseModel):
    """Text with its footnotes. Used for RAG response"""

    text: str = Field(
        description='Text with footnotes used to list outside sources',
        examples=['This is page content [1], and this is more content [2]'],
    )
    footnotes: set[Footnote] = Field(description='Set of footnotes')


@unique
class ChatMessageType(str, Enum):
    """Enumeration to list a chat message type"""

    HUMAN = 'HUMAN'
    AI = 'AI'


class ChatMessage(BaseModel):
    """A conversation chat message"""

    text: str = Field(
        description='Conversation message text', examples=['Hello, how can I do this?']
    )
    type: ChatMessageType = Field(description='The message origin (Human or AI)')


class RagDocumentMetadata(BaseModel):
    """The RAG document metadata"""

    index_session_id: str = Field(
        description='The indexing session id.', examples=['123f-ed01-gt21-gg08']
    )
    id: str = Field(description='The document id.', examples=['e014-g24-0f11-1g3e'])
    title: str = Field(description='The document title.', examples=['Tracking shot'])
    url: Optional[HttpUrl] = Field(
        description='The document url.',
        examples=['https://en.wikipedia.org/wiki/Tracking_shot'],
        default=None,
    )
    chunk: str = Field(description='The document chunk.', examples=['1/3'])


class RagDocument(BaseModel):
    """The definition of RAG document"""

    content: str = Field(
        description='The document content.',
        examples=[
            'In cinematography, a tracking shot is any shot where the camera follows backward, '
            'forward or moves alongside the subject being recorded.'
        ],
    )
    metadata: RagDocumentMetadata = Field(
        description='The document metadata.',
    )


class QADebugData(BaseModel):
    """A QA debug data. This class is not currently used in the QA chain as Langfuse is now supported."""

    user_question: Optional[str] = Field(
        description="The user's initial question.",
        examples=["I'm interested in going to Morocco"],
    )
    documents: List[RagDocument] = Field(
        description='Documents retrieved from the vector store.'
    )
    document_index_name: str = Field(
        description='Index name corresponding to a document collection in the vector database.',
    )
    document_search_params: DocumentSearchParams = Field(
        description='The document search parameters. Ex: number of documents, metadata filter',
    )
    duration: float = Field(
        description='The duration of RAG in seconds.', examples=['7.2']
    )


class RagDebugData(QADebugData):
    """A RAG debug data"""

    condense_question_prompt: Optional[str] = Field(
        description='The prompt of the question rephrased with the history of the conversation.',
        examples=[
            """Given the following conversation and a follow up question,
        rephrase the follow up question to be a standalone question, in its original language.
        Chat History:
        Human: What travel offers are you proposing?
        Assistant: We offer trips to all of Europe and North Africa.
        Follow Up Input: I'm interested in going to Morocco
        Standalone question:"""
        ],
    )
    condense_question: Optional[str] = Field(
        description='The question rephrased with the history of the conversation.',
        examples=['Hello, how to plan a trip to Morocco ?'],
    )
    question_answering_prompt: Optional[str] = Field(
        description='The question answering prompt.',
        examples=[
            'Question: Hello, how to plan a trip to Morocco ?. Answer in French.'
        ],
    )
    answer: str = Field(description='The RAG answer.')

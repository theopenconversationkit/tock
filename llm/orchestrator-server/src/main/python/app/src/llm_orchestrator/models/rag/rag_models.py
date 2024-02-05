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
from typing import Optional

from pydantic import AnyUrl, BaseModel, Field


class Footnote(BaseModel):
    """A footnote model, used to associate document sources with the RAG answer"""

    identifier: str = Field(description='Footnote identifier', examples=['1'])
    title: str = Field(description='Footnote title', examples=['Tock Documentation'])
    url: Optional[AnyUrl] = Field(
        description='Footnote url', examples=['https://doc.tock.ai/tock/'], default=None
    )

    def __eq__(self, other):
        """
        Footnotes are identified by their title and URL.
        When the identifier is not the same for identical footnotes,
        this means that the sources are parts of the same document.
        """
        return (
            isinstance(other, Footnote)
            and self.title == other.title
            and self.url == other.url
        )

    def __hash__(self):
        return hash((self.title, self.url))


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

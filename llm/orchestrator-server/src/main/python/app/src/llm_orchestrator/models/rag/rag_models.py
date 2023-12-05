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
from enum import Enum, unique
from typing import Union, Optional

from pydantic import AnyUrl, BaseModel, Field


class Footnote(BaseModel):
    identifier: str = Field(description='Footnote identifier', examples=['1'])
    title: str = Field(description='Footnote title', examples=['Tock Documentation'])
    url: Optional[AnyUrl] = Field(
        description='Footnote url', examples=['https://doc.tock.ai/tock/'], default=None
    )


class TextWithFootnotes(BaseModel):
    text: str = Field(
        description='Text with footnotes used to list outside sources',
        examples=['This is page content [1], and this is more content [2]'],
    )
    footnotes: list[Footnote] = Field(description='List of footnotes')


@unique
class ChatMessageType(str, Enum):
    USER = 'HUMAN'
    AI = 'AI'


class ChatMessage(BaseModel):
    text: str = Field(
        description='Conversation message text', examples=['Hello, how can I do this?']
    )
    type: ChatMessageType = Field(description='The message origin (Human or AI)')


class MetadataFilter(BaseModel):
    name: str = Field(description='Metadata filter name', examples=['bot_id'])
    value: str = Field(description='Metadata filter value', examples=['my-bot'])

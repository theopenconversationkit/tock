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
"""Model for creating Credentials."""

from pydantic import BaseModel, Field


class Credentials(BaseModel):
    """The basic credentials"""

    username: str = Field(
        description='The username.',
        examples=['nt123']
    )
    password: str = Field(
        description='The password.',
        examples=['a12G-3@p!'],
        min_length=4
    )

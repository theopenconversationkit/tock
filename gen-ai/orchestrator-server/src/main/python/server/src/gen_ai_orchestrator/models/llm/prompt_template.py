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
from typing import Literal

from pydantic import BaseModel, Field


class PromptTemplate(BaseModel):
    formatter: Literal['f-string', 'jinja2'] = Field(
        description='The formatter of this prompt.',
        examples=['jinja2'],
    )
    template: str = Field(
        description='The Jinja2 Template for create a prompt.',

     )
    inputs: dict = Field(
        description='inputs for generation of prompt with the template',

    )

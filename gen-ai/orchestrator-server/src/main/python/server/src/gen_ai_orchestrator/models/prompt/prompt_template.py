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
"""Model for creating PromptTemplate."""

from pydantic import BaseModel, Field

from gen_ai_orchestrator.models.prompt.prompt_formatter import PromptFormatter


class PromptTemplate(BaseModel):
    """A prompt template model, used to specify a formatter"""

    formatter: PromptFormatter = Field(
        description='The formatter of this prompt.',
        examples=[PromptFormatter.JINJA2],
    )
    template: str = Field(
        description='The Jinja2 Template for create a prompt.',
    )
    inputs: dict = Field(
        description='inputs for generation of prompt with the template',
    )

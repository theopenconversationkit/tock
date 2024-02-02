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
        examples = ["""
            {% if OPTIONS.SPELLINGMISTAKES %}
            include sentences with spelling mistakes
            {% endif %}
            {% if OPTIONS.SMSLANGUAGE %}
            include sentences with sms language
            {% endif %}
            {% if OPTIONS.ABBREVIATEDLANGUAGE %}
            include sentences with abbreviated language
            {% endif %}
            Takes into account the previous options and generates in {{ LOCAL }} language, {{ NB_SENTENCES }} sentences derived from the sentences in the following table:
            {% for sentence in SENTENCES %}
            {{ sentence }}
            {% endfor %}"""],
     )
    inputs: dict = Field(
        description='inputs for generation of prompt with the template',
        examples=[{
         "LOCAL": "français",
         "NB_SENTENCES": " nombre de phrases à générer",
         "SENTENCES": ["comment ouvrir un livret A", "donnes moi des informations sur l ouverture d un livret A"],
         "OPTIONS": {"SPELLINGMISTAKES": True,
                     "SMSLANGUAGE": True,
                     "ABBREVIATEDLANGUAGE": True,
            }
        }]
    )

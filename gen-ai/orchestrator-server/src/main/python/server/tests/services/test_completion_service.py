#   Copyright (C) 2024-2026 Credit Mutuel Arkea
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
from gen_ai_orchestrator.models.prompt.prompt_template import PromptTemplate
from gen_ai_orchestrator.services.utils.prompt_utility import (
    validate_prompt_template,
)


def test_validate_prompt_template():
    json = {
        'formatter': 'f-string',
        'template': 'Options:\n{% if options.spelling_mistakes %}- include sentences with spelling mistakes{% endif %}{% if options.sms_language %}- include sentences with sms language{% endif %}\nQuestion: \nTakes into account the previous options and generates in {{ local }} language, {{ nb_sentences }} sentences derived from the sentences in the following list:\n{% for sentence in sentences %}- {{ sentence }}\n{% endfor %}',
        'inputs': {
            'options': {'spelling_mistakes': True, 'sms_language': True},
            'local': 'French',
            'nb_sentences': 5,
            'sentences': ["j'ai faim.", "J'ai envie de manger"],
        },
    }
    template = PromptTemplate(**json)
    validate_prompt_template(template, 'prompt_name')

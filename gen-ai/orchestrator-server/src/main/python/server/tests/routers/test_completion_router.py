#   Copyright (C) 2023-2026 Credit Mutuel Arkea
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
from fastapi.testclient import TestClient

from gen_ai_orchestrator.main import app
from gen_ai_orchestrator.models.errors.errors_models import ErrorCode

client = TestClient(app)


def test_generate_sentences():
    response = client.post(
        '/completion/sentences',
        json={
            'llm_setting': {
                'provider': 'FakeLLM',
                'api_key': {
                    'type': 'Raw',
                    'secret': 'ab7***************************A1IV4B',
                },
                'model': 'dddddd',
                'temperature': '0.0',
                'responses': ['1. vanilla\n2. chocolate\n3. strawberry'],
            },
            'prompt': {
                'formatter': 'jinja2',
                'template': 'Options:\n{% if options.spelling_mistakes %}- include sentences with spelling mistakes{% endif %}{% if options.sms_language %}- include sentences with sms language{% endif %}\nQuestion: \nTakes into account the previous options and generates in {{ local }} language, {{ nb_sentences }} sentences derived from the sentences in the following list:\n{% for sentence in sentences %}- {{ sentence }}\n{% endfor %}',
                'inputs': {
                    'options': {'spelling_mistakes': True, 'sms_language': True},
                    'local': 'French',
                    'nb_sentences': 5,
                    'sentences': ["j'ai faim.", "J'ai envie de manger"],
                },
            },
        },
    )

    assert response.status_code == 200
    assert response.json() == {'sentences': ['vanilla', 'chocolate', 'strawberry']}


def test_generate_sentences_template_error():
    response = client.post(
        '/completion/sentences',
        json={
            'llm_setting': {
                'provider': 'FakeLLM',
                'api_key': {
                    'type': 'Raw',
                    'secret': 'ab7***************************A1IV4B',
                },
                'model': 'dddddd',
                'temperature': '0.0',
                'responses': ['1. vanilla\n2. chocolate\n3. strawberry'],
            },
            'prompt': {
                'formatter': 'jinja2',
                'template': 'Options:\n% if options.spelling_mistakes %}- include sentences with spelling mistakes{% endif %}{% if options.sms_language %}- include sentences with sms language{% endif %}\nQuestion: \nTakes into account the previous options and generates in {{ local }} language, {{ nb_sentences }} sentences derived from the sentences in the following list:\n{% for sentence in sentences %}- {{ sentence }}\n{% endfor %}',
                'inputs': {
                    'options': {'spelling_mistakes': True, 'sms_language': True},
                    'local': 'French',
                    'nb_sentences': 5,
                    'sentences': ["j'ai faim.", "J'ai envie de manger"],
                },
            },
        },
    )

    assert response.status_code == 400
    error = response.json()
    assert error['code'] == ErrorCode.GEN_AI_PROMPT_TEMPLATE_ERROR.value

def test_generate():
    response = client.post(
        '/completion/',
        json={
            'llm_setting': {
                'provider': 'FakeLLM',
                'api_key': {
                    'type': 'Raw',
                    'secret': 'ab7***************************A1IV4B',
                },
                'model': 'dddddd',
                'temperature': '0.0',
                'responses': ['Hi! Im a fake LLM'],
            },
            'prompt': {
                'formatter': 'jinja2',
                'template': '',
                'inputs': {},
            },
            'observability_setting': None
        },
    )

    assert response.status_code == 200
    assert response.json() == {'answer': 'Hi! Im a fake LLM', 'observability_info': None}
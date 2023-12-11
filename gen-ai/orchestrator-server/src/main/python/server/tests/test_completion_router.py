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
from fastapi.testclient import TestClient
from gen_ai_orchestrator.main import app

client = TestClient(app)


def test_generate_sentences():
    # Importing the client here avoid circular import

    response = client.post(
        '/completion/generate-sentences',
        json={
            'llm_setting': {
                'provider': 'FakeLLM',
                'api_key': '',
                'model': '',
                'temperature': '0.0',
                'prompt': 'List 3 ice cream flavors.',
                'responses': ['vanilla, chocolate, strawberry'],
            }
        },
    )

    assert response.status_code == 200
    assert response.json() == {'sentences': ['vanilla', 'chocolate', 'strawberry']}

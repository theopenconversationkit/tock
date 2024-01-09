#   Copyright (C) 2024 Credit Mutuel Arkea
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
from gen_ai_orchestrator.models.document_compressor.document_compressor_provider import DocumentCompressorProvider
from gen_ai_orchestrator.models.errors.errors_models import (
    ErrorCode,
    ErrorMessages,
)

client = TestClient(app)

urls_prefix = '/document-compressor-providers'


def test_get_all_document_compressor_providers():
    """Test getting all Vector Store Providers."""
    response = client.get(f'{urls_prefix}')
    assert response.status_code == 200
    assert len(response.json()) == len(DocumentCompressorProvider)


def test_get_document_compressor_provider_by_id():
    """Test getting provider by id (use first provider listed provider class)."""
    response = client.get(f'{urls_prefix}/{list(DocumentCompressorProvider)[0]}')
    assert response.status_code == 200
    assert response.json()['provider'] == list(DocumentCompressorProvider)[0]


def test_get_document_compressor_provider_by_id_wrong_id():
    """Test getting provider by id, with an id that does not exist."""
    response = client.get(f'{urls_prefix}/wrong_id')
    assert response.status_code == 400
    assert (
        response.json()['message']
        == ErrorMessages().get_message(ErrorCode.DOCUMENT_COMPRESSOR_UNKNOWN_PROVIDER).message
    )


def test_get_document_compressor_provider_setting_by_id():
    """Test getting provider setting example for id (use first provider listed provider class)."""
    response = client.get(f'{urls_prefix}/{list(DocumentCompressorProvider)[0]}/setting/example')
    assert response.status_code == 200
    assert response.json()['provider'] == list(DocumentCompressorProvider)[0]


def test_check_document_compressor_provider_setting():
    """Test checking a provider setting (use example for checking)."""
    response = client.get(f'{urls_prefix}/{list(DocumentCompressorProvider)[0]}/setting/example')
    data = {'setting': response.json()}
    response = client.post(
        f'{urls_prefix}/{list(DocumentCompressorProvider)[0]}/setting/status', json=data
    )
    assert response.status_code == 200
    assert response.json()['valid'] == False

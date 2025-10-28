#   Copyright (C) 2024-2025 Credit Mutuel Arkea
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
from unittest.mock import MagicMock, patch

import pytest
from botocore.exceptions import ClientError

from gen_ai_orchestrator.models.security.ai_provider_secret import (
    AIProviderSecret,
)
from gen_ai_orchestrator.models.security.credentials import Credentials
from gen_ai_orchestrator.utils.aws.aws_secrets_manager_client import (
    AWSSecretsManagerClient,
)
from gen_ai_orchestrator.utils.secret_manager.secret_manager_client import (
    parse_secret_data,
)


@patch('boto3.client')
def test_aws_secrets_manager_get_secret_no_secret(mocked_boto3_client):
    _error_response = {'Error': {'Code': 'ResourceNotFoundException'}}
    _error = ClientError(
        error_response=_error_response, operation_name='operation name'
    )
    mocked_boto3_client.return_value.get_secret_value = MagicMock(side_effect=_error)

    with pytest.raises(Exception):
        AWSSecretsManagerClient().get_secret('secret name')


@patch('boto3.client')
def test_aws_secrets_manager_get_secret_fails(mocked_boto3_client):
    mocked_boto3_client.return_value.get_secret_value = MagicMock(
        side_effect=Exception()
    )

    with pytest.raises(Exception):
        AWSSecretsManagerClient().get_secret('secret name')


def test_parse_to_credentials():
    json_str = '{"username": "bob","password": "alice123!"}'
    credentials = parse_secret_data(json_str, Credentials)
    assert credentials.username == 'bob'
    assert credentials.password == 'alice123!'


def test_parse_to_credentials_fails():
    invalid_json_str = "{'user':'bob'}"
    credentials = parse_secret_data(invalid_json_str, Credentials)
    assert credentials is None


def test_parse_to_ai_provider_secret():
    json_str = '{"secret": "my_ai_secret"}'
    ai_provider_secret = parse_secret_data(json_str, AIProviderSecret)
    assert ai_provider_secret.secret == 'my_ai_secret'


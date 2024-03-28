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
from unittest.mock import MagicMock, patch

import pytest
from botocore.exceptions import ClientError

from gen_ai_orchestrator.utils.aws.aws_secret_manager import (
    AWSSecretManager,
    _parse_to_credentials,
)
from gen_ai_orchestrator.utils.strings import obfuscate


def test_obfuscate():
    obfuscated_str = obfuscate('stg')
    assert obfuscated_str == '*****'
    obfuscated_str = obfuscate('something')
    assert obfuscated_str == 'so******g'


@patch('gen_ai_orchestrator.utils.aws.aws_secret_manager.boto3.client')
@patch('gen_ai_orchestrator.utils.aws.aws_secret_manager._parse_to_credentials')
def test_aws_secrets_manager_get_credentials(mocked_parse, mocked_boto3_client):
    secrets_manager = AWSSecretManager()
    mocked_boto3_client.assert_called_once_with(service_name='secretsmanager')
    secrets_manager.get_credentials('secret name')
    mocked_boto3_client.return_value.get_secret_value.assert_called_once_with(
        SecretId='secret name'
    )


@patch('gen_ai_orchestrator.utils.aws.aws_secret_manager.boto3.client')
def test_aws_secrets_manager_get_secret_no_secret(mocked_boto3_client):
    secrets_manager = AWSSecretManager()
    _error_response = {'Error': {'Code': 'ResourceNotFoundException'}}
    _error = ClientError(
        error_response=_error_response, operation_name='operation name'
    )
    mocked_boto3_client.return_value.get_secret_value = MagicMock(side_effect=_error)
    with pytest.raises(Exception):
        secrets_manager.get_secret('secret name')


@patch('gen_ai_orchestrator.utils.aws.aws_secret_manager.boto3.client')
def test_aws_secrets_manager_get_secret_fails(mocked_boto3_client):
    secrets_manager = AWSSecretManager()
    mocked_boto3_client.return_value.get_secret_value = MagicMock(
        side_effect=Exception()
    )
    with pytest.raises(Exception):
        secrets_manager.get_secret('secret name')


def test__parse_to_credentials():
    json_str = '{"username": "bob","password": "alice"}'
    user, pwd = _parse_to_credentials(json_str)
    assert user == 'bob'
    assert pwd == 'alice'


def test__parse_to_credentials_fails():
    invalid_json_str = "{'something is wrong'}"
    user, pwd = _parse_to_credentials(invalid_json_str)
    assert user == None
    assert pwd == None

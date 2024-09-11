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
from gen_ai_orchestrator.configurations.environment.settings import fetch_open_search_credentials, _Settings
from gen_ai_orchestrator.configurations.logging.logger import setup_logging
from gen_ai_orchestrator.models.security.credentials import Credentials
from unittest.mock import patch


def test_environment():
    """Test settings are read successfully"""
    _Settings()


def test_logging():
    """Test logger is instantiated successfully."""
    setup_logging()


@patch('boto3.client')
@patch('gen_ai_orchestrator.utils.aws.aws_secrets_manager_client.AWSSecretsManagerClient.get_credentials')
@patch('gen_ai_orchestrator.configurations.environment.settings.application_settings',
       _Settings(open_search_aws_secret_manager_name='my_secret_key'))
def test_fetch_open_search_credentials(mock_get_credentials, mock_boto3_client):
    # Test data
    open_search_credentials = Credentials(username="user", password="pwd123456")

    # Configure the mocks to return specific values
    mock_boto3_client.return_value = None
    mock_get_credentials.return_value = open_search_credentials


    # Call the function to fetch aws secret key
    username, password = fetch_open_search_credentials()

    # Check test results
    mock_boto3_client.assert_called_once_with(service_name='secretsmanager')
    mock_get_credentials.assert_called_once_with(secret_name='my_secret_key')
    assert username == open_search_credentials.username
    assert password == open_search_credentials.password


@patch('boto3.client')
@patch('gen_ai_orchestrator.utils.aws.aws_secrets_manager_client.AWSSecretsManagerClient.get_credentials')
@patch('gen_ai_orchestrator.configurations.environment.settings.application_settings',
       _Settings(open_search_aws_secret_manager_name='my_secret_key'))
def test_fetch_bad_open_search_credentials(mock_get_credentials, mock_boto3_client):
    # Test data
    open_search_credentials = None

    # Configure the mocks to return specific values
    mock_boto3_client.return_value = None
    mock_get_credentials.return_value = open_search_credentials

    # Call the function to fetch aws secret key
    username, password = fetch_open_search_credentials()

    # Check test results
    mock_boto3_client.assert_called_once_with(service_name='secretsmanager')
    mock_get_credentials.assert_called_once_with(secret_name='my_secret_key')
    assert username is None
    assert password is None


@patch('boto3.client')
@patch('gen_ai_orchestrator.utils.aws.aws_secrets_manager_client.AWSSecretsManagerClient.get_credentials')
@patch('gen_ai_orchestrator.configurations.environment.settings.application_settings',
       _Settings(open_search_user='default_user', open_search_pwd='default_pwd'))
def test_fetch_default_open_search_credentials(mock_get_credentials, mock_boto3_client):
    # Call the function to fetch aws secret key
    username, password = fetch_open_search_credentials()

    # Check test results
    assert not mock_boto3_client.called
    assert not mock_get_credentials.called
    assert username == 'default_user'
    assert password == 'default_pwd'


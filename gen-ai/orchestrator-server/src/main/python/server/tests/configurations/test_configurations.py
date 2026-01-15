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
from unittest.mock import patch

from gen_ai_orchestrator.configurations.environment.settings import (
    _Settings,
    application_settings,
)
from gen_ai_orchestrator.configurations.logging.logger import setup_logging
from gen_ai_orchestrator.models.security.credentials import Credentials
from gen_ai_orchestrator.models.vector_stores.vectore_store_provider import (
    VectorStoreProvider,
)
from gen_ai_orchestrator.utils.secret_manager.secret_manager_provider import (
    SecretManagerProvider,
)
from gen_ai_orchestrator.utils.secret_manager.secret_manager_service import (
    fetch_default_vector_store_credentials,
)


def test_environment():
    """Test settings are read successfully"""
    _Settings()


def test_logging():
    """Test logger is instantiated successfully."""
    setup_logging()


@patch('boto3.client')
@patch('gen_ai_orchestrator.utils.aws.aws_secrets_manager_client.AWSSecretsManagerClient.get_credentials')
@patch('gen_ai_orchestrator.utils.secret_manager.secret_manager_service.application_settings',
       _Settings(
           vector_store_provider = VectorStoreProvider.OPEN_SEARCH,
           vector_store_host = None,
           vector_store_port = None,
           vector_store_user = None,
           vector_store_pwd = None,
           vector_store_database = None,
           vector_store_secret_manager_provider = SecretManagerProvider.AWS,
           vector_store_credentials_secret_name = 'my_secret_key'
       )
)
def test_fetch_aws_secret_credentials(mock_get_credentials, mock_boto3_client):
    # Test data
    my_credentials = Credentials(username='user', password='pwd123456')

    # Configure the mocks to return specific values
    mock_boto3_client.return_value = None
    mock_get_credentials.return_value = my_credentials

    # Call the function to fetch the credentials of the default vector store
    credentials = fetch_default_vector_store_credentials()

    # Check test results
    mock_boto3_client.assert_called_once_with(service_name='secretsmanager')
    mock_get_credentials.assert_called_once_with('my_secret_key')
    assert credentials.username == my_credentials.username
    assert credentials.password == my_credentials.password

@patch('google.cloud.secretmanager.SecretManagerServiceClient')
@patch('gen_ai_orchestrator.utils.gcp.gcp_secret_manager_client.GCPSecretManagerClient.get_credentials')
@patch('gen_ai_orchestrator.utils.secret_manager.secret_manager_service.application_settings',
       _Settings(
           vector_store_provider = VectorStoreProvider.OPEN_SEARCH,
           vector_store_host = None,
           vector_store_port = None,
           vector_store_user = None,
           vector_store_pwd = None,
           vector_store_database = None,
           vector_store_secret_manager_provider = SecretManagerProvider.GCP,
           vector_store_credentials_secret_name = 'my_secret_key'
       )
)
def test_fetch_gcp_secret_credentials(mock_get_credentials, mock_gcp_secretmanager_client):
    # Test data
    my_credentials = Credentials(username='user', password='pwd123456')

    # Configure the mocks to return specific values
    mock_gcp_secretmanager_client.return_value = None
    mock_get_credentials.return_value = my_credentials

    # Call the function to fetch the credentials of the default vector store
    credentials = fetch_default_vector_store_credentials()

    # Check test results
    mock_gcp_secretmanager_client.assert_called_once()
    mock_get_credentials.assert_called_once_with('my_secret_key')
    assert credentials.username == my_credentials.username
    assert credentials.password == my_credentials.password

@patch('boto3.client')
@patch('gen_ai_orchestrator.utils.aws.aws_secrets_manager_client.AWSSecretsManagerClient.get_credentials')
@patch('gen_ai_orchestrator.utils.secret_manager.secret_manager_service.application_settings',
       _Settings(
           vector_store_provider=None,
           vector_store_host=None,
           vector_store_port=None,
           vector_store_user='default_user',
           vector_store_pwd='default_pwd',
           vector_store_database=None,
           vector_store_secret_manager_provider=None,
           vector_store_credentials_secret_name=None
       ))
def test_fetch_default_credentials(mock_get_credentials, mock_boto3_client):
        # Call the function to fetch the credentials of the default vector store
        credentials = fetch_default_vector_store_credentials()

        # Check test results
        assert not mock_boto3_client.called
        assert not mock_get_credentials.called
        assert credentials.username == 'default_user'
        assert credentials.password == 'default_pwd'


@patch('boto3.client')
@patch('gen_ai_orchestrator.utils.aws.aws_secrets_manager_client.AWSSecretsManagerClient.get_credentials')
@patch('gen_ai_orchestrator.utils.secret_manager.secret_manager_service.application_settings',
       _Settings(
           vector_store_provider=VectorStoreProvider.OPEN_SEARCH,
           vector_store_host=None,
           vector_store_port=None,
           vector_store_user=None,
           vector_store_pwd=None,
           vector_store_database=None,
           vector_store_secret_manager_provider=SecretManagerProvider.AWS,
           vector_store_credentials_secret_name='my_secret_key'
       )
       )
def test_fetch_bad_credentials(mock_get_credentials, mock_boto3_client):
    # Test data
    open_search_credentials = None

    # Configure the mocks to return specific values
    mock_boto3_client.return_value = None
    mock_get_credentials.return_value = open_search_credentials

    # Call the function to fetch the credentials of the default vector store
    credentials = fetch_default_vector_store_credentials()

    # Check test results
    mock_boto3_client.assert_called_once_with(service_name='secretsmanager')
    mock_get_credentials.assert_called_once_with('my_secret_key')
    assert credentials is None

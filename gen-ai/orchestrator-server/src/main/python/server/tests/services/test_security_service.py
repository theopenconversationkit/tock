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
import unittest
from unittest.mock import patch

from gen_ai_orchestrator.configurations.environment.settings import (
    application_settings,
)
from gen_ai_orchestrator.models.security.ai_provider_secret import (
    AIProviderSecret,
)
from gen_ai_orchestrator.models.security.aws_secret_key.aws_secret_key import (
    AwsSecretKey,
)
from gen_ai_orchestrator.models.security.credentials import Credentials
from gen_ai_orchestrator.models.security.gcp_secret_key.gcp_secret_key import (
    GcpSecretKey,
)
from gen_ai_orchestrator.models.security.raw_secret_key.raw_secret_key import (
    RawSecretKey,
)
from gen_ai_orchestrator.services.security.security_service import (
    fetch_secret_key_value,
)
from gen_ai_orchestrator.utils.gcp.gcp_secret_manager_client import (
    GCPSecretManagerClient,
)


class TestSecurityService(unittest.TestCase):

    def test_fetch_unknown_secret_key_value(self):
        # Test data
        my_secret_api_key = '123abc!'

        # Call the function to fetch aws secret key
        value = fetch_secret_key_value({'type': 'UnknownSecretKey', 'secret': my_secret_api_key})

        # Check test results
        self.assertIsNone(value)

    def test_fetch_raw_secret_key_value(self):
        # Test data
        my_secret_api_key = '123abc!'

        # Call the function to fetch aws secret key
        value = fetch_secret_key_value(RawSecretKey(secret=my_secret_api_key))

        # Check test results
        self.assertEqual(value, my_secret_api_key)

    @patch('boto3.client')
    @patch('gen_ai_orchestrator.utils.aws.aws_secrets_manager_client.AWSSecretsManagerClient.get_ai_provider_secret')
    def test_fetch_aws_secret_key_value(self, mock_get_ai_provider_secret, mock_boto3_client):
        # Test data
        aws_secret_name = 'my_secret_key'
        my_secret_api_key = AIProviderSecret(secret='my_secret_key_value')

        # Configure the mocks to return specific values
        mock_boto3_client.return_value = None
        mock_get_ai_provider_secret.return_value = my_secret_api_key

        # Call the function to fetch aws secret key
        value = fetch_secret_key_value(AwsSecretKey(secret_name=aws_secret_name))

        # Check test results
        mock_boto3_client.assert_called_once_with(service_name='secretsmanager')
        mock_get_ai_provider_secret.assert_called_once_with(aws_secret_name)
        self.assertEqual(value, my_secret_api_key.secret)

    @patch('boto3.client')
    @patch('gen_ai_orchestrator.utils.aws.aws_secrets_manager_client.AWSSecretsManagerClient.get_ai_provider_secret')
    def test_fetch_bad_aws_secret_key_value(self, mock_get_ai_provider_secret, mock_boto3_client):
        # Test data
        aws_secret_name = 'my_secret_key'
        my_secret_api_key = None

        # Configure the mocks to return specific values
        mock_boto3_client.return_value = None
        mock_get_ai_provider_secret.return_value = my_secret_api_key

        # Call the function to fetch aws secret key
        value = fetch_secret_key_value(AwsSecretKey(secret_name=aws_secret_name))

        # Check test results
        mock_boto3_client.assert_called_once_with(service_name='secretsmanager')
        mock_get_ai_provider_secret.assert_called_once_with(aws_secret_name)
        self.assertIsNone(value)

    @patch('google.cloud.secretmanager.SecretManagerServiceClient')
    def test_fetch_gcp_secret_key_value(self, mock_gcp_secret_manager_client):
        # Test data
        gcp_secret_name = 'my_secret_key'
        gcp_secret = AIProviderSecret(secret='my_secret_key_value')
        secret_value = f'{{"secret":"{gcp_secret.secret}"}}'

        # Configure the mocks to return specific values
        mock_gcp_secret_manager_client.return_value.access_secret_version.return_value.payload.data = bytes(
            secret_value, 'utf8')

        # Call the function to fetch aws secret key
        value = fetch_secret_key_value(GcpSecretKey(secret_name=gcp_secret_name))

        # Check test results
        mock_gcp_secret_manager_client.assert_called_once_with()
        mock_gcp_secret_manager_client.return_value.access_secret_version.assert_called_once_with(
            name=f'projects/{application_settings.gcp_project_id}/secrets/{gcp_secret_name}/versions/latest')
        self.assertEqual(gcp_secret.secret, value)

    @patch('google.cloud.secretmanager.SecretManagerServiceClient')
    def test_fetch_bad_gcp_secret_key_value(self, mock_gcp_secret_manager_client):
        # Test data
        gcp_secret_name = 'my_secret_key'
        gcp_secret = AIProviderSecret(secret='my_secret_key_value')
        secret_value = f'{{"bad-secret-attribute":"{gcp_secret.secret}"}}'

        # Configure the mocks to return specific values
        mock_gcp_secret_manager_client.return_value.access_secret_version.return_value.payload.data = bytes(
            secret_value, 'utf8')

        # Call the function to fetch aws secret key
        value = fetch_secret_key_value(GcpSecretKey(secret_name=gcp_secret_name))

        # Check test results
        mock_gcp_secret_manager_client.assert_called_once_with()
        mock_gcp_secret_manager_client.return_value.access_secret_version.assert_called_once_with(
            name=f'projects/{application_settings.gcp_project_id}/secrets/{gcp_secret_name}/versions/latest')
        self.assertIsNone(value)

    @patch('google.cloud.secretmanager.SecretManagerServiceClient')
    def test_fetch_gcp_credentials_value(self, mock_gcp_secret_manager_client):
        # Test data
        gcp_secret_name = 'my_secret_key'
        gcp_secret = Credentials(username='my_username', password='my_password')
        secret_value = f'{{"username":"{gcp_secret.username}", "password":"{gcp_secret.password}"}}'

        # Configure the mocks to return specific values
        mock_gcp_secret_manager_client.return_value.access_secret_version.return_value.payload.data = bytes(
            secret_value, 'utf8')

        # Call the function to fetch aws secret key
        value = GCPSecretManagerClient().get_credentials(gcp_secret_name)

        # Check test results
        mock_gcp_secret_manager_client.assert_called_once_with()
        mock_gcp_secret_manager_client.return_value.access_secret_version.assert_called_once_with(
            name=f'projects/{application_settings.gcp_project_id}/secrets/{gcp_secret_name}/versions/latest')
        self.assertEqual(gcp_secret, value)

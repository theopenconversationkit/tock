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
"""API Client to consume GCP Secret Manager API"""

import logging
from typing import TypeVar

from google.api_core.exceptions import NotFound
from google.cloud import secretmanager

from gen_ai_orchestrator.configurations.environment.settings import (
    application_settings,
)
from gen_ai_orchestrator.utils.secret_manager.secret_manager_client import (
    SecretManagerClient,
)

logger = logging.getLogger(__name__)
T = TypeVar('T')


class GCPSecretManagerClient(SecretManagerClient):
    """GCP Secret Manager Client."""

    def __init__(self):
        # Create the Secret Manager client.
        self.client = secretmanager.SecretManagerServiceClient()

    def get_secret(self, secret_id: str) -> str:
        """
        Retrieve individual secret by name, using the access_secret_version API.
        :param secret_id: The id of the secret to be fetched.
        """

        # Build the resource name of the secret
        secret_name = f"projects/{application_settings.gcp_project_id}/secrets/{secret_id}/versions/latest"

        try:
            # Access the secret version.
            response = self.client.access_secret_version(name=secret_name)
            payload = response.payload.data.decode('UTF-8')
            logging.info(f'The requested secret {secret_name} has been successfully retrieved.')
            return payload
        except NotFound as e:
            logger.error(f'The requested secret {secret_name} was not found.')
            raise
        except Exception as e:
            logger.error(f'An unknown error occurred: {str(e)}.')
            raise

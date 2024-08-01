#   Copyright (C) 2023-2024 Credit Mutuel Arkea
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

import json
import os
import logging
from typing import Optional, Type, TypeVar

from google.cloud import secretmanager

from gen_ai_orchestrator.configurations.environment.settings import application_settings
from gen_ai_orchestrator.models.security.ai_provider_secret import AIProviderSecret
from gen_ai_orchestrator.models.security.credentials import Credentials
from gen_ai_orchestrator.utils.instance import singleton

logger = logging.getLogger(__name__)
T = TypeVar('T')


@singleton
class GCPSecretManagerClient:
    """GCP Secret Manager Client."""

    def __init__(self):
        # Create the Secret Manager client.
        self.client = secretmanager.SecretManagerServiceClient()

    def get_credentials(self, secret_id: str) -> Optional[Credentials]:
        """
        Get a user credentials
        Expected storage format of the secret : {"username":"my-user_name","password":"op******3"}

        Args:
            secret_id: The GCP Secret ID
        """

        return parse_secret_data(self.get_secret(secret_id), Credentials)

    def get_ai_provider_secret(self, secret_id: str) -> Optional[AIProviderSecret]:
        """
        Get the AI provider secret
        Expected storage format of the secret : {"secret":"op******3"}

        Args:
            secret_id: The GCP Secret ID
        """

        return parse_secret_data(self.get_secret(secret_id), AIProviderSecret)

    def get_secret(self, secret_id: str):
        """
        Retrieve individual secret by name, using the access_secret_version API.
        :param secret_id: The id of the secret to be fetched.
        """

        try:
            # Build the resource name of the secret
            secret_name = f"projects/{application_settings.gcp_project_id}/secrets/{secret_id}/versions/latest"

            # Access the secret version.
            response = self.client.access_secret_version(name=secret_name)
            payload = response.payload.data.decode("UTF-8")
            logging.info(f'The requested secret {secret_name} has been successfully retrieved.')
            return payload
        # TODO MASS : DefaultCredentialsError(GoogleAuthError)
#        except ClientError as e:
#            if e.response['Error']['Code'] == 'ResourceNotFoundException':
#                logger.error(f'The requested secret {secret_name} was not found.')
#                raise
#            else:
#                logger.error(f'Error retrieving {secret_name} secret.')
#                raise
        except Exception as e:
            logger.error(f'An unknown error occurred: {str(e)}.')
            # PermissionDenied("Permission 'secretmanager.versions.access' denied for resource 'projects/bx270-e99-secret-rec-543/secrets/hhh/versions/latest' (or it may not exist).")


            # Avec: bx270-e99-secret-rec-543-e95f8a906655.json
            # PermissionDenied("Permission 'secretmanager.secrets.create' denied for resource 'projects/bx270-e99-secret-rec-543' (or it may not exist).")

            # Avec: bdi01-e99-secret-rec-584-4e3c2e3454fd.json
            # 400 Constraint constraints/gcp.resourceLocations violated for [orgpolicy:projects/364419067793] attempting to create a secret in [global]. For more information, see https://cloud.google.com/resource-manager/docs/organization-policy/defining-locations. [violations {
            #   type: "constraints/gcp.resourceLocations"
            #   subject: "orgpolicy:projects/364419067793"
            #   description: "Constraint constraints/gcp.resourceLocations violated for [orgpolicy:projects/364419067793] attempting to create a secret in [global]. For more information, see https://cloud.google.com/resource-manager/docs/organization-policy/defining-locations."
            # }
            # ]
            raise


def parse_secret_data(secret_data, obj_type: Type[T]) -> Optional[T]:
    """
    Parse the JSON secret data to retrieve an object of specified type.
    Args:
        secret_data: A json data
        obj_type: Type of object to return

    Returns:
        The object of specified type.

    Raises:
         JSONDecodeError: otherwise
    """
    try:
        secret_dict = json.loads(secret_data)
        return obj_type(**secret_dict)
    except json.JSONDecodeError as e:
        logger.error(f'Error parsing secret data: {str(e)}')
        return None

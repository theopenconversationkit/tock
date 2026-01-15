#   Copyright (C) 2025-2026 Credit Mutuel Arkea
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
import logging
from functools import lru_cache
from typing import Optional

from gen_ai_orchestrator.configurations.environment.settings import (
    application_settings,
)
from gen_ai_orchestrator.models.security.credentials import Credentials
from gen_ai_orchestrator.utils.aws.aws_secrets_manager_client import (
    AWSSecretsManagerClient,
)
from gen_ai_orchestrator.utils.gcp.gcp_secret_manager_client import (
    GCPSecretManagerClient,
)
from gen_ai_orchestrator.utils.secret_manager.secret_manager_provider import (
    SecretManagerProvider,
)
from gen_ai_orchestrator.utils.strings import obfuscate

logger = logging.getLogger(__name__)

# Define a mapping of secret manager providers to their corresponding client classes
secret_manager_provider_map = {
    SecretManagerProvider.AWS.value: AWSSecretsManagerClient,
    SecretManagerProvider.GCP.value: GCPSecretManagerClient
}

@lru_cache(maxsize=1)
def fetch_default_vector_store_credentials() -> Optional[Credentials]:
    """Fetch the Vector Store credentials."""
    if application_settings.vector_store_credentials_secret_name:
        secret_name = application_settings.vector_store_credentials_secret_name
        secret_manager_provider = application_settings.vector_store_secret_manager_provider

        # Fetch the corresponding client based on the provider
        secret_manager_client_class = secret_manager_provider_map.get(secret_manager_provider)

        if secret_manager_client_class:
            logger.info(f'Using {secret_manager_provider} to get vector store credentials...')
            secret_manager_client = secret_manager_client_class()
            credentials = secret_manager_client.get_credentials(secret_name)
        else:
            credentials = _get_credentials_from_env()
    else:
        credentials = _get_credentials_from_env()

    # Log whether credentials were successfully retrieved
    if credentials:
        logger.info('A default Vector Store Credentials have been successfully retrieved.')
        logger.debug(
            'A default Vector Store Credentials have been defined [Credentials=(user:%s, password:%s)] for [Provider=%s]',
            application_settings.vector_store_provider.value,
            credentials.username,
            obfuscate(credentials.password),
        )
    else:
        logger.info('No credentials were found.')
        logger.warning('No default Vector Store Credentials is defined !')


    return credentials

def _get_credentials_from_env() -> Optional[Credentials]:
    """Fetch credentials from environment variables."""
    if application_settings.vector_store_user or application_settings.vector_store_pwd:
        return Credentials(
            username=application_settings.vector_store_user,
            password=application_settings.vector_store_pwd
        )
    return None


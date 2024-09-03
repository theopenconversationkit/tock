from typing import Optional
import logging
from gen_ai_orchestrator.configurations.environment.settings import application_settings
from gen_ai_orchestrator.models.security.credentials import Credentials
from gen_ai_orchestrator.utils.aws.aws_secrets_manager_client import AWSSecretsManagerClient
from gen_ai_orchestrator.utils.gcp.gcp_secret_manager_client import GCPSecretManagerClient
from gen_ai_orchestrator.utils.secret_manager.secret_manager_provider import SecretManagerProvider
from gen_ai_orchestrator.utils.strings import obfuscate

logger = logging.getLogger(__name__)

# Define a mapping of secret manager providers to their corresponding client classes
secret_manager_provider_map = {
    SecretManagerProvider.AWS.value: AWSSecretsManagerClient,
    SecretManagerProvider.GCP.value: GCPSecretManagerClient
}


def fetch_default_vector_store_credentials() -> Optional[Credentials]:
    """Fetch the Vector Store credentials."""
    credentials = None

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
            # Fetch from environment variables if available
            if application_settings.vector_store_user or application_settings.vector_store_pwd:
                credentials = Credentials(
                    username=application_settings.vector_store_user,
                    password=application_settings.vector_store_pwd
                )
    else:
        # Default credentials if no secret manager is configured
        if application_settings.vector_store_user or application_settings.vector_store_pwd:
            credentials = Credentials(
                username=application_settings.vector_store_user,
                password=application_settings.vector_store_pwd
            )

    # Log whether credentials were successfully retrieved
    if credentials:
        logger.info("Credentials successfully retrieved.")
    else:
        logger.info("No credentials were found.")

    return credentials

vector_store_credentials: Optional[Credentials] = fetch_default_vector_store_credentials()

if vector_store_credentials is not None:
    logger.info(
        'A default Vector Store is defined : [Provider=%s][Credentials=(user:%s, password:%s)]',
        application_settings.vector_store_provider.value,
        vector_store_credentials.username,
        obfuscate(vector_store_credentials.password),
    )
else:
    logger.warning('No default Vector Store is defined !')
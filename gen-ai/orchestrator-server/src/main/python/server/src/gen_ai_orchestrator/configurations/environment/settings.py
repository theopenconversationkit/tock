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
"""
This module manages the initialization of application settings, based on environment variables
The OpenSearch master user credentials are retrieved from AWS Secrets Manager if
open_search_aws_secret_manager_name is set
"""

import logging
from enum import Enum, unique
from typing import Optional, Tuple

from path import Path
from pydantic_settings import BaseSettings, SettingsConfigDict

from gen_ai_orchestrator.models.security.proxy_server_type import ProxyServerType
from gen_ai_orchestrator.utils.aws.aws_secrets_manager_client import AWSSecretsManagerClient
from gen_ai_orchestrator.utils.strings import obfuscate

logger = logging.getLogger(__name__)


def fetch_open_search_credentials() -> Tuple[Optional[str], Optional[str]]:
    """Fetch the OpenSearch credentials."""
    if application_settings.open_search_aws_secret_manager_name is not None:
        logger.info('Use of AWS Secrets Manager to get OpenSearch credentials...')
        credentials = AWSSecretsManagerClient().get_credentials(
            secret_name=application_settings.open_search_aws_secret_manager_name
        )
        if credentials is not None:
            logger.info("The credentials have been successfully retrieved from AWS Secrets Manager.")
            return credentials.username, credentials.password
        else:
            logger.error("No credentials extracted from AWS Secrets Manager.")
            return None, None
    else:
        return application_settings.open_search_user, application_settings.open_search_pwd


@unique
class _Environment(str, Enum):
    """Enumeration to list environment type"""

    DEV = 'DEV'
    PROD = 'PROD'


class _Settings(BaseSettings):
    """Application class for settings, allowing values to be overridden by environment variables."""

    model_config = SettingsConfigDict(
        env_prefix='tock_gen_ai_orchestrator_', case_sensitive=True
    )

    application_environment: _Environment = _Environment.DEV
    application_logging_config_ini: str = (
            Path(__file__).dirname() + '/../logging/config.ini'
    )
    """Request timeout: set the maximum time (in seconds) for the request to be completed."""
    llm_provider_timeout: int = 30
    llm_provider_max_retries: int = 0
    em_provider_timeout: int = 4

    open_search_host: str = 'localhost'
    open_search_port: str = '9200'
    open_search_aws_secret_manager_name: Optional[str] = None
    open_search_user: Optional[str] = 'admin'
    open_search_pwd: Optional[str] = 'admin'
    """Request timeout: set the maximum time (in seconds) for the request to be completed."""
    open_search_timeout: int = 4

    observability_provider_max_retries: int = 0
    """Request timeout (in seconds)."""
    observability_provider_timeout: int = 3

    """
    This AWSLambda proxy is used when the architecture implemented for the Langfuse
    observability tool places it behind an API Gateway which requires its
    own authentication, itself invoked by an AWS Lambda.
    The API Gateway uses the standard "Authorization" header,
    and uses observability_proxy_server_authorization_header_name
    to define the "Authorization bearer token" for Langfuse.
    """
    observability_proxy_server: Optional[ProxyServerType] = None
    observability_proxy_server_authorization_header_name: Optional[str] = None

    """
    List of indexes names that will have default flashrank rerank enabled.
    """
    # TODO should be removed.
    flashrank_rerank_doc_index_to_compress: Optional[str] = 'ns-03-bot-cmso'
    flashrank_rerank_default_model: Optional[str] = 'ms-marco-MultiBERT-L-12'
    flashrank_rerank_default_min_score: float = 0.999
    flashrank_rerank_default_max_documents: int = 6
    flashrank_rerank_override_vectordb_document_number_neighbors: int = 8


application_settings = _Settings()
is_prod_environment = _Environment.PROD == application_settings.application_environment
open_search_username, open_search_password = fetch_open_search_credentials()

logger.info(
    'OpenSearch user credentials: %s:%s',
    open_search_username,
    obfuscate(open_search_password),
)

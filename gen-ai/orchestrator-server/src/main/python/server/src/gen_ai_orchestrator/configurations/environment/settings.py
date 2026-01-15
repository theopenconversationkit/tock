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
"""
This module manages the initialization of application settings, based on environment variables.
The default Vector Store credentials are configured directly on environment,
or retrieved from the Secret Manager Provider if the vector_store_credentials_secret_name is set.
"""

import logging
from enum import Enum, unique
from typing import Optional

from path import Path
from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict

from gen_ai_orchestrator.models.security.proxy_server_type import (
    ProxyServerType,
)
from gen_ai_orchestrator.models.vector_stores.vectore_store_provider import (
    VectorStoreProvider,
)
from gen_ai_orchestrator.utils.secret_manager.secret_manager_provider import (
    SecretManagerProvider,
)

logger = logging.getLogger(__name__)


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
    """ Enable or not the rate limit for the LLM call"""
    llm_rate_limits: bool = True
    em_provider_timeout: int = 4

    vector_store_provider: Optional[VectorStoreProvider] = VectorStoreProvider.OPEN_SEARCH
    vector_store_host: Optional[str] = 'localhost'
    vector_store_port: Optional[str] = '9200'
    vector_store_user: Optional[str] = 'admin'
    vector_store_pwd: Optional[str] = 'admin'
    vector_store_database: Optional[str] = None # Only if necessary. Example: PGVector
    vector_store_secret_manager_provider: Optional[SecretManagerProvider] = None
    vector_store_credentials_secret_name: Optional[str] = None
    """Request timeout: set the maximum time (in seconds) for the request to be completed."""
    vector_store_timeout: int = 4
    """Maximum number of documents to be retrieved from the Vector Store"""
    vector_store_test_max_docs_retrieved: int = 4
    vector_store_test_query: str = 'Any definition'

    """Observability Setting"""
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

    """GCP"""
    # GCP project ID used for GCP Secrets
    gcp_project_id: Optional[str] = Field(alias='tock_gcp_project_id', default=None)


application_settings = _Settings()
is_prod_environment = _Environment.PROD == application_settings.application_environment

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
The OpenSearch master user credentials are retrieved from AWS Secret Manager if
open_search_aws_secret_manager_name is set
"""

import logging
from enum import Enum, unique
from typing import Optional

from path import Path
from pydantic_settings import BaseSettings, SettingsConfigDict

from gen_ai_orchestrator.utils.aws.aws_secret_manager import AWSSecretManager
from gen_ai_orchestrator.utils.strings import obfuscate

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
    ai_provider_timeout: int = 30
    em_provider_timeout: int = 4

    open_search_host: str = 'localhost'
    open_search_port: str = '9200'
    open_search_aws_secret_manager_name: Optional[str] = None
    open_search_user: Optional[str] = 'admin'
    open_search_pwd: Optional[str] = 'admin'
    """Request timeout: set the maximum time (in seconds) for the request to be completed."""
    open_search_timeout: int = 4


application_settings = _Settings()
is_prod_environment = _Environment.PROD == application_settings.application_environment

open_search_username = application_settings.open_search_user
open_search_password = application_settings.open_search_pwd

if application_settings.open_search_aws_secret_manager_name is not None:
    logger.info('Use of AWS Secret Manager to get OpenSearch credentials...')
    open_search_username, open_search_password = AWSSecretManager().get_credentials(
        application_settings.open_search_aws_secret_manager_name
    )

logger.info(
    'OpenSearch user credentials: %s:%s',
    open_search_username,
    obfuscate(open_search_password),
)

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
from enum import Enum, unique
from typing import Optional

from path import Path
from pydantic_settings import BaseSettings, SettingsConfigDict


@unique
class _Environment(str, Enum):
    DEV = 'DEV'
    PROD = 'PROD'


class _Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_prefix='tock_gen_ai_orchestrator_', case_sensitive=True
    )

    application_environment: _Environment = _Environment.DEV
    application_logging_config_ini: str = (
        Path(__file__).dirname() + '/../logging/config.ini'
    )

    open_search_host: str = 'localhost'
    open_search_port: str = '9200'
    open_search_user: Optional[str] = 'admin'
    open_search_pwd: Optional[str] = 'admin'


application_settings = _Settings()
is_prod_environment = _Environment.PROD == application_settings.application_environment

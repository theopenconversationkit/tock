#   Copyright (C) 2023-2025 Credit Mutuel Arkea
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
"""Managing logging configuration"""

import logging
import logging.config

from gen_ai_orchestrator.configurations.environment.settings import (
    application_settings,
)


def setup_logging():
    """Setting up a logging configuration based on an ini file"""
    logging.config.fileConfig(application_settings.application_logging_config_ini)

#   Copyright (C) 2024 Credit Mutuel Arkea
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
import logging.config

import yaml

from llm_orchestrator.configurations.environement.settings import (
    application_settings,
)


def _setup_logging():
    with open(application_settings.application_logging_config_yaml, 'r') as config_file:
        config_dict = yaml.safe_load(config_file)
        logging.config.dictConfig(config_dict)


# configure logging
_setup_logging()

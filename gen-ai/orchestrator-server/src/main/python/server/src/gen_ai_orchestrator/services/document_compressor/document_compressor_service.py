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
"""Module for the Document Compressor Service"""

import logging

from gen_ai_orchestrator.models.document_compressor.document_compressor_types import DocumentCompressorSetting
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import get_compressor_factory

logger = logging.getLogger(__name__)


def check_document_compressor_setting(setting: DocumentCompressorSetting) -> bool:
    """
    Run a check for a given Document Compressor setting.

    Args:
        setting: The Document Compressor Provider setting to be checked

    Returns:
         True for a valid Document Compressor setting. Raise exception otherwise.
    """

    logger.info('Get the Callback handler Factory, then check the Document Compressor setting.')
    return get_compressor_factory(setting).check_document_compressor_setting()

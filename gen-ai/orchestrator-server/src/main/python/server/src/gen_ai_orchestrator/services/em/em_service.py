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
"""Module for the Embedding Model Service"""

import logging

from gen_ai_orchestrator.models.em.em_types import EMSetting
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    get_em_factory,
)

logger = logging.getLogger(__name__)


async def check_em_setting(setting: EMSetting) -> bool:
    """
    Run a check for a given EM setting.

    Args:
        setting: The Large Embedding Model setting to check

    Returns:
         True for a valid EM setting. Raise exception otherwise.
    """

    logger.info('Get the EM Factory, then check the EM setting.')
    return await get_em_factory(setting).check_embedding_model_setting()

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
"""Module for the Vector Store Service"""

import logging
from typing import Optional

from langchain_community.embeddings import FakeEmbeddings

from gen_ai_orchestrator.models.em.em_types import EMSetting
from gen_ai_orchestrator.models.vector_stores.vector_store_types import (
    VectorStoreSetting,
)
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    get_em_factory,
    get_vector_store_factory,
)

logger = logging.getLogger(__name__)


async def check_vector_store_setting(
        vector_store_setting: Optional[VectorStoreSetting],
        em_setting: Optional[EMSetting],
        index_name: Optional[str]) -> bool:
    """
    Run a check for a given Vector Store setting.

    Args:
        vector_store_setting: The Vector Store setting to check
        em_setting: The Embeddings setting to use with the Vector Store
        index_name: the index name

    Returns:
         True for a valid Vector Store setting. Raise exception otherwise.
    """

    logger.info('Get the Callback handler Factory, then check the Vector Store setting.')

    if em_setting is None or index_name is None:
        return await get_vector_store_factory(
            setting=vector_store_setting,
            index_name='fake_index_name',
            embedding_function=FakeEmbeddings(size=1536)
        ).check_vector_store_connection()
    else:
        return await get_vector_store_factory(
            setting=vector_store_setting,
            index_name=index_name,
            embedding_function=get_em_factory(em_setting).get_embedding_model()
        ).check_vector_store_setting()


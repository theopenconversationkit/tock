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
"""Module for the Vector Store Service"""

import logging

from langchain_community.embeddings import FakeEmbeddings

from gen_ai_orchestrator.models.vector_stores.vector_store_types import VectorStoreSetting
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import get_vector_store_factory

logger = logging.getLogger(__name__)


async def check_vector_store_setting(setting: VectorStoreSetting, index_name: str) -> bool:
    """
    Run a check for a given Vector Store setting.

    Args:
        setting: The Vector Store setting to check
        index_name: the index name

    Returns:
         True for a valid Vector Store setting. Raise exception otherwise.
    """

    logger.info('Get the Callback handler Factory, then check the Vector Store setting.')
    return await get_vector_store_factory(setting=setting,
                                          index_name=index_name,
                                          embedding_function=FakeEmbeddings(size=setting.vector_size)
                                          ).check_vector_store_setting()

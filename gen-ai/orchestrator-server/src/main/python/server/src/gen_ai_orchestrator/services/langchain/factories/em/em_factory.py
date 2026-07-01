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
"""Module for the LangChain Embedding Model Factory"""

import logging
from abc import ABC, abstractmethod
from typing import List

from langchain.embeddings.base import Embeddings
from pydantic import BaseModel

from gen_ai_orchestrator.models.em.em_setting import BaseEMSetting

logger = logging.getLogger(__name__)


class LangChainEMFactory(ABC, BaseModel):
    """A base class for LangChain Embedding Model Factory"""

    setting: BaseEMSetting

    @abstractmethod
    def get_embedding_model(self) -> Embeddings:
        """
        Embedding model to call
        :return: [Embeddings] the interface for embedding models.
        """
        pass

    async def check_embedding_model_setting(self) -> bool:
        """
        check the Embedding model setting validity
        :return: True if the setting is valid.
        """
        logger.info('Invoke EM provider to check setting')
        text = 'Hi, are you there?'
        response = await self.embed_query(text)
        logger.info('Embedding successful')
        logger.debug('[text: %s], [response: %s]', text, response)
        return True

    async def embed_query(self, text: str) -> List[float]:
        """Embed text."""
        return await self.get_embedding_model().aembed_query(text)

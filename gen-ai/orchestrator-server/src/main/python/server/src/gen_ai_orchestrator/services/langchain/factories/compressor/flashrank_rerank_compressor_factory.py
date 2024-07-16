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
"""Model for creating FlashrankRerankCompressorFactory"""

import logging
from typing import Dict

from gen_ai_orchestrator.models.compressors.flashrank_rerank.flashrank_rerank_params import \
    FlashrankRerankCompressorParams
from gen_ai_orchestrator.services.langchain.factories.compressor.compressor_factory import LangChainCompressorFactory

from langchain.retrievers.document_compressors import FlashrankRerank
from pydantic import Field

logger = logging.getLogger(__name__)


def _create_hash(param: FlashrankRerankCompressorParams) -> str:
    model = param.model if param.model else "none"
    return model + '-' + str(param.max_documents) + '-' + str(param.min_score)


class FlashrankRerankCompressorFactory(LangChainCompressorFactory):
    """A class for LangChain Flashrank Rerank Compressor Factory"""

    pool_singleton: dict[str, FlashrankRerank] = dict[str, FlashrankRerank]

    def get_compressor(self, param: FlashrankRerankCompressorParams):
        """
        get compressor

        Args:
            param: flashrank rerank parameter

        Returns:
            FlashrankRerank Compressor.
        """
        client_hash = _create_hash(param)
        compressor = self.pool_singleton.get(client_hash)
        if compressor is None:
            compressor = FlashrankRerank(
                top_n=param.min_score,
                score_threshold=param.max_documents,
                model=param.model
            )
        return compressor


flash_rankrerank_factory = FlashrankRerankCompressorFactory()  # Init it as Singleton in a package.

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
from gen_ai_orchestrator.models.compressors.flashrank_rerank.flashrank_rerank_params import \
    FlashrankRerankCompressorParams
from gen_ai_orchestrator.services.langchain.factories.compressor.compressor_factory import LangChainCompressorFactory

from langchain.retrievers.document_compressors import FlashrankRerank
from gen_ai_orchestrator.utils.singleton import singleton

logger = logging.getLogger(__name__)


@singleton
class FlashrankRerankCompressorFactory(LangChainCompressorFactory):
    """A class for LangChain Flashrank Rerank Compressor Factory"""
    param: FlashrankRerankCompressorParams

    pool_singleton: dict[str, FlashrankRerank] = dict[str, FlashrankRerank]

    def __init__(self):
        logger.debug(
            'creation  FlashrankRerankCompressorFactory'
        )

    def create_hash(self):
        model = self.param.model if self.param.model else "none"
        return model + '-' + str(self.param.max_documents) + '-' + str(self.param.min_score)

    def get_compressor(self):
        if not self.pool_singleton[self.create_hash()]:
            self.pool_singleton[self.create_hash()] = FlashrankRerank(
                top_n=self.param.min_score,
                score_threshold=self.param.max_documents,
                model=self.param.model
            )
        return self.pool_singleton[self.create_hash()]

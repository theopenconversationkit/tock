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
"""Model for creating BloomzEMFactory"""

from langchain.embeddings.base import Embeddings

from gen_ai_orchestrator.models.em.bloomz.bloomz_em_setting import (
    BloomzEMSetting,
)
from gen_ai_orchestrator.services.langchain.factories.em.em_factory import (
    LangChainEMFactory,
)
from gen_ai_orchestrator.services.langchain.impls.em.bloomz_embedding import BloomzEmbeddings


class BloomzEMFactory(LangChainEMFactory):
    """A class for Bloomz Embedding Factory"""

    setting: BloomzEMSetting

    def get_embedding_model(self) -> Embeddings:
        return BloomzEmbeddings(
            pooling=self.setting.pooling, api_base=self.setting.api_base
        )

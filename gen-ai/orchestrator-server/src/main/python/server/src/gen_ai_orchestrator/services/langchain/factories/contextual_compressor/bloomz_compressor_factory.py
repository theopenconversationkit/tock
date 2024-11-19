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
from langchain.retrievers.document_compressors.base import (
    BaseDocumentCompressor,
)

from gen_ai_orchestrator.models.contextual_compressor.bloomz.bloomz_compressor_setting import (
    BloomzCompressorSetting,
)
from gen_ai_orchestrator.services.contextual_compressor.bloomz_rerank import (
    BloomzRerank,
)
from gen_ai_orchestrator.services.langchain.factories.contextual_compressor.compressor_factory import (
    CompressorFactory,
)


class BloomzCompressorFactory(CompressorFactory):
    setting: BloomzCompressorSetting

    def get_compressor(self) -> BaseDocumentCompressor:
        return BloomzRerank(
            min_score=self.setting.min_score,
            endpoint=self.setting.endpoint,
            max_documents=self.setting.max_documents,
            label=self.setting.label,
        )

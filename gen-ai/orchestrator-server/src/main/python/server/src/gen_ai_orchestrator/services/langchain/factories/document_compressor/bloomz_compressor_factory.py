#   Copyright (C) 2024-2025 Credit Mutuel Arkea
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

from langchain_core.documents import Document

from gen_ai_orchestrator.models.document_compressor.bloomz.bloomz_compressor_setting import (
    BloomzCompressorSetting,
)
from gen_ai_orchestrator.services.langchain.factories.document_compressor.document_compressor_factory import (
    DocumentCompressorFactory,
)
from gen_ai_orchestrator.services.langchain.impls.document_compressor.bloomz_rerank import (
    BloomzRerank,
)


class BloomzCompressorFactory(DocumentCompressorFactory):
    setting: BloomzCompressorSetting

    def get_compressor(self) -> BloomzRerank:
        return BloomzRerank(
            min_score=self.setting.min_score,
            endpoint=self.setting.endpoint,
            max_documents=self.setting.max_documents,
            label=self.setting.label,
        )

    def check_document_compressor_setting(self) -> bool:
        self.get_compressor().compress_documents(
            documents=[Document(page_content='Hello, world!')],
            query='Hi!'
        )

        return True

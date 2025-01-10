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
from typing import Literal, Optional

from pydantic import Field

from gen_ai_orchestrator.models.document_compressor.document_compressor_provider import DocumentCompressorProvider
from gen_ai_orchestrator.models.document_compressor.document_compressor_setting import BaseDocumentCompressorSetting


class BloomzCompressorSetting(BaseDocumentCompressorSetting):
    """
    A class for Bloomz Compressor Model Setting.
    Usage docs: https://huggingface.co/cmarkea/bloomz-3b-reranking
    """

    provider: Literal[DocumentCompressorProvider.BLOOMZ] = Field(
        description='The document compressor provider.',
        examples=[DocumentCompressorProvider.BLOOMZ],
        default=DocumentCompressorProvider.BLOOMZ.value,
    )
    endpoint: str = Field(
        description='Bloomz scoring endpoint.',
        default='http://localhost:8082'
    )
    label: Optional[str] = Field(
        description='Label to use for reranking. The output label is usually documented on the huggingface model card '
                    'or in the model\'s config.json file (id2label..).', default='entailment'
    )

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
"""Model for creating FlashrankRerankCompressorSetting."""

from typing import Literal, Optional

from pydantic import Field
from langchain.retrievers.document_compressors import FlashrankRerank
from gen_ai_orchestrator.models.compressors.compressor_provider import (
    CompressorProvider,
)
from gen_ai_orchestrator.models.compressors.document_compressor_params import (
    BaseCompressorParams,
)


class FlashrankRerankCompressorParams(BaseCompressorParams):
    provider: Literal[CompressorProvider.FLASHRANK_RERANK] = Field(
        description='The Flashrank Rerank Model Provider.',
        examples=[CompressorProvider.FLASHRANK_RERANK],
        default=CompressorProvider.FLASHRANK_RERANK.value
    )
    model: Optional[str] = Field(description='The model id', examples=[],
                                 default=FlashrankRerank.__fields__['model'].default
                                 )
    min_score: Optional[float] = Field(description='Minimum retailment score.',
                                       default=FlashrankRerank.__fields__['score_threshold'].default
                                       )
    max_documents: Optional[int] = Field(description='Maximum number of documents to return',
                                         default=FlashrankRerank.__fields__['top_n'].default
                                         )

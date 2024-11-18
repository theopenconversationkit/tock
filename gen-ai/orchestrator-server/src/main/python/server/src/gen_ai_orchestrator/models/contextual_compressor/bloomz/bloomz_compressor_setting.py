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

from gen_ai_orchestrator.models.contextual_compressor.compressor_provider import (
    ContextualCompressorProvider,
)
from gen_ai_orchestrator.models.contextual_compressor.compressor_setting import (
    BaseCompressorSetting,
)
from gen_ai_orchestrator.services.contextual_compressor.bloomz_rerank import (
    BloomzRerank,
)


class BloomzCompressorSetting(BaseCompressorSetting):
    provider: Literal[ContextualCompressorProvider.BLOOMZ] = Field(
        description='The contextual compressor provider.',
        examples=[ContextualCompressorProvider.BLOOMZ],
        default=ContextualCompressorProvider.BLOOMZ.value,
    )
    min_score: Optional[float] = Field(
        description='Minimum retailment score.',
        default=BloomzRerank.__fields__['min_score'].default,
    )
    endpoint: str = Field(description='Bloomz scoring endpoint.')
    max_documents: Optional[int] = Field(
        description='Maximum number of documents to return to avoid exceeding max tokens for text generation.',
        default=BloomzRerank.__fields__['max_documents'].default,
    )
    label: Optional[str] = Field(
        description='Label to use for reranking. The output label is usually documented on the huggingface model card '
                    'or in the model\'s config.json file (id2label..).', default='entailment'
    )

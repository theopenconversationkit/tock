#   Copyright (C) 2024-2026 Credit Mutuel Arkea
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
"""Model for creating VllmCompressorSetting."""

from typing import Literal

from pydantic import Field

from gen_ai_orchestrator.models.document_compressor.document_compressor_provider import (
    DocumentCompressorProvider,
)
from gen_ai_orchestrator.models.document_compressor.document_compressor_setting import (
    BaseDocumentCompressorSetting,
)


class VllmCompressorSetting(BaseDocumentCompressorSetting):
    """
    A class for vLLM Compressor (Rerank API) Setting.

    The vLLM server must expose a cross-encoder reranking endpoint at
    ``{endpoint}/v1/rerank`` following the OpenAI-compatible rerank API format.
    """

    provider: Literal[DocumentCompressorProvider.VLLM] = Field(
        description='The document compressor provider.',
        examples=[DocumentCompressorProvider.VLLM],
        default=DocumentCompressorProvider.VLLM.value,
    )
    endpoint: str = Field(
        description='Base URL of the vLLM server (e.g. "http://localhost:8000").',
        examples=['http://localhost:8000'],
    )
    model: str = Field(
        description='Name of the reranking model served by vLLM.',
        examples=['BAAI/bge-reranker-v2-m3'],
    )

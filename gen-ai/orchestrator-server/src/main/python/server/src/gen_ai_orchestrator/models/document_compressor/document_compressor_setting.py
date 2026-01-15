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
from typing import Optional

from pydantic import BaseModel, Field

from gen_ai_orchestrator.models.document_compressor.document_compressor_provider import (
    DocumentCompressorProvider,
)


class BaseDocumentCompressorSetting(BaseModel):
    """A base class for Document Compressor Model Setting."""

    provider: DocumentCompressorProvider = Field(
        description='The document compressor provider.',
        examples=[DocumentCompressorProvider.BLOOMZ],
    )
    min_score: Optional[float] = Field(
        description='Minimum retailment score.',
        default=0.5,
    )
    max_documents: Optional[int] = Field(
        description='Maximum number of documents to return to avoid exceeding max tokens for text generation.',
        default=50,
    )
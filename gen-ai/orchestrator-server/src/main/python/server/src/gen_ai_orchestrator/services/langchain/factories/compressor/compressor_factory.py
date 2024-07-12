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
"""Module for the LangChain Compressor Factory"""

from abc import ABC, abstractmethod

from pydantic import BaseModel
from langchain.retrievers.document_compressors.base import BaseDocumentCompressor

from gen_ai_orchestrator.models.compressors.compressor_types import DocumentCompressorParams


class LangChainCompressorFactory(ABC, BaseModel):
    """A base class for LangChain Compressor Factory"""
    param: DocumentCompressorParams

    @abstractmethod
    def get_compressor(self) -> BaseDocumentCompressor:
        """
                Fabric the Compressor.
                :return: BaseDocumentCompressor .
                """
        pass


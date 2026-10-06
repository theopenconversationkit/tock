#   Copyright (C) 2025-2026 Credit Mutuel Arkea
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
import logging
from typing import Any, Optional, Sequence

from langchain_aws import BedrockRerank
from langchain_core.callbacks import Callbacks
from langchain_core.documents import BaseDocumentCompressor, Document
from pydantic import ConfigDict, PrivateAttr

from gen_ai_orchestrator.errors.exceptions.document_compressor.document_compressor_exceptions import (
    GenAIDocumentCompressorErrorException,
)
from gen_ai_orchestrator.models.errors.errors_models import ErrorInfo

logger = logging.getLogger(__name__)


class AwsBedrockRerank(BaseDocumentCompressor):
    """Document compressor that uses the `AWS Bedrock Rerank API`.

    Note: unlike Bloomz (whose scoring endpoint has no notion of a maximum
    result count), the underlying `langchain_aws.BedrockRerank.rerank()` is
    always called with `top_n=len(documents)` so that every candidate is
    scored, and the `min_score`/`max_documents`/`fill_to_max_documents`
    behaviour below is applied consistently with `BloomzRerank`.
    """

    model_config = ConfigDict(arbitrary_types_allowed=True)

    model_arn: str
    """The ARN of the Bedrock rerank model to use."""
    region_name: Optional[str] = None
    """AWS region, resolved from the profile/credential chain if not set."""
    credentials_profile_name: Optional[str] = None
    """AWS profile for authentication, optional."""
    min_score: float = 0.5
    """Minimum score to use for reranking."""
    max_documents: int = 50
    """Maximum number of documents to return to avoid exceeding max tokens for text generation."""
    fill_to_max_documents: bool = False
    """If True, complete with the best remaining documents up to max_documents."""
    is_fault_tolerant: bool = True
    """If True, the treatment is fault-tolerant."""

    _client: Any = PrivateAttr(default=None)

    def _get_client(self) -> BedrockRerank:
        if self._client is None:
            self._client = BedrockRerank(
                model_arn=self.model_arn,
                region_name=self.region_name,
                credentials_profile_name=self.credentials_profile_name,
            )
        return self._client

    def compress_documents(
        self,
        documents: Sequence[Document],
        query: str,
        callbacks: Callbacks | None = None,
    ) -> Sequence[Document]:
        """
        Compress documents.

        Args:
            documents: A sequence of documents to compress.
            query: The query to use for compressing the documents.
            callbacks: Callbacks to run during the compression process.

        Returns:
            A sequence of compressed documents.
        """
        if len(documents) == 0:  # to avoid empty api call
            return []

        try:
            results = self._get_client().rerank(
                documents=documents, query=query, top_n=len(documents)
            )
        except Exception as exc:
            logger.error(f"[Compressor] Exception during rerank call: {exc}")

            if not self.is_fault_tolerant:
                raise GenAIDocumentCompressorErrorException(
                    ErrorInfo(
                        error=exc.__class__.__name__,
                        cause=str(exc),
                        request=f"[Bedrock Rerank] model_arn={self.model_arn}",
                    )
                )

            logger.warning('[Compressor] Fallback to original documents')
            return documents

        scored_docs = []
        for res in results:
            doc = documents[res['index']]
            doc.metadata['retriever_score'] = res['relevance_score']
            scored_docs.append(doc)

        scored_docs = sorted(
            scored_docs,
            key=lambda d: d.metadata.get('retriever_score', 0),
            reverse=True,
        )

        above_threshold = [
            d
            for d in scored_docs
            if d.metadata.get('retriever_score', 0) >= self.min_score
        ]

        below_threshold = [
            d
            for d in scored_docs
            if d.metadata.get('retriever_score', 0) < self.min_score
        ]

        # base result
        result = above_threshold[: self.max_documents]

        # fill-to-K
        if self.fill_to_max_documents and len(result) < self.max_documents:
            remaining_slots = self.max_documents - len(result)
            result.extend(below_threshold[:remaining_slots])

        return result

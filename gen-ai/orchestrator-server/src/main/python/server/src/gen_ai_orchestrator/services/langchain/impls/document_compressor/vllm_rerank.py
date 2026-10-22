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
import logging
from typing import Sequence

import requests
from langchain_core.callbacks import Callbacks
from langchain_core.documents import BaseDocumentCompressor, Document

from gen_ai_orchestrator.errors.exceptions.document_compressor.document_compressor_exceptions import (
    GenAIDocumentCompressorErrorException,
)
from gen_ai_orchestrator.models.errors.errors_models import ErrorInfo

logger = logging.getLogger(__name__)


class VllmRerank(BaseDocumentCompressor):
    """Document compressor that uses the `vLLM Rerank API`."""

    endpoint: str = 'http://localhost:8000'
    """Base URL of the vLLM server."""
    model: str
    """Name of the reranking model served by vLLM."""
    min_score: float = 0.5
    """Minimum relevance score to retain a document."""
    max_documents: int = 50
    """Maximum number of documents returned."""
    fill_to_max_documents: bool = False
    """If True, pad the result with the best sub-threshold documents up to max_documents."""
    timeout: int = 5
    is_fault_tolerant: bool = True
    """If True, fall back to the original documents on any error."""

    def compress_documents(
        self,
        documents: Sequence[Document],
        query: str,
        callbacks: Callbacks | None = None,
    ) -> Sequence[Document]:
        if len(documents) == 0:
            return []

        url = f"{self.endpoint.rstrip('/')}/v1/rerank"
        try:
            response = requests.post(
                url=url,
                json={
                    'model': self.model,
                    'query': query,
                    'documents': [doc.page_content for doc in documents],
                },
                timeout=self.timeout,
            )

            if response.status_code != 200:
                logger.error(
                    '[Compressor] Bad response %s %s - %s',
                    response.status_code,
                    response.reason,
                    response.text,
                )

                if not self.is_fault_tolerant:
                    raise GenAIDocumentCompressorErrorException(
                        ErrorInfo(
                            error=str(response.status_code),
                            cause=f"Response: {response.text}, Reason: {response.reason}",
                            request=f"[POST] {url}",
                        )
                    )

                logger.warning('[Compressor] Fallback to original documents')
                return documents

            results = response.json().get('results', [])

        except GenAIDocumentCompressorErrorException:
            raise
        except Exception as exc:
            logger.error('[Compressor] Exception during rerank call: %s', exc)

            if not self.is_fault_tolerant:
                raise GenAIDocumentCompressorErrorException(
                    ErrorInfo(
                        error=exc.__class__.__name__,
                        cause=str(exc),
                        request=f"[POST] {url}",
                    )
                )

            logger.warning('[Compressor] Fallback to original documents')
            return documents

        scored_docs = []
        for res in results:
            idx = res.get('index')
            score = res.get('relevance_score', 0.0)
            if idx is None or idx >= len(documents):
                continue
            doc = documents[idx]
            doc.metadata['reranker_score'] = score
            scored_docs.append(doc)

        scored_docs = sorted(
            scored_docs,
            key=lambda d: d.metadata.get('reranker_score', 0),
            reverse=True,
        )

        above_threshold = [
            d for d in scored_docs if d.metadata.get('reranker_score', 0) >= self.min_score
        ]
        below_threshold = [
            d for d in scored_docs if d.metadata.get('reranker_score', 0) < self.min_score
        ]

        result = above_threshold[: self.max_documents]

        if self.fill_to_max_documents and len(result) < self.max_documents:
            remaining_slots = self.max_documents - len(result)
            result.extend(below_threshold[:remaining_slots])

        return result

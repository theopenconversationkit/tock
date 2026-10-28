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
from urllib.parse import urljoin

import requests
from langchain_core.callbacks import Callbacks
from langchain_core.documents import BaseDocumentCompressor, Document

from gen_ai_orchestrator.errors.exceptions.document_compressor.document_compressor_exceptions import (
    GenAIDocumentCompressorErrorException,
    GenAIDocumentCompressorUnknownLabelException,
)
from gen_ai_orchestrator.models.errors.errors_models import ErrorInfo
from gen_ai_orchestrator.services.langchain.impls.document_compressor.rerank_selection import (
    select_reranked_documents,
)

logger = logging.getLogger(__name__)


class BloomzRerank(BaseDocumentCompressor):
    """Document compressor that uses `Bloomz reranking endpoint`."""

    min_score: float = 0.5
    """Minimum score to use for reranking."""
    endpoint: str = 'http://localhost:8082'
    """Model to use for reranking."""
    max_documents: int = 50
    """Maximum number of documents to return to avoid exceeding max tokens for text generation."""
    label: str = 'entailment'
    """Label to use for reranking."""
    fill_to_max_documents: bool = False
    """If True, complete with the best remaining documents up to max_documents."""

    timeout: int = 5
    is_fault_tolerant: bool = True
    """If True, the treatment is fault-tolerant."""
    last_fallback_info: dict | None = None
    """Information about the last fallback, if any."""

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
        # Reset the fallback marker so a previous call's fallback is never read
        # as this call's (the compressor instance is reused).
        self.last_fallback_info = None

        if len(documents) == 0:  # to avoid empty api call
            return []

        url = urljoin(self.endpoint, '/score')
        try:
            response = requests.post(
                url=url,
                json={
                    'contexts': [
                        {'query': query, 'context': document.page_content}
                        for document in documents
                    ]
                },
                timeout=self.timeout,
            )

            if response.status_code != 200:
                logger.error(
                    f"[Compressor] Bad response {response.status_code} "
                    f"{response.reason} - {response.text}"
                )

                if not self.is_fault_tolerant:
                    raise GenAIDocumentCompressorErrorException(
                        ErrorInfo(
                            error=str(response.status_code),
                            cause=f"Response: {response.text}, Reason: {response.reason}",
                            request=f"[POST] {url}",
                        )
                    )

                returned_count = min(len(documents), self.max_documents)
                logger.warning(
                    '[Compressor] Fallback to original documents - returning first %d/%d documents',
                    returned_count,
                    len(documents),
                )
                self.last_fallback_info = {
                    'reason': 'bad_response',
                    'status_code': response.status_code,
                    'returned_documents': returned_count,
                    'total_documents': len(documents),
                    'error': response.text[:200],
                }
                return list(documents)[: self.max_documents]

            results = response.json().get('response', [])

        except Exception as exc:
            logger.error(f"[Compressor] Exception during rerank call: {exc}")

            if not self.is_fault_tolerant:
                raise GenAIDocumentCompressorErrorException(
                    ErrorInfo(
                        error=exc.__class__.__name__,
                        cause=str(exc),
                        request=f"[POST] {url}",
                    )
                )

            returned_count = min(len(documents), self.max_documents)
            logger.warning(
                '[Compressor] Fallback to original documents - returning first %d/%d documents',
                returned_count,
                len(documents),
            )
            self.last_fallback_info = {
                'reason': 'exception',
                'error_type': exc.__class__.__name__,
                'error_message': str(exc)[:200],
                'returned_documents': returned_count,
                'total_documents': len(documents),
            }
            return list(documents)[: self.max_documents]

        scored_docs = []

        for i, doc_results in enumerate(results):
            try:
                doc_entailment = next(
                    d for d in doc_results if d.get('label') == self.label
                )

                score = doc_entailment.get('score', 0.0)
                documents[i].metadata['reranker_score'] = score
                scored_docs.append(documents[i])

            except StopIteration:
                message = (
                    f'[Compressor] Label "{self.label}" not found in {doc_results}'
                )
                logger.warning(message)

                if not self.is_fault_tolerant:
                    raise GenAIDocumentCompressorUnknownLabelException(
                        ErrorInfo(cause=message)
                    )

                continue

            except Exception as exc:
                logger.error(f"[Compressor] Error processing result: {exc}")
                continue

        return select_reranked_documents(
            scored_docs,
            min_score=self.min_score,
            max_documents=self.max_documents,
            fill_to_max_documents=self.fill_to_max_documents,
        )

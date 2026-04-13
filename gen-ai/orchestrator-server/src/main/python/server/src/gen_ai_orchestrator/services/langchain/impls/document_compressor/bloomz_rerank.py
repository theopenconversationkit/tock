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
    fill_to_max_documents: bool = True
    """If True, complete with the best remaining documents up to max_documents."""

    timeout: int = 5

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
                    f'[Compressor] Bad response {response.status_code} '
                    f'{response.reason} - {response.text}'
                )
                logger.warning('[Compressor] Fallback to original documents')
                return documents

            results = response.json().get('response', [])

        except Exception as exc:
            logger.error(f'[Compressor] Exception during rerank call: {exc}')
            logger.warning('[Compressor] Fallback to original documents')
            return documents

        scored_docs = []

        for i, doc_results in enumerate(results):
            try:
                doc_entailment = next(
                    d for d in doc_results if d.get('label') == self.label
                )

                score = doc_entailment.get('score', 0.0)
                documents[i].metadata['retriever_score'] = score
                scored_docs.append(documents[i])

            except StopIteration:
                logger.warning(
                    f'[Compressor] Label "{self.label}" not found in {doc_results}'
                )
                continue

            except Exception as exc:
                logger.error(f'[Compressor] Error processing result: {exc}')
                continue

        scored_docs = sorted(
            scored_docs,
            key=lambda d: d.metadata.get('retriever_score', 0),
            reverse=True,
        )

        above_threshold = [
            d for d in scored_docs
            if d.metadata.get('retriever_score', 0) >= self.min_score
        ]

        below_threshold = [
            d for d in scored_docs
            if d.metadata.get('retriever_score', 0) < self.min_score
        ]

        # base result
        result = above_threshold[: self.max_documents]

        # fill-to-K
        if self.fill_to_max_documents and len(result) < self.max_documents:
            remaining_slots = self.max_documents - len(result)
            result.extend(below_threshold[:remaining_slots])

        return result

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
import logging
from typing import Optional, Sequence
from urllib.parse import urljoin

import requests
from langchain.callbacks.manager import Callbacks
from langchain.retrievers.document_compressors.base import (
    BaseDocumentCompressor,
)
from langchain_core.documents import Document

from gen_ai_orchestrator.errors.exceptions.document_compressor.document_compressor_exceptions import \
    GenAIDocumentCompressorUnknownLabelException, GenAIDocumentCompressorErrorException
from gen_ai_orchestrator.models.errors.errors_models import ErrorInfo

logger = logging.getLogger(__name__)

logging.basicConfig()
logging.getLogger().setLevel(logging.INFO)


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

    def compress_documents(
        self,
        documents: Sequence[Document],
        query: str,
        callbacks: Optional[Callbacks] = None,
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
            )

            if response.status_code != 200:
                logger.error("The scoring server didn't respond as expected.")
                logger.error(f'{response.status_code} {response.reason} - {response.text}')
                raise GenAIDocumentCompressorErrorException(ErrorInfo(
                    error=str(response.status_code),
                    cause=f'Response: {response.text}, Reason: {response.reason}',
                    request=f'[POST] {url}',
                ))
        except GenAIDocumentCompressorErrorException:
            # Re-raise GenAIDocumentCompressorErrorException without modification
            raise
        except Exception as exc:
            logger.error(f'Unknown error ! {exc}')
            raise GenAIDocumentCompressorErrorException(ErrorInfo(
                error=exc.__class__.__name__,
                cause=str(exc),
                request=f'[POST] {url}',
            ))

        final_results = []
        for i, doc_results in enumerate(response.json()['response']):
            labels=list(map(lambda doc: doc['label'], doc_results))
            try:
                doc_entailment = next(
                    filter(lambda cls: cls['label'] == self.label, doc_results)
                )
                if doc_entailment['score'] >= self.min_score:
                    documents[i].metadata['retriever_score'] = doc_entailment['score']
                    final_results.append(documents[i])

            except StopIteration:
                message = f"The label {self.label} doesn't match any known labels {labels}."
                raise GenAIDocumentCompressorUnknownLabelException(ErrorInfo(cause=message))

        return sorted(
            final_results, key=lambda d: d.metadata['retriever_score'], reverse=True
        )[: self.max_documents]

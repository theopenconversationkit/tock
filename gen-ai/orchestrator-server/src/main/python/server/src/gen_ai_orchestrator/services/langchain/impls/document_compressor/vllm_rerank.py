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
from gen_ai_orchestrator.services.langchain.impls.document_compressor.rerank_selection import (
    select_reranked_documents,
)
from gen_ai_orchestrator.services.langchain.impls.document_compressor.rerank_windowing import (
    make_windows,
)

logger = logging.getLogger(__name__)

# Characters reserved on top of the query to keep a safe distance from the
# model's hard token limit (see compress_documents for the budget rationale).
SAFETY_MARGIN = 32
# Below this character budget the condensed query is abnormally long: we skip
# reranking entirely rather than send a request that could exceed the limit.
MIN_BUDGET_CHARS = 1000


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
    max_input_tokens: int = 8192
    """Maximum input length (query + document) in tokens accepted by the vLLM
    model. Must match the server's --max-model-len. Documents longer than the
    derived character budget are split into windows scored separately."""
    is_fault_tolerant: bool = True
    """If True, fall back to the original documents on any error."""
    last_fallback_info: dict | None = None
    """Information about the last fallback, if any."""

    def compress_documents(
        self,
        documents: Sequence[Document],
        query: str,
        callbacks: Callbacks | None = None,
    ) -> Sequence[Document]:
        self.last_fallback_info = None

        if len(documents) == 0:
            return []

        # One token always covers at least one character with the GTE tokenizer,
        # so a character budget is a safe upper bound on the token count: reserve
        # the query length (in chars) plus a safety margin and the request can
        # never exceed max_input_tokens.
        budget = self.max_input_tokens - len(query) - SAFETY_MARGIN

        if budget < MIN_BUDGET_CHARS:
            returned_count = min(len(documents), self.max_documents)
            logger.warning(
                '[Compressor] Query too long (budget=%d chars) - skipping rerank, '
                'returning first %d/%d documents',
                budget,
                returned_count,
                len(documents),
            )
            self.last_fallback_info = {
                'reason': 'query_too_long',
                'budget': budget,
                'returned_documents': returned_count,
                'total_documents': len(documents),
            }
            return list(documents)[: self.max_documents]

        texts, owners = self._build_windows(documents, budget)

        url = f"{self.endpoint.rstrip('/')}/v1/rerank"
        try:
            response = requests.post(
                url=url,
                json={
                    'model': self.model,
                    'query': query,
                    'documents': texts,
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

        window_counts: dict[int, int] = {}
        for owner in owners:
            window_counts[owner] = window_counts.get(owner, 0) + 1

        # Aggregate window scores back to each parent document using max.
        best_scores: dict[int, float] = {}
        for res in results:
            idx = res.get('index')
            score = res.get('relevance_score', 0.0)
            if idx is None or idx >= len(owners):
                continue
            parent = owners[idx]
            if parent not in best_scores or score > best_scores[parent]:
                best_scores[parent] = score

        # Hand the scored documents over in input (retrieval) order, not in the
        # order of the provider's response: the selection's stable sort then
        # breaks score ties by retrieval rank, deterministically, and the studio
        # diagnostic reproduces it when it recomputes thresholds client-side.
        scored_docs = []
        for parent in sorted(best_scores):
            score = best_scores[parent]
            doc = documents[parent]
            doc.metadata['reranker_score'] = score
            if window_counts[parent] > 1:
                doc.metadata['reranker_windows'] = window_counts[parent]
            scored_docs.append(doc)

        return select_reranked_documents(
            scored_docs,
            min_score=self.min_score,
            max_documents=self.max_documents,
            fill_to_max_documents=self.fill_to_max_documents,
        )

    def _build_windows(
        self, documents: Sequence[Document], budget: int
    ) -> tuple[list[str], list[int]]:
        """Build the texts sent to vLLM and the parent index for each one.

        Documents within budget are sent unchanged (one text, current
        behavior). Oversized documents are split into several windows, each
        mapped back to its parent via the returned owners list.
        """
        texts: list[str] = []
        owners: list[int] = []
        for i, doc in enumerate(documents):
            content = doc.page_content
            if len(content) <= budget:
                windows = [content]
            else:
                windows = make_windows(content, budget)
                logger.info(
                    '[Compressor] Windowed document %s (%d chars) into %d windows',
                    doc.metadata.get('id') or doc.metadata.get('chunk'),
                    len(content),
                    len(windows),
                )
            for window in windows:
                # Final hard guarantee, whatever happened before.
                texts.append(window if len(window) <= budget else window[:budget])
                owners.append(i)
        return texts, owners

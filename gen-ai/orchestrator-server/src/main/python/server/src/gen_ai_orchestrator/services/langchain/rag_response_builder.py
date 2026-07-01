#   Copyright (C) 2026 Credit Mutuel Arkea
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
"""
RAG Response Builder
--------------------
Responsible for assembling RAGResponse objects from raw chain outputs,
including footnotes, debug data, and observability metadata.
"""

import json
import logging
from typing import List

from langchain_core.documents import Document

from gen_ai_orchestrator.models.observability.observability_trace import (
    ObservabilityTrace,
)
from gen_ai_orchestrator.models.rag.rag_models import (
    Footnote,
    LLMAnswer,
    LLMCondensedQuestion,
    RAGDebugData,
    RAGDocument,
    RAGDocumentMetadata,
)
from gen_ai_orchestrator.routers.requests.requests import RAGRequest
from gen_ai_orchestrator.routers.responses.responses import RAGResponse
from gen_ai_orchestrator.services.langchain.callbacks.rag_callback_handler import (
    RAGCallbackHandler,
)
from gen_ai_orchestrator.services.langchain.rag_chain_builder import (
    get_chunk_identifier,
)
from gen_ai_orchestrator.services.observability.observabilty_service import (
    get_observability_info,
)

logger = logging.getLogger(__name__)


# ---------------------------------------------------------------------------
# Footnote / source helpers
# ---------------------------------------------------------------------------


def get_source_content(doc: Document) -> str:
    """Strip the title prefix that may have been prepended to the chunk text."""
    title_prefix = f"{doc.metadata['title']}\n\n"
    if doc.page_content.startswith(title_prefix):
        return doc.page_content[len(title_prefix) :]
    return doc.page_content


def extract_rank(value: str | None) -> int:
    """
    Convert "3/15" -> 3
    Missing value -> very large number
    """
    if not value:
        return 999999

    return int(value.split('/')[0])


def footnote_sort_key(doc: Document) -> tuple[int, int]:
    rank_metadata = doc.metadata.get('rank', {})

    if 'rrf' in rank_metadata:
        return 0, extract_rank(rank_metadata['rrf'])

    if 'similarity' in rank_metadata:
        return 1, extract_rank(rank_metadata['similarity'])

    if 'fts' in rank_metadata:
        return 2, extract_rank(rank_metadata['fts'])

    return 3, 999999


def build_footnotes(
    documents: list[Document],
    llm_answer: LLMAnswer,
) -> list[Footnote]:
    """
    Return one Footnote per document whose chunk was actually used in the
    LLM answer (according to context_usage).
    """
    used_chunk_ids = {
        ctx.chunk for ctx in (llm_answer.context_usage or []) if ctx.used_in_response
    }

    used_docs = [
        doc for doc in documents if get_chunk_identifier(doc) in used_chunk_ids
    ]

    sorted_docs = sorted(
        used_docs,
        key=footnote_sort_key,
    )

    return [
        Footnote(
            identifier=doc.metadata['id'],
            title=doc.metadata['title'],
            url=doc.metadata['source'],
            content=get_source_content(doc),
            metadata=doc.metadata.copy(),
        )
        for doc in sorted_docs
    ]


# ---------------------------------------------------------------------------
# Debug data helpers
# ---------------------------------------------------------------------------


def get_rag_documents(handler: RAGCallbackHandler) -> List[RAGDocument]:
    """Convert raw LangChain documents captured by the callback into RAGDocument objects."""
    if handler.records.get('documents') is None:
        return []

    return [
        RAGDocument(
            content=doc.page_content[: len(doc.metadata['title']) + 100] + '...',
            metadata=RAGDocumentMetadata(**doc.metadata),
        )
        for doc in handler.records['documents']
    ]


def get_llm_answer_from_raw(output: str | None) -> LLMAnswer:
    """Parse a raw JSON string (possibly fenced with ```json```) into an LLMAnswer."""
    if output is None:
        return LLMAnswer()

    cleaned = output.strip().removeprefix('```json').removesuffix('```').strip()
    return LLMAnswer(**json.loads(cleaned))


def get_condensing_llm_answer_from_raw(output: str | None) -> LLMCondensedQuestion:
    """Parse a raw JSON string (possibly fenced with ```json```) into an LLMCondensedQuestion."""
    if output is None:
        return LLMCondensedQuestion()

    cleaned = output.strip().removeprefix('```json').removesuffix('```').strip()
    return LLMCondensedQuestion(**json.loads(cleaned))


def build_rag_debug_data(
    request: RAGRequest,
    records_callback_handler: RAGCallbackHandler,
    rag_duration: float,
) -> RAGDebugData:
    history = request.dialog.history if request.dialog else []

    return RAGDebugData(
        user_question=request.question_answering_prompt.inputs['question'],
        question_condensing_prompt=records_callback_handler.records.get('chat_prompt'),
        question_condensing_history=history,
        condensing_llm_answer=get_condensing_llm_answer_from_raw(
            records_callback_handler.records.get(
                'rag_question_condensation_chain_output'
            )
        ),
        question_answering_prompt=records_callback_handler.records.get('rag_prompt'),
        documents=get_rag_documents(records_callback_handler),
        document_index_name=request.document_index_name,
        document_search_params=request.document_search_params,
        answer=get_llm_answer_from_raw(
            records_callback_handler.records.get('rag_chain_output')
        ),
        duration=rag_duration,
    )


# ---------------------------------------------------------------------------
# Full response assembler
# ---------------------------------------------------------------------------


def build_rag_response(
    chain_output: dict,
    llm_answer: LLMAnswer,
    request: RAGRequest,
    records_callback_handler: RAGCallbackHandler,
    observability_handler,
    rag_duration: float,
    debug: bool,
) -> RAGResponse:
    """Assemble the final RAGResponse from all intermediate results."""
    return RAGResponse(
        answer=llm_answer,
        footnotes=build_footnotes(chain_output['documents'], llm_answer),
        observability_info=get_observability_info(
            observability_handler,
            ObservabilityTrace.RAG.value,
        ),
        debug=build_rag_debug_data(request, records_callback_handler, rag_duration)
        if debug
        else None,
    )

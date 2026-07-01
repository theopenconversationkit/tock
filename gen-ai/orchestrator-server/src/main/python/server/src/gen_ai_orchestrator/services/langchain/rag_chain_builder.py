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
RAG Chain Builder
-----------------
Responsible for assembling the LangChain pipeline:
  - question condensation
  - hybrid retrieval (vector + full-text search)
  - RRF ranking
  - answer generation
"""

import asyncio
import json
import logging
from operator import itemgetter
from typing import Any, List, Optional
from urllib.parse import urlparse

from langchain_core.documents import Document
from langchain_core.messages import AIMessage, HumanMessage
from langchain_core.output_parsers import JsonOutputParser
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.prompts import PromptTemplate as LangChainPromptTemplate
from langchain_core.runnables import (
    RunnableLambda,
    RunnableParallel,
    RunnablePassthrough,
    RunnableSerializable,
)

from gen_ai_orchestrator.models.prompt.prompt_formatter import PromptFormatter
from gen_ai_orchestrator.models.prompt.prompt_template import PromptTemplate
from gen_ai_orchestrator.models.rag.rag_models import (
    LLMAnswer,
    LLMCondensedQuestion,
)
from gen_ai_orchestrator.models.vector_stores.vector_store_provider import (
    VectorStoreProvider,
)
from gen_ai_orchestrator.models.vector_stores.vector_store_search_type import (
    DocumentSearchType,
)
from gen_ai_orchestrator.routers.requests.requests import RAGRequest
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    get_em_factory,
    get_llm_factory,
    get_vector_store_factory,
)
from gen_ai_orchestrator.services.utils.prompt_utility import (
    validate_prompt_template,
)

logger = logging.getLogger(__name__)


# ---------------------------------------------------------------------------
# Formatting helpers
# ---------------------------------------------------------------------------


def format_chat_history(x: dict) -> str:
    """Serialize the chat history to a JSON string for prompt injection."""
    messages = []
    for msg in x['chat_history']:
        if isinstance(msg, HumanMessage):
            messages.append({'user': msg.content})
        elif isinstance(msg, AIMessage):
            messages.append({'assistant': msg.content})
    return json.dumps(messages, ensure_ascii=False, indent=2)


def get_chunk_identifier(doc: Document) -> str:
    """Build the chunk identifier exposed to the LLM."""
    document_id = doc.metadata.get('id')
    chunk = doc.metadata.get('chunk')
    if document_id and chunk:
        return f"{document_id}:{chunk}"
    if document_id:
        return str(document_id)
    return str(chunk or '')


def get_web_source_url(doc: Document) -> Optional[str]:
    """Return the document source only when it is an HTTP(S) URL."""
    source = doc.metadata.get('source') or doc.metadata.get('reference')
    if not source:
        return None

    source_url = str(source).strip()
    parsed_url = urlparse(source_url)
    if parsed_url.scheme in {'http', 'https'} and parsed_url.netloc:
        return source_url
    return None


def format_rag_context_documents(
    documents: List[Document],
) -> List[dict[str, Optional[str]]]:
    """Format retrieved documents as the JSON context injected in the RAG prompt."""
    return [
        {
            'chunk_id': get_chunk_identifier(doc),
            'title': doc.metadata.get('title'),
            'source_url': get_web_source_url(doc),
            'chunk_text': doc.page_content,
        }
        for doc in documents
    ]

def format_documents_as_context(documents: list[Document]) -> str:
    """Serialize retrieved documents to a JSON string for prompt injection."""
    return json.dumps(
        format_rag_context_documents(documents),
        ensure_ascii=False,
        indent=2,
    )


# ---------------------------------------------------------------------------
# RRF ranking
# ---------------------------------------------------------------------------


def apply_rrf_ranking(
    ranked_results: list[list[Document]],
    k: int,
    top_n: int,
) -> list[Document]:
    """
    Reciprocal Rank Fusion over multiple ranked document lists.

    Each document is identified by (id, chunk).  Its RRF score is the sum of
    1 / (k + rank) across all lists in which it appears, then the top_n
    highest-scoring documents are returned.
    """

    def doc_key(doc: Document) -> tuple:
        return doc.metadata.get('id'), doc.metadata.get('chunk')

    # Accumulate RRF scores
    scores: dict[tuple, float] = {}

    for results in ranked_results:
        for rank, doc in enumerate(results, start=1):
            key = doc_key(doc)
            scores[key] = scores.get(key, 0.0) + 1.0 / (k + rank)

    # Deduplicate while preserving any Document instance
    unique_docs: dict[tuple, Document] = {}

    for results in ranked_results:
        for doc in results:
            key = doc_key(doc)

            if key not in unique_docs:
                unique_docs[key] = doc
            else:
                # Merge metadata from duplicate doc
                existing = unique_docs[key]

                for meta_key, meta_value in doc.metadata.items():
                    if meta_key not in existing.metadata:
                        existing.metadata[meta_key] = meta_value

    # Attach the computed score and sort
    ranked_docs = sorted(
        unique_docs.values(),
        key=lambda doc: scores[doc_key(doc)],
        reverse=True,
    )
    for rank, doc in enumerate(ranked_docs, start=1):
        doc.metadata['rank']['rrf'] = f"{rank}/{len(ranked_docs)}"

    return ranked_docs[:top_n]


# ---------------------------------------------------------------------------
# Hybrid retriever
# ---------------------------------------------------------------------------


class HybridRetriever:
    """
    Combines a vector-store retriever and a full-text-search retriever,
    then fuses their results with RRF.
    """

    def __init__(
        self, vector_retriever, fts_retriever, rrf_k: int = 60, rrf_top_n: int = 10
    ):
        self.vector_retriever = vector_retriever
        self.fts_retriever = fts_retriever
        self.rrf_k = rrf_k
        self.rrf_top_n = rrf_top_n

    async def retrieve(self, inputs: dict) -> list[Document]:
        condensed_question = inputs['chat_chain_result']['condensed_question']
        key_words = inputs['chat_chain_result']['key_words']

        # If no keywords are provided, do not run the FTS search
        # and do not perform RRF merging.
        if not key_words:
            logger.debug(
                'No keywords provided, skipping FTS retrieval and RRF ranking.'
            )

            docs_vector = await self.vector_retriever.ainvoke(
                input=condensed_question
            )

            # Preserve similarity rank metadata even when FTS and RRF are skipped.
            add_rank_metadata(docs=docs_vector, metadata_key='similarity')

            return docs_vector

        docs_vector, docs_fts = await asyncio.gather(
            self.vector_retriever.ainvoke(input=condensed_question),
            self.fts_retriever.ainvoke(
                input=self.fts_retriever.prepare_query(key_words)
            ),
        )

        add_rank_metadata(docs=docs_vector, metadata_key='similarity')
        add_rank_metadata(docs=docs_fts, metadata_key='fts')

        result = apply_rrf_ranking(
            [docs_vector, docs_fts],
            k=self.rrf_k,
            top_n=self.rrf_top_n,
        )

        return result


# ---------------------------------------------------------------------------
# Similarity retriever
# ---------------------------------------------------------------------------


class SimilarityRetriever:
    def __init__(self, vector_retriever):
        self.vector_retriever = vector_retriever

    async def retrieve(self, inputs: dict) -> list[Document]:
        condensed_question = inputs['chat_chain_result']['condensed_question']
        ranked_docs = await self.vector_retriever.ainvoke(input=condensed_question)

        return add_rank_metadata(
            docs=ranked_docs,
            metadata_key='similarity',
        )


# ---------------------------------------------------------------------------
# FTS retriever
# ---------------------------------------------------------------------------


class FTSRetriever:
    def __init__(
        self,
        fts_retriever,
    ):
        self.fts_retriever = fts_retriever

    async def retrieve(self, inputs: dict) -> list[Document]:
        key_words = inputs['chat_chain_result']['key_words']

        ranked_docs = await self.fts_retriever.ainvoke(
            input=self.fts_retriever.prepare_query(key_words)
        )

        return add_rank_metadata(
            docs=ranked_docs,
            metadata_key='fts',
        )


def add_rank_metadata(
    docs: list[Document],
    metadata_key: str,
) -> list[Document]:
    total = len(docs)

    for rank, doc in enumerate(docs, start=1):
        if 'rank' not in doc.metadata:
            doc.metadata['rank'] = {}
        doc.metadata['rank'][metadata_key] = f"{rank}/{total}"

    return docs


# ---------------------------------------------------------------------------
# Question-condensation chain
# ---------------------------------------------------------------------------


def build_question_condensation_chain(llm, prompt: PromptTemplate):
    """
    Return a chain that rewrites the user question into a stand-alone query
    taking the conversation history into account.
    """
    human_placeholder = (
        '{{ question }}' if prompt.formatter == PromptFormatter.JINJA2 else '{question}'
    )
    return (
        ChatPromptTemplate.from_messages(
            [
                ('system', prompt.template),
                MessagesPlaceholder(variable_name='chat_history'),
                ('human', human_placeholder),
            ],
            template_format=prompt.formatter.value,  # type: ignore[arg-type]
        ).partial(**prompt.inputs)
        | llm
        | JsonOutputParser(
            pydantic_object=LLMCondensedQuestion,
            name='rag_question_condensation_chain_output',
        )
    )


# ---------------------------------------------------------------------------
# Main chain factory
# ---------------------------------------------------------------------------


def create_rag_chain(
    request: RAGRequest,
    vector_db_async_mode: Optional[bool] = True,
) -> RunnableSerializable[Any, dict[str, Any]]:
    # -- Validate prompts --------------------------------------------------
    validate_prompt_template(
        request.question_condensing_prompt, 'Question condensing prompt'
    )
    validate_prompt_template(
        request.question_answering_prompt, 'Question answering prompt'
    )

    # -- Build components --------------------------------------------------
    question_condensing_llm = get_llm_factory(
        setting=request.question_condensing_llm_setting
    ).get_language_model()

    question_answering_llm = get_llm_factory(
        setting=request.question_answering_llm_setting
    ).get_language_model()

    embedding_model = get_em_factory(
        setting=request.embedding_question_em_setting
    ).get_embedding_model()

    vector_store_factory = get_vector_store_factory(
        setting=request.vector_store_setting,
        index_name=request.document_index_name,
        embedding_function=embedding_model,
    )

    search_kwargs = request.document_search_params.to_dict()

    if (
        VectorStoreProvider.OPEN_SEARCH == request.document_search_params.provider
        or DocumentSearchType.SIMILARITY_SEARCH
        == request.document_search_params.search_type
    ):
        similarity_retriever = SimilarityRetriever(
            vector_retriever=vector_store_factory.get_vector_store_retriever(
                search_kwargs=search_kwargs,
                async_mode=vector_db_async_mode,
            )
        )

        retriever = RunnableLambda(
            name='similarity_retriever_retrieve', func=similarity_retriever.retrieve
        )

    elif DocumentSearchType.HYBRID_SEARCH == request.document_search_params.search_type:
        vector_retriever = vector_store_factory.get_vector_store_retriever(
            search_kwargs=search_kwargs,
            async_mode=vector_db_async_mode,
        )
        fts_retriever = vector_store_factory.get_text_store_retriever(
            search_kwargs=search_kwargs,
            async_mode=vector_db_async_mode,
        )

        hybrid_retriever = HybridRetriever(
            vector_retriever=vector_retriever,
            fts_retriever=fts_retriever,
            rrf_top_n=request.document_search_params.k,
        )

        retriever = RunnableLambda(
            name='hybrid_retrieve', func=hybrid_retriever.retrieve
        )

    else:
        # DocumentSearchType.FULL_TEXT_SEARCH == request.document_search_params.search_type
        fts_as_retriever = FTSRetriever(
            fts_retriever=vector_store_factory.get_text_store_retriever(
                search_kwargs=search_kwargs,
                async_mode=vector_db_async_mode,
            )
        )

        retriever = RunnableLambda(name='fts_retrieve', func=fts_as_retriever.retrieve)

    condensation_chain = build_question_condensation_chain(
        question_condensing_llm, request.question_condensing_prompt
    )

    rag_prompt = LangChainPromptTemplate.from_template(
        template=request.question_answering_prompt.template,
        template_format=request.question_answering_prompt.formatter.value,  # type: ignore[arg-type]
        partial_variables=request.question_answering_prompt.inputs,
    )

    # -- Assemble pipeline -------------------------------------------------
    with_condensed_question = RunnableParallel(
        {
            'chat_chain_result': condensation_chain,
            'question': itemgetter('question'),
            'chat_history': itemgetter('chat_history'),
        }
    )

    rag_inputs = with_condensed_question | RunnableParallel(
        {
            'question': lambda x: x['chat_chain_result']['condensed_question'],
            'key_words': lambda x: x['chat_chain_result']['key_words'],
            'chat_history': itemgetter('chat_history'),
            'documents': retriever,
        }
    )

    answer_chain = (
        {
            'context': lambda x: format_documents_as_context(x['documents']),
            'chat_history': format_chat_history,
        }
        | rag_prompt
        | question_answering_llm
        | JsonOutputParser(pydantic_object=LLMAnswer, name='rag_chain_output')
    )

    return rag_inputs | RunnablePassthrough.assign(answer=answer_chain)

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
import logging
from typing import Any, Optional, Union

from langchain_core.callbacks import (
    AsyncCallbackManagerForRetrieverRun,
    CallbackManagerForRetrieverRun,
)
from langchain_core.documents import Document
from langchain_postgres import PGVector
from langchain_postgres.vectorstores import _get_embedding_collection_store
from pydantic import ConfigDict
from sqlalchemy import Engine, Select, cast, func, literal_column, select
from sqlalchemy.dialects.postgresql import REGCONFIG, TSVECTOR
from sqlalchemy.ext.asyncio import AsyncEngine

from gen_ai_orchestrator.services.langchain.factories.vector_stores.full_text_search_retriever import (
    FullTextSearchRetriever,
)

logger = logging.getLogger(__name__)


class _MetadataFilterBuilder(PGVector):
    """
    Translates a metadata filter into an SQL clause exactly like PGVector does
    for similarity search, without connecting to the database.
    Relies on langchain-postgres internals: check it when upgrading the library.
    """

    def __init__(self):
        self.EmbeddingStore, self.CollectionStore = _get_embedding_collection_store()

    def create_filter_clause(self, metadata_filter: dict) -> Any:
        return self._create_filter_clause(metadata_filter)


def build_docs(rows) -> list[Document]:
    docs = [Document(page_content=row.document, metadata=row.cmetadata) for row in rows]

    return docs


def build_statement(
    query: str,
    language: str,
    table_name: str,
    k: int,
    metadata_filter: Optional[dict] = None,
) -> Select:
    filter_builder = _MetadataFilterBuilder()
    embedding_store = filter_builder.EmbeddingStore
    collection_store = filter_builder.CollectionStore

    # fts_vector is added by the Tock schema, it is not mapped by langchain-postgres
    fts_vector = literal_column(
        f'{embedding_store.__tablename__}.fts_vector', type_=TSVECTOR
    )
    ts_query = func.websearch_to_tsquery(cast(language, REGCONFIG), query)
    score = func.ts_rank(fts_vector, ts_query).label('score')

    conditions = [
        collection_store.name == table_name,
        fts_vector.op('@@')(ts_query),
    ]
    if metadata_filter:
        filter_clause = filter_builder.create_filter_clause(metadata_filter)
        if filter_clause is not None:
            conditions.append(filter_clause)

    return (
        select(embedding_store.document, embedding_store.cmetadata, score)
        .join(
            collection_store,
            collection_store.uuid == embedding_store.collection_id,
        )
        .where(*conditions)
        .order_by(score.desc())
        .limit(k)
    )


class PostgreSQLTextRetriever(FullTextSearchRetriever):
    model_config = ConfigDict(arbitrary_types_allowed=True)

    engine: Union[Engine, AsyncEngine]
    table_name: str
    language: str = 'french'
    k: int = 10
    metadata_filter: Optional[dict] = None

    def build_statement(self, query: str) -> Select:
        return build_statement(
            query=query,
            language=self.language,
            table_name=self.table_name,
            k=self.k,
            metadata_filter=self.metadata_filter,
        )

    def _get_relevant_documents(
        self, query: str, *, run_manager: CallbackManagerForRetrieverRun
    ) -> list[Document]:
        logger.debug('Query : %s ', query)
        with self.engine.connect() as conn:
            rows = conn.execute(self.build_statement(query)).fetchall()
        return build_docs(rows)

    async def _aget_relevant_documents(
        self, query: str, *, run_manager: AsyncCallbackManagerForRetrieverRun
    ) -> list[Document]:
        logger.debug('Query : %s ', query)
        async with self.engine.connect() as conn:
            result = await conn.execute(self.build_statement(query))
            rows = result.fetchall()
        return build_docs(rows)

    def prepare_query(self, keywords: list[str]) -> str:
        parts = []

        for kw in keywords:
            if not kw or not kw.strip():
                continue
            parts.append(kw.replace("'", "''").strip())

        return ' OR '.join(parts)

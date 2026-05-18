import logging
from typing import Union

from langchain_core.callbacks import (
    CallbackManagerForRetrieverRun,
    AsyncCallbackManagerForRetrieverRun,
)
from langchain_core.documents import Document
from pydantic import ConfigDict
from sqlalchemy import Engine, text, TextClause
from sqlalchemy.ext.asyncio import AsyncEngine

from gen_ai_orchestrator.services.langchain.factories.vector_stores.full_text_search_retriever import (
    FullTextSearchRetriever,
)

logger = logging.getLogger(__name__)


def build_docs(rows) -> list[Document]:
    docs = [Document(page_content=row.document, metadata=row.cmetadata) for row in rows]

    return docs


def build_sql() -> TextClause:
    return text("""
        WITH q AS (
            SELECT websearch_to_tsquery(:language, unaccent(:query)) AS ts_query
        )
        SELECT
            d.document,
            d.cmetadata,
            ts_rank(d.fts_vector, q.ts_query) AS score
        FROM langchain_pg_embedding d
        JOIN langchain_pg_collection lpc ON lpc.uuid = d.collection_id
        CROSS JOIN q
        WHERE lpc.name = :table_name
          AND d.fts_vector @@ q.ts_query
        ORDER BY score DESC
        LIMIT :k
    """)


class PostgreSQLTextRetriever(FullTextSearchRetriever):
    model_config = ConfigDict(arbitrary_types_allowed=True)

    engine: Union[Engine, AsyncEngine]
    table_name: str
    language: str = "french"
    k: int = 10

    def build_params(self, query: str) -> dict:
        return {
            "query": query,
            "language": self.language,
            "table_name": self.table_name,
            "k": self.k,
        }

    def _get_relevant_documents(
        self, query: str, *, run_manager: CallbackManagerForRetrieverRun
    ) -> list[Document]:
        logger.debug("Query : %s ", query)
        with self.engine.connect() as conn:
            rows = conn.execute(build_sql(), self.build_params(query)).fetchall()
        return build_docs(rows)

    async def _aget_relevant_documents(
        self, query: str, *, run_manager: AsyncCallbackManagerForRetrieverRun
    ) -> list[Document]:
        logger.debug("Query : %s ", query)
        async with self.engine.connect() as conn:
            result = await conn.execute(build_sql(), self.build_params(query))
            rows = result.fetchall()
        return build_docs(rows)

    def prepare_query(self, keywords: list[str]) -> str:
        parts = []

        for kw in keywords:
            if not kw or not kw.strip():
                continue
            parts.append(kw.replace("'", "''").strip())

        return " OR ".join(parts)

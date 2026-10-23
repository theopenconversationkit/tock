#   Copyright (C) 2023-2026 Credit Mutuel Arkea
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
"""Model for creating PGVectorFactory"""

import logging
from typing import Optional

from langchain_core.vectorstores import VectorStoreRetriever
from langchain_postgres import PGVector
from sqlalchemy import text

from gen_ai_orchestrator.configurations.environment.settings import (
    application_settings,
)
from gen_ai_orchestrator.models.vector_stores.pgvector.database_pool_registry import (
    DatabasePool,
)
from gen_ai_orchestrator.models.vector_stores.pgvector.pgvector_setting import (
    PGVectorStoreSetting,
)
from gen_ai_orchestrator.services.langchain.factories.vector_stores.full_text_search_retriever import (
    FullTextSearchRetriever,
)
from gen_ai_orchestrator.services.langchain.factories.vector_stores.postgresql_text_retriever import (
    PostgreSQLTextRetriever,
)
from gen_ai_orchestrator.services.langchain.factories.vector_stores.vector_store_factory import (
    LangChainVectorStoreFactory,
)

logger = logging.getLogger(__name__)


class PGVectorFactory(LangChainVectorStoreFactory):
    """
    A class for LangChain PGVector Factory
    https://api.python.langchain.com/en/latest/vectorstores/langchain_postgres.vectorstores.PGVector.html
    """

    """
    Pool injected from the registry — no connection created here.
    """
    pool: DatabasePool

    setting: PGVectorStoreSetting

    def get_vector_store(
        self,
        async_mode: Optional[bool] = True,
        collection_metadata: Optional[dict] = None,
    ) -> PGVector:
        engine = self.pool.async_engine if async_mode else self.pool.sync_engine
        # PGVector's constructor calls get_or_create: if the collection is absent it is created, born with this
        # collection_metadata (set once, never overwritten on later opens). Callers that must not create a collection
        # (existence probes) never go through here — they read langchain_pg_collection directly.
        return PGVector(
            embeddings=self.embedding_function,
            collection_name=self.index_name,
            connection=engine,
            use_jsonb=True,
            async_mode=async_mode,
            collection_metadata=collection_metadata,
        )

    def read_index_metadata(self) -> Optional[dict]:
        # PGVector reads langchain_pg_collection.cmetadata
        # Read langchain_pg_collection directly on the sync engine, never through PGVector: going through PGVector
        # would get_or_create (and thus create) the collection, which must not happen on a read.
        engine = self.pool.sync_engine
        with engine.connect() as connection:
            # Fresh database: the langchain tables are created lazily on first write. Absent table => no collection.
            if (
                connection.execute(
                    text("SELECT to_regclass('langchain_pg_collection')")
                ).scalar()
                is None
            ):
                return None
            collection = connection.execute(
                text('SELECT cmetadata FROM langchain_pg_collection WHERE name = :name'),
                {'name': self.index_name},
            ).first()
        if collection is None:
            return None
        return collection.cmetadata or {}

    def get_vector_store_retriever(
        self, search_kwargs: dict, async_mode: Optional[bool] = True
    ) -> VectorStoreRetriever:
        return self.get_vector_store(async_mode).as_retriever(
            search_kwargs=search_kwargs
        )

    def get_text_store_retriever(
        self, search_kwargs: dict, async_mode: bool = True
    ) -> FullTextSearchRetriever:
        engine = self.pool.async_engine if async_mode else self.pool.sync_engine
        return PostgreSQLTextRetriever(
            engine=engine,
            table_name=self.index_name,
            k=search_kwargs.get('k', 10),
            metadata_filter=search_kwargs.get('filter'),
        )

    async def check_vector_store_connection(self) -> bool:
        """
        To check the connection information, we perform a vector search :The objective is not the search result itself.
        We do this because the langchain implementation doesn't provide us with a method on the
        VectorStore to check the connection independently.
        """
        await self.get_vector_store().asimilarity_search(
            query=application_settings.vector_store_test_query,
            k=application_settings.vector_store_test_max_docs_retrieved,
        )
        return True

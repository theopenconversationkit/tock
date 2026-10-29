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
from unittest.mock import MagicMock

from sqlalchemy.dialects import postgresql
from sqlalchemy.ext.asyncio import AsyncEngine

from gen_ai_orchestrator.services.langchain.factories.vector_stores.pgvector_factory import (
    PGVectorFactory,
)
from gen_ai_orchestrator.services.langchain.factories.vector_stores.postgresql_text_retriever import (
    FTS_SCORE_METADATA_KEY,
    build_docs,
    build_statement,
)


def compile_statement(metadata_filter=None):
    compiled = build_statement(
        query="tarif OR électricité",
        language="french",
        table_name="my-index",
        k=5,
        metadata_filter=metadata_filter,
    ).compile(dialect=postgresql.dialect())
    return str(compiled), compiled.params


def test_build_statement_without_filter():
    sql, params = compile_statement()

    assert "langchain_pg_embedding.fts_vector @@ websearch_to_tsquery" in sql
    assert "langchain_pg_collection.name =" in sql
    assert "jsonb_path_match" not in sql
    assert "unaccent" not in sql
    assert "french" in params.values()
    assert "tarif OR électricité" in params.values()
    assert "my-index" in params.values()
    assert 5 in params.values()


def test_build_statement_with_equality_filter():
    sql, params = compile_statement({"source": "https://doc.tock.ai"})

    assert "jsonb_path_match(langchain_pg_embedding.cmetadata" in sql
    assert "$.source == $value" in params.values()


def test_build_statement_with_operator_filter():
    sql, params = compile_statement({"$and": [{"source": {"$in": ["a", "b"]}}, {"title": {"$ne": "c"}}]})

    assert " AND " in sql
    assert "$.title != $value" in params.values()
    assert any(isinstance(value, (list, tuple)) and set(value) == {"a", "b"} for value in params.values())


def test_pgvector_factory_passes_filter_to_text_retriever():
    pool = MagicMock()
    pool.async_engine = MagicMock(spec=AsyncEngine)
    factory = PGVectorFactory.model_construct(
        setting=MagicMock(),
        index_name="my-index",
        embedding_function=MagicMock(),
        pool=pool,
    )

    retriever = factory.get_text_store_retriever(search_kwargs={"k": 3, "filter": {"source": "https://doc.tock.ai"}})

    assert retriever.table_name == "my-index"
    assert retriever.k == 3
    assert retriever.metadata_filter == {"source": "https://doc.tock.ai"}


def test_build_docs_keeps_the_ts_rank_without_touching_the_stored_metadata():
    stored = {"id": "doc", "title": "Doc"}
    row = MagicMock(document="content", cmetadata=stored, score=0.42)

    [document] = build_docs([row])

    assert document.page_content == "content"
    assert document.metadata == {"id": "doc", "title": "Doc", FTS_SCORE_METADATA_KEY: 0.42}
    # The row's metadata is copied, never mutated.
    assert stored == {"id": "doc", "title": "Doc"}


def test_build_docs_tolerates_missing_metadata():
    row = MagicMock(document="content", cmetadata=None, score=1.0)

    [document] = build_docs([row])

    assert document.metadata == {FTS_SCORE_METADATA_KEY: 1.0}

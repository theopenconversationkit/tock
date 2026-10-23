CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS langchain_pg_collection (
    uuid UUID NOT NULL,
    name VARCHAR NOT NULL,
    cmetadata JSON,
    CONSTRAINT langchain_pg_collection_pkey PRIMARY KEY (uuid),
    CONSTRAINT langchain_pg_collection_name_key UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS langchain_pg_embedding (
    id VARCHAR NOT NULL,
    collection_id UUID,
    embedding VECTOR,
    document VARCHAR,
    cmetadata JSONB,
    CONSTRAINT langchain_pg_embedding_pkey PRIMARY KEY (id),
    CONSTRAINT langchain_pg_embedding_collection_id_fkey
        FOREIGN KEY (collection_id)
        REFERENCES langchain_pg_collection (uuid)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_cmetadata_gin
    ON langchain_pg_embedding
    USING GIN (cmetadata jsonb_path_ops);

CREATE EXTENSION IF NOT EXISTS unaccent;

ALTER TABLE langchain_pg_embedding
    ADD COLUMN IF NOT EXISTS fts_vector tsvector
    GENERATED ALWAYS AS (
    to_tsvector('french', COALESCE(document, ''))
) STORED;

-- Replace single-column FTS index with composite index (scoped by collection)
DROP INDEX IF EXISTS idx_fts_vector;

CREATE EXTENSION IF NOT EXISTS btree_gin;

CREATE INDEX IF NOT EXISTS idx_fts_vector_composite
    ON langchain_pg_embedding
    USING GIN (collection_id, fts_vector);

-- Single-column B-tree index on collection_id for vector similarity search joins
CREATE INDEX IF NOT EXISTS idx_embedding_collection_id
    ON langchain_pg_embedding (collection_id);

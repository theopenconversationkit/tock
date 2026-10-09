---
title: Vector store providers
---

# Vector store providers

The vector store holds the indexed documents (split into chunks, with their embeddings) that the [RAG](../rag.md)
searches to answer questions. It is configured in the [_Vector DB settings_](../vector-store.md) menu.

Here are the vector stores supported by Tock:

| Provider | `provider` value | Similarity search | Full text search | Hybrid search | [Inspection](../vector-store-inspection.md) |
|----------|------------------|:-----------------:|:----------------:|:-------------:|:----------:|
| [PGVector](https://github.com/pgvector/pgvector) (PostgreSQL) | `PGVector` | ✅ | ✅ | ✅ | ✅ |
| [OpenSearch](https://opensearch.org/) | `OpenSearch` | ✅ | | | vector search only |

With OpenSearch, only similarity search is available: choosing full text or hybrid search
in the [RAG settings](../rag.md#indexing-session) makes RAG requests fail.

## PGVector

```json
{
  "provider": "PGVector",
  "host": "localhost",
  "port": 5432,
  "username": "postgres",
  "password": {
    "type": "Raw",
    "secret": "postgres"
  },
  "database": "postgres"
}
```

Full text and hybrid searches require the full text search column and index of the Tock schema.
The schema is provided in
[`gen-ai/orchestrator-server/sql/schema.sql`](https://github.com/theopenconversationkit/tock/blob/master/gen-ai/orchestrator-server/sql/schema.sql):
it creates the tables used by the indexing tools, and a `fts_vector` column generated from the chunk contents
(with the `french` text search configuration), with its index.
This script is not run automatically: it must be applied to the database, including one that is already indexed
(the column is then computed for the existing chunks).

Full text search does not use the question itself, but the keywords (`key_words`) produced by the
question condensing prompt (see the [RAG settings](../rag.md)). If this prompt returns no keywords,
full text search finds no documents, and hybrid search falls back to similarity search only.

## OpenSearch

```json
{
  "provider": "OpenSearch",
  "host": "localhost",
  "port": 9200,
  "username": "admin",
  "password": {
    "type": "Raw",
    "secret": "*************"
  }
}
```

Passwords can also be stored in a secret manager: see [API keys and secret managers](llm-embedding.md#api-keys-and-secret-managers).

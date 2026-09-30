---
title: Vector store collection metadata
---

# Vector store collection metadata

When Tock creates a vector store collection for a bot's **knowledge base** index, it stamps the collection with a small
set of *contract metadata*. This metadata lets Tock recognise the collections it owns, tell which embedding model they
were built with, and detect when a bot's configuration has drifted away from the collection it points at.

!!! note
    Collection metadata is a **PGVector-only** concept. OpenSearch indices expose no equivalent, so none of the checks
    below apply to them: their embedding coherence is always considered *unknown* and never blocks a save.

## The contract

The metadata is stored in the PGVector `langchain_pg_collection.cmetadata` JSON column. Tock writes the following keys:

| Key                  | Type    | Required | Meaning                                                                        |
|----------------------|---------|----------|--------------------------------------------------------------------------------|
| `schema_version`     | integer | yes      | Version of this contract. Currently `1`. Its presence marks a Tock collection. |
| `created_at`         | string  | yes      | Creation instant, ISO-8601 in UTC, truncated to the second.                    |
| `origin`             | string  | yes      | Always `tock_kb` for a knowledge base index created by Tock.                   |
| `created_by`         | string  | no       | Login of the user who requested the creation, when known.                      |
| `embedding_provider` | string  | yes      | Embedding provider used at creation (e.g. `OpenAI`, `AzureOpenAIService`, `Ollama`). |
| `embedding_model`    | string  | no       | Normalised embedding model name, omitted when the model is unknown.            |

**"Has Tock contract metadata"** means the `cmetadata` contains a `schema_version` key. A collection without it — an
older collection, or one created by another tool — is treated as *not* Tock-owned for every check below.

## Set once, never updated

Contract metadata is written **only when the collection is created**, by the first write of a `CREATE_INDEX` job. It is
never updated afterwards and never back-filled onto pre-existing collections:

- PGVector's constructor calls `get_or_create`, which sets `cmetadata` when it creates the collection but never
  overwrites it on later opens.
- Any operation that must *not* create a collection (existence and state probes) reads
  `langchain_pg_collection`/`langchain_pg_embedding` directly with SQL and never goes through the PGVector constructor,
  which would otherwise create an empty, uncertified collection as a side effect.

As a consequence, an empty PGVector collection *without* contract metadata is considered **missing**: it was almost
certainly created implicitly by a runtime query rather than by a Tock `CREATE_INDEX` job.

## Normalised embedding model

The `embedding_model` value, and every embedding comparison, uses a single normalisation rule so that the same model is
recognised regardless of provider quirks:

- **OpenAI** / **Ollama**: the configured model name.
- **Azure OpenAI**: the model name when it is non-blank, otherwise `null` (the *deployment name* is never used).
- The value is trimmed; an empty result becomes `null`.
- For **Ollama**, a trailing `:latest` tag is stripped.

## Embedding coherence

To decide whether a bot may safely (re-)use a collection, Tock compares the **normalised** embedding model stored on the
collection with the **normalised** model of the bot's current embedding setting:

- Both known and equal → **MATCH**.
- Both known and different → **MISMATCH**.
- Either side unknown (no model, or a collection with no contract metadata) → **UNKNOWN**.

The provider is **not** part of the comparison. Only **MISMATCH** is blocking; **UNKNOWN** never blocks — it is the
deliberately safe default that keeps the studio usable against older collections and OpenSearch.

## Where the checks are used

- **Knowledge base indexing**: a write into a collection whose embedding no longer matches the bot is refused rather than
  silently mixing incompatible vectors.
- **RAG settings save**: pointing a bot at a collection (or changing its embedding) whose embedding is a MISMATCH is
  rejected as a bad request.
- **Index state**: `NONE` (no index configured), `MISSING` (configured but the collection is absent, or empty with no
  contract metadata), or `READY`.

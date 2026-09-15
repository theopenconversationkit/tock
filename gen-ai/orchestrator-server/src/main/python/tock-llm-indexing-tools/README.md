# TOCK LLM Indexing Tools

Tools to index documents into the TOCK RAG vector database.

This project currently provides one tool, `run_vectorisation.py`, which reads a CSV file, chunks its contents, computes embeddings and indexes the resulting chunks into a vector store (OpenSearch or PGVector).

## Prerequisites

- Python `^3.13`
- [Poetry](https://python-poetry.org/)
- The Gen AI Orchestrator server sources, expected at `../server` (this project depends on the `gen_ai_orchestrator` package as a local path dependency)

## Installation

```bash
poetry env use ~/.pyenv/versions/3.13.x/bin/python
poetry install
```

To export the dependencies as a `requirements.txt` (used by the Maven packaging), run:

```bash
./export_deps_as_requirements.sh
```

## Usage

```bash
poetry run python scripts/indexing/vectorisation/run_vectorisation.py --json-config-file=path/to/config.json
```

Options:

| Option                     | Description                                    |
|----------------------------|------------------------------------------------|
| `--json-config-file=<jcf>` | Path to the JSON configuration file (required) |
| `-v`                       | Verbose output (debug level)                   |
| `-h`, `--help`             | Show help                                      |
| `--version`                | Show version                                   |

Logs are written to the console and to a timestamped file: `logs/log_%Y-%m-%d_%Hh%Mm%S.log`. The input/output summaries are logged at debug level (`-v`).

### Input CSV

The CSV file must be pipe-delimited (`|`), with `"` as quote character, and contain the columns `title`, `source` and `text`. Rows with an empty `text` are ignored. The file is read from `{file_location}/{namespace}-{bot_id}/input/{data_csv_file}` (see configuration below).

### Configuration file

See [`run_vectorisation.example.json`](scripts/indexing/vectorisation/run_vectorisation.example.json) for a complete example.

| Field                        | Description                                                                                                                                                                                  |
|------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `bot`                        | Bot info: `namespace`, `bot_id` and `file_location` (base folder for inputs/outputs)                                                                                                         |
| `em_setting`                 | Embedding model settings (provider, model, API key, ...) — see [Embedding Settings](../server/src/gen_ai_orchestrator/models/em/em_types.py)                                                 |
| `vector_store_setting`       | Vector store settings (`OpenSearch` or `PGVector`: host, port, credentials, ...) — see [Vector Store Settings](../server/src/gen_ai_orchestrator/models/vector_stores/vector_store_types.py) |
| `data_csv_file`              | Name of the input CSV file                                                                                                                                                                   |
| `document_index_name`        | Index name to use; when `null`, a name is generated as `ns-{namespace}-bot-{bot_id}-session-{uuid4}`, normalized according to the vector store's naming rules                                |
| `chunk_size`                 | Chunk size in characters (minimum 500)                                                                                                                                                       |
| `embedding_bulk_size`        | Number of chunks embedded and indexed per bulk request                                                                                                                                       |
| `embedding_max_chunks`       | Maximum number of chunks to index (useful for testing); `null` indexes everything                                                                                                            |
| `ignore_source`              | When `true`, the `source` column is not stored as document source                                                                                                                            |
| `append_doc_title_and_chunk` | When `true`, the document title is prepended to each chunk before vectorisation (can improve semantic search)                                                                                |

### Indexed metadata

Each chunk is indexed with the following metadata:

| Metadata tag       | Description                                                                           |
|--------------------|---------------------------------------------------------------------------------------|
| `index_session_id` | UUID of the indexing session (one per script run)                                     |
| `index_datetime`   | Date of the indexing session                                                          |
| `id`               | UUID of each document (one per line of the input CSV)                                 |
| `chunk`            | Chunk number when the document was split: `n/N`                                       |
| `title`            | The `title` column from the input CSV                                                 |
| `source`           | The `source` column from the input CSV (set to `None` when `ignore_source` is `true`) |
| `reference`        | Always keeps the original `source` value, whether the source is ignored or not        |

### Sample output

Verbose run (`-v`) with `embedding_max_chunks` set to 30:

<pre>
------------------------------- RUN VECTORISATION INPUT -------------------------------
Bot                     : my-ns - my-bot
The EM model            : text-embedding-3-small (EMProvider.AZURE_OPEN_AI_SERVICE)
The Vector DB           : localhost (VectorStoreProvider.PGVECTOR)
The data csv path       : my_data.csv
Chunk size              : 7000
Embedding bulk size     : 10
Max chunks to index     : 30
Ignoring sources        : False
Append title and chunk  : False
---------------------------------------------------------------------------------------
Limiting indexation to the first 30 chunks (out of 171)
Vectorized chunks : 10 / 30
Vectorized chunks : 20 / 30
Vectorized chunks : 30 / 30
------------------------------- RUN VECTORISATION OUTPUT ------------------------------
Index name             : ns_my_ns_bot_my_bot_session_b32b83d0_2719_4975_a9ae_2b57415ca619
Index session ID       : b32b83d0-2719-4975-a9ae-2b57415ca619
Documents extracted    : 146 (Docs)
Documents chunked      : 30 (Chunks)
Duration               : 2.28 seconds
Date                   : 2026-09-11 18:03:12
---------------------------------------------------------------------------------------
Status                 : COMPLETED
---------------------------------------------------------------------------------------
</pre>

## Development

The project uses [pre-commit](https://pre-commit.com/) (black, isort, pycln, license headers, pip-audit):

```bash
poetry run pre-commit install
```

## License

Apache License 2.0 — Copyright (C) Credit Mutuel Arkea

---
title: Document indexing
---

# Document indexing

The [RAG](rag.md) answers from documents stored in a [vector store](providers/vector-store.md).
Documents are indexed with the `tock-llm-indexing-tools` Python project, available in the Tock repository:
[`gen-ai/orchestrator-server/src/main/python/tock-llm-indexing-tools`](https://github.com/theopenconversationkit/tock/tree/master/gen-ai/orchestrator-server/src/main/python/tock-llm-indexing-tools).

Its `run_vectorisation.py` script reads a CSV file, splits the documents into chunks, computes their embeddings
and stores them in the vector store (PGVector or OpenSearch).

## Input documents

The CSV file is pipe-delimited (`|`), with `"` as quote character, and contains the columns:

* `title`: the document title,
* `source`: the document source, typically its URL, displayed as the source of the answers,
* `text`: the document content.

## Running the indexing

```bash
poetry install
poetry run python scripts/indexing/vectorisation/run_vectorisation.py --json-config-file=path/to/config.json -v
```

The JSON configuration file sets the bot (namespace and bot ID), the embedding model and vector store settings
(same format as in [the providers pages](providers/llm-embedding.md)), the input file, the index name
and the chunking options (chunk size, bulk size, etc.).
See the [README](https://github.com/theopenconversationkit/tock/blob/master/gen-ai/orchestrator-server/src/main/python/tock-llm-indexing-tools/README.md)
for the complete list of options.

Each run is an **indexing session**, with its own ID and its own index. At the end of the run, the script displays
the index name and the index session ID (with the `-v` option; without it, the script only displays errors).

Enter the index session ID in the [RAG settings](rag.md) of the bot to use the new documents.
The embedding model configured in the RAG settings must be the one used for the indexing.

A complete example, from Markdown files to a working bot, is given in the tutorial
[Build a RAG bot on the Tock documentation](../getting-started/rag-tutorial.md).

## Checking the result

The [vector store exploration](vector-store-inspection.md#vector-store-exploration) shows the content of an index
(documents, chunks) and detects common problems: nearly empty chunks, sources that are not URLs, duplicate titles.

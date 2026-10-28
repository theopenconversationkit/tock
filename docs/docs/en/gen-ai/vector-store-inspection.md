---
title: Gen AI - Vector store inspection
description: "Inspect what the vector store of a bot contains, and why a chunk is or is not given to the LLM."
---

# Vector store inspection

The vector store inspection tools show what the vector store of the bot actually contains,
and why a given chunk is (or is not) handed to the answering model for a question.
They are available in the _Gen AI_ menu:

* [_Vector store exploration_](#vector-store-exploration): what does the index contain?
* [_Retrieval diagnostic_](#retrieval-diagnostic): why this answer? The whole retrieval chain for a question,
  from the store to the chunks sent to the model.

> To access these pages, you need the **_admin_** role
> (more details on roles in [security](../operate/security.md#roles)).
>
> The inspection is available for the [PGVector](providers/vector-store.md) provider. With OpenSearch, only the vector
> search mode is available: full text and hybrid modes are not implemented for this provider, and the diagnostic uses
> vector search whatever mode the bot is configured with (as the runtime does).

## Vector store exploration

Select an index (one index per indexing session) to display its ingestion report:

* statistics: number of documents and chunks, average number of chunks per document, median chunk length,
* anomalies, usable as filters:
    * _nearly empty chunks_: chunks whose content is too short to be usable,
    * _non-URL sources_: documents whose source is not a URL, and cannot be used as a link in answers,
    * _duplicate titles_: identical titles carried by different documents,
* the paginated list of documents, which can be filtered by title, identifier or content, and expanded to show their chunks.

A chunk can be **pinned**: pinned chunks are tracked in the retrieval diagnostic, where they always appear in the results,
even when no search channel returns them. The _Diagnose_ button opens the diagnostic with the pinned chunks.

## Retrieval diagnostic

The diagnostic screen follows the runtime retrieval chain, from top to bottom:

1. **Question**: the question as a user would ask it. It can be condensed (the condensed question and the keywords
   are filled by the condensing LLM, and can be edited). Condensation is not deterministic: the search uses the displayed
   values as they are, so a search can be replayed identically.
2. **Retrieval**: the index and the search mode (_Vector_, _Full text_ or _Hybrid_; full text and hybrid modes require
   keywords). The search mode is taken from the bot configuration and shown read-only.
3. **Context**: _Max documents retrieved_ (`k`), the number of candidates each channel returns and the pool handed to the compressor.
4. **Compression**: whether to apply the [document compressor](compressor.md), with its minimum score
   and maximum number of documents.

> The form starts from the bot's actual retrieval settings; a banner lists any setting you changed and offers to reset
> it to the bot's. The compressor runs on the retrieved documents, so its _maximum number of documents_ cannot exceed
> _Max documents retrieved_. Changing the minimum score or the maximum number of documents is recomputed instantly in the
> browser, without a new search: every candidate's reranker score is shown with the threshold marker, so the minimum can
> be chosen by eye (the score scale depends on the reranker model).
>
> With the maximum number of documents equal to _Max documents retrieved_, no minimum score and no padding, the
> compressor keeps every chunk and only reorders them: a message points it out.
>
> The reranker is called with the same timeout as in production: retrieving more documents increases the reranking time
> and may trigger a fallback, as it would in production.

### Funnel and results

The funnel shows each stage with its number of chunks: vector and full text channels (in hybrid mode), RRF fusion,
the top-k cut, then compression (with its duration). When the compressor fails, it falls back as in production: it
passes on the first documents in retrieval order, unscored, up to its maximum number of documents. The stage is then
shown as _failed, fell back_ with the reason (reranker error, failed call, or question too long for the reranker); the
error message is available as a tooltip. The thresholds cannot be retuned until a new search returns scores.

The results table shows one chunk per line, with its rank and score for each channel (vector, full text, RRF, rerank),
the number of windows an oversized chunk was scored across, and its outcome:

| Outcome          | Meaning                                                                         |
|------------------|---------------------------------------------------------------------------------|
| kept             | Present in the context handed to the answering model                            |
| cut              | Returned by the search, but dropped by the top-k cut                            |
| below threshold  | Reached the compressor but scored below the minimum                             |
| ranked out       | Above the threshold, but ranked past the maximum number of documents           |
| padded in        | Scored below the threshold, but pulled back to fill the document count          |
| cut by fallback  | Compressor fell back: past the first documents passed on unscored               |
| absent           | Returned by no search channel at all                                            |

### Comparing searches

_Set as reference_ freezes a search as the baseline for the next ones. The following searches are then compared with it:
the parameters that changed (index, search mode, question, keywords, max documents retrieved, compression — including the
minimum score and maximum number of documents) and the chunks that were lost, gained or moved. A client-side threshold
change produces a new comparable search without a backend call. Two kinds of absence are distinguished:

* _absent from index_: the two searches target different indexes and the chunk does not exist in the current one (an ingestion problem),
* _outside top k_: the chunk still exists, but no longer surfaces within the retrieved window (a ranking problem).

Comparisons are computed in the browser and are not saved.

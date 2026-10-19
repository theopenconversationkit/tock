---
title: Compressor settings
description: "Configure a document compressor (reranker) to keep only the most relevant documents for the LLM."
---

# The _Compressor settings_ menu

A _document compressor_ (or _reranker_) rescores the documents returned by the vector store against the question,
with a model that reads the question and each document together. It filters out documents below a minimum score
and keeps the best ones, so that the answering model receives fewer, more relevant documents.

> To access this page, you need the **_admin_** role
> (more details on roles in [security](../operate/security.md#roles)).

!!! warning
    In the current RAG chain (multi-query retriever, since release 26.3.3), the compressor configured here
    is not applied when answering: enabling or disabling it has no effect on the bot answers.
    It is used by the [retrieval diagnostic](vector-store-inspection.md#retrieval-diagnostic),
    which shows what it would change.

## Configuration

![Compressor settings](../img/gen-ai/gen-ai-settings-compressor.png "Compressor settings")

* **Compressor activation**: enables or disables the compressor for the bot.
* **Provider**: the only provider available in _Tock Studio_ is `BloomzRerank`, a reranking model exposed as an HTTP service:
    * **Endpoint**: base URL of the service. The orchestrator sends the question and the documents to its `/score` route.
    * **Label**: the label of the model output used as score (e.g. `entailment`).
    * **Minimum score**: documents scored below this value (between 0 and 1) are dropped.
    * **Max documents**: maximum number of documents kept after reranking.
    * **Pad with lower-scoring documents**: if fewer documents than _Max documents_ reach the minimum score,
      complete with the best remaining ones.

The Gen AI orchestrator also supports the [Bedrock Rerank API](https://docs.aws.amazon.com/bedrock/latest/userguide/rerank.html)
(provider `AwsBedrockRerank`, with the ARN of the rerank model in `model_arn`), usable through its
[API](orchestrator-api.md) only. It uses the AWS credentials described for [AWS Bedrock](providers/llm-embedding.md#aws-bedrock).

The compressor is fault-tolerant: if the reranking service fails or does not answer in time,
the original documents are used unchanged.

The settings can be exported (optionally with sensitive data, such as the endpoint) and imported,
to copy them between bots or environments, and deleted.

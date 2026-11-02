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

The compressor runs after retrieval, on the documents returned by the vector store, and before the answering LLM.
The [retrieval diagnostic](vector-store-inspection.md#retrieval-diagnostic) lets you tune its minimum score and
maximum number of documents against the actual reranker scores, before changing the bot configuration.

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

The Gen AI orchestrator also supports:

* **[Bedrock Rerank API](https://docs.aws.amazon.com/bedrock/latest/userguide/rerank.html)** (provider `AwsBedrockRerank`, with the ARN of the rerank model in `model_arn`), usable through its [API](orchestrator-api.md) only. It uses the AWS credentials described for [AWS Bedrock](providers/llm-embedding.md#aws-bedrock).

* **[vLLM Rerank API](https://github.com/vllm-project/vllm)** (provider `VllmRerank`): allows self-hosted reranking using any model supported by vLLM (e.g., `BAAI/bge-reranker-v2-m3`).
    * **Endpoint**: base URL of the vLLM service (e.g., `http://localhost:8000` or a URL behind a gateway).
    * **Model**: name of the reranking model served by vLLM.
    * **Minimum score**, **Max documents**, and **Pad with lower-scoring documents**: same as for `BloomzRerank`.
    * Note: the score scale depends on the model (e.g., sigmoid for BGE models, linear for others); adjust the minimum score threshold accordingly.

The compressor is fault-tolerant: if the reranking service fails or does not answer in time,
the original documents are used unchanged.

The settings can be exported (optionally with sensitive data, such as the endpoint) and imported,
to copy them between bots or environments, and deleted.

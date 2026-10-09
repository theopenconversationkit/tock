---
title: LLM and Embedding model providers
description: "The LLM and embedding model providers supported by Tock, and how to configure them."
---

# LLM and Embedding model providers

The Gen AI features of Tock rely on two kinds of models:

* an **LLM** (Large Language Model), which generates the answers ([RAG](../rag.md)), condenses the questions,
  generates [training sentences](../sentence-generation.md), or answers in the [playground](../playground.md),
* an **embedding model**, which turns texts into vectors to search the [vector store](vector-store.md).
  It must be the same model as the one used to index the documents.

They are configured in _Tock Studio_, in the Gen AI settings screens. Here are the supported providers:

| Provider             | `provider` value     | LLM | Embedding |
|----------------------|----------------------|:---:|:---------:|
| [OpenAI](https://platform.openai.com/docs/overview) (or any OpenAI-compatible API) | `OpenAI` | ✅ | ✅ |
| [Azure OpenAI](https://learn.microsoft.com/azure/ai-services/openai/) | `AzureOpenAIService` | ✅ | ✅ |
| [Ollama](https://ollama.com/)  | `Ollama`             | ✅  | ✅         |
| [AWS Bedrock](https://aws.amazon.com/bedrock/) | `AwsBedrock` | ✅ | ✅ |
| Bloomz (self-hosted embedding service) | `Bloomz` |     | ✅ (orchestrator only) |

## Common LLM settings

* `temperature`: the higher, the more creative the answers.
* `reasoning_effort` (optional): for reasoning models, the reasoning effort: `minimal`, `low`, `medium` or `high`.
  Leave it empty for models that do not support it.

## OpenAI

The `base_url` setting can target any OpenAI-compatible API (default: `https://api.openai.com/v1`).

LLM:

```json
{
  "provider": "OpenAI",
  "api_key": {
    "type": "Raw",
    "secret": "ab7-************-A1IV4B"
  },
  "model": "gpt-4o",
  "base_url": "https://api.openai.com/v1",
  "temperature": 0.7
}
```

Embedding:

```json
{
  "provider": "OpenAI",
  "api_key": {
    "type": "Raw",
    "secret": "ab7-************-A1IV4B"
  },
  "model": "text-embedding-3-small",
  "base_url": "https://api.openai.com/v1"
}
```

## Azure OpenAI

LLM (for embeddings, the same settings without `temperature`):

```json
{
  "provider": "AzureOpenAIService",
  "api_key": {
    "type": "Raw",
    "secret": "ab7-************-A1IV4B"
  },
  "api_base": "https://custom-api-name.openai.azure.com",
  "deployment_name": "custom-deployment-name",
  "model": "gpt-4o",
  "api_version": "2024-10-21",
  "temperature": 0.7
}
```

## Ollama

[Ollama](https://ollama.com/) runs open models locally or on your own servers. No API key is needed.

LLM (for embeddings, the same settings without `temperature`, with an embedding model):

```json
{
  "provider": "Ollama",
  "model": "llama3.1:8b",
  "base_url": "http://localhost:11434",
  "temperature": 0.7
}
```

## AWS Bedrock

[AWS Bedrock](https://aws.amazon.com/bedrock/) gives access to models hosted by AWS (Amazon Nova, Anthropic Claude,
Mistral, Titan embeddings...). No API key is stored: the orchestrator uses the AWS credentials of its environment,
and the region comes from the selected AWS profile. Set one of these environment variables on the orchestrator:

* `tock_gen_ai_orchestrator_aws_bedrock_credentials_profile_name`: the AWS profile to use,
* `tock_gen_ai_orchestrator_aws_bedrock_credentials_allow_default_profile=true`: uses the default AWS credential chain
  (IAM role, IRSA, environment variables). Without one of them, the calls fail.

The model access must be granted in the Bedrock console, for the account and the region.

LLM:

```json
{
  "provider": "AwsBedrock",
  "model": "amazon.nova-lite-v1:0",
  "temperature": 0.7,
  "guardrail_id": "arn:aws:bedrock:eu-west-3:123456789012:guardrail/my-guardrail",
  "guardrail_version": "1",
  "guardrail_trace": false
}
```

The `guardrail_*` settings (optional) apply a [Bedrock Guardrail](https://docs.aws.amazon.com/bedrock/latest/userguide/guardrails.html)
to the LLM calls: `guardrail_id` and `guardrail_version` must be set together, and `guardrail_trace` logs the
details of the guardrail interventions.

Embedding:

```json
{
  "provider": "AwsBedrock",
  "model": "amazon.titan-embed-text-v2:0"
}
```

See [RAG on AWS](../../getting-started/rag-aws.md) for a complete deployment with Bedrock and Amazon OpenSearch.

## Bloomz

Bloomz is an embedding model exposed by a self-hosted HTTP service. It is supported by the Gen AI orchestrator
(for instance with the [indexing tools](../indexing.md)), but cannot be selected in _Tock Studio_.

```json
{
  "provider": "Bloomz",
  "api_base": "http://bloomz-embedding:8080",
  "pooling": "mean"
}
```

`pooling` (optional) is the pooling method of the model: `mean` or `last`.

## API keys and secret managers

An API key (`api_key`) can be:

* a raw value: `{"type": "Raw", "secret": "..."}`,
* the name of a secret in [AWS Secrets Manager](https://aws.amazon.com/secrets-manager/): `{"type": "AwsSecretsManager", "secret_name": "..."}`,
* the name of a secret in [GCP Secret Manager](https://cloud.google.com/secret-manager): `{"type": "GcpSecretManager", "secret_name": "..."}`.

By default, the API keys typed in _Tock Studio_ are stored as raw values in the Tock database.
To store them in a secret manager instead, set the following environment variables on the _Tock Studio_ backend
(`tock/bot_admin`) and on the bot:

| Environment variable                  | Description                                          |
|---------------------------------------|------------------------------------------------------|
| `tock_gen_ai_secret_manager_provider` | `AWS_SECRETS_MANAGER` or `GCP_SECRET_MANAGER`        |
| `tock_gen_ai_secret_prefix`           | Prefix of the secret names (default: `LOCAL/TOCK`)   |

Secrets are then created as `<prefix>/<namespace>/<bot>/<feature>`, and Tock only stores their names.
The Gen AI orchestrator reads them from the same secret manager.

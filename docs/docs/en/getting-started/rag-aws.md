---
title: RAG on AWS
---

# Running RAG on AWS

This guide walks you through setting up a Tock bot's [RAG](../gen-ai/rag.md)
(Retrieval-Augmented Generation) feature entirely on AWS, from a fresh account to a working bot answering
questions from your own documents. It uses:

- **Amazon Bedrock** for both the LLM (answer generation) and the embedding model (document/question vectorization).
- **Amazon OpenSearch Service** (managed) as the vector store.
- The [`tock-docker`](https://github.com/theopenconversationkit/tock-docker) Docker images to run the platform.
- The `tock/llm-indexing-tools` Docker image to index your documents.

No other AWS service is required to follow this guide end to end.

## 1) Prerequisites

- An AWS account with permission to create IAM policies/roles, enable Bedrock model access, and create an
  OpenSearch domain.
- Docker and Docker Compose installed locally (or on the host where you run Tock).
- The [AWS CLI](https://aws.amazon.com/cli/), to create the AWS profile used by the orchestrator.

## 2) Enable Bedrock model access

Bedrock model access must be explicitly granted per AWS account and region before it can be invoked.

1. Open the [Bedrock console](https://console.aws.amazon.com/bedrock/) in the region you plan to use
   (for example `eu-west-3` or `ap-east-2`).
2. Go to **Model access** (left sidebar) and request access to the models you intend to use, for both
   **chat/text** and **embeddings**. For a good cost/quality trade-off to get started, we recommend:
    - LLM: `amazon.nova-lite-v1:0` (cheap, fast, good enough for most RAG scenarios) or
     `anthropic.claude-3-5-haiku-20241022-v1:0` for better answer quality.
    - Embedding: `amazon.titan-embed-text-v2:0`.
3. Wait for the access request to be approved (usually instantaneous for Amazon's own models).

!!! info
    Model IDs are region-specific: not every model is available in every region. Double-check availability for
    your chosen region in the Bedrock console before moving on.

## 3) Create an IAM identity for the orchestrator

The `GenAI Orchestrator` (the service that actually talks to Bedrock) authenticates using the
[default AWS credential chain](https://docs.aws.amazon.com/sdkref/latest/guide/standardized-credentials.html)
(shared credentials file, environment variables, EC2 instance profile, ECS task role, or EKS IRSA role). No access
key/secret is ever stored in Tock settings - only a profile name (or nothing, if you rely on the default chain).

Create an IAM policy granting invoke rights on the models you enabled above:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "BedrockInvoke",
      "Effect": "Allow",
      "Action": [
        "bedrock:InvokeModel",
        "bedrock:InvokeModelWithResponseStream"
      ],
      "Resource": [
        "arn:aws:bedrock:*::foundation-model/amazon.nova-lite-v1:0",
        "arn:aws:bedrock:*::foundation-model/amazon.titan-embed-text-v2:0"
      ]
    }
  ]
}
```

Attach this policy to whichever identity will run the orchestrator container:

- **Local Docker Compose/dev (this guide)**: create an IAM user and run `aws configure --profile bedrock-rag`,
  giving you a named profile in `~/.aws/credentials` that we'll mount into the container.
- **ECS/EKS/EC2 (recommended for production)**: attach the policy to the task role / IRSA service account /
  instance profile instead, and skip the profile name/credentials mount entirely - see the
  `..._allow_default_profile` setting below.

!!! note
    If you plan to use [Bedrock Guardrails](#8-optional-configure-guardrails), also add the guardrail's ARN to the
    `Resource` list above once you've created it.

## 4) Create an Amazon OpenSearch Service domain

1. Open the [OpenSearch Service console](https://console.aws.amazon.com/aos/home) and create a new domain.
2. Choose **Fine-grained access control** with an internal user database, and set a master username/password
   (Tock authenticates with plain HTTP basic auth, so this is the simplest option - no IAM SigV4 signing needed).
   > Amazon OpenSearch **Serverless** is not supported out of the box: it only accepts IAM SigV4-signed requests,
   > which Tock's OpenSearch integration does not implement. Use a regular (provisioned) OpenSearch domain.
3. Under **Network**, choose whatever fits your setup (VPC access is recommended for production; public access
   with an IP-restricted access policy works fine to follow this guide).
4. Once the domain is `Active`, note its **domain endpoint** (without the `https://` prefix), e.g.
   `search-my-domain-abc123xyz.eu-west-3.es.amazonaws.com`.

## 5) Run the Tock stack

Grab the RAG/OpenSearch Docker Compose stack from `tock-docker` as a starting point:

```bash
mkdir tock-aws-rag && cd tock-aws-rag
curl -o docker-compose.yml https://raw.githubusercontent.com/theopenconversationkit/tock-docker/master/docker-compose-rag-opensearch.yml
curl -o .env https://raw.githubusercontent.com/theopenconversationkit/tock-docker/master/.env
mkdir -p scripts && curl -o scripts/setup.sh https://raw.githubusercontent.com/theopenconversationkit/tock-docker/master/scripts/setup.sh
chmod +x scripts/setup.sh
```

This file ships with its own local, 2-node OpenSearch cluster (`opensearch-node1`/`opensearch-node2`/
`opensearch-dashboards`) for local testing. Since we're using a managed AWS OpenSearch domain instead:

1. Remove (or comment out) the `opensearch-node1`, `opensearch-node2`, and `opensearch-dashboards` services, and
   the `opensearch-data1`/`opensearch-data2` volumes at the bottom of the file.
2. Point the `gen_ai_orchestrator-server` service at your AWS domain instead of the local cluster, and mount your
   local AWS credentials/config so the container can authenticate to Bedrock:

```yaml
  gen_ai_orchestrator-server:
    image: "${PLATFORM}tock/gen-ai-orchestrator-server:${TAG}"
    ports:
      - "8000:8000"
    volumes:
      - ~/.aws:/root/.aws:ro
    environment:
      tock_gen_ai_orchestrator_application_environment: DEV
      tock_gen_ai_orchestrator_em_provider_timeout: 120
      tock_gen_ai_orchestrator_llm_provider_timeout: 120
      tock_gen_ai_orchestrator_llm_provider_max_retries: 0
      tock_gen_ai_orchestrator_vector_store_provider: OpenSearch
      tock_gen_ai_orchestrator_vector_store_host: search-my-domain-abc123xyz.eu-west-3.es.amazonaws.com
      tock_gen_ai_orchestrator_vector_store_port: 443
      tock_gen_ai_orchestrator_vector_store_user: admin
      tock_gen_ai_orchestrator_vector_store_pwd: <your master password>
      tock_gen_ai_orchestrator_vector_store_timeout: 5
      tock_gen_ai_orchestrator_vector_store_test_query: virement bancaire
      tock_gen_ai_orchestrator_aws_bedrock_credentials_profile_name: bedrock-rag
```

!!! note
    Running on ECS/EKS/EC2 instead of Docker Compose? Drop the `volumes` mount and
    `..._credentials_profile_name` variable, and set
    `tock_gen_ai_orchestrator_aws_bedrock_credentials_allow_default_profile: true` instead, so the SDK picks up the
    task/instance role automatically.

Then launch the stack:

```bash
docker compose up
```

Once everything is up, _Tock Studio_ is reachable at [http://localhost](http://localhost)
(default login `admin@app.com` / `password`), and the orchestrator itself listens on `http://localhost:8000`.

## 6) Index your documents

Documents are chunked, embedded with Bedrock and stored in OpenSearch by the [indexing tool](../gen-ai/indexing.md),
available as the `tock/llm-indexing-tools` Docker image.

### 6.1) Prepare the CSV file

The indexing tool reads a pipe-delimited (`|`) CSV file with three columns: `title`, `source` (typically the URL
of the document) and `text`. See [Document indexing](../gen-ai/indexing.md#input-documents) for the format, and the
[RAG tutorial](rag-tutorial.md#convert-the-documentation-into-a-csv-file) for an example of conversion from Markdown files.

Put the file in an `ingestion` folder, for instance `ingestion/data.csv`.

### 6.2) Write the indexing configuration

Create `ingestion/config.json`:

```json
{
  "bot": {
    "namespace": "app",
    "bot_id": "new_assistant",
    "file_location": "/ingestion"
  },
  "em_setting": {
    "provider": "AwsBedrock",
    "model": "amazon.titan-embed-text-v2:0"
  },
  "vector_store_setting": {
    "provider": "OpenSearch",
    "host": "search-my-domain-abc123xyz.eu-west-3.es.amazonaws.com",
    "port": 443,
    "username": "admin",
    "password": {
      "type": "Raw",
      "secret": "<your master password>"
    }
  },
  "data_csv_file": "data.csv",
  "document_index_name": null,
  "chunk_size": 1000,
  "embedding_bulk_size": 20,
  "ignore_source": false,
  "append_doc_title_and_chunk": true
}
```

* `bot`: the namespace and the name of the application, as created in _Tock Studio_
  (`app` and `new_assistant` with the wizard of the Docker platform, see [step 7](#7-configure-the-bot-in-tock-studio)).
* `document_index_name`: leave it `null`, so that the index is named after the namespace, the bot and the indexing
  session, as _Tock Studio_ expects.

The other options are described in the [RAG tutorial](rag-tutorial.md#index-the-documentation) and the
[README](https://github.com/theopenconversationkit/tock/blob/master/gen-ai/orchestrator-server/src/main/python/tock-llm-indexing-tools/README.md)
of the indexing tool.

### 6.3) Run the indexing

The indexing tool calls Bedrock with the same code as the orchestrator: mount your AWS configuration and give it
the AWS profile to use.

```bash
docker run --rm \
  -v "$PWD/ingestion:/ingestion" \
  -v ~/.aws:/root/.aws:ro \
  -e tock_gen_ai_orchestrator_aws_bedrock_credentials_profile_name=bedrock-rag \
  tock/llm-indexing-tools:{{ tock_version }} \
  python tock-llm-indexing-tools/scripts/indexing/vectorisation/run_vectorisation.py \
  --json-config-file=/ingestion/config.json -v
```

At the end, the tool displays the **index name** (`ns_app_bot_new_assistant_session_<uuid>`) and the
**index session ID**. Write down the index session ID: you need it in the next step.

## 7) Configure the bot in Tock Studio

When you first log in, the wizard creates an application named `new_assistant` in the `app` namespace
(see [Create the application](rag-tutorial.md#create-the-application)).

Then go to _Gen AI_ > _Rag settings_ (you need the **admin** role):

* **Question condensing** and **Question answering**, _Configuration_:
    * Provider: _AWS Bedrock_
    * Model id: `amazon.nova-lite-v1:0`
    * Temperature: `0.7`
* **Embedding**, _Configuration_:
    * Provider: _AWS Bedrock_
    * Model id: `amazon.titan-embed-text-v2:0`, the model used for the indexing
* **Indexing session**: the **index session ID** displayed by the indexing tool
* Enable **Rag activated**, then _Save_

You can adapt the answering prompt (see the [RAG prompt guide](../gen-ai/rag-prompt.md)).

!!! warning
    The embedding model here **must** match the one used to index your documents - mixing embedding models between
    indexing and querying silently degrades (or breaks) retrieval quality, since vectors won't be comparable.

> There is no need to fill in _Gen AI_ > _Vector DB settings_: the orchestrator already uses your OpenSearch domain,
> set by environment variables in [step 5](#5-run-the-tock-stack). This screen only overrides that connection for a
> given bot.

## 8) Optional: configure guardrails

AWS Bedrock LLM settings support inline [Bedrock Guardrails](https://docs.aws.amazon.com/bedrock/latest/userguide/guardrails.html),
applied directly on every LLM call with no extra network round trip. Once you've created a guardrail in the
Bedrock console, fill in the **Guardrail ID** (identifier or ARN) and the **Guardrail Version** (e.g. `DRAFT` or `1`)
of the _Question answering_ configuration in _Rag settings_. **Enable Guardrail Trace** logs the details of the
guardrail interventions.

Don't forget to add the guardrail's ARN to the IAM policy from step 3, or Bedrock will reject the call with an
access-denied error even though the model invocation itself would otherwise succeed.

## 9) Test it

In _Tock Studio_, go to _Test_ > _Test_ and send a question you know is covered by your indexed documents. You
should get an answer generated from the retrieved document chunks. If not, see the troubleshooting table below.

## Troubleshooting

| Symptom                                                                 | Likely cause                                                                                        | Fix                                                                                                          |
|-------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| `AccessDeniedException` mentioning `bedrock:InvokeModel`                | Model access not granted, or IAM policy doesn't cover the model/region                              | Re-check step 2 (model access) and step 3 (IAM policy `Resource` ARNs, including region)                     |
| `MissingCredentialsProfileName` error from the orchestrator             | Neither a profile name nor the default-profile fallback is configured                               | Set `tock_gen_ai_orchestrator_aws_bedrock_credentials_profile_name`, or set `..._allow_default_profile=true` |
| No documents retrieved / bot always falls back to the "no answer" story | Wrong indexing session ID, or embedding model mismatch between ingestion and RAG settings           | Double-check the session ID from step 6.3, and that both use the same embedding model                        |
| Connection refused / timeout to OpenSearch                              | Domain network access policy or security group doesn't allow the orchestrator container's egress IP | Adjust the OpenSearch domain's access policy/VPC security group                                              |
| `401 Unauthorized` from OpenSearch                                      | Wrong master username/password, or fine-grained access control not enabled                          | Re-check step 4 and the credentials used in both the indexing configuration and the orchestrator                |

## Reference

- [RAG feature overview](../gen-ai/rag.md)
- [Vector store providers](../gen-ai/providers/vector-store.md)
- [LLM/embedding providers](../gen-ai/providers/llm-embedding.md)
- [`tock-docker`](https://github.com/theopenconversationkit/tock-docker) - Docker images and Compose stacks
- [`tock-llm-indexing-tools` README](https://github.com/theopenconversationkit/tock/blob/master/gen-ai/orchestrator-server/src/main/python/tock-llm-indexing-tools/README.md)

---
title: Vector DB settings
---

# The _Vector DB settings_ menu

## Configuration

The _Gen AI_ > _Vector DB settings_ menu configures the vector database the RAG of the bot is connected to.

In AI, vector databases store data as vectors, which makes operations such as semantic similarity or classification easy.
They are used by machine learning models to process and analyze texts, images or other kinds of complex data.

> To access this page, you need the **_admin_** role.
> <br />(more details on roles in [security](../operate/security.md#roles)).

A configuration screen lets Tock connect to a vector database:

![Vector Store](../img/gen-ai/gen-ai-settings-vector-store.png "Vector database configuration screen")

## Usage

- Here is the [list of vector database providers](providers/vector-store.md) supported by Tock.
- Please refer to the documentation of each tool to learn how to use it.
- If no configuration is provided in _Tock Studio_, the default configuration, set with environment variables, is used.

In _Bot Admin_, `tock_gen_ai_orchestrator_vector_store_provider` sets the type of the default vector database
(`PGVector` by default). It lets Tock build the right search parameters for this database when calling the RAG.

In the Gen AI orchestrator:

| Variable | Default | Description |
|----------|---------|-------------|
| `tock_gen_ai_orchestrator_vector_store_provider` | `OpenSearch` | `OpenSearch` or `PGVector` |
| `tock_gen_ai_orchestrator_vector_store_host` | `localhost` | Host of the vector database |
| `tock_gen_ai_orchestrator_vector_store_port` | `9200` | Port (e.g. `5432` for PGVector) |
| `tock_gen_ai_orchestrator_vector_store_user` | `admin` | User |
| `tock_gen_ai_orchestrator_vector_store_pwd` | `admin` | Password |
| `tock_gen_ai_orchestrator_vector_store_database` | | Database name (PGVector only) |
| `tock_gen_ai_orchestrator_vector_store_secret_manager_provider` | | `AWS_SECRETS_MANAGER` or `GCP_SECRET_MANAGER`, to read the credentials from a secret manager |
| `tock_gen_ai_orchestrator_vector_store_credentials_secret_name` | | Name of the secret holding the credentials |
| `tock_gen_ai_orchestrator_vector_store_timeout` | `4` | Request timeout, in seconds |

> The default provider is not the same in _Bot Admin_ (`PGVector`) and in the orchestrator (`OpenSearch`):
> set the variable to the same value on both sides.

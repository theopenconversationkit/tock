---
title: GCP - Overall secret management design
---

# GCP - Overall secret management design #1696

- Proposal PR: [https://github.com/theopenconversationkit/tock/pull/1697](https://github.com/theopenconversationkit/tock/pull/1697)
- GitHub Issue for this feature: https://github.com/theopenconversationkit/tock/issues/1696


API version used : v1 of Google Secret Manager, reference version here.

## Introduction

We have already an integration of AWS Secret manager essentially used for :
* **Generative AI services** dynamic secrets management, secrets vault are created dynamically for generative AI services (LLM, Embedding, Observability, VectorDB) 
* **MongoDB credentials** : Add an implementation of `ai.tock.shared.security.mongo.MongoCredentialsProvider` ([MongoCredentialsProvider](https://github.com/theopenconversationkit/tock/blob/tock-24.3.4/shared/src/main/kotlin/security/mongo/MongoCredentialsProvider.kt#L24)) for GCP and open source the internal one we have for AWS at Arkea. Currently, we only have the connexion string (thought environment variables), but credentials can be provided using ([MongoCredentialsProvider](https://github.com/theopenconversationkit/tock/blob/tock-24.3.4/shared/src/main/kotlin/security/mongo/MongoCredentialsProvider.kt#L24)) (see [MongoClient creation here](https://github.com/theopenconversationkit/tock/blob/tock-24.3.4/shared/src/main/kotlin/Mongos.kt#L178), [asyncMongoClient here](https://github.com/theopenconversationkit/tock/blob/tock-24.3.4/shared/src/main/kotlin/Mongos.kt#L197))
* **iAdvize Connector GraphQL credentials** : currently we have an AWS Secret Manager implementation of [`ai.tock.shared.security.credentials.CredentialsProvider`](https://github.com/theopenconversationkit/tock/blob/tock-24.3.4/shared/src/main/kotlin/security/credentials/CredentialsProvider.kt#L19) this will need to be implemented also for GCP Secret Manager. 

Each of these 3 different secret usages can use different providers independently, for instance you could provide your mongodb credential using environment variables and handle the generate ai related secrets using GCP Secret Manager.

In this design we will as much as possible **refer to GCP Secrets using their secret name et not that full resource name** that include the project_id (using `SECRET-NAME` instead of `projects/my-gcp-project/secrets/SECRET-NAME`), we want to do this so that we can move resources from a project to another without having to do any kind of database data migration (changing project-number in some stored configuration will be difficult). Nevertheless, applications needs to be aware of the GCP project number where the secrets are located, they will all rely on `tock_gcp_project_id` environment variable to do so (see last section about environment variables for more details).

## Architecture design

This is the overall architecture of the components interacting with secrets.

[![Architecture générale composants impactés et flux](../../../img/feat-design-1696-architecture_gcp_secrets.excalidraw.png)](../../../img/feat-design-1696-architecture_gcp_secrets.excalidraw.png){:target="_blank"}

*File editable using [Excalidraw](https://excalidraw.com/) simply import the PNG, it contains scene data.*

## Gen AI secret naming patterns

GCP Secret naming convention :
> **SECRET_STORAGE_PREFIX**-TOCK-**normalized(NAMESPACE)**-**normalized(BOTID)**-GenAI-**FUNCTION**

GCP, Ressource ID pattern :
> projects/**PROJECT_ID**/secrets/**SECRET_STORAGE_PREFIX**-TOCK-**normalized(NAMESPACE)**-**normalized(BOTID)**-GenAI-**FUNCTION**

### Environment isolation
<a id="env_isolation"></a>

You should isolate your critical environment in a separate GCP project (and kube cluster). Nevertheless, sometime you may have only 1 GCP project for non production, if you have multiple non-production TOCK environment that needs to live under the same GCP project (for instance feature based environment for testing purposes) you will need to prefix your GCP secrets names.

The `SECRET_STORAGE_PREFIX` part of the naming is here for that, we recommend using those naming, but you are free to configuration different prefix using **tock_gen_ai_secret_prefix** environment variable (the value of this environment variable should respect GCP constraints or be normalizedfollowing the same rules as namespace normalization ?).

| `SECRET_STORAGE_PREFIX` value      | Description                                                                                                                                                                                                                    |
|------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `PROD`                             | Production environment, might not be necessary if you are under a dedicated GCP project.                                                                                                                                       |
| `DEV`                              | Non production environment used to test a SNAPSHOT release containing multiple features or evolutions                                                                                                                          |
| `LOCAL`                            | A local environment used for dev                                                                                                                                                                                               |
| `FEAT-normalized(ISSUE_REFERENCE)` | Feature based environment usually a TOCK branch, associated with a github issue, we can simply use the issue reference (number). <br><br> *For normalized function see the description bellow used for normalized(NAMESPACE).* |

### Normalization method `normalized(NAMESPACE)`

According to [Google's Secret Manager documentation](https://cloud.google.com/secret-manager/docs/reference/rest/v1/projects.secrets/create#query-parameters) :

> A secret ID is a string with a maximum length of 255 characters and can contain uppercase and lowercase letters, numerals, and the hyphen (-) and underscore (_) characters.

Normalization rules in order :
  * Replace spaces with `-`
  * Filter characters, keep only : letters, numerals, and the hyphen (-) and underscore (_)
  * trim / cut at 255 characters

### Secret `FUNCTION` part

| Description                                                           | AWS Secret name                      | GCP Secret name                      |
|-----------------------------------------------------------------------|--------------------------------------|--------------------------------------|
| Observability tool secret. <br><br> *Eg. Langfuse secret key.*        | `OBSERVABILITY/observabilitySetting` | `OBSERVABILITY-observabilitySetting` |
| RAG Embedding APIs secret. <br><br> *Eg. OpenAI Ada secret key.*      | `RAG/embeddingQuestion`              | `RAG-embeddingQuestion`              |
| RAG LLM Answer generation APIs secret <br><br> *Eg. Open AI LLM key.* | `RAG/questionAnswering`              | `RAG-questionAnswering`              |
| Sentence Generation LLM API secret <br><br> *Eg. Open AI LLM key.*    | `COMPLETION/sentenceGeneration`      | `COMPLETION-sentenceGeneration`      |

Exemple for bot named `my_bot` with namespace `my_namespace` under the GCP with number `478160847739` :
 * Rag Setting LLM secret key will be stored on GCP under the following :
    * Secret Name : PROD-TOCK-**my_namespace**-**my_bot**-GenAI-RAG-questionAnswering
    * Secret ressource (full) name : projects/**my-gcp-project-123**/secrets/PROD-TOCK-**my_namespace**-**my_bot**-GenAI-RAG-questionAnswering
 * Generate sentence LLM secret key will be stored on GCP under the following :
    * Secret Name : PROD-TOCK-**my_namespace**-**my_bot**-GenAI-COMPLETION-sentenceGeneration
    * Secret ressource (full) name : projects/**my-gcp-project-123**/secrets/PROD-TOCK-**my_namespace**-**my_bot**-GenAI-COMPLETION-sentenceGeneration


Reference about secret resource (full) name pattern `projects/project_id/secrets/secret-id`, documented here : [IAM Documentation ressource name format](https://cloud.google.com/iam/docs/conditions-resource-attributes#:~:text=Secret%20Manager%20secrets-,projects/project%2Dnumber/secrets/secret%2Did,-Secret%20Manager%20secret).


## Access management - IAM roles and permissions

### For Gen AI Secrets

*It would be greate to use [IAM conditions](https://cloud.google.com/iam/docs/conditions-overview) to limit secret access only to secrets respecting the Gen AI secret naming pattern.*

Role `tock.gen-ai-secrets.sharer` (reader & writer) :
* secretmanager.secrets.create
* secretmanager.secrets.get
* secretmanager.versions.add
* secretmanager.versions.access
* secretmanager.versions.get
* secretmanager.secrets.update
* secretmanager.secrets.delete

This role `tock.gen-ai-secrets.sharer`, should be applied to the following stack components :
* Bot-Admin / Studio

Role `tock.gen-ai-secrets.shareholder` (reader) :
* secretmanager.versions.access
* secretmanager.versions.get

This role `tock.gen-ai-secrets.shareholder`, should be applied to the following stack components :
* Gen AI Orchestrator

Feature env cleaner role `feature-env-cleaner` :
* secretmanager.*

*This role can be used by CI / CD when removing a feature env if you have feature based environment. Should not be created in production dedicated GCP projects.*

### For Tock MongoDB Secrets

Role `tock.mongodb-secret.shareholder` (reader) :
* secretmanager.versions.access
* secretmanager.versions.get

This role `tock.mongodb-secret.shareholder`, should be applied to the following stack components :
* Bot-Admin / studio
* Bot API
* NLP API

*This secret isn't set though TOCK, should be set using the GCP console or infrastructure scripting.*

### For iAdvize credentials secrets

Role `tock.iadvize-secret.shareholder` (reader) :
* secretmanager.versions.access
* secretmanager.versions.get

This role `tock.iadvize-secret.shareholder`, should be applied to the following stack components :
* Bot API

*This secret isn't set though TOCK, should be set using the GCP console or infrastructure scripting.*

## Secret formats

### Gen AI Orchestrator secret payload format

Secret payload data for all generative AI related secret have the following base format :

```json
{
    "secret":"xxxxxxxxxxxxxxxxxxxxxxxx"
}
```

### MongoDB Secret format

The mongo database secret format is the following :
```json
{
  "password": "********",
  "username": "myUser"
}
```

### iAdvize Secret payload format

The iAdvize secret payload data is the following one :
```json
{
  "password":"****************",
  "username":"myiAdvizeTechnicalMail@exemple.com"
}
```

This is the iAdvize technical account you use for GraphQL calls.

## Generative AI - database storage format

Here is an example of how it should be stored in the database :
```json
{
    "_id": "665ef9bdfdc4c53ead71d0e9",
    "namespace": "01",
    "botId": "cmb",
    "enabled": true,
    "nbSentences": 12,
    "llmSetting": {
        "apiKey": {
            "secretName": "PROD-TOCK-my_namespace-my_bot-GenAI-RAG-questionAnswering",
            "type": "GcpSecretManager"
        },
        "temperature": "0.8",
        "prompt": "Given the base sentences provided below, generate a list of {{nb_sentences}} unique sentences that convey the same meaning but vary in their presentation. \n\nThe variations should reflect a diverse range of alterations, including but not limited to:\n{% if options.spelling_mistakes %}\n- Spelling Mistakes: Introduce common and uncommon spelling errors that do not hinder the overall comprehension of the sentence.\n{% endif %}\n{% if options.sms_language %}\n- Incorporation of Non-Standard Language Features: Where appropriate, use features like onomatopoeia, mimetic words, or linguistic innovations unique to digital communication.\n{% endif %}\n{% if options.abbreviated_language %}\n- Abbreviations and DM (Direct Message) Language: Transform parts of the sentence using popular text messaging abbreviations, internet slang, and shorthand commonly found in online and informal communication.\n{% endif %}\n- Answer as a numbered list.\n- Answer in {{locale}}.\n\nBase Sentences (remember: you must answer as a numbered list):\n{% for sentence in sentences %}\n- {{ sentence }}\n{% endfor %}\n",
        "apiBase": "https://xxxxxx",
        "deploymentName": "xxxxxxx",
        "apiVersion": "2024-03-01-preview",
        "provider": "AzureOpenAIService"
    }
}
```

## Gen AI Orchestrator secret format in API calls

POST /llm-providers/OpenAI/setting/status
```json
{
  "setting": {
    "provider": "OpenAI",
    "api_key": {
      "type": "GcpSecretManager",
      "secretName": "PROD-TOCK-my_namespace-my_bot-GenAI-RAG-questionAnswering"
    },
    "temperature": 1.2,
    "prompt": "How to learn to ride a bike without wheels!",
    "model": "gpt-3.5-turbo"
  },
  "observability_setting": {}
}
```


## List of providers of secret managers :
<a id="secret_manager_providers"></a>

* `AwsSecretsManager`: The AWS Secrets Manager.
* `GcpSecretManager`: The GCP Secret Manager.
* `Env`: Store the secret directly in an environment variable.

## Environment variable settings for secrets

This design introduce the new provider type: `GcpSecretManager`.

| Components                                              | Environment variable name                       | Default      | Allowed values                                            | Description                                                                                                                                                                                                                                                                                                                              |
|---------------------------------------------------------|-------------------------------------------------|--------------|-----------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `Bot Admin`,<br/> `Bot Api`,<br/> `NLP Api`             | `tock_database_mongodb_secret_manager_provider` | `null`       | `Env`,<br/> `AwsSecretsManager`,<br/> `GcpSecretManager`  | The provider of the secret manager used to retrieve credentials for database access (mongodb). <br />See [Secret Manager providers](#secret_manager_providers)                                                                                                                                                                           |
| `Bot Admin`,<br/> `Bot Api`,<br/> `NLP Api`             | `tock_database_mongodb_credentials_secret_name` | `null`       | Any string                                                | The secret name storing database credentials (Only if credentials are not passed in the MongoBD connection string URI).                                                                                                                                                                                                                  |
| `Bot Admin`                                             | `tock_gen_ai_secret_manager_provider`           | `null`       | `Env`,<br/> `AwsSecretsManager`,<br/> `GcpSecretManager`  | The provider of the secret manager used to store and retrieve the Gen AI Api Keys. <br />See [Secret Manager providers](#secret_manager_providers).<br/>Default (`null`): The secret will be stored directly in the database in text format, so it can only be used for local development purposes, which is obviously not a sure thing. |
| `Bot Admin`                                             | `tock_gen_ai_secret_prefix`                     | `LOCAL/TOCK` | Any string                                                | [See this section](#env_isolation)                                                                                                                                                                                                                                                                                                       |
| `Bot Api`                                               | `tock_iadvize_secret_manager_provider`          | `null`       | `Env`,<br/> `AwsSecretsManager`,<br/> `GcpSecretManager`  | (When using iAdvize Connector) The provider of the secret manager used to retrieve credentials for iAdvize (GraphQL). <br />See [Secret Manager providers](#secret_manager_providers)                                                                                                                                                    |
| `Bot Api`                                               | `tock_iadvize_credentials_secret_name`          | `null`       | Any string                                                | (When using iAdvize Connector) The secret name storing iAdvize credentials.                                                                                                                                                                                                                                                              |
| `Bot Admin`,<br/> `Bot Api`,<br/> `Gen AI Orchestrator` | `tock_gcp_project_id`                           | `null`       | Any string                                                | The GCP project ID where secrets are stored and retrieved.                                                                                                                                                                                                                                                                               |
| `Bot Admin`                                             | `tock_gcp_region`                               | `null`       | Any string                                                | The GCP project Region where secrets are stored.                                                                                                                                                                                                                                                                                         |


## Technical change that should be made

### List of existing environment variables that were renamed, may introduce breaking change if used

* `tock_database_credentials_provider` replaced by `tock_database_mongodb_secret_manager_provider`
* `tock_mongodb_credentials_secret_name` replaced by `tock_database_mongodb_credentials_secret_name`
* `tock_iadvize_credentials_provider` replaced by `tock_iadvize_secret_manager_provider`
* `aws_iadvize_credentials_secret_id` replaced by `tock_iadvize_credentials_secret_name`
* `tock_gen_ai_orchestrator_secret_storage_type` replaced by `tock_gen_ai_secret_manager_provider`
* `tock_gen_ai_orchestrator_secret_storage_prefix_name` replaced by `tock_gen_ai_secret_prefix`

### Other changes
* Introduction of a new util (`tock-utils`) module named `tock-gcp-tools`, this module will implement the same logic as the `tock-aws-tools`
* Introduce GCP secret in the Gen Ai Orchestrator. OpenAPI contract not defined in this design currently.
* Secret name generation (normalization) should not be done at the DTO mapper level as it currently assume that we only use AWS Secret Manager :
  * [BotRAGConfigurationDTO.kt#L60](https://github.com/theopenconversationkit/tock/blob/master/bot/admin/server/src/main/kotlin/model/BotRAGConfigurationDTO.kt#L60), [BotRAGConfigurationDTO.kt#L66](https://github.com/theopenconversationkit/tock/blob/master/bot/admin/server/src/main/kotlin/model/BotRAGConfigurationDTO.kt#L66)
  * BotObservabilityConfigurationDTO
  * BotSentenceGenerationDTO
* Default value of `tock_gen_ai_secret_prefix` currently `/dev` has been changed to `LOCAL/TOCK`.

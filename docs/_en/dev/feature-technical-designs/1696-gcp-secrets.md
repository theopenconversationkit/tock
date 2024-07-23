---
title: GCP - Overall secret management design
---

# GCP - Overall secret management design

- Proposal PR: https://github.com/theopenconversationkit/tock/pull/1697
- Github Issue for this feature: https://github.com/theopenconversationkit/tock/issues/1696


API version used : v1 of Google Secret Manager, reference version here.

## Architecture design

This is the overall architecture of the components interacting with secrets.

[![Architecture générale composants impactés et flux](../../../img/feat-design-1696-architecture_gcp_secrets.excalidraw.png)](../../../img/feat-design-1696-architecture_gcp_secrets.excalidraw.png){:target="_blank"}

*File editable using [Excalidraw](https://excalidraw.com/) simply import the PNG, it contains scene data.*

## Gen AI secret naming patterns

GCP Secret naming convention :
> **SECRET_STORAGE_PREFIX**-TOCK-**normalized(NAMESPACE)**-**normalized(BOTID)**-GenAI-**FUNCTION**

GCP, Ressource ID pattern :
> projects/**PROJECT_ID**/secrets/**SECRET_STORAGE_PREFIX**-TOCK-**normalized(NAMESPACE)**-**normalized(BOTID)**-GenAI-**FUNCTION**


### Environment isolation `SECRET_STORAGE_PREFIX` and feature based environements

You should isolate your critical environment in a separate GCP project (and kube cluster). Nevertheless sometime you may have only 1 GCP project for non production, if you have multiple non-production TOCK environment that needs to live under the same GCP project (for instance feature based environment for testing purposes) you will need to prefix your GCP secrets names.

The `SECRET_STORAGE_PREFIX` part of the naming is here for that, we recommand using those naming, but your are free to configuration different prefix using **tock_gen_ai_orchestrator_secret_storage_prefix_name** environment variable (the value of this environment variable should respect GCP constraints or be normalizedfollowing the same rules as namespace normalization ?).

| `SECRET_STORAGE_PREFIX` value | Description |
| --- | --- |
| `PROD` | Production environement, might not be necessary if you are under a dedicated GCP project. |
| `DEV` | Non production environmment used to test a SNAPSHOT release containing multiple features or evolutions |
|  `FEAT-normalized(ISSUE_REFERENCE)` | Feature based environement usually a TOCK branch, associated with a github issue, we can simply use the issue reference (number). <br><br> *For normalized function see the description bellow used for normalized(NAMESPACE).* |

### Normalization method `normalized(NAMESPACE)`

According to [Google's Secret Manager documentation](https://cloud.google.com/secret-manager/docs/reference/rest/v1/projects.secrets/create#query-parameters) :

> A secret ID is a string with a maximum length of 255 characters and can contain uppercase and lowercase letters, numerals, and the hyphen (-) and underscore (_) characters.

Normalization rules in order :
  * Replace spaces with `-`
  * Filter characters, keep only : letters, numerals, and the hyphen (-) and underscore (_)
  * trim / cut at 255 characters

### Secret `FUNCTION` part

| Description | AWS Secret name | GCP Secret name |
| --- | --- | --- |
| Observability tool secret. <br><br> *Eg. Langfuse secret key.* | `OBSERVABILITY/observabilitySetting` | `OBSERVABILITY-observabilitySetting` |
| RAG Embedding APIs secret. <br><br> *Eg. OpenAI Ada secret key.*  | `RAG/embeddingQuestion` | `RAG-embeddingQuestion` |
| RAG LLM Answer generation APIs secret <br><br> *Eg. Open AI LLM key.*  | `RAG/questionAnswering` | `RAG-questionAnswering` |
| Sentence Generation LLM API secret <br><br> *Eg. Open AI LLM key.*| `COMPLETION/sentenceGeneration` | `COMPLETION-sentenceGeneration` |

Exemple for bot named `my_bot` with namespace `my_namespace` under the GCP with number `478160847739` :
 * Rag Setting LLM secret key will be stored on GCP under the following :
    * Secret Name : PROD-TOCK-**my_namespace**-**my_bot**-GenAI-RAG-questionAnswering
    * Secret ressource ID : projects/**478160847739**/secrets/PROD-TOCK-**my_namespace**-**my_bot**-GenAI-RAG-questionAnswering
 * Generate sentence LLM secret key will be stored on GCP under the following :
    * Secret Name : PROD-TOCK-**my_namespace**-**my_bot**-GenAI-RAG-questionAnswering
    * Secret ressource ID : projects/**478160847739**/secrets/PROD-TOCK-**my_namespace**-**my_bot**-GenAI-COMPLETION-sentenceGeneration


Reference about secret ressource ID pattern `projects/project-number/secrets/secret-id` (carrefull *project-number* is the *project-id*), documented here : [IAM Documentation ressource name format](https://cloud.google.com/iam/docs/conditions-resource-attributes#:~:text=Secret%20Manager%20secrets-,projects/project%2Dnumber/secrets/secret%2Did,-Secret%20Manager%20secret).


## Access management - IAM roles and permissions

Bot Admin / Studio, role `bot-admin` :
* secretmanager.secrets.create
* secretmanager.versions.add
* secretmanager.versions.access
* secretmanager.versions.get
* secretmanager.secrets.update
* secretmanager.secrets.delete

Orchestrator role `gen-ai-orchestrator` :
* secretmanager.versions.access
* secretmanager.versions.get

Feature env cleaner role `feature-env-cleaner` :
* secretmanager.*

*This role can be used by CI / CD when removing a feature env if you have feature based environment. Should not be created in production dedicated GCP projects.*


## Gen AI Orchestrator secret payload format

Secret payload data have the following base format :

```json
{
    "secret":"xxxxxxxxxxxxxxxxxxxxxxxx"
}
```

## Database storage format

Here is a exemple of how it should be stored in the database :
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


## Bot Admin - Environnement variable settings

This design introduce the new provider type `GcpSecretManager`.

|Environment variable name | Default | Allowed values | Description |
|--- |--- |--- |--- |
| `tock_gen_ai_orchestrator_secret_storage_type`| `Raw` | `Raw`, `AwsSecretsManager`, `GcpSecretManager` | Type of credential provider used to store secrets : <br> <ul> <li>Raw: Store secret directly into tock's mongodb in raw. Use it only for local dev purposed it's clearly unsafe.</li><li>AwsSecretsManager: rely on AWS Secret Manager.</li><li>GcpSecretManager: rely on GCP Secret Manager.</li></ul> |
| `tock_gen_ai_orchestrator_secret_storage_prefix_name`| `DEV` | any string ? | See section "Environment isolation `SECRET_STORAGE_PREFIX` and feature based environements" of this document.  <br><br> ⚠️ Current default value need to be changed. |


## Technical change that should be made

### Breaking changes

* Default value of `tock_gen_ai_orchestrator_secret_storage_prefix_name` currently `/dev` shouldn't use slashes as it's not allow according to GCP Secret Names constraints. It will be changed to `dev` but it might break all running project that doesn't defined it.


### Other changes
* Introduction of a new util (`tock-utils`) module named `tock-gcp-tools`, this module will implement the same logic as the `tock-aws-tools`
* Introduce GCP secret in the Gen Ai Orchestrator. OpenAPI contrat not defined in this design currently.
* Secret name generation (normalization) should not be done at the DTO mapper level as it currently assume that we only use AWS Secret Manager :
  *  [BotRAGConfigurationDTO.kt#L60](https://github.com/theopenconversationkit/tock/blob/master/bot/admin/server/src/main/kotlin/model/BotRAGConfigurationDTO.kt#L60), [BotRAGConfigurationDTO.kt#L66](https://github.com/theopenconversationkit/tock/blob/master/bot/admin/server/src/main/kotlin/model/BotRAGConfigurationDTO.kt#L66)
  * BotObservabilityConfigurationDTO
  * BotSentenceGenerationDTO
  * .... vector DB settings ...

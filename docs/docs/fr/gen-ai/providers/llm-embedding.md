---
title: Fournisseurs de LLM et d'embeddings
---

# Fournisseurs de LLM et d'embeddings

Les fonctionnalités Gen AI de Tock s'appuient sur deux types de modèles :

* un **LLM** (grand modèle de langage), qui génère les réponses ([RAG](../rag.md)), condense les questions,
  génère des [phrases d'entraînement](../sentence-generation.md) ou répond dans le [playground](../playground.md),
* un **modèle d'embeddings**, qui transforme les textes en vecteurs pour chercher dans la [base vectorielle](vector-store.md).
  Il doit être le même que celui utilisé pour indexer les documents.

Ils se configurent dans _Tock Studio_, dans les écrans de réglages Gen AI. Voici les fournisseurs pris en charge :

| Fournisseur          | Valeur de `provider` | LLM | Embeddings |
|----------------------|----------------------|:---:|:----------:|
| [OpenAI](https://platform.openai.com/docs/overview) (ou toute API compatible OpenAI) | `OpenAI` | ✅ | ✅ |
| [Azure OpenAI](https://learn.microsoft.com/azure/ai-services/openai/) | `AzureOpenAIService` | ✅ | ✅ |
| [Ollama](https://ollama.com/)  | `Ollama`             | ✅  | ✅          |
| [AWS Bedrock](https://aws.amazon.com/bedrock/) | `AwsBedrock` | ✅ | ✅ |
| Bloomz (service d'embeddings auto-hébergé) | `Bloomz` |     | ✅ (orchestrateur uniquement) |

## Réglages communs des LLM

* `temperature` : plus elle est élevée, plus les réponses sont créatives.
* `reasoning_effort` (facultatif) : pour les modèles de raisonnement, l'effort de raisonnement : `minimal`, `low`, `medium` ou `high`.
  Laissez-le vide pour les modèles qui ne le prennent pas en charge.

## OpenAI

Le réglage `base_url` peut cibler n'importe quelle API compatible OpenAI (par défaut : `https://api.openai.com/v1`).

LLM :

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

Embeddings :

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

LLM (pour les embeddings, les mêmes réglages sans `temperature`) :

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

[Ollama](https://ollama.com/) fait tourner des modèles ouverts en local ou sur vos propres serveurs. Aucune clé d'API n'est nécessaire.

LLM (pour les embeddings, les mêmes réglages sans `temperature`, avec un modèle d'embeddings) :

```json
{
  "provider": "Ollama",
  "model": "llama3.1:8b",
  "base_url": "http://localhost:11434",
  "temperature": 0.7
}
```

## AWS Bedrock

[AWS Bedrock](https://aws.amazon.com/bedrock/) donne accès à des modèles hébergés par AWS (Amazon Nova, Anthropic Claude,
Mistral, embeddings Titan...). Aucune clé d'API n'est stockée : l'orchestrateur utilise les identifiants AWS de son
environnement, et la région est celle du profil AWS sélectionné. Définissez l'une de ces variables d'environnement sur l'orchestrateur :

* `tock_gen_ai_orchestrator_aws_bedrock_credentials_profile_name` : le profil AWS à utiliser,
* `tock_gen_ai_orchestrator_aws_bedrock_credentials_allow_default_profile=true` : utilise la chaîne d'identifiants AWS
  par défaut (rôle IAM, IRSA, variables d'environnement). Sans l'une des deux, les appels échouent.

L'accès aux modèles doit être accordé dans la console Bedrock, pour le compte et la région.

LLM :

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

Les réglages `guardrail_*` (facultatifs) appliquent un [Bedrock Guardrail](https://docs.aws.amazon.com/bedrock/latest/userguide/guardrails.html)
aux appels du LLM : `guardrail_id` et `guardrail_version` se renseignent ensemble, et `guardrail_trace` journalise
le détail des interventions du guardrail.

Embeddings :

```json
{
  "provider": "AwsBedrock",
  "model": "amazon.titan-embed-text-v2:0"
}
```

Voir [RAG sur AWS](../../getting-started/rag-aws.md) pour un déploiement complet avec Bedrock et Amazon OpenSearch.

## Bloomz

Bloomz est un modèle d'embeddings exposé par un service HTTP auto-hébergé. Il est pris en charge par l'orchestrateur Gen AI
(par exemple avec les [outils d'indexation](../indexing.md)), mais ne peut pas être sélectionné dans _Tock Studio_.

```json
{
  "provider": "Bloomz",
  "api_base": "http://bloomz-embedding:8080",
  "pooling": "mean"
}
```

`pooling` (facultatif) est la méthode de pooling du modèle : `mean` ou `last`.

## Clés d'API et gestionnaires de secrets

Une clé d'API (`api_key`) peut être :

* une valeur en clair : `{"type": "Raw", "secret": "..."}`,
* le nom d'un secret dans [AWS Secrets Manager](https://aws.amazon.com/secrets-manager/) : `{"type": "AwsSecretsManager", "secret_name": "..."}`,
* le nom d'un secret dans [GCP Secret Manager](https://cloud.google.com/secret-manager) : `{"type": "GcpSecretManager", "secret_name": "..."}`.

Par défaut, les clés d'API saisies dans _Tock Studio_ sont stockées en clair dans la base Tock.
Pour les stocker plutôt dans un gestionnaire de secrets, définissez les variables d'environnement suivantes sur le backend de
_Tock Studio_ (`tock/bot_admin`) et sur le bot :

| Variable d'environnement              | Description                                              |
|---------------------------------------|----------------------------------------------------------|
| `tock_gen_ai_secret_manager_provider` | `AWS_SECRETS_MANAGER` ou `GCP_SECRET_MANAGER`            |
| `tock_gen_ai_secret_prefix`           | Préfixe des noms de secrets (par défaut : `LOCAL/TOCK`)  |

Les secrets sont alors créés sous la forme `<préfixe>/<namespace>/<bot>/<fonctionnalité>`, et Tock n'en stocke que le nom.
L'orchestrateur Gen AI les lit dans le même gestionnaire de secrets.

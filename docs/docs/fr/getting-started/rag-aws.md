---
title: RAG sur AWS
---

# Faire fonctionner le RAG sur AWS

Ce guide vous accompagne dans la mise en place de la [génération à enrichissement contextuel (RAG)](../gen-ai/rag.md)
pour un bot Tock entièrement sur AWS, depuis un compte tout neuf. Le résultat attendu est un bot
capable de répondre à des questions à partir de vos propres documents. Pour ce faire, on utilisera :

- **Amazon Bedrock** à la fois pour le LLM (génération des réponses) et pour le modèle d'embedding
  (vectorisation des documents/questions).
- **Amazon OpenSearch Service** (managé) comme vector store.
- Les images Docker de [`tock-docker`](https://github.com/theopenconversationkit/tock-docker) pour faire tourner
  la plateforme.
- L'image Docker `tock/llm-indexing-tools` pour indexer vos documents.

Aucun autre service AWS n'est nécessaire pour suivre ce guide.

## 1) Prérequis

- Un compte AWS avec les droits nécessaires pour créer des policies/rôles IAM, activer l'accès aux modèles
  Bedrock, et créer un domaine OpenSearch.
- Docker et Docker Compose installés localement (ou sur la machine où tourne Tock).
- L'[AWS CLI](https://aws.amazon.com/cli/), pour créer le profil AWS utilisé par l'orchestrateur.

## 2) Activer l'accès aux modèles Bedrock

L'accès aux modèles Bedrock doit être explicitement accordé par compte AWS et par région avant de pouvoir être
invoqué.

1. Ouvrez la [console Bedrock](https://console.aws.amazon.com/bedrock/) dans la région que vous comptez utiliser
   (par exemple `eu-west-3` ou `ap-east-2`).
2. Allez dans **Model access** (menu de gauche) et demandez l'accès aux modèles que vous comptez utiliser, à la
   fois pour le **chat/texte** et pour les **embeddings**. Pour un bon compromis coût/qualité pour démarrer, on
   recommande :
   - LLM : `amazon.nova-lite-v1:0` (économique, rapide, suffisant pour la plupart des scénarios RAG) ou
     `anthropic.claude-3-5-haiku-20241022-v1:0` pour une meilleure qualité de réponse.
   - Embedding : `amazon.titan-embed-text-v2:0`.
3. Attendez que la demande d'accès soit approuvée (généralement instantané pour les modèles Amazon).

!!! info
    Les identifiants de modèles sont spécifiques à chaque région : un modèle n'est pas forcément disponible dans
    toutes les régions. Vérifiez la disponibilité pour votre région avant de continuer.

## 3) Créer une identité IAM pour l'orchestrateur

Le `GenAI Orchestrator` (le service qui dialogue réellement avec Bedrock) s'authentifie via la
[chaîne de credentials AWS par défaut](https://docs.aws.amazon.com/sdkref/latest/guide/standardized-credentials.html)
(fichier de credentials partagé, variables d'environnement, instance profile EC2, task role ECS, ou rôle IRSA
EKS). Aucune access key/secret n'est jamais stockée dans les paramètres Tock - seulement un nom de profil (ou
rien du tout, si vous vous reposez sur la chaîne par défaut).

Créez une policy IAM accordant les droits d'invocation sur les modèles activés ci-dessus :

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

Attachez cette policy à l'identité qui exécutera le conteneur de l'orchestrateur :

- **Docker Compose local/dev (ce guide)** : créez un utilisateur IAM et lancez
  `aws configure --profile bedrock-rag`, ce qui vous donne un profil nommé dans `~/.aws/credentials` que l'on va
  monter dans le conteneur.
- **ECS/EKS/EC2 (recommandé en production)** : attachez plutôt la policy à la task role / au service account IRSA
  / à l'instance profile, et laissez de côté le nom de profil/montage de credentials - voir le paramètre
  `..._allow_default_profile` ci-dessous.

!!! note
    Si vous comptez utiliser les [guardrails Bedrock](#8-optionnel-configurer-des-guardrails), ajoutez également
    l'ARN du guardrail à la liste `Resource` ci-dessus une fois celui-ci créé.

## 4) Créer un domaine Amazon OpenSearch Service

1. Ouvrez la [console OpenSearch Service](https://console.aws.amazon.com/aos/home) et créez un nouveau domaine.
2. Choisissez **Fine-grained access control** avec une base d'utilisateurs internes, et définissez un
   utilisateur/mot de passe maître (Tock s'authentifie en HTTP basic auth classique, c'est donc l'option la plus
   simple - pas besoin de signature IAM SigV4).
   > Amazon OpenSearch **Serverless** n'est pas supporté nativement : il n'accepte que des requêtes signées en
   > SigV4 par IAM, ce que l'intégration OpenSearch de Tock n'implémente pas. Utilisez un domaine OpenSearch
   > classique (provisionné).
3. Dans **Network**, choisissez ce qui convient à votre contexte (l'accès VPC est recommandé en production ;
   l'accès public avec une policy restreinte par IP convient très bien pour suivre ce guide).
4. Une fois le domaine `Active`, notez son **endpoint** (sans le préfixe `https://`), par exemple
   `search-my-domain-abc123xyz.eu-west-3.es.amazonaws.com`.

## 5) Lancer la stack Tock

Récupérez la stack Docker Compose RAG/OpenSearch de `tock-docker` comme point de départ :

```bash
mkdir tock-aws-rag && cd tock-aws-rag
curl -o docker-compose.yml https://raw.githubusercontent.com/theopenconversationkit/tock-docker/master/docker-compose-rag-opensearch.yml
curl -o .env https://raw.githubusercontent.com/theopenconversationkit/tock-docker/master/.env
mkdir -p scripts && curl -o scripts/setup.sh https://raw.githubusercontent.com/theopenconversationkit/tock-docker/master/scripts/setup.sh
chmod +x scripts/setup.sh
```

Ce fichier embarque son propre cluster OpenSearch local à 2 nœuds (`opensearch-node1`/`opensearch-node2`/
`opensearch-dashboards`) pour les tests en local. Puisqu'on utilise un domaine OpenSearch AWS managé à la place :

1. Supprimez (ou commentez) les services `opensearch-node1`, `opensearch-node2` et `opensearch-dashboards`, ainsi
   que les volumes `opensearch-data1`/`opensearch-data2` en bas du fichier.
2. Faites pointer le service `gen_ai_orchestrator-server` vers votre domaine AWS au lieu du cluster local, et
   montez vos credentials/config AWS locales pour que le conteneur puisse s'authentifier auprès de Bedrock :

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
      tock_gen_ai_orchestrator_vector_store_pwd: <votre mot de passe maître>
      tock_gen_ai_orchestrator_vector_store_timeout: 5
      tock_gen_ai_orchestrator_vector_store_test_query: virement bancaire
      tock_gen_ai_orchestrator_aws_bedrock_credentials_profile_name: bedrock-rag
```

!!! note
    Vous tournez sur ECS/EKS/EC2 plutôt qu'en Docker Compose ? Retirez le montage `volumes` et la variable
    `..._credentials_profile_name`, et définissez plutôt
    `tock_gen_ai_orchestrator_aws_bedrock_credentials_allow_default_profile: true`, pour que le SDK récupère
    automatiquement le rôle de la task/instance.

Puis lancez la stack :

```bash
docker compose up
```

Une fois tout démarré, _Tock Studio_ est accessible sur [http://localhost](http://localhost)
(identifiants par défaut `admin@app.com` / `password`), et l'orchestrateur lui-même écoute sur
`http://localhost:8000`.

## 6) Indexer vos documents

Les documents sont découpés, vectorisés avec Bedrock et stockés dans OpenSearch par l'[outil d'indexation](../gen-ai/indexing.md),
disponible sous forme d'image Docker `tock/llm-indexing-tools`.

### 6.1) Préparer le fichier CSV

L'outil d'indexation lit un fichier CSV délimité par des barres verticales (`|`), avec trois colonnes : `title`,
`source` (en général l'URL du document) et `text`. Voir [Indexation des documents](../gen-ai/indexing.md#documents-en-entree)
pour le format, et le [tutoriel RAG](rag-tutorial.md#convertir-la-documentation-en-fichier-csv) pour un exemple de
conversion depuis des fichiers Markdown.

Placez le fichier dans un dossier `ingestion`, par exemple `ingestion/data.csv`.

### 6.2) Écrire la configuration de l'indexation

Créez `ingestion/config.json` :

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
      "secret": "<votre mot de passe maître>"
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

* `bot` : le namespace et le nom de l'application, tels que créés dans _Tock Studio_
  (`app` et `new_assistant` avec l'assistant de la plateforme Docker, voir l'[étape 7](#7-configurer-le-bot-dans-tock-studio)).
* `document_index_name` : laissez-le à `null`, pour que l'index soit nommé d'après le namespace, le bot et la session
  d'indexation, comme l'attend _Tock Studio_.

Les autres options sont décrites dans le [tutoriel RAG](rag-tutorial.md#indexer-la-documentation) et dans le
[README](https://github.com/theopenconversationkit/tock/blob/master/gen-ai/orchestrator-server/src/main/python/tock-llm-indexing-tools/README.md)
de l'outil d'indexation.

### 6.3) Lancer l'indexation

L'outil d'indexation appelle Bedrock avec le même code que l'orchestrateur : montez votre configuration AWS et
indiquez-lui le profil AWS à utiliser.

```bash
docker run --rm \
  -v "$PWD/ingestion:/ingestion" \
  -v ~/.aws:/root/.aws:ro \
  -e tock_gen_ai_orchestrator_aws_bedrock_credentials_profile_name=bedrock-rag \
  tock/llm-indexing-tools:{{ tock_version }} \
  python tock-llm-indexing-tools/scripts/indexing/vectorisation/run_vectorisation.py \
  --json-config-file=/ingestion/config.json -v
```

À la fin, l'outil affiche le **nom de l'index** (`ns_app_bot_new_assistant_session_<uuid>`) et
l'**identifiant de session d'indexation** (_Index session ID_). Notez ce dernier : il sert à l'étape suivante.

## 7) Configurer le bot dans Tock Studio

À la première connexion, l'assistant crée une application nommée `new_assistant` dans le namespace `app`
(voir [Créer l'application](rag-tutorial.md#creer-lapplication)).

Allez ensuite dans _Gen AI_ > _Rag settings_ (le rôle **admin** est nécessaire) :

* **Question condensing** et **Question answering**, _Configuration_ :
    * Provider : _AWS Bedrock_
    * Model id : `amazon.nova-lite-v1:0`
    * Temperature : `0.7`
* **Embedding**, _Configuration_ :
    * Provider : _AWS Bedrock_
    * Model id : `amazon.titan-embed-text-v2:0`, le modèle utilisé pour l'indexation
* **Indexing session** : l'**identifiant de session d'indexation** affiché par l'outil d'indexation
* Activez **Rag activated**, puis _Save_

Vous pouvez adapter le prompt de réponse (voir le [guide du prompt RAG](../gen-ai/rag-prompt.md)).

!!! warning
    Le modèle d'embedding indiqué ici **doit** être celui utilisé pour indexer vos documents : mélanger les modèles
    d'embedding entre l'indexation et l'interrogation dégrade silencieusement (voire casse) la qualité de la recherche,
    car les vecteurs ne sont pas comparables.

> Inutile de remplir _Gen AI_ > _Vector DB settings_ : l'orchestrateur utilise déjà votre domaine OpenSearch,
> défini par les variables d'environnement de l'[étape 5](#5-lancer-la-stack-tock). Cet écran ne sert qu'à remplacer
> cette connexion pour un bot donné.

## 8) Optionnel : configurer des guardrails

Les réglages LLM AWS Bedrock prennent en charge les [guardrails Bedrock](https://docs.aws.amazon.com/bedrock/latest/userguide/guardrails.html),
appliqués directement à chaque appel du LLM, sans aller-retour réseau supplémentaire. Une fois le guardrail créé dans
la console Bedrock, renseignez le **Guardrail ID** (identifiant ou ARN) et la **Guardrail Version** (par ex. `DRAFT` ou `1`)
de la configuration _Question answering_ dans _Rag settings_. **Enable Guardrail Trace** journalise le détail des
interventions du guardrail.

N'oubliez pas d'ajouter l'ARN du guardrail à la policy IAM de l'étape 3, sans quoi Bedrock rejettera l'appel avec
une erreur d'accès refusé, même si l'invocation du modèle en elle-même aurait autrement fonctionné.

## 9) Tester

Dans _Tock Studio_, allez dans _Test_ > _Test_ et envoyez une question dont vous savez qu'elle est couverte par vos
documents indexés. Vous devriez obtenir une réponse générée à partir des morceaux de documents récupérés. Sinon,
consultez le tableau de dépannage ci-dessous.

## Dépannage

| Symptôme                                                                          | Cause probable                                                                                                                 | Solution                                                                                                            |
|-----------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| `AccessDeniedException` mentionnant `bedrock:InvokeModel`                         | Accès au modèle non accordé, ou policy IAM ne couvrant pas le modèle/la région                                                 | Revérifiez l'étape 2 (accès aux modèles) et l'étape 3 (ARNs `Resource` de la policy IAM, région incluse)            |
| Erreur `MissingCredentialsProfileName` venant de l'orchestrateur                  | Ni un nom de profil, ni le repli sur le profil par défaut ne sont configurés                                                   | Définissez `tock_gen_ai_orchestrator_aws_bedrock_credentials_profile_name`, ou `..._allow_default_profile=true`     |
| Aucun document récupéré / le bot retombe toujours sur l'histoire "pas de réponse" | Mauvais identifiant de session d'indexation, ou modèle d'embedding différent entre l'indexation et le RAG                      | Revérifiez l'identifiant de session de l'étape 6.3, et que les deux utilisent le même modèle d'embedding            |
| Connexion refusée / timeout vers OpenSearch                                       | La policy réseau du domaine OpenSearch ou le security group ne laisse pas passer l'IP sortante du conteneur de l'orchestrateur | Ajustez la policy d'accès du domaine OpenSearch / le security group VPC                                             |
| `401 Unauthorized` de la part d'OpenSearch                                        | Mauvais utilisateur/mot de passe maître, ou fine-grained access control non activé                                             | Revérifiez l'étape 4 et les identifiants utilisés à la fois dans la configuration d'indexation et dans l'orchestrateur |

## Références

- [Présentation de la fonctionnalité RAG](../gen-ai/rag.md)
- [Fournisseurs de vector store](../gen-ai/providers/vector-store.md)
- [Fournisseurs de LLM/embedding](../gen-ai/providers/llm-embedding.md)
- [`tock-docker`](https://github.com/theopenconversationkit/tock-docker) - images Docker et stacks Compose
- [README de `tock-llm-indexing-tools`](https://github.com/theopenconversationkit/tock/blob/master/gen-ai/orchestrator-server/src/main/python/tock-llm-indexing-tools/README.md)

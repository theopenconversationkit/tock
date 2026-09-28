---
title: RAG sur AWS
---

# Faire fonctionner le RAG sur AWS

Ce guide vous accompagne dans la mise en place de la [génération à enrichissement contextuel (RAG)](../user/studio/gen-ai/features/gen-ai-feature-rag.md)
pour un bot TOCK entièrement sur AWS, depuis un compte tout neuf. Le résultat attendu est un bot
capable de répondre à des questions à partir de vos propres documents. Pour ce faire, on utilisera :

- **Amazon Bedrock** à la fois pour le LLM (génération des réponses) et pour le modèle d'embedding
  (vectorisation des documents/questions).
- **Amazon OpenSearch Service** (managé) comme vector store.
- Les images Docker de [`tock-docker`](https://github.com/theopenconversationkit/tock-docker) pour faire tourner
  la plateforme.
- Les scripts `tock-llm-indexing-tools` pour indexer vos documents.

Aucun autre service AWS n'est nécessaire pour suivre ce guide.

## 1) Prérequis

- Un compte AWS avec les droits nécessaires pour créer des policies/rôles IAM, activer l'accès aux modèles
  Bedrock, et créer un domaine OpenSearch.
- Docker et Docker Compose installés localement (ou sur la machine où tourne TOCK).
- Python >= 3.9 et [Poetry](https://python-poetry.org/) installés localement, pour exécuter l'outillage
  d'indexation.

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
EKS). Aucune access key/secret n'est jamais stockée dans les paramètres TOCK - seulement un nom de profil (ou
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
    Si vous comptez utiliser les [guardrails Bedrock](#8-optionnel--configurer-des-guardrails), ajoutez également
    l'ARN du guardrail à la liste `Resource` ci-dessus une fois celui-ci créé.

## 4) Créer un domaine Amazon OpenSearch Service

1. Ouvrez la [console OpenSearch Service](https://console.aws.amazon.com/aos/home) et créez un nouveau domaine.
2. Choisissez **Fine-grained access control** avec une base d'utilisateurs internes, et définissez un
   utilisateur/mot de passe maître (TOCK s'authentifie en HTTP basic auth classique, c'est donc l'option la plus
   simple - pas besoin de signature IAM SigV4).
   > Amazon OpenSearch **Serverless** n'est pas supporté nativement : il n'accepte que des requêtes signées en
   > SigV4 par IAM, ce que l'intégration OpenSearch de TOCK n'implémente pas. Utilisez un domaine OpenSearch
   > classique (provisionné).
3. Dans **Network**, choisissez ce qui convient à votre contexte (l'accès VPC est recommandé en production ;
   l'accès public avec une policy restreinte par IP convient très bien pour suivre ce guide).
4. Une fois le domaine `Active`, notez son **endpoint** (sans le préfixe `https://`), par exemple
   `search-my-domain-abc123xyz.eu-west-3.es.amazonaws.com`.

## 5) Lancer la stack TOCK

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

Une fois tout démarré, Bot Admin est accessible sur [http://localhost](http://localhost)
(identifiants par défaut `admin@app.com` / `password`), et l'orchestrateur lui-même écoute sur
`http://localhost:8000`.

## 6) Indexer vos documents

Les documents sont découpés en morceaux, vectorisés, puis poussés dans OpenSearch à l'aide du script
`index_documents.py` de `gen-ai/orchestrator-server/src/main/python/tock-llm-indexing-tools` (dans le dépôt
principal [`tock`](https://github.com/theopenconversationkit/tock)).

### 6.1) Installer l'outillage

```bash
git clone https://github.com/theopenconversationkit/tock.git
cd tock/gen-ai/orchestrator-server/src/main/python/tock-llm-indexing-tools
poetry install --no-root
```

### 6.2) Préparer un CSV prêt à indexer

Le script attend un CSV avec trois colonnes : `title`, `source`, `text`. Si votre contenu est déjà dans ce
format, passez directement à l'étape suivante. Sinon, `smarttribune_formatter.py`/`smarttribune_consumer.py` et
`webscraper.py` peuvent aider à le produire à partir d'un export Smart Tribune ou en scrapant des pages web -
voir le [README de l'outil](https://github.com/theopenconversationkit/tock/blob/master/gen-ai/orchestrator-server/src/main/python/tock-llm-indexing-tools/README.md)
pour le détail.

### 6.3) Écrire les configs JSON des embeddings et du vector store

`embeddings_bedrock.json` (le script exécute localement la factory d'embeddings de l'orchestrateur, assurez-vous
donc que votre shell dispose des mêmes credentials AWS, par exemple
`export AWS_PROFILE=bedrock-rag AWS_REGION=eu-west-3`) :

```json
{
  "provider": "AwsBedrock",
  "model": "amazon.titan-embed-text-v2:0"
}
```

`vector_store_opensearch.json` :

```json
{
  "provider": "OpenSearch",
  "host": "search-my-domain-abc123xyz.eu-west-3.es.amazonaws.com",
  "port": 443,
  "username": "admin",
  "password": {
    "type": "Raw",
    "secret": "<votre mot de passe maître>"
  }
}
```

### 6.4) Lancer le script d'indexation

```bash
poetry run python scripts/indexing/index_documents.py \
  --input-csv=data.csv \
  --namespace=my_namespace \
  --bot-id=my_bot_id \
  --embeddings-json-config=embeddings_bedrock.json \
  --vector-store-json-config=vector_store_opensearch.json \
  --chunks-size=1000
```

À la fin, le script affiche un **identifiant de session d'indexation** (un UUID) et le nom de l'**index**
généré (`ns-{namespace}-bot-{bot_id}-session-{uuid4}`). Gardez cet identifiant de session sous la main - vous en
aurez besoin à l'étape suivante.

## 7) Configurer le bot dans TOCK Studio

Ouvrez votre bot dans TOCK Studio et allez d'abord dans **Gen AI > Vector Store Settings**, puis dans
**Gen AI > RAG Settings** (le rôle **botUser** est requis pour les deux écrans).

### 7.1) Vector Store Settings

Configurez la connexion à votre domaine OpenSearch :

```json
{
  "provider": "OpenSearch",
  "host": "search-my-domain-abc123xyz.eu-west-3.es.amazonaws.com",
  "port": "443",
  "user": "admin",
  "password": {
    "type": "Raw",
    "value": "<votre mot de passe maître>"
  }
}
```

### 7.2) RAG Settings - LLM Engine

Sélectionnez **AwsBedrock** comme fournisseur et renseignez :

```json
{
  "provider": "AwsBedrock",
  "model": "amazon.nova-lite-v1:0",
  "temperature": "0.7"
}
```

Rédigez ensuite votre prompt système (voir les
[guides de prompt RAG](https://github.com/theopenconversationkit/tock/blob/master/docs/docs/en/user/studio/gen-ai/rag-chain/rag-prompt-system.md)
pour des exemples).

### 7.3) RAG Settings - Embedding Engine

Sélectionnez à nouveau **AwsBedrock**, en faisant correspondre le modèle utilisé pour l'indexation, et collez
l'**identifiant de session d'indexation** de l'étape 6.4 dans le champ **Indexing session** :

```json
{
  "provider": "AwsBedrock",
  "model": "amazon.titan-embed-text-v2:0"
}
```

!!! warning
    Le modèle d'embedding ici **doit** correspondre à celui utilisé pour l'indexation - mélanger des modèles
    d'embedding différents entre l'indexation et l'interrogation dégrade silencieusement (voire casse) la qualité
    de la recherche, les vecteurs n'étant alors plus comparables entre eux.

### 7.4) Configurer le flux "pas de réponse" et activer le RAG

Renseignez la section **Conversation Flow** (ce que dit le bot quand il ne trouve pas de réponse pertinente),
puis activez le RAG. L'activation n'est possible qu'une fois tous les champs obligatoires renseignés.

## 8) Optionnel : configurer des guardrails

Les paramètres LLM AWS Bedrock supportent les [guardrails Bedrock](https://docs.aws.amazon.com/bedrock/latest/userguide/guardrails.html)
en ligne, appliqués directement sur chaque appel LLM sans aller-retour réseau supplémentaire. Une fois un
guardrail créé dans la console Bedrock, ajoutez son identifiant/sa version à la configuration
**RAG Settings > LLM Engine** :

```json
{
  "provider": "AwsBedrock",
  "model": "amazon.nova-lite-v1:0",
  "temperature": "0.7",
  "guardrailId": "arn:aws:bedrock:eu-west-3:123456789012:guardrail/my-guardrail",
  "guardrailVersion": "1",
  "guardrailTrace": false
}
```

N'oubliez pas d'ajouter l'ARN du guardrail à la policy IAM de l'étape 3, sans quoi Bedrock rejettera l'appel avec
une erreur d'accès refusé, même si l'invocation du modèle en elle-même aurait autrement fonctionné.

## 9) Tester

Dans TOCK Studio, allez sur un canal du bot et envoyez une question dont vous savez qu'elle est couverte par vos
documents indexés. Vous devriez obtenir une réponse générée à partir des morceaux de documents récupérés. Sinon,
consultez le tableau de dépannage ci-dessous.

## Dépannage

| Symptôme                                                                          | Cause probable                                                                                                                 | Solution                                                                                                            |
|-----------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| `AccessDeniedException` mentionnant `bedrock:InvokeModel`                         | Accès au modèle non accordé, ou policy IAM ne couvrant pas le modèle/la région                                                 | Revérifiez l'étape 2 (accès aux modèles) et l'étape 3 (ARNs `Resource` de la policy IAM, région incluse)            |
| Erreur `MissingCredentialsProfileName` venant de l'orchestrateur                  | Ni un nom de profil, ni le repli sur le profil par défaut ne sont configurés                                                   | Définissez `tock_gen_ai_orchestrator_aws_bedrock_credentials_profile_name`, ou `..._allow_default_profile=true`     |
| Aucun document récupéré / le bot retombe toujours sur l'histoire "pas de réponse" | Mauvais identifiant de session d'indexation, ou modèle d'embedding différent entre l'indexation et le RAG                      | Revérifiez l'identifiant de session de l'étape 6.4, et que les deux utilisent le même modèle d'embedding            |
| Connexion refusée / timeout vers OpenSearch                                       | La policy réseau du domaine OpenSearch ou le security group ne laisse pas passer l'IP sortante du conteneur de l'orchestrateur | Ajustez la policy d'accès du domaine OpenSearch / le security group VPC                                             |
| `401 Unauthorized` de la part d'OpenSearch                                        | Mauvais utilisateur/mot de passe maître, ou fine-grained access control non activé                                             | Revérifiez l'étape 4 et les identifiants utilisés à la fois dans la config JSON du vector store et dans TOCK Studio |

## Références

- [Présentation de la fonctionnalité RAG](../user/studio/gen-ai/features/gen-ai-feature-rag.md)
- [Fournisseurs de vector store](../user/studio/gen-ai/providers/gen-ai-provider-vector-store.md)
- [Fournisseurs de LLM/embedding](../user/studio/gen-ai/providers/gen-ai-provider-llm-and-embedding.md)
- [`tock-docker`](https://github.com/theopenconversationkit/tock-docker) - images Docker et stacks Compose
- [README de `tock-llm-indexing-tools`](https://github.com/theopenconversationkit/tock/blob/master/gen-ai/orchestrator-server/src/main/python/tock-llm-indexing-tools/README.md)

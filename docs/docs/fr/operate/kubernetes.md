---
title: Kubernetes
---
# Déployer sur Kubernetes

Le dépôt [`tock-helm-chart`](https://github.com/theopenconversationkit/tock-helm-chart) fournit un chart
[Helm](https://helm.sh/) qui déploie une plateforme Tock complète sur [Kubernetes](https://kubernetes.io/).

Cette page résume l'utilisation du chart. La référence de tous ses paramètres est son
[README](https://github.com/theopenconversationkit/tock-helm-chart/blob/master/charts/tock/README.md).

## Ce qui est déployé

Le chart déploie tous les composants Tock :

| Composant | Image | Ressources Kubernetes |
|-----------|-------|-----------------------|
| _Tock Studio_ | `tock/bot_admin` | Deployment, Service, Ingress |
| Bot API | `tock/bot_api` | Deployment, Service, Ingress |
| NLP API | `tock/nlp_api` | Deployment, Service |
| Build worker | `tock/build_worker` | Deployment |
| Duckling | `tock/duckling` | Deployment, Service |
| Compilateur Kotlin | `tock/kotlin_compiler` | Deployment, Service |
| Orchestrateur Gen AI | `tock/gen-ai-orchestrator-server` | Deployment, Service |

Il peut aussi déployer les bases de données sous forme de sous-charts, ou se connecter à des bases existantes :

- [MongoDB](https://www.mongodb.com/) (chart Bitnami), déployé par défaut en _replica set_ de 3 nœuds
- [OpenSearch](https://opensearch.org/) (et éventuellement OpenSearch Dashboards), comme base vectorielle pour le [RAG](../gen-ai/rag.md)
- [PostgreSQL](https://www.postgresql.org/) avec [PGVector](https://github.com/pgvector/pgvector), comme autre base vectorielle

![Tock sur Kubernetes](https://raw.githubusercontent.com/theopenconversationkit/tock-helm-chart/master/tock-24x-on-k8s.png)

Voir la page [architecture](architecture.md) pour le rôle de chaque composant.

## Installation

Le chart est publié à la fois comme artefact OCI et dans un dépôt Helm classique :

- Artefact OCI : `oci://ghcr.io/theopenconversationkit/charts/tock`
- Dépôt Helm : `https://theopenconversationkit.github.io/tock-helm-chart/`, chart `tock`

=== "OCI"

    ```shell
    helm install mytock oci://ghcr.io/theopenconversationkit/charts/tock --version <version-du-chart> -f values.yaml
    ```

=== "Dépôt Helm"

    ```shell
    helm repo add tock https://theopenconversationkit.github.io/tock-helm-chart/
    helm repo update
    helm search repo tock
    helm install mytock tock/tock --version <version-du-chart> -f values.yaml
    ```

> Chaque version du chart déploie par défaut une version donnée de Tock (`appVersion` dans `helm search repo tock`).
> Pour déployer une autre version de Tock, renseignez l'`image.tag` de chaque composant dans votre fichier de valeurs.

Une fois la release installée, Helm affiche les URL de _Tock Studio_ et de la Bot API.
L'identifiant/mot de passe par défaut de _Tock Studio_ est `admin@app.com` / `password` : changez-le avant d'ouvrir
la plateforme (voir [Authentification](#authentification)).

## Principaux paramètres

Les paramètres sont regroupés par composant (`adminWeb`, `botApi`, `nlpApi`, `buildWorker`, `duckling`,
`kotlinCompiler`, `genAiOrchestrator`) et dans une section `global`. Pour chaque composant, vous pouvez régler
l'image, le nombre de réplicas, les ressources, les contextes de sécurité, les contraintes de placement et les
variables d'environnement (section `environment`, voir la [référence de configuration](configuration.md)).

### Ingress

Les composants `adminWeb` et `botApi` sont exposés via un Ingress, sur les hôtes suivants :

- `tockstudio-<release>.<global.wildcardDomain>` pour _Tock Studio_
- `bot-api-<release>.<global.wildcardDomain>` pour la Bot API

```yaml
global:
  wildcardDomain: tock.mondomaine.com
  ingressClassName: nginx

adminWeb:
  ingress:
    enabled: true
    tls:
      - secretName: tock-tls
        hosts:
          - tockstudio-mytock.tock.mondomaine.com

botApi:
  ingress:
    enabled: true
```

Seule la Bot API doit être accessible aux canaux externes (voir [Exposition réseau](installation.md#exposition-reseau)).

### MongoDB

Par défaut, le chart déploie MongoDB en _replica set_ (`global.deployMongoDb.enabled: true`), configuré avec la
section `mongodb` (voir le [chart Bitnami](https://artifacthub.io/packages/helm/bitnami/mongodb)).
La persistance est activée avec un volume de 1 Gio : augmentez `mongodb.persistence.size` pour un usage réel.

Pour utiliser une base existante, désactivez le sous-chart et indiquez la chaîne de connexion :

```yaml
global:
  deployMongoDb:
    enabled: false
  mongodbUrls: mongodb://myuser:mypass@fqdn-node1:27017,fqdn-node2:27017,fqdn-node3:27017/mydb?replicaSet=rs0
  mongodbcheckfqdn: fqdn-node1
  mongodbPort: "27017"
```

> La base doit être un _replica set_ (voir [Base de données MongoDB](installation.md#base-de-donnees-mongodb)).

### Base vectorielle

La base vectorielle utilisée par l'[orchestrateur Gen AI](../gen-ai/index.md) se choisit dans la section `global` :

=== "OpenSearch"

    ```yaml
    global:
      deployOpenSearch:
        enabled: true            # déploie le sous-chart OpenSearch
        dashboardEnabled: false  # déploie éventuellement OpenSearch Dashboards
    ```

=== "PGVector"

    ```yaml
    global:
      deployPgVector:
        enabled: true            # déploie le sous-chart PostgreSQL avec PGVector

    postgresql:
      auth:
        postgresPassword: <mot-de-passe>
    ```

=== "Base existante"

    ```yaml
    global:
      deployOpenSearch:          # ou deployPgVector avec pgVectorHost, pgVectorPort...
        useExisting: true
        openSearchHost: opensearch.mondomaine.com
        openSearchPort: "9200"
        openSearchUser: <utilisateur>
        openSearchPwd: <mot-de-passe>
    ```

Les paramètres de connexion de l'orchestrateur sont alors renseignés par le chart. Les fournisseurs de LLM et
d'embeddings se configurent dans _Tock Studio_ (voir [Fournisseurs de LLM et d'embeddings](../gen-ai/providers/llm-embedding.md)).

### Authentification

Les utilisateurs de _Tock Studio_ peuvent être définis dans une ConfigMap, référencée par `adminWeb.authConfigMap` :

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: admin-web-auth-cfg
data:
  tock_users: "alice@tock.ai,bob@tock.ai"
  tock_passwords: "secret1,secret2"
  tock_organizations: "tock,tock"
  tock_roles: "botUser,nlpUser|botUser|admin|technicalAdmin"
```

```yaml
adminWeb:
  authConfigMap: admin-web-auth-cfg
```

Dans cet exemple, Alice a le rôle `botUser`, et Bob a tous les rôles. Voir [Sécurité](security.md#roles)
pour les rôles, et pour l'authentification OAuth2 / OpenID Connect.

### Certificats d'entreprise

Si votre bot ou l'orchestrateur appelle des services signés par une autorité de certification interne, le chart
peut construire un _truststore_ à partir d'un Secret :

```shell
kubectl create secret generic corp-root-cert --from-file=corp-root-cert.crt
```

```yaml
botApi:
  truststore:
    enabled: true
    certSecret: corp-root-cert

genAiOrchestrator:
  truststore:
    enabled: true
    certSecret: corp-root-cert
```

### Déploiement sans accès Internet

Avec les modèles OpenAI, l'orchestrateur utilise `tiktoken`, qui télécharge ses encodages au démarrage. Sans accès
Internet, fournissez-les dans une image de conteneur d'initialisation, activée avec
`genAiOrchestrator.langchain.tiktokencache`. Le [README](https://github.com/theopenconversationkit/tock-helm-chart/blob/master/charts/tock/README.md#solve-langchain-and-tiktoken-issues-on-on-premise-deployments)
du chart décrit comment construire cette image.

## Recommandations pour la production

- Gardez un seul réplica pour `adminWeb`, `buildWorker` et `kotlinCompiler`, et plusieurs réplicas pour
  `botApi`, `nlpApi`, `duckling` et `genAiOrchestrator` (voir [Cloud](cloud.md#kubernetes)).
- Renseignez les `resources` de chaque composant, avec une limite mémoire supérieure au tas maximal de la JVM
  (voir [Mémoire JVM & Docker](installation.md#memoire-jvm-docker)).
- Activez `global.NetworkPolicy.enabled` pour restreindre le trafic entre les pods.
- Revoyez la configuration MongoDB (authentification, persistance, sauvegardes), ou utilisez une base managée.
- Changez les mots de passe par défaut (_Tock Studio_, OpenSearch, PostgreSQL).

## Exemples

Le dossier [`samples`](https://github.com/theopenconversationkit/tock-helm-chart/tree/master/samples) du dépôt
contient des fichiers de valeurs pour plusieurs environnements : [Rancher Desktop](https://rancherdesktop.io/) ou k3s
(Intel et ARM), GKE avec l'ingress natif ou NGINX, un MongoDB externe, l'authentification locale, le RAG avec
OpenSearch ou PGVector, et un LLM local avec [Ollama](https://ollama.com/).

Par exemple, pour tester Tock avec un RAG sur Rancher Desktop :

```shell
git clone https://github.com/theopenconversationkit/tock-helm-chart.git && cd tock-helm-chart
helm install mytock ./charts/tock -f samples/values-rancher-arm-with-rag-and-pgvector.yaml
```

_Tock Studio_ est alors accessible à l'adresse `http://tockstudio-mytock.rancher.localhost`.

> Sur les processeurs ARM, ou sans instructions AVX, l'image MongoDB par défaut ne démarre pas :
> les exemples utilisent une image compatible (`mongodb.image`).

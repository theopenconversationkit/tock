---
title: Serverless
description: "Faire tourner des bots Tock et l'API NLP sur Cloud Run ou AWS Lambda, sans coût quand ils ne sont pas utilisés."
---

# Serverless

Les bots Tock et l'API NLP peuvent tourner dans un process serverless (GCP Cloud Run, AWS Lambda) : le process
est gelé entre les requêtes, ou arrêté quand il n'y en a pas, si bien qu'un bot qui reçoit peu de messages ne
coûte rien.

Le runtime serverless n'a ni Vert.x ni MongoDB : il stocke ses données dans DynamoDB, et l'admin garde MongoDB.

!!! warning
    Le runtime serverless est nouveau. Seuls les connecteurs qui déclarent des routes indépendantes du serveur y
    tournent (voir [Connecteurs](#connecteurs)).

## Architecture

```
                          publication (change streams + périodique)
 bot_admin  ─────────────────────────────────────────────────────►  DynamoDB  ◄──── bot / API NLP serverless
 (Vert.x,   ◄─────────────────────────────────────────────────────   tables         (sans Vert.x ni MongoDB)
  MongoDB)               recopie des écritures du runtime (Streams)
```

* L'admin (`bot_admin`) reste un serveur permanent sur MongoDB : les recherches de _Tock Studio_ en ont besoin.
* Le runtime ne lit et n'écrit que par clé (par utilisateur, par application, par phrase exacte) : c'est ce que
  DynamoDB fait bien, et DynamoDB ne coûte rien quand il n'est pas utilisé.
* L'admin publie sa configuration (applications, stories, libellés, features, réglages, applications NLP,
  intentions, types d'entités, phrases validées...) dans DynamoDB : à chaque modification, et complètement toutes
  les 5 minutes. Le runtime vérifie, au plus toutes les 10 secondes et au début d'une requête, si une publication
  a eu lieu, et recharge ce qui a changé.
* L'admin recopie les écritures du runtime (dialogues, timelines des utilisateurs, métriques, phrases de l'inbox,
  journaux d'analyse, compteurs de phrases inconnues...) des DynamoDB Streams vers MongoDB, pour que _Tock Studio_
  les affiche.
* Les modèles NLP et les fichiers uploadés sont stockés dans S3 (ou tout stockage compatible S3).

## Le runtime

### Bots

Ajoutez au projet les dépendances d'un bot serverless :

```xml
<dependency>
    <groupId>ai.tock</groupId>
    <artifactId>tock-serverless-bot</artifactId>
    <version>${tock.version}</version>
    <type>pom</type>
</dependency>
```

et démarrez les bots avec le stockage DynamoDB :

```kotlin
fun main() {
    ServerlessBotLauncher.setup(botDynamoModule)
    BotRepository.registerBotProvider(MyBotProvider)
    ServerlessBotLauncher.start()
}
```

`start` démarre un serveur HTTP sur `PORT` (Cloud Run, Lambda Web Adapter). Une requête ne répond qu'une fois
terminé le travail qu'elle a lancé : la réponse du bot est envoyée avant que le webhook ne réponde, et rien ne
tourne pendant que le process est gelé.

### Webhooks acquittés tout de suite

Certains canaux (WhatsApp, Messenger) attendent un acquittement rapide de leurs webhooks. Avec une file, les
webhooks sont acquittés tout de suite, et traités quand la file les livre :

```kotlin
ServerlessBotLauncher.start(
    queueing =
        RequestQueueing(
            // tock-serverless-sqs, ou PubSubRequestQueue(topic) { ... } (tock-serverless-pubsub)
            queue = SqsRequestQueue(queueUrl) { checkNotNull(ServerlessRequestHandler.queueing) },
            queuedPaths = setOf("/whatsapp"),
            // signe les messages de la file : au moins 16 caractères
            secret = mySecret,
            // partagée par les instances (tock-serverless-dynamodb)
            deduplication = DynamoDeduplicationStore(),
        ),
)
```

La file livre les messages sur `POST /_queue/sqs` (SQS derrière le Lambda Web Adapter) ou `/_queue/pubsub` (un
abonnement push Pub/Sub). Un webhook envoyé deux fois est traité une fois.

### Tâches périodiques

Un process gelé n'exécute jamais de minuterie. Les tâches périodiques sont enregistrées par nom, et un
ordonnanceur externe (EventBridge Scheduler, Cloud Scheduler) les déclenche par `POST /_tasks/<nom>` avec le
`tock_serverless_tasks_token` dans `X-Auth-Token`. Les noms sont journalisés au démarrage, par exemple
`translator_statscache` (statistiques des libellés).

### API NLP

Le module `tock-serverless-nlp` fait tourner l'API NLP sur la configuration publiée et sur les modèles stockés
dans S3 par l'admin :

```kotlin
fun main() {
    ServerlessNlpLauncher.setup(nlpDynamoModule, listOf(ducklingModule))
    ServerlessNlpLauncher.start()
}
```

Les applications sont créées dans l'admin, et les journaux y sont recherchés : `application/create` et
`logs/count` répondent 501.

### AWS Lambda

Sans le Lambda Web Adapter, le handler `ai.tock.serverless.lambda.LambdaHandler::handleRequest` reçoit :

* les requêtes HTTP d'une function URL ou d'une API Gateway HTTP API (format 2.0) ;
* les événements SQS de la file des webhooks, avec les échecs partiels de lot (`ReportBatchItemFailures`) ;
* les événements EventBridge Scheduler dont l'entrée est `{"tock_task": "<nom>"}`.

Le process est composé par une `ServerlessApplication`, déclarée dans
`META-INF/services/ai.tock.serverless.ServerlessApplication` :

```kotlin
class MyBotApplication : ServerlessApplication {
    override fun install() {
        ServerlessBotLauncher.setup(botDynamoModule)
        BotRepository.registerBotProvider(MyBotProvider)
        ServerlessBotLauncher.install()
    }
}
```

Elle est installée à l'initialisation de la fonction : avant l'instantané, avec SnapStart.

## L'admin

Ajoutez `tock-bot-storage-dynamodb-publication` et `tock-bot-storage-dynamodb-replication` au classpath de
`bot_admin`, et activez-les :

```
tock_dynamodb_publication_enabled=true
tock_dynamodb_replication_enabled=true
```

Les modèles doivent être construits dans S3 : définissez `tock_nlp_model_storage=s3` sur `bot_admin` et
`build_worker`.

Voir la [référence de configuration](configuration.md#runtime-serverless) pour les autres propriétés.

## Connecteurs

`tock-serverless-bot` apporte les connecteurs qui déclarent des routes indépendantes du serveur, sans leurs
dépendances Vert.x : WhatsApp Cloud, Messenger, WhatsApp, Slack, Google Chat, Teams, Mattermost, Alcmeon, iAdvize,
REST, web et API OpenAI.

* **Web et API OpenAI.** Leurs réponses sont du JSON, ou des server-sent events pour les réponses en streaming
  (`tock_web_direct_sse` pour le connecteur web, `"stream": true` pour une chat completion). Les événements arrivent
  au client au fil de l'eau avec Cloud Run ; une réponse AWS Lambda mise en tampon les envoie tous à sa fin.
* **Sécurité web.** Les utilisateurs sont authentifiés par une `HttpWebSecurity`, choisie par le
  `web_security_mode` du connecteur : `PASSTHROUGH`, `COOKIES` et `COOKIES_ENCRYPTED` sont fournies, avec les cookies
  des handlers Vert.x. Un projet qui lie son propre `WebSecurityHandler` Vert.x (un mode `JWT`...) garde les routes
  Vert.x : pour tourner dans le runtime serverless, il lie à la place une `HttpWebSecurity` avec le tag de son mode.
* **Non pris en charge :** les canaux de server-sent events de `tock-bot-connector-web-sse` (`tock_web_sse` :
  connexions longues et file de messages entre les instances dans MongoDB), les notifications des connecteurs web et
  API OpenAI (elles passent par ces canaux), l'orchestration de bots (`tock-bot-orchestration`) et le mode websocket
  de _Bot API_.

## Offres gratuites

Un Tock pour quelques bots qui reçoivent peu de messages peut tourner dans les offres gratuites : le runtime sur
Cloud Run ou Lambda, les tables dans DynamoDB, et le MongoDB de l'admin sur un cluster gratuit de MongoDB Atlas.
Vérifiez les offres actuelles des fournisseurs : elles changent.

### DynamoDB

L'offre gratuite de DynamoDB couvre 25 Go, 25 unités de lecture et 25 unités d'écriture provisionnées, et 2,5
millions de lectures de streams par mois : pas les requêtes des tables à la demande, que Tock crée par défaut. Créez
les tables avec une capacité provisionnée :

```
tock_dynamodb_billing_mode=provisioned
# pour chacune des 3 tables et des 3 index : 24 unités
tock_dynamodb_read_capacity_units=4
tock_dynamodb_write_capacity_units=4
```

Le mode s'applique à la création des tables : celui des tables existantes se change avec
`aws dynamodb update-table --billing-mode PROVISIONED`. Une table provisionnée limite les requêtes au-delà de ses
unités (le SDK les réessaie) : cela convient à des bots qui reçoivent au plus quelques messages par seconde.

Sur l'admin, lisez les streams moins souvent, et comparez la publication aux tables moins souvent (chaque publication
complète lit les éléments publiés) :

```
# 3 tables lues toutes les 10 s : moins d'un million de lectures de streams par mois
tock_dynamodb_replication_interval_in_ms=10000
tock_dynamodb_publication_interval_in_s=1800
```

### MongoDB Atlas

Un cluster Atlas gratuit a 0,5 Go de stockage (données et index), 500 connexions, 100 opérations par seconde et 500
collections.

* **Stockage.** Réduisez la durée de conservation de ce qu'écrit le runtime, sur chaque process qui utilise MongoDB
  (`bot_admin`, `build_worker`) : les index sont recréés quand leur durée change, et deux process avec des valeurs
  différentes les recréeraient tour à tour.

    ```
    tock_bot_timeline_index_ttl_days=90
    tock_bot_flow_stats_index_ttl_days=90
    tock_user_log_index_ttl_days=90
    tock_nlp_log_stats_index_ttl_days=90
    # les phrases de l'inbox seulement : les phrases validées sont gardées
    tock_nlp_classified_sentences_index_ttl_days=30
    ```

    Les dialogues (`tock_bot_dialog_index_ttl_days`) et les journaux d'analyse (`tock_nlp_log_index_ttl_days`) sont
    gardés 7 jours par défaut.

* **Connexions.** Chaque process a deux pools de connexions : les requêtes (`tock_mongo_url`) et les change streams
  (`tock_async_mongo_url`, par défaut la même URL). Le driver MongoDB ouvre jusqu'à 100 connexions par pool et par
  nœud. Limitez-les dans les URL ; l'admin écoute une vingtaine de change streams, qui gardent chacun une connexion
  pendant leur attente :

    ```
    tock_mongo_url=mongodb+srv://user:password@cluster.mongodb.net/?retryWrites=true&maxPoolSize=10
    tock_async_mongo_url=mongodb+srv://user:password@cluster.mongodb.net/?retryWrites=true&maxPoolSize=30
    ```

## Limites

* Les compteurs recopiés de DynamoDB vers MongoDB (statistiques des libellés, phrases inconnues) et les journaux
  d'analyse sont comptés deux fois si un enregistrement d'un stream est relu (point de reprise perdu).
* Au premier démarrage de la recopie, seules les 24 dernières heures des streams sont recopiées.
* Les statistiques des libellés gardées en mémoire sont perdues si l'instance s'arrête avant le passage suivant
  de la tâche `translator_statscache`.
* Sans modèle dans S3, l'API NLP ne reconnaît que les phrases validées.

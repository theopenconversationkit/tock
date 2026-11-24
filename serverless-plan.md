# Tock serverless, à coût nul quand il n'est pas utilisé

État au 24/11/2026. Quatre branches locales, non poussées :

| Branche | Worktree | Commits | Contenu |
|---|---|---|---|
| `refactor/serverless-core-split` | `~/dev/tock-split` | 21 | Découpage cœur / façades (étapes 0 à 6) et runtime serverless sur DynamoDB (étape 7) |
| `feat/s3-storage` | `~/dev/tock-s3` | 1 | Modèles NLP et fichiers uploadés sur S3 ; reporté sur `refactor/serverless-core-split` (lot C) |
| `feat/poll-change-detection` | `~/dev/tock-versions` | 1 | Mode `poll` : `tock_versions` et vérification des modèles à l'usage |
| `spike/dynamodb` | `~/dev/tock-dynamo-spike` | 1 | Spike de port DynamoDB de 3 DAO ; repris et dépassé par `refactor/serverless-core-split`, à abandonner |

Les quatre partent d'`origin/master` (1fee3fcca). Ce fichier n'est commité sur aucune d'elles.

## Décisions

- **Base de données** (révisé le 21/11) : l'admin reste sur MongoDB, les services exposés passent sur DynamoDB.
  - L'admin est un back-office sans problème de charge : il garde Mongo et ses recherches (Atlas M0 gratuit, puis Atlas Flex de 8 à 30 $/mois).
  - bot-api, nlp-api et les connecteurs n'utilisent que des accès par clé (par `userId`, par application, par texte exact) : c'est le terrain de DynamoDB. La recherche du Studio, qui ne passe pas à l'échelle sur Dynamo, reste côté admin.
  - Le port complet DynamoDB (environ 4 à 5 mois-personne) reste écarté : on ne porte que les méthodes d'exécution des DAO.
  - DocumentDB est écarté : plancher de 30 à 60 $/mois, VPC donc NAT, pas de collation.
- **Échanges admin ↔ exécution** : l'admin publie sa configuration dans DynamoDB, l'exécution y écrit ses données, et l'admin les recopie dans Mongo depuis les DynamoDB Streams. Aucun des deux ne lit la base de l'autre.
  - Publication automatique (change streams + passage complet périodique), sans bouton « publier » : une modification du Studio s'applique comme aujourd'hui. **Choix à confirmer.**
- **Binaires hors de Mongo** pour tenir dans M0 : modèles NLP (GridFS) et fichiers uploadés (collection `cache`), stockés sur S3 ou tout stockage compatible S3 (dont GCS).
- **Cible** :
  - GCP Cloud Run en facturation par instance, avec un minimum de 0 instance, est le plus tolérant : le CPU reste disponible tant que l'instance vit.
  - AWS Lambda impose le mode serverless complet (traitement dans la requête, file d'attente, tâches déclenchées de l'extérieur).
- **Vert.x** (révisé le 21/11) : conservé pour l'existant et pour l'admin, retiré de la version serverless. Les artefacts actuels deviennent des façades « cœur + Vert.x/Mongo », sans aucun changement pour les projets existants.

## Architecture des modules

Principe : extraire vers le bas. Le code sans Vert.x ni Mongo descend dans des modules `*-core`. Les artefacts existants gardent leurs coordonnées, leurs packages et leurs dépendances transitives, et dépendent du cœur. La version serverless assemble le cœur et de nouveaux adaptateurs.

```
tock-shared ──────────► tock-shared-core ◄──────────── tock-serverless-runtime
(vertx/*, Mongos,        (Properties, IOC, Executor,    (exécuteur lié à la requête, serveur JDK,
 vertxExecutor lié)       SPI HTTP neutre,               file d'attente, dédup, /_tasks)
                          ExecutionScope, LeasedUserLock)   ▲   ▲   ▲
                                                            │   │   └ tock-serverless-dynamodb (dédup)
                                                            │   └──── tock-serverless-sqs
                                                            └──────── tock-serverless-pubsub
tock-bot-engine ──────► tock-bot-engine-core ◄──────── tock-bot-storage-dynamodb (DAO d'exécution)
(BotVerticle,            (BotRepository, BotBus,
 VertxBotServer,          XxxRuntimeDAO, BotServer)
 NlpProxy)
tock-bot-storage-mongo ─► tock-bot-storage-model ◄──── tock-bot-storage-dynamodb
                          (documents *Col, sans pilote)
tock-bot-connector-* ─► tock-bot-engine-core (WhatsApp Cloud et Messenger migrés)
tock-nlp-api-service ─► tock-nlp-api-core ◄─────────── tock-nlp-front-storage-dynamodb
tock-storage-dynamodb-core (DynamoStore, DocumentCodec) : partagé par les stockages Dynamo bot et NLP
admin : + tock-bot-storage-dynamodb-publication (Mongo → Dynamo)
        + tock-bot-storage-dynamodb-replication (Streams → Mongo)
```

Un POM de démarrage `tock-serverless-bot` assemblera la version serverless, verrouillé par `maven-enforcer` (`bannedDependencies` sur `io.vertx:*`, `org.mongodb:*`, `org.litote.kmongo:*`). Il n'existe pas encore.

### Décisions d'interface

1. **Connecteurs** : `ConnectorController.registerRoutes(id, HttpRoutes)` neutre, en plus des méthodes `Router` gardées (défaut : `UnsupportedOperationException`). En mode classique, `VertxBotServer` monte les routes neutres sur le `Router`. Le runtime serverless refuse au démarrage tout connecteur qui utilise encore `registerServices(Router)`.
2. **`ExecutionScope`** remplace `verticle.launch` dans `TockConnectorController.handle` : verticle en classique, exécution attendue dans la requête en serverless.
3. **`setPeriodic` nommé** : le runtime serverless expose les tâches sur `POST /_tasks/{name}` (EventBridge Scheduler, Cloud Scheduler). La variante sans nom reste.
4. **DAO séparés par composition** (révisé, l'héritage prévu au départ est écarté) : interfaces `XxxRuntimeDAO` avec les seules méthodes d'exécution ; le code d'exécution appelle `XxxRuntimeDAO.fromInjector()`, qui renvoie le DAO d'exécution lié, sinon `XxxDAO.asRuntimeDAO()`. Une redéfinition ne peut pas garder les valeurs par défaut des paramètres, or les projets compilés appellent les méthodes `…$default` des DAO complets : ceux-ci restent inchangés.
5. **Liaisons par défaut inchangées** : `sharedModule` lie toujours `vertxExecutor()`. Les nouveaux comportements ne s'activent que par un nouveau module ou une nouvelle propriété.

### Règles de compatibilité

- Mêmes groupId, artifactId, packages et noms complets de classes.
- Fonction de premier niveau déplacée de fichier : `@file:JvmName` pour garder sa classe `XxxKt`.
- `internal` utilisé des deux côtés d'une coupure : `public` + `@InternalTockApi` (opt-in de niveau `ERROR`).
- Contrôles en CI (`scripts/api-compat`, job `api-compat`) : japicmp et diff des dépendances runtime de chaque module, contre le merge-base avec la branche cible. La 26.9.1 ne peut pas servir de référence : master a déjà cassé l'API Mongo (Jackson 3 / kmongo-dsl). S'y ajoutent les tests de bytecode des modules `*-core`, les suites existantes, les samples et l'e2e tock-docker.

### Étapes

| # | Étape | Risque pour l'existant | État |
|---|---|---|---|
| 0 | Contrôles CI : japicmp, diff des dépendances, règle d'architecture | nul | fait |
| 1 | `tock-shared-core` ; `tock-shared` devient une façade | faible | fait |
| 2 | SPI HTTP neutre, `ExecutionScope`, `setPeriodic` nommé ; adaptateurs Vert.x dans `tock-shared` | faible | fait |
| 3 | `tock-bot-engine-core` ; `tock-bot-engine` devient une façade | moyen (83 déclarations `internal`) | fait |
| 4 | DAO séparés exécution / admin | faible | fait |
| 5 | Connecteurs un par un vers la SPI neutre | faible à moyen (classpath transitif, vérifié par le diff) | 2 sur 12 (WhatsApp Cloud, Messenger) |
| 6 | `tock-nlp-api-core` | faible | fait, sauf 3 routes |
| 7 | Runtime serverless, stockage DynamoDB ×2, publication, recopie Streams → Mongo, POM de démarrage | nul (ajout seul), sauf l'extraction de `tock-bot-storage-model` | côté bot : fait, y compris la détection des publications ; NLP : stockage, publication et recopie faits, runtime à faire |

Les étapes 0 à 6 se mergent sur master sans changement de comportement. Dernier état vérifié (8a54079da) : `mvn clean verify` passe sur tout le dépôt, 1 181 tests, 0 échec ; `api-compat` ne trouve aucune incompatibilité binaire, aucune dépendance perdue et aucune nouvelle classe en double.

## Avancement détaillé

Branche `refactor/serverless-core-split`, un commit par étape ou par tranche.

### Étape 0 : contrôles CI

- `scripts/api-compat` : japicmp sur l'union des jars publiés, et diff des dépendances runtime de chaque module. Référence réutilisable par `--baseline-dir`.
- Filtres pour Kotlin : `internal` est public en bytecode, et le nom JVM d'un membre `internal` porte le nom du module. Les classes `internal` sont repérées dans les sources de la référence (y compris celles générées par kapt), et les constructeurs des classes dont le seul constructeur est `internal` sont ignorés.
- `clean` avant de construire, et échec sur toute nouvelle classe en double (`CompilationResult` était déjà en double sur master, avec le compilateur K2).
- Vérifié dans les deux sens : un arbre inchangé passe, et une méthode supprimée comme une dépendance passée en `provided` sont détectées.

### Étape 1 : `tock-shared-core`

- Verrouillé par l'enforcer (ni Vert.x, ni Netty, ni pilote Mongo). `IOCs.kt` est entier dans le cœur ; les liaisons Vert.x et Mongo de `sharedModule` sont ajoutées par `tock-shared` via `ServiceLoader`.
- Restent dans `tock-shared` : les exceptions REST (`HttpResponseStatus` de Netty), `TockUserListener` (`User` de Vert.x) et `Dice` (`ObjectId` de bson).
- Limite connue : un projet qui exclut toutes les dépendances transitives de `tock-shared` (`*:*`) doit ajouter `tock-shared-core`. C'était le cas de `nlp-api-client`, passé à `tock-shared-core`.

### Étape 2 : SPI HTTP et exécution

- `ai.tock.shared.http` : `HttpRequest`, `HttpResponse` et `HttpRoutes`, avec des handlers `suspend`. Chemins à la syntaxe de Vert.x (`:param`, `*` final). `find` et `handle` servent au runtime qui n'a pas de routeur.
- `ExecutionScope` et `CoroutineExecutionScope` : l'abstraction de `verticle.launch`.
- `Executor.setPeriodic(name, …)` : méthode par défaut qui délègue à la variante sans nom. Les appels existants ne sont pas renommés : un mock mockk strict d'`Executor` dans un projet ne les intercepterait plus.
- Adaptateur Vert.x : `Router.install(routes, scope)` renvoie les `Route` créées, pour pouvoir les désinstaller.

### Étape 3 : `tock-bot-engine-core`

- `tock-bot-engine` ne garde que `BotVerticle`, `NlpProxyBotService` et `VertxBotServer`. `BotRepository` et `TockConnectorController` passent par l'interface `BotServer`, chargée par `ServiceLoader`.
- Le cœur compile contre Vert.x et `tock-shared` en `provided`, pour les signatures publiques qui exposent leurs types. L'enforcer interdit ces dépendances en `compile` et en `runtime`.
- `BotEngineCoreBytecodeTest` n'autorise de références qu'à cinq classes, utilisées par le serveur Vert.x : `ConnectorProvider`, `TockConnectorController`, `VertxHealthchecks`, `WebSocketController` et `UploadedFilesService`. Le healthcheck par défaut est sorti dans `VertxHealthchecks`, sinon l'initialisation de `BotRepository` aurait lié `RoutingContext` via `invokedynamic`.
- Vérifié avec `jdeps` : `translator-core`, `stt-core`, `nlp-api-client` et les modules gen-ai n'utilisent aucune classe propre à `tock-shared`. Le cœur peut donc les exclure.

### Étape 4 : DAO d'exécution du bot

- 12 interfaces `XxxRuntimeDAO` : timeline utilisateur, configurations des bots et des stories, les cinq configurations suivies par les moniteurs, features, métriques, indicateurs et i18n. Méthodes repérées par analyse, confirmées par le compilateur.
- Les stockages existants, `bot/test-base` et les projets qui lient leurs propres DAO fonctionnent sans changement.

### Étape 5 : connecteurs

- `ConnectorController.registerRoutes(id, HttpRoutes)`, `ConnectorProvider.createHttpRequestFilter` et le filtre neutre `HttpRequestFilter` (mêmes règles que `RequestFilter` : IP directe ou dernière de `X-Forwarded-For`, en-tête `X-Auth-Token`).
- WhatsApp Cloud et Messenger migrés, avec des réponses identiques : pas de `Content-Type`, 500 si la vérification échoue, 403 ou 200 selon le cas. Messenger traite les événements avec l'`Executor` (même pool de workers Vert.x, sans ordre garanti, comme avant). Un test de bytecode par connecteur vérifie qu'il ne référence plus Vert.x.
- Les dépendances des connecteurs ne changent pas (le diff de classpath l'interdit) : le POM de démarrage serverless devra exclure `tock-bot-engine` et Vert.x.

### Étape 6 : `tock-nlp-api-core`

- `NlpApiRoutes` expose les routes d'exécution de nlp-api en `HttpRoutes` (parse, evaluate, merge, unknown, intents, application, application/create, logs/count), avec les mêmes codes et corps JSON qu'`endJson`, sans authentification ni import de dumps. `NlpVerticle` est inchangé.
- Parent `tock-nlp` : le parent `tock-nlp-api` ramènerait `tock-shared` via `tock-aws-tools`.
- `ParserService` et `ConfigurationRepository` passent par `ApplicationRuntimeConfiguration` (7 méthodes de la façade) et par 6 `XxxRuntimeDAO` NLP, chacun avec un adaptateur vers l'objet complet. Les tests existants passent sans modification.

### Étape 7 : runtime serverless

- **Runtime** (`tock-serverless-runtime`) : `TrackingExecutor` et `TrackingExecutionScope` (travail suivi par un `ThreadLocal`, propagé aux coroutines), `RequestWork.awaitIdle` (la requête attend tout ce travail), `ServerlessBotServer`, `ServerlessHttpServer` (serveur HTTP du JDK), `ServerlessBotLauncher`, `POST /_tasks/{name}` (seulement si `tock_serverless_tasks_token` est défini).
- **Ack et file d'attente** : les webhooks des chemins configurés sont mis en file puis acquittés par une 200 ; le consommateur les rejoue sur les mêmes routes, en attendant tout le travail. Messages signés (HMAC-SHA256, `tock_serverless_queue_secret`). Déduplication par empreinte du chemin et du corps. Consommateurs sans SDK : SQS (files standard et FIFO, échecs partiels de lot) et push Pub/Sub.
- **Modèle de persistance** (`tock-bot-storage-model`) : les documents `*Col` et `BotDataRegistry`, avec les mêmes (dé)sérialiseurs générés (`MongoFormatTest`). Seul le DSL de requête généré référence le pilote, en `provided`. `jasypt` devient une dépendance du modèle.
- **DAO DynamoDB du bot** (`tock-bot-storage-dynamodb`) : timelines, configurations d'application et de bot, stories, features (cache de 30 s), i18n, payloads WhatsApp, configurations RAG, magasin de vecteurs, observabilité, compresseur, règles métier, indicateurs, métriques. Format : JSON Jackson 3 compressé, mêmes (dé)sérialiseurs que Mongo.
- **Verrou et déduplication partagés** : `LeasedUserLock` (cœur), `MongoUserLock` et `DynamoUserLock` dessus ; `DynamoDeduplicationStore` dans `tock-serverless-dynamodb`.
- **DAO DynamoDB du NLP** (`tock-nlp-front-storage-dynamodb`) : applications, intentions, types d'entités, dictionnaires, phrases (recherche exacte seule), journaux d'analyse ; `DynamoStore` et `DocumentCodec` partagés dans `tock-storage-dynamodb-core`.
- **Publication admin → DynamoDB** (`tock-bot-storage-dynamodb-publication`, `tock_dynamodb_publication_enabled`) : démarrée par `BotAdditionalModulesService`, aucun changement dans l'admin. Lit Mongo par les interfaces des DAO. N'écrit que ce qui diffère ; ne supprime que ce qu'elle a publié (manifeste `PUB`) ; l'admin gagne sur les clés uniques.
  - NLP : même mécanisme pour applications, intentions, types d'entités et dictionnaires. Phrases validées : cache d'empreintes en mémoire, passage incrémental toutes les 5 min sur `updateDate`, passage complet au démarrage, toutes les heures et sur changement d'application, d'intention ou de type d'entité. L'inbox de l'exécution n'écrase jamais une phrase publiée (`attribute_not_exists(fp)`).
- **Recopie Streams → Mongo** (`tock-bot-storage-dynamodb-replication`, `tock_dynamodb_replication_enabled`) : lecture des Streams par l'API toutes les 5 s, points de reprise dans Mongo. Dialogues recopiés tels quels ; configuration créée par l'exécution ajoutée seulement si l'admin ne l'a pas ; compteurs en différences. NLP : inbox (sauf phrase déjà validée dans Mongo), compteur d'inconnus, journaux d'analyse via `ParseRequestLogDAO`.
- **Test de bout en bout WhatsApp Cloud** : un vrai bot sur DynamoDB Local, classpath sans Vert.x, `tock-shared` ni `tock-bot-engine` (vérifié par le test) ; ne simule plus que le NLP.
- Contrôles positifs faits sur chaque garantie (attente du travail, déduplication, condition du verrou, suppression limitée au manifeste, remplacement sur clé unique, condition de l'inbox, recopie de l'inbox).

### Limites connues

- Au rejeu d'un enregistrement de Stream (perte d'un point de reprise), les compteurs (statistiques des libellés, phrases inconnues) et les journaux d'analyse sont comptés deux fois.
- Au premier démarrage de la recopie, seules les 24 h que garde un Stream sont recopiées.
- Non recopiés : expirations du TTL DynamoDB, suppressions de configuration, statistiques de flux de dialogue, identifiants client.

## Chiffres du spike DynamoDB

- 59 tests de contrat verts ; test différentiel Mongo/Dynamo : 2 400 requêtes sur 2 400 identiques.
- Tour de bot : 11 RCU + 16 WCU au tour 100, sans compter le NLP (lecture de la phrase validée et écriture de l'inbox, environ +1 RCU et +3 WCU).
- Recherche du Studio sur 50 000 phrases : environ 3 140 RCU et 4 s (Mongo : 3 à 60 ms). D'où la recherche gardée dans l'admin.
- Recopie : une table et un shard font environ 0,5 M requêtes `GetRecords` par mois ; 2,5 M sont gratuites.
- Bug Mongo trouvé : l'index unique sur `bot_configuration.path` n'accepte qu'une seule configuration dont le `path` est null.

## Autres branches

### `feat/s3-storage` (reporté sur `refactor/serverless-core-split`)

- `util/aws-tools` : `S3Bucket` et `S3Clients`, configurés par `tock_s3_endpoint`, `tock_s3_region` et `tock_s3_path_style`.
- `nlp/model/storage-s3` : activé par `tock_nlp_model_storage=s3` et `tock_nlp_model_s3_bucket`. Date de mise à jour conservée à la milliseconde dans une métadonnée ; changements détectés par un listing périodique, uniquement en mode `stream`.
- `bot/storage-s3` et l'interface `UploadedFilesStorage` dans bot/engine : activés par `tock_bot_files_storage=s3`. Téléchargements redirigés vers une URL présignée (URL publique optionnelle) ; un fichier absent de S3 est recopié depuis le cache Mongo à la lecture.
- 19 tests sur `adobe/s3mock:5.2.3` (les images MinIO ne sont plus disponibles).
- Corrigé au passage : `provideOrDefault { Impl }` déduisait le type `Impl` au lieu de l'interface.

### `feat/poll-change-detection`

- `tock_change_detection_mode` vaut `stream` (défaut) ou `poll` ; intervalle `tock_change_detection_interval_seconds` (10 par défaut).
- Écriture : un `CommandListener` sur les clients Mongo incrémente `tock_versions` pour les 17 collections marquées `trackChanges()`, seulement si `n > 0` ; actif aussi en mode `stream`, désactivable par `tock_mongo_track_changes=false`.
- Lecture : `MongoChangePoller`, vérifié à l'entrée de chaque requête (`WebVerticle`) et par une tâche de fond. En mode `poll`, `NlpModelRepository` vérifie `get*ModelLastUpdate` à l'usage, au plus une fois par intervalle.
- 5 tests sur Mongo réel et 5 tests du dépôt de modèles. Pas encore testé de bout en bout : le hook de `WebVerticle` et l'enregistrement dans `watch`.
- Utile à l'admin s'il tourne lui-même à 0 instance (Cloud Run), et modèle à reprendre pour le lot B.

## Reste à faire

Dans l'ordre proposé. Les lots A à D rendent un bot WhatsApp Cloud utilisable en serverless de bout en bout ; E et F l'élargissent.

### A. Merger le découpage sur master

PR successives depuis `origin/master`, chacune sans changement de comportement, `api-compat` vert :

1. `api-compat` et son job CI (9141cae7e, ea3e411ef, 57ecd54e2, 7ca180c87).
2. `tock-shared-core` (36b1c9ba5), puis la SPI HTTP et `ExecutionScope` (06697adca).
3. `tock-bot-engine-core` (dfd96cc49), puis les DAO d'exécution (6c8579546).
4. Les routes neutres des connecteurs (9281c48f3) et `tock-nlp-api-core` (074fb0a48).
5. `tock-bot-storage-model` (0c3553119) : seule extraction qui touche le stockage Mongo existant, couverte par `MongoFormatTest` ; à valider aussi par l'e2e tock-docker.
6. L'étape 7 (ajouts seuls) : une PR, ou plusieurs selon la taille acceptable en revue.

Avant d'ouvrir : lancer les contrôles IntelliJ sur les fichiers modifiés (le worktree n'était pas ouvert dans l'IDE) et l'e2e tock-docker.

### B. Détection des publications par l'exécution (fait)

Les DAO DynamoDB n'ont pas de change stream : sans ce lot, une instance chaude ne voyait une publication de l'admin qu'à son démarrage à froid suivant.

- `DynamoChanges` (`tock-storage-dynamodb-core`) : la publication incrémente un compteur par canal (`ADD`) dans l'item `VERSION` de la table de configuration, pour les sortes d'items qu'elle a écrites ou supprimées. Canaux : configurations d'application et de bot, stories, libellés, réglages du bot (un seul canal), applications, intentions, types d'entités et dictionnaires NLP. Les features (cache de 30 s), les indicateurs et les phrases (lus à chaque usage) n'en ont pas besoin.
- Le runtime lit cet item au plus toutes les 10 s (`tock_dynamodb_change_check_interval_ms`), à l'entrée de chaque requête, et appelle les listeners enregistrés par les `listen…` des DAO pour les canaux dont le compteur a changé. Coût : une lecture d'item par intervalle et par instance active. Une tâche périodique nommée (`dynamodb_change_check`) fait la même vérification dans un process non gelé, ou depuis un ordonnanceur externe.
- Registre neutre `ChangeChecks` (`tock-shared-core`) : le runtime serverless appelle `prepareAll()` avant de charger les bots (lecture des compteurs de départ, pour ne pas rater une publication faite pendant le chargement) et `runAll()` à l'entrée de chaque requête. `botDynamoModule` et `nlpDynamoModule` y enregistrent DynamoDB, sans lecture. Le mode `poll` de Mongo (`MongoChangePoller`) pourra s'y enregistrer aussi.
- Libellés : la publication ne dit pas quels libellés ont changé. `I18nDynamoDAO` garde les identifiants renvoyés par `getLabels`, seuls libellés que le cache de `Translator` contient, et appelle le listener pour chacun.
- Trouvé au passage : le lanceur serverless n'appelait ni `initEncryptor()` (qui charge aussi les obfuscateurs) ni `initTranslator()`, contrairement à `BotVerticle`. Corrigé.
- Tests : `DynamoChangesTest` (4), un test de publication (seul le canal modifié est notifié, une seule fois), et un test de bout en bout WhatsApp : un libellé en cache, republié avec un nouveau texte, apparaît dans la réponse suivante. Contrôles positifs faits : sans l'écriture du compteur, 3 tests unitaires sur 4 et le test de bout en bout échouent.

### C. Modèles NLP et fichiers hors de Mongo (fait)

- Le commit de `feat/s3-storage` est reporté sur cette branche : `UploadedFilesStorage` est dans `tock-bot-engine-core`.
- `tock-bot-storage-s3` dépend de `tock-bot-engine-core`, et les deux modules S3 excluent `tock-shared` de `tock-aws-tools` (`S3Bucket` n'utilise que le cœur). Verrouillés par l'enforcer : ni Vert.x, ni Netty, ni Mongo, ni `tock-shared` à l'exécution.
- Modèles : le listing des modèles est une tâche nommée (`nlp_model_s3_poll`) et une vérification `ChangeChecks`, au plus une fois par `tock_nlp_model_s3_refresh_interval_seconds` : un runtime gelé entre ses requêtes voit les nouveaux modèles à la requête suivante.
- `DynamoTockCache` (`tock-storage-dynamodb-core`, lié par `botDynamoModule`) : le cache Tock du runtime (pièces jointes Messenger, fichiers uploadés par défaut), partition `CACHE#<type>` de la table d'exécution, non recopié. Un item DynamoDB fait au plus 400 Ko : en serverless, les fichiers uploadés passent par S3 (`tock_bot_files_storage=s3`).
- Le runtime serverless sert les fichiers uploadés (`/f/*`, `tock_bot_serve_files`) par une route neutre, `UploadedFilesService.routes()` : redirection présignée, contenu ou 404, comme la route Vert.x.
- Tests : les 19 tests S3, plus un test de la vérification à l'entrée des requêtes (contrôle positif fait), `DynamoTockCacheTest`, et un fichier uploadé puis téléchargé dans le test de bout en bout serverless.

### D. Runtime NLP serverless et déploiement

1. **Runtime NLP serverless (fait).**
   - Module `tock-serverless-nlp` : `ServerlessNlpLauncher` (`setup`, `install`, `start`) compose le cœur NLP, le service de modèles et le parseur sur les DAO d'exécution, sans `frontModule` ni `ApplicationConfigurationService` (qui délègue à des DAO complets dès son initialisation). `install()` peut aussi servir dans un process qui fait tourner des bots.
   - `NlpApiRoutes` passe sur `ApplicationRuntimeConfiguration` : `intents` et `application` y sont ; `application/create` et `logs/count` deviennent optionnels et répondent 501 sans création d'application ni moniteur (les deux restent dans l'admin). La signature sur l'`ApplicationConfiguration` complète est gardée.
   - Configuration des moteurs : le DAO S3 des modèles stocke maintenant la configuration NLP de chaque modèle à côté de lui (`configuration/intent/<id>`), écrite par l'admin au moment du build ; le runtime n'a pas de DAO de configuration (lecture : aucune, écriture : refusée). Les modèles plus anciens retombent sur le DAO de configuration.
   - Corrigé (reste de l'étape 6) : `ConfigurationRepository` calculait les intentions partagées par `ApplicationConfigurationService`. Il passe par `NamespaceConfigurationRuntimeDAO.getNamespaceConfiguration` et `IntentDefinitionRuntimeDAO.getIntentsByNamespace`, ajoutées aux interfaces d'exécution. Les configurations de namespace sont publiées dans DynamoDB (partition `NLPNS`, canal `nlp_namespace`).
   - `initializeRuntimeConfiguration()` (`tock-nlp-front-service`) : le chargement du cache du parseur sans les DAO de l'admin.
   - Duckling : `JSONValue` et le client passent de `JsonArray`/`JsonObject` de Vert.x à Jackson (mêmes types décodés, test ajouté) ; la dépendance `vertx-core` du POM est gardée, pour ne pas changer le classpath.
   - jasypt devient une dépendance du runtime NLP (`initEncryptor`, comme `NlpVerticle`).
   - Test de bout en bout (DynamoDB Local, classpath sans Vert.x, Mongo ni `tock-shared`) : parse d'une phrase validée, types d'entités de Duckling enregistrés, et application supprimée par l'admin inconnue à la requête suivante (contrôle positif fait : sans notification, le cache la sert encore).
   - Limites : sans modèle publié dans S3, seules les phrases validées sont reconnues ; le NLP et les bots dans un même process s'appellent encore par HTTP.
2. **Démarreur `tock-serverless-bot` (fait).**
   - Dépendance de type `pom` (pas un parent : `tock-bom` signe les artefacts avec gpg dès `verify`, un projet qui en hériterait signerait ses jars) : runtime, déduplication et stockage DynamoDB, fichiers sur S3, et les connecteurs migrés (WhatsApp Cloud, Messenger) sans `tock-bot-engine`, `tock-shared`, Vert.x ni Mongo. Les files (`tock-serverless-sqs`, `tock-serverless-pubsub`) restent à ajouter par le projet.
   - Son build vérifie son arbre par l'enforcer. Les nouveaux artefacts (stockages DynamoDB, publication, recopie, runtime, files, NLP, démarreur) sont ajoutés à `tock-bom`.
   - `scripts/serverless-deps` (et le job CI `serverless-deps`) : `jdeps` sur les jars Tock d'un démarreur, pris dans les `target` du réacteur, contre son classpath ; seules les références de la liste `ALLOWED` sont admises (méthodes Vert.x gardées pour la compatibilité, DSL de requête KMongo, DAO Mongo remplacés, `frontModule`). Il passe sur `serverless-bot` et `serverless-nlp`.
   - Trouvé par ce contrôle : `DialogCol` appelait `transformData` de `tock-shared` (`MongosKt`) pour les réponses RAG et les actions de debug, qui auraient levé `NoClassDefFoundError` dans le runtime serverless. La règle passe dans `tock-shared-core` (`transformDocumentData`) ; `transformData` y délègue. `StorageModelBytecodeTest` interdit désormais toute classe propre à `tock-shared` (contrôle positif fait).
   - Piège : les worktrees partagent `~/.m2` à la même version ; un autre worktree y avait installé un Messenger d'avant la migration. Le script ne lit donc pas `~/.m2` pour les jars Tock.
3. **Noms des tâches périodiques (fait).**
   - Les appels existants de `setPeriodic` ne sont pas renommés : un mock strict d'`Executor` dans un projet n'intercepterait plus la variante nommée.
   - À la place, `TrackingExecutor` nomme une tâche anonyme d'après la classe et le membre qui l'enregistrent, avec `_2`, `_3`... pour les suivantes : `translator_statscache` (vidage des statistiques i18n), `messengerconnector_registercheckwebhook` (abonnement du webhook Messenger). Le nom ne change pas d'un démarrage à l'autre, et un ordonnanceur externe le déclenche par `POST /_tasks/<nom>`. Les lambdas compilés par K2 (`<propriété>_delegate$lambda$0`) sont gérés (test).
   - Seules ces deux tâches sont sur le chemin d'exécution ; les workers de build, le nettoyage des modèles, le crawler de TTL des phrases, les datasets et la base de connaissances tournent dans l'admin ou les workers, qui restent sur Vert.x.
   - Limite : les statistiques i18n accumulées en mémoire sont perdues si l'instance est arrêtée avant le passage suivant de `translator_statscache`.
4. Images tock-docker : ajouter les modules de publication et de recopie (le stockage NLP Dynamo suit automatiquement) et les modules S3 à l'image de l'admin ; créer l'image du runtime serverless.
5. Adaptateur Lambda natif, ou Lambda Web Adapter devant `ServerlessHttpServer`, avec SnapStart.
6. Essai réel sur Cloud Run (facturation par instance, minimum 0) et sur Lambda, puis relancer `SpikeBenchmark` contre un vrai AWS.

### E. Autres connecteurs

Migrer vers `registerRoutes` : alcmeon, slack, whatsapp, teams, web (et SSE), open-ai, google-chat, iadvize, mattermost et rest. Websocket bot_api : n'autoriser que le mode webhook en serverless.

### F. Profil « free tier » et documentation

- TTL réduites : `tock_nlp_classified_sentences_index_ttl_days` pour l'inbox, statistiques et flux à 30–90 jours.
- `maxPoolSize` réduit, et idéalement un seul `MongoClient` par processus (limite de connexions de M0).
- Documenter toutes les nouvelles propriétés (`tock_dynamodb_*`, `tock_serverless_*`, `tock_s3_*`, `tock_change_detection_*`) et l'architecture admin / exécution.

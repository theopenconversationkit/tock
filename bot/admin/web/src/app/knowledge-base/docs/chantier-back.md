# Chantier back — cartographie des fichiers

Inventaire des ajouts et modifications côté serveur pour rendre la base de
connaissances opérationnelle, réparti sur quatre couches : modèles et persistance,
orchestrateur Python, client Retrofit Kotlin, serveur admin. Convention : **[N]**
nouveau fichier, **[M]** modification d'un fichier existant.

Chemins relatifs à la racine du repo `tock`.

Rappel de principe : **chemin runtime intact**. Les lignes produites par la base de
connaissances sont des lignes ordinaires de l'index, portant les mêmes métadonnées que
celles d'une ingestion documentaire. `rag_chain_builder.py`, les retrievers, le RRF et la
configuration RAG ne sont pas touchés. Un bot dont la base est vide se comporte exactement
comme aujourd'hui.

Le point singulier de ce chantier n'est pas la lecture, c'est l'**écriture** : l'orchestrateur
n'expose aujourd'hui aucun chemin d'écriture vers la base vectorielle, l'indexation vivant
dans un script CLI hors serveur. C'est le seul endroit où l'on ouvre quelque chose de neuf,
et il est traité par un routeur dédié qui réutilise les factories existantes.

---

## Couche 1 — Modèles et persistance (Kotlin)

Base modèles : `bot/engine/src/main/kotlin/admin/`
Base DAO : `bot/storage-mongo/src/main/kotlin/`

Patron de référence : `admin/dataset/Dataset.kt`, `admin/dataset/DatasetDAO.kt` et
`DatasetMongoDAO.kt`. La feature suit exactement le même découpage.

### Modèles de domaine

**[N]** `admin/knowledgebase/KnowledgeBaseEntry.kt`
Data class de l'entrée : `namespace`, `botIds` (liste et non scalaire, pour ne pas fermer
une mutualisation ultérieure), `title`, `searchHints`, `content`, `sourceUrl`, `tags`,
`status`, `contentHash`, traçabilité. Plus les enums `KnowledgeBaseEntryStatus`
(`DRAFT` / `PUBLISHED`) et `KnowledgeBaseProjectionState`
(`INDEXED` / `PENDING` / `ORPHAN` / `NONE`).

L'état de projection n'est **pas** stocké sur l'entrée : il se déduit du journal (ci-dessous)
et de l'`indexSessionId` courant du bot. Le stocker créerait une seconde source de vérité à
tenir synchronisée.

**[N]** `admin/knowledgebase/KnowledgeBaseProjection.kt`
Journal de projection : un document par `(indexSessionId, entryId)`, portant le
`contentHash` projeté, la date, le nombre de lignes écrites et leurs identifiants. C'est lui
qui permet la détection d'écart de niveau 1, sans aucun accès à la base vectorielle.

**[N]** `admin/knowledgebase/KnowledgeBaseJob.kt`
Data class de la tâche : `type` (`SAVE_ENTRY`, `DELETE_ENTRY`, `PUBLISH`, `UNPUBLISH`,
`REPAIR_INDEX`, `CREATE_INDEX`), `state` (`QUEUED`, `RUNNING`, `COMPLETED`, `FAILED`),
`progress`, `failures`, `projected`, `removed`, dates. Calquée sur `DatasetRun`.

> **Réalisé — DERCBOT-2119 : conserver la demande jusqu'à son application.**
> Les identifiants d'entrée sont des UUID sous forme de chaînes, comme les IDs du contrat front.
> Une entrée conserve une `revision`, un `pendingJobId` et, après suppression, un marqueur `deleted`.
> `everPublished` évite un accès vectoriel pour les brouillons jamais publiés.
> Cela permet de reprendre une écriture Mongo interrompue avant la création de sa tâche,
> et de retirer le vecteur après une panne sans perdre son identifiant. L'acquittement compare
> la révision : une ancienne tâche ne peut pas effacer une modification plus récente.
> Ces champs internes ne sont ni éditables ni exportés. Les entrées supprimées sont masquées.
>
> **Réalisé — DERCBOT-2119 : journal limité à une cible précise.**
> Une session seule n'identifie pas un stockage. La clé du journal inclut namespace, bot,
> index physique et configuration de connexion expurgée des secrets, puis l'entrée.
> Une quatrième collection, `knowledge_base_index`, conserve la provenance et l'empreinte
> des paramètres d'embedding. Sans elle, le mode autonome et un changement de modèle ne
> pourraient pas être reconnus après redémarrage. Un changement de modèle connu bloque
> la projection ; l'origine d'un index externe reste inconnue et signalée au métier.
> Une modification des paramètres par défaut de déploiement, absents de la configuration
> du bot, nécessite une vérification explicite de l'index.

### Interfaces DAO

**[N]** `admin/knowledgebase/KnowledgeBaseDAO.kt`
Trois interfaces, sur le modèle de `DatasetDAO` / `DatasetRunDAO` :
`KnowledgeBaseEntryDAO` (CRUD, recherche paginée, tags distincts),
`KnowledgeBaseProjectionDAO` (lecture et écriture du journal),
`KnowledgeBaseJobDAO` (création, lecture, prise de la prochaine tâche en file).

### Implémentation Mongo

**[N]** `bot/storage-mongo/src/main/kotlin/KnowledgeBaseMongoDAO.kt`
Patron `DatasetMongoDAO`. Trois collections via
`MongoBotConfiguration.database.getCollection<T>(…)` :
`knowledge_base_entry`, `knowledge_base_projection`, `knowledge_base_job`.

Index à créer : `(namespace, botIds)` pour la recherche, `(namespace, botIds, title)` pour
la détection de doublons à l'import, `(indexSessionId, entryId)` unique sur le journal,
`(state, createdAt)` sur les tâches pour la prise en file.

**[M]** `bot/storage-mongo/src/main/kotlin/MongoBotConfiguration.kt` ou le module d'injection
équivalent — déclaration des trois DAO, à côté des existants.

---

> **Réalisé — DERCBOT-2119 : un DAO pour les quatre collections.**
> Les opérations étroitement liées sont regroupées dans `KnowledgeBaseDAO`, injecté dans
> `storage-mongo/Ioc.kt`. Les recherches Mongo sont toujours limitées au bot et au namespace.
> Le filtrage, les tags, le rapprochement des titres normalisés et la pagination sont calculés
> en mémoire sur ces quelques centaines d'entrées : cela permet aussi de filtrer l'état dérivé
> sans le stocker en double ni ajouter une agrégation complexe. L'index sur le titre brut
> proposé ne résoudrait pas la recherche normalisée et n'est pas créé.
> L'unicité du journal repose sur son `_id` déterministe ; les tâches sont indexées par état
> et `startedAt`, ainsi que par bot, et les demandes non acquittées par `pendingJobId`.

## Couche 2 — Orchestrateur (Python / FastAPI)

Base : `gen-ai/orchestrator-server/src/main/python/server/src/gen_ai_orchestrator`

C'est ici que se situe la nouveauté réelle du chantier : **le premier chemin d'écriture de
l'orchestrateur vers la base vectorielle**. Les routeurs existants sont tous en lecture ou en
interrogation, et la seule indexation du dépôt est un script CLI,
`tock-llm-indexing-tools/scripts/indexing/vectorisation/run_vectorisation.py`.

La plomberie, elle, existe déjà : les factories exposent `get_vector_store()` et le script
utilise `add_documents()`. Le travail est de l'exposition, pas de l'implémentation.

### Routeur

**[N]** `routers/knowledge_base_router.py`
Patron `vector_store_providers_router.py`. Deux endpoints :

```
POST /knowledge-base/index     embedding + upsert d'un lot de lignes
POST /knowledge-base/delete    suppression des lignes d'un lot d'entrées
```

Aucune logique métier : délègue au service. Gestion d'erreurs par les handlers FastAPI
existants.

**[M]** `main.py`
Une ligne : `app.include_router(knowledge_base_router)`, à la suite des `include_router`
existants.

### Modèles de requête / réponse

**[M]** `routers/requests/requests.py`
`KnowledgeBaseIndexRequest` (vector store setting, `em_setting`, `index_session_id`, liste de
lignes à écrire), `KnowledgeBaseDeleteRequest` (vector store setting, `index_session_id`,
identifiants). Les settings sont remplis par le serveur admin, jamais par le studio.

**[M]** `routers/responses/responses.py`
`KnowledgeBaseIndexResponse` et `KnowledgeBaseDeleteResponse` : nombre de lignes écrites ou
supprimées, et liste des échecs par entrée. Un échec unitaire ne fait pas échouer l'appel.

> **Réalisé — DERCBOT-2119 : vérification réelle et contrats isolés.**
> Un troisième endpoint interne, `POST /knowledge-base/rows`, lit uniquement les lignes KB
> de l'index cible. Il est indispensable pour retrouver une ligne écrite avant un crash,
> une suppression externe ou une orpheline absente du journal. Il évite d'étendre tout
> l'explorateur à un filtre arbitraire. La lecture réussit entièrement avant de modifier le journal.
> Les DTO Python sont dans `knowledge_base_requests.py` et `knowledge_base_responses.py`.
> Les écritures renvoient un résultat par entrée (IDs physiques, nombre, erreur éventuelle),
> utilisé pour acquitter uniquement les opérations réussies. Les erreurs retournées au studio
> sont des clés traduites, jamais les exceptions du fournisseur contenant potentiellement des secrets.

### Service d'indexation

**[N]** `services/knowledge_base/__init__.py`
**[N]** `services/knowledge_base/knowledge_base_service.py`

Quatre responsabilités :

1. **Construire les documents.** Le contenu embeddé suit la forme normée de l'existant :
   titre, ligne vide, puis le texte dans une fence markdown, comme le fait
   `run_vectorisation.py`. Sans cela les lignes KB s'afficheraient différemment des chunks
   documentaires dans les footnotes et dans l'outil d'inspection.

2. **Poser les métadonnées.** `index_session_id`, `index_datetime`, `id`, `chunk` (`"1/1"`),
   `title`, `source`, plus `source_type = "internal_kb"` et `kb_entry_id`.
   **Trois de ces clés sont obligatoires, pas recommandées** : `build_footnotes()` lit
   `metadata['id']`, `metadata['title']` et `metadata['source']` en accès direct, et
   `get_source_content()` fait de même sur `title`. Une clé manquante lèverait une `KeyError`
   en production, au moment précis où l'entrée est utilisée dans une réponse. `source` peut
   valoir `null`, mais la clé doit exister.

3. **Embedder avec le modèle du bot.** L'`EMSetting` transmis par le serveur admin est
   utilisé tel quel. Une entrée embeddée avec un autre modèle est silencieusement
   inexploitable, sans aucun signal : c'est le risque majeur du chantier.

4. **Écrire et supprimer.** `add_documents(ids=…)` avec des identifiants de ligne
   déterministes dérivés de l'identifiant d'entrée, ce qui fait de la mise à jour un upsert
   et de la suppression une opération par identifiant, sans filtre sur métadonnée.
   **À vérifier** sur les versions de `langchain_postgres` et du client OpenSearch utilisées ;
   si le comportement ne se confirme pas, repli sur un filtre métadonnée `kb_entry_id`, avec
   deux implémentations selon le provider.

> **Réalisé — DERCBOT-2119 : upsert isolé entre sessions.**
> Dans `langchain-postgres 0.0.17`, l'ID physique est unique dans toute la table, pas seulement
> dans une collection. Il vaut donc `kb-` suivi du SHA-256 de `indexName/entryId`.
> L'ID documentaire reste celui de l'entrée, donc le chunk reste `{entryId}:1/1`.
> La suppression PGVector utilise `collection_only=True` ; pour les deux fournisseurs,
> on vérifie que chaque ligne appartient bien à cette entrée et porte `source_type=internal_kb`.
> La réparation remplace aussi les doublons ou les lignes sous un ID physique inattendu.
> Le hash comprend le texte réellement projeté et l'URL : modifier une référence doit actualiser
> la citation. La vérification recalcule le hash depuis les données réelles, sans faire confiance
> au `kb_content_hash` de métadonnée. Une source absente vaut `null` : la règle d'anomalie
> existante le tolère déjà, aucun changement de cette règle n'est nécessaire.

### Création d'index — mode autonome

**[M]** `services/langchain/factories/vector_stores/opensearch_factory.py` ou service dédié
PGVector crée l'index implicitement à l'écriture ; OpenSearch exige un mapping `knn_vector`
explicite. Le mode autonome doit donc créer l'index côté OpenSearch avant la première
écriture. À placer dans le service d'indexation plutôt que dans la factory si l'on veut
éviter de toucher un fichier partagé.

Le nom physique de l'index n'est jamais construit par le studio :
`PGVectorUtils.normalizeDocumentIndexName` passe en minuscules puis remplace tout caractère
hors `[a-z0-9_]` par `_`, là où `OpenSearchUtils` conserve les tirets. Il est résolu côté
serveur.

---

> **Réalisé — DERCBOT-2119 : création déjà disponible dans le client OpenSearch.**
> `langchain-community 0.4.2` crée le mapping `knn_vector` lors du premier ajout non vide.
> Aucun nouveau mapping ni changement de factory n'est nécessaire. La KB exige au moins
> une entrée publiée et une configuration RAG enregistrée (éventuellement désactivée), avec
> modèles, prompts et accès au stockage renseignés. Elle ne peut pas inventer ces réglages.
> Sans configuration de stockage propre au bot, les fournisseurs par défaut de l’admin
> (`tock_gen_ai_orchestrator_vector_store_provider`) et de l’orchestrateur doivent être alignés,
> comme pour le RAG existant.
> Le worker écrit d'abord l'index, puis valide et active la session. L'activation réutilise
> les effets du service RAG sur la story inconnue ; une écriture conditionnelle Mongo évite
> d'écraser un réglage RAG modifié entre-temps. Cela modifie `RAGService` et le DAO de configuration,
> mais ni son modèle ni le chemin de recherche/génération. Le schéma PostgreSQL fourni par
> le projet (`sql/schema.sql`, dont la recherche plein texte) reste un prérequis du déploiement.

## Couche 3 — Client orchestrateur (Kotlin / Retrofit)

Base : `gen-ai/orchestrator-client/src/main/kotlin/ai/tock/genai/orchestratorclient`

### Interface Retrofit

**[N]** `api/KnowledgeBaseApi.kt`
Patron `VectorStoreProviderApi.kt`. Deux méthodes, `index` et `delete`, annotations `@POST`
et `@Body`.

### Service et implémentation

**[N]** `services/KnowledgeBaseIndexingService.kt`
**[N]** `services/impl/KnowledgeBaseIndexingServiceImpl.kt`
Patron `VectorStoreProviderService` / `…Impl`. Instancie l'API via le
`GenAIOrchestratorClient` existant et relaie.

### Modèles

**[N]** `requests/KnowledgeBaseRequests.kt`
**[N]** `responses/KnowledgeBaseResponses.kt`
Data classes en `camelCase` — le mapper Jackson existant applique `SNAKE_CASE` + `NON_NULL`,
donc rien à annoter. Volume modeste comparé au chantier d'inspection : deux requêtes, deux
réponses.

---

> **Réalisé — DERCBOT-2119 : client Retrofit.**
> Les fichiers prévus sont conservés, avec une troisième méthode `rows` et ses DTO pour la
> vérification réelle. Le service est déclaré dans `bot/engine/.../engine/Ioc.kt`.
> Chaque réponse HTTP doit réussir et avoir un corps avant de pouvoir acquitter une projection.

## Couche 4 — Serveur admin (Kotlin)

Base : `bot/admin/server/src/main/kotlin`

### Verticle et routes

**[N]** `verticle/KnowledgeBaseVerticle.kt`
Patron `DatasetsVerticle.kt` : constantes de chemin en `companion object`, configuration via
`blockingJsonGet` / `blockingJsonPost` et `checkNamespaceAndExecute`.

```
GET    /bots/:botId/knowledge-base/entries
GET    /bots/:botId/knowledge-base/entries/:entryId
POST   /bots/:botId/knowledge-base/entries
PUT    /bots/:botId/knowledge-base/entries/:entryId
DELETE /bots/:botId/knowledge-base/entries/:entryId
GET    /bots/:botId/knowledge-base/tags
GET    /bots/:botId/knowledge-base/sync
POST   /bots/:botId/knowledge-base/sync
POST   /bots/:botId/knowledge-base/index
POST   /bots/:botId/knowledge-base/bulk-status
POST   /bots/:botId/knowledge-base/import
POST   /bots/:botId/knowledge-base/import/preview
GET    /bots/:botId/knowledge-base/export
GET    /bots/:botId/knowledge-base/jobs/:jobId
GET    /bots/:botId/knowledge-base/jobs/active
```

**[M]** `BotAdminVerticle.kt`
Deux lignes, sur le modèle de `datasetsVerticle` : instanciation (ligne ~106) et
`knowledgeBaseVerticle.configure(this)` (ligne ~171).

**Rôles** : les écritures sont ouvertes à `botUser`, ce qui est la condition de la promesse
de réactivité pour le métier. C'est un point d'arbitrage explicite de la proposition (§8.2) ;
s'il bascule, la restriction porte sur la publication, pas sur l'accès.

### Service métier

**[N]** `service/KnowledgeBaseService.kt`
CRUD, recherche paginée, tags, import et export, calcul de l'état de synchronisation.

Le calcul d'écart a deux niveaux, et c'est le cœur du service :

- **niveau 1, systématique et gratuit** — comparaison du journal avec l'`indexSessionId`
  configuré sur le bot, le `contentHash` de l'entrée et son statut. Mongo seul. Couvre le
  cas dominant, le changement de session. Nuance à respecter : lors d'un changement de
  session, les anciennes lignes vivent dans **un autre index**, qui n'est plus interrogé.
  Elles ne sont donc pas orphelines mais hors de portée. Le bilan est « toutes les entrées
  publiées en attente, zéro orpheline ».
- **niveau 2, ponctuel** — confrontation à la réalité de l'index, seule capable de détecter
  une projection échouée ou une modification externe. Non déclenché au chargement de la liste.

Résolution des settings, comme pour les routes gen-AI existantes : `VectorStoreService` pour
la configuration du stock, `RAGService` pour l'`EMSetting` et l'`indexSessionId`. Réutilisés
tels quels.

> **Réalisé — DERCBOT-2119 : afficher les écarts jusqu'à leur résolution.**
> Le `GET /sync` reste une lecture Mongo, sans appel vectoriel. Le nom physique est calculé
> par les utilitaires Kotlin existants ; appeler `/indexes` ici casserait cette garantie.
> Une dépublication ou suppression asynchrone peut échouer : tant que la ligne existe,
> elle est `ORPHAN`, puis devient `NONE` après retrait réussi. Les erreurs restent visibles
> sur l'entrée (`projectionError`) et dans le bilan (`counts.failed`), même après rechargement
> et même pour une entrée supprimée. `POST /verify` lance une tâche `VERIFY_INDEX` en lecture
> pour détecter les modifications externes ; `POST /sync` vérifie puis répare.
> Lors d'un changement de session, les projections de l'ancienne cible ne sont pas comptées.

### File de tâches

**[N]** `service/KnowledgeBaseJobWorker.kt`
Patron **`DatasetRunWorker.kt`**, qui est le précédent exact : `executor.setPeriodic` avec un
intervalle configurable, garde `AtomicBoolean` sur le créneau de traitement, boucle de drain
tant qu'il reste une tâche en file.

C'est ce worker qui appelle l'orchestrateur, met à jour le journal de projection et fait
progresser la tâche. Deux invariants à tenir :

- **toute écriture vers l'index passe par la file**, y compris l'enregistrement d'une entrée
  seule. C'est ce qui interdit à deux écrivains de se concurrencer sur le même index, et
  c'est la raison pour laquelle l'endpoint d'enregistrement ne projette pas lui-même ;
- **un échec partiel ne fait pas échouer la tâche** : les entrées concernées restent en
  attente et alimentent `failures`. L'état `FAILED` est réservé à la tâche qui n'a pas pu
  s'exécuter du tout.

Seule la projection est mise en file. L'écriture Mongo de l'entrée reste synchrone, ce qui
permet à l'endpoint de renvoyer l'entrée immédiatement, accompagnée de la tâche qui la
projettera.

> **Réalisé — DERCBOT-2119 : sérialisation distribuée et reprise.**
> L'`AtomicBoolean` de `DatasetRunWorker` ne protège qu'une JVM. Le worker KB utilise en plus
> le bail Mongo renouvelable déjà fourni par `UserLock`, partagé entre serveurs admin.
> Il vérifie en lecture seule la présence d'une tâche `QUEUED`/`RUNNING` ou d'une entrée
> dont la tâche manque, puis acquiert le verrou seulement si du travail est détecté.
> Le travail est relu sous verrou avant traitement. Cette vérification évite les écritures
> et les logs de verrouillage à vide ; les échecs déjà rapportés ne réveillent pas le worker.
> Une seule file traite les projections KB ; les modifications Mongo utilisent un verrou court
> distinct par bot et restent possibles pendant le traitement. Aucun broker n'est ajouté.
> Les tâches `RUNNING` sont reprises au démarrage et les tâches manquantes reconstruites depuis
> les entrées non acquittées. Les écritures vectorielles sont idempotentes.
> La tâche relit la dernière révision de chaque entrée avant projection : la dernière édition
> l'emporte, sans verrouiller le formulaire pendant un lot. Un changement de cible pendant
> le traitement arrête les écritures suivantes ; la réparation concerne la nouvelle cible.
> La fréquence de prise en file vaut une seconde par défaut ; la durée réelle dépend du fournisseur.
> Même les tâches sans travail vectoriel passent par la file, au lieu de maintenir un second
> chemin synchrone. Un échec partiel reste `COMPLETED` avec `failures`, jamais un faux succès visuel.

### Import et export

**[N]** `service/KnowledgeBaseImportService.kt`
Lecture des deux formats acceptés, et d'eux seuls : l'enveloppe d'export KB, reconnue à son
champ `format`, et le tableau nu d'un export FAQ, reconnu à ses `utterances` et son
`answer.i18n`. Un fichier qui ne correspond à aucun des deux est refusé, jamais interprété au
mieux.

Correspondance FAQ vers KB : première utterance en `title`, les suivantes en `searchHints`,
`answer.i18n[locale]` en `content`, `tags` repris, `footnotes[0].url` en `sourceUrl` si c'est
une URL `http(s)`. `description`, `enabled`, `intentName` et les identifiants ne sont pas
repris. Les entrées importées sont créées **en brouillon**, sans exception.

Le module FAQ n'est ni lu ni modifié par ce service : il ne consomme qu'un fichier déposé par
l'utilisateur. C'est ce qui garantit qu'on n'empiète pas sur le fonctionnement legacy.

> **Réalisé — DERCBOT-2119 : parsing front, validation back et retrait après import.**
> Le front sait déjà lire le fichier, choisir la locale et produire les candidats : ce travail
> est conservé. Le serveur valide les valeurs, impose `DRAFT` et recalcule les doublons au moment
> de l'import ; il ne fait pas confiance aux IDs ni à l'état envoyés par la prévisualisation.
> Les doublons internes au fichier sont traités également. Le rapprochement secondaire conserve
> le `sourceId` d'origine sans reprendre les IDs propres à l'environnement de destination.
> Un import avec `UPDATE` peut remplacer une entrée publiée : ne lancer aucune tâche laisserait
> son ancienne réponse dans l'index. Le résultat comporte donc un `job` de retrait pour les entrées
> importées (sans effet vectoriel pour les nouvelles). La publication groupée suivante reste explicite.
> Le parsing refuse les versions KB inconnues, tolère les lignes mal formées pour les signaler,
> et préfère le libellé FAQ texte générique à celui d'un connecteur ou d'une interface vocale.

### DTO

**[N]** éventuellement `model/genai/KnowledgeBase*.kt`
Si les corps de requête/réponse admin diffèrent de ceux du client, typiquement le corps
allégé que le studio envoie avant injection des settings. À arbitrer : on peut souvent
réutiliser les data classes du client.

---

> **Réalisé — DERCBOT-2119 : DTO admin nécessaires.**
> Les DTO studio sont regroupés dans `model/knowledgebase/KnowledgeBaseModels.kt` : ils ne
> contiennent ni clés ni settings. Ils sont distincts des DTO orchestrateur en snake_case.
> Les écritures sont ouvertes à `botUser`, `admin` et `technicalAdmin`, avec contrôle du bot
> dans le namespace authentifié. La suppression utilise `POST /entries/:entryId/delete`
> pour conserver la réponse de tâche avec le `RestService` existant ; HTTP DELETE autorise
> bien un corps de réponse, contrairement à la justification initiale.
>
> **Réalisé — DERCBOT-2119 : nettoyage à la suppression du bot.**
> `BotAdminService` purge les quatre collections KB avant les autres configurations. L'acquisition du verrou doit réussir avant ce nettoyage ;
> elle évite qu'une tâche en cours ne recrée des documents après la purge.
> Le cycle de vie des index vectoriels externes reste celui du mécanisme existant de suppression du bot.

## Synthèse par effort

| Zone                            | Fichiers     | Nature                                 | Poids       |
| ------------------------------- | ------------ | -------------------------------------- | ----------- |
| Modèles de domaine + DAO Mongo  | 4 [N], 1 [M] | patron `Dataset*` répété               | faible      |
| Routeur + modèles orchestrateur | 1 [N], 3 [M] | patron existant, déclaratif            | faible      |
| Service d'indexation Python     | 2 [N]        | **écriture neuve** vers le stock       | moyen-élevé |
| Création d'index OpenSearch     | 1 [N/M]      | mapping `knn_vector`                   | moyen       |
| Client Retrofit                 | 5 [N]        | patron + volume déclaratif             | faible      |
| Verticle + routes admin         | 1 [N], 1 [M] | patron `DatasetsVerticle`              | moyen       |
| Service métier + calcul d'écart | 1 [N]        | logique neuve, conceptuellement simple | moyen       |
| Worker de file                  | 1 [N]        | patron `DatasetRunWorker`              | moyen       |
| Import / export                 | 1 [N]        | mapping et validation de format        | moyen       |

Le point dur est **le service d'indexation Python**, parce qu'il ouvre le premier chemin
d'écriture de l'orchestrateur et qu'il porte deux risques silencieux : un modèle d'embedding
divergent en mode mixte, et une métadonnée manquante qui ne se manifeste qu'en production.
Tout le reste est du patron répété ou de la logique simple.

Deux points restent à vérifier avant chiffrage ferme : le comportement d'upsert de
`add_documents(ids=…)` sur les versions utilisées, qui conditionne la simplicité de la
suppression, et la création d'index OpenSearch, qui n'a pas d'équivalent PGVector.

---

## Ce qui n'est PAS touché — garde-fous

- `services/langchain/rag_chain_builder.py` : intouché. Les lignes KB sont des lignes
  ordinaires, le runtime ne sait pas qu'elles existent. C'est la garantie de non-régression
  sur les bots en production.
- Les retrievers, le RRF, la configuration RAG Kotlin : intouchés.
- Le module FAQ, les stories, la NLU : intouchés. La base de connaissances est une entité
  nouvelle dans ses propres collections. L'import ne lit qu'un fichier, jamais la base FAQ.
- `tock-llm-indexing-tools` : intouché. La feature ne remplace pas les chaînes d'ingestion
  existantes, elle les complète, et ne se couple à aucune d'entre elles.
- Aucune dépendance à Qallam, ni dans le code ni dans le modèle.

Seule adhérence assumée, côté front : le test de remontée appelle la recherche de l'outil
d'inspection de la base vectorielle avec `pinnedChunkIds`. Aucun endpoint nouveau, mais la
feature suppose que ce chantier soit livré.


> **Réalisé — DERCBOT-2119 : test de remontée et diagnostic.**
> La recherche d'inspection de `master` ne fonctionnait que pour PGVector. Le service
> d'inspection reçoit un complément OpenSearch pour lister les index et rechercher par
> similarité avec épinglage. La recherche hybride, le plein texte et l'exploration détaillée
> des documents OpenSearch restent indisponibles, comme l'indiquent ses capacités.
> Le endpoint admin KB `/retrieval-test` réutilise la condensation et la recherche d'inspection
> avec les paramètres du bot et les replis du runtime. Il désactive la compression, actuellement
> absente du runtime, et retourne seulement les résultats réellement retenus. Le score peut
> être nul ; un rang absent reste « non remontée ». Aucune priorité KB n'est ajoutée au prompt :
> `source_type` n'est pas transmis au modèle dans le contexte actuel, une telle règle serait trompeuse.
> Le lien diagnostic transmet question, index physique et chunk à épingler. Le test intégré
> est accessible à `botUser` ; l'outil diagnostic conserve ses droits d'accès existants.

> **Réalisé — DERCBOT-2119 : raccordement et suivi front.**
> `KnowledgeBaseRestService` remplace le mock dans le module. Le polling attend la réponse
> précédente (`exhaustMap`) afin de ne pas annuler indéfiniment les requêtes lentes ; la liste
> retrouve les traitements lancés ailleurs et la fiche reprend sa tâche après rechargement.
> Les échecs partiels sont affichés et ne déclenchent pas de message de publication réussie.
> Les suggestions fondées sur les textes de l'entrée sont retirées du test pour éviter un
> résultat artificiellement favorable. L'authentification locale modifiée dans les commits
> front est remise à la valeur de `master`, sans lien avec cette feature.

## Contrat effectivement livré

Racine admin : `/bots/:botId/knowledge-base` ; le namespace vient de la session.
Les dates sont en ISO 8601 et les réponses en camelCase.

| Route | Résultat |
| --- | --- |
| `GET /entries` | `{rows,total,start,end}`, filtres/pagination du front conservés |
| `GET /entries/:entryId` | entrée et projection calculée |
| `POST /entries`, `PUT /entries/:entryId` | `{entry,job}`, entrée persistée immédiatement |
| `POST /entries/:entryId/delete` | tâche de suppression |
| `GET /tags`, `GET /sync` | tags et bilan Mongo |
| `POST /sync`, `POST /verify`, `POST /index` | tâche de réparation, vérification ou création |
| `POST /bulk-status` | `{entryIds,status}` → tâche |
| `GET /jobs/active`, `GET /jobs/:jobId` | tâche ; réponse vide si aucune tâche active |
| `POST /retrieval-test` | `{question,entryId?}` → rang et résultats retenus |
| `POST /import/preview` | `{rows}` → candidats validés et doublons |
| `POST /import` | `{candidates,duplicatePolicy}` → compteurs, `entryIds`, `job` éventuel |
| `GET /export` | enveloppe `tock-knowledge-base`, version 1 |

Le bilan expose également `canCreateIndex`, `embeddingMismatch` et `counts.failed`.
Les limites serveur sont : titre 300 caractères, contenu 12 000, 30 termes de 300,
30 tags de 100, 1 000 entrées par import/action groupée, 100 résultats par page.
Une URL de référence doit être HTTP(S), sans identifiants de connexion.
Le résultat `syncStatus` d'une tâche terminée est recalculé à sa lecture pour refléter
la configuration courante, plutôt qu'un bilan périmé conservé dans la tâche.

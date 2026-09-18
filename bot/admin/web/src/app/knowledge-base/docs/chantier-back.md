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

### DTO

**[N]** éventuellement `model/genai/KnowledgeBase*.kt`
Si les corps de requête/réponse admin diffèrent de ceux du client, typiquement le corps
allégé que le studio envoie avant injection des settings. À arbitrer : on peut souvent
réutiliser les data classes du client.

---

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

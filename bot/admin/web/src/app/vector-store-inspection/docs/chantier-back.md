# Chantier back — cartographie des fichiers

Inventaire des ajouts et modifications côté serveur pour rendre l'outil
opérationnel, réparti sur les trois couches : orchestrateur Python, client
Retrofit Kotlin, serveur admin. Convention : **[N]** nouveau fichier, **[M]**
modification d'un fichier existant.

Chemins relatifs à la racine du repo `tock`.

Rappel de principe : chemin runtime intact. Tout passe par un routeur dédié et un
service d'inspection qui **réutilise** les fonctions existantes de
`rag_chain_builder.py` sans les modifier. Le seul point où l'on frôle le runtime
est l'exposition des scores (§ orchestrateur), et il est traité par des variantes
locales, pas par une modification des retrievers de production.

---

## Couche 1 — Orchestrateur (Python / FastAPI)

Base : `gen-ai/orchestrator-server/src/main/python/server/src/gen_ai_orchestrator`

### Routeur

**[N]** `routers/vector_store_inspection_router.py`
Nouveau routeur, patron calqué sur `vector_store_providers_router.py`. Porte les
quatre endpoints du routeur dédié : `/documents`, `/condense`, `/search`, et le
`/indexes`. Gestion d'erreurs via les handlers FastAPI existants
(`create_error_response`, etc.). Ne contient pas de logique métier — délègue au
service d'inspection.

**[M]** `routers/vector_store_providers_router.py`
Ajouter l'endpoint `GET /vector-store-providers/{provider-id}/capabilities`. Il
complète ce routeur existant plutôt que le routeur dédié, car il n'a rien de
spécifique au debug. Renvoie les capacités déclarées du provider.

**[M]** `main.py`
Une ligne : `app.include_router(vector_store_inspection_router)`, à la suite des
`include_router` existants (autour de la ligne 71).

### Modèles de requête / réponse

**[M]** `routers/requests/requests.py`
Ajouter les modèles Pydantic de requête : `VectorStoreInspectionDocumentsRequest`,
`VectorStoreInspectionCondenseRequest`, `VectorStoreInspectionSearchRequest`.
Chacun porte le `vector_store_setting`, l'`embedding_question_em_setting`, etc.,
tels que décrits au contrat §3 — c'est le serveur admin qui les remplira.

**[M]** `routers/responses/responses.py`
Ajouter les modèles de réponse : `IndexListResponse` / `VectorStoreIndexDescription`,
`VectorStoreInspectionDocumentsResponse` (avec `stats`, `anomalies`, `rows`,
`total`, `start`, `end`), `CondenseResponse`, `SearchResponse` (avec `funnel`,
`results`, chaque `SearchResultChunk` portant `ranks`, `scores`, `outcome`),
`VectorStoreCapabilitiesResponse`. C'est le plus gros volume de déclarations de
la couche.

### Service d'inspection — le cœur

**[N]** `services/vector_store_inspection/__init__.py`
**[N]** `services/vector_store_inspection/vector_store_inspection_service.py`
Le service qui orchestre tout. Quatre responsabilités :

- **listing / documents / stats / anomalies** : accès SQL direct à
  `langchain_pg_embedding` et `langchain_pg_collection` (PGVector), sur le modèle
  d'accès de `PostgreSQLTextRetriever`. Pagination par
  `GROUP BY cmetadata->>'id'`, médiane par `percentile_cont`, les trois anomalies
  par requêtes agrégées. Décision de conception à trancher ici : recalcul des
  anomalies à chaque page ou cache par index.
- **condense** : réutilise `build_question_condensation_chain()` tel quel, capture
  la sortie et l'`effective_prompt`.
- **search** : reconstruit le déroulé de retrieval en réutilisant
  `SimilarityRetriever`, `FTSRetriever`, `add_rank_metadata`, `apply_rrf_ranking`,
  mais en **conservant l'état intermédiaire** (les trois listes de canaux, les
  rangs et scores par étage) que le runtime referme. Applique la compression via
  `BloomzRerank` en lisant `metadata['retriever_score']` pour reconstruire les
  `outcome`. Gère `fetch_k`/`k` dissociés et l'ordre `before_cut`/`after_cut`.
- **exact_rank** des chunks épinglés : `COUNT(*)` des chunks mieux classés, une
  requête PGVector par chunk épinglé.

**[N]** `services/vector_store_inspection/vector_store_inspection_scores.py`
(optionnel, si on veut isoler la mécanique de scoring)
Variante de `build_docs()` du `PostgreSQLTextRetriever` qui **conserve**
`row.score` (le `ts_rank` aujourd'hui jeté), et wrapper autour de
`asimilarity_search_with_score()` côté vectoriel. C'est le seul endroit qui touche
à la logique de scoring ; il vit à part pour ne rien changer aux retrievers
runtime.

### Capabilities

**[N]** `services/vector_store/vector_store_capabilities.py`
Fonction qui retourne les capacités par provider (`search_types`,
`supports_scores`, `notes`). Peut démarrer avec des valeurs codées en dur par
provider. C'est ce qui permet à l'UI de ne pas coder « si OpenSearch alors… ».

### Métadonnées — réutilisation, pas de modification

`services/langchain/rag_chain_builder.py` fournit `get_chunk_identifier()`,
`get_web_source_url()`, et le format des métadonnées. **Réutilisé tel quel**, cité
ici pour mémoire : ne pas le modifier.

---

## Couche 2 — Client orchestrateur (Kotlin / Retrofit)

Base : `gen-ai/orchestrator-client/src/main/kotlin/ai/tock/genai/orchestratorclient`

### Interface Retrofit

**[N]** `api/VectorStoreInspectionApi.kt`
Interface Retrofit, patron `VectorStoreProviderApi.kt`. Cinq méthodes :
`getIndexes`, `getDocuments`, `condense`, `search`, `getCapabilities`, avec les
annotations `@POST`/`@GET`/`@Body`/`@Path` correspondantes.

### Service et implémentation

**[N]** `services/VectorStoreInspectionService.kt`
Interface de service, patron `VectorStoreProviderService.kt`.

**[N]** `services/impl/VectorStoreInspectionServiceImpl.kt`
Implémentation, patron `VectorStoreProviderServiceImpl.kt`. Instancie l'API via le
`GenAIOrchestratorClient` existant et relaie.

### Modèles de requête / réponse

**[N]** `requests/VectorStoreInspectionRequests.kt`
Data classes des trois corps de requête. En `camelCase` — le mapper Jackson
existant applique `SNAKE_CASE` + `NON_NULL`, donc rien à annoter.

**[N]** `responses/VectorStoreInspectionResponses.kt`
Data classes des réponses. C'est le gros du volume : `SearchResponse` imbrique
`SearchFunnel`, `FunnelStage`, `SearchResultChunk`, `ChannelRanks`,
`ChannelScores` — une dizaine de data classes à faire correspondre exactement aux
modèles Pydantic. Fastidieux, mécanique, à tester par sérialisation.

### Modèles partagés (orchestrator-core)

**[M]** éventuellement
`gen-ai/orchestrator-core/.../models/` — si certains types (provider, search
type, outcome) gagnent à être partagés côté core plutôt que redéclarés dans le
client. À arbitrer : réutiliser les enums existants (`VectorStoreProvider`,
`DocumentSearchType`) plutôt que les redéfinir.

---

## Couche 3 — Serveur admin (Kotlin / GenAIVerticle)

Base : `bot/admin/server/src/main/kotlin`

### Routes

**[M]** `verticle/GenAIVerticle.kt`
Ajouter les cinq routes admin, patron des routes `/gen-ai/bots/:botId/…`
existantes (le fichier en contient déjà pour rag, vector-store, document-compressor) :

```
GET  /gen-ai/bots/:botId/vector-store/indexes
POST /gen-ai/bots/:botId/vector-store/documents
POST /gen-ai/bots/:botId/vector-store/condense
POST /gen-ai/bots/:botId/vector-store/search
GET  /gen-ai/bots/:botId/vector-store/capabilities
```

Chaque route, via `blockingJsonGet` / `blockingJsonPost` et
`checkNamespaceAndExecute` : résout les settings nécessaires depuis Mongo, les
injecte dans le corps de la requête orchestrateur (que le studio a envoyé
incomplet), appelle le service client, relaie la réponse. Constantes de chemin à
ajouter à côté des `PATH_CONFIG_*` existantes (lignes ~48-53).

Résolutions par route :

- `/search` : vector store setting (`VectorStoreService`) **et** embedding setting
  (depuis la config RAG).
- `/condense` : LLM de condensation et prompt (depuis la config RAG,
  `RAGService`).
- `/documents`, `/indexes` : vector store setting.
- `/capabilities` : résout le provider configuré pour le bot, puis interroge
  l'orchestrateur — le studio ne passe pas le provider.

### Services métier — réutilisation

`service/VectorStoreService.kt`, `service/RAGService.kt`,
`service/DocumentCompressorService.kt` : **réutilisés tels quels** pour lire les
configurations Mongo. Cités pour mémoire. Le compresseur est lu par la route
existante `/configuration/document-compressor`, aucune nouvelle route côté admin
pour lui.

### Modèles DTO (éventuels)

**[N]** éventuellement `model/genai/VectorStoreInspection*.kt`
Si les corps de requête/réponse admin diffèrent de ceux du client (par exemple le
corps allégé que le studio envoie, avant injection des settings). À arbitrer :
souvent on peut réutiliser directement les data classes du client.

---

## Synthèse par effort

| Zone                                                     | Fichiers       | Nature                                | Poids       |
| -------------------------------------------------------- | -------------- | ------------------------------------- | ----------- |
| Routeur + capabilities orchestrateur                     | 2 [N], 2 [M]   | patron existant                       | faible      |
| Modèles requête/réponse Python                           | 2 [M]          | déclaratif, volumineux                | moyen       |
| Service d'inspection — documents/indexes/stats/anomalies | 1-2 [N]        | SQL neuf                              | moyen-élevé |
| Service d'inspection — search                            | (même fichier) | réutilisation + capture d'état        | **élevé**   |
| Scores                                                   | 1 [N]          | variante locale, non intrusive        | faible      |
| Client Retrofit (api + service + impl + modèles)         | 5 [N], 1 [M]   | patron + volume déclaratif            | moyen       |
| Routes admin GenAIVerticle                               | 1 [M]          | patron existant + résolution settings | moyen       |

Le point dur reste concentré dans **une seule méthode** : le `search` du service
d'inspection, qui doit rouvrir ce que la chaîne referme (canaux séparés, rangs et
scores par étage, comptabilité des `outcome`). Tout le reste est soit du patron
répété, soit du SQL neuf mais conceptuellement simple.

L'instrumentation du compresseur, initialement identifiée comme le risque
principal, est **levée** : `BloomzRerank`
(`services/langchain/impls/document_compressor/bloomz_rerank.py`) pose déjà
`metadata['retriever_score']` sur chaque document scoré et gère lui-même la
tolérance de panne (`is_fault_tolerant`, `fill_to_max_documents`). Le service
d'inspection lit ces scores sans modifier le module.

---

## Ce qui n'est PAS touché — garde-fous

- `services/langchain/rag_chain_builder.py` : réutilisé, jamais modifié. C'est la
  garantie que le runtime n'est pas impacté.
- `services/langchain/impls/document_compressor/bloomz_rerank.py` : réutilisé,
  jamais modifié (module maintenu par une autre équipe).
- `routers/rag_router.py`, `services/rag/rag_service.py` : intouchés.
- Les retrievers de production (`SimilarityRetriever`, `FTSRetriever`,
  `HybridRetriever`) : réutilisés, jamais modifiés. Les scores sont récupérés par
  des appels parallèles (`asimilarity_search_with_score`, variante de
  `build_docs`), pas en modifiant ces classes.

Périmètre exclu, à signaler en revue : tout le SQL (listing, documents,
exact_rank) est **PGVector uniquement**. OpenSearch est hors périmètre, et
`/capabilities` le déclare proprement (`hybrid_and_fts_not_implemented`).

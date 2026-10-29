# Chantier back — cartographie des fichiers

Inventaire des ajouts et modifications côté serveur pour rendre l'outil
opérationnel, réparti sur les trois couches : orchestrateur Python, client
Retrofit Kotlin, serveur admin. Convention : **[N]** nouveau fichier, **[M]**
modification d'un fichier existant.

Chemins relatifs à la racine du repo `tock`.

Rappel de principe : **une seule chaîne**. Le diagnostic n'a pas de copie de la
recherche, du RRF ni de la compression : il appelle le même code que le runtime
(`rag_chain_builder.py`, retrievers de production, compresseurs), avec une trace
optionnelle qui expose les étapes intermédiaires. Le runtime a donc été modifié,
mais sans changement de comportement : mêmes documents, même ordre, mêmes noms
d'étapes et de spans, mêmes footnotes. Seules s'ajoutent deux clés de score dans
les métadonnées des documents. Un test de parité le garantit.

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
- **search** : passe par le **pipeline partagé** (mêmes `SimilarityRetriever`,
  `FTSRetriever`, `HybridRetriever`, `apply_rrf_ranking` que le runtime) avec un
  `RetrievalTrace` optionnel qui **conserve l'état intermédiaire** (listes par
  canal, rangs et scores par étage) que le runtime referme. Applique la compression
  via la fonction partagée en lisant `metadata['reranker_score']` pour reconstruire
  les `outcome`, et mappe le repli tolérant aux fautes au stage `failed_fallback`.
  L'ordre est celui du runtime : récupération → RRF → coupe à `k` → compression.
- **exact_rank** des chunks épinglés : `COUNT(*)` des chunks mieux classés, une
  requête PGVector par chunk épinglé.

**Scoring partagé** (implémenté dans les retrievers, pas isolé) :
`get_vector_store_retriever()` renvoie une sous-classe de `VectorStoreRetriever`
qui conserve `1 - distance` dans `metadata['vector_score']`, et `build_docs()`
du `PostgreSQLTextRetriever` conserve le `ts_rank` dans `metadata['fts_score']`. Mêmes documents, même ordre au
runtime ; seul le score est ajouté (non injecté dans le contexte LLM).

### Capabilities

**[N]** `services/vector_store/vector_store_capabilities.py`
Fonction qui retourne les capacités par provider (`search_types`,
`supports_scores`, `notes`). Peut démarrer avec des valeurs codées en dur par
provider. C'est ce qui permet à l'UI de ne pas coder « si OpenSearch alors… ».

### Chaîne partagée — modifications du runtime

**[M]** `services/langchain/rag_chain_builder.py` : `RetrievalTrace` optionnel passé
aux retrievers (`SimilarityRetriever`, `FTSRetriever`, `HybridRetriever`), et
`compress_documents_with_fallback` partagé par le runtime et le diagnostic. Sans
trace, aucun coût et aucun changement de comportement. Fournit aussi
`get_chunk_identifier()` et le format des métadonnées.

**[M]** `services/langchain/factories/vector_stores/pgvector_factory.py` :
`ScoredVectorStoreRetriever`, sous-classe de `VectorStoreRetriever` qui conserve
`1 - distance` dans `metadata['vector_score']` (même nom de run
`VectorStoreRetriever`, donc même span Langfuse).

**[M]** `services/langchain/factories/vector_stores/postgresql_text_retriever.py` :
`build_docs()` conserve le `ts_rank` dans `metadata['fts_score']`.

**[M]** `services/langchain/rag_response_builder.py` : retire ces deux clés des
footnotes renvoyées au bot.

**[N]** `services/langchain/impls/document_compressor/rerank_selection.py` et
**[M]** `vllm_rerank.py`, `bloomz_rerank.py`, `aws_bedrock_rerank.py` : sélection
factorisée (tri, seuil, complément), reproduite à l'identique côté studio pour le
recalcul instantané des seuils ; réinitialisation de `last_fallback_info` à chaque
appel.

**[N]** `tests/services/test_retrieval_pipeline_parity.py` : garde-fou de parité
runtime / diagnostic.

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
| Chaîne partagée + scores                                 | 1 [N], 7 [M]   | runtime modifié, comportement inchangé | moyen       |
| Client Retrofit (api + service + impl + modèles)         | 5 [N], 1 [M]   | patron + volume déclaratif            | moyen       |
| Routes admin GenAIVerticle                               | 1 [M]          | patron existant + résolution settings | moyen       |

Le point dur reste concentré dans **une seule méthode** : le `search` du service
d'inspection, qui doit rouvrir ce que la chaîne referme (canaux séparés, rangs et
scores par étage, comptabilité des `outcome`). Tout le reste est soit du patron
répété, soit du SQL neuf mais conceptuellement simple.

L'instrumentation du compresseur, initialement identifiée comme le risque
principal, est **levée** : chaque compresseur pose déjà `metadata['reranker_score']`
(et `reranker_windows` pour vLLM) sur les documents qu'il reçoit, et enregistre
son repli dans `last_fallback_info`. Le diagnostic construit le compresseur comme
le runtime (tolérant aux pannes) et lit ces informations après l'appel.

---

## Garde-fous

- `routers/rag_router.py`, `services/rag/rag_service.py` et la signature de
  `create_rag_chain()` : intouchés.
- Comportement du runtime : inchangé, vérifié par
  `test_retrieval_pipeline_parity.py` (mêmes documents transmis au modèle, même
  ordre, mêmes paramètres de recherche, compresseur construit à l'identique) et par
  les tests existants de la chaîne RAG.
- Noms des étapes (`similarity_retriever_retrieve`, `hybrid_retrieve`,
  `fts_retrieve`, `document_compressor`) et capture `RunnableAssign<answer>` :
  inchangés, `RAGCallbackHandler` et Langfuse en dépendent.

Périmètre exclu, à signaler en revue : tout le SQL (listing, documents,
exact_rank) est **PGVector uniquement**. OpenSearch est hors périmètre :
`/capabilities` le déclare (`hybrid_and_fts_not_implemented`), et son inspection
garde un appel de recherche direct, hors de la chaîne partagée. La parité
runtime / diagnostic n'est donc garantie que pour PGVector.

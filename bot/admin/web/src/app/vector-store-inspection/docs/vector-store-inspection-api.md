# Contrat d'interface — outil d'inspection de la base vectorielle

Document de spécification à destination des développeurs back. Décrit les
endpoints, leurs entrées et leurs sorties, et les refactors orchestrateur qu'ils
supposent.

Compagnon du document de présentation générale ; on s'y réfère pour la
justification des choix, ce document-ci ne décrit que les interfaces.

---

## 1. Principes

**Routeur dédié, chemin runtime intact.** Toutes les routes vivent sous un
nouveau `vector_store_inspection_router`. Aucune modification de `rag_router`,
`rag_service`, ni de la signature de `create_rag_chain()`. Les fonctions
réutilisées (`build_question_condensation_chain`, `apply_rrf_ranking`,
`add_rank_metadata`, `get_chunk_identifier`) sont importées telles quelles depuis
`rag_chain_builder.py` — pas de duplication, pas de fork.

**Deux niveaux d'appel.** Le studio ne parle pas à l'orchestrateur. Il appelle le
serveur admin (`GenAIVerticle`), qui relaie via `orchestrator-client`. Chaque
endpoint orchestrateur a donc une route admin correspondante (§7). Le serveur
admin résout `namespace` / `botId` en configurations vector store et embedding
depuis Mongo ; le studio n'envoie jamais de credentials.

**Conventions de nommage.** Sur le fil orchestrateur (FastAPI), le JSON est en
`snake_case` — c'est la convention utilisée dans ce document. Le mapper Jackson de
`orchestrator-client` applique déjà `SNAKE_CASE` + `NON_NULL`, donc les data
classes Kotlin restent en `camelCase`. Côté studio, les modèles TypedScript sont
en `camelCase`.

**Capabilities plutôt que conditions codées en dur.** L'UI n'encode jamais « si
OpenSearch alors pas d'hybride ». Elle interroge les capacités du provider et
construit ses contrôles à partir de la réponse (§6).

**La compression est supposée disponible.** À l'heure actuelle
`create_rag_chain()` ne lit jamais `request.compressor_setting` (régression RAG
v3, `d92e72aa`). C'est un défaut distinct, corrigé indépendamment. Cet outil est
spécifié contre le comportement **attendu** : la compression est un étage de
l'entonnoir à part entière, activable, désactivable et observable.

---

## 2. Vue d'ensemble

| #   | Endpoint                                        | Écran                      | Coût                             |
| --- | ----------------------------------------------- | -------------------------- | -------------------------------- |
| 1   | `GET /vector-store-inspection/indexes`          | sélecteur d'index          | requête catalogue                |
| 2   | `POST /vector-store-inspection/documents`       | Exploration                | requête paginée                  |
| 3   | `POST /vector-store-inspection/condense`        | bandeau de condensation    | 1 appel LLM                      |
| 4   | `POST /vector-store-inspection/search`          | Diagnostic + comparaison   | 1 embedding + 1-2 requêtes stock |
| 5   | `GET /vector-store-providers/{id}/capabilities` | construction des contrôles | statique                         |

L'endpoint 5 est le seul hors du routeur dédié : il complète naturellement le
`vector_store_providers_router` existant.

Aucun endpoint pour la configuration du compresseur : le studio la lit depuis la
route admin existante
`GET /rest/admin/gen-ai/bots/{botId}/configuration/document-compressor`, qui
renvoie `BotDocumentCompressorConfigurationDTO` avec son `enabled`.

---

## 3. Endpoints — orchestrateur

### 3.1 `GET /vector-store-inspection/indexes`

Liste les index appartenant à un couple namespace / botId.

**Réponse**

```json
{
  "indexes": [
    {
      "index_name": "ns_acme_bot_assistant_session_4f2a1b8c",
      "index_session_id": "4f2a1b8c-...",
      "index_datetime": "2026-08-09T03:14:00",
      "document_count": 1284,
      "chunk_count": 7902,
      "is_current": true
    }
  ]
}
```

| Champ              | Type         | Description                                                                                                     |
| ------------------ | ------------ | --------------------------------------------------------------------------------------------------------------- |
| `index_name`       | string       | Nom physique normalisé (`PGVectorUtils.normalizeDocumentIndexName`)                                             |
| `index_session_id` | string       | UUID de la session d'ingestion                                                                                  |
| `index_datetime`   | string (ISO) | Date d'ingestion. Si hétérogène dans l'index, la plus récente                                                   |
| `document_count`   | int          | Nombre de documents (groupes)                                                                                   |
| `chunk_count`      | int          | Nombre de chunks                                                                                                |
| `is_current`       | bool         | Index actuellement utilisé par le bot (résolu côté admin contre `BotRAGConfiguration`, pas par l'orchestrateur) |

**Implémentation PGVector** : `langchain_pg_collection` filtré sur
`name LIKE 'ns_{namespace}_bot_{botId}_session_%'`, en réutilisant le préfixe
produit par la normalisation existante — ne pas la réimplémenter à la main.

---

### 3.2 `POST /vector-store-inspection/documents`

Alimente l'écran d'exploration. Renvoie le bilan d'ingestion et une page de
documents.

**Requête**

```json
{
  "vector_store_setting": {},
  "index_name": "ns_acme_bot_assistant_session_4f2a1b8c",
  "filter": { "text": "resiliation", "document_id": null, "anomaly": null },
  "start": 0,
  "size": 25,
  "include_stats": true,
  "include_chunks": true
}
```

| Champ                | Type           | Description                                                          |
| -------------------- | -------------- | -------------------------------------------------------------------- |
| `filter.text`        | string \| null | `ILIKE` sur titre et contenu. Navigation, pas recherche sémantique   |
| `filter.document_id` | string \| null | Restreint à un document                                              |
| `filter.anomaly`     | string \| null | Un des `code` d'anomalie (§3.2.1). Restreint aux documents concernés |
| `start`              | int            | Offset (contrat de pagination partagé du studio)                     |
| `size`               | int            | Taille de page                                                       |
| `include_stats`      | bool           | Inclure le bloc `stats`                                              |
| `include_chunks`     | bool           | Inclure les chunks de chaque document                                |

**Réponse** (forme `PaginatedResult<T>` du studio : `rows` / `total` / `start` /
`end`)

```json
{
  "stats": {
    "document_count": 1284,
    "chunk_count": 7902,
    "chunks_per_document_avg": 6.2,
    "chunk_length_median": 840,
    "index_datetime": "2026-08-09T03:14:00"
  },
  "anomalies": [
    { "code": "near_empty_chunk", "count": 23, "severity": "warning" },
    { "code": "non_url_source", "count": 61, "severity": "info" },
    { "code": "duplicate_title", "count": 4, "severity": "info" }
  ],
  "rows": [
    {
      "document_id": "c9e4...",
      "title": "Résiliation et date d'échéance du contrat",
      "source": "https://example.com/docs/c9e4...",
      "chunk_count": 9,
      "index_session_id": "4f2a1b8c-...",
      "chunks": [
        {
          "chunk_id": "c9e4...:5/9",
          "chunk": "5/9",
          "content": "...",
          "content_length": 812,
          "metadata": {}
        }
      ]
    }
  ],
  "total": 1284,
  "start": 0,
  "end": 25
}
```

**Le regroupement par document se fait côté serveur.** La hiérarchie document →
chunks n'existe pas dans le stock : il n'y a que des chunks portant
`metadata.id` et `metadata.chunk = "n/N"`. Paginer les chunks en affichant des
documents produirait des pages incohérentes. La pagination tourne donc sur
`GROUP BY cmetadata->>'id'`, et `total` compte les groupes.

`stats` et `anomalies` décrivent **l'index entier**, jamais le sous-ensemble
filtré : les compteurs des pastilles sont ce contre quoi l'utilisateur filtre.
`total`, lui, reflète le compte filtré.

`content` est renvoyé après retrait du préfixe titre, via `get_source_content()`.

#### 3.2.1 Définitions des anomalies

Toutes dérivables du contrat de métadonnées existant, sans modification de
l'ingestion.

| code               | règle                                                                 | severity |
| ------------------ | --------------------------------------------------------------------- | -------- |
| `near_empty_chunk` | `content_length` sous un seuil (~50 caractères, hors préfixe titre)   | warning  |
| `non_url_source`   | `get_web_source_url()` renvoie `null` alors que `source` est non vide | info     |
| `duplicate_title`  | même `title` sur des `id` différents                                  | info     |

Les anomalies de découpage (documents mono-chunk) et de dates hétérogènes ont
été écartées : non pertinentes pour le mode d'ingestion Qallam (un index par
session, pas d'échec de chunking suivi d'insertion réaliste).

---

### 3.3 `POST /vector-store-inspection/condense`

Exécute le premier maillon de la chaîne seul, et alimente une question condensée
vers `/search`.

**Contrat orchestrateur**

```json
{
  "question_condensing_llm_setting": {},
  "question_condensing_prompt": { "formatter": "jinja2", "template": "...", "inputs": {} },
  "question": "Puis-je résilier mon assurance habitation en cours d'année ?",
  "chat_history": [{ "text": "...", "type": "HUMAN" }]
}
```

**Ce que le studio envoie réellement à la route admin**

```json
{
  "question": "Puis-je résilier mon assurance habitation en cours d'année ?"
}
```

**`question_condensing_llm_setting`, `question_condensing_prompt` et `chat_history` ne sont pas
fournis par le studio.** C'est la responsabilité du serveur admin de les résoudre
depuis la configuration RAG courante du bot
(`GET /rest/admin/gen-ai/bots/{botId}/configuration/rag`, champs
`questionCondensingLlmSetting` et `questionCondensingPrompt`) au moment de
l'exécution, et de les injecter dans l'appel orchestrateur — comme il le fait
déjà pour les settings vector store et embedding. Le studio n'envoie que la
question.

**Évolution anticipée mais non implémentée.** Les trois champs sont volontairement
laissés dans le contrat orchestrateur pour permettre, à terme, de **tester un
prompt de condensation modifié** depuis l'outil : le studio amorcerait un
formulaire depuis la config RAG, l'utilisateur éditerait le template, et les deux
champs deviendraient des entrées optionnelles de la route admin (fournis → le
back les utilise ; absents → il retombe sur la config, comportement par défaut
décrit ci-dessus). Cette capacité n'est pas retenue dans la première version, son
intérêt réel restant à confirmer. À implémenter, elle suivrait le même principe
que les seuils du compresseur : amorçage depuis la config, modification possible,
divergence signalée. En attendant, la route admin ignore tout prompt qui lui
serait passé et applique systématiquement la configuration courante. Une meme logique pourrait eventuellement s'appliquer à `chat_history` en proposant coté UI de créer à la main un historique de chat de façon à pouvoir tester spécifiquement le comportement de condensation mais le besopin réel reste à prouver.

**Réponse**

```json
{
  "condensed_question": "Conditions de résiliation anticipée d'un contrat d'assurance habitation",
  "key_words": ["résiliation", "assurance habitation", "anticipée"],
  "effective_prompt": "...prompt réellement envoyé au modèle...",
  "duration": 1.284
}
```

Réutilise `build_question_condensation_chain()` inchangé. `effective_prompt` est
capturé comme le fait `RAGCallbackHandler.on_chain_end()` sur le
`ChatPromptValue` — il renvoie le prompt effectivement appliqué, ce qui permet à
l'utilisateur de voir quel template a servi même lorsqu'il vient de la config.

La condensation n'est pas déterministe. L'UI garde la question renvoyée dans un
champ **éditable** et l'envoie telle quelle à `/search`, pour que deux exécutions
identiques ne produisent pas d'écarts fantômes en mode comparaison.

---

### 3.4 `POST /vector-store-inspection/search`

Le cœur. Exécute une recherche et renvoie l'entonnoir complet.

**Requête**

```json
{
  "vector_store_setting": {},
  "embedding_question_em_setting": {},
  "index_name": "ns_acme_bot_assistant_session_4f2a1b8c",
  "search_type": "HYBRID_SEARCH",
  "query": "Conditions de résiliation anticipée d'un contrat d'assurance habitation",
  "key_words": ["résiliation", "assurance habitation", "anticipée"],
  "fetch_k": 150,
  "k": 8,
  "compression_enabled": true,
  "compression_stage": "before_cut",
  "compression_override": {
    "min_score": 0.4,
    "max_documents": 4,
    "fill_to_max_documents": false
  },
  "pinned_chunk_ids": ["c9e4...:5/9"],
  "pinned_rank_strategy": "exact_rank"
}
```

| Champ                  | Type          | Description                                                   |
| ---------------------- | ------------- | ------------------------------------------------------------- |
| `search_type`          | enum          | `SIMILARITY_SEARCH` \| `FULL_TEXT_SEARCH` \| `HYBRID_SEARCH`  |
| `query`                | string        | Question brute ou condensée — jamais recalculée côté serveur  |
| `key_words`            | string[]      | Requis pour FTS et hybride                                    |
| `fetch_k`              | int           | Candidats ramenés du stock                                    |
| `k`                    | int           | Survivants après la coupe                                     |
| `compression_enabled`  | bool          | `false` désactive l'étage compression                         |
| `compression_stage`    | enum          | `before_cut` (cible) \| `after_cut` (reproduction du runtime) |
| `compression_override` | objet \| null | Seuils du compresseur pour cette exécution (§3.4.1)           |
| `pinned_chunk_ids`     | string[]      | Chunks toujours présents dans la réponse                      |
| `pinned_rank_strategy` | enum          | `truncated` \| `score_only` \| `exact_rank`                   |

**`fetch_k` et `k` sont dissociés.** `fetch_k` gouverne combien de candidats sont
ramenés, `k` combien survivent à la coupe. Le runtime utilise un seul `k` pour
les deux, que l'outil sait reproduire (`fetch_k = k`) comme dépasser. Quand
`fetch_k != k`, l'UI signale que la configuration ne reflète pas le runtime.

**`compression_stage`** :

| valeur       | comportement                                                                                                         |
| ------------ | -------------------------------------------------------------------------------------------------------------------- |
| `before_cut` | ramène `fetch_k` → compresse → coupe à `k`. **Ordre cible.** Le compresseur arbitre l'ensemble du vivier             |
| `after_cut`  | ramène `fetch_k` → coupe à `k` → compresse. Fidèle au runtime, où le compresseur enveloppe un retriever déjà tronqué |

L'UI présente `before_cut` comme le défaut et expose `after_cut` via une case
« reproduire l'ordre du runtime ». Une fois la dissociation `fetch_k` adoptée au
runtime, `compression_stage` disparaît et `before_cut` devient le seul
comportement.

`query` et `key_words` sont toujours fournis par l'appelant et jamais recalculés.
C'est ce qui rend la condensation optionnelle et la comparaison possible. En
hybride sans `key_words`, renvoyer une **erreur explicite** plutôt que retomber
silencieusement en vectoriel comme le fait `HybridRetriever` aujourd'hui.

#### 3.4.1 `compression_override`

Trois seuils modifiables pour une exécution, sans toucher à la config du bot. Le
studio les initialise depuis la config stockée du compresseur, et signale toute
divergence introduite par l'utilisateur.

| Champ                   | Type  | Description                                                         |
| ----------------------- | ----- | ------------------------------------------------------------------- |
| `min_score`             | float | En dessous, le chunk est écarté                                     |
| `max_documents`         | int   | Nombre maximum de chunks conservés                                  |
| `fill_to_max_documents` | bool  | Réintroduit des chunks sous le seuil pour atteindre `max_documents` |

**Réponse**

```json
{
  "funnel": {
    "vector": { "status": "applied", "count": 150 },
    "fts": { "status": "applied", "count": 88, "reason": null },
    "rrf": { "status": "applied", "count": 191 },
    "compression": { "status": "applied", "count": 4, "reason": null },
    "top_k_cut": { "status": "applied", "count": 4, "discarded": 0 }
  },
  "compression_stage": "before_cut",
  "results": [
    {
      "chunk_id": "a3f1...:2/7",
      "document_id": "a3f1...",
      "title": "Résiliation d'une assurance habitation",
      "chunk": "2/7",
      "content": "...",
      "ranks": { "vector": 1, "fts": 3, "rrf": 1 },
      "scores": { "vector": 0.891, "fts": 0.42, "rrf": 0.0304, "compressor": 0.77 },
      "outcome": "kept",
      "pinned": false
    }
  ],
  "duration": 0.412
}
```

**`funnel[].status`** : `applied` \| `skipped` \| `disabled` \| `failed_fallback`.
La distinction est essentielle sur `compression` : `BloomzRerank` est
`is_fault_tolerant` par défaut et renvoie les documents inchangés sur timeout ou
erreur HTTP — sans ce statut, « le compresseur a tout gardé » et « le compresseur
a planté » sont indistinguables. `reason` porte la cause sur `failed_fallback`.

`skipped` : étage non utilisé par le mode courant (FTS et RRF en vectoriel pur).
`disabled` : compression désactivée par l'utilisateur.

**`ranks.*` à `null`** : « non trouvé par ce canal ». Une information, pas une
donnée manquante — en FTS, `null` signifie que les mots-clés n'ont pas matché ce
chunk, ce qui est souvent l'explication recherchée.

**`outcome`** :

| valeur                   | signification                                                   |
| ------------------------ | --------------------------------------------------------------- |
| `kept`                   | présent dans le contexte transmis au modèle                     |
| `cut_by_top_k`           | remonté, écarté par la coupe top-k                              |
| `below_min_score`        | a atteint le compresseur, scoré sous `min_score`                |
| `reranked_out`           | au-dessus de `min_score` mais classé au-delà de `max_documents` |
| `filled_below_threshold` | scoré sous `min_score` mais repêché par `fill_to_max_documents` |
| `not_retrieved`          | remonté par aucun canal                                         |

Les trois du milieu ne surviennent que si la compression a tourné.
`filled_below_threshold` mérite un traitement distinct dans l'UI : le chunk a
survécu malgré un score insuffisant, ce qui n'est pas la même chose que d'être
retenu au mérite.

**Épinglage.** Les chunks de `pinned_chunk_ids` apparaissent toujours dans
`results`, même absents de tous les canaux. `pinned_rank_strategy` gouverne le
coût :

| stratégie    | comportement                                                      | coût                       |
| ------------ | ----------------------------------------------------------------- | -------------------------- |
| `truncated`  | cherché uniquement dans le `fetch_k` ramené ; `rank = null` sinon | nul                        |
| `score_only` | score calculé contre la requête, rang non résolu                  | 1 requête ciblée par chunk |
| `exact_rank` | `COUNT(*)` des chunks mieux classés                               | scan de collection         |

L'outil utilise `exact_rank` par défaut : à la volumétrie réelle (jusqu'à ~40 000
chunks), le `COUNT(*)` reste de l'ordre de 100-300 ms, et « ce chunk est 312ᵉ »
est bien plus parlant qu'un score isolé.

Un chunk épinglé peut être **absent de l'index inspecté** — les épingles
survivent à un changement d'index, ce qui permet de comparer deux ingestions.
Dans ce cas `outcome` vaut `not_retrieved` et tous les rangs et scores sont
`null`. Le RRF reste `null` même en `exact_rank` : la fusion ne s'applique qu'au
vivier ramené, un rang RRF hors fenêtre n'a pas de sens.

---

## 4. Comparaison d'exécutions — 100 % côté client

Pas d'endpoint de comparaison. Le studio garde une exécution en mémoire comme
**exécution de référence** et rend toute recherche suivante comme un delta contre
elle. Rien n'est persisté : la référence vit dans le service d'état de la
fonctionnalité et est perdue en quittant l'écran.

La référence est fixée explicitement (« définir comme référence ») plutôt
qu'implicitement depuis la recherche précédente, pour qu'on puisse essayer
plusieurs variantes contre une base stable.

Un descripteur de diff nomme ce qui a changé (`index`, `search_type`, `query`,
`key_words`, `fetch_k`, `k`, `compression`), affiché en bandeau. Deux libellés
d'absence distincts :

- `absent_from_index` — index différents et le chunk n'existe pas dans l'index
  courant. Problème d'ingestion.
- `outside_fetch_k` — même index, le chunk est simplement sorti de la fenêtre
  ramenée. Problème de classement.

Les fusionner masquerait les deux diagnostics que l'outil existe pour séparer.

---

## 5. Endpoint 5 — capabilities

### `GET /vector-store-providers/{provider_id}/capabilities`

```json
{
  "provider": "PGVector",
  "search_types": ["SIMILARITY_SEARCH", "FULL_TEXT_SEARCH", "HYBRID_SEARCH"],
  "supports_scores": true,
  "supports_index_listing": true,
  "supports_metadata_filter": true,
  "notes": []
}
```

Pour OpenSearch en l'état :

```json
{
  "provider": "OpenSearch",
  "search_types": ["SIMILARITY_SEARCH"],
  "supports_scores": true,
  "supports_index_listing": true,
  "supports_metadata_filter": true,
  "notes": ["hybrid_and_fts_not_implemented"]
}
```

Documente au passage une asymétrie aujourd'hui silencieuse :
`OpenSearchVectorStoreSetting.getDocumentSearchParams()` écrase le
`documentSearchType` demandé et force `SIMILARITY_SEARCH`, et
`OpenSearchFactory.get_text_store_retriever()` lève `NotImplementedError`.

Le `provider_id` est dans le path mais résolu côté admin depuis la config du bot :
le studio ne l'envoie pas lui-même (la route admin correspondante est sans
paramètre, §7).

---

## 6. Refactors orchestrateur requis

C'est là que le vrai poids se trouve. Aucun n'est évitable, tous sans impact
runtime.

### 6.1 Exposer les scores

`get_vector_store_retriever()` passe par `as_retriever()` → `similarity_search`,
sans score. `PostgreSQLTextRetriever.build_docs()` calcule
`ts_rank(d.fts_vector, q.ts_query) AS score` puis le **jette**.

Le service d'inspection appelle `asimilarity_search_with_score()` directement sur
le `VectorStore`, plus une variante de `build_docs()` qui conserve `row.score`.
Retrievers runtime inchangés.

### 6.2 Exposer les canaux hybrides séparément

`HybridRetriever.retrieve()` produit `docs_vector` et `docs_fts` puis ne renvoie
que la fusion. Le service d'inspection rejoue la séquence en appelant les mêmes
fonctions (`add_rank_metadata`, `apply_rrf_ranking`) et garde les trois listes.

**Attention à l'ordre** : `apply_rrf_ranking()` écrit
`doc.metadata['rank']['rrf']` en supposant que la clé `rank` existe déjà — créée
par `add_rank_metadata()`. Appeler le RRF sans avoir posé les rangs lève un
`KeyError`.

### 6.3 Listing, comptage et pagination

Inexistant. Requiert un accès SQL direct à
`langchain_pg_embedding` / `langchain_pg_collection`, sur le modèle de
`PostgreSQLTextRetriever`. Le pool est disponible via `db_pool_registry`.

---

## 7. Routes serveur admin (Kotlin)

Ajoutées à `GenAIVerticle`, sur le modèle des routes `/gen-ai/bots/:botId/…`
existantes. Le serveur admin résout les settings depuis Mongo
(`VectorStoreService`, `RAGService`, `DocumentCompressorService`) et les injecte
dans l'appel orchestrateur.

```
GET  /gen-ai/bots/:botId/vector-store/indexes
POST /gen-ai/bots/:botId/vector-store/documents
POST /gen-ai/bots/:botId/vector-store/condense
POST /gen-ai/bots/:botId/vector-store/search
GET  /gen-ai/bots/:botId/vector-store/capabilities
```

Rôle : `admin`, cohérent avec le reste des écrans de configuration RAG. Ouvrir
l'outil aux `botUser` est un chantier séparé.

Côté `orchestrator-client` : une nouvelle interface Retrofit
`VectorStoreInspectionApi` plus `VectorStoreInspectionService` / `…Impl`, sur le
modèle exact de `VectorStoreProviderApi`. Le mapper Jackson existant applique déjà
`SNAKE_CASE` et `NON_NULL` — les data classes Kotlin restent en `camelCase`.

---

## 8. Récapitulatif des types

Côté studio, les types réutilisent l'existant plutôt que de les redéfinir :

| Notion             | Type existant réutilisé                                                                 |
| ------------------ | --------------------------------------------------------------------------------------- |
| Mode de recherche  | `DocumentSearchType` (`rag/rag-settings/models/engines-configurations`)                 |
| Provider           | `VectorDbProvider` (`configuration/vector-db-settings/models/providers-configuration`)  |
| Config compresseur | `CompressorSettings` / `CompressorSetting` (`configuration/compressor-settings/models`) |
| Pagination         | `PaginatedResult<T>` (`model/nlp`), `Pagination` (`shared/components/pagination`)       |

Types propres à la fonctionnalité (aucun équivalent dans le repo) :
`VectorStoreIndex`, `VectorStoreCapabilities`, `IndexStats`, `IndexAnomaly`,
`InspectedDocument`, `InspectedChunk`, `SearchFunnel`, `FunnelStage`,
`ChannelRanks`, `ChannelScores`, `ChunkOutcome`, `CompressionStage`,
`CompressionOverride`, `PinnedRankStrategy`, `SearchRun`, `RunComparison`,
`RunComparisonRow`, `RunDiffField`, `AbsenceReason`, `RunDelta`.

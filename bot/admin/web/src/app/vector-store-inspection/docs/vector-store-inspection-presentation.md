# Outil d'inspection de la base vectorielle — présentation

Document de travail à destination de l'équipe. Décrit la fonctionnalité
maquettée pour le studio Tock, les décisions de conception, et surtout les
modifications côté serveur qu'une mise en œuvre réelle impliquerait.

La maquette est fonctionnelle : elle sert de spécification vivante pour les
développeurs back, avant d'être rendue opérationnelle en réutilisant ses
composants. Cible : PR sur `theopenconversationkit/tock`.

---

## 1. Le problème

Une fois une base vectorielle ingérée, l'équipe n'a aujourd'hui aucune
visibilité sur ce qui s'y trouve réellement, ni aucun moyen d'analyser pourquoi
un chunk pertinent ne remonte pas pour une question donnée. Le debug existant
(`RAGDebugData`, Langfuse) montre le prompt final et la réponse, mais pas le
cheminement du retrieval : combien de candidats ont été ramenés, comment ils ont
été classés, à quelle étape un chunk a disparu.

L'outil comble ce trou avec deux vues.

- **Exploration** : que contient l'index ? Un bilan d'ingestion, des signaux de
  qualité, la liste des documents explorables jusqu'au chunk.
- **Diagnostic** : pourquoi cette réponse ? Le déroulé complet d'une recherche,
  de la base jusqu'aux chunks réellement transmis au modèle, avec la possibilité
  de comparer deux exécutions.

Le périmètre exclut volontairement l'étage de génération (déjà outillé via
`context_usage`), la modification d'index, et les jeux de test sauvegardés.

---

## 2. Ce que la maquette a mis en évidence

En reconstituant la chaîne RAG pour la maquetter fidèlement, plusieurs défauts
du code actuel sont apparus. Deux sont désormais traités : le compresseur était
débranché (il ne tournait pas au runtime) — rétabli par la PR #2114, qui le place
après le retriever ; et les scores de récupération étaient calculés puis jetés —
désormais conservés par le pipeline partagé (voir §5). Les deux défauts ci-dessous
restent ouverts et **indépendants de cette fonctionnalité** ; l'outil est conçu
pour les rendre visibles.

### 2.1 Asymétrie OpenSearch silencieuse

`OpenSearchVectorStoreSetting.getDocumentSearchParams()` écrase le
`documentSearchType` demandé et force `SIMILARITY_SEARCH`.
`OpenSearchFactory.get_text_store_retriever()` lève `NotImplementedError`. Un
utilisateur qui sélectionne « hybride » sur OpenSearch obtient du vectoriel sans
en être informé. Hybride et FTS n'existent que sur PGVector.

### 2.2 Repli silencieux du HybridRetriever

Si le LLM de condensation ne renvoie pas de `key_words`, le `HybridRetriever`
saute FTS et RRF et retombe en vectoriel pur, sans rien signaler. Acceptable en
production, inacceptable dans un outil de diagnostic — qui doit donc l'exposer.

---

## 3. La fonctionnalité

### 3.1 Vue Exploration

Un bilan d'ingestion pour l'index sélectionné :

- **Statistiques** : nombre de documents, de chunks, moyenne de chunks par
  document, longueur médiane des chunks (en caractères, après retrait du préfixe
  titre).
- **Anomalies** cliquables comme filtres, en trois catégories retenues :
  chunks quasiment vides, sources non-URL (inexploitables comme lien dans une
  réponse), titres dupliqués sur des documents distincts.
- **Liste des documents** paginée, chaque document dépliable jusqu'à ses chunks,
  avec la longueur de chaque chunk visible d'un coup d'œil.
- **Épinglage de chunks** : un chunk épinglé ici sera suivi dans la vue
  Diagnostic, où il apparaîtra toujours dans les résultats, même si aucun canal
  de recherche ne le remonte.

Les anomalies de découpage (documents restés en un seul chunk, etc.) ont été
écartées : Qallam ne produit pas de scénario réaliste d'échec de chunking suivi
d'insertion. Les dates d'ingestion hétérogènes ont été écartées de même : un
index par session d'indexation rend le cas structurellement impossible.

### 3.2 Vue Diagnostic

L'écran est disposé selon la **chaîne runtime**, du haut vers le bas, avec des
flèches entre les étapes :

```
1 · Question    →  brute, puis condensée (optionnel, éditable)
      ↓
2 · Recherche   →  index, mode (verrouillé sur le bot), k (nombre maximal
                   de documents récupérés)
      ↓
3 · Compression →  seuils modifiables, débrayable, recalcul instantané
      ↓
4 · Lancement   →  avertissements, bandeau de divergence, recherche
      ↓
   Entonnoir    →  vectoriel + FTS → RRF → coupe à k → compression
   Résultats    →  un chunk par ligne, rangs et scores par canal, sort
```

Les réglages partent de la configuration réelle du bot (mode de recherche,
nombre de documents remontés, état et seuils du compresseur). La recherche
attend que ces réglages soient chargés ; s'ils ne peuvent pas l'être, le
formulaire repart de valeurs par défaut et un message le signale. Un bandeau signale tout écart introduit et propose de revenir aux
réglages du bot ; un message prévient si le bot n'a pas de configuration RAG, ou
si son RAG est désactivé (le runtime n'applique alors pas ces réglages).

**L'entonnoir** affiche chaque étage avec son compte et son statut. Les canaux
vectoriel et FTS sont présentés en parallèle (séparateur `+`), convergeant sur la
fusion RRF ; les étages suivants sont séparés par des flèches indiquant le nombre
de chunks perdus.

**Le tableau de résultats** a des colonnes variables selon le mode : rang et
score vectoriel, FTS, RRF, score du compresseur, et un « sort » par chunk
(`transmis`, `coupé`, `sous le seuil`, `déclassé`, `repêché`, `coupé par le
repli`, `non transmis`, `absent`). Un rang
`null` sur un canal signifie « non remonté par ce canal » — une information, pas
une donnée manquante.

**La comparaison d'exécutions** permet d'épingler une exécution comme référence
puis de lancer d'autres recherches ; un bandeau nomme ce qui a changé entre les
deux (index, mode, question, mots-clés, nombre de documents remontés, compression
— y compris score minimum et nombre max de documents) et un tableau montre les
chunks gagnés, perdus, ou déplacés, ainsi que ceux qui sont désormais transmis
au modèle ou ne le sont plus. Un changement de seuil côté client produit une
exécution comparable sans appel serveur : il ne déplace aucun rang, mais change
les chunks transmis, et c'est ce que la comparaison montre alors. Deux libellés d'absence distincts :
**absent de l'index** (index différents — problème d'ingestion) et **hors du top
k** (même index — problème de classement). Tout est côté client, rien n'est
persisté.

### 3.3 Raccourci depuis les dialogues

Un bouton dans le logger de dialogue (`/analytics/dialogs`), à côté du raccourci
« Open in playground » existant, ouvre le Diagnostic pré-rempli avec l'état réel
de l'échange : question brute, question condensée et mots-clés effectivement
produits ce jour-là. Rejouer avec les valeurs enregistrées évite qu'une nouvelle
condensation, non déterministe, change les entrées analysées.

---

## 4. Le point central : k, Max documents et compresseur

C'est l'apport le plus important de l'outil : régler le compresseur sur des
scores réels, pas à l'aveugle.

### 4.1 Deux paramètres, deux rôles

| Paramètre                        | Rôle                                                     | Coût                         |
| -------------------------------- | ------------------------------------------------------- | ---------------------------- |
| **k** (`maxDocumentsRetrieved`)  | combien de candidats chaque canal remonte, puis le vivier remis au compresseur | une requête SQL, aucun token |
| **compresseur** (`maxDocuments`) | combien de chunks le reranker conserve après rescoring  | un appel au reranker         |

Depuis la PR #2114, le runtime enchaîne : chaque canal remonte `k`, RRF garde le
top `k`, puis le compresseur rescroe ces `k` documents et coupe à `maxDocuments`.
Le **vrai coupe finale** est donc `maxDocuments`, pas `k`.

### 4.2 k n'est pas un critère de pertinence

`k` ne réordonne rien : c'est un `LIMIT` qui reprend l'ordre du retriever tel
quel. Le compresseur, lui, **rescore** avec un cross-encoder qui lit la question
et le chunk ensemble, et peut remonter en tête un document que le vectoriel
classait 30ᵉ.

Comme le compresseur s'applique aux `k` documents remontés, **`maxDocuments` ne
peut pas dépasser `k`** : au-delà, il ne pourrait rien élargir. Le formulaire
bloque ce cas. Un `k` supérieur à `maxDocuments` est en revanche une marge utile :
si le compresseur échoue (repli tolérant aux fautes), les premiers `maxDocuments`
documents dans l'ordre de récupération sont tout de même transmis.

### 4.3 Réglage instantané

La réponse de recherche porte le score reranker de chaque candidat. Changer le
**score minimum** ou le **nombre max de documents** ne relance donc pas de
recherche : la sélection (et les sorts, et le compteur de compression) est
recalculée côté client, instantanément, par une fonction qui reproduit à
l'identique la sélection du back (voir §5). L'outil affiche tous les scores avec
le marqueur de seuil, pour choisir le minimum à l'œil plutôt qu'en valeur
absolue — l'échelle dépend du modèle de reranking.

Seuls le nombre de documents remontés, la question, les mots-clés, l'index et le
débrayage de la compression déclenchent une nouvelle recherche.

---

## 5. Modifications côté serveur

C'est le vrai poids du chantier. L'API elle-même est légère ; ce sont ces
déverrouillages qui font le travail.

### 5.1 Un pipeline « recherche → RRF → compression » partagé

Le runtime et le diagnostic passent par **la même** implémentation, pour qu'ils
observent exactement les mêmes documents, le même ordre et le même comportement de
compression :

- `get_vector_store_retriever()` renvoie une sous-classe de `VectorStoreRetriever`
  qui appelle `*_with_score` et conserve `1 - distance` dans
  `metadata['vector_score']`. Comme `similarity_search` de PGVector délègue
  déjà à `similarity_search_with_score` puis jette le score, les documents et leur
  ordre sont inchangés — seul le score est ajouté. OpenSearch n'est pas touché.
- `build_docs()` conserve le `ts_rank` déjà calculé dans `metadata['fts_score']`.
  Deux clés distinctes, pour que les deux scores survivent à la fusion RRF d'un
  chunk trouvé par les deux canaux (la fusion ne recopie que les clés absentes).
- Un `RetrievalTrace` optionnel (coût nul quand absent, comportement runtime
  identique) et une fonction de compression partagée exposent l'état
  intermédiaire au seul diagnostic.

Les noms des runnables et la capture `RunnableAssign<answer>` sont inchangés, donc
`RAGCallbackHandler` et les spans Langfuse sont préservés. Le score n'entre ni
dans le contexte du LLM ni dans `RAGDocumentMetadata`.

**Attention à l'ordre** : `apply_rrf_ranking()` écrit `doc.metadata['rank']['rrf']`
en supposant que la clé `rank` existe déjà — elle est créée par
`add_rank_metadata()`. Appeler le RRF sans avoir posé les rangs lève un
`KeyError`. L'ordre est load-bearing.

### 5.2 Sélection du compresseur factorisée

Les trois compresseurs (`VllmRerank`, `BloomzRerank`, `AwsBedrockRerank`)
partagent une unique fonction de sélection (`select_reranked_documents`), que le
front reproduit à l'identique pour le recalcul instantané des seuils. Toute
modification de l'une impose de mettre l'autre à jour (commentaires « MIRROR »
réciproques et cas de test partagés).

### 5.3 Listing, comptage et pagination

Inexistant aujourd'hui. Nécessite un accès SQL direct à
`langchain_pg_embedding` / `langchain_pg_collection`, sur le modèle de
`PostgreSQLTextRetriever`. Le pool est déjà disponible via `db_pool_registry`.
La pagination des documents se fait par `GROUP BY cmetadata->>'id'`, puisque la
hiérarchie document → chunks n'existe pas dans le stock.

---

## 6. Contrat d'API

Routeur dédié `vector_store_inspection_router`, chaîne de recherche partagée
avec le runtime (§5.1). Cinq routes admin relaient les endpoints orchestrateur ;
la sixième, `retrieval-settings`, est propre à l'admin. Toutes vivent dans
`GenAIVerticle` (rôle `admin`, comme le reste de la configuration RAG).

| #   | Endpoint                                          | Écran                         |
| --- | ------------------------------------------------- | ----------------------------- |
| 1   | `GET /vector-store/indexes`                        | sélecteur d'index             |
| 2   | `POST /vector-store/documents`                     | Exploration                   |
| 3   | `POST /vector-store/condense`                      | bandeau de condensation       |
| 4   | `POST /vector-store/search`                        | Diagnostic + comparaison      |
| 5   | `GET /vector-store/capabilities`                   | construction des contrôles    |
| 6   | `GET /vector-store/retrieval-settings`            | préremplissage + divergences  |

Les réglages effectifs du bot (activation du RAG, mode de recherche, nombre de
documents remontés, état/seuils/fournisseur/modèle du compresseur) sont assemblés
côté admin et
exposés par `retrieval-settings`. L'**endpoint du compresseur n'est jamais
exposé**.

Le principe des **capabilities** évite de coder en dur les conditions par
provider dans l'UI. L'interface interroge les capacités déclarées du provider et
construit ses contrôles à partir de la réponse. Le jour où l'on porte l'hybride
sur OpenSearch, il suffit de changer une déclaration côté serveur.

Le contrat détaillé (schémas de requête et réponse, définitions des anomalies,
sémantique des sorts) fait l'objet du document
`vector-store-inspection-api.md`, à committer dans `docs/`.

---

## 7. Points ouverts à trancher en équipe

1. **Seuil `near_empty_chunk`** — à fixer, idéalement relatif au `chunk_size`
   d'ingestion plutôt qu'en valeur absolue.
2. **Filtre texte à grande échelle** — un `ILIKE '%…%'` sur le champ document
   d'un index de 40 000 chunks fait un scan complet. S'appuyer sur le
   `fts_vector` déjà présent (change la sémantique en recherche par mots) ou
   ajouter un index trigramme (`pg_trgm`) ?
3. **Ouverture aux `botUser`** — l'outil est réservé aux admins ; l'ouvrir serait
   un chantier séparé (accès en lecture aux configurations, notamment).

---

## 8. État de la maquette

Fonctionnelle et démontrable :

- Exploration complète : chargement, changement de bot, changement d'index,
  filtres, pagination, épinglage, pont vers le Diagnostic.
- Diagnostic complet : préremplissage depuis les réglages réels du bot (mode
  verrouillé), condensation optionnelle et éditable, seuils du compresseur
  modifiables avec recalcul instantané et graphe des scores, entonnoir à cinq
  étages avec flèches, tableau à colonnes variables, comparaison d'exécutions,
  fidélité du repli et du fenêtrage.
- Données déterministes seedées par le couple namespace/bot : chaque bot a son
  corpus stable, de quelques centaines à ~40 000 chunks selon le bot. Les noms
  d'index sont produits par la même normalisation que `PGVectorUtils`.
- Un cas pédagogique scénarisé : une reformulation qui fait disparaître un chunk
  (démonstration de la comparaison d'exécutions).

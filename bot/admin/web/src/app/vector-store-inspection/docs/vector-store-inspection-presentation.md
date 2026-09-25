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
du code actuel sont apparus. Ils sont **indépendants de cette fonctionnalité** et
relèvent de tickets séparés, mais l'outil est précisément conçu pour les rendre
visibles.

### 2.1 Le compresseur est débranché (régression RAG v3)

`create_rag_chain()` ne lit jamais `request.compressor_setting`. Le seul appelant
de `get_compressor_factory()` est l'endpoint de vérification de configuration.
Aucune trace de `ContextualCompressionRetriever` dans le package Python.

Avant le refactor `d92e72aa` (PR #2057, RAG v3), `rag_chain.py` faisait bien
`if request.compressor_setting: retriever = add_document_compressor(...)`. Le
découpage en trois retrievers n'a reporté cette enveloppe sur aucun d'eux.

Conséquence : **activer ou désactiver le compresseur dans le studio n'a aucun
effet à l'exécution.** Le champ `enabled` est correctement propagé jusqu'au
client Kotlin, mais rien ne le lit côté Python. À investiguer séparément.

### 2.2 Asymétrie OpenSearch silencieuse

`OpenSearchVectorStoreSetting.getDocumentSearchParams()` écrase le
`documentSearchType` demandé et force `SIMILARITY_SEARCH`.
`OpenSearchFactory.get_text_store_retriever()` lève `NotImplementedError`. Un
utilisateur qui sélectionne « hybride » sur OpenSearch obtient du vectoriel sans
en être informé. Hybride et FTS n'existent que sur PGVector.

### 2.3 Les scores sont calculés puis jetés

`get_vector_store_retriever()` passe par `as_retriever()` → `similarity_search`,
qui ne renvoie pas de score. `PostgreSQLTextRetriever.build_docs()` calcule
`ts_rank(...) AS score` dans son SQL puis ne conserve que `row.document` et
`row.cmetadata`. Par ailleurs, `RAGDocumentMetadata.retriever_score` porte en
réalité le score du **compresseur**, pas du retriever — nommage trompeur.

### 2.4 Repli silencieux du HybridRetriever

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

L'écran est disposé selon la **chaîne cible**, du haut vers le bas, avec des
flèches entre les étapes :

```
1 · Question    →  brute, puis condensée (optionnel, éditable)
      ↓
2 · Recherche   →  index, mode, fetchK
      ↓
3 · Compression →  seuils modifiables, débrayable
      ↓
4 · Contexte    →  k
      ↓
   Entonnoir    →  vectoriel + FTS → RRF → compression → coupe
   Résultats    →  un chunk par ligne, rangs et scores par canal, sort
```

**L'entonnoir** affiche chaque étage avec son compte et son statut. Les canaux
vectoriel et FTS sont présentés en parallèle (séparateur `+`), convergeant sur la
fusion RRF ; les étages suivants sont séparés par des flèches indiquant le nombre
de chunks perdus.

**Le tableau de résultats** a des colonnes variables selon le mode : rang et
score vectoriel, FTS, RRF, score du compresseur, et un « sort » par chunk
(`transmis`, `coupé`, `sous le seuil`, `déclassé`, `repêché`, `absent`). Un rang
`null` sur un canal signifie « non remonté par ce canal » — une information, pas
une donnée manquante.

**La comparaison d'exécutions** permet d'épingler une exécution comme référence
puis de lancer d'autres recherches ; un bandeau nomme ce qui a changé entre les
deux (index, mode, question, mots-clés, fetchK, k, compression) et un tableau
montre les chunks gagnés, perdus, ou déplacés. Deux libellés d'absence distincts :
**absent de l'index** (index différents — problème d'ingestion) et **hors du top
fetchK** (même index — problème de classement). Tout est côté client, rien n'est
persisté.

### 3.3 Raccourci depuis les dialogues

Un bouton dans le logger de dialogue (`/analytics/dialogs`), à côté du raccourci
« Open in playground » existant, ouvre le Diagnostic pré-rempli avec l'état réel
de l'échange : question brute, question condensée et mots-clés effectivement
produits ce jour-là. Rejouer avec les valeurs enregistrées évite qu'une nouvelle
condensation, non déterministe, change les entrées analysées.

---

## 4. Le point central : fetchK, k et compresseur

C'est l'apport le plus important de l'outil, et il porte une proposition
d'évolution du runtime.

### 4.1 Trois troncatures, trois rôles

| Paramètre                        | Rôle                                                   | Coût                         |
| -------------------------------- | ------------------------------------------------------ | ---------------------------- |
| **fetchK**                       | combien de candidats ramener de la base                | une requête SQL, aucun token |
| **compresseur** (`maxDocuments`) | combien de chunks le reranker conserve après rescoring | un appel au reranker         |
| **k**                            | combien de chunks partent au modèle de réponse         | des tokens, de la latence    |

`fetchK` répond à « combien de candidats vaut-il la peine d'examiner ? » — la
réponse veut être large. `k` répond à « combien de texte le LLM peut-il
digérer ? » — la réponse veut être petite. Ce sont deux contraintes de nature
différente.

**Le runtime actuel les confond en une seule valeur.** Pour ne pas noyer le LLM,
on met `k = 4`, et l'on se prive de 46 candidats que personne n'a jamais
examinés, alors que les ramener n'aurait rien coûté.

### 4.2 k n'est pas un critère de pertinence

`k` ne réordonne rien : c'est un `LIMIT` qui reprend l'ordre du retriever tel
quel. Le compresseur, lui, **rescore** avec un cross-encoder qui lit la question
et le chunk ensemble, et peut remonter en tête un document que le vectoriel
classait 30ᵉ.

D'où une formulation qui tient dans tous les cas : **k est le contrat avec le
LLM**, une garantie de taille de prompt, pas un réglage de pertinence. En régime
nominal, il ne fait rien — le reranker a déjà tranché — et c'est le signe que la
chaîne fonctionne. Il ne reprend la main que dans trois cas : compresseur
désactivé, `maxDocuments > k`, ou échec du compresseur (`is_fault_tolerant`
renvoie les documents inchangés, `k` évite alors d'injecter 150 chunks au LLM).

Règle de configuration saine : **`maxDocuments ≤ k`**. Un `k` légèrement
supérieur (par exemple 4 / 8) est même une marge utile : si le compresseur
échoue, on transmet un peu plus de documents moins bien qualifiés, en espérant
qu'un chunk pertinent figure dans les 8 premiers. À l'inverse, `maxDocuments > k`
fait que `k` tronque le travail que le reranker vient de faire — l'outil le
signale.

### 4.3 L'ordre des étages

Dans le runtime, le compresseur enveloppe un retriever déjà tronqué à `k` : il ne
voit que `k` documents et ne peut que retrancher. Son `maxDocuments` par défaut
(50) est donc inerte.

La chaîne cible inverse : ramener 150 candidats, **compresser sur les 150**, puis
couper à `k`. Là, le reranker fait son travail — il arbitre un vivier large.

L'outil maquette la cible et permet de constater l'écart via une case à cocher
« reproduire l'ordre actuel du runtime ». Combinée à la comparaison d'exécutions,
elle démontre concrètement, chunk à l'appui, ce qu'un `fetchK` runtime
apporterait — plutôt qu'un paragraphe d'explication.

**Proposition à débattre** : dissocier `fetchK` de `k` sur
`BaseVectorStoreSearchParams` au runtime, pour que le compresseur puisse enfin
arbitrer un vivier large.

---

## 5. Modifications côté serveur

C'est le vrai poids du chantier. L'API elle-même est légère ; ce sont ces
déverrouillages qui font le travail. Chacun est conçu **sans impact sur le
chemin runtime**.

### 5.1 Exposer les scores

Le service d'inspection appelle `asimilarity_search_with_score()` directement sur
le `VectorStore`, et une variante de `build_docs()` qui conserve `row.score`. Les
retrievers de production ne sont pas touchés.

### 5.2 Exposer les canaux hybrides séparément

`HybridRetriever.retrieve()` produit `docs_vector` et `docs_fts` puis ne renvoie
que la fusion. Le service d'inspection rejoue la séquence en appelant les mêmes
fonctions (`add_rank_metadata`, `apply_rrf_ranking`) et conserve les trois
listes. `HybridRetriever` reste inchangé.

**Attention à l'ordre** : `apply_rrf_ranking()` écrit `doc.metadata['rank']['rrf']`
en supposant que la clé `rank` existe déjà — elle est créée par
`add_rank_metadata()`. Appeler le RRF sans avoir posé les rangs lève un
`KeyError`. L'ordre est load-bearing.

### 5.3 Listing, comptage et pagination

Inexistant aujourd'hui. Nécessite un accès SQL direct à
`langchain_pg_embedding` / `langchain_pg_collection`, sur le modèle de
`PostgreSQLTextRetriever`. Le pool est déjà disponible via `db_pool_registry`.
La pagination des documents se fait par `GROUP BY cmetadata->>'id'`, puisque la
hiérarchie document → chunks n'existe pas dans le stock.

---

## 6. Contrat d'API

Routeur dédié `vector_store_inspection_router`, chemin runtime intact. Cinq
endpoints orchestrateur, plus les routes admin correspondantes dans
`GenAIVerticle` (rôle `admin`, comme le reste de la configuration RAG).

| #   | Endpoint                                        | Écran                      |
| --- | ----------------------------------------------- | -------------------------- |
| 1   | `GET /vector-store-inspection/indexes`          | sélecteur d'index          |
| 2   | `POST /vector-store-inspection/documents`       | Exploration                |
| 3   | `POST /vector-store-inspection/condense`        | bandeau de condensation    |
| 4   | `POST /vector-store-inspection/search`          | Diagnostic + comparaison   |
| 5   | `GET /vector-store-providers/{id}/capabilities` | construction des contrôles |

Aucun endpoint n'est nécessaire pour la configuration du compresseur : le studio
lit la route admin existante
`GET /rest/admin/gen-ai/bots/{botId}/configuration/document-compressor`.

Le principe des **capabilities** évite de coder en dur les conditions par
provider dans l'UI. L'interface interroge les capacités déclarées du provider et
construit ses contrôles à partir de la réponse. Le jour où l'on porte l'hybride
sur OpenSearch, il suffit de changer une déclaration côté serveur.

Le contrat détaillé (schémas de requête et réponse, définitions des anomalies,
sémantique des sorts) fait l'objet du document
`vector-store-inspection-api.md`, à committer dans `docs/`.

---

## 7. Points ouverts à trancher en équipe

1. **Dissociation de fetchK au runtime** (§4.3) — proposer un `fetchK` distinct
   de `k` sur `BaseVectorStoreSearchParams` ? C'est la proposition de fond que
   l'outil sert à démontrer.
2. **Renommage de `retriever_score`** — il porte en réalité le score du
   compresseur. Renommer (breaking) ou ajouter des champs distincts par étage ?
3. **Seuil `near_empty_chunk`** — à fixer, idéalement relatif au `chunk_size`
   d'ingestion plutôt qu'en valeur absolue.
4. **Filtre texte à grande échelle** — un `ILIKE '%…%'` sur le champ document
   d'un index de 40 000 chunks fait un scan complet. S'appuyer sur le
   `fts_vector` déjà présent (change la sémantique en recherche par mots) ou
   ajouter un index trigramme (`pg_trgm`) ?
5. **Ouverture aux `botUser`** — l'outil est réservé aux admins ; l'ouvrir serait
   un chantier séparé (accès en lecture aux configurations, notamment).
6. **Régression du compresseur** (§2.1) — ticket dédié.

---

## 8. État de la maquette

Fonctionnelle et démontrable :

- Exploration complète : chargement, changement de bot, changement d'index,
  filtres, pagination, épinglage, pont vers le Diagnostic.
- Diagnostic complet : contrôles pilotés par les capabilities, condensation
  optionnelle et éditable, seuils du compresseur modifiables, entonnoir à cinq
  étages avec flèches, tableau à colonnes variables, comparaison d'exécutions.
- Données déterministes seedées par le couple namespace/bot : chaque bot a son
  corpus stable, de quelques centaines à ~40 000 chunks selon le bot. Les noms
  d'index sont produits par la même normalisation que `PGVectorUtils`.
- Deux cas pédagogiques scénarisés : une reformulation qui fait disparaître un
  chunk (démonstration de la comparaison d'exécutions), et un chunk que le
  reranker rattrape et que la coupe top-k écarterait (démonstration de l'ordre
  des étages).

Reste à faire avant PR : un composant partagé de sélection d'index, les tests
Karma (surtout la logique pure de comparaison), et la mise à jour du document de
contrat.

# Proposition d'évolution Tock — Base de connaissances interne (Knowledge Base)

> Statut : proposition de travail, itération 1. Document destiné à la discussion d'équipe, pas encore une spécification.

## 1. Contexte et problème

Aujourd'hui, alimenter la base vectorielle d'un bot RAG Tock suppose un outillage externe à Tock : une chaîne d'ingestion dédiée (chez nous Qallam, qui ne sera probablement pas open sourcé) ou des scripts Python peu accessibles. Deux conséquences :

1. **Barrière à l'entrée.** Un utilisateur de Tock qui veut monter un bot RAG doit d'abord résoudre un problème d'ingestion qui n'a rien à voir avec Tock. La communauté open source n'a aucune réponse fournie par la plateforme.
2. **Aucune main pour le métier.** Quand le bot répond mal ou incomplètement sur un sujet, la correction passe par le corpus documentaire et par une réingestion. Le délai se compte en jours, et la maîtrise appartient à l'équipe technique, pas à l'équipe métier qui détient la connaissance.

La proposition consiste à ajouter à Tock une base de connaissances interne, faite d'entrées question/réponse, éditable par les utilisateurs métier depuis le studio, embeddée et projetée dans la base vectorielle du bot.

## 2. Objectifs et non-objectifs

### Objectifs

- Permettre à un utilisateur métier de créer, modifier et supprimer des entrées Q/R depuis le studio, sans intervention technique.
- Permettre de monter un bot RAG fonctionnel **avec Tock seul**, sans aucun outil d'ingestion externe.
- Permettre d'enrichir un index déjà alimenté par une chaîne d'ingestion tierce, quelle qu'elle soit.
- Rendre visible l'état de synchronisation entre les entrées éditées et le contenu réel de l'index.
- Reprendre l'existant : supprimer le détournement des FAQ désactivées aujourd'hui utilisé pour injecter de la donnée dans le corpus (§3.5).

### Non-objectifs (explicites)

- **Pas de réponse verbatim, pas de court-circuit du retriever.** Le besoin de réponse pilotée mot pour mot existe, mais il relève des stories existantes. La piste identifiée pour le traiter est l'interposition d'une NLU basée LLM en entrée, afin de pallier les limites actuelles des modèles NLP en cohabitation avec le RAG. C'est un chantier distinct.
- **Pas d'empiètement sur les FAQ et les stories.** La KB est une entité nouvelle, isolée, dans ses propres collections Mongo. Le fonctionnement legacy de Tock n'est pas modifié.
- **Pas de couplage à une chaîne d'ingestion particulière.** Aucune dépendance à Qallam, ni dans le code ni dans le modèle.
- **Pas de fork Arkéa.** La feature est pensée pour être proposée en amont dès la v1.
- **Pas d'outil d'édition de masse.** Le volume cible est de quelques dizaines à quelques centaines d'entrées par bot. L'import et l'export de fichiers, eux, font partie du périmètre : la migration depuis les FAQ en dépend (§3.5).

## 3. Proposition fonctionnelle

### 3.1 L'entrée KB

Une entrée porte :

- un **intitulé**, qui est la formulation principale de l'entrée : ce qu'elle dit, tel qu'un utilisateur le formulerait. Il joue le rôle que tiendrait une question dans une FAQ, **sans imposer la dichotomie question / réponse** : une entrée peut parfaitement énoncer un fait plutôt que répondre à une question. Il identifie aussi l'entrée dans le studio et sert de référence affichée à défaut d'URL ;
- optionnellement, des **termes de rapprochement** : autres formulations, synonymes, mots-clés, tout ce qui aide l'entrée à être retrouvée ;
- un **contenu** en markdown ;
- une **URL de référence optionnelle**, pour renvoyer vers une ressource distante en lien avec la donnée ;
- des **tags**, un **statut** (brouillon / publiée) ;
- une traçabilité (auteur, date de création, date de modification).

### 3.2 Deux modes d'usage

**Mode autonome.** La KB est la seule source du bot. Tock génère lui-même un `indexSessionId`, embedde les entrées, écrit l'index et renseigne la configuration RAG. Un bot RAG opérationnel sans aucun outillage externe. C'est le mode qui porte la valeur de démonstration la plus forte et qui répond au trou fonctionnel de la plateforme.

**Mode mixte.** Un index existe déjà, produit par une chaîne d'ingestion tierce. Tock y ajoute ses propres lignes, en complément des chunks documentaires. Le mode mixte est le cas dérivé du précédent, il n'ajoute aucun mécanisme nouveau côté écriture.

### 3.3 Projection et synchronisation

**Tout est en temps réel, rien n'attend une validation ultérieure.** Créer ou modifier une entrée publiée l'indexe, la dépublier ou la supprimer retire ses lignes, sans qu'aucune action supplémentaire soit requise. L'entrée elle-même est écrite en base immédiatement ; sa projection dans l'index est confiée à une tâche (§3.6) qui aboutit en général en moins d'une seconde, et dont l'échec éventuel est rapporté sur l'entrée que l'utilisateur vient d'enregistrer.

C'est le couple brouillon / publiée qui joue le rôle de validation en deux temps : on prépare en brouillon, on active en publiant. Ajouter une synchronisation explicite ferait doublon, viderait le statut « publiée » de son sens, et ouvrirait un mode d'échec silencieux où le bot continue de répondre faux parce que personne n'a cliqué.

La **synchronisation reste nécessaire, mais comme réparation, pas comme validation**. Elle ne s'affiche donc que lorsqu'un écart existe, et son libellé doit le dire. Trois causes produisent un écart, toutes extérieures à l'action de l'utilisateur :

- une nouvelle session d'index a été configurée sur le bot, cas dominant en mode mixte, puisqu'on ne se couple à aucune chaîne d'ingestion et que Tock n'est pas notifié de la fin d'une ingestion tierce ;
- une projection a échoué, la base vectorielle étant injoignable ;
- l'index a été modifié hors de Tock.

#### États

Un **journal de projection** en Mongo enregistre un document par `(indexSessionId, entryId, contentHash)`, d'où se déduisent trois états :

| État      | Signification                                                                  |
| --------- | ------------------------------------------------------------------------------ |
| `indexed` | publiée et présente dans l'index de la session courante, à jour                |
| `pending` | publiée mais absente ou obsolète dans l'index courant                          |
| `orphan`  | présente dans l'index courant alors qu'aucune entrée en vigueur ne la justifie |

Une entrée dépubliée retombe en `none`, jamais en `orphan`, puisque ses lignes sont retirées sur-le-champ. L'état `orphan` ne concerne par ailleurs que **l'index courant** : lors d'un changement de session, les lignes de l'ancienne session vivent dans un autre index qui n'est plus interrogé, donc elles ne sont pas orphelines, elles sont hors de portée. Le bilan d'un changement de session est « toutes les entrées publiées en attente, zéro orpheline ».

#### Détection de l'écart

Deux niveaux, de coût très différents. Le studio n'interroge jamais la base vectorielle lui-même : il appelle `getSyncStatus` et affiche le résultat.

**Niveau 1, gratuit, systématique.** Comparaison du journal avec l'`indexSessionId` configuré sur le bot, le `contentHash` de l'entrée et son statut. Mongo seul, aucun accès au stock vectoriel. Couvre le cas dominant, le changement de session. C'est ce qui est servi au chargement du board, avec `searchEntries`, soit deux appels et aucune latence de recherche vectorielle.

**Niveau 2, payant, ponctuel.** Confrontation du journal à la réalité de l'index, seule capable de détecter les écritures échouées et les modifications externes. Trois déclencheurs : après un échec d'écriture, sur une action explicite de vérification, ou en tâche de fond après le chargement sans bloquer l'affichage. Jamais un péage sur l'affichage de la liste.

Deux constats sur l'existant. L'endpoint `/vector-store-inspection/documents` n'expose aujourd'hui que trois filtres, texte, `document_id` et anomalie : il n'y a pas de filtre sur métadonnée arbitraire, donc pas de récupération globale des lignes `source_type = internal_kb`. Les capacités déclarées des deux providers portent en revanche `supports_metadata_filter: true`, donc l'ajout d'un tel filtre est peu coûteux et profite aussi à l'outil d'inspection. En revanche la vérification **d'une entrée isolée** est déjà possible sans rien ajouter : le `metadata.id` d'une ligne KB étant l'identifiant de l'entrée, un appel avec `filter.document_id = entryId` dit si la ligne est présente et dans quelle version. C'est exactement ce dont la vue d'édition a besoin.

Point de vigilance : le journal n'est fiable que tant que toutes les écritures passent par Tock. C'est vrai en mode autonome, moins en mode mixte où une chaîne tierce peut réécrire l'index. La vérification de niveau 2 doit donc rester accessible, et non être traitée comme un raffinement.

#### Réparation

Vu les volumes, une reprojection intégrale est acceptable ; le `contentHash` sert uniquement à éviter le travail inutile. Comme toute écriture vers l'index, la réparation est une tâche suivie (§3.6).

### 3.4 Test de remontée

Depuis l'écran d'édition, une action pose une question en langage naturel, lance une recherche sémantique sur l'index courant et affiche le rang de l'entrée dans les résultats. Sans cela, l'optimisation d'une entrée se fait à l'aveugle : on ne distingue pas « l'entrée n'a pas été récupérée » de « elle a été récupérée mais un autre chunk a pris le dessus à la génération ».

**Aucun endpoint nouveau n'est nécessaire.** L'outil d'inspection de la base vectorielle expose déjà `POST /vector-store-inspection/search`, qui accepte un paramètre `pinned_chunk_ids` : les chunks épinglés sont garantis présents dans la réponse, avec leur `outcome`, y compris la valeur `not_retrieved` lorsqu'aucun canal ne les a remontés. C'est exactement la question posée. Le test consiste donc à appeler cette recherche en épinglant le chunk de l'entrée courante et à lire son issue.

Le `chunk_id` est construit par `get_chunk_identifier()` sous la forme `{id}:{chunk}`. En posant que le `metadata.id` d'une ligne KB **est l'identifiant de l'entrée**, le chunk d'une entrée vaut `{entryId}:1/1`, calculable dans l'écran d'édition sans requête préalable.

Le filtre textuel de l'écran d'exploration ne peut pas rendre ce service : c'est un `ILIKE` sur titre et contenu, de la navigation, pas une recherche sémantique.

En complément, une action « ouvrir dans le diagnostic » passe le `chunk_id` en paramètre de route ; le diagnostic l'épingle à l'initialisation et l'utilisateur y accède à l'analyse fine, variation de `k` et de `fetchK`, modes de recherche, compression, comparaison d'exécutions. Le test intégré donne la réponse immédiate, le diagnostic l'investigation.

Deux réserves à connaître. Le test suppose un index : en mode autonome non encore amorcé, l'action est désactivée avec un message explicite. Et tant que la régression RAG v3 sur `compressor_setting` n'est pas corrigée, l'outil d'inspection reflète le comportement **attendu** de la compression et non le runtime réel, écart qu'il faut signaler plutôt que laisser découvrir en recette.

### 3.5 Import et export

Deux besoins concrets, l'un de migration, l'autre d'exploitation.

#### Reprendre l'existant : import depuis les FAQ

Avant cette feature, le besoin d'ajouter au corpus une donnée non ingérable par la chaîne documentaire a été comblé par un détournement : le métier crée des FAQ qu'il laisse **désactivées**, on exporte ces FAQ au format markdown, et on ajoute manuellement les fichiers obtenus au corpus ingéré. Le mécanisme fonctionne, mais il est manuel, fragile, et invisible pour celui qui édite. Le supprimer est l'un des objectifs de la feature, et c'est la meilleure preuve que le besoin préexistait.

La mise en production suppose donc de reprendre ce qui vit aujourd'hui en FAQ. L'import accepte le **JSON d'export des FAQ**, qui est le tableau des `FaqDefinition` renvoyées par `/faq/search` : il porte les utterances et l'`I18nLabel` multilingue, là où l'export markdown est déjà aplati et les a perdues.

Correspondance retenue :

| FAQ                                           | Entrée KB     | Note                                                                                                                  |
| --------------------------------------------- | ------------- | --------------------------------------------------------------------------------------------------------------------- |
| `utterances[0]`                               | `title`       | le titre de la FAQ n'est pas repris : c'est la première utterance qui constitue la formulation principale             |
| `utterances[1..n]`                            | `searchHints` |                                                                                                                       |
| `answer.i18n[locale]`                         | `content`     | l'import détecte les locales présentes ; s'il y en a plusieurs, il demande laquelle retenir, les autres sont ignorées |
| `tags`                                        | `tags`        |                                                                                                                       |
| `footnotes[0].url`                            | `sourceUrl`   | uniquement si c'est une URL `http(s)`                                                                                 |
| `description`                                 | —             | note interne sans équivalent côté KB, ignorée                                                                         |
| `enabled`                                     | —             | affiché dans la prévisualisation pour permettre le tri, mais ne détermine pas le statut                               |
| `intentName`, `applicationName`, identifiants | —             | propres au mécanisme NLU                                                                                              |

L'import est **prévisualisé avant application** : locales détectées, nombre d'entrées lues, correspondances établies, lignes écartées et motif. Les entrées sont créées en **brouillon**, sans exception : un import qui pousserait d'office des dizaines d'entrées dans l'index sans relecture serait le contraire de ce que la feature cherche à apporter. Le rapport d'import propose ensuite une publication en lot des entrées importées (§3.6), l'utilisateur venant précisément de les relire.

Les doublons sont détectés sur l'intitulé normalisé, avec trois politiques au choix : ignorer, mettre à jour l'entrée existante, ou créer malgré tout.

Tock ne touche pas aux FAQ d'origine après un import. La migration est une opération ponctuelle, et la personne qui la mène supprime elle-même les FAQ devenues inutiles une fois l'import validé.

#### Passer d'un environnement à l'autre : export JSON

L'export sert au transfert entre recette et production, dans les deux sens. Enveloppe versionnée plutôt que tableau nu, pour porter la provenance et permettre de faire évoluer le format :

```json
{
  "format": "tock-knowledge-base",
  "version": 1,
  "exportedAt": "2026-09-17T10:00:00Z",
  "namespace": "…",
  "botId": "…",
  "entries": [
    {
      "sourceId": "…",
      "title": "…",
      "searchHints": ["…"],
      "content": "…",
      "sourceUrl": null,
      "tags": ["…"],
      "status": "PUBLISHED"
    }
  ]
}
```

Rien de ce qui est propre à un environnement n'est exporté : ni identifiants internes, ni état de projection, ni session d'index. Le `sourceId` est informatif et sert de clé de rapprochement secondaire lors d'un réimport.

L'importateur accepte les deux formes et les distingue par leur structure : l'enveloppe ci-dessus, reconnue à son champ `format`, pour un export KB, un tableau nu d'objets portant `utterances` et `answer.i18n` pour un export FAQ. Un fichier qui ne correspond à ni l'un ni l'autre est refusé avec un message explicite plutôt qu'interprété au mieux.

### 3.6 Actions en lot et suivi des traitements

Le régime temps réel rend chaque action immédiate, mais il ne dit rien du nombre d'entrées concernées. Préparer plusieurs brouillons puis tout activer d'un coup est un usage naturel, et c'est indispensable après un import : reprendre une centaine de FAQ en les éditant une à une pour les publier serait inacceptable.

Deux points d'entrée pour la publication groupée :

- dans le **rapport d'import**, une action directe sur les entrées qui viennent d'être importées ;
- sur la **liste**, une sélection multiple offrant publication et dépublication groupées, utile aussi hors import.

#### Tout passe par une tâche

Publier cent entrées, c'est cent projections. Le studio émet **un seul appel**, le serveur met le travail en file et rend compte de son avancement, sur le principe déjà en place pour l'exécution des datasets : le front interroge à intervalle et reflète la progression.

La décision structurante est que **l'écriture unitaire emprunte exactement le même chemin**. Une première version prévoyait de traiter l'entrée seule en ligne et de renvoyer une tâche déjà terminée ; c'était une mauvaise idée pour deux raisons. Le serveur aurait eu deux comportements sur un même endpoint, et le front aurait dû gérer les deux formes de réponse, donc le branchement était payé deux fois. Surtout, un enregistrement synchrone pendant qu'un lot tourne met **deux écrivains sur le même index**, éventuellement sur la même entrée. La mise en file supprime ce risque par construction : ce n'est pas un gain de simplicité, c'est un gain de correction.

Le coût est d'une seconde d'attente sur un enregistrement unitaire, ce qui est négligeable au regard du problème évité.

Seule la projection est mise en file. **L'entrée elle-même est écrite en base immédiatement** et revient dans la réponse, accompagnée de la tâche qui la projettera. Corollaire utile : les champs de projection d'une entrée ne sont plus modifiés à l'enregistrement, ils décrivent ce que contient réellement l'index, et seule la tâche est habilitée à les changer. L'état affiché reste donc honnête pendant toute la durée du traitement.

#### Modèle de tâche

Six types partagent le même modèle : enregistrement, suppression, publication, dépublication, réparation d'index, création d'index. Quatre états : `QUEUED`, `RUNNING`, `COMPLETED`, `FAILED`.

Une tâche porte sa progression (`total`, `done`, `failed`), la liste des entrées en échec avec leur motif, le nombre de lignes écrites et supprimées, et l'état de synchronisation une fois terminée, ce qui évite un appel supplémentaire pour rafraîchir le bandeau.

**Un échec partiel ne fait pas échouer la tâche** : les entrées concernées restent en attente et sont listées. L'état `FAILED` est réservé à la tâche qui n'a pas pu s'exécuter du tout.

Deux endpoints suffisent au suivi :

- `GET /knowledge-base/jobs/:jobId` — interrogation pendant l'exécution ;
- `GET /knowledge-base/jobs/active` — tâche en cours sur le bot, quelle qu'en soit l'origine.

Le second n'est pas un confort. Il couvre deux situations qui se produiront : l'utilisateur recharge sa page pendant une publication de cent entrées, et un collègue a lancé une réparation d'index depuis un autre poste. Sans lui, l'écran affiche un état faux.

Puisque le serveur met en file, l'interface n'a pas à empêcher d'enchaîner les actions : elles se succèdent au lieu de se concurrencer.

## 4. Modèle de données

### 4.1 Collection `knowledge_base_entry`

| Champ                              | Type           | Note                                                                                           |
| ---------------------------------- | -------------- | ---------------------------------------------------------------------------------------------- |
| `_id`                              | ObjectId       |                                                                                                |
| `namespace`                        | String         |                                                                                                |
| `botIds`                           | List\<String\> | liste et non scalaire, pour ne pas fermer une mutualisation ultérieure ; un seul élément en v1 |
| `title`                            | String         | formulation principale de l'entrée, embeddée                                                   |
| `searchHints`                      | List\<String\> | facultatif ; formulations alternatives, synonymes, mots-clés, embeddés                         |
| `content`                          | String         | markdown, embeddé                                                                              |
| `sourceUrl`                        | String?        | ressource distante optionnelle                                                                 |
| `tags`                             | List\<String\> |                                                                                                |
| `status`                           | Enum           | `DRAFT` / `PUBLISHED`                                                                          |
| `contentHash`                      | String         | empreinte du contenu projeté                                                                   |
| `author`, `createdAt`, `updatedAt` |                | traçabilité                                                                                    |

### 4.2 Collection `knowledge_base_projection`

Un document par `(indexSessionId, entryId)` : `contentHash` projeté, `projectedAt`, nombre de lignes écrites, identifiants des lignes.

### 4.3 Métadonnées des lignes indexées

Les lignes KB reprennent **strictement** les métadonnées des chunks produits par une ingestion classique, afin d'être indiscernables du point de vue du runtime :

`index_session_id`, `index_datetime`, `id`, `chunk` (`"1/1"`), `title`, `source`

Plus deux ajouts :

- `source_type` : `"internal_kb"`
- `kb_entry_id` : identifiant de l'entrée source

**Trois de ces clés sont obligatoires, pas recommandées.** `build_footnotes()` lit `metadata['id']`, `metadata['title']` et `metadata['source']` en accès direct, et `get_source_content()` fait de même sur `title`. Une ligne KB à laquelle il manquerait une de ces clés déclencherait une `KeyError` au moment de construire les footnotes, c'est-à-dire en production, exactement quand l'entrée est utilisée dans une réponse. `source` peut valoir `null`, mais la clé doit être présente.

`metadata.id` porte l'identifiant de l'entrée KB, ce qui rend le `chunk_id` dérivable (§3.4). `kb_entry_id` reste renseigné pour permettre un filtrage explicite sans reposer sur la convention.

Le `title` de la métadonnée est celui de l'entrée. `source` est alimenté par `sourceUrl` quand elle est renseignée, ce qui fait remonter l'entrée dans le mécanisme de citation existant sans code supplémentaire : `get_web_source_url()` ne retient une source que si c'est une URL `http(s)`, sinon la footnote s'appuie sur le `title`. Cela règle au passage la collision avec la règle d'anomalie `non_url_source` de l'outil d'inspection, qui devra exempter `source_type = internal_kb` ou tolérer l'absence d'URL.

Le nom physique de l'index n'est jamais construit côté studio : PGVector et OpenSearch ne normalisent pas de la même façon, le premier remplaçant tout caractère hors `[a-z0-9_]` par `_`. Il est obtenu depuis l'endpoint `/indexes`, qui renvoie `index_name`.

Aucune métadonnée de langue n'est portée : les chunks issus des chaînes d'ingestion n'en portent pas, aucun retriever ne filtre dessus, et une ligne KB doit rester indiscernable d'un chunk documentaire. Si un bot multilingue a besoin de distinguer ses entrées côté édition, un tag y suffit. Le sujet pourra être rouvert le jour où le retriever saura filtrer sur métadonnée.

### 4.4 Stratégie d'embedding

**Une entrée donne une ligne unique**, dont le contenu embeddé est l'entrée complète : intitulé, termes de rapprochement, contenu. Ce contenu est aussi celui qui est servi au modèle de réponse.

Trois raisons :

1. **Comparabilité des scores.** Les chaînes d'ingestion embeddent le contenu documentaire. Une ligne KB qui n'embedderait que la question vivrait dans une autre région de l'espace vectoriel et ses scores ne seraient pas comparables à ceux des chunks. En mode mixte, c'est exactement le contraire de ce qu'on cherche.
2. **La valeur sémantique est dans le contenu.** C'est lui qui porte le vocabulaire métier, donc le recouvrement avec les questions réelles des utilisateurs. Faire reposer la remontée sur le seul intitulé suppose que le rédacteur métier ait anticipé les formulations, ce qui n'est pas une hypothèse raisonnable.
3. **Les modèles d'embedding utilisés sont entraînés en asymétrique**, requête vers passage. C'est précisément le cas question utilisateur vers contenu de réponse.

Les termes de rapprochement restent utiles, mais comme **vocabulaire de rappel intégré au texte embeddé**, pas comme lignes distinctes. Deux conséquences pratiques :

- on ne dépend plus du dédoublonnage RRF, dont la clé est `(id, chunk)` et qui n'intervient qu'en mode hybride : en similarité pure, plusieurs lignes issues de la même entrée occuperaient plusieurs places du top-k pour une seule et même réponse ;
- il faut garder les termes peu nombreux, une liste trop longue diluant le vecteur.

#### Forme du contenu

Elle est normée par l'existant et doit être reproduite. Le script d'indexation encadre le texte dans une fence markdown, puis préfixe par le titre suivi d'une ligne vide. `get_source_content()` retire exactement ce préfixe pour l'affichage. Une ligne KB prend donc la même forme :

````
{title}

```markdown
{termes de rapprochement, un par ligne}

{content}
````

```

Sans cela, les entrées KB s'afficheraient différemment des chunks documentaires dans les footnotes comme dans l'outil d'inspection, et leur contenu ne serait pas comparable à l'embedding près.

## 5. Architecture et impacts

### 5.1 Ce qui ne bouge pas

C'est le point central pour l'acceptabilité de la feature :

- `create_rag_chain`, les retrievers, le RRF, la fusion, la configuration Kotlin du RAG : **rien ne change**.
- Les lignes KB sont des lignes ordinaires de l'index. Un bot existant dont la KB est vide se comporte exactement comme aujourd'hui.
- Les FAQ, les stories et la NLU ne sont pas touchées.

Aucun risque de régression sur les bots en production.

### 5.2 Ce qui est à créer

**Côté studio (Angular).** Un module `knowledge-base` lazy-loaded, sur le patron de `quality/datatsets` : routing dédié, `AuthGuard`, `ApplicationResolver`, scope Transloco, entrée dans le menu Gen AI. Liste paginée avec filtres (statut, tag, état de projection), formulaire d'édition, bandeau de synchronisation, action de test de remontée.

**Côté Kotlin.** Nouvelle collection, DAO, service, endpoints CRUD. Le service orchestre la projection en appelant l'orchestrateur gen-AI, en lui transmettant l'`EMSetting` et la configuration vector store du bot.

**Côté orchestrateur gen-AI (Python).** C'est ici que se situe la vraie demande, et elle doit être posée sans euphémisme.

L'orchestrateur n'expose aujourd'hui **aucun chemin d'écriture vers la base vectorielle**. Ses routeurs sont tous en lecture ou en interrogation : complétion, RAG, QA, fournisseurs LLM / EM / vector store / compresseur, monitoring. La seule indexation qui existe dans le dépôt est un script CLI, `tock-llm-indexing-tools/scripts/indexing/vectorisation/run_vectorisation.py`, exécuté hors du serveur avec un fichier de configuration JSON.

La feature suppose donc d'ouvrir le premier chemin d'écriture de l'orchestrateur vers le stock vectoriel. C'est un changement de nature, pas un ajout d'endpoints de plus, et c'est le premier point à arbitrer (§8). La plomberie, en revanche, existe déjà : les factories exposent `get_vector_store()` et le script utilise `add_documents()`. Le travail est de l'exposition, pas de l'implémentation.

Deux capacités à créer :

- `POST /knowledge-base/index` — embedding et upsert d'un lot de lignes, avec création de l'index si nécessaire ;
- `POST /knowledge-base/delete` — suppression des lignes d'une entrée.

Elles sont appelées par le serveur admin depuis la file de tâches décrite en §3.6, et servent indifféremment une entrée ou cent : la projection unitaire n'est pas un cas particulier.

**Piste de simplification à vérifier.** En posant des identifiants de ligne déterministes dérivés de l'identifiant d'entrée, `add_documents(ids=…)` réalise un upsert et la suppression se fait par identifiant, sans filtre sur métadonnée. Si le comportement se confirme sur les versions de `langchain_postgres` et du client OpenSearch utilisées, cela simplifie la seconde capacité et règle le point d'arbitrage sur le mode de suppression (§8).

À noter pour éviter une fausse piste : l'endpoint `/qa` existant fait bien de la récupération pure, mais il est câblé en dur sur OpenSearch et ne renvoie que titre, URL et contenu, sans rang, score ni épinglage. Il ne peut pas servir au test de remontée, qui s'appuie sur `/vector-store-inspection/search`.

### 5.3 Contrainte non négociable : cohérence du modèle d'embedding

La projection **doit** passer par l'orchestrateur et réutiliser l'`EMSetting` du bot. Une entrée embeddée avec un autre modèle est silencieusement inexploitable, sans aucun signal.

En mode mixte, un garde-fou est nécessaire : si l'index cible a été produit avec un modèle différent **mais de même dimension**, l'écriture passe et le résultat est inutilisable sans erreur. Une dimension différente échoue bruyamment, c'est le cas facile. À défaut de pouvoir vérifier le modèle d'origine, l'écran doit au minimum afficher un avertissement explicite lors de la première projection sur un index non produit par Tock.

### 5.4 Différences entre providers

| | PGVector | OpenSearch |
| --- | --- | --- |
| Création d'index | implicite à l'écriture | mapping `knn_vector` explicite requis |
| Suppression par métadonnée | filtre sur la colonne de métadonnées | `delete_by_query` |
| Nom normalisé | `ns_{ns}_bot_{botId}_session_{id}` | `ns-{ns}-bot-{botId}-session-{id}` |

Le mode autonome doit donc créer explicitement l'index côté OpenSearch, et générer le `sessionId` côté Tock.

Le nommage n'est pas commun aux deux providers : `PGVectorUtils.normalizeDocumentIndexName` passe en minuscules puis remplace tout caractère hors `[a-z0-9_]` par `_`, là où `OpenSearchUtils` conserve les tirets. Le nom physique est donc toujours résolu côté serveur, ou lu depuis `/indexes`, et jamais reconstruit par le studio.

## 6. Points de vigilance à assumer

### 6.1 Cohabitation KB / corpus documentaire

En mode mixte, le risque principal n'est pas l'absence de l'entrée dans le top-k, c'est sa **cohabitation avec des chunks documentaires qui disent autre chose sur le même sujet**. C'est même le cas d'usage type, puisqu'on crée une entrée KB précisément quand le corpus répond mal. Les deux remontent, et l'arbitrage revient au LLM sans qu'aucune règle de priorité ne lui ait été donnée.

Mitigation proposée : une mention dans le prompt d'answering du type « en cas de divergence, les entrées issues de la base interne font foi », adossée au `source_type` présent dans les métadonnées. C'est peu coûteux, mais ce doit être une décision explicite du projet, pas une découverte en recette.

Point favorable : la chaîne d'answering renvoie déjà un `context_usage` portant un `used_in_response` par chunk, dont sont dérivées les footnotes. On dispose donc nativement d'un second signal, plus fin que la remontée, permettant de savoir si l'entrée a été **utilisée** dans la réponse et non seulement récupérée. C'est exactement ce qu'il faut pour observer l'arbitrage décrit ci-dessus. Hors v1, mais la donnée existe déjà, sans développement backend.

### 6.2 Remontée dans le top-k

Une entrée de quelques centaines de caractères est en concurrence avec des chunks de plusieurs milliers, sans reranking. En pratique, une entrée dont le contenu est pertinent a de bonnes chances de remonter, et la facilité d'édition rend l'optimisation itérative simple. C'est précisément ce que l'action de test de remontée doit rendre observable.

Cela reste une limite structurelle de la chaîne actuelle, à traiter par le chantier reranking, pas par la KB.

## 7. Découpage

### v1

- Collections, DAO, endpoints CRUD Kotlin
- Module studio `knowledge-base` avec liste, édition, statut brouillon/publié
- Endpoints orchestrateur d'indexation et de suppression
- Projection synchrone à l'enregistrement
- Journal de projection, états `indexed` / `pending` / `orphan`, bandeau et action de resynchronisation
- Mode autonome (création d'index et de session par Tock) et mode mixte
- Test de remontée sur l'index courant
- Import du JSON d'export des FAQ, prévisualisé, et export JSON des entrées (§3.5)
- Publication et dépublication en lot, depuis le rapport d'import et depuis la liste (§3.6)
- File de tâches côté serveur et suivi de progression côté studio, pour toute écriture vers l'index (§3.6)

### Hors v1, évolutions identifiées

- **Création d'une entrée depuis un dialogue mal répondu**, pré-remplie avec la question réelle de l'utilisateur, au départ du feedback utilisateur ou des evaluation samples. C'est la boucle qui donne tout son sens à la feature, et elle se branche directement sur le dashboard bot en cours de développement.
- Dates de validité sur une entrée (offre promotionnelle, information temporaire)
- Mutualisation d'entrées entre plusieurs bots d'un namespace (le modèle est déjà ouvert via `botIds`)
- Réservation de slots pour les sources KB dans le top-k, une fois le chantier reranking avancé

## 8. Points à arbitrer

1. **Ouvrir un chemin d'écriture dans l'orchestrateur.** C'est l'arbitrage structurant. L'orchestrateur n'écrit aujourd'hui jamais dans la base vectorielle, l'indexation vivant dans un script CLI hors serveur (§5.2). La feature suppose de franchir ce pas. Est-ce acceptable dès lors que le studio ne manipule ni credentials ni settings et que tout passe par le serveur admin puis l'orchestrateur, ou y a-t-il une objection de principe ?
2. **Droits de publication.** Si le rôle `botUser` suffit à publier, la KB devient un vrai levier de correction rapide pour le métier. Si un rôle admin est requis, la promesse de réactivité tombe et il faut le dire d'emblée. Une piste intermédiaire : `botUser` crée et modifie des brouillons, un rôle supérieur publie.
3. **Priorité KB dans le prompt d'answering.** On ajoute la règle de préséance ou on laisse le LLM arbitrer ?
4. **Nommage upstream.** « Knowledge Base » risque une confusion avec les FAQ existantes pour la communauté. Alternatives à discuter : *Curated Answers*, *Knowledge Entries*, *Internal Knowledge*.
5. **Suppression d'une entrée.** Suppression physique des lignes dans l'index, ou dépublication laissant l'entrée en base avec nettoyage à la resynchronisation ? La première est plus propre, la seconde plus tolérante aux pannes de la base vectorielle. À trancher conjointement avec la piste des identifiants de ligne déterministes (§5.2), qui rendrait la suppression triviale.
6. **Verrouillage des entrées pendant un lot.** Les tâches s'exécutant en file, deux écritures ne se concurrencent plus sur l'index. Reste à décider si le serveur doit en outre empêcher l'édition d'une entrée déjà comprise dans un lot en attente, ou laisser la dernière écriture l'emporter.
7. **Mutualisation front du service d'inspection.** Le test de remontée appelle le service de recherche du module `vector-store-inspection`, déjà structuré en classe abstraite avec implémentations mock et REST interchangeables. Pour éviter une dépendance entre deux features de haut niveau, il est proposé de remonter cette abstraction, ses deux implémentations et ses modèles dans `shared/services`. Décision front, sans impact sur les contrats d'API.
```

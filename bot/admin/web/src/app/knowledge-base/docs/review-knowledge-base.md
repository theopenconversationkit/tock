# Base de connaissances — review Codex et vérification Claude

Pour chaque point, le constat de la review initiale est suivi d'un encart **Vérification Claude** : confirmation, nuances et compléments. La numérotation des sept findings d'origine est conservée.

**Origine des constats.** Codex rapporte que les tests unitaires ciblés et la compilation Angular passent, mais que le test d'intégration PGVector échoue sur une base neuve. Claude a vérifié les sept points **uniquement par lecture du code**, sans exécuter de tests. Aucun nouveau test n'a été lancé pour rédiger ce document. Les numéros de ligne sont ceux des reviews et peuvent évoluer.

**Vue d'ensemble.** Les sept points sont initialement classés P2. Claude ne considère aucun d'eux comme un faux positif et propose de reclasser les points 5 et 7 en P3.

| Point | Sujet | Appréciation de Claude |
| --- | --- | --- |
| 1 | Bascule vers une collection vide | Le plus sérieux ; probabilité faible, impact potentiel en production. |
| 2 | Résultat de tâche inaccessible pendant une panne | Bug réel ; correction locale simple. |
| 3 | Faux message de réussite de la bascule | Bug visible ; autre cas identifié avec une tâche déjà active. |
| 4 | Mauvais index dans le diagnostic | Flux RxJS confirmé par lecture ; cas naturel après création d'un index. |
| 5 | Relecture du corpus pour chaque entrée | Coût quadratique confirmé ; **P3 proposé** aux volumes modestes. |
| 6 | Parcours intégral avant chaque suppression | Coût quadratique confirmé ; touche aussi certains parcours de création. |
| 7 | Test PGVector incomplet | Défaut confirmé par lecture ; **P3 proposé**, car limité au test. |

## 1. Vérifier la création de la collection avant de basculer le bot

**Localisation :** [KnowledgeBaseJobWorker.kt][worker], lignes 182–183.

### Point relevé par Codex · P2

**Scénario.** La dernière entrée publiée est dépubliée alors qu'une tâche `CREATE_INDEX` attend dans la file. Son `pendingJobId` la maintient dans la liste `ids`, mais le worker n'effectue aucune écriture d'indexation pour cette entrée.

**Défaut et impact.** Le contrôle `ids.isNotEmpty()` passe malgré l'absence de création effective de l'index. Avec PGVector, la validation RAG qui suit peut créer une collection vide, sans les métadonnées attendues, puis basculer le bot dessus. L'index est ensuite signalé `MISSING`.

**Correction attendue.** Revérifier les prérequis de création pendant l'exécution et confirmer la création effective de la collection avant toute bascule.

> **Vérification Claude — confirmé ; le plus sérieux des sept points.**
>
> Claude retrouve la chaîne suivante dans le code :
>
> 1. La construction de `ids` inclut les entrées avec `pendingJobId != null`, même dépubliées.
> 2. Une entrée dépubliée mais `everPublished` passe par `remove()`. Dans le scénario décrit, aucune ligne n'est écrite et aucune collection n'est créée par cette suppression.
> 3. `ids.isNotEmpty()` reste pourtant vrai.
> 4. `switchKnowledgeBaseIndex()` appelle `RAGValidationService.validate()`, qui passe par `check_vector_store_setting`, puis une recherche `asimilarity_search` via `get_vector_store()`.
> 5. Le constructeur PGVector utilise un mécanisme `get_or_create`, également signalé par le commentaire de `pgvector_factory.py:62`.
> 6. La collection ainsi créée est vide et sans `schema_version`. `deriveIndexState` la considère comme `MISSING`, alors que le bot a déjà basculé.
>
> **Impact précisé.** La fenêtre de concurrence s'étend de la mise en file à l'exécution. Elle peut durer plusieurs minutes si un traitement volumineux précède cette tâche. Si le RAG est activé, les réponses peuvent se retrouver sans contexte.
>
> **Garde-fous proposés par Claude :**
>
> - Au début de la tâche, revérifier `createIndexBlocker`.
> - Après la boucle et avant la bascule, appeler `service.probe(target)` et exiger un état différent de `MISSING`. C'est le contrôle indispensable : le contrôle initial seul ne couvre pas une dépublication pendant l'exécution.
>
> **Piège à éviter.** Ne pas utiliser `job.projected > 0` comme preuve de création. Le compteur est remis à zéro lors de la reprise d'une tâche `RUNNING` ; après un crash, une collection déjà créée pourrait donc être rejetée à tort.

## 2. Garder le résultat d'une tâche consultable pendant une panne

**Localisation :** [KnowledgeBaseService.kt][service], ligne 549.

### Point relevé par Codex · P2

**Scénario.** Une panne de l'orchestrateur ou du stockage vectoriel fait échouer une tâche. Lorsque le front demande son résultat, `jobDTO()` appelle `sync()`, qui interroge à nouveau le fournisseur indisponible et lève une exception.

**Défaut et impact.** `GET /jobs/:id` renvoie une erreur HTTP 500 au lieu de l'état `FAILED` et de l'erreur déjà persistés. Le tableau de bord abandonne alors le suivi de la tâche, qui ne peut plus être retrouvée via l'endpoint des tâches actives.

**Correction attendue.** Renvoyer le résultat persisté même si le statut de synchronisation complémentaire ne peut pas être obtenu.

> **Vérification Claude — confirmé ; correction locale simple.**
>
> Claude confirme que `jobDTO()` appelle `sync()` pour les tâches `COMPLETED` et `FAILED`. `sync()` appelle `probe()`, qui propage volontairement les erreurs afin qu'une panne ne soit pas confondue avec un index vide.
>
> Le problème est donc reproductible dans le flux normal d'une panne : la tâche échoue, puis sa consultation échoue à son tour. Côté front, le gestionnaire d'erreur remet `this.job` à `null`. Côté Mongo, `activeJobs` ne retourne que les tâches `QUEUED` et `RUNNING` : la tâche échouée disparaît du suivi et l'utilisateur ne voit pas la notification d'échec.
>
> **Correction proposée.** Dans `jobDTO()`, entourer uniquement la récupération du statut complémentaire avec `runCatching { sync(...) }.getOrNull()`, en journalisant l'erreur avec `logger.warn`. Claude relève un précédent de tolérance à l'échec de `probe()` dans `get()`.
>
> **Dépendance avec le point 3.** `notifyIndexChecked` et `notifyIndexCreated` acceptent déjà un `syncStatus` nul, mais l'annonce de bascule doit rester correcte lorsque ce statut manque.

## 3. Afficher le résultat réel de la bascule d'index

**Localisation :** [entries-board.component.ts][board], lignes 502–506.

### Point relevé par Codex · P2

**Scénario.** Une tâche `CREATE_INDEX` se termine en `COMPLETED` avec des échecs sur certaines entrées. Le worker s'abstient alors de basculer le bot, même si cette option avait été demandée.

**Défaut et impact.** Le front donne la priorité à la valeur `switched`, issue de la case cochée, sur la session renvoyée par le serveur. Le dialogue annonce donc que le bot utilise le nouvel index alors qu'il utilise toujours l'ancien.

**Correction attendue.** Traiter explicitement les échecs partiels et déduire la bascule de la configuration résultante, pas de l'intention exprimée dans le formulaire.

> **Vérification Claude — confirmé ; bug visible par l'utilisateur.**
>
> Claude confirme que `switched ?? (...)` donne la priorité à la case cochée, alors que le worker ne bascule que si `failures.isEmpty()`.
>
> **Cas supplémentaire.** `enqueue()` peut renvoyer une tâche `CREATE_INDEX` déjà active. La case cochée dans le dialogue courant ne correspond alors pas nécessairement au `switchIndex` de la tâche réellement suivie.
>
> **Corrections proposées par Claude :**
>
> - Déduire `botSwitched` de `job.syncStatus?.indexSessionId === job.indexSessionId`, comme dans le suivi d'une tâche redécouverte par polling.
> - Si `syncStatus` est absent, se rabattre sur `switched && job.failures.length === 0`.
> - Afficher le nombre d'échecs dans le dialogue de résultat.

**Point à arbitrer lors du correctif — note de rédaction Codex.** Le repli proposé par Claude reste une déduction à partir de la demande, pas une confirmation de la bascule. En particulier, il ne résout pas le cas de la tâche déjà active décrit juste au-dessus. En l'absence de statut fiable, le message ne devrait pas affirmer une bascule confirmée sans autre preuve côté serveur.

## 4. Appliquer l'index demandé lorsque la liste est déjà en cache

**Localisation :** [diagnostic.component.ts][diagnostic], lignes 200–203.

### Point relevé par Codex · P2

**Scénario.** L'utilisateur a déjà visité l'inspection vectorielle pour ce bot, puis ouvre le diagnostic depuis la base de connaissances. `indexes$` rejoue sa liste en cache avant l'affectation de `navigationIndexName`.

**Défaut et impact.** L'appel suivant à `loadIndexes(false)` ne réémet pas la liste vers la souscription qui applique la sélection. L'index précédemment choisi reste sélectionné. La question et le chunk épinglé sont bien transmis, mais la recherche interroge la mauvaise collection.

**Correction attendue.** Appliquer immédiatement l'index demandé à la liste en cache. S'il en est absent, rafraîchir la liste puis réessayer.

> **Vérification Claude — confirmé par lecture du flux RxJS, sans exécution.**
>
> Claude confirme l'ordre des événements par lecture du flux RxJS :
>
> 1. La souscription à `state.indexes$`, un `BehaviorSubject`, reçoit immédiatement la liste en cache.
> 2. Le callback des configurations affecte ensuite `navigationIndexName`.
> 3. `loadIndexes(false)` retourne le cache sans réémettre vers la souscription existante. La sélection demandée n'est donc pas réappliquée.
>
> Le passage depuis la KB transmet `syncStatus.indexName`. Le problème reste invisible si cet index est déjà sélectionné, mais apparaît dans deux cas :
>
> - L'utilisateur avait choisi un autre index dans l'exploration.
> - Un index vient d'être créé ou activé depuis la KB et n'apparaît pas dans la liste en cache. Le message `index_not_found` ne se déclenche même pas dans ce parcours.
>
> **Correction proposée.** Extraire une méthode qui applique la sélection demandée à la liste courante `state.indexes`. Si l'index est absent, appeler `state.refreshIndexes()`, puis réessayer ou avertir l'utilisateur.

## 5. Ne charger que l'entrée en cours de traitement

**Localisation :** [KnowledgeBaseJobWorker.kt][worker], lignes 120–122.

### Point relevé par Codex · P2

**Scénario.** Pour chaque entrée d'une publication en masse, d'une création d'index ou d'une réparation, le worker appelle `dao.entries()` puis sélectionne une seule entrée.

**Défaut et impact.** Chaque itération charge et désérialise tout le corpus du bot, contenu compris. Pour 1 000 entrées, cela représente environ un million de documents lus, tout en conservant le verrou global du worker de projection. Le coût de lecture croît donc au carré du nombre d'entrées.

**Correction attendue.** Ajouter une lecture par identifiant, limitée au namespace et au bot. Cela conserve la relecture fraîche nécessaire à la gestion des modifications concurrentes sans recharger tout le corpus.

> **Vérification Claude — confirmé ; P3 proposé en pratique.**
>
> Le coût quadratique est confirmé, ainsi que la conservation du verrou global pendant le traitement. Claude propose néanmoins **P3 en pratique** : pour un corpus modeste, les appels réseau d'embedding de plusieurs centaines de millisecondes dominent généralement le coût de chaque itération.
>
> **Cas supplémentaire.** Le même schéma existe dans `KnowledgeBaseService.bulk()` : `request.entryIds.distinct().map { entry(...) }` peut provoquer jusqu'à 1 000 chargements complets dans la requête HTTP, sous le verrou de mutation.
>
> **Correction proposée.** Ajouter `KnowledgeBaseDAO.entry(namespace, botId, id)`, avec un filtre sur le namespace et l'appartenance du bot à `botIds`, puis l'utiliser dans les deux parcours. Adapter également le DAO en mémoire utilisé par les tests.

## 6. Limiter le contrôle d'appartenance aux lignes à supprimer

**Localisation :** [knowledge_base_service.py][python-service], lignes 293–294.

### Point relevé par Codex · P2

**Scénario.** Chaque suppression utilise `stored_rows()` pour télécharger l'ensemble des documents KB et recalculer leurs empreintes de contenu, uniquement pour vérifier que les lignes à supprimer appartiennent aux entrées demandées.

**Défaut et impact.** Le worker envoie une demande de suppression par entrée. Une dépublication en masse ou un nettoyage des lignes orphelines répète donc le parcours du corpus restant, avec des coûts de transfert et de calcul quadratiques.

**Correction attendue.** Interroger uniquement les identifiants candidats et leurs métadonnées d'appartenance, en conservant les contrôles sur le type de source et l'identifiant d'entrée.

> **Vérification Claude — confirmé.**
>
> Claude confirme que `stored_rows()` récupère le contenu `e.document` pour toutes les lignes KB et recalcule leur SHA-256. Ces deux opérations sont inutiles pour le contrôle d'appartenance : `entry_id` est extrait des métadonnées via `kb_entry_id`, puis `id`, puis `identifier`.
>
> **Cas supplémentaire.** Ce coût peut aussi toucher `CREATE_INDEX`. Une entrée `everPublished` mais dépubliée déclenche `remove()` avec une liste vide ; le service utilise alors l'identifiant déterministe comme candidat et peut parcourir le corpus pour finalement ne rien supprimer.
>
> **Correction proposée :**
>
> - Calculer les identifiants candidats de la demande.
> - Avec PGVector, limiter la requête à ces identifiants, à la collection et à `source_type = 'internal_kb'`, en ne sélectionnant que l'identifiant de ligne et les métadonnées.
> - Avec OpenSearch, utiliser une requête `ids`.
> - Conserver le contrôle d'appartenance à l'entrée demandée avant suppression.
> - Adapter les tests existants qui remplacent `stored_rows()` par un mock.

## 7. Fournir les métadonnées de création dans le test PGVector

**Localisation :** [test_knowledge_base_service.py][python-tests], lignes 350–354.

### Point relevé par Codex · P2

**Scénario.** Le test est lancé avec `KB_TEST_PG_PORT` pointant vers une base neuve. Les deux premières demandes d'indexation omettent `collection_metadata`.

**Défaut et impact.** `index_entries()` renvoie `knowledge-base.job.index_missing` sans écrire de données. Le test ignore ces résultats, puis `inspect_rows()` échoue avec `UndefinedTable`. La review initiale rapporte une reproduction sur une base pgvector isolée.

**Correction attendue.** Fournir les métadonnées de création pour les écritures initiales et vérifier leurs résultats avant de tester les mises à jour et les suppressions.

> **Vérification Claude — confirmé par lecture ; P3 proposé.**
>
> Claude confirme par lecture que le helper `request()` ne définit pas `collection_metadata`. Sur une base neuve, `to_regclass` renvoie `None`, donc `exists=False`, et `index_entries()` retourne `index_missing`. Le test poursuit malgré cet échec, puis exécute un `SELECT` sur une table inexistante.
>
> **Historique relevé par Claude.** Le garde-fou dans `index_entries()` aurait été ajouté dans `18be24a69`, après l'introduction du test dans `375d571b2`, sans adaptation de celui-ci. Le test est activé à la demande via `KB_TEST_PG_PORT` ; Claude indique n'avoir trouvé aucune référence à cette variable dans les configurations de CI inspectées.
>
> **Sévérité proposée : P3**, car le défaut concerne le test et non le comportement en production.
>
> **Correction proposée :**
>
> - Passer `collection_metadata` avec `schema_version: 1` et les autres métadonnées nécessaires aux écritures initiales.
> - Vérifier `error is None` et `count == 1` avant de poursuivre.
> - Ajouter un cas vérifiant qu'une collection absente, sans métadonnées de création, renvoie bien `index_missing`.
>
> **Limite de validation.** Claude n'a pas lancé de conteneur pgvector. La reproduction exécutée reste celle rapportée dans la review initiale.

## Ordre de traitement proposé par Claude

| Ordre | Points | Motif |
| --- | --- | --- |
| 1 | **1 — Collection avant bascule** | Éviter de basculer un bot en production sur un index vide. |
| 2 | **3 et 2 — Résultat de bascule et suivi des tâches** | Traiter ensemble le message de résultat, les échecs partiels et l'absence possible de `syncStatus`. |
| 3 | **4 — Sélection du diagnostic** | Garantir que le diagnostic interroge la collection demandée depuis la KB. |
| 4 | **5 et 6 — Lectures et suppressions ciblées** | Supprimer les parcours quadratiques, en incluant `bulk()`. |
| 5 | **7 — Test PGVector** | Rétablir le test sur base neuve et couvrir le refus de création sans métadonnées. |

[worker]: ../../../../../server/src/main/kotlin/service/KnowledgeBaseJobWorker.kt
[service]: ../../../../../server/src/main/kotlin/service/KnowledgeBaseService.kt
[board]: ../entries-board/entries-board.component.ts
[diagnostic]: ../../vector-store-inspection/diagnostic/diagnostic.component.ts
[python-service]: ../../../../../../../gen-ai/orchestrator-server/src/main/python/server/src/gen_ai_orchestrator/services/knowledge_base/knowledge_base_service.py
[python-tests]: ../../../../../../../gen-ai/orchestrator-server/src/main/python/server/tests/services/test_knowledge_base_service.py

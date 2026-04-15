## Rappel du besoin

- un dataset a un nom, une description, une liste de questions et un historique de runs,
- un run a un state, des dates, et un snapshot de settings,
- la page de détail compare surtout un run A et un run B,
- la comparaison se fait question par question,
- le rendu compare surtout la réponse texte et les sources (footnotes),
- côté front, `settingsSnapshot` n'est attendu que sur le détail dataset, pas sur la liste ni sur le polling de run.

---

## Connecteur de test

- tout passe par la logique du connecteur de test,
- chaque question est exécutée via l'équivalent interne de `POST /test/talk`,
- le worker force :
  - `sourceWithContent = true`,
  - `debug = true`.

---

## Proposition de modèle

## Dataset

```json
{
  "id": "datasetId",
  "namespace": "heymo-credit",
  "botId": "heymo",
  "name": "Résiliations & réclamations",
  "description": "Questions de régression sur le domaine assurance",
  "createdAt": "2026-03-06T10:00:00Z",
  "createdBy": "RS483",
  "updatedAt": "2026-03-06T10:00:00Z",
  "updatedBy": "RS483",
  "questions": [
    {
      "id": "questionId1",
      "question": "Quels sont les délais légaux pour résilier un contrat d’assurance habitation ?",
      "groundTruth": ""
    }
  ]
}
```

Notes :

- `groundTruth` non nullable côté API :
  - `""` = pas de ground truth renseignée
- validation back stricte :
  - `name` obligatoire
  - au moins 1 question,
  - max `X` questions via variable d'env,
  - question non vide
- update/delete refusés si un run est déjà `QUEUED` ou `RUNNING` sur le dataset

## DatasetRun

Exemple à la création du run :

```json
{
  "id": "runId",
  "datasetId": "datasetId",
  "state": "QUEUED",
  "createdAt": "2026-03-06T10:15:00Z",
  "startedBy": "RS483",
  "startTime": "2026-03-06T10:15:00Z",
  "endTime": null,
  "settingsSnapshot": {
    "...": "snapshot RagSettings"
  },
  "stats": {
    "totalQuestions": 12,
    "completedQuestions": 0,
    "failedQuestions": 0
  }
}
```

- `settingsSnapshot` est absent des réponses de `GET /datasets` et `GET /runs/:runId` (polling), mais présent dans `GET /datasets/:datasetId`,
- `settingsSnapshot` ne doit pas réutiliser tel quel le DTO RAG back existant :
  - il faut persister une projection dédiée,
  - sans secrets, en particulier sans `apiKey`,
- pour exécuter `/test/talk`, le worker doit disposer de la `locale` et du `botApplicationConfigurationId`,
- recommandation de contrat :
  - la `locale` est envoyée explicitement par le front dans `POST /runs`,
  - le serveur résout la configuration REST de test à partir du `botId`,
  - le `botApplicationConfigurationId` reste interne au run,
  - si la résolution de config est impossible ou ambiguë, le run est refusé

## DatasetRunQuestionResult

Modèle back, n'a pas vocation à être exposé tel quel au front.

```json
{
  "id": "runQuestionResultId",
  "runId": "runId",
  "questionId": "questionId1",
  "state": "PENDING",
  "startedAt": null,
  "endedAt": null,
  "userIdModifier": "dataset_runId_questionId1",
  "userActionId": null,
  "dialogId": null,
  "answerActionId": null,
  "error": null
}
```

Pourquoi stocker `dialogId` et `answerActionId` plutôt que l'action complète :

- la vraie réponse bot reste stockée dans les dialogs / actions Tock,
- le module datasets ne stocke ici que de quoi retrouver cette réponse plus tard,
- le front attend `action = null` si le dialog a été purgé plus tard,
- donc il vaut mieux relire l'`ActionReport` à la demande au moment du `GET /runs/:runId/actions`.

## States

### Run

- `QUEUED`
- `RUNNING`
- `COMPLETED`
- `CANCELLED`

Un run fini avec erreurs partielles reste `COMPLETED`.
Les erreurs vivent dans les résultats par question et dans `stats.failedQuestions`.

### Question result

- `PENDING`
- `RUNNING`
- `COMPLETED`
- `FAILED`
- `CANCELLED`

---

## Comportement d'exécution d'un run

Quand l'user clique sur "Run dataset" :

1. on charge le dataset,
2. on vérifie qu'aucun run n'est déjà `QUEUED` ou `RUNNING`,
3. on récupère la `locale` depuis la requête,
4. on résout la config REST de test effective à partir du `botId`,
5. si la résolution est impossible ou ambiguë, on refuse le lancement,
6. on snapshote les settings RAG courants,
7. on crée un `DatasetRun` en `QUEUED`,
8. on précrée les `DatasetRunQuestionResult` en `PENDING`,
9. le worker récupère le run,
10. il passe le run en `RUNNING`,
11. il exécute les questions une par une,
12. il met à jour la progression après chaque question,
13. à la fin il passe le run en `COMPLETED` ou `CANCELLED`.

Point important :

- en cas d'échec d'une question, le run continue,
- le run ne passe pas en `FAILED`.

---

## Une question = une action

- côté front et côté API de comparaison, une question correspond à une seule action résultat,
- on ne veut pas exposer un dialog complet ni un historique de conversation,
- côté back, l'implémentation peut malgré tout s'appuyer techniquement sur un dialog de test Tock pour produire cette action,
- on génère un `userIdModifier` unique par tentative,
- on exécute la question via la logique de `test/talk`,
- on récupère `userActionId` dans la réponse,
- puis on retrouve l'unique vraie réponse bot à rattacher à cette question.

Exemple :

```text
dataset_{runId}_{questionId}
```

### Point clé côté back

Le `userActionId` renvoyé par `/test/talk` n'est pas l'action bot à afficher dans la comparaison.

C'est l'action utilisateur injectée dans le dialog.

Il faut donc ensuite :

1. retrouver le dialog créé pour ce test user,
2. retrouver l'action utilisateur correspondante,
3. prendre la première action bot pertinente après cette action,
4. stocker son `actionId` dans `answerActionId`.

Règle simple de sélection :

- ignorer les actions debug,
- prendre en priorité une réponse `SentenceWithFootnotes`,
- sinon une `Sentence`,
- sinon la première action bot non debug disponible.

---

## Worker

Un composant de type `DatasetRunWorker` dans le back :

- scrute les runs `QUEUED`,
- en claim un de façon atomique,
- le passe en `RUNNING`,
- traite les questions une par une,
- met à jour la progression au fil de l'eau,
- termine le run.

```text
DatasetRun API -> save run QUEUED
DatasetRunWorker -> pick next QUEUED run
DatasetRunWorker -> set RUNNING
DatasetRunWorker -> execute question 1..N
DatasetRunWorker -> save question results
DatasetRunWorker -> set COMPLETED / CANCELLED
```

### Fiabilité

- stocker la progression après chaque question,
- avoir un retry configurable par question,
- prévoir un mécanisme de reprise simple pour éviter qu'un run reste bloqué si le worker meurt.

---

## Cancel

### Si le run est `QUEUED`

- il passe directement en `CANCELLED`,
- `endTime` est renseigné,
- il n'est jamais traité.

### Si le run est `RUNNING`

- l'API d'annulation doit renvoyer un run déjà `CANCELLED` avec `endTime` renseigné pour rester alignée avec le front,
- le worker termine au plus la question en cours,
- il ne lance pas la suivante.

---

## Stratégie d'échec

### Sur une question

- retry technique,
- puis si échec final :
  - `QuestionResult.state = FAILED`,
  - `error` renseigné,
  - on continue le run.

### Sur le run

- `run.state = COMPLETED` si le worker a fini son parcours,
- `run.state = CANCELLED` si annulation,

---

## Exposition API des résultats

Le endpoint `GET /bots/:botId/datasets/:datasetId/runs/:runId/actions` doit :

- relire les `DatasetRunQuestionResult`,
- pour chaque question :
  - si `FAILED` -> `action = null`,
  - si `COMPLETED` et action encore présente -> `action = ActionReport`,
  - si `COMPLETED` mais dialog/action purgé -> `action = null`.

À noter :

- il ne faut pas "recalculer" rétroactivement les anciens runs si le dataset change,
- si une question a été ajoutée après un ancien run, le front sait déjà gérer l'absence de résultat pour ce run historique.

---

## Guards back

Le back doit check :

- dataset vide,
- dataset avec plus de `X` questions,
- question vide,
- 1 run actif max par dataset,
- impossibilité de modifier ou supprimer un dataset pendant un run actif.
- `POST /runs` sans `language`,
- aucune configuration REST de test exploitable pour le `botId`,
- plusieurs configurations REST candidates si la résolution serveur n'est pas univoque.

---

## Forcing côté worker

Forcing côté worker :

- `sourceWithContent = true`
- `debug = true`

Ces deux flags doivent être considérés comme des invariants d'exécution dataset.

---

# Contrat d'interface — Datasets API

Le `namespace` est résolu côté serveur via le cookie de session — il n'apparaît pas dans les URLs.
Le `botId` correspond au `name` du bot courant, fourni par le `StateService` côté front.

Les URLs sont donc de la forme `/bots/:botId/datasets`.

Pour l'exécution d'un dataset :

- la `language` est fournie explicitement par le front au lancement du run,
- le serveur résout lui-même la configuration REST de test effective à partir du `botId`,
- si aucune configuration REST exploitable n'est trouvée, le run n'est pas créé.

---

## Types

```typescript
enum DatasetRunState {
  QUEUED = 'QUEUED',
  RUNNING = 'RUNNING',
  COMPLETED = 'COMPLETED', // Traitement terminé, sans préjuger du résultat des actions
  CANCELLED = 'CANCELLED' // Run interrompu volontairement avant complétion
}

enum DatasetRunActionState {
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}

interface DatasetQuestion {
  id: string;
  question: string;
  groundTruth: string;
}

// settingsSnapshot est absent des réponses de GET /datasets et GET /runs/:runId (polling)
// settingsSnapshot est présent dans les réponses de GET /datasets/:datasetId
interface DatasetRun {
  id: string;
  state: DatasetRunState;
  startTime: string; // ISO 8601
  endTime: string | null; // ISO 8601 — renseigné dès que state = COMPLETED | CANCELLED
  settingsSnapshot?: Partial<RagSettings>;
  startedBy: string;
}

interface Dataset {
  id: string;
  name: string;
  description: string;
  questions: DatasetQuestion[];
  runs: DatasetRun[];
  createdAt: string; // ISO 8601
  createdBy: string;
  updatedAt: string | null; // ISO 8601
  updatedBy: string | null;
}

interface DatasetRunAction {
  datasetId: string;
  runId: string;
  questionId: string;
  state: DatasetRunActionState;
  // null dans deux cas distincts à traiter différemment côté front :
  //   - state = FAILED   → l'action a échoué sans produire de rapport, comportement normal
  //   - state = COMPLETED → le dialogue associé a été purgé de la base, afficher un avertissement
  action: ActionReport | null;
  retryCount: number;
  error?: string | null;
}
```

---

## Endpoints

### `GET /bots/:botId/datasets`

Récupère la liste de tous les datasets du bot.

> Les runs sont retournés **sans** `settingsSnapshot` afin de limiter la taille des échanges.

**Response `200`**

```typescript
Dataset[] // runs[].settingsSnapshot absent
```

---

### `GET /bots/:botId/datasets/:datasetId`

Récupère un dataset complet. Utilisé par la vue détail pour permettre la comparaison
des settings entre runs.

> Les runs sont retournés **avec** `settingsSnapshot`.

**Response `200`**

```typescript
Dataset; // runs[].settingsSnapshot présent
```

**Response `404`** — dataset inconnu

---

### `POST /bots/:botId/datasets`

Crée un nouveau dataset.

**Request body**

```typescript
{
  name: string;
  description: string;
  questions: {
    question: string;
    groundTruth: string;
  }
  [];
}
```

**Response `201`**

```typescript
Dataset; // id généré, runs: [], settingsSnapshot non applicable
```

---

### `PUT /bots/:botId/datasets/:datasetId`

Met à jour un dataset (nom, description, questions). Les runs existants ne sont pas affectés.

**Request body**

```typescript
{
  name: string;
  description: string;
  questions: {
    id?: string; // présent = update, absent = création
    question: string;
    groundTruth: string;
  }[];
}
```

**Response `200`**

```typescript
Dataset; // runs[].settingsSnapshot absent
```

**Response `409`** — un run est déjà `QUEUED` ou `RUNNING` sur ce dataset

---

### `DELETE /bots/:botId/datasets/:datasetId`

Supprime un dataset et tous ses runs.

**Response `204`** — No content

**Response `409`** — un run est déjà `QUEUED` ou `RUNNING` sur ce dataset

---

### `POST /bots/:botId/datasets/:datasetId/runs`

Déclenche un nouveau run. Le serveur snapshote les RAG settings courants au moment du lancement.

> Un run ne peut être créé que si aucun run n'est déjà en état `QUEUED` ou `RUNNING`.

> Le run retourné est sans `settingsSnapshot`.

> Le serveur résout la configuration REST de test effective à partir du `botId`.
> Si la résolution est impossible, le run est refusé.

**Request body**

```typescript
{
  language: string; // locale courante côté front, ex: "fr"
}
```

**Response `201`**

```typescript
DatasetRun; // state: QUEUED, endTime: null, settingsSnapshot absent
```

**Response `409`** — un run est déjà actif

```typescript
{
  error: string;
}
```

**Response `400`** — aucune configuration REST exploitable, ou plusieurs configurations candidates

```typescript
{
  error: string;
}
```

---

### `POST /bots/:botId/datasets/:datasetId/runs/:runId/cancel`

Demande l'annulation d'un run en cours. Le run passe en état `CANCELLED` et son `endTime` est renseigné.
L'entrée est conservée en base avec ses actions partiellement exécutées.

Uniquement possible si le run est en état `QUEUED` ou `RUNNING`.

**Request body** — vide `{}`

**Response `200`**

```typescript
DatasetRun; // state: CANCELLED, endTime renseigné, settingsSnapshot absent
```

**Response `404`** — run inconnu

**Response `409`** — le run est déjà `COMPLETED` ou `CANCELLED`

---

### `GET /bots/:botId/datasets/:datasetId/runs/:runId`

Récupère l'état courant d'un run. Utilisé en polling tant que `state` est `QUEUED` ou `RUNNING`.

> Le polling doit s'arrêter dès que `state` passe à `COMPLETED` ou `CANCELLED`.
> `endTime` est alors renseigné.

> Le run retourné est sans `settingsSnapshot`.

**Response `200`**

```typescript
DatasetRun; // settingsSnapshot absent
```

**Response `404`** — run inconnu

---

### `GET /bots/:botId/datasets/:datasetId/runs/:runId/actions`

Récupère les résultats détaillés d'un run — une entrée par question du dataset.
Disponible uniquement pour un run en état `COMPLETED` ou `CANCELLED`.

Le champ `action` peut être `null` pour deux raisons distinctes, à distinguer via `state` :

| `state`     | `action` | Signification                              | Comportement attendu côté front           |
| ----------- | -------- | ------------------------------------------ | ----------------------------------------- |
| `FAILED`    | `null`   | L'action a échoué sans produire de rapport | Afficher l'état d'échec normalement       |
| `COMPLETED` | `null`   | Le dialogue associé a été purgé de la base | Afficher un avertissement à l'utilisateur |
| `COMPLETED` | présent  | Résultat nominal                           | Afficher le rapport                       |

**Response `200`**

```typescript
DatasetRunAction[]
```

**Response `404`** — run inconnu

**Response `409`** — run pas encore terminé

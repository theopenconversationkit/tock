# Gestion des Annotations et events - DERCBOT-1309

**Epic Jira** : [*DERCBOT-1309*](http://go/j/DERCBOT-1309)


## Contexte et objectif de la feature

Ce document de design définit la gestion des annotations et des events liés aux réponses du bot. L'objectif est d'offrir aux administrateurs et développeurs les outils nécessaires pour évaluer, annoter, et tracer les anomalies ainsi que leurs résolutions.

### Périmètre de la fonctionnalité
Les annotations permettent :
- Aux **administrateur de bot** de marquer une anomalie, de l’analyser et d’y associer des états et raisons spécifiques.
- Aux **administrateur de bot** de filtrer et de suivre les résolutions des anomalies.

## Cas d'usages

### En tant qu'administrateur de bot (rôle: botUser) :
* *UC1* - Je souhaite pouvoir **ajouter une annotation** sur une réponse du bot afin d’indiquer un problème.
* *UC2* - Je souhaite pouvoir **modifier une annotation existante** pour refléter les changements d’état, les raisons, ou ajouter des commentaires.
* *UC3* - Je souhaite **suivre l'historique des events liés à une annotation** comme les changements d'état et les commentaires, pour garder une trace complète des décisions.
* *UC4* - Je souhaite pouvoir **filtrer les réponses** en fonction des états et des raisons des anomalies pour identifier les cas nécessitant une attention immédiate.
* *UC5* - Je souhaite pouvoir **modifier** un commentaire existant.
* *UC6* - Je souhaite pouvoir **supprimer** un commentaire existant.
---

## Modèle de données

### Structure et stockage des annotations

Chaque annotation est un sous-document unique associé à une action spécifique (`actionId`) au sein d'un dialogue (`dialogId`).
- Une action ne peut contenir **qu’une seule annotation** à la fois.
- L’annotation est **nullable**, ce qui signifie qu’une action peut exister sans annotation.
- Lorsqu’une annotation est supprimée, elle est simplement retirée de l’action sans affecter les autres données du dialogue.
- La suppression ou la modification d’une annotation suit la même logique que celle appliquée aux dialogues (ex. expiration alignée sur la purge des dialogues).


```mermaid
classDiagram
    class Annotation {
        +state: AnnotationState
        +reason: AnnotationReasonType
        +description : String
        +ground_truth: String?
        +actionId: ObjectID
        +dialogId: String
        +events: Event[]
        +lastUpdateDate: DateTime
    }

    class AnnotationEvent {
        +eventId: ObjectID
        +creationDate: DateTime
        +lastUpdateDate: DateTime
        +user: String
        +type: EventType
    }

    class AnnotationEventComment {
        +comment: String
    }

    class AnnotationEventChange {
        +before: String?
        +after: String?
    }

    class AnnotationEventType {
        <<enumeration>>
        COMMENT
        STATE
        REASON
        GROUND_TRUTH
        DESCRIPTION
    }

    class AnnotationState {
        <<enumeration>>
        ANOMALY
        REVIEW_NEEDED
        RESOLVED
        WONT_FIX
    }

    class AnnotationReasonType {
        <<enumeration>>
        INACCURATE_ANSWER
        INCOMPLETE_ANSWER
        HALLUCINATION
        INCOMPLETE_SOURCES
        OBSOLETE_SOURCES
        WRONG_ANSWER_FORMAT
        BUSINESS_LEXICON_PROBLEM
        QUESTION_MISUNDERSTOOD
        OTHER
    }


    Annotation "1" *-- "many" AnnotationEvent : contains
    AnnotationEvent <|-- AnnotationEventComment : extends
    AnnotationEvent <|-- AnnotationEventChange : extends
    AnnotationEventType <-- AnnotationEvent : type
    AnnotationReasonType <-- Annotation : reason
    AnnotationState <-- Annotation : state
```

### Exemple de document stocké dans la collection :

Les events (`events`) sont toujours retournés dans l'ordre chronologique, triés par date.

Une purge sera mise sur les annotations, alignée sur la logique de purge des dialogs.

```json
{
  "_id": ObjectId("65a1b2c3d4e5f6a7b8c9d0e1"),
  "actionId": ObjectId("65a1b2c3d4e5f6a7b8c9d0e2"),
  "dialogId": "65a1b2c3d4e5f6a7b8c9d0e0",
  "state": "ANOMALY",
  "reason": "INACCURATE_ANSWER",
  "description": "La date donnée est incorrecte.",
  "ground_truth": "La date butoire de souscription au contrat est le 1er Janvier 2025",
  "events": [
    {
      "eventId": ObjectId("65a1b2c3d4e5f6a7b8c9d0e3"),
      "type": "STATE",
      "creationDate": ISODate("2023-10-01T10:00:00Z"),
      "lastUpdateDate": ISODate("2023-10-01T10:00:00Z"),
      "user": "USER192",
      "before": {
        "state": null
      },
      "after": {
        "state": "ANOMALY"
      }
    },
    {
      "eventId": ObjectId("65a1b2c3d4e5f6a7b8c9d0e4"),
      "type": "COMMENT",
      "creationDate": ISODate("2023-10-01T10:05:00Z"),
      "lastUpdateDate": ISODate("2023-10-01T10:05:00Z"),
      "user": "USER192",
      "comment": "La date donnée est incorrecte."
    },
    {
      "eventId": ObjectId("65a1b2c3d4e5f6a7b8c9d0e5"),
      "type": "STATE",
      "creationDate": ISODate("2023-10-01T11:00:00Z"),
      "lastUpdateDate": ISODate("2023-10-01T11:00:00Z"),
      "user": "ADMIN1",
      "before": {
        "state": "ANOMALY"
      },
      "after": {
        "state": "REVIEW_NEEDED"
      }
    }
  ],
  "createdAt": ISODate("2023-10-01T10:00:00Z"),
  "lastUpdateDate": ISODate("2023-10-01T11:00:00Z"),
}
```

# API Routes Documentation


**[POST] /rest/admin/bots/:botId/dialogs/:dialogId/actions/:actionId/annotation**

Crée une nouvelle annotation.
Un event de changement d'état est automatiquement créé pour passer de `null` à l'état initial `ANOMALY`.  
Une annotation ne peut pas être créée si une action existe déjà pour la même `actionId`.

**Path Parameter**
- `botId` : Identifiant unique du bot.
- `dialogId` : Identifiant unique du dialogue.
- `actionId` : Identifiant unique de l’action.

**Request Body:**

- `actionId`: Obligatoire
- `state`: Obligatoire
- `description`: Obligatoire
- `user`: Obligatoire
- `reason`: Facultatif
- `ground_truth`: Facultatif

**Corps:**
```json
{
  "state": "ANOMALY",
  "description": "Je teste la description",
  "reason": "INACCURATE_ANSWER",
  "groundTruth": "Est-ce que la GT est bien enregistrée?"
}
```
**Response:**
```json
{
  "_id": "679239da1b3e6329afa99ace",
  "actionId": "6791072cab5d311d16f0b884",
  "dialogId": "67910700ab5d311d16f0b872",
  "state": "ANOMALY",
  "reason": "INACCURATE_ANSWER",
  "description": "Je teste la description",
  "groundTruth": "Est-ce que la GT est bien enregistrée?",
  "events": [
    {
      "eventId": "679239da1b3e6329afa99acf",
      "creationDate": "2025-01-23T12:45:14.791551048Z",
      "lastUpdateDate": "2025-01-23T12:45:14.791553841Z",
      "user": "admin@app.com",
      "after": "ANOMALY",
      "type": "STATE"
    }
  ],
  "createdAt": "2025-01-23T12:45:14.791535053Z",
  "lastUpdateDate": "2025-01-23T12:45:14.791114801Z"
}
```

**[POST] /rest/admin/bots/:botId/dialogs/:dialogId/actions/:actionId/annotation/:annotationId/events**

Crée un nouvel event de type comment.

**Path Parameters** :
- `botId` : Identifiant unique du bot.
- `dialogId` : Identifiant unique du dialogue.
- `actionId` : Identifiant unique de l’action.
- `annotationId` : Identifiant unique de l'annotation.

**Request Body:**
- `type`: Type de l'event COMMENT
- `user`: Utilisateur ayant créé l'event.
- `comment`: Commentaire associé à l'event.

**Corps Example (COMMENT):**
```json
{
  "type": "COMMENT",
  "comment": "Je vérifie et reviens vers vous."
}
```

**Response Example (COMMENT):**
```json
{
  "eventId": "67923c551b3e6329afa99ad0",
  "creationDate": "2025-01-23T12:55:49.606860514Z",
  "lastUpdateDate": "2025-01-23T12:55:49.606864425Z",
  "user": "admin@app.com",
  "comment": "Je vérifie et reviens vers vous.",
  "type": "COMMENT"
}
```

**[PUT] /rest/admin/bots/:botId/dialogs/:dialogId/actions/:actionId/annotation**

Met à jour un event.  
On ne peut pas mettre à jour un event de type `comment`.

Une mise à jour de lastUpdateDate sera faite lors de chaque modification.
Une comparaison sera faite sur le back-end entre l'objet stocké sur Mongo et l'objet retourné par le front our déterminer les changements opérés.  

**Path Parameters** :
- `botId` : Identifiant unique du bot.
- `dialogId` : Identifiant unique du dialogue.
- `actionId` : Identifiant unique de l’action.
- `annotationId` : Identifiant unique de l'annotation.

**Corps Example**
```json
{
  "state": "RESOLVED"
}
```

**Response Example:**
```json
{
  "_id": "679239da1b3e6329afa99ace",
  "actionId": "6791072cab5d311d16f0b884",
  "dialogId": "67910700ab5d311d16f0b872",
  "state": "RESOLVED",
  "reason": "INACCURATE_ANSWER",
  "description": "Description V2",
  "groundTruth": "Est-ce que la GT est bien enregistrée?",
  "events": [
    {
      "eventId": "679239da1b3e6329afa99acf",
      "creationDate": "2025-01-23T12:45:14.791Z",
      "lastUpdateDate": "2025-01-23T12:45:14.791Z",
      "user": "admin@app.com",
      "after": "ANOMALY",
      "type": "STATE"
    },
    {
      "eventId": "67926623f119ce63cd8ba12b",
      "creationDate": "2025-01-23T15:54:11.413284007Z",
      "lastUpdateDate": "2025-01-23T15:54:11.413288338Z",
      "user": "admin@app.com",
      "before": "ANOMALY",
      "after": "RESOLVED",
      "type": "STATE"
    }
  ],
  "createdAt": "2025-01-23T12:45:14.791Z",
  "lastUpdateDate": "2025-01-23T15:54:11.413304611Z"
}
```

**[DELETE] /rest/admin/bots/:botId/dialogs/:dialogId/actions/:actionId/annotation/events/:eventId**

Supprime un event.  
On ne peut supprimer qu'un event de type `comment`.

**Path Parameter**
- `botId` : Identifiant unique du bot.
- `dialogId` : Identifiant unique du dialogue.
- `actionId` : Identifiant unique de l’action.
- `annotationId` : Identifiant unique de l'annotation.
- `eventId` : Identifiant unique de l'event.

**Response Example:**
```json
{
  "message": "Event deleted successfully"
}
```

The endpoint /dialogs/search will also reply with the action annotations.


### Sample d'utilisation :

**[POST] /rest/admin/bots/:botId/dialogs/:dialogId/actions/:actionId/annotation**

**Request Example:**
```json
{
  "actionId": "65a1b2c3d4e5f6a7b8c9d0e2",
  "state": "ANOMALY",
  "description": "La réponse donnée est incorrecte.",
  "user": "USER192",
  "reason": "INACCURATE_ANSWER"
}
```

**Response Example:**
```json
{
  "_id": "65a1b2c3d4e5f6a7b8c9d0e1",
  "actionId": "65a1b2c3d4e5f6a7b8c9d0e2",
  "dialogId": "65a1b2c3d4e5f6a7b8c9d0e0",
  "state": "ANOMALY",
  "description": "La réponse donnée est incorrecte.",
  "user": "USER192",
  "events": [],
  "createdAt": "2025-01-01T12:00:00Z",
  "lastUpdateDate": "2025-01-01T12:00:00Z"
}
```

**[POST] /rest/admin/bots/:botId/dialogs/:dialogId/actions/:actionId/annotation/:annotationId/events**

**Request Example:**
```json
{
  "type": "COMMENT",
  "user": "USER192",
  "comment": "L'erreur semble venir d'une mauvaise compréhension de la question."
}

```

**Response Example:**
```json
{
  "eventId": "65a1b2c3d4e5f6a7b8c9d0e3",
  "type": "COMMENT",
  "creationDate": "2025-01-01T12:05:00Z",
  "lastUpdateDate": "2025-01-01T12:05:00Z",
  "user": "USER192",
  "comment": "L'erreur semble venir d'une mauvaise compréhension de la question."
}
```

**[POST] /rest/admin/bots/:botId/dialogs/:dialogId/actions/:actionId/annotation/:annotationId/events**

**Request Example:**
```json
{
  "type": "STATE",
  "user": "ADMIN1",
  "before": {
    "state": "ANOMALY"
  },
  "after": {
    "state": "REVIEW_NEEDED"
  }
}
```

**Response Example:**
```json
{
  "eventId": "65a1b2c3d4e5f6a7b8c9d0e5",
  "type": "STATE",
  "creationDate": "2025-01-01T12:30:00Z",
  "lastUpdateDate": "2025-01-01T12:30:00Z",
  "user": "ADMIN1",
  "before": {
    "state": "ANOMALY"
  },
  "after": {
    "state": "REVIEW_NEEDED"
  }
}
```
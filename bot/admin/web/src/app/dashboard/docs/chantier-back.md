# Tableau de bord par bot — chantier back

Ce document décrit les aménagements back nécessaires au nouveau tableau de bord du Tock
Studio. Il s'adresse à l'équipe back et sert de base de discussion : les schémas de
collection et les contrats d'endpoint sont des **propositions à valider**, pas des
décisions figées.

Le front est maquetté et en grande partie développé. Il consomme aujourd'hui des mocks
pour tout ce qui n'existe pas encore côté back. Chaque section ci-dessous indique ce que
le front attend et sous quelle forme.

## Table des matières

1. [Ce qui ne demande aucun développement](#1-ce-qui-ne-demande-aucun-développement)
2. [About — identité du bot](#2-about--identité-du-bot)
3. [Contacts](#3-contacts)
4. [Historique du bot (timeline)](#4-historique-du-bot-timeline)
5. [Notes de session d'ingestion](#5-notes-de-session-dingestion)
6. [Sécurité — expurgation des snapshots](#6-sécurité--expurgation-des-snapshots)
7. [Récapitulatif des points à trancher](#7-récapitulatif-des-points-à-trancher)

---

## 1. Ce qui ne demande aucun développement

Avant de lister ce qu'il faut construire, voici ce qui est **déjà disponible** et que le
front consomme directement. Autant recentrer l'effort sur ce qui compte.

**Widget « Index de connaissances ».** Aucun développement. La PR #2086
(`vector-store-inspection`) expose déjà tout ce dont le widget a besoin :

`GET /gen-ai/bots/{botId}/vector-store/indexes` renvoie, pour chaque index :

```
{ indexName, indexSessionId, indexDatetime, documentCount, chunkCount, isCurrent }
```

Le widget prend l'entrée `isCurrent == true` et affiche date, volumétries et session. Le
cas d'anomalie « la session configurée n'existe pas dans le store » se déduit sans appel
supplémentaire : aucune entrée `isCurrent` ⇒ alerte. Le provider et l'embedding viennent
de la config RAG que le widget lit déjà.

**Widgets d'usage** (messages traités, retours, issue des réponses, sujets traités,
dernière évaluation). Déjà branchés sur les endpoints existants : `POST /dialogs/stats`,
`POST /bot/{applicationName}/metrics` (indicateurs `rag_status` et `rag_topics`),
`GET /bots/{botId}/evaluation-samples/`. Rien à faire.

---

## 2. About — identité du bot

Le nom technique d'un bot (son `botId`) diffère toujours du nom sous lequel l'assistant
se présente à l'utilisateur final, défini dans le prompt. Le tableau de bord veut afficher
ce nom métier, et une note libre décrivant le bot.

### Modélisation

Deux champs portés par l'application, à ajouter à `ApplicationDefinition` (ou une entité
associée à la paire namespace/botId — à votre appréciation) :

| Champ         | Type      | Rôle                                    |
| ------------- | --------- | --------------------------------------- |
| `displayName` | `String?` | Nom métier, tel qu'écrit dans le prompt |
| `notes`       | `String?` | Note libre : objet, public, périmètre   |

Le nom technique n'est **pas** stocké : il vient déjà de l'application.

### Endpoints

```
GET  /bots/{botId}/identity
PUT  /bots/{botId}/identity      (rôle : admin)
```

**Exemple de réponse `GET` :**

```json
{
  "displayName": "Léa",
  "notes": "Assistant pour les assurés habitation, exposé sur le portail client.\nHors périmètre : auto et santé.",
  "updatedAt": "2026-07-18T09:40:00Z",
  "updatedBy": "r.leroy"
}
```

**Exemple de corps `PUT` :**

```json
{
  "displayName": "Léa",
  "notes": "Assistant pour les assurés habitation, exposé sur le portail client.\nHors périmètre : auto et santé."
}
```

`updatedAt` / `updatedBy` sont renseignés côté serveur.

---

## 3. Contacts

Qui contacter au sujet d'un bot. Aujourd'hui inexistant. Le rôle est **texte libre** :
une liste fermée ne survivrait pas aux besoins qui émergeront. Le front propose des
suggestions (responsable métier, technique, etc.) mais accepte toute saisie.

### Modélisation

Une liste de contacts portée par l'application. Pas de table par contact.

```
BotContact {
  role: String        // libre
  name: String
  email: String?
  link: String?       // page d'équipe, canal, file de tickets
  note: String?       // quand les solliciter
  comment: String?    // commentaire libre
}
```

### Endpoints

```
GET  /bots/{botId}/contacts
PUT  /bots/{botId}/contacts      (rôle : admin)
```

Le `PUT` remplace la liste complète (le front édite contact par contact mais renvoie
l'ensemble, ce qui simplifie la concurrence).

**Exemple de réponse `GET` :**

```json
[
  {
    "role": "Responsable métier",
    "name": "Squad Assurance",
    "email": "squad-assurance@example.com",
    "note": "Questions de contenu et reformulations."
  },
  {
    "role": "Responsable technique",
    "name": "Plateforme conversationnelle",
    "email": "plateforme-conv@example.com",
    "link": "https://wiki.example.com/conv",
    "comment": "Astreinte publiée chaque lundi sur le wiki."
  }
]
```

---

## 4. Historique du bot (timeline)

Le vrai chantier. Une frise chronologique des événements marquants d'un bot : création,
connecteurs, ingestions, évaluations validées, et surtout les modifications de
configuration, dont chacune est inspectable (snapshot avant/après).

### 4.1 Principe d'interception

**Émission explicite au point d'écriture.** Chaque service admin qui persiste une
modification émet, dans la foulée, un événement d'historique. On ne s'appuie **pas** sur
les change streams Mongo : ils ne rejouent pas le passé, imposent un replica set, et ne
portent ni l'auteur ni l'intention.

Le motif existe déjà dans le code : `RAGAnswerHandler` fait exactement cela avec
`BotRepository.saveMetric(...)` à chaque réponse. On réplique cette approche pour les
événements de configuration.

**Pas de backfill.** L'historique démarre à la mise en production. Les événements
antérieurs ne sont pas reconstruits — décision assumée pour éviter un chantier de
rétro-alimentation sans grande valeur.

**Émission sur changement réel uniquement.** Un `save` qui ne modifie rien
fonctionnellement (l'utilisateur ouvre les settings et clique « Enregistrer » sans rien
toucher) ne doit **pas** générer d'entrée. Le service compare l'état à écrire au dernier
snapshot du même type et n'émet que s'ils diffèrent. Cette comparaison sert uniquement à
décider s'il faut écrire ; elle n'introduit pas de couplage durable.

### 4.2 Collection

```
bot_history_event {
  _id
  namespace: String
  botId: String
  type: String            // identifiant stable, voir 4.4
  date: Date
  params: Map<String, Any?>?   // valeurs d'interpolation pour le libellé (voir 4.3)
  author: String
  snapshot: Snapshot?     // présent seulement sur les événements de config (voir 4.5)
}
```

Index recommandé : `{namespace, botId, date}`. L'endpoint de lecture doit être **paginé** :
un bot ingéré chaque semaine produit ~150 événements/an, la frise remonte plusieurs
années.

### 4.3 Événements typés, pas de libellés

**Le back n'émet jamais de libellé lisible.** Il émet un `type` stable et un objet
`params` de valeurs brutes. Le front résout la traduction (`dashboard.history.event.<type>.label`
et `.detail`) et interpole `params`.

Raison : l'application est internationalisée. Un libellé figé à l'écriture, il y a deux
ans, par un back qui ignore la locale du lecteur, s'afficherait dans la mauvaise langue et
avec un formatage numérique erroné. La logique d'identifiants mappés côté front est le
motif standard (cf. les codes d'erreur applicatifs). Le couplage introduit est léger et
**explicite** : un ensemble fini de types, et pour chacun un contrat sur les clés de
`params`.

**Contrat des `params` par type** (à figer et versionner) :

| type             | clés de `params`                    |
| ---------------- | ----------------------------------- |
| `created`        | —                                   |
| `connector`      | `connector`, `label`                |
| `evaluation`     | `positiveRate`, `dialogCount`       |
| `rag-settings`   | — (détail rendu depuis le snapshot) |
| `vector-store`   | —                                   |
| `compressor`     | —                                   |
| `observability`  | —                                   |
| `prompt-context` | —                                   |

Règle d'évolution : on n'ajoute une clé sans risque (le front l'ignore tant qu'il ne
l'utilise pas) ; on ne **retire** jamais une clé sans coordination front.

### 4.4 Périmètre des types d'événements

| type             | déclencheur                                          | snapshot ? |
| ---------------- | ---------------------------------------------------- | ---------- |
| `created`        | création du bot                                      | non        |
| `connector`      | ajout/config d'un connecteur                         | non        |
| `evaluation`     | validation d'un échantillon d'évaluation             | non        |
| `rag-settings`   | `save` de `BotRAGConfiguration`                      | **oui**    |
| `vector-store`   | `save` de `BotVectorStoreConfiguration` (BDD dédiée) | **oui**    |
| `compressor`     | `save` de la config compresseur                      | **oui**    |
| `observability`  | `save` de la config observabilité                    | **oui**    |
| `prompt-context` | `save` de la config `business-rules`                 | **oui**    |

**Point important sur `rag-settings`.** Tout le document `BotRAGConfiguration` vit dans une
seule collection, un seul point d'écriture, **prompts et `indexSessionId` compris**. Un
`save` produit donc **un seul** événement, dont le snapshot contient l'intégralité du
document. Rien n'est exclu du snapshot — un journal d'audit ne masque pas d'information.

Conséquences pour le back, qui simplifient le travail :

- pas de type `prompt-change` séparé : les deux prompts (`questionCondensingPrompt`,
  `questionAnsweringPrompt`) font partie du snapshot RAG. Le front les affiche en diff
  textuel, le reste en diff clé/valeur.
- pas de type `ingestion` séparé : une nouvelle ingestion se traduit par un changement
  d'`indexSessionId` dans les RAG settings, donc un événement `rag-settings` ordinaire.
- la mise en évidence « le corpus a changé » est **entièrement dérivée côté front** à
  partir du diff d'`indexSessionId` entre les deux snapshots. **Rien de spécial à émettre
  côté back.**

**Points d'écriture à instrumenter :** `RAGService.save`, la config vector-db, la config
compresseur, la config observabilité, la config business-rules. Cinq points, tous déjà
centralisés dans les services admin.

### 4.5 Snapshots

Le snapshot capture l'état complet de la config au moment du changement, **pas un diff** :
un diff stocké dépend de son prédécesseur pour être lu et casse si un maillon manque. Un
snapshot complet est autonome ; le diff se calcule à la lecture, côté front, en comparant
au snapshot du changement de même type précédent.

Structure :

```
Snapshot {
  previous: Map<String, Any?>?   // null pour la première modif d'un type
  current: Map<String, Any?>
}
```

`previous` est le `current` de l'événement précédent de même type. Pour le premier
changement d'un type, `previous` est `null` : le front affiche l'état seul, sans diff.

**Format `Map<String, Any?>`**, comme le précédent des runs de datasets (voir §6). Le
snapshot est expurgé des secrets **avant** stockage — non négociable, détaillé en §6.

### 4.6 Endpoint

```
GET /bots/{botId}/history?before={date}&limit={n}
```

**Exemple de réponse :**

```json
{
  "events": [
    {
      "id": "6a1f...",
      "date": "2026-08-22T14:03:00Z",
      "type": "rag-settings",
      "author": "m.bekkari",
      "snapshot": {
        "previous": {
          "maxDocumentsRetrieved": 4,
          "documentsRequired": false,
          "documentSearchType": "HYBRID_SEARCH",
          "indexSessionId": "8f14e45f-ceea-4a5b-9c2f-3d1b70e12a44",
          "questionAnsweringPrompt": { "template": "Tu es l'assistant..." }
        },
        "current": {
          "maxDocumentsRetrieved": 6,
          "documentsRequired": true,
          "documentSearchType": "HYBRID_SEARCH",
          "indexSessionId": "8f14e45f-ceea-4a5b-9c2f-3d1b70e12a44",
          "questionAnsweringPrompt": { "template": "Tu es l'assistant..." }
        }
      }
    },
    {
      "id": "5c02...",
      "date": "2026-06-28T10:00:00Z",
      "type": "evaluation",
      "author": "m.bekkari",
      "params": { "positiveRate": 88, "dialogCount": 210 }
    }
  ],
  "hasMore": true
}
```

Noter l'absence de tout champ `label`/`detail` : le rendu est entièrement côté front.

---

## 5. Notes de session d'ingestion

Le widget « Index de connaissances » permet d'attacher une note libre à une session
d'ingestion (sources, options du pipeline, exclusions délibérées). Une table pour un seul
champ texte serait disproportionnée, mais loger la note dans l'entrée d'historique pose
deux problèmes : l'historique est immuable alors qu'une note s'édite, et toutes les
sessions n'ont pas d'entrée d'historique (démarrage à la MEP, ingestions hors `save`).

**Solution : une collection légère d'annotations, clé = session d'indexation.** Ce n'est
pas « une table pour un champ » : c'est une table d'annotations dont la clé est un objet
métier réel et durable.

### Collection

```
bot_index_session_note {
  namespace: String
  botId: String
  indexSessionId: String
  text: String
  updatedAt: Date
  updatedBy: String
}
```

Clé unique : `{namespace, botId, indexSessionId}`.

**Notes conservées après purge de la session.** Quand une session disparaît du vector
store, sa note **subsiste**. Elle acquiert alors une valeur de trace historique : elle
documente un corpus qui n'existe plus, ce qui est précisément utile. Pas de purge en
cascade.

### Endpoints

```
GET  /bots/{botId}/index-sessions/{indexSessionId}/note
PUT  /bots/{botId}/index-sessions/{indexSessionId}/note      (rôle : admin)
```

**Exemple de réponse `GET` :**

```json
{
  "indexSessionId": "8f14e45f-ceea-4a5b-9c2f-3d1b70e12a44",
  "text": "Sources : portail-client (crawl prof. 3), export FAQ du 04/07, glossaire interne.\nQallam : chunk 1200/150, captioning on, images ignorées.",
  "updatedAt": "2026-08-11T09:40:00Z",
  "updatedBy": "r.leroy"
}
```

**Exemple de corps `PUT` :**

```json
{ "text": "Sources : portail-client (crawl prof. 3), export FAQ du 04/07, glossaire interne." }
```

### Jointures à la lecture

La note se raccroche à ses consommateurs par `indexSessionId`, sans couplage d'écriture :

- le widget « Index de connaissances » lit la note de la session courante
- la future vue d'exploration lira la note de chaque session listée par `/indexes`
- la modale d'un événement `rag-settings` dont le corpus a changé peut afficher la note de
  la session concernée

---

## 6. Sécurité — expurgation des snapshots

**Point non négociable.** Les configs RAG et vector store contiennent des `LLMSetting` /
`EMSetting` porteurs de **clés d'API**. Un snapshot naïf figerait ces secrets en clair dans
une collection d'historique jamais purgée, servie à tout utilisateur `admin`. Une entrée
d'historique étant immuable, on ne pourrait pas « corriger » la fuite après coup.

**Les snapshots sont expurgés des secrets avant écriture, récursivement.** Un filtre de
premier niveau ne suffit pas : les clés sont imbriquées dans les sous-objets
`questionAnsweringLlmSetting`, `emSetting`, etc.

**Précédent à réutiliser.** `DatasetService` fait déjà exactement cela pour le snapshot de
settings des runs de datasets :

```kotlin
private fun buildSettingsSnapshot(namespace: String, botId: String): Map<String, Any?> {
    val ragConfiguration = ragConfigurationDAO.findByNamespaceAndBotId(namespace, botId)
        ?: return emptyMap()
    val rawSnapshot = objectMapper.convertValue(BotRAGConfigurationDTO(ragConfiguration), Map::class.java)
    return sanitizeSnapshot(rawSnapshot) as Map<String, Any?>
}

private fun sanitizeSnapshot(value: Any?): Any? =
    when (value) {
        is Map<*, *> -> value.entries
            .filter { it.key != "apiKey" }
            .associate { (k, v) -> k.toString() to sanitizeSnapshot(v) }
        is Iterable<*> -> value.map { sanitizeSnapshot(it) }
        else -> value
    }
```

À vérifier avant réutilisation : que `apiKey` est bien le **seul** nom de champ sensible
sur l'ensemble des configs concernées (RAG, vector store, compresseur, observabilité). Le
`prompt-context` (`business-rules`) ne contient que des listes de sujets — aucun secret,
expurgation triviale.

**Conséquences assumées de l'expurgation** (à documenter côté front, mais qui découlent du
back) :

- un snapshot expurgé n'est **pas restaurable** : c'est une trace de lecture, pas une
  sauvegarde.
- un changement portant **uniquement** sur une clé d'API produit un diff **vide** côté
  front : la clé étant absente des deux snapshots, aucune différence n'est visible. C'est
  l'angle mort acceptable, très préférable à la moindre fuite.

---

## 7. Récapitulatif des points à trancher

Les décisions déjà prises avec l'équipe front, listées pour mémoire :

- historique démarré à la MEP, pas de backfill
- snapshot complet expurgé, pas de diff stocké
- diff calculé à la lecture contre le changement de même type précédent
- émission sur changement réel uniquement
- notes de session conservées après purge

Les points restant à valider côté back :

1. **Rattachement de `displayName`/`notes` et des contacts** : sur `ApplicationDefinition`
   directement, ou une entité associée ? (impact migration)
2. **`apiKey` est-il le seul champ sensible** sur l'ensemble des configs snapshotées, ou
   faut-il étendre la liste des clés expurgées ?
3. **Granularité de l'émission** : la comparaison au dernier snapshot pour décider
   d'émettre est-elle acceptable en performance au point d'écriture, ou préférez-vous
   émettre systématiquement quitte à avoir des entrées « vides » filtrables ?
4. **Pagination de l'historique** : curseur sur `date`, ou offset classique ?
5. **Purge éventuelle** de `bot_history_event` : prévoit-on une rétention maximale, ou
   l'historique est-il conservé indéfiniment ?

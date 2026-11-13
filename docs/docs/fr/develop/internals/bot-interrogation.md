---
title: Interrogation d'un bot
description: "Comment un message arrive jusqu'au bot : points d'entrée (Studio, connecteurs), story RAG, protocoles HTTP et SSE."
---

# Interrogation d'un bot : points d'entrée, story RAG et protocoles HTTP/SSE

> Objectif : comprendre **comment un message arrive jusqu'au bot**,
> les **deux points d'entrée** (Studio et connecteurs), le focus sur la **story RAG**, et la
> différence entre les protocoles **HTTP classique** et **SSE (streaming)**.
>
> Les références de code désignent les fichiers du dépôt ; les numéros de ligne sont volontairement omis.

---

## 1. Vue d'ensemble

Quel que soit le canal d'entrée, tous les messages convergent vers le **même moteur** :

```
                 ┌─────────────────────────────────────────────┐
  Studio  ─────► │                                             │
  (test)         │   ConnectorController.handle / handleUserEvent│
                 │          (TockConnectorController)           │
 Connecteurs ──► │                    │                         │
  (web, etc.)    │                    ▼                         │
                 │   Bot.handleAction → NLP → getStory → story.handle │
                 └─────────────────────────────────────────────┘
```

Point de convergence : `TockConnectorController.handleAction(...)`
(`bot/engine/src/main/kotlin/engine/TockConnectorController.kt`) qui appelle
`Bot.handleAction(...)` (`bot/engine/src/main/kotlin/Bot.kt`). C'est là que se font :

- le **parsing NLP** de la phrase (`nlp.parseSentence(...)`, `Bot.kt`),
- la **résolution de la story** (`getStory(...)` → `botDefinition.findStoryDefinition(...)`),
- l'**exécution** de la story (`story.handle(bus)`, `Bot.kt`).

La différence entre les deux points d'entrée se situe **avant** ce tronc commun : c'est la
manière dont le message HTTP est reçu et transformé en `Event`/`Action`.

---

## 2. Point d'entrée n°1 — Via le Tock Studio (fenêtre « Test the bot »)

C'est la zone de dialogue de test de l'interface d'administration.

**Point clé et contre-intuitif :** le Studio **n'appelle PAS le moteur en direct (in-process)**.
Il fait un **vrai appel HTTP sortant** vers le **connecteur REST** du bot en cours d'exécution.
Un connecteur REST est d'ailleurs ajouté automatiquement à la création de chaque bot
(`bot/admin/server/src/main/kotlin/BotAdminVerticle.kt`, `addRestConnector(...)`).

### Flux détaillé

```
Studio UI (Angular)
  POST /test/talk                          test-dialog.component.ts
    → TestCoreService (route admin)        TestCoreService.kt   (blockingJsonPost, sécurisé botUser)
    → TestTalkService.talk()               TestTalkService.kt
    → ConnectorRestClient.talk()           ConnectorRestClient.kt   ───► HTTP (Retrofit)
    → POST <path>/:locale                  RestConnector.kt         (webhook du connecteur REST)
    → controller.handle(action, data)      RestConnector.kt
    → TockConnectorController.handleAction TockConnectorController.kt
    → Bot.handleAction → NLP → story       Bot.kt
```

### Étapes

1. **Front** : le composant `test-dialog.component.ts` (`talkRequest`) envoie un
   `POST /test/talk` avec un corps `BotDialogRequest`.
2. **Route admin** : `TestCoreService.registerServices()` monte la route
   `POST /test/talk` (`TestCoreService.kt`), vérifie le namespace, puis appelle
   `TestTalkService.talk(...)`.
3. **Pont vers le bot** : `TestTalkService` (`TestTalkService.kt`) :
   - charge la `BotApplicationConfiguration` (avec contrôle d'autorisation sur le namespace),
   - récupère un `ConnectorRestClient` (en cache) pointant sur le `baseUrl` du bot
     (ou `tock_bot_admin_rest_default_base_url` par défaut),
   - construit des **identifiants de test synthétiques** (`buildTestPlayerId` →
     `test_<confId>_<locale>_<userIdModifier>`),
   - positionne `test = true`, `debugEnabled`, `sourceWithContent`,
   - appelle `restClient.talk(path, locale, ClientMessageRequest(...))`.
4. **Connecteur REST (côté bot)** : `RestConnector.register()` expose la route
   `POST <path>/:locale` (`RestConnector.kt`). Le handler parse le `MessageRequest`,
   crée un `RestConnectorCallback` (avec le `testBehaviour` du bot quand `test=true`), puis
   entre dans le moteur via `controller.handle(...)`.

> **Conséquence pratique :** pour que la fenêtre de test du Studio fonctionne, le bot doit
> tourner et son connecteur REST doit être joignable depuis le serveur d'admin. C'est de
> l'HTTP classique requête/réponse (bloquant) — **pas de streaming** sur ce chemin.

---

## 3. Point d'entrée n°2 — Via les connecteurs (canaux externes)

Chaque canal (web, Messenger, Slack, WhatsApp, Teams, Google Chat, OpenAI…) implémente
l'interface `ai.tock.bot.connector.Connector` et enregistre **son propre webhook HTTP**
via `controller.registerServices(path) { router -> ... }`.

### Exemple : le connecteur Web

```
Canal externe (widget, app…)
  POST <path>                              WebConnector.kt (handler principal)
    → WebConnector.handleRequest()         WebConnector.kt (désérialise WebConnectorRequest)
    → WebConnector.handleEvent()           WebConnector.kt (construit WebConnectorCallback)
    → controller.handleUserEvent(event)    WebConnector.kt
    → TockConnectorController.handleAction TockConnectorController.kt
    → Bot.handleAction → NLP → story       Bot.kt
```

- `register()` (`WebConnector.kt`) monte la route principale `POST <path>`, plus éventuellement les routes SSE (voir §5).
- `handleRequest()` désérialise le corps en `WebConnectorRequest`, résout le `connectorId`
  (avec option « web bridge »), convertit en `Event` (`request.toEvent(...)`), puis appelle
  `handleEvent()`.
- `handleEvent()` construit un `WebConnectorCallback` et le passe à
  `controller.handleUserEvent(...)`.

Les autres connecteurs suivent le même patron : *webhook → parsing du payload du canal →
`ConnectorCallback` spécifique → `controller.handle(...)`.* La spécificité d'un connecteur,
c'est uniquement le **format d'entrée/sortie** et le **callback** ; le cœur moteur est partagé.

### Différence clé entre les deux points d'entrée

| | **Studio (§2)** | **Connecteur (§3)** |
|---|---|---|
| Déclencheur | Dev/testeur dans l'UI admin | Utilisateur final sur un canal |
| Transport | HTTP sortant admin → connecteur REST | Webhook entrant du canal |
| Identité | IDs de test synthétiques, `test=true` | Vrai `userId` du canal |
| Comportement | `testBehaviour` appliqué | Comportement de production |
| Moteur | **Identique** (`Bot.handleAction`) | **Identique** (`Bot.handleAction`) |

---

## 4. Focus — La Story RAG (Retrieval Augmented Generation)

La story RAG est la story qui répond par **génération IA** (LLM + recherche documentaire)
lorsqu'aucune story configurée ne correspond.

### 4.1 Définition et déclenchement

- `RAGStoryDefinition` (`bot/engine/src/main/kotlin/definition/RAGStoryDefinition.kt`) est une
  story dont le **starter intent est `Intent.unknown`** : elle **remplace la story de
  fallback** (`unknown`). Identifiant : `tock_rag_story`.
- La décision d'aiguillage est dans `BotDefinition.findStoryDefinition(...)`
  (`bot/engine/src/main/kotlin/definition/BotDefinition.kt`) :

  ```kotlin
  else -> (if (ragConfiguration?.enabled == true) ragStory else null) ?: unknownStory
  ```

  Autrement dit : si l'intent entrant **ne correspond à aucune story configurée**
  (et n'est ni `keyword` ni `ragexcluded`), le bot bascule sur la **story RAG**
  **uniquement si `ragConfiguration.enabled == true`** ; sinon il garde la story `unknown`.
- Il existe aussi une story dédiée `tock_ragexcluded_story` (intent `ragexcluded`) qui
  **court-circuite** le RAG pour renvoyer une réponse figée.
- La configuration RAG est tenue à jour à chaud par
  `BotRAGConfigurationMonitor` (`bot/engine/src/main/kotlin/engine/config/BotRAGConfigurationMonitor.kt`),
  qui recharge `ragConfiguration` depuis la base quand elle change.

### 4.2 Le handler RAG

- `RAGAnswerHandler` (`bot/engine/src/main/kotlin/engine/config/RAGAnswerHandler.kt`),
  un `AbstractProactiveAnswerHandler`.
- Son point d'entrée `handleProactiveAnswer(botBus)` (`RAGAnswerHandler.kt`) :
  1. enregistre une métrique `STORY_HANDLED`,
  2. appelle `rag(this)` (`RAGAnswerHandler.kt`) qui construit la `RAGRequest` et interroge
     l'orchestrateur Gen AI,
  3. construit les **footnotes** (sources documentaires),
  4. envoie un `SendSentenceWithFootnotes` avec
     `ActionMetadata(isGenAiRagAnswer = true, observabilityInfo = ...)`
     (`RAGAnswerHandler.kt`),
  5. renvoie éventuellement une **redirection** vers une autre story
     (`ragStoryRedirection`, si l'orchestrateur suggère un `redirectionIntent`).
- L'exécution passe par `AbstractProactiveAnswerHandler.handle(...)` dans un
  `executor.executeBlocking { ... }` → **exécution toujours bloquante** du point de vue du
  thread moteur. En mode streaming (voir §4.4), c'est un `CountDownLatch` qui attend la fin
  du flux SSE de l'orchestrateur : les fragments de texte sont transmis au client **pendant**
  cette attente.

### 4.3 Appel au serveur d'orchestration Gen AI

Le moteur **n'appelle jamais un LLM directement**. Il passe par un client Retrofit du module
`gen-ai/orchestrator-client`, qui appelle le **Gen AI Orchestrator Server** (serveur Python) :

```
RAGAnswerHandler.rag()                     RAGAnswerHandler.kt
  → RAGService.rag(query, debug)           (interface, injectée)
  → RAGServiceImpl.rag()                    RAGServiceImpl.kt   → ragApi.rag(...).execute().body()
  → POST /rag                               RAGApi.kt ───► HTTP (Retrofit, BLOQUANT)
  → Gen AI Orchestrator Server (Python)    rag_router.py  @post('') → RAGResponse (JSON complet)
```

- Interface HTTP : `@POST("/rag") fun rag(...): Call<RAGResponse>` (`RAGApi.kt`).
- Implémentation **synchrone** : `ragApi.rag(query, debug).execute().body()`
  (`RAGServiceImpl.kt`) → **une seule réponse JSON complète**, pas de flux.
- Client HTTP : `GenAIOrchestratorClient` — URL de base
  `tock_gen_ai_orchestrator_server_url` (défaut `http://localhost:8000`), timeout
  `tock_gen_ai_orchestrator_client_request_timeout_ms` (défaut 55 000 ms).
- La `RAGRequest` (`gen-ai/orchestrator-client/.../requests/RAGRequest.kt`) embarque notamment :
  l'historique du dialogue (`getDialogHistory`, N derniers messages), les settings LLM
  (condensation de question + réponse), les prompts, le setting d'embedding, l'index
  documentaire (`documentIndexName`), les paramètres de recherche, le vector store, et le
  setting d'**observabilité** (Langfuse).
- La `RAGResponse` contient `answer` (le texte LLM), `footnotes` (sources), `debug` et
  `observabilityInfo` (lien de trace Langfuse, réécrit en URL publique par
  `updateObservabilityInfo`, `RAGAnswerHandler.kt`).

**Chemin streaming** (quand `action.metadata.streamedResponse == true`) :

```
RAGAnswerHandler.ragStream()
  → RAGServiceImpl.ragStream(listener)
  → BackgroundEventSource                  (bibliothèque LaunchDarkly, contourne RAGApi)
  → POST /rag/stream                       ───► SSE vers le Gen AI Orchestrator Server
  ← event: token (×N)
    → listener.onToken(token)
    → callback.sendStreamedToken(token.content)  [hook du ConnectorCallback]
  ← event: answer (terminal)
    → listener.onAnswer(response)
    → finalResponse = RAGResponse
  → RAGResult (retourné à handleProactiveAnswer)
  → send(SendSentenceWithFootnotes)
```

Le chemin streaming utilise `BackgroundEventSource` directement, et non le client Retrofit
`RAGApi` (qui ne gère que des réponses JSON uniques). Les fragments arrivent au fil de l'eau
et sont confiés au hook `ConnectorCallback.sendStreamedToken` ; l'événement `answer` terminal
contient le `RAGResponse` complet (footnotes, observabilité, debug), exactement comme en mode
classique.

### 4.4 Point important sur le streaming RAG

L'orchestrateur Gen AI dispose de **deux endpoints** pour répondre aux questions RAG :

1. **Classique `POST /rag`** : renvoie une **réponse JSON unique et complète**. Le moteur
   l'appelle via le client Retrofit (`RAGApi.rag()`, `RAGServiceImpl.rag()`). C'est le chemin
   par défaut (`action.metadata.streamedResponse == false`).

2. **Streaming `POST /rag/stream`** : renvoie un **flux SSE**. Quand
   `action.metadata.streamedResponse == true`, `RAGAnswerHandler.rag()` appelle
   `ragStream(botBus)`. Le serveur Python émet :
   - `event: token` (×N) : chaque fragment de texte généré par le LLM ;
   - `event: answer` (terminal) : le `RAGResponse` complet (texte final, footnotes, debug,
     observabilité).

   Chaque fragment reçu est transmis au connecteur par `callback.sendStreamedToken(...)`.
   C'est un **hook** de `ConnectorCallback`, sans effet par défaut. Chaque connecteur décide
   quoi en faire :
   - **web** : le transforme en trame sur le canal SSE `/sse?userid=` (voir §5.3) ;
   - **REST** (route `<path>/:locale/stream`) et **Google Chat** : l'utilisent directement
     pour leur propre streaming ;
   - les autres connecteurs l'ignorent.

**Qui positionne `streamedResponse` ?** Le flag est porté par l'action utilisateur
(`ActionMetadata.streamedResponse`). Le connecteur REST le force sur la route `/stream`, Google
Chat le positionne selon son paramètre `enableStreaming`, et le connecteur web le positionne
lui-même quand un canal SSE du client est connecté sur l'instance (§5.3). Le client web n'a
donc rien à envoyer.

**Remarque sur l'exécution** : les deux chemins tournent dans `executor.executeBlocking { }`.
En streaming, un `CountDownLatch` bloque le thread moteur jusqu'à la fin du flux de
l'orchestrateur. La réponse finale (`SendSentenceWithFootnotes`) n'est émise qu'après le retour
de `ragStream()`. C'est la seule action du domaine : les fragments ne sont ni des actions ni
persistés.

### 4.5 Côté admin : configuration (à ne pas confondre)

`bot/admin/server/src/main/kotlin/service/RAGService.kt` **ne fait pas** la réponse au
runtime : il gère uniquement la **configuration** de la story RAG
(`getRAGConfiguration`, `saveRag`, `deleteConfig`…), avec validation contre les services
providers de l'orchestrateur. À distinguer du `RAGService` du module `orchestrator-client`
qui, lui, est le **client runtime** d'appel au LLM.

---

## 5. Protocoles : HTTP classique vs SSE (streaming)

### 5.1 Rappel — qu'est-ce que SSE ?

**SSE (Server-Sent Events)** est un protocole de **streaming unidirectionnel serveur → client**,
au-dessus d'HTTP. La connexion reste **ouverte** (`Content-Type: text/event-stream`,
transfert *chunked*, `keep-alive`) et le serveur pousse des évènements au format
`event: message\ndata: <payload>\n\n` au fil de l'eau. À opposer à l'HTTP classique
**requête → une seule réponse → fermeture**.

Primitives bas niveau (Vert.x), partagées par tous les connecteurs :
`shared/src/main/kotlin/vertx/VertXs.kt` :

- `HttpServerResponse.setupSSE(...)` : passe la réponse en *chunked*, pose les
  en-têtes SSE (`text/event-stream`, `keep-alive`, `no-cache`, `X-Accel-Buffering: no`) et
  démarre un ping de keep-alive.
- `sendSseMessage(data)` : écrit `event: message\ndata: $data\n\n`.
- `sendSsePing()` : écrit un `event: ping`.

### 5.2 Comment Tock choisit entre HTTP et SSE

Le streaming n'est **pas un transport séparé** : il est **piloté par des métadonnées** qui
circulent dans le moteur.

- `MetadataEvent.STREAM_RESPONSE_METADATA = "TOCK_STREAM_RESPONSE"` et le helper
  `Map<String,String>.hasStreamMetadata()` (`MetadataEvent.kt`).
- `ActionMetadata.streamedResponse` et `ActionMetadata.lastAnswer` (`ActionMetadata.kt`).
- Côté Bot API, un handler peut appeler `enableStreaming()` / `disableStreaming()`
  (`TockClientBus.kt`) pour émettre ces métadonnées, puis envoyer des `SendSentence`
  partiels, le dernier portant `lastAnswer = true`.

### 5.3 Le connecteur Web : 3 modes

`bot/connector-web/src/main/kotlin/WebConnector.kt` : deux variables de configuration :

```kotlin
private val sseEnabled       = booleanProperty("tock_web_sse", false)         // canal SSE persistant
private val directSseEnabled = booleanProperty("tock_web_direct_sse", false)  // stream de la réponse d'un POST
```

| Mode | Déclenchement | Route | Transport | Usage |
|---|---|---|---|---|
| **HTTP classique** | défaut | `POST <path>` | 1 requête → 1 réponse `application/json` | Le plus simple, bufferisé |
| **Canal SSE persistant** | `tock_web_sse=true` | `GET <path>/sse?userid=` | Connexion SSE ouverte en continu (`EventSource`) | Réponses poussées, notifications *push*, **streaming RAG** |
| **SSE direct** | `tock_web_direct_sse=true` | `POST <path>/sse/direct` | La réponse du POST est streamée en SSE | Route historique, sans streaming RAG par fragments |

Détails :

- **HTTP classique** : `WebConnectorCallback.sendResponse()` renvoie toute la réponse d'un coup
  (`application/json`).
- **Canal SSE persistant** : module dédié `bot/connector-web-sse`.
  - `SseEndpoint.configureRoute(...)` monte `GET <path>/sse`, exige un `userId` (contexte
    sécurisé ou `?userid=`) et enregistre un canal SSE.
  - `SseChannels` : registre en mémoire **plus repli base de données**. Si aucun canal local
    n'existe, l'évènement est **persisté** (`ChannelMongoDAO`, collection *capped* MongoDB +
    *change stream*) pour qu'une **autre instance** (celle qui détient la connexion du client)
    le pousse. C'est ce qui fait fonctionner le SSE en **cluster horizontal**.
  - `notify(...)` côté `WebConnector` lève une exception **sauf si `sseEnabled`**.
  - Avec `tock_web_sse=true`, **toute** réponse du bot est aussi poussée sur ce canal ; c'est le
    seul interrupteur, aucune variable supplémentaire n'active le streaming RAG.
- **SSE direct** : route `POST <path>/sse/direct` (`context.response().setupSSE()` puis
  `handleRequest(...)`). Les actions sont diffusées une par une par
  `WebConnectorCallback.sendStreamedResponse(action)`. Le flag
  `tock_web_connector_merge_stream_response` (défaut `true`) fusionne les fragments en un
  message cohérent. Cette route existait avant le streaming RAG et n'émet pas d'évènements
  `token`.

**Streaming RAG sur le canal persistant.** Dans `WebConnector.handleEvent`, le streaming est
activé quand `tock_web_sse=true`, que la requête n'est pas une requête `/sse/direct` et que le
canal SSE de l'utilisateur est connecté **sur cette instance** (`sseEndpoint.isConnected`). Le
connecteur positionne alors `metadata.streamedResponse = true` sur l'action utilisateur, ce qui
fait choisir `ragStream` par le moteur. Chaque fragment reçu par `sendStreamedToken` est ensuite
transformé par `WebConnectorCallback` en trame SSE `event: message` :

- les métadonnées portent `RESPONSE_ID` et `TOCK_STREAM_RESPONSE=true` ; le client remplace le
  message de même `RESPONSE_ID` ;
- par défaut, la trame contient le texte cumulé déjà rendu (markdown converti en HTML) ;
- si le client a envoyé `streamMode: "delta"` dans la requête, la trame ne contient que la fin
  du texte qui a changé, avec `TOCK_STREAM_MODE=delta` et `TOCK_STREAM_KEEP=N` : le client
  garde les `N` premiers caractères du message courant et ajoute le texte de la trame ;
- les trames partielles portent `TOCK_STREAM_PARTIAL=true` et le `TOCK_REQUEST_ID` repris ; la réponse finale désigne
  le message de remplacement avec `TOCK_STREAM_REPLACE=<actionId>` (ou `TOCK_STREAM_DISCARD=true` sans texte), voir
  [Streaming des réponses RAG](rag-streaming.md) ;
- la réponse finale est écrite par le même contexte Vert.x que les trames partielles, donc après
  elles, et une trame tardive est ignorée ;
- si aucun canal n'est connecté, la réponse complète est simplement renvoyée dans le `POST`.

Détail et contrat complets : [Streaming des réponses RAG](rag-streaming.md).

Aiguillage de la réponse finale (`WebConnector.handleWebConnectorCallback`, simplifié) :

```kotlin
if (callback.streamedResponse) {
    // /sse/direct : SSE chunk par chunk sur la requête
    callback.sendStreamedResponse(event)
} else {
    callback.addAction(event)
    if (sseEnabled) {
        // push sur le canal persistant, après les trames partielles éventuelles
        callback.sendFinalOverSse { sseEndpoint.sendResponse(...) }
    }
    if (event.metadata.lastAnswer) callback.sendResponse()  // HTTP : corps JSON unique
}
```

### 5.4 Le connecteur OpenAI : SSE « façon OpenAI »

`bot/connector-open-ai/src/main/kotlin/OpenAIConnector.kt` expose une API compatible
OpenAI : si la requête contient `stream = true` (`OpenAIConnector.kt`), la réponse est
streamée en SSE sous forme de *chunks* `chat.completion.chunk`, terminés par le sentinelle
OpenAI `data: [DONE]\n\n` (`OpenAIConnectorCallback.sendSseEnd()`). Sinon, réponse JSON
buffered classique.

### 5.5 Côté front

- Le **Studio Angular** (`bot/admin/web`) **n'a pas** de client SSE : la fenêtre de test
  utilise de l'HTTP classique (`POST /test/talk`).
- Le streaming côté navigateur est géré par les kits clients **Tock React Kit** et **Tock Vue
  Kit**. Les deux ouvrent un `EventSource` sur `<path>/sse?userid=` et appliquent le même
  contrat (`RESPONSE_ID` + `TOCK_STREAM_RESPONSE`). Le mode delta est une option
  (`streamMode`) de chacun.

### 5.6 Récapitulatif des flags

| Flag / option | Effet |
|---|---|
| `tock_web_sse` | Active le canal SSE persistant `GET <path>/sse`, les notifications push et le streaming RAG sur ce canal |
| `tock_web_direct_sse` | Active la route `POST <path>/sse/direct` (streaming de la réponse d'un POST) |
| `tock_web_connector_merge_stream_response` (défaut `true`) | Fusionne les fragments streamés (`sendStreamedResponse`) en un message |
| `streamMode: "delta"` (corps de la requête web) | Trames partielles réduites à la fin du texte (`TOCK_STREAM_KEEP`) |
| `metadata.streamedResponse` (action) | Choisit `ragStream` dans le moteur ; positionné par le connecteur, pas par le client web |
| `stream` (corps OpenAI) | Active le SSE « façon OpenAI » |

---

## 6. Synthèse

1. **Deux points d'entrée**, un seul moteur :
   - **Studio** → `POST /test/talk` → `TestTalkService` → appel HTTP au **connecteur REST** du
     bot → moteur. (IDs de test, `test=true`, HTTP classique.)
   - **Connecteurs** → webhook du canal (`POST <path>`) → `ConnectorCallback` → moteur.
   Les deux rejoignent `TockConnectorController.handleAction` → `Bot.handleAction`.

2. **Story RAG** : sélectionnée comme fallback de l'intent `unknown` **si RAG activé**, elle
   appelle le **Gen AI Orchestrator** (serveur Python) via un client Retrofit **bloquant**
   (`POST /rag`), et émet **un seul** message avec footnotes. Si le streaming est actif, l'orchestrateur
   expose aussi `POST /rag/stream` ; les fragments sont alors transmis au connecteur par le
   hook `sendStreamedToken`, mais la réponse du domaine reste une action unique.

3. **HTTP vs SSE** : l'HTTP classique renvoie **une réponse unique**. Le SSE (streaming)
   garde la connexion ouverte et pousse des évènements `text/event-stream`. Côté web, Tock
   propose un **canal SSE persistant** (push asynchrone et streaming RAG, scalable en cluster
   via MongoDB) et la route historique **SSE direct**. Le streaming visible côté client est
   **toujours produit par la couche connecteur**, jamais par le LLM lui-même.

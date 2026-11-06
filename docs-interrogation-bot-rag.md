# Interrogation d'un bot Tock — Points d'entrée, Story RAG & protocoles HTTP/SSE

> Document d'architecture. Objectif : comprendre **comment un message arrive jusqu'au bot**,
> les **deux points d'entrée** (Studio et connecteurs), le focus sur la **story RAG**, et la
> différence entre les protocoles **HTTP classique** et **SSE (streaming)**.
>
> Les références de code sont données sous la forme `chemin/fichier.kt:ligne`.

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
(`bot/engine/src/main/kotlin/engine/TockConnectorController.kt:172`) qui appelle
`Bot.handleAction(...)` (`bot/engine/src/main/kotlin/Bot.kt:104`). C'est là que se font :

- le **parsing NLP** de la phrase (`nlp.parseSentence(...)`, `Bot.kt:252`),
- la **résolution de la story** (`getStory(...)` → `botDefinition.findStoryDefinition(...)`),
- l'**exécution** de la story (`story.handle(bus)`, `Bot.kt:155`).

La différence entre les deux points d'entrée se situe **avant** ce tronc commun : c'est la
manière dont le message HTTP est reçu et transformé en `Event`/`Action`.

---

## 2. Point d'entrée n°1 — Via le Tock Studio (fenêtre « Test the bot »)

C'est la zone de dialogue de test de l'interface d'administration.

**Point clé et contre-intuitif :** le Studio **n'appelle PAS le moteur en direct (in-process)**.
Il fait un **vrai appel HTTP sortant** vers le **connecteur REST** du bot en cours d'exécution.
Un connecteur REST est d'ailleurs ajouté automatiquement à la création de chaque bot
(`bot/admin/server/src/main/kotlin/BotAdminVerticle.kt:576`, `addRestConnector(...)`).

### Flux détaillé

```
Studio UI (Angular)
  POST /test/talk                          test-dialog.component.ts:286
    → TestCoreService (route admin)        TestCoreService.kt:117   (blockingJsonPost, sécurisé botUser)
    → TestTalkService.talk()               TestTalkService.kt:44/59
    → ConnectorRestClient.talk()           ConnectorRestClient.kt:73   ───► HTTP (Retrofit)
    → POST <path>/:locale                  RestConnector.kt:74         (webhook du connecteur REST)
    → controller.handle(action, data)      RestConnector.kt:86
    → TockConnectorController.handleAction TockConnectorController.kt:172
    → Bot.handleAction → NLP → story       Bot.kt:104
```

### Étapes

1. **Front** : le composant `test-dialog.component.ts` (`talkRequest`, ligne 282) envoie un
   `POST /test/talk` avec un corps `BotDialogRequest`.
2. **Route admin** : `TestCoreService.registerServices()` monte la route
   `POST /test/talk` (`TestCoreService.kt:117`), vérifie le namespace, puis appelle
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
   `POST <path>/:locale` (`RestConnector.kt:74`). Le handler parse le `MessageRequest`,
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
  POST <path>                              WebConnector.kt:185   (handler principal)
    → WebConnector.handleRequest()         WebConnector.kt:210   (désérialise WebConnectorRequest)
    → WebConnector.handleEvent()           WebConnector.kt:244   (construit WebConnectorCallback)
    → controller.handleUserEvent(event)    WebConnector.kt:269
    → TockConnectorController.handleAction TockConnectorController.kt:172
    → Bot.handleAction → NLP → story       Bot.kt:104
```

- `register()` (`WebConnector.kt:124`) monte la route principale `POST <path>`
  (`WebConnector.kt:185`), plus éventuellement les routes SSE (voir §5).
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

- `RAGStoryDefinition` (`bot/engine/src/main/kotlin/definition/RAGStoryDefinition.kt:26`) est une
  story dont le **starter intent est `Intent.unknown`** : elle **remplace la story de
  fallback** (`unknown`). Identifiant : `tock_rag_story`.
- La décision d'aiguillage est dans `BotDefinition.findStoryDefinition(...)`
  (`bot/engine/src/main/kotlin/definition/BotDefinition.kt:97`) :

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

- `RAGAnswerHandler` (`bot/engine/src/main/kotlin/engine/config/RAGAnswerHandler.kt:62`),
  un `AbstractProactiveAnswerHandler`.
- Son point d'entrée `handleProactiveAnswer(botBus)` (`RAGAnswerHandler.kt:66`) :
  1. enregistre une métrique `STORY_HANDLED`,
  2. appelle `rag(this)` (`RAGAnswerHandler.kt:167`) qui construit la `RAGRequest` et interroge
     l'orchestrateur Gen AI,
  3. construit les **footnotes** (sources documentaires),
  4. envoie un `SendSentenceWithFootnotes` avec
     `ActionMetadata(isGenAiRagAnswer = true, observabilityInfo = ...)`
     (`RAGAnswerHandler.kt:95`),
  5. renvoie éventuellement une **redirection** vers une autre story
     (`ragStoryRedirection`, si l'orchestrateur suggère un `redirectionIntent`).
- L'exécution passe par `AbstractProactiveAnswerHandler.handle(...)` dans un
  `executor.executeBlocking { ... }` → **exécution toujours bloquante** du point de vue du
  thread moteur. En mode streaming (voir §4.4), c'est un `CountDownLatch` qui attend la fin
  du flux SSE — les tokens sont transmis au client **pendant** cette attente.

### 4.3 Appel au serveur d'orchestration Gen AI

Le moteur **n'appelle jamais un LLM directement**. Il passe par un client Retrofit du module
`gen-ai/orchestrator-client`, qui appelle le **Gen AI Orchestrator Server** (serveur Python) :

```
RAGAnswerHandler.rag()                     RAGAnswerHandler.kt:167
  → RAGService.rag(query, debug)           (interface, injectée)
  → RAGServiceImpl.rag()                    RAGServiceImpl.kt:29   → ragApi.rag(...).execute().body()
  → POST /rag                               RAGApi.kt:26           ───► HTTP (Retrofit, BLOQUANT)
  → Gen AI Orchestrator Server (Python)    rag_router.py  @post('') → RAGResponse (JSON complet)
```

- Interface HTTP : `@POST("/rag") fun rag(...): Call<RAGResponse>` (`RAGApi.kt:26`).
- Implémentation **synchrone** : `ragApi.rag(query, debug).execute().body()`
  (`RAGServiceImpl.kt:29`) → **une seule réponse JSON complète**, pas de flux.
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
  `updateObservabilityInfo`, `RAGAnswerHandler.kt:115`).

**Chemin streaming** (quand `action.metadata.streamedResponse == true`) :

```
RAGAnswerHandler.ragStream()               RAGAnswerHandler.kt:276
  → RAGServiceImpl.ragStream(listener)      RAGServiceImpl.kt
  → BackgroundEventSource                  (LaunchDarkly library, bypass RAGApi)
  → POST /rag/stream                       ───► SSE vers Gen AI Orchestrator Server
  ← event: token (×N)
    → listener.onToken(token)
    → callback.sendStreamedToken(token.content)  [transmis immédiatement au client]
  ← event: answer (terminal)
    → listener.onAnswer(response)
    → finalResponse = RAGResponse
  → RAGResult (retourné à handleProactiveAnswer)
  → send(SendSentenceWithFootnotes)        RAGAnswerHandler.kt:97
```

La différence clé : le chemin SSE utilise `BackgroundEventSource` directement, pas le client
Retrofit `RAGApi` (qui ne supporte que les réponses JSON uniques). Les tokens arrivent un à un
sur la réponse du client au fil de l'eau, tandis que l'event `answer` terminal contient le
`RAGResponse` complet (footnotes, observabilité, debug) — exactement comme le mode classique.

### 4.4 Point important sur le streaming RAG

L'orchestrateur Gen AI dispose de **deux endpoints** pour répondre aux questions RAG :

1. **Classique `POST /rag`** — renvoie une **réponse JSON unique et complète** (pas de
   streaming). Le moteur Tock appelle via le client Retrofit classique (`RAGApi.rag()`,
   `RAGServiceImpl.rag()`) et reçoit un `RAGResponse` en une seule fois. C'est le chemin par
   défaut (`action.metadata.streamedResponse == false`).

2. **Streaming `POST /rag/stream`** — renvoie un **flux SSE** où chaque token du LLM arrive
   individuellement. Quand `action.metadata.streamedResponse == true`, `RAGAnswerHandler.rag()`
   appelle `ragStream(botBus)` (`RAGAnswerHandler.kt:276`), qui ouvre une connexion SSE
   via `BackgroundEventSource` (LaunchDarkly library) — **sans passer par le Retrofit
   `RAGApi`**. Le serveur Python émet :
   - `event: token` (×N) — chaque fragment de texte généré par le LLM.
   - `event: answer` (terminal) — le `RAGResponse` complet (texte final, footnotes, debug,
     observabilité).

   Chaque `event: token` reçu est **immédiatement transmis au client** via
   `callback.sendStreamedToken(token.content)` (`RAGAnswerHandler.kt:318`), permettant un
   affichage progressif. L'`event: answer` terminal ramène le `RAGResponse` complet.

**Remarque sur l'exécution** : les **deux chemins** (classique et streaming) tournent dans
`executor.executeBlocking { }` (`AbstractProactiveAnswerHandler.kt:43`). Le streaming n'échappe
pas à ce bloc : en streaming mode, c'est un `CountDownLatch` qui bloque le thread moteur
jusqu'à la fin du flux SSE — les tokens sont transmis au client **pendant** cette attente.
La réponse finale (`SendSentenceWithFootnotes`) n'est émise qu'après le retour de `ragStream()`.

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

- `HttpServerResponse.setupSSE(...)` (ligne 258) : passe la réponse en *chunked*, pose les
  en-têtes SSE (`text/event-stream`, `keep-alive`, `no-cache`, `X-Accel-Buffering: no`) et
  démarre un ping de keep-alive.
- `sendSseMessage(data)` (ligne 301) : écrit `event: message\ndata: $data\n\n`.
- `sendSsePing()` (ligne 295) : écrit un `event: ping`.

### 5.2 Comment Tock choisit entre HTTP et SSE

Le streaming n'est **pas un transport séparé** : il est **piloté par des métadonnées** qui
circulent dans le moteur.

- `MetadataEvent.STREAM_RESPONSE_METADATA = "TOCK_STREAM_RESPONSE"`
  (`bot/engine/src/main/kotlin/engine/event/MetadataEvent.kt:44`) + helper
  `Map<String,String>.hasStreamMetadata()`.
- `ActionMetadata.streamedResponse` et `ActionMetadata.lastAnswer`
  (`bot/engine/src/main/kotlin/engine/action/ActionMetadata.kt`).
- Côté Bot API, un handler peut appeler `enableStreaming()` / `disableStreaming()`
  (`bot/api/client/.../TockClientBus.kt:108`) pour émettre ces métadonnées, puis envoyer des
  `SendSentence` partiels, le dernier portant `lastAnswer = true`.

### 5.3 Le connecteur Web : 3 modes

`bot/connector-web/src/main/kotlin/WebConnector.kt` — deux flags de configuration
(lignes 90-91) :

```kotlin
private val sseEnabled       = booleanProperty("tock_web_sse", false)         // canal SSE persistant
private val directSseEnabled = booleanProperty("tock_web_direct_sse", false)  // stream de la réponse d'un POST
```

| Mode | Déclenchement | Route | Transport | Usage |
|---|---|---|---|---|
| **HTTP classique** | défaut | `POST <path>` | 1 requête → 1 réponse `application/json` | Le plus simple, buffered |
| **SSE direct** | `tock_web_direct_sse=true` | `<path>/sse/direct` (toutes méthodes, typiquement POST) | La réponse est streamée en SSE (headers `text/event-stream`) | « Texte qui s'affiche au fil de l'eau » |
| **Canal SSE persistant** | `tock_web_sse=true` | `GET <path>/sse` | Connexion SSE ouverte en continu (`EventSource`) | Notifications *push* asynchrones |

Détails :

- **HTTP classique** — `WebConnectorCallback.sendResponse()`
  (`WebConnectorCallback.kt:75`) : `putHeader(CONTENT_TYPE, "application/json").end(...)`.
  Toute la réponse est renvoyée en une fois.
- **SSE direct** — route `<path>/sse/direct` (`WebConnector.kt:168`), acceptant toutes les
  méthodes HTTP (en pratique POST, car le corps est lu) : `context.response().setupSSE()`
  puis `handleRequest(...)`. La réponse est diffusée chunk par chunk via
  `WebConnectorCallback.sendStreamedResponse(action)` (`WebConnectorCallback.kt:87`) qui appelle
  `sendSseMessage(...)`. Le flag `tock_web_connector_merge_stream_response` (défaut `true`)
  fusionne les fragments en un message cohérent.
- **Canal SSE persistant** — module dédié `bot/connector-web-sse` :
  - `SseEndpoint.configureRoute(...)` (`SseEndpoint.kt:58`) monte `GET <path>/sse`, exige un
    `userId` (contexte sécurisé ou `?userId=`), et enregistre un canal SSE
    (`channels.register(...)`).
  - `SseChannels` (`channel/SseChannels.kt`) : registre en mémoire **+ repli base de données**.
    Si aucun canal local, l'évènement est **persisté** (`ChannelMongoDAO`, collection *capped*
    MongoDB + *change stream*) pour qu'une **autre instance** du backend (celle qui détient la
    connexion SSE du client) le pousse. C'est ce qui fait fonctionner le SSE en **cluster
    horizontal**.
  - `notify(...)` côté `WebConnector` lève une exception **sauf si `sseEnabled`** : les
    notifications *push* nécessitent ce canal persistant.

Aiguillage central (`WebConnector.handleWebConnectorCallback`, ~`WebConnector.kt:412`) :

```kotlin
if (callback.streamedResponse) {
    if (event.metadata.lastAnswer) callback.addMetadata(MetadataEvent.lastAnswer(...))
    callback.sendStreamedResponse(event)                 // SSE : chunk par chunk
} else {
    callback.addAction(event)
    if (sseEnabled) sseEndpoint.sendResponse(...)        // push sur le canal persistant
    if (event.metadata.lastAnswer) callback.sendResponse()  // HTTP : corps JSON unique
}
```

### 5.4 Le connecteur OpenAI : SSE « façon OpenAI »

`bot/connector-open-ai/src/main/kotlin/OpenAIConnector.kt` expose une API compatible
OpenAI : si la requête contient `stream = true` (`OpenAIConnector.kt:151`), la réponse est
streamée en SSE sous forme de *chunks* `chat.completion.chunk`, terminés par le sentinelle
OpenAI `data: [DONE]\n\n` (`OpenAIConnectorCallback.sendSseEnd()`). Sinon, réponse JSON
buffered classique.

### 5.5 Côté front

- Le **Studio Angular** (`bot/admin/web`) **n'a pas** de client SSE : la fenêtre de test
  utilise de l'HTTP classique (`POST /test/talk`).
- Le streaming SSE côté navigateur est géré par la lib externe **Tock Vue Kit**, illustrée par
  la page de démo `sse-test-page.html` à la racine (case `streamedResponse`, nécessite
  `export tock_web_direct_sse=true` côté serveur).

### 5.6 Récapitulatif des flags

| Flag / option | Effet |
|---|---|
| `tock_web_sse` | Active le canal SSE persistant `GET <path>/sse` + notifications push |
| `tock_web_direct_sse` | Streame la réponse d'un POST en SSE (`<path>/sse/direct`) |
| `tock_web_connector_merge_stream_response` (défaut `true`) | Fusionne les fragments streamés en un message |
| `streamedResponse` (sur `WebConnectorRequest`) | Opt-in côté client pour le streaming |
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
   (`POST /rag`), et émet **un seul** message avec footnotes. L'orchestrateur **ne streame pas**.

3. **HTTP vs SSE** : l'HTTP classique renvoie **une réponse unique**. Le SSE (streaming)
   garde la connexion ouverte et pousse des évènements `text/event-stream`. Tock propose,
   côté web, un **SSE direct** (streaming de la réponse d'un POST) et un **canal SSE
   persistant** (push asynchrone, scalable en cluster via MongoDB). Le streaming visible côté
   client est **toujours produit par la couche connecteur**, jamais par le LLM lui-même.

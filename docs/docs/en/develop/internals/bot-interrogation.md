---
title: Querying a bot
description: "How a message reaches the bot: entry points (Studio, connectors), the RAG story, HTTP and SSE protocols."
---

# Querying a bot: entry points, RAG story and HTTP/SSE protocols

> Goal: understand **how a message reaches the bot**, the **two entry points** (Studio and connectors),
> the focus on the **RAG story**, and the difference between the **classic HTTP** and **SSE (streaming)** protocols.
>
> Code references designate files of the repository; line numbers are deliberately omitted.

---

## 1. Overview

Whatever the input channel, all messages converge on the **same engine**:

```
                 ┌─────────────────────────────────────────────┐
  Studio  ─────► │                                             │
  (test)         │   ConnectorController.handle / handleUserEvent│
                 │          (TockConnectorController)           │
 Connectors ───► │                    │                         │
  (web, etc.)    │                    ▼                         │
                 │   Bot.handleAction → NLP → getStory → story.handle │
                 └─────────────────────────────────────────────┘
```

Convergence point: `TockConnectorController.handleAction(...)`
(`bot/engine/src/main/kotlin/engine/TockConnectorController.kt`), which calls
`Bot.handleAction(...)` (`bot/engine/src/main/kotlin/Bot.kt`). This is where the following happens:

- **NLP parsing** of the sentence (`nlp.parseSentence(...)`, `Bot.kt`),
- **story resolution** (`getStory(...)` → `botDefinition.findStoryDefinition(...)`),
- **story execution** (`story.handle(bus)`, `Bot.kt`).

The difference between the two entry points lies **before** this common trunk: how the HTTP message is
received and turned into an `Event`/`Action`.

---

## 2. Entry point 1: through the Tock Studio ("Test the bot" window)

This is the test dialog area of the administration interface.

**Key and counter-intuitive point:** the Studio **does NOT call the engine directly (in-process)**.
It makes a **real outgoing HTTP call** to the **REST connector** of the running bot.
A REST connector is moreover added automatically when each bot is created
(`bot/admin/server/src/main/kotlin/BotAdminVerticle.kt`, `addRestConnector(...)`).

### Detailed flow

```
Studio UI (Angular)
  POST /test/talk                          test-dialog.component.ts
    → TestCoreService (admin route)        TestCoreService.kt   (blockingJsonPost, secured botUser)
    → TestTalkService.talk()               TestTalkService.kt
    → ConnectorRestClient.talk()           ConnectorRestClient.kt   ───► HTTP (Retrofit)
    → POST <path>/:locale                  RestConnector.kt         (REST connector webhook)
    → controller.handle(action, data)      RestConnector.kt
    → TockConnectorController.handleAction TockConnectorController.kt
    → Bot.handleAction → NLP → story       Bot.kt
```

### Steps

1. **Front**: the `test-dialog.component.ts` component (`talkRequest`) sends a
   `POST /test/talk` with a `BotDialogRequest` body.
2. **Admin route**: `TestCoreService.registerServices()` mounts the route
   `POST /test/talk` (`TestCoreService.kt`), checks the namespace, then calls
   `TestTalkService.talk(...)`.
3. **Bridge to the bot**: `TestTalkService` (`TestTalkService.kt`):
   - loads the `BotApplicationConfiguration` (with an authorization check on the namespace),
   - gets a `ConnectorRestClient` (cached) pointing to the `baseUrl` of the bot
     (or `tock_bot_admin_rest_default_base_url` by default),
   - builds **synthetic test identifiers** (`buildTestPlayerId` →
     `test_<confId>_<locale>_<userIdModifier>`),
   - sets `test = true`, `debugEnabled`, `sourceWithContent`,
   - calls `restClient.talk(path, locale, ClientMessageRequest(...))`.
4. **REST connector (bot side)**: `RestConnector.register()` exposes the route
   `POST <path>/:locale` (`RestConnector.kt`). The handler parses the `MessageRequest`,
   creates a `RestConnectorCallback` (with the bot `testBehaviour` when `test=true`), then
   enters the engine through `controller.handle(...)`.

> **Practical consequence:** for the Studio test window to work, the bot must be running and its REST
> connector must be reachable from the admin server. This is classic blocking request/response HTTP,
> with **no streaming** on this path.

---

## 3. Entry point 2: through the connectors (external channels)

Each channel (web, Messenger, Slack, WhatsApp, Teams, Google Chat, OpenAI…) implements the
`ai.tock.bot.connector.Connector` interface and registers **its own HTTP webhook**
through `controller.registerServices(path) { router -> ... }`.

### Example: the Web connector

```
External channel (widget, app…)
  POST <path>                              WebConnector.kt (main handler)
    → WebConnector.handleRequest()         WebConnector.kt (deserializes WebConnectorRequest)
    → WebConnector.handleEvent()           WebConnector.kt (builds WebConnectorCallback)
    → controller.handleUserEvent(event)    WebConnector.kt
    → TockConnectorController.handleAction TockConnectorController.kt
    → Bot.handleAction → NLP → story       Bot.kt
```

- `register()` (`WebConnector.kt`) mounts the main route `POST <path>`, plus optionally the SSE routes (see §5).
- `handleRequest()` deserializes the body into a `WebConnectorRequest`, resolves the `connectorId`
  (with the "web bridge" option), converts it into an `Event` (`request.toEvent(...)`), then calls
  `handleEvent()`.
- `handleEvent()` builds a `WebConnectorCallback` and passes it to
  `controller.handleUserEvent(...)`.

The other connectors follow the same pattern: *webhook → parsing of the channel payload →
specific `ConnectorCallback` → `controller.handle(...)`.* What is specific to a connector is only the
**input/output format** and the **callback**; the engine core is shared.

### Key difference between the two entry points

| | **Studio (§2)** | **Connector (§3)** |
|---|---|---|
| Trigger | Dev/tester in the admin UI | End user on a channel |
| Transport | Outgoing HTTP admin → REST connector | Incoming channel webhook |
| Identity | Synthetic test IDs, `test=true` | Real channel `userId` |
| Behavior | `testBehaviour` applied | Production behavior |
| Engine | **Identical** (`Bot.handleAction`) | **Identical** (`Bot.handleAction`) |

---

## 4. Focus: the RAG story (Retrieval Augmented Generation)

The RAG story answers with **AI generation** (LLM + document search) when no configured story matches.

### 4.1 Definition and trigger

- `RAGStoryDefinition` (`bot/engine/src/main/kotlin/definition/RAGStoryDefinition.kt`) is a
  story whose **starter intent is `Intent.unknown`**: it **replaces the fallback story**
  (`unknown`). Identifier: `tock_rag_story`.
- The routing decision is in `BotDefinition.findStoryDefinition(...)`
  (`bot/engine/src/main/kotlin/definition/BotDefinition.kt`):

  ```kotlin
  else -> (if (ragConfiguration?.enabled == true) ragStory else null) ?: unknownStory
  ```

  In other words: if the incoming intent **matches no configured story**
  (and is neither `keyword` nor `ragexcluded`), the bot switches to the **RAG story**
  **only if `ragConfiguration.enabled == true`**; otherwise it keeps the `unknown` story.
- There is also a dedicated story `tock_ragexcluded_story` (intent `ragexcluded`) that
  **short-circuits** RAG to return a fixed answer.
- The RAG configuration is kept up to date at runtime by
  `BotRAGConfigurationMonitor` (`bot/engine/src/main/kotlin/engine/config/BotRAGConfigurationMonitor.kt`),
  which reloads `ragConfiguration` from the database when it changes.

### 4.2 The RAG handler

- `RAGAnswerHandler` (`bot/engine/src/main/kotlin/engine/config/RAGAnswerHandler.kt`),
  an `AbstractProactiveAnswerHandler`.
- Its entry point `handleProactiveAnswer(botBus)` (`RAGAnswerHandler.kt`):
  1. records a `STORY_HANDLED` metric,
  2. calls `rag(this)` (`RAGAnswerHandler.kt`), which builds the `RAGRequest` and queries the Gen AI
     orchestrator,
  3. builds the **footnotes** (document sources),
  4. sends a `SendSentenceWithFootnotes` with
     `ActionMetadata(isGenAiRagAnswer = true, observabilityInfo = ...)`
     (`RAGAnswerHandler.kt`),
  5. optionally returns a **redirection** to another story
     (`ragStoryRedirection`, if the orchestrator suggests a `redirectionIntent`).
- Execution goes through `AbstractProactiveAnswerHandler.handle(...)` in an
  `executor.executeBlocking { ... }` → **always blocking** from the engine thread's point of view. In
  streaming mode (see §4.4), a `CountDownLatch` waits for the end of the orchestrator SSE stream: text
  fragments are forwarded to the client **during** this wait.

### 4.3 Calling the Gen AI orchestration server

The engine **never calls an LLM directly**. It goes through a Retrofit client of the
`gen-ai/orchestrator-client` module, which calls the **Gen AI Orchestrator Server** (Python server):

```
RAGAnswerHandler.rag()                     RAGAnswerHandler.kt
  → RAGService.rag(query, debug)           (interface, injected)
  → RAGServiceImpl.rag()                    RAGServiceImpl.kt   → ragApi.rag(...).execute().body()
  → POST /rag                               RAGApi.kt ───► HTTP (Retrofit, BLOCKING)
  → Gen AI Orchestrator Server (Python)    rag_router.py  @post('') → RAGResponse (complete JSON)
```

- HTTP interface: `@POST("/rag") fun rag(...): Call<RAGResponse>` (`RAGApi.kt`).
- **Synchronous** implementation: `ragApi.rag(query, debug).execute().body()`
  (`RAGServiceImpl.kt`) → **a single complete JSON response**, no stream.
- HTTP client: `GenAIOrchestratorClient` — base URL
  `tock_gen_ai_orchestrator_server_url` (default `http://localhost:8000`), timeout
  `tock_gen_ai_orchestrator_client_request_timeout_ms` (default 55,000 ms).
- The `RAGRequest` (`gen-ai/orchestrator-client/.../requests/RAGRequest.kt`) carries in particular:
  the dialog history (`getDialogHistory`, last N messages), the LLM settings
  (question condensation + answer), the prompts, the embedding setting, the document index
  (`documentIndexName`), the search parameters, the vector store, and the **observability** setting
  (Langfuse).
- The `RAGResponse` contains `answer` (the LLM text), `footnotes` (sources), `debug` and
  `observabilityInfo` (Langfuse trace link, rewritten to a public URL by
  `updateObservabilityInfo`, `RAGAnswerHandler.kt`).

**Streaming path** (when `action.metadata.streamedResponse == true`):

```
RAGAnswerHandler.ragStream()
  → RAGServiceImpl.ragStream(listener)
  → BackgroundEventSource                  (LaunchDarkly library, bypasses RAGApi)
  → POST /rag/stream                       ───► SSE to the Gen AI Orchestrator Server
  ← event: token (×N)
    → listener.onToken(token)
    → callback.sendStreamedToken(token.content)  [ConnectorCallback hook]
  ← event: answer (terminal)
    → listener.onAnswer(response)
    → finalResponse = RAGResponse
  → RAGResult (returned to handleProactiveAnswer)
  → send(SendSentenceWithFootnotes)
```

The streaming path uses `BackgroundEventSource` directly, not the Retrofit `RAGApi` client (which only
handles single JSON responses). Fragments arrive progressively and are handed to the
`ConnectorCallback.sendStreamedToken` hook; the terminal `answer` event carries the complete
`RAGResponse` (footnotes, observability, debug), exactly as in classic mode.

### 4.4 Important point about RAG streaming

The Gen AI orchestrator has **two endpoints** to answer RAG questions:

1. **Classic `POST /rag`**: returns a **single, complete JSON response**. The engine calls it through the
   Retrofit client (`RAGApi.rag()`, `RAGServiceImpl.rag()`). This is the default path
   (`action.metadata.streamedResponse == false`).

2. **Streaming `POST /rag/stream`**: returns an **SSE stream**. When
   `action.metadata.streamedResponse == true`, `RAGAnswerHandler.rag()` calls
   `ragStream(botBus)`. The Python server emits:
   - `event: token` (×N): each text fragment generated by the LLM;
   - `event: answer` (terminal): the complete `RAGResponse` (final text, footnotes, debug,
     observability).

   Each received fragment is forwarded to the connector with `callback.sendStreamedToken(...)`.
   This is a `ConnectorCallback` **hook**, with no effect by default. Each connector decides what to do
   with it:
   - **web**: turns it into a frame on the `/sse?userid=` SSE channel (see §5.3);
   - **REST** (route `<path>/:locale/stream`) and **Google Chat**: use it directly for their own
     streaming;
   - the other connectors ignore it.

**Who sets `streamedResponse`?** The flag is carried by the user action (`ActionMetadata.streamedResponse`).
The REST connector forces it on the `/stream` route, Google Chat sets it according to its `enableStreaming`
parameter, and the web connector sets it itself when an SSE channel of the client is connected on the
instance (§5.3). The web client therefore has nothing to send.

**Note on execution**: both paths run in `executor.executeBlocking { }`. In streaming, a
`CountDownLatch` blocks the engine thread until the orchestrator stream ends. The final response
(`SendSentenceWithFootnotes`) is only emitted after `ragStream()` returns. It is the only domain action:
fragments are neither actions nor persisted.

### 4.5 Admin side: configuration (not to be confused)

`bot/admin/server/src/main/kotlin/service/RAGService.kt` **does not** produce the runtime answer: it only
manages the **configuration** of the RAG story (`getRAGConfiguration`, `saveRag`, `deleteConfig`…), with
validation against the orchestrator service providers. Not to be confused with the `RAGService` of the
`orchestrator-client` module, which is the **runtime client** calling the LLM.

---

## 5. Protocols: classic HTTP vs SSE (streaming)

### 5.1 Reminder: what is SSE?

**SSE (Server-Sent Events)** is a **unidirectional server → client streaming** protocol over HTTP. The
connection stays **open** (`Content-Type: text/event-stream`, *chunked* transfer, `keep-alive`) and the
server pushes events in the format `event: message\ndata: <payload>\n\n` as they occur. Compare with
classic HTTP: **request → single response → close**.

Low-level primitives (Vert.x), shared by all connectors: `shared/src/main/kotlin/vertx/VertXs.kt`:

- `HttpServerResponse.setupSSE(...)`: switches the response to *chunked*, sets the SSE headers
  (`text/event-stream`, `keep-alive`, `no-cache`, `X-Accel-Buffering: no`) and starts a keep-alive ping.
- `sendSseMessage(data)`: writes `event: message\ndata: $data\n\n`.
- `sendSsePing()`: writes an `event: ping`.

### 5.2 How Tock chooses between HTTP and SSE

Streaming is **not a separate transport**: it is **driven by metadata** flowing through the engine.

- `MetadataEvent.STREAM_RESPONSE_METADATA = "TOCK_STREAM_RESPONSE"` and the helper
  `Map<String,String>.hasStreamMetadata()` (`MetadataEvent.kt`).
- `ActionMetadata.streamedResponse` and `ActionMetadata.lastAnswer` (`ActionMetadata.kt`).
- On the Bot API side, a handler can call `enableStreaming()` / `disableStreaming()`
  (`TockClientBus.kt`) to emit this metadata, then send partial `SendSentence`s, the last one carrying
  `lastAnswer = true`.

### 5.3 The Web connector: 3 modes

`bot/connector-web/src/main/kotlin/WebConnector.kt`: two configuration variables:

```kotlin
private val sseEnabled       = booleanProperty("tock_web_sse", false)         // persistent SSE channel
private val directSseEnabled = booleanProperty("tock_web_direct_sse", false)  // stream the response of a POST
```

| Mode | Trigger | Route | Transport | Usage |
|---|---|---|---|---|
| **Classic HTTP** | default | `POST <path>` | 1 request → 1 `application/json` response | Simplest, buffered |
| **Persistent SSE channel** | `tock_web_sse=true` | `GET <path>/sse?userid=` | SSE connection kept open (`EventSource`) | Pushed responses, *push* notifications, **RAG streaming** |
| **Direct SSE** | `tock_web_direct_sse=true` | `POST <path>/sse/direct` | The POST response is streamed as SSE | Historical route, without fragment-level RAG streaming |

Details:

- **Classic HTTP**: `WebConnectorCallback.sendResponse()` returns the whole response at once
  (`application/json`).
- **Persistent SSE channel**: dedicated `bot/connector-web-sse` module.
  - `SseEndpoint.configureRoute(...)` mounts `GET <path>/sse`, requires a `userId` (secured context or
    `?userid=`) and registers an SSE channel.
  - `SseChannels`: in-memory registry **plus database fallback**. If no local channel exists, the event is
    **persisted** (`ChannelMongoDAO`, MongoDB *capped* collection + *change stream*) so that **another
    instance** (the one holding the client connection) pushes it. This is what makes SSE work in a
    **horizontal cluster**.
  - `notify(...)` on the `WebConnector` throws **unless `sseEnabled`**.
  - With `tock_web_sse=true`, **every** bot response is also pushed on this channel; it is the only
    switch, no additional variable enables RAG streaming.
- **Direct SSE**: route `POST <path>/sse/direct` (`context.response().setupSSE()` then
  `handleRequest(...)`). Actions are broadcast one by one by
  `WebConnectorCallback.sendStreamedResponse(action)`. The
  `tock_web_connector_merge_stream_response` flag (default `true`) merges the fragments into a coherent
  message. This route predates RAG streaming and does not emit `token` events.

**RAG streaming on the persistent channel.** In `WebConnector.handleEvent`, streaming is enabled when
`tock_web_sse=true`, the request is not a `/sse/direct` request and the user's SSE channel is connected
**on this instance** (`sseEndpoint.isConnected`). The connector then sets `metadata.streamedResponse = true`
on the user action, which makes the engine choose `ragStream`. Each fragment received by
`sendStreamedToken` is then turned by `WebConnectorCallback` into an `event: message` SSE frame:

- the metadata carries `RESPONSE_ID` and `TOCK_STREAM_RESPONSE=true`; the client replaces the message with
  the same `RESPONSE_ID`;
- by default, the frame contains the accumulated, already rendered text (markdown converted to HTML);
- if the client sent `streamMode: "delta"` in the request, the frame only contains the end of the text that
  changed, with `TOCK_STREAM_MODE=delta` and `TOCK_STREAM_KEEP=N`: the client keeps the first `N`
  characters of the current message and appends the frame text;
- partial frames carry `TOCK_STREAM_PARTIAL=true` and the echoed `TOCK_REQUEST_ID`; the final response designates
  the replacing message with `TOCK_STREAM_REPLACE=<actionId>` (or `TOCK_STREAM_DISCARD=true` when there is no text),
  see [RAG answer streaming](rag-streaming.md);
- the final response is written by the same Vert.x context as the partial frames,
  hence after them, and a late frame is ignored;
- if no channel is connected, the complete response is simply returned in the `POST`.

Full detail and contract: [Streaming RAG answers](rag-streaming.md).

Routing of the final response (`WebConnector.handleWebConnectorCallback`, simplified):

```kotlin
if (callback.streamedResponse) {
    // /sse/direct: SSE chunk by chunk on the request
    callback.sendStreamedResponse(event)
} else {
    callback.addAction(event)
    if (sseEnabled) {
        // push on the persistent channel, after any partial frames
        callback.sendFinalOverSse { sseEndpoint.sendResponse(...) }
    }
    if (event.metadata.lastAnswer) callback.sendResponse()  // HTTP: single JSON body
}
```

### 5.4 The OpenAI connector: "OpenAI-style" SSE

`bot/connector-open-ai/src/main/kotlin/OpenAIConnector.kt` exposes an OpenAI-compatible API: if the request
contains `stream = true`, the response is streamed as SSE in the form of `chat.completion.chunk` *chunks*,
terminated by the OpenAI sentinel `data: [DONE]\n\n` (`OpenAIConnectorCallback.sendSseEnd()`). Otherwise,
a classic buffered JSON response.

### 5.5 Front side

- The **Angular Studio** (`bot/admin/web`) **has no** SSE client: the test window uses classic HTTP
  (`POST /test/talk`).
- Browser-side streaming is handled by the **Tock React Kit** and **Tock Vue Kit** client kits. Both open an
  `EventSource` on `<path>/sse?userid=` and apply the same contract (`RESPONSE_ID` +
  `TOCK_STREAM_RESPONSE`). The delta mode is an option (`streamMode`) of each of them.

### 5.6 Summary of flags

| Flag / option | Effect |
|---|---|
| `tock_web_sse` | Enables the persistent SSE channel `GET <path>/sse`, push notifications and RAG streaming on this channel |
| `tock_web_direct_sse` | Enables the `POST <path>/sse/direct` route (streaming of a POST response) |
| `tock_web_connector_merge_stream_response` (default `true`) | Merges streamed fragments (`sendStreamedResponse`) into one message |
| `streamMode: "delta"` (web request body) | Partial frames reduced to the end of the text (`TOCK_STREAM_KEEP`) |
| `metadata.streamedResponse` (action) | Makes the engine choose `ragStream`; set by the connector, not by the web client |
| `stream` (OpenAI body) | Enables "OpenAI-style" SSE |

---

## 6. Summary

1. **Two entry points**, one engine:
   - **Studio** → `POST /test/talk` → `TestTalkService` → HTTP call to the bot **REST connector** → engine.
     (Test IDs, `test=true`, classic HTTP.)
   - **Connectors** → channel webhook (`POST <path>`) → `ConnectorCallback` → engine.
   Both join `TockConnectorController.handleAction` → `Bot.handleAction`.

2. **RAG story**: selected as the fallback of the `unknown` intent **if RAG is enabled**, it calls the
   **Gen AI Orchestrator** (Python server) through a **blocking** Retrofit client (`POST /rag`), and emits
   **a single** message with footnotes. When streaming is active, the orchestrator also exposes
   `POST /rag/stream`; fragments are then forwarded to the connector through the `sendStreamedToken` hook,
   but the domain response remains a single action.

3. **HTTP vs SSE**: classic HTTP returns **a single response**. SSE (streaming) keeps the connection open
   and pushes `text/event-stream` events. On the web side, Tock offers a **persistent SSE channel**
   (asynchronous push and RAG streaming, scalable in a cluster through MongoDB) and the historical
   **direct SSE** route. The streaming visible on the client side is **always produced by the connector
   layer**, never by the LLM itself.

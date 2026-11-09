---
title: RAG answer streaming
description: "How Tock streams RAG answers to clients: SSE contract, delta mode, activation per connector and streamable data."
---

# RAG answer streaming

This page describes how a RAG answer is displayed progressively, what enables it, and what can (or cannot) be
streamed. It complements the [SSE and streaming section of the web connector](../../channels/web.md#server-sent-events-sse-and-streaming).

## Principle

Streaming is a **transport and presentation** concern, not a **domain** one. The bot answer remains **a single
action** (`SendSentenceWithFootnotes`), persisted once in the dialog. Text fragments only exist for progressive display
on the client: the answer is never split into N partial actions.

```
LLM (question_answering_llm)                            [Python orchestrator]
  │ tokens
  ▼
POST /rag/stream → SSE: "token" event (×N), then "answer" event
  │
  ▼
RAGServiceImpl.ragStream → listener.onToken / onAnswer  [orchestrator client]
  │
  ▼
RAGAnswerHandler.ragStream                              [engine]
  • onToken(delta) → callback.sendStreamedToken(delta)
  • onAnswer(resp) → SendSentenceWithFootnotes (1 domain action)
  │
  ▼
Connector (web, REST, Google Chat)                      [progressive display, then final answer]
```

The engine exposes a single extension point: `ConnectorCallback.sendStreamedToken(content)`. Each connector decides
what to do with it.

## Web connector: contract on the `/sse` channel

With `tock_web_sse=true`, the web connector uses no dedicated event: it sends ordinary `message` frames on the
`<path>/sse?userid=<id>` channel, with the standard Tock streaming contract (the same as the Bot API one), consumed by
`tock-react-kit` and `tock-vue-kit`:

- the metadata of each frame contain `RESPONSE_ID` and `TOCK_STREAM_RESPONSE=true`;
- the client replaces the message with the same `RESPONSE_ID` by the frame content;
- the final answer (footnotes, buttons, etc.) keeps `RESPONSE_ID` and `TOCK_STREAM_RESPONSE=true` and replaces the
  streamed message: **the final answer is the source of truth**.

An answer is streamed when three conditions are met: `tock_web_sse=true`, a client whose `/sse` channel is connected to
the instance handling the message, and a bot with an active RAG story. The connector then sets
`metadata.streamedResponse=true` on the user action itself, which makes the engine choose `ragStream`. Otherwise the
complete answer is returned in the `POST` response.

### Frame ordering

Partial frames and the final answer are written by the same Vert.x context (`runOnContext`, FIFO): the final answer
always goes out after the frames already queued, and a late partial frame is dropped. Tokens arrive on an orchestrator
thread; this re-marshalling guarantees the order.

### Delta mode

By default each frame contains the accumulated text, already rendered (markdown converted to HTML). A client can request
the delta mode with `"streamMode": "delta"` in the request body. Frames then carry `TOCK_STREAM_MODE=delta` and
`TOCK_STREAM_KEEP=N`: the client keeps the first `N` characters of the current message and appends the frame text. The
server renders the text as in accumulated mode and only sends the end that changed (the HTML of a partial markdown is
not a prefix of the next one, hence the "keep N characters" rule). The final answer is always complete.

### Clients

| Client | Activation |
|---|---|
| `tock-react-kit` | consumes `/sse` automatically; option `streamMode: 'delta'` |
| `tock-vue-kit` | `preferences.messages.streamedResponse: true`; `preferences.messages.streamMode: 'delta'` |

Both open an `EventSource` on `<path>/sse?userid=<id>`. A browser limits HTTP/1.1 connections to 6 per host: during
development, several tabs with an open `EventSource` can block `POST` requests.

## Other connectors

- **REST**: the `<path>/:locale` route never streams; `<path>/:locale/stream` forces `metadata.streamedResponse=true`.
- **Google Chat**: the connector `enableStreaming` parameter (`"1"`), set in the Studio, enables streaming for all
  incoming messages.
- **`/sse/direct` route** (`tock_web_direct_sse=true`): historical route of the web connector, which streams the
  answer of a `POST` on the same request. It does not emit the RAG fragments.

These connectors use `sendStreamedToken`; their behavior is not changed by the web connector streaming.

## No per-bot activation setting

Streaming does not change the produced answer (same footnotes, same persistence). The one who knows whether it can
stream is the client, not the bot: a per-bot setting could enable streaming for a caller unable to read it. Activation
is therefore driven by the request (connected channel, `/stream` route) and bounded by the server switches (`tock_web_sse`
in particular). Dataset runs go through the base REST route: they always receive the complete final answer. Never point
the dataset path to `/stream`.

## What can be streamed

The Python flow (`execute_rag_chain_stream`) has three steps:

```
1. rag_inputs.ainvoke()   → retrieval + condensation   (BEFORE streaming)
2. answer_chain.astream() → tokens of the "answer" field (DURING streaming)
3. build_rag_response()   → footnotes / obs / debug      (AFTER streaming)
```

- **Intrinsically terminal** (cannot be streamed): footnotes (they depend on `context_usage`, the last field of the
  `LLMAnswer` JSON), the observability trace, `duration` / `step_durations`, and `status`, `redirection_intent`,
  `score` and `display_answer`.
- **Available early** (not streamed today): retrieved documents and debug prompts, known from step 1.
- **The text**: already streamed.

The progressive text and the final answer can differ (post-stream guardrail, truncation): this is why the final answer
always replaces the progressive display. Fragments are not persisted; only the final answer is stored.

## Environment variables

| Variable | Default | Role |
|---|---|---|
| `tock_web_sse` | `false` | Enables the persistent SSE channel, and therefore the streaming of web connector answers |
| `tock_web_direct_sse` | `false` | Enables the `<path>/sse/direct` route |
| `tock_web_connector_merge_stream_response` | `true` | Merges the partial `SendSentence` of Bot API streaming into one answer (no effect on RAG streaming) |
| `tock_web_sse_keepalive_delay` | `10` | Seconds between two SSE pings |

---
title: OpenAI API
---

# OpenAI-compatible connector

This connector exposes the bot through an API compatible with the OpenAI _chat completions_ API.
Any client supporting this API, such as [Open WebUI](https://docs.openwebui.com/), can then talk to the bot.

* **Connector type**: `openai`
* **Sources and README**: [connector-open-ai](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-open-ai)

## Configuration

Create an _OpenAI_ connector in _Tock Studio_, then use its URL as base URL of the OpenAI API in the client,
for instance `http://<bot-host>/io/<namespace>/<bot>/openai`.

Example with Open WebUI:

```shell
docker run -d -p 3000:3000 -e PORT=3000 -e OFFLINE_MODE=True -e OPENAI_API_KEY=NONE \
  -e OPENAI_API_BASE_URL=http://host.docker.internal:8080/io/app/new_assistant/openai \
  -e ENABLE_FORWARD_USER_INFO_HEADERS=true -v open-webui:/app/backend/data \
  --name open-webui --restart always ghcr.io/open-webui/open-webui:main
```

## Streaming

Answers are streamed by default. To also support non-streamed requests, set `tock_openai_support_unstreamed` to `true`.

---
title: API OpenAI
---

# Connecteur compatible OpenAI

Ce connecteur expose le bot via une API compatible avec l'API _chat completions_ d'OpenAI.
N'importe quel client qui prend en charge cette API, comme [Open WebUI](https://docs.openwebui.com/), peut alors dialoguer avec le bot.

* **Type de connecteur** : `openai`
* **Sources et README** : [connector-open-ai](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-open-ai)

## Configuration

Créez un connecteur _OpenAI_ dans _Tock Studio_, puis utilisez son URL comme URL de base de l'API OpenAI dans le client,
par exemple `http://<hôte-du-bot>/io/<namespace>/<bot>/openai`.

Exemple avec Open WebUI :

```shell
docker run -d -p 3000:3000 -e PORT=3000 -e OFFLINE_MODE=True -e OPENAI_API_KEY=NONE \
  -e OPENAI_API_BASE_URL=http://host.docker.internal:8080/io/app/new_assistant/openai \
  -e ENABLE_FORWARD_USER_INFO_HEADERS=true -v open-webui:/app/backend/data \
  --name open-webui --restart always ghcr.io/open-webui/open-webui:main
```

## Streaming

Les réponses sont streamées par défaut. Pour prendre aussi en charge les requêtes non streamées, passez
`tock_openai_support_unstreamed` à `true`.

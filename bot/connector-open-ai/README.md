# Tock Open AI Connector

A (very) simple Open AI compatible API connector.

## Stream message

Currently, only streamed messages are supported by default.

To enable sequential (unstreamed) messages, set the `tock_openai_support_unstreamed` property to `true`.

## Open WebUI

Tested with [Open WebUI](https://docs.openwebui.com/) client.

In order to start Open WebUI using docker, the important env var below is `OPENAI_API_BASE_URL=http://127.0.0.1:8080/io/app/new_assistant/openai` - just set the url of your tock connector. 

```
docker run -d -p 3000:3000 -e PORT=3000 -e OFFLINE_MODE=True -e OPENAI_API_KEY=NONE -e OPENAI_API_BASE_URL=http://host.docker.internal:8080/io/app/new_assistant/openai -e ENABLE_FORWARD_USER_INFO_HEADERS=true -v open-webui:/app/backend/data --name open-webui --restart always ghcr.io/open-webui/open-webui:main
```

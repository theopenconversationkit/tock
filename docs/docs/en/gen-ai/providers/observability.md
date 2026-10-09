---
title: LLM observability providers
description: "The LLM observability providers supported by Tock to trace prompts, answers, latency and cost."
---

# LLM observability providers

An observability provider traces the LLM calls made by the Gen AI orchestrator (prompts, answers, latency, cost).
It is configured in the [_Observability settings_](../observability.md) menu.

Here are the LLM observability providers supported by Tock:

| Provider | `provider` value |
|----------|------------------|
| [Langfuse](https://langfuse.com/docs) | `Langfuse` |

## Langfuse

```json
{
  "provider": "Langfuse",
  "url": "http://localhost:3000",
  "public_url": "https://langfuse.example.com",
  "secret_key": {
    "type": "Raw",
    "secret": "sk-lf-****************-ceabe45abe8f"
  },
  "public_key": "pk-lf-****************-b77e68ef7d2c"
}
```

| Field | Description |
|-------|-------------|
| `url` | URL used by the orchestrator to reach the Langfuse server (default: `http://localhost:3000`) |
| `public_url` | Optional URL of the Langfuse server as seen from the users' browser, used for the links to the traces in _Tock Studio_. Defaults to `url` |
| `secret_key` | Langfuse secret key |
| `public_key` | Langfuse public key |

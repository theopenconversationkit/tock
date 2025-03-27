---
title: Gen AI - Observability Settings
---

# The _Gen AI - Observability Settings_ Menu

- **LLM Observability** helps monitor, analyze, and understand the behavior of large-scale language models.
- This includes collecting data on their performance, detecting anomalies, and understanding the errors they may produce.
- The goal is to ensure that these models operate reliably and transparently, providing insights that help improve their performance and address potential issues.
- Specifically, we can:
  - View different sequences of LLM calls, including input and output prompts.
  - Analyze contextual document sections used.
  - Track information and metrics such as costs, token consumption, latency, etc.

> To access this page, you must have the **_botUser_** role.
> <br />(More details on roles can be found in [security](../../../../../admin/securite#rôles)).

## Configuration
To enable Tock to connect to an observability tool, a configuration screen has been set up:

![LLM Observability](../../../../../img/gen-ai/gen-ai-feature-observability.png "Configuration screen for the AI observability tool")

## Public URL Configuration

- The **Public URL** field allows specifying an externally accessible URL for observability tools such as Langfuse.
- This URL will be used in the frontend interface to redirect users to observability traces, replacing the internal URL, which may not be publicly accessible.

## Usage

- Here is the [list of LLM observability providers](../../providers/gen-ai-provider-observability) supported by Tock.
- Please refer to each tool's documentation to understand how to use it.
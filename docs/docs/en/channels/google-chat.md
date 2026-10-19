---
title: Google Chat
description: "Connect a Tock bot to Google Chat: configuration, supported messages and feedback buttons."
---

# Google Chat connector

The Google Chat connector connects a bot to [Google Chat](https://workspace.google.com/products/chat/).

* **Connector type**: `google_chat`
* **Sources and README**: [connector-google-chat](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-google-chat)

## Prerequisites

1. Create a Google Chat app (see the [Google Chat documentation](https://developers.google.com/workspace/chat)).
   Each connector needs its own Google Cloud project, since the Chat API settings (avatar, name, endpoint) are project-specific.
2. Grant the `chat.bots.get` and `chat.bots.update` permissions. With service account impersonation, the source
   service account also needs the `roles/iam.serviceAccountTokenCreator` role on the target service account.
3. In the [configuration of the Google Chat API](https://console.cloud.google.com/apis/api/chat.googleapis.com/hangouts-chat),
   set the HTTP endpoint URL to the URL of the connector (for instance `https://<bot-host>/io/<namespace>/<bot>/google_chat`),
   and use it as authentication audience.

## Configuration

In _Tock Studio_, create a _Google Chat_ connector in _Settings > Configurations_:

| Field | Description |
|-------|-------------|
| Application base URL | Public base URL of the bot |
| Authentication Audience (Google Chat app connection setting) | The HTTP endpoint URL configured in Google Cloud |
| Service account email to impersonate | (optional) email of the target service account; takes priority over the JSON credentials |
| Service account credential file path | (optional) path of a JSON credential file, used instead of the JSON content below |
| Service account credential json content | JSON credentials of the service account |
| Use condensed footnotes | `1`: condensed sources, `0`: detailed sources |
| Display sources without URL | `1`: displayed, `0`: hidden |
| Introductory message | (optional) message sent once, at the start of a new conversation |
| Use thread | `1`: the bot replies in threads, `0` (default): it replies directly in the space |
| Sources label | Title of the sources block (default: `Sources`) |
| Waiting message | Message displayed while the answer is being generated (default: `💭 Thinking...`) |
| Enable feedback buttons | `1`: thumbs up / down buttons on the final answers, `0` (default): disabled |

## Behavior

* **Threads**: with _Use thread_ enabled, the bot replies in the thread of the user message, or starts a new thread when there is none.
* **Formatting**: Markdown is converted to the simplified formatting of Google Chat (bold, italic, code, lists, links...).
* **Sources**: the sources of RAG answers are displayed as footnotes, condensed or detailed.
* **Feedback**: when enabled, the first vote is stored on the corresponding Tock action, and the buttons are then disabled.

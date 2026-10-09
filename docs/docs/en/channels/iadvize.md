---
title: iAdvize
description: "Connect a Tock bot to iAdvize with its bot API, and hand the conversation over to human agents."
---

# iAdvize connector

The iAdvize connector connects a bot to [iAdvize](https://www.iadvize.com/), a conversational platform for customer service,
with its [bot API](https://developers.iadvize.com/).
The bot can hand the conversation over to human agents.

* **Connector type**: `iadvize`
* **Sources and README**: [connector-iadvize](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-iadvize)

## Prerequisites

Create an iAdvize app with a bot plugin on the [iAdvize developer platform](https://developers.iadvize.com/).
In the app, set the health check URL and the base URL of the bot plugin to the URL of the Tock connector.

## Configuration

In _Tock Studio_, create an _iAdvize_ connector in _Settings > Configurations_:

| Field | Description |
|-------|-------------|
| Editor's URL | URL of the bot editor |
| Conversation first message | First message sent to the user |
| Human transfer rule | (optional) iAdvize distribution rule used to transfer the conversation to human agents |
| Distribution rule unavailable message | Message sent when the transfer is not possible |
| IAdvize secret token | (optional) token used to check the requests sent by iAdvize |
| Default locale | (optional) default locale, e.g. `en`, `fr` |

The iAdvize GraphQL API credentials are read from a secret manager or from environment variables
(`tock_iadvize_secret_manager_provider`, `tock_iadvize_credentials_secret_name`):
see the [iadvize-client](https://github.com/theopenconversationkit/tock/tree/master/util/iadvize-client) module.

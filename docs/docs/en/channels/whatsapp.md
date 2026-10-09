---
title: WhatsApp
description: "Connect a Tock bot to WhatsApp with the WhatsApp Business Cloud API of Meta."
---

# WhatsApp connector

The WhatsApp connector connects a bot to [WhatsApp](https://www.whatsapp.com/), with the
[WhatsApp Business Cloud API](https://developers.facebook.com/docs/whatsapp/cloud-api) of Meta.

* **Connector type**: `whatsapp_cloud`
* **Sources and README**: [connector-whatsapp-cloud](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-whatsapp-cloud)

> The former connector for the _On-Premise API_ (`whatsapp`), sunset by Meta, is deprecated.

## Prerequisites

Retrieve from Meta:

* the **WhatsApp Phone Number Id**: the ID of the phone number the bot sends messages from,
* the **WhatsApp Business Account Id**: the ID of the WhatsApp business account the app is registered on,
* a **Call Token**: the token allowing the bot to send messages,
* optionally, the **Meta Application Id**, only needed for [template management](#templates).

Choose a **Webhook verify token**, used when registering the webhook in the Meta admin interface.

## Configuration

1. In _Tock Studio_, create a _WhatsApp Cloud_ connector in _Settings > Configurations_, with these values.
2. In the webhook settings of the Meta application (with `messages` webhook events enabled), set:
    * the URL of the connector, for instance `https://<bot-host>/io/<namespace>/<bot>/whatsapp_cloud`
      (the connector path is displayed in its configuration),
    * the webhook verify token.

To test a bot running locally, expose it with a secure tunnel (for instance [zrok](https://zrok.io/) or [ngrok](https://ngrok.com/)).

## Messages

The connector provides builders for WhatsApp messages: texts, images, reply buttons, lists, templates...
Texts that are too long are truncated, and extra buttons are ignored, with a warning in the logs
(set `tock_whatsapp_error_on_invalid_messages` to `true` to throw an error instead, useful in automated tests).

## Templates

The connector provides an experimental management of [message templates](https://developers.facebook.com/docs/whatsapp/business-management-api/message-templates/):
the templates declared by the bot are created on the business account, so that they don't have to be created
manually in the Meta console.

To enable it, set `tock_whatsapp_sync_templates` to `true`, set the _Meta Application Id_ of the connector,
and provide at least one `WhatsappTemplateProvider` implementation (registered in
`META-INF/services/ai.tock.bot.connector.whatsapp.cloud.spi.WhatsappTemplateProvider`).
See the [README](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-whatsapp-cloud) for an example.

> Template messages are charged by Meta: check the [pricing](https://developers.facebook.com/docs/whatsapp/pricing/).

## Proxy

Calls to the WhatsApp API use the default proxy selector of the JVM. If the proxy requires authentication, set the
`tock_proxy_user` and `tock_proxy_password` properties (Basic authentication only).

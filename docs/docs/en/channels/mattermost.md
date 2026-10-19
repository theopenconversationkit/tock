---
title: Mattermost
description: "Connect a Tock bot to Mattermost with webhooks, on a channel or with a slash command."
---

# Mattermost connector

The Mattermost connector connects a bot to [Mattermost](https://mattermost.com/), with webhooks.
It answers the messages of a channel (optionally only those starting with a trigger word), or a slash command.

* **Connector type**: `mattermost`
* **Sources and README**: [connector-mattermost](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-mattermost)

## Prerequisites

* A Mattermost server, with the system administrator rights to create webhooks and allow the calls to the Tock host
* A URL of the bot that the Mattermost server can reach

## Configuration

1. In the Mattermost _System Console_ (_Environment > Developer_), allow Mattermost to call the Tock host
   (_Allow untrusted internal connections to_).
2. Create an **incoming webhook** (used by Tock to post messages) on the channel: its URL ends with its token.
3. Create an **outgoing webhook** (or a slash command) on the same channel, with the connector URL as callback,
   for instance `https://<bot-host>/io/<namespace>/<bot>/mattermost`. Mattermost gives its token.
4. In _Tock Studio_, create a _Mattermost_ connector in _Settings > Configurations_:

| Field | Description |
|-------|-------------|
| Mattermost Server Url | For instance `https://mattermost.mydomain.com` |
| Incoming webhook token | Token of the incoming webhook |
| Outgoing webhook token | Token of the outgoing webhook or slash command |
| Optional Channel Id | (optional) dedicated channel |
| Optional Tock Username | (optional) username displayed for the bot |

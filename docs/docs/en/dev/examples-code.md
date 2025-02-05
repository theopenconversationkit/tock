---
title: Examples
---

# Tock code examples

## Examples in *Bot Samples*

The [tock-bot-samples](https://github.com/theopenconversationkit/tock-bot-samples) repository contains code examples, including those used in
[Tock documentation](../guides/api.md) to program journeys in _WebHook_ or _WebSocket_ modes.

## The *Open Data* bot

The [tock-bot-open-data](https://github.com/theopenconversationkit/tock-bot-open-data) repository contains an
example of a bot implementation based on the [SNCF _Open Data_ API](https://www.digital.sncf.com/startup/api).

This bot uses the Kotlin framework for Tock (and not the _Bot API_ mode via _Webhook_ or _WebSocket_).

It also implements internationalization with two languages ​​offered: French and English.

### Deploy the bot with Docker

To deploy the bot with [Docker](https://www.docker.com/) / [Docker Compose](https://docs.docker.com/compose/),
follow the instructions in the [tock-docker](https://github.com/theopenconversationkit/tock-docker#user-content-run-the-open-data-bot-example) repository.

### Deploy the bot in your IDE

If you prefer to deploy a Tock platform without the Open Data Bot, and run it in your IDE (allowing you to do step-by-step debugging, for example), follow these instructions:

* Deploy a Tock NLU stack using the `docker-compose.yml` descriptor as explained [here](https://github.com/theopenconversationkit/tock-docker#user-content-docker-images-for-tock)

* Request your own [SNCF Open Data key](https://data.sncf.com/) (free) and configure the environment variable (see [OpenDataConfiguration](https://github.com/theopenconversationkit/tock-bot-open-data/blob/master/src/main/kotlin/ai.tock/bot/open/data/OpenDataConfiguration.kt#L29))

* Set up a connector: Messenger, Google Assistant or other (see [channels and connectors](../user/guides/canaux.md))

* Start the `OpenDataBot` launcher in your IDE, IntelliJ or other. The bot is operational, talk to it! :)
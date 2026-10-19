---
title: Develop
description: "Go beyond Tock Studio: program journeys in Kotlin or with the Bot API in other languages."
---

# Developing bots with Tock

_Tock Studio_ allows you to build conversational journeys (or _stories_) including text, buttons, images,
carousels, etc. To go further, it is possible to program journeys
in [Kotlin](https://kotlinlang.org/), [Javascript](https://nodejs.org/), [Python](https://www.python.org/)
or other languages.

![logo kotlin](../../img/kothlin.png "Kotlin"){style="width:75px;"}
![logo nodejs](../../img/nodejs.png "Node.js"){style="width:75px;"}
![logo python](../../img/python.png "Python"){style="width:75px;"}
![logo rest-api](../../img/restapi.png "REST API"){style="width:75px;"}

## Choosing a development mode

Two modes are available:

| | [_Bot API_](bot-api.md) (recommended) | [_Integrated bot_](kotlin-bot.md) |
|---|---|---|
| Languages | Kotlin, Javascript/Node.js, Python, or any language through the API | Kotlin only |
| Connection to the platform | Through the `bot_api` service (_WebSocket_ or _WebHook_) | Direct access to the MongoDB database of the platform |
| [Demo platform](https://demo.tock.ai/) | Available | Not available |
| Features | Most of the Tock features | All the Tock features, including the [coroutine stories](coroutine-stories.md) |
| Setup | Simple: the bot only needs an API key | A Tock platform (usually with [Docker](https://www.docker.com/)) and a shared MongoDB access |

Choose the _Bot API_ mode unless you need a feature that only the integrated mode provides.

## The _Bot API_ mode

The bot is an application that connects to the `bot_api` service of the platform, and receives the user messages
to answer them:

![BOT API](../../img/bot_api.png "BOT API")

See the [_Bot API_](bot-api.md) page.

## The _Integrated bot_ mode

The historical development mode of Tock: the bot embeds the Tock framework and accesses the MongoDB database
directly. Most of the bots built by the Tock designers are developed in this way.

![Bot TOCK](../../img/bot_open_data.png "Bot Tock")

See the [_Integrated bot_](kotlin-bot.md) page.

## Going further

* [Internationalization](i18n.md) of the answers
* [Testing](test.md) the stories
* [Creating a connector](connectors.md)
* [APIs](api.md) of the platform and of the [Gen AI orchestrator](../gen-ai/orchestrator-api.md)
* [Code examples](examples.md)

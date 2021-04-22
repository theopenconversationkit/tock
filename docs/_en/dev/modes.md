---
title: Modes
---

# Bot development with Tock

In order to go beyond the possibilities offered by _Tock Studio_ to build bots & conversational assistants, 
you can program your conversation flow using one of the following modes:

## The _Bot API_ Mode

The _Tock Bot API_ mode allows the development using any programming language of your choosing by integrating Tock's conversational REST API :

![BOT API](../img/bot_api.png "BOT API")

This mode is only available on the [Tock's demo platform](https://demo.tock.ai/). 

If you want to know more, read further [_here_](bot-api.md).

## The _Integrated Bot_ Mode

In this mode, you have access to all the possible functionalities that the Tock framework offers for developing a bot.

> Historically, most of the bots published by Tock's creators have an integrated bot.
 
The implementation of this solution is more complex than using the REST API and the integrated bot will need direct access 
to a MongoDB instance.

In consequence, you will need to :

- Install and configure your bot platform (using [Docker](https://www.docker.com/)) on your poste or on a given server
- Share access to your MongoDB instance
- Develop your bot by using [Kotlin](https://kotlinlang.org/)

![Bot TOCK](../img/bot_open_data.png "Bot Tock")

If you want to know more, read further [_here_](integrated-bot.md).

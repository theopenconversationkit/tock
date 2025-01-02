---
title: Modes
---

# Developing bots with Tock

_Tock Studio_ allows you to build conversational paths (or _stories_) including text, buttons, images,
carousels, etc. To go further, it is possible to program paths
in [Kotlin](https://kotlinlang.org/), [Javascript](https://nodejs.org/), [Python](https://www.python.org/)
or other languages.
<!-->To do bug imf nodejs and python<!-->
<img alt="Kotlin logo" title="Kotlin"
 src="https://upload.wikimedia.org/wikipedia/commons/7/74/Kotlin_Icon.png"
 style="width: 50px;">
<img alt="Nodejs logo" title="Nodejs"
 src="https://www.boostit.net/wp-content/uploads/2016/08/node-js-icon.png"
 style="width: 50px;">
<img alt="Python Logo" title="Python"
src="https://www.libraries.rutgers.edu/sites/default/files/styles/resize_to_300px_width/public/events/2020/01/python_3_2.png"
style="width: 50px;">
<img alt="API" title="Bot API"
src="https://zappysys.com/blog/wp-content/uploads/2018/06/REST-API-icon.jpg"
style="width: 50px;">

Two modes / frameworks / architectures are proposed:

## The *Bot API* mode

The _Tock Bot API_ mode (recommended for most cases) allows you to develop in [Kotlin](https://kotlinlang.org/)
or other languages ​​with the clients provided for [Javascript/Nodejs](https://nodejs.org/) and
[Python](https://www.python.org/) or any language using the Tock API:

![BOT API](../../img/bot_api.png "BOT API")

This mode is the only one available on the [Tock demo platform](https://demo.tock.ai/).
It is also the only mode that allows you to develop in any programming language, via the API.

For more information, see the [_Bot API_](../bot-api) page.

## The *Integrated Bot* mode

In this mode, you can access all the features and possibilities of the Tock framework to develop a bot.

> This is the historical development mode of Tock, and currently most of the bots published by Tock designers.
are developed in this way.

Setting up the solution is more complex than the _Bot API_ mode and requires in particular that the bot component
accesses the MongoDB database directly. It is therefore necessary to use this mode:

- To install a platform (usually with [Docker](https://www.docker.com/)) on your workstation or on a server
- To share the connection to the MongoDB database between the development workstations and the other components
of the Tock platform used
- To master the programming language [Kotlin](https://kotlinlang.org/)

![Bot TOCK](../img/bot_open_data.png "Bot Tock")

For more information, see the page [_Bot intégré_](../bot-integre).
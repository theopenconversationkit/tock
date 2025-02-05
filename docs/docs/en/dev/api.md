---
title: API
---

# Tock APIs

This section of the Tock documentation provides a brief overview of the different APIs offered by Tock.

## *Tock Web Connector API*

The Tock Web Connector allows interaction with a bot through a REST API.

The documentation for this API is available at [/api/web-connector](../../api/web-connector.html).

## *Tock NLU API*

The Tock _NLU/NLP_ API (Natural Language Understanding/Processing) allows programmatic querying of the conversational model and sentence analysis.

The Tock NLU API documentation is available at [/api](https://doc.tock.ai/tock/api/).

You can find this documentation on the Tock demo platform at [https://demo.tock.ai/doc/](https://demo.tock.ai/doc/).

If you have deployed a Tock platform locally using the provided [Docker images](https://github.com/theopenconversationkit/tock-docker), this documentation is available at [http://localhost/doc/index.html](http://localhost/doc/index.html).

## *Tock Studio / Admin API*

Similarly, the _Tock Studio_ API documentation is available at [/api/admin](../../api/admin.html).

You can find this documentation on the Tock demo platform at [https://demo.tock.ai/doc/admin.html](https://demo.tock.ai/doc/admin.html).

If you have deployed a Tock platform locally using the provided [Docker images](https://github.com/theopenconversationkit/tock-docker), this documentation is available at [http://localhost/doc/admin.html](http://localhost/doc/admin.html).

## *Tock Bot Definition API*

This API allows the creation of bots and journeys (_stories_) using any programming language.  
A Tock bot can include journeys configured in Tock Studio, supplemented by journeys developed in a programming language to implement complex rules, interact with other APIs, and more.

This API is used by Kotlin, JavaScript/Node.js, and Python clients available in both _WebHook_ and _WebSocket_ modes.

> Note: The API is still under development (beta), and its documentation will be available soon.

For development in _Bot API_ mode, see [this page](bot-api.md).

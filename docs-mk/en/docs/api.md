# Tock documented APIs

## _Tock Web Connector API_

The Tock _Web Connector_ allows to interact with a bot using a REST API.

API documentation is available at [/api/web-connector](../../../api/web-connector). 

## _Tock Web Connector API_

To test a model via the Tock _NLU_ API, see [/api](../../api/).

It is also possible to run the provided [Docker images](https://github.com/theopenconversationkit/tock-docker)
and open [http://localhost/doc/index.html](http://localhost/doc/index.html)   

## _Tock Studio / Admin API_

Tock Studio _Admin_ API (to manage the models) is documented: [/api/admin](../../api/admin.html) 

It is also possible to run the provided [Docker images](https://github.com/theopenconversationkit/tock-docker)
and open [http://localhost/doc/admin.html](http://localhost/doc/admin.html).

## _Tock Bot Definition API_

This API allows to create bots and stories with any programing language.
Tock bots can be composed of configured stories (using Tock Studio builder) and 
programatic stories, possibly implementing complex rules or leveraging other external APIs.
  
This API is used by provided Kotlin clients in _WebHook_ or _WebSocket_ mode, 
as well as the [`tock-node`](https://github.com/theopenconversationkit/tock-node) component in Javascript.

> The API is under development, it will soon be documented.

To know more about the _Bot API_ development framework, see [this page](bot-api.md).
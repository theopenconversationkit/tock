# Welcome to Tock - open conversational platform

<img alt="Tock logo" src="assets/images/logo.svg" style="width: 100px;">

**Tock** (*The Open Conversation Kit*) is a complete and open platform to build conversational agents - also known as _bots_. 

Tock does not depend on 3rd-party APIs, although it is possible to integrate with them.
Users choose which components to embed and decide to keep (or share) ownership of conversational data and models.

> Tock has been used in production for several years by [OUI.sncf](https://www.oui.sncf/services/assistant) to
> propose various assistants over its own channels (Web, mobile), social networks, as well as smart speakers.

> The platform source code is available on [GitHub](https://github.com/theopenconversationkit/tock) 
> under the [Apache License, version 2.0](https://github.com/theopenconversationkit/tock/blob/master/LICENSE).

## Features

* _Standalone_ bots or integrated with Web sites, mobile apps, social networks, smart speakers.
* Full-featured _NLU_ _(Natural Language Understanding)_ platform, compatible with algorithms such as 
[OpenNLP](https://opennlp.apache.org/), [Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/), [Duckling](https://github.com/facebook/duckling),
can be deployed alone (for use cases like the [Internet Of Things](https://fr.wikipedia.org/wiki/Internet_des_objets) for instance)
* _Tock Studio_ user interfaces:
    * Models management, bot training
    * Conversational no-code story builder
    * Internationalization (_i18n_) support for multilingual bots
    * Conversations, performance et model errors monitoring
    * Interactive trends / users flow analytics (_Bot Flow_)
* Frameworks available to develop complex stories and integrate with 3rd-party services: <br/> [Kotlin](https://kotlinlang.org/) _DSL_ plus any-language _API_
* Numerous connectors to [Messenger](https://www.messenger.com/), [WhatsApp](https://www.whatsapp.com/), 
[Google Assistant / Home](https://assistant.google.com/), [Twitter](https://twitter.com/), [Alexa](https://alexa.amazon.com/), 
[Business Chat / iMessage](https://www.apple.com/fr/ios/business-chat/), [Teams](https://products.office.com/fr-fr/microsoft-teams/), 
[Slack](https://slack.com/)... (see [connectors](dev/connectors.md))
* _Cloud_ or _on-premise_ setups, with or without [Docker](https://www.docker.com/), 
_"embedded"_ bots without Internet 

![NLU interface example - qualifying a sentence](img/tock-nlp-admin.png "NLU interface example - qualifying a sentence")

## Technologies

Tock runs on [JVM](https://fr.wikipedia.org/wiki/Machine_virtuelle_Java) platforms. The reference language is [Kotlin](https://kotlinlang.org/),
 but other programming languages can be leveraged through the available APIs.
 
Tock relies on [Vert.x](http://vertx.io/) and [MongoDB](https://www.mongodb.com ). 
Various _NLU_ libraries and algorithms can be used, such as [Apache OpenNLP](https://opennlp.apache.org/) or [Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/),
but Tock does not depend on them directly.

Graphical interfaces _(Tock Studio)_ are made with [Angular](https://angular.io/) in [Typescript](https://www.typescriptlang.org/).

## Getting started...

* Read [Tutorial](guide/studio.md) and start using the [demo/sandbox platform](https://demo.tock.ai/)


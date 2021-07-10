---
layout: page
title: Tock
---
# Welcome to Tock - open conversational platform

<img alt="Tock Logo" src="{{site.baseurl}}/{{ site.tock_logo }}" style="width: 150px;">

**Tock** (*The Open Conversation Kit*) is a complete and open platform to build conversational agents - also known as _bots_. 

The [Tock.ai](https://doc.tock.ai/) site is a good starting point to learn about the solution and its growing community.

Tock does not depend on 3rd-party APIs, although it is possible to integrate with them.
Users choose which components to embed and decide to keep (or share) ownership of conversational data and models.

> Tock has been used in production for several years by [OUI.sncf](https://www.oui.sncf/services/assistant) to
> propose various assistants over its own channels (Web, mobile), social networks, as well as smart speakers.

> The platform source code is available on [GitHub](https://github.com/theopenconversationkit/tock) 
> under the [Apache License, version 2.0](https://github.com/theopenconversationkit/tock/blob/master/LICENSE).

|   |   |
|---|---|
| ![AlloCovid](https://doc.tock.ai/fr/images/allocovid.png) | To find out more about [_AlloCovid_](https://www.allocovid.com/) (built with Tock) please visit [www.allocovid.com](https://www.allocovid.com/). Available by phone, on the Web and WhatsApp, the bot informs and guides French population about the Covid-19, thanks to experts, partners and volunteers. [This page](about/showcase.md#allocovid) gives technical details and links to the sources. |

## Features

* Bots _standalone_ or integrated with Web sites, mobile apps, social networks, smart speakers.
* Full-featured _NLU_ _<sup>([Natural Language Understanding](https://en.wikipedia.org/wiki/Natural-language_understanding))</sup>_ platform:
    * Leveraging open technologies, such as 
[OpenNLP](https://opennlp.apache.org/), [Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/), 
[Duckling](https://github.com/facebook/duckling), [Rasa](https://rasa.com/) 
(later [Spacy](https://spacy.io/), [BERT](https://en.wikipedia.org/wiki/BERT_(language_model)), ...)
    * Can be deployed alone (for use cases like [_Internet Of Things_](https://en.wikipedia.org/wiki/Internet_of_Things))
* _Tock Studio_ user interfaces:
    * NLU model management, bot training and performance monitoring
    * Zero-code conversational stories and decision trees builder
    * Internationalization support (_i18n_) for multilingual bots
    * Dialog monitoring and user flow analytics
* Frameworks provided to develop complex stories and integrate with 3<sup>rd</sup>-party services:  
[Kotlin](https://kotlinlang.org/), [Javascript/Nodejs](https://nodejs.org/), [Python](https://www.python.org/) _DSLs_ 
and any-language _REST API_ (see [_Bot API_](dev/bot-api.md))
* Numerous text/voice integrations available with [Messenger](https://www.messenger.com/), [WhatsApp](https://www.whatsapp.com/), 
[Google Assistant](https://assistant.google.com/), [Alexa](https://alexa.amazon.com/), [Twitter](https://twitter.com/), 
[Apple Business Chat](https://www.apple.com/fr/ios/business-chat/), [Teams](https://products.office.com/fr-fr/microsoft-teams/), 
[Slack](https://slack.com/)... (see [connectors](dev/connectors.md))
* _Cloud_ or _on-premise_ setups, with or without [Docker](https://www.docker.com/), 
_"embedded"_ bots without Internet 

![NLU interface example - qualifying a sentence](img/tock-nlp-admin.png "NLU interface example - qualifying a sentence")

## Technologies

Tock components can run as _containers_ (provided implementation for [Docker](https://www.docker.com/)). 

The application runs on [JVM](https://fr.wikipedia.org/wiki/Machine_virtuelle_Java) platforms. 
The reference language is [Kotlin](https://kotlinlang.org/), but other programming languages can be leveraged through the available APIs.
 
On the server side, Tock relies on [Vert.x](http://vertx.io/) and [MongoDB](https://www.mongodb.com ) <sup>(alt. [DocumentDB](https://aws.amazon.com/fr/documentdb/))</sup>. 
Various _NLU_ libraries and algorithms can be used, but Tock does not depend on them directly.

_Tock Studio_ graphical user interfaces are built with [Angular](https://angular.io/) in [Typescript](https://www.typescriptlang.org/).

[React](https://reactjs.org) and [Flutter](https://flutter.dev/) toolkits are provided for Web and Mobile integrations.

## Getting started...
* [Contents](toc.md)
* Read [Tutorial](guide/studio.md) and start using the [demo/sandbox platform](https://demo.tock.ai/)
* User manual for [developers](dev/modes.md)

*[NLU]: Natural Language Understanding
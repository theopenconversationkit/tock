# Contribute to Tock

The Tock project is open to contribution and any feedback is welcome!

This page details the source structure and coding conventions for the platform.

## Main technologies

The applicative platform is the [JVM](https://fr.wikipedia.org/wiki/Machine_virtuelle_Java).

The reference language is [Kotlin](https://kotlinlang.org/), but other programming languages can be used through the 
provided API.
 
Tock leverages [Vert.x](http://vertx.io/) and [MongoDB](https://www.mongodb.com ).

Graphical interfaces (namely _Tock Studio_) are powered by [Angular](https://angular.io/) in [Typescript](https://www.typescriptlang.org/).

## Source structure

### Repositories

* [`tock`](https://github.com/theopenconversationkit/tock): main source repository, including the framework 
and platform components under the [Apache 2 license](https://github.com/theopenconversationkit/tock/blob/master/LICENSE).

* [`tock-corenlp`](https://github.com/theopenconversationkit/tock-corenlp): optional module, leveraging a dependency to
[Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/) (instead of [Apache OpenNLP](https://opennlp.apache.org/)), 
under [GPL license](https://fr.wikipedia.org/wiki/Licence_publique_g%C3%A9n%C3%A9rale_GNU).

* [`tock-docker`](https://github.com/theopenconversationkit/tock-docker): [Docker](https://www.docker.com/) 
and [Docker Compose](https://docs.docker.com/compose/) images/descriptors, for platform hands-on and fast deployment
of various configurations.

* [`tock-bot-samples`](): code samples, in particular the _WebHook_ and _WebSocket_ modes examples from 
[Tock programing guides](../dev/bot-api.md).
 
* [`tock-bot-open-data`](https://github.com/theopenconversationkit/tock-bot-open-data): a bot example, based on 
the [SNCF _Open Data_ API](https://www.digital.sncf.com/startup/api), also implementing basic internationalization (_i18n_)
 mecanisms with two distinct languages.

### The `tock` repository

> TODO : detail modules and repo structure

### Le `tock-docker` repository

> TODO : detail modules and repo structure, how Maven and Docker builds work, etc.

## Build Tock from sources

### Tock (core)

Tock is built with [Maven](https://maven.apache.org/), including the Web modules leveraging 
[NPM](https://www.npmjs.com/) et [Angular](https://angular.io/):
 
`$ mvn package`

Continuous integration build jobs are available on [Travis](https://travis-ci.org/theopenconversationkit/tock).

### Docker images

Tock Docker images can be rebuilt from sources, included in repository [`tock-docker`](https://github.com/theopenconversationkit/tock-docker).
One can use [Maven](https://maven.apache.org/) to trigger the [Docker](https://www.docker.com/) build:

`$ mvn docker:build`

Docker containers can then be instantiated from images, or Docker Compose stacks from the various descriptors 
at the root of the repository.

## Run in IDE

> To run Tock using Docker Compose outside the IDE, rather see [Deploy Tock with Docker](../getting-started.md).

Tock components (NLU, Studio, bot...) can run in an IDE, such as  
[IntelliJ](https://www.jetbrains.com/idea/), [Eclipse](https://www.eclipse.org/) or [Visual Studio Code](https://code.visualstudio.com/) for instance.

Beside the [Docker images](https://github.com/theopenconversationkit/tock-docker/blob/master/docker-compose.yml),
IntelliJ configurations are provided with Tock sources:

- The _Tock Studio_ interfaces/server: [BotAdmin](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/BotAdmin.xml) 
- The alternative standalone NLU interfaces/server: [Admin](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/Admin.xml) 
- The NLU service: [NlpService](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/NlpService.xml)
- The Duckling entity-recognition service: [Duckling](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/Duckling.xml)
- The NLU model-builder service: [BuildWorker](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/BuildWorker.xml)
- The script compilation service: [KotlinCompilerServer](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/KotlinCompilerServer.xml)

The _OpenDataBot_ example also has a run configuration available:

- [OpenDataBot](https://github.com/theopenconversationkit/tock-bot-open-data/blob/master/.idea/runConfigurations/OpenDataBot.xml)

To start the _Tock Studio_ interfaces, please refer to the commands described in the following pages:

- [Full _Tock Studio_ server commands](https://github.com/theopenconversationkit/tock/blob/master/bot/admin/web/README.md)
- [Standalone NLU server commands](https://github.com/theopenconversationkit/tock/blob/master/nlp/admin/web/README.md)

## Conventions

[Kotlin Code Conventions](https://kotlinlang.org/docs/reference/coding-conventions.html) are used to develop Tock.

## Contact us

To contribute to the project or to known more about the implementation, feel free to [contact us](contact.md). 
Ideas and feedback is more than welcome.
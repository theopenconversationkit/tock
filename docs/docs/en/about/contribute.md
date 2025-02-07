---
title: Contributing
---

# Contribute to Tock

The Tock project is open to contribution and any feedback is welcome!

This page details the source structure and coding conventions for the platform.

## TL;DR

See [`CONTRIBUTING.md`](https://github.com/theopenconversationkit/tock/blob/master/CONTRIBUTING.md).

## Main technologies

Tock components can run as _containers_ (provided implementation for [Docker](https://www.docker.com/)).

The application runs on [JVM](https://fr.wikipedia.org/wiki/Machine_virtuelle_Java) platforms.
The reference language is [Kotlin](https://kotlinlang.org/), but other programming languages can be leveraged through the available APIs.

On the server side, Tock relies on [Vert.x](http://vertx.io/) and [MongoDB](https://www.mongodb.com) <sup>(alt. [DocumentDB](https://aws.amazon.com/fr/documentdb/))</sup>.
Various _NLU_ libraries and algorithms can be used, but Tock does not depend on them directly.

_Tock Studio_ graphical user interfaces are built with [Angular](https://angular.io/) in [Typescript](https://www.typescriptlang.org/).

[React](https://reactjs.org) and [Flutter](https://flutter.dev/) toolkits are provided for Web and Mobile integrations.

## Source structure

### Repositories

- [`tock`](https://github.com/theopenconversationkit/tock): main source repository, including the framework
  and platform components under the [Apache 2 license](https://github.com/theopenconversationkit/tock/blob/master/LICENSE).

- [`tock-corenlp`](https://github.com/theopenconversationkit/tock-corenlp): optional module, leveraging a dependency to
  [Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/) (instead of [Apache OpenNLP](https://opennlp.apache.org/)),
  under [GPL license](https://fr.wikipedia.org/wiki/Licence_publique_g%C3%A9n%C3%A9rale_GNU).

- [`tock-docker`](https://github.com/theopenconversationkit/tock-docker): [Docker](https://www.docker.com/)
  and [Docker Compose](https://docs.docker.com/compose/) images/descriptors, for platform hands-on and fast deployment
  of various configurations.

- [`tock-bot-samples`](https://github.com/theopenconversationkit/tock-bot-samples): code samples, in particular the _WebHook_ and _WebSocket_ modes examples from
  [Tock programming guides](../dev/bot-api.md).

- [`tock-bot-open-data`](https://github.com/theopenconversationkit/tock-bot-open-data): a bot example, based on
  the [SNCF _Open Data_ API](https://www.digital.sncf.com/startup/api), also implementing basic internationalization (_i18n_)
  mecanisms with two distinct languages.

### The `tock` repository

> TODO : detail modules and repo structure

### Le `tock-docker` repository

> TODO : detail modules and repo structure, how Maven and Docker builds work, etc.

## Build & run

### Build Tock from sources

#### Tock (core)

Tock is built with [Maven](https://maven.apache.org/), including the Web modules leveraging
[NPM](https://www.npmjs.com/) et [Angular](https://angular.io/):

`$ mvn package`

Continuous integration build jobs are available on [Travis](https://travis-ci.org/theopenconversationkit/tock).

#### Docker images

Tock Docker images can be rebuilt from sources, included in repository [`tock-docker`](https://github.com/theopenconversationkit/tock-docker).
One can use [Maven](https://maven.apache.org/) to trigger the [Docker](https://www.docker.com/) build:

`$ mvn package docker:build`

Docker containers can then be instantiated from images, or Docker Compose stacks from the various descriptors
at the root of the repository.

### Run Tock in IDE

> To run Tock using Docker Compose outside the IDE, rather see [Deploy Tock with Docker](../guides/platform.md).

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

To start the _Tock Studio_ interfaces, please refer to the commands described in the following page:

- [Full _Tock Studio_ server commands](https://github.com/theopenconversationkit/tock/blob/master/bot/admin/web/README)

## Code

### Commits & merge requests

To submit a feature or bugfix:

1. [Create an _issue_](https://github.com/theopenconversationkit/tock/issues/new):
   - Reccommended format for the title: - `[Component] Title` where component might be
     _Studio_, _Core_, _Doc_, etc. and title usually is like _Do or fix something_
2. [Create a _pull request_](https://github.com/theopenconversationkit/tock/pulls) and link it to the issue(s):
   - All commits should be [_signed_](https://help.github.com/en/github/authenticating-to-github/managing-commit-signature-verification)
   - Please rebase and squash unnecessary commits (tips: PR can be tagged as _Draft_) before submitting
   - Recommended format for the branch name :
     - `ISSUEID_short_title`
   - Recommended format for the commit(s) message(s):
     - `resolves #ISSUEID Component: title` for features
     - `fixes #ISSUEID Component: title` for fixes
3. To be merged, a _pull request_ must pass the tests and be reviewed by at least two of these developers:
   - [@vsct-jburet](https://github.com/vsct-jburet),
     [@francoisno](https://github.com/francoisno),
     [@NainJaune](https://github.com/NainJaune),
     [@elebescond](https://github.com/elebescond),
     [@SarukaUsagi](https://github.com/SarukaUsagi),
     [@MaximeLeFrancois](https://github.com/MaximeLeFrancois),
     [@bakic](https://github.com/bakic),
     [@broxmik](https://github.com/broxmik),
     [@mrboizo](https://github.com/mrboizo)

### Code conventions

[Kotlin Code Conventions](https://kotlinlang.org/docs/reference/coding-conventions.html) are used.

### Unit tests

Every new feature or fix should embed its unit test(s).

## Contact us

To contribute to the project or to known more about the implementation, feel free to [contact us](contact.md).
Ideas and feedback is more than welcome.

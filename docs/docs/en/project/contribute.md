---
title: Contributing
---

# Contribute to Tock

The Tock project is open to contribution and any feedback is welcome!

This page details the source structure and coding conventions for the platform.

## TL;DR

See [`CONTRIBUTING.md`](https://github.com/theopenconversationkit/tock/blob/master/CONTRIBUTING.md).

## Main technologies

See [Technologies](../index.md#technologies) on the home page.

## Source structure

### Repositories

- [`tock`](https://github.com/theopenconversationkit/tock): main source repository, including the framework
  and platform components under the [Apache 2 license](https://github.com/theopenconversationkit/tock/blob/master/LICENSE).

- [`tock-corenlp`](https://github.com/theopenconversationkit/tock-corenlp): optional module, leveraging a dependency to
  [Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/) (instead of [Apache OpenNLP](https://opennlp.apache.org/)),
  under [GPL license](https://en.wikipedia.org/wiki/GNU_General_Public_License).

- [`tock-docker`](https://github.com/theopenconversationkit/tock-docker): [Docker](https://www.docker.com/)
  and [Docker Compose](https://docs.docker.com/compose/) images/descriptors, for platform hands-on and fast deployment
  of various configurations.

- [`tock-helm-chart`](https://github.com/theopenconversationkit/tock-helm-chart): [Helm](https://helm.sh/) chart
  to deploy the Tock platform on [Kubernetes](https://kubernetes.io/) (see [Deploy on Kubernetes](../operate/kubernetes.md)).

- [`tock-react-kit`](https://github.com/theopenconversationkit/tock-react-kit): [React](https://react.dev/) toolkit
  to integrate a Tock bot into a Web page (see [React](../channels/index.md#react)).

- [`tock-vue-kit`](https://github.com/theopenconversationkit/tock-vue-kit): [Vue](https://vuejs.org/) toolkit
  to integrate a Tock bot into a Web page (see [Vue](../channels/index.md#vue)).

- [`tock-genai-core`](https://github.com/theopenconversationkit/tock-genai-core): core Python components
  (models, factories, error management) shared by the Tock [Gen AI](../gen-ai/index.md) components.

- [`tock-mcp-server`](https://github.com/theopenconversationkit/tock-mcp-server): [MCP](https://modelcontextprotocol.io/)
  server, written in Go, that exposes a Tock bot to AI agents and assistants through its [Web connector](../channels/web.md).


- [`tock-bot-demo`](https://github.com/theopenconversationkit/tock-bot-demo): a demo bot written in Kotlin
  with the [Bot API](../develop/bot-api.md), under [AGPL v3 license](https://www.gnu.org/licenses/agpl-3.0.html).

- [`tock-bot-open-data`](https://github.com/theopenconversationkit/tock-bot-open-data): a bot example, based on
  the [SNCF _Open Data_ API](https://data.sncf.com/), also implementing basic internationalization (_i18n_)
  mechanisms with two distinct languages.

### The `tock` repository

Here is an overview of the sources of the `tock` repository (see also the [packages](../develop/internals/packages.md) page):

* `bot`: the conversational platform (interfaces, API, connectors, etc.), depending on the _NLU_ modules
* `docs`: this documentation site, built with MkDocs
* `dokka`: the Dokka documentation of the Kotlin framework
* `etc`: utility scripts
* `gen-ai`: the Gen AI orchestrator (Python) and its Kotlin client
* `nlp`: the _NLU_ platform alone (interfaces, API, entity models, etc.)
* `scripts`: other utility scripts, for instance to develop on Messenger with ngrok
* `shared`: Kotlin components shared between the modules of the framework
* `stt`: _speech-to-text_ implementations and wrappers
* `translator`: implementations and wrappers for multilingual bots (_i18n_)

Note: there are "two _admins_" (i.e. two _Tock Studio_ interfaces) in the sources. The _NLU_ / _NLP_ platform
can indeed be installed alone, without the conversational tools. Hence:

* `nlp/admin`: the components and interfaces for _NLU_ / _NLP_ only
* `bot/admin`: reuses the _NLP_ / _NLU_ components (as Maven dependencies) and rebuilds the interfaces,
adding the conversational tools

### The `tock-docker` repository

The repository contains a Maven module structure mirroring the components of the Tock platform.
Each module holds the Docker implementation of a component and relies on the
`io.fabric8:docker-maven-plugin` Maven plugin to wrap the Docker build.

At the root of the repository, several Docker Compose descriptors deploy a platform
from the already built images. Different configurations are available, including _Bot API_ mode,
_integrated_ mode, standalone _NLU_ platform, etc. The reference descriptor for the
_Bot API_ mode is `docker-compose-bot.yml`.

## Build & run

### Build Tock from sources

#### Tock (core)

Tock is built with [Maven](https://maven.apache.org/), including the Web modules leveraging
[NPM](https://www.npmjs.com/) and [Angular](https://angular.dev/).
It requires a JDK 21 or later; Node.js is downloaded by the Maven build for the Web modules:

`$ mvn package`

Continuous integration builds run on [GitHub Actions](https://github.com/theopenconversationkit/tock/actions).

#### Docker images

Tock Docker images can be rebuilt from sources, included in repository [`tock-docker`](https://github.com/theopenconversationkit/tock-docker).
One can use [Maven](https://maven.apache.org/) to trigger the [Docker](https://www.docker.com/) build:

`$ mvn package docker:build`

Docker containers can then be instantiated from images, or Docker Compose stacks from the various descriptors
at the root of the repository.

### Run Tock in IDE

> To run Tock using Docker Compose outside the IDE, rather see [Deploy Tock with Docker](../getting-started/run-platform.md).

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

- [Full _Tock Studio_ server commands](https://github.com/theopenconversationkit/tock/blob/master/bot/admin/web/README.md)

## Code

### Commits & merge requests

To submit a feature or bugfix:

1. [Create an _issue_](https://github.com/theopenconversationkit/tock/issues/new):
   - Recommended format for the title: - `[Component] Title` where component might be
     _Studio_, _Core_, _Doc_, etc. and title usually is like _Do or fix something_
2. [Create a _pull request_](https://github.com/theopenconversationkit/tock/pulls) and link it to the issue(s):
   - All commits should be [_signed_](https://docs.github.com/authentication/managing-commit-signature-verification/signing-commits)
   - Please rebase and squash unnecessary commits (tips: PR can be tagged as _Draft_) before submitting
   - Recommended format for the branch name :
     - `ISSUEID_short_title`
   - Recommended format for the commit(s) message(s):
     - `resolves #ISSUEID Component: title` for features
     - `fixes #ISSUEID Component: title` for fixes

### Code conventions

[Kotlin Code Conventions](https://kotlinlang.org/docs/reference/coding-conventions.html) are used.

### Unit tests

Every new feature or fix should embed its unit test(s).

## Contact us

To contribute to the project or to known more about the implementation, feel free to [contact us](community.md).
Ideas and feedback is more than welcome.

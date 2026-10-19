---
title: Packages
description: "Overview of the Tock source repository: modules of the bot platform, NLU, Gen AI and documentation."
---

# Tock packages
## Bot (tock-bot): everything that defines a bot
```
├── bot:
│   ├── admin: Tock Studio
│   │   ├── kotlin-compiler: (optional) script compiler, to type scripts directly in the [_Stories and Answers_] Studio screen
│   │   ├── server: Studio backend
│   │   ├── test: Studio test plans
│   │   └── web: Angular Studio front, including the bot analytics screens and the skeleton of the configuration screens
│   ├── api: Bot API mode
│   │   ├── client: base classes/interfaces and definitions of the Bot API client
│   │   ├── model: Bot API DTOs
│   │   ├── retrofit-jackson-client: Jackson mappers for the type-safe HTTP client Retrofit
│   │   ├── service: Bot API services, with BotApiService, BotApiDefinition, BotApiClient, BotApiHandler and the Bot API launcher
│   │   ├── webhook: webhook mode jar
│   │   ├── webhook-base: launcher and Verticle definition of the webhook mode
│   │   ├── websocket: websocket mode jar
│   │   └── websocket-base: launcher and definition of the websocket mode
│   ├── connector-alcmeon: Alcmeon connector
│   ├── connector-google-chat: Google Chat connector
│   ├── connector-iadvize: iAdvize connector
│   ├── connector-mattermost: Mattermost connector
│   ├── connector-messenger: Messenger connector
│   ├── connector-open-ai: OpenAI-compatible connector (chat completions API)
│   ├── connector-rest: basic REST connector
│   ├── connector-rest-client: basic REST client connector (used in tests)
│   ├── connector-slack: Slack connector
│   ├── connector-teams: Teams connector
│   ├── connector-web: Web connector, also used in the Studio
│   ├── connector-web-model: web message model types, e.g. Button, QuickReply, Image, Carousel, Message, etc.
│   ├── connector-web-sse: Server-Sent Events support for the Web connector
│   ├── connector-whatsapp-cloud: WhatsApp connector (WhatsApp Business Cloud API)
│   ├── dialogflow: NLP delegated to DialogFlow
│   ├── engine: bot engine, defining all the conceptual and functional objects of Tock: integrated mode, DialogManager, connectors, bot, stories, etc.
│   │   ├── admin: bot admin engine, made of the following parts:
│   │   │   ├── answer: DTOs of the Studio answers (simple, message, script, builtin)
│   │   │   ├── bot: DTOs of the Studio bot configuration and version
│   │   │   ├── dialog: DTOs of dialog statistics
│   │   │   ├── message: DTOs of message types
│   │   │   ├── story: story DTOs and DAO interface to manage stories
│   │   │   │   └── dump: story export DTOs
│   │   │   ├── test: TestPlan and TestExecution DTOs and DAOs, /rest/admin/application/plans
│   │   │   └── user: user analytics DTOs and DAOs, `/rest/admin/users/search`
│   │   ├── connector:
│   │   │   └── media: media messages (more than simple text, can be transformed by the connector), e.g. carousel, card or file
│   │   ├── definition: abstract classes and interfaces of BotDefinition, Story, Handler, Steps, EventListener, etc.: the foundations of the DialogManager, TestBehavior
│   │   └── engine: core of the Tock engine, with Bot, Bus, ConnectorController and the parts below
│   │       ├── action: Action abstraction (user or bot), action type enum, and implementations of the action types (sendSentence, sendLocation, sendChoice, SendAttachment)
│   │       ├── config: bot configuration / refresh
│   │       ├── dialog: DTOs and methods to act on or get information about the dialog, including stories, entities and the state of the conversation
│   │       ├── event: abstract definition of a Tock event and implementations of the event types, e.g. Login, Logout, EndConversation, StartConversation
│   │       ├── feature: bot features, e.g. bot activation/deactivation
│   │       ├── message: message types (Sentence, Suggestion, Location, Choice, Attachment, etc.), then parsed by the NLP
│   │       │   └── parser: simple DSL parsing for the message types
│   │       ├── monitoring: request timers
│   │       ├── nlp: NLP processing, with the NLPController, NlpListener and NLPCallStats interfaces
│   │       ├── stt: helpers and methods to override speech-to-text results
│   │       └── user: user information and link between a dialog and a user (UserTimeline)
│   ├── engine-jackson: Jackson bindings
│   ├── orchestration: orchestration between bots (main and secondary)
│   ├── storage-mongo: MongoDB storage and DAOs
│   ├── test: Tock test-base jar
│   ├── test-base: test foundations, with mocks and JUnit definitions
│   ├── toolkit: toolkit-base with connectors
│   ├── toolkit-base: methods to install a bot from code and base IoC ("Bot Toolkit - to build chatbots with ease"), without connectors
│   └── xray: Xray automated test plugin, usable with Jira
```
## Docs: Tock documentation
```
├── docs: this documentation (MkDocs)
│   ├── docs
│   │   ├── en: English pages (reference)
│   │   ├── fr: French pages
│   │   └── img: images
│   └── hooks: MkDocs build hooks (API files copy, redirects, variables...)
```
## Gen AI (tock-gen-ai): generative AI
```
├── gen-ai
│   ├── orchestrator-core: Kotlin models shared with the orchestrator (LLM, embedding, vector store, observability, document compressor settings)
│   ├── orchestrator-client: Kotlin client of the orchestrator API
│   └── orchestrator-server: Python (FastAPI) Gen AI orchestrator: RAG, sentence generation, LLM providers, indexing tools
```
## Etc: deployment scripts and open source deployment process
```
├── etc: deployment scripts and markdown documentation about deployment
```
## Nlp (tock-nlp): the Tock NLP engine
```
├── nlp: the Tock NLP engine
│   ├── admin: NLP part of the admin
│   │   ├── server: NLP admin backend
│   │   └── web: front of all the NLP screens (training, tests, etc.)
│   ├── api: controllers, services and swagger documentation of the NLP API
│   │   ├── client: NLP controllers
│   │   ├── doc: API swagger
│   │   └── service: NLP Verticle and NlpService launcher
│   ├── build-model-worker: classes used to build NLP models
│   ├── build-model-worker-on-aws-batch: on AWS Batch
│   ├── build-model-worker-on-demand: on an on-demand platform
│   ├── core: NLP service core
│   │   ├── client: client interface for the Tock NLP entry points
│   │   ├── service: dictionaries and entities classes, and NLP evaluation result DTOs
│   │   └── shared: DTOs and interfaces used in the NLP module
│   ├── entity-evaluator: NLP entity evaluators/classifiers
│   │   ├── duckling: Duckling
│   │   ├── entity-value: default entities, e.g. email, phone number, distance, date, temperature, url, etc.
│   │   └── rest: REST connector for NLP classification
│   ├── front: NLP front classes
│   │   ├── client
│   │   ├── ioc: front IoC modules
│   │   ├── service: DAO and service interfaces for NLP management in the front
│   │   ├── shared: front and dump DTOs
│   │   └── storage-mongo: MongoDB DAOs of the front
│   ├── integration-tests: NLP integration tests
│   └── model: NLP models
│       ├── client: client for NLP model operations
│       ├── opennlp: OpenNLP engine
│       ├── rasa: Rasa engine
│       ├── sagemaker: AWS SageMaker hosted model
│       ├── service: classes to implement a new NLP engine
│       ├── shared: shared DTO classes
│       └── storage-mongo: MongoDB storage of the NLP libraries
```
## Scripts
```
├── scripts: scripts for Messenger access tokens
│   └── connector-messenger
```
## Shared: shared classes and utilities
```
├── shared: including Vert.x, Jackson, MongoDB, security providers (e.g. AWS, GitHub)
```
## Stt and translator: speech-to-text, and translation for the internationalization (i18n) of answers
```
├── stt: Google speech-to-text support
│   ├── core: speech-to-text core
│   ├── google-speech: calls to Google Speech
│   └── noop: no-op speech-to-text implementation
└── translator: internationalization of answers
    ├── core: translation and i18n core
    ├── deepl-translate: calls to DeepL
    ├── google-translate: calls to Google Translate
    └── noop: no-op translation implementation
```

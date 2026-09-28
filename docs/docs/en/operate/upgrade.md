---
title: Upgrading Tock
---

# Upgrading Tock

This page describes how to upgrade a Tock platform and the bots that use it to a new version,
and lists the changes that require an action.

## Procedure

1. Read the [changelog](../project/changelog.md) and the [GitHub release notes](https://github.com/theopenconversationkit/tock/releases)
   of every version between your current version and the target version, and the [version notes](#version-notes) below.
2. Back up the MongoDB databases, for instance with
   [`mongodump`](https://www.mongodb.com/docs/database-tools/mongodump/), and the vector store if you use the RAG.
3. Upgrade all the components of the platform to the same version: `bot_admin`, `nlp_api`, `build_worker`,
   `bot_api`, `kotlin_compiler`, `duckling_bot`, and the Gen AI orchestrator. With the
   [`tock-docker`](https://github.com/theopenconversationkit/tock-docker) descriptors, set the `TAG` variable
   to the new version.
4. Upgrade the Tock dependencies of your bots to the same version (see [Integrated bot](../develop/kotlin-bot.md)
   and [Bot API](../develop/bot-api.md)).
5. Restart the components, then check their [healthchecks](supervision.md#healthchecks) and the logs.
6. In _Tock Studio_, check the answers of the bot in _Test_ > _Test_, and the model builds in
   _Model Quality_ > _Model Builds_.

The components and the bots of a platform must use the same version: mixing versions is not supported.

The MongoDB indexes, and the data migrations when there are any, are applied by the components at startup:
no migration script needs to be run, unless stated in the version notes.

## Version notes

### 26.3.5

**Removed connectors.** The connectors for Alexa (`connector-alexa`), Google Assistant (`connector-ga`), Twitter
(`connector-twitter`), Apple Business Chat (`connector-businesschat`) and Rocket.Chat (`connector-rocketchat`), whose
platforms were shut down or are no longer used, are no longer provided. Remove their configurations in
_Settings_ > _Configurations_, and their dependencies from your bots.

### 26.3.0

**The RAG answers must be a structured JSON object.** The _Question answering_ prompt of every bot, in
_Gen AI_ > _Rag settings_, must be updated before deploying this version, otherwise the bot can no longer
answer with the RAG. See the [RAG prompt](../gen-ai/rag-prompt.md) documentation, which describes the expected
[JSON output](../gen-ai/rag-prompt.md#4-json-output-schema) and provides prompt examples.

### 25.10.0

**The Kotlin client of the _Bot API_ uses coroutines.** The `send` and `end` functions of `ClientBus` are now
`suspend` functions: code that calls them outside of a story handler must be adapted.

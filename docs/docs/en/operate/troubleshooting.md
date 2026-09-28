---
title: Troubleshooting
---

# Troubleshooting

This page lists common problems and their usual causes. Start by checking the logs and the
[healthchecks](supervision.md#healthchecks) of the components.

## The components do not start

**MongoDB connection errors.** Tock requires a MongoDB _replica set_, even with a single node:
`tock_mongo_url` must contain the `replicaSet` parameter and the replica set must be initialized
(see [Installation](installation.md#replica-set-architecture)).

**`no 'tock_encrypt_pass' set` error.** Outside the dev environment (`tock_env` different from `dev`), the
`tock_encrypt_pass` property is required as soon as data is encrypted (see [Security](security.md#application-encryption)).

## I cannot log in to _Tock Studio_

**The credentials are refused.** By default, the credentials are `admin@app.com` / `password`. If `tock_users`
and `tock_passwords` are set, they replace this default user (see [Security](security.md#implementation-by-properties)).

**The login succeeds, but the session is immediately lost.** Outside the dev environment, the session cookie
is only sent over HTTPS. Serve _Tock Studio_ over HTTPS, or set `tock_https_env=false` if TLS is handled by a
proxy that forwards HTTP requests. With several `bot_admin` instances, the sessions are not shared: use a single
instance, or sticky sessions on the load balancer.

## The bot does not answer

**In _Test_ > _Test_, the bot answers with an error.** The test connector calls the bot at the _Application base url_
of its configuration, in _Settings_ > _Configurations_: this URL must be reachable from the `bot_admin` service
(for instance `http://bot_api:8080` with the `tock-docker` descriptors, see [Configuration](../studio/configuration.md)).

**In _Bot API_ mode, nothing happens.** Check the logs of your bot: the API key must be the one of the
configuration, and the bot must reach the `bot_api` service (_WebSocket_ mode) or be reachable by it
(_WebHook_ mode, see [Bot API](../develop/bot-api.md)).

**The bot answers with another story.** The sentence is not recognized as the expected intent:
qualify it in _Language Understanding_ > _Inbox sentences_ (see [Language understanding](../studio/nlu.md)),
and check that the intent is the main intent of the story, in _Stories & Answers_ > _All stories_.

**The new sentences are not taken into account.** The model is rebuilt by the `build_worker` service shortly
after a sentence is validated: check that this service is running, and see the builds in
_Model Quality_ > _Model Builds_.

## The RAG does not answer

**The bot answers with a technical error.** The Gen AI orchestrator is not reachable: check
`tock_gen_ai_orchestrator_server_url` in the `bot_api` and `bot_admin` services, and the orchestrator
healthcheck (`/health-check`). Since Tock 26.3.0, the _Question answering_ prompt must also produce a JSON
object (see [Upgrading Tock](upgrade.md#2630)).

**The answers never cite any document.** Check that an indexing session is selected in the
[RAG settings](../gen-ai/rag.md#indexing-session), and that the vector store configured in _Tock Studio_ or by the
environment variables is the one where the documents were indexed: the default vector store is not the same in
_Bot Admin_ and in the orchestrator (see [Vector DB settings](../gen-ai/vector-store.md)).
The [retrieval diagnostic](../gen-ai/vector-store-inspection.md#retrieval-diagnostic) shows the documents found
for a question.

## The model building fails

**`OutOfMemoryError` in the `build_worker`.** The memory needed grows with the number of sentences, intents and
entities: see the [memory recommendations](installation.md#jvm-docker-memory).

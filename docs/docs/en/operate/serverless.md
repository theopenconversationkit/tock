---
title: Serverless
description: "Run Tock bots and the NLP API on Cloud Run or AWS Lambda, at no cost when they are not used."
---

# Serverless

Tock bots and the NLP API can run in a serverless process (GCP Cloud Run, AWS Lambda): the process is frozen
between the requests, or stopped when there is none, so that a bot that receives few messages costs nothing.

The serverless runtime has neither Vert.x nor MongoDB: it stores its data in DynamoDB, and the admin keeps
MongoDB.

!!! warning
    The serverless runtime is new. Only the connectors that declare server independent routes run in it (see
    [Connectors](#connectors)).

## Architecture

```
                         publication (change streams + periodic)
 bot_admin  ─────────────────────────────────────────────────────►  DynamoDB  ◄──── serverless bot / NLP API
 (Vert.x,   ◄─────────────────────────────────────────────────────   tables         (no Vert.x, no MongoDB)
  MongoDB)               copy of the runtime writes (Streams)
```

* The admin (`bot_admin`) stays a long running server on MongoDB: the searches of _Tock Studio_ need it.
* The runtime only reads and writes by key (by user, by application, by exact sentence): this is what DynamoDB
  does well, and DynamoDB costs nothing when it is not used.
* The admin publishes its configuration (applications, stories, labels, features, settings, NLP applications,
  intents, entity types, validated sentences...) to DynamoDB: at each change, and completely every 5 minutes.
  The runtime checks, at most every 10 seconds and at the start of a request, whether a publication happened,
  and reloads what has changed.
* The admin copies the writes of the runtime (dialogs, user timelines, metrics, inbox sentences, parse logs,
  unknown sentence counts...) from the DynamoDB Streams to MongoDB, so that _Tock Studio_ shows them.
* The NLP models and the uploaded files are stored in S3 (or any S3 compatible storage).

## The runtime

### Bots

Add the dependencies of a serverless bot to the project:

```xml
<dependency>
    <groupId>ai.tock</groupId>
    <artifactId>tock-serverless-bot</artifactId>
    <version>${tock.version}</version>
    <type>pom</type>
</dependency>
```

and start the bots with the DynamoDB storage:

```kotlin
fun main() {
    ServerlessBotLauncher.setup(botDynamoModule)
    BotRepository.registerBotProvider(MyBotProvider)
    ServerlessBotLauncher.start()
}
```

`start` starts an HTTP server on `PORT` (Cloud Run, Lambda Web Adapter). A request answers once the work it has
started is done: the answer of the bot is sent before the webhook answers, and nothing runs while the process
is frozen.

### Webhooks acknowledged at once

Some channels (WhatsApp, Messenger) expect a quick acknowledgment of their webhooks. With a queue, the webhooks
are acknowledged at once, and processed when the queue delivers them:

```kotlin
ServerlessBotLauncher.start(
    queueing =
        RequestQueueing(
            // tock-serverless-sqs, or PubSubRequestQueue(topic) { ... } (tock-serverless-pubsub)
            queue = SqsRequestQueue(queueUrl) { checkNotNull(ServerlessRequestHandler.queueing) },
            queuedPaths = setOf("/whatsapp"),
            // signs the queued messages: at least 16 characters
            secret = mySecret,
            // shared by the instances (tock-serverless-dynamodb)
            deduplication = DynamoDeduplicationStore(),
        ),
)
```

The queue delivers the messages to `POST /_queue/sqs` (SQS through the Lambda Web Adapter) or `/_queue/pubsub`
(a Pub/Sub push subscription). A webhook sent twice is processed once.

### Periodic tasks

A frozen process never runs a timer. The periodic tasks are registered by name, and an external scheduler
(EventBridge Scheduler, Cloud Scheduler) triggers them with `POST /_tasks/<name>` and the
`tock_serverless_tasks_token` in `X-Auth-Token`. The names are logged at startup, for instance
`translator_statscache` (statistics of the labels).

### NLP API

The `tock-serverless-nlp` module runs the NLP API on the published configuration and on the models stored in
S3 by the admin:

```kotlin
fun main() {
    ServerlessNlpLauncher.setup(nlpDynamoModule, listOf(ducklingModule))
    ServerlessNlpLauncher.start()
}
```

The applications are created in the admin, and the logs are searched there: `application/create` and
`logs/count` answer 501.

### AWS Lambda

Without the Lambda Web Adapter, the `ai.tock.serverless.lambda.LambdaHandler::handleRequest` handler receives:

* the HTTP requests of a function URL or of an API Gateway HTTP API (payload format 2.0);
* the SQS events of the queue of the webhooks, with the partial batch failures (`ReportBatchItemFailures`);
* the EventBridge Scheduler events whose input is `{"tock_task": "<name>"}`.

The process is set up by a `ServerlessApplication`, declared in
`META-INF/services/ai.tock.serverless.ServerlessApplication`:

```kotlin
class MyBotApplication : ServerlessApplication {
    override fun install() {
        ServerlessBotLauncher.setup(botDynamoModule)
        BotRepository.registerBotProvider(MyBotProvider)
        ServerlessBotLauncher.install()
    }
}
```

It is installed when the function initializes: before the snapshot, with SnapStart.

## The admin

Add `tock-bot-storage-dynamodb-publication` and `tock-bot-storage-dynamodb-replication` to the classpath of
`bot_admin`, and enable them:

```
tock_dynamodb_publication_enabled=true
tock_dynamodb_replication_enabled=true
```

The models must be built to S3: set `tock_nlp_model_storage=s3` on `bot_admin` and `build_worker`.

See the [configuration reference](configuration.md#serverless-runtime) for the other properties.

## Connectors

`tock-serverless-bot` brings the connectors that declare server independent routes, without their Vert.x
dependencies: WhatsApp Cloud, Messenger, WhatsApp, Slack, Google Chat, Teams, Mattermost, Alcmeon, iAdvize, REST,
web and OpenAI API.

* **Web and OpenAI API.** Their answers are JSON, or server-sent events for the streamed answers
  (`tock_web_direct_sse` for the web connector, `"stream": true` for a chat completion). The events reach the client
  as they are sent with Cloud Run; a buffered AWS Lambda response sends them all at its end.
* **Web security.** The users are authenticated by an `HttpWebSecurity`, chosen by the `web_security_mode` of the
  connector: `PASSTHROUGH`, `COOKIES` and `COOKIES_ENCRYPTED` are provided, with the cookies of the Vert.x handlers.
  A project that binds its own Vert.x `WebSecurityHandler` (a `JWT` mode...) keeps the Vert.x routes: to run in the
  serverless runtime, it binds an `HttpWebSecurity` with the tag of its mode instead.
* **Not supported:** the server-sent events channels of `tock-bot-connector-web-sse` (`tock_web_sse`: long lived
  connections and a queue of messages between the instances in MongoDB), the notifications of the web and OpenAI API
  connectors (they need these channels), the orchestration of bots (`tock-bot-orchestration`) and the websocket mode
  of _Bot API_.

## Limits

* The counters copied from DynamoDB to MongoDB (statistics of the labels, unknown sentences) and the parse logs
  are counted twice if a record of a stream is read again (lost checkpoint).
* At the first start of the copy, only the last 24 hours of the streams are copied.
* The statistics of the labels kept in memory are lost if the instance stops before the next
  `translator_statscache` task.
* Without a model in S3, the NLP API only recognizes the validated sentences.

---
title: Plateforme
---

# Deploying a platform with Docker

In the previous sections to discover and test Tock, you used the
[demo platform](https://demo.tock.ai/). This allowed you to discover
the construction and configuration of Tock bots without having to install the platform first.

In this guide, you will learn how to deploy a complete Tock platform in a few minutes, thanks
to the [Docker](https://www.docker.com/) / [Docker Compose](https://docs.docker.com/compose/) implementation examples provided.

Note that it is entirely possible to deploy Tock without using Docker. 
## What you will create

* A complete local Tock platform: _Tock Studio_, _Bot API_, etc.

* A bot and a minimal configuration to test the platform

* (Optional) A [Kotlin](https://kotlinlang.org/) program connecting to the local platform via
_WebSocket_

## Prerequisites

* About 20 minutes

* To deploy the platform locally, a development environment with recent versions of
[Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/) installed

> If you don't want to use Docker, no problem. There are other ways to deploy the MongoDB database
>and Kotlin services on JVM. However, you can browse the `Dockerfile` and
>`docker-compose.yml` as examples to instantiate these services.

* (Optional) For the WebSocket program, a development environment (or _IDE_) supporting
[Kotlin](https://kotlinlang.org/), for example [IntelliJ](https://www.jetbrains.com/idea/) with recent versions
of [JDK](https://jdk.java.net/) and [Maven](https://maven.apache.org/)

> Without _IDE_ or without Maven, no problem. It is quite possible to compile and run the program with other tools.

## Deploy a Tock platform - without sources

It is possible to retrieve only a few files from the GitHub repository, without downloading all the Tock sources.
In a few command lines, the platform is operational.

However, it is essential to have recent versions of
[Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/).

> To start from the sources of the Tock Docker repository, go instead to the
[next paragraph](#deploying-a-tock-platform-from-sources).
```shell
# Get the lastest docker-compose from GitHub (including Bot API)
$ curl -o docker-compose.yml https://raw.githubusercontent.com/theopenconversationkit/tock-docker/master/docker-compose-bot.yml
# Get the lastest database-init script from GitHub
$ mkdir -p scripts && curl -o scripts/setup.sh https://raw.githubusercontent.com/theopenconversationkit/tock-docker/master/scripts/setup.sh && chmod +x scripts/setup.sh
# Get the lastest Tock version/tag from GitHub
$ curl -o .env https://raw.githubusercontent.com/theopenconversationkit/tock-docker/master/.env

# Run the stack
$ docker-compose up
```
## Deploying a Tock platform - from sources

This is an alternative way to start Tock, from the [Tock Docker](https://github.com/theopenconversationkit/tock-docker) repository.

In addition to [Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/), you need either a [Git](https://git-scm.com/) client to retrieve the sources (`git clone` command) or to have already copied the GitHub sources locally.

> To start without Git or the repository sources locally, follow the
[previous paragraph](../guides/platform.md#deploy-a-tock-platform-without-sources).
```shell
# Get the lastest sources from GitHub
$ git clone https://github.com/theopenconversationkit/tock-docker.git && cd tock-docker
# Make the database-init script executable
$ chmod +x scripts/setup.sh

# Run the stack (including Bot API)
$ docker-compose -f docker-compose-bot.yml up
```
## Accessing *Tock Studio*

Once the platform is ready, the _Tock Studio_ interfaces are on port `80` by default:

* Go to [http://localhost](http://localhost)

> After deploying the platform, it initializes, and you may have to wait a few seconds
>before the _Tock Studio_ interfaces are accessible.

* Log in with the default `admin@app.com` / `password` credentials

> It is obviously recommended to change these values ​​when installing a platform intended for long-term use
>(production, platform shared between teams, etc.).

## Create an application, a connector and an intention

As in the [first bot](../guides/studio.md) guide using the demo platform, you will create a
_Tock_ application and a connector to start using the local platform. Feel free to go back to the
previous guides for more comments.

When you first access the local platform:

* Enter a name for the application

* Select a language - you can add others later

* Validate to create the application

* Go to _Settings_ > _Configurations_

* _Create a new Configuration_

* Select the _Slack_ connector type

* _Create_

> Note the _API Key_ automatically generated for your application. It will be useful if you try the _WebSocket_ mode
> later in this guide (optional).

* Go to _Stories & Answers_ > _New Story_

* Enter a user phrase for example "hello"

* In the _Add new Answer_ field, enter a response for example "what a beautiful day!"

* Finish with _Create Story_

* Go to _Test_ > _Test the Bot_

* Say "hello" 🙋, the bot answers you 🤖

## Connect a course in Kotlin (optional)

As in the guide [program courses](../guides/api.md) using the demo platform, you will create a
_Kotlin application_ connecting in _WebSocket_ to the local Tock platform. Feel free to go back to the
previous guides for more details.

* Create a Kotlin project for example with Maven as indicated in the guide [program courses](../guides/api.md)

> The _classpath_ must include `tock-bot-api-websocket` to use the _WebSocket_ mode.

* Create a Kotlin file (e.g. in `src/main/kotlin/StartWebSocket.kt)

* Edit it with the following code:

```kotlin
import ai.tock.bot.api.client.newBot
import ai.tock.bot.api.client.newStory
import ai.tock.bot.api.websocket.start

fun main() {
    start( // Do not use #startWithDemo when integrating with a local platform 
        newBot(
            "PUT-YOUR-TOCK-APP-API-KEY-HERE", // Get your app API key from Bot Configurations in Tock Studio
            newStory("qui-es-tu") { // Answer for the 'qui-es-tu' story
                send("Je suis un assistant conversationnel construit avec Tock")
                end("Comment puis-je aider ?")
            }
        ),
        "http://localhost:8080" // Local platform URL (default host/port)
    ) 
}
```
> You can find this code (and other examples) in the [tock-bot-samples](https://github.com/theopenconversationkit/tock-bot-samples) repository.

* Replace the API key with the one of your own Tock application. To do this, in _Tock Studio_,
go to _Settings_ > _Configurations_ and report the _API Key_ value in the code.

* Run the function (_main_) in your development environment.

* Go back to Tock in _Test_ > _Test the Bot_ and say "who are you?": the bot does not answer yet.

* Go to _Language Understanding_ > _Inbox_, for the sentence you just entered:

* Change the intent to _New intent_

* Name it "who-are-you" as in the code (so that the link is made)

* Create the intent with _Create_

* Finish qualifying the sentence with _Validate_

* Go back to _Test_ > _Test the Bot_. Say "who are you?": the bot answers!

## Congratulations!

You have just deployed your own Tock conversational platform locally.

This can be used to better understand the architecture and check the _portability_ of the solution, but also during
developments, for Tock contributors or if you have to work without Internet access
(on the move, on a restricted network, etc.).

> Warning, the provided Docker implementation is not enough to guarantee resilience and scalability of the platform
>whatever the production conditions. For this, some recommendations are proposed in the
>[high availability](../admin/availability.md) section of the Tock manual.

## Continue...

You have just completed the Tock quick start guides.

From here, you can launch directly on a Tock platform.

Other pages also present customer case studies, code examples, how to contact the Tock community, etc.
---
title: API
---

# Programming journeys in Kotlin

The _Tock Studio_ interfaces allow you to create relatively simple bots and journeys, such as
_decision trees_ and answers to common questions. This is sufficient for many conversational
use cases.

However, it is possible to build more complex responses and journeys:

* Connect to a user account

* Aggregate information from business repositories

* Call the services of the _IS (Information System)_ in an organization

* Integrate external APIs to enrich your journeys with third-party services

* Perform actions and _transactions_: create tickets, payments, etc.

* Implement specific management rules and behaviors

* Optimize the sequences between intentions

To build complex journeys, _Tock_ offers several integration modes intended for
different development languages ​​and frameworks.

In this guide, you will use the [Kotlin](https://kotlinlang.org/) language and the
_WebSocket_ mode to add an intent to a bot initiated in _Tock Studio_.

If you want, you can skip this step and [deploy a platform with Docker](../guides/platform.md).

## What you will create

* A Tock _intention_ developed with the [Kotlin](https://kotlinlang.org/) language

* A program connecting to the bot in _WebSocket_ to enrich it with programmed paths

## Prerequisites

* About 10 minutes

* A functional Tock bot (for example following the [first Tock bot](../guides/studio.md) guide)

* A development environment (or _IDE_) supporting [Kotlin](https://kotlinlang.org/), for example
[IntelliJ](https://www.jetbrains.com/idea/) with recent versions of [JDK](https://jdk.java.net/)
and [Maven](https://maven.apache.org/)

> If you don't want to use an _IDE_, or Maven, no problem. It is quite possible to do the same
>exercise with other tools.
>
> It is also possible to use other ways of developing than the _WebSocket_ mode and other


## Create a Kotlin program with the Tock dependency

There are many ways to create a project in Kotlin.

Add to the _classpath_ the `tock-bot-api-websocket` library for the _WebSocket_ mode.

If you are using [Apache Maven](https://maven.apache.org/), here is an example of a _POM_ (`pom.xml`) for Kotlin with
the `tock-bot-api-websocket` dependency included:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>test</groupId>
    <artifactId>tock-kotlin-websocket</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <plugin.kotlin.version>1.3.41</plugin.kotlin.version>
        <plugin.source.version>3.1.0</plugin.source.version>
        <lib.tock.version>24.9.4</lib.tock.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>ai.tock</groupId>
            <artifactId>tock-bot-api-websocket</artifactId>
            <version>${lib.tock.version}</version>
        </dependency>
    </dependencies>

    <build>
        <sourceDirectory>${project.basedir}/src/main/kotlin</sourceDirectory>
        <plugins>
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <version>${plugin.kotlin.version}</version>
                <executions>
                    <execution>
                        <id>compile</id>
                        <phase>compile</phase>
                        <goals>
                            <goal>compile</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>test-compile</id>
                        <phase>test-compile</phase>
                        <goals>
                            <goal>test-compile</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-source-plugin</artifactId>
                <version>${plugin.source.version}</version>
                <executions>
                    <execution>
                        <id>attach-sources</id>
                        <phase>verify</phase>
                        <goals>
                            <goal>jar-no-fork</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

> You can find this code and other examples in the [tock-bot-samples](https://github.com/theopenconversationkit/tock-bot-samples) repository.

## Create a function that connects to Tock

* Create a Kotlin file (e.g. in `src/main/kotlin/StartWebSocket.kt)

* Edit it with the following code:

```kotlin
import ai.tock.bot.api.client.newBot
import ai.tock.bot.api.client.newStory
import ai.tock.bot.api.websocket.startWithDemo

fun main() {
    startWithDemo( // Integrate with the Tock demo platform by default
        newBot(
            "PUT-YOUR-TOCK-APP-API-KEY-HERE", // Get your app API key from Bot Configurations in Tock Studio
            newStory("qui-es-tu") { // Answer for the 'qui-es-tu' story
                send("Je suis un assistant conversationnel construit avec Tock")
                end("Comment puis-je aider ?")
            }
        )
    )
}
```

> You can find this code (and other examples) in the [tock-bot-samples](https://github.com/theopenconversationkit/tock-bot-samples) repository.

* Replace the API key with the one from your own Tock application. To do this, in _Tock Studio_,
go to _Configuration_ > _Bot Configurations_ and report the _API Key_ value in the code.

* Run the function (_main_) in your development environment. You should see a log line
like this one:

```
[main] INFO ai.tock.bot.api.websocket.BotApiWebSocketClient - start web socket client: {...}
```

> Check if other logs from `BotApiWebSocketClient` indicate any errors. If so,
> it may be an API key configuration error.

## Finish the configuration in *Tock Studio*

* Go back to Tock and go to _Stories & Answers_ > _Stories_

* Uncheck the _Only Configured Stories_ option. You will then see all the journeys, including the "who-are-you" that you just
programmatically declared

* Go to _Test_ > _Test the Bot_ and enter one or more sentences like "who are you?" for example.

You will notice that the bot is not yet answering this question - it may even be answering another
intent. There is indeed some configuration to be done for the _qualification_ to work.

At this point, the journey does exist in Tock, but the _intent_ has not been created automatically.
You can check this by looking at the list of available intents in _Language Understanding_ > _Intents_ > _build_
(the default category).

> This point will be improved soon ([issue #533](https://github.com/theopenconversationkit/tock/issues/533)).

* Go to _Language Understanding_ > _Inbox_, for the last sentence you just entered:

* Change the intent to _New intent_

* Name it "who-are-you" as in the code (so that the link is made)

* Create the intent with _Create_

* Then finish qualifying the sentence with _Validate_

* If you have entered other sentences for this intent, for each of them select the intent in the
list then confirm with _Validate_

* Go back to _Test_ > _Test the Bot_. If you ask the question again, the bot now gives you the answer
built into the Kotlin code (ie. "I am an assistant...").

## Congratulations!

You have just configured your first programmatic _story_ in Kotlin.

In this way, you can take full advantage of the possibilities of a programming language to
build all kinds of simple and complex paths, query third-party APIs, implement
business rules, etc.

> If you program a _story_ already defined in _Tock Studio_, it is the definition present in _Tock Studio_
>that is used to build the answers at runtime.

## Continue...

In the next section you will learn how to:

* [Deploy a Tock platform](../guides/platform.md) in a few minutes with Docker


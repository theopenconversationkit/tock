---
title: Bot API
---

# _Tock Bot API Mode_

> This is the recommended way to start developing stories with Tock. 

[Kotlin](https://kotlinlang.org/), [Javascript](https://nodejs.org/) and [Python](https://www.python.org/) clients are available.
Any programming language can be used, leveraging the Tock [Bot API](../api.md#tock-bot-definition-api). 

[<img alt="Kotlin logo" title="Kotlin"
      src="https://upload.wikimedia.org/wikipedia/commons/7/74/Kotlin_Icon.png" 
      style="width: 50px;">](../bot-api#develop-with-kotlin)
[<img alt="Nodejs logo" title="Nodejs"
      src="https://www.boostit.net/wp-content/uploads/2016/08/node-js-icon.png" 
      style="width: 50px;">](../bot-api#develop-with-javascript)
[<img alt="Python logo" title="Python"
      src="https://www.libraries.rutgers.edu/sites/default/files/styles/resize_to_300px_width/public/events/2020/01/python_3_2.png" 
      style="width: 50px;">](../bot-api#develop-with-python)
[<img alt="API logo" title="Bot API"
      src="https://zappysys.com/blog/wp-content/uploads/2018/06/REST-API-icon.jpg" 
      style="width: 50px;">](../bot-api#develop-through-the-api)

## Connect to the demo platform

Rather than deploying its own Tock platform, it is possible to test the _WebSocket_ or _Webhook_ modes directly on the
[Tock demo platform](https://demo.tock.ai/).

## Develop with Kotlin

<img alt="Kotlin logo" title="Kotlin"
src="https://upload.wikimedia.org/wikipedia/commons/7/74/Kotlin_Icon.png" 
style="width: 100px;">

### Enable WebSocket mode

> This is the preferred mode at startup, requiring no additional tunnel / setup.

To use the _WebSocket_ client, add the `tock-bot-api-websocket` dependency to your [Kotlin](https://kotlinlang.org/) application / project.

Using [Maven](https://maven.apache.org/) :

```xml
        <dependency>
            <groupId>ai.tock</groupId>
            <artifactId>tock-bot-api-websocket</artifactId>
            <version>24.3.5</version>
        </dependency>
```

Or [Gradle](https://gradle.org/) :

```groovy
      compile 'ai.tock:tock-bot-api-websocket:24.3.5'
```

### Enable WebHook mode

Alternatively, you can choose to use the _WebHook_ client.
Add the `tock-bot-api-webhook` dependency to your [Kotlin](https://kotlinlang.org/) application / project.

Using [Maven](https://maven.apache.org/) :

```xml
        <dependency>
            <groupId>ai.tock</groupId>
            <artifactId>tock-bot-api-webhook</artifactId>
            <version>24.3.5</version>
        </dependency>
```

Or [Gradle](https://gradle.org/) :

```groovy
      compile 'ai.tock:tock-bot-api-webhook:24.3.5'
```

In this case, unlike the _WebSocket_ mode, the bot application must be reachable by the
Tock platform and so has to expose a public URL (you can use [ngrok](https://ngrok.com/) in order to provide this URL). 

This URL must be specified in the _webhook url_ field in the _Configuration_> _Bot Configurations_ view of _Tock Studio_.

> Note that the _WebHook_ mode may require the setup of a secure tunnel to the Bot API running client. 
> As a matter of fact, the client may not be reachable from the server. 
> For development, tools like [ngrok](https://ngrok.com/) may help. 
 
### Set up the API key
 
In _Tock Studio_, after configuring a bot, go to _Configuration_> _Bot Configurations_ and copy
the API key of the bot to which you want to connect.
 
You can enter / paste this key into the Kotlin code (see below).
 
### Create Stories
 
The following formats are supported:


* Text with Buttons (Quick Replies)
* "Card" format
* "Carousel" format
* Specific channel formats like Messenger format, Slack format, etc.

Here is a simple bot with a few stories:
 
```kotlin
fun main() {
    startWithDemo(
        newBot(
            "PUT-YOUR-TOCK-APP-API-KEY-HERE", // Get your app API key from Bot Configurations in Tock Studio
             newStory("greetings") { // Intent 'greetings'
                 end("Hello!") // Raw text answer
             },
             newStory("location") { // Intent 'location'
                 end(
                    // Anwser with a card - including text, file(image, video,..) and user action suggestions
                     newCard(
                         "Card title",
                         "Card sub title",
                         newAttachment("https://url-image.png"),
                         newAction("Action 1"),
                         newAction("Action 2", "http://redirection") 
                     )
                 )
             },
             newStory("goodbye") { // Intent 'goodbye'
                 end {
                     // Answer with Messenger-specific button/quick reply
                     buttonsTemplate("Are you sure ?", nlpQuickReply("Stay here"))
                 } 
             },
             // Fallback answer when the bot does find a correct response
             unknownStory {
                 end("Sorry I don't understand :(") 
             }
        )
    )
}
```

Please consult the [full source code sample](https://github.com/theopenconversationkit/tock-bot-demo).
 
## Develop with Javascript

<img alt="Nodejs logo" title="Nodejs"
src="https://www.boostit.net/wp-content/uploads/2016/08/node-js-icon.png" 
style="width: 100px;">

A [Nodejs](https://nodejs.org/) client is available to program Tock stories in Javascript.  
Please visit the [`tock-node`](https://github.com/theopenconversationkit/tock-node) repository and documentation.
 
## Develop with Python

<img alt="Python logo" title="Python"
src="https://www.libraries.rutgers.edu/sites/default/files/styles/resize_to_300px_width/public/events/2020/01/python_3_2.png" 
style="width: 100px;">

A client is available to program Tock stories in [Python](https://www.python.org/).  
Please visit the [`tock-py`](https://github.com/theopenconversationkit/tock-py) repository and documentation.

## Develop through the API

<img alt="API logo" title="REST API"
src="https://zappysys.com/blog/wp-content/uploads/2018/06/REST-API-icon.jpg" 
style="width: 100px;">

It is possible to develop in the language of your choice by using directly the underlying REST  
[API](../../api#tock-bot-definition-api).

## Install Bot API server-side

To use Tock's _Bot API_ mode without the demo platform, a specific module must be deployed on your own server. 

Called `bot-api` in Docker Compose descriptors, this service:

* Expose the _Bot API_ to the potential customers whatever their programming language are.
* Accept _WebSocket_ connections and / or connections to the configured webhook.

Compared to the "demo mode", The only required change in the code is to replace
the `startWithDemo` method with the `start` one, specifying the `bot-api` target server address.

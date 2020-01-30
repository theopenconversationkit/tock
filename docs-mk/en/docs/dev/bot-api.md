# _Tock Bot API Mode_

This is the recommended way to start to develop with Tock. 

You add custom answers using a REST API. Kotlin client and [Node client](https://github.com/theopenconversationkit/tock-node) are available.

## Connect to the demo platform

Rather than deploying its own Tock platform, it is possible to test the _WebSocket_ or _Webhook_ modes directly on the
[Tock demo platform](https://demo.tock.ai/).

## Develop with Kotlin

### Enable WebSocket mode

This is the preferred mode at startup.

To use the websocket client, add the `tock-bot-api-websocket` dependency to your [Kotlin](https://kotlinlang.org/) application / project.

Using [Maven](https://maven.apache.org/) :

```xml
        <dependency>
            <groupId>ai.tock</groupId>
            <artifactId>tock-bot-api-websocket</artifactId>
            <version>19.9.3</version>
        </dependency>
```

Or [Gradle](https://gradle.org/) :

```gradle
      compile 'ai.tock:tock-bot-api-websocket:19.9.3'
```

### Enable WebHook mode

Alternatively, you can choose to use the _WebHook_ client.
Add the `tock-bot-api-webhook` dependency to your [Kotlin](https://kotlinlang.org/) application / project.

Using [Maven](https://maven.apache.org/) :

```xml
        <dependency>
            <groupId>ai.tock</groupId>
            <artifactId>tock-bot-api-webhook</artifactId>
            <version>19.9.3</version>
        </dependency>
```

Or [Gradle](https://gradle.org/) :

```gradle
      compile 'ai.tock:tock-bot-api-webhook:19.9.3'
```

In this case, unlike the _WebSocket_ mode, the bot application must be reachable by the
Tock platform and so has to expose a public URL (you can use [ngrok](https://ngrok.com/) in order to provide this URL). 

This URL must be specified in the _webhook url_ field in the _Configuration_> _Bot Configurations_ view of _Tock Studio_.
 
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
 
## Develop in another language

### Node

Please consult the dedicated [node client](https://github.com/theopenconversationkit/tock-node) documentation.

### API

It is possible to develop in the language of your choice by using directly the underlying REST API.

### Install Bot API on your own servers

To use Tock's _Bot API_ mode without the demo platform, a specific module must be deployed on your own server. 

Called `bot-api` in Docker Compose descriptors, this service:

* Expose the _Bot API_ to the potential customers whatever their programming language are.
* Accept _WebSocket_ connections and / or connections to the configured webhook.

Compared to the "demo mode", The only required change in the code is to replace
the `startWithDemo` method with the `start` one, specifying the `bot-api` target server address.
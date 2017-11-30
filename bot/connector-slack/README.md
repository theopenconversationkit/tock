# Tock Slack Connector

This connector is quite basic, and does not yet support every feature available on Slack API.

## Install:

On your Slack Workspace, add an incoming webhook and an outgoing webhook.

### Incoming webhook
You will be given a slack url "Webhook URL". This URL will look like this:

https://hooks.slack.com/services/slackToken1/slackToken2/slackToken3


slackToken1 is your Slack Workspace id.
slackToken2 is your Slack Webhook id.
slackToken3 is the authentication token, and the only token that can be regenerated.

Then you can configure your connector for your bot:


```kotlin
object MyBotConfiguration {
    fun registerSlackConnector() {
        addSlackConnector("my-bot-id",
                "/path-to-my-slack-bot-http-endpoint",
                "my-bot-name",
                "slackToken1",
                "slackToken2",
                "slackToken3"
                )
    }
}
```

### Outgoing webhook:

You just have to give slack a url to contact your bot.
Say your tock bot installation lies at http://my-tock-bot-domain-name, then in our case (with the above configuration) the url will be:

```
http://my-tock-bot-domain-name/path-to-my-slack-bot-http-endpoint
```

Your slack connector is ready to be used !!

### How to use
##### Simple message:
```kotlin
val greetings = story("hello") { bus ->
    with(bus) {
        resetDialogState()
        send("Hello !")
    }
}
```

##### Multiline message

```kotlin
val thankyou = story("thankyou") { bus ->
    with(bus) {
        withSlack {
            multiLineMessage(listOf("You're", "welcome", "!"))
        }
        end()
    }
}
```

##### Attachment message
RTF (Rich Text Format) is a feature of Slack, you can find a documentation [here](https://api.slack.com/docs/message-formatting)

```kotlin
val attachmentStory = story("attachment") { bus ->
    with(bus) {
        val attachments = arrayOf(attachmentField("title", "value"), attachmentField("title", "value"))
        withSlack {
            attachmentMessage(
                    *attachments,
                    fallback = "fallback",
                    text = "text"
            )
        }
        end()
    }
}
```
##### Emojis

Slack connector for Tock supports a few slack emojis, more will be added

###### Just an emoji without text:

```kotlin
val story = story("story") { bus ->
    with(bus) {
        withSlack {
            emojiMessage(SlackEmoji.SMILE)
        }
        end()
    }
}
```

###### Emoji and text:

```kotlin
val story = story("story") { bus ->
    with(bus) {
        withSlack {
            textMessage("hello! ${emoji(SlackEmoji.SMILE)}")
        }
        end()
    }
}
```

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

* Then you can configure your connector for your bot: go to the Configuration -> Bot Configurations menu in the Tock Bot administration interface, and create a new configuration.


### Event Subscriptions:

Enable Event Subscriptions as described here: https://api.slack.com/events-api#subscriptions.
Say your tock bot installation lies at http://my-tock-bot-domain-name, then in our case (with the above configuration) the url will be:

```
http://my-tock-bot-domain-name/[relative path of bot configuration]
```

You need also to set the same url for 'Interactive Components' menu in order to handle button clicks.

Then you just have to create a Bot User and to subscribe to the *message.channels* Bot Event.

Invite the Bot user to the channel.

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
        withSlack {
            slackMessage(
                         "Hey!",
                         slackAttachment(
                                "You know what?",
                                slackButton("Itineraries", search),
                                slackButton("Departures", Departures),
                                slackButton("Arrivals", Arrivals)
                            )
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

### Other options

Look at [KDoc](https://voyages-sncf-technologies.github.io/tock/dokka/tock/fr.vsct.tock.bot.connector.slack/index.html)
 for other options

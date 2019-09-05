# Tock Twitter Connector

This connector only supports:

 - Direct Message API (https://developer.twitter.com/en/docs/direct-messages/api-features)
 - Post and engage API for posting Tweet only (https://developer.twitter.com/en/docs/tweets/post-and-engage/overview)
 - Media API (https://developer.twitter.com/en/docs/media/upload-media/overview)

## Apply for a develop account

https://developer.twitter.com/en/apply/user

## Create an application

https://developer.twitter.com/en/apps

## Configure Access permission

Enable "Read, write, and direct messages" permission

##  Setup a dev environment for the subscription API

https://developer.twitter.com/en/account/environments

## Configure connector

You will be given:

 - Application ID
 - Account Id (You can find it here > https://twitter.com/settings/your_twitter_data/account)
 - environment name
 - Consumer API Key
 - Consumer API Secret
 - Access Token
 - Access Token secret
 

Access token is associated to your developer account by default.

* Then you can configure your connector for your bot: go to the Configuration -> Bot Configurations menu in the Tock Bot administration interface, and create a new configuration.

## Create a webhook and validate it (using twurl)

### Install `twurl` on your machine
* See https://github.com/twitter/twurl for details
* Get the bot URL 

### Authorize twurl using your app

    twurl authorize --consumer-key xxxxxxxxx --consumer-secret yyyyyyyy

### Registering a webhook in "develop" environment

    twurl -X POST "/1.1/account_activity/all/develop/webhooks.json?url=https://botUrl/aiv-twitter"

### List webhooks in "develop" environment

    twurl "/1.1/account_activity/all/develop/webhooks.json"

    [{"id":"1234","url":"https://botUrl/aiv-twitter","valid":true,"created_timestamp":"2019-02-25 10:59:12 +0000"}]


### Subscribe current twitter account on "develop" environment

    twurl -X POST "/1.1/account_activity/all/develop/subscriptions.json"

### How to use
#### Private conversation (Direct message)
##### Simple message:

```kotlin
val greetings = story("hello") { bus ->
    end {
        resetDialogState()
        withTwitter {
            directMessage("Hello !")
        }
    }
}
```

##### Quick replies
```kotlin
val greetings = story("hello") { bus ->
    end {
        resetDialogState()
        withTwitter {
            directMessageWithOptions(
                "hello",
                option("option 1", "description option 1", intent1),
                option("option 2", "description option 2", intent2)
            )
        }
    }
}
```

##### URL Button
```kotlin
val greetings = story("hello") { bus ->
    end {
        resetDialogState()
        withTwitter {
            directMessageWithButtons("Hello !",
                webUrl(
                    "label",
                    "http://oui.sncf"
                )
            )
        }
    }
}
```

#### Public conversation (limited)
The current implementation listens only the top level tweets (no reply), the basic use case is to invite someone to chat with Direct Message

##### Simple message
```kotlin
val greetings = story("hello") { bus ->
    end {
        resetDialogState()
        withPublicTwitter {
            tweet("Hello !")
        }
    }
}
```

##### Simple message with invitation for DM
```kotlin
val greetings = story("hello") { bus ->
    end {
        resetDialogState()
        tweetWithInviteForDM {
            tweet("Hello !")
        }
    }
}
```

##### End the conversation only if the connector is twitter and the conversation is public
```kotlin
val greetings = story("hello") { bus ->
    end {
        resetDialogState()
        tweetWithInviteForDM {
            tweet("Hello !")
        }
        endIfPublicTwitter()
    }
}
```

##### How to test public Tweet
```kotlin
@Test
internal fun myTest() {
    val expected = Tweet("Hello !")
    
    bus.connectorType = twitterConnectorType
    
    ext.send(metadata = ActionMetadata(visibility = ActionVisibility.public)) {
        firstAnswer.assert(expected)
    }
}
```
# Tock Twitter Connector

This connector is quite basic, and does not yet support every feature available on Twitter API.

In fact this connector only supports Direct Message API.

https://developer.twitter.com/en/docs/direct-messages/api-features

## Apply for a develop account

https://developer.twitter.com/en/apply/user

## Create an application

https://developer.twitter.com/en/apps

## Configure Access permission

Enable "Read, write, and direct messages" permission

##  Setup a dev environment for the subscription API

https://developer.twitter.com/en/account/environments

### Configure connector

You will be given:

 - Application ID
 - environment name
 - Consumer API Key
 - Consumer API Secret
 - Access Token
 - Access Token secret
 
 
Access token is associated to your developer account by default.

* Then you can configure your connector for your bot: go to the Configuration -> Bot Configurations menu in the Tock Bot administration interface, and create a new configuration.

### How to use
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

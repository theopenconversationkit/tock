## Prerequisites

* You need to retrieve from Facebook these values:

    * **Application id** : The Messenger application id.  
    * **Page id** : The Facebook page id.
    * **Call Token** : The token allowing the messenger app to call the Facebook page.
    * **Webhook token** : A token (choose what you want) used when registering the webhook in the Messenger admin interface.
    * **Secret** : the messenger application secret key.

* Then go to the Configuration -> Bot Configurations menu in the Tock Bot administration interface,
 and create a new configuration with these parameters. 

For a more detailed guide, refer to https://doc.tock.ai/tock/master/guides/messenger.html
 
## Bot API 
 
* Take the bot demo url (ie  https://demo.tock.ai ) and use it in the webhook interface of messenger settings, to specify :
    * the url : https://demo.tock.ai/io/xxxx/new_assistant/messenger (where */io/xxxx/new_assistant/messenger* is the relative path of the connector)
    * the webhook token you set
    
-> The bot is ready !
 
## Integrated mode

In order to connect your bot with a messenger bot application, you need a Messenger application with "messages" and "messaging_postbacks" webhook events activated (look at the [Facebook documentation](https://developers.facebook.com/docs/messenger-platform/guides/quick-start)). 

* A secure ssl tunnel (for example [ngrok](https://ngrok.com/)) is required to test the bot in dev mode:

```sh 
    ngrok http 8080
``` 

* Take the ngrok value (ie  https://xxxx.ngrok.io ) and use it in the webhook interface of messenger settings, to specify :
   * the url : https://xxxx.ngrok.io/messenger (where /messenger is the relative path of the connector)
   * the webhook token you set
   
* You can use a script to automatically up ngrok and updating webhook (https://github.com/theopenconversationkit/tock/scripts/connector-messenger/update.sh)

* A Messenger integration sample is available in the [open data Bot](https://github.com/theopenconversationkit/tock-bot-open-data) source code

* The documentation of the messenger builders is available in [KDoc format](https://theopenconversationkit.github.io/tock/dokka/tock/ai.tock.bot.connector.messenger/index.html)

-> The bot is ready !

## Handover protocol & orchestration

Tock supports subscription of many bots to the same messenger app, linked to a different page each. 

You need to set a different path for each connector configuration ( there is an unique constraint restriction ).

When subscribing to the messenger facebook, just use one of these connector paths. Other bots will work automatically.

## Proxy authentication

TOCK uses the default proxy selector for calls to the Messenger API.
If the bot is deployed behind an HTTP proxy that requires authentication to perform these calls,
you will need to set credentials in the form of the `tock_proxy_user` and `tock_proxy_password` properties (only Basic auth is supported).
These properties can be set either as Java system properties or as environment variables.

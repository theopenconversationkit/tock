In order to connect your bot with a messenger bot application, you need a Messenger application with "messages" and "messaging_postbacks" webhook events activated (look at the [Facebook documentation](https://developers.facebook.com/docs/messenger-platform/guides/quick-start)). 

* A secure ssl tunnel (for example [ngrok](https://ngrok.com/)) is required to test the bot in dev mode:

```sh 
    ngrok http 8080
``` 

* You need to retrieve from Facebook these values:

    * **pageId** : the Facebook page id.
    * **pageToken** : a token for the messenger app and the Facebook page.
    * **applicationSecret** : the messenger application secret key.
    * **webhookVerifyToken** : a webhook verify token you choose.

* Then add in your code:

```kotlin

addMessengerConnector( 
    pageId,
    pageToken,
    applicationSecret,
    webhookVerifyToken
    )
```

* Take the ngrok value (ie  https://xxxx.ngrok.io ) and use it in the webhook interface of messenger settings, to specify :
   * the url : https://xxxx.ngrok.io/messenger
   * the verify token you set in tock_bot_open_data_webhook_verify_token env var

* Full documentation is available in [KDoc format](https://voyages-sncf-technologies.github.io/tock/dokka/tock/fr.vsct.tock.bot.connector.messenger/index.html)

* A Messenger integration sample is available in the [open data Bot](https://github.com/voyages-sncf-technologies/tock-bot-open-data) source code
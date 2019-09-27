In order to connect your bot with a messenger bot application, you need a Messenger application with "messages" and "messaging_postbacks" webhook events activated (look at the [Facebook documentation](https://developers.facebook.com/docs/messenger-platform/guides/quick-start)). 

* A secure ssl tunnel (for example [ngrok](https://ngrok.com/)) is required to test the bot in dev mode:

```sh 
    ngrok http 8080
``` 

* You need to retrieve from Facebook these values:

    * **Application id** : The Messenger application id.  
    * **Page id** : The Facebook page id.
    * **Call Token** : The token allowing the messenger app to call the Facebook page.
    * **Webhook token** : A token (choose what you want) used when registering the webhook in the Messenger admin interface.
    * **Secret** : the messenger application secret key.

* Then go to the Configuration -> Bot Configurations menu in the Tock Bot administration interface,
 and create a new configuration with these parameters. Choose also the relative rest path of the messenger bot (e.g. /messenger). 

* Take the ngrok value (ie  https://xxxx.ngrok.io ) and use it in the webhook interface of messenger settings, to specify :
   * the url : https://xxxx.ngrok.io/messenger
   * the webhook token you set
   
* You can use a script to automatically up ngrok and updating webhook (https://github.com/theopenconversationkit/tock/scripts/connector-messenger/update.sh)

* A Messenger integration sample is available in the [open data Bot](https://github.com/theopenconversationkit/tock-bot-open-data) source code

* The documentation of the messenger builders is available in [KDoc format](https://theopenconversationkit.github.io/tock/dokka/tock/fr.vsct.tock.bot.connector.messenger/index.html)

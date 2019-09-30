In order to connect your bot with a Google Assistant application,
 you need a Google account and a Google Actions project with Actions sdk setup - see https://developers.google.com/actions/sdk/create-a-project
 
* A secure ssl tunnel (for example [ngrok](https://ngrok.com/)) is required to test the bot in dev mode:

```sh 
    ngrok http 8080
``` 

* A sample action project configuration is available here : [google_actions_en.json](https://raw.githubusercontent.com/theopenconversationkit/tock-bot-open-data/master/src/main/resources/google_actions_en.json)- you will need to set the ngrok host in dev mode and to replace to bot_open_data id with your bot id (*app* by default).

* Then go to the Configuration -> Bot Configurations menu in the Tock Bot administration interface, and create a new configuration

* A Google Assistant integration sample is available in the [open data Bot](https://github.com/theopenconversationkit/tock-bot-open-data) source code

* The documentation of the google assistant builders is available in [KDoc format](https://theopenconversationkit.github.io/tock/dokka/tock/ai.tock.bot.connector.ga/index.html)

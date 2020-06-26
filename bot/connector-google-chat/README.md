## Prerequisites

* You need to create and publish a Google  Chat bot :  https://developers.google.com/hangouts/chat/how-tos/bots-publish

* You need to retrieve from Google API these elements:

    * **Bot project number** : The Google identifier of your bot.  
    * **Json Credential** : The json credential page generated when you added the service accunt with Project Owner role.
    
* Then go to the Configuration -> Bot Configurations menu in the Tock Bot administration interface,
 and create a new Hangout Chat configuration with these parameters : inquire either serviceCredentialPath if you added json credential to your integrated bot resources or serviceCredentialContent with json credential file content. 
 
## Bot API 
 
* TO BE IMPLEMENTED AND TESTED
 
## Integrated mode

In order to connect your bot with a google chat bot application, you need to configure your bot url in Google API. It should match your local bot url with path specified in configuration 

* On your local machine, you can configure a secure ssl tunnel (for example [ngrok](https://ngrok.com/)) is required to test the bot in dev mode:

```sh 
    ngrok http 8080
``` 

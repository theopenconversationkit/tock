# Configure a bot for Messenger

If you followed the guide [Create your first bot with _Tock Studio_](studio.md), you created a connector with type Slack.

In this guide, you will create a configuration for [Facebook Messenger](https://facebook.com/messenger/) 
and set up the bot to chat with it on this application.

## What you will build

* A configuration (in both Facebook and Tock) to send and receive messages through _Messenger_

* A bot who can chat on a Facebook _page_ or on [Messenger](https://www.messenger.com/)

## What you need

* About 20 minutes

* A working Tock bot (e.g. following the [first Tock bot](studio.md) guide)

* A [Facebook Developer](https://developers.facebook.com/) account

## Create a Facebook page

* Create a Facebook page

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-page-0.png" alt="Créer une page partie 1"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-page-1.png" alt="Créer une page partie 2"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 33%;" />

* Give it a name (e.g. _My Tock Bot_)

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-page-2.png" alt="Créer une page partie 3"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

> Our advice: do not publish the page to limit its access by Messenger users:
  _Settings > General > Page Visibility > **Page unpublished**_

## Create a Facebook App

_For more details on creating an App, you can read the [Facebook Developer Documentation](https://developers.facebook.com/docs/development/create-an-app)._

* Go on the page [Facebook for developers > My Apps](https://developers.facebook.com/apps/)

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-app-0.png" alt="Créer une application partie 1"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

* _Create App_ > *Manage Business Integrations*

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-app-1.png" alt="Créer une application partie 2"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

* Set your app name and email

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-app-2.png" alt="Créer une application partie 3"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 75%;" />

* Add a product: _Messenger_

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/add-messenger-page-0.png" alt="Ajouter messenger à une application partie 1"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/add-messenger-page-1.png" alt="Ajouter messenger à une application partie 2" style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width:  40%;" />



## Create a Messenger Connector

* In _Tock Studio_ go to _Settings_ > _Configurations_:

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-connector-0.png"
alt="Créer un connecteur messenger partie 1"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 75%;" />

* Create a connector with type _Messenger_ and open the _Connector Custom Configuration_ section

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-connector-1.png"
alt="Créer un connecteur messenger partie 2"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 75%;" />

* Fill in the fields (see below for each field)

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/connect-tock-0.png" alt="Connecter Tock partie 1"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 75%;" />

* _Application Id_: you can find it on your app's settings page at [https://developers.facebook.com](https://developers.facebook.com)

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/app-id.png" alt="Trouver l'id d'application"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width:  75%;" />

* _Page Id_: you can find it on the page you made for your app at [https://facebook.com](https://facebook.com)

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/page-id-0.png" alt="ID de page partie 1"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width:  75%;" />

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/page-id-1.png" alt="ID de page partie 2"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 75%;" />

* _Call Token_: this token can be found on your app's settings page at [https://developers.facebook.com](https://developers.facebook.com)

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/generate-token.png" alt="Générer un token"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width:  75%;" />

* _Webhook Token_: choose an arbitrary token (even `token` if you so wish) and note it down for later -
  Facebook will use it to call the Tock webhook

* _Secret_: you can copy it from your app's settings page at [https://developers.facebook.com](https://developers.facebook.com)

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/app-secret.png" alt="Secret"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

* _Persona Id_: you can leave this field empty

* Check that the connector's configuration is correctly saved

## Configure the Callback URL

* Go back to the configuration page for your application at [https://developers.facebook.com](https://developers.facebook.com):
  _Products_ > _Messenger_ > _Settings_ > **_Webhooks_**

* Click on _Setup Webhooks_

* Input the URL at which your Tock instance is currently deployed, and the webhook token you chose earlier
  while configuring the connector

> To retrieve your connector's URL, go on the configuration page _Tock Studio_ > _Settings_ > _Configurations_,
> unroll your Messenger connector's configuration, and concatenate the content of the field _Application base url_ with
> that of the field _Relative REST path_.

* Click on _Verify and Save_

* Test your bot on Messenger!


## Congratulations!

Your bot can now chat on Messenger, as well as on other channels you added connectors for.

The conversational model, features and personality of your assistant are built and remain independent of the channels on which the bot is present. However, nothing prevents you from creating
specific paths or responses for a particular channel, as you will see through the various
different Tock tools: _Responses_ handling screen, activation of intentions on the channels of your choice 
with the _Story Rules_ screen, use of _DSLs_ and of the _Bot API_ to take advantage of specific graphical components, etc.


## Continue...

To learn more about the Messenger connector bundled with Tock, you can take a look at the
[connector-slack](https://github.com/theopenconversationkit/Tock/tree/master/bot/connector-slack) directory on GitHub, 
where you will find sources and the connector's _README_.

To learn more about _Tock Studio_ and about Tock's features and deployment modes, you can also 
browse the more complete [user guide](../toc.md).


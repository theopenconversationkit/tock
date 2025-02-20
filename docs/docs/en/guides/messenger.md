---
title: Messenger
---
# Configure your bot for Messenger

If you followed the guide [Create your first bot with _Tock Studio_](../guides/studio.md), you declared a Slack-type connector.

In this guide, you will create a configuration for [Facebook Messenger](https://fr-fr.facebook.com/messenger/)
and integrate the bot to communicate with it on this social network.

If you wish, you can also skip this step and go directly to [the rest](../guides/api.md).

## What you will create

* A configuration (in Facebook and in Tock) to receive and send messages via Messenger

* A bot that speaks on a Facebook _page_ or in [Messenger](https://www.messenger.com/)

## Prerequisites

* About 20 minutes

* A functional Tock bot (for example following the guide [first Tock bot](../guides/studio.md))

* A [Facebook Developer](https://developers.facebook.com/) account

## Create a Facebook page

* Create a Facebook page

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-page-0.png" alt="Create a page part 1"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-page-1.png" alt="Create a page part 2"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 33%;" />

* Give it a name (e.g. _My Tock Bot_)

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-app-2.png" alt="create an app part 3" 
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 75%;" />

> Recommendation: do not publish the page to limit its access to Messenger users:
_Settings > General > Page visibility > **Not published**_

## Create a Facebook application

* Go to the page [Facebook for developers > See all apps](https://developers.facebook.com/apps/)

* _Add an app_


<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-app-0.png" alt="Créer une application partie 1"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

 _Create an app_ > _Manage professional integrations_


<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-app-1.png" alt="Créer une application partie 2"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

* Enter a name for the _application_
<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-app-2.png" alt="Créer une application partie 3" 
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 75%;" />
* Add a product: _Messenger_

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/add-messenger-page-0.png" alt="Ajouter messenger à une application partie 1"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/add-messenger-page-1.png" alt="Add messenger to an application part 2" style="box -shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 40%;" />

## Create a Messenger connector

* In _Tock Studio_ go to _Settings_ > _Configurations_ :

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-connector-0.png "
alt="Create a messenger connector part 1"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 75%;" />

* Create a connector of type _Messenger_ and open the _Connector Custom Configuration_ section

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-connector-1.png"
alt="Create a messenger connector part 2"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 75%;" />

* Complete the fields (see below field by field):

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/connect-tock-0.png" alt ="Connect Tock part 1"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 75%;"/>

* _Application Id_ : it is found on your application page on [https://developers.facebook.com](https://developers.facebook.com)


<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/app-id.png" alt="Trouver l'id d'application" 
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width:  75%;" />

* _Page Id_ : it is on the page linked to your application on [https://facebook.com](https://facebook.com)

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/page-id-0.png" alt="ID de page partie 1" 
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width:  75%;" />

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/page-id-1.png" alt="Page ID part 2"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 75%;" />

* _Call Token_: The token can be found on your app page on [https://developers.facebook.com](https://developers.facebook.com)

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-app-0.png" alt="Créer une application partie 1"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

* _Webhook Token_ : choose any token (even `token` if you want) and write it down for later -
every call to Tock from Facebook will be made passing this token

* _Secret_ : copy from your user's page application on [https://developers.facebook.com](https://developers.facebook.com)

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-app-1.png" alt="Créer une application partie 2"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />


* _Persona Id_ : you can leave this field empty

* Check that the connector configuration is saved

## Configure the callback URL

* Go back to your app's configuration page on [https://developers.facebook.com](https://developers.facebook.com):
_Products_ > _Messenger_ > _Settings_ > **_Webhooks_**

* Click _Add Callback URL_ (or _Edit Callback URL_ if you previously had a different URL)

* Enter the URL where your Tock connector is currently deployed, and the webhook token you chose
when configuring the connector

> To find your connector's URL, go to the _Tock Studio_ > _Settings_ > _Configurations_ configuration page,
> expand your Messenger connector configuration, and concatenate the contents of the _Application base url_ field and the contents of the
> _Relative REST path_ field.

* Click _Review and save_

* Test your bot on Messenger!

## Congratulations!

Your bot now communicates on Messenger, in addition to the other channels you have integrated it into.

The conversational model, features and personality of your assistant are built and
remain independent of the channels on which the bot is present. However, nothing prevents you from creating
paths or responses specifically for a particular channel, as you will see through
different Tock tools: _Responses_ management screen, activation of intentions on a particular channel
with the _Story Rules_ screen, use of _DSLs_ and the _Bot API_ to take advantage of specific graphical
components, etc.

## Continue...

In the following sections you will learn how to:

* [Create programmed paths in Kotlin](../guides/api.md), opening the way to complex behaviors and
integrating third-party APIs if needed

* [Deploy a Tock platform](../guides/platform.md) in a few minutes with Docker

To learn more about the Messenger connector provided with Tock, go to the
[connector-messenger](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-messenger) folder on GitHub,
where you will find the sources and the _README_ of the connector.


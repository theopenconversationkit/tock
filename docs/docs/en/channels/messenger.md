---
title: Messenger
---
# Messenger connector

The Messenger connector connects a bot to a Facebook page, to talk with the users on
[Facebook Messenger](https://www.messenger.com/).

* **Connector type**: `messenger`
* **Sources and README**: [connector-messenger](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-messenger)

## What you will create

* A configuration (in Facebook and in Tock) to receive and send messages via Messenger

* A bot that speaks on a Facebook _page_ or in [Messenger](https://www.messenger.com/)

## Prerequisites

* About 20 minutes

* A functional Tock bot (for example following the guide [first Tock bot](../getting-started/first-bot-studio.md))

* A [Facebook Developer](https://developers.facebook.com/) account

## Create a Facebook page

* Create a Facebook page

<img src="../img/channels/messenger/create-page-0.png" alt="Create a page part 1"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

<img src="../img/channels/messenger/create-page-1.png" alt="Create a page part 2"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 33%;" />

* Give it a name (e.g. _My Tock Bot_)

<img src="../img/channels/messenger/create-app-2.png" alt="create an app part 3" 
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 75%;" />

> Recommendation: do not publish the page to limit its access to Messenger users:
_Settings > General > Page visibility > **Not published**_

## Create a Facebook application

* Go to the page [Facebook for developers > See all apps](https://developers.facebook.com/apps/)

* _Add an app_


<img src="../img/channels/messenger/create-app-0.png" alt="Créer une application partie 1"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

 _Create an app_ > _Manage professional integrations_


<img src="../img/channels/messenger/create-app-1.png" alt="Créer une application partie 2"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

* Enter a name for the _application_
<img src="../img/channels/messenger/create-app-2.png" alt="Créer une application partie 3" 
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 75%;" />
* Add a product: _Messenger_

<img src="../img/channels/messenger/add-messenger-page-0.png" alt="Ajouter messenger à une application partie 1"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

<img src="../img/channels/messenger/add-messenger-page-1.png" alt="Add messenger to an application part 2" style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 40%;" />

## Create a Messenger connector

* In _Tock Studio_ go to _Settings_ > _Configurations_ :

<img src="../img/channels/messenger/create-connector-0.png"
alt="Create a messenger connector part 1"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 75%;" />

* Create a connector of type _Messenger_ and open the _Connector Custom Configuration_ section

<img src="../img/channels/messenger/create-connector-1.png"
alt="Create a messenger connector part 2"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 75%;" />

* Complete the fields (see below field by field):

<img src="../img/channels/messenger/connect-tock-0.png" alt ="Connect Tock part 1"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 75%;"/>

* _Application Id_ : it is found on your application page on [https://developers.facebook.com](https://developers.facebook.com)


<img src="../img/channels/messenger/app-id.png" alt="Trouver l'id d'application" 
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width:  75%;" />

* _Page Id_ : it is on the page linked to your application on [https://facebook.com](https://facebook.com)

<img src="../img/channels/messenger/page-id-0.png" alt="ID de page partie 1" 
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width:  75%;" />

<img src="../img/channels/messenger/page-id-1.png" alt="Page ID part 2"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 75%;" />

* _Call Token_: The token can be found on your app page on [https://developers.facebook.com](https://developers.facebook.com)

<img src="../img/channels/messenger/create-app-0.png" alt="Créer une application partie 1"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

* _Webhook Token_ : choose any token (even `token` if you want) and write it down for later -
every call to Tock from Facebook will be made passing this token

* _Secret_ : copy from your user's page application on [https://developers.facebook.com](https://developers.facebook.com)

<img src="../img/channels/messenger/create-app-1.png" alt="Créer une application partie 2"
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

## Going further

The conversational model, features and personality of your assistant remain independent of the channels
on which the bot is present. You can nevertheless create answers specifically for a channel: in the
_Answers_ screen, with [story rules](../studio/stories-and-answers.md#story-rules) for a configuration, or with the
Messenger components of the _DSLs_ and of the _Bot API_.

To learn more about the Messenger connector provided with Tock, go to the
[connector-messenger](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-messenger) folder on GitHub,
where you will find the sources and the _README_ of the connector.

---
title: Channels
---

# Building a multichannel bot with Tock

## Notion of *connector*

A Tock _connector_ allows you to integrate a bot into an external communication channel (text or voice).
Aside from the _test connector_ type (used internally by the _Tock Studio_ interface), connectors
are associated with channels external to the Tock platform.

The whole point of Tock connectors lies in the ability to develop conversational assistants
independently of the channel(s) used to talk to it. It is thus possible to create a bot for a channel,
then make it multichannel later by adding connectors.

The _Web connector_ has the particularity of exposing a generic API to interact with a Tock bot.
As a result, it allows even more integrations on the "frontend" side, using this API as a gateway.

This page actually lists:
<!-->To do bug img slack, ggassistant, whatsapp, teams, twitter.....<!-->
- The [_connectors_](../channels#connectors-provided-with-tock) provided with the Tock distribution:
[<img alt="Logo Messenger" title="Facebook Messenger"
src="https://cdn.iconscout.com/icon/free/png-256/facebook-messenger-2-569346.png"
style="width: 50px;">](../channels#messenger)
![Logo slack](../../img/slack.png "List of applications"){style="width: 75px;"}

![Logo Google assistant](../../img/googelassist.png "List of applications"){style="width: 75px;"}
[<img alt="Google Home logo" title="Google Home"
 src="https://phoneky.co.uk/thumbs/android/thumbs/ico/3/chromecast-android.jpg"
 style="width: 50px;">](../channels#google-assistant-home)

![Logo Alexa](../../img/alexa2.png "List of applications"){style="width: 75px;"}

[<img alt="RocketChat Logo" title="Rocket.Chat"
 src="https://dl2.macupdate.com/images/icons256/58493.png?d=1565347347"
 style="width: 50px;">](../channels#rocketchat)
[<img alt="WhatsApp Logo" title="Facebook WhatsApp"
 src="https://appradarcentral.com/wp-content/uploads/2017/07/WhatsApp-Messenger.png"
 style="width: 50px;">](../channels#whatsapp)
[<img alt="Teams Logo" title="Microsoft Teams"
 src="https://cdn.worldvectorlogo.com/logos/microsoft-teams.svg"
 style="width: 50px;">](../channels#teams)
[<img alt="Logo Business Chat" title="Apple Business Chat"
 src="http://cdn.osxdaily.com/wp-content/uploads/2014/11/Messages-icon-300x300.png"
 style="width: 50px;">](../channels#business-chat)
[<img alt="Twitter Logo" title="Twitter"
 src="https://d2v9ipibika81v.cloudfront.net/uploads/sites/112/2016/06/twitter-logo.jpg"
 style="width: 50px;">](../channels#twitter)
[<img alt="Allo-Media Logo" title="Allo-Media"
 src="https://s3-eu-central-1.amazonaws.com/glassdollar/logos/GD_5bcf9307048f6.png"
 style="width: 50px;">](../channels#allo-media)
[<img alt="Google Chat logo" title="Google Chat"
 src="https://cdn.zapier.com/storage/photos/bfbce5bee25b1b50d8a910c30588c61e.png"
 style="width: 50px;">](../channels#google-chat)
[<img alt="Logo Web" title="Web (generic)"
 src="https://static.vecteezy.com/system/resources/previews/000/425/842/non_2x/vector-web-search-icon.jpg"
 style="width: 50px;">](../canaux#web-generic)
[<img alt="Logo Test" title="Test (generic)"
 src="https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcR_8-ubeyOzkkKclCUX3V-LSJVik_u8wtbJs6FBGWp9P19kzAQH&usqp=CAU"
 style="width: 50px;">](../channels#generic-test)

- The [kits using the _Web connector_](../channels#integrations-via-the-web-connector) to integrate other channels:
[<img alt="React Logo" title="React"
src="https://blog.octo.com/wp-content/uploads/2015/12/react-logo-1000-transparent.png"
style="width: 50px;">](../channels#react)
[<img alt="Flutter Logo" title="Google Flutter"
src="https://plugins.jetbrains.com/files/9212/97400/icon/pluginIcon.png"
style="width: 60px;">](../channels#flutter-beta)
[<img alt="Logo SharePoint" title="Microsoft SharePoint"
src="https://expertime.com/wp-content/uploads/2019/12/Logo_SharePoint-expertime.png"
style="width: 50px;">](../channels#sharepoint-beta)

- The [possible integrations for voice processing](../channels#voice-technologies):
[<img alt="Logo Android" title="Google Android"
src="https://www.wortis.fr/wp-content/uploads/2019/05/icon-wortis-android.png"
style="width: 50px;">](../channels#google-android)
[<img alt="Logo Google Assistant" title="Google Assistant" 
src="https://res-5.cloudinary.com/crunchbase-production/image/upload/c_lpad,h_120,w_120,f_auto,b_white,q_auto:eco/g0oshbe7blfnsrylchxd"
 style="width: 50px;">](../channels#google-android)
[<img alt="Google Home logo" title="Google Home"
 src="https://phoneky.co.uk/thumbs/android/thumbs/ico/3/chromecast-android.jpg"
 style="width: 50px;">](../channels#google-android)
[<img alt="Teams Logo" title="Microsoft Teams"
 src="https://cdn.worldvectorlogo.com/logos/microsoft-teams.svg"
 style="width: 50px;">](../channels#google-android)
[<img alt="iOS Logo" title="Apple iOS"
 src="https://www.freeiconspng.com/uploads/app-ios-png-4.png"
 style="width: 50px;">](../channels#apple-ios)
[<img alt="BusinessChat Logo" title="Apple Business Chat"
 src="http://cdn.osxdaily.com/wp-content/uploads/2014/11/Messages-icon-300x300.png"
 style="width: 50px;">](../channels#apple-ios)
[<img alt="Alexa Logo" title="Amazon Alexa"
 src="https://cognyapps.com/wp-content/uploads/2018/09/amazon-alexa-logo-e1538253665426.png"
 style="width: 50px;">](../channels#amazon-alexa)
[<img alt="Allo-Media Logo" title="Allo-Media"
 src="https://s3-eu-central-1.amazonaws.com/glassdollar/logos/GD_5bcf9307048f6.png"
 style="width: 50px;">](../channels#allo-media-voxygen)
[<img alt="Voxygen Logo" title="Voxygen"
 src="https://res-1.cloudinary.com/crunchbase-production/image/upload/c_lpad,h_256,w_256,f_auto,q_auto:eco/v1502521579/dyuj1cgjsnzhpo6ojwq0.png"
 style="width: 50px;">](../channels#allo-media-voxygen)
[<img alt="Nuance Logo" title="Nuance"
src="https://www.dicteedragon.fr/img/m/2.jpg"
style="width: 50px;">](../canaux#nuance)

## Connectors provided with Tock

Tock provides many connectors for different types of channels (see below). New connectors are
regularly added to the platform, depending on project needs but also on the schedule for opening
public channels to bots.

> Examples: arrival of Google Home in France in 2017, Alexa in 2018, opening of WhatsApp APIs then Business Chat in 2019, etc.

To learn more about the referenced bots using this or that connector in production,
do not hesitate to consult the page [Tock showcase](.. /../about/showcase).

### Messenger

<img alt="Logo Messenger" title="Facebook Messenger"
src="https://cdn.iconscout.com/icon/free/png-256/facebook-messenger-2-569346.png"
style ="width: 100px;">

* **Channel** : [Facebook Messenger](https://www.messenger.com/)
* **Type** : text _(+ voice via voice message upload )_
* **Status** : Tock connector used in production since 2016

The guide [Connect your bot to Messenger](../../guide/messenger) explains how to integrate a bot
Tock with a Facebook page / [Messenger] (https://www.messenger.com/).

To learn more about this connector, you can also go to the folder
[connector-messenger](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-messenger) on GitHub,
where you will find the sources and the _README_ of the connector.

### Slack
<!-->To do bug img<!-->

<img alt="Logo Slack" title="Slack"
src="https://www.macupdate.com/images/icons256/50617. png"
style="width: 100px;">

* **Channel** : [Slack](https://slack.com/)
* **Type** : text
* **Status** : Tock connector used outside production

The guide [Connect your bot to Slack](../../guide/slack) explains how to integrate a bot
Tock with a [Slack](https://slack.com/) _channel_.

To learn more about this connector, you can also go to the folder
[connector-slack](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-slack) on GitHub,
where you will find the sources and the _README_ of the connector.

### Google Assistant / Home

<img alt="Google Assistant logo" title="Google Assistant"
src="https://res-5.cloudinary.com/crunchbase-production/image/upload/c_lpad,h_120,w_120,f_auto,b_white,q_auto:eco/g0oshbe7blfnsrylchxd"
style="width: 100px;">
<img alt="Google Home logo" title="Google Home"
src="https://phoneky.co.uk/thumbs/android/thumbs/ico/3/chromecast-android.jpg"
style="width: 100px;">

* **Channel**: [Google Assistant](https://assistant.google.com/) / [Google Home](https://store.google.com/fr/product/google_home)
* **Type**: text + voice
* **Status** : Tock connector used in production since 2017

To learn more about this connector, see its sources and its _README_ in the folder
[connector-ga](https://github.com/theopenconversationkit/tock/tree/ master/bot/connector-ga) on GitHub.

### Alexa / Echo

<img alt="Alexa Logo" title="Amazon Alexa / Amazon Echo"
src="https://cognyapps.com/wp-content/uploads/2018/09/amazon-alexa-logo- e1538253665426.png"
style="width: 100px;">

* **Channel** : [Amazon Alexa](https://alexa.amazon.com/) / Amazon Echo
* **Type** : voice
* ** Status**: Tock connector used in production since 2018

Important note: in the case of Alexa, the NLP model is necessarily built and hosted at Amazon.

Only the conversational framework part of Tock can be used.

To learn more about this connector, see its sources and its _README_ in the
[connector-alexa](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-alexa) folder on GitHub.

## # Rocket.Chat

<img alt="RocketChat Logo" title="Rocket.Chat"
src="https://dl2.macupdate.com/images/icons256/58493.png?d=1565347347"
style="width: 100px;">

* **Channel** : [Rocket.Chat](https://rocket.chat/)
* **Type** : text
* **Status** : to be specified

To learn more about this connector, see its sources and its _README_ in the folder
[connector-rocketchat](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-rocketchat) on GitHub.

### WhatsApp

<img alt="Logo WhatsApp" title="Facebook WhatsApp"
src="https://appradarcentral.com/wp-content/uploads/2017/07/WhatsApp-Messenger.png"
style="width: 100px;">

* **Channel** : [WhatsApp from Facebook](https://www.whatsapp.com/)
* **Type** : text
* **Status** : Tock connector used in production since 2019

To learn more about this connector, see its sources and its _README_ in the folder
[connector-whatsapp](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-whatsapp) on GitHub.

### Teams

<img alt="Teams Logo" title="Microsoft Teams"
src="https://cdn.worldvectorlogo.com/logos/microsoft-teams.svg"
style="width: 100px;">

* **Channel** : [Microsoft Teams](https://products.office.com/fr-fr/microsoft-teams/)
* **Type** : text + voice
* **Status** : Tock connector used in production since 2019

To learn more about this connector, see its sources and its _README_ in the folder
[connector-teams](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-teams) on GitHub.

### Business Chat

<img alt="Logo BusinessChat" title="Apple Business Chat"
src="http://cdn.osxdaily.com/wp-content/uploads/2014/11/Messages-icon-300x300.png"
style="width: 100px;">

* **Channel** : [Apple Business Chat (Messages)](https://www.apple.com/fr/ios/business-chat/)
* **Type** : text
* **Status** : Tock connector used in production since 2019

To learn more about this connector, see its sources and its _README_ in the folder
[connector-businesschat](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-businesschat) on GitHub.

### Twitter

<img alt="Twitter Logo" title="Twitter"
src="https://d2v9ipibika81v.cloudfront.net/uploads/sites/112/2016/06/twitter-logo.jpg"
style="width: 100px;">

* **Channel** : [Twitter](https://twitter.com/) (private messages)
* **Type** : text
* **Status** : Tock connector used in production since 2019

To learn more about this connector, see its sources and its _README_ in the folder
[connector-twitter](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-twitter) on GitHub.

### Allo-Media

<img alt="Logo Allo-Media" title="Allo-Media"
src="https://s3-eu-central-1.amazonaws.com/glassdollar/logos/GD_5bcf9307048f6.png"
style="width: 100px;">

* **Channel** : [Allo-Media](https://www.allo-media.net/) (telephony)
* **Type** : voice
* **Status** : Tock connector used in production since 2020

This connector was developed for the [AlloCovid](https://www.allocovid.com/) bot.
For more information, see the [AlloMediaConnector](https://github.com/theopenconversationkit/allocovid/blob/master/src/main/kotlin/AlloMediaConnector.kt)
class with the [bot sources](https://github.com/theopenconversationkit/allocovid) on GitHub.

### Google Chat

<img alt="Google Chat Logo" title="Google Chat"
src="https://cdn.zapier.com/storage/photos/bfbce5bee25b1b50d8a910c30588c61e.png"
style="width: 100px;">

* **Channel** : [Google Chat](https://chat.google.com) (formerly Google Hangouts)
* **Type** : text
* **Status** : Tock connector used outside of production

To learn more about this connector, see its sources and _README_ in the
[connector-google-chat](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-google-chat) folder on GitHub.

### Web (generic)

<img alt="Web Logo" title="Web (generic)"
src="https://static.vecteezy.com/system/resources/previews/000/425/842/non_2x/vector-web-search-icon.jpg"
style="width: 100px;">

This generic connector allows you to integrate a Tock bot into any website or application:
portal, web or mobile application, REST client, etc.

The connector exposes a REST API to the bot, easily integrated from any web or mobile application, or programming language.

Several kits and components based on the Web connector are already available to integrate Tock bots into
different sites and applications, such as websites with [React](../channels#react),
native mobile applications with [Flutter](../channels#flutter-beta) or even
intranets [SharePoint](../channels#sharepoint-beta).

* **Channel** : Web (generic for all sites & web applications)
* **Type** : text
* **Status** : Tock connector used in production since 2020

To learn more about this connector, see its sources and its _README_ in the folder
[connector-web](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-web) on GitHub.
It contains examples and documentation in _Swagger_ format of the REST API.

### Test (generic)

<img alt="Web Logo" title="Test (generic)"
src="https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcR_8-ubeyOzkkKclCUX3V-LSJVik_u8wtbJs6FBGWp9P19kzAQH&usqp=CAU"
style="width: 100px;">

This connector is internal to Tock, it is used to communicate with a bot directly in the
_Tock Studio_ interface (_Test_ > _Test the bot_ view) by emulating other connectors.

## Integrations via the Web connector

The _Web connector_ exposes a generic API to interact with a Tock bot.
As a result, it allows even more integrations on the "frontend" side, using this API as a gateway.

### React
<!-->To do bug img blog.octo.com<!-->
<img alt="Logo React" title="React"
src="https://blog.octo.com/wp-content/uploads/2015/12/react-logo-1000-transparent.png"
style="width: 100px;">

This React component integrates a Tock bot and renders it graphically in a web application.
The web application communicates with the bot via a [Web connector](../channels#generic-web).

* **Integration** : [React](https://fr.reactjs.org/) (JavaScript / JSX)
* **Type** : Web applications
* **Status** : Used in production since 2020

For more information, see the sources and the _README_ in the repository
[`tock-react-kit`](https://github.com/theopenconversationkit/tock-react-kit) on GitHub.

### Flutter *(beta)*

<img alt="Logo Flutter" title="Google Flutter"
src="https://plugins.jetbrains.com/files/9212/97400/icon/pluginIcon.png"
style="width: 100px;">

This Flutter component integrates a Tock bot and provides its graphical rendering in a mobile or web application. The app communicates with the bot via a [web connector](../channels#generic-web).

* **Integration**: [Flutter](https://flutter.dev/) (Dart)
* **Type**: Native mobile and web apps
* **Status**: Beta, in development

For more information, see the sources and the _README_ in the
[`tock-flutter-kit`](https://github.com/theopenconversationkit/tock-flutter-kit) repository on GitHub.

### SharePoint *(beta)*

<img alt="SharePoint Logo" title="Microsoft SharePoint"
src="https://expertime.com/wp-content/uploads/2019/12/Logo_SharePoint-expertime.png"
style="width: 100px;">

This _WebPart_ component allows you to integrate a Tock bot into a SharePoint site.
It embeds the [tock-react-kit](../channels#react) to communicate with the bot
via a [Web connector](../channels#generic-web) and manage the graphic rendering of the bot in the SharePoint page.

* **Integration** : [Microsoft SharePoint](https://www.microsoft.com/en-us/microsoft-365/sharepoint/collaboration)
* **Type** : Websites & Intranets
* **Status** : Beta, in development

For more information, see the sources and the _README_ in the
[`tock-sharepoint`](https://github.com/theopenconversationkit/tock-sharepoint) repository on GitHub.

## Voice technologies

Tock bots process sentences in text format by default (_chatbots_). However, voice technologies can be
integrated into the bot's "terminals" in order to obtain voice conversations (_voicebots_ and _callbots_):

- Translation of voice into text (_Speech-To-Text_) upstream of the processing by the bot (ie. before the _NLU_ step)
- Translation of text into voice (_Text-To-Speech_) downstream of the processing by the bot (ie. voice synthesis of the bot's response)

Some _connectors_ provided with Tock allow a bot to be integrated into an external channel
managing the STT and TTS voice aspects.

In addition, other voice technologies have been integrated into Tock in recent years.
They are mentioned for information purposes, even when no ready-to-use _connector_ is provided.

### Google / Android

Google's _Speech-To-Text_ and _Text-To-Speech_ functions are used through the
[Google Assistant / Home connector](../channels#google-assistant-home), also by the voice
functions of the [Microsoft Teams app for Android](https://play.google.com/store/apps/details?id=com.microsoft.teams)
compatible with the [Teams connector](../channels#teams), as well as within the Android platform
in particular for native mobile developments. <!--><To do bug img <!-->
<img alt="Android Logo" title="Google Android"
src="https://www.wortis.fr/wp-content/uploads/2019/05/icon-wortis-android.png"
style="width: 100px;">
<img alt="Google Assistant logo" title="Google Assistant"
src="https://res-5.cloudinary.com/crunchbase-production/image/upload/c_lpad,h_120,w_120,f_auto,b_white,q_auto:eco/g0oshbe7blfnsrylchxd"
style="width: 50px;">
<img alt="Google Home logo" title="Google Home"
src="https://phoneky.co.uk/thumbs/android/thumbs/ico/3/chromecast-android.jpg"
style="width: 50px;">
<img alt="Teams Logo" title="Microsoft Teams"
src="https://cdn.worldvectorlogo.com/logos/microsoft-teams.svg"
style="width: 50px;">

* **Technology** : STT & TTS Google / Android
* **Status** : used with Tock in production
(via connectors [Google Assistant / Home](../channels#google-assistant-home),
[Microsoft Teams](../channels#teams)
and natively Android for botsintegrated _on-app_)

### Apple / iOS

Apple's _Speech-To-Text_ and _Text-To-Speech_ features are used through the
[Business Chat connector](../channels#business-chat), as well as within iOS
for native mobile developments.

<img alt="Logo iOS" title="Apple iOS"
src="https://www.freeiconspng.com/uploads/app-ios-png-4.png"
style="width: 100px;">
<img alt="Logo BusinessChat" title="Apple Business Chat"
src="http://cdn.osxdaily.com/wp-content/uploads/2014/11/Messages-icon-300x300.png"
style="width: 50px;">

* **Technology** : STT & TTS Apple / iOS
* **Status** : used with Tock in production (via Business Chat connector
and natively iOS for integrated _on-app_ bots)

### Amazon / Alexa

Alexa (Amazon) _Speech-To-Text_ and _Text-To-Speech_ functions are used through the
[Alexa connector / Echo](../channels#alexa-echo).

<img alt="Alexa Logo" title="Amazon Alexa"
src="https://cognyapps.com/wp-content/uploads/2018/09/amazon-alexa-logo-e1538253665426.png"
style="width: 100px;">

* **Technology**: STT & TTS Amazon / Alexa
* **Status**: used with Tock in production (via Alexa connector)

### Allo-Media & Voxygen

The company [Allo-Media](https://www.allo-media.net/) offers an AI platform based on phone calls.

[Voxygen](https://www.voxygen.fr/) offers speech synthesis services.

On the occasion of the development of the [AlloCovid](https://www.allocovid.com/) bot, an [Allo-Media connector](../channels#allo-media)
was developed to integrate the bot (Tock) with the Allo-Media services:
_Speech-To-Text_ and _Text-To-Speech_ with Voxygen.

<img alt="Allo-Media Logo" title="Allo-Media"
src="https://s3-eu-central-1.amazonaws.com/glassdollar/logos/GD_5bcf9307048f6.png"
style="width: 100px;"> <img alt="Voxygen Logo" title="Voxygen"
src="https://res-1.cloudinary.com/crunchbase-production/image/upload/c_lpad,h_256,w_256,f_auto,q_auto:eco/v1502521579/dyuj1cgjsnzhpo6ojwq0.png"
style="width: 100px;">

* **Technology**: Allo-Media & Voxygen
* **Status**: used with Tock in production (via Allo-Media connector)

### Nuance

[Nuance](https://www.nuance.com) offers speech recognition & AI solutions.

For voice command experiments in 2016, Nuance had been
integrated with Tock for its _Speech-To-Text_ functions.

Although this integration has not been maintained since, it worked
after a few days of implementation.

<img alt="Logo Nuance" title="Nuance"
src="https://www.dicteedragon.fr/img/m/2.jpg"
style="width: 100px;">

* **Technology**: Nuance
* **Status**: used with Tock in 2016

## Connector architecture & data governance

With a view to _governance_ of conversational models and data, the Tock connector architecture has several advantages:

* The model is built in Tock, it is not shared via connectors
* The choice of a bot's connectors allows you to control the propagation (or not) of conversations

> For example, for a bot internal to a company, you can choose to use only connectors

>to its own channels (website, etc.) or internal to the company (enterprise applications, professional space on

an Android phone, etc.).

* Even if a bot is connected to several external channels/partners, only the Tock platform has all the
conversations on all these channels.

## Developing your own connector

It is possible to create your own Tock connector, for example to interface a Tock bot with a channel specific to
the organization (often a specific website or mobile application), or when a general public channel
opens to conversational bots and the Tock connector does not yet exist.

The [_Bot Framework_](../../dev/bot-integre) section of the Tock developer manual gives instructions for
implementing your own connector.
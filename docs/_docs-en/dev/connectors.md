---
title: Connectors
---

# Tock Connectors

## Introduction

Tock _connectors_ integrate bots with various text/voice channels. 
Beside the _test connector_ used by _Tock Studio_ internally, connectors refer to channels, external to the Tock platform.

The Tock connector architecture makes it possible to build conversational assistants, loosely coupled to
the channels they are exposed to. One can first build a bot for a given channel, then make it a 
multichannel bot by adding connectors.

The _Web connector_ exposes a generic API to interact with a Tock bot.
As a consequence, more integrations are possible on the "frontend" by leveraging this API as a gateway.

This page actually lists:

- The [_connectors_](connectors.md#connectors-provided-with-tock) provided with Tock:  
[<img alt="Messenger Logo" title="Facebook Messenger"
      src="https://cdn.iconscout.com/icon/free/png-256/facebook-messenger-2-569346.png" 
      style="width: 50px;">](connectors.md#messenger)
[<img alt="Slack Logo" title="Slack"
 src="https://www.macupdate.com/images/icons256/50617.png" 
 style="width: 50px;">](connectors.md#slack)
[<img alt="Google Assistant Logo" title="Google Assistant"
 src="https://res-5.cloudinary.com/crunchbase-production/image/upload/c_lpad,h_120,w_120,f_auto,b_white,q_auto:eco/g0oshbe7blfnsrylchxd" 
 style="width: 50px;">](connectors.md#google-assistant-home)
[<img alt="Google Home Logo" title="Google Home"
 src="https://phoneky.co.uk/thumbs/android/thumbs/ico/3/chromecast-android.jpg" 
 style="width: 50px;">](connectors.md#google-assistant-home)
[<img alt="Alexa Logo" title="Amazon Alexa / Amazon Echo"
 src="https://cognyapps.com/wp-content/uploads/2018/09/amazon-alexa-logo-e1538253665426.png" 
 style="width: 50px;">](connectors.md#alexa-echo)
[<img alt="RocketChat Logo" title="Rocket.Chat"
 src="https://dl2.macupdate.com/images/icons256/58493.png?d=1565347347" 
 style="width: 50px;">](connectors.md#rocketchat)
[<img alt="WhatsApp Logo" title="Facebook WhatsApp"
 src="https://appradarcentral.com/wp-content/uploads/2017/07/WhatsApp-Messenger.png" 
 style="width: 50px;">](connectors.md#whatsapp)
[<img alt="Teams Logo" title="Microsoft Teams"
 src="https://cdn.worldvectorlogo.com/logos/microsoft-teams.svg" 
 style="width: 50px;">](connectors.md#teams)
[<img alt="Business Chat Logo" title="Apple Business Chat"
 src="http://cdn.osxdaily.com/wp-content/uploads/2014/11/Messages-icon-300x300.png" 
 style="width: 50px;">](connectors.md#business-chat)
[<img alt="Twitter Logo" title="Twitter"
 src="https://d2v9ipibika81v.cloudfront.net/uploads/sites/112/2016/06/twitter-logo.jpg" 
 style="width: 50px;">](connectors.md#twitter)
[<img alt="Allo-Media Logo" title="Allo-Media"
  src="https://s3-eu-central-1.amazonaws.com/glassdollar/logos/GD_5bcf9307048f6.png" 
  style="width: 50px;">](connectors.md#allo-media)
[<img alt="Google Chat Logo" title="Google Chat"
 src="https://cdn.zapier.com/storage/photos/bfbce5bee25b1b50d8a910c30588c61e.png" 
 style="width: 50px;">](connectors.md#google-chat)  
[<img alt="Web Logo" title="Web (generic)"
 src="https://static.vecteezy.com/system/resources/previews/000/425/842/non_2x/vector-web-search-icon.jpg" 
 style="width: 50px;">](connectors.md#web-generic)
[<img alt="Test Logo" title="Test (generic)"
 src="https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcR_8-ubeyOzkkKclCUX3V-LSJVik_u8wtbJs6FBGWp9P19kzAQH&usqp=CAU" 
 style="width: 50px;">](connectors.md#test-generic)

- Available [toolkits leveraging the _Web connector_](connectors.md#integrations-through-the-web-connector) to integrate 
with more channels:  
[<img alt="React logo" title="React"
      src="https://blog.octo.com/wp-content/uploads/2015/12/react-logo-1000-transparent.png" 
      style="width: 50px;">](connectors.md#react)
[<img alt="Flutter logo" title="Google Flutter"
 src="https://plugins.jetbrains.com/files/9212/97400/icon/pluginIcon.png" 
 style="width: 60px;">](connectors.md#flutter-beta)
[<img alt="SharePoint logo" title="Microsoft SharePoint"
 src="https://expertime.com/wp-content/uploads/2019/12/Logo_SharePoint-expertime.png" 
 style="width: 50px;">](connectors.md#sharepoint-beta)

- [Voice technologies](connectors.md#voice-technologies) possible to integrate with Tock:  
[<img alt="Android Logo" title="Google Android"
 src="https://www.wortis.fr/wp-content/uploads/2019/05/icon-wortis-android.png" 
 style="width: 50px;">](connectors.md#google-android)
[<img alt="Google Assistant Logo" title="Google Assistant"
 src="https://res-5.cloudinary.com/crunchbase-production/image/upload/c_lpad,h_120,w_120,f_auto,b_white,q_auto:eco/g0oshbe7blfnsrylchxd" 
 style="width: 50px;">](connectors.md#google-android)
[<img alt="Google Home Logo" title="Google Home"
 src="https://phoneky.co.uk/thumbs/android/thumbs/ico/3/chromecast-android.jpg" 
 style="width: 50px;">](connectors.md#google-android)
[<img alt="Teams Logo" title="Microsoft Teams"
 src="https://cdn.worldvectorlogo.com/logos/microsoft-teams.svg" 
 style="width: 50px;">](connectors.md#google-android)
[<img alt="iOS Logo" title="Apple iOS"
 src="https://www.freeiconspng.com/uploads/app-ios-png-4.png" 
 style="width: 50px;">](connectors.md#apple-ios)
[<img alt="BusinessChat Logo" title="Apple Business Chat"
 src="http://cdn.osxdaily.com/wp-content/uploads/2014/11/Messages-icon-300x300.png" 
 style="width: 50px;">](connectors.md#apple-ios)
[<img alt="Alexa Logo" title="Amazon Alexa"
 src="https://cognyapps.com/wp-content/uploads/2018/09/amazon-alexa-logo-e1538253665426.png" 
 style="width: 50px;">](connectors.md#amazon-alexa)
[<img alt="Allo-Media Logo" title="Allo-Media"
 src="https://s3-eu-central-1.amazonaws.com/glassdollar/logos/GD_5bcf9307048f6.png" 
 style="width: 50px;">](connectors.md#allo-media-voxygen)
[<img alt="Voxygen Logo" title="Voxygen" 
 src="https://res-1.cloudinary.com/crunchbase-production/image/upload/c_lpad,h_256,w_256,f_auto,q_auto:eco/v1502521579/dyuj1cgjsnzhpo6ojwq0.png" 
 style="width: 50px;">](connectors.md#allo-media-voxygen)
[<img alt="Nuance Logo" title="Nuance"
 src="https://www.dicteedragon.fr/img/m/2.jpg" 
 style="width: 50px;">](connectors.md#nuance)

## Connectors provided with Tock

Many _connectors_ are provided with Tock for various types of text/voice external channels. 
New connectors are regularly added to the platform, depending on user project needs and 
availability of new messaging channels.
 
 > Examples: Google Home arriving in France in 2017, Alexa in 2018, WhatsApp then 
>Business Chat APIs opened in 2019, etc. 

To find, which bot leverages which connector in production, please refer to the 
[Tock user showcase](../about/showcase.md) page.

### Messenger

<img alt="Messenger Logo" title="Facebook Messenger"
src="https://cdn.iconscout.com/icon/free/png-256/facebook-messenger-2-569346.png" 
style="width: 100px;">

* **Channel** : [Facebook Messenger](https://www.messenger.com/)
* **Type** : text _(+ voice through voice recording upload)_
* **Status** : Tock connector in production since 2016

Please refer to [connector-messenger](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-messenger) 
for sources and _README_ instructions.

### Slack

<img alt="Slack Logo" title="Slack"
src="https://www.macupdate.com/images/icons256/50617.png" 
style="width: 100px;">

* **Channel** : [Slack](https://slack.com/)
* **Type** : text
* **Status** : Tock connector not used for production (no use case yet)

Please refer to [connector-slack](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-slack) 
for sources and _README_ instructions.

### Google Assistant / Home

<img alt="Google Assistant Logo" title="Google Assistant"
src="https://res-5.cloudinary.com/crunchbase-production/image/upload/c_lpad,h_120,w_120,f_auto,b_white,q_auto:eco/g0oshbe7blfnsrylchxd" 
style="width: 100px;">
<img alt="Google Home Logo" title="Google Home"
src="https://phoneky.co.uk/thumbs/android/thumbs/ico/3/chromecast-android.jpg" 
style="width: 100px;">

* **Channel** : [Google Assistant](https://assistant.google.com/) / [Google Home](https://store.google.com/fr/product/google_home)
* **Type** : text + voice
* **Status** : Tock connector in production since 2017

Please refer to [connector-ga](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-ga)
for sources and _README_ instructions.

### Alexa / Echo

<img alt="Alexa Logo" title="Amazon Alexa / Amazon Echo"
src="https://cognyapps.com/wp-content/uploads/2018/09/amazon-alexa-logo-e1538253665426.png" 
style="width: 100px;">

* **Channel** : [Amazon Alexa](https://alexa.amazon.com/) / Amazon Echo
* **Type** : voice
* **Status** : Tock connector in production since 2018

Important : please note that the NLP model for Alexa is necessarily built and managed by Amazon online services.
Only the conversational framework can be used from Tock.

Please refer to [connector-alexa](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-alexa)
for sources and _README_ instructions.

### Rocket.Chat

<img alt="RocketChat Logo" title="Rocket.Chat"
src="https://dl2.macupdate.com/images/icons256/58493.png?d=1565347347" 
style="width: 100px;">

* **Channel** : [Rocket.Chat](https://rocket.chat/)
* **Type** : text
* **Status** : to be precised

Please refer to [connector-rocketchat](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-rocketchat)
for sources and _README_ instructions.

### WhatsApp

<img alt="WhatsApp Logo" title="Facebook WhatsApp"
src="https://appradarcentral.com/wp-content/uploads/2017/07/WhatsApp-Messenger.png" 
style="width: 100px;">

* **Channel** : [WhatsApp from Facebook](https://www.whatsapp.com/)
* **Type** : text
* **Status** : Tock connector in production since 2019

Please refer to [connector-whatsapp](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-whatsapp)
for sources and _README_ instructions.

### Teams

<img alt="Teams Logo" title="Microsoft Teams"
src="https://cdn.worldvectorlogo.com/logos/microsoft-teams.svg" 
style="width: 100px;">

* **Channel** : [Microsoft Teams](https://products.office.com/fr-fr/microsoft-teams/)
* **Type** : text + voice
* **Status** : Tock connector in production since 2019

Please refer to [connector-teams](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-teams)
for sources and _README_ instructions.

### Business Chat

<img alt="BusinessChat Logo" title="Apple Business Chat"
src="http://cdn.osxdaily.com/wp-content/uploads/2014/11/Messages-icon-300x300.png" 
style="width: 100px;">

* **Channel** : [Apple Business Chat (Messages)](https://www.apple.com/fr/ios/business-chat/)
* **Type** : text
* **Status** : Tock connector in production since 2019

Please refer to [connector-businesschat](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-businesschat)
for sources and _README_ instructions.

### Twitter

<img alt="Twitter Logo" title="Twitter"
src="https://d2v9ipibika81v.cloudfront.net/uploads/sites/112/2016/06/twitter-logo.jpg" 
style="width: 100px;">

* **Channel** : [Twitter](https://twitter.com/) (messages privés)
* **Type** : text
* **Status** : Tock connector in production since 2019

Please refer to [connector-twitter](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-twitter)
for sources and _README_ instructions.

### Allo-Media

<img alt="Allo-Media Logo" title="Allo-Media"
src="https://s3-eu-central-1.amazonaws.com/glassdollar/logos/GD_5bcf9307048f6.png" 
style="width: 100px;">

* **Channel** : [Allo-Media](https://www.allo-media.net/) (téléphonie)
* **Type** : voice
* **Status** : Tock connector in production since 2020

This connector has been developped for the French [AlloCovid](https://www.allocovid.com/) bot.
To know more, please check the [AlloMediaConnector](https://github.com/theopenconversationkit/allocovid/blob/master/src/main/kotlin/AlloMediaConnector.kt)
class and the [bot sources](https://github.com/theopenconversationkit/allocovid) also on GitHub.

### Google Chat

<img alt="Google Chat Logo" title="Google Chat"
src="https://cdn.zapier.com/storage/photos/bfbce5bee25b1b50d8a910c30588c61e.png" 
style="width: 100px;">

* **Channel** : [Google Chat](https://www.allo-media.net/) (aka Google Hangouts)
* **Type** : text
* **Status** : Tock connector not used for production (no use case yet)

Please refer to [connector-google-chat](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-google-chat)
for sources and _README_ instructions.

### Web (generic)

<img alt="Web Logo" title="Web (generic)"
 src="https://static.vecteezy.com/system/resources/previews/000/425/842/non_2x/vector-web-search-icon.jpg" 
 style="width: 100px;">

This generic connector integrates Tock bots with Web sites or applications: 
portals, dedicated sites, apps, REST clients, etc.

The connector exposes a REST API to the bot, making it easy to integrate with any Website, mobile application or programming language.

Several toolkits and components consuming the Web connector API are already available to integrate Tock bots with 
 more types of sites and applications, such as Websites with [React](connectors.md#react), 
mobile-native applications with [Flutter](connectors.md#flutter-beta) and 
intranet sites on [SharePoint](connectors.md#sharepoint-beta).

* **Channel** : Web (generic for any Web site or application)
* **Type** : text
* **Status** : Tock connector in production since 2020

Please refer to [connector-web](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-web)
for sources and _README_ instructions, including samples and the _Swagger_ documentation for the REST API.

### Test (generic)

<img alt="Web Logo" title="Test (generic)"
 src="https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcR_8-ubeyOzkkKclCUX3V-LSJVik_u8wtbJs6FBGWp9P19kzAQH&usqp=CAU" 
 style="width: 100px;">
 
This Tock-internal connector allows to talk to a bot directly from the  
_Tock Studio_ interface (_Test_ > _Test the bot_) and emulates other connectors.


## Integrations through the Web connector

The _Web connector_ exposes a generic API to interact with a bot. As a consequence, more integrations are possible 
 on the "frontend", by consuming the API as a gateway to the bot.

### React

<img alt="React logo" title="React"
src="https://blog.octo.com/wp-content/uploads/2015/12/react-logo-1000-transparent.png" 
style="width: 100px;">

This React component integrates and renders a Tock bot inside a Web application or site.  
The Webapp communicates with the bot through a [Web connector](connectors.md#web-generic).

* **Integration**: [React](https://fr.reactjs.org/) (JavaScript / JSX)
* **Type**: Web applications
* **Status**: in production since 2020

Please refer to [`tock-react-kit`](https://github.com/theopenconversationkit/tock-react-kit)
for sources and _README_ instructions.

### Flutter _(beta)_

<img alt="Flutter logo" title="Google Flutter"
src="https://plugins.jetbrains.com/files/9212/97400/icon/pluginIcon.png" 
style="width: 100px;">

This Flutter component integrates and renders a Tock bot inside a mobile or Web application.  
The application communicates with the bot through a [Web connector](connectors.md#web-generic).

* **Integration**: [Flutter](https://flutter.dev/) (Dart)
* **Type**: mobile-native or Web applications
* **Status**: beta, in development

Please refer to [`tock-flutter-kit`](https://github.com/theopenconversationkit/tock-flutter-kit)
for sources and _README_ instructions.

### SharePoint _(beta)_

<img alt="SharePoint logo" title="Microsoft SharePoint"
src="https://expertime.com/wp-content/uploads/2019/12/Logo_SharePoint-expertime.png" 
style="width: 100px;">

This _WebPart_ component integrates and renders a Tock bot inside a SharePoint page/site.  
The component embeds the [tock-react-kit](connectors.md#react) to communicate with the bot through a 
[Web connector](connectors.md#web-generic) and render the bot inside the SharePoint page.

* **Integration**: [Microsoft SharePoint](https://www.microsoft.com/en-us/microsoft-365/sharepoint/collaboration)
* **Type**: Web & intranet sites
* **Status**: beta, in development

Please refer to [`tock-sharepoint`](https://github.com/theopenconversationkit/tock-sharepoint)
for sources and _README_ instructions.


## Voice Technologies

Tock bots merely process text sentences by default. Nevertheless, voice and speech 
technologies can be leveraged around the bot to achieve voice dialogs (namely voicebots and callbots):

- Translating _Speech-To-Text_ before bot processing (ie. before _NLU_)
- Translating _Text-To-Speech_ after bot processing (ie. synthesis speech from bot answer)

Some of the provided _connectors_ integrate with external channels, capable of STT and TTS.

More voice technologies have been integrated with Tock over time, even when no ready-to-use connector is provided.

### Google / Android

Google _Speech-To-Text_ and _Text-To-Speech_ features are used by the 
[Google Assistant / Home connector](connectors.md#google-assistant-home), the microphone feature from the 
[Microsoft Teams app for Android](https://play.google.com/store/apps/details?id=com.microsoft.teams)
compatible with the [Teams connector](connectors.md#teams), as well as the Android platform 
for mobile-native development.

<img alt="Android Logo" title="Google Android"
src="https://www.wortis.fr/wp-content/uploads/2019/05/icon-wortis-android.png" 
style="width: 100px;">
<img alt="Google Assistant Logo" title="Google Assistant"
src="https://res-5.cloudinary.com/crunchbase-production/image/upload/c_lpad,h_120,w_120,f_auto,b_white,q_auto:eco/g0oshbe7blfnsrylchxd" 
style="width: 50px;">
<img alt="Google Home Logo" title="Google Home"
src="https://phoneky.co.uk/thumbs/android/thumbs/ico/3/chromecast-android.jpg" 
style="width: 50px;">
<img alt="Teams Logo" title="Microsoft Teams"
src="https://cdn.worldvectorlogo.com/logos/microsoft-teams.svg" 
style="width: 50px;">

* **Technologie** : Google / Android STT & TTS
* **Status** : used with Tock in production 
(through [Google Assistant / Home](connectors.md#google-assistant-home) and 
[Microsoft Teams](connectors.md#teams) connectors, as well as native Android for _on-app_ mobile bots)

### Apple / iOS

Apple _Speech-To-Text_ and _Text-To-Speech_ features are used by the 
[Business Chat connector](connectors.md#business-chat), as well as the iOS platform 
for mobile-native development.

<img alt="iOS Logo" title="Apple iOS"
src="https://www.freeiconspng.com/uploads/app-ios-png-4.png" 
style="width: 100px;">
<img alt="BusinessChat Logo" title="Apple Business Chat"
src="http://cdn.osxdaily.com/wp-content/uploads/2014/11/Messages-icon-300x300.png" 
style="width: 50px;">

* **Technologie** : Apple / iOS STT & TTS
* **Status** : used with Tock in production (though [Business Chat connector](connectors.md#business-chat) 
and native iOS for _on-app_ mobile bots)
 
### Amazon / Alexa

<img alt="Alexa Logo" title="Amazon Alexa"
src="https://cognyapps.com/wp-content/uploads/2018/09/amazon-alexa-logo-e1538253665426.png" 
style="width: 100px;">

* **Technologie** : Amazon / Alexa STT & TTS
* **Status** : used with Tock in production (through Alexa connector)
 
### Allo-Media & Voxygen

To build the French [AlloCovid](https://www.allocovid.com/) bot, an [Allo-Media connector](connectors.md#allo-media)
has been developped, to integrate the Tock bot with [Allo-Media](https://www.allo-media.net/) services: 
_Speech-To-Text_ (from phone speech) and _Text-To-Speech_ (leveraging [Voxygen](https://www.voxygen.fr/) synthesis voices).

<img alt="Allo-Media Logo" title="Allo-Media"
src="https://s3-eu-central-1.amazonaws.com/glassdollar/logos/GD_5bcf9307048f6.png" 
style="width: 100px;"> <img alt="Voxygen Logo" title="Voxygen" 
src="https://res-1.cloudinary.com/crunchbase-production/image/upload/c_lpad,h_256,w_256,f_auto,q_auto:eco/v1502521579/dyuj1cgjsnzhpo6ojwq0.png" 
style="width: 100px;">

* **Technologie** : Allo-Media & Voxygen
* **Status** : used with Tock in production (though [Allo-Media connector](connectors.md#allo-media))
 
### Nuance

[Nuance](https://www.nuance.com) propose des solutions de reconnaissance vocale & IA.

Back in 2016 for voice command usages, [Nuance](https://www.nuance.com) was successfully 
integrated with Tock for its _Speech-To-Text_ features.

<img alt="Nuance Logo" title="Nuance"
src="https://www.dicteedragon.fr/img/m/2.jpg" 
style="width: 100px;">

* **Technologie** : Nuance
* **Status** : used with Tock in 2016


## Define your own connector

It is possible to develop its own connector.

An example of custom connector can be seen in the [Bot Open Data sample project](https://github.com/theopenconversationkit/tock-bot-open-data/tree/master/src/main/kotlin/connector). 

To develop your own, follow these steps:

1) Implement the interface [Connector](https://doc.tock.ai/tock/dokka/tock/ai.tock.bot.connector/-connector/index.html) 

Here is an example of implementation:

```kotlin

val testConnectorType = ConnectorType("test")

class TestConnector(val applicationId: String, val path: String) : Connector {

    override val connectorType: ConnectorType = testConnectorType

    override fun register(controller: ConnectorController) {
        controller.registerServices(path) { router ->
            //main API
            router.post("$path/message").blockingHandler { context ->
                //ConnectorRequest is my business object passed by the front app
                val message: ConnectorRequest = mapper.readValue(context.bodyAsString)
                
                //business object mapped to Tock event
                val event = readUserMessage(message)
                //we pass the Tock event to the framework
                val callback = TestConnectorCallback(applicationId, message.userId, context, controller)
                controller.handle(event, ConnectorData(callback))
            }
            
        }
            
    }
    
    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        callback as TestConnectorCallback
        if (event is Action) {
            //we record the action
            callback.actions.add(event)
            //if it's the last action to send, send the answer
            if (event.metadata.lastAnswer) {
                callback.sendAnswer()
            }
        } else {
            logger.trace { "unsupported event: $event" }
        }
    }    
}

// to retrieve all actions before sending
class TestConnectorCallback(
        override val applicationId: String,
        val userId: String,
        val context: RoutingContext,
        val controller: ConnectorController,
        val actions: MutableList<Action> = CopyOnWriteArrayList()): ConnectorCallbackBase(applicationId, testConnectorType) {
    
    internal fun sendAnswer() {
            //we transform the list of Tock responses into a business response
            val response = mapper.writeValueAsString(actions.map{...})
            //then we send the answer
            context.response().end(response)
    }
    
}         

```

2) Implement the interface [ConnectorProvider](https://doc.tock.ai/tock/dokka/tock/ai.tock.bot.connector/-connector-provider/index.html)

Here is an example of implementation:

```kotlin
object TestConnectorProvider : ConnectorProvider {

    override val connectorType: ConnectorType = testConnectorType

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        return TestConnector(
                connectorConfiguration.connectorId,
                connectorConfiguration.path
        )
    }
}

class TestConnectorProviderService: ConnectorProvider by TestConnectorProvider

```

3) Make this connector available via a Service Loader

By placing a file META-INF/services/ai.tock.bot.connector.ConnectorProvider
in the classpath, containing the class name :

```kotlin
mypackage.TestConnectorProviderService
```

4) Add all classes and files created in the admin classpath and bot classpath

The new connector must then be available in the "Bot Configurations" administration interface.
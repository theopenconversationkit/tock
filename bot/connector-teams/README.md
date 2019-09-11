# Tock Teams Connector

This connector allows you to talk with a bot in Teams, the Microsoft solution for chat, using the [Microsoft REST API](https://docs.microsoft.com/en-us/azure/bot-service/rest-api/bot-framework-rest-connector-api-reference?view=azure-bot-service-4.0)


## Create your bot
See [https://voyages-sncf-technologies.github.io/tock/en/](https://voyages-sncf-technologies.github.io/tock/en/)

The Microsoft library used is [Microsoft Bot Framework Connector for Java](https://github.com/microsoft/botbuilder-java/tree/master/libraries/bot-connector)

### Available Cards

When using the connector teams, you can create [cards](https://docs.microsoft.com/en-us/microsoftteams/platform/concepts/cards/cards-reference). 

Currently supported cards :

* Action Card, a card to usually display buttons
```kotlin
class TeamsCardAction(
    val actionTitle: String,
    val buttons: List<CardAction>)

//use extension bus.teamsMessageWithButtonCard(...)

```
* Hero Card, a card that typically contains a single large image, one or more buttons and text. 
```kotlin
class TeamsHeroCard(
    val title: String,
    val subtitle: String?,
    val attachmentContent: String,
    val images: List<CardImage>?,
    val buttons: List<CardAction>?,
    val tap: CardAction?
)
//use extension bus.teamsHeroCard(...)
```
* Carousel, to display a carousel of cards
```kotlin
class TeamsCarousel(val listMessage: List<TeamsBotMessage>)
//use extension bus.teamsCarousel(...)
```

Look at [Dokka documentation](https://voyages-sncf-technologies.github.io/tock/dokka/tock/fr.vsct.tock.bot.connector.teams.messages/index.html)
for more information.

## Deploy your bot on localhost (Tock Integrated Bot version)

[Ngrok](https://ngrok.com/) is your friend. Prefer the Basic offer, to have a custom subdomain in order to avoid to have to change the endpoint every time you reboot ngrok.

## Create an ApplicationId for your bot
[https://dev.botframework.com/bots/new](https://dev.botframework.com/bots/new)

## Configure the connector Teams in Tock Studio
You'll need your local endpoint, your appId and your password.

![](statics/connector_teams.png)

## Talk to your bot on teams
[https://docs.microsoft.com/fr-fr/microsoftteams/add-bots#upload-your-bot-for-personal-chat](https://docs.microsoft.com/fr-fr/microsoftteams/add-bots#upload-your-bot-for-personal-chat)
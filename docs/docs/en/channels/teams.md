---
title: Microsoft Teams
---

# Microsoft Teams connector

The Teams connector connects a bot to [Microsoft Teams](https://www.microsoft.com/microsoft-teams/),
with the [Bot Framework REST API](https://learn.microsoft.com/azure/bot-service/rest-api/bot-framework-rest-connector-api-reference).

* **Connector type**: `teams`
* **Sources and README**: [connector-teams](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-teams)

## Prerequisites

* A Microsoft 365 tenant where custom apps can be uploaded in Teams
* An Azure subscription, to register the bot in Azure Bot Service
* A public HTTPS URL for the bot

## Configuration

1. Register a bot in the Microsoft Bot Framework / Azure Bot Service, to get an **App ID** and a **password**.
2. In _Tock Studio_, create a _Teams_ connector in _Settings > Configurations_:

    | Field | Description |
    |-------|-------------|
    | `appId` | App ID of the bot in the Bot Framework / Azure Bot Service |
    | `password` | Password (client secret) of the bot |

3. Set the messaging endpoint of the bot to the URL of the connector,
   for instance `https://<bot-host>/io/<namespace>/<bot>/teams`.
4. Add the bot to Teams (see [upload your app in Teams](https://learn.microsoft.com/microsoftteams/platform/concepts/deploy-and-publish/apps-upload)).

To test a bot running locally, expose it with a secure tunnel (for instance [ngrok](https://ngrok.com/)).

The timeouts of the calls to Microsoft are set by `tock_microsoft_request_timeout` and
`tock_teams_request_timeout_ms` (see [Configuration](../operate/configuration.md#other-connectors)).

## Cards

In addition to texts, the connector supports the following [cards](https://learn.microsoft.com/microsoftteams/platform/task-modules-and-cards/cards/cards-reference):

* action card (buttons): `teamsMessageWithButtonCard(...)`,
* hero card (image, text and buttons): `teamsHeroCard(...)`,
* carousel of cards: `teamsCarousel(...)`.

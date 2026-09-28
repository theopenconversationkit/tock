---
title: Microsoft Teams
---

# Connecteur Microsoft Teams

Le connecteur Teams relie un bot à [Microsoft Teams](https://www.microsoft.com/microsoft-teams/),
via l'[API REST du Bot Framework](https://learn.microsoft.com/azure/bot-service/rest-api/bot-framework-rest-connector-api-reference).

* **Type de connecteur** : `teams`
* **Sources et README** : [connector-teams](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-teams)

## Configuration

1. Enregistrez un bot dans le Microsoft Bot Framework / Azure Bot Service, pour obtenir un **App ID** et un **mot de passe**.
2. Dans _Tock Studio_, créez un connecteur _Teams_ dans _Settings > Configurations_ :

    | Champ | Description |
    |-------|-------------|
    | `appId` | App ID du bot dans le Bot Framework / Azure Bot Service |
    | `password` | Mot de passe (secret client) du bot |

3. Définissez l'URL du connecteur comme _messaging endpoint_ du bot,
   par exemple `https://<hôte-du-bot>/io/<namespace>/<bot>/teams`.
4. Ajoutez le bot à Teams (voir [charger une application dans Teams](https://learn.microsoft.com/microsoftteams/platform/concepts/deploy-and-publish/apps-upload)).

Pour tester un bot qui tourne en local, exposez-le avec un tunnel sécurisé (par exemple [ngrok](https://ngrok.com/)).

Les délais maximaux des appels à Microsoft sont définis par `tock_microsoft_request_timeout` et
`tock_whatsapp_request_timeout_ms` (voir [Configuration](../operate/configuration.md#autres-connecteurs)).

## Cartes

En plus des textes, le connecteur prend en charge les [cartes](https://learn.microsoft.com/microsoftteams/platform/task-modules-and-cards/cards/cards-reference) suivantes :

* carte d'action (boutons) : `teamsMessageWithButtonCard(...)`,
* carte « hero » (image, texte et boutons) : `teamsHeroCard(...)`,
* carrousel de cartes : `teamsCarousel(...)`.

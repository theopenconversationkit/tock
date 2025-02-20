---
title: Slack
---

# Configurer son bot pour Slack

Si vous avez suivi le guide [Créer son premier bot avec _Tock Studio_](../guides/studio.md), vous avez déclaré un connecteur
 de type Slack mais celui-ci n'est pas encore configuré pour que le bot parle réellement sur [Slack](https://slack.com/).

Avec un peu de configuration côté Slack et côté Tock, un bot peut recevoir des messages et répondre sur ce canal.

Si vous le souhaitez, vous pouvez aussi sauter cette étape et [configurer un canal Messenger](messenger.md) 
ou passer directement à [la suite](api.md).
 
## Ce que vous allez créer

* Une configuration (dans Slack et dans Tock) pour recevoir et envoyer des messages Slack

* Un bot qui parle sur une _chaîne_ Slack

## Pré-requis

* Environ 15 minutes

* Un bot Tock fonctionnel (par exemple suite au guide [premier bot Tock](../guides/studio.md))

* Un compte Slack et un _espace de travail_ / une _chaîne_ où intégrer le bot

> Si vous n'avez jamais utilisé Slack, rendez-vous sur sur [https://slack.com/](https://slack.com/)

## Créer une application dans Slack

* Allez sur la page [Create a Slack app](https://api.slack.com/apps/new)

* Entrez un nom pour l'_application_

* Sélectionnez un _espace de travail_

* Terminez avec _Create App_

## Activer l'envoi de messages à Slack

* Ouvrez _Incoming Webhooks_ et cochez _Activate Incoming Webhooks_

* Cliquez sur _Add New Webhook to Workspace_ 

* Sélectionnez une _chaîne_ ou une personne pour la conversation avec le bot

* Terminez par _Installer_

* Copiez la _Webhook URL_ qui vient d'être créée

> La _Webhook URL_ ressemble dans son format à quelque chose comme : 
> https://hooks.slack.com/services/{workspaceToken}/{webhookToken}/{authToken}

* Dans _Tock Studio_ allez dans _Configuration_ > _Bot Configurations_

* Trouvez votre connecteur de type _Slack_ (ou créez-en un nouveau si besoin) et ouvrez la section _Connector Custom Configuration_

* Saisissez dans les trois champs _tokens_ les jetons issus de l'adresse précédemment copiée :

    * _Token 1_ : le premier token de la _WebhookURL_, ou _workspaceToken_

    * _Token 2_ : le deuxième token de la _WebhookURL_, ou _webhookToken_

    * _Token 3_ : le dernier token de la _WebhookURL_, ou _authToken_

* Terminez avec _Update_

> Attention : en cas de réinstallation de l'application Slack dans le _workspace_, URL et jetons sont changés
> et doivent être reportés dans la configuration côté Tock.

## Activer la reception de messages depuis Slack

* Dans la page de votre application Slack, allez dans _Event Subscriptions_ et activez _Enable Events_

* Entrez dans le champ _Request URL_ l'adresse complète de votre connecteur Slack dans Tock.

> Sur la plateforme de démonstration Tock, cette adresse sera du type 
>https://demo.tock.ai/{chemin_relatif_du_connecteur_slack}

> Le chemin relatif du connecteur est indiqué dans la page _Bot Configurations_. Sur la ligne correspondant à votre
>connecteur Slack, il s'agit du champ _Relative REST path_

* Ouvrez _Add Workspace Event_ et sélectionnez l'évenement _message.channels_ pour 
utiliser le bot sur une _chaîne_ Slack.
  
> D'autres événements "message" sont également disponibles : _message.im_ pour les messages privés,
  _message.groups_, etc. Cf la [documentation Slack](https://api.slack.com/events).

* Validez avec _Save Changes_

* Allez dans _Interactive Components_ et activez _Interactivity_

* Entrez la même _Request URL_ que précédemment

* Validez avec _Save Changes_

## Créer un bot Slack (et lui parler)

* Dans la page de votre application Slack, allez dans _Bot Users_ et faites _Add a Bot User_

* Choisissez un nom / identifiant pour le bot dans Slack

* Validez avec _Add Bot User_

* Allez dans _Install App_ et _Reinstall App_

* Sélectionnez la _chaîne_ Slack puis _Installer_

* Dans Slack, allez sur la _chaîne_ et ajoutez le bot à la _chaîne_
  
* Parlez au bot (par exemple "bonjour"). Il vous répond maintenant dans Slack !

## Regarder la conversation dans Tock Studio (optionnel)

Quelque soient les canaux utilisés pour converser avec le bot, vous pouvez suivre les conversations directement dans 
tous les écrans _Tock Studio_, par exemple : _Language Understanding_ > _Inbox_ et _Logs_, 
ou encore toutes vues du menu _Analytics_ :

* Dans Tock, ouvrez _Analytics_ > _Users_ et cliquez sur l'icône _Display dialog_ pour voir toute la 
conversation provenant de Slack


## Félicitations!

Vous venez de configurer votre bot pour qu'il parle également sur Slack.

Comme vous le constatez, connecter un bot Tock à un (ou plusieurs) canaux externes n'est qu'une affaire de configuration.
 Vous pouvez construire le modèle conversationnel, les fonctionnalités et la personnalité de votre assistant 
 indépendamment des canaux sur lesquels vous souhaitez lui parler, aujourd'hui ou à l'avenir.


## Continuer...

Dans les sections suivantes vous apprendrez à :

* [Configurer le bot pour le canal Messenger](../guides/messenger.md) (requiert un compte Facebook)

* [Créer des parcours programmés en Kotlin](../guides/api.md), ouvrant la voie à des comportements complexes et 
l'intégration d'API tierces si besoin

* [Déployer une plateforme Tock](platform.md) en quelques minutes avec Docker

Pour en savoir plus sur le connecteur Slack fourni avec Tock, rendez-vous dans le dossier 
[connector-slack](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-slack) sur GitHub, 
où vous retrouverez les sources et le _README_ du connecteur.


---
title: Canaux
---

# Construire un bot multicanal avec Tock

## Notion de *connecteur*

Un _connecteur_ Tock permet d'intégrer un bot à un canal de communication (textuel ou vocal) externe.
Mis à part le type _connecteur de test_ (utilisé en interne par l'interface _Tock Studio_), les connecteurs 
sont associés à des canaux externes à la plateforme Tock.

Tout l'intérêt des connecteurs Tock réside dans la possibilité de développer des assistants conversationnels 
indépendamment du ou des canaux utilisés pour lui parler. Il est ainsi possible de créer un bot pour un canal,
puis le rendre multicanal par la suite en ajoutant des connecteurs.

Le _connecteur Web_ a la particularité d'exposer une API générique pour interagir avec un bot Tock.
En conséquence, il permet encore davantage d'intégrations côté "frontend", utilisant cette API comme passerelle.

Cette page liste en fait :

- Les [_connecteurs_](#integrations-via-le-connecteur-web) fournis avec la distribution Tock :

![logo messenger](../../img/messenger.png "whatsapp"){style="width:50px;"}
![Logo slack](../../img/slack.png "Slack"){style="width: 75px;"}
![Logo Google assistant](../../img/googelassist.png "google assisstant"){style="width: 70px;"}
![logo google home](../../img/googlehome.png "google home "){style="width:50px;"}
![Logo Alexa](../../img/alexa2.png "alexa"){style="width: 75px;"}
![logo Rocket rachat](../../img/rocketrachat.png "rocket rachat"){style="width:50px;"}
![logo whatsapp](../../img/whatsapp.png "whatsapp"){style="width:50px;"}
![logo teams](../../img/teams.png "teams"){style="width:50px;"}
![logo BusinessChat Logo](../../img/message.png "BusinessChat Logo"){style="width:50px;"}
![logo twitter](../../img/twitter.png "twitter"){style="width:50px;"}
![logo google chat ](../../img/ggchat.png "google chat"){style="width:50px;"}
![logo mattermost](../../img/mattermost.svg "Mattermost"){style="width:50px;"}
![logo web](../../img/web.png "web"){style="width:50px;"}
![logo test](../../img/test.jpeg "test"){style="width:50px;"}

- Les [kits utilisant le _connecteur Web_](#integrations-via-le-connecteur-web) pour intégrer d'autres canaux :  

![logo React](../../img/React.png "React"){style="width:50px;"}
![logo flutter](../../img/flutter.png "allo media"){style="width:50px;"}
![logo Sharepoint](../../img/sharepoint.png "Sharepopint"){style="width:50px;"}

- Les [intégrations possibles pour le traitement de la voix](canaux.md#technologies-vocales) :  
![logo android](../../img/android.png "allo media"){style="width:50px;"}
![Logo Google assistant](../../img/googelassist.png "google assistant"){style="width: 70px;"}
![logo google home](../../img/googlehome.png "google home "){style="width:50px;"}
![logo teams](../../img/teams.png "teams"){style="width:50px;"}
![logo ios](../../img/ios.png "ios"){style="width:50px;"}
![logo BusinessChat Logo](../../img/message.png "BusinessChat Logo"){style="width:50px;"}
![Logo Alexa](../../img/alexa2.png "Alexa"){style="width: 75px;"}
![Logo voxygen](../../img/voxygen.png "Voxygen"){style="width: 100px;"}
![Logo nuance](../../img/nuance.png "Nuance"){style="width: 75px;"}

## Connecteurs fournis avec Tock

Tock fournit de nombreux connecteurs pour différents types de canaux (voir ci-dessous). De nouveaux connecteurs sont 
régulièrement ajoutés à la plateforme, en fonction des besoins projets mais aussi du calendrier d'ouverture aux bots 
des canaux grand public.
 
 > Exemples : arrivée de Google Home en France en 2017, Alexa en 2018, ouverture des API WhatsApp puis Business Chat en 2019, etc. 

Pour en savoir plus sur les bots référencés utilisant tel ou tel connecteur en production, 
n'hésitez pas à consulter la page [vitrine Tock](../../about/showcase.md).

### Messenger

![logo messenger](../../img/messenger.png "whatsapp"){style="width:50px;"}


* **Canal** : [Facebook Messenger](https://www.messenger.com/)
* **Type** : texte _(+ voix via l'upload de messages vocaux)_
* **Status** : connecteur Tock utilisé en production depuis 2016

Le guide [Connecter son bot à Messenger](../../guides/messenger.md) explique comment intégrer un bot 
Tock avec une page Facebook / [Messenger](https://www.messenger.com/).

Pour en savoir plus sur ce connecteur, vous pouvez aussi vous rendre dans le dossier 
[connector-messenger](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-messenger) sur GitHub, 
où vous retrouverez les sources et le _README_ du connecteur.

### Slack
![logo slack](../../img/slack.png "slack"){style="width:100px;"}

* **Canal** : [Slack](https://slack.com/)
* **Type** : texte
* **Status** : connecteur Tock utilisé hors production

Le guide [Connecter son bot à Slack](../../guides/slack.md)explique comment intégrer un bot 
Tock avec une _chaîne_ [Slack](https://slack.com/).

Pour en savoir plus sur ce connecteur, vous pouvez aussi vous rendre dans le dossier 
[connector-slack](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-slack) sur GitHub, 
où vous retrouverez les sources et le _README_ du connecteur.

### Google Assistant - Home

![Logo Google assistant](../../img/googelassist.png "google assistant"){style="width: 100px;"}

![logo google home](../../img/googlehome.png "google home "){style="width:100px;"}

* **Canal** : [Google Assistant](https://assistant.google.com/) / [Google Home](https://store.google.com/fr/product/google_home)
* **Type** : texte + voix
* **Status** : connecteur Tock utilisé en production depuis 2017

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-ga](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-ga) sur GitHub.

### Alexa / Echo

![Logo Alexa](../../img/alexa2.png "alexa"){style="width: 75px;"}


* **Canal** : [Amazon Alexa](https://alexa.amazon.com/) / Amazon Echo
* **Type** : voix
* **Status** : connecteur Tock utilisé en production depuis 2018

Remarque importante : dans le cas d'Alexa, le modèle NLP est forcément construit et hébergé chez Amazon. 
Seul la partie framework conversationel de Tock peut être utilisée.

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-alexa](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-alexa) sur GitHub.

### Rocket.Chat

![logo Rocket rachat](../../img/rocketrachat.png "rocket rachat"){style="width:50px;"}

* **Canal** : [Rocket.Chat](https://rocket.chat/)
* **Type** : texte
* **Status** : à préciser

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-rocketchat](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-rocketchat) sur GitHub.

### WhatsApp

![logo whatsapp](../../img/whatsapp.png "whatsapp"){style="width:100px;"}

* **Canal** : [WhatsApp from Meta](https://www.whatsapp.com/)
* **Type** : texte
* **Status** :
  * [*On-Premise API*](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-whatsapp) : connecteur Tock
    utilisé en production depuis 2019, déprécié suite à la fin du support par Meta
  * [*Cloud API*](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-whatsapp-cloud) : connecteur Tock utilisé en production depuis 2024

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-whatsapp](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-whatsapp-cloud) sur GitHub.

### Teams

![logo teams](../../img/teams.png "teams"){style="width:100px;"}


* **Canal** : [Microsoft Teams](https://products.office.com/fr-fr/microsoft-teams/)
* **Type** : texte + voix
* **Status** : connecteur Tock utilisé en production depuis 2019

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-teams](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-teams) sur GitHub.

### Business Chat

![logo BusinessChat Logo](../../img/message.png "BusinessChat Logo"){style="width:50px;"}

* **Canal** : [Apple Business Chat (Messages)](https://www.apple.com/fr/ios/business-chat/)
* **Type** : texte
* **Status** : connecteur Tock utilisé en production depuis 2019

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-businesschat](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-businesschat) sur GitHub.

### Twitter

![logo twitter](../../img/twitter.png "twitter"){style="width:100px;"}

* **Canal** : [Twitter](https://twitter.com/) (messages privés)
* **Type** : texte
* **Status** : connecteur Tock précédemment utilisé en production, pas de support actif

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-twitter](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-twitter) sur GitHub.

### Allo-Media

![logo BusinessChat Logo](../../img/allomedia.png "BusinessChat Logo"){style="width:75px;"}

* **Canal** : [Allo-Media](https://www.allo-media.net/) (téléphonie)
* **Type** : voix
* **Status** : connecteur Tock utilisé en production depuis 2020

Ce connecteur a été développé pour le bot [AlloCovid](https://www.allocovid.com/).
Pour en savoir plus, voir la classe [AlloMediaConnector](https://github.com/theopenconversationkit/allocovid/blob/master/src/main/kotlin/AlloMediaConnector.kt)
avec les [sources du bot](https://github.com/theopenconversationkit/allocovid) sur GitHub.

### Google Chat

![logo google chat ](../../img/ggchat.png "google chat"){style="width:50px;"}

* **Canal** : [Google Chat](https://chat.google.com) (anciennement Google Hangouts)
* **Type** : texte
* **Status** : connecteur Tock utilisé hors production

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-google-chat](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-google-chat) sur GitHub.

### Mattermost

![logo mattermost Logo](../../img/mattermost.svg "Mattermost"){style="width:50px;"}

* **Channel** : [Mattermost](https://mattermost.com/)
* **Type** : text
* **Status** : Tock connector not used for production (no use case yet)

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier
[connector-mattermost](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-mattermost) sur GitHub.

### Web generique

![logo web](../../img/web.png "web"){style="width:75px;"}

Ce connecteur générique permet d'intégrer un bot Tock à n'importe quel site Web ou application :
portail, application Web ou mobile, client REST, etc.

Le connecteur expose une API REST vers le bot, facilement intégrable depuis n'importe quelle application Web ou mobile, ou langage de programmation.

Plusieurs kits et composants basés sur le connecteur Web sont déjà disponibles pour intégrer des bots Tock à 
différents sites et applications, comme des sites Web avec [React](canaux.md#react), 
des applications mobiles natives avec [Flutter](canaux.md#flutter-beta) ou encore des 
intranets [SharePoint](canaux.md#sharepoint-beta).

* **Canal** : Web (générique pour tous sites & applications Web)
* **Type** : texte
* **Status** : connecteur Tock utilisé en production depuis 2020

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier
[connector-web](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-web) sur GitHub.
Il contient exemples et documentation format _Swagger_ de l'API REST.


### Test generique

![logo web](../../img/web.png "web"){style="width:75px;"}

 
Ce connecteur est interne à Tock, il sert à dialoguer avec un bot directement dans l'interface 
_Tock Studio_ (vue _Test_ > _Test the bot_) en émulant d'autres connecteurs.


## Integrations via le connecteur Web

Le _connecteur Web_ expose une API générique pour interagir avec un bot Tock.
En conséquence, il permet encore davantage d'intégrations côté "frontend", utilisant cette API comme passerelle.

### React

![logo React](../../img/React.png "React"){style="width:50px;"}

Ce composant React intègre un bot Tock et en assure le rendu graphique dans une application Web.  
L'application Web communique avec le bot via un [connecteur Web](#web-generique).

* **Intégration** : [React](https://fr.reactjs.org/) (JavaScript / JSX)
* **Type** : applications Web
* **Status** : utilisé en production depuis 2020

Pour en savoir plus, voir les sources et le _README_ dans le dépôt 
[`tock-react-kit`](https://github.com/theopenconversationkit/tock-react-kit) sur GitHub.

### Flutter *(beta)*


![logo flutter](../../img/flutter.png "allo media"){style="width:50px;"}


Ce composant Flutter intègre un bot Tock et en assure le rendu graphique dans une application mobile ou Web.  
L'application communique avec le bot via un [connecteur Web](#web-generique).

* **Intégration** : [Flutter](https://flutter.dev/) (Dart)
* **Type** : applications mobiles natives et Web
* **Status** : beta, en développement

Pour en savoir plus, voir les sources et le _README_ dans le dépôt 
[`tock-flutter-kit`](https://github.com/theopenconversationkit/tock-flutter-kit) sur GitHub.

### SharePoint *(beta)*

![logo Sharepoint](../../img/sharepoint.png "Sharepopint"){style="width:50px;"}

Ce composant _WebPart_ permet d'intégrer un bot Tock dans un site SharePoint.  
Il embarque le [tock-react-kit](canaux.md#react) pour communiquer avec le bot 
via un [connecteur Web](canaux.md#web-generique) et gérer le rendu graphique du bot dans la page SharePoint.

* **Intégration** : [Microsoft SharePoint](https://www.microsoft.com/fr-fr/microsoft-365/sharepoint/collaboration)
* **Type** : sites Web & intranets
* **Status** : beta, en développement

Pour en savoir plus, voir les sources et le _README_ dans le dépôt 
[`tock-sharepoint`](https://github.com/theopenconversationkit/tock-sharepoint) sur GitHub.


## Technologies vocales

Les bots Tock traitent des phrases en format texte par défaut (_chatbots_). Néanmoins, on peut 
intégrer des technologies vocales aux "bornes" du bot afin d'obtenir des conversations vocales (_voicebots_ et _callbots_) :

- Traduction de la voix en texte (_Speech-To-Text_) en amont du traitement par le bot (ie. avant l'étape _NLU_)
- Traduction du texte en voix (_Text-To-Speech_) en aval du traitement par le bot (ie. synthèse vocale de la réponse du bot)

Certains _connecteurs_ fournis avec Tock permettent d'intégrer un bot à un canal externe 
gérant les aspects vocaux STT et TTS.

En outre, d'autres technologies vocales ont pu être intégrées à Tock ces dernières années.
Elles sont mentionnées à titre indicatif, même quand il n'est pas fourni de _connecteur_ prêt à l'emploi.

### Google / Android

Les fonctions _Speech-To-Text_ et _Text-To-Speech_ de Google sont utilisées à travers le 
[connecteur Google Assistant / Home](canaux.md#integrations-via-le-connecteur-web), également par les fonctions 
vocales de l'[application Microsoft Teams pour Android](https://play.google.com/store/apps/details?id=com.microsoft.teams)
compatible avec le [connecteur Teams](#teams), ainsi qu'au sein de la plateforme Android 
notamment pour des développements mobiles natifs.

![logo android](../../img/android.png "allo media"){style="width:75px;"}

![Logo Google assistant](../../img/googelassist.png "google assistant"){style="width: 75px;"}

![logo google home](../../img/googlehome.png "google home "){style="width:75px;"}

![logo teams](../../img/teams.png "teams"){style="width:75px;"}

* **Technologie** : STT & TTS Google / Android
* **Status** : utilisé avec Tock en production 
(via connecteurs [Google Assistant / Home](#google-assistant-home), 
[Microsoft Teams](canaux.md#teams) 
et en natif Android pour les bots intégrés _on-app_)
 
### Apple / iOS

Les fonctions _Speech-To-Text_ et _Text-To-Speech_ d'Apple sont utilisées à travers le 
[connecteur Business Chat](#business-chat), ainsi qu'au sein d'iOS
pour des développements mobiles natifs.

![logo ios](../../img/ios.png "ios"){style="width:50px;"}

![logo BusinessChat Logo](../../img/message.png "BusinessChat Logo"){style="width:50px;"}

* **Technologie** : STT & TTS Apple / iOS
* **Status** : utilisé avec Tock en production (via connecteur Business Chat 
et en natif iOS pour les bots intégrés _on-app_)
 
### Amazon / Alexa

Les fonctions _Speech-To-Text_ et _Text-To-Speech_ d'Alexa (Amazon) sont utilisées à travers le 
[connecteur Alexa / Echo](#alexa-echo).

![Logo Alexa](../../img/alexa2.png "alexa"){style="width: 75px;"}

* **Technologie** : STT & TTS Amazon / Alexa
* **Status** : utilisé avec Tock en production (via connecteur Alexa)
 
### Allo-Media & Voxygen

La société [Allo-Media](https://www.allo-media.net/) propose une plateforme IA basée sur les appels téléphoniques.

[Voxygen](https://www.voxygen.fr/) propose des services de synthèse vocale.

A l'occasion du développement du bot [AlloCovid](https://www.allocovid.com/), un [connecteur Allo-Media](#allo-media)
a été développé pour intégrer le bot (Tock) aux services Allo-Media : 
_Speech-To-Text_ et _Text-To-Speech_ avec Voxygen.

![logo android](../../img/android.png "allo media"){style="width:50px;"}

![Logo voxygen](../../img/voxygen.png "Voxygen"){style="width: 100px;"}

* **Technologie** : Allo-Media & Voxygen
* **Status** : utilisé avec Tock en production (via connecteur Allo-Media)
 
### Nuance

[Nuance](https://www.nuance.com) propose des solutions de reconnaissance vocale & IA.

Pour des expérimentations de commande vocale en 2016, Nuance avait été
intégré à Tock pour ses fonctions _Speech-To-Text_. 
Même si cette intégration n'a pas été maintenue depuis, cela fonctionnait
après quelques jours de mise en place.

![Logo nuance](../../img/nuance.png "Nuance"){style="width: 100px;"}

* **Technologie** : Nuance
* **Status** : utilisé avec Tock en 2016

## Architecture de connecteurs & gouvernance des données

Dans une optique de _gouvernance_ des modèles et données conversationnelles, l'architecture en connecteurs 
Tock présente plusieurs avantages :

* Le modèle est construit dans Tock, il n'est pas partagé via les connecteurs
* Le choix des connecteurs d'un bot permet de maitriser la propagation (ou non) des conversations

> Par exemple, pour un bot interne à une entreprise, on peut choisir de n'utiliser que des connecteurs 
>vers des canaux propres (site Web, etc.) ou internes à l'entreprise (applications d'entreprise, espace pro sur 
>un téléphone Android, etc.). 

* Même si un bot est connecté à plusieurs canaux/partenaires externes, seule la plateforme Tock possède l'ensemble des
conversations sur tous ces canaux.

## Developper son propre connecteur

Il est possible de créer son propre connecteur Tock, par exemple pour interfacer un bot Tock avec un canal propre à 
l'organisation (souvent un site Web ou une application mobile spécifiques), ou bien quand un canal grand public 
s'ouvre aux bots conversationnels et que le connecteur Tock n'existe pas encore.

La section [_Bot Framework_](../../dev/bot-integre.md) du manuel développeur Tock donne des indications pour 
implémenter son propre connecteur.

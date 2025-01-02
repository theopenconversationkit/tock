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
<!-->To do bug img slack, ggassistant, whatsapp, teams, twitter.....<!-->
- Les [_connecteurs_](../canaux#connecteurs-fournis-avec-tock) fournis avec la distribution Tock :  
[<img alt="Logo Messenger" title="Facebook Messenger"
      src="https://cdn.iconscout.com/icon/free/png-256/facebook-messenger-2-569346.png" 
      style="width: 50px;">](../canaux#messenger)
[<img alt="Logo Slack" title="Slack"
 src="https://www.macupdate.com/images/icons256/50617.png" 
 style="width: 50px;">](../canaux#slack)
[<img alt="Logo Google Assistant" title="Google Assistant"
 src="https://res-5.cloudinary.com/crunchbase-production/image/upload/c_lpad,h_120,w_120,f_auto,b_white,q_auto:eco/g0oshbe7blfnsrylchxd" 
 style="width: 50px;">](../canaux#google-assistant-home)
[<img alt="Logo Google Home" title="Google Home"
 src="https://phoneky.co.uk/thumbs/android/thumbs/ico/3/chromecast-android.jpg" 
 style="width: 50px;">](../canaux#google-assistant-home)
[<img alt="Logo Alexa" title="Amazon Alexa / Amazon Echo"
 src="https://cognyapps.com/wp-content/uploads/2018/09/amazon-alexa-logo-e1538253665426.png" 
 style="width: 50px;">](../canaux#alexa-echo)
[<img alt="Logo RocketChat" title="Rocket.Chat"
 src="https://dl2.macupdate.com/images/icons256/58493.png?d=1565347347" 
 style="width: 50px;">](../canaux#rocketchat)
[<img alt="Logo WhatsApp" title="Facebook WhatsApp"
 src="https://appradarcentral.com/wp-content/uploads/2017/07/WhatsApp-Messenger.png" 
 style="width: 50px;">](../canaux#whatsapp)
[<img alt="Logo Teams" title="Microsoft Teams"
 src="https://cdn.worldvectorlogo.com/logos/microsoft-teams.svg" 
 style="width: 50px;">](../canaux#teams)
[<img alt="Logo Business Chat" title="Apple Business Chat"
 src="http://cdn.osxdaily.com/wp-content/uploads/2014/11/Messages-icon-300x300.png" 
 style="width: 50px;">](../canaux#business-chat)
[<img alt="Logo Twitter" title="Twitter"
 src="https://d2v9ipibika81v.cloudfront.net/uploads/sites/112/2016/06/twitter-logo.jpg" 
 style="width: 50px;">](../canaux#twitter)
[<img alt="Logo Allo-Media" title="Allo-Media"
  src="https://s3-eu-central-1.amazonaws.com/glassdollar/logos/GD_5bcf9307048f6.png" 
  style="width: 50px;">](../canaux#allo-media)
[<img alt="Logo Google Chat" title="Google Chat"
   src="https://cdn.zapier.com/storage/photos/bfbce5bee25b1b50d8a910c30588c61e.png" 
   style="width: 50px;">](../canaux#google-chat)  
[<img alt="Logo Web" title="Web (générique)"
 src="https://static.vecteezy.com/system/resources/previews/000/425/842/non_2x/vector-web-search-icon.jpg" 
 style="width: 50px;">](../canaux#web-générique)
[<img alt="Logo Test" title="Test (générique)"
 src="https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcR_8-ubeyOzkkKclCUX3V-LSJVik_u8wtbJs6FBGWp9P19kzAQH&usqp=CAU" 
 style="width: 50px;">](../canaux#test-generique)

- Les [kits utilisant le _connecteur Web_](../canaux#integrations-via-le-connecteur-web) pour intégrer d'autres canaux :  
[<img alt="Logo React" title="React"
      src="https://blog.octo.com/wp-content/uploads/2015/12/react-logo-1000-transparent.png" 
      style="width: 50px;">](../canaux#react)
[<img alt="Logo Flutter" title="Google Flutter"
 src="https://plugins.jetbrains.com/files/9212/97400/icon/pluginIcon.png" 
 style="width: 60px;">](../canaux#flutter-beta)
[<img alt="Logo SharePoint" title="Microsoft SharePoint"
 src="https://expertime.com/wp-content/uploads/2019/12/Logo_SharePoint-expertime.png" 
 style="width: 50px;">](../canaux#sharepoint-beta)

- Les [intégrations possibles pour le traitement de la voix](../canaux#technologies-vocales) :  
[<img alt="Logo Android" title="Google Android"
 src="https://www.wortis.fr/wp-content/uploads/2019/05/icon-wortis-android.png" 
 style="width: 50px;">](../canaux#google-android)
[<img alt="Logo Google Assistant" title="Google Assistant"
 src="https://res-5.cloudinary.com/crunchbase-production/image/upload/c_lpad,h_120,w_120,f_auto,b_white,q_auto:eco/g0oshbe7blfnsrylchxd" 
 style="width: 50px;">](../canaux#google-android)
[<img alt="Logo Google Home" title="Google Home"
 src="https://phoneky.co.uk/thumbs/android/thumbs/ico/3/chromecast-android.jpg" 
 style="width: 50px;">](../canaux#google-android)
[<img alt="Logo Teams" title="Microsoft Teams"
 src="https://cdn.worldvectorlogo.com/logos/microsoft-teams.svg" 
 style="width: 50px;">](../canaux#google-android)
[<img alt="Logo iOS" title="Apple iOS"
 src="https://www.freeiconspng.com/uploads/app-ios-png-4.png" 
 style="width: 50px;">](../canaux#apple-ios)
[<img alt="Logo BusinessChat" title="Apple Business Chat"
 src="http://cdn.osxdaily.com/wp-content/uploads/2014/11/Messages-icon-300x300.png" 
 style="width: 50px;">](../canaux#apple-ios)
[<img alt="Logo Alexa" title="Amazon Alexa"
 src="https://cognyapps.com/wp-content/uploads/2018/09/amazon-alexa-logo-e1538253665426.png" 
 style="width: 50px;">](../canaux#amazon-alexa)
[<img alt="Logo Allo-Media" title="Allo-Media"
 src="https://s3-eu-central-1.amazonaws.com/glassdollar/logos/GD_5bcf9307048f6.png" 
 style="width: 50px;">](../canaux#allo-media-voxygen)
[<img alt="Logo Voxygen" title="Voxygen" 
 src="https://res-1.cloudinary.com/crunchbase-production/image/upload/c_lpad,h_256,w_256,f_auto,q_auto:eco/v1502521579/dyuj1cgjsnzhpo6ojwq0.png" 
 style="width: 50px;">](../canaux#allo-media-voxygen)
[<img alt="Logo Nuance" title="Nuance"
 src="https://www.dicteedragon.fr/img/m/2.jpg" 
 style="width: 50px;">](../canaux#nuance)

## Connecteurs fournis avec Tock

Tock fournit de nombreux connecteurs pour différents types de canaux (voir ci-dessous). De nouveaux connecteurs sont 
régulièrement ajoutés à la plateforme, en fonction des besoins projets mais aussi du calendrier d'ouverture aux bots 
des canaux grand public.
 
 > Exemples : arrivée de Google Home en France en 2017, Alexa en 2018, ouverture des API WhatsApp puis Business Chat en 2019, etc. 

Pour en savoir plus sur les bots référencés utilisant tel ou tel connecteur en production, 
n'hésitez pas à consulter la page [vitrine Tock](../../apropos/vitrine).

### Messenger

<img alt="Logo Messenger" title="Facebook Messenger"
src="https://cdn.iconscout.com/icon/free/png-256/facebook-messenger-2-569346.png" 
style="width: 100px;">

* **Canal** : [Facebook Messenger](https://www.messenger.com/)
* **Type** : texte _(+ voix via l'upload de messages vocaux)_
* **Status** : connecteur Tock utilisé en production depuis 2016

Le guide [Connecter son bot à Messenger](../../guide/messenger) explique comment intégrer un bot 
Tock avec une page Facebook / [Messenger](https://www.messenger.com/).

Pour en savoir plus sur ce connecteur, vous pouvez aussi vous rendre dans le dossier 
[connector-messenger](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-messenger) sur GitHub, 
où vous retrouverez les sources et le _README_ du connecteur.

### Slack
<!-->To do bug img<!-->

<img alt="Logo Slack" title="Slack"
src="https://www.macupdate.com/images/icons256/50617.png" 
style="width: 100px;">

* **Canal** : [Slack](https://slack.com/)
* **Type** : texte
* **Status** : connecteur Tock utilisé hors production

Le guide [Connecter son bot à Slack](../../guide/slack) explique comment intégrer un bot 
Tock avec une _chaîne_ [Slack](https://slack.com/).

Pour en savoir plus sur ce connecteur, vous pouvez aussi vous rendre dans le dossier 
[connector-slack](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-slack) sur GitHub, 
où vous retrouverez les sources et le _README_ du connecteur.

### Google Assistant / Home

<img alt="Logo Google Assistant" title="Google Assistant"
src="https://res-5.cloudinary.com/crunchbase-production/image/upload/c_lpad,h_120,w_120,f_auto,b_white,q_auto:eco/g0oshbe7blfnsrylchxd" 
style="width: 100px;">
<img alt="Logo Google Home" title="Google Home"
src="https://phoneky.co.uk/thumbs/android/thumbs/ico/3/chromecast-android.jpg" 
style="width: 100px;">

* **Canal** : [Google Assistant](https://assistant.google.com/) / [Google Home](https://store.google.com/fr/product/google_home)
* **Type** : texte + voix
* **Status** : connecteur Tock utilisé en production depuis 2017

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-ga](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-ga) sur GitHub.

### Alexa / Echo

<img alt="Logo Alexa" title="Amazon Alexa / Amazon Echo"
src="https://cognyapps.com/wp-content/uploads/2018/09/amazon-alexa-logo-e1538253665426.png" 
style="width: 100px;">

* **Canal** : [Amazon Alexa](https://alexa.amazon.com/) / Amazon Echo
* **Type** : voix
* **Status** : connecteur Tock utilisé en production depuis 2018

Remarque importante : dans le cas d'Alexa, le modèle NLP est forcément construit et hébergé chez Amazon. 
Seul la partie framework conversationel de Tock peut être utilisée.

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-alexa](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-alexa) sur GitHub.

### Rocket.Chat

<img alt="Logo RocketChat" title="Rocket.Chat"
src="https://dl2.macupdate.com/images/icons256/58493.png?d=1565347347" 
style="width: 100px;">

* **Canal** : [Rocket.Chat](https://rocket.chat/)
* **Type** : texte
* **Status** : à préciser

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-rocketchat](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-rocketchat) sur GitHub.

### WhatsApp

<img alt="Logo WhatsApp" title="Facebook WhatsApp"
src="https://appradarcentral.com/wp-content/uploads/2017/07/WhatsApp-Messenger.png" 
style="width: 100px;">

* **Canal** : [WhatsApp from Facebook](https://www.whatsapp.com/)
* **Type** : texte
* **Status** : connecteur Tock utilisé en production depuis 2019

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-whatsapp](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-whatsapp) sur GitHub.

### Teams

<img alt="Logo Teams" title="Microsoft Teams"
src="https://cdn.worldvectorlogo.com/logos/microsoft-teams.svg" 
style="width: 100px;">

* **Canal** : [Microsoft Teams](https://products.office.com/fr-fr/microsoft-teams/)
* **Type** : texte + voix
* **Status** : connecteur Tock utilisé en production depuis 2019

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-teams](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-teams) sur GitHub.

### Business Chat

<img alt="Logo BusinessChat" title="Apple Business Chat"
src="http://cdn.osxdaily.com/wp-content/uploads/2014/11/Messages-icon-300x300.png" 
style="width: 100px;">

* **Canal** : [Apple Business Chat (Messages)](https://www.apple.com/fr/ios/business-chat/)
* **Type** : texte
* **Status** : connecteur Tock utilisé en production depuis 2019

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-businesschat](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-businesschat) sur GitHub.

### Twitter

<img alt="Logo Twitter" title="Twitter"
src="https://d2v9ipibika81v.cloudfront.net/uploads/sites/112/2016/06/twitter-logo.jpg" 
style="width: 100px;">

* **Canal** : [Twitter](https://twitter.com/) (messages privés)
* **Type** : texte
* **Status** : connecteur Tock utilisé en production depuis 2019

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-twitter](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-twitter) sur GitHub.

### Allo-Media

<img alt="Logo Allo-Media" title="Allo-Media"
src="https://s3-eu-central-1.amazonaws.com/glassdollar/logos/GD_5bcf9307048f6.png" 
style="width: 100px;">

* **Canal** : [Allo-Media](https://www.allo-media.net/) (téléphonie)
* **Type** : voix
* **Status** : connecteur Tock utilisé en production depuis 2020

Ce connecteur a été développé pour le bot [AlloCovid](https://www.allocovid.com/).
Pour en savoir plus, voir la classe [AlloMediaConnector](https://github.com/theopenconversationkit/allocovid/blob/master/src/main/kotlin/AlloMediaConnector.kt)
avec les [sources du bot](https://github.com/theopenconversationkit/allocovid) sur GitHub.

### Google Chat

<img alt="Logo Google Chat" title="Google Chat"
src="https://cdn.zapier.com/storage/photos/bfbce5bee25b1b50d8a910c30588c61e.png" 
style="width: 100px;">

* **Canal** : [Google Chat](https://chat.google.com) (anciennement Google Hangouts)
* **Type** : texte
* **Status** : connecteur Tock utilisé hors production

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-google-chat](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-google-chat) sur GitHub.

### Web (générique)

<img alt="Logo Web" title="Web (générique)"
 src="https://static.vecteezy.com/system/resources/previews/000/425/842/non_2x/vector-web-search-icon.jpg" 
 style="width: 100px;">

Ce connecteur générique permet d'intégrer un bot Tock à n'importe quel site Web ou application :
portail, application Web ou mobile, client REST, etc.

Le connecteur expose une API REST vers le bot, facilement intégrable depuis n'importe quelle application Web ou mobile, ou langage de programmation.

Plusieurs kits et composants basés sur le connecteur Web sont déjà disponibles pour intégrer des bots Tock à 
différents sites et applications, comme des sites Web avec [React](../canaux#react), 
des applications mobiles natives avec [Flutter](../canaux#flutter-beta) ou encore des 
intranets [SharePoint](../canaux#sharepoint-beta).

* **Canal** : Web (générique pour tous sites & applications Web)
* **Type** : texte
* **Status** : connecteur Tock utilisé en production depuis 2020

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier
[connector-web](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-web) sur GitHub.
Il contient exemples et documentation format _Swagger_ de l'API REST.


### Test (générique)

<img alt="Logo Web" title="Test (générique)"
 src="https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcR_8-ubeyOzkkKclCUX3V-LSJVik_u8wtbJs6FBGWp9P19kzAQH&usqp=CAU" 
 style="width: 100px;">
 
Ce connecteur est interne à Tock, il sert à dialoguer avec un bot directement dans l'interface 
_Tock Studio_ (vue _Test_ > _Test the bot_) en émulant d'autres connecteurs.


## Intégrations via le connecteur Web

Le _connecteur Web_ expose une API générique pour interagir avec un bot Tock.
En conséquence, il permet encore davantage d'intégrations côté "frontend", utilisant cette API comme passerelle.

### React

<img alt="Logo React" title="React"
src="https://blog.octo.com/wp-content/uploads/2015/12/react-logo-1000-transparent.png" 
style="width: 100px;">

Ce composant React intègre un bot Tock et en assure le rendu graphique dans une application Web.  
L'application Web communique avec le bot via un [connecteur Web](../canaux#web-générique).

* **Intégration** : [React](https://fr.reactjs.org/) (JavaScript / JSX)
* **Type** : applications Web
* **Status** : utilisé en production depuis 2020

Pour en savoir plus, voir les sources et le _README_ dans le dépôt 
[`tock-react-kit`](https://github.com/theopenconversationkit/tock-react-kit) sur GitHub.

### Flutter _(beta)_

<img alt="Logo Flutter" title="Google Flutter"
src="https://plugins.jetbrains.com/files/9212/97400/icon/pluginIcon.png" 
style="width: 100px;">

Ce composant Flutter intègre un bot Tock et en assure le rendu graphique dans une application mobile ou Web.  
L'application communique avec le bot via un [connecteur Web](../canaux#web-générique).

* **Intégration** : [Flutter](https://flutter.dev/) (Dart)
* **Type** : applications mobiles natives et Web
* **Status** : beta, en développement

Pour en savoir plus, voir les sources et le _README_ dans le dépôt 
[`tock-flutter-kit`](https://github.com/theopenconversationkit/tock-flutter-kit) sur GitHub.

### SharePoint _(beta)_

<img alt="Logo SharePoint" title="Microsoft SharePoint"
src="https://expertime.com/wp-content/uploads/2019/12/Logo_SharePoint-expertime.png" 
style="width: 100px;">

Ce composant _WebPart_ permet d'intégrer un bot Tock dans un site SharePoint.  
Il embarque le [tock-react-kit](../canaux#react) pour communiquer avec le bot 
via un [connecteur Web](../canaux#web-générique) et gérer le rendu graphique du bot dans la page SharePoint.

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
[connecteur Google Assistant / Home](../canaux#google-assistant-home), également par les fonctions 
vocales de l'[application Microsoft Teams pour Android](https://play.google.com/store/apps/details?id=com.microsoft.teams)
compatible avec le [connecteur Teams](../canaux#teams), ainsi qu'au sein de la plateforme Android 
notamment pour des développements mobiles natifs.

<img alt="Logo Android" title="Google Android"
src="https://www.wortis.fr/wp-content/uploads/2019/05/icon-wortis-android.png" 
style="width: 100px;">
<img alt="Logo Google Assistant" title="Google Assistant"
src="https://res-5.cloudinary.com/crunchbase-production/image/upload/c_lpad,h_120,w_120,f_auto,b_white,q_auto:eco/g0oshbe7blfnsrylchxd" 
style="width: 50px;">
<img alt="Logo Google Home" title="Google Home"
src="https://phoneky.co.uk/thumbs/android/thumbs/ico/3/chromecast-android.jpg" 
style="width: 50px;">
<img alt="Logo Teams" title="Microsoft Teams"
src="https://cdn.worldvectorlogo.com/logos/microsoft-teams.svg" 
style="width: 50px;">

* **Technologie** : STT & TTS Google / Android
* **Status** : utilisé avec Tock en production 
(via connecteurs [Google Assistant / Home](../canaux#google-assistant-home), 
[Microsoft Teams](../canaux#teams) 
et en natif Android pour les bots intégrés _on-app_)
 
### Apple / iOS

Les fonctions _Speech-To-Text_ et _Text-To-Speech_ d'Apple sont utilisées à travers le 
[connecteur Business Chat](../canaux#business-chat), ainsi qu'au sein d'iOS
pour des développements mobiles natifs.

<img alt="Logo iOS" title="Apple iOS"
src="https://www.freeiconspng.com/uploads/app-ios-png-4.png" 
style="width: 100px;">
<img alt="Logo BusinessChat" title="Apple Business Chat"
src="http://cdn.osxdaily.com/wp-content/uploads/2014/11/Messages-icon-300x300.png" 
style="width: 50px;">

* **Technologie** : STT & TTS Apple / iOS
* **Status** : utilisé avec Tock en production (via connecteur Business Chat 
et en natif iOS pour les bots intégrés _on-app_)
 
### Amazon / Alexa

Les fonctions _Speech-To-Text_ et _Text-To-Speech_ d'Alexa (Amazon) sont utilisées à travers le 
[connecteur Alexa / Echo](../canaux#alexa-echo).

<img alt="Logo Alexa" title="Amazon Alexa"
src="https://cognyapps.com/wp-content/uploads/2018/09/amazon-alexa-logo-e1538253665426.png" 
style="width: 100px;">

* **Technologie** : STT & TTS Amazon / Alexa
* **Status** : utilisé avec Tock en production (via connecteur Alexa)
 
### Allo-Media & Voxygen

La société [Allo-Media](https://www.allo-media.net/) propose une plateforme IA basée sur les appels téléphoniques.

[Voxygen](https://www.voxygen.fr/) propose des services de synthèse vocale.

A l'occasion du développement du bot [AlloCovid](https://www.allocovid.com/), un [connecteur Allo-Media](../canaux#allo-media)
a été développé pour intégrer le bot (Tock) aux services Allo-Media : 
_Speech-To-Text_ et _Text-To-Speech_ avec Voxygen.

<img alt="Logo Allo-Media" title="Allo-Media"
src="https://s3-eu-central-1.amazonaws.com/glassdollar/logos/GD_5bcf9307048f6.png" 
style="width: 100px;"> <img alt="Logo Voxygen" title="Voxygen" 
src="https://res-1.cloudinary.com/crunchbase-production/image/upload/c_lpad,h_256,w_256,f_auto,q_auto:eco/v1502521579/dyuj1cgjsnzhpo6ojwq0.png" 
style="width: 100px;">

* **Technologie** : Allo-Media & Voxygen
* **Status** : utilisé avec Tock en production (via connecteur Allo-Media)
 
### Nuance

[Nuance](https://www.nuance.com) propose des solutions de reconnaissance vocale & IA.

Pour des expérimentations de commande vocale en 2016, Nuance avait été
intégré à Tock pour ses fonctions _Speech-To-Text_. 
Même si cette intégration n'a pas été maintenue depuis, cela fonctionnait
après quelques jours de mise en place.

<img alt="Logo Nuance" title="Nuance"
src="https://www.dicteedragon.fr/img/m/2.jpg" 
style="width: 100px;">

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

## Développer son propre connecteur

Il est possible de créer son propre connecteur Tock, par exemple pour interfacer un bot Tock avec un canal propre à 
l'organisation (souvent un site Web ou une application mobile spécifiques), ou bien quand un canal grand public 
s'ouvre aux bots conversationnels et que le connecteur Tock n'existe pas encore.

La section [_Bot Framework_](../../dev/bot-integre) du manuel développeur Tock donne des indications pour 
implémenter son propre connecteur.

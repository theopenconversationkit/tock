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

![logo messenger](../img/messenger.png "Messenger"){style="width:50px;"}
![Logo slack](../img/slack.png "Slack"){style="width: 75px;"}
![logo whatsapp](../img/whatsapp.png "whatsapp"){style="width:50px;"}
![logo teams](../img/teams.png "teams"){style="width:50px;"}
![logo google chat ](../img/ggchat.png "google chat"){style="width:50px;"}
![logo mattermost](../img/mattermost.svg "Mattermost"){style="width:50px;"}
![logo web](../img/web.png "web"){style="width:50px;"}
![logo web](../img/openai.png "Open AI"){style="width:50px;"}
![logo test](../img/test.jpeg "test"){style="width:50px;"}

- Les [kits utilisant le _connecteur Web_](#integrations-via-le-connecteur-web) pour intégrer d'autres canaux :  

![logo React](../img/React.png "React"){style="width:50px;"}
![logo Vue](../img/Vue.svg "Vue"){style="width:50px;"}
![logo Sharepoint](../img/sharepoint.png "SharePoint"){style="width:50px;"}

- Les [intégrations possibles pour le traitement de la voix](index.md#technologies-vocales) :  
![logo android](../img/android.png "Android"){style="width:50px;"}
![logo teams](../img/teams.png "teams"){style="width:50px;"}
![logo ios](../img/ios.png "ios"){style="width:50px;"}
![Logo voxygen](../img/voxygen.png "Voxygen"){style="width: 100px;"}
![Logo nuance](../img/nuance.png "Nuance"){style="width: 75px;"}

## Connecteurs fournis avec Tock

| Canal | Type | Connecteur | Documentation |
|-------|------|------------|---------------|
| Web (sites, applications mobiles) | texte | `web` | [Web](web.md) |
| [WhatsApp](https://www.whatsapp.com/) | texte | `whatsapp_cloud` | [WhatsApp](whatsapp.md) |
| [Messenger](https://www.messenger.com/) | texte | `messenger` | [Messenger](messenger.md) |
| [Microsoft Teams](https://www.microsoft.com/microsoft-teams/) | texte | `teams` | [Teams](teams.md) |
| [Slack](https://slack.com/) | texte | `slack` | [Slack](slack.md) |
| [Google Chat](https://workspace.google.com/products/chat/) | texte | `google_chat` | [Google Chat](google-chat.md) |
| [Mattermost](https://mattermost.com/) | texte | `mattermost` | [Mattermost](mattermost.md) |
| [iAdvize](https://www.iadvize.com/) | texte | `iadvize` | [iAdvize](iadvize.md) |
| [Alcmeon](https://www.alcmeon.com/) | texte | `alcmeon` | [Alcmeon](alcmeon.md) |
| Clients compatibles OpenAI (ex. [Open WebUI](https://docs.openwebui.com/)) | texte | `openai` | [API OpenAI](openai.md) |

De nouveaux connecteurs sont régulièrement ajoutés, en fonction des besoins des projets.
Les connecteurs Alexa, Google Assistant, Twitter, Apple Business Chat et Rocket.Chat ont été retirés dans Tock 26.3.5
(voir [Mettre à jour Tock](../operate/upgrade.md#2635)).
Pour en savoir plus sur les bots utilisant ces connecteurs en production, voir la [vitrine Tock](../project/showcase.md).

Le connecteur _test_ est interne à Tock : il permet de parler à un bot directement dans _Tock Studio_
(menu _Test_), en émulant les autres connecteurs.

## Integrations via le connecteur Web

Le _connecteur Web_ expose une API générique pour interagir avec un bot Tock.
En conséquence, il permet encore davantage d'intégrations côté "frontend", utilisant cette API comme passerelle.

### React

![logo React](../img/React.png "React"){style="width:50px;"}

Ce composant React intègre un bot Tock et en assure le rendu graphique dans une application Web.  
L'application Web communique avec le bot via un [connecteur Web](web.md).

* **Intégration** : [React](https://fr.reactjs.org/) (JavaScript / JSX)
* **Type** : applications Web
* **Status** : utilisé en production depuis 2020

Pour en savoir plus, voir les sources et le _README_ dans le dépôt 
[`tock-react-kit`](https://github.com/theopenconversationkit/tock-react-kit) sur GitHub.

### Vue

![logo Vue](../img/Vue.svg "Vue"){style="width:50px;"}

Ce widget de chat Vue 3 intègre un bot Tock et en assure le rendu graphique dans une page ou une application Web.  
C'est une alternative au [kit React](#react) : il s'intègre aussi bien dans une simple page HTML que dans une
application Vue, Angular, React ou Svelte. La page communique avec le bot via un [connecteur Web](web.md).
Son apparence et ses libellés sont personnalisables, par exemple avec le
[Tock Vue Kit Editor](https://github.com/theopenconversationkit/tock-vue-kit-editor).

* **Intégration** : [Vue](https://fr.vuejs.org/) 3 (JavaScript / TypeScript)
* **Type** : applications et sites Web
* **Status** : utilisé en production, publié sur [npm](https://www.npmjs.com/package/tock-vue-kit),
  voir la [page de démonstration](https://doc.tock.ai/tock-vue-kit/)

Pour en savoir plus, voir les sources et le _README_ dans le dépôt 
[`tock-vue-kit`](https://github.com/theopenconversationkit/tock-vue-kit) sur GitHub.

### SharePoint *(beta)*

![logo Sharepoint](../img/sharepoint.png "SharePoint"){style="width:50px;"}

Ce composant _WebPart_ permet d'intégrer un bot Tock dans un site SharePoint.  
Il embarque le [tock-react-kit](index.md#react) pour communiquer avec le bot 
via un [connecteur Web](web.md) et gérer le rendu graphique du bot dans la page SharePoint.

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

Les fonctions _Speech-To-Text_ et _Text-To-Speech_ de Google sont utilisées par les fonctions
vocales de l'[application Microsoft Teams pour Android](https://play.google.com/store/apps/details?id=com.microsoft.teams)
compatible avec le [connecteur Teams](teams.md), ainsi qu'au sein de la plateforme Android
pour des développements mobiles natifs.

![logo android](../img/android.png "android"){style="width:75px;"}

![logo teams](../img/teams.png "teams"){style="width:75px;"}

* **Technologie** : STT & TTS Google / Android
* **Status** : utilisé avec Tock en production
(via le connecteur [Microsoft Teams](teams.md) et en natif Android pour les bots intégrés _on-app_)

### Apple / iOS

Les fonctions _Speech-To-Text_ et _Text-To-Speech_ d'Apple sont utilisées au sein d'iOS
pour des développements mobiles natifs.

![logo ios](../img/ios.png "ios"){style="width:50px;"}

* **Technologie** : STT & TTS Apple / iOS
* **Status** : utilisé avec Tock en production (en natif iOS pour les bots intégrés _on-app_)

### Allo-Media & Voxygen

La société [Allo-Media](https://www.allo-media.net/) propose une plateforme IA basée sur les appels téléphoniques.

[Voxygen](https://www.voxygen.fr/) propose des services de synthèse vocale.

A l'occasion du développement du bot [AlloCovid](https://www.allocovid.com/), un [connecteur Allo-Media](https://github.com/theopenconversationkit/allocovid/blob/master/src/main/kotlin/AlloMediaConnector.kt)
a été développé pour intégrer le bot (Tock) aux services Allo-Media : 
_Speech-To-Text_ et _Text-To-Speech_ avec Voxygen.

![logo android](../img/android.png "Android"){style="width:50px;"}

![Logo voxygen](../img/voxygen.png "Voxygen"){style="width: 100px;"}

* **Technologie** : Allo-Media & Voxygen
* **Status** : utilisé avec Tock en production (via connecteur Allo-Media)
 
### Nuance

En 2016, le _Speech-To-Text_ de [Nuance](https://www.nuance.com) a été intégré à Tock pour des expérimentations de commande vocale.
Cette intégration n'est plus maintenue.

## Architecture de connecteurs & gouvernance des données

Dans une optique de _gouvernance_ des modèles et données conversationnelles, l'architecture en connecteurs 
Tock présente plusieurs avantages :

* Le modèle est construit dans Tock, il n'est pas partagé via les connecteurs
* Le choix des connecteurs d'un bot permet de maitriser la propagation (ou non) des conversations

> Par exemple, pour un bot interne à une entreprise, on peut choisir de n'utiliser que des connecteurs 
> vers des canaux propres (site Web, etc.) ou internes à l'entreprise (applications d'entreprise, espace pro sur 
> un téléphone Android, etc.). 

* Même si un bot est connecté à plusieurs canaux/partenaires externes, seule la plateforme Tock possède l'ensemble des
conversations sur tous ces canaux.

## Developper son propre connecteur

Il est possible de créer son propre connecteur Tock, par exemple pour interfacer un bot Tock avec un canal propre à 
l'organisation (souvent un site Web ou une application mobile spécifiques), ou bien quand un canal grand public 
s'ouvre aux bots conversationnels et que le connecteur Tock n'existe pas encore.

La section [Développer son propre connecteur](../develop/connectors.md#developper-son-propre-connecteur) du manuel développeur Tock donne des indications pour 
implémenter son propre connecteur.

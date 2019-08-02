# Construire un bot multicanal avec Tock

## Notion de _connecteur_

Un _connecteur_ Tock permet d'intégrer un bot à un canal de communication textuel ou vocal externe.
Mis à part le type _connecteur de test_, dédié aux tests via l'interface _Tock Studio_, les connecteurs 
sont associés à des canaux externes à la plateforme Tock.

Tout l'intérêt des connecteurs Tock réside dans la possibilité de développer des assistants conversationnels 
indépendamment du ou des canaux utilisés pour lui parler. Il est ainsi possible de créer un bot pour un canal,
puis le rendre multicanal par la suite en ajoutant des connecteurs.

## Connecteurs fournis avec Tock

Tock fournit de nombreux connecteurs pour différents types de canaux (voir ci-dessous). De nouveaux connecteurs sont 
régulièrement ajoutés à la plateforme, en fonction des besoins projets mais aussi du calendrier d'ouverture aux bots 
des canaux grand public.
 
 > Exemples : arrivée de Google Home en France en 2017, Alexa en 2018, ouverture des API WhatsApp puis Business Chat en 2019, etc. 

### Messenger

<img alt="Logo Messenger" 
src="https://cdn.iconscout.com/icon/free/png-256/facebook-messenger-2-569346.png" 
style="width: 100px;">

* **Canal** : [Facebook Messenger](https://www.messenger.com/)
* **Type** : texte _(+ voix via l'upload de messages vocaux)_
* **Status** : connecteur Tock utilisé en production depuis 2016

Le guide [Connecter son bot à Messenger](../guide/messenger.md) explique comment intégrer un bot 
Tock avec une page Facebook / [Messenger](https://www.messenger.com/).

Pour en savoir plus sur ce connecteur, vous pouvez aussi vous rendre dans le dossier 
[connector-messenger](https://github.com/voyages-sncf-technologies/tock/tree/master/bot/connector-messenger) sur github, 
où vous retrouverez les sources et le _README_ du connecteur.

### Slack

<img alt="Logo Slack" 
src="https://www.macupdate.com/images/icons256/50617.png" 
style="width: 100px;">

* **Canal** : [Slack](https://slack.com/)
* **Type** : texte
* **Status** : connecteur Tock utilisé hors production

Le guide [Connecter son bot à Slack](../guide/slack.md) explique comment intégrer un bot 
Tock avec une _chaîne_ [Slack](https://slack.com/).

Pour en savoir plus sur ce connecteur, vous pouvez aussi vous rendre dans le dossier 
[connector-slack](https://github.com/voyages-sncf-technologies/tock/tree/master/bot/connector-slack) sur github, 
où vous retrouverez les sources et le _README_ du connecteur.

### Google Assistant / Google Home

<img alt="Logo Google Assistant" 
src="https://res-5.cloudinary.com/crunchbase-production/image/upload/c_lpad,h_120,w_120,f_auto,b_white,q_auto:eco/g0oshbe7blfnsrylchxd" 
style="width: 100px;">

* **Canal** : [Google Assistant](https://assistant.google.com/) / [Google Home](https://store.google.com/fr/product/google_home)
* **Type** : texte + voix
* **Status** : connecteur Tock utilisé en production depuis 2017

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-ga](https://github.com/voyages-sncf-technologies/tock/tree/master/bot/connector-ga) sur github.

### Alexa / Echo

<img alt="Logo Alexa" 
src="https://cdn.wezift.com/assets/apps/amazon-alexa/logo/_imgSingle/9ec0bc97e359276643553d8c33e49872.png?mtime=20171201192101" 
style="width: 100px;">

* **Canal** : [Amazon Alexa](https://alexa.amazon.com/)
* **Type** : voix
* **Status** : connecteur Tock utilisé en production depuis 2018

Remarque importante : dans le cas d'Alexa, le modèle NLP est forcément construit et hébergé chez Amazon. 
Seul la partie framework conversationel de Tock peut être utilisée.

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-alexa](https://github.com/voyages-sncf-technologies/tock/tree/master/bot/connector-alexa) sur github.

### Rocket.Chat

<img alt="Logo RocketChat" 
src="https://dl2.macupdate.com/images/icons256/58493.png?d=1565347347" 
style="width: 100px;">

* **Canal** : [Rocket.Chat](https://rocket.chat/)
* **Type** : texte
* **Status** : à préciser

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-rocketchat](https://github.com/voyages-sncf-technologies/tock/tree/master/bot/connector-rocketchat) sur github.

### WhatsApp

<img alt="Logo WhatsApp" 
src="https://appradarcentral.com/wp-content/uploads/2017/07/WhatsApp-Messenger.png" 
style="width: 100px;">

* **Canal** : [WhatsApp from Facebook](https://www.whatsapp.com/)
* **Type** : texte
* **Status** : connecteur Tock utilisé en production depuis 2019

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-whatsapp](https://github.com/voyages-sncf-technologies/tock/tree/master/bot/connector-whatsapp) sur github.

### Teams

<img alt="Logo Teams" 
src="https://cdn.worldvectorlogo.com/logos/microsoft-teams.svg" 
style="width: 100px;">

* **Canal** : [Microsoft Teams](https://products.office.com/fr-fr/microsoft-teams/)
* **Type** : texte + voix
* **Status** : connecteur Tock utilisé en production depuis 2019

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-teams](https://github.com/voyages-sncf-technologies/tock/tree/master/bot/connector-teams) sur github.

### Business Chat / Messages

<img alt="Logo BusinessChat" 
src="http://cdn.osxdaily.com/wp-content/uploads/2014/11/Messages-icon-300x300.png" 
style="width: 100px;">

* **Canal** : [Apple Business Chat (Messages)](https://www.apple.com/fr/ios/business-chat/)
* **Type** : texte
* **Status** : connecteur Tock utilisé en production depuis 2019

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-businesschat](https://github.com/voyages-sncf-technologies/tock/tree/master/bot/connector-businesschat) sur github.

### Twitter

<img alt="Logo Twitter" 
src="https://d2v9ipibika81v.cloudfront.net/uploads/sites/112/2016/06/twitter-logo.jpg" 
style="width: 100px;">

* **Canal** : [Twitter](https://twitter.com/) (messages privés)
* **Type** : texte
* **Status** : connecteur Tock en développement

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-twitter](https://github.com/voyages-sncf-technologies/tock/tree/master/bot/connector-twitter) sur github.

### Web

> Work in progress

### Test

Ce connecteur est interne à Tock, il sert à dialoguer avec un bot directement dans l'interface 
_Tock Studio_ (vue _Test_ > _Test the bot_).

## Architecture & gouvernance des modèles et données

Dans une optique de _gouvernance des modèles et données_ conversationnelles, l'architecture en connecteurs 
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

La section [_Bot Framework_](../dev/bot-integre.md) du manuel développeur Tock donne des indications pour 
implémenter son propre connecteur.

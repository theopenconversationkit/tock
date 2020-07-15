# Configurer son bot pour Messenger

Si vous avez suivi le guide [Créer son premier bot avec _Tock Studio_](studio.md), vous avez déclaré un connecteur
 de type Slack.
 
Dans ce guide, vous allez créer une configuration pour [Facebook Messenger](https://fr-fr.facebook.com/messenger/) 
et intégrer le bot pour dialoguer avec lui sur ce réseau social.

Si vous le souhaitez, vous pouvez aussi sauter cette étape et passer directement à [la suite](api.md).
 
## Ce que vous allez créer

* Une configuration (dans Facebook et dans Tock) pour recevoir et envoyer des messages via Messenger

* Un bot qui parle sur une _page_ Facebook ou dans [Messenger](https://www.messenger.com/)

## Pré-requis

* Environ 20 minutes

* Un bot Tock fonctionnel (par exemple suite au guide [premier bot Tock](studio.md))

* Un compte [Facebook Developer](https://developers.facebook.com/)

## Créer une page Facebook

* Créez une page Facebook

* Donnez-lui un nom (par exemple _My Tock Bot_)

* Recommandation : ne publiez pas la page pour limiter son accès des utilisateurs Messenger : 
_Paramètres > Général > Visibilité de la page > **Non publiée**_

## Créer une application Facebook

* Allez sur la page [Facebook for developers > Voir toutes les applications](https://developers.facebook.com/apps/)

* _Ajouter une app_

* Entrez un nom pour l'_application_

* _Créer un ID d'app_... Notez l'ID de la page, vous en aurez besoin plus tard.

* Ajoutez un produit : _Messenger_

* Dans les paramètres, générez un jeton (_token_) pour votre page. Notez ce _token_ pour la suite.

## Configurer un connecteur Messenger dans Tock

* Dans _Tock Studio_ allez dans _Settings_ > _Configurations_

* Créez un connecteur de type _Messenger_ et ouvrez la section _Connector Custom Configuration_

* Configurez l'ID de page et le _token_ précédemment générés côté Facebook

* Comme _Webhook token_, saisissez `token` (par exemple)

* Dans le champ _Secret_, entrez la clef secrète que vous trouverez dans le portail Facebook for developers :
_paramètres > général > afficher la clef secrète_ 

* Vérifiez que la configuration du connecteur est bien enregistrée

* ...

* Testez votre bot sur Messenger !


## Félicitations!

Votre bot dialogue désormais sur Messenger, en plus des autres canaux auquel vous l'avez intégré.

Le modèle conversationnel, les fonctionnalités et la personnalité de votre assistant sont construits et 
restent indépendants des canaux sur lesquels le bot est présent. Toutefois, rien ne vous empêche de créer 
des parcours ou des réponses spécifiquement pour tel ou tel canal, comme vous le verrez au travers 
de différents outils Tock : écran de gestion des _Responses_, activation d'intentions sur tel ou tel canal 
avec l'écran _Story Rules_, utilisation des _DSLs_ et de la _Bot API_ pour tirer parti de composants graphiques 
spécifiques, etc.


## Continuer...

Dans les sections suivantes vous apprendez à :

* [Créer des parcours programmés en Kotlin](api.md), ouvrant la voie à des comportements complexes et 
l'intégration d'API tierces si besoin

* [Déployer une plateforme Tock](plateforme.md) en quelques minutes avec Docker

Pour en savoir plus sur le connecteur Slack fourni avec Tock, rendez-vous dans le dossier 
[connector-slack](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-slack) sur GitHub, 
où vous retrouverez les sources et le _README_ du connecteur.

Pour en savoir plus sur _Tock Studio_, les fonctionnalités et les modes de déploiement de Tock, vous pouvez aussi 
parcourir le [manuel utilisateur](../toc.md), plus complet.


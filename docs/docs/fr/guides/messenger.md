---
title: Messenger
---

# Configurer son bot pour Messenger

Si vous avez suivi le guide [Créer son premier bot avec _Tock Studio_](../guides/studio.md), vous avez déclaré un connecteur
 de type Slack.

Dans ce guide, vous allez créer une configuration pour [Facebook Messenger](https://fr-fr.facebook.com/messenger/) 
et intégrer le bot pour dialoguer avec lui sur ce réseau social.

Si vous le souhaitez, vous pouvez aussi sauter cette étape et passer directement à [la suite](../guides/api.md).

## Ce que vous allez créer

* Une configuration (dans Facebook et dans Tock) pour recevoir et envoyer des messages via Messenger

* Un bot qui parle sur une _page_ Facebook ou dans [Messenger](https://www.messenger.com/)

## Pré-requis

* Environ 20 minutes

* Un bot Tock fonctionnel (par exemple suite au guide [premier bot Tock](../guides/studio.md))

* Un compte [Facebook Developer](https://developers.facebook.com/)

## Créer une page Facebook

* Créez une page Facebook

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-page-0.png" alt="Créer une page partie 1"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-page-1.png" alt="Créer une page partie 2" 
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 33%;" />

* Donnez-lui un nom (par exemple _My Tock Bot_)

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-page-2.png" alt="Créer une page partie 3"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

> Recommandation : ne publiez pas la page pour limiter son accès des utilisateurs Messenger : 
_Paramètres > Général > Visibilité de la page > **Non publiée**_

## Créer une application Facebook

* Allez sur la page [Facebook for developers > Voir toutes les applications](https://developers.facebook.com/apps/)

* _Ajouter une app_

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-app-0.png" alt="Créer une application partie 1"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

* _Créer une app_ > _Gérer les intégrations professionnelles_

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-app-1.png" alt="Créer une application partie 2"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

* Entrez un nom pour l'_application_

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-app-2.png" alt="Créer une application partie 3" 
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 75%;" />

* Ajoutez un produit : _Messenger_

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/add-messenger-page-0.png" alt="Ajouter messenger à une application partie 1"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/add-messenger-page-1.png" alt="Ajouter messenger à une application partie 2" style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width:  40%;" />

## Créer un connecteur Messenger

* Dans _Tock Studio_ allez dans _Settings_ > _Configurations_ :

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-connector-0.png" 
alt="Créer un connecteur messenger partie 1" 
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 75%;" />

* Créez un connecteur de type _Messenger_ et ouvrez la section _Connector Custom Configuration_

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-connector-1.png" 
alt="Créer un connecteur messenger partie 2" 
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 75%;" />

* Compléter les champs (voir ci-dessous champ par champ) :

  <img src="https://doc.tock.ai/fr/images/doc/connector-messenger/connect-tock-0.png" alt="Connecter Tock partie 1" 
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 75%;" />

* _Application Id_ : il se trouve sur la page de votre application sur [https://developers.facebook.com](https://developers.facebook.com)

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/app-id.png" alt="Trouver l'id d'application" 
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width:  75%;" />

* _Page Id_ : il se trouve sur la page liée à votre application sur [https://facebook.com](https://facebook.com)

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/page-id-0.png" alt="ID de page partie 1" 
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width:  75%;" />

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/page-id-1.png" alt="ID de page partie 2" 
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); width: 75%;" />

* _Call Token_ : le jeton se trouve sur la page de votre application sur [https://developers.facebook.com](https://developers.facebook.com) 

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-app-0.png" alt="Créer une application partie 1"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

* _Créer une app_ > _Gérer les intégrations professionnelles_

<img src="https://doc.tock.ai/fr/images/doc/connector-messenger/create-app-1.png" alt="Créer une application partie 2"
style="box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);" />

* _Persona Id_ : vous pouvez laisser ce champ vide

* Vérifiez que la configuration du connecteur est bien enregistrée

## Configurer l'URL de rappel

* Retournez sur la page de configuration de votre application sur [https://developers.facebook.com](https://developers.facebook.com) :
  _Produits_ > _Messenger_ > _Paramètres_ > **_Webhooks_**

* Cliquez sur _Ajouter l'URL de rappel_ (ou _Modifier l'URL de rappel_ si vous aviez précédemment une autre URL)

* Entrez l'URL à laquelle votre connecteur Tock est actuellement déployé, et le jeton de webhook que vous avez choisi
  lors de la configuration du connecteur
  
> Pour retrouver l'URL de votre connecteur, allez sur la page de configuration _Tock Studio_ > _Settings_ > _Configurations_,
> déroulez la configuration de votre connecteur Messenger, et concaténez le contenu du champ _Application base url_ et celui du champ
> _Relative REST path_.
  
* Cliquez sur _Vérifier et enregistrer_

* Testez votre bot sur Messenger !


## Félicitations !

Votre bot dialogue désormais sur Messenger, en plus des autres canaux auquel vous l'avez intégré.

Le modèle conversationnel, les fonctionnalités et la personnalité de votre assistant sont construits et 
restent indépendants des canaux sur lesquels le bot est présent. Toutefois, rien ne vous empêche de créer 
des parcours ou des réponses spécifiquement pour tel ou tel canal, comme vous le verrez au travers 
de différents outils Tock : écran de gestion des _Responses_, activation d'intentions sur tel ou tel canal 
avec l'écran _Story Rules_, utilisation des _DSLs_ et de la _Bot API_ pour tirer parti de composants graphiques 
spécifiques, etc.


## Continuer...

Dans les sections suivantes vous apprendrez à :

* [Créer des parcours programmés en Kotlin](../guides/api.md), ouvrant la voie à des comportements complexes et 
l'intégration d'API tierces si besoin

* [Déployer une plateforme Tock](platform.md) en quelques minutes avec Docker

Pour en savoir plus sur le connecteur Messenger fourni avec Tock, rendez-vous dans le dossier 
[connector-messenger](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-messenger) sur GitHub, 
où vous retrouverez les sources et le _README_ du connecteur.




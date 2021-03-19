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

![Créer une page partie 1](https://raw.githubusercontent.com/theopenconversationkit/theopenconversationkit.github.io/master/fr/images/doc/connector-messenger/create-page-0.png)

<img src="https://raw.githubusercontent.com/theopenconversationkit/theopenconversationkit.github.io/master/fr/images/doc/connector-messenger/create-page-1.png" alt="Créer une page partie 2" style="zoom:50%;" />

* Donnez-lui un nom (par exemple _My Tock Bot_)

<img src="https://raw.githubusercontent.com/theopenconversationkit/theopenconversationkit.github.io/master/fr/images/doc/connector-messenger/create-page-2.png" alt="Créer une page partie 3" style="zoom: 50%;" />

* Recommandation : ne publiez pas la page pour limiter son accès des utilisateurs Messenger : 
_Paramètres > Général > Visibilité de la page > **Non publiée**_

## Créer une application Facebook

* Allez sur la page [Facebook for developers > Voir toutes les applications](https://developers.facebook.com/apps/)

* _Ajouter une app_

![Créer une application partie 1](https://raw.githubusercontent.com/theopenconversationkit/theopenconversationkit.github.io/master/fr/images/doc/connector-messenger/create-app-0.png)

* _Créer une app_ > *Gérer les intégrations professionnelles*

![Créer une application partie 2](https://raw.githubusercontent.com/theopenconversationkit/theopenconversationkit.github.io/master/fr/images/doc/connector-messenger/create-app-1.png)

* Entrez un nom pour l'_application_

<img src="https://raw.githubusercontent.com/theopenconversationkit/theopenconversationkit.github.io/master/fr/images/doc/connector-messenger/create-app-2.png" alt="Créer une application partie 3" style="zoom:50%;" />

* Ajoutez un produit : _Messenger_

![Ajouter messenger à une application partie 1](https://raw.githubusercontent.com/theopenconversationkit/theopenconversationkit.github.io/master/fr/images/doc/connector-messenger/add-messenger-page-0.png)

<img src="https://raw.githubusercontent.com/theopenconversationkit/theopenconversationkit.github.io/master/fr/images/doc/connector-messenger/add-messenger-page-1.png" alt="Ajouter messenger à une application partie 2" style="zoom: 50%;" />

## Créer un connecteur Messenger

* Dans *Tock Studio* allez dans *Settings* > *Configurations* :

<img src="https://raw.githubusercontent.com/theopenconversationkit/theopenconversationkit.github.io/master/fr/images/doc/connector-messenger/create-connector-0.png" alt="Créer un connecteur messenger partie 1" style="zoom:50%;" />

* Créez un connecteur de type *Messenger* et ouvrez la section *Connector Custom Configuration*

<img src="https://raw.githubusercontent.com/theopenconversationkit/theopenconversationkit.github.io/master/fr/images/doc/connector-messenger/create-connector-1.png" alt="Créer un connecteur messenger partie 2" style="zoom:50%;" />

### Remplir les champs

<img src="https://raw.githubusercontent.com/theopenconversationkit/theopenconversationkit.github.io/master/fr/images/doc/connector-messenger/connect-tock-0.png" alt="Connecter tock partie 1" style="zoom:50%;" />

### 1️⃣ Id d'application

Allez sur https://developers.facebook.com, sur la page de votre application :

<img src="https://raw.githubusercontent.com/theopenconversationkit/theopenconversationkit.github.io/master/fr/images/doc/connector-messenger/app-id.png" alt="Trouver l'id d'application" style="zoom: 33%;" />

### 2️⃣ Id de page

Allez sur https://facebook.com, sur la page de votre application :

<img src="https://raw.githubusercontent.com/theopenconversationkit/theopenconversationkit.github.io/master/fr/images/doc/connector-messenger/page-id-0.png" alt="ID de page partie 1" style="zoom: 33%;" />

<img src="https://raw.githubusercontent.com/theopenconversationkit/theopenconversationkit.github.io/master/fr/images/doc/connector-messenger/page-id-1.png" alt="ID de page partie 2" style="zoom:50%;" />

### 3️⃣ Token d'appel

Retournez sur https://developers.facebook.com, sur la page de votre application :

<img src="https://raw.githubusercontent.com/theopenconversationkit/theopenconversationkit.github.io/master/fr/images/doc/connector-messenger/generate-token.png" alt="Générer un token" style="zoom: 50%;" />

### 4️⃣ Jeton de webhook

Choisissez un jeton quelconque, Facebook l'utilisera pour appeler le webhook Tock.

### 5️⃣ Secret

Toujours sur https://developers.facebook.com, sur la page de votre application :

<img src="https://raw.githubusercontent.com/theopenconversationkit/theopenconversationkit.github.io/master/fr/images/doc/connector-messenger/app-secret.png" alt="Secret" style="zoom:33%;" />



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


# Bienvenue sur Tock : une plateforme conversationnelle ouverte

<img alt="Logo Tock" src="assets/images/logo.svg" style="width: 100px;">


**Tock** (*The Open Conversation Kit*) est une plateforme complète pour construire des agents conversationnels - souvent appelés _bots_. 

Contrairement à la plupart des solutions conversationnelles, Tock ne dépend pas d'API tierces, bien qu'il soit possible d'en intégrer.
L'utilisateur choisit les composants qu'il embarque et peut ainsi conserver la maîtrise de ses modèles et données conversationnelles.

> Tock est utilisé en production depuis plusieurs années par [OUI.sncf](https://www.oui.sncf/services/assistant) pour
> proposer des assistants sur des canaux propres (Web, mobile), réseaux sociaux et enceintes connectées.

> L'ensemble du code source est disponible sur 
> [GitHub](https://github.com/theopenconversationkit/tock) 
> sous une [licence Apache 2](https://github.com/theopenconversationkit/tock/blob/master/LICENSE). 
>
> Tock est une solution soutenue par l'association [_TOSIT (The Open Source I Trust)_](http://tosit.fr/).


## Fonctionnalités

* Assitants _embarqués_ ou intégrés à des sites Web, applications mobiles, réseaux sociaux, enceintes connectées etc.
* Plateforme _NLU_ complète _(Natural Language Understanding - ou TALN en français (Traitement automatique du langage))_, qui utilise différentes librairies open-source comme
[OpenNLP](https://opennlp.apache.org/), [Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/), [Duckling](https://github.com/facebook/duckling),
(et demain [Spacy](https://spacy.io/), [Rasa](https://rasa.com/), ...)
déployable seule si besoin (pour des usages comme l'[Internet des objets](https://fr.wikipedia.org/wiki/Internet_des_objets))
* Interfaces _Tock Studio_ :
    * Gestion des modèles, entraînement du bot
    * Construction de parcours conversationnels sans code
    * Support de l'internationalisation (_i18n_) pour les bots multilingues
    * Suivi des conversations, performances et erreurs des modèles
    * Analyse interactive des tendances / parcours utilisateurs (_Bot Flow_)
* Frameworks pour développer des parcours complexes et intégrer des services tiers : <br/> _DSL_ en [Kotlin](https://kotlinlang.org/) ou [Node](https://nodejs.org) et _API_ tous langages
* Nombreux connecteurs pour [Messenger](https://www.messenger.com/), [WhatsApp](https://www.whatsapp.com/), 
[Google Assistant / Home](https://assistant.google.com/), [Twitter](https://twitter.com/), [Alexa](https://alexa.amazon.com/), 
[Business Chat / iMessage](https://www.apple.com/fr/ios/business-chat/), [Teams](https://products.office.com/fr-fr/microsoft-teams/), 
[Slack](https://slack.com/)... (voir [connecteurs](utilisateur/canaux.md))
* Installation _cloud_ ou _on-premise_, avec ou sans [Docker](https://www.docker.com/), 
bot _"embarqué"_ sans Internet 

![Interface d'admin NLU - qualification de phrase](img/tock-nlp-admin.png "Exemple de qualification de phrase")

## Technologies

L'ensemble de la plateforme est _containerisée_ avec [Docker](https://www.docker.com/). 

La plateforme applicative par défaut est la [JVM](https://fr.wikipedia.org/wiki/Machine_virtuelle_Java).
 
Le langage de référence est [Kotlin](https://kotlinlang.org/) mais d'autres langages de programmation peuvent être utilisés via les API mises à disposition.
 
Tock utilise notamment [Vert.x](http://vertx.io/) et [MongoDB](https://www.mongodb.com ). 

Les interfaces graphiques _(Tock Studio)_ sont écrites avec [Angular](https://angular.io/) en [Typescript](https://www.typescriptlang.org/).

Un widget [React](https://reactjs.org) de base est disponible pour les interfaces Web.

## Démarrer...

* [Table des matières](toc.md)
* [Guides](guide/studio.md) et [plateforme de démonstration](https://demo.tock.ai/)
* Manuels [utilisateur](utilisateur/concepts.md), [développeur](dev/modes.md), [administrateur](admin/architecture.md)
* [Ressources (supports, video)](apropos/ressources.md) et [exemples de code](dev/exemples-code.md)


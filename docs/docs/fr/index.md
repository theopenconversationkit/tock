---
layout: page
title: Bienvenue !
permalink: /fr/
---

# Bienvenue sur Tock : une plateforme conversationnelle ouverte

![NLU interface example - qualifying a sentence](img/favicon.png "NLU interface example - qualifying a sentence")


**Tock** (*The Open Conversation Kit*) est une plateforme complète pour construire des agents conversationnels - souvent appelés _bots_. 

Contrairement à la plupart des solutions conversationnelles, Tock ne dépend pas d'API tierces, bien qu'il soit possible d'en intégrer.
L'utilisateur choisit les composants qu'il embarque et peut ainsi conserver la maîtrise de ses modèles et données conversationnelles.

> Tock est utilisé en production depuis 2016 par [OUI.sncf](https://www.oui.sncf/services/assistant)
> (Web/mobile, réseaux sociaux, enceintes connectées) et [de plus en plus d'organisations](about/showcase.md) 
> (ENEDIS, Linagora, AlloCovid...).

L'ensemble du code source est disponible sur [GitHub](https://github.com/theopenconversationkit/tock) sous 
[licence Apache 2](https://github.com/theopenconversationkit/tock/blob/master/LICENSE). 

## Aperçu

Le site [Tock.ai](https://doc.tock.ai/) est un bon point d'entrée pour découvrir la solution, ses applications et sa communauté grandissante. 
Des [guides](guides/studio.md), [supports](about/ressources.md) et une [video de démonstration](https://www.youtube.com/watch?v=UsKkpYL7Hto) 
(20 minutes, en Anglais) sont également disponibles :

<a href="https://www.youtube.com/watch?v=UsKkpYL7Hto"
target="tock_osxp">

![logo rest-api](img/tockosxp2021.png "rest api")
</a>

## Fonctionnalités

* Assistants autonomes ou intégrés à des sites Web, applications mobiles, réseaux sociaux, enceintes connectées etc. 
sans dépendre d'un canal particulier
* Plateforme _NLU_ complète _<sup>([Natural Language Understanding](https://en.wikipedia.org/wiki/Natural-language_understanding) 
ou [TAL](https://fr.wikipedia.org/wiki/Traitement_automatique_du_langage_naturel) en français)</sup>_
    * Utilisant des briques open-source comme [OpenNLP](https://opennlp.apache.org/), [Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/),
[Duckling](https://github.com/facebook/duckling), [Rasa](https://rasa.com/) 
(demain [Spacy](https://spacy.io/), [CamemBERT](https://camembert-model.fr/), ...)
    * Déployable seule si besoin pour des usages comme l'[_Internet des objets_](https://fr.wikipedia.org/wiki/Internet_des_objets)
* Interfaces _Tock Studio_ :
    * Gestion des modèles, entraînement du bot et performances
    * Construction de parcours conversationnels et arbres de décision sans code
    * Support de l'internationalisation (_i18n_) pour les bots multilingues
    * Suivi des conversations et tendances / parcours utilisateurs (_Analytics_)
* Frameworks pour développer des parcours complexes et intégrer des services tiers : <br/> _DSLs_ en 
[Kotlin](https://kotlinlang.org/), [Javascript/Nodejs](https://nodejs.org/), [Python](https://www.python.org/) 
et _API_ tous langages (voir [_Bot API_](dev/bot-api.md))
* Nombreux connecteurs texte et voix : [Messenger](https://www.messenger.com/), [WhatsApp](https://www.whatsapp.com/), 
[Google Assistant](https://assistant.google.com/), [Alexa](https://alexa.amazon.com/), [Twitter](https://twitter.com/), 
[Apple Business Chat](https://www.apple.com/fr/ios/business-chat/), [Teams](https://products.office.com/fr-fr/microsoft-teams/), 
[Slack](https://slack.com/)... (voir [canaux](user/guides/canaux.md))
* Installation _cloud_ ou _on-premise_, avec ou sans [Docker](https://www.docker.com/), 
même _"embarqué"_ sans Internet 

![NLU interface example - qualifying a sentence](img/tock-nlp-admin.png "NLU interface example - qualifying a sentence")

## Technologies

L'ensemble de la plateforme peut fonctionner _conteneurisée_ (implémentation [Docker](https://www.docker.com/) fournie). 

La plateforme applicative par défaut est la [JVM](https://fr.wikipedia.org/wiki/Machine_virtuelle_Java). 
Le langage de référence est [Kotlin](https://kotlinlang.org/) mais d'autres langages de programmation peuvent être utilisés via les API mises à disposition.

Côté serveur, Tock utilise [Vert.x](http://vertx.io/) et [MongoDB](https://www.mongodb.com ) <sup>(alt. [DocumentDB](https://aws.amazon.com/fr/documentdb/))</sup>. 
Différentes briques _NLU_ peuvent être utilisées, mais Tock n'a pas de dépendance forte envers l'une d'elles.

Les interfaces graphiques _Tock Studio_ sont écrites avec [Angular](https://angular.io/) en [Typescript](https://www.typescriptlang.org/).

Des intégrations [React](https://reactjs.org) et [Flutter](https://flutter.dev/) sont fournies pour les interfaces Web et Mobile.

## Démarrer...

* [Guides](guides/studio.md) et [plateforme de démonstration](https://demo.tock.ai/)
* Manuels [utilisateur](user/concepts.md), [développeur](dev/modes.md), [administrateur](admin/architecture.md)
* [Ressources (supports, video)](about/ressources.md) et [exemples de code](dev/examples-code.md)

[NLU]: https://en.wikipedia.org/wiki/Natural-language_understanding "Natural Language Understanding"
*[NLU]: Natural Language Understanding
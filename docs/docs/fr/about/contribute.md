---
title: Contribuer
---

# Contribuer à Tock

Le projet Tock est ouvert à la contribution et toute proposition est la bienvenue !

Cette page donne des indications sur la structure et les conventions du code de la plateforme.

## TL;DR

Voir [`CONTRIBUTING.md`](https://github.com/theopenconversationkit/tock/blob/master/CONTRIBUTING.md) 
(anglais uniquement).

## Principales technologies

L'ensemble de la plateforme peut fonctionner _conteneurisée_ (implémentation [Docker](https://www.docker.com/) fournie). 

La plateforme applicative par défaut est la [JVM](https://fr.wikipedia.org/wiki/Machine_virtuelle_Java). 
Le langage de référence est [Kotlin](https://kotlinlang.org/) mais d'autres langages de programmation peuvent être utilisés via les API mises à disposition.

Côté serveur, Tock utilise [Vert.x](http://vertx.io/) et [MongoDB](https://www.mongodb.com ) <sup>(alt. [DocumentDB](https://aws.amazon.com/fr/documentdb/))</sup>. 
Différentes briques _NLU_ peuvent être utilisées, mais Tock n'a pas de dépendance forte envers l'une d'elles.

Les interfaces graphiques _Tock Studio_ sont écrites avec [Angular](https://angular.io/) en [Typescript](https://www.typescriptlang.org/).

Des intégrations [React](https://reactjs.org) et [Flutter](https://flutter.dev/) sont fournies pour les interfaces Web et Mobile.

## Structure des sources

### Les dépôts

* [`tock`](https://github.com/theopenconversationkit/tock) : dépôt principal comprend le framework et les composants 
de la plateforme sous [licence Apache 2](https://github.com/theopenconversationkit/tock/blob/master/LICENSE). 

* [`tock-corenlp`](https://github.com/theopenconversationkit/tock-corenlp) : code utilisant une dépendance optionnelle à 
[Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/) (à la place d'[Apache OpenNLP](https://opennlp.apache.org/)), 
sous licence [GPL](https://fr.wikipedia.org/wiki/Licence_publique_g%C3%A9n%C3%A9rale_GNU). 

* [`tock-docker`](https://github.com/theopenconversationkit/tock-docker) : des images [Docker](https://www.docker.com/) 
et [Docker Compose](https://docs.docker.com/compose/), pour faciliter la prise en main et le déploiement de la plateforme dans différentes configurations.

* [`tock-bot-samples`](https://github.com/theopenconversationkit/tock-bot-samples) : des exemples de code notamment pour programmer des parcours en mode _WebHook_ ou _WebSocket_ 
comme dans les [guides Tock](../guides/api.md).
 
* [`tock-bot-open-data`](https://github.com/theopenconversationkit/tock-bot-open-data) : un exemple de bot basé 
sur les [API _Open Data_ de la SNCF](https://www.digital.sncf.com/startup/api), implémentant également des bases pour 
l'internationalisation avec deux langues proposées.

### Le dépôt `tock`

Voici une première description des sources dans le dépôt `tock` :

* `bot` : la plateforme conversationnelle (interfaces, API, connecteurs, etc.), en dépendance sur les modules _NLU_
* `docs` : le sites de documentation, générés avec MkDocs
* `docs-mk` : les sources pour les sites de documentation, pour MkDocs
* `dokka` : la documentation Dokka du framework Kotlin
* `etc` : des scripts utilitaires, par exemple pour générer les sites avec MkDocs
* `nlp` : la plateforme _NLU_ uniquement (interfaces, API, modèles d'entités, etc.) 
* `scripts` : d'autres scripts utilitaires, par exemple pour développer sur Messenger avec ngrok
* `shared` : des composants Kotlin partagés entre les différents modules du framework
* `stt` : des implémentations et wrappers pour le _speech-to-text_
* `translator` : des implémentations et wrappers pour le multilingue (_i18n_)

Remarque : il existe "deux _admin_" (ie. deux interfaces _Tock Studio_) dans les sources. En effet, il est possible 
d'installer la plateforme _NLU_ / _NLP_ seule sans les outils conversationnels. En conséquence :

* `nlp/admin` : contient les composants et interfaces graphiques pour le _NLU_ / _NLP_ seulement
* `bot/admin` : reprend les composants _NLP_ / _NLU_ (en dépendance dans le build Maven) et reconstruit les interfaces 
en y ajoutant les outils conversationnels  

### Le dépôt `tock-docker`

Le dépôt contient une structure de modules Maven reprenant les différents composants de la plateforme Tock. 
Chacun de ces modules porte une implémentation Docker du composant en s'appuie sur le plugin Maven 
`io.fabric8:docker-maven-plugin` pour encapsuler le build Docker.

A la racine du dépôt se trouvent différents descripteurs Docker Compose permettant de déployer une plateforme 
en se basant sur les images déjà construites. Différentes configurations existent, notamment en mode _Bot API_ 
ou en mode _intégré_, avec la plateforme _NLU_ standalone, etc. Le descripteur de référence pour le mode 
_Bot API_ est `docker-compose-bot.yml`.

## Build & run

### Construire Tock à partir des sources

#### Tock (core)

Le projet est construit avec [Maven](https://maven.apache.org/), y compris les modules Web impliquant 
[NPM](https://www.npmjs.com/) et [Angular](https://angular.io/) :
 
`$ mvn package`

Un build d'intégration continue est disponible sur [Travis](https://travis-ci.org/theopenconversationkit/tock).

#### Images Docker

Les images Docker de Tock peuvent être reconstruites à partir des sources du dépôt [`tock-docker`](https://github.com/theopenconversationkit/tock-docker).
Pour cela, utilisez [Maven](https://maven.apache.org/) qui déclenchera le build [Docker](https://www.docker.com/) :

`$ mvn package docker:build`

Vous pouvez ensuite instancier ces images via Docker ou les stacks Docker Compose avec les descripteurs à la racine du dépôt.

### Exécuter dans un IDE

> Pour démarrer Tock avec Docker Compose hors d'un IDE, voir [Déployer Tock avec Docker](../guides/platform.md).

Les différents composants Tock peuvent s'exécuter depuis un IDE (environnement de développement intégré). 
Des configurations sont fournies pour [IntelliJ](https://www.jetbrains.com/idea/).

Voir la section [Installation Tock](../admin/installation.md).  

Pour exécuter le bot/exemple en mode intégré, une configuration est aussi disponible : [OpenDataBot](https://github.com/theopenconversationkit/tock-bot-open-data/blob/master/.idea/runConfigurations/OpenDataBot.xml).

## Code

### Commits & merge requests

Pour soumettre une évolution ou un correctif :

1. [Créer une _issue_](https://github.com/theopenconversationkit/tock/issues/new):
    - Format recommandé pour le titre :
        - `[Component] Title`
        - De préférence en anglais
        - Composant : par exemple 
    _Studio_, _Core_, _Doc_, etc.
        - Titre : par exemple _Do or fix something_
2. [Créer une _pull request_](https://github.com/theopenconversationkit/tock/pulls) et la lier à l'_issue_:
    - Tous les commits doivent être [_signés_](https://help.github.com/en/github/authenticating-to-github/managing-commit-signature-verification) 
    - SVP _rebase_ ou _squash_ les commits superflus
        - Astuce : vous pouvez noter la PR comme _Draft_ avant de la soumettre
    - Format recommandé pour le nom de la branche :
        - `ISSUEID_short_title`
    - Format recommandé pour le(s) message(s) de commit(s) :
        - `resolves #ISSUEID Component: title` pour les évolutions
        - `fixes #ISSUEID Component: title` pour les correctifs
3. Avant d'être intégrée, une _pull request_ doit passer les tests et être approuvée par au moins deux de ces développeurs :
    - [@vsct-jburet](https://github.com/vsct-jburet),
    [@francoisno](https://github.com/francoisno),
    [@NainJaune](https://github.com/NainJaune),
    [@elebescond](https://github.com/elebescond),
    [@SarukaUsagi](https://github.com/SarukaUsagi),
    [@MaximeLeFrancois](https://github.com/MaximeLeFrancois),
    [@bakic](https://github.com/bakic),
    [@broxmik](https://github.com/broxmik),
    [@mrboizo](https://github.com/mrboizo)

### Conventions de code

Les [Kotlin Code Conventions](https://kotlinlang.org/docs/reference/coding-conventions.html) sont utilisées pour 
développer le code de Tock.

### Tests unitaires

Chaque nouvelle évolution ou correctif devrait embarquer ses tests unitaires.

## Nous contacter

_Un problème ? Une question sur l'implémentation ? Une idée à partager ?_

Pour contribuer au projet ou simplement en savoir plus, n'hésitez pas à [nous contacter](contact.md).
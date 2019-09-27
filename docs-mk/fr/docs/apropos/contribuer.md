# Contribuer à Tock

Le projet Tock est ouvert à la contribution et toute proposition est la bienvenue !

Cette page donne des indications sur la structure et les conventions du code de la plateforme.

## Principales technologies

La plateforme applicative est la [JVM](https://fr.wikipedia.org/wiki/Machine_virtuelle_Java).

Le langage de référence est [Kotlin](https://kotlinlang.org/) mais d'autres langages de programmation peuvent être utilisés via les API mises à disposition.
 
Tock utilise [Vert.x](http://vertx.io/) et [MongoDB](https://www.mongodb.com ).

> Pour l’instant les applications sont 
essentiellement développées en _blocking IO_, mais pour la suite l’approche _[fibers](http://docs.paralleluniverse.co/quasar/)_ sera privilégiée.

Les interfaces graphiques _(Tock Studio)_ sont écrites avec [Angular](https://angular.io/) en [Typescript](https://www.typescriptlang.org/).

## Structure des sources

### Les dépôts

* [`tock`](https://github.com/theopenconversationkit/tock) : dépôt principal comprend le framework et les composants 
de la plateforme sous [licence Apache 2](https://github.com/theopenconversationkit/tock/blob/master/LICENSE). 

* [`tock-corenlp`](https://github.com/theopenconversationkit/tock-corenlp) : code utilisant une dépendance optionnelle à 
[Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/) (à la place d'[Apache OpenNLP](https://opennlp.apache.org/)), 
sous licence [GPL](https://fr.wikipedia.org/wiki/Licence_publique_g%C3%A9n%C3%A9rale_GNU). 

* [`tock-docker`](https://github.com/theopenconversationkit/tock-docker) : des images [Docker](https://www.docker.com/) 
et [Docker Compose](https://docs.docker.com/compose/), pour faciliter la prise en main et le déploiement de la plateforme dans différentes configurations.

* [`tock-bot-samples`]() : des exemples de code notamment pour programmer des parcours en mode _WebHook_ ou _WebSocket_ 
comme dans les [guides Tock](../guide/api.md).
 
* [`tock-bot-open-data`](https://github.com/theopenconversationkit/tock-bot-open-data) : un exemple de bot basé 
sur les [API _Open Data_ de la SNCF](https://www.digital.sncf.com/startup/api), implémentant également des bases pour 
l'internationalisation avec deux langues proposées.

### Le dépôt `tock`

> TODO : détailler les modules et la structure du repo

### Le dépôt `tock-docker`

> TODO : détailler les modules et la structure du repo, le fonctionnement des builds Maven et Docker, etc.

## Construire Tock à partir des sources

### Tock (core)

Le projet est construit avec [Maven](https://maven.apache.org/), y compris les modules Web impliquant 
[NPM](https://www.npmjs.com/) et [Angular](https://angular.io/) :
 
`$ mvn package`

Un build d'intégration continue est disponible sur [Travis](https://travis-ci.org/voyages-sncf-technologies/tock).

### Images Docker

Les images Docker de Tock peuvent être reconstruites à partir des sources du dépôt [`tock-docker`](https://github.com/theopenconversationkit/tock-docker).
Pour cela, utilisez [Maven](https://maven.apache.org/) qui déclenchera le build [Docker](https://www.docker.com/) :

`$ mvn docker:build`

Vous pouvez ensuite instancier ces images via Docker ou les stacks Docker Compose avec les descripteurs à la racine du dépôt.

## Exécuter dans un IDE

> Pour démarrer Tock avec Docker Compose hors d'un IDE, voir [Déployer Tock avec Docker](../guide/plateforme.md).

Il est possible d'exécuter les différents composants de Tock (NLU, Studio, bot...) depuis un IDE comme 
[IntelliJ](https://www.jetbrains.com/idea/), [Eclipse](https://www.eclipse.org/) ou [Visual Studio Code](https://code.visualstudio.com/) par exemple.

Outre les [images Docker](https://github.com/theopenconversationkit/tock-docker/blob/master/docker-compose.yml),
des configurations pour IntelliJ sont fournies avec les sources de Tock :

- Le serveur d'administration du bot : [BotAdmin](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/BotAdmin.xml) 
- Le serveur d'administration du NLP uniquement : [Admin](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/Admin.xml) 
- Le service NLP : [NlpService](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/NlpService.xml)
- Le service Duckling : [Duckling](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/Duckling.xml)
- Le service de construction des modèles NLP : [BuildWorker](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/BuildWorker.xml)
- Le service de compilation des scripts : [KotlinCompilerServer](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/KotlinCompilerServer.xml)

Pour le bot d'exemple :

- [OpenDataBot](https://github.com/theopenconversationkit/tock-bot-open-data/blob/master/.idea/runConfigurations/OpenDataBot.xml)

Enfin pour lancer les interfaces d'administration, vous aurez besoin de lancer les commandes décrites dans les liens suivants :

- [Pour l'administration complète (Bot + NLP)](https://github.com/theopenconversationkit/tock/blob/master/bot/admin/web/README.md)
- [Pour l'administration NLP uniquement](https://github.com/theopenconversationkit/tock/blob/master/nlp/admin/web/README.md)

## Conventions

Les [Kotlin Code Conventions](https://kotlinlang.org/docs/reference/coding-conventions.html) sont utilisées pour 
développer le code de Tock.

## Nous contacter

_Un problème ? Une question sur l'implémentation ? Une idée à partager ?_

Pour contribuer au projet ou simplement en savoir plus, n'hésitez pas à [nous contacter](contact.md).
# Contribuer au projet tock

## Construire le projet

Le projet utilise [Maven](https://maven.apache.org/). 
 
``` mvn package ```

Un build d'intégration continue est disponible sur [Travis](https://travis-ci.org/voyages-sncf-technologies/tock).

## Lancer le projet  dans l'IDE

Outre les [images docker](https://github.com/voyages-sncf-technologies/tock-docker/blob/master/docker-compose.yml),
des configurations IntelliJ sont disponibles :

- Le serveur d'administration du bot : [BotAdmin](https://github.com/voyages-sncf-technologies/tock/blob/master/.idea/runConfigurations/BotAdmin.xml) 
- Le serveur d'administration du NLP uniquement : [Admin](https://github.com/voyages-sncf-technologies/tock/blob/master/.idea/runConfigurations/Admin.xml) 
- Le service NLP : [NlpService](https://github.com/voyages-sncf-technologies/tock/blob/master/.idea/runConfigurations/NlpService.xml)
- Le service Duckling : [Duckling](https://github.com/voyages-sncf-technologies/tock/blob/master/.idea/runConfigurations/Duckling.xml)
- Le service de construction des modèles NLP : [BuildWorker](https://github.com/voyages-sncf-technologies/tock/blob/master/.idea/runConfigurations/BuildWorker.xml)
- Le service de compilation des scripts : [KotlinCompilerServer](https://github.com/voyages-sncf-technologies/tock/blob/master/.idea/runConfigurations/KotlinCompilerServer.xml)

Pour le bot d'exemple :

- [OpenDataBot](https://github.com/voyages-sncf-technologies/tock-bot-open-data/blob/master/.idea/runConfigurations/OpenDataBot.xml)

Enfin pour lancer les interfaces d'administration, vous aurez besoin de lancer les commandes décrites dans les liens suivants :

- [Pour l'administration complète (Bot + NLP)](https://github.com/voyages-sncf-technologies/tock/blob/master/bot/admin/web/README.md)
- [Pour l'administration NLP uniquement](https://github.com/voyages-sncf-technologies/tock/blob/master/nlp/admin/web/README.md)

## Code conventions

Le format à suivre est décrit dans les [Kotlin Code Conventions](https://kotlinlang.org/docs/reference/coding-conventions.html)

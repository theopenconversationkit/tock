# Contribuer au projet tock

## Construire le projet

Le projet utilise [Maven](https://maven.apache.org/). 

Il est nécessaire d'utiliser un jdk8 lors du build
 (les jvm de 8 à 10 sont supportées à l'exécution).
 
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

Enfin pour lancer les interfaces d'administration, vous aurez besoin d'un *node v6*
et de lancer les commandes suivantes dans les répertoires

- pour l'administration complètre : bot/admin/web
- pour l'administration NLP uniquement : nlp/admin/web

```
npm install
ng serve
```

## Code conventions

The format to follow is described in [Kotlin Code Conventions](https://kotlinlang.org/docs/reference/coding-conventions.html)

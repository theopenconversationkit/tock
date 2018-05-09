# Contribute to the Tock project

## Build the project

The project uses [Maven](https://maven.apache.org/).

Jdk8 is required during the build (jvm from 8 to 10 are supported at runtime).

``` mvn package ```

A CI build is available on [Travis](https://travis-ci.org/voyages-sncf-technologies/tock).

## Start the project in the IDE

In addition to [docker images](https://github.com/voyages-sncf-technologies/tock-docker/blob/master/docker-compose.yml),
the project provides IntelliJ configurations:

- The bot administration server: [BotAdmin](https://github.com/voyages-sncf-technologies/tock/blob/master/.idea/runConfigurations/BotAdmin.xml) 
- The NLP administration server only: [Admin](https://github.com/voyages-sncf-technologies/tock/blob/master/.idea/runConfigurations/Admin.xml) 
- The NLP service: [NlpService](https://github.com/voyages-sncf-technologies/tock/blob/master/.idea/runConfigurations/NlpService.xml)
- The Duckling service: [Duckling](https://github.com/voyages-sncf-technologies/tock/blob/master/.idea/runConfigurations/Duckling.xml)
- Build engine for NLP models: [BuildWorker](https://github.com/voyages-sncf-technologies/tock/blob/master/.idea/runConfigurations/BuildWorker.xml)
- To manage script compilation at runtime: [KotlinCompilerServer](https://github.com/voyages-sncf-technologies/tock/blob/master/.idea/runConfigurations/KotlinCompilerServer.xml)

And for the open data bot:

- [OpenDataBot](https://github.com/voyages-sncf-technologies/tock-bot-open-data/blob/master/.idea/runConfigurations/OpenDataBot.xml)

In order to launch the administration front interfaces, you will need a *node v6*
and use the following commands in the directories

- for bot and NLP administration interface: bot/admin/web
- for NLP administration only: nlp/admin/web

```
npm install
ng serve
```

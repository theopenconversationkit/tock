# Contribute to the Tock project

## Build the project

The project uses [Maven](https://maven.apache.org/).

``` mvn package ```

A CI build is available on [Travis](https://travis-ci.org/theopenconversationkit/tock).

## Start the project in the IDE

In addition to [docker images](https://github.com/theopenconversationkit/tock-docker/blob/master/docker-compose.yml),
the project provides IntelliJ configurations:

- The bot administration server: [BotAdmin](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/BotAdmin.xml) 
- The NLP administration server only: [Admin](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/Admin.xml) 
- The NLP service: [NlpService](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/NlpService.xml)
- The Duckling service: [Duckling](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/Duckling.xml)
- Build engine for NLP models: [BuildWorker](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/BuildWorker.xml)
- To manage script compilation at runtime: [KotlinCompilerServer](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/KotlinCompilerServer.xml)

And for the open data bot:

- [OpenDataBot](https://github.com/theopenconversationkit/tock-bot-open-data/blob/master/.idea/runConfigurations/OpenDataBot.xml)

In order to launch the administration front interfaces, please consult the README files:

- [For bot and NLP administration interface](https://github.com/theopenconversationkit/tock/blob/master/bot/admin/web/README.md)
- [For NLP administration only](https://github.com/theopenconversationkit/tock/blob/master/nlp/admin/web/README.md)

## Code conventions

Conventions are described in [Kotlin Code Conventions](https://kotlinlang.org/docs/reference/coding-conventions.html)


---
title: Supervision
---

# Supervision

Ce chapitre présente quelques aspects supervision et _monitoring_ du fonctionnement de la plateforme 
et des bots Tock.

> A venir : plus de détails sur la manière de monitorer les bots, voire des exemples de dashboards pour quelques 
>technologies de monitoring classiques. N'hésitez pas à partager les vôtres.

## Lignes de vie (healthchecks)

L'url `/healthcheck` renvoie une code `HTTP 200` si tout est correct.

Pour certaines images, le ligne de vie peut ne pas être présente à la racine. En particulier :
 
- Pour `tock/admin`, la ligne de vie est localisée par défaut dans `/rest/admin/healthcheck` 
- Pour `tock/nlp_api` , la ligne de vie est `/rest/nlp/healthcheck` 
 
## Journalisation (logs)

### Logs applicatifs

Tock utilise [SLF4J](http://www.slf4j.org) et [Logback](http://logback.qos.ch/) pour générer ses logs applicatifs côté serveur.

Par défaut Tock configure automatiquement ses logs et quelques propriétés permettent de modifier la configuration.

Il est possible de configurer finement les logs en fonction des besoins, notamment avec des fichiers de 
configuration Logback ou avec Docker Compose.

#### Auto-configuration

Par défaut, en l'absence de configuration spécifique, Tock configure ses logs automatiquement grâce à la classe 
[`LogbackConfigurator`](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/LogbackConfigurator.kt).
Celle-ci configure [Logback](http://logback.qos.ch/) programmatiquement, avec le comportement suivant :

- Niveau de log général `DEBUG` si `tock_env=dev` (par défaut), ou `INFO` pour les autres environnements et la production
    - Exception pour les logs `org.mongodb.driver` toujours à `INFO`
    - Exception pour les logs `io.netty` toujours à `INFO`
    - Exception pour les logs `okhttp3` toujours à `INFO`
    - Exception pour les logs `io.mockk` toujours à `INFO`
- Logs dirigés vers la console (sortie standard) par défaut, mais possibilité d'écrire dans des fichiers
    - En mode logs fichiers, écriture dans `log/logFile.log`, archivage d'un fichier chaque jour 
    (`log/logFile.%d{yyyy-MM-dd}.log`), maximum `30` jours ou `3GB` de logs conservés

> Pour en savoir plus sur la configuration automatiques des logs Tock, se référer à l'implémentation 
[`LogbackConfigurator`](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/LogbackConfigurator.kt).

Des variables d'environnement permettent de configurer ces différents modes de journalisation. 
Elles peuvent être définies indépendamment sur chaque composant produisant des logs.

| Variable d'environnement      | Valeur par défaut                            | Description |
|-------------------------------|----------------------------------------------|-------------|
| `tock_env`                    | `dev`                                        | Environnement (attention : contrôle d'autres mécanismes que les logs). |
| `tock_logback_enabled`        | `true`                                       | Activation des logs. |
| `tock_default_log_level`      | `DEBUG` si `tock_env=dev` (autrement `INFO`) | Niveau de log général (hors exceptions, voir plus haut). |
| `tock_logback_file_appender`  | `false`                                      | Logs fichiers (voir détails plus haut) à la place des logs console (sortie standard). |

Selon le mode de déploiement utilisé, ces variables d'environnement peuvent être ajoutées soit 
directement en ligne de commande, soit dans un descripteur type `docker-compose.yml`, `dockerrun.aws.json` ou autre.

Voici un exemple configurant les logs de Tock Studio (`admin_web`) dans Docker :

=== "docker-compose.yml"

    ```yaml hl_lines="6 7"
    version: '3'
    services:
      admin_web:
        image: tock/bot_admin:$TAG
        environment:
        - tock_default_log_level=WARN
        - tock_logback_file_appender=true
    ```

=== "dockerrun.aws.json"

    ```json hl_lines="8 9"
    {
      "AWSEBDockerrunVersion": 2,
      "containerDefinitions": [
        {
          "name": "admin_web",
          "image": "tock/bot_admin:${TAG}",
          "environment": [
            { "name": "tock_default_log_level", "value": "WARN" },
            { "name": "tock_logback_file_appender", "value": "true" }
          ]
        }
      ]
    }
    ```

#### Fichiers Logback

Il est possible de configurer finement les logs Tock en configurant directement [Logback](http://logback.qos.ch/).
Pour cela, différentes possibilités existent, notamment des fichiers de configuration en format XML ou Groovy. 
Ci-dessous un exemple de configuration :

=== "logback.xml"

    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <configuration debug="false">
    
        <logger name="ai.tock" level="DEBUG" />
    
        <logger name="org.mongodb" level="WARN" />
    
        <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>
                    %d %-5level [%thread] %logger{1}: %msg%n
                </pattern>
            </encoder>
        </appender>
    
        <root level="INFO">
            <appender-ref ref="console" />
        </root>
    
    </configuration>
    ```

=== "logback.groovy"

    ```groovy
    import ch.qos.logback.classic.encoder.PatternLayoutEncoder
    import ch.qos.logback.core.ConsoleAppender
    
    import static ch.qos.logback.classic.Level.DEBUG
    import static ch.qos.logback.classic.Level.INFO
    import static ch.qos.logback.classic.Level.WARN
    
    appender("console", ConsoleAppender) {
      encoder(PatternLayoutEncoder) {
        pattern = "%d %-5level [%thread] %logger{1}: %msg%n"
      }
    }
    logger("ai.tock", DEBUG)
    logger("org.mongodb", WARN)
    root(INFO, ["console"])
    ```

Dans cet exemple :

- Le niveau de log général est `INFO`, avec des exceptions :
    - Exception pour les logs `ai.tock` toujours à `DEBUG`
    - Exception pour les logs `org.mongodb` toujours à `WARN`
- Les logs sont dirigés vers la console (sortie standard)

De nombreuses possibilités sont offertes pour configurer les logs grâce à ces fichiers de configuration :
niveaux de logs ajustés en fonction des packages/frameworks embarqués, modification du pattern par défaut, 
journalisation de logs fichiers et archivage auto avec un 
[`RollingFileAppender`](http://logback.qos.ch/manual/appenders.html#RollingFileAppender), redirection des logs 
vers un serveur de base de données ou email, etc.
Pour en savoir plus, se référer à la [documentation Logback](http://logback.qos.ch/manual/configuration.html).

Une fois le fichier de configuration Logback créé, il faut s'assurer que celui-ci est dans la _classpath_ du 
composant Tock et que Logback l'identifie comme la configuration à suivre (pour cela on utilise généralement 
la propriété ` -Dlogback.configurationFile` au démarrage du composant Java).

Ci-dessous un exemple complet dans Docker Compose avec :

- Un fichier `logback.xml` embarqué par Maven (`pom.xml`) dans l'image Docker
- Quelques variables d'environnement définies pour pouvoir rapidement ajuste les principaux niveaux de log 
directement dans Docker-Compose (sans avoir à modifier le fichier XML ni l'image Docker)'

=== "logback.xml"

    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <configuration debug="false">

        <!-- Leverage env variables with defaults, for more flexibility -->
        <variable name="tock_default_log_level" value="${tock_default_log_level:-WARN}" />
        <variable name="tock_service_log_level" value="${tock_service_log_level:-INFO}" />
        <variable name="tock_database_log_level" value="${tock_database_log_level:-WARN}" />
    
        <logger name="ai.tock" level="${tock_service_log_level}" />
        <logger name="org.mongodb" level="${tock_database_log_level}" />
    
        <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>
                    %d %-5level [%thread] %logger{1}: %msg%n
                </pattern>
            </encoder>
        </appender>
    
        <root level="${tock_default_log_level}">
            <appender-ref ref="console" />
        </root>
    
    </configuration>
    ```

=== "pom.xml"

    ```xml hl_lines="21 22 23 24 29"
    <?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>
        <artifactId>tock-docker-bot-admin</artifactId>
        <build>
            <plugins>
                <plugin>
                    <groupId>io.fabric8</groupId>
                    <artifactId>docker-maven-plugin</artifactId>
                    <configuration>
                        <images>
                            <image>
                                <name>tock/bot_admin:${project.version}</name>
                                <build>
                                    <assembly>
                                        <inline>
                                            <dependencySets>
                                                ...
                                            </dependencySets>
                                            <files>
                                                <file>
                                                    <source>logback.xml</source>
                                                    <outputDirectory>.</outputDirectory>
                                                </file>
                                            </files>
                                        </inline>
                                    </assembly>
                                    <cmd>
                                        <shell>java $JAVA_ARGS -Dlogback.configurationFile='file:///maven/logback.xml' -Dfile.encoding=UTF-8 -Dtock_nlp_model_refresh_rate=10 -cp '/maven/*' ai.tock.bot.admin.StartBotAdminServerKt</shell>
                                    </cmd>
                                    ...
    ``` 

=== "docker-compose.yml"

    ```yaml hl_lines="6 7 8"
    version: '3'
    services:
      admin_web:
        image: tock/bot_admin:$TAG
        environment:
        - tock_default_log_level=WARN # Default, see logback.xml
        - tock_service_log_level=INFO # Default, see logback.xml
        - tock_database_log_level=WARN # Default, see logback.xml
    ```

> Le code complet de cet exemple appliqué à tous les modules Tock est disponible dans le dépôt `tock-docker` 
> sur la branche [`logbackxml`](https://github.com/theopenconversationkit/tock-docker/tree/logbackxml).

#### Docker Compose

Pour les utilisateurs de [Docker Compose](), un mécanisme permet de configurer les logs directement dans les 
descripteurs en YAML. Voir le dépot [`tock-docker`](https://github.com/theopenconversationkit/tock-docker) pour un 
exemple d'implémentation de Tock dans Docker Compose.

Chaque composant applicatif peut avoir sa propre configuration :

=== "docker-compose.yml"

    ```yaml
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "5"
    ```

L'exemple ci-dessus configure une rotation automatique des fichiers de logs, de manière à avoir maximum 
`5` fichiers de logs de maximum `10 Mo` chacun (le plus ancien étant supprimé pour en créer un nouveau si besoin).

Pour en savoir plus, voir la [documentation Docker Compose](https://docs.docker.com/compose/compose-file/#logging) de ce mécanisme.

### Chiffrement et anonymisation

Voir la page [sécurité](securite.md) concernant les possibilités de chiffrement et anonymisation des logs.

---
title: Supervision
---
[//]: # (Traduit avec Google Translate et Reverso)
# Supervision

This chapter presents some supervision and _monitoring_ aspects of the functioning of the platform
and Tock bots.

> Coming soon: more details on how to monitor bots, even examples of dashboards for some
>classic monitoring technologies. Feel free to share yours.

## Healthchecks

The APIs of the different Tock components include different probes or lifelines (_healthchecks_) to check if everything is working properly.

These lifelines can be used by automated monitoring systems.

### Probes & paths

Each `WebVerticle` exposes 3 probes returning the `HTTP 200` code if everything is working.

| Default path | Description | Property to modify the path |
|---------------------|-------------------------------------------------------------------------|-------------------|
| `/healthcheck` | The component is working properly. _(Detailed mode: see below)_ | `tock_vertx_healthcheck_path` |
| `/health/readiness` | The component is ready to process requests. | `tock_vertx_readinesscheck_path` |
| `/health/liveness` | The component is started. | `tock_vertx_livenesscheck_path` |

For some components and images, the lifeline cannot be exposed directly at the root, the path is
modified. In particular:

- For `tock/admin`, the lifeline is located by default in `/rest/admin/healthcheck`
- For `tock/nlp_api`, the lifeline is `/rest/nlp/healthcheck`

Each default path can be modified with a dedicated property (see table above).

### Detailed mode

The main lifeline `/healthcheck` can perform a more detailed inspection (ie. usually check
the connection to other components) if the `tock_detailed_healthcheck_enabled` property is enabled.

The lifeline response then specifies the components checked.

Here is an example of activation in the `bot_admin` Docker image:

- {: data-hl-lines="6"} docker-compose.yml
```yaml
    version: '3'
    services:
      admin_web:
        image: tock/bot_admin:$TAG
        environment:
        - tock_detailed_healthcheck_enabled=true
```

- {: data-hl-lines="8"} dockerrun.aws.json
```json
  {
    "AWSEBDockerrunVersion": 2,
    "containerDefinitions": [
      {
        "name": "admin_web",
        "image": "tock/bot_admin:${TAG}",
        "environment": [
          { "name": "tock_detailed_healthcheck_enabled", "value": "true" }
        ]
      }
    ]
  }
```
{: .tabbed-code}

Example of a lifeline response in detailed mode:

```json
  {
    "results": [
      {
        "id": "duckling_service",
        "status": "OK"
      },
      {
        "id": "tock_front_database",
        "status": "OK"
      },
      {
        "id": "tock_model_database",
        "status": "OK"
      },
      {
        "id": "tock_bot_database",
        "status": "OK"
      }
    ]
  }
```

> See below for a summary of the lifelines component by component.

### Details by component

The table below details the path and checks performed by the main lifeline component
by component, in normal and detailed mode:

| Component / Image | Default Path | Default Check | Detailed Check (`tock_detailed_healthcheck_enabled=true`) |
|------------------------------------|------------------------------------------------------|----------------------------------------------------------------------|-------------------------------------------------------------------------|
| NLP | `/rest/nlp/healthcheck` | Duckling / entity providers OK. | `front` and `model` databases OK. |
| Duckling / RestEntityProvider | `/healthcheck` | Duckling bridge initialized. | _Idem_ |
| Build Worker | `/healthcheck` | Worker ready to analyze the model. | `front` and `model` databases OK. |
| Bot / Bot Api | `/healthcheck` | Bot installed, connected to the database, NLP OK. | _Idem_ |
| WebHook (Bot Api) | `/healthcheck` | OK | _Idem_ |
| Kotlin Compile | `/healthcheck` | OK | _Idem_ |
| NLP / Bot Admin | `/rest/admin/healthcheck` | OK | Duckling / entity providers OK, `front` `model` and `bot` databases OK. |

Below are examples of responses from different components in verbose mode:

- NLP
```json
  {
    "results": [
      {
        "id": "duckling_service",
        "status": "OK"
      },
      {
        "id": "tock_front_database",
        "status": "OK"
      },
      {
        "id": "tock_model_database",
        "status": "OK"
      }
    ]
  }
```

- Duckling
```json
  {
    "results": [
      {
        "id": "duckling_bridge",
        "status": "OK"
      }
    ]
  }
```

- Build Worker
```json
  {
    "results": [
      {
        "id": "tock_front_database",
        "status": "OK"
      },
      {
        "id": "tock_model_database",
        "status": "OK"
      }
    ]
  }
```

- Bot Api
```json
  {
    "results": [
      {
        "id": "nlp_client",
        "status": "OK"
      }
    ]
  }
```

- Kotlin Compiler
```json
 {
  "results": []
 }
```

- Bot Admin
```json
  {
    "results": [
      {
        "id": "duckling_service",
        "status": "OK"
      },
      {
        "id": "tock_front_database",
        "status": "OK"
      },
      {
        "id": "tock_model_database",
        "status": "OK"
      },
      {
        "id": "tock_bot_database",
        "status": "OK"
      }
    ]
  }
```
{:.tabbed-code}

### Lifeline Monitoring

The different probes and lifelines can be used to configure monitoring systems and other container orchestrators, for example, to measure service availability, trigger alerts or dynamically
remediate problems.

Depending on the tools and technologies used, lifeline configuration can be done in different ways.
Here are some examples for reference:

- At the level of the _Cloud_ or _on-premise_ load balancers (_load balancers_), for example:
- in the [_Health checks_ section at the _ELB_ level in AWS](https://docs.aws.amazon.com/autoscaling/ec2/userguide/as-add-elb-healthcheck.html)
- with the [`health_check` directive in NGINX](https://docs.nginx.com/nginx/admin-guide/load-balancer/http-health-check/)
- with the [`httpchk option` in HAProxy](https://www.haproxy.com/documentation/aloha/latest/traffic-management/lb-layer7/health-checks/#check-an-http-service)
- In _Docker_ (`Dockerfile` descriptors):
- with the [`health_check` instruction `HEALTHCHECK`](https://docs.docker.com/engine/reference/builder/#healthcheck) to check the
component status
- with [`ENTRYPOINT` or `CMD`](https://docs.docker.com/engine/reference/builder/#understand-how-cmd-and-entrypoint-interact)
to wait for the component to be ready at startup
- In _Docker Compose_ (`docker-compose.yml` descriptors):
- with [`healthcheck`](https://docs.docker.com/compose/compose-file/compose-file-v3/#healthcheck)
- In _Kubernetes_:
- with [`livenessProbe` and `readinessProbe`](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/)
- Etc.

## Logging

### Application logs

Tock uses [SLF4J](http://www.slf4j.org) and [Logback](http://logback.qos.ch/) to generate its server-side application logs.

By default, Tock automatically configures its logs and a few properties allow you to modify the configuration.

It is possible to finely configure the logs according to your needs, in particular with Logback configuration files or with Docker Compose.

#### Auto-configuration

By default, in the absence of specific configuration, Tock configures its logs automatically using the class
[`LogbackConfigurator`](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/LogbackConfigurator.kt).
This configures [Logback](http://logback.qos.ch/) programmatically, with the following behavior:

- General log level `DEBUG` if `tock_env=dev` (default), or `INFO` for other environments and production
- Exception for `org.mongodb.driver` logs always at `INFO`
- Exception for `io.netty` logs always at `INFO`
- Exception for `okhttp3` logs always at `INFO`
- Exception for `io.mockk` logs always at `INFO`
- Logs directed to the console (standard output) by default, but possibility to write to files
- In file log mode, writing to `log/logFile.log`, archiving a file every day
(`log/logFile.%d{yyyy-MM-dd}.log`), maximum `30` days or `3GB` of logs kept

> To learn more about automatic configuration of Tock logs, refer to the implementation
[`LogbackConfigurator`](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/LogbackConfigurator.kt).

Environment variables allow to configure these different logging modes.
They can be defined independently on each component producing logs.

| Environment variable | Default value | Description |
|------------------------------|----------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `tock_env` | `dev` | Environment (warning: controls other mechanisms than logs). |
| `tock_logback_enabled` | `true` | Enable logs. |
| `tock_default_log_level` | `DEBUG` if `tock_env=dev` (otherwise `INFO`) | General log level (except exceptions, see above). |
| `tock_retrofit_log_level` | `BASIC` if `tock_env=dev` (otherwise `NONE`) | Log level (requests and responses) for application client services using Retrofit (TockNlpClient, BotApiClient, and many connectors...) |
| `tock_logback_file_appender` | `false` | Logs files (see details above) instead of console logs (standard output). |

Depending on the deployment mode used, these environment variables can be added either
directly on the command line, or in a descriptor like `docker-compose.yml`, `dockerrun.aws.json` or other.

Here is an example configuring Tock Studio logs (`admin_web`) in Docker:

- {: data-hl-lines="6 7"} docker-compose.yml
```yaml
  version: '3'
  services:
    admin_web:
      image: tock/bot_admin:$TAG
      environment:
      - tock_default_log_level=WARN
      - tock_logback_file_appender=true
```

- {: data-hl-lines="8"} dockerrun.aws.json
```json
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
{: .tabbed-code}

#### Logback Files

It is possible to finely configure the Tock logs by directly configuring [Logback](http://logback.qos.ch/).
For this, different possibilities exist, in particular configuration files in XML or Groovy format. Below is an example configuration:

- logback.xml
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
- logback.groovy
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
{:.tabbed-code}

In this example:

- The general log level is `INFO`, with exceptions:
- Exception for `ai.tock` logs always at `DEBUG`
- Exception for `org.mongodb` logs always at `WARN`
- Logs are directed to the console (standard output)

Many possibilities are offered to configure the logs thanks to these configuration files:
log levels adjusted according to the embedded packages/frameworks, modification of the default pattern,
logging of log files and automatic archiving with a
[`RollingFileAppender`](http://logback.qos.ch/manual/appenders.html#RollingFileAppender), redirection of logs
to a database or email server, etc.
For more information, refer to the [Logback documentation](http://logback.qos.ch/manual/configuration.html).

Once the Logback configuration file has been created, you must ensure that it is in the _classpath_ of the
Tock component and that Logback identifies it as the configuration to follow (for this, you generally use
the `-Dlogback.configurationFile` property when starting the Java component).

Below is a complete example in Docker Compose with:

- A `logback.xml` file embedded by Maven (`pom.xml`) in the Docker image
- Some environment variables defined to be able to quickly adjust the main log levels
directly in Docker-Compose (without having to modify the XML file or the Docker image)'

^
- logback.xml
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
- {: data-hl-lines="21 22 23 24 29"} pom.xml

```xml
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
- {: data-hl-lines="6 7 8"} docker-compose.yml
```yaml
    version: '3'
    services:
      admin_web:
        image: tock/bot_admin:$TAG
        environment:
        - tock_default_log_level=WARN # Default, see logback.xml
        - tock_service_log_level=INFO # Default, see logback.xml
        - tock_database_log_level=WARN # Default, see logback.xml
```
{: .tabbed-code}

> The full code for this example applied to all Tock modules is available in the `tock-docker` repository
> on the [`logbackxml`](https://github.com/theopenconversationkit/tock-docker/tree/logbackxml) branch.

#### Docker Compose

For [Docker Compose](https://docs.docker.com/compose/) users, a mechanism allows to configure logs directly in YAML descriptors. See the [`tock-docker`](https://github.com/theopenconversationkit/tock-docker) repository for an
example of Tock implementation in Docker Compose.

Each application component can have its own configuration:

- docker-compose.yml
```yaml
logging:
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "5"
```
{: .tabbed-code}

The example above configures an automatic rotation of log files, so as to have a maximum of
`5` log files of maximum `10 MB` each (the oldest being deleted to create a new one if necessary).

For more information, see the [Docker Compose documentation](https://docs.docker.com/compose/compose-file/#logging) for this mechanism.

### Encryption and anonymization

See the [security](../admin/security.md) page for log encryption and anonymization options.